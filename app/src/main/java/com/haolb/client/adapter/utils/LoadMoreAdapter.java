package com.haolb.client.adapter.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.haolb.client.R;

/**
 * @author zitian.zhang
 * @since 2012-4-24 下午02:21:36
 */
public class LoadMoreAdapter extends AdapterWrapper {

    protected View clickView = null;
    protected View pendingView = null;
    protected View failedView = null;
    protected Context context;
    protected int clickResource = -1;
    protected int pendingResource = -1;
    protected int failedResource = -1;
    protected volatile boolean autoLoad = false;
    protected OnLoadMoreListener mOnLoadMoreListener;
    protected LoadState mState = LoadState.DONE;
    protected final Object mLockObject = new Object();

    public LoadMoreAdapter(Context context, ListAdapter wrapped, int totalCount) {
        this(context, wrapped, R.layout.item_click_load, R.layout.item_loading, R.layout.item_failed, totalCount);
    }

    public LoadMoreAdapter(Context context, ListAdapter wrapped, int clickResource, int pendingResource,
            int failedResource, int totalCount) {
        super(wrapped);
        this.context = context;
        this.pendingResource = pendingResource;
        this.clickResource = clickResource;
        this.failedResource = failedResource;
        this.setTotalCount(totalCount);
    }

    /**
     * 设置总数（会触发加载完毕）
     * 
     * @param totalCount
     */
    public void setTotalCount(int totalCount) {
        hasMore(getWrappedAdapter().getCount() < totalCount);
    }

    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    public boolean isAutoLoad() {
        return autoLoad;
    }

    @Override
    public int getCount() {
        switch (mState) {
        case DONE:
        case LOADING:
        case FAILED:
            return super.getCount() + 1;
        default:
            return super.getCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getWrappedAdapter().getCount()) {
            return IGNORE_ITEM_VIEW_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (position == super.getCount()) {
            switch (mState) {
            case DONE:
                if (autoLoad) {
                    if (pendingView == null) {
                        pendingView = getPendingView(parent);
                    }
                    if (mOnLoadMoreListener != null) {
                        synchronized (mLockObject) {
                            setState(LoadState.LOADING);
                            mOnLoadMoreListener.onLoad((AdapterView<?>) parent);
                            // notifyDataSetChanged();
                        }
                    }
                    return pendingView;
                } else {
                    if (clickView == null) {
                        clickView = getClickView(parent);
                        clickView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (mOnLoadMoreListener != null) {
                                    synchronized (mLockObject) {
                                        mOnLoadMoreListener.onLoad((AdapterView<?>) parent);
                                        // notifyDataSetChanged();
                                    }
                                }
                                setState(LoadState.LOADING);
                            }
                        });
                    }
                    return clickView;
                }
            case LOADING:
                if (pendingView == null) {
                    pendingView = getPendingView(parent);
                    pendingView.setClickable(true);
                }
                return pendingView;
            case FAILED:
                if (failedView == null) {
                    failedView = getFailedView(parent);
                    failedView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (mOnLoadMoreListener != null) {
                                synchronized (mLockObject) {
                                    mOnLoadMoreListener.onLoad((AdapterView<?>) parent);
                                    // notifyDataSetChanged();
                                }
                            }
                            setState(LoadState.LOADING);
                        }
                    });
                }
                return failedView;
            }
        }
        return super.getView(position, convertView, parent);
    }

    protected View getFailedView(ViewGroup parent) {
        if (context != null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(failedResource, parent, false);
        }
        throw new RuntimeException(
                "You must either override getFailedView() or supply a pending View resource via the constructor");
    }

    protected View getPendingView(ViewGroup parent) {
        if (context != null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(pendingResource, parent, false);
        }
        throw new RuntimeException(
                "You must either override getPendingView() or supply a pending View resource via the constructor");
    }

    protected View getClickView(ViewGroup parent) {
        if (context != null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(clickResource, parent, false);
        }
        throw new RuntimeException(
                "You must either override getClickView() or supply a click View resource via the constructor");
    }

    public LoadState getState() {
        return this.mState;
    }

    public void setState(LoadState state) {
        if (state == null) {
            throw new NullPointerException("load state must not be null");
        }
        this.mState = state;
        notifyDataSetChanged();
    }

    public void hasMore(boolean hasMore) {
        if (hasMore) {
            this.mState = LoadState.DONE;
        } else {
            this.mState = LoadState.DISABLE;
        }
        notifyDataSetChanged();
    }

    public OnLoadMoreListener getOnLoadMoreListener() {
        return this.mOnLoadMoreListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }
}