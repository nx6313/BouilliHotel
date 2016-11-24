package com.bouilli.nxx.bouillihotel.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.WelcomeActivity;
import com.bouilli.nxx.bouillihotel.asyncTask.InitBaseDataTask;

import java.util.Random;
import java.util.concurrent.Executors;

/**
 * Created by 18230 on 2016/10/29.
 */

public class PollingService extends Service {
    public static final String ACTION = "com.bouilli.nxx.bouillihotel.service.PollingService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onStart(Intent intent, int startId) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new PollingThread().start();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Polling thread
     * 模拟向Server轮询的异步线程
     */
    int count = 0;
    class PollingThread extends Thread {
        @Override
        public void run() {
            while(true){
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count ++;
                //当计数能被2整除时弹出通知
                if (count % 2 == 0) {
                    // 每2秒执行一次数据请求
                    new InitBaseDataTask(PollingService.this, true).executeOnExecutor(Executors.newCachedThreadPool());
                    // 数据请求成功并发送广播后，再2秒后执行下一次循环
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
