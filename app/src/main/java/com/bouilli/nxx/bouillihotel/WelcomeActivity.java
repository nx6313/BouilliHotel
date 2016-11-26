package com.bouilli.nxx.bouillihotel;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.asyncTask.InitBaseDataTask;
import com.bouilli.nxx.bouillihotel.util.ComFun;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class WelcomeActivity extends Activity {
    private View view;
    public static Handler mHandler = null;
    public static final int MSG_INIT_BASE_DATA = 1;
    public static final int MSG_TO_MAIN = 2;

    private int showTime = 0;// 单位毫秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏和状态栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉状态栏
        view = View.inflate(this, R.layout.activity_welcome, null);
        setContentView(view);

        // 开启线程计时
        new Thread(){
            @Override
            public void run() {
                showTime++;
                super.run();
            }
        }.start();

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

        // 初始化数据（初始化完成后跳转页面）
        initData();

        mHandler = new WelcomeActivity.mHandler();
    }

    // 初始化数据
    public void initData(){
        new InitBaseDataTask(WelcomeActivity.this).executeOnExecutor(Executors.newCachedThreadPool());
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
                        ComFun.showToast(WelcomeActivity.this, "初始化数据成功", Toast.LENGTH_SHORT);
                    }else{
                        ComFun.showToast(WelcomeActivity.this, "初始化数据失败", Toast.LENGTH_SHORT);
                    }
                    if(showTime < 1800){
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Timer timer = new Timer();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        Message msg = new Message();
                                        msg.what = WelcomeActivity.MSG_TO_MAIN;
                                        mHandler.sendMessage(msg);
                                    }
                                };
                                timer.schedule(task, 1800 - showTime);
                            }
                        });
                        thread.start();
                    }else{
                        Message m = new Message();
                        m.what = WelcomeActivity.MSG_TO_MAIN;
                        mHandler.sendMessage(m);
                    }
                    break;
                case MSG_TO_MAIN:
                    full(false);
                    Intent mainActivityIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(mainActivityIntent);
                    WelcomeActivity.this.finish();
                    break;
            }
            super.handleMessage(msg);
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

}
