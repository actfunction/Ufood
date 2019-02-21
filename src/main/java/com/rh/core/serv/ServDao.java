/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.db.QueryCallback;
import com.rh.core.base.db.RowHandler;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;

/**
 * 面向数据表对象的数据载体，可以存放各种数据库的内容
 * @author wanglong
 * @version $Id$
 * */

public class ServDao {

    /**
     * 私有化空构建体方法，避免实例化
     */
    private ServDao() {
    }

    /**
     * 保存数据，自动根据是否设定了主键来判断是添加还是修改保存
     * @param servId 服务主键
     * @param dataBean 数据信息
     * @return 保存后的数据
     */
    public static Bean save(String servId, Bean dataBean) {
        if (dataBean.getId().length() > 0) {
            return update(servId, dataBean);
        } else {
            return create(servId, dataBean);
        }
    }
    
    /**
     * 直接创建一个servBean并插入到数据库
     * @param servId 服务主键
     * @param dataBean 数据信息
     * @return 已经插入的servBean
     */
    public static Bean create(String servId, Bean dataBean) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        Bean createBean = dataBean.copyOf(); //复制一份确保参数信息不会被修改
        List<Object> preValue = new ArrayList<Object>(dataBean.size());
        String psql = Transaction.getBuilder().insert(servDef, createBean, preValue);
        //进行唯一组约束的判断
        String uniqueStr = ServUtils.checkUniqueExists(servDef, createBean, true);
        if (uniqueStr != null) {
            throw new TipException(Context.getSyMsg("SY_SAVE_UNIQUE_EXISTS", uniqueStr));
        }
        boolean result = Transaction.getExecutor().execute(psql, preValue) > 0 ? true : false;
        if (result) {
            createBean.setId(createBean.getStr(servDef.getPKey())); //设置主键字段
            return createBean;
        } else {
            return null;
        }
    }
    
    /**
     * 根据Bean的信息执行更新操作
     * @param servId 服务主键
     * @param dataBean 数据信息
     * @return 已经插入的servBean
     */
    public static Bean update(String servId, Bean dataBean) {
        ServDefBean servDef = ServUtils.getServDef(servId);

        Bean updateBean = dataBean.copyOf(); //复制一份参数确保原参数不被修改
        updateBean.set("S_MTIME", DateUtils.getDatetimeTS());
        List<Object> preValue = getPreValueClone(updateBean);
        String psql = Transaction.getBuilder().update(servDef, updateBean, preValue);
        boolean result = Transaction.getExecutor().execute(psql, preValue) > 0 ? true : false;
        if (result) {
            String key = servDef.getPKey();
            if (updateBean.contains(key)) { //设置主键字段
                updateBean.setId(updateBean.getStr(key));
            }
            servDef.clearDataCache(updateBean.getId()); //清除缓存
            return updateBean;
        } else {
            return null;
        }
    }

    /**
     * 基于主键删除数据，根据是否启用假删除设置，如果启用，假删除当前数据，没有启用则真删除。
     * @param servId 服务主键
     * @param id 数据主键
     * @return 删除结果
     */
    public static boolean delete(String servId, String id) {
        return delete(servId, new Bean(id));
    }
    
    /**
     * 根据是否启用假删除设置，如果启用，假删除当前数据，没有启用则真删除。
     * @param servId 服务主键
     * @param whereBean 数据信息
     * @return 删除结果
     */
    public static boolean delete(String servId, Bean whereBean) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        boolean trueDel = true;
        if (servDef.hasFalseDelete()) {
            trueDel = false; //启用假删除
        }
        List<Object> preValue = getPreValueClone(whereBean);
        String psql = Transaction.getBuilder().delete(servDef, whereBean, preValue, trueDel);
        boolean result = Transaction.getExecutor().execute(psql, preValue) > 0 ? true : false;
        if (result && !whereBean.getId().isEmpty()) { //基于主键的删除
            servDef.clearDataCache(whereBean.getId()); //清除缓存
        }
        return result;
    }
    
    /**
     * 基于主键真删除当前数据
     * @param servId 服务主键
     * @param id 数据主键
     * @return 删除结果
     */
    public static boolean destroy(String servId, String id) {
        return destroy(servId, new Bean(id));
    }
    
    /**
     * 真删除当前数据
     * @param servId 服务主键
     * @param whereBean 数据信息
     * @return 删除结果
     */
    public static boolean destroy(String servId, Bean whereBean) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        List<Object> preValue = getPreValueClone(whereBean);
        String psql = Transaction.getBuilder().delete(servDef, whereBean, preValue, true);
        boolean result = Transaction.getExecutor().execute(psql, preValue) > 0 ? true : false;
        if (result && !whereBean.getId().isEmpty()) { //基于主键删除
            servDef.clearDataCache(whereBean.getId()); //清除缓存
        }
        return result;
    }    

    /**
     * 批量插入数据库
     * @param servId 服务主键
     * @param beans 数据信息
     * @return 成功插入的数量
     */
    public static int creates(String servId, List<Bean> beans) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        int count = 0;
        String psql = null;
        List<List<Object>> params = new ArrayList<List<Object>>(beans.size());
        for (Bean dataBean : beans) {
            List<Object> param = new ArrayList<Object>(dataBean.size());
            psql = Transaction.getBuilder().insert(servDef, dataBean, param);
            params.add(param);
        }
        if (psql != null) { // 多个对象执行批处理方法
            count = Transaction.getExecutor().executeBatch(psql, params);
        }
        return count;
    }
    
    /**
     * 根据主键获取数据记录，不包含关联子数据
     * @param servId 服务Id
     * @param dataId 数据主键，多主键采用数字方式提供参数
     * @return 数据记录,如果不存在则返回null
     */
    public static Bean find(String servId, String dataId) {
        return find(servId, dataId, false);
    }
    
    /**
     * 根据主键获取数据记录，多主键按照顺序提供数组
     * @param servId 服务Id
     * @param dataId 数据主键，多主键采用数字方式提供参数
     * @param withLinks 是否包含关联数据，true:包含关联定义所有子；false：不包含关联数据
     * @return 数据记录,如果不存在则返回null
     */
    public static Bean find(String servId, String dataId, boolean withLinks) {
        return find(servId, new Bean(dataId).set(Constant.PARAM_LINK_FLAG, withLinks));
    }
    
    /**
     * 根据主键获取数据记录，多主键按照顺序提供数组
     * @param servId 服务Id
     * @param paramBean 参数信息，可以重载SELECT、TABLE、WHERE，如果没有重载WHERE，要求必须有ID，
     *                  另外可以设定是否包含所有子孙数据列表
     * @return 数据记录,如果不存在则返回null
     */
    public static Bean find(String servId, Bean paramBean) {
        Bean result;
        boolean withLinks = paramBean.getBoolean(Constant.PARAM_LINK_FLAG);
        ServDefBean servDef = ServUtils.getServDef(servId);
        if (!withLinks && servDef.hasCache()) {
            String dataId = paramBean.getId();
            result = (Bean) servDef.getDataCache(dataId);
        } else {
            result = null;
        }
        if (result == null) {
            List<Object> preValue = getPreValueClone(paramBean);
            String psql = Transaction.getBuilder().select(servDef, paramBean, preValue);
            if (!paramBean.getId().isEmpty()) { //明确了基于主键查询
                result = Transaction.getExecutor().queryById(psql, preValue);
            } else { //非主键查询限定只返回一条
                result = Transaction.getExecutor().queryOne(psql, preValue);
            }
            if (result != null) {
                if (withLinks) {
                    //获取关联数据，并设置到结果对象中
                    LinkedHashMap<String, Bean> links = servDef.getAllLinks();
                    for (String key : links.keySet()) {
                        Bean link = links.get(key);
                        if (link.getInt("LINK_SHOW_TYPE") == ServConstant.LINK_SHOW_TYPE_URL) { //url关联不获取数据
                            continue;
                        }
                        if (!link.isEmpty("LINK_EXPRESSION")) { //处理表达式，禁止不符合规则的数据获取
                            if (!Lang.isTrueScript(ServUtils.replaceSysAndData(link.getStr("LINK_EXPRESSION"), 
                                    result))) { //表达式不为true，则不允许获取数据
                                continue;
                            }
                        }
                        List<Bean> dataList = ServUtils.getLinkDataList(servId, link, result, 1);
                        result.set(link.getStr("LINK_SERV_ID"), dataList);
                    }
                } else if (servDef.hasCache()) {
                    servDef.setDataCache(result.getId(), result); //设置缓存
                }
            }
        }
        return result;
    }
    
    /**
     * 根据where条件获取数据记录集
     * @param servId 服务Id
     * @param where and起始的where条件语句
     * @return 数据记录集
     */
    public static List<Bean> finds(String servId, String where) {
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, where);
        return ServDao.finds(servId, paramBean);
    }
    
    /**
     * 根据where条件获取数据记录集，支持3级以内的级联查询，需要指定PARAM_LINK_FLAG为true
     * @param servId 服务Id
     * @param paramBean 参数信息
     * @return 数据记录集
     */
    public static List<Bean> finds(final String servId, Bean paramBean) {
        QueryCallback qc = null;
        final ServDefBean serv = ServUtils.getServDef(servId);
        final int linkLevel = paramBean.getInt(Constant.PARAM_LINK_LEVEL);
        if (paramBean.getBoolean(Constant.PARAM_LINK_FLAG) && linkLevel < 3) { //设定了级联查找，向下2层级内获取关联数据
            qc = new QueryCallback() {
                public void call(List<Bean> columns, Bean data) {
                    //获取关联数据，并设置到结果对象中
                    LinkedHashMap<String, Bean> links = serv.getAllLinks();
                    for (String key : links.keySet()) {
                        Bean link = links.get(key);
                        if (link.getInt("LINK_SHOW_TYPE") == ServConstant.LINK_SHOW_TYPE_URL) { //url关联不获取数据
                            continue;
                        }
                        if (!link.isEmpty("LINK_EXPRESSION")) { //处理表达式，禁止不符合规则的数据获取
                            if (!Lang.isTrueScript(ServUtils.replaceSysAndData(link.getStr("LINK_EXPRESSION"), 
                                    data))) { //表达式不为true，则不允许获取数据
                                continue;
                            }
                        }
                        List<Bean> dataList = ServUtils.getLinkDataList(servId, link, data, linkLevel);
                        data.set(link.getStr("LINK_SERV_ID"), dataList);
                    }
                } //end call
            };
        }
        return finds(servId, paramBean, qc);
    }
    
    /**
     * 根据where条件获取数据记录集
     * @param servId 服务Id
     * @param paramBean 参数信息
     * @param qc 回调方法
     * @return 数据记录集
     */
    public static List<Bean> finds(String servId, Bean paramBean, QueryCallback qc) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        List<Bean> result;
        List<Object> preValue = getPreValueClone(paramBean);
        String psql = Transaction.getBuilder().select(servDef, paramBean, preValue);
        int rowNum = paramBean.get(Constant.PARAM_ROWNUM, -1);  //兼容以前方法，不建议使用此参数
        rowNum = paramBean.get(Constant.PAGE_SHOWNUM, rowNum);
        int page = paramBean.get(Constant.PAGE_NOWPAGE, 1);
        int start = (page - 1) * rowNum + 1;
        result = Transaction.getExecutor().query(psql, start, rowNum, preValue, qc);
        return result;
    }
    
    /**
     * 根据where条件获处理数据记录集
     * @param servId 服务Id
     * @param paramBean 参数信息
     * @param rowHandler 行处理器
     */
    public static void findsCall(String servId, Bean paramBean, RowHandler rowHandler) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        List<Object> preValue = getPreValueClone(paramBean);
        String psql = Transaction.getBuilder().select(servDef, paramBean, preValue);
        int rowNum = paramBean.get(Constant.PARAM_ROWNUM, -1);  //兼容以前方法，不建议使用此参数
        rowNum = paramBean.get(Constant.PAGE_SHOWNUM, rowNum);
        int page = paramBean.get(Constant.PAGE_NOWPAGE, 1);
        int start = (page - 1) * rowNum + 1;
        Transaction.getExecutor().queryCall(psql, start, rowNum, preValue, rowHandler);
    }
    
    /**
     * 根据paramBean参数组装的where条件获取数据的数量
     * @param servId 服务Id
     * @param paramBean 参数信息
     * @return 符合条件的数据数量
     */
    public static int count(String servId, Bean paramBean) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        paramBean.set(Constant.PARAM_SELECT, " count(*) COUNT_ ");
        List<Object> preValue = getPreValueClone(paramBean);
        String psql = Transaction.getBuilder().select(servDef, paramBean, preValue);
        return Transaction.getExecutor().count(psql, preValue);
    }
    
    /**
     * 根据参数进行删除处理
     * @param servId 服务Id
     * @param whereBean 参数信息
     * @return 数据记录集
     */
    public static int deletes(String servId, Bean whereBean) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        boolean trueDel = true;
        if (servDef.hasFalseDelete()) {
            trueDel = false; //启用假删除
        }
        List<Object> preValue = getPreValueClone(whereBean);
        String psql = Transaction.getBuilder().delete(servDef, whereBean, preValue, trueDel);
        int  count = Transaction.getExecutor().execute(psql, preValue);
        if (count > 0) { //清除缓存
            servDef.clearDataCache();
        }
        return count;
    }
    
    /**
     * 根据参数进行真删除处理
     * @param servId 服务Id
     * @param whereBean 参数信息
     * @return 数据记录集
     */
    public static int destroys(String servId, Bean whereBean) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        List<Object> preValue = getPreValueClone(whereBean);
        String psql = Transaction.getBuilder().delete(servDef, whereBean, preValue, true);
        int count = Transaction.getExecutor().execute(psql, preValue);
        if (count > 0) { //清除缓存
            servDef.clearDataCache();
        }
        return count;
    }
    
    /**
     * 根据参数进行真删除处理
     * @param servId 服务Id
     * @param setBean 更新数据项的参数信息
     * @param whereBean 过滤条件数据项参数信息
     * @return 数据记录集
     */
    public static int updates(String servId, Bean setBean, Bean whereBean) {
        ServDefBean servDef = ServUtils.getServDef(servId);
        List<Object> preValue = getPreValueClone(whereBean);
        String psql = Transaction.getBuilder().update(servDef, setBean, whereBean, preValue);
        int count = Transaction.getExecutor().execute(psql, preValue);
        if (count > 0) { //清除缓存
            servDef.clearDataCache();
        }
        return count;
    }
    
    /**
     * 获取参数中的preValue设定，为了避免修改参数，会clone一份新的数据，如果不存在，则返回空List
     * @param dataBean 参数信息
     * @return preValue设定
     */
    @SuppressWarnings("unchecked")
    private static List<Object> getPreValueClone(Bean dataBean) {
        List<Object> preValue; //处理prevalue,存在就复制一份
        if (dataBean.contains(Constant.PARAM_PRE_VALUES)) {
            preValue = (List<Object>) ((ArrayList<Object>) dataBean.get(Constant.PARAM_PRE_VALUES)).clone();
        } else {
            preValue = new ArrayList<Object>();
        }
        return preValue;
    }
}
