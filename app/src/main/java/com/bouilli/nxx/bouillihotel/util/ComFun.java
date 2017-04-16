package com.bouilli.nxx.bouillihotel.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ant.liao.GifView;
import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.customview.GifViewByMovie;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * 显示Toast提示信息(单例模式)
     * @param context
     * @param text
     * @param duration
     */
    public static void showToastSingle(Context context, String text, int duration) {
        Toast mToastSingle = Toast.makeText(context, text, duration);
        mToastSingle.show();
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
     * 显示网络异常提示层
     * @param activity
     * @param loadingTipValue
     */
    public static Toast netErrorToast = null;
    private static int netErrorCount = 0;
    public static void showNetErrorTip(Context context, String loadingTipValue){
        if(netErrorToast == null){
            netErrorCount = 0;
            View netErrorTipView = LayoutInflater.from(context).inflate(R.layout.net_error_tip, null);
            TextView loadingTip = (TextView) netErrorTipView.findViewById(R.id.loadingTip);
            if(loadingTip != null){
                if(strNull(loadingTipValue)){
                    loadingTip.setText(loadingTipValue);
                }
            }
            netErrorToast = new Toast(context);
            netErrorToast.setGravity(Gravity.BOTTOM, 0, DisplayUtil.dip2px(context, 90));
            netErrorToast.setDuration(Toast.LENGTH_LONG);
            netErrorToast.setView(netErrorTipView);
            netErrorToast.show();
        }else{
            netErrorCount++;
            if(netErrorCount > 20){
                netErrorToast = null;
            }
        }
    }

    /**
     * 显示菜单预览卡片
     * @param activity
     * @param loadingTipValue
     */
    public static AlertDialog menuCardDialog = null;
    public static void showMenuCard(Activity activity, String menuName, String menuPrice, String menuDes, final MenuCardEvent menuCardEvent){
        menuCardDialog = new AlertDialog.Builder(activity).setCancelable(true).create();
        menuCardDialog.show();

        //设置dialog的宽度和手机宽度一样
        WindowManager.LayoutParams lp = menuCardDialog.getWindow().getAttributes();
        lp.width = menuCardDialog.getWindow().getWindowManager().getDefaultDisplay().getWidth();
        menuCardDialog.getWindow().setAttributes(lp);//设置宽度

        Window win = menuCardDialog.getWindow();
        win.setWindowAnimations(R.style.AnimBottom);
        View menuCardView = activity.getLayoutInflater().inflate(R.layout.menu_card_dialog, null);
        win.setContentView(menuCardView);
        if(strNull(menuName)) {
            TextView cardMenuName = (TextView) menuCardView.findViewById(R.id.card_menu_name);
            cardMenuName.setText(menuName);
        }
        if(strNull(menuPrice)){
            TextView cardMenuPrice = (TextView) menuCardView.findViewById(R.id.card_menu_price);
            cardMenuPrice.setText("菜品单价：￥ " + menuPrice + " 元");
        }
        if(strNull(menuDes) && !menuDes.equals("-")){
            TextView cardMenuDes = (TextView) menuCardView.findViewById(R.id.card_menu_des);
            cardMenuDes.setText("菜品简介：" + menuDes);
            cardMenuDes.setVisibility(View.VISIBLE);
        }
        Button btnUpdateMenuInfo = (Button) menuCardView.findViewById(R.id.btnUpdateMenuInfo);
        Button btnMoveMenuTo = (Button) menuCardView.findViewById(R.id.btnMoveMenuTo);
        Button btnMenuCardCancel = (Button) menuCardView.findViewById(R.id.btnMenuCardCancel);
        btnUpdateMenuInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                menuCardEvent.updateMenuInfo(menuCardDialog);
            }
        });
        btnMoveMenuTo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                menuCardEvent.moveMenuInfo(menuCardDialog);
            }
        });
        btnMenuCardCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                menuCardDialog.dismiss();
            }
        });
    }

    public interface MenuCardEvent{
        void updateMenuInfo(AlertDialog view);
        void moveMenuInfo(AlertDialog view);
    }

    /**
     * 根据当前时间获取随机数值
     * @return
     */
    public static String getRandomIntByTime() {
        return DateFormatUtil.dateToStr(new Date());
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
        loadingGif.setGifImage(R.drawable.loading10);
        loadingGif.setShowDimension(240, 240);
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
    public static AlertDialog showLoading(Activity activity, String loadingTipValue, boolean cancelable){
        loadingDialog = new AlertDialog.Builder(activity).setCancelable(cancelable).create();
        loadingDialog.show();
        Window win = loadingDialog.getWindow();
        View loadingView = activity.getLayoutInflater().inflate(R.layout.loading_dialog, null);
        win.setContentView(loadingView);
        GifView loadingGif = (GifView) loadingView.findViewById(R.id.loadingGif);
        loadingGif.setGifImage(R.drawable.loading10);
        loadingGif.setShowDimension(240, 240);
        loadingGif.setGifImageType(GifView.GifImageType.COVER);
        TextView loadingTip = (TextView) loadingView.findViewById(R.id.loadingTip);
        if(loadingTip != null){
            if(strNull(loadingTipValue)){
                loadingTip.setText(loadingTipValue);
            }
        }
        return loadingDialog;
    }

    /**
     * 显示加载框
     * @param activity
     * @param loadingTipValue
     */
    public static void showLoading2(Activity activity, String loadingTipValue, boolean cancelable){
        loadingDialog = new AlertDialog.Builder(activity).setCancelable(cancelable).create();
        loadingDialog.show();
        Window win = loadingDialog.getWindow();
        View loadingView = activity.getLayoutInflater().inflate(R.layout.loading_dialog_movie, null);
        win.setContentView(loadingView);
        GifViewByMovie loadingGif = (GifViewByMovie) loadingView.findViewById(R.id.loadingGif);
        loadingGif.setMovieResource(R.drawable.loading1);
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

    public static double add(double d1, BigDecimal d2) {
        // 进行加法运算
        BigDecimal b1 = new BigDecimal(d1);
        return b1.add(d2).doubleValue();
    }
    public static double sub(double d1, double d2) {
        // 进行减法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.subtract(b2).doubleValue();
    }
    public static double mul(double d1, double d2) {
        // 进行乘法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).doubleValue();
    }
    public static double div(double d1, double d2,int len) {
        // 进行除法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2,len,BigDecimal. ROUND_HALF_UP).doubleValue();
    }
    public static double round(double d, int len) {
        // 进行四舍五入操作
        BigDecimal b1 = new BigDecimal(d);
        BigDecimal b2 = new BigDecimal(1);
        // 任何一个数字除以1都是原数字
        // ROUND_HALF_UP是BigDecimal的一个常量，表示进行四舍五入的操作
        return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 并0操作
     * @return
     */
    public static String addZero(String val){
        if(ComFun.strNull(val)){
            if(val.contains(".")){
                int pointNum = new BigDecimal(val).scale();
                if(pointNum <= 2){
                    for(int i=0; i<2-pointNum; i++){
                        val += "0";
                    }
                    return val;
                }else{
                    return addZero(round(Double.parseDouble(val), 2) + "");
                }
            }else{
                return val + ".00";
            }
        }
        return "0.00";
    }

    /**
     * 格式化流水订单详情数据(  去掉(-)、替换#N#为空格  )
     * @param menuDetailInfo
     * @return
     */
    public static String formatMenuDetailInfo(String menuDetailInfo){
        if(ComFun.strNull(menuDetailInfo)){
            menuDetailInfo = menuDetailInfo.replaceAll("\\(-\\)", "");
            menuDetailInfo = menuDetailInfo.replaceAll("#N#", "       ");
            return menuDetailInfo;
        }
        return "";
    }

    /**
     * 格式化流水订单详情数据(  去掉(-)、替换#N#为换行  )
     * @param menuDetailInfo
     * @return
     */
    public static String formatMenuDetailInfo2(String menuDetailInfo){
        if(ComFun.strNull(menuDetailInfo)){
            menuDetailInfo = menuDetailInfo.replaceAll("\\(-\\)", "");
            menuDetailInfo = menuDetailInfo.replaceAll("#N#", "\n");
            return menuDetailInfo;
        }
        return "";
    }

    /**
     * 格式化订餐备注信息数据(  去掉-、去掉#N#  )
     * @param menuDetailInfo
     * @return
     */
    public static String formatMenuDetailInfo3(String menuDetailInfo){
        if(ComFun.strNull(menuDetailInfo)){
            if(menuDetailInfo.startsWith("-")){
                menuDetailInfo = menuDetailInfo.replaceFirst("-", "");
            }
            if(menuDetailInfo.endsWith("-")){
                menuDetailInfo = menuDetailInfo.substring(0, menuDetailInfo.length() - 1);
            }
            menuDetailInfo = menuDetailInfo.replaceAll("#N#-", "");
            menuDetailInfo = menuDetailInfo.replaceAll("-#N#", "");
            menuDetailInfo = menuDetailInfo.replaceAll("#N#", "");
            menuDetailInfo = menuDetailInfo.replaceAll("-", "、");
            return menuDetailInfo;
        }
        return "";
    }

    /**
     * 检测当前网络状态
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断服务是否运行
     * @param mContext
     * @param className
     * @return
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(50);
        if (serviceList.size()>0) {
            for (int i=0; i<serviceList.size(); i++) {
                if (serviceList.get(i).service.getClassName().equals(className) == true) {
                    isRunning = true;
                    break;
                }
            }
        }else{
            return false;
        }
        return isRunning;
    }

    /**
     * 获取程序版本号
     * @param mContext
     * @return
     * @throws Exception
     */
    public static int getVersionNo(Context mContext) throws Exception{
        //获取packagemanager的实例
        PackageManager packageManager = mContext.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
        return packInfo.versionCode;
    }

    /**
     * 获取程序版本号显示值
     * @param mContext
     * @return
     * @throws Exception
     */
    public static String getVersionName(Context mContext) throws Exception{
        //获取packagemanager的实例
        PackageManager packageManager = mContext.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
        return packInfo.versionName;
    }

    /**
     * 安装apk
     * @param mContext
     * @param file
     */
    public static void installApk(Context mContext, File file) {
        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        //执行的数据类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    /**
     * 处理消息，根据时间，适当增加时间戳，去掉两天之前的消息；相邻时间之间相差大于1小时，则添加时间戳
     * @param msgContentList
     * @return
     */
    public static List<String> disposeMsgTime(List<String> msgContentList) {
        if(strNull(msgContentList) && msgContentList.size() > 0){
            List<String> result = new ArrayList<>();
            String lastSendTime = null;
            for(String msgContent : msgContentList){
                // 发送人Id、发送人名称、发送时间、发送内容
                String[] msgContentArr = msgContent.split("&\\|\\|&");
                if(DateFormatUtil.differentDays(DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.YYYYMMDD), DateFormatUtil.dateToDate(DateFormatUtil.YYYYMMDD)) < 2){
                    // 消息在近两天内
                    if(result.size() == 0){
                        if(DateFormatUtil.differentDays(DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.YYYYMMDD), DateFormatUtil.dateToDate(DateFormatUtil.YYYYMMDD)) < 1){
                            result.add(DateFormatUtil.dateToStr(DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.TYPE), DateFormatUtil.HHMM));
                        }else if(DateFormatUtil.differentDays(DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.YYYYMMDD), DateFormatUtil.dateToDate(DateFormatUtil.YYYYMMDD)) < 2){
                            result.add("昨天 " + DateFormatUtil.dateToStr(DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.TYPE), DateFormatUtil.HHMM));
                        }
                    }
                    if(strNull(lastSendTime)){
                        // 和上一个日期进行比较，相差是否大于1小时
                        if(DateFormatUtil.differentHours(DateFormatUtil.strToDate(lastSendTime, DateFormatUtil.TYPE), DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.TYPE)) > 1){
                            if(DateFormatUtil.differentDays(DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.YYYYMMDD), DateFormatUtil.dateToDate(DateFormatUtil.YYYYMMDD)) < 1){
                                result.add(DateFormatUtil.dateToStr(DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.TYPE), DateFormatUtil.HHMM));
                            }else if(DateFormatUtil.differentDays(DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.YYYYMMDD), DateFormatUtil.dateToDate(DateFormatUtil.YYYYMMDD)) < 2){
                                result.add("昨天 " + DateFormatUtil.dateToStr(DateFormatUtil.strToDate(msgContentArr[2], DateFormatUtil.TYPE), DateFormatUtil.HHMM));
                            }
                        }
                    }
                    lastSendTime = msgContentArr[2];
                    result.add(msgContent);
                }
            }
            return result;
        }
        return null;
    }
}
