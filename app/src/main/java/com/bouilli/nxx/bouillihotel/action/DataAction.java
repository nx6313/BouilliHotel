package com.bouilli.nxx.bouillihotel.action;

import android.content.Context;

import com.bouilli.nxx.bouillihotel.action.base.BaseAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 18230 on 2016/10/30.
 */

public class DataAction extends BaseAction {

    /**
     * 初始化程序基础数据
     * @param uri
     * @return
     */
    public static String initBaseData(Context context, String uri){
        return getHttpData(context, uri, null);
    }
}
