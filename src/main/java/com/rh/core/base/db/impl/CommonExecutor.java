/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db.impl;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.TableBean;

/**
 * 执行SQL语句的通用实现类
 * 
 * @author wanglong
 * @version $Id$
 */
public class CommonExecutor extends SqlExecutor {
	/**
	 * 单例实例
	 */
	private static CommonExecutor inst;
	
	/**
	 * 私有构建体，禁止new方式实例化
	 */
	private CommonExecutor() {	    
	}

	/**
	 * 获取当前实例
	 * @return	当前实例
	 */
	public static SqlExecutor getExecutor() {
		if (inst == null) {
			inst = new CommonExecutor();
		}
		return inst;
	}
	
    /**
     * 根据表名获取表的定义信息(通用实现)
     * @param tableCode 表名或者视图名
     * @return 定义信息
     */
    public TableBean getDBTable(String tableCode) {
        return null;
    }
    
    @Override
    public List<Bean> getViewList() {
        return null;
    }
}