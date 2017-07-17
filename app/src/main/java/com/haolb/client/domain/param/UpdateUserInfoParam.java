package com.haolb.client.domain.param;

/**
 * type	是	用户属性	String	nickname:表示更新的是密名
					        sex:性别 0:男 1 女
					        address:地区
					        canSearchByPhone:
					是否可以通过手机后搜索到我 0:可以 1不可以
intValue	否	属性对应的值	int	
stringValue	否	属性对应的值	String	
 * @author gzxooo
 *
 */
public class UpdateUserInfoParam extends BaseParam {
	private static final long serialVersionUID = 1L;
//	public String type;
//	public Integer intValue;
//	public String stringValue;
    public String nickname;
}
