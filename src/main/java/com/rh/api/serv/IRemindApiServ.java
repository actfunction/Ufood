package com.rh.api.serv;

import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;

public interface IRemindApiServ {

	public ApiOutBean getRemind(Bean reqData);

	public ApiOutBean saveRemind(Bean reqData);
}
