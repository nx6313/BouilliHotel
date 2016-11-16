package com.bouilli.nxx.bouillihotel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.AddNewTableTask;
import com.bouilli.nxx.bouillihotel.asyncTask.DeleteTableTask;
import com.bouilli.nxx.bouillihotel.customview.FlowLayout;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class TableEditActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_ADD_TABLE = 1;
    public static final int MSG_DELETE_GROUP = 2;
    public static final int MSG_DELETE_TABLE = 3;
    public static final int MSG_ADD_TABLE_TO_COM = 4;
    private FloatingActionButton add_new_table_info;
    private FloatingActionButton delete_new_table_info;
    private PopupWindow editTablePupopWindow;

    private LinearLayout editTableMainLayout;

    private EditText etTableGroupName;
    private EditText etTableGroupNo;
    private EditText etTableNumStart;
    private EditText etTableNumEnd;
    private EditText etTableNums;
    private Button btn_save_table_group;

    private boolean[] flags = null;//初始复选情况
    private List<String> deleteSelectTableGroupNameList = new ArrayList<>();

    private String tableGroupNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initTableView();

        // 点击添加桌组图标按钮
        initAddNewTableGroup();
        // 点击删除桌组图标按钮
        initDeleteTableGroup();

        mHandler = new mHandler();
    }

    // 初始化桌组布局
    private void initTableView(){
        editTableMainLayout = (LinearLayout) findViewById(R.id.editTableMainLayout);
        editTableMainLayout.removeAllViews();
        tableGroupNames = SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames");
        if(ComFun.strNull(tableGroupNames)){
            for(String tableGroupName : tableGroupNames.split(",")){
                if(ComFun.strNull(tableGroupName)){
                    String thisGroupTableInfo = SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + tableGroupName);
                    if(ComFun.strNull(thisGroupTableInfo)){
                        View tableGroupItemView = TableEditActivity.this.getLayoutInflater().inflate(R.layout.edit_table_item_pager, null);
                        LinearLayout.LayoutParams tableGroupItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        tableGroupItemLp.setMargins(DisplayUtil.dip2px(TableEditActivity.this, 16), DisplayUtil.dip2px(TableEditActivity.this, 8), DisplayUtil.dip2px(TableEditActivity.this, 16), DisplayUtil.dip2px(TableEditActivity.this, 8));
                        tableGroupItemView.setLayoutParams(tableGroupItemLp);
                        TextView tvGroupName = (TextView) tableGroupItemView.findViewWithTag("tvGroupName");
                        tvGroupName.setText(tableGroupName);

                        // 添加餐桌
                        FlowLayout flGroupLayout = (FlowLayout) tableGroupItemView.findViewWithTag("flGroupLayout");
                        addTableView(thisGroupTableInfo.split(","), flGroupLayout, tableGroupName);

                        editTableMainLayout.addView(tableGroupItemView);
                    }
                }
            }
        }else{

        }
    }

    private void addTableView(final String[] thisGroupTableInfoArr, FlowLayout parentView, final String tableGroupName){
        LinearLayout tableAddNewObject = new LinearLayout(TableEditActivity.this);
        tableAddNewObject.setGravity(Gravity.CENTER);
        tableAddNewObject.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tableAddNewObjLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tableAddNewObjLp.setMargins(DisplayUtil.dip2px(TableEditActivity.this, 10), DisplayUtil.dip2px(TableEditActivity.this, 10), DisplayUtil.dip2px(TableEditActivity.this, 10), DisplayUtil.dip2px(TableEditActivity.this, 10));
        tableAddNewObject.setLayoutParams(tableAddNewObjLp);
        // ImageView
        ImageView tableAddNewImg = new ImageView(TableEditActivity.this);
        tableAddNewImg.setTag("tableAddNewImg");
        ViewGroup.LayoutParams tableAddNewImgLp = new ViewGroup.LayoutParams(DisplayUtil.dip2px(TableEditActivity.this, 46), DisplayUtil.dip2px(TableEditActivity.this, 46));
        tableAddNewImg.setLayoutParams(tableAddNewImgLp);
        tableAddNewImg.setImageResource(R.drawable.add);
        tableAddNewObject.addView(tableAddNewImg);
        parentView.addView(tableAddNewObject);
        tableAddNewObject.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                View editTablePopup = getLayoutInflater().inflate(R.layout.edit_table, null);
                editTablePupopWindow = new PopupWindow(editTablePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                editTablePupopWindow.setTouchable(true);
                editTablePupopWindow.setOutsideTouchable(true);
                editTablePupopWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

                editTablePupopWindow.showAsDropDown(findViewById(R.id.activity_edit_table_title_bar));

                etTableGroupName = (EditText) editTablePopup.findViewById(R.id.etTableGroupName);
                etTableGroupNo = (EditText) editTablePopup.findViewById(R.id.etTableGroupNo);
                etTableNumStart = (EditText) editTablePopup.findViewById(R.id.etTableNumStart);
                etTableNumEnd = (EditText) editTablePopup.findViewById(R.id.etTableNumEnd);
                etTableNums = (EditText) editTablePopup.findViewById(R.id.etTableNums);
                btn_save_table_group = (Button) editTablePopup.findViewById(R.id.btn_save_table_group);
                etTableGroupName.setText("组名："+tableGroupName);
                etTableGroupNo.setText("组代号："+thisGroupTableInfoArr[0].split("\\.")[0]);
                etTableGroupName.setEnabled(false);
                etTableGroupNo.setEnabled(false);
                // 打开输入法
                etTableGroupName.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
                    @Override
                    public void onGlobalLayout() {
                        ComFun.openIME(TableEditActivity.this, etTableGroupName);
                    }
                });
                etTableNumStart.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().equals("")){
                            etTableNums.setText("");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                etTableNumEnd.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().equals("")){
                            etTableNums.setText("");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                etTableNums.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().equals("")){
                            etTableNumStart.setText("");
                            etTableNumEnd.setText("");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                btn_save_table_group.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        // 提交前输入验证
                        if(!(etTableNumStart.getText().toString().equals("") && etTableNumEnd.getText().toString().equals(""))
                                && etTableNumStart.getText().toString().equals("")){
                            ComFun.showToast(TableEditActivity.this, "请输入桌号起始值", Toast.LENGTH_SHORT);
                        }else if(!(etTableNumStart.getText().toString().equals("") && etTableNumEnd.getText().toString().equals(""))
                                && etTableNumEnd.getText().toString().equals("")){
                            ComFun.showToast(TableEditActivity.this, "请输入桌号结束值", Toast.LENGTH_SHORT);
                        }else if(!etTableNumStart.getText().toString().equals("") && !etTableNumEnd.getText().toString().equals("")
                                && Integer.parseInt(etTableNumStart.getText().toString()) > Integer.parseInt(etTableNumEnd.getText().toString())){
                            ComFun.showToast(TableEditActivity.this, "桌号起始值不能大于结束值", Toast.LENGTH_SHORT);
                        }else if((etTableNumStart.getText().toString().equals("") && etTableNumEnd.getText().toString().equals(""))
                                && etTableNums.getText().toString().equals("")){
                            ComFun.showToast(TableEditActivity.this, "桌号不能为空", Toast.LENGTH_SHORT);
                        }else if(!etTableNums.getText().toString().equals("") && !etTableNums.getText().toString().replaceAll("[^0-9^\\-]", "").equals(etTableNums.getText().toString())){
                            ComFun.showToast(TableEditActivity.this, "桌号中只能包含数字和符号“-”", Toast.LENGTH_SHORT);
                        }else{
                            // 验证成功，提交数据到服务器
                            if(editTablePupopWindow.isShowing()){
                                editTablePupopWindow.dismiss();
                            }
                            // 关闭输入法键盘
                            ComFun.closeIME(TableEditActivity.this, etTableGroupName);
                            // 显示加载动画
                            ComFun.showLoading(TableEditActivity.this, "餐桌数据提交中，请稍后");
                            // 异步任务提交数据
                            new AddNewTableTask(TableEditActivity.this, tableGroupName, thisGroupTableInfoArr[0].split("\\.")[0],
                                    etTableNumStart.getText().toString(), etTableNumEnd.getText().toString(), etTableNums.getText().toString(), false).executeOnExecutor(Executors.newCachedThreadPool());
                        }
                    }
                });
            }
        });
        for(int i=0; i<thisGroupTableInfoArr.length; i++){
            LinearLayout tableObject = new LinearLayout(TableEditActivity.this);
            tableObject.setGravity(Gravity.CENTER);
            tableObject.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams tableObjLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tableObjLp.setMargins(DisplayUtil.dip2px(TableEditActivity.this, 10), DisplayUtil.dip2px(TableEditActivity.this, 10), DisplayUtil.dip2px(TableEditActivity.this, 10), DisplayUtil.dip2px(TableEditActivity.this, 10));
            tableObject.setLayoutParams(tableObjLp);
            // ImageView
            ImageView tableImg = new ImageView(TableEditActivity.this);
            tableImg.setTag("tableImg"+thisGroupTableInfoArr[i]);
            tableImg.setTag(R.id.tag_table_state, thisGroupTableInfoArr[i]);
            ViewGroup.LayoutParams tableImgLp = new ViewGroup.LayoutParams(DisplayUtil.dip2px(TableEditActivity.this, 46), DisplayUtil.dip2px(TableEditActivity.this, 46));
            tableImg.setLayoutParams(tableImgLp);
            tableImg.setImageResource(R.drawable.dining_table_1);
            tableObject.addView(tableImg);
            // TextView
            TextView tableDes = new TextView(TableEditActivity.this);
            ViewGroup.LayoutParams tableDesLp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tableDes.setLayoutParams(tableDesLp);
            tableDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tableDes.setText(thisGroupTableInfoArr[i]);
            tableDes.setTag("tableTxtInfo");
            tableDes.setTag(R.id.tag_table_txt_no, "tableTxtInfo_"+thisGroupTableInfoArr[i]);
            TextPaint tableDesTp = tableDes.getPaint();
            tableDesTp.setFakeBoldText(true);
            tableObject.addView(tableDes);
            parentView.addView(tableObject);
            tableObject.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final String tableDesInfo = ((TextView) ((LinearLayout) v).getChildAt(1)).getText().toString();
                    new android.support.v7.app.AlertDialog.Builder(TableEditActivity.this).setTitle("删除餐桌").setMessage("确认删除餐桌【"+ tableDesInfo +"】吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 显示加载动画
                                    ComFun.showLoading(TableEditActivity.this, "正在删除餐桌，请稍后");
                                        new DeleteTableTask(TableEditActivity.this, "table", null, tableDesInfo).executeOnExecutor(Executors.newCachedThreadPool());
                                }
                            })
                            .setNegativeButton("取消", null).show();
                    return false;
                }
            });
        }
    }

    // 初始化添加新桌组悬浮按钮及事件
    public void initAddNewTableGroup(){
        add_new_table_info = (FloatingActionButton) findViewById(R.id.add_new_table_info);
        add_new_table_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View editTablePopup = getLayoutInflater().inflate(R.layout.edit_table, null);
                editTablePupopWindow = new PopupWindow(editTablePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                editTablePupopWindow.setTouchable(true);
                editTablePupopWindow.setOutsideTouchable(true);
                editTablePupopWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

                editTablePupopWindow.showAsDropDown(findViewById(R.id.activity_edit_table_title_bar));

                etTableGroupName = (EditText) editTablePopup.findViewById(R.id.etTableGroupName);
                etTableGroupNo = (EditText) editTablePopup.findViewById(R.id.etTableGroupNo);
                etTableNumStart = (EditText) editTablePopup.findViewById(R.id.etTableNumStart);
                etTableNumEnd = (EditText) editTablePopup.findViewById(R.id.etTableNumEnd);
                etTableNums = (EditText) editTablePopup.findViewById(R.id.etTableNums);
                btn_save_table_group = (Button) editTablePopup.findViewById(R.id.btn_save_table_group);
                // 打开输入法
                etTableGroupName.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
                    @Override
                    public void onGlobalLayout() {
                        ComFun.openIME(TableEditActivity.this, etTableGroupName);
                    }
                });
                etTableNumStart.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().equals("")){
                            etTableNums.setText("");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                etTableNumEnd.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().equals("")){
                            etTableNums.setText("");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                etTableNums.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(!s.toString().equals("")){
                            etTableNumStart.setText("");
                            etTableNumEnd.setText("");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                btn_save_table_group.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        // 提交前输入验证
                        if(etTableGroupName.getText().toString().equals("")){
                            ComFun.showToast(TableEditActivity.this, "请输入组名称", Toast.LENGTH_SHORT);
                        }else if(etTableGroupNo.getText().toString().equals("")){
                            ComFun.showToast(TableEditActivity.this, "请输入组代号", Toast.LENGTH_SHORT);
                        }else if(!(etTableNumStart.getText().toString().equals("") && etTableNumEnd.getText().toString().equals(""))
                                && etTableNumStart.getText().toString().equals("")){
                            ComFun.showToast(TableEditActivity.this, "请输入桌号起始值", Toast.LENGTH_SHORT);
                        }else if(!(etTableNumStart.getText().toString().equals("") && etTableNumEnd.getText().toString().equals(""))
                                && etTableNumEnd.getText().toString().equals("")){
                            ComFun.showToast(TableEditActivity.this, "请输入桌号结束值", Toast.LENGTH_SHORT);
                        }else if(!etTableNumStart.getText().toString().equals("") && !etTableNumEnd.getText().toString().equals("")
                                && Integer.parseInt(etTableNumStart.getText().toString()) > Integer.parseInt(etTableNumEnd.getText().toString())){
                            ComFun.showToast(TableEditActivity.this, "桌号起始值不能大于结束值", Toast.LENGTH_SHORT);
                        }else if((etTableNumStart.getText().toString().equals("") && etTableNumEnd.getText().toString().equals(""))
                                && etTableNums.getText().toString().equals("")){
                            ComFun.showToast(TableEditActivity.this, "桌号不能为空", Toast.LENGTH_SHORT);
                        }else if(!etTableNums.getText().toString().equals("") && !etTableNums.getText().toString().replaceAll("[^0-9^\\-]", "").equals(etTableNums.getText().toString())){
                            ComFun.showToast(TableEditActivity.this, "桌号中只能包含数字和符号“-”", Toast.LENGTH_SHORT);
                        }else{
                            // 验证成功，提交数据到服务器
                            if(editTablePupopWindow.isShowing()){
                                editTablePupopWindow.dismiss();
                            }
                            // 关闭输入法键盘
                            ComFun.closeIME(TableEditActivity.this, etTableGroupName);
                            // 显示加载动画
                            ComFun.showLoading(TableEditActivity.this, "餐桌数据提交中，请稍后");
                            // 异步任务提交数据
                            new AddNewTableTask(TableEditActivity.this, etTableGroupName.getText().toString(), etTableGroupNo.getText().toString(),
                                    etTableNumStart.getText().toString(), etTableNumEnd.getText().toString(), etTableNums.getText().toString(), true).executeOnExecutor(Executors.newCachedThreadPool());
                        }
                    }
                });
            }
        });
    }

    // 初始化删除桌组悬浮按钮及事件
    public void initDeleteTableGroup(){
        delete_new_table_info = (FloatingActionButton) findViewById(R.id.delete_new_table_info);
        delete_new_table_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectTableGroupNameList.clear();
                // 获取当前所有桌组信息
                tableGroupNames = SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames");
                if(ComFun.strNull(tableGroupNames)){
                    flags = new boolean[tableGroupNames.split(",").length];
                    for(int i=0; i<tableGroupNames.split(",").length; i++){
                        flags[i] = false;
                    }
                    AlertDialog.Builder builder=new android.app.AlertDialog.Builder(TableEditActivity.this);
                    //设置对话框的图标
                    builder.setIcon(R.drawable.mode);
                    //设置对话框的标题
                    builder.setTitle("删除餐桌组(将删除组内所有餐桌)");
                    builder.setMultiChoiceItems(tableGroupNames.split(","), flags, new DialogInterface.OnMultiChoiceClickListener(){
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            flags[which] = isChecked;
                            String tableGroupNames = SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames");
                            deleteSelectTableGroupNameList.add(tableGroupNames.split(",")[which]);
                        }
                    });

                    //添加一个确定按钮
                    builder.setPositiveButton(" 确 定 ", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            if(deleteSelectTableGroupNameList.size() > 0){
                                // 显示加载动画
                                ComFun.showLoading(TableEditActivity.this, "正在删除餐桌组，请稍后");
                                new DeleteTableTask(TableEditActivity.this, "group", deleteSelectTableGroupNameList, null).executeOnExecutor(Executors.newCachedThreadPool());
                            }else{
                                ComFun.showToast(TableEditActivity.this, "没有选择要删除的桌组", Toast.LENGTH_SHORT);
                            }
                        }
                    });
                    //添加一个取消按钮
                    builder.setNegativeButton(" 取 消 ", null);
                    //创建一个复选框对话框
                    Dialog dialog = builder.create();
                    dialog.show();
                }else{
                    ComFun.showToast(TableEditActivity.this, "桌组信息为空，请添加桌组管理", Toast.LENGTH_SHORT);
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
                case MSG_ADD_TABLE:
                    // 隐藏加载动画
                    ComFun.hideLoading(TableEditActivity.this);
                    String addNewTableResult = b.getString("addNewTableResult");
                    String groupName = b.getString("groupName");
                    String groupNo = b.getString("groupNo");
                    String startNum = b.getString("startNum");
                    String endNum = b.getString("endNum");
                    String tableNums = b.getString("tableNums");
                    if (addNewTableResult.equals("true")) {
                        ComFun.showToast(TableEditActivity.this, "添加新餐桌成功", Toast.LENGTH_SHORT);
                        // 添加或更新本地餐桌数据
                        if(ComFun.strNull(SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames"))){
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames", SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames") + "," + groupName);
                        }else{
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames", groupName);
                        }
                        StringBuilder tableInfoSimpleDetailSb = new StringBuilder("");
                        StringBuilder tableInfoDetailSb = new StringBuilder("");
                        if(ComFun.strNull(tableNums)){
                            String[] tableNumArr = tableNums.split("-");
                            if(ComFun.strNull(tableNumArr) && tableNumArr.length > 0){
                                for(String s : tableNumArr){
                                    if(ComFun.strNull(s)){
                                        tableInfoSimpleDetailSb.append(groupNo+"."+Integer.parseInt(s));
                                        tableInfoSimpleDetailSb.append(",");
                                        tableInfoDetailSb.append(groupNo+"."+Integer.parseInt(s)+"|1");
                                        tableInfoDetailSb.append(",");
                                    }
                                }
                            }
                        }else{
                            for(int i=Integer.parseInt(startNum); i<=Integer.parseInt(endNum); i++){
                                tableInfoSimpleDetailSb.append(groupNo+"."+i);
                                tableInfoSimpleDetailSb.append(",");
                                tableInfoDetailSb.append(groupNo+"."+i+"|1");
                                tableInfoDetailSb.append(",");
                            }
                        }
                        if(ComFun.strNull(tableInfoDetailSb.toString())){
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + groupName, tableInfoSimpleDetailSb.toString().substring(0, tableInfoSimpleDetailSb.toString().length() - 1));
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfo" + groupName, tableInfoDetailSb.toString().substring(0, tableInfoDetailSb.toString().length() - 1));
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableFullInfo", SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableFullInfo") + "," + tableInfoDetailSb.toString().substring(0, tableInfoDetailSb.toString().length() - 1));
                        }else{
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + groupName, "");
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfo" + groupName, "");
                        }
                        // 更新该页面餐桌布局(添加了新桌组)
                        initTableView();
                    }else if (addNewTableResult.equals("false")) {
                        ComFun.showToast(TableEditActivity.this, "添加新餐桌失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (addNewTableResult.equals("time_out")) {
                        ComFun.showToast(TableEditActivity.this, "添加新餐桌超时，请稍后重试", Toast.LENGTH_SHORT);
                    }else if (addNewTableResult.equals("has_table_group")) {
                        ComFun.showToast(TableEditActivity.this, "组【"+ groupName +"】或组代号【"+ groupNo +"】已经存在，请重新添加", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_ADD_TABLE_TO_COM:
                    // 隐藏加载动画
                    ComFun.hideLoading(TableEditActivity.this);
                    String addNewTableToComResult = b.getString("addNewTableResult");
                    String groupNameToCom = b.getString("groupName");
                    String groupNoToCom = b.getString("groupNo");
                    String startNumToCom = b.getString("startNum");
                    String endNumToCom = b.getString("endNum");
                    String tableNumsToCom = b.getString("tableNums");
                    if (addNewTableToComResult.equals("true")) {
                        ComFun.showToast(TableEditActivity.this, "在组【"+ groupNameToCom +"】中补充餐桌成功", Toast.LENGTH_SHORT);
                        // 添加或更新本地餐桌数据
                        String thisGroupTableInfo = SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + groupNameToCom);
                        StringBuilder tableInfoSimpleDetailSb = new StringBuilder("");
                        StringBuilder tableInfoDetailSb = new StringBuilder("");
                        if(ComFun.strNull(tableNumsToCom)){
                            String[] tableNumArr = tableNumsToCom.split("-");
                            if(ComFun.strNull(tableNumArr) && tableNumArr.length > 0){
                                for(String s : tableNumArr){
                                    if(ComFun.strNull(s)){
                                        if(!ComFun.strInArr(thisGroupTableInfo.split(","), s)){
                                            tableInfoSimpleDetailSb.append(groupNoToCom+"."+Integer.parseInt(s));
                                            tableInfoSimpleDetailSb.append(",");
                                            tableInfoDetailSb.append(groupNoToCom+"."+Integer.parseInt(s)+"|1");
                                            tableInfoDetailSb.append(",");
                                        }
                                    }
                                }
                            }
                        }else{
                            for(int i=Integer.parseInt(startNumToCom); i<=Integer.parseInt(endNumToCom); i++){
                                if(!ComFun.strInArr(thisGroupTableInfo.split(","), String.valueOf(i))){
                                    tableInfoSimpleDetailSb.append(groupNoToCom+"."+i);
                                    tableInfoSimpleDetailSb.append(",");
                                    tableInfoDetailSb.append(groupNoToCom+"."+i+"|1");
                                    tableInfoDetailSb.append(",");
                                }
                            }
                        }
                        if(ComFun.strNull(tableInfoDetailSb.toString())){
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + groupNameToCom, thisGroupTableInfo + "," + tableInfoSimpleDetailSb.toString().substring(0, tableInfoSimpleDetailSb.toString().length() - 1));
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfo" + groupNameToCom, thisGroupTableInfo + "," + tableInfoDetailSb.toString().substring(0, tableInfoDetailSb.toString().length() - 1));
                            SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableFullInfo", SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableFullInfo") + "," + tableInfoDetailSb.toString().substring(0, tableInfoDetailSb.toString().length() - 1));
                        }
                        // 更新该页面餐桌布局(添加了新桌组)
                        initTableView();
                    }else if (addNewTableToComResult.equals("false")) {
                        ComFun.showToast(TableEditActivity.this, "在组【"+ groupNameToCom +"】中补充餐桌失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (addNewTableToComResult.equals("time_out")) {
                        ComFun.showToast(TableEditActivity.this, "在组【"+ groupNameToCom +"】中补充餐桌超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_DELETE_GROUP:
                    // 隐藏加载动画
                    ComFun.hideLoading(TableEditActivity.this);
                    String deleteGroupResult = b.getString("deleteTableResult");
                    if (deleteGroupResult.equals("true")) {
                        ComFun.showToast(TableEditActivity.this, "删除餐桌组成功", Toast.LENGTH_SHORT);
                        // 更新该页面餐桌布局
                        String deleteTableGroup = b.getString("deleteTableGroup");
                        deleteTableInfoFromSharedPerferences(deleteTableGroup, 1);
                        initTableView();
                    }else if (deleteGroupResult.equals("false")) {
                        ComFun.showToast(TableEditActivity.this, "删除餐桌组失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (deleteGroupResult.equals("time_out")) {
                        ComFun.showToast(TableEditActivity.this, "删除餐桌组超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_DELETE_TABLE:
                    // 隐藏加载动画
                    ComFun.hideLoading(TableEditActivity.this);
                    String deleteTableResult = b.getString("deleteTableResult");
                    if (deleteTableResult.equals("true")) {
                        ComFun.showToast(TableEditActivity.this, "删除餐桌成功", Toast.LENGTH_SHORT);
                        // 更新该页面餐桌布局
                        String deleteTableInfo = b.getString("deleteTableInfo");
                        deleteTableInfoFromSharedPerferences(deleteTableInfo, 2);
                        initTableView();
                    }else if (deleteTableResult.equals("false")) {
                        ComFun.showToast(TableEditActivity.this, "删除餐桌失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (deleteTableResult.equals("time_out")) {
                        ComFun.showToast(TableEditActivity.this, "删除餐桌超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    // 从本地文件中删除已删除的餐桌信息
    public void deleteTableInfoFromSharedPerferences(String deleteTableInfo, int type){
        tableGroupNames = SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames");
        if(ComFun.strNull(tableGroupNames)){
            StringBuilder sb1 = new StringBuilder("");
            if(tableGroupNames.contains(",")){
                for(String tbGroupName : tableGroupNames.split(",")){
                    if(type == 2){
                        String thisGroupTableInfo = SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + tbGroupName);
                        if(ComFun.strNull(thisGroupTableInfo)){
                            String[] tableInfoArr = thisGroupTableInfo.split(",");
                            if(ComFun.strInArr(tableInfoArr, deleteTableInfo)){
                                StringBuilder sb = new StringBuilder("");
                                for(String old : tableInfoArr){
                                    if(!old.equals(deleteTableInfo)){
                                        sb.append(old);
                                        sb.append(",");
                                    }
                                }
                                if(ComFun.strNull(sb.toString())){
                                    SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + tbGroupName, sb.toString().substring(0, sb.toString().length() - 1));
                                }else{
                                    SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + tbGroupName, "");
                                }
                            }
                        }
                    }else{
                        if(deleteTableInfo.contains(",")){
                            if(!ComFun.strInArr(deleteTableInfo.split(","), tbGroupName)){
                                sb1.append(tbGroupName);
                                sb1.append(",");
                            }
                        }else{
                            if(!deleteTableInfo.equals(tbGroupName)){
                                sb1.append(tbGroupName);
                                sb1.append(",");
                            }
                        }
                    }
                }
            }else{
                if(type == 2){
                    String thisGroupTableInfo = SharedPreferencesTool.getFromShared(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + tableGroupNames);
                    if(ComFun.strNull(thisGroupTableInfo)){
                        String[] tableInfoArr = thisGroupTableInfo.split(",");
                        if(ComFun.strInArr(tableInfoArr, deleteTableInfo)){
                            StringBuilder sb = new StringBuilder("");
                            for(String old : tableInfoArr){
                                if(!old.equals(deleteTableInfo)){
                                    sb.append(old);
                                    sb.append(",");
                                }
                            }
                            if(ComFun.strNull(sb.toString())){
                                SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + tableGroupNames, sb.toString().substring(0, sb.toString().length() - 1));
                            }else{
                                SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableInfoSimple" + tableGroupNames, "");
                            }
                        }
                    }
                }else{
                    if(deleteTableInfo.contains(",")){
                        if(!ComFun.strInArr(deleteTableInfo.split(","), tableGroupNames)){
                            sb1.append(tableGroupNames);
                            sb1.append(",");
                        }
                    }else{
                        if(!deleteTableInfo.equals(tableGroupNames)){
                            sb1.append(tableGroupNames);
                            sb1.append(",");
                        }
                    }
                }
            }
            if(type == 1 && ComFun.strNull(sb1.toString())){
                SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames", sb1.toString().substring(0, sb1.toString().length() - 1));
            }else if(type == 1){
                SharedPreferencesTool.addOrUpdate(TableEditActivity.this, "BouilliTableInfo", "tableGroupNames", "");
            }
        }
    }

}
