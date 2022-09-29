package com.netbyte.vtunnel.service;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.netbyte.vtunnel.model.Const;
import com.netbyte.vtunnel.model.LocalIp;
import com.netbyte.vtunnel.utils.Ipv6AddressUtil;
import com.netbyte.vtunnel.ws.MyWebSocketClient;

import java.net.UnknownHostException;

public class IpService {
    private static final String TAG = "IPService";
    private final String serverIp;
    private final int serverPort;
    private final String key;
    private final boolean https;

    public IpService(String serverIp, int serverPort, String key, boolean https) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.key = key;
        this.https = https;
    }

    public LocalIp pickIp() {
        @SuppressLint("DefaultLocale") String api = String.format("%s://%s:%d/register/pick/ip", https ? "https" : "http", serverIp, serverPort);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
        if (TextUtils.isEmpty(resp)) {
            return null;
        }
        String[] ip = resp.split("/");
        if (ip.length == 2) {
            return new LocalIp(ip[0], Integer.parseInt(ip[1]));
        }
        return new LocalIp(Const.DEFAULT_LOCAL_ADDRESS, Const.DEFAULT_LOCAL_PREFIX_LENGTH);
    }

    public LocalIp pickIpv6() {
        @SuppressLint("DefaultLocale") String api = String.format("%s://%s:%d/register/prefix/ipv6", https ? "https" : "http", serverIp, serverPort);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
        if (TextUtils.isEmpty(resp)) {
            return null;
        }
        String[] ip = resp.split("/");
        if (ip.length == 2) {
            String ipv6Net = ip[0];
            int prefixLength = Integer.parseInt(ip[1]);
            String randomIpv6Address = null;
            try {
                randomIpv6Address = Ipv6AddressUtil.randomString(ipv6Net, prefixLength);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (randomIpv6Address != null) {
                return new LocalIp(randomIpv6Address, prefixLength);
            }
        }
        return new LocalIp(Const.DEFAULT_LOCAL_V6_ADDRESS, Const.DEFAULT_LOCAL_V6_PREFIX_LENGTH);
    }

    public void keepAliveIp(String ip) {
        @SuppressLint("DefaultLocale") String api = String.format("%s://%s:%d/register/keepalive/ip?ip=%s", https ? "https" : "http", serverIp, serverPort, ip);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
    }

    public void deleteIp(String ip) {
        @SuppressLint("DefaultLocale") String api = String.format("%s://%s:%d/register/delete/ip?ip=%s", https ? "https" : "http", serverIp, serverPort, ip);
        String resp = MyWebSocketClient.httpGet(api, key);
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
    }
}
