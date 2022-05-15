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
import com.netbyte.vtunnel.model.LocalIp;
import com.netbyte.vtunnel.model.Stats;
import com.netbyte.vtunnel.service.IpService;
import com.netbyte.vtunnel.service.MyVpnService;
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

public class VpnThread extends BaseThread {
    private static final String TAG = "VPNThread";
    private final Config config;
    private FileInputStream in = null;
    private FileOutputStream out = null;
    private ParcelFileDescriptor tun = null;
    private WebSocket webSocket = null;

    public VpnThread(Config config, MyVpnService vpnService, IpService ipService, NotificationManager notificationManager, NotificationCompat.Builder notificationBuilder) {
        this.config = config;
        this.vpnService = vpnService;
        this.ipService = ipService;
        this.notificationManager = notificationManager;
        this.notificationBuilder = notificationBuilder;
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "start");
            // pick ip
            LocalIp localIP = ipService.pickIp();
            if (localIP == null) {
                vpnService.stopVpn();
                return;
            }
            Global.LOCAL_IP = localIP.getLocalIp();
            //pick ipv6
            LocalIp localIPv6 = ipService.pickIpv6();
            if (localIPv6 == null) {
                vpnService.stopVpn();
                return;
            }
            Global.LOCAL_IPv6 = localIPv6.getLocalIp();
            // create tun
            tun = createTunnel(config, localIP, localIPv6);
            if (tun == null) {
                vpnService.stopVpn();
                return;
            }
            in = new FileInputStream(tun.getFileDescriptor());
            out = new FileOutputStream(tun.getFileDescriptor());
            // create ws client
            @SuppressLint("DefaultLocale") String uri = String.format("%s://%s:%d%s", config.isWss() ? "wss" : "ws", config.getServerAddress(), config.getServerPort(), config.getPath());
            webSocket = MyWebSocketClient.connectWebSocket(uri, config.getKey(), config, out);
            if (webSocket == null || !webSocket.isOpen()) {
                Log.i(TAG, "webSocket is not open");
                vpnService.stopVpn();
                closeTun();
                return;
            }
            // start notify threads
            NotifyThread notifyThread = new NotifyThread(notificationManager, notificationBuilder, vpnService, ipService);
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
                        if (config.isObfs()) {
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

    private ParcelFileDescriptor createTunnel(Config config, LocalIp localIP, LocalIp localIPv6) throws PackageManager.NameNotFoundException {
        if (config == null || localIP == null || localIPv6 == null) {
            return null;
        }
        VpnService.Builder builder = vpnService.new Builder();
        builder.setMtu(AppConst.MTU)
                .addAddress(localIP.getLocalIp(), localIP.getLocalPrefixLength())
                .addAddress(localIPv6.getLocalIp(), localIPv6.getLocalPrefixLength())
                .addRoute(AppConst.DEFAULT_ROUTE, 0)
                .addRoute(AppConst.DEFAULT_ROUTEv6, 0)
                .addDnsServer(config.getDns())
                .setSession(AppConst.APP_NAME)
                .setConfigureIntent(null)
                .allowFamily(OsConstants.AF_INET)
                .allowFamily(OsConstants.AF_INET6)
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
