package com.netbyte.vtunnel.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.activity.MainActivity;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.thread.StatThread;
import com.netbyte.vtunnel.thread.UdpThread;
import com.netbyte.vtunnel.thread.VpnThread;
import com.netbyte.vtunnel.thread.WsThread;
import com.netbyte.vtunnel.utils.VCipher;
import com.netbyte.vtunnel.config.AppConst;

public class VTunnelService extends VpnService {
    private static String serverIP, localIP;
    private static int localPrefixLength;
    private static int serverPort;
    private static String dns;
    private static String protocol;
    private static String key;
    private VpnThread udpThread, wsThread;
    private StatThread statThread;
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
                doDisconnect();
                return START_NOT_STICKY;
            }
            // 0.init config
            initConfig(intent);
            // 1.create notification
            createNotification();
            // 2.connect
            doConnect();
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, "error on onStartCommand:" + e.toString());
        }
        return START_STICKY;
    }

    private void initConfig(Intent intent) {
        if (intent == null) {
            return;
        }
        Bundle ex = intent.getExtras();
        serverIP = ex.getString("serverIP");
        serverPort = ex.getInt("serverPort");
        protocol = ex.getString("protocol");
        String[] localIPArray = ex.getString("localIP").split("/");
        if (localIPArray.length >= 1) {
            localIP = localIPArray[0];
            localPrefixLength = AppConst.DEFAULT_LOCAL_PREFIX_LENGTH;
        }
        if (localIPArray.length >= 2) {
            localPrefixLength = Integer.parseInt(localIPArray[1]);
        }
        dns = ex.getString("dns");
        key = ex.getString("key");
        vCipher = new VCipher(key);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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
            if (protocol.equals("udp")) {
                startUdpThread();
            } else if (protocol.equals("websocket")) {
                startWsThread();
            }
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, "error on connecting:" + e.toString());
        }
    }

    private void doDisconnect() {
        Log.i(AppConst.DEFAULT_TAG, "disconnecting...");
        stopThreads();
    }

    private void stopThreads() {
        if (udpThread != null) {
            udpThread.finish();
            udpThread = null;
        }
        if (wsThread != null) {
            wsThread.finish();
            wsThread = null;
        }
        if (statThread != null) {
            statThread.finish();
            statThread = null;
        }
    }

    private void startUdpThread() {
        udpThread = new UdpThread(serverIP, serverPort, localIP, localPrefixLength, dns, vCipher, this);
        udpThread.start();
    }

    private void startWsThread() {
        wsThread = new WsThread(serverIP, serverPort, localIP, localPrefixLength, dns, vCipher, this);
        wsThread.start();
    }

    private void startStatThread() {
        statThread = new StatThread(notificationManager, notificationBuilder, this);
        statThread.start();
    }
}
