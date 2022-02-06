package com.netbyte.vtunnel.utils;

import java.math.BigDecimal;

public class FormatUtil {

    public static String formatByte(long size) {
        if (size <= 1) {
            return size + " Byte";
        }
        if (size < 1024) {
            return size + " Bytes";
        }
        BigDecimal result;
        double kiloByte = Long.valueOf(size).doubleValue() / 1024;
        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            result = new BigDecimal(Double.toString(kiloByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            result = new BigDecimal(Double.toString(megaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            result = new BigDecimal(Double.toString(gigaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " GB";
        }
        result = new BigDecimal(teraBytes);
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " TB";
    }

    public static String formatTime(long second) {
        if (second <= 0) {
            return "00:00:00";
        }
        long days = second / 86400;
        second = second % 86400;
        long hours = second / 3600;
        second = second % 3600;
        long minutes = second / 60;
        second = second % 60;
        if (days > 0) {
            return String.join(":", number2Str(days), number2Str(hours), number2Str(minutes), number2Str(second));
        }
        return String.join(":", number2Str(hours), number2Str(minutes), number2Str(second));
    }

    private static String number2Str(long number) {
        if (number >= 10) {
            return String.valueOf(number);
        } else {
            return "0" + number;
        }
    }

}
