/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db;


import java.util.List;

import com.rh.core.base.Bean;

/**
 * 行处理器通用实现类
 * @author wanglong
 */
public abstract class RowHandler implements IRowHandler {
    /**
     * 处理行数据
     * @param columns 列数据
     * @param data 行数据
     */
    public abstract void handle(List<Bean> columns, Bean data);
    /**
     * 整体查询结束后通过回调方法提供列相关信息，无论是否有数据信息都执行此回调。
     * @param count 此次查询的数据数（不是分页查询的总记录数）
     * @param columns 列信息
     */
    public void end(int count, List<Bean> columns) {        
    }
}
