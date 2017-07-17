package com.haolb.client.utils.tuski;

import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;

/**
 * The appearance for a {@link com.mixin.client.utils.tuski.Tuski}.
 */

public class Appearance {
    public static final Appearance DEFAULT_BOTTOM;
    public static final Appearance DEFAULT_TOP;
    public static final Appearance BOTTOM_LONG;

    static {
        DEFAULT_BOTTOM = new Builder().build();
        BOTTOM_LONG = new Builder().setDuration(3500).build();
        DEFAULT_TOP = new Builder().setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL).build();
    }

    final int duration;
    final int backgroundResourceId;
    final int textColorResourceId;
    final int height;
    final int width;
    final int gravity;
    /**
     * The text size in dp
     * <p/>
     * 0 sets the text size to the system theme default
     */
    final int textSize;
    /**
     * The resource id for the in animation
     */
    final int inAnimationResId;
    /**
     * The resource id for the out animation
     */
    final int outAnimationResId;

    final Animation inAnimation;
    final Animation outAnimation;

    private Appearance(final Builder builder) {
        this.duration = builder.duration;
        this.backgroundResourceId = builder.backgroundResourceId;
        this.textColorResourceId = builder.textColorResourceId;
        this.height = builder.height;
        this.width = builder.width;
        this.gravity = builder.gravity;
        this.textSize = builder.textSize;
        this.inAnimationResId = builder.inAnimationResId;
        this.outAnimationResId = builder.outAnimationResId;
        this.inAnimation = builder.inAnimation;
        this.outAnimation = builder.outAnimation;
    }

    /**
     * Builder for the {@link com.mixin.client.utils.tuski.Appearance} object.
     */
    public static class Builder {
        private int duration;
        private int backgroundResourceId;
        private int textColorResourceId;
        private int height;
        private int width;
        private int gravity;
        private int textSize;
        private int inAnimationResId;
        private int outAnimationResId;
        private Animation outAnimation;
        private Animation inAnimation;

        public Builder() {
            duration = 2000;
            backgroundResourceId = android.R.drawable.toast_frame;
            textColorResourceId = android.R.color.white;
            height = LayoutParams.WRAP_CONTENT;
            width = LayoutParams.MATCH_PARENT;
            gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            inAnimationResId = 0;
            outAnimationResId = 0;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder setBackgroundResource(int backgroundDrawableResourceId) {
            this.backgroundResourceId = backgroundDrawableResourceId;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setTextColorResource(int textColor) {
            this.textColorResourceId = textColor;
            return this;
        }

        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * The text size in dp
         */
        public Builder setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder setInAnimation(int inAnimationResId) {
            this.inAnimationResId = inAnimationResId;
            return this;
        }

        public Builder setInAnimation(Animation animation) {
            this.inAnimation = animation;
            return this;
        }

        public Builder setOutAnimation(int outAnimationResId) {
            this.outAnimationResId = outAnimationResId;
            return this;
        }

        public Builder setOutAnimation(Animation animation) {
            this.outAnimation = animation;
            return this;
        }

        public Appearance build() {
            return new Appearance(this);
        }
    }
}
