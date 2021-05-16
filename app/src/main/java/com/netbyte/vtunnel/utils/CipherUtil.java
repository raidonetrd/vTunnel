package com.netbyte.vtunnel.utils;

public class CipherUtil {
    private static String key = "8pUsXuZw4z6B9EhGdKgNjQnjmVsYv2x5";

    public CipherUtil(String key) {
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

    public String getKey() {
        return key;
    }

    static class RC4 {
        final byte[] S = new byte[256];
        final byte[] T = new byte[256];
        final int keyLength;

        public RC4(final byte[] key) {
            if (key.length < 1 || key.length > 256) {
                throw new IllegalArgumentException(
                        "key must be between 1 and 256 bytes");
            } else {
                keyLength = key.length;
                for (int i = 0; i < 256; i++) {
                    S[i] = (byte) i;
                    T[i] = key[i % keyLength];
                }
                int j = 0;
                byte tmp;
                for (int i = 0; i < 256; i++) {
                    j = (j + S[i] + T[i]) & 0xFF;
                    tmp = S[j];
                    S[j] = S[i];
                    S[i] = tmp;
                }
            }
        }

        public byte[] encrypt(final byte[] plaintext) {
            final byte[] cipherText = new byte[plaintext.length];
            int i = 0, j = 0, k, t;
            byte tmp;
            for (int counter = 0; counter < plaintext.length; counter++) {
                i = (i + 1) & 0xFF;
                j = (j + S[i]) & 0xFF;
                tmp = S[j];
                S[j] = S[i];
                S[i] = tmp;
                t = (S[i] + S[j]) & 0xFF;
                k = S[t];
                cipherText[counter] = (byte) (plaintext[counter] ^ k);
            }
            return cipherText;
        }

        public byte[] decrypt(final byte[] ciphertext) {
            return encrypt(ciphertext);
        }
    }

}

