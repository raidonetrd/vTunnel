package com.netbyte.vtunnel.thread;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.model.Config;
import com.netbyte.vtunnel.service.IPService;
import com.netbyte.vtunnel.service.SimpleVPNService;
import com.netbyte.vtunnel.ws.WsClient;
import com.netbyte.vtunnel.utils.SSLUtil;
import com.netbyte.vtunnel.utils.CipherUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

public class WsThread extends VpnThread {
    private static final String TAG = "WsThread";

    public WsThread(Config config, CipherUtil cipherUtil, SimpleVPNService vpnService, IPService ipService) {
        this.config = config;
        this.cipherUtil = cipherUtil;
        this.vpnService = vpnService;
        this.ipService = ipService;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        WsClient wsClient = null;
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            Log.i(TAG, "start");
            this.localIP = ipService.pickIp();
            super.initTunnel();
            if (Objects.isNull(this.tunnel)) {
                return;
            }
            in = new FileInputStream(tunnel.getFileDescriptor());
            out = new FileOutputStream(tunnel.getFileDescriptor());
            @SuppressLint("DefaultLocale") String uri = String.format("wss://%s:%d/way-to-freedom", config.getServerIP(), config.getServerPort());
            wsClient = new WsClient(new URI(uri), cipherUtil, config);
            wsClient.setSocketFactory(SSLUtil.createEasySSLContext().getSocketFactory());
            wsClient.connectBlocking();
            wsClient.setOutStream(out);
            while (RUNNING) {
                try {
                    byte[] buf = new byte[AppConst.BUFFER_SIZE];
                    int ln = in.read(buf);
                    if (ln > 0) {
                        if (wsClient.isOpen()) {
                            byte[] data = Arrays.copyOfRange(buf, 0, ln);
                            if (config.isObfuscate()) {
                                data = cipherUtil.xor(data);
                            }
                            wsClient.send(data);
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

    public void stopRunning() {
        super.stopRunning();
    }

}
