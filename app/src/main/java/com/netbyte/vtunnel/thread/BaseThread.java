package com.netbyte.vtunnel.thread;

import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.model.Global;
import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.service.MyVPNService;

public class BaseThread extends Thread {
    protected MyVPNService vpnService;
    protected IPService ipService;
    protected NotificationManager notificationManager;
    protected NotificationCompat.Builder notificationBuilder;

    public void startRunning() {
        Global.RUNNING = true;
    }

}
