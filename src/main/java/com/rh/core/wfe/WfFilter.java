package com.rh.core.wfe;

import com.rh.core.wfe.serv.WfOut;

/**
 * 表单定义
 * 
 */
public interface WfFilter {

	/**
	 * 
	 * @param wfAct
	 *            流程实例
	 * @param formButton
	 *            表单定义ACT 列表
	 */
	void doButtonFilter(WfAct wfAct, WfOut formButton);

}