package com.rh.core.base.db.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.DS;
import com.rh.core.base.db.IRowHandler;
import com.rh.core.base.db.QueryCallback;
import com.rh.core.base.db.RowHandlerList;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.TableBean;
import com.rh.core.base.db.Transaction;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;

/**
 * 执行SQL语句的Mysql实现类
 * 
 * @author wanglong
 * @version $Id$
 */
public class MysqlExecutor extends SqlExecutor {
	/** log */
	private static Log log = LogFactory.getLog(MysqlExecutor.class);
	/**
	 * 单例实例
	 */
	private static MysqlExecutor inst;

	/**
	 * 私有构建体，禁止new方式实例化
	 */
	public MysqlExecutor() {
	}

	/**
	 * 获取当前实例
	 * 
	 * @return 当前实例
	 */
	public static SqlExecutor getExecutor() {
		if (inst == null) {
			inst = new MysqlExecutor();
		}
		return inst;
	}

	/**
	 * 查询sql
	 * 
	 * @param conn
	 *            数据库连接
	 * @param sql
	 *            sql语句
	 * @param values
	 *            prepared sql 参数信息，如果null则执行普通sql
	 * @param qc
	 *            回调方法
	 * @return 查询列表数据
	 */
	public List<Bean> query(Connection conn, String sql, List<Object> values, QueryCallback qc) {
		RowHandlerList rh = new RowHandlerList();
		queryCall(conn, sql, values, rh, qc);
		return rh;
	}

	/**
	 * 查询sql
	 * 
	 * @param conn
	 *            数据库连接
	 * @param sql
	 *            sql语句
	 * @param values
	 *            prepared sql 参数信息，如果null则执行普通sql
	 * @param rh
	 *            行处理器
	 * @param qc
	 *            回调方法
	 */
	public void queryCall(Connection conn, String sql, List<Object> values, IRowHandler rh, QueryCallback qc) {
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean ownConn = false;
		try {
			if (conn == null) {
				conn = BaseContext.getConn();
				ownConn = true;
			}
			long startTime = System.currentTimeMillis(); // 起始时间
			if (values == null || values.size() == 0) { // 正常sql
				// 创建Statement对象与数据库进行数据查询操作
				stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				stmt.setMaxRows(MAX_SIZE);
				sql = replaceSql(sql);
				rs = stmt.executeQuery(sql);
			} else { // prepared sql
				pstmt = conn.prepareStatement(sql);
				pstmt.setMaxRows(MAX_SIZE);
				for (int i = 0; i < values.size(); i++) {
					pstmt.setObject(i + 1, values.get(i));
				}
				rs = pstmt.executeQuery();
			}
			long endTime = System.currentTimeMillis(); // 结束时间
			// 对结果集数据进行包装处理
			int count = 0;
			int columnCount = 0;
			ArrayList<Bean> columnList = new ArrayList<Bean>();
			ResultSetMetaData rsmd = rs.getMetaData(); // 获取查询字段列表
			columnCount = rsmd.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				Bean columnBean = new Bean();
				columnBean.set("NAME", rsmd.getColumnName(i));
				columnBean.set("TYPE", rsmd.getColumnType(i));
				columnBean.set("SIZE", rsmd.getPrecision(i));
				columnBean.set("SCALE", rsmd.getScale(i));
				columnList.add(columnBean);
			}
			while (rs.next()) {
				Bean bean = new Bean();
				for (int i = 0; i < columnCount; i++) {
					if (i == 0) { // 设置通用主键
						bean.setId(rs.getString(1));
					}
					Bean columnBean = columnList.get(i);
					Object value;
					switch (columnBean.getInt("TYPE")) {
					case Types.VARCHAR:
						value = rs.getObject(i + 1);
						if (value == null) {
							bean.set(columnBean.get("NAME"), "");
						} else {
							bean.set(columnBean.get("NAME"), value);
						}
						break;
					case Types.NUMERIC:
						value = rs.getObject(i + 1);
						if (value == null) {
							bean.set(columnBean.get("NAME"), 0);
						} else {
							if (((BigDecimal) value).scale() == 0) {
								if (columnBean.getInt("SIZE") <= 9) { // 根据长度决定采用哪种类型
									bean.set(columnBean.get("NAME"), ((BigDecimal) value).intValue());
								} else {
									bean.set(columnBean.get("NAME"), ((BigDecimal) value).longValue());
								}
							} else {
								bean.set(columnBean.get("NAME"), ((BigDecimal) value).doubleValue());
							}
						}
						break;
					case Types.BLOB:
						bean.set(columnBean.get("NAME"), rs.getBlob(i + 1));
						break;
					case Types.LONGVARBINARY:
						bean.set(columnBean.get("NAME"), rs.getLong(i + 1));
						break;
					case Types.LONGVARCHAR:
						bean.set(columnBean.get("NAME"), rs.getObject(i + 1));
						break;
					case Types.TIMESTAMP:
						bean.set(columnBean.get("NAME"), DateUtils.getByTimestamp(rs.getTimestamp(i + 1)));
						break;
					default:
						// 处理一般型字段
						bean.set(columnBean.get("NAME"), rs.getObject(i + 1));
					}
				}
				if (qc != null) { // 执行回调方法
					bean.set(Constant.PARAM_ROWNUM, count); // 设置当前行，便于回调方法使用
					qc.call(columnList, bean);
				}
				if (rh != null) {
					bean.set(Constant.PARAM_ROWNUM, count); // 设置当前行，便于回调方法使用
					rh.handle(columnList, bean);
				}
				count++;
			}
			if (qc != null) { // 执行回调方法提供列信息，无论是否有数据信息都执行此回调
				qc.end(count, columnList);
			}
			if (rh != null) { // 执行回调方法提供列信息，无论是否有数据信息都执行此回调
				rh.end(count, columnList);
			}
			long sqlTime = endTime - startTime;
			if (isLogSql(sqlTime, count)) { // 记录sql、执行时间及数据量
				sql = getLogSql(sql, values);
				StringBuilder sbLog = new StringBuilder(sql).append(" [count:").append(count);
				sbLog.append("] ").append((endTime - startTime)).append("ms").append(" ").append(conn.hashCode());
				log.info(sbLog);
			}
		} catch (SQLException e) {
			log.error(getLogSql(sql, values) + " " + e.getMessage());
			throw new RuntimeException(ERROR_MSG, e);
		} finally {
			// 关闭结果集、数据交互及数据连接
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				} else if (pstmt != null) {
					pstmt.close();
				}
				if (ownConn) {
					conn.close();
				}
			} catch (SQLException e) {
				log.error(sql + " " + e.getMessage());
				throw new RuntimeException(ERROR_MSG, e);
			}
		}
	}

	/**
	 * 查询sql执行，采用通用游标方式支持分页
	 * 
	 * @param conn
	 *            数据库连接，如果参数conn为null时自己获取缺省数据源的conn
	 * @param sql
	 *            sql语句
	 * @param offset
	 *            记录所在位置，从1开始
	 * @param count
	 *            获取记录数，大于0表示分页处理
	 * @param values
	 *            prepare sql的参数信息
	 * @param rh
	 *            行处理器，支持大数据量自定义处理
	 * @param qc
	 *            回调方法
	 */
	public void queryCall(Connection conn, String sql, int offset, int count, List<Object> values, IRowHandler rh,
			QueryCallback qc) {
		if (count > 0) { // 分页处理
			StringBuilder paging = new StringBuilder();
			offset = offset - 1; // mysql的位移从0开始
			if (values != null) { // psql
				paging.append(sql).append(" limit ?,?");
				values.add(offset);
				values.add(count);
			} else {
				paging.append(sql).append(" limit ").append(offset).append(",").append(count);
			}
			sql = paging.toString();
		}
		queryCall(conn, sql, values, rh, qc);
	}

	/**
	 * 查询sql执行，采用rownum方式支持分页
	 * 
	 * @param conn
	 *            数据库连接
	 * @param sql
	 *            sql语句
	 * @param offset
	 *            记录所在位置
	 * @param count
	 *            获取记录数，大于0表示分页处理
	 * @param values
	 *            prepared sql 参数信息，如果null则执行普通sql
	 * @param qc
	 *            回调方法
	 * @return 查询列表数据
	 */
	public List<Bean> query(Connection conn, String sql, int offset, int count, List<Object> values, QueryCallback qc) {
		if (count > 0) { // 分页处理
			StringBuilder paging = new StringBuilder();
			if (offset > 0) {
				offset = offset - 1; // mysql的位移从0开始
			} else if (offset < 0) {
				offset = 0;
			}
			if (values != null) { // psql
				paging.append(sql).append(" limit ?,?");
				values.add(offset);
				values.add(count);
			} else {
				paging.append(sql).append(" limit ").append(offset).append(",").append(count);
			}
			sql = paging.toString();
		}
		return query(conn, sql, values, qc);
	}

	/**
	 * 根据表名获取表的定义信息(待各数据库各自实现)
	 * 
	 * @param tableCode
	 *            表名或者视图名
	 * @return 定义信息
	 */
	public TableBean getDBTable(String tableCode) {
		TableBean tableBean = null;
		Connection conn = null;
		DatabaseMetaData dbmd = null; // 数据库metadata
		ResultSet trs = null; // 表结果集
		ResultSet irs = null; // 字段结果集
		boolean ownConn = false;
		try {
			conn = Transaction.getConn();
			if (conn == null) {
				conn = BaseContext.getConn(); // 获取缺省数据源的连接
				ownConn = true;
			}
			dbmd = conn.getMetaData();
			String catelog = BaseContext.getDSBean(Transaction.getDsName()).getStr(DS.SCHEMA);
			trs = dbmd.getTables(catelog, null, tableCode, null);
			tableBean = null;
			if (trs.next()) {
				tableBean = new TableBean();
				tableBean.set("TABLE_CODE", trs.getString("TABLE_NAME").toUpperCase());
				tableBean.set("TABLE_TYPE", trs.getString("TABLE_TYPE"));
				String cmtSql = "select TABLE_COMMENT COMMENTS from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='"
						+ catelog + "' and TABLE_NAME='" + tableCode + "'";
				Bean cmtBean = queryOne(conn, cmtSql);
				if (cmtBean != null) {
					String memo = cmtBean.getStr("COMMENTS");
					if (memo.length() > 0) { // 有字段说明信息
						tableBean.set("SERV_MEMO", memo);
						memo = memo.replaceFirst("，", ",").replaceFirst(" ", ",").replace("：", ",");
						if (memo.indexOf(",") > 0) { // 截取说明前部信息作为表中文名称
							tableBean.set("SERV_NAME", memo.substring(0, memo.indexOf(",")));
						} else {
							tableBean.set("SERV_NAME", memo);
						}
					}
				}
				if (tableBean.isView()) { // 特殊处理视图的定义
					cmtSql = "select VIEW_DEFINITION TEXT from INFORMATION_SCHEMA.VIEWS where TABLE_SCHEMA='" + catelog
							+ "' and TABLE_NAME='" + tableCode + "'";
					tableBean.set("VIEW_QUERY_SQL", queryOne(cmtSql).getStr("TEXT"));
				}
				irs = dbmd.getPrimaryKeys(catelog, null, tableCode);
				StringBuilder sbKeys = new StringBuilder();
				while (irs.next()) {
					sbKeys.append(irs.getString("COLUMN_NAME")).append(Constant.SEPARATOR); // 设置唯一主键
				}
				irs.close();
				if (sbKeys.length() > 0) {
					sbKeys.setLength(sbKeys.length() - 1);
					tableBean.set("SERV_KEYS", sbKeys.toString()); // 设置唯一主键
				}
				// get item list
				List<Bean> itemList = new ArrayList<Bean>();
				irs = dbmd.getColumns(catelog, null, tableCode, "%");
				Bean fieldBean;
				int order = 0;
				cmtSql = "select COLUMN_NAME, COLUMN_COMMENT COMMENTS from INFORMATION_SCHEMA.COLUMNS "
						+ "where TABLE_SCHEMA='" + catelog + "' and TABLE_NAME='" + tableCode + "'";
				HashMap<Object, Bean> cmtHash = BeanUtils.listToHash((List<Bean>) query(conn, cmtSql));
				while (irs.next()) { // 获取字段定义
					fieldBean = new Bean(irs.getString("COLUMN_NAME")); // 以字段名做虚拟主键用来判断是否重复
					fieldBean.set("ITEM_ID", Lang.getUUID()); // 设置UUID，为后面插入做准备
					fieldBean.set("ITEM_CODE", irs.getString("COLUMN_NAME"));
					String type = irs.getString("TYPE_NAME");
					fieldBean.set("$ITEM_FIELD_TYPE_SRC", type);
					String length = irs.getString("COLUMN_SIZE");
					String def = "";
					int isHide = 2;
					if ((order == 0) && (tableBean.isEmpty("SERV_KEYS"))) { // 没有主键默认取第一个作为主键
						tableBean.set("SERV_KEYS", irs.getString("COLUMN_NAME"));
					}
					if (type.equals("NUMBER") || type.equals("NUMBERIC") || type.equals("DECIMAL")) { // 数字类型
						fieldBean.set("ITEM_FIELD_TYPE", Constant.ITEM_FIELD_TYPE_NUM);
						if (irs.getInt("DECIMAL_DIGITS") > 0) { // 有小数位
							length = length + Constant.SEPARATOR + irs.getString("DECIMAL_DIGITS");
						}
						def = "0"; // 设置缺省值
					} else if (type.equals("LONG") || type.equals("CLOG")) { // 大文本或者CLOB
						fieldBean.set("ITEM_FIELD_TYPE", Constant.ITEM_FIELD_TYPE_BIGTEXT);
						length = "99999999";
					} else if (type.equals("TIMESTAMP")) {
						fieldBean.set("ITEM_FIELD_TYPE", Constant.ITEM_FIELD_TYPE_TIME);
						length = "26";
					} else {
						fieldBean.set("ITEM_FIELD_TYPE", Constant.ITEM_FIELD_TYPE_STR);
					}
					if (fieldBean.getStr("ITEM_CODE").equals("S_FLAG")) {
						def = Constant.YES;
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_PUBLIC")) {
						def = Constant.NO;
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_MTIME")) {
						def = "@DATETIME@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_ATIME")) {
						def = "@DATETIME@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_USER")) {
						def = "@USER_CODE@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("USER_NAME")) {
						def = "@USER_NAME@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_DEPT")) {
						def = "@DEPT_CODE@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("DEPT_NAME")) {
						def = "@DEPT_NAME@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_TDEPT")) {
						def = "@TDEPT_CODE@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("TDEPT_NAME")) {
						def = "@TDEPT_NAME@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_ODEPT")) {
						def = "@ODEPT_CODE@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("ODEPT_NAME")) {
						def = "@ODEPT_NAME@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("ODEPT_LEVEL")) {
						def = "@ODEPT_LEVEL@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_YEAR")) {
						def = "@DATE_YEAR@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_YEARMONTH")) {
						def = "@DATE_YEARMONTH@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_CMPY")) {
						def = "@CMPY_CODE@";
						isHide = 1;
						tableBean.set("SERV_CMPY", "S_CMPY");
					} else if (fieldBean.getStr("ITEM_CODE").indexOf("S_WF") >= 0
							|| fieldBean.getStr("ITEM_CODE").equals("SERV_ID")) {
						isHide = 1;
					} else if (tableBean.getStr("SERV_KEYS").indexOf(fieldBean.getStr("ITEM_CODE")) >= 0) {
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("YYYYMMDD")) {
						def = "@DATE@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("APPLY_CODE")) {
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_UNAME")) {
						def = "@USER_NAME@";
						isHide = 1;
					} else if (fieldBean.getStr("ITEM_CODE").equals("S_CODE_PATH")) {
						def = "@ODEPT_CODE_PATH@";
						isHide = 1;
					}
					fieldBean.set("ITEM_FIELD_LENGTH", length);
					fieldBean.set("ITEM_INPUT_DEFAULT", def);
					fieldBean.set("ITEM_HIDDEN", isHide);
					if (cmtHash.containsKey(fieldBean.getStr("ITEM_CODE"))) {
						String memo = cmtHash.get(fieldBean.getStr("ITEM_CODE")).getStr("COMMENTS");
						if ((memo != null) && memo.length() > 0) { // 有字段说明信息
							fieldBean.set("ITEM_MEMO", memo);
							memo = memo.replaceFirst("，", ",").replaceFirst(" ", ",").replace("：", ",")
									.replace(":", ",").replace("，", ",");
							if (memo.indexOf(",") > 0) { // 截取说明前部信息作为字段名称
								fieldBean.set("ITEM_NAME", memo.substring(0, memo.indexOf(",")));
							} else {
								fieldBean.set("ITEM_NAME", memo);
							}
						} else {
							fieldBean.set("ITEM_NAME", fieldBean.get("ITEM_CODE"));
							fieldBean.set("ITEM_MEMO", "");
						}
					} else {
						fieldBean.set("ITEM_NAME", fieldBean.get("ITEM_CODE"));
						fieldBean.set("ITEM_MEMO", "");
					}
					fieldBean.set("ITEM_NOTNULL", irs.getString("NULLABLE").equals("0") ? "1" : "2"); // 非空
					fieldBean.set("ITEM_ORDER", order);
					fieldBean.set("ITEM_TYPE", Constant.ITEM_TYPE_TABLE); // 表字段
					if (tableBean.isEmpty("SERV_SQL_ORDER")
							&& fieldBean.getStr("ITEM_CODE").equalsIgnoreCase("S_MTIME")) {
						tableBean.set("SERV_SQL_ORDER", " S_MTIME desc");
					} else if (tableBean.isEmpty("SERV_SQL_WHERE")
							&& fieldBean.getStr("ITEM_CODE").equalsIgnoreCase("S_FLAG")) {
						tableBean.set("SERV_SQL_WHERE", " and S_FLAG=" + Constant.YES);
					}
					order += 10;
					itemList.add(fieldBean);
				} // end while
				tableBean.set("ITEM_LIST", itemList);
			} // end if next
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (trs != null) {
					trs.close();
				}
				if (irs != null) {
					irs.close();
				}
				if (dbmd != null) {
					dbmd = null;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (ownConn) {
				BaseContext.endConn(conn);
			}
		}
		return tableBean;
	}

	@Override
	public List<Bean> getViewList() {
		boolean ownConn = false;
		Connection conn = null;
		ResultSet rs = null; // 表结果集
		List<Bean> viewList = new ArrayList<Bean>();
		try {
			conn = Transaction.getConn();
			if (conn == null) {
				conn = BaseContext.getConn(); // 获取缺省数据源的连接
				ownConn = true;
			}
			String catelog = BaseContext.getDSBean(Transaction.getDsName()).getStr(DS.SCHEMA);
			String sql = "select TABLE_NAME VIEW_NAME, VIEW_DEFINITION VIEW_TEXT from INFORMATION_SCHEMA.VIEWS "
					+ "where TABLE_SCHEMA='" + catelog + "'";
			viewList = query(conn, sql);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (ownConn) {
				BaseContext.endConn(conn);
			}
		}
		return viewList;
	}

	@Override
	protected String replaceSql(String sql) {
		return sql.replaceAll("\\\\", "\\\\\\\\");
	}
}