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

public class MoveMenuTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String menuItemInfo;
    private String moveMenuId;
    private String moveMenuName;
    private String moveMenuGroupId;
    private String moveMenuToGroupId;

    public MoveMenuTask(Context context, String menuItemInfo, String moveMenuId, String moveMenuName, String moveMenuGroupId, String moveMenuToGroupId){
        this.context = context;
        this.menuItemInfo = menuItemInfo;
        this.moveMenuId = moveMenuId;
        this.moveMenuName = moveMenuName;
        this.moveMenuGroupId = moveMenuGroupId;
        this.moveMenuToGroupId = moveMenuToGroupId;
    }

    @Override
    protected String doInBackground(Void... params) {
        return MenuAction.moveMenu(context, URIUtil.MOVE_MENU_URI, moveMenuId, moveMenuGroupId, moveMenuToGroupId);
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
                msg.what = MenuEditActivity.MSG_MOVE_MENU;
                data.putString("menuInfoNameFMove", moveMenuName);
                data.putString("menuItemInfo", menuItemInfo);
                data.putString("moveMenuId", moveMenuId);
                data.putString("moveMenuGroupId", moveMenuGroupId);
                data.putString("moveMenuToGroupId", moveMenuToGroupId);
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("moveMenuResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("moveMenuResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("moveMenuResult", "time_out");
                }
                msg.setData(data);
                MenuEditActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
