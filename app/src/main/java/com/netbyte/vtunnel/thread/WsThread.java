package com.netbyte.vtunnel.thread;

import android.annotation.SuppressLint;
import android.net.VpnService;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.utils.HttpUtil;
import com.netbyte.vtunnel.ws.WsClient;
import com.netbyte.vtunnel.utils.SSLUtil;
import com.netbyte.vtunnel.utils.CipherUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

public class WsThread extends VpnThread {
    private static final String TAG = "WsThread";

    public WsThread(String serverIP, int serverPort, String dns, CipherUtil cipherUtil, VpnService vpnService) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.dns = dns;
        this.cipherUtil = cipherUtil;
        this.vpnService = vpnService;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        WsClient wsClient = null;
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            Log.i(TAG, "start");
            pickIp(serverIP, serverPort, cipherUtil.getKey());
            super.initTunnel();
            in = new FileInputStream(tunnel.getFileDescriptor());
            out = new FileOutputStream(tunnel.getFileDescriptor());
            @SuppressLint("DefaultLocale") String uri = String.format("wss://%s:%d/way-to-freedom", serverIP, serverPort);
            wsClient = new WsClient(new URI(uri), cipherUtil);
            wsClient.setSocketFactory(SSLUtil.createEasySSLContext().getSocketFactory());
            wsClient.connectBlocking();
            wsClient.setOutStream(out);
            while (THREAD_RUNNABLE) {
                try {
                    byte[] buf = new byte[AppConst.BUFFER_SIZE];
                    int ln = in.read(buf);
                    if (ln > 0) {
                        if (wsClient.isOpen()) {
                            byte[] data = Arrays.copyOfRange(buf, 0, ln);
                            wsClient.send(cipherUtil.encrypt(data));
                            AppConst.UP_BYTE.addAndGet(ln);
                        } else if (wsClient.isClosed()) {
                            wsClient.reconnectBlocking();
                            sleep(1000);
                            Log.i(TAG, "ws reconnect...");
                        } else {
                            sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error on WsThread:" + e.toString());
                }
            }
            Log.i(TAG, "stop");
        } catch (Exception e) {
            Log.e(TAG, "error on WsThread:" + e.toString());
        } finally {
            if (wsClient != null) {
                wsClient.close();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tunnel != null) {
                try {
                    tunnel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tunnel = null;
            }
        }
    }

    public void finish() {
        super.finish();
    }

    private void pickIp(String serverIP, int serverPort, String key) {
        @SuppressLint("DefaultLocale") String api = String.format("https://%s:%d/register/pick/ip", serverIP, serverPort);
        String resp = "";
        try {
            resp = HttpUtil.get(api, "key", key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, String.format("get api:%s resp:%s", api, resp));
        if (TextUtils.isEmpty(resp)) {
            return;
        }
        String[] ip = resp.split("/");
        if (ip.length == 2) {
            this.localIP = ip[0];
            this.localPrefixLength = Integer.parseInt(ip[1]);
        } else {
            this.localIP = AppConst.DEFAULT_LOCAL_ADDRESS;
            this.localPrefixLength = AppConst.DEFAULT_LOCAL_PREFIX_LENGTH;
        }
    }
}
