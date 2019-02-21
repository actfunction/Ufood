package com.rh.gw.serv;

import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

import com.rh.gw.util.GwExtTabUtils;

/**
 * 业务发文特派办扩展类
 * 
 * @author kfzx-linll
 */
public class YwfwTpbServ extends GwExtServ {

	/**
	 * 根据前后页签的不同来删除数据库
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean deleteRalateTab(ParamBean paramBean) {
		GwExtTabUtils gwUtil = new GwExtTabUtils();
		return gwUtil.deleteRalateTab(paramBean);
	}

	
	/**
	 * 根据流程实例ID和节点ID获得自定义变量
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getTabs(ParamBean paramBean) {
		GwExtTabUtils gwUtil = new GwExtTabUtils();
		return gwUtil.getTabs(paramBean);
	}
}
