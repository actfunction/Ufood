/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db;

/**
 * 生成SQL语句的接口类，供enum Context.DB_TYPE实现command模式。
 * 
 * @author wanglong
 * @version $Id$
 */
public interface DbType {
    /**
     * 获取sqlBuilder
     * @return sqlBuilder
     */
	SqlBuilder getBuilder();
	
    /**
     * 获取sqlExecutor
     * @return sqlExecutor
     */
	SqlExecutor getExecutor();
}
