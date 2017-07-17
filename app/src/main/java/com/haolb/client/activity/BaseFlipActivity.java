package com.haolb.client.activity;

import android.os.Bundle;
import android.view.MotionEvent;

import com.haolb.client.utils.FlipActivityHelper;

/**
 * @author zitian.zhang
 * @since 2013-3-21 下午9:01:24
 */
public abstract class BaseFlipActivity extends BaseActivity {

    public FlipActivityHelper mFlipHelper = new FlipActivityHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFlipHelper.onCreate(myBundle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mFlipHelper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    public boolean isCanFlip() {
        return this.mFlipHelper.isCanFlip();
    }

    public void setCanFlip(boolean canFlip) {
        this.mFlipHelper.setCanFlip(canFlip);
    }

    public boolean isTouchOnHScroll() {
        return this.mFlipHelper.isTouchOnHScroll();
    }

    public void setTouchOnHScroll(boolean touchOnHScroll) {
        this.mFlipHelper.setTouchOnHScroll(touchOnHScroll);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mFlipHelper.dispatchTouchEvent(ev);
    }


}
