package com.haolb.client.adapter.utils;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.haolb.client.utils.CompatUtil;
import com.haolb.client.utils.QArrays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 加强版ArrayAdapter
 * 
 * @author zitian.zhang
 */
public abstract class QArrayAdapter<T> extends BaseAdapter implements Filterable {

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter. The content of this list is referred
     * to as "the array" in the documentation.
     */
    protected List<T> mObjects;
    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation performed on the array should be
     * synchronized on this lock. This lock is also used by the filter (see {@link #getFilter()} to make a synchronized
     * copy of the original array of data.
     */
    protected final Object mLock = new Object();
    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = true;
    protected Context mContext;
    protected ArrayList<T> mOriginalValues;
    private ArrayFilter mFilter;
    protected LayoutInflater mInflater;

    /**
     * Constructor
     * 
     * @param context The current context.
     */
    public QArrayAdapter(Context context) {
        init(context, new ArrayList<T>());
    }

    /**
     * Constructor
     * 
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     */
    public QArrayAdapter(Context context, T[] objects) {
        init(context, QArrays.asList(objects));
    }

    /**
     * Constructor
     * 
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     * @param readOnly 如果为只读 则无法向adapter加入新的数据（将抛出异常）
     */
    public QArrayAdapter(Context context, T[] objects, boolean readOnly) {
        init(context, readOnly ? QArrays.asReadOnlyList(objects) : QArrays.asList(objects));
    }

    /**
     * Constructor
     * 
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     */
    public QArrayAdapter(Context context, List<T> objects) {
        init(context, objects);
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
     * Adds the specified object at the end of the array.
     * 
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.add(object);
                if (mNotifyOnChange) {
                    notifyDataSetChanged();
                }
            }
        } else {
            mObjects.add(object);
            if (mNotifyOnChange) {
                notifyDataSetChanged();
            }
        }
    }

    public void addAll(int index, List<T> notes) {
        if (notes == null) {
            return;
        }
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.addAll(index, notes);
                if (mNotifyOnChange) {
                    notifyDataSetChanged();
                }
            }
        } else {
            mObjects.addAll(index, notes);
            if (mNotifyOnChange) {
                notifyDataSetChanged();
            }
        }
    }

    public void addAll(int index, T[] notes) {
        addAll(index, QArrays.asReadOnlyList(notes));
    }

    public void addAll(List<T> notes) {
        if (notes == null) {
            return;
        }
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.addAll(notes);
                if (mNotifyOnChange) {
                    notifyDataSetChanged();
                }
            }
        } else {
            mObjects.addAll(notes);
            if (mNotifyOnChange) {
                notifyDataSetChanged();
            }
        }
    }

    public void addAll(T[] notes) {
        addAll(QArrays.asReadOnlyList(notes));
    }
    
    public void setData(List<T> notes) {
    	clear();
    	addAll(notes);
    }

    /**
     * Inserts the specified object at the specified index in the array.
     * 
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(T object, int index) {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.add(index, object);
                if (mNotifyOnChange) {
                    notifyDataSetChanged();
                }
            }
        } else {
            mObjects.add(index, object);
            if (mNotifyOnChange) {
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Removes the specified object from the array.
     * 
     * @param object The object to remove.
     */
    public void remove(T object) {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.remove(object);
            }
        } else {
            mObjects.remove(object);
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.clear();
            }
        } else {
            mObjects.clear();
        }
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     * 
     * @param comparator The comparator used to sort the objects contained in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        Collections.sort(mObjects, comparator);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * Control whether methods that change the list ({@link #add}, {@link #insert}, {@link #remove}, {@link #clear})
     * automatically call {@link #notifyDataSetChanged}. If set to false, caller must manually call
     * notifyDataSetChanged() to have the changes reflected in the attached view. The default is true, and calling
     * notifyDataSetChanged() resets the flag to true.
     * 
     * @param notifyOnChange if true, modifications to the list will automatically call {@link #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    private void init(Context context, List<T> objects) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObjects = objects;
        if (mObjects == null) {
            throw new NullPointerException("list must not be null");
        }
    }

    // public View inflate(int resouse) {
    // return mInflater.inflate(resouse, null);
    // }
    public View inflate(int resouse, ViewGroup root, boolean attachToRoot) {
        return mInflater.inflate(resouse, root, attachToRoot);
    }

    /**
     * Returns the context associated with this array adapter. The context is used to create views from the resource
     * passed to the constructor.
     * 
     * @return The Context associated with this adapter.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mObjects.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getItem(int position) {
        return mObjects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     * 
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     * <p>
     * An array filter constrains the content of the array adapter with a prefix. Each item that does not start with the
     * supplied prefix is removed from the list.
     * </p>
     */
    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<T>(mObjects);
                }
            }
            if (prefix == null || prefix.length() == 0) {
                synchronized (mLock) {
                    ArrayList<T> list = new ArrayList<T>(mOriginalValues);
                    results.values = list;
                    results.count = list.size();
                }
            } else {
                String prefixString = prefix.toString().toLowerCase(Locale.US);
                final ArrayList<T> values = mOriginalValues;
                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<T>(count);
                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    final String valueText = value.toString().toLowerCase(Locale.US);
                    // First match against the whole, non-splitted value
                    if (valueText.contains(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // noinspection unchecked
            mObjects = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
