package com.rh.fm;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;

public class AuthServ extends CommonServ{
	
	public OutBean getAuthData(ParamBean paramBean){
		OutBean outBean = new OutBean();
		UserBean user = Context.getUserBean();
		String userOdept = user.getODeptCode();
		String userPath = user.getCodePath();
		
		List<Bean> list = ServDao.finds("FM_PROC_AUTH", "and PUBLIC_FLAG = 1 and instr('"+userPath+"',CODE_PATH) > 0");
		for (Bean bean : list) {
			String servId = bean.getStr("REG_PROC_SERVID");
			if(!servId.isEmpty()){
				List<Bean> tjList = ServDao.finds("FM_PROC_DATA_TJ", " and 	SERV_ID = '"+servId+"' and ODEPT_CODE='"+userOdept+"'");
				if(tjList.size() > 0){
					bean.set("USE_NUMS", tjList.get(0).getInt("USE_NUMS"));
					bean.set("AVG_TIME", tjList.get(0).getInt("AVG_TIME"));
					bean.set("OVER_NUMS", tjList.get(0).getInt("OVER_NUMS"));
					bean.set("TOTAL_TIME", tjList.get(0).getInt("TOTAL_TIME"));
				}
			}
		}
		outBean.setData(list);
		return outBean;
	}
}
