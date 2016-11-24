package com.bouilli.nxx.bouillihotel.action.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bouilli.nxx.bouillihotel.util.ComFun;
import com.bouilli.nxx.bouillihotel.util.Constants;
import com.bouilli.nxx.bouillihotel.util.PropertiesUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by 18230 on 2016/10/30.
 */

public class BaseAction {
    public static String IP_CONFIG;
    public static String TOMCAT_PORT;
    public static String URI_PER;
    public static String PROJECT_NAME = "BouilliHotelServer";

    /**
     * 获取网络数据基类
     *
     * @param url
     * @param params
     * @return  成功或者超时
     */
    protected static String getHttpData(Context context, String url, Map<String, String> params) {
        IP_CONFIG = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "ipconfig");
        TOMCAT_PORT = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "port");
        URI_PER = "http://"+ IP_CONFIG +":"+ TOMCAT_PORT +"/"+ PROJECT_NAME +"/";
        URL httpUrl;
        HttpURLConnection conn = null;
        StringBuffer sb = new StringBuffer("");
        OutputStream out;
        try {
            httpUrl = new URL(URI_PER + url);
            try {
                conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                conn.setRequestMethod("POST");
                conn.setConnectTimeout(Constants.REQUEST_TIMEOUT);
                conn.setReadTimeout(Constants.REQUEST_TIMEOUT);

                conn.connect();

                out = conn.getOutputStream();
                String content = getHttpParam(context, params);
                if (ComFun.strNull(content)) {
                    out.write(content.getBytes());
                }
                if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String str;
                    while ((str = reader.readLine()) != null) {
                        sb.append(str);
                    }
                    // 在返回值中添加返回成功的请求码
                    if (ComFun.strNull(sb.toString()) && sb.toString().startsWith("{") && sb.toString().endsWith("}")) {
                        JSONObject jsob = new JSONObject(sb.toString());
                        if (jsob.isNull("responseCode")) {
                            // 返回值中没有responseCode
                            jsob.put("responseCode", Constants.HTTP_REQUEST_SUCCESS_CODE);
                            sb = new StringBuffer(jsob.toString());
                        }
                    } else {
                        JSONObject jsob = new JSONObject();
                        jsob.put("responseCode", Constants.HTTP_REQUEST_FAIL_CODE);
                        sb = new StringBuffer(jsob.toString());
                        Log.d(Constants.OVERALL_TAG, "访问地址：" + url
                                + "，该HTTP访问服务器无返回JSON值，或者返回值异常");
                        if(ComFun.strNull(params)){
                            Log.d(Constants.OVERALL_TAG, "访问参数："+ content +"=========访问地址：" + url);
                        }
                        if(ComFun.strNull(sb.toString())){
                            Log.d(Constants.OVERALL_TAG, "服务器返回值的内容："+ sb.toString() +"=========访问地址：" + url);
                        }
                    }
                } else {
                    JSONObject jsob = new JSONObject();
                    jsob.put("responseCode", Constants.HTTP_REQUEST_FAIL_CODE);
                    sb = new StringBuffer(jsob.toString());
                    Log.e(Constants.OVERALL_TAG,
                            "访问服务器失败，错误码：" + conn.getResponseCode() +"=========访问地址：" + url);
                }
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    Log.e(Constants.OVERALL_TAG, "访问服务器out流关闭异常=========访问地址：" + url);
                    Log.e(Constants.OVERALL_TAG, e.getMessage());
                }
            } catch (SocketTimeoutException e) {
                Log.e(Constants.OVERALL_TAG, "访问服务器超时，访问地址：" + url);
                Log.e(Constants.OVERALL_TAG, e.getMessage());
                try {
                    JSONObject jsob = new JSONObject();
                    jsob.put("responseCode",
                            Constants.HTTP_REQUEST_OUT_TIME_CODE);
                    sb.append(jsob.toString());
                } catch (JSONException e1) {
                    Log.e(Constants.OVERALL_TAG, "访问服务器返回值json封装异常=========访问地址：" + url);
                    Log.e(Constants.OVERALL_TAG, e.getMessage());
                }
            } catch (IOException e) {
                Log.e(Constants.OVERALL_TAG, "访问服务器运行时异常，访问地址：" + url);
                Log.e(Constants.OVERALL_TAG, e.getMessage());
                try {
                    JSONObject jsob = new JSONObject();
                    jsob.put("responseCode", Constants.HTTP_REQUEST_OUT_TIME_CODE);
                    sb.append(jsob.toString());
                } catch (JSONException e1) {
                    Log.e(Constants.OVERALL_TAG, "访问服务器返回值json封装异常=========访问地址：" + url);
                    Log.e(Constants.OVERALL_TAG, e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        if (ComFun.strNull(sb.toString()) && !sb.toString().equals("") && sb.toString().startsWith("{") && sb.toString().endsWith("}")) {
            return sb.toString();
        }
        return null;
    }

    /**
     * 得到http访问参数
     *
     * @param params
     * @return
     */
    protected static String getHttpParam(Context context, Map<String, String> params) {
        String result = null;
        StringBuilder sb = new StringBuilder("");
        // 默认加上当前登录用户Id
        String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
        sb.append("userId=" + userId + "&");
        if (ComFun.strNull(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        if (ComFun.strNull(sb.toString())) {
            result = sb.toString().substring(0, sb.toString().length() - 1);
        }
        return result;
    }

    /**
     * 获取网络图片
     *
     * @param imageUrl
     * @return
     */
    public static Bitmap getHttpImg(Context context, String imageUrl) {
        IP_CONFIG = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "ipconfig");
        TOMCAT_PORT = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "port");
        URI_PER = "http://"+ IP_CONFIG +":"+ TOMCAT_PORT +"/"+ PROJECT_NAME +"/";
        InputStream in = null;
        Bitmap bitmap = null;
        try {
            URL url = new URL(URI_PER + imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            in = conn.getInputStream();
            if (ComFun.strNull(in)) {
                bitmap = BitmapFactory.decodeStream(in);
            }
        } catch (IOException e) {
            Log.e(Constants.OVERALL_TAG, "获取网络图片异常=========访问地址：" + imageUrl);
            Log.e(Constants.OVERALL_TAG, e.getMessage());
        } finally {
            try {
                if (ComFun.strNull(in)) {
                    in.close();
                }
            } catch (IOException e) {
                Log.e(Constants.OVERALL_TAG, "获取网络图片关闭输出流异常=========访问地址：" + imageUrl);
                Log.e(Constants.OVERALL_TAG, e.getMessage());
            }
        }
        return bitmap;
    }
}
