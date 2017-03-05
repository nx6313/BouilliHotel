package com.bouilli.nxx.bouillihotel.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.WelcomeActivity;
import com.bouilli.nxx.bouillihotel.asyncTask.InitBaseDataTask;
import com.bouilli.nxx.bouillihotel.asyncTask.InitOrderDataTask;
import com.bouilli.nxx.bouillihotel.broadcastReceiver.BouilliBroadcastReceiver;
import com.bouilli.nxx.bouillihotel.fragment.MainFragment;
import com.bouilli.nxx.bouillihotel.util.ComFun;

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
    class PollingThread extends Thread {
        @Override
        public void run() {
            while(true){
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 检测网络连接是否可用
                boolean isNetworkAvailable = ComFun.isNetworkAvailable(PollingService.this);
                if(isNetworkAvailable){
                    // 每2秒执行一次数据请求
                    new InitBaseDataTask(PollingService.this, true).executeOnExecutor(Executors.newCachedThreadPool());
                    new InitOrderDataTask(PollingService.this).executeOnExecutor(Executors.newCachedThreadPool());
                }else{
                    // 发送全局广播，说明网络异常
                    Intent intent = new Intent();
                    intent.setAction(BouilliBroadcastReceiver.ACTION_NOT_NET);
                    PollingService.this.sendBroadcast(intent);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("BOUILLI", ">>>polling服务停止");
    }

}
