package com.bouilli.nxx.bouillihotel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.bouilli.nxx.bouillihotel.customview.GifViewByMovie;
import com.bouilli.nxx.bouillihotel.customview.NavigationTabBar;
import com.bouilli.nxx.bouillihotel.fragment.adapter.SelectMenuFragmentPageAdapter;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectMenuActivity extends AppCompatActivity {
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILED = 1;
    public static final int RESULT_CANCLE = 2;

    private FloatingActionButton current_order_menu_info;

    private ViewPager viewPager;
    private SelectMenuFragmentPageAdapter mAdapter;

    private EditText selectMenuSearchEt;
    private LinearLayout orderSelectLayoutMain;
    private LinearLayout orderSearchLayoutMain;
    private GifViewByMovie searchLoadingGif;
    private LinearLayout searchLoadingLayout;

    private Button btnClearAllOrder;
    private Button btnCancleOrder;
    private Button btnSureOrder;

    public static Map<String, Object[]> hasOrderThisTableMap = new HashMap<>();// 保存该餐桌当前未传送至后厨的新选择的菜品信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_select_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Intent toThisIntent = this.getIntent();
        String tableNum = toThisIntent.getExtras().getString("tableNum");
        toolbar.setTitle("餐桌【"+ tableNum +"】点菜");
        setSupportActionBar(toolbar);

        initSearch();

        initTabBar();

        initCurrentOrderMenuInfo();

        initOrderViewBtn();
    }

    // 初始化菜品首字母搜索
    private void initSearch(){
        selectMenuSearchEt = (EditText) findViewById(R.id.selectMenuSearchEt);
        orderSelectLayoutMain = (LinearLayout) findViewById(R.id.orderSelectLayoutMain);
        orderSearchLayoutMain = (LinearLayout) findViewById(R.id.orderSearchLayoutMain);
        searchLoadingGif = (GifViewByMovie) findViewById(R.id.searchLoadingGif);
        //使用GifView太耗费内存，会导致OOM
        //searchLoadingGif.setGifImage(R.drawable.loading6);
        //searchLoadingGif.setShowDimension(240, 240);
        //searchLoadingGif.setGifImageType(GifView.GifImageType.WAIT_FINISH);
        searchLoadingLayout = (LinearLayout) findViewById(R.id.searchLoadingLayout);
        selectMenuSearchEt.setText("");
        selectMenuSearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().equals("")){
                    orderSelectLayoutMain.setVisibility(View.GONE);
                    orderSearchLayoutMain.setVisibility(View.VISIBLE);
                }else{
                    orderSelectLayoutMain.setVisibility(View.VISIBLE);
                    orderSearchLayoutMain.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    // 初始化选项卡
    private void initTabBar(){
        String menuGroupNames = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "menuGroupNames");
        // 添加常用选项卡
        menuGroupNames = "-1#&#常用," + menuGroupNames;
        viewPager = (ViewPager) findViewById(R.id.selectMenuViewPager);
        FragmentManager fm = getSupportFragmentManager();
        mAdapter = new SelectMenuFragmentPageAdapter(fm, menuGroupNames.split(",").length);
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(menuGroupNames.split(",").length);

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.selectMenuTabBar);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        if(ComFun.strNull(menuGroupNames)){
            for(int i=0; i<menuGroupNames.split(",").length; i++){
                NavigationTabBar.Model itemModel = new NavigationTabBar.Model.Builder(getResources().getDrawable(R.drawable.none), Color.parseColor(colors[i]))
                        .title(menuGroupNames.split(",")[i].split("#&#")[1]).build();
                models.add(itemModel);
            }
        }else{
            for(int i=0; i<menuGroupNames.split(",").length; i++){
                NavigationTabBar.Model itemModel = new NavigationTabBar.Model.Builder(getResources().getDrawable(R.drawable.none), Color.parseColor(colors[0]))
                        .title("暂无菜品").build();
                models.add(itemModel);
            }
        }
        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);

        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(final NavigationTabBar.Model model, final int index) {

            }

            @Override
            public void onEndTabSelected(final NavigationTabBar.Model model, final int index) {
                model.hideBadge();
                // 滑动选菜页菜品选项卡，更新页面信息，保证常用选项卡下的菜品的选择数量也其他页面上同类菜品的选择数量保持一致
            }
        });
    }

    // 初始化查看当前所选订单详情的悬浮按钮事件
    public void initCurrentOrderMenuInfo(){
        current_order_menu_info = (FloatingActionButton) findViewById(R.id.current_order_menu_info);
    }

    // 初始化选择订单页面三个主要按钮事件
    public void initOrderViewBtn(){
        btnClearAllOrder = (Button) findViewById(R.id.btnClearAllOrder);
        btnCancleOrder = (Button) findViewById(R.id.btnCancleOrder);
        btnSureOrder = (Button) findViewById(R.id.btnSureOrder);
        // 清除当前所选按钮
        btnClearAllOrder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 清除当前所选后，仍然在选菜页面，更新选择的菜品信息及页面
                hasOrderThisTableMap.clear();
            }
        });
        // 取消按钮
        btnCancleOrder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 清除当前所选后，返回餐桌详情页面
                hasOrderThisTableMap.clear();
                setResult(RESULT_CANCLE);
                finish();
            }
        });
        // 确定按钮
        btnSureOrder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                StringBuilder newOrderMenusSb = new StringBuilder("");
                if(!hasOrderThisTableMap.isEmpty() && hasOrderThisTableMap.size() > 0){
                    for(Map.Entry<String, Object[]> map : hasOrderThisTableMap.entrySet()){
                        newOrderMenusSb.append(map.getValue()[0]+"%"+map.getValue()[1]);
                        newOrderMenusSb.append(",");
                    }
                    hasOrderThisTableMap.clear();
                }
                Intent intent = new Intent();
                if(ComFun.strNull(newOrderMenusSb.toString())){
                    intent.putExtra("newOrderMenus", newOrderMenusSb.toString().substring(0, newOrderMenusSb.toString().length() - 1));
                }else{
                    intent.putExtra("newOrderMenus", "-");
                }
                setResult(RESULT_SUCCESS, intent);
                finish();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            setResult(RESULT_CANCLE);
            this.
            finish();
        }
        return true;
    }
}
