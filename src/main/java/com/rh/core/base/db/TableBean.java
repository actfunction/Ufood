package com.rh.core.base.db;

import java.util.List;

import com.rh.core.base.Bean;

/**
 * 表定义扩展Bean
 * 
 * @author wanglong
 * 
 */
public class TableBean extends Bean {
    /**
     * sid
     */
    private static final long serialVersionUID = -7014710231299682974L;

    /**
     * 获取表名
     * @return  表名
     */
    public String getTableCode() {
        return this.getStr("TABLE_CODE");
    }
    
    /**
     * 获取表中文名称
     * @return  表中文名称
     */
    public String getTableName() {
        return this.getStr("SERV_NAME");
    }
    
    /**
     * 获取表类型：view：视图；table：表
     * @return  表类型
     */
    public String getTableType() {
        return this.getStr("TABLE_TYPE");
    }
    
    /**
     * 获取表说明
     * @return  表说明
     */
    public String getTableMemo() {
        return this.getStr("SERV_MEMO");
    }
    
    /**
     * 获取主键字段，一个表只有一个主键字段
     * @return 主键字段名
     */
    public String getPKey() {
        return this.getStr("SERV_KEYS");
    }
    
    /**
     * 获取字段列表
     * @return 字段列表
     */
    public List<Bean> getItemList() {
        return this.getList("ITEM_LIST");
    }
    
    /**
     * 获取缺省的过滤条件
     * @return  过滤条件
     */
    public String getDefSqlWhere() {
        return this.getStr("SERV_SQL_WHERE");
    }
    
    /**
     * 获取缺省的排序规则
     * @return  缺省排序规则
     */
    public String getDefSqlOrder() {
        return this.getStr("SERV_SQL_ORDER");
    }
    
    /**
     * 是否为视图，true是，false则为表
     * @return  是否为视图
     */
    public boolean isView() {
        return this.getTableType().equalsIgnoreCase("VIEW");
    }
    
    /**
     * 获取视图的查询SQL
     * @return 视图的查询SQL
     */
    public String getViewQuerySql() {
        return this.getStr("VIEW_QUERY_SQL");
        
    }
    
    
    /**
     * 获取表的公司字段
     * @return  公司字段
     */
    public String getServCmpy() {
        return this.getStr("SERV_CMPY");
    }
}
