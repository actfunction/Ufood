package com.rh.core.org.serv;

import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.UserStateBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.EncryptUtils;

/**
 * 
 * @author wanglong
 * 
 */
public class UserSelfInfoServ extends CommonServ {

	/**
	 * 个人信息功能 点击菜单弹出当前用户信息的卡片
	 * 
	 * @param paramBean
	 *            参数信息
	 * @return outBean
	 */
	public OutBean show(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		String url = Context.appStr(APP.CONTEXTPATH) + "/sy/base/view/stdCardView.jsp?sId=" + paramBean.getServId()
				+ "&pkCode=" + Context.getUserBean().getCode();
		// URL跳转
		outBean.setToDispatcher(url).setOk();
		return outBean;
	}

	/**
	 * 只能用SY_ORG_USER 服务来修改用户数据，便于用户监听。
	 * 
	 * @param paramBean
	 *            参数
	 * @return 操作结果
	 */
	@Override
	public OutBean save(ParamBean paramBean) {
		UserBean userBean = Context.getUserBean();
		// 缺省只能保存本人的信息
		paramBean.setId(userBean.getCode());
		paramBean.setServId(ServMgr.SY_ORG_USER);
		paramBean.setAct(ServMgr.ACT_SAVE);
		// yangjinyun:
		// 如果要修改密码，判断用户是否没有登录过，或者强制要求用户输入密码，否则不允许用户修改密码，如果要修改密码，必须先验证老密码是否正确。
		// 这段程序只是为了兼容前台某些情况不能提交旧密码情况，其实应该要求前台输入老密码，如果老密码正确才能修改。
		if (paramBean.isNotEmpty("USER_PASSWORD")) {
			UserStateBean userStateBean = UserMgr.getUserState(userBean.getCode());
			// 如果登录过，且不强制要求用户修改密码，则不允许用户修改密码。
			if (userStateBean != null && userStateBean.getInt("MODIFY_PASSWORD") != 1) {
				paramBean.remove("USER_PASSWORD");
			}
		}
		boolean modifyPassword = false;
		if (paramBean.isNotEmpty("USER_PASSWORD")) {
			modifyPassword = true;
		}

		OutBean outBean = ServMgr.act(paramBean);

		// 用户修改密码之后，修改UserState表数据。
		if (outBean.isOk() && modifyPassword) {
			saveUserState(userBean);
		}

		return outBean;
	}

	/**
	 * 修改UserState，把Modify_password字段的值改成2。
	 * 
	 * @param userBean
	 *            被修改的用户UserBean 对象
	 */
	private void saveUserState(UserBean userBean) {
		try {
			ParamBean userStateParam = new ParamBean(ServMgr.SY_ORG_USER_STATE, ServMgr.ACT_SAVE);
			userStateParam.set("MODIFY_PASSWORD", 2);
			userStateParam.setId(userBean.getCode());
			ServMgr.act(userStateParam);
		} catch (Exception e) {
			// 忽略错误
		}
	}

	/**
	 * 修改用户密码
	 * 
	 * @param paramBean
	 *            参数信息
	 * @return outBean
	 */
	public OutBean saveInfo(ParamBean paramBean) {
		boolean modifyPassword = false;
		OutBean outBean = new OutBean();
		String oldPassword = paramBean.getStr("OLD_PASSWORD"); // 输入的旧密码
		String newPassword = paramBean.getStr("USER_PASSWORD"); // 新密码
		UserBean userBean = Context.getUserBean();
		// 如果输入旧密码就进行加密判断正确性 否则直接保存修改的数据
		if (!newPassword.equals("")) {
			// 对输入的旧密码进行加密对比
			String enOldPswd = EncryptUtils.encrypt(oldPassword,
					Context.getSyConf("SY_USER_PASSWORD_ENCRYPT", EncryptUtils.DES));
			if (!enOldPswd.equals(userBean.getPassword())) {
				throw new TipException("输入的旧密码错误。");
			}
			paramBean.set("USER_PASSWORD", newPassword);
			modifyPassword = true;
		}
		paramBean.setId(userBean.getCode());
		paramBean.set("modifyPass", "yes");
		outBean = ServMgr.act(ServMgr.SY_ORG_USER, "save", paramBean);
		outBean.setOk();
		if (modifyPassword) {
			saveUserState(userBean);
		}
		return outBean;
	}

	/**
	 * @param paramBean
	 *            参数信息
	 * @param outBean
	 *            参数信息
	 */
	protected void afterSave(ParamBean paramBean, OutBean outBean) {
		if (outBean.isOk()) {
			UserMgr.clearSelfUserCache();
			ServDefBean userDef = ServUtils.getServDef(ServMgr.SY_ORG_USER); // 清除用户服务对应的字典
			userDef.clearDictCache();
		}
	}
}
