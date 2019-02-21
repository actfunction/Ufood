package com.rh.api.app;

import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;

public interface IFlowServ {

	/** 获得常用流程列表 */
	public ApiOutBean getCommList(ApiParamBean paramBean);

	/** 新增常用流程 */
	public ApiOutBean addComm(ApiParamBean paramBean);

	/** 删除常用流程 */
	public ApiOutBean delCommList(ApiParamBean paramBean);

	/** 获得流程类型列表 */
	public ApiOutBean getFlowTypeList(ApiParamBean paramBean);

	/** 获得流程列表 */
	public ApiOutBean getFlowList(ApiParamBean paramBean);
	
	/** 获得流程跟踪数据 */
	public ApiOutBean getWfeTrack(ApiParamBean paramBean);
}
