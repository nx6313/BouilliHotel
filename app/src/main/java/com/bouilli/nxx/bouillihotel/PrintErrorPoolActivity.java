package com.bouilli.nxx.bouillihotel;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.db.DBHelper;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.DisplayUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.io.IOException;
import java.util.Map;

public class PrintErrorPoolActivity extends AppCompatActivity {
    public static Handler mHandler = null;
    private static final int MSG_REF = 1;
    private LinearLayout printErrorPoolMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_error_pool);

        mHandler = new PrintErrorPoolActivity.mHandler();

        printErrorPoolMainLayout = (LinearLayout) findViewById(R.id.printErrorPoolMainLayout);
        printErrorPoolMainLayout.requestFocus();

        // 初始打票机回收站数据
        initPrintErrorPool();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    Message msg = new Message();
                    msg.what = PrintErrorPoolActivity.MSG_REF;
                    PrintErrorPoolActivity.mHandler.sendMessage(msg);

                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    // 键："printItem_" + 序号          内容：打票类型 #&# 打票Id #&# 打票桌号 #&# 打票内容 #&# 服务员 #&# 订单时间
    private void initPrintErrorPool() {
        final String userId = SharedPreferencesTool.getFromShared(PrintErrorPoolActivity.this, "BouilliProInfo", "userId");
        if(ComFun.strNull(userId)){
            printErrorPoolMainLayout.removeAllViews();
            Map<String, ?> map = SharedPreferencesTool.getListFromShared(PrintErrorPoolActivity.this, "printPool_" + userId);
            int index = 0;
            for(Map.Entry<String, ?> m : map.entrySet()){
                index++;
                LinearLayout recordItemLayout = new LinearLayout(PrintErrorPoolActivity.this);
                recordItemLayout.setClickable(true);
                recordItemLayout.setFocusable(true);
                recordItemLayout.setFocusableInTouchMode(true);
                recordItemLayout.setPadding(DisplayUtil.dip2px(PrintErrorPoolActivity.this, 8), DisplayUtil.dip2px(PrintErrorPoolActivity.this, 8), DisplayUtil.dip2px(PrintErrorPoolActivity.this, 8), DisplayUtil.dip2px(PrintErrorPoolActivity.this, 8));
                if(index % 2 == 0){
                    recordItemLayout.setBackgroundColor(Color.parseColor("#9b5353"));
                }else{
                    recordItemLayout.setBackgroundColor(Color.parseColor("#539b8f"));
                }
                recordItemLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams recordItemLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                recordItemLayoutLp.setMargins(0, 0, 0, DisplayUtil.dip2px(PrintErrorPoolActivity.this, 14));
                recordItemLayout.setLayoutParams(recordItemLayoutLp);
                // 订单时间
                LinearLayout orderTimeLayout = new LinearLayout(PrintErrorPoolActivity.this);
                orderTimeLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams orderTimeLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                orderTimeLayout.setLayoutParams(orderTimeLayoutLp);
                TextView orderTime = new TextView(PrintErrorPoolActivity.this);
                if(index % 2 == 0){
                    orderTime.setTextColor(Color.parseColor("#ffffff"));
                }
                orderTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                orderTime.setText("类型：");
                LinearLayout.LayoutParams orderTimeLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(PrintErrorPoolActivity.this, 120), ViewGroup.LayoutParams.WRAP_CONTENT);
                orderTime.setLayoutParams(orderTimeLp);
                TextPaint orderTimeTp = orderTime.getPaint();
                orderTimeTp.setFakeBoldText(true);
                orderTimeLayout.addView(orderTime);
                TextView orderTime2 = new TextView(PrintErrorPoolActivity.this);
                if(index % 2 == 0){
                    orderTime2.setTextColor(Color.parseColor("#ffffff"));
                }
                orderTime2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                if(m.getValue().toString().split("#&#")[0].equals("1")){
                    orderTime2.setText("点菜单小票");
                }else{
                    orderTime2.setText("消费小票");
                }
                LinearLayout.LayoutParams orderTime2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                orderTime2.setLayoutParams(orderTime2Lp);
                TextPaint orderTime2Tp = orderTime2.getPaint();
                orderTime2Tp.setFakeBoldText(true);
                orderTimeLayout.addView(orderTime2);
                recordItemLayout.addView(orderTimeLayout);
                // 桌号
                LinearLayout orderTableNoLayout = new LinearLayout(PrintErrorPoolActivity.this);
                orderTableNoLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams orderTableNoLayoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                orderTableNoLayout.setLayoutParams(orderTableNoLayoutLp);
                TextView orderTableNo = new TextView(PrintErrorPoolActivity.this);
                if(index % 2 == 0){
                    orderTableNo.setTextColor(Color.parseColor("#ffffff"));
                }
                orderTableNo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                orderTableNo.setText("桌号：");
                LinearLayout.LayoutParams orderTableNoLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(PrintErrorPoolActivity.this, 120), ViewGroup.LayoutParams.WRAP_CONTENT);
                orderTableNo.setLayoutParams(orderTableNoLp);
                TextPaint orderTableNoTp = orderTableNo.getPaint();
                orderTableNoTp.setFakeBoldText(true);
                orderTableNoLayout.addView(orderTableNo);
                TextView orderTableNo2 = new TextView(PrintErrorPoolActivity.this);
                if(index % 2 == 0){
                    orderTableNo2.setTextColor(Color.parseColor("#ffffff"));
                }
                orderTableNo2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                orderTableNo2.setText(m.getValue().toString().split("#&#")[2]);
                LinearLayout.LayoutParams orderTableNo2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                orderTableNo2.setLayoutParams(orderTableNo2Lp);
                TextPaint orderTableNo2Tp = orderTableNo2.getPaint();
                orderTableNo2Tp.setFakeBoldText(true);
                orderTableNoLayout.addView(orderTableNo2);
                recordItemLayout.addView(orderTableNoLayout);
                // 订单详情
                TextView orderDes = new TextView(PrintErrorPoolActivity.this);
                if(index % 2 == 0){
                    orderDes.setTextColor(Color.parseColor("#ffffff"));
                }else{
                    orderDes.setTextColor(Color.parseColor("#3A3A3A"));
                }
                orderDes.setText(m.getValue().toString().split("#&#")[3]);
                LinearLayout.LayoutParams orderDesLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                orderDesLp.setMargins(DisplayUtil.dip2px(PrintErrorPoolActivity.this, 8), DisplayUtil.dip2px(PrintErrorPoolActivity.this, 10), DisplayUtil.dip2px(PrintErrorPoolActivity.this, 8), DisplayUtil.dip2px(PrintErrorPoolActivity.this, 6));
                orderDes.setLayoutParams(orderDesLp);
                TextPaint orderDesTp = orderDes.getPaint();
                orderDesTp.setFakeBoldText(true);
                orderDes.setPadding(DisplayUtil.dip2px(PrintErrorPoolActivity.this, 10), DisplayUtil.dip2px(PrintErrorPoolActivity.this, 10), DisplayUtil.dip2px(PrintErrorPoolActivity.this, 10), DisplayUtil.dip2px(PrintErrorPoolActivity.this, 10));
                orderDes.setBackgroundResource(R.drawable.bg_record_circle);

                orderDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                recordItemLayout.addView(orderDes);

                printErrorPoolMainLayout.addView(recordItemLayout);

                final String printKey = m.getKey();

                final String printType = m.getValue().toString().split("#&#")[0];
                final String printAboutTable = m.getValue().toString().split("#&#")[2];
                final String printMsg = m.getValue().toString().split("#&#")[3];
                final String printOrderNum = m.getValue().toString().split("#&#")[4];
                final String printFuWuYuan = m.getValue().toString().split("#&#")[5];
                final String printOrderTime = m.getValue().toString().split("#&#")[6];
                final String outUserName = m.getValue().toString().split("#&#")[7];
                final String outUserPhone = m.getValue().toString().split("#&#")[8];
                final String outUserAddress = m.getValue().toString().split("#&#")[9];

                final String printRecordId = m.getValue().toString().split("#&#")[1];
                recordItemLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            boolean printUseVol = SharedPreferencesTool.getBooleanFromShared(PrintErrorPoolActivity.this, "BouilliSetInfo", "printUseVol");
                            if(printUseVol){
                                // 打印请求
                                if(MyApplication.mBluetoothAdapter == null){
                                    try {
                                        MyApplication.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                    } catch (Exception e) {}
                                }
                                if(!(MyApplication.mBluetoothSocket != null && MyApplication.mBluetoothSocket.isConnected())){
                                    String printAddress = SharedPreferencesTool.getFromShared(PrintErrorPoolActivity.this, "BouilliProInfo", "printAddress");
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
                                                    MyApplication.mOutputStream.write(0);
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
                                                    MyApplication.mOutputStream.write(0);
                                                    MyApplication.mOutputStream.write(("*****************************\n").getBytes("GBK"));
                                                }else{
                                                    MyApplication.mOutputStream.write(0x1c);
                                                    MyApplication.mOutputStream.write(0x21);
                                                    MyApplication.mOutputStream.write(12);
                                                    MyApplication.mOutputStream.write(("  红烧肉刀削面\n").getBytes("GBK"));
                                                    MyApplication.mOutputStream.write(0x1c);
                                                    MyApplication.mOutputStream.write(0x21);
                                                    MyApplication.mOutputStream.write(0);
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
                                                }
                                                MyApplication.mOutputStream.write(("账单编号："+ printOrderNum +"\n").getBytes("GBK"));
                                                MyApplication.mOutputStream.write(("服务员："+ printFuWuYuan +"\n").getBytes("GBK"));
                                                MyApplication.mOutputStream.write(("下单时间："+ printOrderTime +"\n").getBytes("GBK"));
                                                MyApplication.mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                                                MyApplication.mOutputStream.write(("谢谢您的惠顾！欢迎下次再来！\n").getBytes("GBK"));
                                                String userMobel = SharedPreferencesTool.getFromShared(PrintErrorPoolActivity.this, "BouilliProInfo", "userMobel");
                                                if(ComFun.strNull(userMobel) && !userMobel.equals("-")){
                                                    MyApplication.mOutputStream.write(("联系电话： "+ userMobel +"\n").getBytes("GBK"));
                                                }
                                            }
                                            MyApplication.mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                                            MyApplication.mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                                            MyApplication.mOutputStream.flush();

                                            // 声音提示
                                            String printVolSource = SharedPreferencesTool.getFromShared(PrintErrorPoolActivity.this, "BouilliSetInfo", "printVolSource");
                                            if(!printVolSource.equals("-")){
                                                Uri ringUri = Uri.parse(printVolSource);
                                                if(ringUri != null){
                                                    MediaPlayer mediaPlayer = new MediaPlayer();
                                                    try {
                                                        mediaPlayer.setDataSource(PrintErrorPoolActivity.this, ringUri);
                                                        final AudioManager audioManager = (AudioManager) PrintErrorPoolActivity.this.getSystemService(Context.AUDIO_SERVICE);
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
                                            SQLiteOpenHelper sqlite = new DBHelper(PrintErrorPoolActivity.this);
                                            SQLiteDatabase updateDb = sqlite.getWritableDatabase();
                                            ContentValues values = new ContentValues();
                                            values.put("current_state", 1);
                                            if(printType.equals("1")){
                                                updateDb.update("printinfo", values, "record_id = '"+ printRecordId +"' and current_state = 0", null);
                                            }else{
                                                updateDb.update("billinfo", values, "bill_id = '"+ printRecordId +"' and current_state = 0", null);
                                            }
                                            updateDb.close();

                                            ComFun.showToast(PrintErrorPoolActivity.this, "已成功打印", Toast.LENGTH_SHORT);
                                            SharedPreferencesTool.deleteFromShared(PrintErrorPoolActivity.this, "printPool_" + userId, printKey);
                                            initPrintErrorPool();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }else{
                                    ComFun.showToast(PrintErrorPoolActivity.this, "创建蓝牙连接失败，请检查您的设备是否支持蓝牙功能或者打票机是否正确配对", Toast.LENGTH_SHORT);
                                }
                            }else{
                                ComFun.showToast(PrintErrorPoolActivity.this, "打票机未启用", Toast.LENGTH_SHORT);
                            }
                        }
                    }
                });
            }
        }
    }

    class mHandler extends Handler {
        public mHandler() {
        }

        public mHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REF:
                    initPrintErrorPool();
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
