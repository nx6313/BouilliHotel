package com.bouilli.nxx.bouillihotel.customview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;

/**
 * Created by 18230 on 2016/11/12.
 */

public class AmountView extends LinearLayout implements View.OnClickListener, TextWatcher, View.OnLongClickListener {
    private Context context;
    private static final String TAG = "AmountView";
    private int amount = 0; //购买数量
    private int goods_storage = 999;// 最大限制

    private OnAmountChangeListener mListener;

    private EditText etAmount;
    private Button btnDecrease;
    private Button btnIncrease;

    public AmountView(Context context) {
        this(context, null);
        this.context = context;
    }

    public AmountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        LayoutInflater.from(context).inflate(R.layout.view_amount, this);
        etAmount = (EditText) findViewById(R.id.etAmount);
        btnDecrease = (Button) findViewById(R.id.btnDecrease);
        btnIncrease = (Button) findViewById(R.id.btnIncrease);
        btnDecrease.setOnClickListener(this);
        btnIncrease.setOnClickListener(this);
        etAmount.addTextChangedListener(this);
        etAmount.setOnLongClickListener(this);

        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.AmountView);
        int btnWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.AmountView_btnWidth, DisplayUtil.dip2px(context, 30));
        int tvWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.AmountView_tvWidth, DisplayUtil.dip2px(context, 40));
        int tvTextSize = obtainStyledAttributes.getDimensionPixelSize(R.styleable.AmountView_tvTextSize, 16);
        int btnTextSize = obtainStyledAttributes.getDimensionPixelSize(R.styleable.AmountView_btnTextSize, 20);
        obtainStyledAttributes.recycle();

        LayoutParams btnParams = new LayoutParams(btnWidth, btnWidth);
        btnDecrease.setLayoutParams(btnParams);
        btnIncrease.setLayoutParams(btnParams);
        if (btnTextSize != 0) {
            btnDecrease.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnTextSize);
            btnIncrease.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnTextSize);
        }

        LayoutParams textParams = new LayoutParams(tvWidth, LayoutParams.MATCH_PARENT);
        etAmount.setLayoutParams(textParams);
        if (tvTextSize != 0) {
            etAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, tvTextSize);
        }
    }

    public void setEtAmount(CharSequence sequence){
        etAmount.setText(sequence);
    }

    public int getEtAmount(){
        return Integer.parseInt(etAmount.getText().toString().trim());
    }

    public void setOnAmountChangeListener(OnAmountChangeListener onAmountChangeListener) {
        this.mListener = onAmountChangeListener;
    }

    public void setGoods_storage(int goods_storage) {
        this.goods_storage = goods_storage;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        int clickType = 0;
        if (i == R.id.btnDecrease) {
            if (amount > 0) {
                clickType = -1;
                amount--;
                etAmount.setText(amount + "");
            }
        } else if (i == R.id.btnIncrease) {
            if (amount < goods_storage) {
                clickType = 1;
                amount++;
                etAmount.setText(amount + "");
            }
        }

        etAmount.clearFocus();

        if (mListener != null) {
            mListener.onAmountChange(this, amount, clickType);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        View inputAmountValView = ((Activity) context).getLayoutInflater().inflate(R.layout.input_amount_val ,null);
        final EditText input_amount_tv = (EditText) inputAmountValView.findViewById(R.id.input_amount_tv);
        input_amount_tv.requestFocus();
        InputMethodManager imm = (InputMethodManager) input_amount_tv.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
        if(ComFun.strNull(etAmount.getText().toString().trim()) && !etAmount.getText().toString().trim().equals("0")){
            input_amount_tv.setText(etAmount.getText().toString());
        }
        input_amount_tv.setSelection(input_amount_tv.getText().toString().length());
        input_amount_tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    int value = Integer.parseInt(s.toString());
                    if(value > goods_storage){
                        input_amount_tv.setText(goods_storage+"");
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
        inputAmountValPopup.showAtLocation(((Activity) context).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        input_amount_sure.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View input_amount_ok_v) {
                if(ComFun.strNull(input_amount_tv.getText().toString().trim()) && !input_amount_tv.getText().toString().trim().equals("0")){
                    etAmount.setText(input_amount_tv.getText().toString());
                }else{
                    etAmount.setText("0");
                }
                if(inputAmountValPopup != null && inputAmountValPopup.isShowing()){
                    inputAmountValPopup.dismiss();
                }
            }
        });
        return false;
    }

    private int oldValue = 0;
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if(etAmount.getText().toString().equals("")){
            oldValue = 0;
        }else{
            oldValue = Integer.valueOf(etAmount.getText().toString());
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    int clickTypeAfterTextChanged = 0;
    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().isEmpty())
            return;
        if(s.toString().equals("")){
            amount = 0;
        }else{
            amount = Integer.valueOf(s.toString());
        }
        if (amount > goods_storage) {
            if(amount > oldValue){
                clickTypeAfterTextChanged = 2;
            }else if(amount < oldValue){
                clickTypeAfterTextChanged = -2;
            }
            etAmount.setText(goods_storage + "");
            etAmount.setSelection(etAmount.getText().toString().length());
            return;
        }

        if (mListener != null) {
            mListener.onAmountChange(this, amount, clickTypeAfterTextChanged);
        }
    }


    public interface OnAmountChangeListener {
        void onAmountChange(View view, int amount, int clickType);
    }

}
