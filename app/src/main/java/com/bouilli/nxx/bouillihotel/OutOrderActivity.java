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
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.customview.NavigationTabBar;
import com.bouilli.nxx.bouillihotel.customview.NoSlideViewPager;
import com.bouilli.nxx.bouillihotel.fragment.adapter.OutFragmentPageAdapter;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SnackbarUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                    Snackbar snackbar = SnackbarUtil.IndefiniteSnackbar(message_info, "", -2, Color.parseColor("#FAFAFA"), Color.parseColor("#FFD3D3"));
                    View add_view = LayoutInflater.from(snackbar.getView().getContext()).inflate(R.layout.see_table_order_info, null);
                    LinearLayout tableDetailMainLayout = (LinearLayout) add_view.findViewById(R.id.tableDetailMainLayout);
                    tableDetailMainLayout.removeAllViews();
                    int index = 0;
                    for(Map.Entry<String, List<String[]>> map : tableInfoMap.entrySet()){
                        index++;
                        TextView tvTitle = new TextView(OutOrderActivity.this);
                        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        TextPaint tvTitleTp = tvTitle.getPaint();
                        tvTitleTp.setFakeBoldText(true);
                        LinearLayout.LayoutParams tvTitleLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        tvTitleLp.setMargins(DisplayUtil.dip2px(OutOrderActivity.this, 0), DisplayUtil.dip2px(OutOrderActivity.this, 0), DisplayUtil.dip2px(OutOrderActivity.this, 0), DisplayUtil.dip2px(OutOrderActivity.this, 10));
                        tvTitle.setLayoutParams(tvTitleLp);
                        tvTitle.setText(map.getKey());
                        tableDetailMainLayout.addView(tvTitle);

                        for(String[] detailArr : map.getValue()){
                            LinearLayout tableDetailItemLayout = new LinearLayout(OutOrderActivity.this);
                            tableDetailItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                            LinearLayout.LayoutParams tableDetailItemLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            tableDetailItemLayout.setLayoutParams(tableDetailItemLayoutLp);

                            TextView tvContent = new TextView(OutOrderActivity.this);
                            LinearLayout.LayoutParams tvContentLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(OutOrderActivity.this, 0), LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                            tvContent.setLayoutParams(tvContentLp);
                            tvContent.setText(detailArr[0]);
                            tableDetailItemLayout.addView(tvContent);

                            TextView tvBuyCount = new TextView(OutOrderActivity.this);
                            tvBuyCount.setGravity(Gravity.END);
                            LinearLayout.LayoutParams tvBuyCountLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(OutOrderActivity.this, 0), LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                            tvBuyCount.setLayoutParams(tvBuyCountLp);
                            tvBuyCount.setText(detailArr[1]);
                            tableDetailItemLayout.addView(tvBuyCount);

                            tableDetailMainLayout.addView(tableDetailItemLayout);
                        }

                        if(index < tableInfoMap.size()){
                            // 添加分割线
                            View splitView = new View(OutOrderActivity.this);
                            splitView.setBackgroundColor(Color.parseColor("#b7b7b7"));
                            LinearLayout.LayoutParams splitViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(OutOrderActivity.this, 1));
                            splitViewLp.setMargins(DisplayUtil.dip2px(OutOrderActivity.this, 0), DisplayUtil.dip2px(OutOrderActivity.this, 16), DisplayUtil.dip2px(OutOrderActivity.this, 0), DisplayUtil.dip2px(OutOrderActivity.this, 16));
                            splitView.setLayoutParams(splitViewLp);
                            tableDetailMainLayout.addView(splitView);
                        }

                    }
                    SnackbarUtil.SnackbarAddView(snackbar, add_view, 0);
                    snackbar.show();
                    break;
            }
            super.handleMessage(msg);
        }
    }

}
