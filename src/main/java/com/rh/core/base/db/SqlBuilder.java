/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base.db;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;

/**
 * 生成SQL语句的接口类
 * 
 * @author wanglong
 * @version $Id$
 */
public abstract class SqlBuilder {
  
    /**
     * 基于设定的参数生成查询sql
     * @param servDef 服务定义
     * @param paramBean 参数信息，支持设置select、table和where条件
     * @param preValue prepare的参数信息
     * @return  拼装好的sql
     */
    public String select(ServDefBean servDef, Bean paramBean, List<Object> preValue) {
        StringBuilder sb = new StringBuilder("select ");
        
        //在查询语句Select之后加入指定关键字，如：Distinct
        if (paramBean.isNotEmpty(Constant.SELECT_KEYWORDS)) {
            sb.append(" ").append(paramBean.getStr(Constant.SELECT_KEYWORDS)).append(" ");
        }
        
        if (paramBean.contains(Constant.PARAM_SELECT)) {
            String keyField = servDef.getPKey();
            String select = paramBean.getStr(Constant.PARAM_SELECT);
            String pn = "\\s*((?i)" + keyField + "|distinct).*"; //正则：忽略大小写，忽略以主键字段或distinct开头
            if (paramBean.isNotEmpty(Constant.PARAM_GROUP) || select.indexOf("(") >= 0 || select.equals("*")
                    || select.matches(pn)) { //select为复杂结构（必然包含分组、子查询、distinct等不再处理主键字段） 
                sb.append(select);
            } else { //特殊处理主键字段，将主键字段放在第一个位置
                sb.append(keyField).append(",");
                if (select.endsWith(keyField)) {
                    sb.append(select.replaceAll(",\\s*" + keyField, ""));             
                } else {
                    sb.append(select.replace(keyField + ",", ""));             
                }
            }
        } else {
            List<Bean> items = servDef.getViewItems();
            for (int i = 0; i < items.size(); i++) { //获取全部字段数据
                Bean itemBean = items.get(i);
                sb.append(itemBean.get("ITEM_CODE")).append(",");
            }
            sb.setLength(sb.length() - 1);
        }
        String tableName;
        if (paramBean.contains(Constant.PARAM_TABLE)) {
            tableName = paramBean.getStr(Constant.PARAM_TABLE);
        } else {
            tableName = servDef.getTableView();
        }
        sb.append(" from ").append(tableName).append(" where 1=1 ");
        if (paramBean.contains(Constant.PARAM_WHERE)) {
            sb.append(paramBean.get(Constant.PARAM_WHERE));
        } else {
            sb.append(preWhere(servDef, paramBean, preValue));
        }
        if (paramBean.isNotEmpty(Constant.PARAM_GROUP)) {
            sb.append(" group by ").append(paramBean.get(Constant.PARAM_GROUP));
        }
        if (paramBean.isNotEmpty(Constant.PARAM_ORDER)) {
            sb.append(" order by ").append(paramBean.get(Constant.PARAM_ORDER));
        }
        return sb.toString();
    }

    /**
     * 得到假删除SQL
     * @param servDef  服务定义
     * @param paramBean 参数信息，支持重载table和where条件，如果没有重载where条件
     * @param preValue prepare的参数信息
     * @return  假删除SQL
     */
    public String delete(ServDefBean servDef, Bean paramBean, List<Object> preValue) {
        return delete(servDef, paramBean, preValue, false);
    }
    
    /**
     * 得到删除语句的SQL
     * @param servDef 服务定义
     * @param paramBean 参数信息，支持重载table信息
     * @param preValues prepare参数数据
     * @param trueDelete 是否真删除
     * @return 插入语句SQL
     */
    public String delete(ServDefBean servDef, Bean paramBean, List<Object> preValues, boolean trueDelete) {
        StringBuilder sbSql = new StringBuilder();
        String tableName = paramBean.contains(Constant.PARAM_TABLE) ? paramBean.getStr(Constant.PARAM_TABLE)
                : servDef.getTableAction();
        if (trueDelete) {
            sbSql.append("delete from ").append(tableName);
        } else {
            sbSql.append("update ").append(tableName).append(" set S_FLAG=?");
            preValues.add(0, Constant.NO_INT);
            //假删除自动处理更新时间
            if (servDef.containsItem("S_MTIME")) {
                sbSql.append(",S_MTIME=?");
                preValues.add(1, DateUtils.getDatetimeTS());
            }
        }
        sbSql.append(" where 1=1 ");
        String where = null;
        if (paramBean.contains(Constant.PARAM_WHERE)) {
            where = paramBean.getStr(Constant.PARAM_WHERE);
        } else {
            where = preWhere(servDef, paramBean, preValues);
        }
        if (where.trim().isEmpty()) {
            throw new RuntimeException("NO WHERE DELETE IS FORBIDDEN!");
        }
        sbSql.append(where);
        return sbSql.toString();
    }
    

    /**
     * 得到修改语句的SQL
     * @param servDef 服务定义
     * @param setBean 设置值的信息
     * @param whereBean 过滤条件
     * @param preValue prepare的参数信息
     * @return 修改语句SQL
     */
    public String update(ServDefBean servDef, Bean setBean, Bean whereBean, List<Object> preValue) {
        List<Object> values = new ArrayList<Object>();
        StringBuilder sbSql = new StringBuilder("update ")
            .append(servDef.getTableAction()).append(" set ");
        for (Object key : setBean.keySet()) {
            Bean item = servDef.getItem(key);
            if ((item != null) && (item.getInt("ITEM_TYPE") == Constant.ITEM_TYPE_TABLE)) {
                sbSql.append(key).append("=?,");
                values.add(setBean.get(key));
            }
        }
        if (servDef.containsItem("S_MTIME") && setBean.isEmpty("S_MTIME")) { //存在未处理的更新时间
            sbSql.append("S_MTIME=?,");
            values.add(DateUtils.getDatetimeTS());
        }
        // 去掉逗号
        sbSql.setLength(sbSql.length() - 1);
        // 得到基于主键的where条件
        sbSql.append(" where 1=1 ");
        String where = null;
        if (whereBean.contains(Constant.PARAM_WHERE)) {
            where = whereBean.getStr(Constant.PARAM_WHERE);
        } else {
            where = preWhere(servDef, whereBean, preValue);
        }
        if (where.trim().isEmpty()) {
            throw new RuntimeException("NO WHERE UPDATE IS FORBIDDEN!");
        }
        sbSql.append(where);
        preValue.addAll(0, values);
        return sbSql.toString();
    }
    
    /**
     * 基于主键的修改prepared sql以及值的列表
     * @param servDef 服务定义
     * @param paramBean 参数信息，支持重载table和where条件，如果没有重载where条件，要有必须有id信息
     * @param preValue prepare的参数信息
     * @return  拼装好的psql。
     */
    public String update(ServDefBean servDef, Bean paramBean, List<Object> preValue) {
        List<Object> values = new ArrayList<Object>();
        String tableName = paramBean.contains(Constant.PARAM_TABLE) ? paramBean.getStr(Constant.PARAM_TABLE)
                : servDef.getTableAction();
        StringBuilder sbSql = new StringBuilder("update ").append(tableName).append(" set ");
        for (Object key : paramBean.keySet()) {
            Bean item = servDef.getItem(key);
            if ((item != null) && (item.getInt("ITEM_TYPE") == Constant.ITEM_TYPE_TABLE)) {
                sbSql.append(key).append("=?,");
                if (item.getStr("ITEM_FIELD_TYPE").equals(Constant.ITEM_FIELD_TYPE_NUM)) { //处理数据类型
                    if (item.getStr("ITEM_FIELD_LENGTH").indexOf(Constant.SEPARATOR) > 0) { //浮点
                        values.add(paramBean.getDouble(key));
                    } else { //整形
                        values.add(paramBean.getLong(key));
                    }  
                } else { //其他类型
                    if (servDef.getBoolean("SAFE_FLAG")) { //启用安全html，进行替换
                        values.add(paramBean.getStr(key).replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
                    } else {
                        values.add(paramBean.get(key));
                    }
                }
            }
        }
        // 去掉逗号
        sbSql.setLength(sbSql.length() - 1);
        // 得到基于主键的where条件
        sbSql.append(" where 1=1 ");
        if (paramBean.contains(Constant.PARAM_WHERE)) {
            sbSql.append(paramBean.getStr(Constant.PARAM_WHERE));
        } else {
            sbSql.append(preWhere(servDef, paramBean, preValue));
        }
        preValue.addAll(0, values);
        return sbSql.toString();
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
	 * 根据数据bean和表名自动生成插入sql
	 * @param tableCode	表名
	 * @param dataBean	数据bean
	 * @return	sql语句
	 */
	public String insertByBean(String tableCode, Bean dataBean) {
		StringBuilder psql = new StringBuilder("insert into ").append(tableCode);
		StringBuilder sbValues = new StringBuilder(" values (");
		StringBuilder sbFields = new StringBuilder(" (");
		List<Object> values = new ArrayList<Object>();
		for (Object key : dataBean.keySet()) {
			//去除不符合条件的系统保留数据项
		    String keyStr = key.toString();
			if (!keyStr.startsWith("$") && !keyStr.startsWith("_")) {
				sbFields.append(key).append(",");
				sbValues.append("?,");
				values.add(dataBean.get(key));
			}
		}
		sbValues.setLength(sbValues.length() - 1);
		sbFields.setLength(sbFields.length() - 1);
		psql.append(sbFields).append(")").append(sbValues).append(")");
		dataBean.set(Constant.PARAM_PRE_VALUES, values);
		return psql.toString();
	}

    /**
     * 根据服务数据拼装prepared sql where语句
     * @param servDef 服务定义
     * @param dataBean 参数信息
     * @return  带and的where语句
     */
    public String where(ServDefBean servDef, Bean dataBean) {
        StringBuilder sbSql = new StringBuilder();
        String quotes;
        if (dataBean.getId().length() > 0) { //设置了主键
            Bean itemBean = servDef.getItem(servDef.getPKey());
            if (itemBean != null) {
                if (itemBean.getStr("ITEM_FIELD_TYPE").equals(Constant.ITEM_FIELD_TYPE_NUM)) {
                    quotes = "";
                } else {
                    quotes = "'";
                }
                sbSql.append(" and ").append(servDef.getPKey()).append("=").append(quotes);
                sbSql.append(dataBean.getId()).append(quotes);
            }
        } else {
            for (Object key : dataBean.keySet()) {
                Bean itemBean = servDef.getItem(key);
                if (itemBean != null) {
                    if (itemBean.getStr("ITEM_FIELD_TYPE").equals(Constant.ITEM_FIELD_TYPE_NUM)) {
                        quotes = "";
                    } else {
                        quotes = "'";
                    }
                    sbSql.append(" and ").append(key).append("=").append(quotes);
                    sbSql.append(dataBean.get(key)).append(quotes);
                }
            }
        }
        return sbSql.toString();
    }
    
    /**
     * 根据服务数据拼装prepared sql where语句
     * @param servDef 服务定义
     * @param dataBean 参数信息
     * @param preValues prepare的参数信息
     * @return	带and的where语句
     */
	public String preWhere(ServDefBean servDef, Bean dataBean, List<Object> preValues) {
    	StringBuilder sbSql = new StringBuilder();
    	if (dataBean.getId().length() > 0) { //设置了主键
    	    sbSql.append(" and ").append(servDef.getPKey()).append("=?");
    	    preValues.add(dataBean.getId());
    	} else {
    	    for (Object key : dataBean.keySet()) {
    	        Bean itemBean = servDef.getItem(key);
    	        if (itemBean != null) {
    	            if (dataBean.getStr(key).length() > 0) { //处理非空
        	            sbSql.append(" and ").append(key).append("=?");
        	            preValues.add(dataBean.get(key));
    	            } else { //处理为空的情况
                        sbSql.append(" and ").append(key).append(" is null");
    	            }
    	        }
    	    }
    	}
    	return sbSql.toString();
    }
	
    /**
     * 获取指定表定义对应的DDL SQL语句
     * @param tableBean 表定义信息
     * @return 表定义SQL
     */
    public abstract String getDBTableDDL(TableBean tableBean);
    
    /**
     * SQL位与运算
     * @param item1 字段1
     * @param item2 字段2
     * @return 与运算表达式
     */
    public abstract String bitand(String item1, String item2);
    
    /**
     * SQL字符串合并运算
     * @param item1 字段1
     * @param item2 字段2
     * @return 合并运算表达式
     */
    public abstract String concat(String item1, String item2);
    
    /**
     * SQL字段为空判断方法，item1为空则返回item2的值
     * @param item1 字段1
     * @param item2 字段2
     * @return 字段为空表达式
     */
    public abstract String nvl(String item1, String item2);
    
    /**
     * SQL字符串截串处理
     * @param item 字段
     * @param start 起始位置，从1开始
     * @param len 获取长度
     * @return 截串处理表达式
     */
    public abstract String substr(String item, int start, int len);
    
	/**
	 * 插入数据时需要初始化的各种数据
	 * @param serv 服务定义
	 * @param dataBean 数据信息
	 */
    protected void initInsertData(ServDefBean serv, Bean dataBean) {
	    String servId = serv.getId();
	    List<Bean> combineItems = serv.getCombineItems();
	    for (Bean item : combineItems) { //处理组合值字段
	        String itemCode = item.getStr("ITEM_CODE");
	        dataBean.set(itemCode, ServUtils.genCombineItem(servId, itemCode, 
	                item.getStr("ITEM_INPUT_CONFIG"), dataBean));
	    }
        if (dataBean.getId().length() == 0) { //处理主键字段
            if (dataBean.getStr(serv.get("SERV_KEYS")).length() > 0) {
                dataBean.setId(dataBean.getStr(serv.get("SERV_KEYS")));
            } else { // 根据Id判断是否自动赋UUID
                dataBean.set(serv.get("SERV_KEYS"), Lang.getUUID());
                dataBean.setId(dataBean.getStr(serv.get("SERV_KEYS")));
            }
        }
	}
}