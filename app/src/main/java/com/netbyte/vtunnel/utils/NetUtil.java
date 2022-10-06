package com.netbyte.vtunnel.utils;


import android.text.TextUtils;

import com.netbyte.vtunnel.ws.MyWebSocketClient;

import org.asynchttpclient.ws.WebSocket;

public class NetUtil {
    private final static String PATTERN_IPV4 = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

    public static boolean checkServer(String server, String path, String key, boolean wss) {
        if (TextUtils.isEmpty(server) || TextUtils.isEmpty(path) || TextUtils.isEmpty(key)) {
            return false;
        }
        boolean result = false;
        WebSocket webSocket = null;
        try {
            String uri = String.format("%s://%s%s", wss ? "wss" : "ws", server, path);
            webSocket = MyWebSocketClient.connectWebSocket(uri, key);
            if (webSocket != null && webSocket.isOpen()) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        return dns.matches(PATTERN_IPV4);
    }

}
