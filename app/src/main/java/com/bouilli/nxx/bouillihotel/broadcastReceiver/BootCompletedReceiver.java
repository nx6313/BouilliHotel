package com.bouilli.nxx.bouillihotel.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.push.org.androidpn.client.ServiceManager;
import com.bouilli.nxx.bouillihotel.util.ComFun;

/**
 * Created by 18230 on 2017/3/30.
 */

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComFun.showToast(context, "启动红烧肉推送服务", Toast.LENGTH_SHORT);
        ServiceManager serviceManager = new ServiceManager(context);
        serviceManager.setNotificationIcon(R.drawable.cholesterol);
        serviceManager.startService();
    }
}
