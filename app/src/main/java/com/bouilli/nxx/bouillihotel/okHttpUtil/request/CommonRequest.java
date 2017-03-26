package com.bouilli.nxx.bouillihotel.okHttpUtil.request;

import android.content.Context;

import com.bouilli.nxx.bouillihotel.util.PropertiesUtil;
import com.bouilli.nxx.bouillihotel.util.SharedPreferencesTool;

import java.io.File;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 负责创建各种类型的请求对象，包括get, post, 文件上传类型, 文件下载类型
 */
public class CommonRequest {
    public static String IP_CONFIG;
    public static String TOMCAT_PORT;
    public static String PROJECT_NAME = "BouilliHotelServer";
    public static String URI_PER;

    public static Request createPostRequest(Context context, String url, RequestParams params) {
        IP_CONFIG = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "ipconfig");
        TOMCAT_PORT = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "port");
        URI_PER = "http://"+ IP_CONFIG +":"+ TOMCAT_PORT +"/"+ PROJECT_NAME +"/";

        FormBody.Builder mFormBodyBuild = new FormBody.Builder();

        // 默认加上当前登录用户Id
        String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
        if(params == null){
            params = new RequestParams();
        }
        params.put("userId", userId);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                mFormBodyBuild.add(entry.getKey(), entry.getValue());
            }
        }
        FormBody mFormBody = mFormBodyBuild.build();
        return new Request.Builder().url(URI_PER + url).post(mFormBody).build();
    }

    public static Request createGetRequest(Context context, String url, RequestParams params) {
        IP_CONFIG = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "ipconfig");
        TOMCAT_PORT = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "port");
        URI_PER = "http://"+ IP_CONFIG +":"+ TOMCAT_PORT +"/"+ PROJECT_NAME +"/";

        StringBuilder urlBuilder = new StringBuilder(URI_PER + url).append("?");

        // 默认加上当前登录用户Id
        String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
        if(params == null){
            params = new RequestParams();
        }
        params.put("userId", userId);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        return new Request.Builder().url(urlBuilder.substring(0, urlBuilder.length() - 1)).get().build();
    }

    private static final MediaType FILE_TYPE = MediaType.parse("application/octet-stream");

    /**
     * 文件上传请求
     * @param url
     * @param params
     * @return
     */
    public static Request createMultiPostRequest(Context context, String url, RequestParams params) {
        IP_CONFIG = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "ipconfig");
        TOMCAT_PORT = PropertiesUtil.getPropertiesURL("bouilli.prop", context, "port");
        URI_PER = "http://"+ IP_CONFIG +":"+ TOMCAT_PORT +"/"+ PROJECT_NAME +"/";

        MultipartBody.Builder requestBody = new MultipartBody.Builder();
        requestBody.setType(MultipartBody.FORM);

        // 默认加上当前登录用户Id
        String userId = SharedPreferencesTool.getFromShared(context, "BouilliProInfo", "userId");
        if(params == null){
            params = new RequestParams();
        }
        params.put("userId", userId);

        if (params != null) {

            for (Map.Entry<String, Object> entry : params.fileParams.entrySet()) {
                if (entry.getValue() instanceof File) {
                    requestBody.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                            RequestBody.create(FILE_TYPE, (File) entry.getValue()));
                } else if (entry.getValue() instanceof String) {

                    requestBody.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                            RequestBody.create(null, (String) entry.getValue()));
                }
            }
        }
        return new Request.Builder().url(URI_PER + url).post(requestBody.build()).build();
    }
}
