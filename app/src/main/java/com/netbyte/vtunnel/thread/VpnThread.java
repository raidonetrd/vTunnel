package com.netbyte.vtunnel.thread;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.utils.CipherUtil;
import com.netbyte.vtunnel.utils.PackageUtil;

import java.util.ArrayList;
import java.util.List;

public class VpnThread extends Thread {
    protected volatile boolean THREAD_RUNNABLE = true;
    protected VpnService vpnService;
    protected ParcelFileDescriptor tunnel;
    protected String serverIP;
    protected int serverPort;
    protected CipherUtil cipherUtil;
    protected String localIP;
    protected int localPrefixLength;
    protected String dns;


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void initTunnel() throws PackageManager.NameNotFoundException {
        AppConst.LOCAL_ADDRESS = localIP;
        VpnService.Builder builder = vpnService.new Builder();
        builder.setMtu(AppConst.MTU)
                .addAddress(localIP, localPrefixLength)
                .addRoute(AppConst.DEFAULT_ROUTE, 0)
                .addDnsServer(dns)
                .setSession(AppConst.APP_NAME)
                .setConfigureIntent(null)
                .allowFamily(OsConstants.AF_INET)
                .setBlocking(true);
        for (String packageName : bypassApps()) {
            builder.addDisallowedApplication(packageName);
        }
        this.tunnel = builder.establish();
        Log.i("VpnThread", "init tunnel has done");
    }

    public void finish() {
        this.THREAD_RUNNABLE = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> bypassApps() {
        PackageManager pm = vpnService.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packages = pm.queryIntentActivities(intent, 0);
        ArrayList<String> result = new ArrayList<>();
        result.add(AppConst.APP_PACKAGE_NAME);
        for (ResolveInfo resolveInfo : packages) {
            try {
                String packageName = resolveInfo.activityInfo.packageName;
                for (String word : PackageUtil.bypassPackageList) {
                    if (packageName.toLowerCase().contains(word)) {
                        result.add(packageName);
                    }
                }
            } catch (Exception e) {
                Log.e(AppConst.DEFAULT_TAG, "error on bypass apps:" + e.getMessage());
            }
        }
        Log.i(AppConst.DEFAULT_TAG, "bypass apps:" + result);
        return result;
    }
}
