package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bouilli.nxx.bouillihotel.OrderRecordActivity;
import com.bouilli.nxx.bouillihotel.action.DataAction;
import com.bouilli.nxx.bouillihotel.db.DBHelper;
import com.bouilli.nxx.bouillihotel.fragment.MainFragment;
import com.bouilli.nxx.bouillihotel.fragment.OutOrderFragment;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class InitOrderDataTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private boolean forPrintServiceFlag = false;

    public InitOrderDataTask(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // 检测用户权限等信息，如果是传菜员权限登录，则额外传传菜员Id参数值
        String userPermission = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userPermission");
        String hasExitLast = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "hasExitLast");
        if(ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 3 && ComFun.strNull(hasExitLast) && hasExitLast.equals("false")){
            forPrintServiceFlag = true;
        }else{
            forPrintServiceFlag = false;
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.initOrderData(context, URIUtil.INIT_ORDER_DATA_URI, forPrintServiceFlag);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // 发送Handler通知页面更新UI
        Bundle data = new Bundle();
        if(ComFun.strNull(result)){
            try {
                JSONObject jsob = new JSONObject(result);
                String responseCode = jsob.getString("responseCode");
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("initOrderDataResult", "true");
                    // 保存订单数据至本地
                    if(jsob.has("orderRecordList")){
                        JSONArray orderRecordList = jsob.getJSONArray("orderRecordList");
                        StringBuilder orderRecordFullSb = new StringBuilder("");
                        for (int i = 0; i < orderRecordList.length(); i++) {
                            String orderRecord = (String) orderRecordList.get(i);// 订单流水信息
                            orderRecordFullSb.append(orderRecord);
                            orderRecordFullSb.append(",");
                        }
                        // 发送流水信息页面更新广播
                        if(ComFun.strNull(orderRecordFullSb.toString())){
                            Intent intentRecord = new Intent();
                            intentRecord.putExtra("orderRecordFull", orderRecordFullSb.toString().substring(0, orderRecordFullSb.toString().length() - 1));
                            intentRecord.setAction(OrderRecordActivity.MSG_REFDATA);
                            context.sendBroadcast(intentRecord);
                        }else{// 发送空数据广播
                            Intent intentRecord = new Intent();
                            intentRecord.putExtra("orderRecordFull", "");
                            intentRecord.setAction(OrderRecordActivity.MSG_REFDATA);
                            context.sendBroadcast(intentRecord);
                        }
                    }
                    // 打包、外卖数据
                    StringBuilder wmRefSb = new StringBuilder("");
                    if(jsob.has("wmList")){
                        JSONArray wmList = jsob.getJSONArray("wmList");
                        StringBuilder wmInfoSb = new StringBuilder("");
                        for (int i = 0; i < wmList.length(); i++) {
                            String wmDetailInfo = (String) wmList.get(i);
                            wmInfoSb.append(wmDetailInfo);
                            wmRefSb.append("No." + ((wmDetailInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((wmDetailInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                            if(i < wmList.length()){
                                wmInfoSb.append("#@#,#");
                                wmRefSb.append("#@#,#");
                            }
                        }
                        if(ComFun.strNull(wmInfoSb.toString())){
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "wmInfos", wmInfoSb.toString());
                        }
                    }
                    StringBuilder dbRefSb = new StringBuilder("");
                    if(jsob.has("dbList")){
                        JSONArray dbList = jsob.getJSONArray("dbList");
                        StringBuilder dbInfoSb = new StringBuilder("");
                        for (int i = 0; i < dbList.length(); i++) {
                            String dbDetailInfo = (String) dbList.get(i);
                            dbInfoSb.append(dbDetailInfo);
                            dbRefSb.append("No." + ((dbDetailInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((dbDetailInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                            if(i < dbList.length()){
                                dbInfoSb.append("#@#,#");
                                dbRefSb.append("#@#,#");
                            }
                        }
                        if(ComFun.strNull(dbInfoSb.toString())){
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "dbInfos", dbInfoSb.toString());
                        }
                    }
                    if(jsob.has("wmList") || jsob.has("dbList")){
                        // 发送主页面更新广播
                        Intent intent = new Intent();
                        intent.putExtra("wmDataRef", wmRefSb.toString());
                        intent.putExtra("dbDataRef", dbRefSb.toString());
                        intent.setAction(OutOrderFragment.MSG_REF_OUTORDER_DATA);
                        context.sendBroadcast(intent);
                    }
                    // 发送主页面更新广播
                    Intent intent = new Intent();
                    intent.putExtra("newData", true);
                    intent.setAction(MainFragment.MSG_REFDATA);
                    context.sendBroadcast(intent);
                    // 获取打票机数据（根据登录用户及其设置的相关打印菜品类型）
                    if(forPrintServiceFlag){
                        if(jsob.has("orderPrintList")){
                            JSONArray orderPrintList = jsob.getJSONArray("orderPrintList");
                            for (int i = 0; i < orderPrintList.length(); i++) {
                                String orderPrint = (String) orderPrintList.get(i);// 订单流水信息

                                //SQLiteOpenHelper selectSqlite = new DBHelper(context);
                                //SQLiteDatabase selectDb = selectSqlite.getReadableDatabase();
                                //Cursor cursor = selectDb.query("printinfo", new String[]{"record_id"}, "record_id = '"+ orderPrint.split("#&#")[0] +"'", null, null, null, null, null);
                                //int recordIdIndex = cursor.getColumnIndex("record_id");
                                //List<String> resultList = new ArrayList<>();
                                //for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
                                //    resultList.add(cursor.getString(recordIdIndex));
                                //}
                                //cursor.close();
                                //selectDb.close();

                                //if(resultList.size() == 0){
                                SQLiteOpenHelper sqlite = new DBHelper(context);
                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                db.execSQL("insert into printinfo(record_id, table_id, table_no, print_context, current_state, create_date, create_user, order_num, print_date, print_count, out_user_name, out_user_phone, out_user_address, tip_count) " +
                                        "values('"+ orderPrint.split("#&#")[0] +"', '"+ orderPrint.split("#&#")[1] +"', '"+ orderPrint.split("#&#")[2] +"', '"+ orderPrint.split("#&#")[3] +"', "+ Integer.valueOf(orderPrint.split("#&#")[4]) +", '"+ orderPrint.split("#&#")[5] +"', '"+ orderPrint.split("#&#")[8] +"', '"+ orderPrint.split("#&#")[9] +"', '"+ orderPrint.split("#&#")[6] +"', "+ Integer.valueOf(orderPrint.split("#&#")[7]) +", '"+ orderPrint.split("#&#")[10] +"', '"+ orderPrint.split("#&#")[11] +"', '"+ orderPrint.split("#&#")[12] +"', 0)");
                                db.close();
                                //}
                            }
                        }
                        if(jsob.has("accountBillList")){
                            JSONArray accountBillList = jsob.getJSONArray("accountBillList");
                            for (int i = 0; i < accountBillList.length(); i++) {
                                String accountBillPrint = (String) accountBillList.get(i);// 小票信息

                                SQLiteOpenHelper sqlite = new DBHelper(context);
                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                db.execSQL("insert into billinfo(bill_id, table_no, print_context, order_num, order_waiter, order_date, out_user_name, out_user_phone, out_user_address, current_state, tip_count) " +
                                        "values('"+ accountBillPrint.split("#&#")[0] +"', '"+ accountBillPrint.split("#&#")[1] +"', '"+ accountBillPrint.split("#&#")[2] +"', '"+ accountBillPrint.split("#&#")[3] +"', '"+ accountBillPrint.split("#&#")[4] +"', '"+ accountBillPrint.split("#&#")[5] +"', '"+ accountBillPrint.split("#&#")[6] +"', '"+ accountBillPrint.split("#&#")[7] +"', '"+ accountBillPrint.split("#&#")[8] +"', 0, 0)");
                                db.close();
                            }
                        }
                    }
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("initOrderDataResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("initOrderDataResult", "time_out");
                }
            } catch (JSONException e) {
                data.putString("initOrderDataResult", "false");
            }
        }else{
            data.putString("initOrderDataResult", "false");
        }
    }
}
