/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db.impl;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.SqlBuilder;
import com.rh.core.base.db.TableBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;

/**
 * 生成SQL语句的Oracle实现类
 * 
 * @author WeiTieliang
 * @version $Id$ 1.0
 */
public class OscarBuilder extends SqlBuilder {
	/**
	 * 单例实例
	 */
	private static OscarBuilder inst;
	
	/**
	 * 私有构建体，禁止new方式实例化
	 */
	private OscarBuilder() {
	}

	/**
	 * 获取当前实例
	 * @return	当前实例
	 */
	public static SqlBuilder getBuilder() {
		if (inst == null) {
			inst = new OscarBuilder();
		}
		return inst;
	}
    
	/**
	 * 根据服务信息拼装prepareSql及对应的参数值，参数值会放置在dataBean.$SERV_VALUES中。
	 * @param servDef 服务定义
	 * @param dataBean 数据信息
     * @param preValue prepare的参数信息
	 * @return	拼装好的psql。
	 */
    public String insert(ServDefBean servDef, Bean dataBean, List<Object> preValue) {
        initInsertData(servDef, dataBean);
        StringBuilder bfField = new StringBuilder("insert into ")
            .append(servDef.getTableAction()).append(" (");
        StringBuilder bfValue = new StringBuilder(") values (");
        List<Bean> items = servDef.getTableItems();
        for (Bean item : items) {
            Object itemCode = item.get("ITEM_CODE");
            bfField.append(itemCode).append(",");
            bfValue.append("?,");
            if (!dataBean.contains(itemCode)) { //没有数据项则使用缺省值
                String def = item.getStr("ITEM_INPUT_DEFAULT");
                if ((def.length() > 0) && def.indexOf("@") >= 0) {
                    def = ServUtils.replaceSysVars(def);
                }
                dataBean.set(itemCode, def);
            }
            if (item.getStr("ITEM_FIELD_TYPE").equals(Constant.ITEM_FIELD_TYPE_NUM) 
                    && dataBean.contains(itemCode)) { //处理数据类型
                if (item.getStr("ITEM_FIELD_LENGTH").indexOf(Constant.SEPARATOR) > 0) { //浮点
                    preValue.add(dataBean.getDouble(itemCode));
                } else { //整形
                    preValue.add(dataBean.getLong(itemCode));
                }
            } else { //其他类型 
//            	if (item.getInt("ITEM_FIELD_CLOB") == Constant.YES_INT 
//		                && dataBean.contains(itemCode)) { 
//		            preValue.add(dataBean.getClob(itemCode));
//		            continue;
//		        }
                if (servDef.getBoolean("SAFE_FLAG")) { //启用安全html，进行替换
                    preValue.add(dataBean.getStr(itemCode).replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
                } else {
                    preValue.add(dataBean.get(itemCode));
                }
            }
        }
        // 去掉逗号
        bfField.setLength(bfField.length() - 1);
        bfValue.setLength(bfValue.length() - 1);
	    bfField.append(bfValue).append(")");
		return bfField.toString();
	}
    
    /**
     * 基于主键的修改prepared sql以及值的列表
     * @param servDef 服务定义
     * @param dataBean 参数信息，支持重载table和where条件，如果没有重载where条件，要有必须有id信息
     * @param preValue prepare的参数信息
     * @return  拼装好的psql。
     */
    public String update(ServDefBean servDef, Bean dataBean, List<Object> preValue) {
        List<Object> values = new ArrayList<Object>();
        String tableName = dataBean.contains(Constant.PARAM_TABLE) ? dataBean.getStr(Constant.PARAM_TABLE)
                : servDef.getTableAction();
        StringBuilder sbSql = new StringBuilder("update ").append(tableName).append(" set ");
        for (Object key : dataBean.keySet()) {
            Bean item = servDef.getItem(key);
            if ((item != null) && (item.getInt("ITEM_TYPE") == Constant.ITEM_TYPE_TABLE)) {
                sbSql.append(key).append("=?,");
                if (item.getStr("ITEM_FIELD_TYPE").equals(Constant.ITEM_FIELD_TYPE_NUM)) { //处理数据类型
                    if (item.getStr("ITEM_FIELD_LENGTH").indexOf(Constant.SEPARATOR) > 0) { //浮点
                        values.add(dataBean.getDouble(key));
                    } else { //整形
                        values.add(dataBean.getLong(key));
                    }  
                } else { //其他类型
//                	if (item.getInt("ITEM_FIELD_CLOB") == Constant.YES_INT) { 
//    		            preValue.add(dataBean.getClob(key));
//    		            continue;
//    		        }
                    if (servDef.getBoolean("SAFE_FLAG")) { //启用安全html，进行替换
                        values.add(dataBean.getStr(key).replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
                    } else {
                        values.add(dataBean.get(key));
                    }
                }
            }
        }
        // 去掉逗号
        sbSql.setLength(sbSql.length() - 1);
        // 得到基于主键的where条件
        sbSql.append(" where 1=1 ");
        if (dataBean.contains(Constant.PARAM_WHERE)) {
            sbSql.append(dataBean.getStr(Constant.PARAM_WHERE));
        } else {
            sbSql.append(preWhere(servDef, dataBean, preValue));
        }
        preValue.addAll(0, values);
        return sbSql.toString();
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
        sb.append("CONVERT(").append(item1).append(",INT)")
        .append(", ").append("CONVERT(").append(item2)
        .append(",INT)").append(")");
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