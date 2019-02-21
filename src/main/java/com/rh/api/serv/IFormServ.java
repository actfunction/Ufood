package com.rh.api.serv;

import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;

public interface IFormServ {

	public ApiOutBean getServDef(String servId);
	
	public ApiOutBean getDictDef(String dictId);
	
	public ApiOutBean saveServDef(ApiParamBean bean);
	
	public ApiOutBean getServList();
	
	public ApiOutBean createServ(ApiParamBean paramBean);
	
	public ApiOutBean getServerUrl(ApiParamBean paramBean);

}
