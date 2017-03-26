package com.bouilli.nxx.bouillihotel.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.MainActivity;
import com.bouilli.nxx.bouillihotel.R;
import com.bouilli.nxx.bouillihotel.db.DBHelper;
import com.bouilli.nxx.bouillihotel.push.org.androidpn.client.NotificationService;
import com.bouilli.nxx.bouillihotel.util.ComFun;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 18230 on 2016/10/29.
 */

public class PrintService extends Service {
    public static final String ACTION = "com.bouilli.nxx.bouillihotel.service.PrintService";
    public static final int NOTIFY_ID = 1432257853;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * @deprecated
     */
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
        useForeground("红烧肉点餐打印服务已开启", "红烧肉点餐打印服务", "  打印服务运行中");
        new PrintThread().start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void useForeground(CharSequence tickerText, String currTitle, String currSong) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        /* Method 01
         * this method must SET SMALLICON!
         * otherwise it can't do what we want in Android 4.4 KitKat,
         * it can only show the application info page which contains the 'Force Close' button.*/
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(PrintService.this)
                .setSmallIcon(R.drawable.print_set)
                .setTicker(tickerText)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(currTitle)
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
                Cursor cursor = db.query("printinfo", new String[]{"record_id, table_no, print_context, create_date, create_user, order_num, out_user_name, out_user_phone, out_user_address"}, "current_state = 0 and tip_count = 0", null, null, null, null, null);
                int recordIdIndex = cursor.getColumnIndex("record_id");
                int tableNoIndex = cursor.getColumnIndex("table_no");
                int printContextIndex = cursor.getColumnIndex("print_context");
                int createDateIndex = cursor.getColumnIndex("create_date");
                int createUserIndex = cursor.getColumnIndex("create_user");
                int orderNumIndex = cursor.getColumnIndex("order_num");
                int outUserNameIndex = cursor.getColumnIndex("out_user_name");
                int outUserPhoneIndex = cursor.getColumnIndex("out_user_phone");
                int outUserAddressIndex = cursor.getColumnIndex("out_user_address");
                for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
                    resultList.add("1#&#" + cursor.getString(recordIdIndex) + "#&#" + cursor.getString(tableNoIndex) + "#&#" +
                            cursor.getString(printContextIndex) + "#&#" + cursor.getString(createDateIndex) + "#&#" +
                            cursor.getString(createUserIndex) + "#&#" + cursor.getString(orderNumIndex) + "#&#" +
                            cursor.getString(outUserNameIndex) + "#&#" + cursor.getString(outUserPhoneIndex) + "#&#" +
                            cursor.getString(outUserAddressIndex));
                }
                cursor.close();
                // 查询需打印小票表
                Cursor cursor_bill = db.query("billinfo", new String[]{"bill_id, table_no, print_context, order_num, order_waiter, order_date, out_user_name, out_user_phone, out_user_address"}, "current_state = 0 and tip_count = 0", null, null, null, null, null);
                int billIdIndex = cursor_bill.getColumnIndex("bill_id");
                int billTableNoIndex = cursor_bill.getColumnIndex("table_no");
                int billPrintContextIndex = cursor_bill.getColumnIndex("print_context");
                int billOrderNumIndex = cursor_bill.getColumnIndex("order_num");
                int billOrderWaiterIndex = cursor_bill.getColumnIndex("order_waiter");
                int billOrderDateIndex = cursor_bill.getColumnIndex("order_date");
                int billOutUserNameIndex = cursor.getColumnIndex("out_user_name");
                int billOutUserPhoneIndex = cursor.getColumnIndex("out_user_phone");
                int billOutUserAddressIndex = cursor.getColumnIndex("out_user_address");
                for (cursor_bill.moveToFirst(); !(cursor_bill.isAfterLast()); cursor_bill.moveToNext()) {
                    resultList.add("2#&#" + cursor_bill.getString(billIdIndex) + "#&#" + cursor_bill.getString(billTableNoIndex) + "#&#" +
                            cursor_bill.getString(billPrintContextIndex) + "#&#" + cursor_bill.getString(billOrderDateIndex) + "#&#" +
                            cursor_bill.getString(billOrderWaiterIndex) + "#&#" + cursor_bill.getString(billOrderNumIndex) + "#&#" +
                            cursor_bill.getString(billOutUserNameIndex) + "#&#" + cursor_bill.getString(billOutUserPhoneIndex) + "#&#" +
                            cursor_bill.getString(billOutUserAddressIndex));
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
                        broadcast.putExtra("printOrderTime", resultList.get(i).split("#&#")[4]);
                        broadcast.putExtra("printFuWuYuan", resultList.get(i).split("#&#")[5]);
                        broadcast.putExtra("printOrderNum", resultList.get(i).split("#&#")[6]);
                        broadcast.putExtra("outUserName", resultList.get(i).split("#&#")[7]);
                        broadcast.putExtra("outUserPhone", resultList.get(i).split("#&#")[8]);
                        broadcast.putExtra("outUserAddress", resultList.get(i).split("#&#")[9]);
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
        // 清除打印服务的状态栏通知（如果有的话）
        stopForeground(true);
    }

}
