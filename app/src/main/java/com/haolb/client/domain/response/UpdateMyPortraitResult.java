package com.haolb.client.domain.response;

public class UpdateMyPortraitResult extends BaseResult {
	private static final long serialVersionUID = 1L;
	public UpdateMyPortraitData  data;
	
	public static class UpdateMyPortraitData implements BaseData {
		private static final long serialVersionUID = 1L;
		public String portrait="";
	}
}
