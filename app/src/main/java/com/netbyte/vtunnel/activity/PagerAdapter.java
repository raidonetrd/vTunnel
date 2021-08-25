package com.netbyte.vtunnel.activity;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private  int tabCount;

    public PagerAdapter(FragmentManager fm, int tabCount) {
        super(fm,tabCount);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomeTab();
            case 1:
                return new ConfigTab();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
