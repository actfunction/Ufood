/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.start;

import java.util.Properties;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import com.rh.core.base.BaseContext;



/**
 * 日志处理管理类
 * 
 * @author wanglong
 * @version $Id$
 */
public class LogMgr {

	/** 配置信息 */
	private static Properties prop = null;
	
	/**
	 * 初始化log4j日志资源
	 */
	public void start() {

        String path = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
//        PropertyConfigurator.configure(new StringBuffer(path).append("properties/log4j.properties").toString());
		prop = BaseContext.getProperties(path + "properties/"
		        + BaseContext.app(BaseContext.SYS_PARAM_LOG, "log4j.properties"));
        PropertyConfigurator.configure(prop);
        System.out.println("Log is OK!...........................");
	}
	
	/**
	 * 动态变更log的级别，用于生产环境临时调试
	 * @param toLevel	变更到的级别
	 */
	public static void changeLevel(String toLevel) {
		String old = prop.getProperty("log4j.rootCategory");
		toLevel = toLevel + old.substring(old.indexOf(","));
		prop.setProperty("log4j.rootCategory", toLevel);
		PropertyConfigurator.configure(prop);
		System.out.println("{log4j.rootCategory} from:" + old + " to:" + toLevel);
	}
	
	/**
	 * 动态装载配置文件
	 * @param fileName	配置文件路径
	 */
	public void loadAndWatch(String fileName) {
		PropertyConfigurator.configureAndWatch(fileName, 6000);
	}
	
	/**
	 * 释放日志资源
	 */
	public void stop() {
        LogManager.shutdown();
        LogFactory.releaseAll();
	}
}