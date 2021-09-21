package com.netbyte.vtunnel.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class App implements Serializable {
    private Drawable icon;
    private String name;
    private String packageName;
    private boolean bypass;

    public App(Drawable icon,String name, String packageName, boolean bypass) {
        this.icon = icon;
        this.name = name;
        this.packageName = packageName;
        this.bypass = bypass;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
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
        return this.name;
    }
}
