package com.bouilli.nxx.bouillihotel;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.InitBaseDataTask;
import com.bouilli.nxx.bouillihotel.asyncTask.UserLoginTask;
import com.bouilli.nxx.bouillihotel.customview.ClearEditText;
import com.bouilli.nxx.bouillihotel.customview.HorizontalProgressbarWithProgress;
import com.bouilli.nxx.bouillihotel.db.DBHelper;
import com.bouilli.nxx.bouillihotel.db.DBInfo;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.MyTagHandler;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class WelcomeActivity extends Activity {
    private View view;
    public static Handler mHandler = null;
    public static final int MSG_INIT_BASE_DATA = 1;
    public static final int MSG_USER_LOGIN = 2;
    private Handler mWelcomeHandler;
    private WelcomeTask mWelcomeTesk;

    private RelativeLayout login_layout;
    private RelativeLayout new_version_layout;

    private Button btnCancelUpdateVersion;
    private Button btnUpdateVersion;

    private ClearEditText tvLoginName;
    private ClearEditText tvLoginPwd;
    private Button btnLogin;

    private AlertDialog versionDialog;
    private HorizontalProgressbarWithProgress downloadProgressBar;
    private int downloadTotal = 0;
    private boolean downloading = false;
    private URL url;
    private File file;
    private List<HashMap<String, Integer>> threadList;
    private int length;

    Handler downloadHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 0){
                downloadProgressBar.setProgress(msg.arg1);
                if(msg.arg1 >= length){
                    if(versionDialog != null && versionDialog.isShowing()){
                        versionDialog.dismiss();
                    }
                    ComFun.installApk(WelcomeActivity.this, file);
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏和状态栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉状态栏
        view = View.inflate(this, R.layout.activity_welcome, null);
        setContentView(view);

        mHandler = new WelcomeActivity.mHandler();

        login_layout = (RelativeLayout) findViewById(R.id.login_layout);
        login_layout.setVisibility(View.GONE);
        new_version_layout = (RelativeLayout) findViewById(R.id.new_version_layout);
        new_version_layout.setVisibility(View.GONE);

        btnCancelUpdateVersion = (Button) findViewById(R.id.btnCancelUpdateVersion);
        btnUpdateVersion = (Button) findViewById(R.id.btnUpdateVersion);

        tvLoginName = (ClearEditText) findViewById(R.id.tvLoginName);
        tvLoginPwd = (ClearEditText) findViewById(R.id.tvLoginPwd);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        // 渐变展示启动屏
        //AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
        //aa.setDuration(500);
        //view.startAnimation(aa);
        // 文字位移动画
        //TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -100);
        //animation.setDuration(200);//设置动画持续时间
        //animation.setRepeatCount(0);//设置重复次数
        //animation.setFillAfter(true);
        //animation.setRepeatMode(Animation.REVERSE);//设置反方向执行
        //findViewById(R.id.fire1).startAnimation(animation);

        // 清除打印服务的状态栏通知（如果有的话）
        //NotificationManager mNotificationManager = (NotificationManager) WelcomeActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.cancel(123456789);

        // 初始化数据（初始化完成后跳转页面）
        initData();

        // 登录按钮事件
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ComFun.strNull(tvLoginName.getText().toString()) && ComFun.strNull(tvLoginPwd.getText().toString())){
                    btnLogin.requestFocus();
                    ComFun.showLoading2(WelcomeActivity.this, "登陆中，请稍后...", false);
                    // 执行登录任务
                    new UserLoginTask(WelcomeActivity.this, tvLoginName.getText().toString().trim(), tvLoginPwd.getText().toString().trim()).executeOnExecutor(Executors.newCachedThreadPool());
                }else{
                    if(!ComFun.strNull(tvLoginName.getText().toString())){
                        ComFun.showToast(WelcomeActivity.this, "请输入登录账号", Toast.LENGTH_SHORT);
                        tvLoginName.requestFocus();
                        tvLoginName.setHintTextColor(Color.parseColor("#E14D49"));
                    }else if(!ComFun.strNull(tvLoginPwd.getText().toString())){
                        ComFun.showToast(WelcomeActivity.this, "请输入密码", Toast.LENGTH_SHORT);
                        tvLoginPwd.requestFocus();
                        tvLoginPwd.setHintTextColor(Color.parseColor("#E14D49"));
                    }
                }
            }
        });
        // 取消下载更新按钮事件
        btnCancelUpdateVersion.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 判断是否需要重新登录
                String hasExitLast = SharedPreferencesTool.getFromShared(WelcomeActivity.this, "BouilliProInfo", "hasExitLast");
                if(!ComFun.strNull(hasExitLast) || hasExitLast.equals("true")){
                    // 判断之前是否登录过，登陆过的话，将用户名和密码默认填写
                    String userLoginName = SharedPreferencesTool.getFromShared(WelcomeActivity.this, "BouilliProInfo", "userLoginName");
                    if(ComFun.strNull(userLoginName)){
                        tvLoginName.setText(userLoginName);
                    }
                    new_version_layout.setVisibility(View.GONE);
                    login_layout.setVisibility(View.VISIBLE);
                    AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
                    aa.setInterpolator(new AccelerateInterpolator());
                    aa.setDuration(600);
                    login_layout.startAnimation(aa);
                }else{
                    mWelcomeHandler = new Handler();
                    mWelcomeTesk = new WelcomeTask();
                    mWelcomeHandler.postDelayed(mWelcomeTesk, 100);
                }
            }
        });
        threadList = new ArrayList<>();
        // 下载更新按钮事件
        btnUpdateVersion.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startToDownloadNewVersion();
            }
        });
    }

    private void startToDownloadNewVersion(){
        downloading = true;
        // 弹框下载新版本
        versionDialog = new AlertDialog.Builder(WelcomeActivity.this).setCancelable(false).create();
        versionDialog.show();
        Window win = versionDialog.getWindow();
        View downloadVersionView = WelcomeActivity.this.getLayoutInflater().inflate(R.layout.download_version_dialog, null);
        win.setContentView(downloadVersionView);
        LinearLayout downloadVersionMain = (LinearLayout) downloadVersionView.findViewById(R.id.downloadVersionMain);
        downloadVersionMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(downloading){
                    downloading = false;
                    ComFun.showToast(WelcomeActivity.this, "下载已暂停", Toast.LENGTH_SHORT);
                }else{
                    downloading = true;
                    ComFun.showToast(WelcomeActivity.this, "继续开始下载", Toast.LENGTH_SHORT);
                    // 恢复下载
                    beginDownload();
                }
            }
        });
        downloadProgressBar = (HorizontalProgressbarWithProgress) downloadVersionView.findViewById(R.id.downloadProgressBar);
        // 开启线程下载
        beginDownload();
    }

    private void beginDownload(){
        if(threadList.size() == 0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        url = new URL("http://139.224.25.220/BouilliHotelServer/download/bouilli.apk");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
                        length = conn.getContentLength();

                        downloadProgressBar.setMax(length);// 按照百分比现实进度
                        downloadProgressBar.setProgress(0);

                        if(length < 0){
                            ComFun.showToast(WelcomeActivity.this, "文件不存在", Toast.LENGTH_SHORT);
                            return;
                        }
                        file = new File(Environment.getExternalStorageDirectory(), "bouilli.apk");
                        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                        randomAccessFile.setLength(length);

                        int blockSize = length / 3;
                        for(int i=0; i<3; i++){
                            int begin = i * blockSize;
                            int end = (i + 1) * blockSize;
                            if(i == 2){
                                end = length;
                            }
                            HashMap<String, Integer> map = new HashMap<>();
                            map.put("begin", begin);
                            map.put("end", end);
                            map.put("finished", 0);
                            threadList.add(map);
                            // 创建新的线程，下载文件
                            Thread t = new Thread(new DownloadRunnable(i, begin, end, file, url));
                            t.start();
                        }

                    } catch (MalformedURLException e) {
                        ComFun.showToast(WelcomeActivity.this, "URL 不正确！", Toast.LENGTH_SHORT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }else{
            // 恢复下载
            for(int i=0; i<threadList.size(); i++){
                HashMap<String, Integer> map = threadList.get(i);
                int begin = map.get("begin");
                int end = map.get("end");
                int finished = map.get("finished");
                Thread t = new Thread(new DownloadRunnable(i, begin + finished, end, file, url));
                t.start();
            }
        }
    }

    class DownloadRunnable implements Runnable {
        private int begin;
        private int end;
        private File file;
        private URL url;
        private int id;

        public DownloadRunnable(int id, int begin, int end, File file, URL url){
            this.begin = begin;
            this.end = end;
            this.file = file;
            this.url = url;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                if(begin > end){
                    return;
                }
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
                conn.setRequestProperty("Range", "bytes=" + begin + "-" + end);

                InputStream is = conn.getInputStream();
                byte[] buf = new byte[1024 * 1024];
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(begin);
                int len;
                HashMap<String, Integer> map = threadList.get(id);
                while((len = is.read(buf)) != -1 && downloading){
                    randomAccessFile.write(buf, 0, len);
                    updateProgress(len);
                    map.put("finished", map.get("finished") + len);
                }
                is.close();
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    synchronized private void updateProgress(int add){
        downloadTotal += add;
        downloadHandler.obtainMessage(0, downloadTotal, 0).sendToTarget();
    }

    // 初始化数据
    public void initData(){
        // 初始化创建表
        SQLiteOpenHelper deleteSqlite = new DBHelper(WelcomeActivity.this);
        SQLiteDatabase deleteDb = deleteSqlite.getReadableDatabase();
        deleteDb.execSQL(DBInfo.CreateTable.CREATE_PRINT_TABLE);
        deleteDb.execSQL(DBInfo.CreateTable.CREATE_BILL_TABLE);
        deleteDb.close();
        // 检测网络连接是否可用
        boolean isNetworkAvailable = ComFun.isNetworkAvailable(WelcomeActivity.this);
        if(isNetworkAvailable){
            new InitBaseDataTask(WelcomeActivity.this).executeOnExecutor(Executors.newCachedThreadPool());
        }else{
            ComFun.showToast(WelcomeActivity.this, "网络异常，请检查网络连接", Toast.LENGTH_SHORT);
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("initBaseDataResult", "false");
            msg.what = WelcomeActivity.MSG_INIT_BASE_DATA;
            msg.setData(data);
            WelcomeActivity.mHandler.sendMessage(msg);
        }
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
                case MSG_INIT_BASE_DATA:
                    String addNewTableResult = b.getString("initBaseDataResult");
                    if (addNewTableResult.equals("true")) {
                        //ComFun.showToast(WelcomeActivity.this, "初始化数据成功", Toast.LENGTH_SHORT);
                    }else{
                        //ComFun.showToast(WelcomeActivity.this, "初始化数据失败", Toast.LENGTH_SHORT);
                    }
                    // 先判断是否有新版本
                    boolean hasNewVersionFlag = false;
                    int newVersionNo;
                    String newVersionName = "";
                    int currentVersionNo;
                    String currentVersionName = "";
                    try {
                        newVersionNo = SharedPreferencesTool.getFromShared(WelcomeActivity.this, "BouilliProInfo", "newVersionNo", 1);
                        newVersionName = SharedPreferencesTool.getFromShared(WelcomeActivity.this, "BouilliProInfo", "newVersionName");
                        currentVersionNo = ComFun.getVersionNo(WelcomeActivity.this);
                        currentVersionName = ComFun.getVersionName(WelcomeActivity.this);
                        if(newVersionNo > currentVersionNo){
                            // 有新版本
                            hasNewVersionFlag = true;
                        }
                    } catch (Exception e) {}
                    if(hasNewVersionFlag){
                        login_layout.setVisibility(View.GONE);
                        new_version_layout.setVisibility(View.VISIBLE);
                        AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
                        aa.setInterpolator(new AccelerateInterpolator());
                        aa.setDuration(600);
                        new_version_layout.startAnimation(aa);
                        TextView updateVersionContent = (TextView) findViewById(R.id.updateVersionContent);
                        String newVersionContent = SharedPreferencesTool.getFromShared(WelcomeActivity.this, "BouilliProInfo", "newVersionContent");
                        updateVersionContent.setText("发现新版本：\n当前版本：V." + currentVersionName + "   最新版本：" + newVersionName + "\n\n更新内容：\n" + Html.fromHtml(newVersionContent, null, new MyTagHandler(WelcomeActivity.this)));
                    }else{
                        // 判断是否需要重新登录
                        String hasExitLast = SharedPreferencesTool.getFromShared(WelcomeActivity.this, "BouilliProInfo", "hasExitLast");
                        if(!ComFun.strNull(hasExitLast) || hasExitLast.equals("true")){
                            // 判断之前是否登录过，登陆过的话，将用户名和密码默认填写
                            String userLoginName = SharedPreferencesTool.getFromShared(WelcomeActivity.this, "BouilliProInfo", "userLoginName");
                            if(ComFun.strNull(userLoginName)){
                                tvLoginName.setText(userLoginName);
                            }
                            new_version_layout.setVisibility(View.GONE);
                            login_layout.setVisibility(View.VISIBLE);
                            AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
                            aa.setInterpolator(new AccelerateInterpolator());
                            aa.setDuration(600);
                            login_layout.startAnimation(aa);
                        }else{
                            mWelcomeHandler = new Handler();
                            mWelcomeTesk = new WelcomeTask();
                            mWelcomeHandler.postDelayed(mWelcomeTesk, 1000);
                        }
                    }
                    break;
                case MSG_USER_LOGIN:
                    ComFun.hideLoading(WelcomeActivity.this);
                    if(b.containsKey("userLoginResult")){
                        String userLoginResult = b.getString("userLoginResult");
                        if(userLoginResult.equals("true")){
                            // 清空打印数据表
                            SQLiteOpenHelper deleteSqlite = new DBHelper(WelcomeActivity.this);
                            SQLiteDatabase deleteDb = deleteSqlite.getReadableDatabase();
                            deleteDb.execSQL(DBInfo.CreateTable.CLEAR_PRINT_TABLE);
                            deleteDb.execSQL(DBInfo.CreateTable.CLEAR_BILL_TABLE);
                            deleteDb.close();
                            // 跳转主页面
                            mWelcomeHandler = new Handler();
                            mWelcomeTesk = new WelcomeTask();
                            mWelcomeHandler.postDelayed(mWelcomeTesk, 500);
                        }else if (userLoginResult.equals("error")) {
                            ComFun.showToast(WelcomeActivity.this, "登录失败，用户名或密码错误", Toast.LENGTH_SHORT);
                        }else if (userLoginResult.equals("false")) {
                            ComFun.showToast(WelcomeActivity.this, "登录失败，请联系管理员", Toast.LENGTH_SHORT);
                        }else if (userLoginResult.equals("time_out")) {
                            ComFun.showToast(WelcomeActivity.this, "登录超时，请稍后重试", Toast.LENGTH_SHORT);
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 欢迎页定时跳转程序任务
     */
    class WelcomeTask implements Runnable {

        @Override
        public void run() {
            full(false);
            Intent mainActivityIntent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(mainActivityIntent);
            WelcomeActivity.this.finish();
        }
    }

    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                v.clearFocus();
                return true;
            }
        }
        return false;
    }

}
