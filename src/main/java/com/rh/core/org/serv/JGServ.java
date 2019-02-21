package com.rh.core.org.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.comm.CacheMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

public class JGServ extends CommonServ {

	@Override
	protected void afterSave(ParamBean paramBean, OutBean outBean) {
		CacheMgr.getInstance().remove(outBean.getStr("USER_CODE"), "_JIANGANG_");
		CacheMgr.getInstance().remove(outBean.getStr("USER_CODE"), "SY_ORG_USER");
	}

	@Override
	protected void afterDelete(ParamBean paramBean, OutBean outBean) {
		List<Bean> delList = outBean.getDataList();

		for (Bean b : delList) {
			CacheMgr.getInstance().remove(b.getStr("USER_CODE"), "_JIANGANG_");
			CacheMgr.getInstance().remove(b.getStr("USER_CODE"), "SY_ORG_USER");
		}
	}
}
