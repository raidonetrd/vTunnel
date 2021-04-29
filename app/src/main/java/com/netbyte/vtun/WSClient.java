package com.netbyte.vtun;


import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

public class WSClient extends WebSocketClient {

    private FileOutputStream tunOutStream;
    private VCipher vCipher;

    public WSClient(URI serverUri, VCipher vCipher) {
        super(serverUri);
        this.vCipher = vCipher;

    }

    public void setTunOutStream(FileOutputStream tunOutStream) {
        this.tunOutStream = tunOutStream;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.i("WSClient", "ws client is open");
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        if (tunOutStream == null || byteBuffer.remaining() == 0) {
            return;
        }
        byte[] buf = new byte[byteBuffer.remaining()];
        byteBuffer.get(buf);
        try {
            byte[] data = vCipher.decrypt(buf);
            MainActivity.downByte.addAndGet(data.length);
            tunOutStream.write(data);
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
