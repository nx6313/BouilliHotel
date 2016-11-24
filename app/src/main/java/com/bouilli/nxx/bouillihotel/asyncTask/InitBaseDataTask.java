package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.bouilli.nxx.bouillihotel.MyApplication;
import com.bouilli.nxx.bouillihotel.OrderRecordActivity;
import com.bouilli.nxx.bouillihotel.WelcomeActivity;
import com.bouilli.nxx.bouillihotel.action.DataAction;
import com.bouilli.nxx.bouillihotel.db.DBHelper;
import com.bouilli.nxx.bouillihotel.fragment.MainFragment;
import com.bouilli.nxx.bouillihotel.service.PrintService;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 18230 on 2016/10/30.
 */

public class InitBaseDataTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private boolean forPollingServiceFlag = false;

    public InitBaseDataTask(Context context){
        this.context = context;
    }
    public InitBaseDataTask(Context context, boolean forPollingServiceFlag){
        this.context = context;
        this.forPollingServiceFlag = forPollingServiceFlag;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.initBaseData(context, URIUtil.INIT_BASE_DATA_URI);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(ComFun.strNull(result)){
            try {
                JSONObject jsob = new JSONObject(result);
                String responseCode = jsob.getString("responseCode");
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    if(!forPollingServiceFlag){
                        // 当是启动程序时，并且成功获取到程序基本数据时，清除收集中已经保存的所有的SharedPreferences文件
                        SharedPreferencesTool.clearShared(context, new String[]{ "BouilliProInfo", "BouilliTableInfo", "BouilliMenuInfo" });
                    }
                    data.putString("initBaseDataResult", "true");
                    // 保存用户基本信息到数据缓存文件
                    SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "userId", "admin");
                    // 保存基本数据至本地
                    // 餐桌数据
                    StringBuilder tableGroupNameSb = new StringBuilder("");
                    StringBuilder tableNumSimpleSb;
                    StringBuilder tableNumSb;
                    if(jsob.has("groupNameList") && jsob.has("tableInfoMap")){
                        JSONArray groupNameList = jsob.getJSONArray("groupNameList");
                        JSONObject tableInfoMap = jsob.getJSONObject("tableInfoMap");
                        StringBuilder tableFullNumSb = new StringBuilder("");
                        for (int i = 0; i < groupNameList.length(); i++) {
                            String groupName = (String) groupNameList.get(i);
                            tableGroupNameSb.append(groupName + ",");
                            tableNumSimpleSb = new StringBuilder("");
                            tableNumSb = new StringBuilder("");
                            for (int j = 0; j < tableInfoMap.getJSONArray(groupName).length(); j++) {
                                String thisGroupTableNum = (String) tableInfoMap.getJSONArray(groupName).get(j);
                                tableNumSimpleSb.append(thisGroupTableNum.split("\\|")[0] + ",");// 记录餐桌 组代号.餐桌号
                                tableNumSb.append(thisGroupTableNum + ",");// 记录每个餐桌组内的餐桌信息（组代号.餐桌号|餐桌状态|餐桌当前就餐信息id）
                                tableFullNumSb.append(thisGroupTableNum + ",");// 记录所有餐桌组内的餐桌信息（组代号.餐桌号|餐桌状态|餐桌当前就餐信息id）
                            }
                            if(ComFun.strNull(tableNumSb.toString())){
                                SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableInfoSimple" + groupName, tableNumSimpleSb.toString().substring(0, tableNumSimpleSb.toString().length() - 1));
                                SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableInfo" + groupName, tableNumSb.toString().substring(0, tableNumSb.toString().length() - 1));
                            }else{
                                SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableInfoSimple" + groupName, "");
                                SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableInfo" + groupName, "");
                            }
                        }
                        if(ComFun.strNull(tableGroupNameSb.toString())){
                            SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableGroupNames", tableGroupNameSb.toString().substring(0, tableGroupNameSb.toString().length() - 1));
                        }else{
                            SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableGroupNames", "");
                        }

                        if(ComFun.strNull(tableFullNumSb.toString())){
                            SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableFullInfo", tableFullNumSb.toString().substring(0, tableFullNumSb.toString().length() - 1));
                        }else{
                            SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableFullInfo", "");
                        }
                    }else{
                        // 数据集为空
                        SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableGroupNames", "");
                        SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableFullInfo", "");
                    }
                    // 菜单数据
                    if(jsob.has("menuGroupNameList") && jsob.has("menuInfoMap")){
                        JSONArray menuGroupNameList = jsob.getJSONArray("menuGroupNameList");
                        JSONObject menuInfoMap = jsob.getJSONObject("menuInfoMap");
                        StringBuilder menuGroupNamesFullSb = new StringBuilder("");
                        StringBuilder menuGroupChildFullSb;
                        for (int i = 0; i < menuGroupNameList.length(); i++) {
                            String groupName = (String) menuGroupNameList.get(i);
                            menuGroupNamesFullSb.append(groupName + ",");
                            menuGroupChildFullSb = new StringBuilder("");
                            for (int j = 0; j < menuInfoMap.getJSONArray(groupName.split("#&#")[0]).length(); j++) {
                                String thisGroupMenuInfo = (String) menuInfoMap.getJSONArray(groupName.split("#&#")[0]).get(j);
                                // Id&GroupId&MenuName&MenuDes&Price&UseCount
                                if(!thisGroupMenuInfo.equals("-")){
                                    menuGroupChildFullSb.append(thisGroupMenuInfo + ",");
                                    SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", thisGroupMenuInfo.split("#&#")[0], thisGroupMenuInfo);
                                }
                            }
                            if(ComFun.strNull(menuGroupChildFullSb.toString())){
                                SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuItemChild"+groupName.split("#&#")[0], menuGroupChildFullSb.toString().substring(0, menuGroupChildFullSb.toString().length() - 1));
                            }
                        }
                        if(ComFun.strNull(menuGroupNamesFullSb.toString())){
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuGroupNames", menuGroupNamesFullSb.toString().substring(0, menuGroupNamesFullSb.toString().length() - 1));
                        }else{
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuGroupNames", "");
                        }
                    }else{
                        // 数据集为空
                        SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuGroupNames", "");
                    }
                    // 常用菜品数据
                    if(jsob.has("ofenUseMenuInfoList")){
                        JSONArray ofenUseMenuInfoList = jsob.getJSONArray("ofenUseMenuInfoList");
                        StringBuilder oftenUseMenuSb = new StringBuilder("");
                        for (int i = 0; i < ofenUseMenuInfoList.length(); i++) {
                            oftenUseMenuSb.append(ofenUseMenuInfoList.get(i));
                            oftenUseMenuSb.append(",");
                        }
                        if(ComFun.strNull(oftenUseMenuSb.toString())){
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "oftenUseMenus", oftenUseMenuSb.toString().substring(0, oftenUseMenuSb.toString().length() - 1));
                        }
                    }
                    // 当前登录用户权限数据
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("initBaseDataResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("initBaseDataResult", "time_out");
                }
                if(!forPollingServiceFlag){
                    msg.what = WelcomeActivity.MSG_INIT_BASE_DATA;
                    msg.setData(data);
                    WelcomeActivity.mHandler.sendMessage(msg);
                }else{
                    if(jsob.has("orderRecordList")){
                        JSONArray orderRecordList = jsob.getJSONArray("orderRecordList");
                        StringBuilder orderRecordFullSb = new StringBuilder("");
                        for (int i = 0; i < orderRecordList.length(); i++) {
                            String orderRecord = (String) orderRecordList.get(i);// 订单流水信息
                            orderRecordFullSb.append(orderRecord);
                            orderRecordFullSb.append(",");

                            SQLiteOpenHelper selectSqlite = new DBHelper(context);
                            SQLiteDatabase selectDb = selectSqlite.getReadableDatabase();
                            Cursor cursor = selectDb.query("printinfo", new String[]{"record_id"}, "record_id = '"+ orderRecord.split("#&#")[0] +"'", null, null, null, null, null);
                            int recordIdIndex = cursor.getColumnIndex("record_id");
                            List<String> resultList = new ArrayList<>();
                            for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
                                resultList.add(cursor.getString(recordIdIndex));
                            }
                            cursor.close();
                            selectDb.close();

                            if(resultList.size() == 0){
                                SQLiteOpenHelper sqlite = new DBHelper(context);
                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                db.execSQL("insert into printinfo(record_id, table_id, table_no, print_context, current_state, create_date, print_date, print_count) " +
                                        "values('"+ orderRecord.split("#&#")[0] +"', '"+ orderRecord.split("#&#")[1] +"', '"+ orderRecord.split("#&#")[2] +"', '"+ orderRecord.split("#&#")[3] +"', "+ Integer.valueOf(orderRecord.split("#&#")[4]) +", '"+ orderRecord.split("#&#")[5] +"', '"+ orderRecord.split("#&#")[6] +"', "+ Integer.valueOf(orderRecord.split("#&#")[7]) +")");
                                db.close();
                            }
                        }
                        // 发送流水信息页面更新广播
                        if(ComFun.strNull(orderRecordFullSb.toString())){
                            Intent intentRecord = new Intent();
                            intentRecord.putExtra("orderRecordFull", orderRecordFullSb.toString().substring(0, orderRecordFullSb.toString().length() - 1));
                            intentRecord.setAction(OrderRecordActivity.MSG_REFDATA);
                            context.sendBroadcast(intentRecord);
                        }
                    }
                    // 发送主页面更新广播
                    Intent intent = new Intent();
                    intent.putExtra("newData", true);
                    intent.setAction(MainFragment.MSG_REFDATA);
                    context.sendBroadcast(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
