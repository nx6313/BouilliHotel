package com.bouilli.nxx.bouillihotel;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.database.sqlite.SQLiteDatabase;

import com.bouilli.nxx.bouillihotel.entity.build.DaoMaster;
import com.bouilli.nxx.bouillihotel.entity.build.DaoSession;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by 18230 on 2016/11/20.
 */

public class MyApplication extends Application {
    public static DaoMaster daoMaster;
    public static DaoSession daoSession;
    public static SQLiteDatabase db;
    public static DaoMaster.DevOpenHelper helper;

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    public static OutputStream mOutputStream = null;

    public static List<String> mpairedDeviceList = new ArrayList<>();// 设备已配对的蓝牙对象列表
    public static BluetoothAdapter mBluetoothAdapter = null;   //创建蓝牙适配器
    public static BluetoothDevice mBluetoothDevice = null;
    public static BluetoothSocket mBluetoothSocket = null;

    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static Set<BluetoothDevice> pairedDevices = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        helper = new DaoMaster.DevOpenHelper(this, "BouilliHotelData.db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    public static SQLiteDatabase getSqLiteDatabase() {
        return db;
    }

}
