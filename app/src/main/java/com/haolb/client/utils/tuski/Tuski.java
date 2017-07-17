package com.haolb.client.utils.tuski;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 消息提示，用于替换Toast(这玩意从4.2开始可以被用户禁用了 哇擦雷) <br / >
 * 不过由于系统安全限制，使用Tuski进行提示时消息仅能悬浮于当前activity前面，这个问题后续考虑解决::>_<::<br />
 * <p>
 * WARNING!!! {@link android.app.Activity#onDestory()}时要调用 Tuski.clearTuskiesForActivity(this); 防止activity泄露
 * </p>
 *
 * @author zitian.zhang
 * @since 2013-1-18 上午11:04:59
 *
 */
public final class Tuski {
    private static final int TEXT_ID = 0x101;
    public static final int INDEX_HEAD = 0;
    public static final int INDEX_TAIL = -1;
    private final CharSequence text;
    private final Appearance style;
    private final View customView;

    private Activity activity;
    private ViewGroup viewGroup;
    int indexAddToViewGroup = INDEX_HEAD;
    private FrameLayout tuskiView;
    private Animation inAnimation;
    private Animation outAnimation;
    TuskiListener listener;

    private Tuski(Activity activity, CharSequence text, Appearance style) {
        if (activity == null || text == null || style == null) {
            throw new IllegalArgumentException("Null parameters are not accepted");
        }
        this.activity = activity;
        this.viewGroup = null;
        this.text = text;
        this.style = style;
        this.customView = null;
    }

    private Tuski(Activity activity, CharSequence text, Appearance style, ViewGroup viewGroup, int indexAddToViewGroup) {
        if (activity == null || text == null || style == null) {
            throw new IllegalArgumentException("Null parameters are not accepted");
        }

        this.activity = activity;
        this.text = text;
        this.style = style;
        this.viewGroup = viewGroup;
        this.indexAddToViewGroup = indexAddToViewGroup;
        this.customView = null;
    }

    private Tuski(Activity activity, View customView) {
        if (activity == null || customView == null) {
            throw new IllegalArgumentException("Null parameters are not accepted");
        }

        this.activity = activity;
        this.viewGroup = null;
        this.customView = customView;
        this.style = new Appearance.Builder().build();
        this.text = null;
    }

    private Tuski(Activity activity, View customView, ViewGroup viewGroup, int indexAddToViewGroup) {
        if (activity == null || customView == null) {
            throw new IllegalArgumentException("Null parameters are not accepted");
        }

        this.activity = activity;
        this.customView = customView;
        this.viewGroup = viewGroup;
        this.indexAddToViewGroup = indexAddToViewGroup;
        this.style = new Appearance.Builder().build();
        this.text = null;

    }

    /**
     * Creates a {@link com.mixin.client.utils.tuski.Tuski} with provided text and style for a given activity.
     *
     * @param activity The {@link android.app.Activity} that the {@link com.mixin.client.utils.tuski.Tuski} should be attached to.
     * @param text The text you want to display.
     * @param style The style that this {@link com.mixin.client.utils.tuski.Tuski} should be created with.
     *
     * @return The created {@link com.mixin.client.utils.tuski.Tuski}.
     */
    public static Tuski makeText(Activity activity, CharSequence text, Appearance style) {
        return new Tuski(activity, text, style);
    }

    /**
     * Creates a {@link com.mixin.client.utils.tuski.Tuski} with provided text and style for a given activity.
     *
     * @param activity The {@link android.app.Activity} that represents the context in which the Tuski should exist.
     * @param text The text you want to display.
     * @param style The style that this {@link com.mixin.client.utils.tuski.Tuski} should be created with.
     * @param viewGroup The {@link android.view.ViewGroup} that this {@link com.mixin.client.utils.tuski.Tuski} should be added to.
     *
     * @return The created {@link com.mixin.client.utils.tuski.Tuski}.
     */
    public static Tuski makeText(Activity activity, CharSequence text, Appearance style, ViewGroup viewGroup,
            int indexAddToViewGroup) {
        return new Tuski(activity, text, style, viewGroup, indexAddToViewGroup);
    }

    /**
     * Creates a {@link com.mixin.client.utils.tuski.Tuski} with provided text-resource and style for a given activity.
     *
     * @param activity The {@link android.app.Activity} that the {@link com.mixin.client.utils.tuski.Tuski} should be attached to.
     * @param textResourceId The resource id of the text you want to display.
     * @param style The style that this {@link com.mixin.client.utils.tuski.Tuski} should be created with.
     *
     * @return The created {@link com.mixin.client.utils.tuski.Tuski}.
     */
    public static Tuski makeText(Activity activity, int textResourceId, Appearance style) {
        return makeText(activity, activity.getString(textResourceId), style);
    }

    /**
     * Creates a {@link com.mixin.client.utils.tuski.Tuski} with provided text-resource and style for a given activity.
     *
     * @param activity The {@link android.app.Activity} that represents the context in which the Tuski should exist.
     * @param textResourceId The resource id of the text you want to display.
     * @param style The style that this {@link com.mixin.client.utils.tuski.Tuski} should be created with.
     * @param viewGroup The {@link android.view.ViewGroup} that this {@link com.mixin.client.utils.tuski.Tuski} should be added to.
     *
     * @return The created {@link com.mixin.client.utils.tuski.Tuski}.
     */
    public static Tuski makeText(Activity activity, int textResourceId, Appearance style, ViewGroup viewGroup,
            int indexAddToViewGroup) {
        return makeText(activity, activity.getString(textResourceId), style, viewGroup, indexAddToViewGroup);
    }

    /**
     * Creates a {@link com.mixin.client.utils.tuski.Tuski} with provided text-resource and style for a given activity.
     *
     * @param activity The {@link android.app.Activity} that the {@link com.mixin.client.utils.tuski.Tuski} should be attached to.
     * @param customView The custom {@link android.view.View} to display
     *
     * @return The created {@link com.mixin.client.utils.tuski.Tuski}.
     */
    public static Tuski makeCustomView(Activity activity, View customView) {
        return new Tuski(activity, customView);
    }

    /**
     * Creates a {@link com.mixin.client.utils.tuski.Tuski} with provided text-resource and style for a given activity.
     *
     * @param activity The {@link android.app.Activity} that represents the context in which the Tuski should exist.
     * @param customView The custom {@link android.view.View} to display
     * @param viewGroup The {@link android.view.ViewGroup} that this {@link com.mixin.client.utils.tuski.Tuski} should be added to.
     *
     * @return The created {@link com.mixin.client.utils.tuski.Tuski}.
     */
    public static Tuski makeCustomView(Activity activity, View customView, ViewGroup viewGroup, int indexAddToViewGroup) {
        return new Tuski(activity, customView, viewGroup, indexAddToViewGroup);
    }

    /**
     * Cancels all queued {@link com.mixin.client.utils.tuski.Tuski}s. If there is a {@link com.mixin.client.utils.tuski.Tuski} displayed currently, it will be the last one
     * displayed.
     */
    public static void cancelAllTuskies() {
        TuskiHandler.getInstance().clearTuskiQueue();
    }

    /**
     * Clears (and removes from {@link android.app.Activity}'s content view, if necessary) all tuskies for the provided activity
     *
     * @param activity - The {@link android.app.Activity} to clear the tuskies for.
     */
    public static void clearTuskiesForActivity(Activity activity) {
        TuskiHandler.getInstance().clearTuskisForActivity(activity);
    }

    /**
     * Cancels a {@link com.mixin.client.utils.tuski.Tuski} immediately.
     */
    public void cancel() {
        TuskiHandler manager = TuskiHandler.getInstance();
        manager.removeTuskiImmediately(this);
    }

    /**
     * Displays the {@link com.mixin.client.utils.tuski.Tuski}. If there's another {@link com.mixin.client.utils.tuski.Tuski} visible at the time, this {@link com.mixin.client.utils.tuski.Tuski} will be
     * displayed afterwards.
     */
    public void show() {
        TuskiHandler.getInstance().add(this);
    }

    public Animation getInAnimation() {
        if (this.inAnimation == null && this.activity != null) {
            if (getStyle().inAnimation != null) {
                this.inAnimation = getStyle().inAnimation;
            } else if (getStyle().inAnimationResId > 0) {
                this.inAnimation = AnimationUtils.loadAnimation(getActivity(), getStyle().inAnimationResId);
            } else {
                this.inAnimation = Animations.genFadeInAnim();
            }
        }

        return inAnimation;
    }

    public Animation getOutAnimation() {
        if (this.outAnimation == null && this.activity != null) {
            if (getStyle().outAnimation != null) {
                this.outAnimation = getStyle().outAnimation;
            } else if (getStyle().outAnimationResId > 0) {
                this.outAnimation = AnimationUtils.loadAnimation(getActivity(), getStyle().outAnimationResId);
            } else {
                this.outAnimation = Animations.genFadeOutAnim();
            }
        }

        return outAnimation;
    }

    public void setTuskiListener(TuskiListener l) {
        this.listener = l;
    }

    boolean isShowing() {
        return activity != null && tuskiView != null && tuskiView.getParent() != null;
    }

    void detachActivity() {
        activity = null;
    }

    void detachViewGroup() {
        viewGroup = null;
    }

    void detachListener() {
        listener = null;
    }

    Appearance getStyle() {
        return style;
    }

    Activity getActivity() {
        return activity;
    }

    ViewGroup getViewGroup() {
        return viewGroup;
    }

    CharSequence getText() {
        return text;
    }

    View getView() {
        // return the custom view if one exists
        if (this.customView != null) {
            return this.customView;
        }

        // if already setup return the view
        if (this.tuskiView == null) {
            initializeTuskiView();
        }

        return tuskiView;
    }

    private void initializeTuskiView() {
        Resources resources = this.activity.getResources();

        this.tuskiView = initializeTuskiViewGroup(resources);

        // create content view
        RelativeLayout contentView = initializeContentView(resources);
        this.tuskiView.addView(contentView);
    }

    private FrameLayout initializeTuskiViewGroup(Resources resources) {
        FrameLayout tuskiView = new FrameLayout(this.activity);

        final int height = this.style.height;
        final int width = this.style.width;

        tuskiView.setLayoutParams(new FrameLayout.LayoutParams(width != 0 ? width
                : FrameLayout.LayoutParams.MATCH_PARENT, height));

        if (this.style.backgroundResourceId != 0) {
            tuskiView.setBackgroundResource(this.style.backgroundResourceId);
        }
        return tuskiView;
    }

    private RelativeLayout initializeContentView(final Resources resources) {
        RelativeLayout contentView = new RelativeLayout(this.activity);
        contentView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // contentView.setPadding(padding, padding, padding, padding);

        TextView text = initializeTextView(resources);

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        contentView.addView(text, textParams);
        return contentView;
    }

    private TextView initializeTextView(final Resources resources) {
        TextView text = new TextView(this.activity);
        text.setId(TEXT_ID);
        text.setText(this.text);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setGravity(Gravity.CENTER);

        // set the text color if set
        if (this.style.textColorResourceId != 0) {
            text.setTextColor(resources.getColor(this.style.textColorResourceId));
        }

        // Set the text size. If the user has set a text size and text
        // appearance, the text size in the text appearance
        // will override this.
        if (this.style.textSize != 0) {
            text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, this.style.textSize);
        }

        return text;
    }

}
