package com.bouilli.nxx.bouillihotel.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;

import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.db.DBHelper;
import com.bouilli.nxx.bouillihotel.util.ComFun;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 18230 on 2016/10/29.
 */

public class PrintService extends Service {
    public static final String ACTION = "com.bouilli.nxx.bouillihotel.service.PrintService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void showNotification(){
        NotificationManager mNotificationManager = (NotificationManager) PrintService.this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(PrintService.this);
        builder.setContentTitle("红烧肉点餐打印服务");
        builder.setContentText("打印服务运行中");
        builder.setSmallIcon(R.drawable.print_set);
        builder.setTicker("红烧肉点餐打印服务已开启");
        builder.setWhen(System.currentTimeMillis());
        //Intent printNotificationIntent = new Intent(getApplicationContext(), OrderRecordActivity.class);
        //PendingIntent printNotificationPendingIntent = PendingIntent.getActivity(OrderRecordActivity.this, 0, printNotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //builder.setContentIntent(printNotificationPendingIntent);
        Notification printNotification = builder.build();
        // printNotification.sound = Uri.parse("file:///sdcard/notification/ringer.mp3");
        // printNotification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");
        printNotification.defaults |= Notification.DEFAULT_VIBRATE;
        printNotification.flags |= Notification.FLAG_ONGOING_EVENT;// 添加进正在运行组
        printNotification.flags |= Notification.FLAG_NO_CLEAR;// 不可清除
        mNotificationManager.notify(123456789, printNotification);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onStart(Intent intent, int startId) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification();
        new PrintThread().start();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Print thread
     * 模拟向Server轮询的异步线程
     */
    class PrintThread extends Thread {
        @Override
        public void run() {
            while(true){
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                SQLiteOpenHelper sqlite = new DBHelper(PrintService.this);
                SQLiteDatabase db = sqlite.getReadableDatabase();
                List<String> resultList = new ArrayList<>();
                // 查询需打印订单数据表
                Cursor cursor = db.query("printinfo", new String[]{"record_id, table_no, print_context"}, "current_state = 0 and tip_count = 0", null, null, null, null, null);
                int recordIdIndex = cursor.getColumnIndex("record_id");
                int tableNoIndex = cursor.getColumnIndex("table_no");
                int printContextIndex = cursor.getColumnIndex("print_context");
                for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
                    resultList.add("1#&#" + cursor.getString(recordIdIndex) + "#&#" + cursor.getString(tableNoIndex) + "#&#" + cursor.getString(printContextIndex));
                }
                cursor.close();
                // 查询需打印小票表
                Cursor cursor_bill = db.query("billinfo", new String[]{"bill_id, table_no, print_context"}, "current_state = 0 and tip_count = 0", null, null, null, null, null);
                int billIdIndex = cursor_bill.getColumnIndex("bill_id");
                int billTableNoIndex = cursor_bill.getColumnIndex("table_no");
                int billPrintContextIndex = cursor_bill.getColumnIndex("print_context");
                for (cursor_bill.moveToFirst(); !(cursor_bill.isAfterLast()); cursor_bill.moveToNext()) {
                    resultList.add("2#&#" + cursor_bill.getString(billIdIndex) + "#&#" + cursor_bill.getString(billTableNoIndex) + "#&#" + cursor_bill.getString(billPrintContextIndex));
                }
                cursor_bill.close();
                db.close();
                if(ComFun.strNull(resultList) && resultList.size() > 0){
                    for(int i=0; i<resultList.size(); i++){
                        // 发送打印服务广播
                        Intent broadcast = new Intent();
                        broadcast.setAction("com.nxx.bouilli.broadcastReceiver.broadcast");
                        broadcast.putExtra("printType", resultList.get(i).split("#&#")[0]);
                        broadcast.putExtra("printRecordId", resultList.get(i).split("#&#")[1]);
                        broadcast.putExtra("printAboutTable", resultList.get(i).split("#&#")[2]);
                        broadcast.putExtra("printContent", ComFun.formatMenuDetailInfo2(resultList.get(i).split("#&#")[3]));
                        PrintService.this.sendBroadcast(broadcast);
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清除打印服务的状态栏通知（如果有的话）
        NotificationManager mNotificationManager = (NotificationManager) PrintService.this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(123456789);
    }

}
