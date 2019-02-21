/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
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

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.BaseContext;
import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.db.IRowHandler;
import com.rh.core.base.db.QueryCallback;
import com.rh.core.base.db.RowHandlerList;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.TableBean;
import com.rh.core.base.db.Transaction;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.Strings;

/**
 * 执行SQL语句的Oscar实现类
 * 
 * @author Weitieliang
 * @version $Id$
 */
public class OscarExecutor extends SqlExecutor {
    /** log */
    private static Log log = LogFactory.getLog(OscarExecutor.class);
	public static String DBUSER = "platform";
	public static String DBPASSWD = "platform";
	public static String DBURL = "jdbc:oscar://192.168.8.121:2003/OSRDB";
	public static String DBDRIVER = "com.oscar.Driver";

    /**
     * 单例实例
     */
    private static OscarExecutor inst;

    /**
     * 私有构建体，禁止new方式实例化
     */
    private OscarExecutor() {
    }

    /**
     * 获取当前实例
     * @return 当前实例
     */
    public static SqlExecutor getExecutor() {
        if (inst == null) {
            inst = new OscarExecutor();
        }
        return inst;
    }  
  
    // 将字Clob转成String类型  
    public String ClobToString(Clob cs) throws SQLException, IOException {  
        String reString = "";  
        Reader is = cs.getCharacterStream();// 得到流  
        BufferedReader br = new BufferedReader(is);  
        String s = br.readLine();  
        StringBuffer sb = new StringBuffer();  
        while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING  
            sb.append(s);  
            s = br.readLine();  
        }  
        reString = sb.toString();  
        return reString;  
    }  

    /**
     * 执行sql语句处理
     * @param conn 数据库连接
     * @param sql sql语句
     * @param values prepared sql参数，null表示正常sql
     * @return 执行成功的数量
     */
    public int execute(Connection conn, String sql, List<Object> values) {
        Statement stmt = null;
        PreparedStatement pstmt = null;
        int rtnVal = 0;
        boolean ownConn = false;
        long startTime = System.currentTimeMillis(); // 起始时间
        try {
            if (conn == null) {
                conn = BaseContext.getConn();
                ownConn = true;
            }
            if (values == null || values.size() == 0) { // 正常sql
                stmt = conn.createStatement();
                sql = replaceSql(sql);
                sql = Strings.removeInvisibleChar(sql); // 移除不可见ASCII字符
                rtnVal = stmt.executeUpdate(sql);
            } else { // prepared sql
                pstmt = conn.prepareStatement(sql);
                for (int i = 0; i < values.size(); i++) {
                    Object val = values.get(i);
                    if (val instanceof String) {
                        // 移除不可见ASCII字符
                        val = Strings.removeInvisibleChar((String) val);
                    } 
                    pstmt.setObject(i + 1, val);
                    pstmt.setObject(i + 1, values.get(i));
                }
                
                rtnVal = pstmt.executeUpdate();
            }
            long sqlTime = System.currentTimeMillis() - startTime;
            if (isLogSql(sqlTime, rtnVal)) { // 记录sql、执行时间及数据量
                sql = getLogSql(sql, values);
                StringBuilder sbLog = new StringBuilder(sql).append(" [count:").append(rtnVal);
                sbLog.append("] ").append(sqlTime).append("ms").append(" ").append(conn.hashCode());
                log.info(sbLog);
            }
        } catch (SQLException e) {
            log.error(getLogSql(sql, values)  + " " + e.getMessage());
            throw new RuntimeException(ERROR_MSG, e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                } else if (pstmt != null) {
                    pstmt.close();
                }
                if (ownConn) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(sql + "  " + e.getMessage());
                throw new RuntimeException(ERROR_MSG, e);
            }
        }
        return rtnVal;
    }

	/**
     * 查询sql
     * @param conn 数据库连接
     * @param sql sql语句
     * @param values prepared sql 参数信息，如果null则执行普通sql
     * @param qc 回调方法
     * @return 查询列表数据
     */
    public List<Bean> query(Connection conn, String sql, List<Object> values, QueryCallback qc) {
        RowHandlerList rh = new RowHandlerList();
        queryCall(conn, sql, values, rh, qc);
        return rh;
    }
    
    /**
     * 查询sql
     * @param conn 数据库连接
     * @param sql sql语句
     * @param values prepared sql 参数信息，如果null则执行普通sql
     * @param rh 行处理器
     * @param qc 回调方法
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
            StopWatch sw = new StopWatch();
            sw.start();            
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
                                if (columnBean.getInt("SIZE") <= 9) { //根据长度决定采用哪种类型
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
                    case Types.CLOB:
                        bean.set(columnBean.get("NAME"), clobToString(rs.getClob(i + 1)));
                        break;
                    case Types.NCLOB:
                        bean.set(columnBean.get("NAME"), clobToString(rs.getClob(i + 1)));
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
                    bean.set(Constant.PARAM_ROWNUM, count); //设置当前行，便于回调方法使用
                    qc.call(columnList, bean);
                }
                if (rh != null) {
                    bean.set(Constant.PARAM_ROWNUM, count); //设置当前行，便于回调方法使用
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
            long sqlTime = sw.getTime();
            if (isLogSql(sqlTime, count)) { // 记录sql、执行时间及数据量
                sql = getLogSql(sql, values);
                StringBuilder sbLog = new StringBuilder(sql).append(" [count:").append(count);
                sbLog.append("] [time:").append(sqlTime).append("ms] ")
                    .append(conn.hashCode());
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
                }
                if (pstmt != null) {
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
     * 
     * @param clob clob类型数据对象
     * @return 将clob字段的值转换成字符串
     */
    private String clobToString(Clob clob) {
        if (clob == null) {
            return "";
        }
        try {
            String reString = "";
            Reader is;

            is = clob.getCharacterStream();
            // 得到流
            BufferedReader br = new BufferedReader(is);
            String s = br.readLine();
            StringBuffer sb = new StringBuffer();
            boolean firstLine = true;
            while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
                if (firstLine) {
                	firstLine = false;
                } else {
                	sb.append("\n");
                }
            	sb.append(s);
                s = br.readLine();
            }
            reString = sb.toString();
//            log.debug("---------- clobToString : " + reString + "----------");
            return reString;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }

        return "";
    }

    /**
     * 查询sql执行，采用通用游标方式支持分页
     * @param conn 数据库连接，如果参数conn为null时自己获取缺省数据源的conn
     * @param sql sql语句
     * @param offset 记录所在位置，从1开始
     * @param count 获取记录数，大于0表示分页处理
     * @param values prepare sql的参数信息
     * @param rh    行处理器，支持大数据量自定义处理
     * @param qc    回调方法
     */
    public void queryCall(Connection conn, String sql, int offset, int count, List<Object> values,
            IRowHandler rh, QueryCallback qc) {
        if (count > 0) { // 分页处理
            StringBuilder paging = new StringBuilder();
            if (values != null) { // psql
                paging.append("select * from ( select row_.*, rownum rownum_ from ( ");
                paging.append(sql).append(" ) row_) where rownum_<? and rownum_>=?");
                values.add(offset + count);
                values.add(offset);
            } else {
                paging.append("select * from ( select row_.*, rownum rownum_ from ( ");
                paging.append(sql).append(" ) row_) where rownum_<").append(offset + count);
                paging.append(" and rownum_>=").append(offset);
            }
            sql = paging.toString();
        }
        queryCall(conn, sql, values, rh, qc);
    }
    
    /**
     * 查询sql执行，采用rownum方式支持分页
     * @param conn 数据库连接
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @param values prepared sql 参数信息，如果null则执行普通sql
     * @param qc 回调方法
     * @return 查询列表数据
     */
    public List<Bean> query(Connection conn, String sql, int offset, int count, List<Object> values, 
            QueryCallback qc) {
        if (count > 0) { // 分页处理
            StringBuilder paging = new StringBuilder();
            if (values != null) { // psql
                paging.append("select * from ( select row_.*, rownum rownum_ from ( ");
                paging.append(sql).append(" ) row_) where rownum_<? and rownum_>=?");
                values.add(offset + count);
                values.add(offset);
            } else {
                paging.append("select * from ( select row_.*, rownum rownum_ from ( ");
                paging.append(sql).append(" ) row_) where rownum_<").append(offset + count);
                paging.append(" and rownum_>=").append(offset);
            }
            sql = paging.toString();
        }
        return query(conn, sql, values, qc);
    }

    /**
     * 根据表名获取表的定义信息
     * @param tableCode 表名称
     * @return 表定义信息bean
     */
    public TableBean getDBTable(String tableCode) {
        TableBean tableBean = null;
        Connection conn = null;
        DatabaseMetaData dbmd = null;    //数据库metadata
        ResultSet trs = null;  //表结果集
        ResultSet irs = null;  //字段结果集
        boolean ownConn = false;
        try {
            conn = Transaction.getConn();
            if (conn == null) {
                conn = BaseContext.getConn();  // 获取缺省数据源的连接
                ownConn = true;
            }
            dbmd = conn.getMetaData();
            String dbUserName = dbmd.getUserName().toUpperCase();
            trs = dbmd.getTables(null, dbUserName, tableCode, null);
            tableBean = null;
            if (trs.next()) {
                tableBean = new TableBean();
                tableBean.set("TABLE_CODE", trs.getString("TABLE_NAME"));
                tableBean.set("TABLE_TYPE", trs.getString("TABLE_TYPE"));
                String cmtSql = "select COMMENTS from USER_TAB_COMMENTS where TABLE_NAME='" + tableCode + "'";
                Bean cmtBean = queryOne(conn, cmtSql);
                if (cmtBean != null) {
                    String memo = cmtBean.getStr("COMMENTS");
                    if (memo.length() > 0) { // 有字段说明信息
                        tableBean.set("SERV_MEMO", memo);
                        memo = memo.replaceFirst("，", ",").replaceFirst(" ", ",").replace("：", ",");
                        if (memo.indexOf(",") > 0) {  // 截取说明前部信息作为表中文名称
                            tableBean.set("SERV_NAME", memo.substring(0, memo.indexOf(",")));
                        } else {
                            tableBean.set("SERV_NAME", memo);
                        }
                    }
                }
                if (tableBean.isView()) { //特殊处理视图的定义
                    cmtSql = "select VIEW_NAME,TEXT from USER_VIEWS where VIEW_NAME='" + tableCode + "'";
                    tableBean.set("VIEW_QUERY_SQL", queryOne(cmtSql).getStr("TEXT"));
                }
                irs = dbmd.getPrimaryKeys(null, dbUserName, tableCode);
                StringBuilder sbKeys = new StringBuilder();
                while (irs.next()) {
                    sbKeys.append(irs.getString("COLUMN_NAME")).append(Constant.SEPARATOR); //设置唯一主键
                }
                irs.close();
                if (sbKeys.length() > 0) {
                    sbKeys.setLength(sbKeys.length() - 1);
                    tableBean.set("SERV_KEYS", sbKeys.toString()); //设置唯一主键
                }
                //get item list
                List<Bean> itemList = new ArrayList<Bean>();
                irs = dbmd.getColumns(null, dbUserName, tableCode, "%");
                Bean fieldBean;
                int order = 0;
                cmtSql = "select COLUMN_NAME, COMMENTS from " + "USER_COL_COMMENTS where TABLE_NAME='"
                        + tableCode + "'";
                HashMap<Object, Bean> cmtHash = BeanUtils.listToHash((List<Bean>) query(conn, cmtSql));
                while (irs.next()) { // 获取字段定义
                    fieldBean = new Bean(irs.getString("COLUMN_NAME")); // 以字段名做虚拟主键用来判断是否重复
                    fieldBean.set("ITEM_ID", Lang.getUUID()); // 设置UUID，为后面插入做准备
                    fieldBean.set("ITEM_CODE", irs.getString("COLUMN_NAME"));
                    String type = irs.getString("TYPE_NAME");
                    fieldBean.set("$ITEM_FIELD_TYPE_SRC", type);
                    String length = irs.getString("COLUMN_SIZE");
                    String def = "";
                    if ((order == 0) && (tableBean.isEmpty("SERV_KEYS"))) { //没有主键默认取第一个作为主键
                        tableBean.set("SERV_KEYS", irs.getString("COLUMN_NAME"));
                    }
                    if (type.equals("NUMBER") || type.equals("NUMBERIC")) { // 数字类型
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
                    } else if (fieldBean.getStr("ITEM_CODE").equals("S_PUBLIC")) {
                        def = Constant.NO;
                    } else if (fieldBean.getStr("ITEM_CODE").equals("S_MTIME")) {
                        def = "@DATETIME_TS@";
                    } else if (fieldBean.getStr("ITEM_CODE").equals("S_ATIME")) {
                        def = "@DATETIME_TS@";
                    } else if (fieldBean.getStr("ITEM_CODE").equals("S_USER")) {
                        def = "@USER_CODE@";
                    } else if (fieldBean.getStr("ITEM_CODE").equals("S_DEPT")) {
                        def = "@DEPT_CODE@";
                    } else if (fieldBean.getStr("ITEM_CODE").equals("S_TDEPT")) {
                        def = "@TDEPT_CODE@";
                    } else if (fieldBean.getStr("ITEM_CODE").equals("S_ODEPT")) {
                        def = "@ODEPT_CODE@";
                    } else if (fieldBean.getStr("ITEM_CODE").equals("S_CMPY")) {
                        def = "@CMPY_CODE@";
                        tableBean.set("SERV_CMPY", "S_CMPY");
                    }
                    fieldBean.set("ITEM_FIELD_LENGTH", length);
                    fieldBean.set("ITEM_INPUT_DEFAULT", def);
                    if (cmtHash.containsKey(fieldBean.getStr("ITEM_CODE"))) {
                        String memo = cmtHash.get(fieldBean.getStr("ITEM_CODE")).getStr("COMMENTS");
                        if ((memo != null) && memo.length() > 0) { // 有字段说明信息
                            fieldBean.set("ITEM_MEMO", memo);
                            memo = memo.replaceFirst("，", ",").replaceFirst(" ", ",")
                                    .replace("：", ",").replace(":", ",").replace("，", ",");
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
                    fieldBean.set("ITEM_TYPE", Constant.ITEM_TYPE_TABLE); //表字段
                    if (tableBean.isEmpty("SERV_SQL_ORDER") 
                            && fieldBean.getStr("ITEM_CODE").equalsIgnoreCase("S_MTIME")) {
                        tableBean.set("SERV_SQL_ORDER", " S_MTIME desc");
                    } else if (tableBean.isEmpty("SERV_SQL_WHERE") 
                            && fieldBean.getStr("ITEM_CODE").equalsIgnoreCase("S_FLAG")) {
                        tableBean.set("SERV_SQL_WHERE", " and S_FLAG=" + Constant.YES);
                    }
                    order += 10;
                    itemList.add(fieldBean);
                }
                tableBean.set("ITEM_LIST", itemList);
            }
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
        ResultSet rs = null;  //表结果集
        List<Bean> viewList = new ArrayList<Bean>();
        try {
            conn = Transaction.getConn();
            if (conn == null) {
                conn = BaseContext.getConn();  // 获取缺省数据源的连接
                ownConn = true;
            }
            String sql = "select VIEW_NAME,TEXT VIEW_TEXT from USER_VIEWS";
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
}
