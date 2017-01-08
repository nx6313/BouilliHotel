package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.PrintAreaActivity;
import com.bouilli.nxx.bouillihotel.UserPermissionEditActivity;
import com.bouilli.nxx.bouillihotel.action.DataAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class SaveUserPrintSetTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String selectPrintAreaId;

    public SaveUserPrintSetTask(Context context, String selectPrintAreaId){
        this.context = context;
        this.selectPrintAreaId = selectPrintAreaId;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.saveUserPrintSet(context, URIUtil.SAVE_USER_PRINT_SET_URI, selectPrintAreaId);
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
                msg.what = PrintAreaActivity.MSG_SAVE_USER_PRINT_SET;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("saveUserPrintSetResult", "true");
                    data.putString("selectPrintAreaId", selectPrintAreaId);
                    String printAddress = jsob.getString("printAddress");
                    data.putString("printAddress", printAddress);
                    String printAboutMenuGroupId = jsob.getString("printAboutMenuGroupId");
                    data.putString("printAboutMenuGroupId", printAboutMenuGroupId);
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("saveUserPrintSetResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("saveUserPrintSetResult", "time_out");
                }
                msg.setData(data);
                PrintAreaActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
