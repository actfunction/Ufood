/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
 package com.rh.core.base;
 
/**
 * 提示型例外
 * @author wanglong
 * @version $Id$
 */
public class TipException extends RuntimeException {

    /** 类版本标识 */
    private static final long serialVersionUID = 1L;

    /**
     * 构造带指定详细消息的新异常
     * @param msg 详细消息
     */
    public TipException(String msg) {
        super(msg);
    }
}