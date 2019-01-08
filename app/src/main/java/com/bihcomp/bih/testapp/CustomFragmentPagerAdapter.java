package com.bihcomp.bih.testapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class CustomFragmentPagerAdapter extends FragmentPagerAdapter {
    private String[] VIEW_MAPNTOP_TITLES = {"연락처","갤러리","부다스핸드"};
    private ArrayList<Fragment> fList;

    // 아답터 생성자
    public CustomFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fList) {
        super(fm);
        this.fList = fList;
    }

    // tab title 반환 함수.
    @Override
    public CharSequence getPageTitle(int position) {

        return VIEW_MAPNTOP_TITLES[position];

    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    // 해당 프라그먼트 호출 함수.
    @Override
    public Fragment getItem(int position) {
        return this.fList.get(position);
    }

    @Override
    public int getCount() {
        return fList.size();
    }
}
