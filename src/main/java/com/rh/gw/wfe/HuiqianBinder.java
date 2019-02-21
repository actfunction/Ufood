package com.rh.gw.wfe;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.org.UserBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;

public class HuiqianBinder implements ExtendBinder {

	
	/**
	 * 得到会签部门下相关角色的用户
	 */
	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {

		Bean gwBean = currentWfAct.getProcess().getServInstBean();// 获取表单数据
		String gwCopyTo = gwBean.getStr("GW_COSIGN_TO_CODE"); // 获取表单的多个会签部门
		gwCopyTo = gwCopyTo.replaceAll(",", "','");
		
		SqlExecutor se = Context.getExecutor();// 获取SqlExecutor对象，用于执行sql语句
		
		// 筛选数据：没有被删除的数据，，OA_SJWS角色的数据，，DEPT_CODE部门的数据
		String xbUserSql = "SELECT USER_CODE FROM SY_ORG_ROLE_USER_V WHERE S_FLAG = 1 and ROLE_CODE = 'R_GW_SJWS' "
							+ "and RU_ID in (SELECT RU_ID FROM SY_ORG_ROLE_USER WHERE DEPT_CODE in ('" + gwCopyTo+ "') "
							+ "or TDEPT_CODE in ('" + gwCopyTo + "')"
							+ "or ODEPT_CODE in('" + gwCopyTo+ "'))";
		
		List<Bean> xbUserSqlList = se.query(xbUserSql); // 执行SQL
//		ServDao.finds("", new ParamBean().setWhere("AND S_FLAG = 1 and ROLE_CODE = 'OA_SJWS' and DEPT_CODE in ('"
//				+ gwCopyTo + "')"))

		ExtendBinderResult result = new ExtendBinderResult();
		result.setAutoSelect(false);
		
		// 处理SQL返回的结果数据
		StringBuffer aUserIDs = new StringBuffer();// ID
		if (xbUserSqlList != null && xbUserSqlList.size() > 0) {
			for (Bean b : xbUserSqlList) {
				aUserIDs.append(b.getStr("USER_CODE")).append(",");
			}
			// 去掉最后一个逗号
			String userIds = "";
			if (!aUserIDs.toString().isEmpty()) {    //如果不为空
				userIds = aUserIDs.toString().substring(0, aUserIDs.length() - 1);
			}

			result.setUserIDs(userIds);
		} else {
			result.setUserIDs(null);
		}
        result.setReadOnly(false);
		return result;
	}
}
