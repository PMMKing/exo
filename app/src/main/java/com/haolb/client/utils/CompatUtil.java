package com.haolb.client.utils;

import android.os.Build;

public enum CompatUtil {
    ;
    static final int API3 = 3;
    static final int API4 = 4;
    static final int API5 = 5;
    static final int API8 = 8;
    @SuppressWarnings("deprecation")
    static int version = Integer.valueOf(Build.VERSION.SDK).intValue();

    public static int getSDKVersion() {
        return version;
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return getSDKVersion() >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return getSDKVersion() >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return getSDKVersion() >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return getSDKVersion() >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return getSDKVersion() >= Build.VERSION_CODES.JELLY_BEAN;
    }
}