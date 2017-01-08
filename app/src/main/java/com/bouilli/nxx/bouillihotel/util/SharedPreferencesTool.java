package com.bouilli.nxx.bouillihotel.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by 18230 on 2016/10/29.
 */

public class SharedPreferencesTool {
    public static void addOrUpdate(Context context, String sharedName,
                                   String key, String value) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString(key, value);
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

    public static Boolean getBooleanFromShared(Context context, String sharedName,
                                       String key) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                sharedName, Activity.MODE_PRIVATE);
        boolean defaultVal = false;
        Boolean getVal = mySharedPreferences.getBoolean(key, defaultVal);
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
