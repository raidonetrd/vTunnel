package com.netbyte.vtunnel.thread;

import com.netbyte.vtunnel.model.Global;
import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.service.MyVPNService;

public class BaseThread extends Thread {
    protected MyVPNService vpnService;
    protected IPService ipService;

    public void startRunning() {
        Global.RUNNING = true;
    }

}
