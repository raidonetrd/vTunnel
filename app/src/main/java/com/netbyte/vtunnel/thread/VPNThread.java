package com.netbyte.vtunnel.thread;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.model.Config;
import com.netbyte.vtunnel.model.Global;
import com.netbyte.vtunnel.model.LocalIP;
import com.netbyte.vtunnel.model.Stats;
import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.service.MyVPNService;
import com.netbyte.vtunnel.utils.CipherUtil;
import com.netbyte.vtunnel.ws.MyWebSocketClient;

import org.asynchttpclient.ws.WebSocket;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class VPNThread extends BaseThread {
    private static final String TAG = "VPNThread";
    private final Config config;
    private FileInputStream in = null;
    private FileOutputStream out = null;
    private ParcelFileDescriptor tun = null;
    private WebSocket webSocket = null;

    public VPNThread(Config config, MyVPNService vpnService, IPService ipService, NotificationManager notificationManager, NotificationCompat.Builder notificationBuilder) {
        this.config = config;
        this.vpnService = vpnService;
        this.ipService = ipService;
        this.notificationManager = notificationManager;
        this.notificationBuilder = notificationBuilder;
    }

    @Override
    public void run() {
        Global.START_TIME = System.currentTimeMillis();
        try {
            Log.i(TAG, "start");
            // pick ip
            LocalIP localIP = ipService.pickIp();
            if (localIP == null) {
                vpnService.stopVPN();
                return;
            }
            Global.LOCAL_IP = localIP.getLocalIP();
            // create tun
            tun = createTunnel(config, localIP);
            if (tun == null) {
                vpnService.stopVPN();
                return;
            }
            in = new FileInputStream(tun.getFileDescriptor());
            out = new FileOutputStream(tun.getFileDescriptor());
            // create ws client
            @SuppressLint("DefaultLocale") String uri = String.format("wss://%s:%d%s", config.getServerAddress(), config.getServerPort(), config.getPath());
            webSocket = MyWebSocketClient.connectWebSocket(uri, config.getKey(), config, out);
            if (webSocket == null || !webSocket.isOpen()) {
                Log.i(TAG, "webSocket is not open");
                vpnService.stopVPN();
                closeTun();
                return;
            }
            // start monitor threads
            MonitorThread monitorThread = new MonitorThread(vpnService, ipService);
            monitorThread.start();
            // start notify threads
            NotifyThread notifyThread = new NotifyThread(notificationManager, notificationBuilder, vpnService);
            notifyThread.start();
            // forward data
            byte[] buf = new byte[AppConst.BUFFER_SIZE];
            while (Global.RUNNING) {
                try {
                    int ln = in.read(buf);
                    if (ln <= 0) {
                        continue;
                    }
                    if (webSocket != null && webSocket.isOpen()) {
                        byte[] data = Arrays.copyOfRange(buf, 0, ln);
                        if (config.isObfuscate()) {
                            data = CipherUtil.xor(data, config.getKey().getBytes(StandardCharsets.UTF_8));
                        }
                        webSocket.sendBinaryFrame(data);
                        Stats.UPLOAD_BYTES.addAndGet(ln);
                    } else {
                        Log.i(TAG, "ws client is reconnecting...");
                        webSocket = MyWebSocketClient.connectWebSocket(uri, config.getKey(), config, out);
                        TimeUnit.MILLISECONDS.sleep(200);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error on WsThread:" + e.toString());
                }
            }
            Log.i(TAG, "stop");
        } catch (Exception e) {
            Log.e(TAG, "error on WsThread:" + e.toString());
        } finally {
            closeTun();
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
        appList.add(AppConst.APP_PACKAGE_NAME); // skip itself
        if (!TextUtils.isEmpty(config.getBypassApps())) {
            appList.addAll(Arrays.asList(config.getBypassApps().split(",")));
        }
        for (String packageName : appList) {
            builder.addDisallowedApplication(packageName);
        }
        Log.i(TAG, "bypass apps:" + appList);
        return builder.establish();
    }

    private void closeTun() {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.sendCloseFrame();
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
