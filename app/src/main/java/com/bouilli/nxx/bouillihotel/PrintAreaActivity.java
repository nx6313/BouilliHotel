package com.bouilli.nxx.bouillihotel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.AddPrintTask;
import com.bouilli.nxx.bouillihotel.asyncTask.DeletePrintTask;
import com.bouilli.nxx.bouillihotel.asyncTask.GetPrintInfoTask;
import com.bouilli.nxx.bouillihotel.asyncTask.SaveUserPrintSetTask;
import com.bouilli.nxx.bouillihotel.asyncTask.okHttpTask.AllRequestUtil;
import com.bouilli.nxx.bouillihotel.customview.FlowLayout;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class PrintAreaActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_ADD_PRINT = 1;
    public static final int MSG_DELETE_PRINT = 2;
    public static final int MSG_INIT_PRINT = 3;
    public static final int MSG_SAVE_USER_PRINT_SET = 4;
    private LinearLayout printAreaSetLayout;
    private LinearLayout printAreaSelectLayout;

    private Button btnAddNewPrintArea;
    private LinearLayout printAreaSetMainLayout;
    private RadioGroup rgSelectPrintArea;

    private String selectedBlueBaseName;// 当前选中蓝牙连接设备名称

    private String selectPrintAreaId;// 当前选择的打票机区域关联打票机设备Id

    private Button btnSureSelectPrintArea;// 确定选择打票机区域按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_area);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mHandler = new PrintAreaActivity.mHandler();

        printAreaSetLayout = (LinearLayout) findViewById(R.id.printAreaSetLayout);
        printAreaSelectLayout = (LinearLayout) findViewById(R.id.printAreaSelectLayout);
        btnSureSelectPrintArea = (Button) findViewById(R.id.btnSureSelectPrintArea);
        rgSelectPrintArea = (RadioGroup) findViewById(R.id.rgSelectPrintArea);
        rgSelectPrintArea.removeAllViews();
        rgSelectPrintArea.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final RadioGroup group, int checkedId) {
                boolean printUseVol = SharedPreferencesTool.getBooleanFromShared(PrintAreaActivity.this, "BouilliSetInfo", "printUseVol");
                final RadioButton selectRb = (RadioButton) group.findViewById(checkedId);
                if(selectRb != null && selectRb.isChecked()){
                    String thisSelectRbPrintId = selectRb.getTag().toString().split("&")[0];
                    if(printUseVol){
                        if(!ComFun.strNull(selectPrintAreaId) || (ComFun.strNull(selectPrintAreaId) && !selectPrintAreaId.equals(thisSelectRbPrintId))){
                            selectPrintAreaId = thisSelectRbPrintId;
                            final String printAddress = selectRb.getTag().toString().split("&")[1];
                            new android.support.v7.app.AlertDialog.Builder(PrintAreaActivity.this).setTitle("提示").setMessage("需要执行打票机连接检测吗？请在确定检测之前确保您的手机已经和蓝牙打票机正确配对\n\n取消则可先保存数据，之后会为您检测打票机连接")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // 执行任务检测所选打票机连接是否正确
                                            Handler checkPrintHandler = new Handler();
                                            checkPrintHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        if(MyApplication.mBluetoothAdapter == null){
                                                            MyApplication.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                                        }
                                                        MyApplication.mBluetoothDevice = MyApplication.mBluetoothAdapter.getRemoteDevice(printAddress);
                                                        MyApplication.mBluetoothSocket = MyApplication.mBluetoothDevice.createRfcommSocketToServiceRecord(MyApplication.SPP_UUID);
                                                        if(!MyApplication.mBluetoothSocket.isConnected()){
                                                            MyApplication.mBluetoothSocket.connect();
                                                        }

                                                        ComFun.showToast(PrintAreaActivity.this, "创建蓝牙连接成功，请点击按钮【确定选择】保存设置", Toast.LENGTH_LONG);
                                                    } catch (Exception e) {
                                                        ComFun.showToast(PrintAreaActivity.this, "对不起，创建蓝牙连接失败，请检查您的设备是否支持蓝牙功能或者打票机是否正确配对", Toast.LENGTH_LONG);
                                                        group.clearCheck();
                                                    }
                                                }
                                            }, 10);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ComFun.showToast(PrintAreaActivity.this, "请点击按钮【确定选择】保存设置", Toast.LENGTH_LONG);
                                        }
                                    }).show();
                        }
                    }else{
                        group.clearCheck();
                        ComFun.showToast(PrintAreaActivity.this, "请先设置打票机启用", Toast.LENGTH_SHORT);
                    }
                }
            }
        });
        // 根据权限显示隐藏指定区域
        String userPermission = SharedPreferencesTool.getFromShared(PrintAreaActivity.this, "BouilliProInfo", "userPermission");
        if(ComFun.strNull(userPermission)){
            if(Integer.parseInt(userPermission) == 0 || Integer.parseInt(userPermission) == 1){
                printAreaSetLayout.setVisibility(View.VISIBLE);
                printAreaSelectLayout.setVisibility(View.GONE);
            }else{
                printAreaSetLayout.setVisibility(View.GONE);
                printAreaSelectLayout.setVisibility(View.VISIBLE);
            }
        }else{
            // 测试号
            printAreaSetLayout.setVisibility(View.GONE);
            printAreaSelectLayout.setVisibility(View.VISIBLE);
        }

        btnAddNewPrintArea = (Button) findViewById(R.id.btnAddNewPrintArea);
        printAreaSetMainLayout = (LinearLayout) findViewById(R.id.printAreaSetMainLayout);
        printAreaSetMainLayout.removeAllViews();
        // 初始化打票机区域布局
        AllRequestUtil.GetPrintInfo(PrintAreaActivity.this, null, false);

        btnAddNewPrintArea.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                View printAreaSetItemView = View.inflate(PrintAreaActivity.this, R.layout.print_area_set_item, null);
                ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(2).requestFocus();
                ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(2).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        selectPrintDevice(v);
                    }
                });
                FlowLayout printAboutMenuTypeCkLayout = (FlowLayout) ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(1)).getChildAt(1);
                printAboutMenuTypeCkLayout.removeAllViews();
                // 获取缓存数据中，菜品类型
                String menuGroupNames = SharedPreferencesTool.getFromShared(PrintAreaActivity.this, "BouilliMenuInfo", "menuGroupNames");
                if(ComFun.strNull(menuGroupNames)){
                    for(String menuGroup : menuGroupNames.split(",")){
                        CheckBox printAboutMenuGroupCk = new CheckBox(PrintAreaActivity.this);
                        ViewGroup.MarginLayoutParams printAboutMenuGroupCkLp = new ViewGroup.MarginLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        printAboutMenuGroupCkLp.setMargins(DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4));
                        printAboutMenuGroupCk.setLayoutParams(printAboutMenuGroupCkLp);
                        printAboutMenuGroupCk.setText(menuGroup.split("#&#")[1]);
                        printAboutMenuGroupCk.setTag(menuGroup.split("#&#")[0]);
                        printAboutMenuTypeCkLayout.addView(printAboutMenuGroupCk);
                    }
                }else{
                    TextView printAboutMenuGroupNullTv = new TextView(PrintAreaActivity.this);
                    ViewGroup.MarginLayoutParams printAboutMenuGroupNullTvLp = new ViewGroup.MarginLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    printAboutMenuGroupNullTvLp.setMargins(DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4));
                    printAboutMenuGroupNullTv.setLayoutParams(printAboutMenuGroupNullTvLp);
                    printAboutMenuGroupNullTv.setText("未找到相关菜品类型，请先添加");
                    printAboutMenuTypeCkLayout.addView(printAboutMenuGroupNullTv);
                }
                printAreaSetMainLayout.addView(printAreaSetItemView);
            }
        });
        // 确定选择打票机区域
        btnSureSelectPrintArea.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ComFun.strNull(selectPrintAreaId)){
                    // 执行打票机设置保存任务
                    ComFun.showLoading(PrintAreaActivity.this, "正在保存打票机设置，请稍后");
                    new SaveUserPrintSetTask(PrintAreaActivity.this, selectPrintAreaId).executeOnExecutor(Executors.newCachedThreadPool());
                }else{
                    ComFun.showToast(PrintAreaActivity.this, "请先选择打票区域", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if(printAreaSetMainLayout.getChildCount() > 0){
                boolean canBackFlag = true;
                for(int i=0; i<printAreaSetMainLayout.getChildCount(); i++){
                    if(((RelativeLayout) ((LinearLayout) ((LinearLayout) printAreaSetMainLayout.getChildAt(i)).getChildAt(0)).getChildAt(0)).getChildAt(1).getVisibility() == View.VISIBLE
                            && ComFun.strNull(((EditText) ((RelativeLayout) ((LinearLayout) ((LinearLayout) printAreaSetMainLayout.getChildAt(i)).getChildAt(0)).getChildAt(0)).getChildAt(1)).getText().toString().trim())){
                        canBackFlag = false;
                        break;
                    }
                }
                if(canBackFlag){
                    PrintAreaActivity.this.finish();
                }else{
                    new android.support.v7.app.AlertDialog.Builder(PrintAreaActivity.this).setTitle("提示").setMessage("存在打票机设置尚未保存，确定不保存吗？")
                            .setPositiveButton("确定不保存", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PrintAreaActivity.this.finish();
                                }
                            })
                            .setNegativeButton("按错了，留下继续操作", null).show();
                }
            }else{
                PrintAreaActivity.this.finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 点击编辑打票机区域按钮
    public void editPrintArea(View v){
        v.setVisibility(View.GONE);
        ((LinearLayout) v.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
        // 显示编辑框
        TextView tvPrintName = (TextView) ((RelativeLayout) ((LinearLayout) v.getParent().getParent()).getChildAt(0)).getChildAt(0);
        EditText etPrintName = (EditText) ((RelativeLayout) ((LinearLayout) v.getParent().getParent()).getChildAt(0)).getChildAt(1);
        TextView tvPrintAddress = (TextView) ((LinearLayout) v.getParent().getParent()).getChildAt(2);
        tvPrintName.setVisibility(View.GONE);
        etPrintName.setVisibility(View.VISIBLE);
        etPrintName.setHint(tvPrintName.getText().toString().trim());
        etPrintName.setText(tvPrintName.getText().toString().trim());
        tvPrintAddress.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                selectPrintDevice(v);
            }
        });
        // 菜品类型相关
        FlowLayout printAboutMenuGroupLayout = (FlowLayout) ((LinearLayout) ((LinearLayout) v.getParent().getParent().getParent()).getChildAt(1)).getChildAt(1);
        for(int p=0; p<printAboutMenuGroupLayout.getChildCount(); p++){
            CheckBox menuGroupPrintCb = (CheckBox) printAboutMenuGroupLayout.getChildAt(p);
            menuGroupPrintCb.setEnabled(true);
        }
    }

    // 点击保存打票机区域按钮
    public void savePrintArea(View v){
        // 保存打票机设置
        TextView tvPrintName = (TextView) ((RelativeLayout) ((LinearLayout) v.getParent().getParent()).getChildAt(0)).getChildAt(0);
        EditText etPrintName = (EditText) ((RelativeLayout) ((LinearLayout) v.getParent().getParent()).getChildAt(0)).getChildAt(1);
        TextView tvPrintAddress = (TextView) ((LinearLayout) v.getParent().getParent()).getChildAt(2);
        // 获取打印相关菜品类型
        FlowLayout printAboutMenuGroupLayout = (FlowLayout) ((LinearLayout) ((LinearLayout) v.getParent().getParent().getParent()).getChildAt(1)).getChildAt(1);
        StringBuilder selectPrintAboutMenuGroupIds = new StringBuilder("");
        for(int p=0; p<printAboutMenuGroupLayout.getChildCount(); p++){
            CheckBox menuGroupPrintCb = (CheckBox) printAboutMenuGroupLayout.getChildAt(p);
            if(menuGroupPrintCb.isChecked()){
                selectPrintAboutMenuGroupIds.append(menuGroupPrintCb.getTag().toString().trim());
                selectPrintAboutMenuGroupIds.append("|");
            }
        }
        // 判断所有区域配置中的菜品类型是否重复

        if(ComFun.strNull(etPrintName.getText().toString().trim()) && ComFun.strNull(tvPrintAddress.getText().toString().trim()) && ComFun.strNull(selectPrintAboutMenuGroupIds.toString().trim()) && !checkPrintAreaMenuGroupSetRepeat()){
            // 执行打票机配置保存任务
            String printInfoId = "";
            if(ComFun.strNull(((LinearLayout) v.getParent().getParent()).getChildAt(2).getTag())) {
                printInfoId = ((LinearLayout) v.getParent().getParent()).getChildAt(2).getTag().toString().trim();
            }
            ComFun.showLoading(PrintAreaActivity.this, "正在保存打票机设置，请稍后");
            new AddPrintTask(PrintAreaActivity.this, etPrintName.getText().toString().trim(), tvPrintAddress.getText().toString().trim(), printInfoId, selectPrintAboutMenuGroupIds.substring(0, selectPrintAboutMenuGroupIds.length() - 1),
                v, tvPrintName, etPrintName, tvPrintAddress, printAboutMenuGroupLayout).executeOnExecutor(Executors.newCachedThreadPool());
        }else{
            if(!ComFun.strNull(etPrintName.getText().toString().trim())){
                ComFun.showToast(PrintAreaActivity.this, "打票机名称不能为空", Toast.LENGTH_SHORT);
            }else if(!ComFun.strNull(tvPrintAddress.getText().toString().trim())){
                ComFun.showToast(PrintAreaActivity.this, "打票机蓝牙地址不能为空", Toast.LENGTH_SHORT);
            }else if(checkPrintAreaMenuGroupSetRepeat()){
                ComFun.showToast(PrintAreaActivity.this, "打票机区域所关联的菜品类型不能重复", Toast.LENGTH_SHORT);
            }
        }
    }

    /**
     * 检测所有区域配置中的菜品类型是否重复
     * @return
     */
    public boolean checkPrintAreaMenuGroupSetRepeat(){
        List<String> hasMenuGroupIdList = new ArrayList<>();
        for(int p=0; p<printAreaSetMainLayout.getChildCount(); p++){
            FlowLayout printAboutMenuTypeCkLayout = (FlowLayout) ((LinearLayout) ((LinearLayout) printAreaSetMainLayout.getChildAt(p)).getChildAt(1)).getChildAt(1);
            for(int cb=0; cb<printAboutMenuTypeCkLayout.getChildCount(); cb++){
                CheckBox printAboutMenuGroupCk = (CheckBox) printAboutMenuTypeCkLayout.getChildAt(cb);
                if(printAboutMenuGroupCk.isChecked()){
                    if(hasMenuGroupIdList.contains(printAboutMenuGroupCk.getTag().toString())){
                        return true;
                    }else{
                        hasMenuGroupIdList.add(printAboutMenuGroupCk.getTag().toString());
                    }
                }
            }
        }
        return false;
    }

    // 点击删除打票机区域按钮
    public void deletePrintArea(final View v){
        if(ComFun.strNull(((LinearLayout) v.getParent().getParent()).getChildAt(2).getTag())){
            new android.support.v7.app.AlertDialog.Builder(PrintAreaActivity.this).setTitle("提示").setMessage("确定删除该蓝牙打票机区域吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String printInfoId = ((LinearLayout) v.getParent().getParent()).getChildAt(2).getTag().toString().trim();
                            ComFun.showLoading(PrintAreaActivity.this, "正在删除打票机区域，请稍后");
                            if(ComFun.strNull(printInfoId)){
                                new DeletePrintTask(PrintAreaActivity.this, printInfoId, printAreaSetMainLayout, (View) v.getParent().getParent().getParent()).executeOnExecutor(Executors.newCachedThreadPool());
                            }
                        }
                    })
                    .setNegativeButton("取消", null).show();
        }else{
            printAreaSetMainLayout.removeView((View) v.getParent().getParent().getParent());
        }
    }

    // 点击选择蓝牙打票机设备按钮
    public void selectPrintDevice(final View v){
        if(ComFun.strNull(((TextView) v).getText().toString().trim())){
            // 提示将清除该蓝牙打票机地址设置
            new android.support.v7.app.AlertDialog.Builder(PrintAreaActivity.this).setTitle("提示").setMessage("确定清除该蓝牙打票机地址吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((TextView)v).setText("");
                        }
                    })
                    .setNegativeButton("取消", null).show();
        }else{
            if(MyApplication.mBluetoothAdapter != null && MyApplication.mBluetoothAdapter.isEnabled()){
                MyApplication.pairedDevices = MyApplication.mBluetoothAdapter.getBondedDevices();
                if(MyApplication.pairedDevices.size() > 0){
                    MyApplication.mpairedDeviceList.clear();
                    for (BluetoothDevice device : MyApplication.pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        String getName = device.getName() + "#" + device.getAddress();
                        MyApplication.mpairedDeviceList.add(getName);
                    }
                    // 配对的设备列表中肯定有，默认为已选择第一项
                    selectedBlueBaseName = MyApplication.mpairedDeviceList.get(0);
                    // 弹出选择蓝牙设备单选弹框
                    AlertDialog.Builder builder=new android.app.AlertDialog.Builder(PrintAreaActivity.this);
                    //设置对话框的图标
                    builder.setIcon(R.drawable.mode);
                    //设置对话框的标题
                    builder.setTitle("选择小票打印机");
                    builder.setSingleChoiceItems(MyApplication.mpairedDeviceList.toArray(new String[MyApplication.mpairedDeviceList.size()]), 0, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedBlueBaseName = MyApplication.mpairedDeviceList.get(which);
                        }
                    });

                    //添加一个确定按钮
                    builder.setPositiveButton(" 确 定 ", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            String temString = selectedBlueBaseName.substring(selectedBlueBaseName.length()-17);
                            try {
                                ((TextView)v).setText(temString);
                            } catch (Exception e) {
                                // TODO: handle exception
                                ((TextView)v).setText("");
                                ComFun.showToast(PrintAreaActivity.this, "您选择的设备不支持打印服务，请重新选择", Toast.LENGTH_SHORT);
                            }
                        }
                    });
                    //添加一个取消按钮
                    builder.setNegativeButton(" 取 消 ", null);
                    //创建一个单选框对话框
                    Dialog dialog = builder.create();
                    dialog.show();
                }else{
                    ComFun.showToast(PrintAreaActivity.this, "没有与任何设备配对连接，请先与设备配对", Toast.LENGTH_SHORT);
                }
            }else{
                ComFun.showToast(PrintAreaActivity.this, "对不起，您的设备未开启蓝牙功能或不支持蓝牙功能", Toast.LENGTH_SHORT);
            }
        }
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
            if(printAreaSetMainLayout.getChildCount() > 0){
                boolean canBackFlag = true;
                for(int i=0; i<printAreaSetMainLayout.getChildCount(); i++){
                    if(((RelativeLayout) ((LinearLayout) ((LinearLayout) printAreaSetMainLayout.getChildAt(i)).getChildAt(0)).getChildAt(0)).getChildAt(1).getVisibility() == View.VISIBLE
                            && ComFun.strNull(((EditText) ((RelativeLayout) ((LinearLayout) ((LinearLayout) printAreaSetMainLayout.getChildAt(i)).getChildAt(0)).getChildAt(0)).getChildAt(1)).getText().toString().trim())){
                        canBackFlag = false;
                        break;
                    }
                }
                if(canBackFlag){
                    PrintAreaActivity.this.finish();
                }else{
                    new android.support.v7.app.AlertDialog.Builder(PrintAreaActivity.this).setTitle("提示").setMessage("存在打票机设置尚未保存，确定不保存吗？")
                            .setPositiveButton("确定不保存", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PrintAreaActivity.this.finish();
                                }
                            })
                            .setNegativeButton("按错了，留下继续操作", null).show();
                }
            }else{
                PrintAreaActivity.this.finish();
            }
        }
        return true;
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
                case MSG_INIT_PRINT:
                    if(b.containsKey("AllPrintsInfo")){
                        String AllPrintsInfo = b.getString("AllPrintsInfo");
                        if(ComFun.strNull(AllPrintsInfo)){
                            int defaultSelectRbId = -1;
                            int index = 0;
                            for(String printInfo : AllPrintsInfo.split(",")){
                                index++;
                                if(printAreaSetLayout.getVisibility() == View.VISIBLE){
                                    View printAreaSetItemView = View.inflate(PrintAreaActivity.this, R.layout.print_area_set_item, null);
                                    ((RelativeLayout) ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(0)).getChildAt(0).setVisibility(View.VISIBLE);
                                    ((TextView) ((RelativeLayout) ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(0)).getChildAt(0)).setText(printInfo.split("#&#")[1]);
                                    ((RelativeLayout) ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(0)).getChildAt(1).setVisibility(View.GONE);

                                    ((TextView) ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(2)).setText(printInfo.split("#&#")[2]);
                                    ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(2).setTag(printInfo.split("#&#")[0]);
                                    ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(2).requestFocus();
                                    ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(2).setOnClickListener(null);
                                    ((LinearLayout) ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(4)).getChildAt(0).setVisibility(View.VISIBLE);
                                    ((LinearLayout) ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(0)).getChildAt(4)).getChildAt(1).setVisibility(View.GONE);

                                    FlowLayout printAboutMenuTypeCkLayout = (FlowLayout) ((LinearLayout) ((LinearLayout) printAreaSetItemView).getChildAt(1)).getChildAt(1);
                                    printAboutMenuTypeCkLayout.removeAllViews();
                                    // 获取缓存数据中，菜品类型
                                    String menuGroupNames = SharedPreferencesTool.getFromShared(PrintAreaActivity.this, "BouilliMenuInfo", "menuGroupNames");
                                    if(ComFun.strNull(menuGroupNames)){
                                        for(String menuGroup : menuGroupNames.split(",")){
                                            CheckBox printAboutMenuGroupCk = new CheckBox(PrintAreaActivity.this);
                                            ViewGroup.MarginLayoutParams printAboutMenuGroupCkLp = new ViewGroup.MarginLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                            printAboutMenuGroupCkLp.setMargins(DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4));
                                            printAboutMenuGroupCk.setLayoutParams(printAboutMenuGroupCkLp);
                                            printAboutMenuGroupCk.setText(menuGroup.split("#&#")[1]);
                                            printAboutMenuGroupCk.setTag(menuGroup.split("#&#")[0]);
                                            printAboutMenuGroupCk.setEnabled(false);
                                            if(ComFun.strInArr(printInfo.split("#&#")[3].split("\\|"), menuGroup.split("#&#")[0])){
                                                printAboutMenuGroupCk.setChecked(true);
                                            }
                                            printAboutMenuTypeCkLayout.addView(printAboutMenuGroupCk);
                                        }
                                    }else{
                                        TextView printAboutMenuGroupNullTv = new TextView(PrintAreaActivity.this);
                                        ViewGroup.MarginLayoutParams printAboutMenuGroupNullTvLp = new ViewGroup.MarginLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                        printAboutMenuGroupNullTvLp.setMargins(DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4), DisplayUtil.dip2px(PrintAreaActivity.this, 4));
                                        printAboutMenuGroupNullTv.setLayoutParams(printAboutMenuGroupNullTvLp);
                                        printAboutMenuGroupNullTv.setText("未找到相关菜品类型，请先添加");
                                        printAboutMenuTypeCkLayout.addView(printAboutMenuGroupNullTv);
                                    }
                                    printAreaSetMainLayout.addView(printAreaSetItemView);
                                }else{
                                    RadioButton rbSelectPrintAreaItem = new RadioButton(PrintAreaActivity.this);
                                    RadioGroup.LayoutParams rbSelectPrintAreaItemLp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                                    rbSelectPrintAreaItemLp.setMargins(0, 0, 0, DisplayUtil.dip2px(PrintAreaActivity.this, 8));
                                    rbSelectPrintAreaItem.setLayoutParams(rbSelectPrintAreaItemLp);
                                    StringBuilder printAreaAboutMenuGroupNameSb = new StringBuilder("");
                                    if(printInfo.split("#&#")[4].split("\\|").length > 0){
                                        for(String printAreaAboutMenuName : printInfo.split("#&#")[4].split("\\|")){
                                            if(ComFun.strNull(printAreaAboutMenuName) && !printAreaAboutMenuName.equals("-")){
                                                printAreaAboutMenuGroupNameSb.append(printAreaAboutMenuName);
                                                printAreaAboutMenuGroupNameSb.append("、");
                                            }
                                        }
                                    }
                                    if(printInfo.split("#&#")[5].equals("use")){
                                        // 已被占用，不可选择
                                        rbSelectPrintAreaItem.setEnabled(false);
                                    }
                                    String printAreaAboutMenuGroupNameStr = "";
                                    if(ComFun.strNull(printAreaAboutMenuGroupNameSb.toString())){
                                        printAreaAboutMenuGroupNameStr = printAreaAboutMenuGroupNameSb.toString().substring(0, printAreaAboutMenuGroupNameSb.toString().length() - 1);
                                    }
                                    if(ComFun.strNull(printAreaAboutMenuGroupNameStr)){
                                        rbSelectPrintAreaItem.setText("作为打票区域【 "+ printInfo.split("#&#")[1] +" 】\n打印的菜品分类：" + printAreaAboutMenuGroupNameStr);
                                    }else{
                                        rbSelectPrintAreaItem.setText("作为打票区域【 "+ printInfo.split("#&#")[1] +" 】\n打印的菜品分类：无");
                                    }
                                    rbSelectPrintAreaItem.setId(index);
                                    rbSelectPrintAreaItem.setTag(printInfo.split("#&#")[0] + "&" + printInfo.split("#&#")[2]);
                                    rgSelectPrintArea.addView(rbSelectPrintAreaItem);
                                    String printAreaId = SharedPreferencesTool.getFromShared(PrintAreaActivity.this, "BouilliProInfo", "printAreaId");
                                    String printAddress = SharedPreferencesTool.getFromShared(PrintAreaActivity.this, "BouilliProInfo", "printAddress");
                                    if (ComFun.strNull(printAreaId) && ComFun.strNull(printAddress) && printInfo.split("#&#")[0].equals(printAreaId) && printInfo.split("#&#")[2].equals(printAddress)) {
                                        defaultSelectRbId = index;
                                        selectPrintAreaId = printAreaId;
                                    }
                                }
                            }
                            if(printAreaSetLayout.getVisibility() == View.GONE){
                                // 初始化默认选中
                                boolean printUseVol = SharedPreferencesTool.getBooleanFromShared(PrintAreaActivity.this, "BouilliSetInfo", "printUseVol");
                                if(printUseVol){
                                    String printAreaId = SharedPreferencesTool.getFromShared(PrintAreaActivity.this, "BouilliProInfo", "printAreaId");
                                    String printAddress = SharedPreferencesTool.getFromShared(PrintAreaActivity.this, "BouilliProInfo", "printAddress");
                                    if(ComFun.strNull(printAreaId) && ComFun.strNull(printAddress) && defaultSelectRbId != -1){
                                        rgSelectPrintArea.check(defaultSelectRbId);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case MSG_ADD_PRINT:
                    // 隐藏加载动画
                    ComFun.hideLoading(PrintAreaActivity.this);
                    String addPrintResult = b.getString("addPrintResult");
                    if (addPrintResult.equals("true")) {
                        if(b.containsKey("printInfoId")){
                            ComFun.showToast(PrintAreaActivity.this, "修改打票机设置成功", Toast.LENGTH_SHORT);
                        }else{
                            ComFun.showToast(PrintAreaActivity.this, "保存打票机设置成功", Toast.LENGTH_SHORT);
                        }
                    }else if (addPrintResult.equals("false")) {
                        if(b.containsKey("printInfoId")){
                            ComFun.showToast(PrintAreaActivity.this, "修改打票机设置失败，请联系管理员", Toast.LENGTH_SHORT);
                        }else{
                            ComFun.showToast(PrintAreaActivity.this, "保存打票机设置失败，请联系管理员", Toast.LENGTH_SHORT);
                        }
                    }else if (addPrintResult.equals("time_out")) {
                        if(b.containsKey("printInfoId")){
                            ComFun.showToast(PrintAreaActivity.this, "修改打票机设置超时，请稍后重试", Toast.LENGTH_SHORT);
                        }else{
                            ComFun.showToast(PrintAreaActivity.this, "保存打票机设置超时，请稍后重试", Toast.LENGTH_SHORT);
                        }
                    }
                    break;
                case MSG_DELETE_PRINT:
                    // 隐藏加载动画
                    ComFun.hideLoading(PrintAreaActivity.this);
                    String deletePrintResult = b.getString("deletePrintResult");
                    if (deletePrintResult.equals("true")) {
                        ComFun.showToast(PrintAreaActivity.this, "删除打票机成功", Toast.LENGTH_SHORT);
                    }else if (deletePrintResult.equals("false")) {
                        ComFun.showToast(PrintAreaActivity.this, "删除打票机失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (deletePrintResult.equals("time_out")) {
                        ComFun.showToast(PrintAreaActivity.this, "删除打票机超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_SAVE_USER_PRINT_SET:
                    // 隐藏加载动画
                    ComFun.hideLoading(PrintAreaActivity.this);
                    String saveUserPrintSetResult = b.getString("saveUserPrintSetResult");
                    if (saveUserPrintSetResult.equals("true")) {
                        ComFun.showToast(PrintAreaActivity.this, "保存打票机设置成功", Toast.LENGTH_SHORT);
                        if(b.containsKey("selectPrintAreaId")){
                            String selectPrintAreaId = b.getString("selectPrintAreaId");
                            String printAddress = b.getString("printAddress");
                            String printAboutMenuGroupId = b.getString("printAboutMenuGroupId");
                            SharedPreferencesTool.addOrUpdate(PrintAreaActivity.this, "BouilliProInfo", "printAreaId", selectPrintAreaId);
                            SharedPreferencesTool.addOrUpdate(PrintAreaActivity.this, "BouilliProInfo", "printAddress", printAddress);
                            SharedPreferencesTool.addOrUpdate(PrintAreaActivity.this, "BouilliProInfo", "printAboutMenuGroupId", printAboutMenuGroupId);
                        }
                    }else if (saveUserPrintSetResult.equals("false")) {
                        ComFun.showToast(PrintAreaActivity.this, "保存打票机设置失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (saveUserPrintSetResult.equals("time_out")) {
                        ComFun.showToast(PrintAreaActivity.this, "保存打票机设置超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
