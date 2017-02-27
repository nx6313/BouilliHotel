package com.bouilli.nxx.bouillihotel.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.EditOrderActivity;
import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 18230 on 2016/11/5.
 */

public class OutOrderFragment extends Fragment {
    int mNum;// 页号
    private RefDataBroadCastReceive refDataBroadCastReceive;// 刷新数据广播实例
    public static String MSG_REF_OUTORDER_DATA = "requestNewOutOrderDataBouilliHotel";

    private LinearLayout outOrderMainLayout;

    private Map<Integer, List<String>> outOrderNumberMap = new HashMap<>();

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
        outOrderMainLayout = (LinearLayout) view.findViewById(R.id.outOrderMainLayout);
        initView();
        return view;
    }

    private void initView() {
        outOrderMainLayout.removeAllViews();
        outOrderNumberMap = new HashMap<>();
        String wmOrDbInfos;
        if(mNum == 0){
            // 外卖
            wmOrDbInfos = SharedPreferencesTool.getFromShared(getActivity(), "BouilliMenuInfo", "wmInfos", "");
        }else{
            // 打包
            wmOrDbInfos = SharedPreferencesTool.getFromShared(getActivity(), "BouilliMenuInfo", "dbInfos", "");
        }
        if(ComFun.strNull(wmOrDbInfos)){
            String[] wmOrDbInfoArr = wmOrDbInfos.split("#@#,#");
            int wmOrDbItemIndex = 0;
            for(String wmOrDbInfo : wmOrDbInfoArr){
                LinearLayout menuChildItemlayout = new LinearLayout(getActivity());
                if(mNum == 0){
                    // 外卖
                    List<String> outNumberList = new ArrayList<>();
                    if(outOrderNumberMap.containsKey(0)){
                        outNumberList = outOrderNumberMap.get(0);
                    }
                    outNumberList.add("No." + ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                    outOrderNumberMap.put(0, outNumberList);
                    menuChildItemlayout.setTag("[外卖餐] No." + ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                }else{
                    // 打包
                    List<String> outNumberList = new ArrayList<>();
                    if(outOrderNumberMap.containsKey(1)){
                        outNumberList = outOrderNumberMap.get(1);
                    }
                    outNumberList.add("No." + ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                    outOrderNumberMap.put(1, outNumberList);
                    menuChildItemlayout.setTag("[打包餐] No." + ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                }
                menuChildItemlayout.setPadding(DisplayUtil.dip2px(getActivity(), 20), 0, DisplayUtil.dip2px(getActivity(), 20), 0);
                if(wmOrDbItemIndex % 2 == 0){
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#D2D4CA"));
                }else{
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#DDD5DB"));
                }
                menuChildItemlayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuChildItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemlayout.setLayoutParams(menuChildItemLp);
                // 子项每一项图标
                ImageView menuChildItemImg = new ImageView(getActivity());
                menuChildItemImg.setTag(R.id.tag_table_order_id, (wmOrDbInfo.split("#&&#")[0]).split("#&#")[0]);
                menuChildItemImg.setImageResource(R.drawable.menu1);
                LinearLayout.LayoutParams menuChildItemImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(getActivity(), 45), DisplayUtil.dip2px(getActivity(), 45));
                menuChildItemImgLp.setMargins(DisplayUtil.dip2px(getActivity(), 2), DisplayUtil.dip2px(getActivity(), 2), DisplayUtil.dip2px(getActivity(), 2), DisplayUtil.dip2px(getActivity(), 2));
                menuChildItemImg.setLayoutParams(menuChildItemImgLp);
                menuChildItemlayout.addView(menuChildItemImg);
                // 子项每一项主体（名称带简要说明）
                LinearLayout menuChildItemDeslayout = new LinearLayout(getActivity());
                menuChildItemDeslayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemDeslayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams menuChildItemDesLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(getActivity(), 0), LinearLayout.LayoutParams.MATCH_PARENT, 1);
                menuChildItemDesLp.setMargins(DisplayUtil.dip2px(getActivity(), 8), 0, 0, 0);
                menuChildItemDeslayout.setLayoutParams(menuChildItemDesLp);
                // 主体-->菜名
                TextView menuChildItemDesNameTxt = new TextView(getActivity());
                menuChildItemDesNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                if(mNum == 0){
                    // 外卖
                    menuChildItemDesNameTxt.setText("[外卖] No." + ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                }else{
                    // 打包
                    menuChildItemDesNameTxt.setText("[打包] No." + ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((wmOrDbInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                }
                menuChildItemDesNameTxt.setTextColor(Color.parseColor("#000000"));
                TextPaint menuChildItemDesNameTxtTp = menuChildItemDesNameTxt.getPaint();
                menuChildItemDesNameTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesNameTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesNameTxt.setLayoutParams(menuChildItemDesNameTxtLp);
                menuChildItemDesNameTxt.setSingleLine(true);
                menuChildItemDesNameTxt.setEllipsize(TextUtils.TruncateAt.END);
                menuChildItemDeslayout.addView(menuChildItemDesNameTxt);
                // 主体-->服务员
                TextView menuChildItemDesssTxt = new TextView(getActivity());
                menuChildItemDesssTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                menuChildItemDesssTxt.setText("服务员：" + (wmOrDbInfo.split("#&&#")[0]).split("#&#")[4]);
                TextPaint menuChildItemDesssTxtTp = menuChildItemDesssTxt.getPaint();
                menuChildItemDesssTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesssTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesssTxt.setLayoutParams(menuChildItemDesssTxtLp);
                menuChildItemDeslayout.addView(menuChildItemDesssTxt);
                menuChildItemlayout.addView(menuChildItemDeslayout);
                // 子项每一项单价
                LinearLayout menuChildItemPricelayout = new LinearLayout(getActivity());
                menuChildItemPricelayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemPricelayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuChildItemPriceLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                menuChildItemPriceLp.setMargins(DisplayUtil.dip2px(getActivity(), 8), 0, 0, 0);
                menuChildItemPricelayout.setLayoutParams(menuChildItemPriceLp);
                TextView buyCountTv = new TextView(getActivity());
                buyCountTv.setTextColor(Color.parseColor("#303030"));
                buyCountTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                TextPaint buyCountTvTp = buyCountTv.getPaint();
                buyCountTvTp.setFakeBoldText(true);
                LinearLayout.LayoutParams buyCountTvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                buyCountTv.setLayoutParams(buyCountTvLp);
                buyCountTv.setText("下单时间： " + (wmOrDbInfo.split("#&&#")[0]).split("#&#")[3].substring(0, 16));
                menuChildItemPricelayout.addView(buyCountTv);
                menuChildItemlayout.addView(menuChildItemPricelayout);

                outOrderMainLayout.addView(menuChildItemlayout);

                menuChildItemlayout.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        String tag_table_order_id = ((LinearLayout) v).getChildAt(0).getTag(R.id.tag_table_order_id).toString();
                        Intent intentKongXian = new Intent(getActivity(), EditOrderActivity.class);
                        intentKongXian.putExtra("showType", 3);
                        intentKongXian.putExtra("tableNum", "-1");
                        intentKongXian.putExtra("outOrderAccount", "outAccount");
                        intentKongXian.putExtra("outOrderNumber", v.getTag().toString().trim());
                        intentKongXian.putExtra("tableOrderId", tag_table_order_id);
                        startActivity(intentKongXian);
                    }
                });

                wmOrDbItemIndex++;
            }
        }
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
                String wmDataRef = intent.getExtras().getString("wmDataRef");
                String dbDataRef = intent.getExtras().getString("dbDataRef");
                // 对比当前页面
                List<String> outNumberList;
                if(mNum == 0){
                    outNumberList = outOrderNumberMap.get(0);
                    if(ComFun.strNull(outNumberList)){
                        // 跟wmDataRef比较是否一样
                        String[] wmDataRefArr = wmDataRef.split("#@#,#");
                        Collections.sort(outNumberList);
                        StringBuilder str1 = new StringBuilder("");
                        for(String str : outNumberList){
                            str1.append(str);
                        }
                        List<String> str2List = new ArrayList<>();
                        for(String str : wmDataRefArr){
                            str2List.add(str);
                        }
                        Collections.sort(str2List);
                        StringBuilder str2 = new StringBuilder("");
                        for(String str : str2List){
                            str2.append(str);
                        }
                        if(!str1.toString().equals(str2.toString())){
                            initView();
                        }
                    }
                }else{
                    outNumberList = outOrderNumberMap.get(1);
                    if(ComFun.strNull(outNumberList)){
                        // 跟dbDataRef比较是否一样
                        String[] dbDataRefArr = dbDataRef.split("#@#,#");
                        Collections.sort(outNumberList);
                        StringBuilder str1 = new StringBuilder("");
                        for(String str : outNumberList){
                            str1.append(str);
                        }
                        List<String> str2List = new ArrayList<>();
                        for(String str : dbDataRefArr){
                            str2List.add(str);
                        }
                        Collections.sort(str2List);
                        StringBuilder str2 = new StringBuilder("");
                        for(String str : str2List){
                            str2.append(str);
                        }
                        if(!str1.toString().equals(str2.toString())){
                            initView();
                        }
                    }
                }
                if(!ComFun.strNull(outNumberList)){
                    if(ComFun.strNull(wmDataRef) || ComFun.strNull(dbDataRef)){
                        initView();
                    }
                }
            }
        }
    }

}
