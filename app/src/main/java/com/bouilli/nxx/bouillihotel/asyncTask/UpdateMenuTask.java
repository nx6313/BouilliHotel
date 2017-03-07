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

public class UpdateMenuTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String menuId;
    private String menuInfoName;
    private String oldMenuInGroupId;
    private String selectMenuGroupId;
    private String menuInfoDes;
    private String menuInfoPrice;

    public UpdateMenuTask(Context context, String menuId, String menuInfoName, String oldMenuInGroupId, String selectMenuGroupId, String menuInfoDes, String menuInfoPrice){
        this.context = context;
        this.menuId = menuId;
        this.menuInfoName = menuInfoName;
        this.oldMenuInGroupId = oldMenuInGroupId;
        this.selectMenuGroupId = selectMenuGroupId;
        this.menuInfoDes = menuInfoDes;
        this.menuInfoPrice = menuInfoPrice;
    }

    @Override
    protected String doInBackground(Void... params) {
        return MenuAction.updateMenu(context, URIUtil.UPDATE_MENU_URI, menuId, menuInfoName, selectMenuGroupId, menuInfoDes, menuInfoPrice);
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
                msg.what = MenuEditActivity.MSG_UPDATE_MENU;
                data.putString("menuId", menuId);
                data.putString("menuInfoName", menuInfoName);
                data.putString("oldMenuInGroupId", oldMenuInGroupId);
                data.putString("selectMenuGroupId", selectMenuGroupId);
                data.putString("menuInfoDes", menuInfoDes);
                data.putString("menuInfoPrice", menuInfoPrice);
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("updateMenuResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("updateMenuResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("updateMenuResult", "time_out");
                }
                msg.setData(data);
                MenuEditActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
