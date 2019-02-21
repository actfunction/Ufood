/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.start;

import com.rh.core.comm.CacheMgr;
import com.rh.resource.Resource;

/**
 * 缓存装载类。
 * 
 * @author wanglong
 * @version $Id$
 */
public class CacheLoader {
	
	/**
	 * 初始化缓存服务
	 */
	public void start() {
        CacheMgr.getInstance();
        Resource.getServMap();
        Resource.getDictMap();
        System.out.println("Cache is OK!.........................");
	}
	
	/**
	 * 关闭时时清除缓存
	 */
	public void stop() {
	    CacheMgr.shutdown();
	}
}