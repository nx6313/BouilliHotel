package com.bouilli.nxx.bouillihotel;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.AddNewMenuTask;
import com.bouilli.nxx.bouillihotel.asyncTask.AddNewUserTask;
import com.bouilli.nxx.bouillihotel.asyncTask.DeleteMenuTask;
import com.bouilli.nxx.bouillihotel.asyncTask.DeleteUserTask;
import com.bouilli.nxx.bouillihotel.asyncTask.GetUserInfoTask;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.MyDatePickerDialog;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class UserPermissionEditActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_ADD_USER = 1;
    public static final int MSG_INIT_USER = 2;
    public static final int MSG_DELETE_USER = 3;
    private FloatingActionButton add_new_user_info;
    private PopupWindow editUserPupopWindow;

    private String userGroupNames = "2#&#员工,3#&#传菜员,4#&#后厨管理员,1#&#副管理员,0#&#系统管理员";
    Map<String, String> userItemMap = new HashMap<>();

    private TextView tv_add_user_tip;
    private RadioGroup rgUserInfoGroup;
    private RadioGroup rgUserInfoSex;
    private EditText etUserInfoName;
    private EditText etUserInfoAge;
    private EditText etUserInfoBirthday;
    private EditText etUserInfoPhone;
    private Button btn_save_user_info;

    private LinearLayout userMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new UserPermissionEditActivity.mHandler();

        tv_add_user_tip = (TextView) findViewById(R.id.tv_add_user_tip);

        userMainLayout = (LinearLayout) findViewById(R.id.userMainLayout);
        userMainLayout.removeAllViews();

        new GetUserInfoTask(UserPermissionEditActivity.this).executeOnExecutor(Executors.newCachedThreadPool());

        initAddNewUser();
    }

    private void initUserView(){
        if(ComFun.strNull(userGroupNames)){
            userMainLayout.removeAllViews();
            int index = 0;
            for(String userGroupName : userGroupNames.split(",")){
                String userItemChiles = userItemMap.get("userItemChild" + userGroupName.split("#&#")[0]);
                if(ComFun.strNull(userItemChiles)){
                    index++;
                    LinearLayout userItemlayout = new LinearLayout(UserPermissionEditActivity.this);
                    userItemlayout.setTag("close");
                    userItemlayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                    userItemlayout.setPadding(DisplayUtil.dip2px(UserPermissionEditActivity.this, 10), 0, 0, 0);
                    userItemlayout.setBackgroundResource(R.drawable.edit_menu_item_style);
                    userItemlayout.setClickable(true);
                    userItemlayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams userItemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(UserPermissionEditActivity.this, 40));
                    userItemlayout.setLayoutParams(userItemLp);
                    // item图标
                    ImageView userItemImg = new ImageView(UserPermissionEditActivity.this);
                    userItemImg.setTag("itemImg");
                    userItemImg.setImageResource(R.drawable.menu_close);
                    LinearLayout.LayoutParams userItemImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(UserPermissionEditActivity.this, 20), DisplayUtil.dip2px(UserPermissionEditActivity.this, 29));
                    userItemImgLp.setMargins(DisplayUtil.dip2px(UserPermissionEditActivity.this, 2), DisplayUtil.dip2px(UserPermissionEditActivity.this, 2), DisplayUtil.dip2px(UserPermissionEditActivity.this, 2), DisplayUtil.dip2px(UserPermissionEditActivity.this, 2));
                    userItemImg.setLayoutParams(userItemImgLp);
                    userItemlayout.addView(userItemImg);
                    // item文字
                    TextView userItemDes = new TextView(UserPermissionEditActivity.this);
                    userItemDes.setTag("userItemNameTxt");
                    userItemDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    userItemDes.setText(userGroupName.split("#&#")[1]);
                    TextPaint userItemTp = userItemDes.getPaint();
                    userItemTp.setFakeBoldText(true);
                    LinearLayout.LayoutParams userItemDesLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(UserPermissionEditActivity.this, 0), LinearLayout.LayoutParams.WRAP_CONTENT, 3);
                    userItemDesLp.setMargins(DisplayUtil.dip2px(UserPermissionEditActivity.this, 8), 0, 0, 0);
                    userItemDes.setLayoutParams(userItemDesLp);
                    userItemlayout.addView(userItemDes);
                    // item数量
                    TextView userItemChildNum = new TextView(UserPermissionEditActivity.this);
                    userItemChildNum.setTag(userGroupName.split("#&#")[0]);
                    userItemChildNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    if(ComFun.strNull(userItemChiles)){
                        userItemChildNum.setText("共 "+ userItemChiles.split(",").length +" 名");
                    }else{
                        userItemChildNum.setText("共 0 名");
                    }
                    TextPaint userItemChildNumTp = userItemChildNum.getPaint();
                    userItemChildNumTp.setFakeBoldText(true);
                    LinearLayout.LayoutParams userItemChildNumLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(UserPermissionEditActivity.this, 0), LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                    userItemChildNumLp.setMargins(DisplayUtil.dip2px(UserPermissionEditActivity.this, 8), 0, 0, 0);
                    userItemChildNum.setLayoutParams(userItemChildNumLp);
                    userItemlayout.addView(userItemChildNum);
                    userMainLayout.addView(userItemlayout);
                    // 添加对应该菜品组下的详细菜品信息数据
                    LinearLayout userChildlayout = new LinearLayout(UserPermissionEditActivity.this);
                    userChildlayout.setTag("itemChild_"+ userGroupName.split("#&#")[0]);
                    userChildlayout.setOrientation(LinearLayout.VERTICAL);
                    userChildlayout.setVisibility(View.GONE);// 默认隐藏下拉子项
                    int chileItemIndex = 0;
                    for(String childInfo : userItemChiles.split(",")){
                        LinearLayout userChildItemlayout = new LinearLayout(UserPermissionEditActivity.this);
                        userChildItemlayout.setTag("user_"+ userGroupName.split("#&#")[0]);
                        userChildItemlayout.setPadding(DisplayUtil.dip2px(UserPermissionEditActivity.this, 20), 0, DisplayUtil.dip2px(UserPermissionEditActivity.this, 20), 0);
                        if(chileItemIndex % 2 == 0){
                            userChildItemlayout.setBackgroundColor(Color.parseColor("#aeb39f"));
                        }else{
                            userChildItemlayout.setBackgroundColor(Color.parseColor("#c1b0be"));
                        }
                        userChildItemlayout.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams userChildItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        userChildItemlayout.setLayoutParams(userChildItemLp);
                        // 子项每一项图标
                        ImageView userChildItemImg = new ImageView(UserPermissionEditActivity.this);
                        userChildItemImg.setTag(childInfo.split("#&#")[0]);
                        if(userGroupName.split("#&#")[0].equals("0")){
                            // 系统管理员
                            userChildItemImg.setImageResource(R.drawable.user_super_manager);
                        }else if(userGroupName.split("#&#")[0].equals("1")){
                            // 副管理员
                            userChildItemImg.setImageResource(R.drawable.user_manager);
                        }else{
                            // 员工
                            if(childInfo.split("#&#")[3].equals("0")){
                                // 男
                                userChildItemImg.setImageResource(R.drawable.user_man);
                            }else{
                                // 女
                                userChildItemImg.setImageResource(R.drawable.user_woman);
                            }
                        }
                        userChildItemImg.setPadding(DisplayUtil.dip2px(UserPermissionEditActivity.this, 5), DisplayUtil.dip2px(UserPermissionEditActivity.this, 5), DisplayUtil.dip2px(UserPermissionEditActivity.this, 5), DisplayUtil.dip2px(UserPermissionEditActivity.this, 5));
                        LinearLayout.LayoutParams userChildItemImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(UserPermissionEditActivity.this, 45), DisplayUtil.dip2px(UserPermissionEditActivity.this, 45));
                        userChildItemImgLp.setMargins(DisplayUtil.dip2px(UserPermissionEditActivity.this, 2), DisplayUtil.dip2px(UserPermissionEditActivity.this, 2), DisplayUtil.dip2px(UserPermissionEditActivity.this, 2), DisplayUtil.dip2px(UserPermissionEditActivity.this, 2));
                        userChildItemImg.setLayoutParams(userChildItemImgLp);
                        userChildItemlayout.addView(userChildItemImg);
                        // 子项每一项主体（名称带简要说明）
                        LinearLayout userChildItemDeslayout = new LinearLayout(UserPermissionEditActivity.this);
                        userChildItemDeslayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                        userChildItemDeslayout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams userChildItemDesLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(UserPermissionEditActivity.this, 0), LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        userChildItemDesLp.setMargins(DisplayUtil.dip2px(UserPermissionEditActivity.this, 8), 0, 0, 0);
                        userChildItemDeslayout.setLayoutParams(userChildItemDesLp);
                        // 主体-->人名
                        TextView userChildItemDesNameTxt = new TextView(UserPermissionEditActivity.this);
                        userChildItemDesNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        userChildItemDesNameTxt.setText(childInfo.split("#&#")[1]);
                        userChildItemDesNameTxt.setTextColor(Color.parseColor("#000000"));
                        TextPaint userChildItemDesNameTxtTp = userChildItemDesNameTxt.getPaint();
                        userChildItemDesNameTxtTp.setFakeBoldText(true);
                        LinearLayout.LayoutParams userChildItemDesNameTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        userChildItemDesNameTxt.setLayoutParams(userChildItemDesNameTxtLp);
                        userChildItemDeslayout.addView(userChildItemDesNameTxt);
                        userChildItemlayout.addView(userChildItemDeslayout);
                        if(!childInfo.split("#&#")[2].equals("-")){
                            TextView userChildItemDesssTxt = new TextView(UserPermissionEditActivity.this);
                            userChildItemDesssTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            userChildItemDesssTxt.setText(childInfo.split("#&#")[2]);
                            TextPaint userChildItemDesssTxtTp = userChildItemDesssTxt.getPaint();
                            userChildItemDesssTxtTp.setFakeBoldText(true);
                            LinearLayout.LayoutParams userChildItemDesssTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            userChildItemDesssTxt.setLayoutParams(userChildItemDesssTxtLp);
                            userChildItemDeslayout.addView(userChildItemDesssTxt);
                        }
                        // 子项每一项介绍
                        LinearLayout userChildItemPricelayout = new LinearLayout(UserPermissionEditActivity.this);
                        userChildItemPricelayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                        userChildItemPricelayout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams userChildItemPriceLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(UserPermissionEditActivity.this, 0), LinearLayout.LayoutParams.MATCH_PARENT, 1);
                        userChildItemPriceLp.setMargins(DisplayUtil.dip2px(UserPermissionEditActivity.this, 8), 0, 0, 0);
                        userChildItemPricelayout.setLayoutParams(userChildItemPriceLp);
                        // 介绍文本
                        TextView userChildItemPriceTxt = new TextView(UserPermissionEditActivity.this);
                        userChildItemPriceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        if(!childInfo.split("#&#")[5].equals("-")){
                            userChildItemPriceTxt.setAutoLinkMask(Linkify.PHONE_NUMBERS);
                            userChildItemPriceTxt.setLinkTextColor(Color.parseColor("#39808D"));
                            userChildItemPriceTxt.setText(childInfo.split("#&#")[5]);
                        }else{
                            if(userGroupName.split("#&#")[0].equals("0")){
                                // 系统管理员
                                userChildItemPriceTxt.setText("管理系统");
                            }else{
                                userChildItemPriceTxt.setText("无手机号");
                            }
                        }
                        TextPaint userChildItemPriceTxtTp = userChildItemPriceTxt.getPaint();
                        userChildItemPriceTxtTp.setFakeBoldText(true);
                        LinearLayout.LayoutParams userChildItemPriceTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        userChildItemPriceTxt.setLayoutParams(userChildItemPriceTxtLp);
                        userChildItemPricelayout.addView(userChildItemPriceTxt);
                        userChildItemlayout.addView(userChildItemPricelayout);

                        userChildlayout.addView(userChildItemlayout);
                        userChildItemlayout.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                final String userGroupId = v.getTag().toString();
                                final String userInfoId = (((LinearLayout) v).getChildAt(0)).getTag().toString();
                                final String userInfo = ((TextView) ((LinearLayout) ((LinearLayout) v).getChildAt(1)).getChildAt(0)).getText().toString();
                                if(!userGroupId.equals("user_0")){
                                    String delObjAb = "员工";
                                    if(userGroupId.equals("user_1")){
                                        delObjAb = "副管理员";
                                    }else if(userGroupId.equals("user_2")){
                                        delObjAb = "员工";
                                    }else if(userGroupId.equals("user_3")){
                                        delObjAb = "传菜员";
                                    }else if(userGroupId.equals("user_4")){
                                        delObjAb = "后厨管理员";
                                    }
                                    new android.support.v7.app.AlertDialog.Builder(UserPermissionEditActivity.this).setTitle("删除人员").setMessage("确认删除"+ delObjAb +"【"+ userInfo +"】吗？")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // 显示加载动画
                                                    ComFun.showLoading(UserPermissionEditActivity.this, "正在删除，请稍后");
                                                    new DeleteUserTask(UserPermissionEditActivity.this, userInfoId, userGroupId.split("_")[1]).executeOnExecutor(Executors.newCachedThreadPool());
                                                }
                                            })
                                            .setNegativeButton("取消", null).show();
                                }else{
                                    ComFun.showToast(UserPermissionEditActivity.this, "系统管理员不可删除", Toast.LENGTH_SHORT);
                                }
                                return false;
                            }
                        });
                        chileItemIndex++;
                    }
                    userMainLayout.addView(userChildlayout);
                    LinearLayout.LayoutParams userChildLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    userChildlayout.setLayoutParams(userChildLp);
                    // 如果不是最后一项，则添加分割线
                    if(index < userGroupNames.split(",").length){
                        View splitView = new View(UserPermissionEditActivity.this);
                        splitView.setBackgroundColor(Color.parseColor("#b7b7b7"));
                        LinearLayout.LayoutParams splitViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(UserPermissionEditActivity.this, 1));
                        splitView.setLayoutParams(splitViewLp);
                        userMainLayout.addView(splitView);
                    }
                    // 点击item事件
                    userItemlayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String openCloseState = v.getTag().toString();
                            String userItemAboutGroupId = ((LinearLayout)v).getChildAt(2).getTag().toString();
                            if(openCloseState.equals("close")){
                                v.setTag("open");
                                ImageView itemImg = (ImageView) v.findViewWithTag("itemImg");
                                itemImg.setImageResource(R.drawable.menu_open);
                                LinearLayout itemChildLayout = (LinearLayout) userMainLayout.findViewWithTag("itemChild_"+ userItemAboutGroupId);
                                if(itemChildLayout != null){
                                    itemChildLayout.setVisibility(View.VISIBLE);
                                }
                            }else{
                                v.setTag("close");
                                ImageView itemImg = (ImageView) v.findViewWithTag("itemImg");
                                itemImg.setImageResource(R.drawable.menu_close);
                                LinearLayout itemChildLayout = (LinearLayout) userMainLayout.findViewWithTag("itemChild_"+ userItemAboutGroupId);
                                if(itemChildLayout != null){
                                    itemChildLayout.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                }
            }
            if(index != 0){
                tv_add_user_tip.setVisibility(View.GONE);
            }else{
                tv_add_user_tip.setVisibility(View.VISIBLE);
            }
        }
    }

    // 初始化添加新人悬浮按钮及事件
    public void initAddNewUser(){
        add_new_user_info = (FloatingActionButton) findViewById(R.id.add_new_user_info);
        add_new_user_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View editTablePopup = getLayoutInflater().inflate(R.layout.edit_user_info, null);
                rgUserInfoGroup = (RadioGroup) editTablePopup.findViewById(R.id.rgUserInfoGroup);
                rgUserInfoGroup.removeAllViews();
                for(String userGroupN : userGroupNames.split(",")){
                    if(!userGroupN.split("#&#")[0].equals("0")){
                        RadioButton radioButton = new RadioButton(UserPermissionEditActivity.this);
                        radioButton.setTag(userGroupN.split("#&#")[0]);
                        RadioGroup.LayoutParams radioButtonLp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                        radioButton.setLayoutParams(radioButtonLp);
                        radioButton.setText(userGroupN.split("#&#")[1]);
                        rgUserInfoGroup.addView(radioButton);
                    }
                }
                rgUserInfoSex = (RadioGroup) editTablePopup.findViewById(R.id.rgUserInfoSex);
                rgUserInfoSex.removeAllViews();
                for(String userSexN : "0#&#男,1#&#女".split(",")){
                    RadioButton radioButton = new RadioButton(UserPermissionEditActivity.this);
                    radioButton.setTag(userSexN.split("#&#")[0]);
                    RadioGroup.LayoutParams radioButtonLp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                    radioButton.setLayoutParams(radioButtonLp);
                    radioButton.setText(userSexN.split("#&#")[1]);
                    rgUserInfoSex.addView(radioButton);
                }
                editUserPupopWindow = new PopupWindow(editTablePopup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                editUserPupopWindow.setTouchable(true);
                editUserPupopWindow.setOutsideTouchable(true);
                editUserPupopWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

                editUserPupopWindow.showAsDropDown(findViewById(R.id.activity_edit_user_title_bar));

                etUserInfoName = (EditText) editTablePopup.findViewById(R.id.etUserInfoName);
                etUserInfoAge = (EditText) editTablePopup.findViewById(R.id.etUserInfoAge);
                etUserInfoBirthday = (EditText) editTablePopup.findViewById(R.id.etUserInfoBirthday);
                etUserInfoBirthday.setCursorVisible(false);// 设置光标不可见
                etUserInfoBirthday.setFocusable(false);// 无焦点
                etUserInfoBirthday.setFocusableInTouchMode(false);// 触摸时也得不到焦点
                etUserInfoBirthday.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        new MyDatePickerDialog(UserPermissionEditActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                etUserInfoBirthday.setText(month + "月" + dayOfMonth + "日");
                            }
                        }, year, month, day, "选择人员生日", 4).show();
                    }
                });
                etUserInfoPhone = (EditText) editTablePopup.findViewById(R.id.etUserInfoPhone);
                btn_save_user_info = (Button) editTablePopup.findViewById(R.id.btn_save_user_info);
                // 打开输入法
                etUserInfoName.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
                    @Override
                    public void onGlobalLayout() {
                        ComFun.openIME(UserPermissionEditActivity.this, etUserInfoName);
                    }
                });

                btn_save_user_info.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        // 提交前输入验证
                        String selectUserGroupId = "";
                        for(int i=0; i<rgUserInfoGroup.getChildCount(); i++){
                            if(((RadioButton)rgUserInfoGroup.getChildAt(i)).isChecked()){
                                selectUserGroupId = rgUserInfoGroup.getChildAt(i).getTag().toString();
                            }
                        }
                        String selectUserSexId = "";
                        for(int i=0; i<rgUserInfoSex.getChildCount(); i++){
                            if(((RadioButton)rgUserInfoSex.getChildAt(i)).isChecked()){
                                selectUserSexId = rgUserInfoSex.getChildAt(i).getTag().toString();
                            }
                        }
                        if(etUserInfoName.getText().toString().trim().equals("")){
                            ComFun.showToast(UserPermissionEditActivity.this, "请填写人员名称", Toast.LENGTH_SHORT);
                        }else if(selectUserGroupId.trim().equals("")){
                            ComFun.showToast(UserPermissionEditActivity.this, "请选择人员工作类型", Toast.LENGTH_SHORT);
                        }else if(selectUserSexId.trim().equals("")){
                            ComFun.showToast(UserPermissionEditActivity.this, "请选择人员性别", Toast.LENGTH_SHORT);
                        }else{
                            // 验证成功，提交数据到服务器
                            if(editUserPupopWindow.isShowing()){
                                editUserPupopWindow.dismiss();
                            }
                            // 关闭输入法键盘
                            ComFun.closeIME(UserPermissionEditActivity.this, etUserInfoName);
                            // 显示加载动画
                            ComFun.showLoading(UserPermissionEditActivity.this, "人员数据提交中，请稍后");
                            // 异步任务提交数据
                            new AddNewUserTask(UserPermissionEditActivity.this, etUserInfoName.getText().toString(), selectUserGroupId,
                                    selectUserSexId, etUserInfoAge.getText().toString(),
                                    etUserInfoBirthday.getText().toString(), etUserInfoPhone.getText().toString()).executeOnExecutor(Executors.newCachedThreadPool());
                        }
                    }
                });
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
                case MSG_INIT_USER:
                    // 隐藏加载动画
                    String initUserResult = b.getString("initUserResult");
                    if (initUserResult.equals("true")) {
                        // 添加人员数据
                        if(b.containsKey("userType0")){
                            String userType0 = b.getString("userType0");
                            userItemMap.put("userItemChild0", userType0);
                        }
                        if(b.containsKey("userType1")){
                            String userType1 = b.getString("userType1");
                            userItemMap.put("userItemChild1", userType1);
                        }
                        if(b.containsKey("userType2")){
                            String userType2 = b.getString("userType2");
                            userItemMap.put("userItemChild2", userType2);
                        }
                        if(b.containsKey("userType3")){
                            String userType3 = b.getString("userType3");
                            userItemMap.put("userItemChild3", userType3);
                        }
                        if(b.containsKey("userType4")){
                            String userType4 = b.getString("userType4");
                            userItemMap.put("userItemChild4", userType4);
                        }
                        initUserView();
                    }else if (initUserResult.equals("false")) {
                        ComFun.showToast(UserPermissionEditActivity.this, "获取人员信息失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (initUserResult.equals("time_out")) {
                        ComFun.showToast(UserPermissionEditActivity.this, "获取人员信息超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_ADD_USER:
                    // 隐藏加载动画
                    ComFun.hideLoading(UserPermissionEditActivity.this);
                    String addUserResult = b.getString("addUserResult");
                    if (addUserResult.equals("true")) {
                        // 添加人员数据
                        ComFun.showToast(UserPermissionEditActivity.this, "添加人员成功", Toast.LENGTH_SHORT);
                        String userInfoGroupId = b.getString("userInfoGroupId");
                        String userId = b.getString("userId");
                        String userInfoName = b.getString("userInfoName");
                        String userInfoAge = b.getString("userInfoAge");
                        String userInfoSexId = b.getString("userInfoSexId");
                        String userInfoBirthday = b.getString("userInfoBirthday");
                        String userInfoPhone = b.getString("userInfoPhone");
                        if(userItemMap.containsKey("userItemChild"+userInfoGroupId)){
                            userItemMap.put("userItemChild"+userInfoGroupId, userItemMap.get("userItemChild"+userInfoGroupId)+","+
                                    userId+"#&#"+userInfoName+"#&#"+(ComFun.strNull(userInfoAge)?userInfoAge:"-")+"#&#"+
                                    userInfoSexId+"#&#"+(ComFun.strNull(userInfoBirthday)?userInfoBirthday:"-")+"#&#"+
                                    (ComFun.strNull(userInfoPhone)?userInfoPhone:"-"));
                        }else{
                            userItemMap.put("userItemChild"+userInfoGroupId, userId+"#&#"+userInfoName+"#&#"+
                                    (ComFun.strNull(userInfoAge)?userInfoAge:"-")+"#&#"+
                                    userInfoSexId+"#&#"+(ComFun.strNull(userInfoBirthday)?userInfoBirthday:"-")+"#&#"+
                                    (ComFun.strNull(userInfoPhone)?userInfoPhone:"-"));
                        }
                        initUserView();
                    }else if (addUserResult.equals("false")) {
                        ComFun.showToast(UserPermissionEditActivity.this, "添加人员失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (addUserResult.equals("time_out")) {
                        ComFun.showToast(UserPermissionEditActivity.this, "添加人员超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_DELETE_USER:
                    // 隐藏加载动画
                    ComFun.hideLoading(UserPermissionEditActivity.this);
                    String deleteUserResult = b.getString("deleteUserResult");
                    if (deleteUserResult.equals("true")) {
                        // 删除人员数据
                        ComFun.showToast(UserPermissionEditActivity.this, "删除人员成功", Toast.LENGTH_SHORT);
                        String userGroupId = b.getString("userGroupId");
                        String userInfoId = b.getString("userInfoId");
                        if(userItemMap.containsKey("userItemChild"+userGroupId)){
                            String userDel = userItemMap.get("userItemChild"+userGroupId);
                            if(ComFun.strNull(userDel)){
                                StringBuilder userSb = new StringBuilder("");
                                for(String userIn : userDel.split(",")){
                                    if(!userIn.split("#&#")[0].equals(userInfoId)){
                                        userSb.append(userIn);
                                        userSb.append(",");
                                    }
                                }
                                if(ComFun.strNull(userSb.toString())){
                                    userItemMap.put("userItemChild"+userGroupId, userSb.toString().substring(0, userSb.toString().length() - 1));
                                }
                            }
                        }
                        initUserView();
                    }else if (deleteUserResult.equals("false")) {
                        ComFun.showToast(UserPermissionEditActivity.this, "添加人员失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (deleteUserResult.equals("time_out")) {
                        ComFun.showToast(UserPermissionEditActivity.this, "添加人员超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
