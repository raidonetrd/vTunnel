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
import com.netbyte.vtunnel.utils.VCipher;
import com.netbyte.vtunnel.utils.Whitelist;

import java.util.ArrayList;
import java.util.List;

public class VpnThread extends Thread {
    protected volatile boolean THREAD_RUNNABLE = true;
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
        AppConst.SUBNET = String.format("%s/%d", localIP, localPrefixLength);
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
                for (String word : Whitelist.wordsList) {
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
