package com.bouilli.nxx.bouillihotel.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bouilli.nxx.bouillihotel.R;

/**
 * Created by 18230 on 2016/11/5.
 */

public class OutOrderFragment extends Fragment {
    int mNum;// 页号
    private RefDataBroadCastReceive refDataBroadCastReceive;// 刷新数据广播实例
    public static String MSG_REF_OUTORDER_DATA = "requestNewOutOrderDataBouilliHotel";

    public static OutOrderFragment newInstance(int num){
        OutOrderFragment fragment = new OutOrderFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册广播接收器
        refDataBroadCastReceive = new RefDataBroadCastReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction("requestNewOutOrderDataBouilliHotel");
        getActivity().registerReceiver(refDataBroadCastReceive, filter);
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.out_order_fragment_pager, null);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消注册广播
        getActivity().unregisterReceiver(refDataBroadCastReceive);
    }

    public class RefDataBroadCastReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MSG_REF_OUTORDER_DATA)){

            }
        }
    }

}
