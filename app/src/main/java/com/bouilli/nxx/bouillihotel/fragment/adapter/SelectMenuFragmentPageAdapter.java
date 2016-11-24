package com.bouilli.nxx.bouillihotel.fragment.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.customview.AmountView;
import com.bouilli.nxx.bouillihotel.fragment.SelectMenuFragment;
import com.bouilli.nxx.bouillihotel.util.ComFun;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 18230 on 2016/11/5.
 */

public class SelectMenuFragmentPageAdapter extends FragmentPagerAdapter {
    public static List<String> fragmentTagNameList = new ArrayList<>();
    private static FragmentManager fm;
    private static int fragmentCount = 0;

    public SelectMenuFragmentPageAdapter(FragmentManager fm, int fragmentCount) {
        super(fm);
        this.fm = fm;
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

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        fragmentTagNameList.add(makeFragmentName(container.getId(), getItemId(position)));
        return super.instantiateItem(container, position);
    }

    public static String makeFragmentName(int viewId, Long index){
        return "android:switcher:" + viewId + ":" + index;
    }

}
