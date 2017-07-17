package com.haolb.client.adapter.utils;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author zitian.zhang
 * @since 2012-12-13 下午6:51:21
 * 
 */
public interface IExSupportV7 {
    public int getChildType(int groupPosition, int childPosition);

    public int getChildTypeCount();

    public int getGroupType(int groupPosition);

    public int getGroupTypeCount();

    public void registerDataSetObserver(DataSetObserver dataSetObserver);

    /**
     * @return
     */
    public boolean areAllItemsEnabled();

    /**
     * @param groupPosition
     * @param childPosition
     * @return
     */
    public Object getChild(int groupPosition, int childPosition);

    /**
     * @param groupPosition
     * @param childPosition
     * @return
     */
    public long getChildId(int groupPosition, int childPosition);

    /**
     * @param groupPosition
     * @return
     */
    public int getChildrenCount(int groupPosition);

    /**
     * @param groupPosition
     * @param childPosition
     * @param isLastChild
     * @param convertView
     * @param parent
     * @return
     */
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent);

    /**
     * @param groupPosition
     * @return
     */
    public Object getGroup(int groupPosition);

    /**
     * @return
     */
    public int getGroupCount();

    /**
     * @param groupPosition
     * @return
     */
    public long getGroupId(int groupPosition);

    /**
     * @param groupPosition
     * @param isExpanded
     * @param convertView
     * @param parent
     * @return
     */
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent);

    /**
     * @return
     */
    public boolean hasStableIds();

    /**
     * @param groupPosition
     * @param childPosition
     * @return
     */
    public boolean isChildSelectable(int groupPosition, int childPosition);

    /**
     * @param groupPosition
     */
    public void onGroupCollapsed(int groupPosition);

    /**
     * @param groupPosition
     */
    public void onGroupExpanded(int groupPosition);
}
