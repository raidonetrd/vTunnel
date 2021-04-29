package com.netbyte.vtun.thread;

import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.system.OsConstants;
import android.util.Log;

import com.netbyte.vtun.config.AppConst;
import com.netbyte.vtun.utils.VCipher;
import com.netbyte.vtun.utils.Whitelist;

public class VpnThread extends Thread {

    protected VpnService vpnService;
    protected ParcelFileDescriptor tunnel;
    protected String serverIP;
    protected int serverPort;
    protected VCipher vCipher;
    protected String localIP;
    protected int localPrefixLength;
    protected String dns;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void initTunnel() throws PackageManager.NameNotFoundException {
        VpnService.Builder builder = vpnService.new Builder();
        builder.setMtu(AppConst.MTU)
                .addAddress(localIP, localPrefixLength)
                .addRoute(AppConst.DEFAULT_ROUTE, 0)
                .addDnsServer(dns)
                .setSession(AppConst.APP_NAME)
                .setConfigureIntent(null)
                .allowFamily(OsConstants.AF_INET)
                .setBlocking(true);
        for (String packageName : Whitelist.packageList) {
            builder.addDisallowedApplication(packageName);
        }
        this.tunnel = builder.establish();
        Log.i("initTunnel", "done");
    }

}
