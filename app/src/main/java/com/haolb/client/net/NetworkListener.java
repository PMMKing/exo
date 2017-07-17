package com.haolb.client.net;

public interface NetworkListener {

	public void onNetStart(NetworkParam param);

	public void onNetEnd(NetworkParam param);

	public void onNetError(NetworkParam param, int errCode);

	public boolean onMsgSearchComplete(NetworkParam param);

	public void onNetCancel(NetworkParam param);

}
