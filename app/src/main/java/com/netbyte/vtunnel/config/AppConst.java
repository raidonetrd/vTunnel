package com.netbyte.vtunnel.config;

import java.util.concurrent.atomic.AtomicLong;

public class AppConst {

    public static final String APP_NAME = "vTunnel";
    public static final String APP_PACKAGE_NAME = "com.netbyte.vtunnel";
    public static final String NOTIFICATION_CHANNEL_ID = "vTunnel";
    public static final String NOTIFICATION_CHANNEL_NAME = "vTunnel";
    public static final int NOTIFICATION_ID = 911;
    public static final String DEFAULT_TAG = "vTunnel";
    public static final int BUFFER_SIZE = 1500;
    public static final int MTU = 1500;
    public static final String DEFAULT_SERVER_ADDRESS = "192.168.0.1";
    public static final String DEFAULT_SERVER_PORT = "443";
    public static final String DEFAULT_LOCAL_ADDRESS = "172.16.0.253";
    public static final int DEFAULT_LOCAL_PREFIX_LENGTH = 24;
    public static final String DEFAULT_KEY = "1233456";
    public static final String PROTOCOL_WS = "websocket";
    public static final String PROTOCOL_UDP = "udp";
    public static final String DEFAULT_DNS = "223.5.5.5";
    public static final String DEFAULT_ROUTE = "0.0.0.0";

    public static final String BTN_ACTION_CONNECT = "connect";
    public static final String BTN_ACTION_DISCONNECT = "disconnect";

    public static AtomicLong DOWN_BYTE = new AtomicLong(0);
    public static AtomicLong UP_BYTE = new AtomicLong(0);
    public static String LOCAL_ADDRESS = "";

}
