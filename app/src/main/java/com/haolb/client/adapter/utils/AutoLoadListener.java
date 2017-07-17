package com.haolb.client.adapter.utils;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.haolb.client.utils.QLog;

/**
 * TODO　暂时不用这个类
 * 
 * @deprecated
 * @author zitian.zhang
 */
@Deprecated
public class AutoLoadListener implements OnScrollListener {

    private int mLastVisiblePosition = 0;
    private int mLastVisiblePositionY = 0;
    private OnLoadMoreListener mOnLoadMoreListener;
    protected OnScrollListener mWrappedListener;
    private LoadState mState = LoadState.DONE;
    private final Object mLockObject = new Object();

    public AutoLoadListener() {
        this(null);
    }

    /**
     * @param wrappedListener 其他的滚动监听接口
     */
    public AutoLoadListener(OnScrollListener wrappedListener) {
        this.mWrappedListener = wrappedListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    public OnScrollListener getWrappedListener() {
        return this.mWrappedListener;
    }

    /**
     * 指定一个autoload以外的事件监听
     * 
     * @param wrapListener
     */
    public void setWrappedListener(OnScrollListener wrapListener) {
        this.mWrappedListener = wrapListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            // 滚动到底部
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                View v = view.getChildAt(view.getChildCount() - 1);
                int[] location = new int[2];
                v.getLocationOnScreen(location);// 获取在整个屏幕内的绝对坐标
                int y = location[1];
                QLog.d("scroll", "x" + location[0] + " y" + location[1]);
                if (view.getLastVisiblePosition() != mLastVisiblePosition && mLastVisiblePositionY != y)// 第一次拖至底部
                {
                    mLastVisiblePosition = view.getLastVisiblePosition();
                    mLastVisiblePositionY = y;
                    QLog.d("scroll", "第一次");
                    return;
                } else if (view.getLastVisiblePosition() == mLastVisiblePosition && mLastVisiblePositionY == y)// 第二次拖至底部
                {
                    QLog.d("scroll", "第二次");
                    switch (mState) {
                    case DONE:
                        if (mOnLoadMoreListener != null) {
                            synchronized (mLockObject) {
                                setState(LoadState.LOADING);
                                mOnLoadMoreListener.onLoad(view);
                            }
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
            QLog.d("scroll", "未滚动到底部，第二次拖至底部都初始化");
            // 未滚动到底部，第二次拖至底部都初始化
            mLastVisiblePosition = 0;
            mLastVisiblePositionY = 0;
        }
        if (mWrappedListener != null) {
            mWrappedListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mWrappedListener != null) {
            mWrappedListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public LoadState getState() {
        return this.mState;
    }

    public void setState(LoadState state) {
        this.mState = state;
    }
}