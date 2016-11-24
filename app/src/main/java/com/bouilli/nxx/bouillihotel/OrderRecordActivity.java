package com.bouilli.nxx.bouillihotel;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

public class OrderRecordActivity extends AppCompatActivity {
    private RefRecordDataBroadCastReceive refRecordDataBroadCastReceive;// 刷新数据广播实例
    public static final String MSG_REFDATA = "requestNewRecordDataBouilliHotel";

    private List<String> hasRecordOrderIdList = new ArrayList<>();

    private TextView topTotalRecordTv;
    private LinearLayout orderRecordMainLayout;

    private static final boolean AUTO_HIDE = true;

    private static final int AUTO_HIDE_DELAY_MILLIS = 8000;

    private static final int UI_ANIMATION_DELAY = 600;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_record);

        mVisible = true;
        mContentView = findViewById(R.id.orderRecordMainLayout);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.orderRecordMainLayout).setOnTouchListener(mDelayHideTouchListener);

        topTotalRecordTv = (TextView) findViewById(R.id.topTotalRecordTv);
        orderRecordMainLayout = (LinearLayout) findViewById(R.id.orderRecordMainLayout);
        orderRecordMainLayout.removeAllViews();

        // 注册广播接收器
        refRecordDataBroadCastReceive = new RefRecordDataBroadCastReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction("requestNewRecordDataBouilliHotel");
        OrderRecordActivity.this.registerReceiver(refRecordDataBroadCastReceive, filter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onRestart() {
        //toggle();
        hide();
        super.onRestart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消注册广播
        OrderRecordActivity.this.unregisterReceiver(refRecordDataBroadCastReceive);
    }

    public class RefRecordDataBroadCastReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MSG_REFDATA)){
                if(intent.getExtras().containsKey("orderRecordFull")){
                    String orderRecordFull = intent.getExtras().getString("orderRecordFull");
                    // 更新订单流水页面显示
                    if(ComFun.strNull(orderRecordFull)){
                        topTotalRecordTv.setText("当前(今日)共有订单数："+ orderRecordFull.split(",").length +" 个");
                        int index = 0;
                        for(String orderRecord : orderRecordFull.split(",")){
                            if(ComFun.strNull(orderRecord)){
                                if(!hasRecordOrderIdList.contains(orderRecord.split("#&#")[0])){
                                    hasRecordOrderIdList.add(orderRecord.split("#&#")[0]);

                                    LinearLayout recordItemLayout = new LinearLayout(OrderRecordActivity.this);
                                    recordItemLayout.setPadding(DisplayUtil.dip2px(OrderRecordActivity.this, 8), DisplayUtil.dip2px(OrderRecordActivity.this, 8), DisplayUtil.dip2px(OrderRecordActivity.this, 8), DisplayUtil.dip2px(OrderRecordActivity.this, 8));
                                    if(index % 2 == 0){
                                        recordItemLayout.setBackgroundColor(Color.parseColor("#9b5353"));
                                    }else{
                                        recordItemLayout.setBackgroundColor(Color.parseColor("#539b8f"));
                                    }
                                    recordItemLayout.setOrientation(LinearLayout.VERTICAL);
                                    LinearLayout.LayoutParams recordItemLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    recordItemLayout.setLayoutParams(recordItemLayoutLp);
                                    // 订单时间
                                    LinearLayout orderTimeLayout = new LinearLayout(OrderRecordActivity.this);
                                    orderTimeLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    LinearLayout.LayoutParams orderTimeLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    orderTimeLayout.setLayoutParams(orderTimeLayoutLp);
                                    TextView orderTime = new TextView(OrderRecordActivity.this);
                                    if(index % 2 == 0){
                                        orderTime.setTextColor(Color.parseColor("#ffffff"));
                                    }
                                    orderTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                                    orderTime.setText("订单创建时间：");
                                    LinearLayout.LayoutParams orderTimeLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(OrderRecordActivity.this, 120), ViewGroup.LayoutParams.WRAP_CONTENT);
                                    orderTime.setLayoutParams(orderTimeLp);
                                    orderTimeLayout.addView(orderTime);
                                    TextView orderTime2 = new TextView(OrderRecordActivity.this);
                                    if(index % 2 == 0){
                                        orderTime2.setTextColor(Color.parseColor("#ffffff"));
                                    }
                                    orderTime2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                                    orderTime2.setText(orderRecord.split("#&#")[5]);
                                    LinearLayout.LayoutParams orderTime2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                                    orderTime2.setLayoutParams(orderTime2Lp);
                                    orderTimeLayout.addView(orderTime2);
                                    recordItemLayout.addView(orderTimeLayout);
                                    // 桌号
                                    LinearLayout orderTableNoLayout = new LinearLayout(OrderRecordActivity.this);
                                    orderTableNoLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    LinearLayout.LayoutParams orderTableNoLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    orderTableNoLayout.setLayoutParams(orderTableNoLayoutLp);
                                    TextView orderTableNo = new TextView(OrderRecordActivity.this);
                                    if(index % 2 == 0){
                                        orderTableNo.setTextColor(Color.parseColor("#ffffff"));
                                    }
                                    orderTableNo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                                    orderTableNo.setText("桌号：");
                                    LinearLayout.LayoutParams orderTableNoLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(OrderRecordActivity.this, 120), ViewGroup.LayoutParams.WRAP_CONTENT);
                                    orderTableNo.setLayoutParams(orderTableNoLp);
                                    orderTableNoLayout.addView(orderTableNo);
                                    TextView orderTableNo2 = new TextView(OrderRecordActivity.this);
                                    if(index % 2 == 0){
                                        orderTableNo2.setTextColor(Color.parseColor("#ffffff"));
                                    }
                                    orderTableNo2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                                    orderTableNo2.setText(orderRecord.split("#&#")[2]);
                                    LinearLayout.LayoutParams orderTableNo2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                                    orderTableNo2.setLayoutParams(orderTableNo2Lp);
                                    orderTableNoLayout.addView(orderTableNo2);
                                    recordItemLayout.addView(orderTableNoLayout);
                                    // 订单状态
                                    LinearLayout orderStateLayout = new LinearLayout(OrderRecordActivity.this);
                                    orderStateLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    LinearLayout.LayoutParams orderStateLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    orderStateLayout.setLayoutParams(orderStateLayoutLp);
                                    TextView orderState = new TextView(OrderRecordActivity.this);
                                    if(index % 2 == 0){
                                        orderState.setTextColor(Color.parseColor("#ffffff"));
                                    }
                                    orderState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                                    orderState.setText("出单状态：");
                                    LinearLayout.LayoutParams orderStateLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(OrderRecordActivity.this, 120), ViewGroup.LayoutParams.WRAP_CONTENT);
                                    orderState.setLayoutParams(orderStateLp);
                                    orderStateLayout.addView(orderState);
                                    TextView orderState2 = new TextView(OrderRecordActivity.this);
                                    if(index % 2 == 0){
                                        orderState2.setTextColor(Color.parseColor("#ffffff"));
                                    }
                                    orderState2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                                    if(Integer.parseInt(orderRecord.split("#&#")[4]) == 0){
                                        orderState2.setText("打票机暂未打印");
                                    }else{
                                        orderState2.setText("打票机已打印");
                                    }
                                    LinearLayout.LayoutParams orderState2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                                    orderState2.setLayoutParams(orderState2Lp);
                                    orderStateLayout.addView(orderState2);
                                    recordItemLayout.addView(orderStateLayout);
                                    // 订单详情
                                    TextView orderDes = new TextView(OrderRecordActivity.this);
                                    if(index % 2 == 0){
                                        orderDes.setTextColor(Color.parseColor("#ffffff"));
                                    }
                                    orderDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                    orderDes.setText(ComFun.formatMenuDetailInfo(orderRecord.split("#&#")[3]));
                                    LinearLayout.LayoutParams orderDesLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    orderDesLp.setMargins(DisplayUtil.dip2px(OrderRecordActivity.this, 8), DisplayUtil.dip2px(OrderRecordActivity.this, 10), DisplayUtil.dip2px(OrderRecordActivity.this, 8), 0);
                                    orderDes.setLayoutParams(orderDesLp);
                                    recordItemLayout.addView(orderDes);

                                    orderRecordMainLayout.addView(recordItemLayout, 0);
                                    index++;
                                }
                            }
                        }
                    }else{
                        topTotalRecordTv.setText("当前(今日)共有订单数：0 个");
                    }
                }
            }
        }
    }
}
