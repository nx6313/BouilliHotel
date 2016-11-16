package com.bouilli.nxx.bouillihotel.fragment.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bouilli.nxx.bouillihotel.fragment.SelectMenuFragment;

/**
 * Created by 18230 on 2016/11/5.
 */

public class SelectMenuFragmentPageAdapter extends FragmentPagerAdapter {
    private int fragmentCount = 0;

    public SelectMenuFragmentPageAdapter(FragmentManager fm, int fragmentCount) {
        super(fm);
        this.fragmentCount = fragmentCount;
    }

    @Override
    public int getCount() {
        return fragmentCount;
    }

    @Override
    public Fragment getItem(int position) {
        return SelectMenuFragment.newInstance(position);
    }
}
