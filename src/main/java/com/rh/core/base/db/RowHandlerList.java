/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db;


import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;

/**
 * 列表行处理器
 * @author wanglong
 *
 */
public class RowHandlerList extends ArrayList<Bean> implements IRowHandler {
    /**
     * uid信息
     */
    private static final long serialVersionUID = -1104658171304130534L;

    /**
     * 将行数据加入列表
     * @param columns 列头信息
     * @param data 行数据
     */
    public void handle(List<Bean> columns, Bean data) {
        this.add(data);
    }
    
    /**
     * 整体查询结束后通过回调方法提供列相关信息，无论是否有数据信息都执行此回调。
     * @param count 此次查询的数据数（不是分页查询的总记录数）
     * @param columns 列信息
     */
    public void end(int count, List<Bean> columns) {        
    }
}
