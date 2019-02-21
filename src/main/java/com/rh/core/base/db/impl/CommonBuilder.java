/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db.impl;

import com.rh.core.base.db.SqlBuilder;
import com.rh.core.base.db.TableBean;

/**
 * 生成SQL语句的通用实现类
 * 
 * @author wanglong
 * @version $Id$
 */
public class CommonBuilder extends SqlBuilder {
	/**
	 * 单例实例
	 */
	private static CommonBuilder inst;
	
	/**
	 * 私有构建体，禁止new方式实例化
	 */
	private CommonBuilder() {
	}

	/**
	 * 获取当前实例
	 * @return	当前实例
	 */
	public static SqlBuilder getBuilder() {
		if (inst == null) {
			inst = new CommonBuilder();
		}
		return inst;
	}
	
    /**
     * 获取指定表定义对应的DDL SQL语句
     * @param tableBean 表定义信息
     * @return 表定义SQL
     */
    public String getDBTableDDL(TableBean tableBean) {
        return "";
    }
    
    @Override
    public String bitand(String item1, String item2) {
        StringBuilder sb = new StringBuilder(item1);
        sb.append(" & ").append(item2);
        return sb.toString();
    }
    
    @Override
    public String concat(String item1, String item2) {
        StringBuilder sb = new StringBuilder("CONCAT(");
        sb.append(item1).append(", ").append(item2).append(")");
        return sb.toString();
    }
    
    @Override
    public String nvl(String item1, String item2) {
        StringBuilder sb = new StringBuilder("ISNULL(");
        sb.append(item1).append(", ").append(item2).append(")");
        return sb.toString();
    }
    
    @Override
    public String substr(String item, int start, int len) {
        StringBuilder sb = new StringBuilder("SUBSTRING(");
        sb.append(item).append(", ").append(start).append(", ").append(len).append(")");
        return sb.toString();
    }
}