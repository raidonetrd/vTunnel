package com.netbyte.vtunnel.thread;

import android.net.VpnService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.ws.WSClient;
import com.netbyte.vtunnel.utils.SSLUtil;
import com.netbyte.vtunnel.utils.VCipher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class WsThread extends VpnThread {

    public WsThread(String serverIP, int serverPort, String localIp, int localPrefixLength, String dns, VCipher vCipher, VpnService vpnService) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.localIP = localIp;
        this.localPrefixLength = localPrefixLength;
        this.dns = dns;
        this.vCipher = vCipher;
        this.vpnService = vpnService;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        WSClient wsClient = null;
        try {
            Log.i("WsThread", "start");
            super.initTunnel();
            String uri = String.format("wss://%s:%d/way-to-freedom", serverIP, serverPort);
            wsClient = new WSClient(new URI(uri), vCipher);
            SSLContext sslContext = SSLUtil.createEasySSLContext();
            SSLSocketFactory factory = sslContext.getSocketFactory();
            wsClient.setSocketFactory(factory);
            wsClient.connectBlocking();
            FileInputStream in = new FileInputStream(tunnel.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(tunnel.getFileDescriptor());
            wsClient.setOutStream(out);
            while (THREAD_RUNNABLE) {
                try {
                    byte[] buf = new byte[AppConst.BUFFER_SIZE];
                    int ln = in.read(buf);
                    if (ln > 0) {
                        if (wsClient.isOpen()) {
                            byte[] data = Arrays.copyOfRange(buf, 0, ln);
                            wsClient.send(vCipher.encrypt(data));
                            AppConst.UP_BYTE.addAndGet(ln);
                        } else if (wsClient.isClosed()) {
                            wsClient.reconnectBlocking();
                            sleep(1000);
                            Log.i("WsThread", "ws reconnect...");
                        } else {
                            sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    Log.e("WsThread", "error on WsThread:" + e.toString());
                }
            }
            Log.i("WsThread", "stop");
        } catch (Exception e) {
            Log.e("WsThread", "error on WsThread:" + e.toString());
        } finally {
            if (wsClient != null) {
                wsClient.close();
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
}
