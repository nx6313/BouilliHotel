package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;

import com.bouilli.nxx.bouillihotel.PrintAreaActivity;
import com.bouilli.nxx.bouillihotel.action.DataAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class DeletePrintTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String printInfoId;
    private LinearLayout printAreaSetMainLayout;
    private View v;

    public DeletePrintTask(Context context, String printInfoId, LinearLayout printAreaSetMainLayout, View v){
        this.context = context;
        this.printInfoId = printInfoId;
        this.printAreaSetMainLayout = printAreaSetMainLayout;
        this.v = v;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.deletePrint(context, URIUtil.DELETE_PRINT_URI, printInfoId);
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
                msg.what = PrintAreaActivity.MSG_DELETE_PRINT;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    printAreaSetMainLayout.removeView(v);
                    data.putString("deletePrintResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("deletePrintResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("deletePrintResult", "time_out");
                }
                msg.setData(data);
                PrintAreaActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
