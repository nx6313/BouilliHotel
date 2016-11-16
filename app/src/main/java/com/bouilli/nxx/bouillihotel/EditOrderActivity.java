package com.bouilli.nxx.bouillihotel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;

import java.util.HashMap;
import java.util.Map;

public class EditOrderActivity extends Activity {
    public static final int RESULT_FOR_SELECT_MENU = 1;
    private LinearLayout editOrderLayout;
    private LinearLayout orderPage_mainLayout;
    public static Map<String, Object[]> tableReadyOrderMap = new HashMap<>();// 保存该餐桌正在制作中的菜品信息
    public static Map<String, Object[]> tableHasNewOrderMap = new HashMap<>();// 保存该餐桌新选择的菜品信息

    private Intent toThisIntent;
    private Button btnOrderPageEndMenu;
    private Button btnOrderPageUpMenu;
    private Button btnOrderPageAddNewMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order);
        tableReadyOrderMap = new HashMap<>();// 保存该餐桌正在制作中的菜品信息
        tableHasNewOrderMap = new HashMap<>();// 保存该餐桌新选择的菜品信息

        editOrderLayout = (LinearLayout) findViewById(R.id.content_edit_order);

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        int screenHeight = outMetrics.heightPixels;

        // 初始化布局宽度和高度
        editOrderLayout.getLayoutParams().width = screenWidth * 7 / 8;
        editOrderLayout.getLayoutParams().height = screenHeight * 3 / 4;
        // 初始化点击按钮
        initBtnEvent();
        // 初始化布局背景色
        toThisIntent = this.getIntent();
        String tableNum = toThisIntent.getExtras().getString("tableNum");
        TextView orderPage_tableNum = (TextView) findViewById(R.id.orderPage_tableNum);
        orderPage_tableNum.setText("餐桌号【 " + tableNum + " 】");
        initThisTableOrderedView();
        int showType = toThisIntent.getExtras().getInt("showType");
        if(showType == 1){// 空闲
            editOrderLayout.setBackgroundColor(Color.parseColor("#EAF8EF"));
            // 直接跳转选菜页面
            new Thread(){
                @Override
                public void run() {
                    try { Thread.sleep(100); }catch (Exception e){}
                    String tableNum = toThisIntent.getExtras().getString("tableNum");
                    Intent toSelectMenuIntent = new Intent(EditOrderActivity.this, SelectMenuActivity.class);
                    toSelectMenuIntent.putExtra("tableNum", tableNum);
                    startActivityForResult(toSelectMenuIntent, RESULT_FOR_SELECT_MENU);
                }
            }.start();
        }else{// 占用
            editOrderLayout.setBackgroundColor(Color.parseColor("#F8EDEA"));
            // 调用任务根据餐桌号获取该餐桌就餐信息数据
        }
    }

    // 初始化点击按钮
    public void initBtnEvent(){
        // 传菜
        btnOrderPageUpMenu = (Button) findViewById(R.id.btnOrderPageUpMenu);
        btnOrderPageUpMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 先走任务上传菜到服务器，成功后，执行以下
                if(tableHasNewOrderMap.size() > 0){
                    for(Map.Entry<String, Object[]> map : tableHasNewOrderMap.entrySet()){
                        if(tableReadyOrderMap.containsKey(map.getKey())){
                            Object[] objArr = tableReadyOrderMap.get(map.getKey());
                            tableReadyOrderMap.put(map.getKey(), new Object[]{ objArr[0], Integer.parseInt(objArr[1].toString()) + Integer.parseInt(map.getValue()[1].toString()) });
                        }else{
                            tableReadyOrderMap.put(map.getKey(), map.getValue());
                        }
                    }
                    tableHasNewOrderMap.clear();
                    initThisTableOrderedView();
                }else{
                    ComFun.showToast(EditOrderActivity.this, "该餐桌还没有点新菜哦", Toast.LENGTH_SHORT);
                }
            }
        });
        // 加菜
        btnOrderPageAddNewMenu = (Button) findViewById(R.id.btnOrderPageAddNewMenu);
        btnOrderPageAddNewMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String tableNum = toThisIntent.getExtras().getString("tableNum");
                Intent toSelectMenuIntent = new Intent(EditOrderActivity.this, SelectMenuActivity.class);
                toSelectMenuIntent.putExtra("tableNum", tableNum);
                startActivityForResult(toSelectMenuIntent, RESULT_FOR_SELECT_MENU);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case RESULT_FOR_SELECT_MENU:
                if(resultCode == SelectMenuActivity.RESULT_SUCCESS){
                    // 点击了确定，获取页面传过来的新选择的菜品
                    String newOrderMenus = data.getStringExtra("newOrderMenus");
                    if(!newOrderMenus.equals("-")){
                        // 更新餐桌详情信息页面已选菜的数据
                        for(String selectInfo : newOrderMenus.split(",")){
                            if(ComFun.strNull(selectInfo)){
                                tableHasNewOrderMap.put(selectInfo.split("#&#")[0], new Object[]{ selectInfo.split("%")[0], selectInfo.split("%")[1] });
                            }
                        }
                        // 更新订单菜品布局信息
                        initThisTableOrderedView();

                    }else{
                        // 选餐页面将选择的菜清空了
                        tableHasNewOrderMap.clear();
                    }
                }else if(resultCode == SelectMenuActivity.RESULT_CANCLE){
                    // 点击了取消或按返回键，如果该餐桌未选择任何菜，直接关闭该页面
                    int showType = toThisIntent.getExtras().getInt("showType");
                    if(showType == 1){
                        if(tableHasNewOrderMap.size() == 0){
                            finish();
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initThisTableOrderedView(){
        orderPage_mainLayout = (LinearLayout) findViewById(R.id.orderPage_mainLayout);
        orderPage_mainLayout.removeAllViews();
        if(tableHasNewOrderMap.size() > 0){
            // 添加未上报的菜布局
            for(Map.Entry<String, Object[]> map : tableHasNewOrderMap.entrySet()){
                LinearLayout orderPageItemLayout = new LinearLayout(EditOrderActivity.this);
                orderPageItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                orderPageItemLayout.setGravity(Gravity.LEFT|Gravity.CENTER);
                orderPageItemLayout.setBackgroundResource(R.drawable.order_menu_new1);
                LinearLayout.LayoutParams orderPageItemLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                orderPageItemLayout.setLayoutParams(orderPageItemLayoutLp);
                // 菜名和备注
                LinearLayout caiMingBeiZhuLayout = new LinearLayout(EditOrderActivity.this);
                caiMingBeiZhuLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams caiMingBeiZhuLayoutLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7);
                caiMingBeiZhuLayout.setLayoutParams(caiMingBeiZhuLayoutLp);
                // 菜名文字
                TextView caiMingTxt = new TextView(EditOrderActivity.this);
                caiMingTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                caiMingTxt.setTextColor(Color.parseColor("#ffffff"));
                TextPaint caiMingTxtTp = caiMingTxt.getPaint();
                caiMingTxtTp.setFakeBoldText(true);
                caiMingTxt.setSingleLine(true);
                caiMingTxt.setText(map.getValue()[0].toString().split("#&#")[2]);
                LinearLayout.LayoutParams caiMingTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                caiMingTxt.setLayoutParams(caiMingTxtLp);
                caiMingBeiZhuLayout.addView(caiMingTxt);
                // 备注文字
                if(!map.getValue()[0].toString().split("#&#")[3].equals("-")){
                    TextView beiZhuTxt = new TextView(EditOrderActivity.this);
                    beiZhuTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    beiZhuTxt.setTextColor(Color.parseColor("#e5c1ec"));
                    beiZhuTxt.setSingleLine(true);
                    beiZhuTxt.setText(map.getValue()[0].toString().split("#&#")[3]);
                    LinearLayout.LayoutParams beiZhuTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    beiZhuTxt.setLayoutParams(beiZhuTxtLp);
                    caiMingBeiZhuLayout.addView(beiZhuTxt);
                }
                orderPageItemLayout.addView(caiMingBeiZhuLayout);
                // 数量
                TextView orderCountTxt = new TextView(EditOrderActivity.this);
                orderCountTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                orderCountTxt.setTextColor(Color.parseColor("#ffffff"));
                TextPaint orderCountTxtTp = orderCountTxt.getPaint();
                orderCountTxtTp.setFakeBoldText(true);
                orderCountTxt.setSingleLine(true);
                orderCountTxt.setText(map.getValue()[1]+" 份");
                LinearLayout.LayoutParams orderCountTxtLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                orderCountTxt.setLayoutParams(orderCountTxtLp);
                orderPageItemLayout.addView(orderCountTxt);
                // 去掉该菜按钮(只在未上报部分显示)
                Button orderRemoveBtn = new Button(EditOrderActivity.this);
                orderRemoveBtn.setTag(map.getKey());
                orderRemoveBtn.setTextColor(Color.parseColor("#e1dfdf"));
                TextPaint orderRemoveBtnTp = orderRemoveBtn.getPaint();
                orderRemoveBtnTp.setFakeBoldText(true);
                orderRemoveBtn.setBackgroundResource(R.drawable.edit_order_btn_style_1);
                orderRemoveBtn.setText("去掉");
                orderRemoveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                LinearLayout.LayoutParams orderRemoveBtnLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(EditOrderActivity.this, 50), DisplayUtil.dip2px(EditOrderActivity.this, 30));
                orderRemoveBtn.setLayoutParams(orderRemoveBtnLp);
                orderRemoveBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        tableHasNewOrderMap.remove(v.getTag().toString());
                        initThisTableOrderedView();
                    }
                });
                orderPageItemLayout.addView(orderRemoveBtn);

                orderPage_mainLayout.addView(orderPageItemLayout);
            }
        }
        if(tableHasNewOrderMap.size() > 0 && tableReadyOrderMap.size() > 0){
            // 添加分割线布局
            View orderOldNewSplitView = new View(EditOrderActivity.this);
            orderOldNewSplitView.setBackgroundResource(R.drawable.line_2);
            LinearLayout.LayoutParams orderOldNewSplitViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            orderOldNewSplitViewLp.setMargins(0, DisplayUtil.dip2px(EditOrderActivity.this, 5), 0, DisplayUtil.dip2px(EditOrderActivity.this, 5));
            orderOldNewSplitView.setLayoutParams(orderOldNewSplitViewLp);

            orderPage_mainLayout.addView(orderOldNewSplitView);
        }
        if(tableReadyOrderMap.size() > 0){
            // 添加已上报的菜布局
            for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                LinearLayout orderPageItemLayout = new LinearLayout(EditOrderActivity.this);
                orderPageItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                orderPageItemLayout.setGravity(Gravity.LEFT|Gravity.CENTER);
                orderPageItemLayout.setBackgroundResource(R.drawable.order_menu_old1);
                LinearLayout.LayoutParams orderPageItemLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                orderPageItemLayout.setLayoutParams(orderPageItemLayoutLp);
                // 菜名和备注
                LinearLayout caiMingBeiZhuLayout = new LinearLayout(EditOrderActivity.this);
                caiMingBeiZhuLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams caiMingBeiZhuLayoutLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7);
                caiMingBeiZhuLayout.setLayoutParams(caiMingBeiZhuLayoutLp);
                // 菜名文字
                TextView caiMingTxt = new TextView(EditOrderActivity.this);
                caiMingTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                caiMingTxt.setTextColor(Color.parseColor("#ffffff"));
                TextPaint caiMingTxtTp = caiMingTxt.getPaint();
                caiMingTxtTp.setFakeBoldText(true);
                caiMingTxt.setSingleLine(true);
                caiMingTxt.setText(map.getValue()[0].toString().split("#&#")[2]);
                LinearLayout.LayoutParams caiMingTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                caiMingTxt.setLayoutParams(caiMingTxtLp);
                caiMingBeiZhuLayout.addView(caiMingTxt);
                // 备注文字
                if(!map.getValue()[0].toString().split("#&#")[3].equals("-")){
                    TextView beiZhuTxt = new TextView(EditOrderActivity.this);
                    beiZhuTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    beiZhuTxt.setTextColor(Color.parseColor("#e5c1ec"));
                    beiZhuTxt.setSingleLine(true);
                    beiZhuTxt.setText(map.getValue()[0].toString().split("#&#")[3]);
                    LinearLayout.LayoutParams beiZhuTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    beiZhuTxt.setLayoutParams(beiZhuTxtLp);
                    caiMingBeiZhuLayout.addView(beiZhuTxt);
                }
                orderPageItemLayout.addView(caiMingBeiZhuLayout);
                // 数量
                TextView orderCountTxt = new TextView(EditOrderActivity.this);
                orderCountTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                orderCountTxt.setTextColor(Color.parseColor("#ffffff"));
                TextPaint orderCountTxtTp = orderCountTxt.getPaint();
                orderCountTxtTp.setFakeBoldText(true);
                orderCountTxt.setSingleLine(true);
                orderCountTxt.setText(map.getValue()[1]+" 份");
                LinearLayout.LayoutParams orderCountTxtLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                orderCountTxt.setLayoutParams(orderCountTxtLp);
                orderPageItemLayout.addView(orderCountTxt);

                orderPage_mainLayout.addView(orderPageItemLayout);
            }
        }
    }
}
