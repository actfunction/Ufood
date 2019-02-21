package com.rh.core.base.start.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.rh.core.base.Bean;



/**
 * 通过DriverManager模拟数据源服务。
 * 
 * @author wanglong
 * @version $Id$
 */
public class SimpleDataSource implements DataSource {
	
	/**
	 * 初始化参数
	 */
	private Bean initBean = null;
	
	/**
	 * 构建体方法
	 * @param init 初始化信息
	 */
	public SimpleDataSource(Bean init) {
		initBean = init;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Connection getConnection() throws SQLException {
		try {
			Class.forName(initBean.getStr("class"));
		} catch (ClassNotFoundException e) {
			throw new SQLException(e.getMessage(), e);
		}
        //具体的数据库操作逻辑
		return DriverManager.getConnection(initBean.getStr("url"), initBean.getStr("user"),
				initBean.getStr("password"));
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }
	
}