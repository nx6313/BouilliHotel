package com.bouilli.nxx.bouillihotel.fragment.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bouilli.nxx.bouillihotel.fragment.OrderEveryFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 18230 on 2016/11/5.
 */

public class OrderEveryFragmentPageAdapter extends FragmentStatePagerAdapter {
    private List<Map<String, Object[]>> tableReadyOrderList = new ArrayList<>();// 保存该餐桌正在制作中的菜品信息
    private List<Map<String, Object[]>> tableHasNewOrderList = new ArrayList<>();// 保存该餐桌正在制作中的菜品信息

    public OrderEveryFragmentPageAdapter(FragmentManager fm,
                                         List<Map<String, Object[]>> tableReadyOrderList, List<Map<String, Object[]>> tableHasNewOrderList) {
        super(fm);
        this.tableReadyOrderList = tableReadyOrderList;
        this.tableHasNewOrderList = tableHasNewOrderList;
    }

    @Override
    public int getCount() {
        return tableReadyOrderList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return OrderEveryFragment.newInstance(position, tableReadyOrderList, tableHasNewOrderList);
    }

    @Override
    public int getItemPosition(Object object) {
        //return super.getItemPosition(object);
        return POSITION_NONE;
    }
}
