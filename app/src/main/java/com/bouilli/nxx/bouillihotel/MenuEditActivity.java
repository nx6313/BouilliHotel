package com.bouilli.nxx.bouillihotel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.AddNewMenuTask;
import com.bouilli.nxx.bouillihotel.asyncTask.DeleteMenuTask;
import com.bouilli.nxx.bouillihotel.asyncTask.DeleteTableTask;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MenuEditActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_ADD_MENU = 1;
    public static final int MSG_DELETE_MENU = 2;
    private FloatingActionButton add_new_menu_info;
    private FloatingActionButton add_new_menu_des_info;
    private FloatingActionButton delete_new_menu_info;
    private boolean[] flags = null;//初始复选情况
    private List<String> deleteSelectMenuGroupIdList = new ArrayList<>();
    private PopupWindow editMenuPupopWindow;

    private String menuGroupNames;

    private EditText etMenuGroupName;
    private Button btn_save_menu_group;

    private RadioGroup rgMenuInfoGroup;
    private EditText etMenuInfoName;
    private EditText etMenuInfoDes;
    private EditText etMenuInfoPrice;
    private Button btn_save_menu_info;

    private LinearLayout menuMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initMenuView();
        initAddNewMenuGroup();
        initAddNewMenu();
        initDeleteMenuGroup();

        mHandler = new MenuEditActivity.mHandler();

        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            MenuEditActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 初始化菜品布局
    private void initMenuView(){
        menuMainLayout = (LinearLayout) findViewById(R.id.menuMainLayout);
        menuMainLayout.removeAllViews();
        menuGroupNames = SharedPreferencesTool.getFromShared(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames");
        if(ComFun.strNull(menuGroupNames)){
            int index = 0;
            for(String menuGroupName : menuGroupNames.split(",")){
                index++;
                LinearLayout menuItemlayout = new LinearLayout(MenuEditActivity.this);
                menuItemlayout.setTag("close");
                menuItemlayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuItemlayout.setPadding(DisplayUtil.dip2px(MenuEditActivity.this, 10), 0, 0, 0);
                menuItemlayout.setBackgroundResource(R.drawable.edit_menu_item_style);
                menuItemlayout.setClickable(true);
                menuItemlayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuItemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(MenuEditActivity.this, 40));
                menuItemlayout.setLayoutParams(menuItemLp);
                // item图标
                ImageView menuItemImg = new ImageView(MenuEditActivity.this);
                menuItemImg.setTag("itemImg");
                menuItemImg.setImageResource(R.drawable.menu_close);
                LinearLayout.LayoutParams menuItemImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MenuEditActivity.this, 20), DisplayUtil.dip2px(MenuEditActivity.this, 29));
                menuItemImgLp.setMargins(DisplayUtil.dip2px(MenuEditActivity.this, 2), DisplayUtil.dip2px(MenuEditActivity.this, 2), DisplayUtil.dip2px(MenuEditActivity.this, 2), DisplayUtil.dip2px(MenuEditActivity.this, 2));
                menuItemImg.setLayoutParams(menuItemImgLp);
                menuItemlayout.addView(menuItemImg);
                // item文字
                TextView menuItemDes = new TextView(MenuEditActivity.this);
                menuItemDes.setTag("menuItemNameTxt");
                menuItemDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                menuItemDes.setText(menuGroupName.split("#&#")[1]);
                TextPaint menuItemTp = menuItemDes.getPaint();
                menuItemTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuItemDesLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MenuEditActivity.this, 0), LinearLayout.LayoutParams.WRAP_CONTENT, 3);
                menuItemDesLp.setMargins(DisplayUtil.dip2px(MenuEditActivity.this, 8), 0, 0, 0);
                menuItemDes.setLayoutParams(menuItemDesLp);
                menuItemlayout.addView(menuItemDes);
                // item数量
                String menuItemChiles = SharedPreferencesTool.getFromShared(MenuEditActivity.this, "BouilliMenuInfo", "menuItemChild"+menuGroupName.split("#&#")[0]);
                TextView menuItemChildNum = new TextView(MenuEditActivity.this);
                menuItemChildNum.setTag(menuGroupName.split("#&#")[0]);
                menuItemChildNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                if(ComFun.strNull(menuItemChiles)){
                    menuItemChildNum.setText("共 "+ menuItemChiles.split(",").length +" 个");
                }else{
                    menuItemChildNum.setText("共 0 个");
                }
                TextPaint menuItemChildNumTp = menuItemChildNum.getPaint();
                menuItemChildNumTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuItemChildNumLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MenuEditActivity.this, 0), LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                menuItemChildNumLp.setMargins(DisplayUtil.dip2px(MenuEditActivity.this, 8), 0, 0, 0);
                menuItemChildNum.setLayoutParams(menuItemChildNumLp);
                menuItemlayout.addView(menuItemChildNum);
                menuMainLayout.addView(menuItemlayout);
                // 添加对应该菜品组下的详细菜品信息数据
                if(ComFun.strNull(menuItemChiles)){
                    LinearLayout menuChildlayout = new LinearLayout(MenuEditActivity.this);
                    menuChildlayout.setTag("itemChild_"+ menuGroupName.split("#&#")[0]);
                    menuChildlayout.setOrientation(LinearLayout.VERTICAL);
                    menuChildlayout.setVisibility(View.GONE);// 默认隐藏下拉子项
                    int chileItemIndex = 0;
                    for(String childInfo : menuItemChiles.split(",")){
                        LinearLayout menuChildItemlayout = new LinearLayout(MenuEditActivity.this);
                        menuChildItemlayout.setTag(menuGroupName.split("#&#")[0]);
                        menuChildItemlayout.setPadding(DisplayUtil.dip2px(MenuEditActivity.this, 20), 0, DisplayUtil.dip2px(MenuEditActivity.this, 20), 0);
                        if(chileItemIndex % 2 == 0){
                            menuChildItemlayout.setBackgroundColor(Color.parseColor("#aeb39f"));
                        }else{
                            menuChildItemlayout.setBackgroundColor(Color.parseColor("#c1b0be"));
                        }
                        menuChildItemlayout.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams menuChildItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        menuChildItemlayout.setLayoutParams(menuChildItemLp);
                        // 子项每一项图标
                        ImageView menuChildItemImg = new ImageView(MenuEditActivity.this);
                        menuChildItemImg.setTag(childInfo.split("#&#")[0]);
                        menuChildItemImg.setImageResource(R.drawable.menu1);
                        LinearLayout.LayoutParams menuChildItemImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MenuEditActivity.this, 45), DisplayUtil.dip2px(MenuEditActivity.this, 45));
                        menuChildItemImgLp.setMargins(DisplayUtil.dip2px(MenuEditActivity.this, 2), DisplayUtil.dip2px(MenuEditActivity.this, 2), DisplayUtil.dip2px(MenuEditActivity.this, 2), DisplayUtil.dip2px(MenuEditActivity.this, 2));
                        menuChildItemImg.setLayoutParams(menuChildItemImgLp);
                        menuChildItemlayout.addView(menuChildItemImg);
                        // 子项每一项主体（名称带简要说明）
                        LinearLayout menuChildItemDeslayout = new LinearLayout(MenuEditActivity.this);
                        menuChildItemDeslayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                        menuChildItemDeslayout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams menuChildItemDesLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MenuEditActivity.this, 0), LinearLayout.LayoutParams.MATCH_PARENT, 3);
                        menuChildItemDesLp.setMargins(DisplayUtil.dip2px(MenuEditActivity.this, 8), 0, 0, 0);
                        menuChildItemDeslayout.setLayoutParams(menuChildItemDesLp);
                        // 主体-->菜名
                        TextView menuChildItemDesNameTxt = new TextView(MenuEditActivity.this);
                        menuChildItemDesNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        menuChildItemDesNameTxt.setText(childInfo.split("#&#")[2]);
                        menuChildItemDesNameTxt.setTextColor(Color.parseColor("#000000"));
                        TextPaint menuChildItemDesNameTxtTp = menuChildItemDesNameTxt.getPaint();
                        menuChildItemDesNameTxtTp.setFakeBoldText(true);
                        LinearLayout.LayoutParams menuChildItemDesNameTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        menuChildItemDesNameTxt.setLayoutParams(menuChildItemDesNameTxtLp);
                        menuChildItemDeslayout.addView(menuChildItemDesNameTxt);
                        // 主体-->菜简介
                        if(!childInfo.split("#&#")[3].equals("-")){
                            TextView menuChildItemDesssTxt = new TextView(MenuEditActivity.this);
                            menuChildItemDesssTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            menuChildItemDesssTxt.setText(childInfo.split("#&#")[3]);
                            TextPaint menuChildItemDesssTxtTp = menuChildItemDesssTxt.getPaint();
                            menuChildItemDesssTxtTp.setFakeBoldText(true);
                            LinearLayout.LayoutParams menuChildItemDesssTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            menuChildItemDesssTxt.setLayoutParams(menuChildItemDesssTxtLp);
                            menuChildItemDeslayout.addView(menuChildItemDesssTxt);
                        }
                        menuChildItemlayout.addView(menuChildItemDeslayout);
                        // 子项每一项单价
                        LinearLayout menuChildItemPricelayout = new LinearLayout(MenuEditActivity.this);
                        menuChildItemPricelayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                        menuChildItemPricelayout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams menuChildItemPriceLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MenuEditActivity.this, 0), LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        menuChildItemPriceLp.setMargins(DisplayUtil.dip2px(MenuEditActivity.this, 8), 0, 0, 0);
                        menuChildItemPricelayout.setLayoutParams(menuChildItemPriceLp);
                        // 单价文本
                        TextView menuChildItemPriceTxt = new TextView(MenuEditActivity.this);
                        menuChildItemPriceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        menuChildItemPriceTxt.setText(childInfo.split("#&#")[4] + " 元");
                        TextPaint menuChildItemPriceTxtTp = menuChildItemPriceTxt.getPaint();
                        menuChildItemPriceTxtTp.setFakeBoldText(true);
                        LinearLayout.LayoutParams menuChildItemPriceTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        menuChildItemPriceTxt.setLayoutParams(menuChildItemPriceTxtLp);
                        menuChildItemPricelayout.addView(menuChildItemPriceTxt);
                        menuChildItemlayout.addView(menuChildItemPricelayout);

                        menuChildlayout.addView(menuChildItemlayout);
                        menuChildItemlayout.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                final String menuGroupId = v.getTag().toString();
                                final String menuNameId = (((LinearLayout) v).getChildAt(0)).getTag().toString();
                                final String menuNameInfo = ((TextView) ((LinearLayout) ((LinearLayout) v).getChildAt(1)).getChildAt(0)).getText().toString();
                                new android.support.v7.app.AlertDialog.Builder(MenuEditActivity.this).setTitle("删除菜品").setMessage("确认删除菜品【"+ menuNameInfo +"】吗？")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // 显示加载动画
                                                ComFun.showLoading(MenuEditActivity.this, "正在删除菜品，请稍后");
                                                List<String> deleteMenuGroupIds = new ArrayList<>();
                                                deleteMenuGroupIds.add(menuGroupId);
                                                new DeleteMenuTask(MenuEditActivity.this, "menuInfo", deleteMenuGroupIds, menuNameId, menuNameInfo).executeOnExecutor(Executors.newCachedThreadPool());
                                            }
                                        })
                                        .setNegativeButton("取消", null).show();
                                return false;
                            }
                        });
                        chileItemIndex++;
                    }
                    menuMainLayout.addView(menuChildlayout);
                    LinearLayout.LayoutParams menuChildLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    menuChildlayout.setLayoutParams(menuChildLp);
                }
                // 如果不是最后一项，则添加分割线
                if(index < menuGroupNames.split(",").length){
                    View splitView = new View(MenuEditActivity.this);
                    splitView.setBackgroundColor(Color.parseColor("#b7b7b7"));
                    LinearLayout.LayoutParams splitViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(MenuEditActivity.this, 1));
                    splitView.setLayoutParams(splitViewLp);
                    menuMainLayout.addView(splitView);
                }
                // 点击item事件
                menuItemlayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String openCloseState = v.getTag().toString();
                        String menuItemAboutGroupId = ((LinearLayout)v).getChildAt(2).getTag().toString();
                        if(openCloseState.equals("close")){
                            v.setTag("open");
                            ImageView itemImg = (ImageView) v.findViewWithTag("itemImg");
                            itemImg.setImageResource(R.drawable.menu_open);
                            LinearLayout itemChildLayout = (LinearLayout) menuMainLayout.findViewWithTag("itemChild_"+ menuItemAboutGroupId);
                            if(itemChildLayout != null){
                                itemChildLayout.setVisibility(View.VISIBLE);
                            }
                        }else{
                            v.setTag("close");
                            ImageView itemImg = (ImageView) v.findViewWithTag("itemImg");
                            itemImg.setImageResource(R.drawable.menu_close);
                            LinearLayout itemChildLayout = (LinearLayout) menuMainLayout.findViewWithTag("itemChild_"+ menuItemAboutGroupId);
                            if(itemChildLayout != null){
                                itemChildLayout.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }
        }
    }

    // 初始化添加新菜品悬浮按钮及事件
    public void initAddNewMenu(){
        add_new_menu_des_info = (FloatingActionButton) findViewById(R.id.add_new_menu_des_info);
        add_new_menu_des_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuGroupNames = SharedPreferencesTool.getFromShared(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames");
                if(ComFun.strNull(menuGroupNames)){
                    View editTablePopup = getLayoutInflater().inflate(R.layout.edit_menu_info, null);
                    rgMenuInfoGroup = (RadioGroup) editTablePopup.findViewById(R.id.rgMenuInfoGroup);
                    rgMenuInfoGroup.removeAllViews();
                    for(String menuGroupN : menuGroupNames.split(",")){
                        RadioButton radioButton = new RadioButton(MenuEditActivity.this);
                        radioButton.setTag(menuGroupN.split("#&#")[0]);
                        RadioGroup.LayoutParams radioButtonLp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                        radioButton.setLayoutParams(radioButtonLp);
                        radioButton.setText(menuGroupN.split("#&#")[1]);
                        rgMenuInfoGroup.addView(radioButton);
                    }
                    editMenuPupopWindow = new PopupWindow(editTablePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    editMenuPupopWindow.setTouchable(true);
                    editMenuPupopWindow.setOutsideTouchable(true);
                    editMenuPupopWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

                    editMenuPupopWindow.showAsDropDown(findViewById(R.id.activity_edit_menu_title_bar));

                    etMenuInfoName = (EditText) editTablePopup.findViewById(R.id.etMenuInfoName);
                    etMenuInfoDes = (EditText) editTablePopup.findViewById(R.id.etMenuInfoDes);
                    etMenuInfoPrice = (EditText) editTablePopup.findViewById(R.id.etMenuInfoPrice);
                    btn_save_menu_info = (Button) editTablePopup.findViewById(R.id.btn_save_menu_info);
                    // 打开输入法
                    etMenuInfoName.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
                        @Override
                        public void onGlobalLayout() {
                            ComFun.openIME(MenuEditActivity.this, etMenuInfoName);
                        }
                    });

                    btn_save_menu_info.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            // 提交前输入验证
                            String selectMenuGroupId = "";
                            for(int i=0; i<rgMenuInfoGroup.getChildCount(); i++){
                                if(((RadioButton)rgMenuInfoGroup.getChildAt(i)).isChecked()){
                                    selectMenuGroupId = rgMenuInfoGroup.getChildAt(i).getTag().toString();
                                }
                            }
                            if(etMenuInfoName.getText().toString().trim().equals("")){
                                ComFun.showToast(MenuEditActivity.this, "请输入菜品名称", Toast.LENGTH_SHORT);
                            }else if(selectMenuGroupId.trim().equals("")){
                                ComFun.showToast(MenuEditActivity.this, "请选择菜品类型", Toast.LENGTH_SHORT);
                            }else if(etMenuInfoPrice.getText().toString().trim().equals("")){
                                ComFun.showToast(MenuEditActivity.this, "请输入菜品单价", Toast.LENGTH_SHORT);
                            }else{
                                // 验证成功，提交数据到服务器
                                if(editMenuPupopWindow.isShowing()){
                                    editMenuPupopWindow.dismiss();
                                }
                                // 关闭输入法键盘
                                ComFun.closeIME(MenuEditActivity.this, etMenuInfoName);
                                // 显示加载动画
                                ComFun.showLoading(MenuEditActivity.this, "菜品数据提交中，请稍后");
                                // 异步任务提交数据
                                new AddNewMenuTask(MenuEditActivity.this, null, etMenuInfoName.getText().toString(), selectMenuGroupId,
                                        etMenuInfoDes.getText().toString(), etMenuInfoPrice.getText().toString(), "menuInfo").executeOnExecutor(Executors.newCachedThreadPool());
                            }
                        }
                    });
                }else{
                    ComFun.showToast(MenuEditActivity.this, "当前未添加任何菜单组，请先添加组", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    // 初始化添加新菜品组悬浮按钮及事件
    public void initAddNewMenuGroup(){
        add_new_menu_info = (FloatingActionButton) findViewById(R.id.add_new_menu_info);
        add_new_menu_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View editTablePopup = getLayoutInflater().inflate(R.layout.edit_menu, null);
                editMenuPupopWindow = new PopupWindow(editTablePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                editMenuPupopWindow.setTouchable(true);
                editMenuPupopWindow.setOutsideTouchable(true);
                editMenuPupopWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

                editMenuPupopWindow.showAsDropDown(findViewById(R.id.activity_edit_menu_title_bar));

                etMenuGroupName = (EditText) editTablePopup.findViewById(R.id.etMenuGroupName);
                btn_save_menu_group = (Button) editTablePopup.findViewById(R.id.btn_save_menu_group);
                // 打开输入法
                etMenuGroupName.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
                    @Override
                    public void onGlobalLayout() {
                        ComFun.openIME(MenuEditActivity.this, etMenuGroupName);
                    }
                });

                btn_save_menu_group.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        // 提交前输入验证
                        if(etMenuGroupName.getText().toString().equals("")){
                            ComFun.showToast(MenuEditActivity.this, "请输入组名称", Toast.LENGTH_SHORT);
                        }else{
                            // 验证成功，提交数据到服务器
                            if(editMenuPupopWindow.isShowing()){
                                editMenuPupopWindow.dismiss();
                            }
                            // 关闭输入法键盘
                            ComFun.closeIME(MenuEditActivity.this, etMenuGroupName);
                            // 显示加载动画
                            ComFun.showLoading(MenuEditActivity.this, "菜品数据提交中，请稍后");
                            // 异步任务提交数据
                            new AddNewMenuTask(MenuEditActivity.this, etMenuGroupName.getText().toString(), null, null, null, null, "group").executeOnExecutor(Executors.newCachedThreadPool());
                        }
                    }
                });
            }
        });
    }

    // 初始化删除桌组悬浮按钮及事件
    public void initDeleteMenuGroup(){
        delete_new_menu_info = (FloatingActionButton) findViewById(R.id.delete_new_menu_info);
        delete_new_menu_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectMenuGroupIdList.clear();
                menuGroupNames = SharedPreferencesTool.getFromShared(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames");
                if(ComFun.strNull(menuGroupNames)){
                    flags = new boolean[menuGroupNames.split(",").length];
                    String[] menuGroupNameArr = new String[menuGroupNames.split(",").length];
                    for(int i=0; i<menuGroupNames.split(",").length; i++){
                        flags[i] = false;
                        menuGroupNameArr[i] = menuGroupNames.split(",")[i].split("#&#")[1];
                    }
                    AlertDialog.Builder builder=new android.app.AlertDialog.Builder(MenuEditActivity.this);
                    //设置对话框的图标
                    builder.setIcon(R.drawable.mode);
                    //设置对话框的标题
                    builder.setTitle("删除菜品组(将删除组内所有菜品)");
                    builder.setMultiChoiceItems(menuGroupNameArr, flags, new DialogInterface.OnMultiChoiceClickListener(){
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            flags[which] = isChecked;
                            String menuGroupNames = SharedPreferencesTool.getFromShared(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames");
                            deleteSelectMenuGroupIdList.add(menuGroupNames.split(",")[which].split("#&#")[0]);
                        }
                    });

                    //添加一个确定按钮
                    builder.setPositiveButton(" 确 定 ", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            if(deleteSelectMenuGroupIdList.size() > 0){
                                // 显示加载动画
                                ComFun.showLoading(MenuEditActivity.this, "正在删除菜品组，请稍后");
                                new DeleteMenuTask(MenuEditActivity.this, "group", deleteSelectMenuGroupIdList, null, null).executeOnExecutor(Executors.newCachedThreadPool());
                            }else{
                                ComFun.showToast(MenuEditActivity.this, "没有选择要删除的菜品组", Toast.LENGTH_SHORT);
                            }
                        }
                    });
                    //添加一个取消按钮
                    builder.setNegativeButton(" 取 消 ", null);
                    //创建一个复选框对话框
                    Dialog dialog = builder.create();
                    dialog.show();
                }else{
                    ComFun.showToast(MenuEditActivity.this, "菜品组信息为空，请添加桌组管理", Toast.LENGTH_SHORT);
                }
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
                case MSG_ADD_MENU:
                    // 隐藏加载动画
                    ComFun.hideLoading(MenuEditActivity.this);
                    String addType = b.getString("addType");
                    String addNewMenuResult = b.getString("addNewMenuResult");
                    String groupName = b.getString("groupName");
                    String menuInfoName = b.getString("menuInfoName");
                    String menuInfoDes = b.getString("menuInfoDes");
                    String menuInfoPrice = b.getString("menuInfoPrice");
                    if (addNewMenuResult.equals("true")) {
                        String otherData = "";
                        if(b.containsKey("otherData")){
                            otherData = b.getString("otherData");
                        }
                        // 添加或更新本地菜品组数据
                        if(addType.equals("group")){
                            ComFun.showToast(MenuEditActivity.this, "添加新菜品组成功", Toast.LENGTH_SHORT);
                            if(ComFun.strNull(otherData)){
                                menuGroupNames = SharedPreferencesTool.getFromShared(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames");
                                if(ComFun.strNull(menuGroupNames)){
                                    if(!ComFun.strInArr(menuGroupNames.split(","), groupName)){
                                        SharedPreferencesTool.addOrUpdate(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames", menuGroupNames + "," + otherData + "#&#" + groupName);
                                    }
                                }else{
                                    SharedPreferencesTool.addOrUpdate(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames", otherData + "#&#" + groupName);
                                }
                            }
                        }else{
                            ComFun.showToast(MenuEditActivity.this, "添加新菜品【"+ menuInfoName +"】成功", Toast.LENGTH_SHORT);
                            if(ComFun.strNull(otherData)){
                                BigDecimal price = new BigDecimal(menuInfoPrice);
                                price.setScale(2);
                                String menuItemChiles = SharedPreferencesTool.getFromShared(MenuEditActivity.this, "BouilliMenuInfo", "menuItemChild"+otherData.split(",")[1]);
                                String newMenuInfos = otherData.split(",")[0] + "#&#" + otherData.split(",")[1] + "#&#" + menuInfoName
                                        + "#&#" + (ComFun.strNull(menuInfoDes)?menuInfoDes:"-") + "#&#" + price + "#&#0";
                                if(ComFun.strNull(menuItemChiles)){
                                    SharedPreferencesTool.addOrUpdate(MenuEditActivity.this, "BouilliMenuInfo", "menuItemChild"+otherData.split(",")[1], menuItemChiles + "," + newMenuInfos);
                                }else{
                                    SharedPreferencesTool.addOrUpdate(MenuEditActivity.this, "BouilliMenuInfo", "menuItemChild"+otherData.split(",")[1], newMenuInfos);
                                }
                            }
                        }
                        initMenuView();
                    }else if (addNewMenuResult.equals("false")) {
                        if(addType.equals("group")){
                            ComFun.showToast(MenuEditActivity.this, "添加新菜品组失败，请联系管理员", Toast.LENGTH_SHORT);
                        }else{
                            ComFun.showToast(MenuEditActivity.this, "添加新菜品失败，请联系管理员", Toast.LENGTH_SHORT);
                        }
                    }else if (addNewMenuResult.equals("time_out")) {
                        if(addType.equals("group")){
                            ComFun.showToast(MenuEditActivity.this, "添加新菜品组超时，请稍后重试", Toast.LENGTH_SHORT);
                        }else{
                            ComFun.showToast(MenuEditActivity.this, "添加新菜品超时，请稍后重试", Toast.LENGTH_SHORT);
                        }
                    }else if (addNewMenuResult.equals("has_menu_group")) {
                        if(addType.equals("group")){
                            ComFun.showToast(MenuEditActivity.this, "组【"+ groupName +"已经存在，请重新添加", Toast.LENGTH_SHORT);
                        }
                    }
                    break;
                case MSG_DELETE_MENU:
                    // 隐藏加载动画
                    ComFun.hideLoading(MenuEditActivity.this);
                    String deleteType = b.getString("deleteType");
                    String deleteMenuResult = b.getString("deleteMenuResult");
                    String menuGroupIds = b.getString("menuGroupIds");
                    String menuInfoId = b.getString("menuInfoId");
                    String menuInfoNameFDel = b.getString("menuInfoNameFDel");
                    if (deleteMenuResult.equals("true")) {
                        // 更新本地菜品组数据
                        if(deleteType.equals("group")){
                            ComFun.showToast(MenuEditActivity.this, "删除菜品组成功", Toast.LENGTH_SHORT);
                            menuGroupNames = SharedPreferencesTool.getFromShared(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames");
                            if(ComFun.strNull(menuGroupNames)){
                                StringBuilder menuGroupDelAfter = new StringBuilder("");
                                for(String menuGroup : menuGroupNames.split(",")){
                                    if(!ComFun.strInArr(menuGroupIds.split(","), menuGroup.split("#&#")[0])){
                                        menuGroupDelAfter.append(menuGroup);
                                        menuGroupDelAfter.append(",");
                                    }
                                }
                                if(ComFun.strNull(menuGroupDelAfter.toString())){
                                    SharedPreferencesTool.addOrUpdate(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames", menuGroupDelAfter.toString().substring(0, menuGroupDelAfter.toString().length() - 1));
                                }else{
                                    SharedPreferencesTool.addOrUpdate(MenuEditActivity.this, "BouilliMenuInfo", "menuGroupNames", "");
                                }
                            }
                        }else{
                            if(!menuGroupIds.contains(",")){
                                ComFun.showToast(MenuEditActivity.this, "删除菜品【"+ menuInfoNameFDel +"】成功", Toast.LENGTH_SHORT);
                                String menuItemChiles = SharedPreferencesTool.getFromShared(MenuEditActivity.this, "BouilliMenuInfo", "menuItemChild"+menuGroupIds);
                                StringBuilder menuInfoDelAfter = new StringBuilder("");
                                for(String menuItem : menuItemChiles.split(",")){
                                    if(!menuItem.split("#&#")[0].equals(menuInfoId)){
                                        menuInfoDelAfter.append(menuItem);
                                        menuInfoDelAfter.append(",");
                                    }
                                }
                                if(ComFun.strNull(menuInfoDelAfter.toString())){
                                    SharedPreferencesTool.addOrUpdate(MenuEditActivity.this, "BouilliMenuInfo", "menuItemChild"+menuGroupIds, menuInfoDelAfter.toString().substring(0, menuInfoDelAfter.toString().length() - 1));
                                }else{
                                    SharedPreferencesTool.addOrUpdate(MenuEditActivity.this, "BouilliMenuInfo", "menuItemChild"+menuGroupIds, "");
                                }
                            }
                        }
                        initMenuView();
                    }else if (deleteMenuResult.equals("false")) {
                        if(deleteType.equals("group")){
                            ComFun.showToast(MenuEditActivity.this, "删除菜品组失败，请联系管理员", Toast.LENGTH_SHORT);
                        }else{
                            ComFun.showToast(MenuEditActivity.this, "删除菜品失败，请联系管理员", Toast.LENGTH_SHORT);
                        }
                    }else if (deleteMenuResult.equals("time_out")) {
                        if(deleteType.equals("group")){
                            ComFun.showToast(MenuEditActivity.this, "删除菜品组超时，请稍后重试", Toast.LENGTH_SHORT);
                        }else{
                            ComFun.showToast(MenuEditActivity.this, "删除菜品超时，请稍后重试", Toast.LENGTH_SHORT);
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
