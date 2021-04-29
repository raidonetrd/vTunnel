package com.netbyte.vtun.thread;

import android.widget.TextView;

import com.netbyte.vtun.config.AppConst;

public class StatThread implements Runnable {

    private TextView textView;

    public StatThread(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void run() {
        while (AppConst.STAT_THREAD_RUNNABLE) {
            try {
                textView.setText(String.format("Network: up %dKB down %dKB", AppConst.UP_BYTE.get() / 1024, AppConst.DOWN_BYTE.get() / 1024));
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
