/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */
package com.rh.core.util.scheduler;

/**
 * 
 * @author wanglong
 * 
 */
public class RhSchedulerException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 634855976115085451L;

    /**
     * 构造函数
     * @param message - exception message
     */
    public RhSchedulerException(String message) {
        super(message);
    }

    /**
     * @param root - exception
     */
    public RhSchedulerException(Throwable root) {
        super(root);
    }

    /**
     * @param message - message
     * @param root - exception
     */
    public RhSchedulerException(String message, Throwable root) {
        super(message, root);
    }
}
