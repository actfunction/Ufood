/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.plug.search;

import com.rh.core.base.Bean;

/**
 * 索引监听接口，实现此接口即可进行自定义索引处理
 * 
 * @author wanglong
 * @version $Id$
 */
public interface IndexListener {
    /**
     * 单条数据索引处理，在索引提交服务器前执行
     * @param iaMsg 索引信息
     * @param searchDef 搜索定义信息
     * @param data 数据信息
     */
    void index(ARhIndex iaMsg, Bean searchDef, Bean data);
}
