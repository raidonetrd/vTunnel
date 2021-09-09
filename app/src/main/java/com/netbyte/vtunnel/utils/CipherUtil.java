package com.netbyte.vtunnel.utils;

import java.nio.charset.StandardCharsets;

public class CipherUtil {
    private static byte [] keys = "8pUsXuZw4z6B9EhGdKgNjQnjmVsYv2x5".getBytes(StandardCharsets.UTF_8);

    public CipherUtil(String key) {
        this.keys = key.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] xor(byte[] src) {
        int keyLength = keys.length;
        for (int i = 0; i < src.length; i++) {
            src[i] ^= keys[i%keyLength];
        }
        return src;
    }

}

