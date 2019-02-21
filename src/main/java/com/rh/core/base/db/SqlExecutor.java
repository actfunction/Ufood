/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.BaseContext;
import com.rh.core.base.BaseContext.THREAD;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.Strings;

/**
 * 执行通用SQL的父类
 * 
 * @author wanglong
 * @version $Id$
 */
public abstract class SqlExecutor {
    /** max size */
    public static final int MAX_SIZE = BaseContext.app("SQL_MAX_SIZE", 50000);

    /** log */
    private static Log log = LogFactory.getLog(SqlExecutor.class);
    
    /** sql监控超时  */
    protected static final String LOG_SQL_TIME = "LOG_SQL_TIME";
    /** sql监控超长  */
    protected static final String LOG_SQL_SIZE = "LOG_SQL_SIZE";
    /**  错误提示信息，返回前端，避免SQL语句错误直接传递给前端  */
    protected static final String ERROR_MSG = "数据库错误!";

    /**
     * 查询单体数据
     * @param sql sql语句
     * @return 查询结果
     */
    public Bean queryOne(String sql) {
        return queryOne(Transaction.getConn(), sql);
    }

    /**
     * 查询单体数据
     * @param conn 数据库连接
     * @param sql sql语句
     * @return 查询结果
     */
    public Bean queryOne(Connection conn, String sql) {
        return queryOne(conn, sql, null);
    }

    /**
     * 查询单条数据
     * @param sql sql语句
     * @param values prepared sql参数
     * @return 查询结果
     */
    public Bean queryOne(String sql, List<Object> values) {
        return queryOne(Transaction.getConn(), sql, values);
    }

    /**
     * 基于主键查询单体数据
     * @param sql sql语句
     * @param values prepared sql参数
     * @return 查询结果
     */
    public Bean queryById(String sql, List<Object> values) {
        return queryById(Transaction.getConn(), sql, values);
    }

    /**
     * 基于主键查询单体数据
     * @param sql sql语句
     * @param values prepared sql参数
     * @return 查询结果
     */
    public Bean queryById(Connection conn, String sql, List<Object> values) {
        List<Bean> rtnList = query(conn, sql, values);
        if (!rtnList.isEmpty()) {
            return rtnList.get(0);
        } else {
            return null;
        }
    }
    
    /**
     * 查询单体数据
     * @param conn 数据库连接
     * @param sql sql语句
     * @param values prepared sql参数
     * @return 查询结果
     */
    public Bean queryOne(Connection conn, String sql, List<Object> values) {
        List<Bean> rtnList = query(conn, sql, 1, 1, values);
        if (!rtnList.isEmpty()) {
            return rtnList.get(0);
        } else {
            return null;
        }
    }

    /**
     * 查询sql，不分页方式
     * @param sql sql语句
     * @return 查询列表数据
     */
    public List<Bean> query(String sql) {
        return query(sql, null, null);
    }
    
    /**
     * 查询sql，不分页方式
     * @param sql sql语句
     * @param qc 回调方法
     * @return 查询列表数据
     */
    public List<Bean> query(String sql, QueryCallback qc) {
        return query(sql, null, qc);
    }

    /**
     * 查询sql
     * @param conn 数据库连接
     * @param sql sql语句
     * @return 查询列表数据
     */
    public List<Bean> query(Connection conn, String sql) {
        return query(conn, sql, null);
    }

    /**
     * 查询sql
     * @param sql sql语句
     * @param values 参数值（prepared sql）
     * @return 查询列表数据
     */
    public List<Bean> query(String sql, List<Object> values) {
        return query(Transaction.getConn(), sql, values);
    }

    /**
     * 查询sql
     * @param sql sql语句
     * @param values 参数值（prepared sql）
     * @param qc 回调方法
     * @return 查询列表数据
     */
    public List<Bean> query(String sql, List<Object> values, QueryCallback qc) {
        return query(Transaction.getConn(), sql, values, qc);
    }
    
    /**
     * 查询sql，不分页方式
     * @param conn 数据库连接
     * @param sql sql语句
     * @param values 参数值（prepared sql）
     * @return 查询列表数据
     */
    public List<Bean> query(Connection conn, String sql, List<Object> values) {
        return query(conn, sql, 0, -1, values, null);
    }

    /**
     * 查询sql，不分页方式
     * @param conn 数据库连接
     * @param sql sql语句
     * @param values 参数值（prepared sql）
     * @param qc 回调方法
     * @return 查询列表数据
     */
    public List<Bean> query(Connection conn, String sql, List<Object> values, QueryCallback qc) {
        return query(conn, sql, 0, -1, values, qc);
    }
    
    /**
     * 查询sql执行，采用通用游标方式支持分页
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @return 查询列表数据
     */
    public List<Bean> query(String sql, int offset, int count) {
        return query(sql, offset, count, null);
    }

    /**
     * 查询sql执行，采用rownum方式支持分页
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @param values prepared sql 参数信息
     * @return 查询列表数据
     */
    public List<Bean> query(String sql, int offset, int count, List<Object> values) {
        return query(Transaction.getConn(), sql, offset, count, values, null);
    }

    /**
     * 查询sql执行
     * @param sql sql语句
     * @param values prepared sql 参数信息
     * @param rh 行处理器
     */
    public void queryCall(String sql, List<Object> values, IRowHandler rh) {
        queryCall(Transaction.getConn(), sql, 0, -1, values, rh, null);
    }
    
    /**
     * 查询sql执行
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @param values prepared sql 参数信息
     * @param rh 行处理器
     */
    public void queryCall(String sql, int offset, int count, List<Object> values, IRowHandler rh) {
        queryCall(Transaction.getConn(), sql, offset, count, values, rh, null);
    }
    
    /**
     * 查询sql执行
     * @param conn 连接
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @param values prepared sql 参数信息
     * @param rh 行处理器
     */
    public void queryCall(Connection conn, String sql, int offset, int count, List<Object> values, 
            IRowHandler rh) {
        queryCall(conn, sql, offset, count, values, rh, null);
    }
    
    /**
     * 查询sql执行
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @param values prepared sql 参数信息
     * @param qc 回调方法
     * @return 查询列表数据
     */
    public List<Bean> query(String sql, int offset, int count, List<Object> values, QueryCallback qc) {
        return query(Transaction.getConn(), sql, offset, count, values, qc);
    }
    
    /**
     * 查询sql执行，采用通用游标方式支持分页
     * @param conn 数据库连接
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @return 查询列表数据
     */
    public List<Bean> query(Connection conn, String sql, int offset, int count) {
        return query(conn, sql, offset, count, null, null);
    }
    
    /**
     * 查询sql执行，采用通用游标方式支持分页
     * @param conn 数据库连接，如果参数conn为null时自己获取缺省数据源的conn
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @param values prepare sql的参数信息
     * @return 查询列表数据
     */
    public List<Bean> query(Connection conn, String sql, int offset, int count, List<Object> values) {
        return query(conn, sql, offset, count, values, null);
    }
    
    /**
     * 查询sql执行，采用通用游标方式支持分页
     * @param conn 数据库连接，如果参数conn为null时自己获取缺省数据源的conn
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @param values prepare sql的参数信息
     * @param qc    回调方法
     * @return 查询列表数据
     */
    public List<Bean> query(Connection conn, String sql, int offset, int count, List<Object> values,
            QueryCallback qc) {
        RowHandlerList rh = new RowHandlerList();
        queryCall(conn, sql, offset, count, values, rh, qc);
        return rh;
    }
    
    /**
     * 查询sql执行，采用通用游标方式支持分页
     * @param conn 数据库连接，如果参数conn为null时自己获取缺省数据源的conn
     * @param sql sql语句
     * @param offset 记录所在位置
     * @param count 获取记录数，大于0表示分页处理
     * @param values prepare sql的参数信息
     * @param rh    行处理器，支持大数据量自定义处理
     * @param qc    回调方法
     */
    public void queryCall(Connection conn, String sql, int offset, int count, List<Object> values,
            IRowHandler rh, QueryCallback qc) {
        boolean ownConn = false; // 是否为自身的conn
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (conn == null) {
                conn = BaseContext.getConn();
                ownConn = true;
            }
            // 分页、滚动游标
            int concurType = (count > 0) ? ResultSet.TYPE_SCROLL_INSENSITIVE : ResultSet.TYPE_FORWARD_ONLY;
            long startTime = System.currentTimeMillis(); // 起始时间
            if (values == null || values.size() == 0) { // 正常sql
                // 创建Statement对象与数据库进行数据查询操作
                stmt = conn.createStatement(concurType, ResultSet.CONCUR_READ_ONLY);
                stmt.setMaxRows(MAX_SIZE);
                sql = replaceSql(sql);
                rs = stmt.executeQuery(sql);
            } else { // prepared sql
                pstmt = conn.prepareStatement(sql, concurType, ResultSet.CONCUR_READ_ONLY);
                pstmt.setMaxRows(MAX_SIZE);
                for (int i = 0; i < values.size(); i++) {
                    pstmt.setObject(i + 1, values.get(i));
                }
                rs = pstmt.executeQuery();
            }
            long endTime = System.currentTimeMillis(); // 结束时间
            if (offset > 0) {
                rs.absolute(offset);
            }
            // 对结果集数据进行包装处理
            int icount = 0;
            int columnCount = 0;
            ArrayList<Bean> columnList = new ArrayList<Bean>();
            ResultSetMetaData rsmd = rs.getMetaData(); // 获取查询字段列表
            columnCount = rsmd.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                Bean columnBean = new Bean();
                columnBean.set("NAME", rsmd.getColumnName(i));
                columnBean.set("TYPE", rsmd.getColumnType(i));
                columnBean.set("SCALE", rsmd.getScale(i));
                columnList.add(columnBean);
            }
            while (rs.next() && (icount < count)) {
                Bean bean = new Bean();
                for (int i = 0; i < columnCount; i++) {
                    if (i == 0) { // 设置虚拟主键
                        bean.setId(rs.getString(1));
                    }
                    Bean columnBean = columnList.get(i);
                    switch (columnBean.getInt("TYPE")) {
                        case Types.VARCHAR:
                            Object value = rs.getObject(i + 1);
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
                            } else if (columnBean.getInt("SCALE") <= 0) {
                                bean.set(columnBean.get("NAME"), ((BigDecimal) value).intValue());
                            } else {
                                bean.set(columnBean.get("NAME"), ((BigDecimal) value).toString());
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
                    bean.set(Constant.PARAM_ROWNUM, icount); //设置当前行，便于回调方法使用
                    qc.call(columnList, bean);
                }
                if (rh != null) {
                    bean.set(Constant.PARAM_ROWNUM, icount); //设置当前行，便于回调方法使用
                    rh.handle(columnList, bean);
                }
                icount++;
            } // end while
            if (qc != null) { // 执行回调方法提供列信息，无论是否有数据信息都执行此回调
                qc.end(icount, columnList);
            }
            if (rh != null) { // 执行回调方法提供列信息，无论是否有数据信息都执行此回调
                rh.end(icount, columnList);
            }
            long sqlTime = endTime - startTime;
            if (isLogSql(sqlTime, icount)) { // 记录sql、执行时间及数据量
                sql = sql + " " + paramToString(values);
                StringBuilder sbLog = new StringBuilder(sql).append(" [count:").append(icount);
                sbLog.append("] ").append((sqlTime)).append("ms").append(" ").append(conn.hashCode());
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
     * 查询sql，不分页
     * @param conn 数据库连接
     * @param sql sql语句
     * @param values prepared sql 参数信息，如果null则执行普通sql
     * @param rh 行处理器
     */
    public void queryCall(Connection conn, String sql, List<Object> values, IRowHandler rh) {
        queryCall(conn, sql, values, rh, null);
    }
    
    /**
     * 查询sql，不分页
     * @param conn 数据库连接
     * @param sql sql语句
     * @param values prepared sql 参数信息，如果null则执行普通sql
     * @param rh 行处理器
     * @param qc 回调方法
     */
    public void queryCall(Connection conn, String sql, List<Object> values, IRowHandler rh, QueryCallback qc) {
        queryCall(conn, sql, 0, -1, values, rh, qc);
    }
    
    /**
     * 分页查询数据
     * @param sql sql语句
     * @param pageNum 当前页数，从1开始
     * @param count 每页记录数
     * @param qc 回调方法
     * @return 分页查询后的结果集
     */
    public List<Bean> queryPage(String sql, int pageNum, int count, QueryCallback qc) {
        return queryPage(sql, pageNum, count, null, qc);
    }

    /**
     * 分页查询数据
     * @param sql sql语句
     * @param pageNum 当前页数，从1开始
     * @param count 每页记录数
     * @param values prepared sql 参数，null表示正常sql
     * @param qc 回调方法
     * @return 分页查询后的结果集
     */
    public List<Bean> queryPage(String sql, int pageNum, int count, List<Object> values, QueryCallback qc) {
        return queryPage(Transaction.getConn(), sql, pageNum, count, values, qc);
    }

    /**
     * 分页查询数据
     * @param conn 数据库连接
     * @param sql sql语句
     * @param pageNum 当前页数，从1开始
     * @param count 每页记录数
     * @param qc 回调方法
     * @return 分页查询后的结果集
     */
    public List<Bean> queryPage(Connection conn, String sql, int pageNum, int count, QueryCallback qc) {
        return queryPage(conn, sql, pageNum, count, null, qc);
    }

    /**
     * 分页查询数据
     * @param conn 数据库连接
     * @param sql sql语句
     * @param pageNum 当前页数，从1开始
     * @param count 每页记录数
     * @param values prepared sql 参数，null表示正常sql
     * @param qc 回调方法
     * @return 分页查询后的结果集
     */
    public List<Bean> queryPage(Connection conn, String sql, int pageNum, int count, List<Object> values,
            QueryCallback qc) {
        int offset;
        if (count < 0) {
            count = 0;
        }
        if (pageNum <= 0) {
            offset = 0;
        } else {
            offset = ((pageNum - 1) * count) + 1;
        }
        return query(conn, sql, offset, count, values, qc);
    }

    /**
     * 获取SQL语句的数量
     * @param sql sql语句
     * @return 总记录数
     */
    public int count(String sql) {
        return count(Transaction.getConn(), sql);
    }

    /**
     * 获取SQL语句的数量
     * @param conn 数据库连接
     * @param sql sql语句
     * @return 总记录数
     */
    public int count(Connection conn, String sql) {
        return count(conn, sql, null);
    }

    /**
     * 获取SQL语句的数量
     * @param sql sql语句
     * @param values prepared sql参数，null表示正常sql
     * @return 总记录数
     */
    public int count(String sql, List<Object> values) {
        return count(Transaction.getConn(), sql, values);
    }

    /**
     * 获取SQL语句的数量
     * @param conn 数据库连接
     * @param sql sql语句
     * @param values prepared sql参数，null表示正常sql
     * @return 总记录数
     */
    public int count(Connection conn, String sql, List<Object> values) {
        int count = 0;
        String pn = ".*\\(\\s*((?i)SELECT)\\s+.*"; //如果存在子查询则直接算最终的结果合计数
        if (sql.matches(pn)) {
            sql = "select count(*) COUNT_ from (" + sql + ") t";
        } else {
            // 通过正则表达式，将参数sql语句中的包含大写字母的FROM替换为小写的from，
            // 以便下面的：sql.indexOf(" from ")能得到正确结果
            sql = sql.replaceAll("\\s+([f|F][R|r][o|O][m|M])\\s+", " from ");
            // 对于是图表的分页展示处理。将分组的sql在加一层count。
            sql = sql.replaceAll("\\s+([g|G][R|r][o|O][u|U][p|P])\\s+[b|B][y|Y]\\s+", " group by ");
            if (sql.indexOf(" group by ") > 0) {
                sql = " from (" + sql + ")";
            }
            // 预处理SQL语句，去除order by 部分
            int pos = sql.indexOf(" from ");
            sql = "select count(*) COUNT_ " + sql.substring(pos);
            sql = sql.replaceFirst(" (order by|ORDER BY)\\s[\\s\\w_,]+", "");
        }
        count = ((Bean) queryOne(conn, sql, values)).getInt("COUNT_");
        if (log.isDebugEnabled()) {
            log.debug("Count:" + count);
        }
        return count;
    }

    /**
     * 执行sql语句处理
     * @param sql sql语句
     * @return 执行成功的数量
     */
    public int execute(String sql) {
        return execute(Transaction.getConn(), sql);
    }

    /**
     * 执行sql语句处理
     * @param conn 数据库连接
     * @param sql sql语句
     * @return 执行成功的数量
     */
    public int execute(Connection conn, String sql) {
        return execute(conn, sql, null);
    }

    /**
     * 执行sql语句处理
     * @param sql sql语句
     * @param values prepared sql参数，null表示正常sql
     * @return 执行成功的数量
     */
    public int execute(String sql, List<Object> values) {
        return execute(Transaction.getConn(), sql, values);
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
     * 执行sql语句，支持多条sql语句批量处理
     * @param sql 多条sql语句
     * @return 执行成功的数量
     */
    public int executeBatch(String[] sql) {
        return executeBatch(Transaction.getConn(), sql);
    }

    /**
     * 执行sql语句，支持多条sql语句批量处理
     * @param conn 数据库连接
     * @param sql 多条sql语句
     * @return 执行成功的数量
     */
    public int executeBatch(Connection conn, String[] sql) {
        boolean ownConn = false;
        Statement stmt = null;
        int rtnVal = 0;
        try {
            if (conn == null) {
                conn = BaseContext.getConn();
                ownConn = true;
            }
            stmt = conn.createStatement();

            long startTime = System.currentTimeMillis(); // 起始时间
            if (sql.length == 1) {
                sql[0] = replaceSql(sql[0]);
                rtnVal = stmt.executeUpdate(sql[0]);
                if (log.isDebugEnabled()) { // 记录sql语句和执行结果
                    log.debug(sql[0] + " [count:" + rtnVal + "]");
                }
            } else {
                for (int i = 0; i < sql.length; i++) {
                    sql[i] = replaceSql(sql[i]);
                    stmt.addBatch(sql[i]);
                }
                int[] ns = stmt.executeBatch();
                for (int i = 0; i < ns.length; i++) {
                    if (ns[i] > 0) {
                        rtnVal = rtnVal + ns[i];
                    } else if (ns[i] == -2) { // SUCCESS_NO_INFO = -2。成功执行命令，影响的行数是未知的
                        rtnVal++;
                    }
                    if (log.isDebugEnabled()) { // 记录sql语句和执行结果
                        log.debug(sql[i] + " [count:" + rtnVal + "]");
                    }
                }
            }
            long endTime = System.currentTimeMillis(); // 结束时间
            long sqlTime = endTime - startTime;
            if (isLogSql(sqlTime, rtnVal)) { // 记录sql、执行时间及数据量
                log.info((sqlTime) + "ms");
            }
        } catch (SQLException e) {
            log.error(sql + " " + e.getMessage());
            throw new RuntimeException(ERROR_MSG, e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (ownConn) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(sql + " " + e.getMessage());
                throw new RuntimeException(ERROR_MSG, e);
            }
        }
        return rtnVal;
    }

    /**
     * 执行prepare类型sql语句，需要提供对应的参数，支持批量处理多条数据
     * @param psql prepare sql语句
     * @param params 参数列表
     * @return 执行成功的数量
     */
    public int executeBatch(String psql, List<List<Object>> params) {
        return executeBatch(Transaction.getConn(), psql, params);
    }

    /**
     * 执行prepare类型sql语句，需要提供对应的参数，支持批量处理多条数据
     * @param conn 数据库连接
     * @param psql prepare sql语句
     * @param params 参数列表
     * @return 执行成功的数量
     */
    public int executeBatch(Connection conn, String psql, List<List<Object>> params) {
        PreparedStatement stmt = null;
        int rtnVal = 0;
        boolean ownConn = false;
        StringBuilder logs = new StringBuilder("");
        try {
            if (conn == null) {
                conn = BaseContext.getConn();
                ownConn = true;
            }
            stmt = conn.prepareStatement(psql);

            int dataSize = params.size() - 1;
            long startTime = System.currentTimeMillis(); // 起始时间
            for (int i = 0; i <= dataSize; i++) {
                List<Object> records = params.get(i);
                for (int j = 0; j < records.size(); j++) {
                    stmt.setObject(j + 1, records.get(j));
                }
                if (log.isDebugEnabled()) {
                    logs.append(getLogSql(psql, records)).append("\r\n");
                }
                
                stmt.addBatch();
                if ((i == dataSize) || (i % MAX_SIZE == 0)) {
                    int[] ns = stmt.executeBatch();
                    for (int j = 0; j < ns.length; j++) {
                        if (ns[j] > 0) {
                            rtnVal = rtnVal + ns[j];
                        } else if (ns[j] == -2) { // SUCCESS_NO_INFO = -2。成功执行命令，影响的行数是未知的
                            rtnVal++;
                        }
                    }
                }
            }
            long endTime = System.currentTimeMillis(); // 结束时间
            long sqlTime = endTime - startTime;
            if (isLogSql(sqlTime, rtnVal)) { // 记录sql、执行时间及数据量
                StringBuilder sbLog = new StringBuilder(psql).append(logs)
                        .append(" [count:").append(rtnVal).append("] ")
                        .append((endTime - startTime)).append("ms");
                log.info(sbLog);
            }
        } catch (SQLException e) {
            log.error(logs + " " + e.getMessage(), e);
            throw new RuntimeException(ERROR_MSG, e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (ownConn) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(psql + " " + e.getMessage(), e);
                throw new RuntimeException(ERROR_MSG, e);
            }
        }
        return rtnVal;
    }

    /**
     * 执行Bean类型sql语句，需要提供对应的参数，支持批量处理多条数据
     * @param beanSql bena sql语句,带#Key#信息，其中key为Bean中的键值
     * @param beanList 数据Bean列表
     * @return 执行成功的数量
     */
    public int executeBatchBean(String beanSql, List<Bean> beanList) {
        return executeBatchBean(Transaction.getConn(), beanSql, beanList);
    }
    
    /**
     * 执行Bean类型sql语句，需要提供对应的参数，支持批量处理多条数据
     * @param conn 数据库连接
     * @param beanSql bena sql语句,带#Key#信息，其中key为Bean中的键值
     * @param beanList 数据Bean列表
     * @return 执行成功的数量
     */
    public int executeBatchBean(Connection conn, String beanSql, List<Bean> beanList) {
        String psql = beanSql.replaceAll("#((\\w|_)*)#", "?");
        String[] itemNames = BeanUtils.getFieldCodes(beanSql).split(Constant.SEPARATOR);
        PreparedStatement stmt = null;
        int rtnVal = 0;
        boolean ownConn = false;
        try {
            if (conn == null) {
                conn = BaseContext.getConn();
                ownConn = true;
            }
            stmt = conn.prepareStatement(psql);
            long startTime = System.currentTimeMillis(); // 起始时间
            int batchSize = BaseContext.getInitConfig("SY_SQL_BATCH_SIZE", 10000);
            int dataSize = beanList.size() - 1;
            for (int i = 0; i <= dataSize; i++) {
                Bean data = beanList.get(i);
                for (int j = 0; j < itemNames.length; j++) {
                    stmt.setObject(j + 1, data.get(itemNames[j]));
                }
                stmt.addBatch();
                if ((i == dataSize) || (i % batchSize == 0)) {
                    int[] ns = stmt.executeBatch();
                    for (int j = 0; j < ns.length; j++) {
                        if (ns[j] > 0) {
                            rtnVal = rtnVal + ns[j];
                        } else if (ns[j] == -2) { // SUCCESS_NO_INFO = -2。成功执行命令，影响的行数是未知的
                            rtnVal++;
                        }
                    }
                }
            }
            long endTime = System.currentTimeMillis(); // 结束时间
            long sqlTime = endTime - startTime;
            if (isLogSql(sqlTime, rtnVal)) { // 记录sql、执行时间及数据量
                StringBuilder sbLog = new StringBuilder(psql)
                        .append(" [count:").append(rtnVal).append("] ")
                        .append((endTime - startTime)).append("ms");
                log.info(sbLog);
            }
        } catch (SQLException e) {
            log.error(psql + " " + e.getMessage(), e);
            throw new RuntimeException(ERROR_MSG, e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (ownConn) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error(psql + " " + e.getMessage(), e);
                throw new RuntimeException(ERROR_MSG, e);
            }
        }
        return rtnVal;
    }

    /**
     * 根据表名获取表的定义信息(待各数据库各自实现)
     * @param tableCode 表名或者视图名
     * @return 定义信息
     */
    public abstract TableBean getDBTable(String tableCode);
    
    /**
     * 获取系统视图列表
     * @return 视图列表
     */
    public abstract List<Bean> getViewList();
    
    /**
     * 对非prepare的sql语句进行预处理，对\和'进行相应替换。支持各数据库实现类进行扩展。
     * @param sql 未处理osql语句
     * @return 处理结果
     */
    protected String replaceSql(String sql) {
        return sql;
    }
    
    /**
     * 将对象数组的参数信息生成字符串信息
     * @param params 对象数组的参数信息
     * @return 字符串信息
     */
    protected String paramToString(List<Object> params) {
        StringBuilder sb = new StringBuilder("[");
        if (params != null) {
            int size = params.size();
            for (int i = 0; i < size; i++) {
                sb.append(params.get(i)).append("{").append(i + 1).append("},");
            }
            if (size > 0) {
                sb.setLength(sb.length() - 1);
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * 将preparesql以及对象数组的参数信息生成SQL字符串信息
     * @param sql 带?的sql
     * @param params 对象数组的参数信息
     * @return 字符串信息
     */
    protected String getLogSql(String sql, List<Object> params) {
        if (params != null) {
            try {
                String[] sqls = sql.split("\\?");
                StringBuilder sb = new StringBuilder(sqls[0]);
                int len = sqls.length;
                for (int i = 1; i < len; i++) {
                    String quote = Lang.isNum(params.get(i - 1)) ? "" : "'";
                    sb.append(quote).append(params.get(i - 1)).append(quote).append(sqls[i]);
                }
                if (params.size() == len) {
                    String quote = Lang.isNum(params.get(len - 1)) ? "" : "'";
                    sb.append(quote).append(params.get(len - 1)).append(quote);
                }
                sql = sb.toString();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return sql;
    }
    
    
    /**
     * 根据sql执行时间、数量以及log模式确定是否记录sql
     * @param sqlTime sql执行时间（毫秒）
     * @param sqlCount sql数量
     * @return 是否记录sql语句
     */
    protected boolean isLogSql(long sqlTime, int sqlCount) {
        boolean logSql = false;
        if (log.isDebugEnabled()) { //调试模式自动记录sql
            logSql = true;
            //如果线程变量设定Logdebug为false，则不输出
            if (!BaseContext.isEmpytyThread(THREAD.LOGDEBUG)) {
                if (!(Boolean) BaseContext.getThread(THREAD.LOGDEBUG)) {
                    logSql = false;
                }
            }
        } else { //非调试模式
            if (BaseContext.appInt(LOG_SQL_TIME) > 0) { 
                if (sqlTime > BaseContext.appInt(LOG_SQL_TIME)) { //超时记录sql
                    logSql = true;
                }
            }
            if (BaseContext.appInt(LOG_SQL_SIZE) > 0) {
                if (sqlCount > BaseContext.appInt(LOG_SQL_SIZE)) { //超长记录sql
                    logSql = true;
                }
            }
        }
        return logSql;
    }
}
