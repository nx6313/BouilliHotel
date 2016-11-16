package com.bouilli.nxx.bouillihotel.callBack;

import android.animation.ObjectAnimator;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by 18230 on 2016/10/29.
 */
public class MsgCallBack extends Snackbar.Callback {
    private View view;
    public MsgCallBack(View view){
        this.view = view;
    }

    @Override
    public void onDismissed(Snackbar snackbar, int event) {
        if(view != null){
            ObjectAnimator.ofFloat(view, "alpha", (float)0.4).setDuration(200).start();
        }
        if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE) {
            // 滑动消失
        }
        super.onDismissed(snackbar, event);
    }
}
