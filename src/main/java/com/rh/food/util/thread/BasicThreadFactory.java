package com.rh.food.util.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;

/**
 * 基础线程工厂类：主要功能是自定义线程名<br>
 * 
 */
public class BasicThreadFactory implements ThreadFactory {

    private String threadName;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public BasicThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable r) {
        String mainThreadName = Thread.currentThread().getName();
        Thread thread = new Thread(r, mainThreadName + "-" + threadName + "-" + threadNumber.getAndIncrement());
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LoggerFactory.getLogger(t.getName()).error("BasicThreadFactory-" + e.getMessage(), e);
            }
        });

        return thread;
    }

}
