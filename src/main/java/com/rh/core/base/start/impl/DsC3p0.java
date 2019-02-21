/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.start.impl;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.BaseContext.DS;
import com.rh.core.util.Constant;



/**
 * C3P0数据源管理类，处理数据源的初始化和关闭。
 * 
 * @author wanglong
 * @version $Id$
 */
public class DsC3p0 {
	
	/**
	 * 初始化数据库连接池，支持多数据源。
	 */
	public void start() {
		try {
		    String confFile = BaseContext.appStr(APP.WEBINF) + "/db.properties";
		    Properties prop = BaseContext.getProperties(confFile);
		    String[] names = prop.getProperty("names").split(Constant.SEPARATOR);
		    int i = 0;
		    for (String name : names) {
		        String url = prop.getProperty(name + ".jdbcUrl");
		        String user = prop.getProperty(name + ".user");
		        String password = prop.getProperty(name + ".password");
		        try {
    		        ComboPooledDataSource ds = new ComboPooledDataSource();
    		        ds.setDriverClass(prop.getProperty(name + ".driverClass"));        
    		        ds.setJdbcUrl(url);
    		        ds.setUser(user);            
    		        ds.setPassword(password);
    		        ds.setMinPoolSize(Integer.parseInt(prop.getProperty(name + ".minPoolSize")));
    		        ds.setAcquireIncrement(Integer.parseInt(prop.getProperty(name + ".acquireIncrement")));
    		        ds.setMaxPoolSize(Integer.parseInt(prop.getProperty(name + ".maxPoolSize")));
    		        BaseContext.addDataSource(name, name, url, user, i == 0, ds, null, null);
                    i++;
                    System.out.println("dsName(JNDI):" + name + " Url=" + url + "(" + user + ") is OK!");
    		    } catch (Exception e) {
    		        System.out.println("dsName(JNDI):" + name + " Url=" + url + "(" + user + ") is ERROR! " 
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
	    try {
    	    for (String name : BaseContext.getDSNames()) {
    	        if (!name.isEmpty()) {
    	            DataSources.destroy((DataSource) BaseContext.getDSBean(name).get(DS.DS));
    	        }
    	    }
	    } catch (SQLException e) {
            throw new RuntimeException(e);
	    }
	}
}