package com.netbyte.vtunnel.ws;


import android.util.Log;

import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.utils.CipherUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

public class WsClient extends WebSocketClient {
    private static final String TAG = "WsClient";
    private final CipherUtil cipherUtil;
    private FileOutputStream out;

    public WsClient(URI serverUri, CipherUtil cipherUtil) {
        super(serverUri);
        this.cipherUtil = cipherUtil;
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
        byte[] data = cipherUtil.decrypt(buf);
        AppConst.DOWN_BYTE.addAndGet(data.length);
        try {
            out.write(data);
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
