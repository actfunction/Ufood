package com.rh.core.org.serv;

import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 
 * @author wanglong
 * 
 */
public class RoleServ extends CommonServ {
	private static final String ROLE_SCOPE_DEF = "ROLE_SCOPE_DEF";

	@Override
	protected void beforeSave(ParamBean paramBean) {
		super.beforeSave(paramBean);
		if (paramBean.isNotEmpty(ROLE_SCOPE_DEF)) { // 根据前台数据，合并角色显示范围
			String scopeDef = paramBean.getStr(ROLE_SCOPE_DEF);
			String[] scopes = scopeDef.split(",");

			int scopeVal = 0;

			for (String scope : scopes) {
				scopeVal += Integer.parseInt(scope);
			}

			if (scopeVal > 511) {
				scopeVal = 511;
			}

			paramBean.set("ROLE_SCOPE", scopeVal);
		}
	}

	@Override
	protected void afterByid(ParamBean paramBean, OutBean outBean) {
		super.afterByid(paramBean, outBean);

		int scope = outBean.getInt("ROLE_SCOPE");
		if (scope > 0) { // 根据合并的数据拆分出可供多选框反选的值。
			String scopeDef = "0";

			for (int i = 0; i < 10; i++) {
				int pos = (int) Math.pow(2, i);
				if ((scope & pos) > 0) {
					scopeDef += "," + pos;
				}
			}

			outBean.set(ROLE_SCOPE_DEF, scopeDef);
		}
	}

	@Override
	protected void beforeQuery(ParamBean paramBean) {
		super.beforeQuery(paramBean);

		// 如果是全部角色，则不按照级别过滤
		if (paramBean.getServId().equals("SY_ORG_ROLE_ALL")) {
			return;
		}

		if (paramBean.getList("_treeWhere").size() > 0) {
			return;
		}

		UserBean user = Context.getUserBean();
		if (!paramBean.getServId().equals("OA_ORG_ROLE")) {
			StringBuilder where = new StringBuilder();
			where.append(" and (S_PUBLIC = 1 or ROLE_DEPT = '" + user.getODeptCode() + "')");
			paramBean.setQueryExtWhere(where.toString());
		} else {
			StringBuilder where = new StringBuilder();
			String odeptCode = user.getODeptCode();
			where.append(
					" and (S_PUBLIC = 1 or BELONG_ODEPT in (select DEPT_CODE from SY_ORG_DEPT where DEPT_TYPE = 2 and instr(CODE_PATH,'"
							+ odeptCode + "') > 0))");
			paramBean.setQueryExtWhere(where.toString());
		}

	}

}
