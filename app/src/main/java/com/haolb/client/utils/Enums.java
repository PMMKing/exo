package com.haolb.client.utils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author zitian.zhang
 * @since 2012-6-1 下午03:45:54
 */
public enum Enums {
    ;

    /**
     * 获得code对应的enum
     * 
     * @author zitian.zhang
     * @param <T>
     * @param c
     * @param code
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends ITypeCode> T getTypeOf(Class<T> c, int code) {
        try {
            Method m = c.getMethod("values", (Class<T>[]) null);
            T[] ret = (T[]) m.invoke(null);
            for (T type : ret) {
                int tv = type.getCode();
                if (tv == code) {
                    return type;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends ITypeCode> T optTypeOf(Class<T> c, int code, T fallback) {
        T ret = getTypeOf(c, code);
        return ret == null ? fallback : ret;
    }

    /**
     * 获得expr对应的第一个enum(可能会有多个，只会找到第一个)
     * 
     * @author zitian.zhang
     * @param <T>
     * @param c
     * @param desc
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends ITypeDesc> T getFirstTypeOf(Class<T> c, String desc) {
        try {
            Method m = c.getMethod("values", (Class<T>[]) null);
            T[] ret = (T[]) m.invoke(null);
            for (T type : ret) {
                String tv = type.getDesc();
                if (tv.equalsIgnoreCase(desc)) {
                    return type;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface ITypeCode extends Serializable {

        public int getCode();
    }

    public interface ITypeDesc extends Serializable {

        public String getDesc();
    }

    public interface IType extends ITypeCode, ITypeDesc {
    }
}
