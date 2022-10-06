package com.netbyte.vtunnel.ws;

import android.util.Log;

import com.netbyte.vtunnel.model.Config;
import com.netbyte.vtunnel.model.Stats;
import com.netbyte.vtunnel.utils.CipherUtil;

import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MyWebSocketListener implements WebSocketListener {
    private static final String TAG = "MyWebSocketListener";
    private Config config;
    private FileOutputStream out;

    public MyWebSocketListener() {

    }

    public MyWebSocketListener(Config config, FileOutputStream out) {
        this.config = config;
        this.out = out;
    }

    @Override
    public void onOpen(WebSocket websocket) {
        Log.i(TAG, "onOpen");
    }

    @Override
    public void onClose(WebSocket websocket, int code, String reason) {
        Log.i(TAG, "onClose code:" + code + " reason:" + reason);
    }

    @Override
    public void onError(Throwable t) {
        Log.e(TAG, "onError " + t.getMessage());
    }

    @Override
    public void onBinaryFrame(byte[] payload, boolean finalFragment, int rsv) {
        if (out == null || config == null || payload == null || payload.length == 0) {
            return;
        }
        if (Objects.equals(config.getObfs(), "on")) {
            payload = CipherUtil.xor(payload, config.getKey().getBytes(StandardCharsets.UTF_8));
        }
        try {
            out.write(payload);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        Stats.DOWNLOAD_BYTES.addAndGet(payload.length);
    }
}
