package com.netbyte.vtunnel.model;


import java.io.Serializable;

public class LocalIP implements Serializable {
    private String localIP;
    private int localPrefixLength;

    public LocalIP(String localIP, int localPrefixLength) {
        this.localIP = localIP;
        this.localPrefixLength = localPrefixLength;
    }

    public String getLocalIP() {
        return localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public int getLocalPrefixLength() {
        return localPrefixLength;
    }

    public void setLocalPrefixLength(int localPrefixLength) {
        this.localPrefixLength = localPrefixLength;
    }
}
