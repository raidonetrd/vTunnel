package com.netbyte.vtunnel.model;


import java.io.Serializable;

public class Config implements Serializable {
    private String serverIP;
    private int serverPort;
    private String dns;
    private String key;
    private String bypassApps;
    private boolean obfuscate;

    public Config(String serverIP, int serverPort, String dns, String key, String bypassUrl, boolean obfuscate) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.dns = dns;
        this.key = key;
        this.bypassApps = bypassUrl;
        this.obfuscate = obfuscate;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public String getBypassApps() {
        return bypassApps;
    }

    public void setBypassApps(String bypassApps) {
        this.bypassApps = bypassApps;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isObfuscate() {
        return obfuscate;
    }

    public void setObfuscate(boolean obfuscate) {
        this.obfuscate = obfuscate;
    }
}
