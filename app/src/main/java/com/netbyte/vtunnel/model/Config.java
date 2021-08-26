package com.netbyte.vtunnel.model;


import java.io.Serializable;

public class Config implements Serializable {
    private String serverIP;
    private int serverPort;
    private String dns;
    private String key;
    private String bypassUrl;

    public Config() {

    }

    public Config(String serverIP, int serverPort, String dns, String key, String bypassUrl) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.dns = dns;
        this.key = key;
        this.bypassUrl = bypassUrl;
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

    public String getBypassUrl() {
        return bypassUrl;
    }

    public void setBypassUrl(String bypassUrl) {
        this.bypassUrl = bypassUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
