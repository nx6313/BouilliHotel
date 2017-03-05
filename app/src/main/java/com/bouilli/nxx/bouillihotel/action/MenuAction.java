package com.bouilli.nxx.bouillihotel.action;

import android.content.Context;

import com.bouilli.nxx.bouillihotel.action.base.BaseAction;
import com.bouilli.nxx.bouillihotel.util.ComFun;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 18230 on 2016/10/30.
 */

public class MenuAction extends BaseAction {

    /**
     * 添加新菜品
     * @param uri
     * @return
     */
    public static String addNewMenu(Context context, String uri, String groupName, String menuInfoName, String selectMenuGroupId, String menuInfoDes, String menuInfoPrice, String addType){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("groupName", groupName);
        paramMap.put("menuInfoName", menuInfoName);
        paramMap.put("selectMenuGroupId", selectMenuGroupId);
        paramMap.put("menuInfoDes", menuInfoDes);
        paramMap.put("menuInfoPrice", menuInfoPrice);
        paramMap.put("addType", addType);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 删除菜品
     * @param uri
     * @return
     */
    public static String deleteMenu(Context context, String uri, String deleteType, List<String> deleteSelectMenuGroupIdList, String menuInfoId){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("deleteType", deleteType);
        StringBuilder deleteMenuGroupIds = new StringBuilder("");
        if(ComFun.strNull(deleteSelectMenuGroupIdList) && deleteSelectMenuGroupIdList.size() > 0){
            for(String s : deleteSelectMenuGroupIdList){
                deleteMenuGroupIds.append(s);
                deleteMenuGroupIds.append(",");
            }
        }
        if(ComFun.strNull(deleteMenuGroupIds.toString())){
            paramMap.put("menuGroupIds", deleteMenuGroupIds.toString().substring(0, deleteMenuGroupIds.toString().length() - 1));
        }
        paramMap.put("menuInfoId", menuInfoId);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 传菜品
     * @param uri
     * @return
     */
    public static String sendMenu(Context context, String uri, String tableNum, int showType, Map<String, Object[]> tableHasNewOrderMap, String tableOrderId){
        String orderType = "1";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("tableOrderId", tableOrderId);
        paramMap.put("tableNum", tableNum);
        paramMap.put("showType", showType+"");
        StringBuilder orderMenuInfoSb = new StringBuilder("");
        for(Map.Entry<String, Object[]> m : tableHasNewOrderMap.entrySet()){
            String menuId = m.getKey();
            String menuPrice = m.getValue()[0].toString().split("#&#")[4].toString();
            String menuBuyCount = m.getValue()[1].toString();
            String menuAboutRemark = m.getValue()[2].toString();
            orderMenuInfoSb.append(menuId + "|" + menuPrice + "|" + menuBuyCount + "|" + menuAboutRemark);
            orderMenuInfoSb.append(",");
        }
        if(ComFun.strNull(orderMenuInfoSb.toString())){
            if(showType == -10){
                if(tableNum.equals("wmTable")){
                    orderType = "2";
                }else{
                    orderType = "3";
                }
                paramMap.put("takeOutMenuInfo", orderMenuInfoSb.toString().substring(0, orderMenuInfoSb.toString().length() - 1));
            }else{
                paramMap.put("orderMenuInfo", orderMenuInfoSb.toString().substring(0, orderMenuInfoSb.toString().length() - 1));
            }
        }
        paramMap.put("orderType", orderType);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 根据餐桌号获取该餐桌就餐信息数据
     * @param uri
     * @return
     */
    public static String getMenuInThisTable(Context context, String uri, String tableOrderId){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("tableOrderId", tableOrderId);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 餐桌结账
     * @param uri
     * @return
     */
    public static String settleAccount(Context context, String uri, String tableOrderId, String printAccountBillId, String tableNo, String printContext, String outUserName, String outUserPhone, String outUserAddress){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("tableOrderId", tableOrderId);
        if(ComFun.strNull(printAccountBillId) && ComFun.strNull(tableNo) && ComFun.strNull(printContext)){
            paramMap.put("printAccountBillId", printAccountBillId);
            paramMap.put("tableNo", tableNo);
            paramMap.put("printContext", printContext);
        }
        paramMap.put("outUserName", outUserName);
        paramMap.put("outUserPhone", outUserPhone);
        paramMap.put("outUserAddress", outUserAddress);
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 添加人员
     * @param uri
     * @return
     */
    public static String addNewUser(Context context, String uri, String userInfoName, String userInfoGroupId, String userInfoSexId, String userInfoAge, String userInfoBirthday, String userInfoPhone) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userInfoName", userInfoName);
        paramMap.put("userInfoGroupId", userInfoGroupId);
        paramMap.put("userInfoSexId", userInfoSexId);
        paramMap.put("userInfoAge", userInfoAge);
        paramMap.put("userInfoBirthday", userInfoBirthday);
        paramMap.put("userInfoPhone", userInfoPhone);
        return getHttpData(context, uri, paramMap);
    }
}
