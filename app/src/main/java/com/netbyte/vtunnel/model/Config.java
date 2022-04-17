package com.netbyte.vtunnel.model;


import java.io.Serializable;

public class Config implements Serializable {
    private String serverAddress;
    private int serverPort;
    private String path;
    private String dns;
    private String key;
    private boolean obfs;
    private boolean wss;
    private String bypassApps;

    public Config(String serverAddress, int serverPort, String path, String dns, String key, String bypassApps, boolean obfs, boolean wss) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.path = path;
        this.dns = dns;
        this.key = key;
        this.bypassApps = bypassApps;
        this.obfs = obfs;
        this.wss = wss;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public boolean isObfs() {
        return obfs;
    }

    public void setObfs(boolean obfs) {
        this.obfs = obfs;
    }

    public boolean isWss() {
        return wss;
    }

    public void setWss(boolean wss) {
        this.wss = wss;
    }

    public String getBypassApps() {
        return bypassApps;
    }

    public void setBypassApps(String bypassApps) {
        this.bypassApps = bypassApps;
    }


    @Override
    public String toString() {
        return "Config{" +
                "serverAddress='" + serverAddress + '\'' +
                ", serverPort=" + serverPort +
                ", path='" + path + '\'' +
                ", dns='" + dns + '\'' +
                ", key='" + key + '\'' +
                ", obfs=" + obfs +
                ", wss=" + wss +
                ", bypassApps='" + bypassApps + '\'' +
                '}';
    }
}
