package com.haolb.client.adapter.utils;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

/**
 * @author zitian.zhang
 * @since 2012-4-24 下午02:21:29
 */
public class ExpandableListAdapterWrapper extends BaseExpandableListAdapter implements IExSupportV7 {

    private IExSupportV7 wrapped = null;

    public ExpandableListAdapterWrapper(IExSupportV7 wrapped) {
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
    public boolean areAllItemsEnabled() {
        return wrapped.areAllItemsEnabled();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return wrapped.getChild(groupPosition, childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return wrapped.getChildId(groupPosition, childPosition);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return wrapped.getChildrenCount(groupPosition);
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return wrapped.getChildType(groupPosition, childPosition);
    }

    @Override
    public int getChildTypeCount() {
        return wrapped.getChildTypeCount();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent) {
        return wrapped.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return wrapped.getGroup(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return wrapped.getGroupCount();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return wrapped.getGroupId(groupPosition);
    }

    @Override
    public int getGroupType(int groupPosition) {
        return wrapped.getGroupType(groupPosition);
    }

    @Override
    public int getGroupTypeCount() {
        return wrapped.getGroupTypeCount();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return wrapped.getGroupView(groupPosition, isExpanded, convertView, parent);
    }

    @Override
    public boolean hasStableIds() {
        return wrapped.hasStableIds();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return wrapped.isChildSelectable(groupPosition, childPosition);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        wrapped.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        wrapped.onGroupExpanded(groupPosition);
    }

    public IExSupportV7 getWrappedAdapter() {
        return wrapped;
    }
}