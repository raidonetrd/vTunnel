package com.netbyte.vtun;

public class VCipher {
    private static String key = "8pUsXuZw4z6B9EhGdKgNjQnjmVsYv2x5";

    public VCipher() {

    }

    public VCipher(String key) {
        this.key = key;
    }

    public byte[] encrypt(byte[] data) {
        RC4 rc4 = new RC4(key.getBytes());
        return rc4.encrypt(data);
    }

    public byte[] decrypt(byte[] data) {
        RC4 rc4 = new RC4(key.getBytes());
        return rc4.decrypt(data);
    }

}