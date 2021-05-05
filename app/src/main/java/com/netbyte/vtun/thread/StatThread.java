package com.netbyte.vtun.thread;

import android.app.NotificationManager;
import android.net.VpnService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.netbyte.vtun.config.AppConst;
import com.netbyte.vtun.utils.ByteUtil;

public class StatThread extends Thread {
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private VpnService vpnService;

    public StatThread(NotificationManager notificationManager, NotificationCompat.Builder builder, VpnService vpnService) {
        this.notificationManager = notificationManager;
        this.builder = builder;
        this.vpnService = vpnService;
    }

    @Override
    public void run() {
        Log.i("StatThread", "start");
        vpnService.startForeground(AppConst.NOTIFICATION_ID, builder.build());
        int checkCount = 0;
        while (AppConst.STAT_THREAD_RUNNABLE) {
            try {
                Thread.sleep(2000);
                String text = "";
                if (checkCount > 5 && AppConst.DOWN_BYTE.get() == 0) {
                    text = "Status:Failed to connect!";
                } else {
                    text = String.format("Network: ↑ %s ↓ %s", ByteUtil.format(AppConst.UP_BYTE.get()), ByteUtil.format(AppConst.DOWN_BYTE.get()));
                }
                builder.setContentText(text).setStyle(new NotificationCompat.BigTextStyle().bigText(text));
                notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
                checkCount++;
            } catch (InterruptedException e) {
                Log.i("StatThread", "error:" + e.getMessage());
            }
        }
        AppConst.UP_BYTE.set(0);
        AppConst.DOWN_BYTE.set(0);
        vpnService.stopForeground(true);
        Log.i("StatThread", "stop");
    }

}
