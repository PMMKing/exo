package com.haolb.client.adapter.utils;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

/**
 * @author zitian.zhang
 * @since 2012-4-24 下午02:19:26
 */
public class AdapterWrapper extends BaseAdapter implements WrapperListAdapter {

    private ListAdapter wrapped = null;

    public AdapterWrapper(ListAdapter wrapped) {
        super();
        this.wrapped = wrapped;
        wrapped.registerDataSetObserver(new DataSetObserver() {

            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                notifyDataSetInvalidated();
            }
        });
    }

    @Override
    public Object getItem(int position) {
        return wrapped.getItem(position);
    }

    @Override
    public int getCount() {
        return wrapped.getCount();
    }

    @Override
    public int getViewTypeCount() {
        return wrapped.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return wrapped.getItemViewType(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return wrapped.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return wrapped.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return wrapped.getView(position, convertView, parent);
    }

    @Override
    public long getItemId(int position) {
        return wrapped.getItemId(position);
    }

    @Override
    public ListAdapter getWrappedAdapter() {
        return wrapped;
    }
}