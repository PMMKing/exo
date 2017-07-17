package com.haolb.client.adapter.utils;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * @author zitian.zhang
 * @since 2012-4-24 下午09:17:57
 */
public class ScrollExpandableLoadMoreAdapter extends ExpandableLoadMoreAdapter implements OnScrollListener {

    AutoLoadListener mScrollLoadListener;

    public ScrollExpandableLoadMoreAdapter(IExSupportV7 wrapped) {
        super(wrapped);
        mScrollLoadListener = new AutoLoadListener();
    }

    public ScrollExpandableLoadMoreAdapter(Context context, IExSupportV7 wrapped, int clickResource, int pendingResource) {
        super(context, wrapped, clickResource, pendingResource);
        mScrollLoadListener = new AutoLoadListener();
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        super.setOnLoadMoreListener(onLoadMoreListener);
        mScrollLoadListener.setOnLoadMoreListener(onLoadMoreListener);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollLoadListener.onScrollStateChanged(view, scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mScrollLoadListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    @Override
    public synchronized void setState(LoadState state) {
        super.setState(state);
        mScrollLoadListener.setState(state);
    }
}
