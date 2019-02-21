package com.rh.api.serv;

import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;

public interface IFormInfoApiServ {

	public ApiOutBean getServDef(String servId);

	public ApiOutBean getServDefAndData2(Bean reqData);

	public ApiOutBean getDict(String dictCode);

	public ApiOutBean getDictTreeData(String dictCode, String pid);

	public ApiOutBean save(Bean reqData);

	public ApiOutBean delete(Bean reqData);

	public ApiOutBean saveResultData(Bean reqData);

	public ApiOutBean copyZhengwen(Bean reqData);

}
