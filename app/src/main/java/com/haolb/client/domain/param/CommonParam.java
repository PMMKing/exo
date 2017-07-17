package com.haolb.client.domain.param;

import java.io.Serializable;

/**
 * 通用参数
 * @author zexu
 */
public class CommonParam implements Serializable {
	private static final long serialVersionUID = 1L;
	public String token;
	public Integer userId = -1;
	
	public String imei;
	public int platform = 1;////1:android,2:ios
	//客户端版本信息
	public String versionName;
	public int versionCode;
    public String city;
}
