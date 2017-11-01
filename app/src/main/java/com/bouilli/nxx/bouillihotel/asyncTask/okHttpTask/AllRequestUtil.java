package com.bouilli.nxx.bouillihotel.asyncTask.okHttpTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import com.bouilli.nxx.bouillihotel.BusinessActivity;
import com.bouilli.nxx.bouillihotel.EditOrderActivity;
import com.bouilli.nxx.bouillihotel.MainActivity;
import com.bouilli.nxx.bouillihotel.MenuEditActivity;
import com.bouilli.nxx.bouillihotel.MyApplication;
import com.bouilli.nxx.bouillihotel.OrderRecordActivity;
import com.bouilli.nxx.bouillihotel.OutOrderActivity;
import com.bouilli.nxx.bouillihotel.PerformanceAssessActivity;
import com.bouilli.nxx.bouillihotel.PrintAreaActivity;
import com.bouilli.nxx.bouillihotel.TableEditActivity;
import com.bouilli.nxx.bouillihotel.WelcomeActivity;
import com.bouilli.nxx.bouillihotel.broadcastReceiver.BouilliBroadcastReceiver;
import com.bouilli.nxx.bouillihotel.db.DBHelper;
import com.bouilli.nxx.bouillihotel.entity.TableGroup;
import com.bouilli.nxx.bouillihotel.entity.build.TableGroupDao;
import com.bouilli.nxx.bouillihotel.fragment.MainFragment;
import com.bouilli.nxx.bouillihotel.fragment.OutOrderFragment;
import com.bouilli.nxx.bouillihotel.okHttpUtil.CommonOkHttpClient;
import com.bouilli.nxx.bouillihotel.okHttpUtil.exception.OkHttpException;
import com.bouilli.nxx.bouillihotel.okHttpUtil.listener.DisposeDataHandle;
import com.bouilli.nxx.bouillihotel.okHttpUtil.listener.DisposeDataListener;
import com.bouilli.nxx.bouillihotel.okHttpUtil.request.CommonRequest;
import com.bouilli.nxx.bouillihotel.okHttpUtil.request.RequestParams;
import com.bouilli.nxx.bouillihotel.service.PollingService;
import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;
import com.bouilli.nxx.bouillihotel.util.URIUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;

/**
 * Created by 18230 on 2017/3/26.
 */

public class AllRequestUtil {

    /**
     * 用户登录
     *
     * @param context
     * @param params
     */
    public static Call UserLogin(final Context context, RequestParams params) {
        Call loginCall = CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.USER_LOGIN_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = WelcomeActivity.MSG_USER_LOGIN;
                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    if (jsob.has("loginUserInfo")) {
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
                        // 打票机相关(终端机参数--关联打票机)
                        if (jsob.has("loginUserPrintSetAbout")) {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setData(data);
                WelcomeActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                    ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_LOGIN_ERROR_ERROR) {
                    ComFun.showToast(context, "登录失败，用户名或密码错误", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                    ComFun.showToast(context, "登录失败，请联系管理员", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(context, "登录超时，请稍后重试", Toast.LENGTH_SHORT);
                }
            }
        }));
        return loginCall;
    }

    /**
     * 初始化系统基本参数
     *
     * @param context
     * @param params
     */
    public static void InitBaseData(final Context context, RequestParams params, final boolean forPollingServiceFlag) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.INIT_BASE_DATA_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                if (forPollingServiceFlag) {
                    // PollingService,继续执行InitOrderData
                    InitOrderData(context, null);
                }
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    if (!forPollingServiceFlag) {
                        // 当是启动程序时，并且成功获取到程序基本数据时，清除已经保存的所有的SharedPreferences文件
                        SharedPreferencesTool.clearShared(context, new String[]{"BouilliTableInfo", "BouilliMenuInfo"});
                    }
                    data.putString("initBaseDataResult", "true");
                    // 保存基本数据至本地
                    // 餐桌数据
                    StringBuilder tableGroupNameSb = new StringBuilder("");
                    StringBuilder tableNumSimpleSb;
                    StringBuilder tableNumSb;
                    if (jsob.has("groupNameList") && jsob.has("tableInfoMap")) {
                        JSONArray groupNameList = jsob.getJSONArray("groupNameList");
                        JSONObject tableInfoMap = jsob.getJSONObject("tableInfoMap");
                        StringBuilder tableFullNumSb = new StringBuilder("");
                        for (int i = 0; i < groupNameList.length(); i++) {
                            String groupName = (String) groupNameList.get(i);
                            tableGroupNameSb.append(groupName + ",");
                            tableNumSimpleSb = new StringBuilder("");
                            tableNumSb = new StringBuilder("");
                            for (int j = 0; j < tableInfoMap.getJSONArray(groupName).length(); j++) {
                                String thisGroupTableNum = (String) tableInfoMap.getJSONArray(groupName).get(j);
                                tableNumSimpleSb.append(thisGroupTableNum.split("\\|")[0] + ",");// 记录餐桌 组代号.餐桌号
                                tableNumSb.append(thisGroupTableNum + ",");// 记录每个餐桌组内的餐桌信息（组代号.餐桌号|餐桌状态|餐桌当前就餐信息id）
                                tableFullNumSb.append(thisGroupTableNum + ",");// 记录所有餐桌组内的餐桌信息（组代号.餐桌号|餐桌状态|餐桌当前就餐信息id）
                            }
                            if (ComFun.strNull(tableNumSb.toString())) {
                                SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableInfoSimple" + groupName, tableNumSimpleSb.toString().substring(0, tableNumSimpleSb.toString().length() - 1));
                                SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableInfo" + groupName, tableNumSb.toString().substring(0, tableNumSb.toString().length() - 1));
                            } else {
                                SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableInfoSimple" + groupName, "");
                                SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableInfo" + groupName, "");
                            }
                        }
                        if (ComFun.strNull(tableGroupNameSb.toString())) {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableGroupNames", tableGroupNameSb.toString().substring(0, tableGroupNameSb.toString().length() - 1));
                        } else {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableGroupNames", "");
                        }

                        if (ComFun.strNull(tableFullNumSb.toString())) {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableFullInfo", tableFullNumSb.toString().substring(0, tableFullNumSb.toString().length() - 1));
                        } else {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableFullInfo", "");
                        }
                    } else {
                        // 数据集为空
                        SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableGroupNames", "");
                        SharedPreferencesTool.addOrUpdate(context, "BouilliTableInfo", "tableFullInfo", "");
                    }
                    // 菜单数据
                    if (jsob.has("menuGroupNameList") && jsob.has("menuInfoMap")) {
                        JSONArray menuGroupNameList = jsob.getJSONArray("menuGroupNameList");
                        JSONObject menuInfoMap = jsob.getJSONObject("menuInfoMap");
                        StringBuilder menuGroupNamesFullSb = new StringBuilder("");
                        StringBuilder menuAllGroupChildFullSb = new StringBuilder("");
                        StringBuilder menuGroupChildFullSb;
                        for (int i = 0; i < menuGroupNameList.length(); i++) {
                            String groupName = (String) menuGroupNameList.get(i);
                            menuGroupNamesFullSb.append(groupName + ",");
                            menuGroupChildFullSb = new StringBuilder("");
                            for (int j = 0; j < menuInfoMap.getJSONArray(groupName.split("#&#")[0]).length(); j++) {
                                String thisGroupMenuInfo = (String) menuInfoMap.getJSONArray(groupName.split("#&#")[0]).get(j);
                                // Id&GroupId&MenuName&MenuDes&Price&UseCount
                                if (!thisGroupMenuInfo.equals("-")) {
                                    menuAllGroupChildFullSb.append(thisGroupMenuInfo + ",");
                                    menuGroupChildFullSb.append(thisGroupMenuInfo + ",");
                                    SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", thisGroupMenuInfo.split("#&#")[0], thisGroupMenuInfo);
                                }
                            }
                            if (ComFun.strNull(menuGroupChildFullSb.toString())) {
                                SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuItemChild" + groupName.split("#&#")[0], menuGroupChildFullSb.toString().substring(0, menuGroupChildFullSb.toString().length() - 1));
                            }
                        }
                        if (ComFun.strNull(menuGroupNamesFullSb.toString())) {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuGroupNames", menuGroupNamesFullSb.toString().substring(0, menuGroupNamesFullSb.toString().length() - 1));
                        } else {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuGroupNames", "");
                        }
                        if (ComFun.strNull(menuAllGroupChildFullSb.toString())) {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuAllItemChild", menuAllGroupChildFullSb.toString().substring(0, menuAllGroupChildFullSb.toString().length() - 1));
                        } else {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuAllItemChild", "");
                        }
                    } else {
                        // 数据集为空
                        SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "menuGroupNames", "");
                    }
                    // 常用菜品数据
                    if (jsob.has("ofenUseMenuInfoList")) {
                        JSONArray ofenUseMenuInfoList = jsob.getJSONArray("ofenUseMenuInfoList");
                        StringBuilder oftenUseMenuSb = new StringBuilder("");
                        for (int i = 0; i < ofenUseMenuInfoList.length(); i++) {
                            oftenUseMenuSb.append(ofenUseMenuInfoList.get(i));
                            oftenUseMenuSb.append(",");
                        }
                        if (ComFun.strNull(oftenUseMenuSb.toString())) {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "oftenUseMenus", oftenUseMenuSb.toString().substring(0, oftenUseMenuSb.toString().length() - 1));
                        }
                    }
                    // 当前程序版本数据(在程序欢迎页面初始化数据时更改该相关数据)
                    if (!forPollingServiceFlag) {
                        if (jsob.has("lastVersionNo") && jsob.has("lastVersionName") && jsob.has("lastVersionContent")) {
                            // 将更新内容存入配置文件BouilliProInfo
                            SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "newVersionNo", jsob.getInt("lastVersionNo"));
                            SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "newVersionName", jsob.getString("lastVersionName"));
                            SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "newVersionContent", jsob.getString("lastVersionContent"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!forPollingServiceFlag) {
                    msg.what = WelcomeActivity.MSG_INIT_BASE_DATA;
                    msg.setData(data);
                    WelcomeActivity.mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (!forPollingServiceFlag) {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "初始化数据失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "初始化数据超时，请稍后重试", Toast.LENGTH_SHORT);
                    }

                    // 发送Handler通知页面更新UI
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    msg.what = WelcomeActivity.MSG_INIT_BASE_DATA;
                    data.putString("initBaseDataResult", "false");
                    msg.setData(data);
                    WelcomeActivity.mHandler.sendMessage(msg);
                }
            }
        }));
    }

    /**
     * 初始化订单数据
     *
     * @param context
     * @param params
     */
    public static void InitOrderData(final Context context, RequestParams params) {
        // 检测用户权限等信息，如果是传菜员权限登录，则额外传传菜员Id参数值
        String userPermission = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userPermission");
        String hasExitLast = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "hasExitLast");
        boolean forPrintFlag;
        if (ComFun.strNull(userPermission) && Integer.parseInt(userPermission) == 3 && ComFun.strNull(hasExitLast) && hasExitLast.equals("false")) {
            forPrintFlag = true;
            params = new RequestParams();
            String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
            params.put("forPrintServiceUserId", userId);
        } else {
            forPrintFlag = false;
        }
        final boolean forPrintServiceFlag = forPrintFlag;
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.INIT_ORDER_DATA_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                // PollingService,继续执行InitChatData
                String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
                RequestParams getChatDataParams = new RequestParams();
                getChatDataParams.put("userId", userId);
                InitChatData(context, getChatDataParams);
            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    // 保存订单数据至本地
                    if (jsob.has("orderRecordList")) {
                        JSONArray orderRecordList = jsob.getJSONArray("orderRecordList");
                        StringBuilder orderRecordFullSb = new StringBuilder("");
                        for (int i = 0; i < orderRecordList.length(); i++) {
                            String orderRecord = (String) orderRecordList.get(i);// 订单流水信息
                            orderRecordFullSb.append(orderRecord);
                            orderRecordFullSb.append(",");
                        }
                        // 发送流水信息页面更新广播
                        if (ComFun.strNull(orderRecordFullSb.toString())) {
                            Intent intentRecord = new Intent();
                            intentRecord.putExtra("orderRecordFull", orderRecordFullSb.toString().substring(0, orderRecordFullSb.toString().length() - 1));
                            intentRecord.setAction(OrderRecordActivity.MSG_REFDATA);
                            context.sendBroadcast(intentRecord);
                        } else {// 发送空数据广播
                            Intent intentRecord = new Intent();
                            intentRecord.putExtra("orderRecordFull", "");
                            intentRecord.setAction(OrderRecordActivity.MSG_REFDATA);
                            context.sendBroadcast(intentRecord);
                        }
                    }
                    // 打包、外卖数据
                    StringBuilder wmRefSb = new StringBuilder("");
                    if (jsob.has("wmList")) {
                        JSONArray wmList = jsob.getJSONArray("wmList");
                        StringBuilder wmInfoSb = new StringBuilder("");
                        for (int i = 0; i < wmList.length(); i++) {
                            String wmDetailInfo = (String) wmList.get(i);
                            wmInfoSb.append(wmDetailInfo);
                            wmRefSb.append("No." + ((wmDetailInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((wmDetailInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                            if (i < wmList.length()) {
                                wmInfoSb.append("#@#,#");
                                wmRefSb.append("#@#,#");
                            }
                        }
                        if (ComFun.strNull(wmInfoSb.toString())) {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "wmInfos", wmInfoSb.toString());
                        }
                    }
                    StringBuilder dbRefSb = new StringBuilder("");
                    if (jsob.has("dbList")) {
                        JSONArray dbList = jsob.getJSONArray("dbList");
                        StringBuilder dbInfoSb = new StringBuilder("");
                        for (int i = 0; i < dbList.length(); i++) {
                            String dbDetailInfo = (String) dbList.get(i);
                            dbInfoSb.append(dbDetailInfo);
                            dbRefSb.append("No." + ((dbDetailInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).substring(2, ((dbDetailInfo.split("#&&#")[0]).split("#&#")[1].split(">>")[1]).length()));
                            if (i < dbList.length()) {
                                dbInfoSb.append("#@#,#");
                                dbRefSb.append("#@#,#");
                            }
                        }
                        if (ComFun.strNull(dbInfoSb.toString())) {
                            SharedPreferencesTool.addOrUpdate(context, "BouilliMenuInfo", "dbInfos", dbInfoSb.toString());
                        }
                    }
                    if (jsob.has("wmList") || jsob.has("dbList")) {
                        // 发送主页面更新广播
                        Intent intent = new Intent();
                        intent.putExtra("wmDataRef", wmRefSb.toString());
                        intent.putExtra("dbDataRef", dbRefSb.toString());
                        intent.setAction(OutOrderFragment.MSG_REF_OUTORDER_DATA);
                        context.sendBroadcast(intent);
                    }
                    // 发送主页面更新广播
                    Intent intent = new Intent();
                    intent.putExtra("newData", true);
                    intent.setAction(MainFragment.MSG_REFDATA);
                    context.sendBroadcast(intent);
                    // 获取打票机数据（根据登录用户及其设置的相关打印菜品类型）
                    if (forPrintServiceFlag) {
                        if (jsob.has("orderPrintList")) {
                            JSONArray orderPrintList = jsob.getJSONArray("orderPrintList");
                            for (int i = 0; i < orderPrintList.length(); i++) {
                                String orderPrint = (String) orderPrintList.get(i);// 订单流水信息

                                SQLiteOpenHelper sqlite = new DBHelper(context);
                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                db.execSQL("insert into printinfo(record_id, table_id, table_no, print_context, current_state, create_date, create_user, order_num, print_date, print_count, out_user_name, out_user_phone, out_user_address, tip_count) " +
                                        "values('" + orderPrint.split("#&#")[0] + "', '" + orderPrint.split("#&#")[1] + "', '" + orderPrint.split("#&#")[2] + "', '" + orderPrint.split("#&#")[3] + "', " + Integer.valueOf(orderPrint.split("#&#")[4]) + ", '" + orderPrint.split("#&#")[5] + "', '" + orderPrint.split("#&#")[8] + "', '" + orderPrint.split("#&#")[9] + "', '" + orderPrint.split("#&#")[6] + "', " + Integer.valueOf(orderPrint.split("#&#")[7]) + ", '" + orderPrint.split("#&#")[10] + "', '" + orderPrint.split("#&#")[11] + "', '" + orderPrint.split("#&#")[12] + "', 0)");
                                db.close();
                            }
                        }
                        if (jsob.has("accountBillList")) {
                            JSONArray accountBillList = jsob.getJSONArray("accountBillList");
                            for (int i = 0; i < accountBillList.length(); i++) {
                                String accountBillPrint = (String) accountBillList.get(i);// 小票信息

                                SQLiteOpenHelper sqlite = new DBHelper(context);
                                SQLiteDatabase db = sqlite.getWritableDatabase();
                                db.execSQL("insert into billinfo(bill_id, table_no, print_context, order_num, order_waiter, order_date, out_user_name, out_user_phone, out_user_address, current_state, tip_count) " +
                                        "values('" + accountBillPrint.split("#&#")[0] + "', '" + accountBillPrint.split("#&#")[1] + "', '" + accountBillPrint.split("#&#")[2] + "', '" + accountBillPrint.split("#&#")[3] + "', '" + accountBillPrint.split("#&#")[4] + "', '" + accountBillPrint.split("#&#")[5] + "', '" + accountBillPrint.split("#&#")[6] + "', '" + accountBillPrint.split("#&#")[7] + "', '" + accountBillPrint.split("#&#")[8] + "', 0, 0)");
                                db.close();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {

            }
        }));
    }

    /**
     * 初始化聊天信息数据
     *
     * @param context
     * @param params
     */
    public static void InitChatData(final Context context, RequestParams params) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.INIT_CHAT_DATA_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(Object responseObj) {
                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    if (jsob.has("hasNewChat")) {
                        String hasNewChat = jsob.getString("hasNewChat");
                        if (hasNewChat.equals("true")) {
                            // 有新数据
                            // 保存数据至缓存
                            if (jsob.has("userId") && jsob.has("chatList")) {
                                String userId = jsob.getString("userId");
                                JSONArray chatList = jsob.getJSONArray("chatList");
                                for (int i = 0; i < chatList.length(); i++) {
                                    String chatMsg = (String) chatList.get(i);// 聊天消息
                                    SharedPreferencesTool.addOrUpdateChatPro(context, SharedPreferencesTool.CHAT_PRO_NAME, userId, chatMsg);
                                }
                            }

                            // 发送广播，说明获取到新的聊天数据
                            Intent getNewMsgIntent = new Intent();
                            getNewMsgIntent.setAction(Constants.MSG_GET_NEW_CHAT_MSG);
                            MyApplication.getInstance().sendBroadcast(getNewMsgIntent);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {

            }
        }));
    }

    /**
     * 检查程序版本
     *
     * @param context
     * @param params
     */
    public static void CheckVersion(final Context context, RequestParams params) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.CHECK_VERSION_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = MainActivity.MSG_CHECK_NEW_VERSION;
                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    if (jsob.has("lastVersionNo") && jsob.has("lastVersionName") && jsob.has("lastVersionContent")) {
                        data.putInt("lastVersionNo", jsob.getInt("lastVersionNo"));
                        data.putString("lastVersionName", jsob.getString("lastVersionName"));
                        data.putString("lastVersionContent", jsob.getString("lastVersionContent"));
                        // 将更新内容存入配置文件BouilliProInfo
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "newVersionName", jsob.getInt("lastVersionNo"));
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "newVersionName", jsob.getString("lastVersionName"));
                        SharedPreferencesTool.addOrUpdate(context, "BouilliProInfo", "newVersionContent", jsob.getString("lastVersionContent"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setData(data);
                MainActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                    ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                    ComFun.showToast(context, "检查更新失败，请联系管理员", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(context, "检查更新超时，请稍后重试", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_LAST_VERSION_IS_NULL) {
                    ComFun.showToast(context, "当前已经是最新的版本啦", Toast.LENGTH_SHORT);
                }
            }
        }));
    }

    /**
     * 添加新的餐桌组及餐桌号
     */
    public static Call addNewTableGroupTableNums(final Context context, final RequestParams params, final boolean addNewFlag) {
        // 模拟保存本地数据库
        TableGroupDao tableGroupDao = MyApplication.getDaoSession().getTableGroupDao();
        //创建对象，并将对象添加到数据库
        TableGroup customer = new TableGroup();
        customer.setId(null);
        customer.setTableGroupName(params.get("groupName"));
        customer.setTableGroupCode(params.get("groupNo"));
        customer.setTableGroupNo(params.get("tableNums"));
        tableGroupDao.insert(customer);

        Call tableEditCall = CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.ADD_NEW_TABLE_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                if (addNewFlag) {
                    msg.what = TableEditActivity.MSG_ADD_TABLE;
                } else {
                    msg.what = TableEditActivity.MSG_ADD_TABLE_TO_COM;
                }

                JSONObject jsob = (JSONObject) responseObj;

                data.putString("groupName", params.get("groupName"));
                data.putString("groupNo", params.get("groupNo"));
                data.putString("startNum", params.get("startNum"));
                data.putString("endNum", params.get("endNum"));
                data.putString("tableNums", params.get("tableNums"));
                if (jsob.equals(Constants.HTTP_REQUEST_SUCCESS_CODE)) {
                    data.putString("addNewTableResult", "true");
                } else if (jsob.equals(Constants.REQUEST_CODE_HAS)) {
                    data.putString("addNewTableResult", "has_table_group");
                }
                msg.setData(data);
                TableEditActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (addNewFlag) {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "添加新餐桌失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "添加新餐桌超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                } else {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "在组【" + params.get("groupName") + "】中补充餐桌失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "在组【" + params.get("groupName") + "】中补充餐桌超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                }
            }
        }));
        return tableEditCall;
    }

    /**
     * 初始化营业额统计分析报表数据
     *
     * @param context
     * @param params
     */
    public static void InitMonthTurnover(final Context context, RequestParams params) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.GET_MONTH_TURNOVER_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = BusinessActivity.MSG_INIT_MONTH_TURNOVER;
                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    data.putString("initMonthTurnoverResult", "true");
                    if (jsob.has("dataJson")) {
                        data.putString("dataJson", jsob.getString("dataJson"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setData(data);
                BusinessActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                    ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                    ComFun.showToast(context, "初始化月报表数据失败，请联系管理员", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(context, "初始化月报表数据超时，请稍后重试", Toast.LENGTH_SHORT);
                }
            }
        }));
    }

    /**
     * 订单页面 获取打印机信息（结账打印小票时 使用）
     *
     * @param context
     * @param params
     */
    public static void GetPrintInfo(final Context context, RequestParams params, final boolean accountNeedFlag) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.GET_PRINTS_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                if (accountNeedFlag) {
                    msg.what = EditOrderActivity.MSG_GET_PRINT_INFO_ACCOUNT_NEED;
                } else {
                    msg.what = PrintAreaActivity.MSG_INIT_PRINT;
                }
                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    if (jsob.has("printList")) {
                        JSONArray printList = jsob.getJSONArray("printList");
                        StringBuilder printInfoSb = new StringBuilder("");
                        for (int i = 0; i < printList.length(); i++) {
                            String printInfo = (String) printList.get(i);
                            if (ComFun.strNull(printInfo)) {
                                printInfoSb.append(printInfo);
                                printInfoSb.append(",");
                            }
                        }
                        if (ComFun.strNull(printInfoSb.toString())) {
                            data.putString("AllPrintsInfo", printInfoSb.toString().substring(0, printInfoSb.toString().length() - 1));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setData(data);
                if (accountNeedFlag) {
                    EditOrderActivity.mHandler.sendMessage(msg);
                } else {
                    PrintAreaActivity.mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (!accountNeedFlag) {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "初始化打票机数据失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "初始化打票机数据超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                }
            }
        }));
    }

    /**
     * 根据餐桌号获取该餐桌就餐信息数据
     *
     * @param context
     * @param params
     * @param seeFlag       标记是非员工获取点餐信息（不进入后台）
     * @param seeFlagForOut 标记是非员工获取点餐信息/外卖情况（不进入后台）
     * @param tableOrderId
     * @param tableNum      seeFlag为true时使用（不进入后台）
     */
    public static void GetMenuInThisTable(final Context context, RequestParams params, final boolean seeFlag, final boolean seeFlagForOut, final String tableOrderId, final String tableNum) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.GET_TABLE_ORDER_INFO_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    StringBuilder orderInfoDetailSb = new StringBuilder("");
                    if (jsob.has("orderInfoDetailAllMap")) {
                        JSONObject orderInfoDetailAllMap = jsob.getJSONObject("orderInfoDetailAllMap");
                        String[] tableOrderIdArr = tableOrderId.split("#");
                        for (int i = 0; i < tableOrderIdArr.length; i++) {
                            String tableOrderId = tableOrderIdArr[i];
                            for (int j = 0; j < orderInfoDetailAllMap.getJSONArray(tableOrderId).length(); j++) {
                                orderInfoDetailSb.append(orderInfoDetailAllMap.getJSONArray(tableOrderId).get(j) + ",");
                            }
                            if (ComFun.strNull(orderInfoDetailSb.toString())) {
                                orderInfoDetailSb = new StringBuilder(orderInfoDetailSb.toString().substring(0, orderInfoDetailSb.toString().length() - 1));
                            }
                            orderInfoDetailSb.append("||#|#|#||");
                        }
                        if (ComFun.strNull(orderInfoDetailSb.toString())) {
                            data.putString("orderInfoDetails", orderInfoDetailSb.toString().substring(0, orderInfoDetailSb.toString().length() - "||#|#|#||".length()));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!seeFlag) {
                    msg.what = EditOrderActivity.MSG_GET_TABLE_ORDER_INFO;
                    msg.setData(data);
                    EditOrderActivity.mHandler.sendMessage(msg);
                } else {
                    if (seeFlagForOut) {
                        msg.what = OutOrderActivity.MSG_SEE_TABLE_INFO;
                        data.putString("tableNum", tableNum);
                        msg.setData(data);
                        OutOrderActivity.mHandler.sendMessage(msg);
                    } else {
                        msg.what = MainActivity.MSG_SEE_TABLE_INFO;
                        data.putString("tableNum", tableNum);
                        msg.setData(data);
                        MainActivity.mHandler.sendMessage(msg);
                    }
                }
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (!seeFlag) {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "初始化餐桌数据失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "初始化餐桌数据超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                } else {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "获取餐桌信息失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "获取餐桌信息超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                }
            }
        }));
    }

    /**
     * 结账
     *
     * @param context
     * @param params
     * @param tableNo
     * @param tableOrderId
     */
    public static void SettleAccount(final Context context, RequestParams params, final String tableNo, final String tableOrderId) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.SETTLE_ACCOUNT, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = EditOrderActivity.MSG_ACCOUNT;

                // JSONObject jsob = (JSONObject) responseObj;

                data.putString("accountResult", "true");
                data.putString("tableNo", tableNo);
                data.putString("tableOrderId", tableOrderId);

                msg.setData(data);
                EditOrderActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                    ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                    ComFun.showToast(context, "结账操作失败，请联系管理员", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(context, "结账操作超时，请稍后重试", Toast.LENGTH_SHORT);
                }
            }
        }));
    }

    /**
     * 提交餐桌点餐数据
     *
     * @param context
     * @param params
     */
    public static void SendMenu(final Context context, RequestParams params) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.SEND_MENU_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = EditOrderActivity.MSG_SEND_MENU;

                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    String tableOrderInfoPId = jsob.getString("tableOrderInfoPId");
                    String showType = jsob.getString("showType");
                    data.putString("showType", showType);
                    data.putString("tableOrderInfoPId", tableOrderInfoPId);
                    data.putString("sendMenuResult", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setData(data);
                EditOrderActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                    ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                    ComFun.showToast(context, "提交数据失败，请联系管理员", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(context, "提交数据超时，请稍后重试", Toast.LENGTH_SHORT);
                }
            }
        }));
    }

    /**
     * 删除菜品/菜品组
     *
     * @param context
     * @param params
     * @param deleteType
     * @param deleteSelectMenuGroupIdList
     * @param menuInfoId
     * @param menuInfoNameFDel
     */
    public static void DeleteMenu(final Context context, RequestParams params, final String deleteType, final List<String> deleteSelectMenuGroupIdList,
                                  final String menuInfoId, final String menuInfoNameFDel) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.DELETE_MENU_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = MenuEditActivity.MSG_DELETE_MENU;

                // JSONObject jsob = (JSONObject) responseObj;

                data.putString("deleteType", deleteType);
                StringBuilder deleteMenuGroupIds = new StringBuilder("");
                if (ComFun.strNull(deleteSelectMenuGroupIdList) && deleteSelectMenuGroupIdList.size() > 0) {
                    for (String s : deleteSelectMenuGroupIdList) {
                        deleteMenuGroupIds.append(s);
                        deleteMenuGroupIds.append(",");
                    }
                }
                if (ComFun.strNull(deleteMenuGroupIds.toString())) {
                    data.putString("menuGroupIds", deleteMenuGroupIds.toString().substring(0, deleteMenuGroupIds.toString().length() - 1));
                }
                data.putString("menuInfoId", menuInfoId);
                data.putString("menuInfoNameFDel", menuInfoNameFDel);

                msg.setData(data);
                MenuEditActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (deleteType.equals("group")) {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "删除菜品组失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "删除菜品组超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                } else {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "删除菜品失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "删除菜品超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                }
            }
        }));
    }

    /**
     * 更新菜品信息
     *
     * @param context
     * @param params
     * @param menuId
     * @param menuInfoName
     * @param oldMenuInGroupId
     * @param selectMenuGroupId
     * @param menuInfoDes
     * @param menuInfoPrice
     */
    public static void UpdateMenu(final Context context, RequestParams params, final String menuId, final String menuInfoName,
                                  final String oldMenuInGroupId, final String selectMenuGroupId, final String menuInfoDes, final String menuInfoPrice) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.UPDATE_MENU_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = MenuEditActivity.MSG_UPDATE_MENU;

                // JSONObject jsob = (JSONObject) responseObj;

                data.putString("menuId", menuId);
                data.putString("menuInfoName", menuInfoName);
                data.putString("oldMenuInGroupId", oldMenuInGroupId);
                data.putString("selectMenuGroupId", selectMenuGroupId);
                data.putString("menuInfoDes", menuInfoDes);
                data.putString("menuInfoPrice", menuInfoPrice);

                msg.setData(data);
                MenuEditActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                    ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                    ComFun.showToast(context, "修改菜品失败，请联系管理员", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(context, "修改菜品超时，请稍后重试", Toast.LENGTH_SHORT);
                }
            }
        }));
    }

    /**
     * 移动菜品至新组
     *
     * @param context
     * @param params
     * @param moveMenuName
     * @param menuItemInfo
     * @param moveMenuId
     * @param moveMenuGroupId
     * @param moveMenuToGroupId
     */
    public static void MoveMenu(final Context context, RequestParams params, final String moveMenuName, final String menuItemInfo,
                                final String moveMenuId, final String moveMenuGroupId, final String moveMenuToGroupId) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.MOVE_MENU_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = MenuEditActivity.MSG_MOVE_MENU;

                // JSONObject jsob = (JSONObject) responseObj;

                data.putString("menuInfoNameFMove", moveMenuName);
                data.putString("menuItemInfo", menuItemInfo);
                data.putString("moveMenuId", moveMenuId);
                data.putString("moveMenuGroupId", moveMenuGroupId);
                data.putString("moveMenuToGroupId", moveMenuToGroupId);

                msg.setData(data);
                MenuEditActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                    ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                    ComFun.showToast(context, "移动菜品失败，请联系管理员", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(context, "移动菜品超时，请稍后重试", Toast.LENGTH_SHORT);
                }
            }
        }));
    }

    /**
     * 添加新菜品/菜品组
     *
     * @param context
     * @param params
     * @param addType
     * @param groupName
     * @param menuInfoName
     * @param selectMenuGroupId
     * @param menuInfoDes
     * @param menuInfoPrice
     */
    public static void AddNewMenu(final Context context, RequestParams params, final String addType, final String groupName,
                                  final String menuInfoName, final String selectMenuGroupId, final String menuInfoDes, final String menuInfoPrice) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.ADD_NEW_MENU_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {
                ComFun.hideLoading((Activity) context);
            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = MenuEditActivity.MSG_ADD_MENU;

                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    if (jsob.has("otherData")) {
                        String otherData = jsob.getString("otherData");
                        data.putString("otherData", otherData);
                    }
                    data.putString("addType", addType);
                    data.putString("groupName", groupName);
                    data.putString("menuInfoName", menuInfoName);
                    data.putString("selectMenuGroupId", selectMenuGroupId);
                    data.putString("menuInfoDes", menuInfoDes);
                    data.putString("menuInfoPrice", menuInfoPrice);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setData(data);
                MenuEditActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (addType.equals("group")) {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "添加新菜品组失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "添加新菜品组超时，请稍后重试", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_HAS_ERROR) {
                        ComFun.showToast(context, "组【" + groupName + "已经存在，请重新添加", Toast.LENGTH_SHORT);
                    }
                } else {
                    if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                        ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                        ComFun.showToast(context, "添加新菜品失败，请联系管理员", Toast.LENGTH_SHORT);
                    } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                        ComFun.showToast(context, "添加新菜品超时，请稍后重试", Toast.LENGTH_SHORT);
                    }
                }
            }
        }));
    }

    /**
     * 初始化用户营业额
     */
    public static void InitUserTurnover(final Context context, RequestParams params) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.GET_USER_TURNOVER_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = PerformanceAssessActivity.MSG_INIT_USER_TURNOVER;

                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    if (jsob.has("weakUserDataJson")) {
                        data.putString("weakUserDataJson", jsob.getString("weakUserDataJson"));
                    }
                    if (jsob.has("monthUserDataJson")) {
                        data.putString("monthUserDataJson", jsob.getString("monthUserDataJson"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setData(data);
                PerformanceAssessActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                    ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                    ComFun.showToast(context, "初始化员工个人业绩数据失败，请联系管理员", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(context, "初始化员工个人业绩数据超时，请稍后重试", Toast.LENGTH_SHORT);
                }
            }
        }));
    }

    /**
     * 发送聊天消息
     */
    public static void SendChatMsg(final Context context, RequestParams params, final String sendingRandomId) {
        CommonOkHttpClient.post(CommonRequest.createPostRequest(context, URIUtil.SEND_CHAT_MSG_URI, params), new DisposeDataHandle(new DisposeDataListener() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onSuccess(Object responseObj) {
                // 发送Handler通知页面更新UI
                Message msg = new Message();
                Bundle data = new Bundle();
                msg.what = MainActivity.MSG_SEND_CHAT_SUCCESS;

                try {
                    JSONObject jsob = (JSONObject) responseObj;

                    if (jsob.has("userName")) {
                        data.putString("userName", jsob.getString("userName"));
                    }
                    if (jsob.has("sendDateTime")) {
                        data.putString("sendDateTime", jsob.getString("sendDateTime"));
                    }
                    if (jsob.has("sendMsgContent")) {
                        data.putString("sendMsgContent", jsob.getString("sendMsgContent"));
                    }
                    if (jsob.has("sendingRandomId")) {
                        data.putString("sendingRandomId", jsob.getString("sendingRandomId"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg.setData(data);
                MainActivity.mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(OkHttpException okHttpE) {
                if (okHttpE.getEcode() == Constants.HTTP_NETWORK_ERROR) {
                    ComFun.showToast(context, "网络连接异常，请检查网络连接", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_REQUEST_FAIL_ERROR) {
                    ComFun.showToast(context, "发送聊天消息失败，点击重新发送", Toast.LENGTH_SHORT);
                } else if (okHttpE.getEcode() == Constants.HTTP_OUT_TIME_ERROR) {
                    ComFun.showToast(context, "发送聊天消息超时，点击重新发送", Toast.LENGTH_SHORT);
                }
                // 发送消息发送失败广播
                Intent sendChatFailIntent = new Intent();
                sendChatFailIntent.putExtra("sendingRandomId", sendingRandomId);
                sendChatFailIntent.setAction(Constants.MSG_SEND_CHAT_FAIL);
                MyApplication.getInstance().sendBroadcast(sendChatFailIntent);
            }
        }));
    }
}
