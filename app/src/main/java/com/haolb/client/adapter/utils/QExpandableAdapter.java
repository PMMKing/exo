package com.haolb.client.adapter.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.haolb.client.R;
import com.haolb.client.utils.CompatUtil;

/**
 * @author zitian.zhang
 * @since 2013-5-27 上午11:49:27
 */
public abstract class QExpandableAdapter<Group, Child> extends
		BaseExpandableListAdapter {

	private static final int[] EMPTY_STATE_SET = {};
	/** State indicating the group is expanded. */
	private static final int[] GROUP_EXPANDED_STATE_SET = { android.R.attr.state_expanded };
	/** State indicating the group is empty (has no children). */
	private static final int[] GROUP_EMPTY_STATE_SET = { android.R.attr.state_empty };
	/** State indicating the group is expanded and empty (has no children). */
	private static final int[] GROUP_EXPANDED_EMPTY_STATE_SET = {
			android.R.attr.state_expanded, android.R.attr.state_empty };
	/** States for the group where the 0th bit is expanded and 1st bit is empty. */
	private static final int[][] GROUP_STATE_SETS = { EMPTY_STATE_SET, // 00
			GROUP_EXPANDED_STATE_SET, // 01
			GROUP_EMPTY_STATE_SET, // 10
			GROUP_EXPANDED_EMPTY_STATE_SET // 11
	};
	protected final Context mContext;
	protected final LayoutInflater mInflater;

	public QExpandableAdapter(Context context) {
		this.mContext = context;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View inflate(int resouse, ViewGroup root, boolean attachToRoot) {
		return mInflater.inflate(resouse, root, attachToRoot);
	}

	/**
	 * 适配2.3以下的{@link android.view.View#setTag(int, Object)},避免内存泄漏<br/>
	 * WARING!!! 通过这种方式setTag时请勿调用该view的{@link android.view.View#setTag(Object)} （会覆盖原本的Tag）
	 *
	 * @param view
	 * @param id
	 */
	@SuppressWarnings("unchecked")
	protected void setIdToTag(View view, int id) {
		if (CompatUtil.hasHoneycomb()) {
			view.setTag(id, view.findViewById(id));
		} else {
			Object tag = view.getTag();
			if (!SparseArray.class.isInstance(tag)) {
				tag = new SparseArray<Object>();
				view.setTag(tag);
			}
			((SparseArray<Object>) tag).put(id, view.findViewById(id));
			view.setTag(tag);
		}
	}

	/**
	 * WARING!!! 通过这种方式setTag时请勿调用该view的{@link android.view.View#getTag()} （会覆盖原本的Tag）
	 * 
	 * @param view
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected View getViewFromTag(View view, int id) {
		if (CompatUtil.hasHoneycomb()) {
			return (View) view.getTag(id);
		} else {
			Object tag = view.getTag();
			if (!SparseArray.class.isInstance(tag)) {
				return null;
			}
			return (View) ((SparseArray<Object>) tag).get(id);
		}
	}

	/**
	 * Returns the context associated with this array adapter. The context is
	 * used to create views from the resource passed to the constructor.
	 * 
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return mContext;
	}

	@Override
	public final void onGroupCollapsed(int groupPosition) {
		notifyDataSetChanged();
		onGroupCollapsedEx(groupPosition);
	}

	@Override
	public final void onGroupExpanded(int groupPosition) {
		notifyDataSetChanged();
		onGroupExpandedEx(groupPosition);
	}

	protected void onGroupCollapsedEx(int groupPosition) {
	}

	protected void onGroupExpandedEx(int groupPosition) {
	}

	@Override
	abstract public Group getGroup(int groupPosition);

	@Override
	abstract public Child getChild(int groupPosition, int childPosition);

	public int getGroupType(int groupPosition) {
		return 0;
	}

	public int getGroupTypeCount() {
		return 1;
	}

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		return 0;
	}

	public int getChildTypeCount() {
		return 1;
	}

	@Override
	public final View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View view;
		if (convertView != null
				&& convertView.getTag(R.id.view_type).equals(
						getGroupType(groupPosition) + 1)) {
			view = convertView;
		} else {
			view = newGroupView(mContext, parent, getGroupType(groupPosition));
			view.setTag(R.id.view_type, getGroupType(groupPosition) + 1);
		}

		bindGroupViewInternal(view, mContext, getGroup(groupPosition),
				getGroupType(groupPosition), groupPosition, isExpanded);

		return view;
	}

	@Override
	public final View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View view;
		if (convertView != null
				&& convertView.getTag(R.id.view_type).equals(
						getChildType(groupPosition, childPosition) + 1)) {
			view = convertView;
		} else {
			view = newChildView(mContext, parent,
					getChildType(groupPosition, childPosition));
			view.setTag(R.id.view_type,
					getChildType(groupPosition, childPosition) + 1);
		}

		bindChildView(view, mContext, getGroup(groupPosition),
				getChild(groupPosition, childPosition),
				getChildType(groupPosition, childPosition), groupPosition,
				childPosition, isLastChild);

		return view;
	}

	private void setIndicatorStateInternal(Drawable indicator,
			int groupPosition, boolean isExpanded) {
		final int stateSetIndex = (isExpanded ? 1 : 0) | // Expanded?
				(getChildrenCount(groupPosition) == 0 ? 2 : 0); // Empty?
		indicator.setState(GROUP_STATE_SETS[stateSetIndex]);
	}

	private void bindGroupViewInternal(View view, Context context, Group group,
			int groupType, int groupPosition, boolean isExpanded) {
		setIndicatorStateInternal(
				bindGroupView(view, context, group, groupType, groupPosition,
						isExpanded), groupPosition, isExpanded);
	}

	abstract protected View newGroupView(Context context, ViewGroup parent,
			int groupType);

	/**
	 * @return group indicator drawable
	 */
	abstract protected Drawable bindGroupView(View view, Context context,
			Group group, int groupType, int groupPosition, boolean isExpanded);

	abstract protected View newChildView(Context context, ViewGroup parent,
			int childType);

	abstract protected void bindChildView(View view, Context context,
			Group group, Child child, int childType, int groupPosition,
			int childPosition, boolean isLastChild);
}