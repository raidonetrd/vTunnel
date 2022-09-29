package com.netbyte.vtunnel.model;


import java.io.Serializable;

public class Config implements Serializable {
    private String serverAddress;
    private int serverPort;
    private String path;
    private String dns;
    private String key;
    private String obfs;
    private String proto;
    private String bypassApps;

    public Config(String serverAddress, int serverPort, String path, String dns, String key, String obfs, String proto, String bypassApps) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.path = path;
        this.dns = dns;
        this.key = key;
        this.obfs = obfs;
        this.proto = proto;
        this.bypassApps = bypassApps;
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

    public String getObfs() {
        return obfs;
    }

    public void setObfs(String obfs) {
        this.obfs = obfs;
    }

    public String getProto() {
        return proto;
    }

    public void setProto(String proto) {
        this.proto = proto;
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
                ", obfs='" + obfs + '\'' +
                ", proto='" + proto + '\'' +
                ", bypassApps='" + bypassApps + '\'' +
                '}';
    }
}
