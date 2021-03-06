package com.bouilli.nxx.bouillihotel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.customview.AmountView;
import com.bouilli.nxx.bouillihotel.customview.ElasticScrollView;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SerializableMap;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;
import com.bouilli.nxx.bouillihotel.util.StringUtil;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectMenuActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_SELECT_NEW_MENU = 3;
    public static final int MSG_UPDATE_BUY_CAR = 4;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILED = 1;
    public static final int RESULT_CANCLE = 2;

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
    private LinearLayout searchLoadingLayout;

    private LinearLayout selectMenuLayout;
    private TextView selectMenuTitleTv;
    private LinearLayout selectMenuMainLayout;

    private Button btnToAllOrder;
    private Button btnCancleOrder;
    private Button btnSureOrder;

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
        if(tableNum.equals("wmTable") || tableNum.equals("dbTable")){
            if(tableNum.equals("wmTable")){
                toolbar.setTitle("外卖点菜");
            }else{
                toolbar.setTitle("打包点菜");
            }
        }else{
            toolbar.setTitle("餐桌【"+ tableNum +"】点菜");
        }
        setSupportActionBar(toolbar);

        selectMenuLayout = (LinearLayout) findViewById(R.id.selectMenuLayout);
        selectMenuTitleTv = (TextView) findViewById(R.id.selectMenuTitleTv);
        selectMenuMainLayout = (LinearLayout) findViewById(R.id.selectMenuMainLayout);

        tableReadyOrderMap = new HashMap<>();// 保存该餐桌已点至后厨的菜品信息
        hasOrderThisTableMap = new HashMap<>();// 保存该餐桌当前未传送至后厨的新选择的菜品信息

        anim_mask_layout = createAnimLayout();

        WindowManager wm = SelectMenuActivity.this.getWindowManager();
        screenHeight = wm.getDefaultDisplay().getHeight();

        initThisTableHasSelect();

        initSearch();

        initTabBar();

        initOrderViewBtn();

        mHandler = new SelectMenuActivity.mHandler();
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
        selectMenuSearchEt.setInputType(InputType.TYPE_NULL);
        try {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(selectMenuSearchEt, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final PopupWindow[] keyBoxesPopup = {null};
        selectMenuSearchEt.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    if(keyBoxesPopup[0] == null || (keyBoxesPopup[0] != null && !keyBoxesPopup[0].isShowing())){
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if(inputMethodManager.isActive()){
                            inputMethodManager.hideSoftInputFromWindow(SelectMenuActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                        // 显示自定义输入法弹框界面
                        View keyBoxesView = SelectMenuActivity.this.getLayoutInflater().inflate(R.layout.key_boxes, null);
                        keyBoxesView.requestFocus();
                        keyBoxesPopup[0] = new PopupWindow(keyBoxesView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
                        keyBoxesPopup[0].setFocusable(false);
                        keyBoxesPopup[0].setTouchable(true);
                        keyBoxesPopup[0].setOutsideTouchable(true);
                        keyBoxesPopup[0].setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
                        keyBoxesPopup[0].showAtLocation(SelectMenuActivity.this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                    }
                }
            }
        });
        selectMenuSearchEt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(keyBoxesPopup[0] == null || (keyBoxesPopup[0] != null && !keyBoxesPopup[0].isShowing())){
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(inputMethodManager.isActive()){
                        inputMethodManager.hideSoftInputFromWindow(SelectMenuActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    // 显示自定义输入法弹框界面
                    View keyBoxesView = SelectMenuActivity.this.getLayoutInflater().inflate(R.layout.key_boxes, null);
                    keyBoxesView.requestFocus();
                    keyBoxesPopup[0] = new PopupWindow(keyBoxesView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
                    keyBoxesPopup[0].setFocusable(false);
                    keyBoxesPopup[0].setTouchable(true);
                    keyBoxesPopup[0].setOutsideTouchable(true);
                    keyBoxesPopup[0].setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
                    keyBoxesPopup[0].showAtLocation(SelectMenuActivity.this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                }
            }
        });
        selectMenuSearchClearBtn = (Button) findViewById(R.id.selectMenuSearchClearBtn);
        orderSelectLayoutMain = (LinearLayout) findViewById(R.id.orderSelectLayoutMain);
        orderSearchLayoutMain = (LinearLayout) findViewById(R.id.orderSearchLayoutMain);
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
            }else{
                ComFun.showToast(SelectMenuActivity.this, "没有找到相关的菜品", Toast.LENGTH_SHORT);
            }
        }
    }

    // 初始化选项卡
    private void initTabBar(){
        String menuGroupNames = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "menuGroupNames");
        if(ComFun.strNull(menuGroupNames)){
            selectMenuTitleTv.setText("常用（显示被点最多的二十项菜品）");
            String oftenUseMenus = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "oftenUseMenus");
            initOrderMenuDetail(oftenUseMenus.split(","));
            // 添加常用选项卡
            final String initMenuGroupNames = "-1#&#常用," + menuGroupNames;
            selectMenuLayout.requestFocus();
            selectMenuLayout.setOnFocusChangeListener(new View.OnFocusChangeListener(){
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        // 创建下拉popupWindow
                        View selectMenuPopupView = SelectMenuActivity.this.getLayoutInflater().inflate(R.layout.select_menu_group_pull, null);
                        selectMenuPopupView.requestFocus();
                        final PopupWindow selectMenuPopup = new PopupWindow(selectMenuPopupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
                        selectMenuPopup.setFocusable(false);
                        selectMenuPopup.setTouchable(true);
                        selectMenuPopup.setOutsideTouchable(true);
                        selectMenuPopup.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
                        ((LinearLayout) selectMenuPopupView).removeAllViews();
                        int selectMenuIndex = 0;
                        for(String menuGroupObj : initMenuGroupNames.split(",")){
                            selectMenuIndex++;
                            TextView menuGroupSelectItemTv = new TextView(SelectMenuActivity.this);
                            menuGroupSelectItemTv.setGravity(Gravity.CENTER);
                            menuGroupSelectItemTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            menuGroupSelectItemTv.setTextColor(Color.parseColor("#cccccc"));
                            TextPaint menuGroupSelectItemTp = menuGroupSelectItemTv.getPaint();
                            menuGroupSelectItemTp.setFakeBoldText(true);
                            menuGroupSelectItemTv.setPadding(0, DisplayUtil.dip2px(SelectMenuActivity.this, 4), 0, DisplayUtil.dip2px(SelectMenuActivity.this, 4));
                            if(menuGroupObj.split("#&#")[0].equals("-1")){
                                menuGroupSelectItemTv.setText(menuGroupObj.split("#&#")[1] + "（显示被点最多的二十项菜品）");
                            }else{
                                menuGroupSelectItemTv.setText(menuGroupObj.split("#&#")[1]);
                            }
                            menuGroupSelectItemTv.setTag(menuGroupObj.split("#&#")[0]);
                            menuGroupSelectItemTv.setBackgroundResource(R.drawable.select_menu_item_style_1);
                            if(menuGroupObj.split("#&#")[0].equals("-1")){
                                if(selectMenuTitleTv.getText().toString().equals(menuGroupObj.split("#&#")[1] + "（显示被点最多的二十项菜品）")){
                                    menuGroupSelectItemTv.setBackgroundResource(R.color.selectMenuItemFocus1);
                                }
                            }else{
                                if(selectMenuTitleTv.getText().toString().equals(menuGroupObj.split("#&#")[1])){
                                    menuGroupSelectItemTv.setBackgroundResource(R.color.selectMenuItemFocus1);
                                }
                            }
                            menuGroupSelectItemTv.setFocusable(true);
                            menuGroupSelectItemTv.setFocusableInTouchMode(true);
                            menuGroupSelectItemTv.setClickable(true);
                            LinearLayout.LayoutParams menuGroupSelectItemTvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(SelectMenuActivity.this, 36));
                            menuGroupSelectItemTv.setLayoutParams(menuGroupSelectItemTvLp);
                            menuGroupSelectItemTv.setOnFocusChangeListener(new View.OnFocusChangeListener(){
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if(hasFocus){
                                        if(selectMenuPopup.isShowing()){
                                            selectMenuTitleTv.setText(((TextView) v).getText().toString().trim());
                                            selectMenuPopup.dismiss();
                                            if(v.getTag().toString().equals("-1")){
                                                String oftenUseMenus = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "oftenUseMenus");
                                                initOrderMenuDetail(oftenUseMenus.split(","));
                                            }else{
                                                String thisGroupMenuInfo = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "menuItemChild" + v.getTag().toString());
                                                initOrderMenuDetail(thisGroupMenuInfo.split(","));
                                            }
                                        }
                                    }
                                }
                            });
                            ((LinearLayout) selectMenuPopupView).addView(menuGroupSelectItemTv);
                            // 添加分割线
                            if(selectMenuIndex < initMenuGroupNames.split(",").length){
                                View menuGroupSelectItemLine = new View(SelectMenuActivity.this);
                                menuGroupSelectItemLine.setBackgroundColor(Color.parseColor("#B48E99"));
                                LinearLayout.LayoutParams menuGroupSelectItemLineLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(SelectMenuActivity.this, 1));
                                menuGroupSelectItemLine.setLayoutParams(menuGroupSelectItemLineLp);
                                ((LinearLayout) selectMenuPopupView).addView(menuGroupSelectItemLine);
                            }
                        }
                        selectMenuPopup.showAsDropDown(selectMenuLayout);
                    }
                }
            });
            selectMenuLayout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    // 创建下拉popupWindow
                    View selectMenuPopupView = SelectMenuActivity.this.getLayoutInflater().inflate(R.layout.select_menu_group_pull, null);
                    selectMenuPopupView.requestFocus();
                    final PopupWindow selectMenuPopup = new PopupWindow(selectMenuPopupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
                    selectMenuPopup.setFocusable(false);
                    selectMenuPopup.setTouchable(true);
                    selectMenuPopup.setOutsideTouchable(true);
                    selectMenuPopup.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
                    ((LinearLayout) selectMenuPopupView).removeAllViews();
                    int selectMenuIndex = 0;
                    for(String menuGroupObj : initMenuGroupNames.split(",")){
                        selectMenuIndex++;
                        TextView menuGroupSelectItemTv = new TextView(SelectMenuActivity.this);
                        menuGroupSelectItemTv.setGravity(Gravity.CENTER);
                        menuGroupSelectItemTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        menuGroupSelectItemTv.setTextColor(Color.parseColor("#cccccc"));
                        TextPaint menuGroupSelectItemTp = menuGroupSelectItemTv.getPaint();
                        menuGroupSelectItemTp.setFakeBoldText(true);
                        menuGroupSelectItemTv.setPadding(0, DisplayUtil.dip2px(SelectMenuActivity.this, 4), 0, DisplayUtil.dip2px(SelectMenuActivity.this, 4));
                        if(menuGroupObj.split("#&#")[0].equals("-1")){
                            menuGroupSelectItemTv.setText(menuGroupObj.split("#&#")[1] + "（显示被点最多的二十项菜品）");
                        }else{
                            menuGroupSelectItemTv.setText(menuGroupObj.split("#&#")[1]);
                        }
                        menuGroupSelectItemTv.setTag(menuGroupObj.split("#&#")[0]);
                        menuGroupSelectItemTv.setBackgroundResource(R.drawable.select_menu_item_style_1);
                        if(menuGroupObj.split("#&#")[0].equals("-1")){
                            if(selectMenuTitleTv.getText().toString().equals(menuGroupObj.split("#&#")[1] + "（显示被点最多的二十项菜品）")){
                                menuGroupSelectItemTv.setBackgroundResource(R.color.selectMenuItemFocus1);
                            }
                        }else{
                            if(selectMenuTitleTv.getText().toString().equals(menuGroupObj.split("#&#")[1])){
                                menuGroupSelectItemTv.setBackgroundResource(R.color.selectMenuItemFocus1);
                            }
                        }
                        menuGroupSelectItemTv.setFocusable(true);
                        menuGroupSelectItemTv.setFocusableInTouchMode(true);
                        menuGroupSelectItemTv.setClickable(true);
                        LinearLayout.LayoutParams menuGroupSelectItemTvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(SelectMenuActivity.this, 36));
                        menuGroupSelectItemTv.setLayoutParams(menuGroupSelectItemTvLp);
                        menuGroupSelectItemTv.setOnFocusChangeListener(new View.OnFocusChangeListener(){
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if(hasFocus){
                                    if(selectMenuPopup.isShowing()){
                                        selectMenuTitleTv.setText(((TextView) v).getText().toString().trim());
                                        selectMenuPopup.dismiss();
                                        if(v.getTag().toString().equals("-1")){
                                            String oftenUseMenus = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "oftenUseMenus");
                                            initOrderMenuDetail(oftenUseMenus.split(","));
                                        }else{
                                            String thisGroupMenuInfo = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "menuItemChild" + v.getTag().toString());
                                            initOrderMenuDetail(thisGroupMenuInfo.split(","));
                                        }
                                    }
                                }
                            }
                        });
                        ((LinearLayout) selectMenuPopupView).addView(menuGroupSelectItemTv);
                        // 添加分割线
                        if(selectMenuIndex < initMenuGroupNames.split(",").length){
                            View menuGroupSelectItemLine = new View(SelectMenuActivity.this);
                            menuGroupSelectItemLine.setBackgroundColor(Color.parseColor("#B48E99"));
                            LinearLayout.LayoutParams menuGroupSelectItemLineLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(SelectMenuActivity.this, 1));
                            menuGroupSelectItemLine.setLayoutParams(menuGroupSelectItemLineLp);
                            ((LinearLayout) selectMenuPopupView).addView(menuGroupSelectItemLine);
                        }
                    }
                    selectMenuPopup.showAsDropDown(selectMenuLayout);
                }
            });
        }else{
            selectMenuTitleTv.setText("暂无菜品（请先添加菜品数据）");
        }
    }

    // 添加菜品布局
    private void initOrderMenuDetail(String[] thisGroupMenuInfoArr){
        selectMenuMainLayout.removeAllViews();
        int chileItemIndex = 0;
        if(thisGroupMenuInfoArr == null){
            String oftenUseMenus = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "oftenUseMenus");
            thisGroupMenuInfoArr = oftenUseMenus.split(",");
        }
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
                final AmountView amountView = new AmountView(SelectMenuActivity.this);
                amountView.setTag("amountView_order");
                LinearLayout.LayoutParams amountViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                amountView.setLayoutParams(amountViewLp);
                if(SelectMenuActivity.hasOrderThisTableMap.containsKey(thisGroupMenuInfo.split("#&#")[0])){
                    amountView.setEtAmount(SelectMenuActivity.hasOrderThisTableMap.get(thisGroupMenuInfo.split("#&#")[0])[1].toString());
                }
                SelectMenuActivity.selectMenuAmountViewPoor.put("menuId_" + thisGroupMenuInfo.split("#&#")[0], amountView);
                menuChildItemPricelayout.addView(amountView);
                menuChildItemlayout.addView(menuChildItemPricelayout);

                selectMenuMainLayout.addView(menuChildItemlayout);

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
                        msg.what = SelectMenuActivity.MSG_SELECT_NEW_MENU;
                        SelectMenuActivity.mHandler.sendMessage(msg);
                    }
                });

                chileItemIndex++;
            }
        }
    }

    // 初始化选择订单页面三个主要按钮事件
    public void initOrderViewBtn(){
        btnToAllOrder = (Button) findViewById(R.id.btnToAllOrder);
        // 初始化选择菜品按钮数量值
        int totalHasSelect = 0;
        for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
            int buyNum = Integer.parseInt(map.getValue()[1].toString());
            totalHasSelect += buyNum;
        }
        int totalNewSelect = 0;
        for(Map.Entry<String, Object[]> map : hasOrderThisTableMap.entrySet()){
            int buyNum = Integer.parseInt(map.getValue()[1].toString());
            totalNewSelect += buyNum;
        }
        int allSelectMenuSize = totalHasSelect + totalNewSelect;
        btnToAllOrder.setText("已选("+ totalNewSelect +" / "+ totalHasSelect +" / "+ allSelectMenuSize +")");

        btnCancleOrder = (Button) findViewById(R.id.btnCancleOrder);
        btnSureOrder = (Button) findViewById(R.id.btnSureOrder);
        // 当前已选按钮
        btnToAllOrder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 清除当前所选后，仍然在选菜页面，更新选择的菜品信息及页面
                Intent toThisIntent = SelectMenuActivity.this.getIntent();
                String tableNum = toThisIntent.getExtras().getString("tableNum");
                int totalHasSelect = 0;
                for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                    int buyNum = Integer.parseInt(map.getValue()[1].toString());
                    totalHasSelect += buyNum;
                }
                int totalNewSelect = 0;
                for(Map.Entry<String, Object[]> map : hasOrderThisTableMap.entrySet()){
                    int buyNum = Integer.parseInt(map.getValue()[1].toString());
                    totalNewSelect += buyNum;
                }
                int allSelectMenuSize = totalHasSelect + totalNewSelect;
                if(allSelectMenuSize > 0){
                    // 跳转到购物车详情页面
                    Intent toBuyCarIntent = new Intent(SelectMenuActivity.this, BuyCarDetailActivity.class);
                    toBuyCarIntent.putExtra("tableNum", tableNum);
                    Bundle bundle = new Bundle();
                    SerializableMap tableReadyOrderMapSer = new SerializableMap();
                    tableReadyOrderMapSer.setMap(tableReadyOrderMap);
                    bundle.putSerializable("tableReadyOrderMap", tableReadyOrderMapSer);
                    SerializableMap hasOrderThisTableMapSer = new SerializableMap();
                    hasOrderThisTableMapSer.setMap(hasOrderThisTableMap);
                    bundle.putSerializable("hasOrderThisTableMap", hasOrderThisTableMapSer);
                    toBuyCarIntent.putExtras(bundle);
                    startActivity(toBuyCarIntent);
                }else{
                    ComFun.showToast(SelectMenuActivity.this, "餐桌【"+ tableNum +"】还没有选择菜品哦", Toast.LENGTH_SHORT);
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
                    int totalHasSelect = 0;
                    for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                        int buyNum = Integer.parseInt(map.getValue()[1].toString());
                        totalHasSelect += buyNum;
                    }
                    int totalNewSelect = 0;
                    for(Map.Entry<String, Object[]> map : hasOrderThisTableMap.entrySet()){
                        int buyNum = Integer.parseInt(map.getValue()[1].toString());
                        totalNewSelect += buyNum;
                    }
                    int allSelectMenuSize = totalHasSelect + totalNewSelect;
                    btnToAllOrder.setText("已选("+ totalNewSelect +" / "+ totalHasSelect +" / "+ allSelectMenuSize +")");
                    break;
                case MSG_UPDATE_BUY_CAR:
                    if(b.containsKey("thisMenuInfo") && ComFun.strNull(b.getString("thisMenuInfo"))){
                        String selectMenuInfo = b.getString("thisMenuInfo");
                        String selectNum = b.getString("selectNum");
                        if(!ComFun.strNull(selectNum)){
                            selectNum = "0";
                        }
                        if(selectNum == "0"){
                            hasOrderThisTableMap.remove(selectMenuInfo.split("#&#")[0]);
                        }else{
                            Object[] newOrderInfo = new Object[]{ selectMenuInfo, selectNum, "-" };// 菜id, 选择数量, 备注信息
                            hasOrderThisTableMap.put(selectMenuInfo.split("#&#")[0], newOrderInfo);
                        }
                        int totalHasSelectForBuyCar = 0;
                        for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                            int buyNum = Integer.parseInt(map.getValue()[1].toString());
                            totalHasSelectForBuyCar += buyNum;
                        }
                        int totalNewSelectForBuyCar = 0;
                        for(Map.Entry<String, Object[]> map : hasOrderThisTableMap.entrySet()){
                            int buyNum = Integer.parseInt(map.getValue()[1].toString());
                            totalNewSelectForBuyCar += buyNum;
                        }
                        int allSelectMenuSizeForBuyCar = totalHasSelectForBuyCar + totalNewSelectForBuyCar;
                        btnToAllOrder.setText("已选("+ totalNewSelectForBuyCar +" / "+ totalHasSelectForBuyCar +" / "+ allSelectMenuSizeForBuyCar +")");
                        initOrderMenuDetail(null);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                        String menuGroupNames = SharedPreferencesTool.getFromShared(SelectMenuActivity.this, "BouilliMenuInfo", "menuGroupNames");
                        // 常用选项卡
                        menuGroupNames = "-1#&#常用," + menuGroupNames;
                        for(int index=0; index<menuGroupNames.split(",").length; index++){
                            AmountView amountView = selectMenuAmountViewPoor.get("menuId_" + thisMenuInfo.split("#&#")[0]);
                            if(amountView != null){
                                amountView.setEtAmount(amount+"");
                            }
                        }
                        // 发送Handler通知页面更新UI
                        Message msg = new Message();
                        msg.what = SelectMenuActivity.MSG_SELECT_NEW_MENU;
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
        int endY = screenHeight - DisplayUtil.dip2px(SelectMenuActivity.this, 20) - start_location[1];

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
            this.finish();
        }
        return true;
    }

    /***************************************************************  输入法按键  ***************************************************************/
    public void clickKeyBox(View v){
        if(selectMenuSearchEt != null){
            selectMenuSearchEt.setText(selectMenuSearchEt.getText().toString() + ((Button) v).getText().toString());
            selectMenuSearchEt.setSelection(selectMenuSearchEt.getText().length());
        }
    }
    public void clickDelKeyBox(View v){
        if(selectMenuSearchEt != null){
            if(ComFun.strNull(selectMenuSearchEt.getText().toString())){
                selectMenuSearchEt.setText(selectMenuSearchEt.getText().toString().substring(0, selectMenuSearchEt.getText().toString().length() - 1));
                selectMenuSearchEt.setSelection(selectMenuSearchEt.getText().length());
            }
        }
    }
}
