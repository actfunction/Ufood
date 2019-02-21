package com.rh.core.base.db;

import java.util.List;

import com.rh.core.base.Bean;

/**
 * 生成query方法的回调处理。
 * 
 * @author wanglong
 * @version $Id$
 */
public abstract class QueryCallback {
    /**
     * 处理查询每行的回调方法。
     * @param columns   行定义列表，内部Bean包含NAME和TYPE属性
     * @param data  当前行数据
     */
	public abstract void call(List<Bean> columns, Bean data);
	
	/**
	 * 整体查询结束后通过回调方法提供列相关信息，无论是否有数据信息都执行此回调。
	 * @param count 此次查询的数据数（不是分页查询的总记录数）
	 * @param columns 列信息
	 */
	public void end(int count, List<Bean> columns) {	    
	}
}
