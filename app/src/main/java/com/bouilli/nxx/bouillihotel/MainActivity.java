package com.bouilli.nxx.bouillihotel;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.CheckVersionTask;
import com.bouilli.nxx.bouillihotel.customview.HorizontalProgressbarWithProgress;
import com.bouilli.nxx.bouillihotel.customview.NavigationTabBar;
import com.bouilli.nxx.bouillihotel.customview.NoSlideViewPager;
import com.bouilli.nxx.bouillihotel.fragment.adapter.FragmentPageAdapter;
import com.bouilli.nxx.bouillihotel.service.PollingService;
import com.bouilli.nxx.bouillihotel.service.PrintService;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.MyTagHandler;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static Handler mHandler = null;
    public static final int MSG_CHECK_NEW_VERSION = 1;
    public static final int MSG_SEE_TABLE_INFO = 2;
    public static final int MSG_SEE_TABLE_INFO_LOADING = 3;
    private Toolbar toolbar;
    private FloatingActionButton new_order = null;// 添加新订单悬浮按钮
    private FloatingActionButton message_info = null;// 查看订单信息悬浮按钮

    private long exitTime;

    private NoSlideViewPager viewPager;
    private FragmentPageAdapter mAdapter;

    private ImageView userHeadImgView;
    private TextView login_user_name;
    private TextView login_user_permission;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTabBar();

        mHandler = new MainActivity.mHandler();
        threadList = new ArrayList<>();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                Intent outOrderIntent = new Intent(MainActivity.this, OutOrderActivity.class);
                startActivity(outOrderIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

        String userLoginName = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userLoginName");
        if(ComFun.strNull(userLoginName)){
            login_user_name.setText(userLoginName);
        }else{
            login_user_name.setText("蜜糖丶小妖");
        }
        if(ComFun.strNull(userPermission)){
            if(Integer.parseInt(userPermission) == 0){
                login_user_permission.setText("系统管理员");
                // 超级管理员
                navigationView.inflateMenu(R.menu.activity_main_drawer_manager);
            }else if(Integer.parseInt(userPermission) == 1){
                login_user_permission.setText("副管理员");
                // 普通管理员
                navigationView.inflateMenu(R.menu.activity_main_drawer_transfer);
            }else if(Integer.parseInt(userPermission) == 2){
                login_user_permission.setText("员工");
                navigationView.inflateMenu(R.menu.activity_main_drawer);
            }else if(Integer.parseInt(userPermission) == 3){
                login_user_permission.setText("传菜员");
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
                // 直接跳转至订单流水页面，并释放该页面
                Intent orderRecordDataIntent = new Intent(MainActivity.this, OrderRecordActivity.class);
                MainActivity.this.startActivity(orderRecordDataIntent);
                MainActivity.this.finish();
            }
        }else{
            login_user_permission.setText("测试账号");
            navigationView.inflateMenu(R.menu.activity_main_drawer);
        }
        // 初始化显示版本号（在检查更新后面括弧显示）
        try{
            String currentVersionName = ComFun.getVersionName(MainActivity.this);
            if(ComFun.strNull(currentVersionName)){
                navigationView.getMenu().findItem(R.id.nav_update).setTitle(navigationView.getMenu().findItem(R.id.nav_update).getTitle() + "（当前版本：V"+ currentVersionName +"）");
            }
        }catch (Exception e){}
        // 设置默认选中项
        if(Integer.parseInt(userPermission) != 4){
            navigationView.getMenu().getItem(0).setChecked(true);
            navigationView.setNavigationItemSelectedListener(this);

            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }

        // 开启轮询服务
        Intent refDataServiceIntent = new Intent(MainActivity.this, PollingService.class);
        refDataServiceIntent.setAction(PollingService.ACTION);
        refDataServiceIntent.setPackage(getPackageName());
        MainActivity.this.startService(refDataServiceIntent);
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
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

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
            new CheckVersionTask(MainActivity.this).executeOnExecutor(Executors.newCachedThreadPool());
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

                            String userPermission = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliProInfo", "userPermission");
                            if(Integer.parseInt(userPermission) == 3){
                                if(!ComFun.isServiceRunning(MainActivity.this, "com.bouilli.nxx.bouillihotel.service.PrintService")){
                                    ComFun.showToast(MainActivity.this, "退出打印服务", Toast.LENGTH_SHORT);
                                }else{
                                    ComFun.showToast(MainActivity.this, "打印服务仍然运行中", Toast.LENGTH_SHORT);
                                }
                            }
                            NotificationManager mNotificationManager = (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.cancel(123456789);

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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(navigationView != null && navigationView.isShown()){
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawers();
            }else{
                if(viewPager != null){
                    // 记录当前点餐区域选项卡索引
                    int currentViewPagerIndex = viewPager.getCurrentItem();
                    SharedPreferencesTool.addOrUpdate(MainActivity.this, "BouilliProInfo", "lastMainPageIndex", currentViewPagerIndex);
                }
                if (System.currentTimeMillis() - exitTime > 2000) {
                    ComFun.showToast(this, "再按一次离开", 2000);
                    exitTime = System.currentTimeMillis();
                } else {
                    System.exit(0);
                }
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
                case MSG_CHECK_NEW_VERSION:
                    // 隐藏加载动画
                    ComFun.hideLoading(MainActivity.this);
                    String checkNewVersionResult = b.getString("checkNewVersionResult");
                    if (checkNewVersionResult.equals("true")) {
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
                    }else if (checkNewVersionResult.equals("false")) {
                        ComFun.showToast(MainActivity.this, "检查更新失败，请联系管理员", Toast.LENGTH_SHORT);
                    }else if (checkNewVersionResult.equals("time_out")) {
                        ComFun.showToast(MainActivity.this, "检查更新超时，请稍后重试", Toast.LENGTH_SHORT);
                    }else if (checkNewVersionResult.equals("none")) {
                        ComFun.showToast(MainActivity.this, "当前已经是最新的版本啦", Toast.LENGTH_SHORT);
                    }
                    break;
                case MSG_SEE_TABLE_INFO_LOADING:
                    // 显示加载动画
                    ComFun.showLoading(MainActivity.this, "正在获取餐桌数据，请稍后", true);
                    break;
                case MSG_SEE_TABLE_INFO:
                    // 隐藏加载动画
                    ComFun.hideLoading(MainActivity.this);
                    String getTableOrderInfoResult = b.getString("getTableOrderInfoResult");
                    if (getTableOrderInfoResult.equals("true")) {
                        String tableNum = b.getString("tableNum");
                        StringBuilder tableReadyOrderSb = new StringBuilder("餐桌【" + tableNum + "】\n\n");
                        String orderInfoDetails = b.getString("orderInfoDetails");
                        for(String orderInfo : orderInfoDetails.split(",")){
                            BigDecimal price = new BigDecimal(orderInfo.split("\\|")[0].split("#&#")[4]);
                            int buyNum = Integer.parseInt(orderInfo.split("\\|")[1]);
                            double totalMoneyUnit = ComFun.add(0.0, price.multiply(new BigDecimal(buyNum)));
                            if(orderInfo.split("\\|")[2].equals("-")){
                                tableReadyOrderSb.append("【" + orderInfo.split("\\|")[0].split("#&#")[2] + "】购买" + orderInfo.split("\\|")[1] + "份 ---------------- "+ totalMoneyUnit +" 元");
                            }else{
                                tableReadyOrderSb.append("【" + orderInfo.split("\\|")[0].split("#&#")[2] + "】购买" + orderInfo.split("\\|")[1] + "份（" + orderInfo.split("\\|")[2] + "） ---------------- "+ totalMoneyUnit +" 元");
                            }
                            tableReadyOrderSb.append("\n");
                            // orderInfo.split("\\|")[0].split("#&#")[0],
                            // new Object[]{ orderInfo.split("\\|")[0], orderInfo.split("\\|")[1], orderInfo.split("\\|")[2] });
                            // 键：菜品id，值：[菜品信息(菜id #&# 菜组id #&# 菜名称 #&# 菜描述 #&# 菜单价 #&# 菜被点次数), 点餐数量, 备注信息]
                        }
                        Snackbar snackbar = SnackbarUtil.IndefiniteSnackbar(message_info, "", -2, Color.parseColor("#FAFAFA"), Color.parseColor("#FF6868"));
                        View add_view = LayoutInflater.from(snackbar.getView().getContext()).inflate(R.layout.see_table_order_info, null);
                        ((TextView) add_view.findViewById(R.id.seeTableInfoTv)).setText(tableReadyOrderSb.toString());
                        SnackbarUtil.SnackbarAddView(snackbar, add_view, 0);
                        snackbar.show();
                    }else if (getTableOrderInfoResult.equals("false")) {
                        ComFun.showToast(MainActivity.this, "获取餐桌信息失败", Toast.LENGTH_SHORT);
                    }else {
                        ComFun.showToast(MainActivity.this, "获取餐桌信息超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
            }
            super.handleMessage(msg);
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
}
