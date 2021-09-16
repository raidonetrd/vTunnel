package com.netbyte.vtunnel.utils;

public class CipherUtil {

    public static byte[] xor(byte[] src, byte[] key) {
        int keyLength = key.length;
        for (int i = 0; i < src.length; i++) {
            src[i] ^= key[i % keyLength];
        }
        return src;
    }

}

