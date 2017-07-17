package com.haolb.client.utils;

import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.haolb.client.activity.BaseActivity;

/**
 * @author zitian.zhang
 * @since 2013-3-25 下午6:27:00
 */
public class FlipActivityHelper implements OnGestureListener {
    private GestureDetectorCompat detector;
    private boolean canFlip;
    private final BaseActivity mActivity;
    private boolean touchOnHScroll;

    private static float SWIPE_MIN_DISTANCE = 150;

    private static float SWIPE_THRESHOLD_VELOCITY = 100;

    public FlipActivityHelper(BaseActivity activity) {
        this.mActivity = activity;
    }

    public void onCreate(Bundle myBundle) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(mActivity);
        // SWIPE_MIN_DISTANCE = 50.0F * mActivity.getResources().getDisplayMetrics().density;
        // 屏幕宽度的1/3
        SWIPE_MIN_DISTANCE = mActivity.getResources().getDisplayMetrics().widthPixels == 0 ? SWIPE_MIN_DISTANCE
                : mActivity.getResources().getDisplayMetrics().widthPixels * 1 / 3;
        SWIPE_THRESHOLD_VELOCITY = 3 * viewConfiguration.getScaledMinimumFlingVelocity();
        detector = new GestureDetectorCompat(mActivity, this);
        if (myBundle != null) {
            setCanFlip(myBundle.getBoolean("canFlip", true));
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("canFlip", isCanFlip());
    }

    public boolean isCanFlip() {
        return canFlip;
    }

    public void setCanFlip(boolean canFlip) {
        this.canFlip = canFlip;
    }

    @Override
    public boolean onDown(MotionEvent e) {

        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) {
            return false;
        }
        float dX = e2.getX() - e1.getX();
        float dY = e2.getY() - e1.getY();

        float x = Math.abs(dX);
        float y = Math.abs(dY);
        double z = Math.sqrt(x * x + y * y);
        int angle = Math.round((float) (Math.asin(y / z) / Math.PI * 180));

        // QLog.e("angel", "角度:" + angle + "--distance:" + SWIPE_MIN_DISTANCE + "--dX:" + Math.abs(dX));

        if (Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY // && Math.abs(velocityY) < 0.3 * Math.abs(velocityX)
                && Math.abs(dX) > SWIPE_MIN_DISTANCE && angle < 30) {
            if (dX > 0) {
                BaseActivity.simulateKey(KeyEvent.KEYCODE_BACK);
                return true;
            }
        }
        return false;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isCanFlip() && !touchOnHScroll && detector.onTouchEvent(ev)
                && (ev.getAction() & MotionEventCompat.ACTION_MASK) == MotionEvent.ACTION_UP) {
            return true;
        } else {
            touchOnHScroll = false;
            return mActivity.superDispatchTouchEvent(ev);
        }

    }

    public boolean isTouchOnHScroll() {
        return this.touchOnHScroll;
    }

    public void setTouchOnHScroll(boolean touchOnHScroll) {
        this.touchOnHScroll = touchOnHScroll;
    }
}