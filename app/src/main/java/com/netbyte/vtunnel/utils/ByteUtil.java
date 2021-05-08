package com.netbyte.vtunnel.utils;

import java.math.BigDecimal;

public class ByteUtil {

    public static String format(long size) {
        double kiloByte = Long.valueOf(size).doubleValue() / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }
        BigDecimal result;
        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            result = new BigDecimal(Double.toString(kiloByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            result = new BigDecimal(Double.toString(megaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            result = new BigDecimal(Double.toString(gigaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        result = new BigDecimal(teraBytes);
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

}
