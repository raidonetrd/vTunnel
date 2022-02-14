package com.netbyte.vtunnel.service;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.model.LocalIP;
import com.netbyte.vtunnel.ws.MyWebSocketClient;

public class IPService {
    private static final String TAG = "IPService";
    private final String serverIP;
    private final int serverPort;
    private final String key;

    public IPService(String serverIP, int serverPort, String key) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.key = key;
    }

    public LocalIP pickIp() {
        @SuppressLint("DefaultLocale") String api = String.format("https://%s:%d/register/pick/ip", serverIP, serverPort);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
        if (TextUtils.isEmpty(resp)) {
            return null;
        }
        String[] ip = resp.split("/");
        if (ip.length == 2) {
            return new LocalIP(ip[0], Integer.parseInt(ip[1]));
        }
        return new LocalIP(AppConst.DEFAULT_LOCAL_ADDRESS, AppConst.DEFAULT_LOCAL_PREFIX_LENGTH);
    }

    public void keepAliveIp(String ip) {
        @SuppressLint("DefaultLocale") String api = String.format("https://%s:%d/register/keepalive/ip?ip=%s", serverIP, serverPort, ip);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
    }

    public void deleteIp(String ip) {
        @SuppressLint("DefaultLocale") String api = String.format("https://%s:%d/register/delete/ip?ip=%s", serverIP, serverPort, ip);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
    }
}
