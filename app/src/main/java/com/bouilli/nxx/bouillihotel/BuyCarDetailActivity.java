package com.bouilli.nxx.bouillihotel;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bouilli.nxx.bouillihotel.customview.AmountView;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SerializableMap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BuyCarDetailActivity extends AppCompatActivity {
    private LinearLayout buyCarMainLayout;

    public static Map<String, Object[]> tableReadyOrderMap = new HashMap<>();// 保存该餐桌已点至后厨的菜品信息
    public static Map<String, Object[]> hasOrderThisTableMap = new HashMap<>();// 保存该餐桌当前未传送至后厨的新选择的菜品信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_car_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Intent toThisIntent = this.getIntent();
        String tableNum = toThisIntent.getExtras().getString("tableNum");
        if(tableNum.equals("wmTable") || tableNum.equals("dbTable")){
            if(tableNum.equals("wmTable")){
                toolbar.setTitle("外卖点菜");
            }else{
                toolbar.setTitle("打包点菜");
            }
        }else{
            toolbar.setTitle("餐桌【"+ tableNum +"】点菜");
        }
        setSupportActionBar(toolbar);

        buyCarMainLayout = (LinearLayout) findViewById(R.id.buyCarMainLayout);

        Bundle bundle = toThisIntent.getExtras();
        SerializableMap tableReadyOrderMapSer = (SerializableMap) bundle.get("tableReadyOrderMap");
        SerializableMap hasOrderThisTableMapSer = (SerializableMap) bundle.get("hasOrderThisTableMap");
        tableReadyOrderMap = tableReadyOrderMapSer.getMap();
        hasOrderThisTableMap = hasOrderThisTableMapSer.getMap();
        initView();

        setupActionBar();
    }

    // 初始化布局
    private void initView() {
        buyCarMainLayout.removeAllViews();
        // 创建详细数据部分
        double totalMoney = 0.0;
        // 未上传
        if(hasOrderThisTableMap.size() > 0){
            LinearLayout weiLayout = new LinearLayout(BuyCarDetailActivity.this);
            weiLayout.setGravity(Gravity.CENTER|Gravity.LEFT);
            weiLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams weiLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            weiLayoutLp.setMargins(0, DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), 0, 0);
            weiLayout.setLayoutParams(weiLayoutLp);

            int tableReadyOrderItemIndex = 0;
            int tableReadyOrderCount = 0;
            double totalReadyMoney = 0.0;
            for(Map.Entry<String, Object[]> tableReadyOrder : hasOrderThisTableMap.entrySet()){
                BigDecimal price = new BigDecimal(tableReadyOrder.getValue()[0].toString().split("#&#")[4]);
                int buyNum = Integer.parseInt(tableReadyOrder.getValue()[1].toString());
                BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                totalReadyMoney = ComFun.add(totalReadyMoney, thisTotalPrice);
                tableReadyOrderCount += buyNum;
                LinearLayout menuChildItemlayout = new LinearLayout(BuyCarDetailActivity.this);
                menuChildItemlayout.setPadding(DisplayUtil.dip2px(BuyCarDetailActivity.this, 20), 0, DisplayUtil.dip2px(BuyCarDetailActivity.this, 20), 0);
                if(tableReadyOrderItemIndex % 2 == 0){
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#aeb39f"));
                }else{
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#c1b0be"));
                }
                menuChildItemlayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuChildItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemlayout.setLayoutParams(menuChildItemLp);
                // 子项每一项图标
                ImageView menuChildItemImg = new ImageView(BuyCarDetailActivity.this);
                menuChildItemImg.setTag(String.valueOf(tableReadyOrder.getValue()[0]));
                menuChildItemImg.setImageResource(R.drawable.menu1);
                LinearLayout.LayoutParams menuChildItemImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(BuyCarDetailActivity.this, 45), DisplayUtil.dip2px(BuyCarDetailActivity.this, 45));
                menuChildItemImgLp.setMargins(DisplayUtil.dip2px(BuyCarDetailActivity.this, 2), DisplayUtil.dip2px(BuyCarDetailActivity.this, 2), DisplayUtil.dip2px(BuyCarDetailActivity.this, 2), DisplayUtil.dip2px(BuyCarDetailActivity.this, 2));
                menuChildItemImg.setLayoutParams(menuChildItemImgLp);
                menuChildItemlayout.addView(menuChildItemImg);
                // 子项每一项主体（名称带简要说明）
                LinearLayout menuChildItemDeslayout = new LinearLayout(BuyCarDetailActivity.this);
                menuChildItemDeslayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemDeslayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams menuChildItemDesLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(BuyCarDetailActivity.this, 0), LinearLayout.LayoutParams.MATCH_PARENT, 1);
                menuChildItemDesLp.setMargins(DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), 0, 0, 0);
                menuChildItemDeslayout.setLayoutParams(menuChildItemDesLp);
                // 主体-->菜名
                TextView menuChildItemDesNameTxt = new TextView(BuyCarDetailActivity.this);
                menuChildItemDesNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                menuChildItemDesNameTxt.setText(String.valueOf(tableReadyOrder.getValue()[0]).split("#&#")[2]);
                menuChildItemDesNameTxt.setTextColor(Color.parseColor("#000000"));
                TextPaint menuChildItemDesNameTxtTp = menuChildItemDesNameTxt.getPaint();
                menuChildItemDesNameTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesNameTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesNameTxt.setLayoutParams(menuChildItemDesNameTxtLp);
                menuChildItemDesNameTxt.setSingleLine(true);
                menuChildItemDesNameTxt.setEllipsize(TextUtils.TruncateAt.END);
                menuChildItemDeslayout.addView(menuChildItemDesNameTxt);
                // 主体-->菜简介
                TextView menuChildItemDesssTxt = new TextView(BuyCarDetailActivity.this);
                menuChildItemDesssTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                menuChildItemDesssTxt.setText("单价：" + String.valueOf(tableReadyOrder.getValue()[0]).split("#&#")[4] + " 元");
                TextPaint menuChildItemDesssTxtTp = menuChildItemDesssTxt.getPaint();
                menuChildItemDesssTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesssTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesssTxt.setLayoutParams(menuChildItemDesssTxtLp);
                menuChildItemDeslayout.addView(menuChildItemDesssTxt);
                menuChildItemlayout.addView(menuChildItemDeslayout);
                // 子项每一项单价
                LinearLayout menuChildItemPricelayout = new LinearLayout(BuyCarDetailActivity.this);
                menuChildItemPricelayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemPricelayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuChildItemPriceLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                menuChildItemPriceLp.setMargins(DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), 0, 0, 0);
                menuChildItemPricelayout.setLayoutParams(menuChildItemPriceLp);

                AmountView amountView = new AmountView(BuyCarDetailActivity.this);
                amountView.setTag("amountView_order");
                LinearLayout.LayoutParams amountViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                amountViewLp.setMargins(0, 0, DisplayUtil.dip2px(BuyCarDetailActivity.this, 10), 0);
                amountView.setLayoutParams(amountViewLp);
                amountView.setEtAmount(String.valueOf(buyNum));
                menuChildItemPricelayout.addView(amountView);

                Button orderRemoveBtn = new Button(BuyCarDetailActivity.this);
                orderRemoveBtn.setTag(tableReadyOrder.getKey());
                orderRemoveBtn.setTextColor(Color.parseColor("#e1dfdf"));
                orderRemoveBtn.setBackgroundResource(R.drawable.edit_order_btn_style_1);
                orderRemoveBtn.setPadding(0, 0, 0, 0);
                orderRemoveBtn.setText("删除");
                orderRemoveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                TextPaint orderRemoveBtnTp = orderRemoveBtn.getPaint();
                orderRemoveBtnTp.setFakeBoldText(true);
                LinearLayout.LayoutParams orderRemoveBtnLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(BuyCarDetailActivity.this, 50), DisplayUtil.dip2px(BuyCarDetailActivity.this, 30));
                orderRemoveBtn.setLayoutParams(orderRemoveBtnLp);
                orderRemoveBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        hasOrderThisTableMap.remove(v.getTag().toString());
                        initView();

                        String thisMenuInfo = ((LinearLayout) v.getParent().getParent()).getChildAt(0).getTag().toString();
                        // 发送Handler通知SelectMenuActivity页面更新UI
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("thisMenuInfo", thisMenuInfo);
                        data.putString("selectNum", "0");
                        msg.what = SelectMenuActivity.MSG_UPDATE_BUY_CAR;
                        msg.setData(data);
                        SelectMenuActivity.mHandler.sendMessage(msg);
                    }
                });
                menuChildItemPricelayout.addView(orderRemoveBtn);

                menuChildItemlayout.addView(menuChildItemPricelayout);

                weiLayout.addView(menuChildItemlayout);

                // 绑定每样菜的数字选择事件监听
                amountView.setOnAmountChangeListener(new AmountView.OnAmountChangeListener() {
                    @Override
                    public void onAmountChange(View view, int amount, int clickType) {
                        String thisMenuId = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString().split("#&#")[0];
                        String thisMenuName = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString().split("#&#")[2];
                        String thisMenuInfo = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString();
                        //ComFun.showToast(getActivity(), thisMenuName+"，已选："+amount+"个", Toast.LENGTH_SHORT);
                        if(amount > 0){
                            Object[] newOrderInfo = new Object[]{ thisMenuInfo, amount, "-" };// 菜id, 选择数量, 备注信息
                            BuyCarDetailActivity.hasOrderThisTableMap.put(thisMenuId, newOrderInfo);
                        }else{
                            if(BuyCarDetailActivity.hasOrderThisTableMap.containsKey(thisMenuId) && clickType == -1){
                                BuyCarDetailActivity.hasOrderThisTableMap.remove(thisMenuId);
                            }
                        }
                        initView();

                        // 发送Handler通知SelectMenuActivity页面更新UI
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("thisMenuInfo", thisMenuInfo);
                        data.putString("selectNum", amount+"");
                        msg.what = SelectMenuActivity.MSG_UPDATE_BUY_CAR;
                        msg.setData(data);
                        SelectMenuActivity.mHandler.sendMessage(msg);
                    }
                });

                tableReadyOrderItemIndex++;
            }
            // 添加总计条目
            TextView buyDetailTv = new TextView(BuyCarDetailActivity.this);
            buyDetailTv.setPadding(0, DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), DisplayUtil.dip2px(BuyCarDetailActivity.this, 15), DisplayUtil.dip2px(BuyCarDetailActivity.this, 8));
            buyDetailTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            buyDetailTv.setGravity(Gravity.CENTER|Gravity.RIGHT);
            buyDetailTv.setBackgroundColor(Color.parseColor("#1FB6CD"));
            buyDetailTv.setTextColor(Color.parseColor("#2E2E2E"));
            TextPaint buyDetailTvTp = buyDetailTv.getPaint();
            buyDetailTvTp.setFakeBoldText(true);
            LinearLayout.LayoutParams buyCountTvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            buyDetailTv.setLayoutParams(buyCountTvLp);
            buyDetailTv.setText("未上传部分： 共点餐 " + hasOrderThisTableMap.size() + " 道（"+ tableReadyOrderCount +" 份），总价：" + totalReadyMoney + " 元");
            weiLayout.addView(buyDetailTv);

            totalMoney = ComFun.add(totalMoney, new BigDecimal(totalReadyMoney));

            buyCarMainLayout.addView(weiLayout);
        }
        // 已上传
        if(tableReadyOrderMap.size() > 0){
            LinearLayout yiLayout = new LinearLayout(BuyCarDetailActivity.this);
            yiLayout.setGravity(Gravity.CENTER|Gravity.LEFT);
            yiLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams yiLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            yiLayoutLp.setMargins(0, DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), 0, 0);
            yiLayout.setLayoutParams(yiLayoutLp);

            int hasOrderItemIndex = 0;
            int hasOrderCount = 0;
            double totalHasMoney = 0.0;
            for(Map.Entry<String, Object[]> hasOrderThisTable : tableReadyOrderMap.entrySet()){
                BigDecimal price = new BigDecimal(hasOrderThisTable.getValue()[0].toString().split("#&#")[4]);
                int buyNum = Integer.parseInt(hasOrderThisTable.getValue()[1].toString());
                BigDecimal thisTotalPrice = price.multiply(new BigDecimal(buyNum));
                totalHasMoney = ComFun.add(totalHasMoney, thisTotalPrice);
                hasOrderCount += buyNum;
                LinearLayout menuChildItemlayout = new LinearLayout(BuyCarDetailActivity.this);
                menuChildItemlayout.setPadding(DisplayUtil.dip2px(BuyCarDetailActivity.this, 20), 0, DisplayUtil.dip2px(BuyCarDetailActivity.this, 20), 0);
                if(hasOrderItemIndex % 2 == 0){
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#aeb39f"));
                }else{
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#c1b0be"));
                }
                menuChildItemlayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuChildItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemlayout.setLayoutParams(menuChildItemLp);
                // 子项每一项图标
                ImageView menuChildItemImg = new ImageView(BuyCarDetailActivity.this);
                menuChildItemImg.setImageResource(R.drawable.menu1);
                LinearLayout.LayoutParams menuChildItemImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(BuyCarDetailActivity.this, 45), DisplayUtil.dip2px(BuyCarDetailActivity.this, 45));
                menuChildItemImgLp.setMargins(DisplayUtil.dip2px(BuyCarDetailActivity.this, 2), DisplayUtil.dip2px(BuyCarDetailActivity.this, 2), DisplayUtil.dip2px(BuyCarDetailActivity.this, 2), DisplayUtil.dip2px(BuyCarDetailActivity.this, 2));
                menuChildItemImg.setLayoutParams(menuChildItemImgLp);
                menuChildItemlayout.addView(menuChildItemImg);
                // 子项每一项主体（名称带简要说明）
                LinearLayout menuChildItemDeslayout = new LinearLayout(BuyCarDetailActivity.this);
                menuChildItemDeslayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemDeslayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams menuChildItemDesLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(BuyCarDetailActivity.this, 0), LinearLayout.LayoutParams.MATCH_PARENT, 1);
                menuChildItemDesLp.setMargins(DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), 0, 0, 0);
                menuChildItemDeslayout.setLayoutParams(menuChildItemDesLp);
                // 主体-->菜名
                TextView menuChildItemDesNameTxt = new TextView(BuyCarDetailActivity.this);
                menuChildItemDesNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                menuChildItemDesNameTxt.setText(String.valueOf(hasOrderThisTable.getValue()[0]).split("#&#")[2]);
                menuChildItemDesNameTxt.setTextColor(Color.parseColor("#000000"));
                TextPaint menuChildItemDesNameTxtTp = menuChildItemDesNameTxt.getPaint();
                menuChildItemDesNameTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesNameTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesNameTxt.setLayoutParams(menuChildItemDesNameTxtLp);
                menuChildItemDesNameTxt.setSingleLine(true);
                menuChildItemDesNameTxt.setEllipsize(TextUtils.TruncateAt.END);
                menuChildItemDeslayout.addView(menuChildItemDesNameTxt);
                // 主体-->菜简介
                TextView menuChildItemDesssTxt = new TextView(BuyCarDetailActivity.this);
                menuChildItemDesssTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                menuChildItemDesssTxt.setText("单价：" + String.valueOf(hasOrderThisTable.getValue()[0]).split("#&#")[4] + " 元");
                TextPaint menuChildItemDesssTxtTp = menuChildItemDesssTxt.getPaint();
                menuChildItemDesssTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesssTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesssTxt.setLayoutParams(menuChildItemDesssTxtLp);
                menuChildItemDeslayout.addView(menuChildItemDesssTxt);
                menuChildItemlayout.addView(menuChildItemDeslayout);
                // 子项每一项单价
                LinearLayout menuChildItemPricelayout = new LinearLayout(BuyCarDetailActivity.this);
                menuChildItemPricelayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemPricelayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuChildItemPriceLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                menuChildItemPriceLp.setMargins(DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), 0, 0, 0);
                menuChildItemPricelayout.setLayoutParams(menuChildItemPriceLp);
                TextView buyCountTv = new TextView(BuyCarDetailActivity.this);
                buyCountTv.setTextColor(Color.parseColor("#303030"));
                TextPaint buyCountTvTp = buyCountTv.getPaint();
                buyCountTvTp.setFakeBoldText(true);
                LinearLayout.LayoutParams buyCountTvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                buyCountTv.setLayoutParams(buyCountTvLp);
                buyCountTv.setText("购买 " + buyNum + " 份");
                menuChildItemPricelayout.addView(buyCountTv);
                menuChildItemlayout.addView(menuChildItemPricelayout);

                yiLayout.addView(menuChildItemlayout);

                hasOrderItemIndex++;
            }
            // 添加总计条目
            TextView buyDetailTv = new TextView(BuyCarDetailActivity.this);
            buyDetailTv.setPadding(0, DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), DisplayUtil.dip2px(BuyCarDetailActivity.this, 15), DisplayUtil.dip2px(BuyCarDetailActivity.this, 8));
            buyDetailTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            buyDetailTv.setGravity(Gravity.CENTER|Gravity.RIGHT);
            buyDetailTv.setBackgroundColor(Color.parseColor("#1FB6CD"));
            buyDetailTv.setTextColor(Color.parseColor("#2E2E2E"));
            TextPaint buyDetailTvTp = buyDetailTv.getPaint();
            buyDetailTvTp.setFakeBoldText(true);
            LinearLayout.LayoutParams buyCountTvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            buyDetailTv.setLayoutParams(buyCountTvLp);
            buyDetailTv.setText("已上传部分： 共点餐 " + tableReadyOrderMap.size() + " 道（"+ hasOrderCount +" 份），总价：" + totalHasMoney + " 元");
            yiLayout.addView(buyDetailTv);

            totalMoney = ComFun.add(totalMoney, new BigDecimal(totalHasMoney));

            buyCarMainLayout.addView(yiLayout);
        }
        // 创建标题部分
        TextView buyCarTitleTv = new TextView(BuyCarDetailActivity.this);
        buyCarTitleTv.setBackgroundResource(R.drawable.bg_round_circle);
        buyCarTitleTv.setPadding(DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), DisplayUtil.dip2px(BuyCarDetailActivity.this, 4), DisplayUtil.dip2px(BuyCarDetailActivity.this, 8), DisplayUtil.dip2px(BuyCarDetailActivity.this, 4));
        buyCarTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        buyCarTitleTv.setText("未上传：" + hasOrderThisTableMap.size() + " 道、已上传：" + tableReadyOrderMap.size() + " 道、共：" + (hasOrderThisTableMap.size() + tableReadyOrderMap.size()) + " 道，总计 " + totalMoney + " 元");
        TextPaint buyCarTitleTvTp = buyCarTitleTv.getPaint();
        buyCarTitleTvTp.setFakeBoldText(true);
        LinearLayout.LayoutParams buyCarTitleTvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buyCarTitleTv.setLayoutParams(buyCarTitleTvLp);
        buyCarMainLayout.addView(buyCarTitleTv, 0);
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
            BuyCarDetailActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
