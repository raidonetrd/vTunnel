package com.netbyte.vtunnel.model;


import java.io.Serializable;

public class LocalIp implements Serializable {
    private String localIp;
    private int localPrefixLength;

    public LocalIp(String localIp, int localPrefixLength) {
        this.localIp = localIp;
        this.localPrefixLength = localPrefixLength;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public int getLocalPrefixLength() {
        return localPrefixLength;
    }

    public void setLocalPrefixLength(int localPrefixLength) {
        this.localPrefixLength = localPrefixLength;
    }
}
