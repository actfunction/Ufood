/*
 * Copyright (c) 2012 Ruaho All rights reserved.
 */
package com.rh.core.comm;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.THREAD;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;


/**
 * 配置管理类
 * @author wanglong
 * 
 */
public class ConfMgr {
    /** 公共配置字典编码 */
    public static final String SY_COMM_CONFIG_PUBLIC = "SY_COMM_CONFIG_PUBLIC";
    /** 私有配置字典编码 */
    public static final String SY_COMM_CONFIG = "SY_COMM_CONFIG";
    
    /**
     * 获取系统配置
     * @param key 配置key
     * @param def 默认值
     * @return 指定key的value
     */
    public static boolean getConf(String key, boolean def) {
        String value = getConf(key);
        if (value == null) {
            return def;
        } else {
            return Boolean.parseBoolean(value);
        }
    }
    
    /**
     * 获取系统配置
     * @param key 配置key
     * @param def 默认值
     * @return 指定key的value
     */
    public static int getConf(String key, int def) {
        String value = getConf(key);
        if (value == null) {
            return def;
        } else {
            return Integer.parseInt(value);
        }
    }
    
    /**
     * 获取系统配置
     * @param key 配置key
     * @param def 默认值
     * @return 指定key的value
     */
    public static String getConf(String key, String def) {
        String value = getConf(key);
        if (value == null || value.length() == 0) {
            value =  def;
        }
        return value;
    }
    
    
    
    /**
     * 获取系统配置
     * @param key 配置key
     * @return 指定key的value
     */
    private static String getConf(String key) {
        String value = null;
        if (Context.appBoolean("MOBILE")) { // 移动版优先取前缀为MB_的配置
            value = getConfVal("MB_" + key);
        }
        
        if (value == null) { // 没取到则取不带前缀的配置
            value = getConfVal(key);
        }
        return value;
    }
    
    /**
     * 获取系统配置
     * @param key 配置key
     * @return 指定key的value
     */
    private static String getConfVal(String key) {
        String value = null;
        String extWhere = null;
        if (!Context.isEmpytyThread(DictMgr.THREAD_DICT_EXT_WHERE)) { //忽略自定义字典过滤条件
            extWhere = Context.getThreadStr(DictMgr.THREAD_DICT_EXT_WHERE);
            Context.removeThread(DictMgr.THREAD_DICT_EXT_WHERE);
        }
        Bean item;
        if (Context.getThreadStr(THREAD.CMPYCODE).length() > 0 
                && DictMgr.contains(SY_COMM_CONFIG, key)) { //如果存在私有公司先判断是否在私有字典中
            item = DictMgr.getItem(SY_COMM_CONFIG, key);
            if (item != null && (item.getInt("FLAG") != Constant.NO_INT)) {
                value = item.getStr("NAME");
            }
        }
        if (value == null && DictMgr.contains(SY_COMM_CONFIG_PUBLIC, key)) { //再判断是否在共有字典中
            item = DictMgr.getItem(SY_COMM_CONFIG_PUBLIC, key);
            if (item != null && (item.getInt("FLAG") != Constant.NO_INT)) {
                value = item.getStr("NAME");
            }
        }
        if (extWhere != null) { //恢复字典自定义过滤条件环境变量
            Context.setThread(DictMgr.THREAD_DICT_EXT_WHERE, extWhere);
        }
        return value;
    }
}
