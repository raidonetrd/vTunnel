package com.netbyte.vtunnel.utils;


import android.text.TextUtils;

import com.netbyte.vtunnel.ws.WsClient;

import java.net.InetAddress;
import java.net.URI;

public class NetUtil {
    public static boolean checkServer(String server, String key) {
        if (TextUtils.isEmpty(server) || TextUtils.isEmpty(key)) {
            return false;
        }
        boolean result = false;
        WsClient wsClient = null;
        try {
            String uri = String.format("wss://%s/way-to-freedom", server);
            wsClient = new WsClient(new URI(uri));
            wsClient.setSocketFactory(SSLUtil.createEasySSLContext().getSocketFactory());
            wsClient.addHeader("key", key);
            wsClient.connectBlocking();
            if (wsClient.isOpen()) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (wsClient != null && wsClient.isOpen()) {
                wsClient.close();
            }
        }
        return result;
    }

    public static boolean checkDNS(String dns) {
        if (TextUtils.isEmpty(dns)) {
            return false;
        }
        boolean result = false;
        try {
            result = InetAddress.getByName(dns).isReachable(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
