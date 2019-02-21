package com.rh.fm;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDef;

public class FmServDef extends ServDef{
	
	public void beforeSave(ParamBean paramBean) {
		
		// 强制编码大写
		if (!paramBean.isEmpty("SERV_ID")) {
			paramBean.set("SERV_ID", paramBean.getStr("SERV_ID").toUpperCase());
		}
		Bean oldBean = paramBean.getSaveOldData();
		// 项目模式下，修改操作，修改内置服务，则标记为混合服务
		if (!paramBean.getAddFlag() && oldBean.getInt("PRO_FLAG") == PRO_FLAG_INNER) {
			paramBean.set("PRO_FLAG", PRO_FLAG_MIX); // 产品标记为混合服务
		}
		
		if(!(paramBean.getAddFlag() == false && paramBean.isEmpty("SERV_ID"))){
			String servId = paramBean.getStr("SERV_ID");
			paramBean.set("SERV_SQL_WHERE", " and APPLY_CATALOG = '"+servId+"'");
		}
	}
}
