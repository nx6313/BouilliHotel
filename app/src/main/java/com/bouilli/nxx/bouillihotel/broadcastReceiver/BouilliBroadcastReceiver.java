package com.bouilli.nxx.bouillihotel.broadcastReceiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.MyApplication;
import com.bouilli.nxx.bouillihotel.db.DBHelper;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.io.IOException;
import java.util.Map;

/**
 * Created by 18230 on 2016/11/11.
 */

public class BouilliBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_NOT_NET = "com.nxx.bouilli.netNotFound";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("com.nxx.bouilli.broadcastReceiver.broadcast")){
            String userPermission = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userPermission");
            String hasExitLast = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "hasExitLast");
            boolean printUseVol = SharedPreferencesTool.getBooleanFromShared(context, "BouilliSetInfo", "printUseVol");
            if(ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 3 && ComFun.strNull(hasExitLast) && hasExitLast.equals("false")){
                String printType = "";// 1为订单数据  2为小票数据
                String printRecordId = "";
                String printAboutTable = "";
                String printMsg = "";
                String printOrderNum = "";
                String printFuWuYuan = "";
                String printOrderTime = "";
                String outUserName = "-";
                String outUserPhone = "-";
                String outUserAddress = "-";
                if(intent.hasExtra("printType") && intent.hasExtra("printRecordId") && intent.hasExtra("printAboutTable") && intent.hasExtra("printContent")
                        && intent.hasExtra("printOrderNum") && intent.hasExtra("printFuWuYuan") && intent.hasExtra("printOrderTime")){
                    printType = intent.getStringExtra("printType");
                    printRecordId = intent.getStringExtra("printRecordId");
                    printAboutTable = intent.getStringExtra("printAboutTable");
                    printMsg = intent.getStringExtra("printContent");
                    printOrderNum = intent.getStringExtra("printOrderNum");
                    printFuWuYuan = intent.getStringExtra("printFuWuYuan");
                    printOrderTime = intent.getStringExtra("printOrderTime");
                }
                if(intent.hasExtra("outUserName") && intent.hasExtra("outUserPhone") && intent.hasExtra("outUserAddress")){
                    outUserName = intent.getStringExtra("outUserName");
                    outUserPhone = intent.getStringExtra("outUserPhone");
                    outUserAddress = intent.getStringExtra("outUserAddress");
                }
                if(ComFun.strNull(printRecordId) && ComFun.strNull(printMsg)){
                    if(printUseVol){
                        // 打印请求
                        if(MyApplication.mBluetoothAdapter == null){
                            try {
                                MyApplication.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            } catch (Exception e) {}
                        }
                        if(!(MyApplication.mBluetoothSocket != null && MyApplication.mBluetoothSocket.isConnected())){
                            String printAddress = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "printAddress");
                            try {
                                MyApplication.mBluetoothDevice = MyApplication.mBluetoothAdapter.getRemoteDevice(printAddress);
                                MyApplication.mBluetoothSocket = MyApplication.mBluetoothDevice.createRfcommSocketToServiceRecord(MyApplication.SPP_UUID);
                                MyApplication.mBluetoothSocket.connect();
                            } catch (Exception e) {}
                        }
                        if(MyApplication.mBluetoothAdapter != null && MyApplication.mBluetoothAdapter.isEnabled()){
                            if(MyApplication.mBluetoothSocket != null){
                                try {
                                    MyApplication.mOutputStream = MyApplication.mBluetoothSocket.getOutputStream();
                                    if(ComFun.strNull(printAboutTable)){
                                        if(printType.equals("1")){
                                            MyApplication.mOutputStream.write(0x1c);
                                            MyApplication.mOutputStream.write(0x21);
                                            MyApplication.mOutputStream.write(12);
                                            MyApplication.mOutputStream.write(("     后厨点菜单\n").getBytes("GBK"));
                                            MyApplication.mOutputStream.write(0x1c);
                                            MyApplication.mOutputStream.write(0x21);
                                            MyApplication.mOutputStream.write(16);
                                            MyApplication.mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                                            if(printAboutTable.contains(">>")){
                                                if(printAboutTable.contains("DB")){
                                                    MyApplication.mOutputStream.write(("餐桌：打包单 "+ printAboutTable.substring(9, printAboutTable.length()) +" 号\n").getBytes("GBK"));
                                                }else{
                                                    MyApplication.mOutputStream.write(("餐桌：外卖单 "+ printAboutTable.substring(9, printAboutTable.length()) +" 号\n").getBytes("GBK"));
                                                }
                                            }else{
                                                MyApplication.mOutputStream.write(("餐桌："+ printAboutTable.substring(printAboutTable.indexOf(".") + 1, printAboutTable.length()) +" 号\n").getBytes("GBK"));
                                            }
                                            MyApplication.mOutputStream.write(0x1c);
                                            MyApplication.mOutputStream.write(0x21);
                                            MyApplication.mOutputStream.write(16);
                                            MyApplication.mOutputStream.write(("*****************************\n").getBytes("GBK"));
                                        }else{
                                            MyApplication.mOutputStream.write(0x1c);
                                            MyApplication.mOutputStream.write(0x21);
                                            MyApplication.mOutputStream.write(12);
                                            MyApplication.mOutputStream.write(("  红烧肉刀削面\n").getBytes("GBK"));
                                            MyApplication.mOutputStream.write(0x1c);
                                            MyApplication.mOutputStream.write(0x21);
                                            MyApplication.mOutputStream.write(16);
                                            MyApplication.mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                                            MyApplication.mOutputStream.write(("-----------------------------\n").getBytes("GBK"));
                                        }
                                    }else{
                                        MyApplication.mOutputStream.write(("**********  ###  **********\n").getBytes("GBK"));
                                    }
                                    MyApplication.mOutputStream.write((printMsg+"\n").getBytes("GBK"));
                                    if(printType.equals("1")){
                                        MyApplication.mOutputStream.write(("*****************************\n").getBytes("GBK"));
                                        if(!outUserPhone.equals("-") && !outUserAddress.equals("-")){
                                            if(outUserName.equals("-")){
                                                MyApplication.mOutputStream.write(("外卖联系人姓名：匿名\n").getBytes("GBK"));
                                            }else{
                                                MyApplication.mOutputStream.write(("外卖联系人姓名："+ outUserName +"\n").getBytes("GBK"));
                                            }
                                            MyApplication.mOutputStream.write(("外卖联系人电话："+ outUserPhone +"\n").getBytes("GBK"));
                                            MyApplication.mOutputStream.write(("外卖联系人地址："+ outUserAddress +"\n").getBytes("GBK"));
                                            MyApplication.mOutputStream.write(("*****************************\n").getBytes("GBK"));
                                        }
                                        MyApplication.mOutputStream.write(("账单编号："+ printOrderNum +"\n").getBytes("GBK"));
                                        MyApplication.mOutputStream.write(("服务员："+ printFuWuYuan +"\n").getBytes("GBK"));
                                        MyApplication.mOutputStream.write(("下单时间："+ printOrderTime +"\n").getBytes("GBK"));
                                    }else{
                                        MyApplication.mOutputStream.write(("-----------------------------\n").getBytes("GBK"));
                                        if(!outUserPhone.equals("-") && !outUserAddress.equals("-")){
                                            if(outUserName.equals("-")){
                                                MyApplication.mOutputStream.write(("外卖联系人姓名：匿名\n").getBytes("GBK"));
                                            }else{
                                                MyApplication.mOutputStream.write(("外卖联系人姓名："+ outUserName +"\n").getBytes("GBK"));
                                            }
                                            MyApplication.mOutputStream.write(("外卖联系人电话："+ outUserPhone +"\n").getBytes("GBK"));
                                            MyApplication.mOutputStream.write(("外卖联系人地址："+ outUserAddress +"\n").getBytes("GBK"));
                                            MyApplication.mOutputStream.write(("-----------------------------\n").getBytes("GBK"));
                                        }
                                        MyApplication.mOutputStream.write(("账单编号："+ printOrderNum +"\n").getBytes("GBK"));
                                        MyApplication.mOutputStream.write(("服务员："+ printFuWuYuan +"\n").getBytes("GBK"));
                                        MyApplication.mOutputStream.write(("下单时间："+ printOrderTime +"\n").getBytes("GBK"));
                                        MyApplication.mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                                        MyApplication.mOutputStream.write(("谢谢您的惠顾！欢迎下次再来！\n").getBytes("GBK"));
                                        String userMobel = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userMobel");
                                        if(ComFun.strNull(userMobel) && !userMobel.equals("-")){
                                            MyApplication.mOutputStream.write(("联系电话： "+ userMobel +"\n").getBytes("GBK"));
                                        }
                                    }
                                    MyApplication.mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                                    MyApplication.mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                                    MyApplication.mOutputStream.flush();

                                    // 声音提示
                                    String printVolSource = SharedPreferencesTool.getFromShared(context, "BouilliSetInfo", "printVolSource");
                                    if(!printVolSource.equals("-")){
                                        Uri ringUri = Uri.parse(printVolSource);
                                        if(ringUri != null){
                                            MediaPlayer mediaPlayer = new MediaPlayer();
                                            try {
                                                mediaPlayer.setDataSource(context, ringUri);
                                                final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                                if(audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0){
                                                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                                                    mediaPlayer.setLooping(false);
                                                    mediaPlayer.prepare();
                                                    mediaPlayer.start();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    SQLiteOpenHelper sqlite = new DBHelper(context);
                                    SQLiteDatabase updateDb = sqlite.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put("current_state", 1);
                                    if(printType.equals("1")){
                                        updateDb.update("printinfo", values, "record_id = '"+ printRecordId +"' and current_state = 0", null);
                                    }else{
                                        updateDb.update("billinfo", values, "bill_id = '"+ printRecordId +"' and current_state = 0", null);
                                    }
                                    updateDb.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }else{
                            //ComFun.showToast(context, "创建蓝牙连接失败，请检查您的设备是否支持蓝牙功能或者打票机是否正确配对", Toast.LENGTH_SHORT);
                            String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
                            if(printType.equals("1")){
                                SQLiteOpenHelper sqlite = new DBHelper(context);
                                SQLiteDatabase db = sqlite.getReadableDatabase();
                                // 查询需打印订单数据表
                                int hasCount = 0;
                                Cursor cursor = db.query("printinfo", new String[]{"record_id"}, "record_id = '"+ printRecordId +"' and tip_count = 0", null, null, null, null, null);
                                for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
                                    hasCount++;
                                }
                                cursor.close();
                                db.close();
                                if(hasCount > 0){
                                    // 保存至本地缓存（与登录账号关联）
                                    if(ComFun.strNull(userId)){
                                        Map<String, ?> map = SharedPreferencesTool.getListFromShared(context, "printPool_" + userId);
                                        SharedPreferencesTool.addOrUpdate(context, "printPool_" + userId, "printItem_" + map.size(), printType + "#&#" + printRecordId + "#&#" + printAboutTable + "#&#" + printMsg + "#&#" + printOrderNum + "#&#" + printFuWuYuan + "#&#" + printOrderTime + "#&#" + outUserName + "#&#" + outUserPhone + "#&#" + outUserAddress);
                                    }
                                    ComFun.showToastSingle(context, "餐桌【"+ printAboutTable +"】有新订单，检测到打票机连接异常，打票数据已保存，请检查连接后从小票回收站中选择打印\n\n订单详情：\n"+printMsg, 3000);
                                    SQLiteDatabase updateDb = sqlite.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put("tip_count", 1);
                                    updateDb.update("printinfo", values, "record_id = '"+ printRecordId +"' and tip_count = 0", null);
                                    updateDb.close();
                                }
                            }else{
                                SQLiteOpenHelper sqlite = new DBHelper(context);
                                SQLiteDatabase db = sqlite.getReadableDatabase();
                                // 查询需打印订单数据表
                                int hasCount = 0;
                                Cursor cursor_bill = db.query("billinfo", new String[]{"bill_id"}, "bill_id = '"+ printRecordId +"' and tip_count = 0", null, null, null, null, null);
                                for (cursor_bill.moveToFirst(); !(cursor_bill.isAfterLast()); cursor_bill.moveToNext()) {
                                    hasCount++;
                                }
                                cursor_bill.close();
                                db.close();
                                if(hasCount > 0){
                                    // 保存至本地缓存（与登录账号关联）
                                    if(ComFun.strNull(userId)){
                                        Map<String, ?> map = SharedPreferencesTool.getListFromShared(context, "printPool_" + userId);
                                        SharedPreferencesTool.addOrUpdate(context, "printPool_" + userId, "printItem_" + map.size(), printType + "#&#" + printRecordId + "#&#" + printAboutTable + "#&#" + printMsg + "#&#" + printOrderNum + "#&#" + printFuWuYuan + "#&#" + printOrderTime + "#&#" + outUserName + "#&#" + outUserPhone + "#&#" + outUserAddress);
                                    }
                                    ComFun.showToastSingle(context, "餐桌【"+ printAboutTable +"】需要小票，检测到打票机连接异常，小票数据已保存，请检查连接后从小票回收站中选择打印\n\n小票详情：\n"+printMsg, 3000);
                                    SQLiteDatabase updateDb = sqlite.getWritableDatabase();
                                    ContentValues values = new ContentValues();
                                    values.put("tip_count", 1);
                                    updateDb.update("billinfo", values, "bill_id = '"+ printRecordId +"' and tip_count = 0", null);
                                    updateDb.close();
                                }
                            }
                        }
                    }else{
                        String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
                        if(printType.equals("1")){
                            SQLiteOpenHelper sqlite = new DBHelper(context);
                            SQLiteDatabase db = sqlite.getReadableDatabase();
                            // 查询需打印订单数据表
                            int hasCount = 0;
                            Cursor cursor = db.query("printinfo", new String[]{"record_id"}, "record_id = '"+ printRecordId +"' and tip_count = 0", null, null, null, null, null);
                            for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
                                hasCount++;
                            }
                            cursor.close();
                            db.close();
                            if(hasCount > 0){
                                // 保存至本地缓存（与登录账号关联）
                                if(ComFun.strNull(userId)){
                                    Map<String, ?> map = SharedPreferencesTool.getListFromShared(context, "printPool_" + userId);
                                    SharedPreferencesTool.addOrUpdate(context, "printPool_" + userId, "printItem_" + map.size(), printType + "#&#" + printRecordId + "#&#" + printAboutTable + "#&#" + printMsg + "#&#" + printOrderNum + "#&#" + printFuWuYuan + "#&#" + printOrderTime + "#&#" + outUserName + "#&#" + outUserPhone + "#&#" + outUserAddress);
                                }
                                ComFun.showToastSingle(context, "餐桌【"+ printAboutTable +"】有新订单，检测到打票机未启用，请从菜单【打票机设置】处开启，开启后从小票回收站中选择打印\n\n订单详情：\n"+printMsg, 3000);
                                SQLiteDatabase updateDb = sqlite.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("tip_count", 1);
                                updateDb.update("printinfo", values, "record_id = '"+ printRecordId +"' and tip_count = 0", null);
                                updateDb.close();
                            }
                        }else{
                            SQLiteOpenHelper sqlite = new DBHelper(context);
                            SQLiteDatabase db = sqlite.getReadableDatabase();
                            // 查询需打印订单数据表
                            int hasCount = 0;
                            Cursor cursor_bill = db.query("billinfo", new String[]{"bill_id"}, "bill_id = '"+ printRecordId +"' and tip_count = 0", null, null, null, null, null);
                            for (cursor_bill.moveToFirst(); !(cursor_bill.isAfterLast()); cursor_bill.moveToNext()) {
                                hasCount++;
                            }
                            cursor_bill.close();
                            db.close();
                            if(hasCount > 0){
                                // 保存至本地缓存（与登录账号关联）
                                if(ComFun.strNull(userId)){
                                    Map<String, ?> map = SharedPreferencesTool.getListFromShared(context, "printPool_" + userId);
                                    SharedPreferencesTool.addOrUpdate(context, "printPool_" + userId, "printItem_" + map.size(), printType + "#&#" + printRecordId + "#&#" + printAboutTable + "#&#" + printMsg + "#&#" + printOrderNum + "#&#" + printFuWuYuan + "#&#" + printOrderTime + "#&#" + outUserName + "#&#" + outUserPhone + "#&#" + outUserAddress);
                                }
                                ComFun.showToastSingle(context, "餐桌【"+ printAboutTable +"】需要小票，检测到打票机未启用，请从菜单【打票机设置】处开启，开启后从小票回收站中选择打印\n\n小票详情：\n"+printMsg, 3000);
                                SQLiteDatabase updateDb = sqlite.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("tip_count", 1);
                                updateDb.update("billinfo", values, "bill_id = '"+ printRecordId +"' and tip_count = 0", null);
                                updateDb.close();
                            }
                        }
                    }
                }
            }
        }else if(intent.getAction().equals(ACTION_NOT_NET)){
            //ComFun.showToast(context, "网络异常，请检查网络连接", Toast.LENGTH_SHORT);
            //ComFun.showNetErrorTip(context, "网络异常，请检查网络连接");
        }
    }

}
