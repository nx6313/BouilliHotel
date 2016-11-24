package com.bouilli.nxx.bouillihotel.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.util.Log;

import com.bouilli.nxx.bouillihotel.MyApplication;
import com.bouilli.nxx.bouillihotel.asyncTask.InitBaseDataTask;
import com.bouilli.nxx.bouillihotel.db.DBHelper;
import com.bouilli.nxx.bouillihotel.util.ComFun;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by 18230 on 2016/10/29.
 */

public class PrintService extends Service {
    public static final String ACTION = "com.bouilli.nxx.bouillihotel.service.PrintService";

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
                Cursor cursor = db.query("printinfo", new String[]{"record_id, table_no, print_context"}, "current_state = 0", null, null, null, null, null);
                int recordIdIndex = cursor.getColumnIndex("record_id");
                int tableNoIndex = cursor.getColumnIndex("table_no");
                int printContextIndex = cursor.getColumnIndex("print_context");
                List<String> resultList = new ArrayList<>();
                for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
                    resultList.add(cursor.getString(recordIdIndex) + "#&#" + cursor.getString(tableNoIndex) + "#&#" + cursor.getString(printContextIndex));
                }
                cursor.close();
                db.close();
                if(ComFun.strNull(resultList) && resultList.size() > 0){
                    for(int i=0; i<resultList.size(); i++){
                        SQLiteDatabase updateDb = sqlite.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("current_state", 1);
                        updateDb.update("printinfo", values, "record_id = '"+ resultList.get(i).split("#&#")[0] +"' and current_state = 0", null);
                        updateDb.close();
                        // 发送打印服务广播
                        Intent broadcast=new Intent();
                        broadcast.setAction("com.nxx.bouilli.broadcastReceiver.broadcast");
                        broadcast.putExtra("printAboutTable", resultList.get(i).split("#&#")[1]);
                        broadcast.putExtra("printContent", ComFun.formatMenuDetailInfo2(resultList.get(i).split("#&#")[2]));
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
    }

}
