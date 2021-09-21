package com.netbyte.vtunnel.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.netbyte.vtunnel.activity.BypassTab;
import com.netbyte.vtunnel.activity.HomeTab;
import com.netbyte.vtunnel.activity.SettingsTab;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int tabCount;

    public PagerAdapter(FragmentManager fm, int tabCount) {
        super(fm, tabCount);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomeTab();
            case 1:
                return new SettingsTab();
            case 2:
                return new BypassTab();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
