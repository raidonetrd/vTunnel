package com.netbyte.vtunnel.thread;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.service.SimpleVPNService;
import com.netbyte.vtunnel.utils.ByteUtil;


public class StatThread extends Thread {
    private static final String TAG = "StatThread";
    private volatile boolean RUNNING = true;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder builder;
    private final SimpleVPNService vpnService;
    private final IPService ipService;

    public StatThread(NotificationManager notificationManager, NotificationCompat.Builder builder, SimpleVPNService vpnService, IPService ipService) {
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
        while (RUNNING) {
            try {
                Thread.sleep(3000);
                if (TextUtils.isEmpty(AppConst.LOCAL_ADDRESS)) {
                    String title = "Failed to connect!";
                    builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title));
                    break;
                }
                if (isAirplaneModeOn(vpnService.getApplicationContext())) {
                    Log.i(TAG, "airplane mode on");
                    vpnService.stopVPN();
                    break;
                }
                String title = String.format("IP: %s", AppConst.LOCAL_ADDRESS);
                String text = String.format("Net: ↑ %s ↓ %s", ByteUtil.format(AppConst.UP_BYTE.get()), ByteUtil.format(AppConst.DOWN_BYTE.get()));
                builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(text));
                notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
                checkCount++;
                if (checkCount % 100 == 0) {
                    ipService.keepAliveIp(AppConst.LOCAL_ADDRESS);
                }
                AppConst.UP_BYTE.set(0);
                AppConst.DOWN_BYTE.set(0);
            } catch (InterruptedException e) {
                Log.i(TAG, "error:" + e.getMessage());
            }
        }
        if (!isAirplaneModeOn(vpnService.getApplicationContext())) {
            // delete local ip
            ipService.deleteIp(AppConst.LOCAL_ADDRESS);
        }
        Log.i(TAG, "stop");
    }

    public void stopRunning() {
        this.RUNNING = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private  boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

}
