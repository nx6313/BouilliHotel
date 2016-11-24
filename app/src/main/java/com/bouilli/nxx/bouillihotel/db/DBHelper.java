package com.bouilli.nxx.bouillihotel.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by 18230 on 2016/11/20.
 */

public class DBHelper extends SQLiteOpenHelper {

    /**
     * 构造器
     * @param context 上下文
     */
    public DBHelper(Context context) {
        super(context, DBInfo.DB.DB_NAME, null, DBInfo.DB.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBInfo.CreateTable.CREATE_PRINT_TABLE);
        Log.d("DBHelper--->onCreate", "创建了表："+ DBInfo.Table.PRINT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DBHelper--->onCreate", "版本变更，更新SQLLite数据库："+DBInfo.DB.DB_NAME+"，当前版本："+DBInfo.DB.VERSION);
        db.execSQL(DBInfo.CreateTable.DROP_PRINT_TABLE);//实际开发时，先进行数据的备份
    }
}
