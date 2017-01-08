package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.EditOrderActivity;
import com.bouilli.nxx.bouillihotel.PrintAreaActivity;
import com.bouilli.nxx.bouillihotel.UserPermissionEditActivity;
import com.bouilli.nxx.bouillihotel.action.DataAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class GetPrintInfoTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private boolean accountNeedFlag = false;

    public GetPrintInfoTask(Context context){
        this.context = context;
    }
    public GetPrintInfoTask(Context context, boolean accountNeedFlag){
        this.context = context;
        this.accountNeedFlag = accountNeedFlag;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.getPrintInfo(context, URIUtil.GET_PRINTS_URI);
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
                if(accountNeedFlag){
                    msg.what = EditOrderActivity.MSG_GET_PRINT_INFO_ACCOUNT_NEED;
                }else{
                    msg.what = PrintAreaActivity.MSG_INIT_PRINT;
                }
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("initPrintResult", "true");
                    if(jsob.has("printList")){
                        JSONArray printList = jsob.getJSONArray("printList");
                        StringBuilder printInfoSb = new StringBuilder("");
                        for (int i = 0; i < printList.length(); i++) {
                            String printInfo = (String) printList.get(i);
                            if(ComFun.strNull(printInfo)){
                                printInfoSb.append(printInfo);
                                printInfoSb.append(",");
                            }
                        }
                        if(ComFun.strNull(printInfoSb.toString())){
                            data.putString("AllPrintsInfo", printInfoSb.toString().substring(0, printInfoSb.toString().length() - 1));
                        }
                    }
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("initPrintResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("initPrintResult", "time_out");
                }
                msg.setData(data);
                if(accountNeedFlag){
                    EditOrderActivity.mHandler.sendMessage(msg);
                }else{
                    PrintAreaActivity.mHandler.sendMessage(msg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
