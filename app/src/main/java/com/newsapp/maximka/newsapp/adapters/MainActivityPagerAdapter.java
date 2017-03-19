package com.newsapp.maximka.newsapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class MainActivityPagerAdapter extends FragmentPagerAdapter {
    private final String[] HEADERS = { "News", "Music" };
    private List<Fragment> pages;

    public MainActivityPagerAdapter(FragmentManager fm, List<Fragment> pages) {
        super(fm);
        this.pages = pages;
    }

    @Override
    public Fragment getItem(int position) {
        return pages.get(position);
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return HEADERS[position];
    }
}
