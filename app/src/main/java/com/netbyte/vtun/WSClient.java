package com.netbyte.vtun;


import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

public class WSClient extends WebSocketClient {

    private ParcelFileDescriptor tun;
    private FileOutputStream out;
    private VCipher vCipher;

    public WSClient(URI serverUri, ParcelFileDescriptor tun, VCipher vCipher) {
        super(serverUri);
        this.tun = tun;
        this.vCipher = vCipher;
        this.out = new FileOutputStream(tun.getFileDescriptor());
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.i("WSClient", "open");
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() == 0) {
            return;
        }
        byte[] buf = new byte[byteBuffer.remaining()];
        byteBuffer.get(buf);
        try {
            out.write(vCipher.decrypt(buf));
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
