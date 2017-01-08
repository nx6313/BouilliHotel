package com.bouilli.nxx.bouillihotel.customview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by 18230 on 2016/11/28.
 */

public class MarqueeTextView extends TextView {

    public MarqueeTextView(Context con) {
        super(con);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        // TODO Auto-generated method stub
        if(getEditableText().equals(TextUtils.TruncateAt.MARQUEE)){
            return true;
        }
        return super.isFocused();
    }

}
