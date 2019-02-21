package com.rh.api.impl;

import java.util.List;

import com.rh.api.BaseApiServ;
import com.rh.api.serv.IMenuApiServ;
import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.MenuServ;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.lang.ListHandler;

public class MenuServImpl extends BaseApiServ implements IMenuApiServ {

	@Override
	public ApiOutBean getUserMenu(final Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		Bean menuData = new Bean();
		UserBean userBean = (UserBean) Context.getUserBean();
		if (userBean == null) { // 用户必须登录才可以获取菜单
			return outBean;
		}
		// 先从缓存获取菜单
		List<Bean> menuTree = UserMgr.getCacheMenuList(userBean.getCode());
		if (reqData.isNotEmpty("PID")) { // 设定了动态取一部分菜单信息
			DictMgr.handleTree(menuTree, new ListHandler<Bean>() {
				public void handle(Bean data) {
					if (data.getStr("ID").equals(reqData.getStr("PID"))) { // 取当前节点下所有的子孙
						reqData.set(DictMgr.CHILD_NODE, data.getList(DictMgr.CHILD_NODE));
						reqData.set("PARENT_NODE", data);
					}
				}

			});
			menuTree = reqData.getList(DictMgr.CHILD_NODE);
		}
		if (!reqData.contains(MenuServ.LEFTMENU)) {
			menuData.set(MenuServ.TOPMENU, menuTree);
		} else {
			menuData.set(MenuServ.LEFTMENU, menuTree);
		}
		menuData.set("PARENT_NODE", reqData.getBean("PARENT_NODE"));
		outBean.setData(menuData);
		return outBean;
	}
}
