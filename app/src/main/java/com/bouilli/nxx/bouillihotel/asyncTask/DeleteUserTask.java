package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

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

public class DeleteUserTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String userGroupId;
    private String userInfoId;

    public DeleteUserTask(Context context, String userInfoId, String userGroupId){
        this.context = context;
        this.userGroupId = userGroupId;
        this.userInfoId = userInfoId;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.deleteUserInfo(context, URIUtil.DELETE_USERS_URI, userInfoId);
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
                msg.what = UserPermissionEditActivity.MSG_DELETE_USER;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("deleteUserResult", "true");
                    data.putString("userGroupId", userGroupId);
                    data.putString("userInfoId", userInfoId);
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("deleteUserResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("deleteUserResult", "time_out");
                }
                msg.setData(data);
                UserPermissionEditActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
