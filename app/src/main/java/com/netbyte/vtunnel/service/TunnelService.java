package com.netbyte.vtunnel.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.activity.MainActivity;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.thread.StatThread;
import com.netbyte.vtunnel.thread.VpnThread;
import com.netbyte.vtunnel.thread.WsThread;
import com.netbyte.vtunnel.utils.CipherUtil;
import com.netbyte.vtunnel.config.AppConst;

public class TunnelService extends VpnService {
    private static String serverIP, localIP;
    private static int serverPort;
    private static String dns;
    private static String key;
    private static String bypassUrl;
    private VpnThread wsThread;
    private StatThread statThread;
    private PendingIntent pendingIntent;
    private CipherUtil cipherUtil;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    public TunnelService() {
    }

    @Override
    public void onCreate() {
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        switch (intent.getAction()) {
            case AppConst.BTN_ACTION_CONNECT:
                // 0.init config
                initConfig(intent);
                // 1.create notification
                createNotification();
                // 2.connect
                doConnect();
                return START_STICKY;
            case AppConst.BTN_ACTION_DISCONNECT:
                doDisconnect();
                return START_NOT_STICKY;
            default:
                return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doDisconnect();
    }

    @Override
    public void onRevoke() {
        doDisconnect();
    }

    private void initConfig(Intent intent) {
        Bundle ex = intent.getExtras();
        String server = ex.getString("server").trim();
        if (server.contains(":")) {
            serverIP = server.split(":")[0];
            serverPort = Integer.valueOf(server.split(":")[1]);
        } else {
            serverIP = server;
            serverPort = AppConst.DEFAULT_SERVER_PORT;
        }
        dns = ex.getString("dns");
        key = ex.getString("key");
        bypassUrl = ex.getString("bypassUrl");
        localIP = AppConst.DEFAULT_LOCAL_ADDRESS;
        cipherUtil = new CipherUtil(key);
    }


    private void createNotification() {
        NotificationChannel channel = new NotificationChannel(AppConst.NOTIFICATION_CHANNEL_ID, AppConst.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        notificationBuilder = new NotificationCompat.Builder(this, channel.getId());
        notificationBuilder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(AppConst.APP_NAME)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true);
    }

    private void doConnect() {
        Log.i(AppConst.DEFAULT_TAG, "connecting " + serverIP + " " + serverPort + " " + localIP + " " + dns);
        try {
            stopThreads();
            startStatThread();
            startWsThread();
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, "error on connecting:" + e.toString());
        }
    }

    private void doDisconnect() {
        Log.i(AppConst.DEFAULT_TAG, "disconnecting...");
        stopThreads();
    }

    private void stopThreads() {
        if (wsThread != null) {
            wsThread.finish();
            wsThread = null;
        }
        if (statThread != null) {
            statThread.finish();
            statThread = null;
        }
    }

    private void startWsThread() {
        wsThread = new WsThread(serverIP, serverPort, dns, bypassUrl, cipherUtil, this);
        wsThread.start();
    }

    private void startStatThread() {
        statThread = new StatThread(serverIP, serverPort, key, notificationManager, notificationBuilder, this);
        statThread.start();
    }
}
