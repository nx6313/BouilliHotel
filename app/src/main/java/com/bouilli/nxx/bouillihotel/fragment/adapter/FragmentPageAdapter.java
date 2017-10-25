package com.bouilli.nxx.bouillihotel.fragment.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bouilli.nxx.bouillihotel.fragment.MainFragment;

/**
 * Created by 18230 on 2016/11/5.
 */

public class FragmentPageAdapter extends FragmentPagerAdapter {
    private boolean nullFlag = false;
    private int fragmentCount = 0;

    public FragmentPageAdapter(FragmentManager fm, int fragmentCount) {
        super(fm);
        if (fragmentCount == 0) {
            nullFlag = true;
            this.fragmentCount = 1;
        } else {
            this.fragmentCount = fragmentCount;
        }
    }

    @Override
    public int getCount() {
        return fragmentCount;
    }

    @Override
    public Fragment getItem(int position) {
        return MainFragment.newInstance(position, nullFlag);
    }
}
