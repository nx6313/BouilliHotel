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

public class SendMenuTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String tableNum;
    private int showType;
    private Map<String, Object[]> tableHasNewOrderMap;
    private String tableOrderId;

    public SendMenuTask(Context context, String tableNum, int showType, Map<String, Object[]> tableHasNewOrderMap, String tableOrderId){
        this.context = context;
        this.tableNum = tableNum;
        this.showType = showType;
        this.tableHasNewOrderMap = tableHasNewOrderMap;
        this.tableOrderId = tableOrderId;
    }

    @Override
    protected String doInBackground(Void... params) {
        return MenuAction.sendMenu(context, URIUtil.SEND_MENU_URI, tableNum, showType, tableHasNewOrderMap, tableOrderId);
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
                msg.what = EditOrderActivity.MSG_SEND_MENU;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("sendMenuResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("sendMenuResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("sendMenuResult", "time_out");
                }
                msg.setData(data);
                EditOrderActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
