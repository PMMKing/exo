package com.haolb.client.utils;

/**
 * 
 * @author zexu
 *
 */
public enum EqualUtils {
    ;

    public static boolean equals(Object lhs, Object rhs) {
        if (lhs == null) {
            if (rhs != null) {
                return false;
            }
        } else if (!lhs.equals(rhs)) {
            return false;
        }
        return true;
    }

    public static boolean equalsIgnoreCase(String lhs, String rhs) {
        if (lhs == null) {
            if (rhs != null) {
                return false;
            }
        } else if (!lhs.equalsIgnoreCase(rhs)) {
            return false;
        }
        return true;
    }
}
