package com.bouilli.nxx.bouillihotel;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.callBack.MsgCallBack;
import com.bouilli.nxx.bouillihotel.customview.AmountView;
import com.bouilli.nxx.bouillihotel.customview.ElasticScrollView;
import com.bouilli.nxx.bouillihotel.customview.GifViewByMovie;
import com.bouilli.nxx.bouillihotel.customview.NavigationTabBar;
import com.bouilli.nxx.bouillihotel.fragment.SelectMenuFragment;
import com.bouilli.nxx.bouillihotel.fragment.adapter.SelectMenuFragmentPageAdapter;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;
import com.bouilli.nxx.bouillihotel.util.SnackbarUtil;
import com.bouilli.nxx.bouillihotel.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectMenuActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    private RefSelectMenuDataBroadCastReceive refSelectMenuDataBroadCastReceive;// 刷新数据广播实例
    public static final String MSG_REFDATA = "refSelectMenuData";
    public static final int MSG_SELECT_NEW_MENU = 3;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILED = 1;
    public static final int RESULT_CANCLE = 2;

    private FloatingActionButton current_order_menu_info;

    private FragmentManager fm;
    private ViewPager viewPager;
    private SelectMenuFragmentPageAdapter mAdapter;

    private EditText selectMenuSearchEt;
    private Button selectMenuSearchClearBtn;
    private Handler mSearchHandler;
    private SearchTask mSearchTesk;
    private ElasticScrollView menuSearchScrollView;
    private LinearLayout menuSearchLayout;

    private int screenHeight;

    private ViewGroup anim_mask_layout;
    private ImageView imgIcon;

    private LinearLayout orderSelectLayoutMain;
    private LinearLayout orderSearchLayoutMain;
    private GifViewByMovie searchLoadingGif;
    private LinearLayout searchLoadingLayout;

    private Button btnClearAllOrder;
    private Button btnCancleOrder;
    private Button btnSureOrder;
    private Snackbar skShoppingCart = null;

    public static Map<String, AmountView> selectMenuAmountViewPoor = new HashMap<>();// 保存菜品选择的数字选择控件对象

    public static Map<String, Object[]> tableReadyOrderMap = new HashMap<>();// 保存该餐桌已点至后厨的菜品信息
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

        tableReadyOrderMap = new HashMap<>();// 保存该餐桌已点至后厨的菜品信息
        hasOrderThisTableMap = new HashMap<>();// 保存该餐桌当前未传送至后厨的新选择的菜品信息

        anim_mask_layout = createAnimLayout();

        WindowManager wm = SelectMenuActivity.this.getWindowManager();
        screenHeight = wm.getDefaultDisplay().getHeight();

        initThisTableHasSelect();

        initSearch();

        initTabBar();

        initCurrentOrderMenuInfo();

        initOrderViewBtn();

        mHandler = new SelectMenuActivity.mHandler();


        // 注册广播接收器
        refSelectMenuDataBroadCastReceive = new RefSelectMenuDataBroadCastReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MSG_REFDATA);
        SelectMenuActivity.this.registerReceiver(refSelectMenuDataBroadCastReceive, filter);
    }

    // 初始化该餐桌新选择且还没传至后厨的菜
    private void initThisTableHasSelect(){
        Intent toThisIntent = this.getIntent();
        if(toThisIntent.hasExtra("tableHasOrders")){
            String tableHasOrders = toThisIntent.getExtras().getString("tableHasOrders");
            if(ComFun.strNull(tableHasOrders)){
                for(String hasOrder : tableHasOrders.split(",")){
                    tableReadyOrderMap.put(hasOrder.split("\\|")[0], new Object[]{ hasOrder.split("\\|")[1], hasOrder.split("\\|")[2], hasOrder.split("\\|")[3] });
                }
            }
        }
        if(toThisIntent.hasExtra("tableHasNewOrders")){
            String tableHasNewOrders = toThisIntent.getExtras().getString("tableHasNewOrders");
            if(ComFun.strNull(tableHasNewOrders)){
                for(String hasNewOrder : tableHasNewOrders.split(",")){
                    hasOrderThisTableMap.put(hasNewOrder.split("\\|")[0], new Object[]{ hasNewOrder.split("\\|")[1], hasNewOrder.split("\\|")[2], hasNewOrder.split("\\|")[3] });
                }
            }
        }
    }

    // 初始化菜品首字母搜索
    private void initSearch(){
        menuSearchScrollView = (ElasticScrollView) findViewById(R.id.menuSearchScrollView);
        menuSearchLayout = (LinearLayout) findViewById(R.id.menuSearchLayout);
        mSearchHandler = new Handler();
        mSearchTesk = new SearchTask();
        selectMenuSearchEt = (EditText) findViewById(R.id.selectMenuSearchEt);
        selectMenuSearchClearBtn = (Button) findViewById(R.id.selectMenuSearchClearBtn);
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
                    searchLoadingLayout.setVisibility(View.VISIBLE);
                    menuSearchScrollView.setVisibility(View.GONE);
                    mSearchHandler.removeCallbacks(mSearchTesk);
                    mSearchHandler.postDelayed(mSearchTesk, 500);
                }else{
                    orderSelectLayoutMain.setVisibility(View.VISIBLE);
                    orderSearchLayoutMain.setVisibility(View.GONE);
                    mSearchHandler.removeCallbacks(mSearchTesk);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        selectMenuSearchClearBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                selectMenuSearchEt.setText("");
                selectMenuSearchEt.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                orderSelectLayoutMain.setVisibility(View.VISIBLE);
                orderSearchLayoutMain.setVisibility(View.GONE);
                mSearchHandler.removeCallbacks(mSearchTesk);
            }
        });
    }

    /**
     * 搜索任务
     */
    class SearchTask implements Runnable {

        @Override
        public void run() {
            searchLoadingLayout.setVisibility(View.GONE);
            // 从菜品缓存数据中查找符合的数据
            String menuAllItemChild = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "menuAllItemChild");
            if(ComFun.strNull(menuAllItemChild)) {
                List<String> searchResultMenuList = new ArrayList<>();
                for(String menuAllItem : menuAllItemChild.split(",")){
                    String menuName = menuAllItem.split("#&#")[2];
                    String caseWordMenuName = StringUtil.getFirstLetter(menuName);
                    if(menuName.contains(selectMenuSearchEt.getText().toString().trim())
                            || caseWordMenuName.toUpperCase().startsWith(selectMenuSearchEt.getText().toString().trim().toUpperCase())){
                        searchResultMenuList.add(menuAllItem);
                    }
                }
                if(searchResultMenuList.size() > 0){
                    menuSearchScrollView.setVisibility(View.VISIBLE);
                    addMenuView(searchResultMenuList);
                }else{
                    menuSearchScrollView.setVisibility(View.GONE);
                    ComFun.showToast(SelectMenuActivity.this, "没有找到相关的菜品", Toast.LENGTH_SHORT);
                }
            }
        }
    }

    // 初始化选项卡
    private void initTabBar(){
        String menuGroupNames = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "menuGroupNames");
        // 添加常用选项卡
        menuGroupNames = "-1#&#常用," + menuGroupNames;
        viewPager = (ViewPager) findViewById(R.id.selectMenuViewPager);
        fm = getSupportFragmentManager();
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
                for(Map.Entry<String, Object[]> map : hasOrderThisTableMap.entrySet()) {
                    AmountView amountView = selectMenuAmountViewPoor.get("menuId_" + index + "_" + map.getValue()[0].toString().split("#&#")[0]);
                    if(amountView != null){
                        amountView.setEtAmount(map.getValue()[1].toString());
                    }
                }
            }
        });
    }

    // 初始化查看当前所选订单详情的悬浮按钮事件
    public void initCurrentOrderMenuInfo(){
        Intent toThisIntent = this.getIntent();
        final String tableNum = toThisIntent.getExtras().getString("tableNum");
        current_order_menu_info = (FloatingActionButton) findViewById(R.id.current_order_menu_info);
        current_order_menu_info.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(tableReadyOrderMap.size() > 0 || hasOrderThisTableMap.size() > 0){
                    if (skShoppingCart != null && skShoppingCart.isShown()) {
                        skShoppingCart.dismiss();
                        ObjectAnimator.ofFloat(v, "alpha", (float)0.4).setDuration(200).start();
                    } else {
                        double totalHasMoney = 0.0;
                        for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                            BigDecimal price = new BigDecimal(map.getValue()[0].toString().split("#&#")[4]);
                            int buyNum = Integer.parseInt(map.getValue()[1].toString());
                            BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                            totalHasMoney = ComFun.add(totalHasMoney, thisTotalPrice);
                        }
                        double totalMoney = 0.0;
                        for(Map.Entry<String, Object[]> map : hasOrderThisTableMap.entrySet()){
                            BigDecimal price = new BigDecimal(map.getValue()[0].toString().split("#&#")[4]);
                            int buyNum = Integer.parseInt(map.getValue()[1].toString());
                            BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                            totalMoney = ComFun.add(totalMoney, thisTotalPrice);
                        }
                        skShoppingCart = Snackbar.make(v, "", Snackbar.LENGTH_INDEFINITE).setAction("Action", null);
                        SnackbarUtil.setSnackbarColor(skShoppingCart, 0xffebebe4, 0xff4a9db5);
                        skShoppingCart.setCallback(new MsgCallBack(v));
                        View snackbarview = skShoppingCart.getView();
                        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout)snackbarview;
                        View add_view = LayoutInflater.from(snackbarview.getContext()).inflate(R.layout.view_shopping_cart_item, null);

                        if(hasOrderThisTableMap.size() > 0){
                            TextView shopping_cart_title = (TextView) add_view.findViewById(R.id.shopping_cart_title);
                            shopping_cart_title.setVisibility(View.VISIBLE);
                            shopping_cart_title.setText("【" + tableNum + "】新选 >> " + hasOrderThisTableMap.size() + "道 >> 金额：￥" + ComFun.addZero(String.valueOf(totalMoney)) + " 元");
                            LinearLayout shopping_cart_main = (LinearLayout) add_view.findViewById(R.id.shopping_cart_main);
                            shopping_cart_main.setVisibility(View.VISIBLE);
                            shopping_cart_main.removeAllViews();
                            for(Map.Entry<String, Object[]> map : hasOrderThisTableMap.entrySet()){// 新选
                                LinearLayout shopping_cart_item_layout = new LinearLayout(SelectMenuActivity.this);
                                shopping_cart_item_layout.setTag("cart_" + map.getKey());
                                shopping_cart_item_layout.setOrientation(LinearLayout.HORIZONTAL);
                                LinearLayout.LayoutParams shopping_cart_item_layoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                shopping_cart_item_layout.setLayoutParams(shopping_cart_item_layoutLp);
                                TextView shopping_cart_item_MenuName = new TextView(SelectMenuActivity.this);
                                shopping_cart_item_MenuName.setTextColor(Color.parseColor("#E0C220"));
                                LinearLayout.LayoutParams shopping_cart_item_MenuNameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                                shopping_cart_item_MenuName.setLayoutParams(shopping_cart_item_MenuNameLp);
                                shopping_cart_item_MenuName.setText(map.getValue()[0].toString().split("#&#")[2]);
                                shopping_cart_item_layout.addView(shopping_cart_item_MenuName);
                                TextView shopping_cart_item_MenuNum = new TextView(SelectMenuActivity.this);
                                shopping_cart_item_MenuNum.setTextColor(Color.parseColor("#E0C220"));
                                LinearLayout.LayoutParams shopping_cart_item_MenuNumLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                shopping_cart_item_MenuNum.setLayoutParams(shopping_cart_item_MenuNumLp);
                                shopping_cart_item_MenuNum.setText(map.getValue()[1].toString() + " 份");
                                shopping_cart_item_layout.addView(shopping_cart_item_MenuNum);
                                shopping_cart_main.addView(shopping_cart_item_layout);
                            }
                        }
                        if(tableReadyOrderMap.size() > 0){
                            TextView shopping_has_cart_title = (TextView) add_view.findViewById(R.id.shopping_has_cart_title);
                            shopping_has_cart_title.setVisibility(View.VISIBLE);
                            shopping_has_cart_title.setText("【" + tableNum + "】已选 >> " + tableReadyOrderMap.size() + "道 >> 金额：￥" + ComFun.addZero(String.valueOf(totalHasMoney)) + " 元");
                            LinearLayout shopping_has_cart_main = (LinearLayout) add_view.findViewById(R.id.shopping_has_cart_main);
                            shopping_has_cart_main.setVisibility(View.VISIBLE);
                            shopping_has_cart_main.removeAllViews();
                            for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){// 已选
                                LinearLayout shopping_has_cart_item_layout = new LinearLayout(SelectMenuActivity.this);
                                shopping_has_cart_item_layout.setOrientation(LinearLayout.HORIZONTAL);
                                LinearLayout.LayoutParams shopping_has_cart_item_layoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                shopping_has_cart_item_layout.setLayoutParams(shopping_has_cart_item_layoutLp);
                                TextView shopping_cart_item_MenuName = new TextView(SelectMenuActivity.this);
                                shopping_cart_item_MenuName.setTextColor(Color.parseColor("#D9D9D9"));
                                LinearLayout.LayoutParams shopping_has_cart_item_MenuNameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                                shopping_cart_item_MenuName.setLayoutParams(shopping_has_cart_item_MenuNameLp);
                                shopping_cart_item_MenuName.setText(map.getValue()[0].toString().split("#&#")[2]);
                                shopping_has_cart_item_layout.addView(shopping_cart_item_MenuName);
                                TextView shopping_cart_item_MenuNum = new TextView(SelectMenuActivity.this);
                                shopping_cart_item_MenuNum.setTextColor(Color.parseColor("#D9D9D9"));
                                LinearLayout.LayoutParams shopping_has_cart_item_MenuNumLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                shopping_cart_item_MenuNum.setLayoutParams(shopping_has_cart_item_MenuNumLp);
                                shopping_cart_item_MenuNum.setText(map.getValue()[1].toString() + " 份");
                                shopping_has_cart_item_layout.addView(shopping_cart_item_MenuNum);
                                shopping_has_cart_main.addView(shopping_has_cart_item_layout);
                            }
                        }
                        if(tableReadyOrderMap.size() > 0 && hasOrderThisTableMap.size() > 0){
                            TextView shopping_cart_total_money = (TextView) add_view.findViewById(R.id.shopping_cart_total_money);
                            shopping_cart_total_money.setVisibility(View.VISIBLE);
                            double allTotalMoney = ComFun.add(totalHasMoney, new BigDecimal(totalMoney));
                            shopping_cart_total_money.setText("总金额：￥" + ComFun.addZero(String.valueOf(allTotalMoney)) + " 元");
                        }

                        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        p.gravity= Gravity.CENTER_VERTICAL;
                        snackbarLayout.addView(add_view, 1, p);
                        skShoppingCart.show();
                        snackbarLayout.setOrientation(LinearLayout.VERTICAL);
                        ObjectAnimator.ofFloat(v, "alpha", 1).setDuration(200).start();
                    }
                }else{
                    ComFun.showToast(SelectMenuActivity.this, "该餐桌当前没有新选择的菜哦", Toast.LENGTH_SHORT);
                }
            }
        });
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
                Intent toThisIntent = SelectMenuActivity.this.getIntent();
                String tableNum = toThisIntent.getExtras().getString("tableNum");
                if(hasOrderThisTableMap.size() > 0){
                    new android.support.v7.app.AlertDialog.Builder(SelectMenuActivity.this).setTitle("清空已选").setMessage("确认清空餐桌【"+ tableNum +"】新选择的菜品吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    hasOrderThisTableMap.clear();
                                    // 更新页面
                                    for(Map.Entry<String, AmountView> map : selectMenuAmountViewPoor.entrySet()){
                                        if(map.getValue() != null){
                                            map.getValue().setEtAmount("0");
                                        }
                                    }
                                }
                            })
                            .setNegativeButton("取消", null).show();
                }else{
                    ComFun.showToast(SelectMenuActivity.this, "餐桌【"+ tableNum +"】还没有新增菜品哦", Toast.LENGTH_SHORT);
                }
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
                        newOrderMenusSb.append(map.getValue()[0]+"%"+map.getValue()[1]+"%"+map.getValue()[2]);
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

    class mHandler extends Handler {
        public mHandler() {
        }

        public mHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            switch (msg.what) {
                case MSG_SELECT_NEW_MENU:
                    if(b.containsKey("thisMenuInfo") && ComFun.strNull(b.getString("thisMenuInfo"))){
                        String selectMenuInfo = b.getString("thisMenuInfo");
                        String selectNum = b.getString("selectNum");
                        if(!ComFun.strNull(selectNum)){
                            selectNum = "0";
                        }
                        if (skShoppingCart != null && skShoppingCart.isShown()) {
                            double totalHasMoney = 0.0;
                            for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                                BigDecimal price = new BigDecimal(map.getValue()[0].toString().split("#&#")[4]);
                                int buyNum = Integer.parseInt(map.getValue()[1].toString());
                                BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                                totalHasMoney = ComFun.add(totalHasMoney, thisTotalPrice);
                            }
                            double totalMoney = 0.0;
                            for(Map.Entry<String, Object[]> map : hasOrderThisTableMap.entrySet()){
                                BigDecimal price = new BigDecimal(map.getValue()[0].toString().split("#&#")[4]);
                                int buyNum = Integer.parseInt(map.getValue()[1].toString());
                                BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                                totalMoney = ComFun.add(totalMoney, thisTotalPrice);
                            }
                            Intent toThisIntent = SelectMenuActivity.this.getIntent();
                            final String tableNum = toThisIntent.getExtras().getString("tableNum");
                            View snackbarview = skShoppingCart.getView();
                            Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout)snackbarview;
                            TextView shopping_cart_title = (TextView) snackbarLayout.findViewById(R.id.shopping_cart_title);
                            LinearLayout shopping_cart_main = (LinearLayout) snackbarLayout.findViewById(R.id.shopping_cart_main);
                            TextView shopping_cart_total_money = (TextView) snackbarLayout.findViewById(R.id.shopping_cart_total_money);
                            if(shopping_cart_main.getVisibility() == View.VISIBLE){
                                LinearLayout shopping_cart_item_layout = (LinearLayout) shopping_cart_main.findViewWithTag("cart_" + selectMenuInfo.split("#&#")[0]);
                                if(shopping_cart_item_layout != null){
                                    shopping_cart_title.setText("【" + tableNum + "】新选 >> " + hasOrderThisTableMap.size() + "道 >> 金额：￥" + totalMoney + " 元");
                                    if(Integer.parseInt(selectNum) > 0){
                                        ((TextView) shopping_cart_item_layout.getChildAt(1)).setText(selectNum + " 份");
                                    }else{
                                        shopping_cart_main.removeView(shopping_cart_item_layout);
                                        if(shopping_cart_main.getChildCount() == 0){
                                            shopping_cart_title.setVisibility(View.GONE);
                                            shopping_cart_main.setVisibility(View.GONE);
                                        }
                                    }
                                }else{
                                    if(Integer.parseInt(selectNum) > 0){
                                        shopping_cart_title.setText("【" + tableNum + "】新选 >> " + hasOrderThisTableMap.size() + "道 >> 金额：￥" + totalMoney + " 元");
                                        shopping_cart_item_layout = new LinearLayout(SelectMenuActivity.this);
                                        shopping_cart_item_layout.setTag("cart_" + selectMenuInfo.split("#&#")[0]);
                                        shopping_cart_item_layout.setOrientation(LinearLayout.HORIZONTAL);
                                        LinearLayout.LayoutParams shopping_cart_item_layoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        shopping_cart_item_layout.setLayoutParams(shopping_cart_item_layoutLp);
                                        TextView shopping_cart_item_MenuName = new TextView(SelectMenuActivity.this);
                                        shopping_cart_item_MenuName.setTextColor(Color.parseColor("#E0C220"));
                                        LinearLayout.LayoutParams shopping_cart_item_MenuNameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                                        shopping_cart_item_MenuName.setLayoutParams(shopping_cart_item_MenuNameLp);
                                        shopping_cart_item_MenuName.setText(selectMenuInfo.split("#&#")[2]);
                                        shopping_cart_item_layout.addView(shopping_cart_item_MenuName);
                                        TextView shopping_cart_item_MenuNum = new TextView(SelectMenuActivity.this);
                                        shopping_cart_item_MenuNum.setTextColor(Color.parseColor("#E0C220"));
                                        LinearLayout.LayoutParams shopping_cart_item_MenuNumLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                        shopping_cart_item_MenuNum.setLayoutParams(shopping_cart_item_MenuNumLp);
                                        shopping_cart_item_MenuNum.setText(selectNum + " 份");
                                        shopping_cart_item_layout.addView(shopping_cart_item_MenuNum);
                                        shopping_cart_main.addView(shopping_cart_item_layout);
                                    }
                                }
                            }else{
                                if(Integer.parseInt(selectNum) > 0){
                                    shopping_cart_main.removeAllViews();
                                    shopping_cart_title.setVisibility(View.VISIBLE);
                                    shopping_cart_title.setText("【" + tableNum + "】新选 >> 1道 >> 金额：￥" + selectMenuInfo.split("#&#")[4] + " 元");
                                    shopping_cart_main.setVisibility(View.VISIBLE);
                                    LinearLayout shopping_cart_item_layout = new LinearLayout(SelectMenuActivity.this);
                                    shopping_cart_item_layout.setTag("cart_" + selectMenuInfo.split("#&#")[0]);
                                    shopping_cart_item_layout.setOrientation(LinearLayout.HORIZONTAL);
                                    LinearLayout.LayoutParams shopping_cart_item_layoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    shopping_cart_item_layout.setLayoutParams(shopping_cart_item_layoutLp);
                                    TextView shopping_cart_item_MenuName = new TextView(SelectMenuActivity.this);
                                    shopping_cart_item_MenuName.setTextColor(Color.parseColor("#E0C220"));
                                    LinearLayout.LayoutParams shopping_cart_item_MenuNameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                                    shopping_cart_item_MenuName.setLayoutParams(shopping_cart_item_MenuNameLp);
                                    shopping_cart_item_MenuName.setText(selectMenuInfo.split("#&#")[2]);
                                    shopping_cart_item_layout.addView(shopping_cart_item_MenuName);
                                    TextView shopping_cart_item_MenuNum = new TextView(SelectMenuActivity.this);
                                    shopping_cart_item_MenuNum.setTextColor(Color.parseColor("#E0C220"));
                                    LinearLayout.LayoutParams shopping_cart_item_MenuNumLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                    shopping_cart_item_MenuNum.setLayoutParams(shopping_cart_item_MenuNumLp);
                                    shopping_cart_item_MenuNum.setText(selectNum + " 份");
                                    shopping_cart_item_layout.addView(shopping_cart_item_MenuNum);
                                    shopping_cart_main.addView(shopping_cart_item_layout);
                                }
                            }
                            LinearLayout shopping_has_cart_main = (LinearLayout) snackbarLayout.findViewById(R.id.shopping_has_cart_main);
                            if(shopping_cart_main.getVisibility() == View.VISIBLE && shopping_has_cart_main.getVisibility() == View.VISIBLE){
                                shopping_cart_total_money.setVisibility(View.VISIBLE);
                            }else{
                                shopping_cart_total_money.setVisibility(View.GONE);
                            }
                            if(shopping_cart_total_money.getVisibility() == View.VISIBLE){
                                double allTotalMoney = ComFun.add(totalHasMoney, new BigDecimal(totalMoney));
                                shopping_cart_total_money.setText("总金额：￥" + ComFun.addZero(String.valueOf(allTotalMoney)) + " 元");
                            }
                            if(shopping_cart_main.getVisibility() == View.GONE && shopping_has_cart_main.getVisibility() == View.GONE){
                                skShoppingCart.dismiss();
                                ObjectAnimator.ofFloat(current_order_menu_info, "alpha", (float)0.4).setDuration(200).start();
                            }
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册广播
        SelectMenuActivity.this.unregisterReceiver(refSelectMenuDataBroadCastReceive);
    }

    public class RefSelectMenuDataBroadCastReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }

    // 添加搜索菜品布局
    private void addMenuView(List<String> thisGroupMenuInfoArr){
        menuSearchLayout.removeAllViews();
        int chileItemIndex = 0;
        for(String thisGroupMenuInfo : thisGroupMenuInfoArr){
            if(ComFun.strNull(thisGroupMenuInfo)){
                LinearLayout menuChildItemlayout = new LinearLayout(SelectMenuActivity.this);
                menuChildItemlayout.setTag("menuChildItemOrderLayout");
                menuChildItemlayout.setPadding(DisplayUtil.dip2px(SelectMenuActivity.this, 20), 0, DisplayUtil.dip2px(SelectMenuActivity.this, 20), 0);
                if(chileItemIndex % 2 == 0){
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#aeb39f"));
                }else{
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#c1b0be"));
                }
                menuChildItemlayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuChildItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemlayout.setLayoutParams(menuChildItemLp);
                // 子项每一项图标
                ImageView menuChildItemImg = new ImageView(SelectMenuActivity.this);
                menuChildItemImg.setTag(thisGroupMenuInfo);
                menuChildItemImg.setImageResource(R.drawable.menu1);
                LinearLayout.LayoutParams menuChildItemImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(SelectMenuActivity.this, 45), DisplayUtil.dip2px(SelectMenuActivity.this, 45));
                menuChildItemImgLp.setMargins(DisplayUtil.dip2px(SelectMenuActivity.this, 2), DisplayUtil.dip2px(SelectMenuActivity.this, 2), DisplayUtil.dip2px(SelectMenuActivity.this, 2), DisplayUtil.dip2px(SelectMenuActivity.this, 2));
                menuChildItemImg.setLayoutParams(menuChildItemImgLp);
                menuChildItemlayout.addView(menuChildItemImg);
                // 子项每一项主体（名称带简要说明）
                LinearLayout menuChildItemDeslayout = new LinearLayout(SelectMenuActivity.this);
                menuChildItemDeslayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemDeslayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams menuChildItemDesLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(SelectMenuActivity.this, 0), LinearLayout.LayoutParams.MATCH_PARENT, 1);
                menuChildItemDesLp.setMargins(DisplayUtil.dip2px(SelectMenuActivity.this, 8), 0, 0, 0);
                menuChildItemDeslayout.setLayoutParams(menuChildItemDesLp);
                // 主体-->菜名
                TextView menuChildItemDesNameTxt = new TextView(SelectMenuActivity.this);
                menuChildItemDesNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                menuChildItemDesNameTxt.setText(thisGroupMenuInfo.split("#&#")[2]);
                menuChildItemDesNameTxt.setTextColor(Color.parseColor("#000000"));
                TextPaint menuChildItemDesNameTxtTp = menuChildItemDesNameTxt.getPaint();
                menuChildItemDesNameTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesNameTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesNameTxt.setLayoutParams(menuChildItemDesNameTxtLp);
                menuChildItemDesNameTxt.setSingleLine(true);
                menuChildItemDesNameTxt.setEllipsize(TextUtils.TruncateAt.END);
                menuChildItemDeslayout.addView(menuChildItemDesNameTxt);
                // 主体-->菜简介
                TextView menuChildItemDesssTxt = new TextView(SelectMenuActivity.this);
                menuChildItemDesssTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                if(!thisGroupMenuInfo.split("#&#")[3].equals("-")){
                    menuChildItemDesssTxt.setText(thisGroupMenuInfo.split("#&#")[4] + " 元【" + thisGroupMenuInfo.split("#&#")[3] + "】");
                }else{
                    menuChildItemDesssTxt.setText(thisGroupMenuInfo.split("#&#")[4] + " 元");
                }
                TextPaint menuChildItemDesssTxtTp = menuChildItemDesssTxt.getPaint();
                menuChildItemDesssTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesssTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesssTxt.setLayoutParams(menuChildItemDesssTxtLp);
                menuChildItemDeslayout.addView(menuChildItemDesssTxt);
                menuChildItemlayout.addView(menuChildItemDeslayout);
                // 子项每一项单价
                LinearLayout menuChildItemPricelayout = new LinearLayout(SelectMenuActivity.this);
                menuChildItemPricelayout.setTag("menuId_" + thisGroupMenuInfo.split("#&#")[0]);
                menuChildItemPricelayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemPricelayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams menuChildItemPriceLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                menuChildItemPriceLp.setMargins(DisplayUtil.dip2px(SelectMenuActivity.this, 8), 0, 0, 0);
                menuChildItemPricelayout.setLayoutParams(menuChildItemPriceLp);
                // 编辑数量按钮
                AmountView amountView = new AmountView(SelectMenuActivity.this);
                amountView.setTag("amountView_order");
                LinearLayout.LayoutParams amountViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                amountView.setLayoutParams(amountViewLp);
                if(SelectMenuActivity.hasOrderThisTableMap.containsKey(thisGroupMenuInfo.split("#&#")[0])){
                    amountView.setEtAmount(SelectMenuActivity.hasOrderThisTableMap.get(thisGroupMenuInfo.split("#&#")[0])[1].toString());
                }
                menuChildItemPricelayout.addView(amountView);
                menuChildItemlayout.addView(menuChildItemPricelayout);

                menuSearchLayout.addView(menuChildItemlayout);

                // 绑定每样菜的数字选择事件监听
                amountView.setOnAmountChangeListener(new AmountView.OnAmountChangeListener() {
                    @Override
                    public void onAmountChange(View view, int amount, int clickType) {
                        // 这个菜增加数量，执行购物车动画
                        if(clickType == 1){
                            // 增加操作
                            setAnim(view);
                        }
                        String thisMenuId = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString().split("#&#")[0];
                        String thisMenuName = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString().split("#&#")[2];
                        String thisMenuInfo = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString();
                        //ComFun.showToast(getActivity(), thisMenuName+"，已选："+amount+"个", Toast.LENGTH_SHORT);
                        if(amount > 0){
                            Object[] newOrderInfo = new Object[]{ thisMenuInfo, amount, "-" };// 菜id, 选择数量, 备注信息
                            SelectMenuActivity.hasOrderThisTableMap.put(thisMenuId, newOrderInfo);
                        }else{
                            if(SelectMenuActivity.hasOrderThisTableMap.containsKey(thisMenuId) && clickType == -1){
                                SelectMenuActivity.hasOrderThisTableMap.remove(thisMenuId);
                            }
                        }
                        // 发送Handler通知页面更新UI
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("thisMenuInfo", thisMenuInfo);
                        data.putString("selectNum", amount+"");
                        msg.what = SelectMenuActivity.MSG_SELECT_NEW_MENU;
                        msg.setData(data);
                        SelectMenuActivity.mHandler.sendMessage(msg);
                    }
                });

                chileItemIndex++;
            }
        }
    }

    /**
     * @Description: 创建动画层
     * @param
     * @return void
     * @throws
     */
    private ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) SelectMenuActivity.this.getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(SelectMenuActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    /**
     * @Description: 添加视图到动画层
     * @param @param vg
     * @param @param view
     * @param @param location
     * @param @return
     * @return View
     * @throws
     */
    private View addViewToAnimLayout(final ViewGroup vg, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        vg.removeAllViews();
        vg.addView(view);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }

    private void setAnim(View startView) {
        anim_mask_layout.removeAllViews();
        Animation mScaleAnimation = new ScaleAnimation(1.5f, 0.1f, 1.5f, 0.1f, Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.1f);
        mScaleAnimation.setDuration(AnimationDuration);
        mScaleAnimation.setFillAfter(true);

        int[] start_location = new int[2];
        startView.getLocationInWindow(start_location);
        // 将组件添加到我们的动画层上
        imgIcon = new ImageView(SelectMenuActivity.this);
        imgIcon.setImageResource(R.drawable.menu2);
        View view = addViewToAnimLayout(anim_mask_layout, imgIcon, start_location);
        int[] end_location = new int[2];
        // 计算位移
        int endX = end_location[0] - start_location[0] + DisplayUtil.dip2px(SelectMenuActivity.this, 25);
        int endY = screenHeight - DisplayUtil.dip2px(SelectMenuActivity.this, 110) - start_location[1];

        Animation mTranslateAnimation = new TranslateAnimation(0, endX, 0, endY);// 移动
        mTranslateAnimation.setDuration(AnimationDuration);

        AnimationSet mAnimationSet = new AnimationSet(false);
        // 这块要注意，必须设为false,不然组件动画结束后，不会归位。
        mAnimationSet.setFillAfter(false);
        mAnimationSet.addAnimation(mScaleAnimation);
        mAnimationSet.addAnimation(mTranslateAnimation);
        view.startAnimation(mAnimationSet);

        mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                anim_mask_layout.removeAllViews();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                anim_mask_layout.removeAllViews();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                anim_mask_layout.removeAllViews();
            }
        });
    }

    /**
     * 动画播放时间
     */
    private int AnimationDuration = 600;

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
                v.clearFocus();
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
