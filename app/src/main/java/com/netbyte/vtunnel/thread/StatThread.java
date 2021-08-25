package com.netbyte.vtunnel.thread;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Build;
import android.provider.Settings;
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
    private final String serverIP;
    private final int serverPort;
    private final String key;

    public StatThread(String serverIP, int serverPort, String key, NotificationManager notificationManager, NotificationCompat.Builder builder, VpnService vpnService) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.key = key;
        this.notificationManager = notificationManager;
        this.builder = builder;
        this.vpnService = vpnService;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void run() {
        Log.i(TAG, "start");
        vpnService.startForeground(AppConst.NOTIFICATION_ID, builder.build());
        int checkCount = 0;
        while (THREAD_RUNNABLE) {
            try {
                Thread.sleep(3000);
                if (checkCount > 3 && AppConst.DOWN_BYTE.get() == 0) {
                    String title = "Failed to connect!";
                    builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title));
                } else {
                    String title = String.format("IP: %s", AppConst.LOCAL_ADDRESS);
                    String text = String.format("Total: ↑ %s ↓ %s", ByteUtil.format(AppConst.UP_BYTE.get()), ByteUtil.format(AppConst.DOWN_BYTE.get()));
                    builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(text));
                }
                notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
                checkCount++;
                if (checkCount % 100 == 0) {
                    keepAliveIp(AppConst.LOCAL_ADDRESS);
                }
                if (isAirplaneModeOn(vpnService.getApplicationContext())) {
                    Log.i(TAG, "airplane mode on");
                    finish();
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "error:" + e.getMessage());
            }
        }
        Log.i(TAG, "stop");
        afterStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void afterStop() {
        if (!isAirplaneModeOn(vpnService.getApplicationContext())) {
            deleteIp(AppConst.LOCAL_ADDRESS, key);
        }
        //reset notification data
        AppConst.UP_BYTE.set(0);
        AppConst.DOWN_BYTE.set(0);
        AppConst.LOCAL_ADDRESS = "";
        //reset connection status
        SharedPreferences preferences = vpnService.getApplicationContext().getSharedPreferences(AppConst.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor preEditor = preferences.edit();
        preEditor.putBoolean("connected", false);
        preEditor.commit();
        //stop service
        vpnService.stopForeground(true);
        vpnService.stopSelf();
    }

    public void finish() {
        this.THREAD_RUNNABLE = false;
    }

    private void keepAliveIp(String ip) {
        @SuppressLint("DefaultLocale") String api = String.format("https://%s:%d/register/keepalive/ip?ip=%s", serverIP, serverPort, ip);
        String resp = "";
        try {
            resp = HttpUtil.get(api, "key", key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
    }

    private void deleteIp(String ip, String key) {
        @SuppressLint("DefaultLocale") String api = String.format("https://%s:%d/register/delete/ip?ip=%s", serverIP, serverPort, ip);
        String resp = "";
        try {
            resp = HttpUtil.get(api, "key", key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

}
