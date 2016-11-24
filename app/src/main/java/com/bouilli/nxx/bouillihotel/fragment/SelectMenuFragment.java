package com.bouilli.nxx.bouillihotel.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private static LinearLayout selectMenuMainLayout;

    private int screenHeight;

    private ViewGroup anim_mask_layout;
    private ImageView imgIcon;

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
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;

        anim_mask_layout = createAnimLayout();

        WindowManager wm = getActivity().getWindowManager();
        screenHeight = wm.getDefaultDisplay().getHeight();
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
                menuChildItemPricelayout.setTag("menuId_" + thisGroupMenuInfo.split("#&#")[0]);
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
                if(SelectMenuActivity.hasOrderThisTableMap.containsKey(thisGroupMenuInfo.split("#&#")[0])){
                    amountView.setEtAmount(SelectMenuActivity.hasOrderThisTableMap.get(thisGroupMenuInfo.split("#&#")[0])[1].toString());
                }
                SelectMenuActivity.selectMenuAmountViewPoor.put("menuId_" + mNum + "_" + thisGroupMenuInfo.split("#&#")[0], amountView);
                menuChildItemPricelayout.addView(amountView);
                menuChildItemlayout.addView(menuChildItemPricelayout);

                selectMenuMainLayout.addView(menuChildItemlayout);

                // 绑定每样菜的数字选择事件监听
                amountView.setOnAmountChangeListener(new AmountView.OnAmountChangeListener() {
                    @Override
                    public void onAmountChange(View view, int amount, int clickType) {
                        // 这个菜增加数量，执行购物车动画
                        if(clickType == 1){
                            // 增加操作
                            setAnim(view);
                        }
                        String thisMenuId = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString().split("#&#")[0];
                        String thisMenuName = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString().split("#&#")[2];
                        String thisMenuInfo = ((LinearLayout) view.getParent().getParent()).getChildAt(0).getTag().toString();
                        //ComFun.showToast(getActivity(), thisMenuName+"，已选："+amount+"个", Toast.LENGTH_SHORT);
                        if(amount > 0){
                            Object[] newOrderInfo = new Object[]{ thisMenuInfo, amount, "-" };// 菜id, 选择数量, 备注信息
                            SelectMenuActivity.hasOrderThisTableMap.put(thisMenuId, newOrderInfo);
                        }else{
                            if(SelectMenuActivity.hasOrderThisTableMap.containsKey(thisMenuId) && clickType == -1){
                                SelectMenuActivity.hasOrderThisTableMap.remove(thisMenuId);
                            }
                        }
                        // 发送Handler通知页面更新UI
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("thisMenuInfo", thisMenuInfo);
                        data.putString("selectNum", amount+"");
                        msg.what = SelectMenuActivity.MSG_SELECT_NEW_MENU;
                        msg.setData(data);
                        SelectMenuActivity.mHandler.sendMessage(msg);
                    }
                });

                chileItemIndex++;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * @Description: 创建动画层
     * @param
     * @return void
     * @throws
     */
    private ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) getActivity().getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    /**
     * @Description: 添加视图到动画层
     * @param @param vg
     * @param @param view
     * @param @param location
     * @param @return
     * @return View
     * @throws
     */
    private View addViewToAnimLayout(final ViewGroup vg, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        vg.removeAllViews();
        vg.addView(view);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }

    private void setAnim(View startView) {
        anim_mask_layout.removeAllViews();
        Animation mScaleAnimation = new ScaleAnimation(1.5f, 0.1f, 1.5f, 0.1f, Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.1f);
        mScaleAnimation.setDuration(AnimationDuration);
        mScaleAnimation.setFillAfter(true);

        int[] start_location = new int[2];
        startView.getLocationInWindow(start_location);
        // 将组件添加到我们的动画层上
        imgIcon = new ImageView(getActivity());
        imgIcon.setImageResource(R.drawable.menu2);
        View view = addViewToAnimLayout(anim_mask_layout, imgIcon, start_location);
        int[] end_location = new int[2];
        // 计算位移
        int endX = end_location[0] - start_location[0] + DisplayUtil.dip2px(getActivity(), 25);
        int endY = screenHeight - DisplayUtil.dip2px(getActivity(), 110) - start_location[1];

        Animation mTranslateAnimation = new TranslateAnimation(0, endX, 0, endY);// 移动
        mTranslateAnimation.setDuration(AnimationDuration);

        AnimationSet mAnimationSet = new AnimationSet(false);
        // 这块要注意，必须设为false,不然组件动画结束后，不会归位。
        mAnimationSet.setFillAfter(false);
        mAnimationSet.addAnimation(mScaleAnimation);
        mAnimationSet.addAnimation(mTranslateAnimation);
        view.startAnimation(mAnimationSet);

        mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                anim_mask_layout.removeAllViews();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                anim_mask_layout.removeAllViews();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                anim_mask_layout.removeAllViews();
            }
        });
    }

    /**
     * 动画播放时间
     */
    private int AnimationDuration = 600;

}
