package com.rh.core.org.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.org.UserStateBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.bean.PageBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;

/**
 * 在线用户服务类
 * 
 * @author wanglong
 * 
 */
public class OnlineUserServ extends CommonServ {

	/**
	 * 列出在线用户列表
	 * 
	 * @param paramBean
	 *            参数信息
	 * @return 输出信息
	 */
	public OutBean query(ParamBean paramBean) {
		String servId = paramBean.getServId();
		ServDefBean servDef = ServUtils.getServDef(servId);
		OutBean outBean = new OutBean();

		PageBean pageBean = paramBean.getQueryPage();

		List<UserStateBean> list = OnlineUserMgr.getOnlineUserList(pageBean.getOffset(), 0);

		int size = OnlineUserMgr.getSize();
		List<Bean> rtnList = new ArrayList<Bean>(size);
		for (UserStateBean userState : list) {
			UserBean userBean = UserMgr.getUser(userState.getStr("USER_CODE"));
			if (userState.isEmpty("USER_LAST_IP")) { // IP为空不在在线用户中
				continue;
			}
			Bean user = new Bean(userState.getId()).set("SID", userState.getId());
			user.set("USER_NAME", userBean.getName()).set("DEPT_NAME", userBean.getDeptName())
					.set("TDEPT_NAME", userBean.getTDeptName()).set("ODEPT_NAME", userBean.getODeptName())
					.set("USER_LOGIN_NAME", userBean.getLoginName()).set("USER_CODE", userBean.getId())
					.set("USER_LAST_LOGIN", userState.getStr("USER_LAST_LOGIN"))
					.set("IP_ADDRESS", userState.getStr("USER_LAST_IP")).set("ACT_CODE", userState.getStr("ACT_CODE"))
					.set("DATA_ID", userState.getStr("DATA_ID"))
					.set("SERV_ID", DictMgr.getFullName("SY_SERV", userState.getStr("SERV_ID")));
			rtnList.add(user);
		}
		outBean.setData(rtnList).setCols(servDef.getAllItems());

		// 增加分页对象
		// PageBean pageBean = new PageBean();
		pageBean.setAllNum(size);
		outBean.setPage(pageBean);

		return outBean;
	}

	/**
	 * 强制选定的在线用户退出
	 * 
	 * @param paramBean
	 *            参数信息
	 * @return 输出信息
	 */
	public OutBean kickOff(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		String[] ids = paramBean.getId().split(Constant.SEPARATOR);
		for (String id : ids) {
			Context.clearOnlineUser(id); // 清除在线用户
		}
		outBean.setOk();
		return outBean;
	}
}
