package com.bouilli.nxx.bouillihotel;

import android.app.Application;
import android.content.Intent;

import com.bouilli.nxx.bouillihotel.service.PrintService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 18230 on 2016/11/20.
 */

public class MyApplication extends Application {
    public static List<String> printPoolList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // 开启打印轮询服务
        Intent printServiceIntent = new Intent(getApplicationContext(), PrintService.class);
        printServiceIntent.setAction(PrintService.ACTION);
        printServiceIntent.setPackage(getPackageName());
        getApplicationContext().startService(printServiceIntent);
    }

}
