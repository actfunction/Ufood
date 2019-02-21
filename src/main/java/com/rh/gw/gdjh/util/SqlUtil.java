/**
 * 审计管理分系统行政办公管理子系统
 * @file:DocumentExchangeServ.java
 * @author: kfzx-zhanglm
 * @date: 2018年12月7日 上午9:35:26
 * @version: V1.0
 * @description: TODO
 */
package com.rh.gw.gdjh.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh.core.base.db.Transaction;

/**
  * 审计管理分系统行政办公管理子系统
 * @author: kfzx-zhanglm
 * @date: 2018年12月7日 上午9:35:26
 * @version: V1.0
 * @description: TODO
 */
public class SqlUtil{
	private static final Logger LOGGER = LoggerFactory.getLogger(SqlUtil.class);
	/**
	 * 
	 * @title: execute
	 * @descriptin: TODO
	 * @param @param map  key-value  key(数据库字段名) value 值
	 * @param @param tableName 数据库表名
	 * @return void
	 * @throws
	 */
	public static int execute(Map<String,Object> map,String tableName) {
		int code = 0;
		try {
			StringBuffer sqlBuffer = new StringBuffer();
			sqlBuffer.append("insert into "+tableName+" (");
			StringBuffer valueBuffer = new StringBuffer();
			valueBuffer.append(" values (");
			for (Map.Entry<String,Object> entry : map.entrySet()) {
				sqlBuffer.append(entry.getKey()+",");
				valueBuffer.append("'"+entry.getValue()+"',");
			}
			sqlBuffer.deleteCharAt(sqlBuffer.length()-1);
			valueBuffer.deleteCharAt(valueBuffer.length()-1);
			sqlBuffer.append(") ");
			valueBuffer.append(")");
			String sql = sqlBuffer.append(valueBuffer).toString();
			System.out.println(sql);
			code = Transaction.getExecutor().execute(sql);
		} catch (Exception e) {
			
			code = -1;
			LOGGER.error("添加数据库错误，map:{},tableName:{}",map,tableName,e);
		}
		return code;
	}
	
	/**
	 * 
	 * @title: update
	 * @descriptin: TODO
	 * @param @param valueMap key-value  key(数据库字段名) value 值
	 * @param @param whereMap key-value  key(数据库字段名) value 值
	 * @param @param tableName 数据库表名
	 * @param @return
	 * @return int
	 * @throws
	 */
	public static int update(Map<String,Object> valueMap ,Map<String,Object> whereMap,String tableName) {
		int code = 0;
		try {
			StringBuffer sqlBuffer = new StringBuffer();
			sqlBuffer.append("update "+tableName+" set ");
			StringBuffer whereBuffer = new StringBuffer();
			whereBuffer.append(" where 1=1 ");
			for (Map.Entry<String,Object> entry : valueMap.entrySet()) {
				sqlBuffer.append(entry.getKey()+"=");
				sqlBuffer.append("'"+entry.getValue()+"',");
			}
			sqlBuffer.deleteCharAt(sqlBuffer.length()-1);
			for (Map.Entry<String,Object> entry : whereMap.entrySet()) {
				whereBuffer.append(" and "+entry.getKey()+"=");
				whereBuffer.append("'"+entry.getValue()+"' ");
			}
			String sql = sqlBuffer.append(whereBuffer).toString();
			System.out.println(sql);
			code = Transaction.getExecutor().execute(sql);
		} catch (Exception e) {
			LOGGER.error("修改数据库错误，valueMap:{},whereMap:{},tableName:{}",valueMap,whereMap,tableName,e);
			code = -1;
			e.printStackTrace();
		}
		return code;
	}
	
}
