package com.haolb.client.adapter.utils;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;

/**
 * TODO 暂时有问题 不要用这个adapter
 * 
 * @author zitian.zhang
 * @since 2012-4-24 下午09:17:57
 */
public class ScrollLoadMoreAdapter extends LoadMoreAdapter implements OnScrollListener {

    AutoLoadListener mScrollLoadListener;

    public ScrollLoadMoreAdapter(Context context, ListAdapter wrapped, int clickResource, int pendingResource,
            int failedResource, int totalCount) {
        super(context, wrapped, clickResource, pendingResource, failedResource, totalCount);
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
    public void setState(LoadState state) {
        super.setState(state);
        mScrollLoadListener.setState(state);
    }
}
