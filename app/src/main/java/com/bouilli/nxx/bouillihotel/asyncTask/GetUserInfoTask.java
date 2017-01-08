package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.EditOrderActivity;
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

public class GetUserInfoTask extends AsyncTask<Void, Void, String> {
    private Context context;

    public GetUserInfoTask(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.getUserInfo(context, URIUtil.GET_USERS_URI);
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
                msg.what = UserPermissionEditActivity.MSG_INIT_USER;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("initUserResult", "true");
                    JSONObject userInfoMap = jsob.getJSONObject("userInfoMap");
                    if(userInfoMap.length() > 0){
                        for(int i=0; i<=4; i++){
                            if(userInfoMap.has("userType"+i)){
                                StringBuilder userInfoSb = new StringBuilder("");
                                for (int j = 0; j < userInfoMap.getJSONArray("userType"+i).length(); j++) {
                                    String thisGroupUserInfo = (String) userInfoMap.getJSONArray("userType"+i).get(j);
                                    userInfoSb.append(thisGroupUserInfo + ",");
                                }
                                if(ComFun.strNull(userInfoSb.toString())){
                                    data.putString("userType"+i, userInfoSb.toString().substring(0, userInfoSb.toString().length() - 1));
                                }
                            }
                        }
                    }
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("initUserResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("initUserResult", "time_out");
                }
                msg.setData(data);
                UserPermissionEditActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
