package com.netbyte.vtun.utils;

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

    private static class TrivialTrustManager implements javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain, String authType) {
        }
    }

}
