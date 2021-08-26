package com.netbyte.vtunnel.thread;

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
import com.netbyte.vtunnel.model.Config;
import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.utils.ByteUtil;


public class StatThread extends Thread {
    private static final String TAG = "StatThread";
    private volatile boolean THREAD_RUNNABLE = true;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder builder;
    private final VpnService vpnService;
    private final IPService ipService;
    private final Config config;

    public StatThread(Config config, NotificationManager notificationManager, NotificationCompat.Builder builder, VpnService vpnService, IPService ipService) {
        this.config = config;
        this.notificationManager = notificationManager;
        this.builder = builder;
        this.vpnService = vpnService;
        this.ipService = ipService;
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
                    ipService.keepAliveIp(AppConst.LOCAL_ADDRESS);
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
            ipService.deleteIp(AppConst.LOCAL_ADDRESS);
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

}
