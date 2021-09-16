package com.netbyte.vtunnel.thread;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.model.Config;
import com.netbyte.vtunnel.model.LocalIP;
import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.service.SimpleVPNService;
import com.netbyte.vtunnel.utils.CipherUtil;
import com.netbyte.vtunnel.utils.HttpUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class VPNThread extends Thread {
    private static final String TAG = "VpnThread";
    protected volatile boolean RUNNING = true;
    protected SimpleVPNService vpnService;
    protected ParcelFileDescriptor tunnel;
    protected CipherUtil cipherUtil;
    protected IPService ipService;
    protected Config config;
    protected LocalIP localIP;

    protected ParcelFileDescriptor createTunnel() throws PackageManager.NameNotFoundException {
        if (config == null || localIP == null) {
            return null;
        }
        VpnService.Builder builder = vpnService.new Builder();
        builder.setMtu(AppConst.MTU)
                .addAddress(localIP.getLocalIP(), localIP.getLocalPrefixLength())
                .addRoute(AppConst.DEFAULT_ROUTE, 0)
                .addDnsServer(config.getDns())
                .setSession(AppConst.APP_NAME)
                .setConfigureIntent(null)
                .allowFamily(OsConstants.AF_INET)
                .setBlocking(true);
        for (String packageName : bypassApps()) {
            builder.addDisallowedApplication(packageName);
        }
        tunnel = builder.establish();
        return tunnel;
    }

    public void stopRunning() {
        this.RUNNING = false;
    }

    public boolean isRunning() {
        return this.RUNNING;
    }

    private List<String> bypassApps() {
        Log.i(TAG, "bypassApps:" + config.getBypassApps());
        ArrayList<String> result = new ArrayList<>();
        result.add(AppConst.APP_PACKAGE_NAME);
        if (TextUtils.isEmpty(config.getBypassApps())) {
            return result;
        }
        List<PackageInfo> packageInfoList = vpnService.getApplicationContext().getPackageManager().getInstalledPackages(0);
        for (PackageInfo info : packageInfoList) {
            String packageName = info.packageName;
            if (config.getBypassApps().contains(packageName)) {
                result.add(packageName);
            }
        }
        Log.i(TAG, "bypass apps:" + result);
        return result;
    }
}
