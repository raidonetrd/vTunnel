package com.netbyte.vtunnel.thread;

import android.app.NotificationManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.service.SimpleVPNService;
import com.netbyte.vtunnel.utils.ByteUtil;

import java.util.concurrent.TimeUnit;


public class NotifyThread extends BaseThread {
    private static final String TAG = "NotifyThread";
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder builder;

    public NotifyThread(NotificationManager notificationManager, NotificationCompat.Builder builder, SimpleVPNService vpnService) {
        this.notificationManager = notificationManager;
        this.builder = builder;
        this.vpnService = vpnService;
    }

    @Override
    public void run() {
        Log.i(TAG, "start");
        vpnService.startForeground(AppConst.NOTIFICATION_ID, builder.build());
        while (RUNNING) {
            try {
                TimeUnit.SECONDS.sleep(2);
                if (TextUtils.isEmpty(AppConst.LOCAL_IP)) {
                    String title = "VPN failed!!!";
                    builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle("").bigText(title));
                    break;
                }
                AppConst.TOTAL_BYTES.addAndGet(AppConst.DOWNLOAD_BYTES.get() + AppConst.UPLOAD_BYTES.get());
                String text = String.format("Network: ↓ %s ↑ %s", ByteUtil.format(AppConst.DOWNLOAD_BYTES.get()), ByteUtil.format(AppConst.UPLOAD_BYTES.get()));
                String summary = String.format("IP: %s Total: %s", AppConst.LOCAL_IP, ByteUtil.format(AppConst.TOTAL_BYTES.get()));
                builder.setStyle(new NotificationCompat.BigTextStyle().setSummaryText(summary).setBigContentTitle("").bigText(text));
                notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
                AppConst.UPLOAD_BYTES.set(0);
                AppConst.DOWNLOAD_BYTES.set(0);
            } catch (InterruptedException e) {
                Log.i(TAG, "error:" + e.getMessage());
            }
        }
        vpnService.stopForeground(true);
        notificationManager.cancel(AppConst.NOTIFICATION_ID);
        Log.i(TAG, "stop");
    }

}
