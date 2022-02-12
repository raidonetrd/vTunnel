package com.netbyte.vtunnel.model;

import java.util.concurrent.atomic.AtomicLong;

public class Stats {
    public static volatile AtomicLong DOWNLOAD_BYTES = new AtomicLong(0);
    public static volatile AtomicLong UPLOAD_BYTES = new AtomicLong(0);
    public static volatile AtomicLong TOTAL_BYTES = new AtomicLong(0);
}
