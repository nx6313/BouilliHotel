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

public class TableAction extends BaseAction {

    /**
     * 添加新餐桌
     * @param uri
     * @return
     */
    public static String addNewTable(Context context, String uri, String groupName, String groupNo, String startNum, String endNum, String tableNums, boolean addNewFlag){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("groupName", groupName);
        paramMap.put("groupNo", groupNo);
        paramMap.put("startNum", startNum);
        paramMap.put("endNum", endNum);
        paramMap.put("tableNums", tableNums);
        if(addNewFlag){
            paramMap.put("addType", "newAdd");
        }else{
            paramMap.put("addType", "comAdd");
        }
        return getHttpData(context, uri, paramMap);
    }

    /**
     * 删除餐桌或组
     * @param uri
     * @return
     */
    public static String deleteTable(Context context, String uri, String deleteTableType, List<String> deleteSelectTableGroupNameList, String deleteTableInfo){
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("deleteTableType", deleteTableType);
        paramMap.put("deleteTableInfo", deleteTableInfo);
        if(deleteSelectTableGroupNameList != null && deleteSelectTableGroupNameList.size() > 0){
            StringBuilder sb = new StringBuilder("");
            for(String s : deleteSelectTableGroupNameList){
                sb.append(s);
                sb.append(",");
            }
            if(ComFun.strNull(sb.toString())){
                paramMap.put("deleteTableGroupNames", sb.toString().substring(0, sb.toString().length() - 1));
            }else{
                paramMap.put("deleteTableGroupNames", null);
            }
        }else{
            paramMap.put("deleteTableGroupNames", null);
        }
        return getHttpData(context, uri, paramMap);
    }
}
