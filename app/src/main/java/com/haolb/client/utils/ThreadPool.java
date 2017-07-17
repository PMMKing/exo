package com.haolb.client.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池封装类
 * 
 * @author gzx
 */
public class ThreadPool {

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private static ThreadPool instance = new ThreadPool();

    private ThreadPool() {
    }

    public static ThreadPool getInstance() {
        return instance;
    }

    public void executeTask(Runnable task) {
        executorService.execute(task);
        // new Thread(task).start() ;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}