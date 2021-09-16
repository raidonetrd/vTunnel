package com.netbyte.vtunnel.model;

import java.io.Serializable;

public class App implements Serializable {
    private String name;
    private String packageName;
    private boolean bypass;

    public App(String name, String packageName, boolean bypass) {
        this.name = name;
        this.packageName = packageName;
        this.bypass = bypass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    @Override
    public String toString() {
        return this.name +":"+ this.packageName;
    }
}
