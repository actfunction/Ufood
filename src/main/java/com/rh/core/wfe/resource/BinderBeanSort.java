package com.rh.core.wfe.resource;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;

/**
 * 绑定树排序
 * 
 */
public class BinderBeanSort {
	private static Log log = LogFactory.getLog(BinderBeanSort.class);

	private static int j = 0;

	/**
	 * 对同一level 的Bean排序
	 * 
	 * @param treeBeanList
	 *            树的节点 列表
	 */
	@SuppressWarnings("unchecked")
	public static void sortTheBindList(List<WfResTreeNode> treeBeanList) {
		ComparatorBean comparator = new ComparatorBean();

		Collections.sort(treeBeanList, comparator);
		j++;
		// 对子列表进行排序
		for (int i = 0; i < treeBeanList.size(); i++) {
			Bean beanTemp = (Bean) treeBeanList.get(i);
			
			log.debug("----" + j + "----" + beanTemp.getStr("SORT") + "----"
					+ beanTemp.getStr("NAME"));

			if (null != beanTemp.get("CHILD")) {
				sortTheBindList((List<WfResTreeNode>) beanTemp.get("CHILD"));
			}
		}
	}

}

/**
 * 
 *
 */
class ComparatorBean implements Comparator<Bean> {
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

		// 比较排序
		if (bean1.getInt("SORT") > bean2.getInt("SORT")) {
			return 1;
		}
		
		if (bean1.getInt("SORT") == bean2.getInt("SORT")) {
			return 0;
		}
		
		return -1;
	}
}
