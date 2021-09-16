package com.netbyte.vtunnel.service;

import android.annotation.SuppressLint;
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
import com.netbyte.vtunnel.model.Config;
import com.netbyte.vtunnel.thread.MonitorThread;
import com.netbyte.vtunnel.thread.NotifyThread;
import com.netbyte.vtunnel.thread.WsThread;
import com.netbyte.vtunnel.model.AppConst;

public class SimpleVPNService extends VpnService {
    private Config config;
    private WsThread wsThread;
    private NotifyThread notifyThread;
    private MonitorThread monitorThread;
    private PendingIntent pendingIntent;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private IPService ipService;

    public SimpleVPNService() {
    }

    @SuppressLint("UnspecifiedImmutableFlag")
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
                // init config
                initConfig(intent);
                // create notification
                createNotification();
                // start VPN
                startVPN();
                return START_STICKY;
            case AppConst.BTN_ACTION_DISCONNECT:
                // stop VPN
                stopVPN();
                return START_NOT_STICKY;
            default:
                return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRevoke() {
        stopVPN();
    }

    private void initConfig(Intent intent) {
        Bundle ex = intent.getExtras();
        String serverIP;
        int serverPort;
        String server = ex.getString("server").trim();
        if (server.contains(":")) {
            serverIP = server.split(":")[0];
            serverPort = Integer.parseInt(server.split(":")[1]);
        } else {
            serverIP = server;
            serverPort = AppConst.DEFAULT_SERVER_PORT;
        }
        String dns = ex.getString("dns");
        String key = ex.getString("key");
        String bypassApps = ex.getString("bypass_apps");
        boolean obfuscate = ex.getBoolean("obfuscate", true);
        this.config = new Config(serverIP, serverPort, dns, key, bypassApps, obfuscate);
        this.ipService = new IPService(config.getServerIP(), config.getServerPort(), config.getKey());
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

    public void startVPN() {
        try {
            // stop threads
            this.stopThreads();
            wsThread = new WsThread(config, this, ipService);
            wsThread.startRunning();
            wsThread.start();
            monitorThread = new MonitorThread(this, ipService);
            monitorThread.startRunning();
            monitorThread.start();
            notifyThread = new NotifyThread(notificationManager, notificationBuilder, this);
            notifyThread.startRunning();
            notifyThread.start();
            Log.i(AppConst.DEFAULT_TAG, "VPN started");
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, "error on startVPN:" + e.toString());
        }

    }

    public void stopVPN() {
        // stop threads
        this.stopThreads();
        // reset data
        this.resetData();
        // stop vpn service
        this.stopSelf();
        Log.i(AppConst.DEFAULT_TAG, "VPN stopped");
    }

    private void stopThreads() {
        if (wsThread != null && wsThread.isRunning()) {
            wsThread.stopRunning();
            wsThread = null;
        }
        if (monitorThread != null && monitorThread.isRunning()) {
            monitorThread.stopRunning();
            monitorThread = null;
        }
        if (notifyThread != null && notifyThread.isRunning()) {
            notifyThread.stopRunning();
            notifyThread = null;
        }
    }

    public void resetData() {
        // reset notification data
        AppConst.UPLOAD_BYTES.set(0);
        AppConst.DOWNLOAD_BYTES.set(0);
        AppConst.TOTAL_BYTES.set(0);
        Log.i(AppConst.DEFAULT_TAG, "data reset");
    }

}
