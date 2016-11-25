package com.bouilli.nxx.bouillihotel.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.EditOrderActivity;
import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.customview.FlowLayout;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

/**
 * Created by 18230 on 2016/11/5.
 */

public class MainFragment extends Fragment {
    int mNum;// 页号
    private RefDataBroadCastReceive refDataBroadCastReceive;// 刷新数据广播实例
    public static String MSG_REFDATA = "requestNewDataBouilliHotel";
    private FlowLayout dining_table_layout;// 存放所有桌子的容器实例

    public static MainFragment newInstance(int num){
        MainFragment fragment = new MainFragment();
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
        filter.addAction("requestNewDataBouilliHotel");
        getActivity().registerReceiver(refDataBroadCastReceive, filter);
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_pager, null);
        dining_table_layout = (FlowLayout) view.findViewById(R.id.dining_table_layout);
        String tableGroupNames = SharedPreferencesTool.getFromShared(getActivity(), "BouilliTableInfo", "tableGroupNames");
        if(ComFun.strNull(tableGroupNames)){
            String thisGroupTableInfo = SharedPreferencesTool.getFromShared(getActivity(), "BouilliTableInfo", "tableInfo" + tableGroupNames.split(",")[mNum]);
            addTableView(thisGroupTableInfo.split(","));
        }
        return view;
    }

    // 添加餐桌布局
    private void addTableView(String[] thisGroupTableInfoArr){
        for(int i=0; i<thisGroupTableInfoArr.length; i++){
            LinearLayout tableObject = new LinearLayout(getContext());
            tableObject.setGravity(Gravity.CENTER);
            tableObject.setOrientation(LinearLayout.VERTICAL);
            tableObject.setFocusable(true);
            LinearLayout.LayoutParams tableObjLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tableObjLp.setMargins(DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10));
            tableObject.setLayoutParams(tableObjLp);
            // ImageView
            ImageView tableImg = new ImageView(getContext());
            tableImg.setTag("tableImg"+thisGroupTableInfoArr[i].split("\\|")[0]);
            tableImg.setTag(R.id.tag_table_state, thisGroupTableInfoArr[i].split("\\|")[1]);
            ViewGroup.LayoutParams tableImgLp = new ViewGroup.LayoutParams(DisplayUtil.dip2px(getContext(), 70), DisplayUtil.dip2px(getContext(), 70));
            tableImg.setLayoutParams(tableImgLp);
            if(Integer.parseInt(thisGroupTableInfoArr[i].split("\\|")[1]) == 1){// 空闲
                tableImg.setImageResource(R.drawable.desk_0);
            }else if(Integer.parseInt(thisGroupTableInfoArr[i].split("\\|")[1]) == 2){// 编辑
                tableImg.setImageResource(R.drawable.desk_2);
                // 闪烁显示动画
                AlphaAnimation aa1 = new AlphaAnimation(1.0f,0.4f);
                aa1.setRepeatCount(-1);//设置重复次数
                aa1.setRepeatMode(Animation.REVERSE);//设置反方向执行
                aa1.setDuration(2300);
                aa1.setFillAfter(true);
                tableImg.startAnimation(aa1);
            }else{// 占用
                tableImg.setTag(R.id.tag_table_order_id, thisGroupTableInfoArr[i].split("\\|")[2]);
                tableImg.setImageResource(R.drawable.desk_1);
            }
            tableObject.addView(tableImg);
            // TextView
            TextView tableDes = new TextView(getContext());
            ViewGroup.LayoutParams tableDesLp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tableDes.setLayoutParams(tableDesLp);
            tableDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tableDes.setText(thisGroupTableInfoArr[i].split("\\|")[0]);
            tableDes.setTag("tableTxtInfo");
            tableDes.setTag(R.id.tag_table_txt_no, "tableTxtInfo_"+thisGroupTableInfoArr[i].split("\\|")[0]);
            TextPaint tableDesTp = tableDes.getPaint();
            tableDesTp.setFakeBoldText(true);
            tableObject.addView(tableDes);
            dining_table_layout.addView(tableObject);
            tableObject.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int tableState = Integer.parseInt(((LinearLayout) v).getChildAt(0).getTag(R.id.tag_table_state).toString());
                    String tableDesInfo = ((TextView) ((LinearLayout) v).getChildAt(1)).getText().toString();
                    if(tableState == 2){// 编辑中
                        ComFun.showToast(getActivity(), "该餐桌正在编辑中，选择别的餐桌吧", Toast.LENGTH_SHORT);
                    }else{
                        if(tableState == 1){// 空闲
                            Intent intentKongXian = new Intent(getActivity(), EditOrderActivity.class);
                            intentKongXian.putExtra("showType", 1);
                            intentKongXian.putExtra("tableNum", tableDesInfo);
                            startActivity(intentKongXian);
                        }else{// 占用
                            String tag_table_order_id = ((LinearLayout) v).getChildAt(0).getTag(R.id.tag_table_order_id).toString();
                            Intent intentKongXian = new Intent(getActivity(), EditOrderActivity.class);
                            intentKongXian.putExtra("showType", 3);
                            intentKongXian.putExtra("tableNum", tableDesInfo);
                            intentKongXian.putExtra("tableOrderId", tag_table_order_id);
                            startActivity(intentKongXian);
                        }
                    }
                }
            });
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
            if(intent.getAction().equals(MSG_REFDATA)){
                boolean newDataFlag = intent.getExtras().getBoolean("newData");
                // 更新餐桌信息
                if(newDataFlag){
                    String tableFullInfo = SharedPreferencesTool.getFromShared(context, "BouilliTableInfo", "tableFullInfo");
                    if(ComFun.strNull(tableFullInfo)){
                        String[] tableFullInfoArr = tableFullInfo.split(",");
                        for(String tableInfo : tableFullInfoArr){
                            if(ComFun.strNull(tableInfo)){
                                ImageView tableImg = (ImageView) dining_table_layout.findViewWithTag("tableImg"+tableInfo.split("\\|")[0]);
                                if(tableImg != null){
                                    tableImg.clearAnimation();
                                    if(Integer.parseInt(tableInfo.split("\\|")[1]) == 1){// 空闲
                                        tableImg.setTag(R.id.tag_table_state, 1);
                                        tableImg.setImageResource(R.drawable.desk_0);
                                    }else if(Integer.parseInt(tableInfo.split("\\|")[1]) == 2){// 编辑
                                        tableImg.setTag(R.id.tag_table_state, 2);
                                        tableImg.setImageResource(R.drawable.desk_2);
                                        // 闪烁显示动画
                                        AlphaAnimation aa1 = new AlphaAnimation(1.0f,0.6f);
                                        aa1.setRepeatCount(-1);//设置重复次数
                                        aa1.setRepeatMode(Animation.REVERSE);//设置反方向执行
                                        aa1.setDuration(2000);
                                        aa1.setFillAfter(true);
                                        tableImg.startAnimation(aa1);
                                    }else{// 占用
                                        tableImg.setTag(R.id.tag_table_state, 3);
                                        tableImg.setTag(R.id.tag_table_order_id, tableInfo.split("\\|")[2]);
                                        tableImg.setImageResource(R.drawable.desk_1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
