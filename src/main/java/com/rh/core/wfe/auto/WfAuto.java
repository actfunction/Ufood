package com.rh.core.wfe.auto;

import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

public interface WfAuto {

	/**
	 * 启动流程
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean startup(ParamBean paramBean);

	/**
	 * 获取起草点
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getStartNode(ParamBean paramBean);

	/**
	 * 送下一步
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean send(ParamBean paramBean);

	/**
	 * 获取节点绑定人员
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getNodeBinder(ParamBean paramBean);

	/**
	 * 获取连接线
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getLine(ParamBean paramBean);

	/**
	 * 获取线条件
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getLineCond(ParamBean paramBean);

	/**
	 * 返回上一步
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean goBack(ParamBean paramBean);

	/**
	 * 结束流程
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean end(ParamBean paramBean);

}
