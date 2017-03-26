package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.WelcomeActivity;
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

public class UserLoginTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String loginName;
    private String loginPwd;

    public UserLoginTask(Context context, String loginName, String loginPwd){
        this.context = context;
        this.loginName = loginName;
        this.loginPwd = loginPwd;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.userLogin(context, URIUtil.USER_LOGIN_URI, loginName, loginPwd);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // 发送Handler通知页面更新UI
        Message msg = new Message();
        Bundle data = new Bundle();
        if(ComFun.strNull(result)){
            try {
                JSONObject jsob = new JSONObject(result);
                String responseCode = jsob.getString("responseCode");
                msg.what = WelcomeActivity.MSG_USER_LOGIN;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("userLoginResult", "true");
                    if(jsob.has("loginUserInfo")){
                        String loginUserInfo = jsob.getString("loginUserInfo");
                        data.putString("loginUserInfo", loginUserInfo);
                        String userId = loginUserInfo.split("#&#")[0];
                        String userPermission = loginUserInfo.split("#&#")[1];
                        String userLoginName = loginUserInfo.split("#&#")[2];
                        String userRealName = loginUserInfo.split("#&#")[3];
                        String userSex = loginUserInfo.split("#&#")[4];
                        String userBirthday = loginUserInfo.split("#&#")[5];// 12-06
                        String userMobel = loginUserInfo.split("#&#")[6];
                        String userPwd = loginUserInfo.split("#&#")[7];
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "userId", userId);
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "userPwd", userPwd);
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "userPermission", userPermission);
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "userLoginName", userLoginName);
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "userRealName", userRealName);
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "userSex", userSex);
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "userBirthday", userBirthday);
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "userMobel", userMobel);
                        // 打票机相关
                        if(jsob.has("loginUserPrintSetAbout")){
                            String loginUserPrintSetAbout = jsob.getString("loginUserPrintSetAbout");
                            String printAreaId = loginUserPrintSetAbout.split("#&#")[0];
                            String printAddress = loginUserPrintSetAbout.split("#&#")[1];
                            String printAboutMenuGroupId = loginUserPrintSetAbout.split("#&#")[1];
                            SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "printAreaId", printAreaId);
                            SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "printAddress", printAddress);
                            SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "printAboutMenuGroupId", printAboutMenuGroupId);
                        }
                        // 登录时，默认将上次登录是否退出的缓存值保存为false，未退出
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "hasExitLast", "false");

                        // 将推送注册标记值置空
                        SharedPreferencesTool.addOrUpdate(context, com.bouilli.nxx.bouillihotel.push.org.androidpn.client.Constants.SHARED_PREFERENCE_NAME, com.bouilli.nxx.bouillihotel.push.org.androidpn.client.Constants.XMPP_AUTHORIZED, "");
                    }
                }else if(responseCode.equals(Constants.HTTP_REQUEST_LOGIN_ERROR_CODE)) {
                    data.putString("userLoginResult", "error");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("userLoginResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("userLoginResult", "time_out");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        msg.setData(data);
        WelcomeActivity.mHandler.sendMessage(msg);
    }

}
