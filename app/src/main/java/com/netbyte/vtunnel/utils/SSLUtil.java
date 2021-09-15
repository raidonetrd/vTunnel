package com.netbyte.vtunnel.utils;

import android.annotation.SuppressLint;

import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class SSLUtil {

    public static SSLContext createEasySSLContext() throws IOException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new TrivialTrustManager()}, null);
            return context;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @SuppressLint("CustomX509TrustManager")
    private static class TrivialTrustManager implements javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain, String authType) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain, String authType) {
        }
    }

}
