/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db.impl;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.TableBean;

/**
 * 执行SQL语句的Mssql实现类
 * 
 * @author wanglong
 * @version $Id$
 */
public class MssqlExecutor extends SqlExecutor {
	/**
	 * 单例实例
	 */
	private static MssqlExecutor inst;
	
	/**
	 * 私有构建体，禁止new方式实例化
	 */
	public MssqlExecutor() {
	}

	/**
	 * 获取当前实例
	 * @return	当前实例
	 */
	public static SqlExecutor getExecutor() {
		if (inst == null) {
			inst = new MssqlExecutor();
		}
		return inst;
	}
	   
    @Override
    public TableBean getDBTable(String tableCode) {
        return null;
    }
    
    @Override
    public List<Bean> getViewList() {
        return null;
    }
}