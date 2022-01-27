package com.netbyte.vtunnel.thread;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.model.Config;
import com.netbyte.vtunnel.model.Global;
import com.netbyte.vtunnel.model.LocalIP;
import com.netbyte.vtunnel.model.Stat;
import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.service.MyVPNService;
import com.netbyte.vtunnel.ws.WsClient;
import com.netbyte.vtunnel.utils.SSLUtil;
import com.netbyte.vtunnel.utils.CipherUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class WsThread extends BaseThread {
    private static final String TAG = "WsThread";
    private final Config config;

    public WsThread(Config config, MyVPNService vpnService, IPService ipService) {
        this.config = config;
        this.vpnService = vpnService;
        this.ipService = ipService;
    }

    @Override
    public void run() {
        WsClient wsClient = null;
        FileInputStream in = null;
        FileOutputStream out = null;
        ParcelFileDescriptor tun = null;
        try {
            Log.i(TAG, "start");
            // pick ip
            LocalIP localIP = ipService.pickIp();
            if (localIP == null) {
                vpnService.stopVPN();
                return;
            }
            Global.LOCAL_IP = localIP.getLocalIP();
            // create ws client
            @SuppressLint("DefaultLocale") String uri = String.format("wss://%s:%d/way-to-freedom", config.getServerAddress(), config.getServerPort());
            wsClient = new WsClient(new URI(uri), config);
            wsClient.setSocketFactory(SSLUtil.createEasySSLContext().getSocketFactory());
            wsClient.addHeader("key", config.getKey());
            wsClient.connectBlocking();
            // create tun
            tun = createTunnel(config, localIP);
            if (tun == null) {
                vpnService.stopVPN();
                return;
            }
            in = new FileInputStream(tun.getFileDescriptor());
            out = new FileOutputStream(tun.getFileDescriptor());
            wsClient.setOutStream(out);
            // forward data
            byte[] buf = new byte[AppConst.BUFFER_SIZE];
            while (Global.RUNNING) {
                try {
                    int ln = in.read(buf);
                    if (ln > 0) {
                        if (wsClient.isOpen()) {
                            byte[] data = Arrays.copyOfRange(buf, 0, ln);
                            if (config.isObfuscate()) {
                                data = CipherUtil.xor(data, config.getKey().getBytes(StandardCharsets.UTF_8));
                            }
                            wsClient.send(data);
                            Stat.UPLOAD_BYTES.addAndGet(ln);
                        } else if (wsClient.isClosed()) {
                            Log.i(TAG, "ws client is reconnecting...");
                            wsClient.reconnectBlocking();
                        } else {
                            TimeUnit.MILLISECONDS.sleep(100);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error on WsThread:" + e.toString());
                }
            }
            Log.i(TAG, "stop");
        } catch (Exception e) {
            Log.e(TAG, "error on WsThread:" + e.toString());
        } finally {
            if (wsClient != null) {
                wsClient.close();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tun != null) {
                try {
                    tun.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ParcelFileDescriptor createTunnel(Config config, LocalIP localIP) throws PackageManager.NameNotFoundException {
        if (config == null || localIP == null) {
            return null;
        }
        VpnService.Builder builder = vpnService.new Builder();
        builder.setMtu(AppConst.MTU)
                .addAddress(localIP.getLocalIP(), localIP.getLocalPrefixLength())
                .addRoute(AppConst.DEFAULT_ROUTE, 0)
                .addDnsServer(config.getDns())
                .setSession(AppConst.APP_NAME)
                .setConfigureIntent(null)
                .allowFamily(OsConstants.AF_INET)
                .setBlocking(true);
        // add apps to bypass
        ArrayList<String> appList = new ArrayList<>();
        appList.add(AppConst.APP_PACKAGE_NAME);
        if (!TextUtils.isEmpty(config.getBypassApps())) {
            appList.addAll(Arrays.asList(config.getBypassApps().split(",")));
        }
        for (String packageName : appList) {
            builder.addDisallowedApplication(packageName);
        }
        Log.i(TAG, "bypass apps:" + appList);
        return builder.establish();
    }

}
