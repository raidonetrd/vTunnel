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

public class WSClient extends WebSocketClient {

    private FileOutputStream out;
    private CipherUtil vCipher;

    public WSClient(URI serverUri, CipherUtil vCipher) {
        super(serverUri);
        this.vCipher = vCipher;
    }

    public void setOutStream(FileOutputStream out) {
        this.out = out;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.i("WSClient", "onOpen");
    }

    @Override
    public void onMessage(String message) {
        Log.i("WSClient", "onMessage:" + message);
    }

    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        if (out == null || byteBuffer.remaining() == 0) {
            return;
        }
        byte[] buf = new byte[byteBuffer.remaining()];
        byteBuffer.get(buf);
        try {
            byte[] data = vCipher.decrypt(buf);
            AppConst.DOWN_BYTE.addAndGet(data.length);
            out.write(data);
        } catch (IOException e) {
            Log.e("WSClient", e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("WSClient", "code:" + code + " reason:" + reason + "remote:" + remote);
    }

    @Override
    public void onError(Exception ex) {
        Log.e("WSClient", ex.getMessage());
    }
}
