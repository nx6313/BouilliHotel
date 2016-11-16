package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.bouilli.nxx.bouillihotel.MenuEditActivity;
import com.bouilli.nxx.bouillihotel.action.MenuAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class AddNewMenuTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String groupName;
    private String selectMenuGroupId;
    private String menuInfoName;
    private String menuInfoDes;
    private String menuInfoPrice;
    private String addType;

    public AddNewMenuTask(Context context, String groupName, String menuInfoName, String selectMenuGroupId, String menuInfoDes, String menuInfoPrice, String addType){
        this.context = context;
        this.groupName = groupName;
        this.menuInfoName = menuInfoName;
        this.selectMenuGroupId = selectMenuGroupId;
        this.menuInfoDes = menuInfoDes;
        this.menuInfoPrice = menuInfoPrice;
        this.addType = addType;
    }

    @Override
    protected String doInBackground(Void... params) {
        return MenuAction.addNewMenu(context, URIUtil.ADD_NEW_MENU_URI, groupName, menuInfoName, selectMenuGroupId, menuInfoDes, menuInfoPrice, addType);
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
                msg.what = MenuEditActivity.MSG_ADD_MENU;
                if(jsob.has("otherData")){
                    String otherData = jsob.getString("otherData");
                    data.putString("otherData", otherData);
                }
                data.putString("addType", addType);
                data.putString("groupName", groupName);
                data.putString("menuInfoName", menuInfoName);
                data.putString("selectMenuGroupId", selectMenuGroupId);
                data.putString("menuInfoDes", menuInfoDes);
                data.putString("menuInfoPrice", menuInfoPrice);
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("addNewMenuResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("addNewMenuResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("addNewMenuResult", "time_out");
                }else if(responseCode.equals(Constants.REQUEST_CODE_HAS)) {
                    data.putString("addNewMenuResult", "has_menu_group");
                }
                msg.setData(data);
                MenuEditActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
