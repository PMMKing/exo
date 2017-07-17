package com.haolb.client.adapter.utils;

/**
 * 加载更多的状态
 * 
 * @author zitian.zhang
 * @since 2012-4-24 下午03:06:12
 */
public enum LoadState {
    /** 加载完成或等待加载 */
    DONE,
    /** 正在加载 */
    LOADING,
    /** 不加载 */
    DISABLE,
    /** 加载失败 */
    FAILED,
}
