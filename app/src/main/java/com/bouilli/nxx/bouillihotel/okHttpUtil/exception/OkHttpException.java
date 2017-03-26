package com.bouilli.nxx.bouillihotel.okHttpUtil.exception;

/**
 * Created by 18230 on 2017/3/26.
 */

public class OkHttpException extends Exception {
    private int ecode;
    private Object emsg;

    public OkHttpException(int ecode, Object emsg) {
        this.ecode = ecode;
        this.emsg = emsg;
    }

    public int getEcode() { return ecode; }

    public Object getEmsg() { return emsg; }
}
