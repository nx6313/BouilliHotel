package com.bouilli.nxx.bouillihotel;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bouilli.nxx.bouillihotel.callBack.MsgCallBack;
import com.bouilli.nxx.bouillihotel.customview.ElasticScrollView;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;
import com.bouilli.nxx.bouillihotel.util.SnackbarUtil;

import java.util.ArrayList;
import java.util.List;

public class OrderRecordActivity extends AppCompatActivity {
    private RefRecordDataBroadCastReceive refRecordDataBroadCastReceive;// 刷新数据广播实例
    public static final String MSG_REFDATA = "requestNewRecordDataBouilliHotel";
    private long exitTime;

    private List<String> hasRecordOrderIdList = new ArrayList<>();

    private ElasticScrollView orderRecordData;
    private TextView topTotalRecordTv;
    private LinearLayout orderRecordMainLayout;

    private FloatingActionButton order_menu_print_set;
    private Snackbar skPrintSet = null;

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
        // 保持屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_order_record);

        mVisible = true;
        mContentView = findViewById(R.id.orderRecordMainLayout);

        // Set up the user interaction to manually show or hide the system UI.
        //mContentView.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        toggle();
        //    }
        //});

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.orderRecordMainLayout).setOnTouchListener(mDelayHideTouchListener);

        orderRecordData = (ElasticScrollView) findViewById(R.id.orderRecordData);
        topTotalRecordTv = (TextView) findViewById(R.id.topTotalRecordTv);
        topTotalRecordTv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                orderRecordData.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        orderRecordMainLayout = (LinearLayout) findViewById(R.id.orderRecordMainLayout);
        orderRecordMainLayout.removeAllViews();

        order_menu_print_set = (FloatingActionButton) findViewById(R.id.order_menu_print_set);
        order_menu_print_set.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (skPrintSet != null && skPrintSet.isShown()) {
                    skPrintSet.dismiss();
                    ObjectAnimator.ofFloat(v, "alpha", (float)0.6).setDuration(200).start();
                } else {
                    skPrintSet = Snackbar.make(v, "打印设置", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Action", null);
                    SnackbarUtil.setSnackbarColor(skPrintSet, 0xff8aaed0, 0xffe09120);
                    skPrintSet.setCallback(new MsgCallBack(v));

                    View snackbarview = skPrintSet.getView();
                    Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout)snackbarview;
                    View add_view = LayoutInflater.from(snackbarview.getContext()).inflate(R.layout.view_print_set_sk, null);

                    TextView select_print_area = (TextView) add_view.findViewById(R.id.select_print_area);
                    select_print_area.requestFocus();

                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    p.gravity= Gravity.CENTER_VERTICAL;
                    snackbarLayout.addView(add_view, 1, p);

                    skPrintSet.show();
                    //snackbarLayout.setOrientation(LinearLayout.VERTICAL);
                    ObjectAnimator.ofFloat(v, "alpha", 1).setDuration(200).start();
                }
            }
        });

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
                        if(orderRecordFull.split(",").length == 0){
                            orderRecordMainLayout.removeAllViews();
                            orderRecordData.setBackgroundColor(Color.parseColor("#539b8f"));
                        }
                        String userPermission = SharedPreferencesTool.getFromShared(OrderRecordActivity.this, "BouilliProInfo", "userPermission");
                        int index;
                        for(String orderRecord : orderRecordFull.split(",")){
                            if(ComFun.strNull(orderRecord)){
                                if(!hasRecordOrderIdList.contains(orderRecord.split("#&#")[0])){
                                    hasRecordOrderIdList.add(orderRecord.split("#&#")[0]);
                                    // 判断应该设置的颜色
                                    LinearLayout recordItemFirstLayout;
                                    if(ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 4){
                                        recordItemFirstLayout = (LinearLayout) orderRecordMainLayout.getChildAt(orderRecordMainLayout.getChildCount() - 1);
                                    }else{
                                        recordItemFirstLayout = (LinearLayout) orderRecordMainLayout.getChildAt(0);
                                    }
                                    if(recordItemFirstLayout == null){
                                        index = 0;
                                    }else{
                                        String firstItemTag = recordItemFirstLayout.getTag().toString();
                                        if(firstItemTag.equals("color0")){
                                            index = 1;
                                            orderRecordData.setBackgroundColor(Color.parseColor("#539b8f"));
                                        }else{
                                            index = 0;
                                            orderRecordData.setBackgroundColor(Color.parseColor("#9b5353"));
                                        }
                                    }

                                    LinearLayout recordItemLayout = new LinearLayout(OrderRecordActivity.this);
                                    recordItemLayout.setTag("color"+index);
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
                                    orderTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                                    orderTime.setText("订单创建时间：");
                                    LinearLayout.LayoutParams orderTimeLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(OrderRecordActivity.this, 120), ViewGroup.LayoutParams.WRAP_CONTENT);
                                    orderTime.setLayoutParams(orderTimeLp);
                                    TextPaint orderTimeTp = orderTime.getPaint();
                                    orderTimeTp.setFakeBoldText(true);
                                    orderTimeLayout.addView(orderTime);
                                    TextView orderTime2 = new TextView(OrderRecordActivity.this);
                                    if(index % 2 == 0){
                                        orderTime2.setTextColor(Color.parseColor("#ffffff"));
                                    }
                                    orderTime2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                                    orderTime2.setText(orderRecord.split("#&#")[5]);
                                    LinearLayout.LayoutParams orderTime2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                                    orderTime2.setLayoutParams(orderTime2Lp);
                                    TextPaint orderTime2Tp = orderTime2.getPaint();
                                    orderTime2Tp.setFakeBoldText(true);
                                    orderTimeLayout.addView(orderTime2);
                                    recordItemLayout.addView(orderTimeLayout);
                                    // 桌号
                                    if(!(orderRecord.split("#&#")[2].equals("null") || orderRecord.split("#&#")[2] == null)){
                                        LinearLayout orderTableNoLayout = new LinearLayout(OrderRecordActivity.this);
                                        orderTableNoLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        LinearLayout.LayoutParams orderTableNoLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        orderTableNoLayout.setLayoutParams(orderTableNoLayoutLp);
                                        TextView orderTableNo = new TextView(OrderRecordActivity.this);
                                        if(index % 2 == 0){
                                            orderTableNo.setTextColor(Color.parseColor("#ffffff"));
                                        }
                                        orderTableNo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                                        orderTableNo.setText("桌号：");
                                        LinearLayout.LayoutParams orderTableNoLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(OrderRecordActivity.this, 120), ViewGroup.LayoutParams.WRAP_CONTENT);
                                        orderTableNo.setLayoutParams(orderTableNoLp);
                                        TextPaint orderTableNoTp = orderTableNo.getPaint();
                                        orderTableNoTp.setFakeBoldText(true);
                                        orderTableNoLayout.addView(orderTableNo);
                                        TextView orderTableNo2 = new TextView(OrderRecordActivity.this);
                                        if(index % 2 == 0){
                                            orderTableNo2.setTextColor(Color.parseColor("#ffffff"));
                                        }
                                        orderTableNo2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                                        orderTableNo2.setText(orderRecord.split("#&#")[2]);
                                        LinearLayout.LayoutParams orderTableNo2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                                        orderTableNo2.setLayoutParams(orderTableNo2Lp);
                                        TextPaint orderTableNo2Tp = orderTableNo2.getPaint();
                                        orderTableNo2Tp.setFakeBoldText(true);
                                        orderTableNoLayout.addView(orderTableNo2);
                                        recordItemLayout.addView(orderTableNoLayout);
                                    }
                                    // 订单详情
                                    TextView orderDes = new TextView(OrderRecordActivity.this);
                                    if(index % 2 == 0){
                                        orderDes.setTextColor(Color.parseColor("#ffffff"));
                                    }else{
                                        orderDes.setTextColor(Color.parseColor("#3A3A3A"));
                                    }
                                    orderDes.setText(ComFun.formatMenuDetailInfo(orderRecord.split("#&#")[3]));
                                    LinearLayout.LayoutParams orderDesLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    orderDesLp.setMargins(DisplayUtil.dip2px(OrderRecordActivity.this, 8), DisplayUtil.dip2px(OrderRecordActivity.this, 10), DisplayUtil.dip2px(OrderRecordActivity.this, 8), DisplayUtil.dip2px(OrderRecordActivity.this, 6));
                                    orderDes.setLayoutParams(orderDesLp);
                                    TextPaint orderDesTp = orderDes.getPaint();
                                    orderDesTp.setFakeBoldText(true);
                                    orderDes.setPadding(DisplayUtil.dip2px(OrderRecordActivity.this, 10), DisplayUtil.dip2px(OrderRecordActivity.this, 10), DisplayUtil.dip2px(OrderRecordActivity.this, 10), DisplayUtil.dip2px(OrderRecordActivity.this, 10));
                                    orderDes.setBackgroundResource(R.drawable.bg_record_circle);

                                    if(ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 4){
                                        orderDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                    }else{
                                        orderDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                                    }

                                    recordItemLayout.addView(orderDes);

                                    // 后厨管理员登录时的操作按键
                                    if(ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 4){
                                        LinearLayout recordDoLayout = new LinearLayout(OrderRecordActivity.this);
                                        LinearLayout.LayoutParams recordDoLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        recordDoLayout.setLayoutParams(recordDoLayoutLp);
                                        recordDoLayout.setOrientation(LinearLayout.HORIZONTAL);
                                        recordDoLayout.setGravity(Gravity.CENTER);

                                        Button btnLessMenu = new Button(OrderRecordActivity.this);
                                        btnLessMenu.setBackgroundResource(R.drawable.order_btn_style);
                                        btnLessMenu.setText("报缺");
                                        btnLessMenu.setTextColor(Color.parseColor("#000000"));
                                        btnLessMenu.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
                                        LinearLayout.LayoutParams btnLessMenuLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(OrderRecordActivity.this, 100), ViewGroup.LayoutParams.WRAP_CONTENT);
                                        btnLessMenuLp.gravity = Gravity.CENTER;
                                        btnLessMenu.setLayoutParams(btnLessMenuLp);
                                        recordDoLayout.addView(btnLessMenu);

                                        Button btnOutMenu = new Button(OrderRecordActivity.this);
                                        btnOutMenu.setBackgroundResource(R.drawable.order_btn_style);
                                        btnOutMenu.setText("出菜");
                                        btnOutMenu.setTextColor(Color.parseColor("#000000"));
                                        btnOutMenu.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
                                        LinearLayout.LayoutParams btnOutMenuLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(OrderRecordActivity.this, 100), ViewGroup.LayoutParams.WRAP_CONTENT);
                                        btnOutMenuLp.setMargins(DisplayUtil.dip2px(OrderRecordActivity.this, 100), 0, 0, 0);
                                        btnOutMenuLp.gravity = Gravity.CENTER;
                                        btnOutMenu.setLayoutParams(btnOutMenuLp);
                                        recordDoLayout.addView(btnOutMenu);

                                        recordItemLayout.addView(recordDoLayout);
                                    }

                                    if(ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 4){
                                        orderRecordMainLayout.addView(recordItemLayout);
                                    }else{
                                        orderRecordMainLayout.addView(recordItemLayout, 0);
                                    }

                                    TranslateAnimation animation = new TranslateAnimation(0, 0, -100, 0);
                                    animation.setDuration(200);//设置动画持续时间
                                    animation.setRepeatCount(0);//设置重复次数
                                    animation.setFillAfter(true);
                                    animation.setInterpolator(new AccelerateInterpolator());
                                    recordItemLayout.startAnimation(animation);
                                }
                            }
                        }
                    }else{
                        topTotalRecordTv.setText("当前(今日)共有订单数：0 个");
                        orderRecordMainLayout.removeAllViews();
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            String userPermission = SharedPreferencesTool.getFromShared(OrderRecordActivity.this, "BouilliProInfo", "userPermission");
            if(ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 4){
                if (System.currentTimeMillis() - exitTime > 2000) {
                    ComFun.showToast(this, "再按一次退出", 2000);
                    exitTime = System.currentTimeMillis();
                } else {
                    SharedPreferencesTool.addOrUpdate(OrderRecordActivity.this, "BouilliProInfo", "hasExitLast", "true");
                    Intent welcomeIntent = new Intent(OrderRecordActivity.this, WelcomeActivity.class);
                    startActivity(welcomeIntent);
                    OrderRecordActivity.this.finish();
                }
            }else{
                // 正常后退
                finish();
            }
        }
        return true;
    }
}
