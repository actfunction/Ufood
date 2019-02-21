package com.rh.core.util.var;

/**
 * 变量需要实现的接口类
 * 
 * @author wanglong
 * @version $Id$
 */
public interface Var {
	/**
	 * 获取变量值
	 * @param key 变量键
	 * @return 变量值
	 */
	String get(String key);
}
