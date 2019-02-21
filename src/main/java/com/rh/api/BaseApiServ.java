package com.rh.api;

import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ServDao;

/**
 * API基类，实现基本返回信息的方法
 * 
 * @author wanglong
 *
 */
public class BaseApiServ {
	
	/***
	 * 获取用户信息，包括角色编码和用户TOKEN
	 * @param uid 用户编码
	 * @return 
	 */
	public ApiOutBean getUser(String uid) {
		ApiOutBean result = new ApiOutBean();
		String userToken = "";
		
		UserBean userBean = UserMgr.getUser(uid);
		Bean userState = ServDao.find("SY_ORG_USER_STATE", uid);
		// 如果不为null则取值
		if (null != userState) {
			userToken = userState.getStr("USER_TOKEN");
		}
		
		userBean.set("USER_TOKEN", userToken);
		userBean.set("ROLE_CODES", userBean.getRoleCodeStr());
		
		result.setData(userBean);
		
		return result;
	}
}
