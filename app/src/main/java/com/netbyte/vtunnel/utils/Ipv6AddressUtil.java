package com.netbyte.vtunnel.utils;

import java.net.Inet6Address;
import java.net.UnknownHostException;

public class Ipv6AddressUtil {
    public static String randomString(String ipv6Net, int length) throws UnknownHostException {
        return randomString(string2ByteArray(ipv6Net), length);
    }

    public static String randomString(byte[] ipv6Net, int length) throws UnknownHostException {
        return Inet6Address.getByAddress(random(ipv6Net, length)).toString().replaceFirst("/", "");
    }

    public static byte[] random(byte[] ipv6Net, int length) {
        byte[] suffix = ByteArrayUtil.nextBits(128 - length);
        return ByteArrayUtil.or(ipv6Net, suffix);
    }

    public static byte[] string2ByteArray(String ipv6) throws UnknownHostException {
        return Inet6Address.getByName(ipv6).getAddress();
    }
}
