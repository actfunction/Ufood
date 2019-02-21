/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.start.impl;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import com.oscar.jdbcx.Jdbc3SimpleDataSource;
import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.APP;



/**
 * C3P0数据源管理类，处理数据源的初始化和关闭。
 * 
 * @author wanglong
 * @version $Id$
 */
public class DsOscar {
	
	/**
	 * 初始化数据库连接池，支持多数据源。
	 */
	public void start() {
		try {
    		//获取数据源，支持多数据源
		    String jndiPrefix = BaseContext.app(BaseContext.SYS_PARAM_JNDI_PREFIX, "");
		    javax.naming.Context env;
		    if (jndiPrefix.length() > 0) {
		        env = (javax.naming.Context) new InitialContext().lookup(jndiPrefix);
		        jndiPrefix += "/";
		    } else {
		        env = (javax.naming.Context) new InitialContext();
		    }
    		String dsPrefix = BaseContext.app(BaseContext.SYS_PARAM_DATASOURCE_PREFIX, "jdbc");
    		NamingEnumeration<NameClassPair> namEnumList = env.list(dsPrefix);
    		String prefix = dsPrefix + "/";
    		int i = 0;
    		while (namEnumList.hasMore()) {
    		    NameClassPair bnd = namEnumList.next();
    		    String jndiName = prefix + bnd.getName();
			    String confFile = BaseContext.appStr(APP.WEBINF) + "/server/db.properties";
			    Properties prop = BaseContext.getProperties(confFile);
		        String url = prop.getProperty("jdbcUrl");
		        String userName = prop.getProperty("user");
		        String userPass = prop.getProperty("password");
		        String fullName = jndiPrefix + jndiName;
		        try {
		        	Jdbc3SimpleDataSource dataSource = new Jdbc3SimpleDataSource();
					// 设置数据源属性
	//				dataSource.setServerName("localhost");
					dataSource.setUrl(url);
	//				dataSource.setPortNumber(2003);
	//				dataSource.setDatabaseName("OSRDB");
					dataSource.setUser(userName);
					dataSource.setPassword(userPass);
	    		    //如果设置了缺省数据源，则直接使用，或者如果没有缺省数据源，设置第一个为缺省数据源
	    		    boolean isDefaultDs = jndiName.equalsIgnoreCase(prefix + "default") || i == 0;
	    		    BaseContext.addOscarDataSource(fullName, jndiName, url, userName, isDefaultDs, dataSource, null, null);
	                i++;
	                System.out.println("dsName(JNDI):" + jndiName + " Url=" + url + "(" + userName + ") is OK!");
			    } catch (Exception e) {
			        System.out.println("dsName(JNDI):" + jndiName + " Url=" + url + "(" + userName + ") is ERROR! " 
			                + e.getMessage());
			    }
    		}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 关闭连接池
	 */
	public void stop() {
		
	}
}