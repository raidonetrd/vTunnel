package com.netbyte.vtunnel.utils;


import android.text.TextUtils;

import com.netbyte.vtunnel.ws.MyWebSocketClient;

import org.asynchttpclient.ws.WebSocket;

import java.net.InetAddress;

public class NetUtil {
    public static boolean checkServer(String server, String path, String key) {
        if (TextUtils.isEmpty(server) || TextUtils.isEmpty(path) || TextUtils.isEmpty(key)) {
            return false;
        }
        boolean result = false;
        WebSocket webSocket = null;
        try {
            String uri = String.format("wss://%s%s", server, path);
            webSocket = MyWebSocketClient.connectWebSocket(uri, key);
            if (webSocket != null && webSocket.isOpen()) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (webSocket != null && webSocket.isOpen()) {
                webSocket.sendCloseFrame();
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
