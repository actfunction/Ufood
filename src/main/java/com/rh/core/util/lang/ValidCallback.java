/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.util.lang;

import com.rh.core.base.Bean;


/**
 * 通用的有效性检查回调处理。
 * 
 * @author wanglong
 * @version $Id$
 */
public interface ValidCallback {
    /**
     * 有效性检查的回调方法
     * @param data  当前行数据
     * @return 是否有效
     */
    boolean valid(Bean data);
}
