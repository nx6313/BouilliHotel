package com.bouilli.nxx.bouillihotel.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.SelectMenuActivity;
import com.bouilli.nxx.bouillihotel.customview.AmountView;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

/**
 * Created by 18230 on 2016/11/5.
 */

public class SelectMenuFragment extends Fragment {
    int mNum;// 页号
    private EditMenuBroadCastReceive selectMenuBroadCastReceive;

    private LinearLayout selectMenuMainLayout;

    public static SelectMenuFragment newInstance(int num){
        SelectMenuFragment fragment = new SelectMenuFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册广播接收器
        selectMenuBroadCastReceive = new EditMenuBroadCastReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction("requestNewDataBouilliHotel");
        getActivity().registerReceiver(selectMenuBroadCastReceive, filter);
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_menu_fragment_pager, null);
        selectMenuMainLayout = (LinearLayout) view.findViewById(R.id.selectMenuMainLayout);
        String menuGroupNames = SharedPreferencesTool.getFromShared(getActivity(), "BouilliMenuInfo", "menuGroupNames");
        if(ComFun.strNull(menuGroupNames)){
            if(mNum == 0){
                // 添加常用菜品
                String oftenUseMenus = SharedPreferencesTool.getFromShared(getActivity(), "BouilliMenuInfo", "oftenUseMenus");
                addMenuView(oftenUseMenus.split(","));
            }else{
                String thisGroupMenuInfo = SharedPreferencesTool.getFromShared(getActivity(), "BouilliMenuInfo", "menuItemChild" + menuGroupNames.split(",")[mNum - 1].split("#&#")[0]);
                addMenuView(thisGroupMenuInfo.split(","));
            }
        }
        return view;
    }

    // 添加菜品布局
    private void addMenuView(String[] thisGroupMenuInfoArr){
        selectMenuMainLayout.removeAllViews();
        int chileItemIndex = 0;
        for(String thisGroupMenuInfo : thisGroupMenuInfoArr){
            if(ComFun.strNull(thisGroupMenuInfo)){
                LinearLayout menuChildItemlayout = new LinearLayout(getActivity());
                menuChildItemlayout.setTag("menuChildItemOrderLayout");
                menuChildItemlayout.setPadding(DisplayUtil.dip2px(getActivity(), 20), 0, DisplayUtil.dip2px(getActivity(), 20), 0);
                if(chileItemIndex % 2 == 0){
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#aeb39f"));
                }else{
                    menuChildItemlayout.setBackgroundColor(Color.parseColor("#c1b0be"));
                }
                menuChildItemlayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuChildItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemlayout.setLayoutParams(menuChildItemLp);
                // 子项每一项图标
                ImageView menuChildItemImg = new ImageView(getActivity());
                menuChildItemImg.setTag(thisGroupMenuInfo);
                menuChildItemImg.setImageResource(R.drawable.menu1);
                LinearLayout.LayoutParams menuChildItemImgLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(getActivity(), 45), DisplayUtil.dip2px(getActivity(), 45));
                menuChildItemImgLp.setMargins(DisplayUtil.dip2px(getActivity(), 2), DisplayUtil.dip2px(getActivity(), 2), DisplayUtil.dip2px(getActivity(), 2), DisplayUtil.dip2px(getActivity(), 2));
                menuChildItemImg.setLayoutParams(menuChildItemImgLp);
                menuChildItemlayout.addView(menuChildItemImg);
                // 子项每一项主体（名称带简要说明）
                LinearLayout menuChildItemDeslayout = new LinearLayout(getActivity());
                menuChildItemDeslayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemDeslayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams menuChildItemDesLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(getActivity(), 0), LinearLayout.LayoutParams.MATCH_PARENT, 1);
                menuChildItemDesLp.setMargins(DisplayUtil.dip2px(getActivity(), 8), 0, 0, 0);
                menuChildItemDeslayout.setLayoutParams(menuChildItemDesLp);
                // 主体-->菜名
                TextView menuChildItemDesNameTxt = new TextView(getActivity());
                menuChildItemDesNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                menuChildItemDesNameTxt.setText(thisGroupMenuInfo.split("#&#")[2]);
                menuChildItemDesNameTxt.setTextColor(Color.parseColor("#000000"));
                TextPaint menuChildItemDesNameTxtTp = menuChildItemDesNameTxt.getPaint();
                menuChildItemDesNameTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesNameTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesNameTxt.setLayoutParams(menuChildItemDesNameTxtLp);
                menuChildItemDesNameTxt.setSingleLine(true);
                menuChildItemDesNameTxt.setEllipsize(TextUtils.TruncateAt.END);
                menuChildItemDeslayout.addView(menuChildItemDesNameTxt);
                // 主体-->菜简介
                TextView menuChildItemDesssTxt = new TextView(getActivity());
                menuChildItemDesssTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                if(!thisGroupMenuInfo.split("#&#")[3].equals("-")){
                    menuChildItemDesssTxt.setText(thisGroupMenuInfo.split("#&#")[4] + " 元【" + thisGroupMenuInfo.split("#&#")[3] + "】");
                }else{
                    menuChildItemDesssTxt.setText(thisGroupMenuInfo.split("#&#")[4] + " 元");
                }
                TextPaint menuChildItemDesssTxtTp = menuChildItemDesssTxt.getPaint();
                menuChildItemDesssTxtTp.setFakeBoldText(true);
                LinearLayout.LayoutParams menuChildItemDesssTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                menuChildItemDesssTxt.setLayoutParams(menuChildItemDesssTxtLp);
                menuChildItemDeslayout.addView(menuChildItemDesssTxt);
                menuChildItemlayout.addView(menuChildItemDeslayout);
                // 子项每一项单价
                LinearLayout menuChildItemPricelayout = new LinearLayout(getActivity());
                menuChildItemPricelayout.setGravity(Gravity.CENTER|Gravity.LEFT);
                menuChildItemPricelayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams menuChildItemPriceLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                menuChildItemPriceLp.setMargins(DisplayUtil.dip2px(getActivity(), 8), 0, 0, 0);
                menuChildItemPricelayout.setLayoutParams(menuChildItemPriceLp);
                // 编辑数量按钮
                AmountView amountView = new AmountView(getActivity());
                amountView.setTag("amountView_order");
                LinearLayout.LayoutParams amountViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                amountView.setLayoutParams(amountViewLp);
                menuChildItemPricelayout.addView(amountView);
                menuChildItemlayout.addView(menuChildItemPricelayout);

                selectMenuMainLayout.addView(menuChildItemlayout);

                // 绑定每样菜的数字选择事件监听
                amountView.setOnAmountChangeListener(new AmountView.OnAmountChangeListener() {
                    @Override
                    public void onAmountChange(View view, int amount) {
                        String thisMenuId = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString().split("#&#")[0];
                        String thisMenuName = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString().split("#&#")[2];
                        String thisMenuInfo = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString();
                        ComFun.showToast(getActivity(), thisMenuName+"，已选："+amount+"个", Toast.LENGTH_SHORT);
                        if(amount > 0){
                            Object[] newOrderInfo = new Object[]{ thisMenuInfo, amount };
                            SelectMenuActivity.hasOrderThisTableMap.put(thisMenuId, newOrderInfo);
                        }else{
                            if(SelectMenuActivity.hasOrderThisTableMap.containsKey(thisMenuId)){
                                SelectMenuActivity.hasOrderThisTableMap.remove(thisMenuId);
                            }
                        }
                    }
                });

                chileItemIndex++;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消注册广播
        getActivity().unregisterReceiver(selectMenuBroadCastReceive);
    }

    public class EditMenuBroadCastReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

}
