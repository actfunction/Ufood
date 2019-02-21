package com.rh.core.wfe.db;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;

/**
 * 节点定义 操作类
 *
 */
public class WfNodeActDao {
	/**
	 * 工作流节点操作 服务 code
	 */
	public static final String SY_WFE_NODE_ACT_SERV = "SY_WFE_NODE_ACT";
	
	/**
	 * 
	 * @param procCode 流程编码
	 * @return 操作列表
	 */
	public static List<Bean> findNodeActList(String procCode) {
		Bean paramBean = new Bean();
		paramBean.set("PROC_CODE", procCode);
		
		return ServDao.finds(SY_WFE_NODE_ACT_SERV, paramBean);
	}
	
	/**
	 * 
	 * @param nodeActBean 节点操作
	 * @return 节点操作Bean
	 */
	public static Bean saveNodeAct(Bean nodeActBean) {
		Bean aNodeActBean = ServDao.create(SY_WFE_NODE_ACT_SERV, nodeActBean);

		return aNodeActBean;
	}
}
