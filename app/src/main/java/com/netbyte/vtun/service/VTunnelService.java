package com.netbyte.vtun.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.netbyte.vtun.MainActivity;
import com.netbyte.vtun.R;
import com.netbyte.vtun.thread.StatThread;
import com.netbyte.vtun.thread.UdpThread;
import com.netbyte.vtun.thread.WsThread;
import com.netbyte.vtun.utils.VCipher;
import com.netbyte.vtun.config.AppConst;


public class VTunnelService extends VpnService {
    private static String serverIP, localIP;
    private static int localPrefixLength = AppConst.DEFAULT_LOCAL_PREFIX_LENGTH;
    private static int serverPort;
    private static String dns;
    private static String protocol;
    private static String token;
    private Thread udpThread, wsThread, statThread;
    private PendingIntent pendingIntent;
    private VCipher vCipher;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    public VTunnelService() {
    }

    @Override
    public void onCreate() {
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent != null && AppConst.BTN_ACTION_DISCONNECT.equals(intent.getAction())) {
                disconnect();
                return START_NOT_STICKY;
            }
            // 0.init config
            initConfig(intent);
            // 1.create notification
            createNotification();
            // 2. connect
            connect();
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, "onStartCommand error:" + e.toString());
        }
        return START_STICKY;
    }

    private void initConfig(Intent intent) {
        Bundle ex = intent.getExtras();
        serverIP = ex.getString("serverIP");
        serverPort = ex.getInt("serverPort");
        protocol = ex.getString("protocol");
        String[] localIPArray = ex.getString("localIP").split("/");
        if (localIPArray.length >= 1) {
            localIP = localIPArray[0];
        }
        if (localIPArray.length >= 2) {
            localPrefixLength = Integer.parseInt(localIPArray[1]);
        }
        dns = ex.getString("dns");
        token = ex.getString("token");
        vCipher = new VCipher(token);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotification() {
        NotificationChannel channel = new NotificationChannel(AppConst.NOTIFICATION_CHANNEL_ID, AppConst.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        notificationBuilder = new NotificationCompat.Builder(this, channel.getId());
        notificationBuilder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(AppConst.APP_NAME)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true);
        startForeground(AppConst.NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
        stopForeground(true);
    }

    private void connect() {
        Log.i(AppConst.DEFAULT_TAG, "connecting " + serverIP + " " + serverPort + " " + localIP + " " + dns);
        try {
            stopAllThreads();
            startStatThread();
            if (protocol.equals("udp")) {
                startUdpThread();
            } else if (protocol.equals("ws")) {
                startWsThread();
            }
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, "disconnect error:" + e.toString());
        }
    }

    private void disconnect() {
        Log.i(AppConst.DEFAULT_TAG, "disconnecting...");
        try {
            removeNotification();
            stopAllThreads();
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, "disconnect error:" + e.toString());
        }
    }

    private void stopAllThreads() {
        try {
            if (udpThread != null) {
                AppConst.UDP_THREAD_RUNNABLE = false;
                udpThread = null;
            }
            if (wsThread != null) {
                AppConst.WS_THREAD_RUNNABLE = false;
                wsThread = null;
            }
            if (statThread != null) {
                AppConst.STAT_THREAD_RUNNABLE = false;
                statThread = null;
            }
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, "close error:" + e.toString());
        }
    }

    private void startUdpThread() {
        AppConst.UDP_THREAD_RUNNABLE = true;
        udpThread = new UdpThread(serverIP, serverPort, localIP, localPrefixLength, dns, vCipher, this);
        udpThread.start();
    }

    private void startWsThread() {
        AppConst.WS_THREAD_RUNNABLE = true;
        wsThread = new WsThread(serverIP, serverPort, localIP, localPrefixLength, dns, vCipher, this);
        wsThread.start();
    }

    private void startStatThread() {
        AppConst.STAT_THREAD_RUNNABLE = true;
        statThread = new StatThread(notificationManager, notificationBuilder);
        statThread.start();
    }
}
