package com.bouilli.nxx.bouillihotel.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bouilli.nxx.bouillihotel.MainActivity;
import com.bouilli.nxx.bouillihotel.R;

/**
 * Created by 18230 on 2017/3/30.
 */

public class EmptyService extends Service {
    public static final String ACTION = "com.bouilli.nxx.bouillihotel.service.EmptyService";
    public static final int NOTIFY_ID = 0;

    @Nullable
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
        useForeground("", "推送服务正在运行...");
        EmptyService.this.stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // 关闭通知栏信息
        stopForeground(true);
    }

    public void useForeground(CharSequence tickerText, String currSong) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        /* Method 01
         * this method must SET SMALLICON!
         * otherwise it can't do what we want in Android 4.4 KitKat,
         * it can only show the application info page which contains the 'Force Close' button.*/
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(EmptyService.this)
                .setSmallIcon(R.drawable.carrot)
                .setTicker(tickerText)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(currSong);
        //.setContentIntent(pendingIntent);
        Notification notification = mNotifyBuilder.build();

        /* Method 02
        Notification notification = new Notification(R.drawable.ic_launcher, tickerText,
                System.currentTimeMillis());
        notification.setLatestEventInfo(PlayService.this, getText(R.string.app_name),
                currSong, pendingIntent);
        */

        startForeground(NOTIFY_ID, notification);
    }
}
