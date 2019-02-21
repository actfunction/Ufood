/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.util.var;

import java.util.Map;

/**
 * map实现变量的接口类。
 * 
 * @author wanglong
 * @version $Id$
 */
public class MapVar implements Var {
    
    /** map存储变量 */
    private Map<Object, Object> map;
    
    /**
     * 构建体方法
     * @param map 变量载体
     */
    public MapVar(Map<Object, Object> map) {
        this.map = map;
    }
    
    /**
     * 获取变量值
     * @param key 键值
     * @return 变量值
     */
    public String get(String key) {
        if (map.containsKey(key)) {
            return String.valueOf(map.get(key));
        } else {
            return key;
        }
    }
}
