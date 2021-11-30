package com.netbyte.vtunnel.ws;


import android.util.Log;

import com.netbyte.vtunnel.model.Config;
import com.netbyte.vtunnel.model.Stat;
import com.netbyte.vtunnel.utils.CipherUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WsClient extends WebSocketClient {
    private static final String TAG = "WsClient";
    private final Config config;
    private FileOutputStream out;

    public WsClient(URI serverUri, Config config) {
        super(serverUri);
        this.config = config;
    }

    public void setOutStream(FileOutputStream out) {
        this.out = out;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.i(TAG, "onOpen");
    }

    @Override
    public void onMessage(String message) {
        Log.i(TAG, "onMessage:" + message);
    }

    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        if (out == null || byteBuffer.remaining() == 0) {
            return;
        }
        byte[] buf = new byte[byteBuffer.remaining()];
        byteBuffer.get(buf);
        if (config.isObfuscate()) {
            buf = CipherUtil.xor(buf, config.getKey().getBytes(StandardCharsets.UTF_8));
        }
        Stat.DOWNLOAD_BYTES.addAndGet(buf.length);
        try {
            out.write(buf);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "code:" + code + " reason:" + reason + "remote:" + remote);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, ex.getMessage());
    }
}
