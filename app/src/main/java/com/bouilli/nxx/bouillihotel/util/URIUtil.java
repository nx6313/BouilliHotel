package com.bouilli.nxx.bouillihotel.util;

/**
 * Created by 18230 on 2016/10/30.
 */

public class URIUtil {

    /**
     * 用户登录任务
     */
    public static final String USER_LOGIN_URI = "toBackLogin.action";

    /**
     * 初始化程序基本数据
     */
    public static final String INIT_BASE_DATA_URI = "initBaseData.action";

    /**
     * 初始化程序订单数据
     */
    public static final String INIT_ORDER_DATA_URI = "initOrderData.action";

    /**
     * 初始化聊天信息数据
     */
    public static final String INIT_CHAT_DATA_URI = "initChatData.action";

    /**
     * 添加新餐桌
     */
    public static final String ADD_NEW_TABLE_URI = "addNewTable.action";

    /**
     * 删除餐桌
     */
    public static final String DELETE_TABLE_URI = "deleteTable.action";

    /**
     * 添加新菜品
     */
    public static final String ADD_NEW_MENU_URI = "addNewMenu.action";
    /**
     * 修改菜品
     */
    public static final String UPDATE_MENU_URI = "updateMenu.action";

    /**
     * 删除菜品
     */
    public static final String DELETE_MENU_URI = "deleteMenu.action";

    /**
     * 移动菜品
     */
    public static final String MOVE_MENU_URI = "moveMenu.action";

    /**
     * 提交点的菜品数据
     */
    public static final String SEND_MENU_URI = "sendMenu.action";

    /**
     * 根据餐桌号获取该餐桌就餐信息数据
     */
    public static final String GET_TABLE_ORDER_INFO_URI = "getTableOrderInfo.action";

    /**
     * 餐桌结账
     */
    public static final String SETTLE_ACCOUNT = "settleAccount.action";

    /**
     * 打印前任务，更新服务器打印状态
     */
    public static final String PRINT_START_URI = "updateOrderPrintState.action";

    /**
     * 添加人员任务
     */
    public static final String ADD_USER_URI = "addUser.action";

    /**
     * 获取管理人员任务
     */
    public static final String GET_USERS_URI = "getUserInfoData.action";

    /**
     * 删除人员任务
     */
    public static final String DELETE_USERS_URI = "deleteUser.action";

    /**
     * 获取打印机任务
     */
    public static final String GET_PRINTS_URI = "getPrintsInfo.action";

    /**
     * 添加打印机任务
     */
    public static final String ADD_PRINT_URI = "addNewPrint.action";

    /**
     * 删除打印机任务
     */
    public static final String DELETE_PRINT_URI = "deletePrint.action";

    /**
     * 保存用户打印机设置任务
     */
    public static final String SAVE_USER_PRINT_SET_URI = "setPrintForUser.action";

    /**
     * 初始化月营业统计数据任务
     */
    public static final String GET_MONTH_TURNOVER_URI = "getMonthTurnover.action";

    /**
     * 初始化月营业统计数据任务
     */
    public static final String GET_USER_TURNOVER_URI = "getTurnoverForUser.action";

    /**
     * 检查更新任务
     */
    public static final String CHECK_VERSION_URI = "checkVersion.action";

    /**
     * 发送聊天消息任务
     */
    public static final String SEND_CHAT_MSG_URI = "sendChatMsg.action";
}
