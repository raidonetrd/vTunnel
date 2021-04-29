package com.netbyte.vtun.config;

import java.util.concurrent.atomic.AtomicLong;

public class AppConst {

    public static final String APP_NAME = "vTunnel";
    public static final String APP_PACKAGE_NAME = "com.netbyte.vtun";
    public static final String DEFAULT_TAG = "vTunnel";
    public static final int BUFFER_SIZE = 1500;
    public static final int MTU = 1500;
    public static final String DEFAULT_SERVER_IP = "192.168.0.1";
    public static final String DEFAULT_SERVER_PORT = "443";
    public static final String DEFAULT_LOCAL_IP = "172.16.0.20/24";
    public static final int DEFAULT_LOCAL_PREFIX_LENGTH = 24;
    public static final String DEFAULT_TOKEN = "6w9z$C&F)J@NcRfWjXn3r4u7x!A%D*G-";
    public static final String DEFAULT_PROTOCOL = "ws";
    public static final String DEFAULT_DNS = "223.5.5.5";
    public static final String DEFAULT_ROUTE = "0.0.0.0";

    public static final String BTN_ACTION_CONNECT = "connect";
    public static final String BTN_ACTION_DISCONNECT = "disconnect";

    public static volatile boolean UDP_THREAD_RUNNABLE = true;
    public static volatile boolean WS_THREAD_RUNNABLE = true;
    public static volatile boolean STAT_THREAD_RUNNABLE = true;

    public static AtomicLong DOWN_BYTE = new AtomicLong(0);
    public static AtomicLong UP_BYTE = new AtomicLong(0);

}
