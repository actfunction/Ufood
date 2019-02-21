/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db.impl;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.SqlBuilder;
import com.rh.core.base.db.TableBean;
import com.rh.core.util.Constant;

/**
 * 生成SQL语句的Oracle实现类
 * 
 * @author wanglong
 * @version $Id$
 */
public class OracleBuilder extends SqlBuilder {
	/**
	 * 单例实例
	 */
	private static OracleBuilder inst;
	
	/**
	 * 私有构建体，禁止new方式实例化
	 */
	private OracleBuilder() {
	}

	/**
	 * 获取当前实例
	 * @return	当前实例
	 */
	public static SqlBuilder getBuilder() {
		if (inst == null) {
			inst = new OracleBuilder();
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
            return "create or replace view " + tableCode + " as " + tableBean.getViewQuerySql()
                    + ";" + Constant.STR_ENTER;
        } else { //表定义
            StringBuilder sb = new StringBuilder("create table ");
            sb.append(tableCode).append("(").append(Constant.STR_ENTER);
            List<Bean> itemList = tableBean.getItemList();
            StringBuilder sbCmt = new StringBuilder();
            String[] keys = tableBean.getStr("SERV_KEYS").split(Constant.SEPARATOR);
            for (Bean item : itemList) {
                sb.append(item.getStr("ITEM_CODE")).append(" ").append(item.getStr("$ITEM_FIELD_TYPE_SRC"));
                if (!item.getStr("$ITEM_FIELD_TYPE_SRC").equals("LONG")
                        && !item.getStr("$ITEM_FIELD_TYPE_SRC").equals("CLOB")
                        && !item.getStr("$ITEM_FIELD_TYPE_SRC").startsWith("TIMESTAMP")
                        && !item.getStr("$ITEM_FIELD_TYPE_SRC").equals("BLOB")) {
                    sb.append("(").append(item.getStr("ITEM_FIELD_LENGTH")).append(")");
                }
                if (item.getInt("ITEM_NOTNULL") == Constant.YES_INT) {
                    sb.append(" not null");
                }
                sb.append(",").append(Constant.STR_ENTER);
                sbCmt.append("comment on column ").append(tableCode).append(".")
                    .append(item.getStr("ITEM_CODE")).append(" is '")
                    .append(item.getStr("ITEM_MEMO").replaceAll("'", "''")).append("';").append(Constant.STR_ENTER);
            }
            sb.append("constraint PK_").append(tableCode).append(" primary key (");
            for (String key : keys) {
                sb.append(key).append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(")").append(Constant.STR_ENTER);
            //注释部分
            sb.append(");").append(Constant.STR_ENTER).append("comment on table ").append(tableCode).append(" is '")
                .append(tableBean.getStr("SERV_MEMO").replaceAll("'", "''")).append("';").append(Constant.STR_ENTER);
            sb.append(sbCmt);        
            return sb.toString();
        }
    }
    
    @Override
    public String bitand(String item1, String item2) {
        StringBuilder sb = new StringBuilder("BITAND(");
        sb.append(item1).append(", ").append(item2).append(")");
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
        StringBuilder sb = new StringBuilder("NVL(");
        sb.append(item1).append(", ").append(item2).append(")");
        return sb.toString();
    }
    
    @Override
    public String substr(String item, int start, int len) {
        StringBuilder sb = new StringBuilder("SUBSTR(");
        sb.append(item).append(", ").append(start).append(", ").append(len).append(")");
        return sb.toString();
    }
}