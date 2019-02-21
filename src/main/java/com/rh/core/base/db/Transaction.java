/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.DB_TYPE;
import com.rh.core.base.BaseContext.DS;
import com.rh.core.base.BaseContext.THREAD;

/**
 * 事务处理方法，使用如下：
 * try {
 * 	Transaction.begin();
 * 	....
 *  Transaction.commit();
 * } finally {
 * 	Transaction.end();
 * }
 * 
 * 支持多级事务嵌套。
 * @author wanglong
 * @version $Id$
 */
public class Transaction {
	/** log */
	private static Log log = LogFactory.getLog(Transaction.class);
	/**
	 * 事务状态
	 */
	private enum TRANS {
	    /** 已提交状态 */
	    COMMITED, 
	    /** 数据库连接 */
	    CONN
	}
	
	/**
	 * 启动一个缺省数据连接的事务。
	 */
	public static void begin() {
		begin("");
	}
	
	/**
	 * 启动指定数据源的事务。
	 * @param dsName	数据源名称
	 */
	@SuppressWarnings("unchecked")
	public static void begin(String dsName) {
		LinkedList<Bean> tranList = (LinkedList<Bean>) BaseContext.getThread(THREAD.TRANSLIST);
		if (tranList == null) {
			tranList = new LinkedList<Bean>();
			BaseContext.setThread(THREAD.TRANSLIST, tranList);
		}
		Bean dsBean = BaseContext.getDSBean(dsName);
		Bean tranBean = new Bean();
		tranBean.set(DS.NAME, dsName);
		tranBean.set(DS.DB_TYPE, dsBean.get(DS.DB_TYPE));
		Connection conn;
		try {
			conn = ((DataSource) dsBean.get(DS.DS)).getConnection();
			conn.setAutoCommit(false);
			tranBean.set(TRANS.CONN, conn);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		tranList.addFirst(tranBean); 
	}
	
	/**
	 * 提交当前事务。
	 */
	@SuppressWarnings("unchecked")
	public static void commit() {
	    Object list = BaseContext.getThread(THREAD.TRANSLIST);
        if (list == null) {
            return;
        }
	    LinkedList<Bean> linkedList = (LinkedList<Bean>) list;
		Bean tranBean = linkedList.getFirst();
		try {
			Connection conn = ((Connection) tranBean.get(TRANS.CONN));
			conn.commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		tranBean.set(TRANS.COMMITED, true);
	}
	   
    /**
     * 回滚当前事务。
     */
    @SuppressWarnings("unchecked")
    public static void rollback() {
        Object list = BaseContext.getThread(THREAD.TRANSLIST);
        if (list == null) {
            return;
        }
        LinkedList<Bean> linkedList = (LinkedList<Bean>) list;      
        Bean tranBean = linkedList.getFirst();
        try {
            Connection conn = ((Connection) tranBean.get(TRANS.CONN));
            conn.rollback();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        tranBean.set(TRANS.COMMITED, true);
    }
    
	/**
	 * 关闭当前事务，如果已经提交，直接关闭事务相关信息，如果没有提交则执行回滚操作。
	 */
	@SuppressWarnings("unchecked")
	public static void end() {
		try {
	        Object list = BaseContext.getThread(THREAD.TRANSLIST);
	        if (list == null) {
	            log.error("No transaction begin,  end error");
	            return;
	        }
	        LinkedList<Bean> tranList = (LinkedList<Bean>) list;
			Bean tranBean = tranList.getFirst();
			Connection conn = ((Connection) tranBean.get(TRANS.CONN));
			if (!tranBean.getBoolean(TRANS.COMMITED)) {
				conn.rollback();
			}
			conn.setAutoCommit(true);
			conn.close();
			tranList.removeFirst(); //先进后出
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 得到当前事务的数据库连接
	 * @return	数据库连接
	 */
	@SuppressWarnings("unchecked")
	public static Connection getConn() {
		Connection conn = null;
		LinkedList<Bean> tranList = ((LinkedList<Bean>) BaseContext.getThread(THREAD.TRANSLIST));
		if (tranList != null && !tranList.isEmpty()) {
			Bean tranBean = tranList.getFirst();
			conn = ((Connection) tranBean.get(TRANS.CONN));
		}
		return conn;
	}
	
	/**
	 * 得到当前事务的数据库类型，如果没有启动事务则返回缺省数据源的数据库类型
	 * @return	当前数据库类型
	 */
	@SuppressWarnings("unchecked")
	public static DbType getDBType() {
		DB_TYPE type = null;
		LinkedList<Bean> tranList = ((LinkedList<Bean>) BaseContext.getThread(THREAD.TRANSLIST));
        if (tranList != null && !tranList.isEmpty()) {
			Bean tranBean = tranList.getFirst();
			if (tranBean != null) {
				type = (DB_TYPE) tranBean.get(DS.DB_TYPE);
			} else {
				type = (DB_TYPE) BaseContext.getDSBean().get(DS.DB_TYPE);
			}
		} else {
			type = (DB_TYPE) BaseContext.getDSBean().get(DS.DB_TYPE);
		}
		return type;
	}
	
	/**
     * 得到当前事务的数据源名称
     * @return  当前事务数据源名称
     */
    @SuppressWarnings("unchecked")
    public static String getDsName() {
        String dsName = "";
        LinkedList<Bean> tranList = ((LinkedList<Bean>) BaseContext.getThread(THREAD.TRANSLIST));
        if (tranList != null && !tranList.isEmpty()) {
            Bean tranBean = tranList.getFirst();
            if (tranBean != null) {
                dsName = tranBean.getStr(DS.NAME);
            }
        }
        return dsName;
    }
    
	/**
	 * 得到当前事务对应数据库类型的sqlBuilder
	 * @return SqlBuilder
	 */
	public static SqlBuilder getBuilder()  {
		return  getDBType().getBuilder();
	}
	
	/**
	 * 得到当前事务对应数据库类型的SqlExecutor
	 * @return SqlExecutor
	 */
	public static SqlExecutor getExecutor()  {
		return  getDBType().getExecutor();
	}
}
