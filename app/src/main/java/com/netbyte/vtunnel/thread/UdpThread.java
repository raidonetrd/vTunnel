package com.netbyte.vtunnel.thread;

import android.net.VpnService;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.utils.CipherUtil;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

public class UdpThread extends VpnThread {
    private static final String TAG = "UdpThread";

    public UdpThread(String serverIP, int serverPort, String localIp, int localPrefixLength, String dns, CipherUtil cipherUtil, VpnService vpnService) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.localIP = localIp;
        this.localPrefixLength = localPrefixLength;
        this.dns = dns;
        this.cipherUtil = cipherUtil;
        this.vpnService = vpnService;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        FileInputStream in = null;
        FileOutputStream out = null;
        DatagramChannel udp = null;
        try {
            Log.i(TAG, "start");
            super.initTunnel();
            in = new FileInputStream(tunnel.getFileDescriptor());
            out = new FileOutputStream(tunnel.getFileDescriptor());
            udp = DatagramChannel.open();
            SocketAddress socketAddress = new InetSocketAddress(serverIP, serverPort);
            udp.connect(socketAddress);
            udp.configureBlocking(false);
            vpnService.protect(udp.socket());
            while (THREAD_RUNNABLE) {
                try {
                    byte[] buf = new byte[AppConst.BUFFER_SIZE];
                    int ln = in.read(buf);
                    if (ln > 0) {
                        byte[] data = Arrays.copyOfRange(buf, 0, ln);
                        ByteBuffer bf = ByteBuffer.wrap(cipherUtil.encrypt(data));
                        udp.write(bf);
                        AppConst.UP_BYTE.addAndGet(ln);
                    }

                    ByteBuffer bf = ByteBuffer.allocate(AppConst.BUFFER_SIZE);
                    ln = udp.read(bf);
                    if (ln > 0) {
                        bf.limit(ln);
                        bf.rewind();
                        buf = new byte[ln];
                        bf.get(buf);
                        out.write(cipherUtil.decrypt(buf));
                        AppConst.DOWN_BYTE.addAndGet(ln);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
            Log.i(TAG, "stop");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (udp != null) {
                try {
                    udp.disconnect();
                    udp.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
}
