/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db.impl;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.BaseContext.DB_TYPE;
import com.rh.core.base.db.SqlBuilder;
import com.rh.core.base.db.TableBean;
import com.rh.core.base.db.Transaction;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;

/**
 * 生成SQL语句的MYSQL实现类
 * 
 * @author wanglong
 * @version $Id$
 */
public class MysqlBuilder extends SqlBuilder {
	/**
	 * 单例实例
	 */
	private static MysqlBuilder inst;
	
	/**
	 * 私有构建体，禁止new方式实例化
	 */
	private MysqlBuilder() {
	}

	/**
	 * 获取当前实例
	 * @return	当前实例
	 */
	public static SqlBuilder getBuilder() {
		if (inst == null) {
			inst = new MysqlBuilder();
		}
		return inst;
	}
    
    @Override
    public String getDBTableDDL(TableBean tableBean) {
        if (tableBean == null) {
            return "";
        }
        String tableCode = tableBean.getTableCode();
        if (tableBean.isView()) { //视图定义
            String query = tableBean.getViewQuerySql();
            if (Transaction.getDBType().equals(DB_TYPE.ORACLE)) { 
                if (tableCode.equals("SY_SERV_TABLE_COL_V")) { //忽略Oracle特有视图
                    return "";
                }
                query = query.replaceAll("NVL\\(", "IFNULL\\(").replaceAll("nvl\\(", "IFNULL\\("); //转换Oracle特定函数为mysql
            }
            return "create or replace view " + tableCode + " as " + query + ";" + Constant.STR_ENTER;
        } else { //表定义
            StringBuilder sb = new StringBuilder("create table ");
            sb.append(tableCode).append("(").append(Constant.STR_ENTER);
            List<Bean> itemList = tableBean.getItemList();
            int size = itemList.size();
            String[] keys = tableBean.getStr("SERV_KEYS").split(Constant.SEPARATOR);
            for (int i = 0; i < size; i++) {
                Bean item = itemList.get(i);
                String itemType = item.getStr("$ITEM_FIELD_TYPE_SRC");
                if (itemType.equalsIgnoreCase("VARCHAR2") || itemType.equalsIgnoreCase("NVARCHAR2")) {
                    itemType = "VARCHAR";
                } else if (itemType.equalsIgnoreCase("NUMBER")) {
                    itemType = "NUMERIC";
                } else if (itemType.equalsIgnoreCase("LONG") || itemType.equalsIgnoreCase("CLOB")) {
                    itemType = "LONGTEXT";
                }
                sb.append(item.getStr("ITEM_CODE")).append(" ").append(itemType);
                if (!itemType.equals("LONGTEXT") && !itemType.startsWith("TIMESTAMP") && !itemType.equals("BLOB")) {
                    sb.append("(").append(item.getStr("ITEM_FIELD_LENGTH")).append(")");
                }
                if (item.getInt("ITEM_NOTNULL") == Constant.YES_INT) {
                    sb.append(" not null");
                }
                if (Lang.arrayHas(keys, item.getStr("ITEM_CODE"))) { //主键字段
                    sb.append(" primary key");
                }
                sb.append(" comment '").append(item.getStr("ITEM_MEMO").replaceAll("'", "''")).append("'");
                if (i < (size - 1)) { //非最后一行
                    sb.append(",").append(Constant.STR_ENTER);
                }
            }
            sb.append(Constant.STR_ENTER).append(") comment '")
                .append(tableBean.getStr("SERV_MEMO").replaceAll("'", "''")).append("';")
                .append(Constant.STR_ENTER).append(Constant.STR_ENTER);
            return sb.toString();
        }
    }
    
    @Override
    public String bitand(String item1, String item2) {
        StringBuilder sb = new StringBuilder(item1);
        sb.append("&").append(item2);
        return sb.toString();
    }
    
    @Override
    public String concat(String item1, String item2) {
        StringBuilder sb = new StringBuilder("CONCAT(");
        sb.append(item1).append(", ").append(item2).append(")");
        return sb.toString();
    }
    
    @Override
    public String nvl(String item1, String item2) {
        StringBuilder sb = new StringBuilder("IFNULL(");
        sb.append(item1).append(", ").append(item2).append(")");
        return sb.toString();
    }
    
    @Override
    public String substr(String item, int start, int len) {
        StringBuilder sb = new StringBuilder("SUBSTRING(");
        sb.append(item).append(", ").append(start).append(", ").append(len).append(")");
        return sb.toString();
    }
}