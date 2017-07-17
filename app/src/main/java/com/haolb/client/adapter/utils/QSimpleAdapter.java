package com.haolb.client.adapter.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.haolb.client.R;

import java.util.List;

/**
 * 方便列表使用，若列表项只有一种布局，使用这个Adapter可以简化开发并优化显示
 */
public abstract class QSimpleAdapter<T> extends QArrayAdapter<T> {

    public QSimpleAdapter(Context context, List<T> objects) {
        super(context, objects);
    }

    public QSimpleAdapter(Context context, T[] objects, boolean readOnly) {
        super(context, objects, readOnly);
    }

    public QSimpleAdapter(Context context, T[] objects) {
        super(context, objects);
    }

    public QSimpleAdapter(Context context) {
        super(context);
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView != null && convertView.getTag(R.id.view_type).equals(getItemViewType(position) + 1)) {
            // Log.d("getTag", convertView.getTag(R.id.view_type).toString());
            v = convertView;
        } else {
            v = newView(mContext, parent);
            v.setTag(R.id.view_type, getItemViewType(position) + 1);
        }
        if (position < mObjects.size()) {
            bindView(v, mContext, getItem(position), position);
        } else {
            bindView(v, mContext, null, position);
        }
        return v;
    }

    /**
     * Makes a new view to hold the data pointed to by arraylist.
     * 
     * @param context Interface to application's global information
     * @param parent The parent to which the new view is attached to
     * @return the newly created view.
     */
    protected abstract View newView(Context context, ViewGroup parent);

    /**
     * Bind an existing view to the data pointed to by arraylist
     * 
     * @param view Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param item Data at position.
     */
    protected abstract void bindView(View view, Context context, T item, int position);
}
