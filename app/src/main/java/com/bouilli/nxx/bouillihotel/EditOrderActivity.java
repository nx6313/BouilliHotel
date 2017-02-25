package com.bouilli.nxx.bouillihotel;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.GetMenuInThisTableTask;
import com.bouilli.nxx.bouillihotel.asyncTask.GetPrintInfoTask;
import com.bouilli.nxx.bouillihotel.asyncTask.SendMenuTask;
import com.bouilli.nxx.bouillihotel.asyncTask.SettleAccountTask;
import com.bouilli.nxx.bouillihotel.customview.ClearEditText;
import com.bouilli.nxx.bouillihotel.fragment.MainFragment;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SerializableMap;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class EditOrderActivity extends Activity {
    public static Handler mHandler = null;
    public static final int MSG_SEND_MENU = 1;
    public static final int MSG_ACCOUNT = 2;
    public static final int MSG_GET_TABLE_ORDER_INFO = 3;
    public static final int MSG_GET_PRINT_INFO_ACCOUNT_NEED = 4;
    public static final int RESULT_FOR_SELECT_MENU = 1;
    private LinearLayout editOrderLayout;
    private LinearLayout orderPage_mainLayout;

    public static Map<String, Object[]> tableReadyOrderMap = new HashMap<>();// 保存该餐桌正在制作中的菜品信息
    // 键：菜品id，值：[菜品信息(菜id #&# 菜组id #&# 菜名称 #&# 菜描述 #&# 菜单价 #&# 菜被点次数), 点餐数量, 备注信息]
    public static Map<String, Object[]> tableHasNewOrderMap = new HashMap<>();// 保存该餐桌新选择的菜品信息

    private Intent toThisIntent;
    private Button btnOrderPageEndMenu;
    private Button btnOrderPageUpMenu;
    private Button btnOrderPageAddNewMenu;
    private TextView orderPage_currentTotalMoney;

    private Map<String, String> printForAccountMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_edit_order);

        mHandler = new EditOrderActivity.mHandler();

        tableReadyOrderMap = new HashMap<>();// 保存该餐桌正在制作中的菜品信息
        tableHasNewOrderMap = new HashMap<>();// 保存该餐桌新选择的菜品信息

        editOrderLayout = (LinearLayout) findViewById(R.id.content_edit_order);

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        int screenHeight = outMetrics.heightPixels;

        // 初始化布局宽度和高度
        //editOrderLayout.getLayoutParams().width = screenWidth * 7 / 8;
        //editOrderLayout.getLayoutParams().height = screenHeight * 3 / 4;
        // 初始化点击按钮
        initBtnEvent();
        // 初始化布局背景色
        toThisIntent = this.getIntent();
        String tableNum = toThisIntent.getExtras().getString("tableNum");
        TextView orderPage_tableNum = (TextView) findViewById(R.id.orderPage_tableNum);
        final Switch orderSwitch = (Switch) findViewById(R.id.orderSwitch);
        final TextView orderSwitchType = (TextView) findViewById(R.id.orderSwitchType);
        if(!tableNum.equals("-1")){
            orderPage_tableNum.setText("餐桌号【 " + tableNum + " 】");
        }else{
            orderPage_tableNum.setVisibility(View.GONE);
            LinearLayout orderSwitchLayout = (LinearLayout) findViewById(R.id.orderSwitchLayout);
            orderSwitchLayout.setVisibility(View.VISIBLE);
            orderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        orderSwitchType.setText("打包餐");
                    }else{
                        orderSwitchType.setText("外卖餐");
                    }
                }
            });
        }
        initThisTableOrderedView();
        int showType = toThisIntent.getExtras().getInt("showType");
        if(showType == 1){// 空闲
            editOrderLayout.setBackgroundColor(Color.parseColor("#C9F1DD"));
            // 直接跳转选菜页面
            Intent toSelectMenuIntent = new Intent(EditOrderActivity.this, SelectMenuActivity.class);
            toSelectMenuIntent.putExtra("tableNum", tableNum);
            startActivityForResult(toSelectMenuIntent, RESULT_FOR_SELECT_MENU);
        }else if(showType == -1){// 编辑中(本地状态)
            editOrderLayout.setBackgroundColor(Color.parseColor("#C9F1DD"));
            new android.support.v7.app.AlertDialog.Builder(EditOrderActivity.this).setTitle("提示").setMessage("该餐桌存在草稿数据，确定继续使用吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SerializableMap hasOrderInEditBook = (SerializableMap) toThisIntent.getSerializableExtra("hasOrderInEditBook");
                            tableHasNewOrderMap.clear();
                            tableHasNewOrderMap.putAll(hasOrderInEditBook.getMap());
                            initThisTableOrderedView();
                        }
                    })
                    .setNegativeButton("取消", null).show();
        }else if(showType == -10){// 打包/外卖
            editOrderLayout.setBackgroundColor(Color.parseColor("#C9F1DD"));
            // 弹框提问是哪种类型
            new android.support.v7.app.AlertDialog.Builder(EditOrderActivity.this).setTitle("提示").setMessage("请选择打包还是外卖？")
                    .setPositiveButton("外卖餐", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            orderSwitch.setChecked(false);
                            orderSwitchType.setText("外卖餐");
                            // 跳转选菜页面
                            Intent toSelectMenuIntent = new Intent(EditOrderActivity.this, SelectMenuActivity.class);
                            toSelectMenuIntent.putExtra("tableNum", "wmTable");
                            startActivityForResult(toSelectMenuIntent, RESULT_FOR_SELECT_MENU);
                        }
                    })
                    .setNegativeButton("打包餐", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            orderSwitch.setChecked(true);
                            orderSwitchType.setText("打包餐");
                            // 跳转选菜页面
                            Intent toSelectMenuIntent = new Intent(EditOrderActivity.this, SelectMenuActivity.class);
                            toSelectMenuIntent.putExtra("tableNum", "dbTable");
                            startActivityForResult(toSelectMenuIntent, RESULT_FOR_SELECT_MENU);
                        }
                    }).show();
        }else{// 占用
            String tableOrderId = toThisIntent.getExtras().getString("tableOrderId");
            editOrderLayout.setBackgroundColor(Color.parseColor("#F8EDEA"));
            // 调用任务根据餐桌号获取该餐桌就餐信息数据
            new GetMenuInThisTableTask(EditOrderActivity.this, tableOrderId).executeOnExecutor(Executors.newCachedThreadPool());
        }
        // 初始化打票机可选项
        new GetPrintInfoTask(EditOrderActivity.this, true).executeOnExecutor(Executors.newCachedThreadPool());
    }

    // 初始化点击按钮
    public void initBtnEvent(){
        // 结账
        btnOrderPageEndMenu = (Button) findViewById(R.id.btnOrderPageEndMenu);
        btnOrderPageEndMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 只结算传过菜的价格
                if(tableReadyOrderMap.size() > 0){
                    // 显示弹框，输入实收金额，计算找零
                    final AlertDialog accountDialog = new AlertDialog.Builder(EditOrderActivity.this).setCancelable(true).create();
                    accountDialog.setView(new EditText(EditOrderActivity.this));
                    accountDialog.show();
                    Window win = accountDialog.getWindow();
                    View accountView = EditOrderActivity.this.getLayoutInflater().inflate(R.layout.account_dialog_view, null);
                    win.setContentView(accountView);
                    TextView amountMoney = (TextView) accountView.findViewById(R.id.amountMoney);
                    double totalMoney2 = 0.0;
                    for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                        BigDecimal price = new BigDecimal(map.getValue()[0].toString().split("#&#")[4]);
                        int buyNum = Integer.parseInt(map.getValue()[1].toString());
                        BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                        totalMoney2 = ComFun.add(totalMoney2, thisTotalPrice);
                    }
                    if(totalMoney2 > 0){
                        amountMoney.setText("应收金额：￥ "+ ComFun.addZero(String.valueOf(totalMoney2)) +" 元");
                    }
                    final ClearEditText realMoney = (ClearEditText) accountView.findViewById(R.id.realMoney);
                    realMoney.setTag(String.valueOf(totalMoney2));
                    final TextView zeroMoney = (TextView) accountView.findViewById(R.id.zeroMoney);
                    realMoney.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if(ComFun.strNull(s.toString())){
                                // 找零 = 实收 - 应收
                                double shiJiGetMoney = Double.parseDouble(s.toString());
                                double shouldGetMoney = Double.parseDouble(realMoney.getTag().toString());
                                double zeroGetMoney = ComFun.sub(shiJiGetMoney, shouldGetMoney);
                                if(zeroGetMoney >= 0){
                                    zeroMoney.setText("找零金额：￥ "+ ComFun.addZero(String.valueOf(zeroGetMoney)) +" 元");
                                }else{
                                    zeroMoney.setText("找零金额：￥ ##.## 元");
                                }
                            }else{
                                // 找零金额赋值为0
                                zeroMoney.setText("找零金额：￥ 0.00 元");
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                    final CheckBox cbPrintSmailBill = (CheckBox) accountView.findViewById(R.id.cbPrintSmailBill);
                    final HorizontalScrollView printSmailBillScrollView = (HorizontalScrollView) accountView.findViewById(R.id.printSmailBillScrollView);
                    final RadioGroup rgPrintGroup = (RadioGroup) accountView.findViewById(R.id.rgPrintGroup);
                    rgPrintGroup.removeAllViews();
                    // 添加可选打票机布局
                    if(printForAccountMap.size() > 0){
                        for(Map.Entry<String, String> accountMap : printForAccountMap.entrySet()){
                            RadioButton accounpPrintRb = new RadioButton(EditOrderActivity.this);
                            accounpPrintRb.setText(accountMap.getValue());
                            accounpPrintRb.setTag(accountMap.getKey());
                            RadioGroup.LayoutParams accounpPrintRbLp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                            accounpPrintRb.setLayoutParams(accounpPrintRbLp);
                            rgPrintGroup.addView(accounpPrintRb);
                        }
                    }
                    cbPrintSmailBill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(rgPrintGroup.getChildCount() > 0){
                                if(isChecked){
                                    rgPrintGroup.clearCheck();
                                    printSmailBillScrollView.setVisibility(View.VISIBLE);
                                    TranslateAnimation animation = new TranslateAnimation(0, 0, -60, 0);
                                    animation.setDuration(200);//设置动画持续时间
                                    animation.setRepeatCount(0);//设置重复次数
                                    animation.setFillAfter(true);
                                    animation.setInterpolator(new OvershootInterpolator());
                                    printSmailBillScrollView.startAnimation(animation);
                                }else{
                                    printSmailBillScrollView.setVisibility(View.GONE);
                                }
                            }else{
                                ComFun.showToast(EditOrderActivity.this, "暂未设置可以打印小票的设备", Toast.LENGTH_SHORT);
                                buttonView.setChecked(false);
                            }
                        }
                    });
                    Button btnAccounts = (Button) accountView.findViewById(R.id.btnAccounts);
                    btnAccounts.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            // 先判断是否需要打印收据小票
                            if(cbPrintSmailBill.isChecked()){
                                // 判断是否选择了需要打印收据小票的设备
                                if(rgPrintGroup.getCheckedRadioButtonId() > 0){
                                    String printAccountBillId = rgPrintGroup.findViewById(rgPrintGroup.getCheckedRadioButtonId()).getTag().toString().trim();
                                    if(ComFun.strNull(printAccountBillId)){
                                        if(accountDialog.isShowing()){
                                            accountDialog.dismiss();
                                        }
                                        // 发送数据通知该相关设备打印收据小票
                                        ComFun.showLoading(EditOrderActivity.this, "结账中，请稍后...");
                                        String tableOrderId = toThisIntent.getExtras().getString("tableOrderId");
                                        String tableNum = toThisIntent.getExtras().getString("tableNum");
                                        // 获取生成小票内容（用#N#符代替换行）
                                        String smailBillContext = createSmailBillContext();
                                        new SettleAccountTask(EditOrderActivity.this, tableOrderId, printAccountBillId, tableNum, smailBillContext).executeOnExecutor(Executors.newCachedThreadPool());
                                    }
                                }else{
                                    ComFun.showToast(EditOrderActivity.this, "请选择打印收据小票的设备", Toast.LENGTH_SHORT);
                                }
                            }else{
                                if(accountDialog.isShowing()){
                                    accountDialog.dismiss();
                                }
                                ComFun.showLoading(EditOrderActivity.this, "结账中，请稍后...");
                                String tableOrderId = toThisIntent.getExtras().getString("tableOrderId");
                                new SettleAccountTask(EditOrderActivity.this, tableOrderId).executeOnExecutor(Executors.newCachedThreadPool());
                            }
                        }
                    });
                }else{
                    ComFun.showToast(EditOrderActivity.this, "无结账数据", Toast.LENGTH_SHORT);
                }
            }
        });
        // 传菜
        btnOrderPageUpMenu = (Button) findViewById(R.id.btnOrderPageUpMenu);
        btnOrderPageUpMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 先走任务上传菜到服务器，成功后，执行以下
                if(tableHasNewOrderMap.size() > 0){
                    ComFun.showLoading2(EditOrderActivity.this, "数据提交中，请稍后...", false);
                    String tableNum = toThisIntent.getExtras().getString("tableNum");
                    // 判断是正常点餐还是外卖打包
                    LinearLayout orderSwitchLayout = (LinearLayout) findViewById(R.id.orderSwitchLayout);
                    if(orderSwitchLayout.getVisibility() == View.VISIBLE){
                        Switch orderSwitch = (Switch) findViewById(R.id.orderSwitch);
                        if(orderSwitch.isChecked()){
                            tableNum = "dbTable";
                        }else{
                            tableNum = "wmTable";
                        }
                    }
                    int showType = toThisIntent.getExtras().getInt("showType");
                    String tableOrderId = toThisIntent.getExtras().getString("tableOrderId");
                    new SendMenuTask(EditOrderActivity.this, tableNum, showType, tableHasNewOrderMap, tableOrderId).executeOnExecutor(Executors.newCachedThreadPool());
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
                // 判断是正常点餐还是外卖打包
                LinearLayout orderSwitchLayout = (LinearLayout) findViewById(R.id.orderSwitchLayout);
                if(orderSwitchLayout.getVisibility() == View.VISIBLE){
                    Switch orderSwitch = (Switch) findViewById(R.id.orderSwitch);
                    if(orderSwitch.isChecked()){
                        toSelectMenuIntent.putExtra("tableNum", "dbTable");
                    }else{
                        toSelectMenuIntent.putExtra("tableNum", "wmTable");
                    }
                }else{
                    toSelectMenuIntent.putExtra("tableNum", tableNum);
                }
                StringBuilder hasOrderSb = new StringBuilder("");
                for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                    hasOrderSb.append(map.getKey() + "|" + map.getValue()[0] + "|" + map.getValue()[1] + "|" + map.getValue()[2]);
                    hasOrderSb.append(",");
                }
                if(ComFun.strNull(hasOrderSb.toString())){
                    toSelectMenuIntent.putExtra("tableHasOrders", hasOrderSb.toString().substring(0, hasOrderSb.toString().length() - 1));
                }
                StringBuilder hasNewOrderSb = new StringBuilder("");
                for(Map.Entry<String, Object[]> map : tableHasNewOrderMap.entrySet()){
                    hasNewOrderSb.append(map.getKey() + "|" + map.getValue()[0] + "|" + map.getValue()[1] + "|" + map.getValue()[2]);
                    hasNewOrderSb.append(",");
                }
                if(ComFun.strNull(hasNewOrderSb.toString())){
                    toSelectMenuIntent.putExtra("tableHasNewOrders", hasNewOrderSb.toString().substring(0, hasNewOrderSb.toString().length() - 1));
                }
                startActivityForResult(toSelectMenuIntent, RESULT_FOR_SELECT_MENU);
            }
        });
    }

    /**
     * 获取生成小票内容（用#N#符代替换行）
     * @return
     */
    public String createSmailBillContext(){
        StringBuilder result = new StringBuilder("");
        if(tableReadyOrderMap.size() > 0){
            double totalMoney = 0.0;
            for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                String menuName = map.getValue()[0].toString().split("#&#")[2];
                BigDecimal price = new BigDecimal(map.getValue()[0].toString().split("#&#")[4]);
                int buyNum = Integer.parseInt(map.getValue()[1].toString());
                BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                totalMoney = ComFun.add(totalMoney, thisTotalPrice);
                result.append(menuName + " " + buyNum + "份" + " " + ComFun.addZero(String.valueOf(thisTotalPrice)) + "元");
                result.append("#N#");
            }
            // 添加总消费
            result.append("#N#");
            result.append("您共计消费： " + ComFun.addZero(String.valueOf(totalMoney)) + "元");
        }
        if(ComFun.strNull(result.toString())){
            return result.toString();
        }
        return "";
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
                        List<String> backMenuIdList = new ArrayList<>();
                        for(String selectInfo : newOrderMenus.split(",")){
                            if(ComFun.strNull(selectInfo)){
                                backMenuIdList.add(selectInfo.split("#&#")[0]);
                                tableHasNewOrderMap.put(selectInfo.split("#&#")[0], new Object[]{ selectInfo.split("%")[0], selectInfo.split("%")[1], selectInfo.split("%")[2] });
                            }
                        }
                        List<String> needDeleteMenuIdList = new ArrayList<>();
                        for(Map.Entry<String, Object[]> map : tableHasNewOrderMap.entrySet()){
                            if(!backMenuIdList.contains(map.getKey())){
                                needDeleteMenuIdList.add(map.getKey());
                            }
                        }
                        for(String needDeleteMenuId : needDeleteMenuIdList){
                            tableHasNewOrderMap.remove(needDeleteMenuId);
                        }
                    }else{
                        // 选餐页面将选择的菜清空了
                        tableHasNewOrderMap.clear();
                    }
                    // 更新订单菜品布局信息
                    initThisTableOrderedView();
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
                orderPageItemLayout.setBackgroundResource(R.drawable.bg_round_circle);
                orderPageItemLayout.setPadding(DisplayUtil.dip2px(EditOrderActivity.this, 10), DisplayUtil.dip2px(EditOrderActivity.this, 4), DisplayUtil.dip2px(EditOrderActivity.this, 10), DisplayUtil.dip2px(EditOrderActivity.this, 4));
                LinearLayout.LayoutParams orderPageItemLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                orderPageItemLayoutLp.setMargins(DisplayUtil.dip2px(EditOrderActivity.this, 15), DisplayUtil.dip2px(EditOrderActivity.this, 2), DisplayUtil.dip2px(EditOrderActivity.this, 15), DisplayUtil.dip2px(EditOrderActivity.this, 2));
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
                if(!map.getValue()[2].equals("-")){
                    TextView beiZhuTxt = new TextView(EditOrderActivity.this);
                    beiZhuTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    beiZhuTxt.setTextColor(Color.parseColor("#6F6F6F"));
                    beiZhuTxt.setPadding(DisplayUtil.dip2px(EditOrderActivity.this, 15), 0, DisplayUtil.dip2px(EditOrderActivity.this, 15), 0);
                    //beiZhuTxt.setSingleLine(true);
                    beiZhuTxt.setText(String.valueOf(map.getValue()[2]));
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
                orderRemoveBtn.setBackgroundResource(R.drawable.edit_order_btn_style_1);
                orderRemoveBtn.setPadding(0, 0, 0, 0);
                orderRemoveBtn.setText("删除");
                orderRemoveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                TextPaint orderRemoveBtnTp = orderRemoveBtn.getPaint();
                orderRemoveBtnTp.setFakeBoldText(true);
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

                final String selectMenuId = map.getKey();
                final String selectCount = String.valueOf(map.getValue()[1]);
                final String selectRemark = String.valueOf(map.getValue()[2]);
                orderPageItemLayout.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        View inputAmountValView = EditOrderActivity.this.getLayoutInflater().inflate(R.layout.input_amount_remark_val ,null);
                        final EditText input_amount_tv = (EditText) inputAmountValView.findViewById(R.id.input_amount_tv);
                        input_amount_tv.requestFocus();
                        InputMethodManager imm = (InputMethodManager) input_amount_tv.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                        input_amount_tv.setText(selectCount);
                        input_amount_tv.setSelection(input_amount_tv.getText().toString().length());
                        input_amount_tv.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if(!s.toString().isEmpty()){
                                    int value = Integer.parseInt(s.toString());
                                    if(value > 999){
                                        input_amount_tv.setText(999+"");
                                        input_amount_tv.setSelection(input_amount_tv.getText().toString().length());
                                    }
                                }
                            }
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }
                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });
                        Button input_amount_sure = (Button) inputAmountValView.findViewById(R.id.input_amount_sure);
                        final PopupWindow inputAmountValPopup = new PopupWindow(inputAmountValView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true);
                        inputAmountValPopup.setTouchable(true);
                        inputAmountValPopup.setOutsideTouchable(true);
                        ColorDrawable dw = new ColorDrawable(0xad000000);
                        inputAmountValPopup.setBackgroundDrawable(dw);
                        inputAmountValPopup.showAtLocation(EditOrderActivity.this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                        final EditText input_remark_tv = (EditText) inputAmountValView.findViewById(R.id.input_remark_tv);
                        if(ComFun.strNull(selectRemark) && !selectRemark.equals("-")){
                            input_remark_tv.setText(selectRemark);
                        }
                        input_remark_tv.setSelection(input_remark_tv.getText().toString().length());
                        input_amount_sure.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View input_amount_ok_v) {
                                if(ComFun.strNull(input_amount_tv.getText().toString().trim()) && !input_amount_tv.getText().toString().trim().equals("0")){
                                    Object[] updateSelectObjArr = tableHasNewOrderMap.get(selectMenuId);
                                    updateSelectObjArr[1] = input_amount_tv.getText().toString();
                                    initThisTableOrderedView();
                                }else{
                                    tableHasNewOrderMap.remove(selectMenuId);
                                    initThisTableOrderedView();
                                }
                                if(ComFun.strNull(input_remark_tv.getText().toString().trim())){
                                    Object[] updateSelectObjArr = tableHasNewOrderMap.get(selectMenuId);
                                    updateSelectObjArr[2] = input_remark_tv.getText().toString().trim();
                                    initThisTableOrderedView();
                                }else{
                                    Object[] updateSelectObjArr = tableHasNewOrderMap.get(selectMenuId);
                                    updateSelectObjArr[2] = "-";
                                    initThisTableOrderedView();
                                }
                                if(inputAmountValPopup != null && inputAmountValPopup.isShowing()){
                                    inputAmountValPopup.dismiss();
                                }
                            }
                        });
                    }
                });
            }
        }
        if(tableHasNewOrderMap.size() > 0 && tableReadyOrderMap.size() > 0){
            // 添加分割线布局
            View orderOldNewSplitView = new View(EditOrderActivity.this);
            orderOldNewSplitView.setBackgroundResource(R.drawable.bg_line);
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
                orderPageItemLayout.setBackgroundResource(R.drawable.bg_round_circle);
                orderPageItemLayout.setPadding(DisplayUtil.dip2px(EditOrderActivity.this, 10), DisplayUtil.dip2px(EditOrderActivity.this, 4), DisplayUtil.dip2px(EditOrderActivity.this, 10), DisplayUtil.dip2px(EditOrderActivity.this, 4));
                LinearLayout.LayoutParams orderPageItemLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                orderPageItemLayoutLp.setMargins(DisplayUtil.dip2px(EditOrderActivity.this, 15), DisplayUtil.dip2px(EditOrderActivity.this, 2), DisplayUtil.dip2px(EditOrderActivity.this, 15), DisplayUtil.dip2px(EditOrderActivity.this, 2));
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
                if(!map.getValue()[2].equals("-") && ComFun.strNull(map.getValue()[2].toString().replaceAll("-", "").replaceAll("#N#", ""))){
                    TextView beiZhuTxt = new TextView(EditOrderActivity.this);
                    beiZhuTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    beiZhuTxt.setTextColor(Color.parseColor("#6F6F6F"));
                    beiZhuTxt.setPadding(DisplayUtil.dip2px(EditOrderActivity.this, 15), 0, DisplayUtil.dip2px(EditOrderActivity.this, 15), 0);
                    //beiZhuTxt.setSingleLine(true);
                    beiZhuTxt.setText(map.getValue()[2].toString().replaceAll("#N#-", "").replaceAll("#N#", "、"));
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
        orderPage_currentTotalMoney = (TextView) findViewById(R.id.orderPage_currentTotalMoney);
        if(tableHasNewOrderMap.size() > 0 || tableReadyOrderMap.size() > 0){
            // 计算当前总金额
            double totalMoney1 = 0.0;
            for(Map.Entry<String, Object[]> map : tableHasNewOrderMap.entrySet()){
                BigDecimal price = new BigDecimal(map.getValue()[0].toString().split("#&#")[4]);
                int buyNum = Integer.parseInt(map.getValue()[1].toString());
                BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                totalMoney1 = ComFun.add(totalMoney1, thisTotalPrice);
            }
            double totalMoney2 = 0.0;
            for(Map.Entry<String, Object[]> map : tableReadyOrderMap.entrySet()){
                BigDecimal price = new BigDecimal(map.getValue()[0].toString().split("#&#")[4]);
                int buyNum = Integer.parseInt(map.getValue()[1].toString());
                BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                totalMoney2 = ComFun.add(totalMoney2, thisTotalPrice);
            }
            double totalMoney = ComFun.add(totalMoney1, new BigDecimal(totalMoney2));
            StringBuilder totalMoneySb = new StringBuilder("");
            if(totalMoney1 > 0){
                totalMoneySb.append("新增菜金额：￥"+ ComFun.addZero(String.valueOf(totalMoney1)) +" 元\n");
            }
            if(totalMoney2 > 0){
                totalMoneySb.append("已点菜金额：￥"+ ComFun.addZero(String.valueOf(totalMoney2)) +" 元\n");
            }
            totalMoneySb.append("总金额：￥"+ ComFun.addZero(String.valueOf(totalMoney)) +" 元");
            orderPage_currentTotalMoney.setText(totalMoneySb.toString());
        }else{
            orderPage_currentTotalMoney.setText("当前总金额：0.00 元");
        }
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
                case MSG_SEND_MENU:
                    // 隐藏加载动画
                    ComFun.hideLoading(EditOrderActivity.this);
                    String sendMenuResult = b.getString("sendMenuResult");
                    if (sendMenuResult.equals("true")) {
                        ComFun.showToast(EditOrderActivity.this, "提交数据成功", Toast.LENGTH_SHORT);
                        for(Map.Entry<String, Object[]> map : tableHasNewOrderMap.entrySet()){
                            if(tableReadyOrderMap.containsKey(map.getKey())){
                                Object[] objArr = tableReadyOrderMap.get(map.getKey());
                                tableReadyOrderMap.put(map.getKey(), new Object[]{ objArr[0], Integer.parseInt(objArr[1].toString()) + Integer.parseInt(map.getValue()[1].toString()), objArr[2] });
                            }else{
                                tableReadyOrderMap.put(map.getKey(), map.getValue());
                            }
                        }
                        String tableOrderInfoPId = b.getString("tableOrderInfoPId");
                        String showType = b.getString("showType");
                        if(showType.equals("-10")){
                            EditOrderActivity.this.finish();
                        }else{
                            toThisIntent.putExtra("tableOrderId", tableOrderInfoPId);
                            toThisIntent.putExtra("showType", 2);
                            tableHasNewOrderMap.clear();
                            editOrderLayout.setBackgroundColor(Color.parseColor("#F8EDEA"));
                            initThisTableOrderedView();
                        }
                    }else if (sendMenuResult.equals("false")) {
                        ComFun.showToast(EditOrderActivity.this, "提交数据失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (sendMenuResult.equals("time_out")) {
                        ComFun.showToast(EditOrderActivity.this, "提交数据超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_GET_TABLE_ORDER_INFO:
                    String getTableOrderInfoResult = b.getString("getTableOrderInfoResult");
                    if (getTableOrderInfoResult.equals("true")) {
                        // 初始化 tableReadyOrderMap 该餐桌正在制作中的菜品信息
                        if(b.containsKey("orderInfoDetails")){
                            String orderInfoDetails = b.getString("orderInfoDetails");
                            for(String orderInfo : orderInfoDetails.split(",")){
                                tableReadyOrderMap.put(orderInfo.split("\\|")[0].split("#&#")[0], new Object[]{
                                        orderInfo.split("\\|")[0], orderInfo.split("\\|")[1], orderInfo.split("\\|")[2]
                                });// 键：菜品id，值：[菜品信息(菜id #&# 菜组id #&# 菜名称 #&# 菜描述 #&# 菜单价 #&# 菜被点次数), 点餐数量, 备注信息]
                            }
                            initThisTableOrderedView();
                        }
                    }else if (getTableOrderInfoResult.equals("false")) {
                        ComFun.showToast(EditOrderActivity.this, "初始化餐桌数据失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (getTableOrderInfoResult.equals("time_out")) {
                        ComFun.showToast(EditOrderActivity.this, "初始化餐桌数据超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_ACCOUNT:
                    // 隐藏加载动画
                    ComFun.hideLoading(EditOrderActivity.this);
                    String accountResult = b.getString("accountResult");
                    if (accountResult.equals("true")) {
                        //String tableNum = toThisIntent.getExtras().getString("tableNum");
                        // 发送主页面更新广播
                        EditOrderActivity.this.finish();
                    }else if (accountResult.equals("false")) {
                        ComFun.showToast(EditOrderActivity.this, "结账操作失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (accountResult.equals("time_out")) {
                        ComFun.showToast(EditOrderActivity.this, "结账操作超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_GET_PRINT_INFO_ACCOUNT_NEED:
                    String initPrintResult = b.getString("initPrintResult");
                    if (initPrintResult.equals("true")) {
                        if(b.containsKey("AllPrintsInfo")){
                            String AllPrintsInfo = b.getString("AllPrintsInfo");
                            if(ComFun.strNull(AllPrintsInfo)){
                                for(String printInfo : AllPrintsInfo.split(",")){
                                    if(printInfo.split("#&#")[6].equals("use")){
                                        printForAccountMap.put(printInfo.split("#&#")[0], printInfo.split("#&#")[1]);
                                    }
                                }
                            }
                        }
                    }else if (initPrintResult.equals("false")) {
                        //ComFun.showToast(EditOrderActivity.this, "初始化结账打票机失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (initPrintResult.equals("time_out")) {
                        //ComFun.showToast(EditOrderActivity.this, "初始化结账打票机超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onDestroy() {
        // 首页将该餐桌设为带草稿的状态
        String tableNum = toThisIntent.getExtras().getString("tableNum");
        if(!tableNum.equals("-1")){
            if(tableReadyOrderMap.size() == 0 && tableHasNewOrderMap.size() > 0){
                // 该餐桌有未传的菜没有执行传操作，并且是新点餐桌（保存为草稿）
                ComFun.showToast(EditOrderActivity.this, tableNum + "号桌 点餐数据自动存为草稿", Toast.LENGTH_SHORT);
                MainActivity.editBookMap.put(tableNum, tableHasNewOrderMap);
            }else if(tableReadyOrderMap.size() == 0 && tableHasNewOrderMap.size() == 0 && MainActivity.editBookMap.containsKey(tableNum)){
                // 该餐桌有未传的菜没有执行传操作，并且是新点餐桌（保存为草稿）
                MainActivity.editBookMap.remove(tableNum);
            }else{
                MainActivity.editBookMap.remove(tableNum);
            }
            // 发送主页面更新广播
            Intent intent = new Intent();
            intent.putExtra("newData", true);
            intent.setAction(MainFragment.MSG_REFDATA);
            EditOrderActivity.this.sendBroadcast(intent);
        }
        super.onDestroy();
    }
}
