package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.TableEditActivity;
import com.bouilli.nxx.bouillihotel.action.TableAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class AddNewTableTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private boolean addNewFlag = true;
    private String groupName;
    private String groupNo;
    private String startNum;
    private String endNum;
    private String tableNums;

    public AddNewTableTask(Context context, String groupName, String groupNo, String startNum, String endNum, String tableNums, boolean addNewFlag){
        this.context = context;
        this.groupName = groupName;
        this.groupNo = groupNo;
        this.startNum = startNum;
        this.endNum = endNum;
        this.tableNums = tableNums;
        this.addNewFlag = addNewFlag;
    }

    @Override
    protected String doInBackground(Void... params) {
        return TableAction.addNewTable(context, URIUtil.ADD_NEW_TABLE_URI, groupName, groupNo, startNum, endNum, tableNums, addNewFlag);
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
                if(addNewFlag){
                    msg.what = TableEditActivity.MSG_ADD_TABLE;
                }else{
                    msg.what = TableEditActivity.MSG_ADD_TABLE_TO_COM;
                }
                data.putString("groupName", groupName);
                data.putString("groupNo", groupNo);
                data.putString("startNum", startNum);
                data.putString("endNum", endNum);
                data.putString("tableNums", tableNums);
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("addNewTableResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("addNewTableResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("addNewTableResult", "time_out");
                }else if(responseCode.equals(Constants.REQUEST_CODE_HAS)) {
                    data.putString("addNewTableResult", "has_table_group");
                }
                msg.setData(data);
                TableEditActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
