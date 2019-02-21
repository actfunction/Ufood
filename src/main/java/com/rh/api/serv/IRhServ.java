package com.rh.api.serv;

import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;

public interface IRhServ {

	/** 得到列表所需数据 */
	public ApiOutBean getServListParam(ApiParamBean paramBean);
	
	/** 模板预览时用到,得到服务定义信息 */
	public ApiOutBean getServTmpl(ApiParamBean paramBean);
}
