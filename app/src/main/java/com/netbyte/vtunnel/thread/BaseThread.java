package com.netbyte.vtunnel.thread;

import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;

import com.netbyte.vtunnel.service.IpService;
import com.netbyte.vtunnel.service.MyVpnService;

public class BaseThread extends Thread {
    protected MyVpnService vpnService;
    protected IpService ipService;
    protected NotificationManager notificationManager;
    protected NotificationCompat.Builder notificationBuilder;
}
