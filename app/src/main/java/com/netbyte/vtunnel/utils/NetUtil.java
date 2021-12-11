package com.netbyte.vtunnel.utils;


import android.text.TextUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetUtil {
    public static boolean checkServer(String server) {
        if (TextUtils.isEmpty(server)) {
            return false;
        }
        boolean result;
        String host = "";
        int port = 443;
        Socket socket = null;
        try {
            String[] serverAddress = server.split(":");
            if (serverAddress.length > 1) {
                host = serverAddress[0];
                port = Integer.parseInt(serverAddress[1]);
            } else {
                host = server;
            }
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 3000);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (socket != null && socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            result = InetAddress.getByName(dns).isReachable(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
