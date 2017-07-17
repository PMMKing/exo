package com.haolb.client.utils;

/**
 * 加解密工具类
 * @author zexu
 *
 */
public abstract class SecureUtil {

	static {
		System.loadLibrary("SecureUtil");
	}
	
	//加密
	public static native String encode(String content,String key);
	
	//解密
	public static native String decode(String content,String key);
	
	
}
