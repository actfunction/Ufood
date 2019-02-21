package com.rh.api.serv;

import java.util.List;

import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;
import com.rh.core.base.Bean;

public interface ISendApiServ {

	public ApiOutBean send(List<Bean> list, Bean param);

	public ApiOutBean sendRead(ApiParamBean paramBean);

	public ApiOutBean undo(Bean reqData);

	public ApiOutBean getDistrList(Bean reqData);

	public ApiOutBean getReadList(Bean reqData);
}
