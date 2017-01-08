package com.bouilli.nxx.bouillihotel.asyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bouilli.nxx.bouillihotel.PrintAreaActivity;
import com.bouilli.nxx.bouillihotel.action.DataAction;
import com.bouilli.nxx.bouillihotel.customview.FlowLayout;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 18230 on 2016/10/30.
 */

public class AddPrintTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private String printName;
    private String printAddress;
    private String printInfoId;
    private String selectMenuAbout;
    private View v;
    private TextView tvPrintName;
    private EditText etPrintName;
    private TextView tvPrintAddress;
    private FlowLayout printAboutMenuGroupLayout;

    public AddPrintTask(Context context, String printName, String printAddress, String printInfoId, String selectMenuAbout, View v, TextView tvPrintName, EditText etPrintName, TextView tvPrintAddress, FlowLayout printAboutMenuGroupLayout){
        this.context = context;
        this.printName = printName;
        this.printAddress = printAddress;
        this.printInfoId = printInfoId;
        this.selectMenuAbout = selectMenuAbout;
        this.v = v;
        this.tvPrintName = tvPrintName;
        this.etPrintName = etPrintName;
        this.tvPrintAddress = tvPrintAddress;
        this.printAboutMenuGroupLayout = printAboutMenuGroupLayout;
    }

    @Override
    protected String doInBackground(Void... params) {
        return DataAction.addNewPrint(context, URIUtil.ADD_PRINT_URI, printName, printAddress, printInfoId, selectMenuAbout);
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
                msg.what = PrintAreaActivity.MSG_ADD_PRINT;
                if(responseCode.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)){
                    String printInfoId = jsob.getString("printInfoId");
                    v.setVisibility(View.GONE);
                    ((LinearLayout) v.getParent()).getChildAt(0).setVisibility(View.VISIBLE);
                    tvPrintName.setText(etPrintName.getText().toString().trim());
                    tvPrintName.setVisibility(View.VISIBLE);
                    etPrintName.setVisibility(View.GONE);
                    tvPrintAddress.setOnClickListener(null);
                    tvPrintAddress.setTag(printInfoId);
                    for(int p=0; p<printAboutMenuGroupLayout.getChildCount(); p++){
                        CheckBox menuGroupPrintCb = (CheckBox) printAboutMenuGroupLayout.getChildAt(p);
                        menuGroupPrintCb.setEnabled(false);
                    }
                    data.putString("addPrintResult", "true");
                    if(ComFun.strNull(printInfoId)){
                        data.putString("printInfoId", printInfoId);
                    }
                }else if(responseCode.equals(Constants.HTTP_REQUEST_FAIL_CODE)) {
                    data.putString("addPrintResult", "false");
                }else if(responseCode.equals(Constants.HTTP_REQUEST_OUT_TIME_CODE)) {
                    data.putString("addPrintResult", "time_out");
                }
                msg.setData(data);
                PrintAreaActivity.mHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
