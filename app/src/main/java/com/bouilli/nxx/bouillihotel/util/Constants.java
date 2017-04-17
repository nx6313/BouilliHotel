package com.bouilli.nxx.bouillihotel.util;

/**
 * Created by 18230 on 2016/10/30.
 */

public class Constants {
    public static final String OVERALL_TAG = "BouilliHotel";
    public static final int REQUEST_TIMEOUT = 10000;

    /**
     * 广播Action：获取网络数据成功
     */
    public static final String MSG_GET_DATA_SUCCESS = "com.nxx.bouilli.getDataSuccess";

    /**
     * 广播Action：获取网络数据失败
     */
    public static final String MSG_GET_DATA_FAIL = "com.nxx.bouilli.getDataFail";

    /**
     * 广播Action：获取到新的聊天消息
     */
    public static final String MSG_GET_NEW_CHAT_MSG = "com.nxx.bouilli.getNewChatMsg";

    /**
     * 广播Action：获取到新的聊天顶部信息
     */
    public static final String MSG_GET_NEW_CHAT_TIP = "com.nxx.bouilli.getNewChatTip";

    /**
     * 广播Action：发送聊天消息成功
     */
    public static final String MSG_SEND_CHAT_SUCCESS = "com.nxx.bouilli.sendChatSuccess";

    /**
     * 广播Action：发送聊天消息失败
     */
    public static final String MSG_SEND_CHAT_FAIL = "com.nxx.bouilli.sendChatFail";


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

    /**
     * 检查新版本，新版本对象为NULL，客户端默认该情况为 当前为最新版本
     */
    public static final String HTTP_LAST_VERSION_NULL = "HTTP_LAST_VERSION_IS_NULL";

    public static final int HTTP_NETWORK_ERROR = -1;
    public static final int HTTP_JSON_ERROR = -2;
    public static final int HTTP_LOGIN_ERROR_ERROR = -3;
    public static final int HTTP_REQUEST_FAIL_ERROR = -4;
    public static final int HTTP_OUT_TIME_ERROR = -5;
    public static final int HTTP_HAS_ERROR = -6;
    public static final int HTTP_OTHER_ERROR = -7;
    public static final int HTTP_IO_ERROR = -8;
    public static final int HTTP_LAST_VERSION_IS_NULL = -9;
}
