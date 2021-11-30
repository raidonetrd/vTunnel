package com.netbyte.vtunnel.thread;

import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.service.MyVPNService;

public class BaseThread extends Thread {
    public static volatile boolean RUNNING;
    protected MyVPNService vpnService;
    protected IPService ipService;

    public void startRunning() {
        RUNNING = true;
    }

    public void stopRunning() {
        RUNNING = false;
    }

    public boolean isRunning() {
        return RUNNING;
    }
}
