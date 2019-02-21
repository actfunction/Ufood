package com.rh.core.wfe.auto.serv;

import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.auto.WfAuto;
import com.rh.core.wfe.auto.impl.WfAutoImpl;

public class WfAutoServ extends CommonServ {

	/**
	 * 启动流程
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean startup(ParamBean paramBean) {
		WfAuto auto = new WfAutoImpl();
		return auto.startup(paramBean);
	}
	
	/**
	 * 获取起草点
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getStartNode(ParamBean paramBean) {
		WfAuto auto = new WfAutoImpl();
		return auto.getStartNode(paramBean);
	}

	/**
	 * 送下一步
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean send(ParamBean paramBean) {
		WfAuto auto = new WfAutoImpl();
		return auto.send(paramBean);
	}

	/**
	 * 获取节点绑定人员
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getNodeBinder(ParamBean paramBean) {
		WfAuto auto = new WfAutoImpl();
		return auto.getNodeBinder(paramBean);
	}

	/**
	 * 获取连接线
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getLine(ParamBean paramBean) {
		WfAuto auto = new WfAutoImpl();
		return auto.getLine(paramBean);
	}

	/**
	 * 获取线条件
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getLineCond(ParamBean paramBean) {
		WfAuto auto = new WfAutoImpl();
		return auto.getLineCond(paramBean);
	}

	/**
	 * 返回上一步
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean goBack(ParamBean paramBean) {
		WfAuto auto = new WfAutoImpl();
		return auto.goBack(paramBean);
	}

	/**
	 * 结束流程
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean end(ParamBean paramBean) {
		WfAuto auto = new WfAutoImpl();
		return auto.end(paramBean);
	}

}
