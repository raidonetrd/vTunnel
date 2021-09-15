package com.netbyte.vtunnel.thread;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.service.SimpleVPNService;

import java.util.concurrent.TimeUnit;

public class MonitorThread extends Thread {
    private static final String TAG = "MonitorThread";
    private volatile boolean RUNNING = true;
    private final SimpleVPNService vpnService;
    private final IPService ipService;

    public MonitorThread(SimpleVPNService vpnService, IPService ipService) {
        this.vpnService = vpnService;
        this.ipService = ipService;
    }

    @Override
    public void run() {
        Log.i(TAG, "start");
        int checkCount = 0;
        while (RUNNING) {
            try {
                TimeUnit.SECONDS.sleep(3);
                if (isAirplaneModeOn(vpnService.getApplicationContext())) {
                    Log.i(TAG, "airplane mode on");
                    vpnService.stopVPN();
                    break;
                }
                checkCount++;
                if (checkCount % 100 == 0) {
                    ipService.keepAliveIp(AppConst.LOCAL_IP);
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "error:" + e.getMessage());
            }
        }
        if (!isAirplaneModeOn(vpnService.getApplicationContext())) {
            // delete local ip
            ipService.deleteIp(AppConst.LOCAL_IP);
        }
        Log.i(TAG, "stop");
    }

    public void stopRunning() {
        this.RUNNING = false;
    }

    private boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

}
