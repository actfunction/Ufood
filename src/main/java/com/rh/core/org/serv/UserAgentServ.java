package com.rh.core.org.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Strings;

/**
 * 用户委托办理服务类
 * 
 * @author wanglong
 * 
 */
public class UserAgentServ extends CommonServ {

	/** 委托状态：1 进行中 */
	public static final int AGT_STATUS_RUNNING = 1;
	/** 委托状态：2 已结束 */
	public static final int AGT_STATUS_DONE = 2;

	/** 增减标志：add 增加 */
	private static final String RECURSIVE_TYPE_ADD = "add";
	/** 增减标志：delete 减少 */
	private static final String RECURSIVE_TYPE_DEL = "delete";

	/**
	 * 删除之前的拦截方法，由子类重载
	 * 
	 * @param paramBean
	 *            参数信息
	 */
	protected void beforeDelete(ParamBean paramBean) {
		List<Bean> dataList = paramBean.getDeleteDatas();
		for (Bean dataBean : dataList) {
			// 清除委托人设定
			if (dataBean.getInt("AGT_STATUS") == AGT_STATUS_RUNNING) {
				clearUserAgentState(dataBean.getStr("USER_CODE"), dataBean.getStr("TO_USER_CODE"));
			}
		}
	}

	/**
	 * 保存之前的操作
	 * 
	 * @param paramBean
	 *            传入的参数Bean
	 */
	@Override
	protected void beforeSave(ParamBean paramBean) {
		String servId = paramBean.getServId();
		Bean oldBean = paramBean.getSaveOldData(); // 原始数据
		Bean userAgent = paramBean.getSaveFullData(); // 更新后全数据
		if (userAgent.getStr("USER_CODE").equals(userAgent.getStr("TO_USER_CODE"))) {
			throw new TipException(Context.getSyMsg("SY_ORG_USER_AGT_SELF_ERROR")); // 不能设置自己为受委托人
		}
		if (paramBean.getId().length() == 0) { // 新增模式，要判断是否有未结束的工作委托
			SqlBean sql = new SqlBean().and("USER_CODE", paramBean.getStr("USER_CODE")).and("AGT_STATUS",
					AGT_STATUS_RUNNING);
			if (ServDao.count(servId, sql) > 0) { // 如果当前用户存在未结束的的工作，提示错误
				throw new TipException(Context.getSyMsg("SY_ORG_USER_AGT_EXIST"));
			}
			addUserAgentState(paramBean.getStr("USER_CODE"), paramBean.getStr("TO_USER_CODE"));
		} else if (paramBean.contains("TO_USER_CODE") && (oldBean.getInt("AGT_STATUS") == AGT_STATUS_RUNNING)) { // 运行状态下修改了被委托人
			// 清除上一个委托人设定
			clearUserAgentState(userAgent.getStr("USER_CODE"), oldBean.getStr("TO_USER_CODE"));
			// 添加新的委托人设定
			addUserAgentState(userAgent.getStr("USER_CODE"), paramBean.getStr("TO_USER_CODE"));
		}
	}

	/**
	 * 启动委托
	 * 
	 * @param paramBean
	 *            参数信息，要求有USER_CODE参数
	 * @return 结果
	 */
	public OutBean startAgent(ParamBean paramBean) {
		String servId = paramBean.getServId();
		OutBean outBean = new OutBean();
		if (paramBean.getId().length() > 0) {
			String userCode = paramBean.getStr("USER_CODE");
			String toUserCode = paramBean.getStr("TO_USER_CODE");
			paramBean.set("AGT_STATUS", AGT_STATUS_RUNNING).set("AGT_BEGIN_DATE", DateUtils.getDatetime())
					.set("AGT_END_DATE", "");
			ServDao.update(servId, paramBean);
			addUserAgentState(userCode, toUserCode);
		}
		outBean.setOk();
		return outBean;
	}

	/**
	 * 结束委托
	 * 
	 * @param paramBean
	 *            参数信息，要求有USER_CODE参数
	 * @return 结果
	 */
	public OutBean endAgent(ParamBean paramBean) {
		String servId = paramBean.getServId();
		OutBean outBean = new OutBean();
		String userCode = paramBean.getStr("USER_CODE");
		if (userCode.length() > 0) {
			Bean param = new Bean();
			param.set("USER_CODE", userCode).set("AGT_STATUS", AGT_STATUS_RUNNING);
			Bean userAgent = ServDao.find(servId, param);
			String toUserCode = userAgent.getStr("TO_USER_CODE");
			userAgent.set("AGT_STATUS", AGT_STATUS_DONE).set("AGT_END_DATE", DateUtils.getDatetime());
			ServDao.update(servId, userAgent);
			clearUserAgentState(userCode, toUserCode);
		}
		outBean.setOk();
		return outBean;
	}

	/**
	 * 添加用户委托信息
	 * 
	 * @param userCode
	 *            委托人编码
	 * @param toUserCode
	 *            被委托人编码
	 */
	private void addUserAgentState(String userCode, String toUserCode) {
		// 更新委托人状态
		Bean userState = new Bean();
		userState.set("USER_CODE", userCode);
		userState.set("USER_AGT_FLAG", Constant.YES); // 设置代理开始
		UserMgr.saveUserState(userState); // 更新
		userState = UserMgr.getUserState(userCode); // 获取最新的用户状态信息
		// 更新被委托人状态
		String subCodes = userState.getStr("SUB_CODES");
		if (Strings.containsValue(subCodes, toUserCode)) { // 不允许被委托人包含在已委托列表中
			throw new TipException(Context.getSyMsg("SY_ORG_USER_AGT_SELF_ERROR"));
		}
		// 增加被委托人的代理设定的用户编码
		subCodes = Strings.addValue(subCodes, userCode);
		recursiveUserAgent(toUserCode, subCodes, RECURSIVE_TYPE_ADD);
	}

	/**
	 * 清除用户委托状态
	 * 
	 * @param userCode
	 *            委托人编码
	 * @param toUserCode
	 *            被委托人编码
	 */
	private void clearUserAgentState(String userCode, String toUserCode) {
		// 更新委托人状态
		Bean userState = new Bean();
		userState.set("USER_CODE", userCode);
		userState.set("USER_AGT_FLAG", Constant.NO); // 设置代理结束
		UserMgr.saveUserState(userState); // 更新并获取最新的用户状态信息
		userState = UserMgr.getUserState(userCode); // 获取最新的用户状态信息
		// 更新被委托人状态，去掉委托人的用户编码
		Bean toUserState = UserMgr.getUserState(toUserCode);
		if (toUserState != null) {
			String subCodes = userState.getStr("SUB_CODES");
			subCodes = Strings.addValue(subCodes, userCode);
			recursiveUserAgent(toUserCode, subCodes, RECURSIVE_TYPE_DEL);
		}
	}

	/**
	 * 递归增减用户代理编码，确保递归调用多级委托
	 * 
	 * @param userCode
	 *            当前用户编码
	 * @param addCode
	 *            需要被增减的用户编码
	 * @param type
	 *            增减标志，add：增加；delete：减少
	 */
	private void recursiveUserAgent(String userCode, String addCode, String type) {
		Bean userState = UserMgr.getUserState(userCode);
		Bean data = new Bean();
		data.set("USER_CODE", userCode);
		String subCodes;
		if (userState != null) {
			subCodes = userState.getStr("SUB_CODES");
		} else {
			subCodes = "";
		}
		if (type.equals(RECURSIVE_TYPE_ADD)) { // 添加
			data.set("SUB_CODES", Strings.addValue(subCodes, addCode));
		} else { // 删除
			data.set("SUB_CODES", Strings.removeValue(subCodes, addCode));
		}
		UserMgr.saveUserState(data); // 更新用户状态
		// 当前用户也处于委托代理状态，递归处理增减用户编码
		if ((userState != null) && (userState.getInt("USER_AGT_FLAG") == Constant.YES_INT)) {
			Bean param = new Bean();
			param.set("USER_CODE", userCode).set("AGT_STATUS", AGT_STATUS_RUNNING);
			Bean userAgent = ServDao.find(ServMgr.SY_ORG_USER_AGENT, param);
			if (userAgent != null) {
				recursiveUserAgent(userAgent.getStr("TO_USER_CODE"), addCode, type);
			}
		}
	}
}
