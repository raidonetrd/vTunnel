package com.netbyte.vtunnel.service;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.model.LocalIp;
import com.netbyte.vtunnel.ws.MyWebSocketClient;

public class IpService {
    private static final String TAG = "IPService";
    private final String serverIp;
    private final int serverPort;
    private final String key;

    public IpService(String serverIp, int serverPort, String key) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.key = key;
    }

    public LocalIp pickIp() {
        @SuppressLint("DefaultLocale") String api = String.format("https://%s:%d/register/pick/ip", serverIp, serverPort);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
        if (TextUtils.isEmpty(resp)) {
            return null;
        }
        String[] ip = resp.split("/");
        if (ip.length == 2) {
            return new LocalIp(ip[0], Integer.parseInt(ip[1]));
        }
        return new LocalIp(AppConst.DEFAULT_LOCAL_ADDRESS, AppConst.DEFAULT_LOCAL_PREFIX_LENGTH);
    }

    public void keepAliveIp(String ip) {
        @SuppressLint("DefaultLocale") String api = String.format("https://%s:%d/register/keepalive/ip?ip=%s", serverIp, serverPort, ip);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
    }

    public void deleteIp(String ip) {
        @SuppressLint("DefaultLocale") String api = String.format("https://%s:%d/register/delete/ip?ip=%s", serverIp, serverPort, ip);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
    }
}