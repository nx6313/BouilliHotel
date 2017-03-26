package com.bouilli.nxx.bouillihotel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.GetMenuInThisTableTask;
import com.bouilli.nxx.bouillihotel.asyncTask.GetPrintInfoTask;
import com.bouilli.nxx.bouillihotel.asyncTask.SendMenuTask;
import com.bouilli.nxx.bouillihotel.asyncTask.SettleAccountTask;
import com.bouilli.nxx.bouillihotel.asyncTask.okHttpTask.AllRequestUtil;
import com.bouilli.nxx.bouillihotel.customview.ClearEditText;
import com.bouilli.nxx.bouillihotel.fragment.MainFragment;
import com.bouilli.nxx.bouillihotel.fragment.OutOrderFragment;
import com.bouilli.nxx.bouillihotel.fragment.adapter.OrderEveryFragmentPageAdapter;
import com.bouilli.nxx.bouillihotel.okHttpUtil.request.RequestParams;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.L;
import com.bouilli.nxx.bouillihotel.util.SerializableMap;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class EditOrderActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_SEND_MENU = 1;
    public static final int MSG_ACCOUNT = 2;
    public static final int MSG_GET_TABLE_ORDER_INFO = 3;
    public static final int MSG_GET_PRINT_INFO_ACCOUNT_NEED = 4;
    public static final int RESULT_FOR_SELECT_MENU = 1;
    private LinearLayout editOrderLayout;
    private ViewPager order_every_pager;
    private OrderEveryFragmentPageAdapter orderEveryAdapter;

    public static List<Map<String, Object[]>> tableReadyOrderList = new ArrayList<>();
    public static List<Map<String, Object[]>> tableHasNewOrderList = new ArrayList<>();

    public static Map<String, Object[]> tableReadyOrderMap = new HashMap<>();// 保存该餐桌正在制作中的菜品信息
    // 键：菜品id，值：[菜品信息(菜id #&# 菜组id #&# 菜名称 #&# 菜描述 #&# 菜单价 #&# 菜被点次数), 点餐数量, 备注信息]
    public static Map<String, Object[]> tableHasNewOrderMap = new HashMap<>();// 保存该餐桌新选择的菜品信息

    private Intent toThisIntent;
    private Button btnOrderPageEndMenu;
    private Button btnOrderPageUpMenu;
    private Button btnOrderPageAddNewMenu;
    private TextView orderPage_currentTotalMoney;

    private FloatingActionButton add_new_table_order;

    private TextView editOrderSendWay;

    private Map<String, String> printForAccountMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_edit_order);

        mHandler = new EditOrderActivity.mHandler();

        btnOrderPageEndMenu = (Button) findViewById(R.id.btnOrderPageEndMenu);
        btnOrderPageUpMenu = (Button) findViewById(R.id.btnOrderPageUpMenu);
        btnOrderPageAddNewMenu = (Button) findViewById(R.id.btnOrderPageAddNewMenu);

        tableReadyOrderList = new ArrayList<>();
        tableHasNewOrderList = new ArrayList<>();
        tableReadyOrderMap = new HashMap<>();// 保存该餐桌正在制作中的菜品信息
        tableHasNewOrderMap = new HashMap<>();// 保存该餐桌新选择的菜品信息

        add_new_table_order = (FloatingActionButton) findViewById(R.id.add_new_table_order);

        order_every_pager = (ViewPager) findViewById(R.id.order_every_pager);
        FragmentManager fm = getSupportFragmentManager();
        tableReadyOrderList.add(tableReadyOrderMap);
        tableHasNewOrderList.add(tableHasNewOrderMap);
        orderEveryAdapter = new OrderEveryFragmentPageAdapter(fm, tableReadyOrderList, tableHasNewOrderList);
        order_every_pager.setAdapter(orderEveryAdapter);

        editOrderLayout = (LinearLayout) findViewById(R.id.content_edit_order);

        editOrderSendWay = (TextView) findViewById(R.id.editOrderSendWay);

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        int screenHeight = outMetrics.heightPixels;

        // 初始化布局宽度和高度
        //editOrderLayout.getLayoutParams().width = screenWidth * 7 / 8;
        //editOrderLayout.getLayoutParams().height = screenHeight * 3 / 4;
        // 初始化打票机可选项
        AllRequestUtil.GetPrintInfo(EditOrderActivity.this, null, true);
        // 初始化点击按钮
        initBtnEvent();
        // 初始化布局背景色
        toThisIntent = this.getIntent();
        final String tableNum = toThisIntent.getExtras().getString("tableNum");
        String outOrderAccount = toThisIntent.getExtras().getString("outOrderAccount");
        final TextView orderPage_tableNum = (TextView) findViewById(R.id.orderPage_tableNum);
        final Switch orderSwitch = (Switch) findViewById(R.id.orderSwitch);
        final TextView orderSwitchType = (TextView) findViewById(R.id.orderSwitchType);
        if(!tableNum.equals("-1")){
            orderPage_tableNum.setText("餐桌号【 " + tableNum + " 】");
        }else{
            if(ComFun.strNull(outOrderAccount) && outOrderAccount.equals("outAccount")){
                // 隐藏传菜和加菜按钮
                btnOrderPageUpMenu.setVisibility(View.GONE);
                btnOrderPageAddNewMenu.setVisibility(View.GONE);
                editOrderSendWay.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        String orderSendWayTag = v.getTag().toString();
                        String defaultVal = "";
                        if(!orderSendWayTag.equals("none")){
                            defaultVal = ((TextView) v).getText().toString();
                        }
                        // 弹框输入送餐联系方式
                        final AlertDialog accountDialog = new AlertDialog.Builder(EditOrderActivity.this).setCancelable(true).create();
                        accountDialog.setView(new EditText(EditOrderActivity.this));
                        accountDialog.show();
                        Window win = accountDialog.getWindow();
                        View sendWayView = EditOrderActivity.this.getLayoutInflater().inflate(R.layout.sendway_dialog_view, null);
                        win.setContentView(sendWayView);
                        final EditText userName = (EditText) sendWayView.findViewById(R.id.userName);
                        userName.clearFocus();
                        final EditText userPhoneNum = (EditText) sendWayView.findViewById(R.id.userPhoneNum);
                        final EditText userAddress = (EditText) sendWayView.findViewById(R.id.userAddress);
                        if(ComFun.strNull(defaultVal)){
                            if(!defaultVal.split("、")[0].equals("匿名")){
                                userName.setText(defaultVal.split("、")[0]);
                            }
                            userPhoneNum.setText(defaultVal.split("、")[1]);
                            userAddress.setText(defaultVal.split("、")[2]);
                        }
                        final Button btnSendWayOk = (Button) sendWayView.findViewById(R.id.btnSendWayOk);
                        btnSendWayOk.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                if(!ComFun.strNull(userName.getText().toString().trim()) &&
                                        !ComFun.strNull(userPhoneNum.getText().toString().trim()) &&
                                        !ComFun.strNull(userAddress.getText().toString().trim())){
                                    // 执行保存(本地化操作)
                                    editOrderSendWay.setTag("none");
                                }else{
                                    if(!ComFun.strNull(userPhoneNum.getText().toString().trim())){
                                        ComFun.showToast(EditOrderActivity.this, "请输入联系电话", Toast.LENGTH_SHORT);
                                        userPhoneNum.requestFocus();
                                        return;
                                    }else if(!ComFun.strNull(userAddress.getText().toString().trim())){
                                        ComFun.showToast(EditOrderActivity.this, "请输入送餐地址", Toast.LENGTH_SHORT);
                                        userAddress.requestFocus();
                                        return;
                                    }
                                    // 执行保存(本地化操作)
                                    String userNameStr = "匿名";
                                    if(ComFun.strNull(userName.getText().toString().trim())){
                                        userNameStr = userName.getText().toString().trim();
                                    }
                                    String userPhoneNumStr = userPhoneNum.getText().toString().trim();
                                    String userAddressStr = userAddress.getText().toString().trim();
                                    editOrderSendWay.setText(userNameStr + "、" + userPhoneNumStr + "、" + userAddressStr);
                                    editOrderSendWay.setTag("has");
                                }
                                accountDialog.dismiss();
                            }
                        });
                    }
                });
            }else{
                // 隐藏结账按钮
                btnOrderPageEndMenu.setVisibility(View.GONE);
            }
            orderPage_tableNum.setVisibility(View.GONE);
            LinearLayout orderSwitchLayout = (LinearLayout) findViewById(R.id.orderSwitchLayout);
            orderSwitchLayout.setVisibility(View.VISIBLE);
            if(ComFun.strNull(outOrderAccount) && outOrderAccount.equals("outAccount")){
                orderSwitch.setEnabled(false);
                String outOrderNumber = toThisIntent.getExtras().getString("outOrderNumber");
                if(outOrderNumber.startsWith("[外卖餐]")){
                    orderSwitch.setChecked(false);
                    editOrderSendWay.setVisibility(View.VISIBLE);
                }else{
                    orderSwitch.setChecked(true);
                }
                orderSwitchType.setText(outOrderNumber);
            }else{
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
        }
        initThisTableOrderedView(false);
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
                            initThisTableOrderedView(false);
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
            String tableOrderIds = toThisIntent.getExtras().getString("tableOrderId");
            editOrderLayout.setBackgroundColor(Color.parseColor("#F8EDEA"));
            // 调用任务根据餐桌号获取该餐桌就餐信息数据
            RequestParams params = new RequestParams();
            params.put("tableOrderId", tableOrderIds);
            AllRequestUtil.GetMenuInThisTable(EditOrderActivity.this, params, false, false, tableOrderIds, null);
        }

        add_new_table_order.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!(tableReadyOrderList.get(tableReadyOrderList.size() - 1).size() == 0 &&
                        tableHasNewOrderList.get(tableHasNewOrderList.size() - 1).size() == 0)){
                    Map<String, Object[]> newAddMap1 = new HashMap<>();
                    Map<String, Object[]> newAddMap2 = new HashMap<>();
                    tableReadyOrderList.add(newAddMap1);
                    tableHasNewOrderList.add(newAddMap2);
                    orderEveryAdapter.notifyDataSetChanged();
                    order_every_pager.setCurrentItem(tableReadyOrderList.size() - 1);
                    orderPage_tableNum.setText("餐桌号【 " + tableNum + " 】( 子订单 " + tableReadyOrderList.size() + " )");
                }else{
                    order_every_pager.setCurrentItem(tableReadyOrderList.size() - 1);
                    ComFun.showToast(EditOrderActivity.this, "子订单 " + tableReadyOrderList.size() + " 就是空的", Toast.LENGTH_SHORT);
                }
            }
        });
        order_every_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                orderPage_tableNum.setText("餐桌号【 " + tableNum + " 】( 子订单 " + (position + 1) + " )");
                // 重新计算该子订单金额
                initIndexPageMoney();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 初始化餐桌数据（初始化调用，选择菜品后也调用）
     * @param flag 初始化时，该值传false，加菜操作以后，该值传true
     */
    private void initThisTableOrderedView(boolean flag) {
        if(!flag){
            tableReadyOrderList.clear();
            tableHasNewOrderList.clear();
            tableReadyOrderList.add(tableReadyOrderMap);
            tableHasNewOrderList.add(tableHasNewOrderMap);
        }else{
            if(order_every_pager.getAdapter().getCount() > 1){
                String tableNum = this.getIntent().getExtras().getString("tableNum");
                TextView orderPage_tableNum = (TextView) findViewById(R.id.orderPage_tableNum);
                orderPage_tableNum.setText("餐桌号【 " + tableNum + " 】( 子订单 " + (order_every_pager.getCurrentItem() + 1) + " )");
            }
        }
        orderEveryAdapter.notifyDataSetChanged();
        // 初始化第一个选项卡页面的金额数量
        initIndexPageMoney();
    }

    private void initIndexPageMoney() {
        tableHasNewOrderMap = tableHasNewOrderList.get(order_every_pager.getCurrentItem());
        tableReadyOrderMap = tableReadyOrderList.get(order_every_pager.getCurrentItem());
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

    // 初始化点击按钮
    public void initBtnEvent(){
        // 结账
        btnOrderPageEndMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 只结算传过菜的价格
                tableReadyOrderMap = tableReadyOrderList.get(order_every_pager.getCurrentItem());
                if(tableReadyOrderMap.size() > 0){
                    if(editOrderSendWay.getVisibility() == View.VISIBLE && editOrderSendWay.getTag().toString().equals("none")){
                        ComFun.showToast(EditOrderActivity.this, "请先设置用户送餐联系方式", Toast.LENGTH_SHORT);
                        return;
                    }
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
                    final TextView userContactWay = (TextView) accountView.findViewById(R.id.userContactWay);
                    // 判断是什么类型结账（餐桌结账/打包外卖结账）
                    if(editOrderSendWay.getVisibility() == View.VISIBLE){
                        userContactWay.setVisibility(View.VISIBLE);
                        userContactWay.setText(editOrderSendWay.getText().toString().trim());
                    }
                    if(userContactWay.getVisibility() == View.VISIBLE){
                        userContactWay.requestFocus();
                        userContactWay.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                realMoney.clearFocus();
                                v.requestFocus();
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(realMoney.getWindowToken(), 0);
                                }
                                String userContactWayTxt = ((TextView) v).getText().toString();
                                if(ComFun.strNull(userContactWayTxt)){
                                    ComFun.showToast(EditOrderActivity.this, "用户送餐联系方式：\n\n" + userContactWayTxt, Toast.LENGTH_LONG);
                                }
                            }
                        });
                    }
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
                            realMoney.clearFocus();
                            userContactWay.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(realMoney.getWindowToken(), 0);
                            }
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
                                        String tableOrderIds = toThisIntent.getExtras().getString("tableOrderId");
                                        String[] tableOrderIdArr = tableOrderIds.split("#");
                                        List<String> tableOrderIdList = new ArrayList<>();
                                        for(int i = 0; i < tableHasNewOrderList.size(); i++){
                                            if(i < tableOrderIdArr.length){
                                                tableOrderIdList.add(tableOrderIdArr[i]);
                                            }else{
                                                tableOrderIdList.add("");
                                            }
                                        }
                                        String tableOrderId = tableOrderIdList.get(order_every_pager.getCurrentItem());
                                        String tableNum = toThisIntent.getExtras().getString("tableNum");
                                        // 获取生成小票内容（用#N#符代替换行）
                                        String smailBillContext = createSmailBillContext();
                                        if(userContactWay.getVisibility() == View.VISIBLE){
                                            String outUserName = userContactWay.getText().toString().trim().split("、")[0];
                                            String outUserPhone = userContactWay.getText().toString().trim().split("、")[1];
                                            String outUserAddress = userContactWay.getText().toString().trim().split("、")[2];
                                            if(outUserName.equals("匿名")){
                                                outUserName = "-";
                                            }

                                            RequestParams requestParams = new RequestParams();
                                            requestParams.put("tableOrderId", tableOrderId);
                                            if(ComFun.strNull(printAccountBillId) && ComFun.strNull(tableNum) && ComFun.strNull(smailBillContext)){
                                                requestParams.put("printAccountBillId", printAccountBillId);
                                                requestParams.put("tableNo", tableNum);
                                                requestParams.put("printContext", smailBillContext);
                                            }
                                            requestParams.put("outUserName", outUserName);
                                            requestParams.put("outUserPhone", outUserPhone);
                                            requestParams.put("outUserAddress", outUserAddress);
                                            AllRequestUtil.SettleAccount(EditOrderActivity.this, requestParams, tableNum, tableOrderId);
                                        }else{
                                            RequestParams requestParams = new RequestParams();
                                            requestParams.put("tableOrderId", tableOrderId);
                                            if(ComFun.strNull(printAccountBillId) && ComFun.strNull(tableNum) && ComFun.strNull(smailBillContext)){
                                                requestParams.put("printAccountBillId", printAccountBillId);
                                                requestParams.put("tableNo", tableNum);
                                                requestParams.put("printContext", smailBillContext);
                                            }
                                            requestParams.put("outUserName", "-");
                                            requestParams.put("outUserPhone", "-");
                                            requestParams.put("outUserAddress", "-");
                                            AllRequestUtil.SettleAccount(EditOrderActivity.this, requestParams, tableNum, tableOrderId);
                                        }
                                    }
                                }else{
                                    ComFun.showToast(EditOrderActivity.this, "请选择打印收据小票的设备", Toast.LENGTH_SHORT);
                                }
                            }else{
                                if(accountDialog.isShowing()){
                                    accountDialog.dismiss();
                                }
                                ComFun.showLoading(EditOrderActivity.this, "结账中，请稍后...");
                                String tableOrderIds = toThisIntent.getExtras().getString("tableOrderId");
                                String[] tableOrderIdArr = tableOrderIds.split("#");
                                List<String> tableOrderIdList = new ArrayList<>();
                                for(int i = 0; i < tableHasNewOrderList.size(); i++){
                                    if(i < tableOrderIdArr.length){
                                        tableOrderIdList.add(tableOrderIdArr[i]);
                                    }else{
                                        tableOrderIdList.add("");
                                    }
                                }
                                String tableOrderId = tableOrderIdList.get(order_every_pager.getCurrentItem());
                                String tableNum = toThisIntent.getExtras().getString("tableNum");
                                if(userContactWay.getVisibility() == View.VISIBLE){
                                    String outUserName = userContactWay.getText().toString().trim().split("、")[0];
                                    String outUserPhone = userContactWay.getText().toString().trim().split("、")[1];
                                    String outUserAddress = userContactWay.getText().toString().trim().split("、")[2];
                                    if(outUserName.equals("匿名")){
                                        outUserName = "-";
                                    }

                                    RequestParams requestParams = new RequestParams();
                                    requestParams.put("tableOrderId", tableOrderId);
                                    requestParams.put("outUserName", outUserName);
                                    requestParams.put("outUserPhone", outUserPhone);
                                    requestParams.put("outUserAddress", outUserAddress);
                                    AllRequestUtil.SettleAccount(EditOrderActivity.this, requestParams, tableNum, tableOrderId);
                                }else{
                                    RequestParams requestParams = new RequestParams();
                                    requestParams.put("tableOrderId", tableOrderId);
                                    requestParams.put("outUserName", "-");
                                    requestParams.put("outUserPhone", "-");
                                    requestParams.put("outUserAddress", "-");
                                    AllRequestUtil.SettleAccount(EditOrderActivity.this, requestParams, tableNum, tableOrderId);
                                }
                            }
                        }
                    });
                }else{
                    ComFun.showToast(EditOrderActivity.this, "无结账数据", Toast.LENGTH_SHORT);
                }
            }
        });
        // 传菜
        btnOrderPageUpMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 先走任务上传菜到服务器，成功后，执行以下
                tableHasNewOrderMap = tableHasNewOrderList.get(order_every_pager.getCurrentItem());
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
                    String tableOrderIds = toThisIntent.getExtras().getString("tableOrderId");
                    String tableOrderId = "";
                    if(ComFun.strNull(tableOrderIds)){
                        String[] tableOrderIdArr = tableOrderIds.split("#");
                        List<String> tableOrderIdList = new ArrayList<>();
                        for(int i = 0; i < tableHasNewOrderList.size(); i++){
                            if(i < tableOrderIdArr.length){
                                tableOrderIdList.add(tableOrderIdArr[i]);
                            }else{
                                tableOrderIdList.add("");
                            }
                        }
                        tableOrderId = tableOrderIdList.get(order_every_pager.getCurrentItem());
                    }

                    RequestParams requestParams = new RequestParams();
                    String orderType = "1";
                    requestParams.put("tableOrderId", tableOrderId);
                    requestParams.put("tableNum", tableNum);
                    if(ComFun.strNull(tableOrderId)){
                        requestParams.put("showType", showType+"");
                    }else{
                        requestParams.put("showType", "1");// 订单Id为空，认为是餐桌新订单（子订单情况）
                    }
                    StringBuilder orderMenuInfoSb = new StringBuilder("");
                    for(Map.Entry<String, Object[]> m : tableHasNewOrderMap.entrySet()){
                        String menuId = m.getKey();
                        String menuPrice = m.getValue()[0].toString().split("#&#")[4].toString();
                        String menuBuyCount = m.getValue()[1].toString();
                        String menuAboutRemark = m.getValue()[2].toString();
                        orderMenuInfoSb.append(menuId + "|" + menuPrice + "|" + menuBuyCount + "|" + menuAboutRemark);
                        orderMenuInfoSb.append(",");
                    }
                    if(ComFun.strNull(orderMenuInfoSb.toString())){
                        if(showType == -10){
                            if(tableNum.equals("wmTable")){
                                orderType = "2";
                            }else{
                                orderType = "3";
                            }
                            requestParams.put("takeOutMenuInfo", orderMenuInfoSb.toString().substring(0, orderMenuInfoSb.toString().length() - 1));
                        }else{
                            requestParams.put("orderMenuInfo", orderMenuInfoSb.toString().substring(0, orderMenuInfoSb.toString().length() - 1));
                        }
                    }
                    requestParams.put("orderType", orderType);
                    AllRequestUtil.SendMenu(EditOrderActivity.this, requestParams);
                }else{
                    ComFun.showToast(EditOrderActivity.this, "该餐桌还没有点新菜哦", Toast.LENGTH_SHORT);
                }
            }
        });
        // 加菜
        btnOrderPageAddNewMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tableHasNewOrderMap = tableHasNewOrderList.get(order_every_pager.getCurrentItem());
                tableReadyOrderMap = tableReadyOrderList.get(order_every_pager.getCurrentItem());
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
        tableReadyOrderMap = tableReadyOrderList.get(order_every_pager.getCurrentItem());
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
                    tableHasNewOrderMap = tableHasNewOrderList.get(order_every_pager.getCurrentItem());
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
                    tableHasNewOrderList.remove(tableHasNewOrderList.size() - 1);
                    tableHasNewOrderList.add(tableHasNewOrderMap);
                    // 更新订单菜品布局信息
                    initThisTableOrderedView(true);
                }else if(resultCode == SelectMenuActivity.RESULT_CANCLE){
                    // 点击了取消或按返回键，如果该餐桌未选择任何菜，直接关闭该页面
                    tableHasNewOrderMap = tableHasNewOrderList.get(order_every_pager.getCurrentItem());
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
                    tableHasNewOrderMap = tableHasNewOrderList.get(order_every_pager.getCurrentItem());
                    tableReadyOrderMap = tableReadyOrderList.get(order_every_pager.getCurrentItem());
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
                        initThisTableOrderedView(true);
                    }
                    break;
                case MSG_GET_TABLE_ORDER_INFO:
                    add_new_table_order.setVisibility(View.VISIBLE);
                    // 初始化 tableReadyOrderMap 该餐桌正在制作中的菜品信息
                    if(b.containsKey("orderInfoDetails")){
                        String orderInfoDetails = b.getString("orderInfoDetails");
                        String[] orderInfoDetailsArr = orderInfoDetails.split("\\|\\|#\\|#\\|#\\|\\|");
                        Map<String, Object[]> tableOrderMap1;
                        Map<String, Object[]> tableOrderMap2;
                        tableReadyOrderList.clear();
                        tableHasNewOrderList.clear();
                        for(String orderInfoDetail : orderInfoDetailsArr){
                            tableOrderMap1 = new HashMap<>();
                            tableOrderMap2 = new HashMap<>();
                            for(String orderInfo : orderInfoDetail.split(",")){
                                tableOrderMap1.put(orderInfo.split("\\|")[0].split("#&#")[0], new Object[]{
                                        orderInfo.split("\\|")[0], orderInfo.split("\\|")[1], orderInfo.split("\\|")[2]
                                });// 键：菜品id，值：[菜品信息(菜id #&# 菜组id #&# 菜名称 #&# 菜描述 #&# 菜单价 #&# 菜被点次数), 点餐数量, 备注信息]
                            }
                            tableReadyOrderList.add(tableOrderMap1);
                            tableHasNewOrderList.add(tableOrderMap2);
                        }
                        initThisTableOrderedView(true);
                    }
                    break;
                case MSG_ACCOUNT:
                    //String tableNum = toThisIntent.getExtras().getString("tableNum");
                    // 发送主页面更新广播
                    String tableOrderId = b.getString("tableOrderId");
                    String tableNo = b.getString("tableNo");
                    if(!ComFun.strNull(tableNo) || (ComFun.strNull(tableNo) && tableNo.equals("-1"))){
                        // 发送订单列表页面更新广播
                        Intent intent = new Intent();
                        intent.putExtra("tableOrderId", tableOrderId);
                        intent.setAction(OutOrderFragment.MSG_REF_OUTORDER_DATA_AFTER_ACC);
                        EditOrderActivity.this.sendBroadcast(intent);
                    }else{
                        // 将订单id “ tableOrderId ” 在餐桌信息缓存数据中清除
                        String thisGroupTableInfo = SharedPreferencesTool.getFromShared(EditOrderActivity.this, "BouilliTableInfo", "tableFullInfo");
                        StringBuilder thisGroupTableInfoUpdate = new StringBuilder("");
                        String[] thisGroupTableInfoArr = thisGroupTableInfo.split(",");
                        for(String thisGroupTableIn : thisGroupTableInfoArr){
                            if(thisGroupTableIn.contains(tableOrderId)){
                                String[] needUpdateTableInfoArr = thisGroupTableIn.split("\\|");
                                if(needUpdateTableInfoArr.length == 3){
                                    String needUpdateTableInfo = needUpdateTableInfoArr[2];
                                    String[] needUpdateTableOrderIdArr = needUpdateTableInfo.split("#");
                                    StringBuilder needUpdateTableOrderIdSb = new StringBuilder("");
                                    for(String needUpdateTableOrderId : needUpdateTableOrderIdArr){
                                        if(!needUpdateTableOrderId.equals(tableOrderId)){
                                            needUpdateTableOrderIdSb.append(needUpdateTableOrderId);
                                            needUpdateTableOrderIdSb.append("#");
                                        }
                                    }
                                    thisGroupTableInfoUpdate.append(needUpdateTableInfoArr[0]);
                                    thisGroupTableInfoUpdate.append("|");
                                    if(ComFun.strNull(needUpdateTableOrderIdSb.toString())){
                                        thisGroupTableInfoUpdate.append(needUpdateTableInfoArr[1]);
                                        thisGroupTableInfoUpdate.append("|");
                                        thisGroupTableInfoUpdate.append(needUpdateTableOrderIdSb.toString().substring(0, needUpdateTableOrderIdSb.toString().length() - 1));
                                    }else{
                                        thisGroupTableInfoUpdate.append("1");
                                    }
                                    thisGroupTableInfoUpdate.append(",");
                                }
                            }else{
                                thisGroupTableInfoUpdate.append(thisGroupTableIn);
                                thisGroupTableInfoUpdate.append(",");
                            }
                        }
                        if(ComFun.strNull(thisGroupTableInfoUpdate.toString())){
                            SharedPreferencesTool.addOrUpdate(EditOrderActivity.this, "BouilliTableInfo", "tableFullInfo", thisGroupTableInfoUpdate.toString().substring(0, thisGroupTableInfoUpdate.toString().length() - 1));
                        }else{
                            SharedPreferencesTool.addOrUpdate(EditOrderActivity.this, "BouilliTableInfo", "tableFullInfo", "");
                        }
                        // 发送主页面更新广播
                        Intent intent = new Intent();
                        intent.putExtra("newData", true);
                        intent.setAction(MainFragment.MSG_REFDATA);
                        EditOrderActivity.this.sendBroadcast(intent);
                    }
                    EditOrderActivity.this.finish();
                    break;
                case MSG_GET_PRINT_INFO_ACCOUNT_NEED:
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
            tableHasNewOrderMap = tableHasNewOrderList.get(order_every_pager.getCurrentItem());
            tableReadyOrderMap = tableReadyOrderList.get(order_every_pager.getCurrentItem());
            if(tableReadyOrderMap.size() == 0 && tableHasNewOrderMap.size() > 0 && tableHasNewOrderList.size() == 1){
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
