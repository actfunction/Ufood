package com.rh.core.wfe.util;

import java.util.Comparator;

import com.rh.core.base.Bean;

/**
 * 按钮根据 ACT_ORDER 排序 
 * @author wanglong
 *
 */
public class ComparatorBtnSort implements Comparator<Bean> {
	/**
	 * 实现接口的方法
	 * 
	 * @param arg0  比较的对象
	 * @param arg1 比较的对象
	 * @return 比较结果
	 */
	public int compare(Bean arg0, Bean arg1) {
		Bean bean1 = (Bean) arg0;
		Bean bean2 = (Bean) arg1;
		if (bean2 == null) {
		    return 1;
		}
		// 比较排序
		if (bean1.getInt("ACT_ORDER") > bean2.getInt("ACT_ORDER")) {
			return 1;
		}
		
		if (bean1.getInt("ACT_ORDER") == bean2.getInt("ACT_ORDER")) {
			return 0;
		}
		
		return -1;
	}
}