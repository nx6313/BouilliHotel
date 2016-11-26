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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class GetMenuInThisTableTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String tableOrderId;

    public GetMenuInThisTableTask(Context context, String tableOrderId){
        this.context = context;
        this.tableOrderId = tableOrderId;
    }

    @Override
    protected String doInBackground(Void... params) {
        return MenuAction.getMenuInThisTable(context, URIUtil.GET_TABLE_ORDER_INFO_URI, tableOrderId);
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
                StringBuilder orderInfoDetailSb = new StringBuilder("");
                if(jsob.has("orderInfoDetailList")){
                    JSONArray orderInfoDetailList = jsob.getJSONArray("orderInfoDetailList");
                    for (int i = 0; i < orderInfoDetailList.length(); i++) {
                        String orderInfoDetail = (String) orderInfoDetailList.get(i);
                        orderInfoDetailSb.append(orderInfoDetail + ",");
                    }
                    if(ComFun.strNull(orderInfoDetailSb.toString())){
                        data.putString("orderInfoDetails", orderInfoDetailSb.toString().substring(0, orderInfoDetailSb.toString().length() - 1));
                    }
                }
                msg.what = EditOrderActivity.MSG_GET_TABLE_ORDER_INFO;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("getTableOrderInfoResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("getTableOrderInfoResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("getTableOrderInfoResult", "time_out");
                }
                msg.setData(data);
                EditOrderActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
