package com.haolb.client.net;

/**
 * 网络任务的状态
 */
public interface TaskStatus {
    public static final int NONE = 0x520;
    public static final int START = 0x521;
    public static final int RUNNING = 0x522;
    public static final int END = 0x523;
    public static final int ERROR = 0x524;
    public static final int SUCCESS = 0x525;
}