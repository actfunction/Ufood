/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.start;

import com.rh.core.base.Context;
import com.rh.core.util.Lang;



/**
 * 数据源管理类，处理数据源的初始化和关闭。
 * 
 * @author wanglong
 * @version $Id$
 */
public class DsMgr {
	
	/**
	 * 初始化数据库连接池，支持多数据源。
	 */
	public void start() {
		Lang.doMethod(getClsName(), "start");
        System.out.println("Datasource is OK!....................");
	}
	
	/**
	 * 关闭连接池
	 */
	public void stop() {
	    Lang.doMethod(getClsName(), "stop");
	}
	
	/**
	 * 获取实现类名称，缺省为com.rh.core.base.start.impl.DsJndi
	 * @return 实现类名称
	 */
	private String getClsName() {
	    String clsName = Context.getInitConfig("rh.datasource", "com.rh.core.base.start.impl.DsJndi");
	    System.out.println("rh.datasource:" + clsName);
	    return clsName;
	}
}