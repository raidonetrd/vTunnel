package com.netbyte.vtunnel.thread;

import android.app.NotificationManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.model.Const;
import com.netbyte.vtunnel.model.Global;
import com.netbyte.vtunnel.model.Stats;
import com.netbyte.vtunnel.service.IpService;
import com.netbyte.vtunnel.service.MyVpnService;
import com.netbyte.vtunnel.utils.FormatUtil;
import com.netbyte.vtunnel.R;

import java.util.concurrent.TimeUnit;


public class NotifyThread extends BaseThread {
    private static final String TAG = "NotifyThread";
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder builder;

    public NotifyThread(NotificationManager notificationManager, NotificationCompat.Builder builder, MyVpnService vpnService, IpService ipService) {
        this.notificationManager = notificationManager;
        this.builder = builder;
        this.vpnService = vpnService;
        this.ipService = ipService;
    }

    @Override
    public void run() {
        Log.i(TAG, "start");
        vpnService.startForeground(Const.NOTIFICATION_ID, builder.build());
        int seconds = 0;
        while (Global.RUNNING) {
            try {
                TimeUnit.SECONDS.sleep(1);
                Stats.TOTAL_BYTES.addAndGet(Stats.DOWNLOAD_BYTES.get() + Stats.UPLOAD_BYTES.get());
                String text = String.format("↓ %s ↑ %s", FormatUtil.formatByte(Stats.DOWNLOAD_BYTES.get()), FormatUtil.formatByte(Stats.UPLOAD_BYTES.get()));
                String summary = String.format("%s: %s", vpnService.getResources().getString(R.string.msg_vpn_data_usage), FormatUtil.formatByte(Stats.TOTAL_BYTES.get()));
                builder.setStyle(new NotificationCompat.BigTextStyle().setSummaryText(summary).setBigContentTitle("").bigText(text));
                notificationManager.notify(Const.NOTIFICATION_ID, builder.build());
                Stats.UPLOAD_BYTES.set(0);
                Stats.DOWNLOAD_BYTES.set(0);
                seconds++;
                if (seconds % 300 == 0) {
                    ipService.keepAliveIp(Global.LOCAL_IP);
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "error:" + e.getMessage());
            }
        }
        resetData();
        vpnService.stopForeground(true);
        notificationManager.cancel(Const.NOTIFICATION_ID);
        ipService.deleteIp(Global.LOCAL_IP);
        Log.i(TAG, "stop");
    }

    public void resetData() {
        Stats.UPLOAD_BYTES.set(0);
        Stats.DOWNLOAD_BYTES.set(0);
        Stats.TOTAL_BYTES.set(0);
        Log.i(Const.DEFAULT_TAG, "reset data");
    }
}
