package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.BusinessActivity;
import com.bouilli.nxx.bouillihotel.PerformanceAssessActivity;
import com.bouilli.nxx.bouillihotel.action.DataAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class InitUserTurnoverTask extends AsyncTask<Void, Void, String> {
    private Context context;

    public InitUserTurnoverTask(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.getMonthTurnover(context, URIUtil.GET_USER_TURNOVER_URI);
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
                msg.what = PerformanceAssessActivity.MSG_INIT_USER_TURNOVER;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("initUserTurnoverResult", "true");
                    if(jsob.has("weakUserDataJson")){
                        data.putString("weakUserDataJson", jsob.getString("weakUserDataJson"));
                    }
                    if(jsob.has("monthUserDataJson")){
                        data.putString("monthUserDataJson", jsob.getString("monthUserDataJson"));
                    }
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("initUserTurnoverResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("initUserTurnoverResult", "time_out");
                }
                msg.setData(data);
                PerformanceAssessActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
