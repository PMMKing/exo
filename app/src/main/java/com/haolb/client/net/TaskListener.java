package com.haolb.client.net;

/**
 * 
 * @author zexu
 *
 */
public interface TaskListener {
    public void onTaskComplete(NetworkTask task);

    public void onTaskCancel(NetworkTask task);

}
