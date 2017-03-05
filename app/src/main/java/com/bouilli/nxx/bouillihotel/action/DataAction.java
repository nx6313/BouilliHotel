package com.bouilli.nxx.bouillihotel.action;

import android.content.Context;

import com.bouilli.nxx.bouillihotel.action.base.BaseAction;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 18230 on 2016/10/30.
 */

public class DataAction extends BaseAction {

    /**
     * 用户登录
     * @param uri
     * @return
     */
    public static String userLogin(Context context, String uri, String loginName, String loginPwd){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("loginName", loginName);
        paramMap.put("loginPwd", loginPwd);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 初始化程序基础数据
     * @param uri
     * @return
     */
    public static String initBaseData(Context context, String uri){
        return getHttpData(context, uri, null);
    }

    /**
     * 初始化程序订单数据
     * @param uri
     * @return
     */
    public static String initOrderData(Context context, String uri, boolean forPrintServiceFlag){
        Map<String, String> paramMap = null;
        if(forPrintServiceFlag){
            paramMap = new HashMap<>();
            String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
            paramMap.put("forPrintServiceUserId", userId);
        }
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 打印前更新服务器状态
     * @param uri
     * @return
     */
    public static String recordBeginPrint(Context context, String uri, String printRecordId){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("printRecordId", printRecordId);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 获取管理用户信息
     * @param uri
     * @return
     */
    public static String getUserInfo(Context context, String uri){
        return getHttpData(context, uri, null);
    }

    /**
     * 删除用户
     * @param uri
     * @return
     */
    public static String deleteUserInfo(Context context, String uri, String userInfoId){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userInfoId", userInfoId);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 初始化获取打票机信息
     * @param uri
     * @return
     */
    public static String getPrintInfo(Context context, String uri){
        return getHttpData(context, uri, null);
    }

    /**
     * 添加打票机
     * @param uri
     * @return
     */
    public static String addNewPrint(Context context, String uri, String printName, String printAddress, String printInfoId, String selectMenuAbout){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("printName", printName);
        paramMap.put("printAddress", printAddress);
        paramMap.put("printInfoId", printInfoId);
        paramMap.put("selectMenuAbout", selectMenuAbout);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 删除打票机
     * @param uri
     * @return
     */
    public static String deletePrint(Context context, String uri, String printInfoId){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("printInfoId", printInfoId);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 保存用户打印机设置
     * @param uri
     * @return
     */
    public static String saveUserPrintSet(Context context, String uri, String selectPrintAreaId){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("printInfoId", selectPrintAreaId);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 初始化月营业统计数据
     * @param uri
     * @return
     */
    public static String getMonthTurnover(Context context, String uri){
        return getHttpData(context, uri, null);
    }

    /**
     * 检查更新
     * @param uri
     * @return
     */
    public static String checkVersion(Context context, String uri){
        return getHttpData(context, uri, null);
    }
}
