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

import java.util.List;

/**
 * Created by 18230 on 2016/10/30.
 */

public class DeleteTableTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String deleteTableType;// table：删除餐桌、group：删除桌组
    private List<String> deleteSelectTableGroupNameList = null;
    private String deleteTableInfo = null;

    public DeleteTableTask(Context context, String deleteTableType, List<String> deleteSelectTableGroupNameList, String deleteTableInfo){
        this.context = context;
        this.deleteTableType = deleteTableType;
        this.deleteSelectTableGroupNameList = deleteSelectTableGroupNameList;
        this.deleteTableInfo = deleteTableInfo;
    }

    @Override
    protected String doInBackground(Void... params) {
        return TableAction.deleteTable(context, URIUtil.DELETE_TABLE_URI, deleteTableType, deleteSelectTableGroupNameList, deleteTableInfo);
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
                if(deleteTableType.equals("group")){
                    msg.what = TableEditActivity.MSG_DELETE_GROUP;
                }else{
                    msg.what = TableEditActivity.MSG_DELETE_TABLE;
                }
                data.putString("deleteTableInfo", deleteTableInfo);
                if(deleteSelectTableGroupNameList != null && deleteSelectTableGroupNameList.size() > 0){
                    StringBuilder sb = new StringBuilder("");
                    for(String s : deleteSelectTableGroupNameList){
                        sb.append(s);
                        sb.append(",");
                    }
                    data.putString("deleteTableGroup", sb.toString().substring(0, sb.toString().length() - 1));
                }
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("deleteTableResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("deleteTableResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("deleteTableResult", "time_out");
                }
                msg.setData(data);
                TableEditActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
