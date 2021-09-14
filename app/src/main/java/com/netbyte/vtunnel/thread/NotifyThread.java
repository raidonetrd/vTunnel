package com.netbyte.vtunnel.thread;

import android.app.NotificationManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.service.SimpleVPNService;
import com.netbyte.vtunnel.utils.ByteUtil;


public class NotifyThread extends Thread {
    private static final String TAG = "NotifyThread";
    private volatile boolean RUNNING = true;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder builder;
    private final SimpleVPNService vpnService;


    public NotifyThread(NotificationManager notificationManager, NotificationCompat.Builder builder, SimpleVPNService vpnService) {
        this.notificationManager = notificationManager;
        this.builder = builder;
        this.vpnService = vpnService;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void run() {
        Log.i(TAG, "start");
        vpnService.startForeground(AppConst.NOTIFICATION_ID, builder.build());
        while (RUNNING) {
            try {
                Thread.sleep(3000);
                if (TextUtils.isEmpty(AppConst.LOCAL_IP)) {
                    String title = "Failed to connect!";
                    builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle("").bigText(title));
                    break;
                }
                String title = String.format("IP: %s", AppConst.LOCAL_IP);
                String text = String.format("Network: ↓ %s ↑ %s", ByteUtil.format(AppConst.DOWNLOAD_BYTES.get()), ByteUtil.format(AppConst.UPLOAD_BYTES.get()));
                builder.setStyle(new NotificationCompat.BigTextStyle().setSummaryText(title).setBigContentTitle("").bigText(text));
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

    public void stopRunning() {
        this.RUNNING = false;
    }

}
