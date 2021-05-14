package com.netbyte.vtunnel.thread;

import android.app.NotificationManager;
import android.net.VpnService;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.utils.ByteUtil;
import com.netbyte.vtunnel.utils.HttpUtil;

import java.io.IOException;

public class StatThread extends Thread {
    private static final String TAG = "StatThread";
    private volatile boolean THREAD_RUNNABLE = true;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder builder;
    private final VpnService vpnService;
    private String protocol;
    private String serverIP;
    private int serverPort;
    private String key;

    public StatThread(String protocol, String serverIP, int serverPort, String key, NotificationManager notificationManager, NotificationCompat.Builder builder, VpnService vpnService) {
        this.protocol = protocol;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.key = key;
        this.notificationManager = notificationManager;
        this.builder = builder;
        this.vpnService = vpnService;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void run() {
        Log.i(TAG, "start");
        vpnService.startForeground(AppConst.NOTIFICATION_ID, builder.build());
        int checkCount = 0;
        while (THREAD_RUNNABLE) {
            try {
                Thread.sleep(3000);
                if (checkCount > 5 && AppConst.DOWN_BYTE.get() == 0) {
                    String title = "Status: Failed to connect!";
                    builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title));
                } else {
                    String title = String.format("Local: %s", AppConst.LOCAL_ADDRESS);
                    String text = String.format("Network: ↑ %s ↓ %s", ByteUtil.format(AppConst.UP_BYTE.get()), ByteUtil.format(AppConst.DOWN_BYTE.get()));
                    builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(text));
                }
                notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
                checkCount++;
                if (AppConst.PROTOCOL_WS.equals(protocol) && checkCount % 100 == 0) {
                    keepAliveIp(AppConst.LOCAL_ADDRESS);
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "error:" + e.getMessage());
            }
        }
        AppConst.UP_BYTE.set(0);
        AppConst.DOWN_BYTE.set(0);
        AppConst.LOCAL_ADDRESS = "";
        vpnService.stopForeground(true);
        Log.i(TAG, "stop");
    }

    public void finish() {
        this.THREAD_RUNNABLE = false;
    }

    private void keepAliveIp(String ip) {
        String api = String.format("https://%s:%d/register/keepalive/ip?ip=%s", serverIP, serverPort, ip);
        String resp = "";
        try {
            resp = HttpUtil.get(api, "key", key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
    }

}
