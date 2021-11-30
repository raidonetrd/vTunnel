package com.netbyte.vtunnel.model;


import java.io.Serializable;

public class Config implements Serializable {

    private String serverAddress;
    private int serverPort;
    private String dns;
    private String key;
    private boolean obfuscate;
    private String bypassApps;

    public Config(String serverAddress, int serverPort, String dns, String key, String bypassApps, boolean obfuscate) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.dns = dns;
        this.key = key;
        this.bypassApps = bypassApps;
        this.obfuscate = obfuscate;

    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
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

    public String getBypassApps() {
        return bypassApps;
    }

    public void setBypassApps(String bypassApps) {
        this.bypassApps = bypassApps;
    }

}
