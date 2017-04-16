package com.bouilli.nxx.bouillihotel.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 18230 on 2016/10/29.
 */

public class SharedPreferencesTool {
    /**
     * 缓存：保存聊天内容
     */
    public static final String CHAT_PRO_NAME = "chatMessageProInfo";

    public static void addOrUpdate(Context context, String sharedName,
                                   String key, String value) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void addOrUpdate(Context context, String sharedName,
                                   String key, Integer value) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void addOrUpdate(Context context, String sharedName,
                                   String key, boolean value) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void addOrUpdateChatPro(Context context, String sharedName,
                                          String key, String... value) {
        if(ComFun.strNull(value) && value.length > 0){
            List<String> msgList = getMsgListFromShared(context, sharedName, key);
            if(msgList == null){
                msgList = new ArrayList<>();
            }
            for(String str : value){
                msgList.add(str);
            }
            JSONArray jsonArray = new JSONArray();
            for(String str : msgList){
                jsonArray.put(str);
            }
            SharedPreferences mySharedPreferences = context.getSharedPreferences(
                    sharedName, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putString(key, jsonArray.toString());
            editor.commit();
        }
    }

    public static void deleteErrorChatPro(Context context, String sharedName,
                                          String key, String errorChatRandomId) {
        List<String> msgDeleteAfterList = new ArrayList<>();
        List<String> msgList = getMsgListFromShared(context, sharedName, key);
        if(ComFun.strNull(msgList) && msgList.size() > 0){
            for(String msg : msgList){
                String[] msgArr = msg.split("&\\|\\|&");
                if(msgArr.length == 5){
                    if(!msgArr[4].equals(errorChatRandomId)){
                        msgDeleteAfterList.add(msg);
                    }
                }else{
                    msgDeleteAfterList.add(msg);
                }
            }
            SharedPreferences mySharedPreferences = context.getSharedPreferences(
                    sharedName, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            JSONArray jsonArray = new JSONArray();
            if(msgDeleteAfterList.size() > 0){
                for(String str : msgDeleteAfterList){
                    jsonArray.put(str);
                }
                editor.putString(key, jsonArray.toString());
            }else{
                editor.putString(key, "");
            }
            editor.commit();
        }
    }

    public static List<String> getMsgListFromShared(Context context, String sharedName,
                                               String key) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        String getVal = mySharedPreferences.getString(key, null);
        if(ComFun.strNull(getVal)){
            try {
                List<String> msgContentList = new ArrayList<>();
                //JSONObject jsonObject = new JSONObject(getVal);
                JSONArray jsonArray = new JSONArray(getVal);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String chatMsgContent = (String) jsonArray.get(i);
                    msgContentList.add(chatMsgContent);
                }
                return msgContentList;
            } catch (JSONException e) {
                L.e("getMsgListFromShared异常：" + e.getMessage());
                return null;
            }
        }
        return null;
    }

    public static Boolean getBooleanFromShared(Context context, String sharedName,
                                       String key) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        boolean defaultVal = false;
        Boolean getVal = mySharedPreferences.getBoolean(key, defaultVal);
        return getVal;
    }

    public static int getFromShared(Context context, String sharedName,
                                       String key, Integer defValue) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        int defaultVal = 0;
        if (defValue != null) {
            defaultVal = defValue;
        }
        int getVal = mySharedPreferences.getInt(key, defaultVal);
        return getVal;
    }

    public static String getFromShared(Context context, String sharedName,
                                       String key, String... defValue) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        String defaultVal = "";
        if (defValue != null && defValue.length > 0) {
            defaultVal = defValue[0];
        }
        String getVal = mySharedPreferences.getString(key, defaultVal);
        return getVal;
    }

    public static Map<String, ?> getListFromShared(Context context,
                                                   String sharedName) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        Map<String, ?> getVal = mySharedPreferences.getAll();
        return getVal;
    }

    public static void clearShared(Context context, String[] sharedNames){
        if(ComFun.strNull(sharedNames)){
            for(String sharedName : sharedNames){
                SharedPreferences mySharedPreferences = context.getSharedPreferences(
                        sharedName, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                editor.clear();
                editor.commit();
            }
        }
    }

    public static void clearShared(Context context, String sharedName){
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public static void deleteFromShared(Context context, String sharedName, String key){
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public static boolean checkIsExit(Context context){
        SharedPreferences exit = context.getSharedPreferences(
                "SHARED_IS_EXIT", Activity.MODE_PRIVATE);
        boolean exFlag = exit.getBoolean("exit", false);
        return exFlag;
    }
}
