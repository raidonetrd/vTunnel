package com.netbyte.vtun;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.system.OsConstants;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


public class VTunService extends VpnService {
    final static String ACTION_DISCONNECT = "disconnect";
    final static int MAX_PACKET_SIZE = 1500;
    private static String serverIP, localIP;
    private static int localPrefixLength = 24;
    private static int serverPort;
    private static String dns;
    private static String protocol = "udp";
    private static String token = "";
    private Thread udpThread, wsThread;
    private ParcelFileDescriptor localTunnel;
    private PendingIntent pendingIntent;
    private VCipher vCipher;

    public VTunService() {
    }

    @Override
    public void onCreate() {
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("VTun", "start: " + intent.getAction());
        try {
            if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
                disconnect();
                return START_NOT_STICKY;
            } else {
                Bundle ex = intent.getExtras();
                serverIP = ex.getString("serverIP");
                serverPort = ex.getInt("serverPort");
                protocol = ex.getString("protocol");
                String[] localIPArray = ex.getString("localIP").split("/");
                if (localIPArray.length >= 1) {
                    localIP = localIPArray[0];
                }
                if (localIPArray.length >= 2) {
                    localPrefixLength = Integer.parseInt(localIPArray[1]);
                }

                dns = ex.getString("dns");
                token = ex.getString("token");
                vCipher = new VCipher(token);

                String chanId = createNotificationChannel("VTun", "VTun");
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, chanId);
                builder.setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("VTun")
                        .setContentText("Server connected")
                        .setWhen(System.currentTimeMillis());
                Notification notification = builder.build();
                startForeground(1, notification);
                connect();
            }
        } catch (Exception e) {
            Log.e("VTun", e.toString());
        }
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return "";
        }
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        NotificationManager service = getSystemService(NotificationManager.class);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initTun() throws PackageManager.NameNotFoundException {
        VpnService.Builder builder = VTunService.this.new Builder();
        builder.setMtu(1500)
                .addAddress(localIP, localPrefixLength)
                .addRoute("0.0.0.0", 0)
                .addDnsServer(dns)
                .setSession("VTun")
                .setConfigureIntent(null)
                .allowFamily(OsConstants.AF_INET)
                .setBlocking(true);
        for (String packageName : Whitelist.packageList) {
            builder.addDisallowedApplication(packageName);
        }
        this.localTunnel = builder.establish();
        Log.i("initTun", "done");
    }

    private void initUdpThread() {
        udpThread = new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                try {
                    Log.i("udpThread", "start");
                    initTun();
                    final DatagramChannel udp = DatagramChannel.open();
                    SocketAddress serverAdd = new InetSocketAddress(serverIP, serverPort);
                    udp.connect(serverAdd);
                    udp.configureBlocking(false);
                    VTunService.this.protect(udp.socket());
                    FileInputStream in = new FileInputStream(localTunnel.getFileDescriptor());
                    FileOutputStream out = new FileOutputStream(localTunnel.getFileDescriptor());
                    while (!isInterrupted()) {
                        try {
                            byte[] buf = new byte[MAX_PACKET_SIZE];
                            int ln = in.read(buf);
                            if (ln > 0) {
                                byte[] data = Arrays.copyOfRange(buf, 0, ln);
                                ByteBuffer bf = ByteBuffer.wrap(vCipher.encrypt(data));
                                udp.write(bf);
                                MainActivity.upByte.addAndGet(ln);
                            }

                            ByteBuffer bf = ByteBuffer.allocate(MAX_PACKET_SIZE);
                            ln = udp.read(bf);
                            if (ln > 0) {
                                bf.limit(ln);
                                bf.rewind();
                                buf = new byte[ln];
                                bf.get(buf);
                                out.write(vCipher.decrypt(buf));
                                MainActivity.downByte.addAndGet(ln);
                            }
                        } catch (Exception e) {
                            Log.e("udpThread", e.toString());
                        }
                    }
                    Log.i("udpThread", "stop");
                } catch (Exception e) {
                    Log.e("udpThread", e.toString());
                }
            }
        };
    }

    private void initWsThread() {
        wsThread = new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                WSClient wsClient = null;
                try {
                    Log.i("wsThread", "start");
                    initTun();
                    String uri = String.format("wss://%s:%d/way-to-freedom", serverIP, serverPort);
                    wsClient = new WSClient(new URI(uri), vCipher);
                    SSLContext sslContext = createEasySSLContext();
                    SSLSocketFactory factory = sslContext.getSocketFactory();
                    wsClient.setSocketFactory(factory);
                    wsClient.connectBlocking();
                    FileInputStream in = new FileInputStream(localTunnel.getFileDescriptor());
                    FileOutputStream out = new FileOutputStream(localTunnel.getFileDescriptor());
                    wsClient.setTunOutStream(out);
                    while (!isInterrupted()) {
                        try {
                            byte[] buf = new byte[MAX_PACKET_SIZE];
                            int ln = in.read(buf);
                            if (ln > 0) {
                                if (wsClient.isClosing()) {
                                    Thread.sleep(1000);
                                }
                                if (wsClient.isClosed()) {
                                    wsClient.reconnectBlocking();
                                    Thread.sleep(3000);
                                    Log.i("wsThread", "ws client reconnect");
                                }
                                if (wsClient.isOpen()) {
                                    byte[] data = Arrays.copyOfRange(buf, 0, ln);
                                    wsClient.send(vCipher.encrypt(data));
                                    MainActivity.upByte.addAndGet(ln);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("wsThread", e.toString());
                        }
                    }
                    Log.i("wsThread", "stop");
                } catch (Exception e) {
                    Log.e("wsThread", e.toString());
                } finally {
                    if (wsClient != null) {
                        wsClient.close();
                    }
                }
            }
        };
    }

    private SSLContext createEasySSLContext() throws IOException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new TrivialTrustManager()}, null);
            return context;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private class TrivialTrustManager implements javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain, String authType) {
        }
    }

    private void close() {
        try {
            if (udpThread != null) {
                udpThread.interrupt();
                udpThread = null;
            }
            if (wsThread != null) {
                wsThread.interrupt();
                wsThread = null;
            }
            if (localTunnel != null) {
                localTunnel.close();
                localTunnel = null;
            }
        } catch (Exception e) {
            Log.e("VTun", e.toString());
        }
    }

    private void disconnect() {
        Log.i("VTun", "disconnecting...");
        try {
            close();
            stopForeground(true);
        } catch (Exception e) {
            Log.e("VTun", e.toString());
        }
    }

    private void connect() {
        Log.i("VTun", "connecting...");
        Log.i("VTun", serverIP + " " + serverPort + " " + localIP + " " + dns);
        try {
            close();
            if (protocol.equals("udp")) {
                initUdpThread();
                udpThread.start();
            } else if (protocol.equals("ws")) {
                initWsThread();
                wsThread.start();
            }
        } catch (Exception e) {
            Log.e("VTun", e.toString());
        }
    }
}
