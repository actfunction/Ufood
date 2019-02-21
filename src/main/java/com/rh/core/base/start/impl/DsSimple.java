package com.rh.core.base.start.impl;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rh.core.base.Bean;
import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.BaseContext.DB_TYPE;
import com.rh.core.base.BaseContext.DS;
import com.rh.core.util.Constant;



/**
 * 简单数据源管理类，通过DirverManager处理数据源的初始化和关闭。
 * 
 * @author wanglong
 * @version $Id$
 */
public class DsSimple {
	
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
		    	Bean init = new Bean();
		        String url = prop.getProperty(name + ".jdbcUrl");
		        String user = prop.getProperty(name + ".user");
    		    String className = prop.getProperty(name + ".driverClass");  
    		    String password = prop.getProperty(name + ".password");
    		    init.set("url", url).set("class", className).set("user", user)
    		    	.set("password", password);
    		    Bean dsBean = new Bean();
                dsBean.set(DS.NAME, name);
                dsBean.set(DS.FULL_NAME, name);
                if (url.indexOf("oracle") >= 0) {
                    dsBean.set(DS.DB_TYPE, DB_TYPE.ORACLE);
                    dsBean.set(DS.SCHEMA, user);  //oracle schema为用户名
                } else if (url.indexOf("sqlserver") >= 0) {
                    dsBean.set(DS.DB_TYPE, DB_TYPE.MSSQL);
                } else if (url.indexOf("mysql") >= 0) {
                    dsBean.set(DS.DB_TYPE, DB_TYPE.MYSQL);
                    Pattern pattern = Pattern.compile(".*/(\\w+)\\??.*");
                    Matcher mt = pattern.matcher(url); //在URL上通过正则表达式查找数据库名
                    if (mt.find()) {
                        dsBean.set(DS.SCHEMA, mt.group(1));  //mysql schema为数据库名
                    }
                } else if (url.indexOf("h2") >= 0) {
                    dsBean.set(DS.DB_TYPE, DB_TYPE.H2);
                    Pattern pattern = Pattern.compile(".*/(\\w+)\\??.*");
                    Matcher mt = pattern.matcher(url); //在URL上通过正则表达式查找数据库名
                    if (mt.find()) {
                        dsBean.set(DS.SCHEMA, mt.group(1));  //mysql schema为数据库名
                    }
                } else if (url.indexOf("db2") >= 0) {
                    dsBean.set(DS.DB_TYPE, DB_TYPE.DB2);
                } else {
                    dsBean.set(DS.DB_TYPE, DB_TYPE.OTHER);
                }
                SimpleDataSource ds = new SimpleDataSource(init);
                dsBean.set(DS.DS, ds);
                dsBean.set(DS.USER_NAME, user);
                dsBean.set(DS.URL, url);
                BaseContext.setDSBean(name, dsBean); //放入应用级变量
                if (i == 0) { //设置第一个为缺省数据源
                    BaseContext.setDSBean("", dsBean); //将缺省数据源放入应用级变量
                }
                i++;
                System.out.println("dsName(JNDI):" + name + " Url=" + url + "(" + user + ") is OK!");
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