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

}
