package com.bouilli.nxx.bouillihotel.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.L;
import com.bouilli.nxx.bouillihotel.util.SerializableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 18230 on 2016/11/5.
 */

public class OrderEveryFragment extends Fragment {
    int mNum;// 页号

    private LinearLayout orderPage_mainLayout;

    private List<Map<String, Object[]>> tableReadyOrderList = new ArrayList<>();// 保存该餐桌正在制作中的菜品信息
    private List<Map<String, Object[]>> tableHasNewOrderList = new ArrayList<>();// 保存该餐桌正在制作中的菜品信息

    public static OrderEveryFragment newInstance(int num, List<Map<String, Object[]>> tableReadyList, List<Map<String, Object[]>> tableHasNewList){
        OrderEveryFragment fragment = new OrderEveryFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        SerializableList tableReadyOrderList = new SerializableList();
        tableReadyOrderList.setList(tableReadyList);
        args.putSerializable("tableReadyList", tableReadyOrderList);
        SerializableList tableHasNewOrderList = new SerializableList();
        tableHasNewOrderList.setList(tableHasNewList);
        args.putSerializable("tableHasNewList", tableHasNewOrderList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        tableReadyOrderList = ((SerializableList) getArguments().getSerializable("tableReadyList")).getList();
        tableHasNewOrderList = ((SerializableList) getArguments().getSerializable("tableHasNewList")).getList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_fragment_pager, null);
        orderPage_mainLayout = (LinearLayout) view.findViewById(R.id.orderPage_mainLayout);
        orderPage_mainLayout.setTag("order|" + mNum);
        initView();
        return view;
    }

    private void initView() {
        orderPage_mainLayout.removeAllViews();
        if(tableHasNewOrderList.size() > 0 && tableHasNewOrderList.get(mNum).size() > 0){
            // 添加未上报的菜布局
            for(Map.Entry<String, Object[]> map : tableHasNewOrderList.get(mNum).entrySet()){
                LinearLayout orderPageItemLayout = new LinearLayout(getActivity());
                orderPageItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                orderPageItemLayout.setGravity(Gravity.LEFT|Gravity.CENTER);
                orderPageItemLayout.setBackgroundResource(R.drawable.bg_round_circle);
                orderPageItemLayout.setPadding(DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 4), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 4));
                LinearLayout.LayoutParams orderPageItemLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                orderPageItemLayoutLp.setMargins(DisplayUtil.dip2px(getActivity(), 15), DisplayUtil.dip2px(getActivity(), 2), DisplayUtil.dip2px(getActivity(), 15), DisplayUtil.dip2px(getActivity(), 2));
                orderPageItemLayout.setLayoutParams(orderPageItemLayoutLp);
                // 菜名和备注
                LinearLayout caiMingBeiZhuLayout = new LinearLayout(getActivity());
                caiMingBeiZhuLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams caiMingBeiZhuLayoutLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7);
                caiMingBeiZhuLayout.setLayoutParams(caiMingBeiZhuLayoutLp);
                // 菜名文字
                TextView caiMingTxt = new TextView(getActivity());
                caiMingTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                caiMingTxt.setTextColor(Color.parseColor("#ffffff"));
                TextPaint caiMingTxtTp = caiMingTxt.getPaint();
                caiMingTxtTp.setFakeBoldText(true);
                caiMingTxt.setSingleLine(true);
                caiMingTxt.setText(map.getValue()[0].toString().split("#&#")[2]);
                LinearLayout.LayoutParams caiMingTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                caiMingTxt.setLayoutParams(caiMingTxtLp);
                caiMingBeiZhuLayout.addView(caiMingTxt);
                // 备注文字
                if(!map.getValue()[2].equals("-")){
                    TextView beiZhuTxt = new TextView(getActivity());
                    beiZhuTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    beiZhuTxt.setTextColor(Color.parseColor("#6F6F6F"));
                    beiZhuTxt.setPadding(DisplayUtil.dip2px(getActivity(), 15), 0, DisplayUtil.dip2px(getActivity(), 15), 0);
                    //beiZhuTxt.setSingleLine(true);
                    beiZhuTxt.setText(String.valueOf(map.getValue()[2]));
                    LinearLayout.LayoutParams beiZhuTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    beiZhuTxt.setLayoutParams(beiZhuTxtLp);
                    caiMingBeiZhuLayout.addView(beiZhuTxt);
                }
                orderPageItemLayout.addView(caiMingBeiZhuLayout);
                // 数量
                TextView orderCountTxt = new TextView(getActivity());
                orderCountTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                orderCountTxt.setTextColor(Color.parseColor("#ffffff"));
                TextPaint orderCountTxtTp = orderCountTxt.getPaint();
                orderCountTxtTp.setFakeBoldText(true);
                orderCountTxt.setSingleLine(true);
                orderCountTxt.setText(map.getValue()[1]+" 份");
                LinearLayout.LayoutParams orderCountTxtLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                orderCountTxt.setLayoutParams(orderCountTxtLp);
                orderPageItemLayout.addView(orderCountTxt);
                // 去掉该菜按钮(只在未上报部分显示)
                Button orderRemoveBtn = new Button(getActivity());
                orderRemoveBtn.setTag(map.getKey());
                orderRemoveBtn.setTextColor(Color.parseColor("#e1dfdf"));
                orderRemoveBtn.setBackgroundResource(R.drawable.edit_order_btn_style_1);
                orderRemoveBtn.setPadding(0, 0, 0, 0);
                orderRemoveBtn.setText("删除");
                orderRemoveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                TextPaint orderRemoveBtnTp = orderRemoveBtn.getPaint();
                orderRemoveBtnTp.setFakeBoldText(true);
                LinearLayout.LayoutParams orderRemoveBtnLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(getActivity(), 50), DisplayUtil.dip2px(getActivity(), 30));
                orderRemoveBtn.setLayoutParams(orderRemoveBtnLp);
                orderRemoveBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        tableHasNewOrderList.get(mNum).remove(v.getTag().toString());
                        initView();
                    }
                });
                orderPageItemLayout.addView(orderRemoveBtn);

                orderPage_mainLayout.addView(orderPageItemLayout);

                final String selectMenuId = map.getKey();
                final String selectCount = String.valueOf(map.getValue()[1]);
                final String selectRemark = String.valueOf(map.getValue()[2]);
                orderPageItemLayout.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        View inputAmountValView = getActivity().getLayoutInflater().inflate(R.layout.input_amount_remark_val ,null);
                        final EditText input_amount_tv = (EditText) inputAmountValView.findViewById(R.id.input_amount_tv);
                        input_amount_tv.requestFocus();
                        InputMethodManager imm = (InputMethodManager) input_amount_tv.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                        input_amount_tv.setText(selectCount);
                        input_amount_tv.setSelection(input_amount_tv.getText().toString().length());
                        input_amount_tv.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if(!s.toString().isEmpty()){
                                    int value = Integer.parseInt(s.toString());
                                    if(value > 999){
                                        input_amount_tv.setText(999+"");
                                        input_amount_tv.setSelection(input_amount_tv.getText().toString().length());
                                    }
                                }
                            }
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }
                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });
                        Button input_amount_sure = (Button) inputAmountValView.findViewById(R.id.input_amount_sure);
                        final PopupWindow inputAmountValPopup = new PopupWindow(inputAmountValView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true);
                        inputAmountValPopup.setTouchable(true);
                        inputAmountValPopup.setOutsideTouchable(true);
                        ColorDrawable dw = new ColorDrawable(0xad000000);
                        inputAmountValPopup.setBackgroundDrawable(dw);
                        inputAmountValPopup.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                        final EditText input_remark_tv = (EditText) inputAmountValView.findViewById(R.id.input_remark_tv);
                        if(ComFun.strNull(selectRemark) && !selectRemark.equals("-")){
                            input_remark_tv.setText(selectRemark);
                        }
                        input_remark_tv.setSelection(input_remark_tv.getText().toString().length());
                        input_amount_sure.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View input_amount_ok_v) {
                                if(ComFun.strNull(input_amount_tv.getText().toString().trim()) && !input_amount_tv.getText().toString().trim().equals("0")){
                                    Object[] updateSelectObjArr = tableHasNewOrderList.get(mNum).get(selectMenuId);
                                    updateSelectObjArr[1] = input_amount_tv.getText().toString();
                                    initView();
                                }else{
                                    tableHasNewOrderList.get(mNum).remove(selectMenuId);
                                    initView();
                                }
                                if(ComFun.strNull(input_remark_tv.getText().toString().trim())){
                                    Object[] updateSelectObjArr = tableHasNewOrderList.get(mNum).get(selectMenuId);
                                    updateSelectObjArr[2] = input_remark_tv.getText().toString().trim();
                                    initView();
                                }else{
                                    Object[] updateSelectObjArr = tableHasNewOrderList.get(mNum).get(selectMenuId);
                                    updateSelectObjArr[2] = "-";
                                    initView();
                                }
                                if(inputAmountValPopup != null && inputAmountValPopup.isShowing()){
                                    inputAmountValPopup.dismiss();
                                }
                            }
                        });
                    }
                });
            }
        }
        if(tableHasNewOrderList.size() > 0 && tableReadyOrderList.size() > 0 &&
                tableHasNewOrderList.get(mNum).size() > 0 && tableReadyOrderList.get(mNum).size() > 0){
            // 添加分割线布局
            View orderOldNewSplitView = new View(getActivity());
            orderOldNewSplitView.setBackgroundResource(R.drawable.bg_line);
            LinearLayout.LayoutParams orderOldNewSplitViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            orderOldNewSplitViewLp.setMargins(0, DisplayUtil.dip2px(getActivity(), 5), 0, DisplayUtil.dip2px(getActivity(), 5));
            orderOldNewSplitView.setLayoutParams(orderOldNewSplitViewLp);

            orderPage_mainLayout.addView(orderOldNewSplitView);
        }
        if(tableReadyOrderList.size() > 0 && tableReadyOrderList.get(mNum).size() > 0){
            // 添加已上报的菜布局
            for(Map.Entry<String, Object[]> map : tableReadyOrderList.get(mNum).entrySet()){
                LinearLayout orderPageItemLayout = new LinearLayout(getActivity());
                orderPageItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                orderPageItemLayout.setGravity(Gravity.LEFT|Gravity.CENTER);
                orderPageItemLayout.setBackgroundResource(R.drawable.bg_round_circle);
                orderPageItemLayout.setPadding(DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 4), DisplayUtil.dip2px(getActivity(), 10), DisplayUtil.dip2px(getActivity(), 4));
                LinearLayout.LayoutParams orderPageItemLayoutLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                orderPageItemLayoutLp.setMargins(DisplayUtil.dip2px(getActivity(), 15), DisplayUtil.dip2px(getActivity(), 2), DisplayUtil.dip2px(getActivity(), 15), DisplayUtil.dip2px(getActivity(), 2));
                orderPageItemLayout.setLayoutParams(orderPageItemLayoutLp);
                // 菜名和备注
                LinearLayout caiMingBeiZhuLayout = new LinearLayout(getActivity());
                caiMingBeiZhuLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams caiMingBeiZhuLayoutLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7);
                caiMingBeiZhuLayout.setLayoutParams(caiMingBeiZhuLayoutLp);
                // 菜名文字
                TextView caiMingTxt = new TextView(getActivity());
                caiMingTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                caiMingTxt.setTextColor(Color.parseColor("#ffffff"));
                TextPaint caiMingTxtTp = caiMingTxt.getPaint();
                caiMingTxtTp.setFakeBoldText(true);
                caiMingTxt.setSingleLine(true);
                caiMingTxt.setText(map.getValue()[0].toString().split("#&#")[2]);
                LinearLayout.LayoutParams caiMingTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                caiMingTxt.setLayoutParams(caiMingTxtLp);
                caiMingBeiZhuLayout.addView(caiMingTxt);
                // 备注文字
                if(!map.getValue()[2].equals("-") && ComFun.strNull(map.getValue()[2].toString().replaceAll("-", "").replaceAll("#N#", ""))){
                    TextView beiZhuTxt = new TextView(getActivity());
                    beiZhuTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    beiZhuTxt.setTextColor(Color.parseColor("#6F6F6F"));
                    beiZhuTxt.setPadding(DisplayUtil.dip2px(getActivity(), 15), 0, DisplayUtil.dip2px(getActivity(), 15), 0);
                    //beiZhuTxt.setSingleLine(true);
                    beiZhuTxt.setText(map.getValue()[2].toString().replaceAll("#N#-", "").replaceAll("#N#", "、"));
                    LinearLayout.LayoutParams beiZhuTxtLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    beiZhuTxt.setLayoutParams(beiZhuTxtLp);
                    caiMingBeiZhuLayout.addView(beiZhuTxt);
                }
                orderPageItemLayout.addView(caiMingBeiZhuLayout);
                // 数量
                TextView orderCountTxt = new TextView(getActivity());
                orderCountTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                orderCountTxt.setTextColor(Color.parseColor("#ffffff"));
                TextPaint orderCountTxtTp = orderCountTxt.getPaint();
                orderCountTxtTp.setFakeBoldText(true);
                orderCountTxt.setSingleLine(true);
                orderCountTxt.setText(map.getValue()[1]+" 份");
                LinearLayout.LayoutParams orderCountTxtLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
                orderCountTxt.setLayoutParams(orderCountTxtLp);
                orderPageItemLayout.addView(orderCountTxt);

                orderPage_mainLayout.addView(orderPageItemLayout);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
