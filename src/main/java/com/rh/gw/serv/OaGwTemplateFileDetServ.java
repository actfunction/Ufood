package com.rh.gw.serv;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

public class OaGwTemplateFileDetServ extends CommonServ{
	

	@Override
	protected OutBean add(ParamBean paramBean) {
		UserBean userBean = Context.getUserBean(); 
		String roleCodeQuotaStr = userBean.getRoleCodeQuotaStr();
		if(roleCodeQuotaStr.indexOf("'R_GW_GWXTGLY'")<0) {
			OutBean outBean = new OutBean();
			outBean.setError("只有管理员才可添加！");
			return outBean;
		}
		return super.add(paramBean);
	}
	
	
}
