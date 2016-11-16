package com.bouilli.nxx.bouillihotel.broadcastReceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.util.ComFun;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by 18230 on 2016/11/11.
 */

public class BouilliBroadcastReceiver extends BroadcastReceiver {
    PowerManager pm = null;
    PowerManager.WakeLock wakeLock = null;
    public static OutputStream mOutputStream = null;

    public static boolean beginPrintServiceFlag = false;// 开启打票机服务的开关，默认关闭
    public static List<String> mpairedDeviceList = new ArrayList<>();// 设备已配对的蓝牙对象列表
    public static BluetoothAdapter mBluetoothAdapter = null;   //创建蓝牙适配器
    public static BluetoothDevice mBluetoothDevice=null;
    public static BluetoothSocket mBluetoothSocket = null;

    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static Set<BluetoothDevice> pairedDevices = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.SCREEN_OFF")){
            // 屏幕灭
            pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PrintService");
            wakeLock.acquire();
        }
        if(intent.getAction().equals("android.intent.action.SCREEN_ON")){
            // 屏幕亮
            if(wakeLock != null){
                wakeLock.release();
                wakeLock = null;
            }
            if(pm != null){
                pm = null;
            }
        }
        if(intent.getAction().equals("com.nxx.bouilli.broadcastReceiver.broadcast")){
            // 打印请求
            String printMsg = intent.getStringExtra("printContent");
            if(beginPrintServiceFlag && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()){
                if(mBluetoothSocket != null && ComFun.strNull(printMsg)){
                    try {
                        mOutputStream = mBluetoothSocket.getOutputStream();
                        mOutputStream.write(("*****************************\n").getBytes("GBK"));
                        mOutputStream.write((printMsg+"\n").getBytes("GBK"));
                        mOutputStream.write(("*****************************\n").getBytes("GBK"));
                        mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                        mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                        mOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else if(beginPrintServiceFlag){
                ComFun.showToast(context, "蓝牙未打开或打票机蓝牙连接失败", Toast.LENGTH_SHORT);
            }
        }
    }

}
