package com.netbyte.vtun.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.netbyte.vtun.MainActivity;
import com.netbyte.vtun.R;
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
    private Thread udpThread, wsThread;
    private PendingIntent pendingIntent;
    private VCipher vCipher;

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
        Log.i(AppConst.DEFAULT_TAG, "start: " + intent.getAction());
        try {
            if (intent != null && AppConst.BTN_ACTION_DISCONNECT.equals(intent.getAction())) {
                disconnect();
                return START_NOT_STICKY;
            } else {
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

                String chanId = createNotificationChannel(AppConst.APP_NAME, AppConst.APP_NAME);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, chanId);
                builder.setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(AppConst.APP_NAME)
                        .setContentText("Running...")
                        .setWhen(System.currentTimeMillis());
                Notification notification = builder.build();
                startForeground(1, notification);
                connect();
            }
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, e.toString());
        }
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return "";
        }
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        NotificationManager service = getSystemService(NotificationManager.class);
        service.createNotificationChannel(chan);
        return channelId;
    }

    private void initUdpThread() {
        AppConst.UDP_THREAD_RUNNABLE = true;
        udpThread = new UdpThread(serverIP, serverPort, localIP, localPrefixLength, dns, vCipher, this);
        udpThread.start();
    }

    private void initWsThread() {
        AppConst.WS_THREAD_RUNNABLE = true;
        wsThread = new WsThread(serverIP, serverPort, localIP, localPrefixLength, dns, vCipher, this);
        wsThread.start();
    }

    private void close() {
        try {
            if (udpThread != null) {
                AppConst.UDP_THREAD_RUNNABLE = false;
                udpThread = null;
            }
            if (wsThread != null) {
                AppConst.WS_THREAD_RUNNABLE = false;
                wsThread = null;
            }
            AppConst.UP_BYTE.set(0);
            AppConst.DOWN_BYTE.set(0);
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, e.toString());
        }
    }

    private void disconnect() {
        Log.i(AppConst.DEFAULT_TAG, "disconnecting...");
        try {
            close();
            stopForeground(true);
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, e.toString());
        }
    }

    private void connect() {
        Log.i(AppConst.DEFAULT_TAG, "connecting...");
        Log.i(AppConst.DEFAULT_TAG, serverIP + " " + serverPort + " " + localIP + " " + dns);
        try {
            close();
            if (protocol.equals("udp")) {
                initUdpThread();
                udpThread.start();
            } else if (protocol.equals("ws")) {
                initWsThread();
                wsThread.start();
            }
        } catch (Exception e) {
            Log.e(AppConst.DEFAULT_TAG, e.toString());
        }
    }
}
