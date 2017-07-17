package com.haolb.client.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlwaysFocusedTextView extends TextView {

    public AlwaysFocusedTextView(Context context) {
        super(context);
    }

    public AlwaysFocusedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AlwaysFocusedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

}