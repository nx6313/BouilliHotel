package com.bouilli.nxx.bouillihotel;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.broadcastReceiver.BouilliBroadcastReceiver;
import com.bouilli.nxx.bouillihotel.callBack.MsgCallBack;
import com.bouilli.nxx.bouillihotel.customview.NavigationTabBar;
import com.bouilli.nxx.bouillihotel.fragment.adapter.FragmentPageAdapter;
import com.bouilli.nxx.bouillihotel.service.PollingService;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;
import com.bouilli.nxx.bouillihotel.util.SnackbarUtil;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar;
    private FloatingActionButton new_order = null;// 添加新订单悬浮按钮
    private FloatingActionButton message_info = null;// 查看订单信息悬浮按钮
    private Snackbar skMessageInfo = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private long exitTime;

    private View mani_activity_content_main;// 点菜主页布局内容
    private View mani_activity_content_statistics;// 数据统计布局内容

    private Menu topRightMenu = null;// 页面右上角的菜单对象

    private ViewPager viewPager;
    private FragmentPageAdapter mAdapter;

    private Button btConnectionToBluePrint;// 连接蓝牙打票机的按钮
    private String selectedBlueBaseName;// 当前选中蓝牙连接设备名称
    private ScrollView orderRecordData;// 订单流水数据页面总布局容器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTabBar();

        // 初始化基本控件
        initBaseWeight();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new_order = (FloatingActionButton) findViewById(R.id.new_order);
        new_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 添加新单
                ObjectAnimator.ofFloat(view, "alpha", 1).setDuration(200).start();
            }
        });

        message_info = (FloatingActionButton) findViewById(R.id.message_info);
        message_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (skMessageInfo != null && skMessageInfo.isShown()) {
                    skMessageInfo.dismiss();
                    ObjectAnimator.ofFloat(view, "alpha", (float)0.4).setDuration(200).start();
                } else {
                    skMessageInfo = Snackbar.make(view, "订餐信息", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Action", null);
                    SnackbarUtil.setSnackbarColor(skMessageInfo, 0xfff44336, 0xffdcc208);
                    skMessageInfo.setCallback(new MsgCallBack(view));
                    skMessageInfo.show();
                    ObjectAnimator.ofFloat(view, "alpha", 1).setDuration(200).start();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        // 设置默认选中项
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        // 开启轮询服务
        Intent refDataServiceIntent = new Intent(MainActivity.this, PollingService.class);
        refDataServiceIntent.setAction(PollingService.ACTION);
        refDataServiceIntent.setPackage(getPackageName());
        MainActivity.this.startService(refDataServiceIntent);
    }

    // 初始化选项卡
    private void initTabBar(){
        String tableGroupNames = SharedPreferencesTool.getFromShared(MainActivity.this, "BouilliTableInfo", "tableGroupNames");
        viewPager = (ViewPager) findViewById(R.id.mainViewPager);
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
                    .title("暂无餐桌").build();
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
    }

    // 初始化基本控件
    public void initBaseWeight(){
        mani_activity_content_main = findViewById(R.id.mani_activity_content_main);// 点菜主页布局内容
        mani_activity_content_statistics = findViewById(R.id.mani_activity_content_statistics);// 数据统计布局内容
        BouilliBroadcastReceiver.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btConnectionToBluePrint = (Button) findViewById(R.id.btConnectionToBluePrint);
        btConnectionToBluePrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btConnectionToBluePrint.getText().equals("打票机连接中（点击断开连接）")){
                    try {
                        BouilliBroadcastReceiver.mOutputStream.close();
                        BouilliBroadcastReceiver.mBluetoothSocket.close();
                        BouilliBroadcastReceiver.beginPrintServiceFlag = false;
                        btConnectionToBluePrint.setText("连接打票机（当前状态已断开）");
                        ComFun.showToast(MainActivity.this, "打票机服务已关闭", Toast.LENGTH_SHORT);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }else{
                    if(BouilliBroadcastReceiver.mBluetoothAdapter != null && BouilliBroadcastReceiver.mBluetoothAdapter.isEnabled()){
                        BouilliBroadcastReceiver.pairedDevices = BouilliBroadcastReceiver.mBluetoothAdapter.getBondedDevices();
                        if(BouilliBroadcastReceiver.pairedDevices.size() > 0){
                            BouilliBroadcastReceiver.mpairedDeviceList.clear();
                            for (BluetoothDevice device : BouilliBroadcastReceiver.pairedDevices) {
                                // Add the name and address to an array adapter to show in a ListView
                                String getName = device.getName() + "#" + device.getAddress();
                                BouilliBroadcastReceiver.mpairedDeviceList.add(getName);
                            }
                            // 配对的设备列表中肯定有，默认为已选择第一项
                            selectedBlueBaseName = BouilliBroadcastReceiver.mpairedDeviceList.get(0);
                            // 弹出选择蓝牙设备单选弹框
                            AlertDialog.Builder builder=new android.app.AlertDialog.Builder(MainActivity.this);
                            //设置对话框的图标
                            builder.setIcon(R.drawable.mode);
                            //设置对话框的标题
                            builder.setTitle("选择打印订单小票的打印机");
                            builder.setSingleChoiceItems(BouilliBroadcastReceiver.mpairedDeviceList.toArray(new String[BouilliBroadcastReceiver.mpairedDeviceList.size()]), 0, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedBlueBaseName = BouilliBroadcastReceiver.mpairedDeviceList.get(which);
                                }
                            });

                            //添加一个确定按钮
                            builder.setPositiveButton(" 确 定 ", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    String temString = selectedBlueBaseName.substring(selectedBlueBaseName.length()-17);
                                    try {
                                        BouilliBroadcastReceiver.mBluetoothDevice = BouilliBroadcastReceiver.mBluetoothAdapter.getRemoteDevice(temString);
                                        BouilliBroadcastReceiver.mBluetoothSocket = BouilliBroadcastReceiver.mBluetoothDevice.createRfcommSocketToServiceRecord(BouilliBroadcastReceiver.SPP_UUID);
                                        BouilliBroadcastReceiver.mBluetoothSocket.connect();
                                        BouilliBroadcastReceiver.beginPrintServiceFlag = true;
                                        btConnectionToBluePrint.setText("打票机连接中（点击断开连接）");
                                        ComFun.showToast(MainActivity.this, "打票机服务已启用", Toast.LENGTH_SHORT);
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                        btConnectionToBluePrint.setText("连接打票机（当前状态已断开）");
                                        ComFun.showToast(MainActivity.this, "您选择的设备不支持打印服务，请重新选择", Toast.LENGTH_SHORT);
                                    }
                                }
                            });
                            //添加一个取消按钮
                            builder.setNegativeButton(" 取 消 ", null);
                            //创建一个单选框对话框
                            Dialog dialog = builder.create();
                            dialog.show();
                        }else{
                            ComFun.showToast(MainActivity.this, "没有与任何设备配对连接，请先与设备配对", Toast.LENGTH_SHORT);
                        }
                    }
                }
            }
        });
        orderRecordData = (ScrollView) findViewById(R.id.orderRecordData);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        topRightMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_show_all) {
            return true;
        }else if (id == R.id.action_show_empty) {
            return true;
        }else if (id == R.id.action_show_use) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            // 显示点菜主页
            toolbar.setTitle("红烧肉点餐");
            mani_activity_content_main.setVisibility(View.VISIBLE);
            mani_activity_content_statistics.setVisibility(View.GONE);
            if(topRightMenu != null){
                topRightMenu.setGroupVisible(0, true);
            }
            new_order.setVisibility(View.VISIBLE);
            message_info.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_data) {
            // 显示数据统计
            toolbar.setTitle("订单流水信息");
            mani_activity_content_statistics.setVisibility(View.VISIBLE);
            mani_activity_content_main.setVisibility(View.GONE);
            if(topRightMenu != null){
                topRightMenu.setGroupVisible(0, false);
            }
            new_order.setVisibility(View.GONE);
            message_info.setVisibility(View.GONE);
            if (skMessageInfo != null && skMessageInfo.isShown()) {
                skMessageInfo.dismiss();
            }
        } else if (id == R.id.nav_desk) {
            // 跳转到编辑餐桌页面
            Intent editTableIntent = new Intent(MainActivity.this, TableEditActivity.class);
            startActivity(editTableIntent);
        } else if (id == R.id.nav_menu) {
            // 跳转到编辑菜单页面
            Intent menuEditActivity = new Intent(MainActivity.this, MenuEditActivity.class);
            startActivity(menuEditActivity);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
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
            if (System.currentTimeMillis() - exitTime > 2000) {
                ComFun.showToast(this, "再按一次离开", 2000);
                exitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
        }
        return true;
    }
}
