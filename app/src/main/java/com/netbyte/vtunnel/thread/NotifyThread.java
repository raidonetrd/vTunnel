package com.netbyte.vtunnel.thread;

import android.app.NotificationManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.model.Global;
import com.netbyte.vtunnel.model.Stats;
import com.netbyte.vtunnel.service.MyVPNService;
import com.netbyte.vtunnel.utils.FormatUtil;

import java.util.concurrent.TimeUnit;


public class NotifyThread extends BaseThread {
    private static final String TAG = "NotifyThread";
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder builder;

    public NotifyThread(NotificationManager notificationManager, NotificationCompat.Builder builder, MyVPNService vpnService) {
        this.notificationManager = notificationManager;
        this.builder = builder;
        this.vpnService = vpnService;
    }

    @Override
    public void run() {
        Log.i(TAG, "start");
        vpnService.startForeground(AppConst.NOTIFICATION_ID, builder.build());
        while (Global.RUNNING) {
            try {
                TimeUnit.SECONDS.sleep(1);
                if (TextUtils.isEmpty(Global.LOCAL_IP)) {
                    String title = "VPN failed!!!";
                    builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle("").bigText(title));
                    break;
                }
                Stats.TOTAL_BYTES.addAndGet(Stats.DOWNLOAD_BYTES.get() + Stats.UPLOAD_BYTES.get());
                String text = String.format("Traffic: ↓ %s ↑ %s", FormatUtil.formatByte(Stats.DOWNLOAD_BYTES.get()), FormatUtil.formatByte(Stats.UPLOAD_BYTES.get()));
                String summary = String.format("IP: %s Total: %s", Global.LOCAL_IP, FormatUtil.formatByte(Stats.TOTAL_BYTES.get()));
                builder.setStyle(new NotificationCompat.BigTextStyle().setSummaryText(summary).setBigContentTitle("").bigText(text));
                notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
                Stats.UPLOAD_BYTES.set(0);
                Stats.DOWNLOAD_BYTES.set(0);
            } catch (InterruptedException e) {
                Log.i(TAG, "error:" + e.getMessage());
            }
        }
        resetData();
        vpnService.stopForeground(true);
        notificationManager.cancel(AppConst.NOTIFICATION_ID);
        Log.i(TAG, "stop");
    }

    public void resetData() {
        // reset notification data
        Stats.UPLOAD_BYTES.set(0);
        Stats.DOWNLOAD_BYTES.set(0);
        Stats.TOTAL_BYTES.set(0);
        Log.i(AppConst.DEFAULT_TAG, "reset data");
    }
}
