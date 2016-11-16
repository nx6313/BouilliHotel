package com.bouilli.nxx.bouillihotel.util;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ant.liao.GifView;
import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.customview.GifViewByMovie;

/**
 * Created by 18230 on 2016/10/30.
 */

public class ComFun {
    private static Toast mToast = null;

    /**
     * 显示Toast提示信息
     * @param context
     * @param text
     * @param duration
     */
    public static void showToast(Context context, String text, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, duration);
        } else {
            mToast.setText(text);
            mToast.setDuration(duration);
        }
        mToast.show();
    }

    /**
     * 判断对象不为空
     *
     * @param str
     * @return
     */
    public static boolean strNull(Object str) {
        if (str != null && str != "" && !str.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 打开输入法
     * @param context
     */
    public static void openIME(Context context, EditText editText){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 关闭输入法
     * @param context
     */
    public static void closeIME(Context context, View view){
        if(view.getWindowToken() != null){
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 显示加载框
     * @param activity
     * @param loadingTipValue
     */
    public static AlertDialog loadingDialog = null;
    public static void showLoading(Activity activity, String loadingTipValue){
        loadingDialog = new AlertDialog.Builder(activity).setCancelable(false).create();
        loadingDialog.show();
        Window win = loadingDialog.getWindow();
        View loadingView = activity.getLayoutInflater().inflate(R.layout.loading_dialog, null);
        win.setContentView(loadingView);
        GifView loadingGif = (GifView) loadingView.findViewById(R.id.loadingGif);
        loadingGif.setGifImage(R.drawable.loading1);
        loadingGif.setShowDimension(64, 64);
        loadingGif.setGifImageType(GifView.GifImageType.COVER);
        TextView loadingTip = (TextView) loadingView.findViewById(R.id.loadingTip);
        if(loadingTip != null){
            if(strNull(loadingTipValue)){
                loadingTip.setText(loadingTipValue);
            }
        }
    }

    /**
     * 显示加载框
     * @param activity
     * @param loadingTipValue
     */
    public static void showLoading(Activity activity, String loadingTipValue, boolean cancelable){
        loadingDialog = new AlertDialog.Builder(activity).setCancelable(cancelable).create();
        loadingDialog.show();
        Window win = loadingDialog.getWindow();
        View loadingView = activity.getLayoutInflater().inflate(R.layout.loading_dialog_movie, null);
        win.setContentView(loadingView);
        GifViewByMovie loadingGif = (GifViewByMovie) loadingView.findViewById(R.id.loadingGif);
        loadingGif.setMovieResource(R.drawable.loading10);
        TextView loadingTip = (TextView) loadingView.findViewById(R.id.loadingTip);
        if(loadingTip != null){
            if(strNull(loadingTipValue)){
                loadingTip.setVisibility(View.VISIBLE);
                loadingTip.setText(loadingTipValue);
            }else{
                loadingTip.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 隐藏加载框
     * @param activity
     */
    public static void hideLoading(Activity activity){
        if(loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }

    public static boolean strInArr(String[] strArr, String str){
        if(strNull(strArr) && strArr.length > 0 && strNull(str)){
            for(String s : strArr){
                if(s.equals(str)){
                    return true;
                }
            }
        }
        return false;
    }
}
