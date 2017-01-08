package com.bouilli.nxx.bouillihotel.util;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Locale;

/**
 * Created by 18230 on 2016/12/2.
 */
public class MyDatePickerDialog extends DatePickerDialog {
    private CharSequence title;

    public MyDatePickerDialog(Context context, OnDateSetListener callBack,
                               int year, int monthOfYear, int dayOfMonth, CharSequence title, int type) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
        this.title = title;
        this.setTitle(title);
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if(type == 0){
            // 只显示年
            if (language.endsWith("zh")) {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
            } else {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
            }
        }else if(type == 1){
            // 只显示月
            if (language.endsWith("zh")) {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
            } else {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
            }
        }else if(type == 2){
            // 只显示日
            if (language.endsWith("zh")) {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
            } else {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
            }
        }else if(type == 3){
            // 显示年、月
            if (language.endsWith("zh")) {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
            } else {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(1).setVisibility(View.GONE);
            }
        }else if(type == 4){
            // 显示月、日
            if (language.endsWith("zh")) {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
            } else {
                ((ViewGroup) ((ViewGroup) this.getDatePicker().getChildAt(0))
                        .getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
            }
        }
        this.setButton2("取消", (OnClickListener)null);
        this.setButton("确定", this);  //setButton和this参数组合表示这个按钮是确定按钮
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        super.onDateChanged(view, year, month, day);
        this.setTitle(title);
    }
}
