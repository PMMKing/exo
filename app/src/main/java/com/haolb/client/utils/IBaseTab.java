package com.haolb.client.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TabHost;

/**
 * @author zitian.zhang
 * @since 2013-5-23 下午2:41:26
 */
public interface IBaseTab {

	public abstract void addTab(String tag, String text, int icon, int id, Class<? extends Fragment> clss, Bundle args);
	
    public abstract void addTab(String tag, String text, int icon, Class<? extends Fragment> clss, Bundle args);

    public abstract void addTab(String tag, int icon, int id, Class<? extends Fragment> clss, Bundle args);

    public abstract void addTab(String tag, View view, Class<? extends Fragment> clss, Bundle args);

    public abstract void addTab(String tag, View view, int id);

    public abstract void addTab(String tag, String text, int icon, int id);

    public abstract TabHost getTabHost();

    public abstract void setCurrentTab(int position);

    public abstract String genDefaultTag();

}