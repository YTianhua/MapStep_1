package com.yth520web.mapstep;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyPagerAdapter extends FragmentPagerAdapter {
    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        if (position==0){
            return new PageFirst();
        }else{
            return new PageSecond();
        }
    }
    @Override
    public int getCount() {
        return 2;
    }
    //获得getPageTitle
    @Override
    public CharSequence getPageTitle(int position) {
        if(position==0){
            return "记录轨迹";
        }else{
            return "跑步记录";
        }
    }
}
