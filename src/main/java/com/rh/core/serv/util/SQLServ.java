package com.rh.core.serv.util;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

public class SQLServ extends CommonServ {

	@Override
	protected void afterSave(ParamBean paramBean, OutBean outBean) {
		SQLTransUtil.SQL_MAP.remove(outBean.getStr("SQL_ID"));
	}

	@Override
	protected void afterDelete(ParamBean paramBean, OutBean outBean) {
		List<Bean> delList = outBean.getDataList();

		for (Bean b : delList) {
			SQLTransUtil.SQL_MAP.remove(b.getStr("SQL_ID"));
		}
	}

}
