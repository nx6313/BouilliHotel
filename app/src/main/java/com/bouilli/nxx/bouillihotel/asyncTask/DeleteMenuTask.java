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

import java.util.List;

/**
 * Created by 18230 on 2016/10/30.
 */

public class DeleteMenuTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String deleteType;
    private List<String> deleteSelectMenuGroupIdList;
    private String menuInfoId;
    private String menuInfoNameFDel;

    public DeleteMenuTask(Context context, String deleteType, List<String> deleteSelectMenuGroupIdList, String menuInfoId, String menuInfoNameFDel){
        this.context = context;
        this.deleteType = deleteType;
        this.deleteSelectMenuGroupIdList = deleteSelectMenuGroupIdList;
        this.menuInfoId = menuInfoId;
        this.menuInfoNameFDel = menuInfoNameFDel;
    }

    @Override
    protected String doInBackground(Void... params) {
        return MenuAction.deleteMenu(context, URIUtil.DELETE_MENU_URI, deleteType, deleteSelectMenuGroupIdList, menuInfoId);
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
                msg.what = MenuEditActivity.MSG_DELETE_MENU;
                data.putString("deleteType", deleteType);
                StringBuilder deleteMenuGroupIds = new StringBuilder("");
                if(ComFun.strNull(deleteSelectMenuGroupIdList) && deleteSelectMenuGroupIdList.size() > 0){
                    for(String s : deleteSelectMenuGroupIdList){
                        deleteMenuGroupIds.append(s);
                        deleteMenuGroupIds.append(",");
                    }
                }
                if(ComFun.strNull(deleteMenuGroupIds.toString())){
                    data.putString("menuGroupIds", deleteMenuGroupIds.toString().substring(0, deleteMenuGroupIds.toString().length() - 1));
                }
                data.putString("menuInfoId", menuInfoId);
                data.putString("menuInfoNameFDel", menuInfoNameFDel);
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    data.putString("deleteMenuResult", "true");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("deleteMenuResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("deleteMenuResult", "time_out");
                }
                msg.setData(data);
                MenuEditActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
