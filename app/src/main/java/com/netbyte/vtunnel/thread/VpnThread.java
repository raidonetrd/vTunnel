package com.netbyte.vtunnel.thread;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.utils.CipherUtil;
import com.netbyte.vtunnel.utils.HttpUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class VpnThread extends Thread {
    private static final String TAG = "VpnThread";
    protected volatile boolean THREAD_RUNNABLE = true;
    protected VpnService vpnService;
    protected ParcelFileDescriptor tunnel;
    protected String serverIP;
    protected int serverPort;
    protected CipherUtil cipherUtil;
    protected String localIP;
    protected int localPrefixLength;
    protected String dns;
    protected String bypassUrl;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void initTunnel() throws PackageManager.NameNotFoundException, IOException {
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
        Log.i(TAG, "init tunnel has done");
    }

    public void finish() {
        this.THREAD_RUNNABLE = false;
    }

    private List<String> bypassApps() {
        List<String> bypassPackageList = new ArrayList<>();
        Log.i(TAG, "bypassUrl:" + bypassUrl);
        if (!TextUtils.isEmpty(bypassUrl)) {
            try {
                String base64AppList = HttpUtil.get(bypassUrl);
                base64AppList = base64AppList.trim();
                Log.i(TAG, "base64AppList:" + base64AppList);
                String decodeAppList = new String(Base64.getDecoder().decode(base64AppList.getBytes(StandardCharsets.UTF_8)));
                String[] appList = decodeAppList.split("\n");
                if (appList.length > 0) {
                    bypassPackageList.addAll(Arrays.asList(appList));
                }
            } catch (IOException e) {
                Log.e(TAG, "failed to get bypass url");
            }
        }
        List<PackageInfo> packageInfoList = vpnService.getApplicationContext().getPackageManager().getInstalledPackages(0);
        ArrayList<String> result = new ArrayList<>();
        result.add(AppConst.APP_PACKAGE_NAME);
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
