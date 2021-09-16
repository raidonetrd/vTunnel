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
import java.util.Collections;
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
        Log.i(TAG, "bypassUrl:" + config.getBypassUrl());
        ArrayList<String> result = new ArrayList<>();
        result.add(AppConst.APP_PACKAGE_NAME);
        if (TextUtils.isEmpty(config.getBypassUrl())) {
            return result;
        }
        List<String> bypassPackageList = new ArrayList<>();
        try {
            String bypassText = HttpUtil.get(config.getBypassUrl());
            if (TextUtils.isEmpty(bypassText)) {
                return result;
            }
            bypassText = bypassText.trim();
            bypassText = new String(Base64.getDecoder().decode(bypassText.getBytes(StandardCharsets.UTF_8)));
            Log.i(TAG, "bypassText:" + bypassText);
            String[] appList = bypassText.split("\n");
            if (appList.length > 0) {
                bypassPackageList.addAll(Arrays.asList(appList));
            }
        } catch (IOException e) {
            Log.e(TAG, "failed to get bypass url");
        }
        List<PackageInfo> packageInfoList = vpnService.getApplicationContext().getPackageManager().getInstalledPackages(0);
        for (PackageInfo info : packageInfoList) {
            String packageName = info.packageName;
            for (String p : bypassPackageList) {
                p = p.trim();
                if (packageName.startsWith(p)) {
                    result.add(packageName);
                }
            }
        }
        Log.i(TAG, "bypass apps:" + result);
        return result;
    }
}
