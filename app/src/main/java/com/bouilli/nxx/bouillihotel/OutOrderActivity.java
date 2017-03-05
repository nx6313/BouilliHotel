package com.bouilli.nxx.bouillihotel;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.customview.NavigationTabBar;
import com.bouilli.nxx.bouillihotel.customview.NoSlideViewPager;
import com.bouilli.nxx.bouillihotel.fragment.adapter.OutFragmentPageAdapter;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.SnackbarUtil;

import java.math.BigDecimal;
import java.util.ArrayList;

public class OutOrderActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    public static final int MSG_SEE_TABLE_INFO = 1;
    public static final int MSG_SEE_TABLE_INFO_LOADING = 2;
    private NoSlideViewPager viewPager;
    private OutFragmentPageAdapter mAdapter;

    private FloatingActionButton message_info = null;// 查看订单信息悬浮按钮（隐藏掉，用于点餐信息锚点）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initTabBar();
        mHandler = new OutOrderActivity.mHandler();

        message_info = (FloatingActionButton) findViewById(R.id.message_info);

        setupActionBar();
    }

    private void initTabBar() {
        String[] outOrderGroupNames = new String[]{ "外卖餐", "打包餐" };
        viewPager = (NoSlideViewPager) findViewById(R.id.outOrderViewPager);
        viewPager.setScanScroll(true);
        FragmentManager fm = getSupportFragmentManager();
        mAdapter = new OutFragmentPageAdapter(fm, outOrderGroupNames.length);
        viewPager.setAdapter(mAdapter);

        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.outOrderTabBar);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        for(int i=0; i<outOrderGroupNames.length; i++){
            NavigationTabBar.Model itemModel = new NavigationTabBar.Model.Builder(getResources().getDrawable(R.drawable.none), Color.parseColor(colors[i + 5]))
                    .title(outOrderGroupNames[i]).build();
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

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            OutOrderActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                case MSG_SEE_TABLE_INFO_LOADING:
                    // 显示加载动画
                    ComFun.showLoading(OutOrderActivity.this, "正在获取点餐数据，请稍后", true);
                    break;
                case MSG_SEE_TABLE_INFO:
                    // 隐藏加载动画
                    ComFun.hideLoading(OutOrderActivity.this);
                    String getTableOrderInfoResult = b.getString("getTableOrderInfoResult");
                    if (getTableOrderInfoResult.equals("true")) {
                        String tableNum = b.getString("tableNum");
                        StringBuilder tableReadyOrderSb = new StringBuilder("点餐类型【" + tableNum + "】\n\n");
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
                        ComFun.showToast(OutOrderActivity.this, "获取餐桌信息失败", Toast.LENGTH_SHORT);
                    }else {
                        ComFun.showToast(OutOrderActivity.this, "获取餐桌信息超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
