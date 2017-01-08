package com.bouilli.nxx.bouillihotel.util;

/**
 * Created by 18230 on 2016/10/30.
 */

public class Constants {
    public static final String OVERALL_TAG = "BouilliHotel";
    public static final int REQUEST_TIMEOUT = 6000;

    /**
     * 返回数据成功
     */
    public static final String HTTP_REQUEST_SUCCESS_CODE = "HTTP_REQUEST_SUCCESS_CODE";

    /**
     * 返回用户登录失败（用户名或密码错误）
     */
    public static final String HTTP_REQUEST_LOGIN_ERROR_CODE = "HTTP_REQUEST_LOGIN_ERROR_CODE";

    /**
     * 返回数据失败
     */
    public static final String HTTP_REQUEST_FAIL_CODE = "HTTP_REQUEST_FAIL_CODE";
    /**
     * 返回数据超时
     */
    public static final String HTTP_REQUEST_OUT_TIME_CODE = "HTTP_REQUEST_OUT_TIME_CODE";
    /**
     * 需要新添加或新保存的数据在数据库中已经存在
     */
    public static final String REQUEST_CODE_HAS = "REQUEST_CODE_HAS";
}
