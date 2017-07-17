package com.haolb.client.adapter.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.haolb.client.R;

/**
 * @author zitian.zhang
 * @since 2012-4-24 下午02:22:10
 */
public class ExpandableLoadMoreAdapter extends ExpandableListAdapterWrapper {

    protected View clickView = null;
    protected View pendingView = null;
    protected Context context;
    protected int clickResource = -1;
    protected int pendingResource = -1;
    protected volatile boolean autoLoad = false;
    protected OnLoadMoreListener mOnLoadMoreListener;
    protected LoadState mState = LoadState.DONE;
    protected final Object mLockObject = new Object();

    public ExpandableLoadMoreAdapter(IExSupportV7 wrapped) {
        super(wrapped);
    }

    public ExpandableLoadMoreAdapter(Context context, IExSupportV7 wrapped) {
        super(wrapped);
        this.context = context;
        this.pendingResource = R.layout.item_loading;
        this.clickResource = R.layout.item_click_load;
    }

    public ExpandableLoadMoreAdapter(Context context, IExSupportV7 wrapped, int clickResource, int pendingResource) {
        super(wrapped);
        this.context = context;
        this.pendingResource = pendingResource;
        this.clickResource = clickResource;
    }

    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    public boolean isAutoLoad() {
        return autoLoad;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (super.getGroupCount() == groupPosition) {
            return 0;
        }
        return super.getChildrenCount(groupPosition);
    }

    @Override
    public int getGroupCount() {
        if (mState == LoadState.DONE || mState == LoadState.LOADING) {
            return super.getGroupCount() + 1; // one more for "pending"
        }
        return super.getGroupCount();
    }

    @Override
    public int getGroupType(int position) {
        if (position == getWrappedAdapter().getGroupCount()) {
            return -1;
        }
        return super.getGroupType(position);
    }

    @Override
    public int getGroupTypeCount() {
        return super.getGroupTypeCount() + 1;
    }

    @Override
    public View getGroupView(int position, boolean isExpanded, View convertView, final ViewGroup parent) {
        if (position == super.getGroupCount()) {
            switch (mState) {
            case DONE:
                if (clickView == null) {
                    clickView = getClickView(parent);
                    if (autoLoad) {
                        if (mOnLoadMoreListener != null) {
                            synchronized (mLockObject) {
                                mState = LoadState.LOADING;
                                mOnLoadMoreListener.onLoad((AdapterView<?>) parent);
                                notifyDataSetChanged();
                            }
                        }
                    } else {
                        clickView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (mOnLoadMoreListener != null) {
                                    synchronized (mLockObject) {
                                        mState = LoadState.LOADING;
                                        mOnLoadMoreListener.onLoad((AdapterView<?>) parent);
                                        notifyDataSetChanged();
                                    }
                                }
                            }
                        });
                    }
                }
                return clickView;
            case LOADING:
                if (pendingView == null) {
                    pendingView = getPendingView(parent);
                }
                return pendingView;
            }
        }
        return super.getGroupView(position, isExpanded, convertView, parent);
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
        this.mState = state;
        notifyDataSetChanged();
    }

    public OnLoadMoreListener getOnLoadMoreListener() {
        return this.mOnLoadMoreListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }
}