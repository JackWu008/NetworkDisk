package net.lzzy.networkdisk.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;


public class SimplePagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private String[] strings;


    public SimplePagerAdapter(FragmentManager fm, List<Fragment> fragments, String[] strings) {
        super(fm);
        this.fragments = fragments;
        this.strings = strings;
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return strings[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {//不销毁

    }

        @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);


    }


}
