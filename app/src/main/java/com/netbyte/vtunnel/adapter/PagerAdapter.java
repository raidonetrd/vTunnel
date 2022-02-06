package com.netbyte.vtunnel.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.netbyte.vtunnel.activity.AppsFragment;
import com.netbyte.vtunnel.activity.ConfigsFragment;
import com.netbyte.vtunnel.activity.HomeFragment;

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
                return new HomeFragment();
            case 1:
                return new ConfigsFragment();
            case 2:
                return new AppsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
