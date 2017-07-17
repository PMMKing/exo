package com.haolb.client.utils.tuski;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * 
 * @author zitian.zhang
 * @since 2013-1-18 下午3:24:26
 * 
 */
final class Animations {
    private static Animation fadeInAnimation, fadeOutAnimation;

    private Animations() {
    }

    public static Animation genFadeInAnim() {
        if (fadeInAnimation == null) {
            fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
            fadeInAnimation.setDuration(400);
        }
        return fadeInAnimation;
    }

    public static Animation genFadeOutAnim() {
        if (fadeOutAnimation == null) {
            fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
            fadeOutAnimation.setDuration(400);
        }
        return fadeOutAnimation;
    }
}
