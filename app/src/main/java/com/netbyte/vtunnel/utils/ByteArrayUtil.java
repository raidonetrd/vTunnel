package com.netbyte.vtunnel.utils;

import java.util.Random;

public class ByteArrayUtil {
    public static byte[] nextBits(int length) {
        int size = length / 8;
        int remainder = length % 8;
        boolean flag = remainder != 0;
        if (flag) {
            size++;
        }
        byte[] result = new byte[size];
        new Random(System.currentTimeMillis()).nextBytes(result);
        if (flag) {
            result[0] = (byte) (result[0] & (0xff >> (8-remainder)));
        }
        return result;
    }

    public static byte[] and(byte[] op1, byte[] op2) {
        int len1 = op1.length;
        int len2 = op2.length;
        byte[] lop = op1;
        byte[] rop = op2;
        int lLen = len1;
        int rLen = len2;
        if(len1 < len2) {
            lop = rop;
            rop = op1;
            lLen = rLen;
            rLen = len1;
        }
        for (int i = 0; i< rLen; i++) {
            lop[lLen - 1 - i] = (byte) (lop[lLen - 1 - i] & rop[rLen - 1 - i]);
        }
        return lop;
    }

    public static byte[] or(byte[] op1, byte[] op2) {
        int len1 = op1.length;
        int len2 = op2.length;
        byte[]lop = op1;
        byte[]rop = op2;
        int lLen = len1;
        int rLen = len2;
        if(len1 < len2) {
            lop = rop;
            rop = op1;
            lLen = rLen;
            rLen = len1;
        }
        for (int i = 0; i<rLen;i++) {
            lop[lLen - 1 - i] = (byte) (lop[lLen - 1 - i] | rop[rLen - 1 - i]);
        }
        return lop;
    }
}
