package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.EditOrderActivity;
import com.bouilli.nxx.bouillihotel.MainActivity;
import com.bouilli.nxx.bouillihotel.OutOrderActivity;
import com.bouilli.nxx.bouillihotel.action.MenuAction;
import com.bouilli.nxx.bouillihotel.fragment.MainFragment;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.L;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by 18230 on 2016/10/30.
 */

public class GetMenuInThisTableTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String tableOrderId;
    private boolean seeFlag = false;// 标记是非员工获取点餐信息（不进入后台）
    private String tableNum;// seeFlag为true时使用（不进入后台）
    private boolean seeFlagForOut = false;// 标记是非员工获取点餐信息/外卖情况（不进入后台）

    public GetMenuInThisTableTask(Context context, String tableOrderId){
        this.context = context;
        this.tableOrderId = tableOrderId;
    }
    public GetMenuInThisTableTask(Context context, String tableOrderId, boolean seeFlag, boolean seeFlagForOut, String tableNum){
        this.context = context;
        this.tableOrderId = tableOrderId;
        this.seeFlag = seeFlag;
        this.seeFlagForOut = seeFlagForOut;
        this.tableNum = tableNum;
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
                if(jsob.has("orderInfoDetailAllMap")){
                    JSONObject orderInfoDetailAllMap = jsob.getJSONObject("orderInfoDetailAllMap");
                    String[] tableOrderIdArr = tableOrderId.split("#");
                    for (int i = 0; i < tableOrderIdArr.length; i++) {
                        String tableOrderId = tableOrderIdArr[i];
                        for(int j = 0; j < orderInfoDetailAllMap.getJSONArray(tableOrderId).length(); j++){
                            orderInfoDetailSb.append(orderInfoDetailAllMap.getJSONArray(tableOrderId).get(j) + ",");
                        }
                        if(ComFun.strNull(orderInfoDetailSb.toString())){
                            orderInfoDetailSb = new StringBuilder(orderInfoDetailSb.toString().substring(0, orderInfoDetailSb.toString().length() - 1));
                        }
                        orderInfoDetailSb.append("||#|#|#||");
                    }
                    if(ComFun.strNull(orderInfoDetailSb.toString())){
                        data.putString("orderInfoDetails", orderInfoDetailSb.toString().substring(0, orderInfoDetailSb.toString().length() - "||#|#|#||".length()));
                    }
                }
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("getTableOrderInfoResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("getTableOrderInfoResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("getTableOrderInfoResult", "time_out");
                }
                if(!seeFlag){
                    msg.what = EditOrderActivity.MSG_GET_TABLE_ORDER_INFO;
                    msg.setData(data);
                    EditOrderActivity.mHandler.sendMessage(msg);
                }else{
                    if(seeFlagForOut){
                        msg.what = OutOrderActivity.MSG_SEE_TABLE_INFO;
                        data.putString("tableNum", tableNum);
                        msg.setData(data);
                        OutOrderActivity.mHandler.sendMessage(msg);
                    }else{
                        msg.what = MainActivity.MSG_SEE_TABLE_INFO;
                        data.putString("tableNum", tableNum);
                        msg.setData(data);
                        MainActivity.mHandler.sendMessage(msg);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
