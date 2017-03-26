package com.bouilli.nxx.bouillihotel.okHttpUtil.listener;

import com.bouilli.nxx.bouillihotel.okHttpUtil.exception.OkHttpException;

/**
 * Created by 18230 on 2017/3/26.
 */

public interface DisposeDataListener {

    /**
     * 请求完成回调事件处理
     */
    public void onFinish();

    /**
     * 请求成功回调事件处理
     */
    public void onSuccess(Object responseObj);

    /**
     * 请求失败回调事件处理
     */
    public void onFailure(OkHttpException okHttpE);
}
