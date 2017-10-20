package com.bouilli.nxx.bouillihotel;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ant.liao.GifView;
import com.bouilli.nxx.bouillihotel.asyncTask.okHttpTask.AllRequestUtil;
import com.bouilli.nxx.bouillihotel.customview.ElasticScrollView;
import com.bouilli.nxx.bouillihotel.customview.HorizontalProgressbarWithProgress;
import com.bouilli.nxx.bouillihotel.customview.NavigationTabBar;
import com.bouilli.nxx.bouillihotel.customview.NoSlideViewPager;
import com.bouilli.nxx.bouillihotel.fragment.adapter.FragmentPageAdapter;
import com.bouilli.nxx.bouillihotel.okHttpUtil.request.RequestParams;
import com.bouilli.nxx.bouillihotel.push.org.androidpn.client.NotificationService;
import com.bouilli.nxx.bouillihotel.push.org.androidpn.client.ServiceManager;
import com.bouilli.nxx.bouillihotel.service.PollingService;
import com.bouilli.nxx.bouillihotel.service.PrintService;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.DateFormatUtil;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.L;
import com.bouilli.nxx.bouillihotel.util.MyTagHandler;
import com.bouilli.nxx.bouillihotel.util.PropertiesUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;
import com.bouilli.nxx.bouillihotel.util.SnackbarUtil;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static Handler mHandler = null;
    public static final int MSG_CHECK_NEW_VERSION = 1;
    public static final int MSG_SEE_TABLE_INFO = 2;
    public static final int MSG_SEE_TABLE_INFO_LOADING = 3;
    public static final int REQUEST_READ_PHONE_STATE = 4;
    public static final int MSG_PUSH_CONNECTION_LOADING = 5;
    public static final int MSG_INIT_DATA_ERROR = 6;
    public static final int MSG_SEND_CHAT_SUCCESS = 7;
    private Toolbar toolbar;
    private FloatingActionButton new_order = null;// 添加新订单悬浮按钮
    private FloatingActionButton message_info = null;// 查看订单信息悬浮按钮

    private Snackbar seeTableInfoSnackbar = null;// 非员工预览餐桌信息

    private long exitTime;

    private NoSlideViewPager viewPager;
    private FragmentPageAdapter mAdapter;

    private ImageView userHeadImgView;
    private TextView login_user_name;
    private TextView login_user_permission;

    private ImageView chat_user_head;
    private TextView chat_user_name;
    private TextView chat_user_desc;
    private ElasticScrollView chatScrollView;
    private EditText chat_input_content;
    private Button chat_input_send;
    private ImageView on_line_state;
    private LinearLayout chatDoLayout;
    private Button clearChatHistory;

    private LinearLayout mainTopTipLayout;

    private NavigationView navigationView;

    public static Map<String, Map<String, Object[]>> editBookMap = new HashMap<>();

    private AlertDialog versionDialog;
    private HorizontalProgressbarWithProgress downloadProgressBar;
    private int downloadTotal = 0;
    private boolean downloading = false;
    private URL url;
    private File file;
    private List<HashMap<String, Integer>> threadList;
    private int length;

    Handler downloadHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 0){
                downloadProgressBar.setProgress(msg.arg1);
                if(msg.arg1 >= length){
                    if(versionDialog != null && versionDialog.isShowing()){
                        versionDialog.dismiss();
                    }
                    ComFun.installApk(MainActivity.this, file);
                }
            }
            return false;
        }
    });

    // 网络请求广播
    private GetDataReceiver getDataReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTabBar();

        mHandler = new MainActivity.mHandler();
        threadList = new ArrayList<>();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 动态注册请求数据广播
        getDataReceiver = new GetDataReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.MSG_GET_DATA_SUCCESS);
        intentFilter.addAction(Constants.MSG_GET_DATA_FAIL);
        intentFilter.addAction(Constants.MSG_GET_NEW_CHAT_MSG);
        intentFilter.addAction(Constants.MSG_GET_NEW_CHAT_TIP);
        intentFilter.addAction(Constants.MSG_SEND_CHAT_SUCCESS);
        intentFilter.addAction(Constants.MSG_SEND_CHAT_FAIL);
        registerReceiver(getDataReceiver, intentFilter);

        new_order = (FloatingActionButton) findViewById(R.id.new_order);
        message_info = (FloatingActionButton) findViewById(R.id.message_info);
        // 获取登录人的权限值
        String userPermission = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userPermission");
        if(!(ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 2)){
            new_order.setVisibility(View.GONE);
            //message_info.setVisibility(View.GONE);
        }

        new_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(seeTableInfoSnackbar != null && seeTableInfoSnackbar.isShown()){
                    seeTableInfoSnackbar.dismiss();
                }
                // 添加新单
                // ObjectAnimator.ofFloat(view, "alpha", 1).setDuration(200).start();
                // 跳转到添加外卖或者打包饭页面
                Intent intentKongXian = new Intent(MainActivity.this, EditOrderActivity.class);
                intentKongXian.putExtra("showType", -10);
                intentKongXian.putExtra("tableNum", "-1");
                startActivity(intentKongXian);
            }
        });

        message_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(seeTableInfoSnackbar != null && seeTableInfoSnackbar.isShown()){
                    seeTableInfoSnackbar.dismiss();
                }
                Intent outOrderIntent = new Intent(MainActivity.this, OutOrderActivity.class);
                startActivity(outOrderIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                if(drawerView.getId() == R.id.nav_view) {
                    login_user_name.requestFocus();
                }
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);

        userHeadImgView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.user_head_img_view);
        // 设置用户头像--异步下载网络图片，并保存（默认显示之前缓存图片）

        // 设置头像点击事件-->跳转至用户详情信息
        userHeadImgView.setClickable(true);
        userHeadImgView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent userDetailIntent = new Intent(MainActivity.this, UserDetailActivity.class);
                startActivity(userDetailIntent);
            }
        });
        login_user_name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.login_user_name);
        login_user_permission = (TextView) navigationView.getHeaderView(0).findViewById(R.id.login_user_permission);

        updateChatContent();
        chat_user_name = (TextView) findViewById(R.id.chat_user_name);
        chat_user_desc = (TextView) findViewById(R.id.chat_user_desc);

        chatScrollView = (ElasticScrollView) findViewById(R.id.chatScrollView);

        chat_input_content = (EditText) findViewById(R.id.chat_input_content);
        chatDoLayout = (LinearLayout) findViewById(R.id.chatDoLayout);
        chatDoLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                chat_input_content.requestFocus();
            }
        });
        chat_input_send = (Button) findViewById(R.id.chat_input_send);
        chat_input_send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                on_line_state = (ImageView) MainActivity.this.findViewById(R.id.on_line_state); // 在线状态
                //if(on_line_state.getVisibility() == View.VISIBLE){
                    if(ComFun.strNull(chat_input_content.getText().toString().trim())){
                        String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userId");
                        String userLoginName = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userLoginName");
                        String userRealName = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userRealName");
                        String sendUserName = userLoginName;
                        if(ComFun.strNull(userRealName)){
                            sendUserName = userLoginName + "（" + userRealName + "）";
                        }
                        String sendingRandomId = ComFun.getRandomIntByTime();
                        if(on_line_state.getVisibility() != View.VISIBLE){
                            // 已掉线
                            ComFun.showToast(MainActivity.this, "当前已掉线，请稍后重试", Toast.LENGTH_SHORT);
                            SharedPreferencesTool.addOrUpdateChatPro(MainActivity.this, SharedPreferencesTool.CHAT_PRO_NAME, userId, userId + "&||&" + sendUserName + "&||&" + DateFormatUtil.dateToStr(new Date(), DateFormatUtil.TYPE_) + "&||&" + chat_input_content.getText().toString().trim() + "&||&" + sendingRandomId + "&||&error");
                            // 发送消息发送失败广播
                            Intent sendChatFailIntent = new Intent();
                            sendChatFailIntent.putExtra("sendingRandomId", sendingRandomId);
                            sendChatFailIntent.putExtra("sendingContent", chat_input_content.getText().toString().trim());
                            sendChatFailIntent.setAction(Constants.MSG_SEND_CHAT_FAIL);
                            MyApplication.getInstance().sendBroadcast(sendChatFailIntent);
                        }else{
                            createMsgLayout(sendUserName, DateFormatUtil.dateToStr(new Date(), DateFormatUtil.TYPE_), chat_input_content.getText().toString().trim(), 4, sendingRandomId);
                            // 滚动聊天区域内容至最下
                            Handler msgContentHandler = new Handler();
                            msgContentHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                            // 执行发送消息任务
                            RequestParams requestParams = new RequestParams();
                            requestParams.put("userId", userId);
                            requestParams.put("messageContent", chat_input_content.getText().toString().trim());
                            requestParams.put("sendingRandomId", sendingRandomId);
                            AllRequestUtil.SendChatMsg(MainActivity.this, requestParams, sendingRandomId);
                        }
                        chat_input_content.setText("");
                    }else{
                        ComFun.showToast(MainActivity.this, "发送内容不能为空", Toast.LENGTH_SHORT);
                        chat_input_content.setText("");
                        // 滚动聊天区域内容至最下
                        Handler msgContentHandler = new Handler();
                        msgContentHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }
                //}else{
                    // 已掉线
                    /*ComFun.showToast(MainActivity.this, "已掉线，请稍后再发送消息", Toast.LENGTH_SHORT);
                    // 滚动聊天区域内容至最下
                    Handler msgContentHandler = new Handler();
                    msgContentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });*/
                //}
            }
        });
        clearChatHistory = (Button) findViewById(R.id.clearChatHistory);
        clearChatHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userId");
                SharedPreferencesTool.getMsgListFromShared(MainActivity.this, SharedPreferencesTool.CHAT_PRO_NAME, userId);
                SharedPreferencesTool.deleteFromShared(MainActivity.this, SharedPreferencesTool.CHAT_PRO_NAME, userId);
                updateChatContent();
            }
        });

        String userLoginName = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userLoginName");
        if(ComFun.strNull(userLoginName)){
            login_user_name.setText(userLoginName);
            chat_user_name.setText(userLoginName);
        }else{
            login_user_name.setText("蜜糖丶小妖 [ 想想光头强，想想灰太狼，人生的道路上没有理由不坚强 ]");
            chat_user_name.setText("蜜糖丶小妖 [ 想想光头强，想想灰太狼，人生的道路上没有理由不坚强 ]");
        }
        if(ComFun.strNull(userPermission)){
            if(Integer.parseInt(userPermission) == 0){
                login_user_permission.setText("系统管理员");
                chat_user_desc.setText("系统管理员");
                // 超级管理员
                navigationView.inflateMenu(R.menu.activity_main_drawer_manager);
            }else if(Integer.parseInt(userPermission) == 1){
                login_user_permission.setText("副管理员");
                chat_user_desc.setText("副管理员");
                // 普通管理员
                navigationView.inflateMenu(R.menu.activity_main_drawer_transfer);
            }else if(Integer.parseInt(userPermission) == 2){
                login_user_permission.setText("员工");
                chat_user_desc.setText("员工");
                navigationView.inflateMenu(R.menu.activity_main_drawer);
            }else if(Integer.parseInt(userPermission) == 3){
                login_user_permission.setText("传菜员");
                chat_user_desc.setText("传菜员");
                navigationView.inflateMenu(R.menu.activity_main_printer);
                // 判断打印设备是否开启,开启则为用户检测蓝牙连接是否正常
                boolean printUseVol = SharedPreferencesTool.getBooleanFromShared(MainActivity.this, "BouilliSetInfo", "printUseVol");
                if(printUseVol){
                    final String printAddress = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "printAddress");
                    if(ComFun.strNull(printAddress)){
                        // 开始检测
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

                                    ComFun.showToast(MainActivity.this, "创建蓝牙连接成功", Toast.LENGTH_LONG);
                                } catch (Exception e) {
                                    ComFun.showToast(MainActivity.this, "对不起，创建蓝牙连接失败，请检查您的设备是否支持蓝牙功能或者打票机是否正确配对", Toast.LENGTH_LONG);
                                }
                            }
                        }, 20);
                    }else{
                        ComFun.showToast(MainActivity.this, "打票机区域未设置，请从菜单【打票机设置】处开启", Toast.LENGTH_SHORT);
                    }
                }else{
                    ComFun.showToast(MainActivity.this, "打票机未启用，请从菜单【打票机设置】处开启", Toast.LENGTH_SHORT);
                }
                if(!ComFun.isServiceRunning(MainActivity.this, "com.bouilli.nxx.bouillihotel.service.PrintService")){
                    // 开启打印轮询服务
                    //ComFun.showToast(MainActivity.this, "启动打印服务", Toast.LENGTH_SHORT);
                    Intent printServiceIntent = new Intent(MainActivity.this, PrintService.class);
                    printServiceIntent.setAction(PrintService.ACTION);
                    printServiceIntent.setPackage(getPackageName());
                    MainActivity.this.startService(printServiceIntent);
                }else{
                    //ComFun.showToast(MainActivity.this, "打印服务运行中", Toast.LENGTH_SHORT);
                }
            }else if(Integer.parseInt(userPermission) == 4){
                // login_user_permission.setText("后厨管理员");
                // chat_user_desc.setText("后厨管理员");
                // 直接跳转至订单流水页面，并释放该页面
                Intent orderRecordDataIntent = new Intent(MainActivity.this, OrderRecordActivity.class);
                MainActivity.this.startActivity(orderRecordDataIntent);
                MainActivity.this.finish();
            }
        }else{
            login_user_permission.setText("DEBUG 登录");
            chat_user_desc.setText("DEBUG 登录");
            navigationView.inflateMenu(R.menu.activity_main_drawer_manager);
        }
        // 初始化显示版本号（在检查更新后面括弧显示）
        try{
            String currentVersionName = ComFun.getVersionName(MainActivity.this);
            if(ComFun.strNull(currentVersionName)){
                ComFun.showToast(MainActivity.this, "当前版本：" + currentVersionName, Toast.LENGTH_SHORT);
                navigationView.getMenu().findItem(R.id.nav_update).setTitle(navigationView.getMenu().findItem(R.id.nav_update).getTitle() + "（当前版本：V"+ currentVersionName +"）");
            }
        }catch (Exception e){}
        // 设置默认选中项
        if(ComFun.strNull(userPermission) && Integer.parseInt(userPermission) != 4){
            navigationView.getMenu().getItem(0).setChecked(true);

            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }
        navigationView.setNavigationItemSelectedListener(this);

        // 动态权限申请
        applyPermission();

        // 开启轮询服务
        Intent refDataServiceIntent = new Intent(MainActivity.this, PollingService.class);
        refDataServiceIntent.setAction(PollingService.ACTION);
        refDataServiceIntent.setPackage(getPackageName());
        MainActivity.this.startService(refDataServiceIntent);

        String openPushServer = PropertiesUtil.getPropertiesURL("bouilli.prop", MainActivity.this, "openPushServer");
        // 开启推送服务
        if(openPushServer.equals("1") && !ComFun.isServiceRunning(MainActivity.this, "com.bouilli.nxx.bouillihotel.push." + NotificationService.SERVICE_NAME)){
            // 发送首页广播，提示连接推送服务器中...
            //Message msg = new Message();
            //Bundle data = new Bundle();
            //msg.what = MainActivity.MSG_PUSH_CONNECTION_LOADING;
            //data.putString("pushConnectionType", "loading");
            //msg.setData(data);
            //MainActivity.mHandler.sendMessage(msg);

            ServiceManager serviceManager = new ServiceManager(MainActivity.this);
            serviceManager.setNotificationIcon(R.drawable.cholesterol);
            serviceManager.startService();
        }
    }

    private void applyPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;
            default:
                break;
        }
    }

    // 初始化选项卡
    private void initTabBar(){
        mainTopTipLayout = (LinearLayout) MainActivity.this.findViewById(R.id.mainTopTipLayout);
        mainTopTipLayout.getChildAt(0).requestFocus();
        String userPermission = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userPermission");
        if(ComFun.strNull(userPermission)){
            if(Integer.parseInt(userPermission) == 0){
                ((TextView) mainTopTipLayout.getChildAt(0)).setText("当前为系统管理员登录，不可进行餐桌点餐操作，仅可以通过下方餐桌显现形式预览当前营业状态");
            }else if(Integer.parseInt(userPermission) == 1){
                ((TextView) mainTopTipLayout.getChildAt(0)).setText("当前为普通管理员登录，不可进行餐桌点餐操作，仅可以通过下方餐桌显现形式预览当前营业状态");
            }else if(Integer.parseInt(userPermission) == 2){
                ((TextView) mainTopTipLayout.getChildAt(0)).setText("请选择下方的餐桌进行点餐操作；餐桌分三种不同的显现形式（一、空闲   二、就餐中   三、草稿状态），其中草稿状态为暂存的餐桌点餐信息，可以选择继续使用草稿数据或者不使用");
            }else if(Integer.parseInt(userPermission) == 3){
                ((TextView) mainTopTipLayout.getChildAt(0)).setText("当前为传菜员登录，不可进行餐桌点餐操作，仅可以通过下方餐桌显现形式预览当前营业状态");
            }else if(Integer.parseInt(userPermission) == 4){
                ((TextView) mainTopTipLayout.getChildAt(0)).setText("后厨管理员的相关通知");
            }
        }else{
            ((TextView) mainTopTipLayout.getChildAt(0)).setText("测试账号的首页顶层通知信息");
        }
        String tableGroupNames = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliTableInfo", "tableGroupNames");
        viewPager = (NoSlideViewPager) findViewById(R.id.mainViewPager);
        viewPager.setScanScroll(false);
        FragmentManager fm = getSupportFragmentManager();
        mAdapter = new FragmentPageAdapter(fm, tableGroupNames.split(",").length);
        viewPager.setAdapter(mAdapter);

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.mainTabBar);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        if(ComFun.strNull(tableGroupNames)){
            for(int i=0; i<tableGroupNames.split(",").length; i++){
                NavigationTabBar.Model itemModel = new NavigationTabBar.Model.Builder(getResources().getDrawable(R.drawable.none), Color.parseColor(colors[i]))
                        .title(tableGroupNames.split(",")[i]).build();
                models.add(itemModel);
            }
        }else{
            NavigationTabBar.Model itemModel = new NavigationTabBar.Model.Builder(getResources().getDrawable(R.drawable.none), Color.parseColor(colors[0]))
                    .title("暂无餐桌（请先添加餐桌数据）").build();
            models.add(itemModel);
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
            }
        });

        // 初始化之前最后一次退出时的选项卡
        int lastViewPagerIndex = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "lastMainPageIndex", 0);
        if(models.size() > lastViewPagerIndex){
            viewPager.setCurrentItem(lastViewPagerIndex);
            //navigationTabBar.setModelIndex(lastViewPagerIndex + 1);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.chat_menu) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.openDrawer(GravityCompat.END);
            drawer.findViewById(R.id.chat_user_name).requestFocus();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        ComFun.showToast(MainActivity.this, "选择菜单项：" + id, Toast.LENGTH_SHORT);

        if (id == R.id.nav_main) {
            // 显示点菜主页
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_business_report) {
            // 显示营业信息报表
            Intent businessIntent = new Intent(MainActivity.this, BusinessActivity.class);
            startActivity(businessIntent);
        } else if (id == R.id.nav_performance_assess) {
            // 显示员工个人业绩考核
            Intent performanceIntent = new Intent(MainActivity.this, PerformanceAssessActivity.class);
            startActivity(performanceIntent);
        } else if (id == R.id.nav_data) {
            // 显示数据统计
            Intent editTableIntent = new Intent(MainActivity.this, OrderRecordActivity.class);
            startActivity(editTableIntent);
        } else if (id == R.id.nav_print_error_pool) {
            // 未打印成功的小票回收站
            Intent printErrorPoolIntent = new Intent(MainActivity.this, PrintErrorPoolActivity.class);
            startActivity(printErrorPoolIntent);
        } else if (id == R.id.nav_print_set) {
            // 跳转到打印机设置页面
            Intent setPrintIntent = new Intent(MainActivity.this, SetPrintActivity.class);
            startActivity(setPrintIntent);
        } else if (id == R.id.nav_desk) {
            // 跳转到编辑餐桌页面
            Intent editTableIntent = new Intent(MainActivity.this, TableEditActivity.class);
            startActivity(editTableIntent);
        } else if (id == R.id.nav_menu) {
            // 跳转到编辑菜单页面
            Intent menuEditActivity = new Intent(MainActivity.this, MenuEditActivity.class);
            startActivity(menuEditActivity);
        } else if (id == R.id.nav_permission_set) {
            // 跳转到编辑员工账号权限管理页面
            Intent menuEditActivity = new Intent(MainActivity.this, UserPermissionEditActivity.class);
            startActivity(menuEditActivity);
        } else if (id == R.id.nav_ofen_remark_set) {
            // 跳转到常用备注设置页面

        } else if (id == R.id.nav_about) {
            // 跳转到关于页面
            Intent aboutActivity = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutActivity);
        }else if(id == R.id.nav_update){
            // 检查更新
            // 直接执行任务，检查是否有版本更新
            // 显示加载动画
            ComFun.showLoading(MainActivity.this, "正在检查是否有新版本，请稍后");
            AllRequestUtil.CheckVersion(MainActivity.this, null);
        } else if (id == R.id.nav_exit) {
            // 退出登录
            new android.support.v7.app.AlertDialog.Builder(MainActivity.this).setTitle("提示").setMessage("确定退出登录吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 检测打印服务是否运行，运行则停止
                            Intent printServiceIntent = new Intent(MainActivity.this, PrintService.class);
                            printServiceIntent.setAction(PrintService.ACTION);
                            printServiceIntent.setPackage(getPackageName());
                            MainActivity.this.stopService(printServiceIntent);
                            // 检测数据刷新轮询服务是否运行，运行则停止
                            Intent refDataServiceIntent = new Intent(MainActivity.this, PollingService.class);
                            refDataServiceIntent.setAction(PollingService.ACTION);
                            refDataServiceIntent.setPackage(getPackageName());
                            MainActivity.this.stopService(refDataServiceIntent);
                            // 关闭推送服务
                            ServiceManager serviceManager = new ServiceManager(MainActivity.this);
                            serviceManager.stopService();

                            String userPermission = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userPermission");
                            if(Integer.parseInt(userPermission) == 3){
                                if(!ComFun.isServiceRunning(MainActivity.this, "com.bouilli.nxx.bouillihotel.service.PrintService")){
                                    ComFun.showToast(MainActivity.this, "退出打印服务", Toast.LENGTH_SHORT);
                                }else{
                                    ComFun.showToast(MainActivity.this, "打印服务仍然运行中", Toast.LENGTH_SHORT);
                                }
                            }
                            NotificationManager mNotificationManager = (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.cancel(PrintService.NOTIFY_ID);

                            // 删除打票机设置信息
                            //SharedPreferencesTool.deleteFromShared(MainActivity.this, "BouilliSetInfo", "printUseVol");
                            //SharedPreferencesTool.deleteFromShared(MainActivity.this, "BouilliProInfo", "printAreaId");
                            //SharedPreferencesTool.deleteFromShared(MainActivity.this, "BouilliProInfo", "printAddress");

                            SharedPreferencesTool.addOrUpdate(MainActivity.this, "BouilliProInfo", "hasExitLast", "true");

                            Intent welcomeIntent = new Intent(MainActivity.this, WelcomeActivity.class);
                            startActivity(welcomeIntent);
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("取消", null).show();
        }
        return true;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止轮询服务
        Intent refDataServiceIntent = new Intent(MainActivity.this, PollingService.class);
        refDataServiceIntent.setAction(PollingService.ACTION);
        refDataServiceIntent.setPackage(getPackageName());
        MainActivity.this.stopService(refDataServiceIntent);
        // 解注册数据请求广播
        unregisterReceiver(getDataReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else if (drawer.isDrawerOpen(GravityCompat.END)) {
                drawer.closeDrawer(GravityCompat.END);
            } else if(seeTableInfoSnackbar != null && seeTableInfoSnackbar.isShown()){
                seeTableInfoSnackbar.dismiss();
            } else {
                if(viewPager != null){
                    // 记录当前点餐区域选项卡索引
                    int currentViewPagerIndex = viewPager.getCurrentItem();
                    SharedPreferencesTool.addOrUpdate(MainActivity.this, "BouilliProInfo", "lastMainPageIndex", currentViewPagerIndex);
                }
                if (System.currentTimeMillis() - exitTime > 2000) {
                    ComFun.showToast(this, "再按一次离开", 2000);
                    exitTime = System.currentTimeMillis();
                } else {
                    // 关闭推送服务
                    //ServiceManager serviceManager = new ServiceManager(MainActivity.this);
                    //serviceManager.stopService();
                    if(ComFun.isServiceRunning(MainActivity.this, "com.bouilli.nxx.bouillihotel.push." + NotificationService.SERVICE_NAME)){
                        MainActivity.this.finish();
                    }else{
                        System.exit(0);
                    }
                }
            }
        }
        return true;
    }

    AlertDialog pushConnectionDialog = null;
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
                case MSG_CHECK_NEW_VERSION:
                    boolean hasNewVersionFlag = false;
                    int lastVersionNo = b.getInt("lastVersionNo");
                    String lastVersionName = b.getString("lastVersionName");
                    int currentVersionNo;
                    String currentVersionName = "";
                    try {
                        currentVersionNo = ComFun.getVersionNo(MainActivity.this);
                        currentVersionName = ComFun.getVersionName(MainActivity.this);
                        if(lastVersionNo > currentVersionNo){
                            // 有新版本
                            hasNewVersionFlag = true;
                        }
                    } catch (Exception e) {}
                    if(hasNewVersionFlag){
                        String lastVersionContent = b.getString("lastVersionContent");
                        // 弹框显示新版本详细内容
                        new android.support.v7.app.AlertDialog.Builder(MainActivity.this).setTitle("发现新版本").setMessage(
                                "当前版本：V." + currentVersionName + "   最新版本：" + lastVersionName + "\n\n更新内容：\n" + Html.fromHtml(lastVersionContent, null, new MyTagHandler(MainActivity.this)) +"\n\n确定下载更新吗？")
                                .setPositiveButton("下载更新", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startToDownloadNewVersion();
                                    }
                                })
                                .setNegativeButton("取消", null).show();
                    }else{
                        ComFun.showToast(MainActivity.this, "当前已经是最新的版本啦", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_SEE_TABLE_INFO_LOADING:
                    // 显示加载动画
                    String seeTableInfoType = b.getString("seeTableInfoType");
                    if(seeTableInfoType.equals("loading")){
                        ComFun.showLoading(MainActivity.this, "正在获取餐桌数据，请稍后", true);
                    }else{
                        if(seeTableInfoSnackbar != null && seeTableInfoSnackbar.isShown()){
                            seeTableInfoSnackbar.dismiss();
                        }
                    }
                    break;
                case MSG_SEE_TABLE_INFO:
                    Map<String, List<String[]>> tableInfoMap = new TreeMap<>();
                    String tableNum = b.getString("tableNum");
                    String orderInfoDetails = b.getString("orderInfoDetails");
                    String[] orderInfoDetailArr = orderInfoDetails.split("\\|\\|#\\|#\\|#\\|\\|");
                    if(orderInfoDetailArr.length > 1){
                        for(int i = 1; i <= orderInfoDetailArr.length; i++){
                            List<String[]> detailList = new ArrayList<>();

                            for(String orderInfo : orderInfoDetailArr[i - 1].split(",")){
                                BigDecimal price = new BigDecimal(orderInfo.split("\\|")[0].split("#&#")[4]);
                                int buyNum = Integer.parseInt(orderInfo.split("\\|")[1]);
                                double totalMoneyUnit = ComFun.add(0.0, price.multiply(new BigDecimal(buyNum)));
                                if(orderInfo.split("\\|")[2].equals("-") || !ComFun.strNull(ComFun.formatMenuDetailInfo3(orderInfo.split("\\|")[2]))){
                                    detailList.add(new String[]{ "【" + orderInfo.split("\\|")[0].split("#&#")[2] + "】购买" + orderInfo.split("\\|")[1] + "份", totalMoneyUnit +" 元" });
                                }else{
                                    detailList.add(new String[]{ "【" + orderInfo.split("\\|")[0].split("#&#")[2] + "】购买" + orderInfo.split("\\|")[1] + "份（" + ComFun.formatMenuDetailInfo3(orderInfo.split("\\|")[2]) + "）", totalMoneyUnit +" 元" });
                                }
                                // orderInfo.split("\\|")[0].split("#&#")[0],
                                // new Object[]{ orderInfo.split("\\|")[0], orderInfo.split("\\|")[1], orderInfo.split("\\|")[2] });
                                // 键：菜品id，值：[菜品信息(菜id #&# 菜组id #&# 菜名称 #&# 菜描述 #&# 菜单价 #&# 菜被点次数), 点餐数量, 备注信息]
                            }

                            tableInfoMap.put("餐桌【" + tableNum + "】( 子订单 " + i + " )", detailList);
                        }
                    }else{
                        List<String[]> detailList = new ArrayList<>();

                        for(String orderInfo : orderInfoDetailArr[0].split(",")){
                            BigDecimal price = new BigDecimal(orderInfo.split("\\|")[0].split("#&#")[4]);
                            int buyNum = Integer.parseInt(orderInfo.split("\\|")[1]);
                            double totalMoneyUnit = ComFun.add(0.0, price.multiply(new BigDecimal(buyNum)));
                            if(orderInfo.split("\\|")[2].equals("-") || !ComFun.strNull(ComFun.formatMenuDetailInfo3(orderInfo.split("\\|")[2]))){
                                detailList.add(new String[]{ "【" + orderInfo.split("\\|")[0].split("#&#")[2] + "】购买" + orderInfo.split("\\|")[1] + "份", totalMoneyUnit +" 元" });
                            }else{
                                detailList.add(new String[]{ "【" + orderInfo.split("\\|")[0].split("#&#")[2] + "】购买" + orderInfo.split("\\|")[1] + "份（" + ComFun.formatMenuDetailInfo3(orderInfo.split("\\|")[2]) + "）", totalMoneyUnit +" 元" });
                            }
                            // orderInfo.split("\\|")[0].split("#&#")[0],
                            // new Object[]{ orderInfo.split("\\|")[0], orderInfo.split("\\|")[1], orderInfo.split("\\|")[2] });
                            // 键：菜品id，值：[菜品信息(菜id #&# 菜组id #&# 菜名称 #&# 菜描述 #&# 菜单价 #&# 菜被点次数), 点餐数量, 备注信息]
                        }

                        tableInfoMap.put("餐桌【" + tableNum + "】", detailList);
                    }
                    seeTableInfoSnackbar = SnackbarUtil.IndefiniteSnackbar(message_info, "", -2, Color.parseColor("#FAFAFA"), Color.parseColor("#FFD3D3"));
                    View add_view = LayoutInflater.from(seeTableInfoSnackbar.getView().getContext()).inflate(R.layout.see_table_order_info, null);
                    LinearLayout tableDetailMainLayout = (LinearLayout) add_view.findViewById(R.id.tableDetailMainLayout);
                    tableDetailMainLayout.removeAllViews();
                    int index = 0;
                    for(Map.Entry<String, List<String[]>> map : tableInfoMap.entrySet()){
                        index++;
                        TextView tvTitle = new TextView(MainActivity.this);
                        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        TextPaint tvTitleTp = tvTitle.getPaint();
                        tvTitleTp.setFakeBoldText(true);
                        LinearLayout.LayoutParams tvTitleLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        tvTitleLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 10));
                        tvTitle.setLayoutParams(tvTitleLp);
                        tvTitle.setText(map.getKey());
                        tableDetailMainLayout.addView(tvTitle);

                        for(String[] detailArr : map.getValue()){
                            LinearLayout tableDetailItemLayout = new LinearLayout(MainActivity.this);
                            tableDetailItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                            LinearLayout.LayoutParams tableDetailItemLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            tableDetailItemLayout.setLayoutParams(tableDetailItemLayoutLp);

                            TextView tvContent = new TextView(MainActivity.this);
                            LinearLayout.LayoutParams tvContentLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MainActivity.this, 0), LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                            tvContent.setLayoutParams(tvContentLp);
                            tvContent.setText(detailArr[0]);
                            tableDetailItemLayout.addView(tvContent);

                            TextView tvBuyCount = new TextView(MainActivity.this);
                            tvBuyCount.setGravity(Gravity.END);
                            LinearLayout.LayoutParams tvBuyCountLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MainActivity.this, 0), LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                            tvBuyCount.setLayoutParams(tvBuyCountLp);
                            tvBuyCount.setText(detailArr[1]);
                            tableDetailItemLayout.addView(tvBuyCount);

                            tableDetailMainLayout.addView(tableDetailItemLayout);
                        }

                        if(index < tableInfoMap.size()){
                            // 添加分割线
                            View splitView = new View(MainActivity.this);
                            splitView.setBackgroundColor(Color.parseColor("#b7b7b7"));
                            LinearLayout.LayoutParams splitViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(MainActivity.this, 1));
                            splitViewLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 16), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 16));
                            splitView.setLayoutParams(splitViewLp);
                            tableDetailMainLayout.addView(splitView);
                        }

                    }
                    SnackbarUtil.SnackbarAddView(seeTableInfoSnackbar, add_view, 0);
                    seeTableInfoSnackbar.show();
                    break;
                case MSG_PUSH_CONNECTION_LOADING:
                    // 显示加载动画
                    String pushConnectionType = b.getString("pushConnectionType");
                    if(pushConnectionType.equals("loading")){
                        if(pushConnectionDialog == null || !pushConnectionDialog.isShowing()){
                            setChatOnlineState(false);
                            pushConnectionDialog = ComFun.showLoading(MainActivity.this, "正在连接推送服务...", true);
                        }
                    }else if(pushConnectionType.equals("connectionSuccess")){
                        if(pushConnectionDialog != null && pushConnectionDialog.isShowing()){
                            pushConnectionDialog.dismiss();
                        }
                        setChatOnlineState(true);
                    }else if(pushConnectionType.equals("connectionPushTimeOut")){
                        if(pushConnectionDialog != null && pushConnectionDialog.isShowing()){
                            pushConnectionDialog.dismiss();
                            setChatOnlineState(false);
                            ComFun.showToast(MainActivity.this, "连接推送服务超时", Toast.LENGTH_SHORT);
                        }
                    }
                    break;
                case MSG_INIT_DATA_ERROR:
                    // 显示加载动画
                    String okHttpE = b.getString("okHttpE");
                    L.toast(MainActivity.this, okHttpE, Toast.LENGTH_SHORT);
                    break;
                case MSG_SEND_CHAT_SUCCESS:
                    String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userId");
                    String userName = b.getString("userName");
                    String sendDateTime = b.getString("sendDateTime");
                    String sendMsgContent = b.getString("sendMsgContent");
                    String sendingRandomId = b.getString("sendingRandomId");
                    SharedPreferencesTool.addOrUpdateChatPro(MainActivity.this, SharedPreferencesTool.CHAT_PRO_NAME, userId, userId + "&||&" + userName + "&||&" + sendDateTime + "&||&" + sendMsgContent + "&||&" + sendingRandomId + "&||&success");

                    Intent getNewMsgIntent = new Intent();
                    getNewMsgIntent.setAction(Constants.MSG_SEND_CHAT_SUCCESS);
                    MyApplication.getInstance().sendBroadcast(getNewMsgIntent);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    // 设置聊天窗在线状态
    public void setChatOnlineState(boolean flag){
        ImageView on_line_state = (ImageView) MainActivity.this.findViewById(R.id.on_line_state); // 在线状态
        ImageView off_line_state = (ImageView) MainActivity.this.findViewById(R.id.off_line_state); // 离线状态
        on_line_state.setVisibility(View.GONE);
        off_line_state.setVisibility(View.GONE);
        EditText chat_input_content = (EditText) MainActivity.this.findViewById(R.id.chat_input_content);
        if(flag){
            on_line_state.setVisibility(View.VISIBLE);
            //chat_input_content.setEnabled(true);
            //chat_input_content.setHint("输入聊天消息");
        }else{
            off_line_state.setVisibility(View.VISIBLE);
            //chat_input_content.setEnabled(false);
            //chat_input_content.setText("");
            //chat_input_content.setHint("当前已掉线");
        }
    }

    // 更新聊天区域内容
    public void updateChatContent(){
        final ElasticScrollView chatScrollView = (ElasticScrollView) MainActivity.this.findViewById(R.id.chatScrollView);
        LinearLayout chatScrollViewMain = (LinearLayout) MainActivity.this.findViewById(R.id.chatScrollViewMain);
        chatScrollViewMain.removeAllViews();
        String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userId");
        List<String> msgContentList = SharedPreferencesTool.getMsgListFromShared(MainActivity.this, SharedPreferencesTool.CHAT_PRO_NAME, userId);
        msgContentList = ComFun.disposeMsgTime(msgContentList);
        if(ComFun.strNull(msgContentList) && msgContentList.size() > 0){
            for(String msgContent : msgContentList){
                // 发送人Id、发送人名称、发送时间、发送内容
                String[] msgContentArr = msgContent.split("&\\|\\|&");
                if(msgContentArr.length > 1){
                    if(msgContentArr[5].equals("error")){
                        // 发送错误的消息，也是本人消息
                        createMsgLayout(msgContentArr[1], msgContentArr[2], msgContentArr[3], 5, msgContentArr[4]);
                    }else{
                        if(msgContentArr[0].equals(userId)){
                            // 本人消息
                            createMsgLayout(msgContentArr[1], msgContentArr[2], msgContentArr[3], 1);
                        }else{
                            createMsgLayout(msgContentArr[1], msgContentArr[2], msgContentArr[3], 2);
                        }
                    }
                }else{
                    createMsgLayout(null, null, msgContentArr[0], 3);
                }
            }
            // 滚动聊天区域内容至最下
            Handler msgContentHandler = new Handler();
            msgContentHandler.post(new Runnable() {
                @Override
                public void run() {
                    chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }

    // 更新聊天框区域顶部提示信息
    public void updateChatTopTip(String tipContent, final int duration){
        final LinearLayout msgTipLayout = (LinearLayout) MainActivity.this.findViewById(R.id.msgTipLayout);

        final TextView tipView = new TextView(MainActivity.this);
        LinearLayout.LayoutParams tipViewLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tipView.setLayoutParams(tipViewLp);
        tipView.setPadding(DisplayUtil.dip2px(MainActivity.this, 15), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 15), DisplayUtil.dip2px(MainActivity.this, 10));
        tipView.setBackgroundColor(Color.parseColor("#d2ffffff"));
        tipView.setTextColor(Color.parseColor("#1e1e1e"));
        tipView.setSingleLine(true);
        tipView.setText(tipContent);
        tipView.setVisibility(View.INVISIBLE);

        msgTipLayout.addView(tipView, 0);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final int curTipTvHeight = tipView.getMeasuredHeight();

                // 添加显示动画
                ObjectAnimator animator = ObjectAnimator.ofInt(tipView, "height", 0, curTipTvHeight);
                animator.setDuration(300);
                final TimeInterpolator decelerateInterpolator = new DecelerateInterpolator();
                final TimeInterpolator accelerateInterpolator = new AccelerateInterpolator();
                final TimeInterpolator linearOutSlowInInterpolator = new LinearOutSlowInInterpolator();
                final TimeInterpolator linearInterpolator = new LinearInterpolator();
                animator.setInterpolator(decelerateInterpolator);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        tipView.setVisibility(View.VISIBLE);
                    }
                });
                animator.start();
                if(duration != -1){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ObjectAnimator animator = ObjectAnimator.ofInt(tipView, "height", 0);
                            animator.setDuration(300);
                            //new DecelerateInterpolator();
                            //new AccelerateInterpolator();
                            //new LinearOutSlowInInterpolator();
                            animator.setInterpolator(decelerateInterpolator);
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    msgTipLayout.removeView(tipView);
                                }
                            });
                            animator.start();
                        }
                    }, duration);
                }
            }
        });
    }

    /**
     * 创建聊天区域聊天会话
     * @param sendUserName
     * @param sendTime
     * @param msgContent
     * @param type
     * @param sendingRandomId
     */
    public void createMsgLayout(String sendUserName, String sendTime, String msgContent, int type, final String... sendingRandomId){
        final LinearLayout chatScrollViewMain = (LinearLayout) MainActivity.this.findViewById(R.id.chatScrollViewMain);
        View addViewLayout = null;
        if(type == 1){
            // 本人消息
            addViewLayout = new LinearLayout(MainActivity.this);
            ((LinearLayout) addViewLayout).setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams addViewLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addViewLayoutLp.gravity = Gravity.RIGHT;
            addViewLayoutLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 50), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 8));
            addViewLayout.setLayoutParams(addViewLayoutLp);

            TextView msgContentTv = new TextView(MainActivity.this);
            LinearLayout.LayoutParams msgContentTvLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            msgContentTv.setLayoutParams(msgContentTvLp);
            msgContentTv.setBackgroundResource(R.drawable.bg_round_key_boxes_focus);
            msgContentTv.setPadding(DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10));
            msgContentTv.setTextColor(Color.parseColor("#262626"));
            msgContentTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            msgContentTv.setText(msgContent);

            ((LinearLayout) addViewLayout).addView(msgContentTv);
        }else if(type == 2){
            // 别人消息
            addViewLayout = new LinearLayout(MainActivity.this);
            ((LinearLayout) addViewLayout).setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams addViewLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addViewLayoutLp.gravity = Gravity.LEFT;
            addViewLayoutLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 50), DisplayUtil.dip2px(MainActivity.this, 8));
            addViewLayout.setLayoutParams(addViewLayoutLp);

            TextView msgSendFromTv = new TextView(MainActivity.this);
            TextPaint msgSendFromTvTp = msgSendFromTv.getPaint();
            msgSendFromTvTp.setFakeBoldText(true);
            LinearLayout.LayoutParams msgSendFromTvLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            msgSendFromTvLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 3));
            msgSendFromTv.setLayoutParams(msgSendFromTvLp);
            msgSendFromTv.setText(sendUserName + "：");

            ((LinearLayout) addViewLayout).addView(msgSendFromTv);

            TextView msgContentTv = new TextView(MainActivity.this);
            LinearLayout.LayoutParams msgContentTvLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            msgContentTv.setLayoutParams(msgContentTvLp);
            msgContentTv.setBackgroundResource(R.drawable.bg_round_key_boxes_del_focus);
            msgContentTv.setPadding(DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10));
            msgContentTv.setTextColor(Color.parseColor("#262626"));
            msgContentTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            msgContentTv.setText(msgContent);

            ((LinearLayout) addViewLayout).addView(msgContentTv);
        }else if(type == 3){
            // 时间戳
            addViewLayout = new TextView(MainActivity.this);
            LinearLayout.LayoutParams addViewLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addViewLayoutLp.gravity = Gravity.CENTER;
            addViewLayoutLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 10));
            addViewLayout.setLayoutParams(addViewLayoutLp);
            addViewLayout.setBackgroundResource(R.drawable.bg_msg_time_round_circle);
            addViewLayout.setPadding(DisplayUtil.dip2px(MainActivity.this, 4), DisplayUtil.dip2px(MainActivity.this, 4), DisplayUtil.dip2px(MainActivity.this, 4), DisplayUtil.dip2px(MainActivity.this, 4));
            ((TextView) addViewLayout).setTextColor(Color.parseColor("#dedede"));
            ((TextView) addViewLayout).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            ((TextView) addViewLayout).setText(msgContent);
        }else if(type == 4){
            // 正在发送的消息，发送状态
            addViewLayout = new LinearLayout(MainActivity.this);
            ((LinearLayout) addViewLayout).setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams addViewLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addViewLayoutLp.gravity = Gravity.RIGHT;
            addViewLayoutLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 50), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 8));
            addViewLayout.setLayoutParams(addViewLayoutLp);
            if(sendingRandomId != null && sendingRandomId.length > 0){
                addViewLayout.setTag("sending_" + sendingRandomId[0]);
            }

            GifView sendingImg = new GifView(MainActivity.this);
            LinearLayout.LayoutParams sendingImgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sendingImgLp.gravity = Gravity.CENTER;
            sendingImgLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 4), DisplayUtil.dip2px(MainActivity.this, 0));
            sendingImg.setLayoutParams(sendingImgLp);
            sendingImg.setGifImage(R.drawable.loading);
            sendingImg.setShowDimension(64, 64);
            sendingImg.setGifImageType(GifView.GifImageType.COVER);
            ((LinearLayout) addViewLayout).addView(sendingImg);

            ImageView sendErrorImg = new ImageView(MainActivity.this);
            LinearLayout.LayoutParams sendErrorImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MainActivity.this, 28), DisplayUtil.dip2px(MainActivity.this, 30));
            sendErrorImgLp.gravity = Gravity.CENTER;
            sendErrorImgLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 4), DisplayUtil.dip2px(MainActivity.this, 0));
            sendErrorImg.setLayoutParams(sendErrorImgLp);
            sendErrorImg.setImageResource(R.drawable.send_error);
            sendErrorImg.setVisibility(View.GONE);
            ((LinearLayout) addViewLayout).addView(sendErrorImg);

            TextView msgContentTv = new TextView(MainActivity.this);
            LinearLayout.LayoutParams msgContentTvLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            msgContentTv.setLayoutParams(msgContentTvLp);
            msgContentTv.setBackgroundResource(R.drawable.bg_round_key_boxes_focus);
            msgContentTv.setPadding(DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10));
            msgContentTv.setTextColor(Color.parseColor("#262626"));
            msgContentTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            msgContentTv.setText(msgContent);

            ((LinearLayout) addViewLayout).addView(msgContentTv);
        }else if(type == 5){
            // 发送失败的消息，发送状态
            addViewLayout = new LinearLayout(MainActivity.this);
            ((LinearLayout) addViewLayout).setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams addViewLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addViewLayoutLp.gravity = Gravity.RIGHT;
            addViewLayoutLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 50), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 8));
            addViewLayout.setLayoutParams(addViewLayoutLp);
            if(sendingRandomId != null && sendingRandomId.length > 0){
                addViewLayout.setTag("sending_" + sendingRandomId[0]);
            }

            GifView sendingImg = new GifView(MainActivity.this);
            LinearLayout.LayoutParams sendingImgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sendingImgLp.gravity = Gravity.CENTER;
            sendingImgLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 4), DisplayUtil.dip2px(MainActivity.this, 0));
            sendingImg.setLayoutParams(sendingImgLp);
            sendingImg.setGifImage(R.drawable.loading);
            sendingImg.setShowDimension(64, 64);
            sendingImg.setGifImageType(GifView.GifImageType.COVER);
            sendingImg.setVisibility(View.GONE);
            ((LinearLayout) addViewLayout).addView(sendingImg);

            ImageView sendErrorImg = new ImageView(MainActivity.this);
            LinearLayout.LayoutParams sendErrorImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(MainActivity.this, 28), DisplayUtil.dip2px(MainActivity.this, 30));
            sendErrorImgLp.gravity = Gravity.CENTER;
            sendErrorImgLp.setMargins(DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 0), DisplayUtil.dip2px(MainActivity.this, 4), DisplayUtil.dip2px(MainActivity.this, 0));
            sendErrorImg.setLayoutParams(sendErrorImgLp);
            sendErrorImg.setImageResource(R.drawable.send_error);
            ((LinearLayout) addViewLayout).addView(sendErrorImg);

            TextView msgContentTv = new TextView(MainActivity.this);
            LinearLayout.LayoutParams msgContentTvLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            msgContentTv.setLayoutParams(msgContentTvLp);
            msgContentTv.setBackgroundResource(R.drawable.bg_round_key_boxes_focus);
            msgContentTv.setPadding(DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10), DisplayUtil.dip2px(MainActivity.this, 10));
            msgContentTv.setTextColor(Color.parseColor("#262626"));
            msgContentTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            msgContentTv.setText(msgContent);

            ((LinearLayout) addViewLayout).addView(msgContentTv);
            addViewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    on_line_state = (ImageView) MainActivity.this.findViewById(R.id.on_line_state); // 在线状态
                    if(on_line_state.getVisibility() != View.VISIBLE){
                        // 已掉线
                        ComFun.showToast(MainActivity.this, "当前已掉线，请稍后重试", Toast.LENGTH_SHORT);
                    }else{
                        String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userId");
                        String userLoginName = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userLoginName");
                        String userRealName = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userRealName");
                        String sendUserName = userLoginName;
                        if(ComFun.strNull(userRealName)){
                            sendUserName = userLoginName + "（" + userRealName + "）";
                        }
                        String sendErrorContent = ((TextView) ((LinearLayout) v).getChildAt(2)).getText().toString();
                        chatScrollViewMain.removeView(v);
                        // 根据错误消息的随机数id值，进行内存删除
                        SharedPreferencesTool.deleteErrorChatPro(MainActivity.this, SharedPreferencesTool.CHAT_PRO_NAME, userId, sendingRandomId[0]);

                        String sendingRandomId = ComFun.getRandomIntByTime();
                        createMsgLayout(sendUserName, DateFormatUtil.dateToStr(new Date(), DateFormatUtil.TYPE_), sendErrorContent, 4, sendingRandomId);
                        // 滚动聊天区域内容至最下
                        // 滚动聊天区域内容至最下
                        Handler msgContentHandler = new Handler();
                        msgContentHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                        // 执行发送消息任务
                        RequestParams requestParams = new RequestParams();
                        requestParams.put("userId", userId);
                        requestParams.put("messageContent", sendErrorContent);
                        requestParams.put("sendingRandomId", sendingRandomId);
                        AllRequestUtil.SendChatMsg(MainActivity.this, requestParams, sendingRandomId);
                    }
                }
            });
        }
        if(addViewLayout != null){
            chatScrollViewMain.addView(addViewLayout);
        }
    }

    private void startToDownloadNewVersion(){
        downloading = true;
        // 弹框下载新版本
        versionDialog = new AlertDialog.Builder(MainActivity.this).setCancelable(false).create();
        versionDialog.show();
        Window win = versionDialog.getWindow();
        View downloadVersionView = MainActivity.this.getLayoutInflater().inflate(R.layout.download_version_dialog, null);
        win.setContentView(downloadVersionView);
        LinearLayout downloadVersionMain = (LinearLayout) downloadVersionView.findViewById(R.id.downloadVersionMain);
        downloadVersionMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(downloading){
                    downloading = false;
                    ComFun.showToast(MainActivity.this, "下载已暂停", Toast.LENGTH_SHORT);
                }else{
                    downloading = true;
                    ComFun.showToast(MainActivity.this, "继续开始下载", Toast.LENGTH_SHORT);
                    // 恢复下载
                    beginDownload();
                }
            }
        });
        downloadProgressBar = (HorizontalProgressbarWithProgress) downloadVersionView.findViewById(R.id.downloadProgressBar);
        // 开启线程下载
        beginDownload();
    }

    private void beginDownload(){
        if(threadList.size() == 0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        url = new URL("http://139.224.25.220/BouilliHotelServer/download/bouilli.apk");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
                        length = conn.getContentLength();

                        downloadProgressBar.setMax(length);// 按照百分比现实进度
                        downloadProgressBar.setProgress(0);

                        if(length < 0){
                            ComFun.showToast(MainActivity.this, "文件不存在", Toast.LENGTH_SHORT);
                            return;
                        }
                        file = new File(Environment.getExternalStorageDirectory(), "bouilli.apk");
                        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                        randomAccessFile.setLength(length);

                        int blockSize = length / 3;
                        for(int i=0; i<3; i++){
                            int begin = i * blockSize;
                            int end = (i + 1) * blockSize;
                            if(i == 2){
                                end = length;
                            }
                            HashMap<String, Integer> map = new HashMap<>();
                            map.put("begin", begin);
                            map.put("end", end);
                            map.put("finished", 0);
                            threadList.add(map);
                            // 创建新的线程，下载文件
                            Thread t = new Thread(new MainActivity.DownloadRunnable(i, begin, end, file, url));
                            t.start();
                        }

                    } catch (MalformedURLException e) {
                        ComFun.showToast(MainActivity.this, "URL 不正确！", Toast.LENGTH_SHORT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }else{
            // 恢复下载
            for(int i=0; i<threadList.size(); i++){
                HashMap<String, Integer> map = threadList.get(i);
                int begin = map.get("begin");
                int end = map.get("end");
                int finished = map.get("finished");
                Thread t = new Thread(new MainActivity.DownloadRunnable(i, begin + finished, end, file, url));
                t.start();
            }
        }
    }

    class DownloadRunnable implements Runnable {
        private int begin;
        private int end;
        private File file;
        private URL url;
        private int id;

        public DownloadRunnable(int id, int begin, int end, File file, URL url){
            this.begin = begin;
            this.end = end;
            this.file = file;
            this.url = url;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                if(begin > end){
                    return;
                }
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
                conn.setRequestProperty("Range", "bytes=" + begin + "-" + end);

                InputStream is = conn.getInputStream();
                byte[] buf = new byte[1024 * 1024];
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(begin);
                int len;
                HashMap<String, Integer> map = threadList.get(id);
                while((len = is.read(buf)) != -1 && downloading){
                    randomAccessFile.write(buf, 0, len);
                    updateProgress(len);
                    map.put("finished", map.get("finished") + len);
                }
                is.close();
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    synchronized private void updateProgress(int add){
        downloadTotal += add;
        downloadHandler.obtainMessage(0, downloadTotal, 0).sendToTarget();
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
            if(v.getId() == R.id.chat_input_content){
                LinearLayout chatDoLayout = (LinearLayout) v.getParent();
                int[] leftTop = { 0, 0 };
                //获取输入框当前的location位置
                chatDoLayout.getLocationInWindow(leftTop);
                int left = leftTop[0];
                int top = leftTop[1];
                int bottom = top + chatDoLayout.getHeight();
                int right = left + chatDoLayout.getWidth();
                if (event.getX() > left && event.getX() < right
                        && event.getY() > top && event.getY() < bottom) {
                    // 点击的是输入框区域，保留点击EditText的事件
                    return false;
                } else {
                    v.clearFocus();
                    return true;
                }
            }
        }
        return false;
    }

    public class GetDataReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(intent.getAction().equals(Constants.MSG_GET_DATA_SUCCESS)){
                setChatOnlineState(true);
            }else if(intent.getAction().equals(Constants.MSG_GET_DATA_FAIL)){
                setChatOnlineState(false);
            }else if(intent.getAction().equals(Constants.MSG_GET_NEW_CHAT_MSG)){
                // 解析缓存中所有消息
                updateChatContent();
            }else if(intent.getAction().equals(Constants.MSG_GET_NEW_CHAT_TIP)){
                //updateChatTopTip(chat_input_content.getText().toString().trim(), 6000);
            }else if(intent.getAction().equals(Constants.MSG_SEND_CHAT_SUCCESS)){
                // 解析缓存中所有消息
                updateChatContent();
            }else if(intent.getAction().equals(Constants.MSG_SEND_CHAT_FAIL)){
                final String sendingRandomId = bundle.getString("sendingRandomId");
                final LinearLayout chatScrollViewMain = (LinearLayout) MainActivity.this.findViewById(R.id.chatScrollViewMain);
                String userLoginName = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userLoginName");
                String userRealName = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userRealName");
                String sendUserName = userLoginName;
                if(ComFun.strNull(userRealName)){
                    sendUserName = userLoginName + "（" + userRealName + "）";
                }
                if(chatScrollViewMain.findViewWithTag("sending_" + sendingRandomId) != null){
                    final LinearLayout errorChatLayout = (LinearLayout) chatScrollViewMain.findViewWithTag("sending_" + sendingRandomId);
                    errorChatLayout.getChildAt(0).setVisibility(View.GONE);
                    errorChatLayout.getChildAt(1).setVisibility(View.VISIBLE);

                    final String sendContent = ((TextView) errorChatLayout.getChildAt(2)).getText().toString();
                    final String userId = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userId");
                    final String sendUserNameFin = sendUserName;
                    SharedPreferencesTool.addOrUpdateChatPro(MainActivity.this, SharedPreferencesTool.CHAT_PRO_NAME, userId, userId + "&||&" + sendUserName + "&||&" + DateFormatUtil.dateToStr(new Date(), DateFormatUtil.TYPE_) + "&||&" + sendContent + "&||&" + sendingRandomId + "&||&error");

                    errorChatLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            on_line_state = (ImageView) MainActivity.this.findViewById(R.id.on_line_state); // 在线状态
                            if(on_line_state.getVisibility() != View.VISIBLE){
                                // 已掉线
                                ComFun.showToast(MainActivity.this, "当前已掉线，请稍后重试", Toast.LENGTH_SHORT);
                            }else{
                                chatScrollViewMain.removeView(errorChatLayout);
                                // 根据错误消息的随机数id值，进行内存删除
                                SharedPreferencesTool.deleteErrorChatPro(MainActivity.this, SharedPreferencesTool.CHAT_PRO_NAME, userId, sendingRandomId);

                                String sendingRandomId = ComFun.getRandomIntByTime();
                                createMsgLayout(sendUserNameFin, DateFormatUtil.dateToStr(new Date(), DateFormatUtil.TYPE_), sendContent, 4, sendingRandomId);
                                // 滚动聊天区域内容至最下
                                // 滚动聊天区域内容至最下
                                Handler msgContentHandler = new Handler();
                                msgContentHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                    }
                                });
                                // 执行发送消息任务
                                RequestParams requestParams = new RequestParams();
                                requestParams.put("userId", userId);
                                requestParams.put("messageContent", sendContent);
                                requestParams.put("sendingRandomId", sendingRandomId);
                                AllRequestUtil.SendChatMsg(MainActivity.this, requestParams, sendingRandomId);
                            }
                        }
                    });
                }else{
                    String sendingContent = bundle.getString("sendingContent");
                    createMsgLayout(sendUserName, DateFormatUtil.dateToStr(new Date(), DateFormatUtil.TYPE_), sendingContent, 5, sendingRandomId);
                    // 滚动聊天区域内容至最下
                    Handler msgContentHandler = new Handler();
                    msgContentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            }
        }
    }
}
