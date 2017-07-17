package com.haolb.client.domain.param;

/**
 * portrait	是	用户头像内容	String	将图片的内容转成base64字符串
 * @author gzxooo
 *
 */
public class UpdateMyPortraitParam extends BaseParam {
	private static final long serialVersionUID = 1L;

	public long byteLength;
	public String ext;
}
