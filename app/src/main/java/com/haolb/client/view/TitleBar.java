package com.haolb.client.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.utils.inject.From;
import com.haolb.client.utils.inject.Injector;


/**
 * 标题栏
 * @author zexu
 *
 */
public class TitleBar extends LinearLayout {

	private OnClickListener listener;
	private OnClickListener rightListener;
	private boolean hasBackBtn;
	private String title;
	private String titleRightText;
	private int rightIconResId;

	@From(R.id.title_left_btn)
	private View ivLeft; // 左侧按钮
	@From(R.id.title_text)
	private TextView tvTitle; // 标题
	// 右侧区域
	@From(R.id.tv_right_text)
	private TextView tvRightText; 

	public TitleBar(Context context) {
		super(context);
		initView(context);
	}

	public TitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public TitleBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            this.setPadding(0, BitmapHelper.dip2px(context, 18) ,0,0);
//            this.setBackgroundResource(R.color.t_theme);
//        }
		LayoutInflater.from(context).inflate(R.layout.title_bar, this, true);
		Injector.inject(this);
		setBackgroundColor(Color.BLACK);
	}

	/**
	 * 设置标题栏
	 * @param listener
	 * @param hasBackBtn
	 * @param title
	 */
	public void setTitleBar(OnClickListener listener, boolean hasBackBtn,
			String title, String titleRightText,OnClickListener rightListener) {
		this.listener = listener;
		this.hasBackBtn = hasBackBtn;
		this.title = title;
		this.titleRightText = titleRightText;
		this.rightListener = rightListener;
		reLayout(0);
	}
	
	/**
	 * 设置标题栏
	 * @param listener
	 * @param hasBackBtn
	 * @param title
	 */
	public void setTitleBar(OnClickListener listener, boolean hasBackBtn,
			String title, int rightIconResId,OnClickListener rightListener) {
		this.listener = listener;
		this.hasBackBtn = hasBackBtn;
		this.title = title;
		this.rightIconResId = rightIconResId;
		this.rightListener = rightListener;
		reLayout(1);
	}
	
	/**
	 * 设置标题栏
	 * @param listener
	 * @param hasBackBtn
	 * @param title
	 */
	public void setTitleBar(OnClickListener listener, boolean hasBackBtn,
			String title) {
		this.listener = listener;
		this.hasBackBtn = hasBackBtn;
		this.title = title;
		reLayout(3);
	}

	public void reLayout(int type) {
		requestLayout();
		// 显示左边返回按钮
		if (hasBackBtn) {
			ivLeft.setVisibility(VISIBLE);
			if (listener != null) {
				ivLeft.setOnClickListener(listener);
			}
		} else { // 左侧按钮不显示
			ivLeft.setVisibility(INVISIBLE);
		}
		tvTitle.setText(title);
		if(type == 3){
			tvRightText.setVisibility(INVISIBLE);
		} else {
			tvRightText.setVisibility(VISIBLE);
			tvRightText.setText(type ==0 ? titleRightText : "");
//			tvRightText.setBackgroundResource(type ==0 ? R.drawable.titlebar_right : rightIconResId);
			if(rightListener != null) {
				tvRightText.setOnClickListener(rightListener);
			}
		}
		
	}

	public void setTitle(String title) {
		tvTitle.setText(title);
	}
}
