package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.UserPermissionEditActivity;
import com.bouilli.nxx.bouillihotel.action.MenuAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class AddNewUserTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String userInfoName;
    private String userInfoGroupId;
    private String userInfoSexId;
    private String userInfoAge;
    private String userInfoBirthday;
    private String userInfoPhone;

    public AddNewUserTask(Context context, String userInfoName, String userInfoGroupId, String userInfoSexId, String userInfoAge, String userInfoBirthday, String userInfoPhone){
        this.context = context;
        this.userInfoName = userInfoName;
        this.userInfoGroupId = userInfoGroupId;
        this.userInfoSexId = userInfoSexId;
        this.userInfoAge = userInfoAge;
        this.userInfoBirthday = userInfoBirthday;
        this.userInfoPhone = userInfoPhone;
    }

    @Override
    protected String doInBackground(Void... params) {
        return MenuAction.addNewUser(context, URIUtil.ADD_USER_URI, userInfoName, userInfoGroupId, userInfoSexId, userInfoAge, userInfoBirthday, userInfoPhone);
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
                msg.what = UserPermissionEditActivity.MSG_ADD_USER;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("addUserResult", "true");
                    String userId = jsob.getString("userId");
                    data.putString("userInfoGroupId", userInfoGroupId);
                    data.putString("userId", userId);
                    data.putString("userInfoName", userInfoName);
                    data.putString("userInfoAge", userInfoAge);
                    data.putString("userInfoSexId", userInfoSexId);
                    data.putString("userInfoBirthday", userInfoBirthday);
                    data.putString("userInfoPhone", userInfoPhone);
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("addUserResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("addUserResult", "time_out");
                }
                msg.setData(data);
                UserPermissionEditActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
