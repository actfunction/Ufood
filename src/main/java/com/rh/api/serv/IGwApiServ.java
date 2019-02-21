package com.rh.api.serv;

import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;

public interface IGwApiServ {

	public ApiOutBean getMaxNum(Bean reqData);

	public ApiOutBean cmRedHead(Bean reqData);

	public ApiOutBean getWfeDelayInfo(Bean reqData);
	
	public ApiOutBean saveDelayInfo(Bean reqData);
	
	public ApiOutBean getZhengwenList(Bean reqData);
}