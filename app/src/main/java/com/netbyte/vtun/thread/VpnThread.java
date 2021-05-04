package com.netbyte.vtun.thread;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.system.OsConstants;
import android.util.Log;

import com.netbyte.vtun.config.AppConst;
import com.netbyte.vtun.utils.VCipher;
import com.netbyte.vtun.utils.Whitelist;

import java.util.ArrayList;
import java.util.List;

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
        for (String packageName : bypassApps()) {
            builder.addDisallowedApplication(packageName);
        }
        this.tunnel = builder.establish();
        Log.i("initTunnel", "done");
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
                Log.e(AppConst.DEFAULT_TAG, "bypassAppPackages error:" + e.getMessage());
            }
        }
        Log.i(AppConst.DEFAULT_TAG, "bypass apps:" + result);
        return result;
    }
}
