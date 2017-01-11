package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.MainActivity;
import com.bouilli.nxx.bouillihotel.action.DataAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class CheckVersionTask extends AsyncTask<Void, Void, String> {
    private Context context;

    public CheckVersionTask(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.checkVersion(context, URIUtil.CHECK_VERSION_URI);
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
                msg.what = MainActivity.MSG_CHECK_NEW_VERSION;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("checkNewVersionResult", "true");
                    if(jsob.has("lastVersionNo") && jsob.has("lastVersionName") && jsob.has("lastVersionContent")){
                        data.putInt("lastVersionNo", jsob.getInt("lastVersionNo"));
                        data.putString("lastVersionName", jsob.getString("lastVersionName"));
                        data.putString("lastVersionContent", jsob.getString("lastVersionContent"));
                        // 将更新内容存入配置文件BouilliProInfo
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "newVersionName", jsob.getInt("lastVersionNo"));
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "newVersionName", jsob.getString("lastVersionName"));
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "newVersionContent", jsob.getString("lastVersionContent"));
                    }
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("checkNewVersionResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("checkNewVersionResult", "time_out");
                }else if(responseCode.equals("none")) {
                    data.putString("checkNewVersionResult", "none");
                }
                msg.setData(data);
                MainActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
