package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.EditOrderActivity;
import com.bouilli.nxx.bouillihotel.action.MenuAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by 18230 on 2016/10/30.
 */

public class SettleAccountTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String tableOrderId;
    /********************* 小票使用参数 *********************/
    private String printAccountBillId;
    private String tableNo;
    private String printContext;

    public SettleAccountTask(Context context, String tableOrderId, String tableNo){
        this.context = context;
        this.tableOrderId = tableOrderId;
        this.tableNo = tableNo;
    }
    public SettleAccountTask(Context context, String tableOrderId, String printAccountBillId, String tableNo, String printContext){
        this.context = context;
        this.tableOrderId = tableOrderId;
        this.printAccountBillId = printAccountBillId;
        this.tableNo = tableNo;
        this.printContext = printContext;
    }

    @Override
    protected String doInBackground(Void... params) {
        return MenuAction.settleAccount(context, URIUtil.SETTLE_ACCOUNT, tableOrderId, printAccountBillId, tableNo, printContext);
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
                msg.what = EditOrderActivity.MSG_ACCOUNT;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("accountResult", "true");
                    data.putString("tableNo", tableNo);
                    data.putString("tableOrderId", tableOrderId);
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("accountResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("accountResult", "time_out");
                }
                msg.setData(data);
                EditOrderActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
