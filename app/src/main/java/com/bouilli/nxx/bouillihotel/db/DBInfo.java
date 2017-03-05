package com.bouilli.nxx.bouillihotel.db;

/**
 * Created by 18230 on 2016/11/20.
 */

public class DBInfo {
    public static final int STATE_DELETE = 0;
    public static final int STATE_ADD = 1;
    public static final int STATE_UPDATE = 2;

    public static class DB {
        /**
         * 数据库名称
         */
        public static final String DB_NAME = "bouilli.db";
        /**
         * 数据库版本
         */
        public static final int VERSION = 2;
    }

    public static class Table {
        /**
         * 打印菜单信息记录表
         */
        public static final String PRINT_TABLE = "printinfo";
        /**
         * 打印菜单信息记录表
         */
        public static final String BILL_TABLE = "billinfo";
    }

    public static class CreateTable {
        /**
         * 打印菜单信息记录表SQL语句
         */
        public static final String CREATE_PRINT_TABLE = "create table if not exists "
                + Table.PRINT_TABLE + " (id integer primary key autoincrement," +
                "record_id varchar(100), table_id varchar(100), table_no varchar(80), print_context text, current_state integer, create_date varchar(100), create_user varchar(200), order_num varchar(100), print_date varchar(100), print_count integer, out_user_name varchar(100), out_user_phone varchar(100), out_user_address varchar(500), tip_count integer); ";
        public static final String DROP_PRINT_TABLE = "drop table if exists " + Table.PRINT_TABLE;
        public static final String CLEAR_PRINT_TABLE = "delete from "+ Table.PRINT_TABLE;
        /**
         * 打印小票信息记录表SQL语句
         */
        public static final String CREATE_BILL_TABLE = "create table if not exists "
                + Table.BILL_TABLE + " (id integer primary key autoincrement," +
                "bill_id varchar(100), table_no varchar(80), print_context text, order_num varchar(100), order_waiter varchar(200), order_date varchar(100), out_user_name varchar(100), out_user_phone varchar(100), out_user_address varchar(500), current_state integer, tip_count integer); ";
        public static final String DROP_BILL_TABLE = "drop table if exists " + Table.BILL_TABLE;
        public static final String CLEAR_BILL_TABLE = "delete from "+ Table.PRINT_TABLE;
    }
}
