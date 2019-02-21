package com.rh.gw.wfe;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;

/**
 * 
 * @author peixiujuan
 *
 */
public class FlowSelfBinder implements ExtendBinder {
	
	/**
     *拟稿人自流转
     *@param currentWfAct currentWfAct
     * @return userIds 
     **/    
    @Override
    public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
    	ExtendBinderResult result = new ExtendBinderResult();
    	
    	UserBean userBean = Context.getUserBean();//获取系统当前登录人信息。
    	
    	String userIds = getUserIds(userBean);
    	
    	result.setUserIDs(userIds);
    	
    	return result;
    }
    
    /**
     *拟稿人自流转根据user的角色获得该用户部门下角色相同的用户
     *@param UserBean
     * @return String userIds
     **/ 
    private String getUserIds(UserBean userBean) {		
		StringBuffer sql = new StringBuffer( "SELECT USER_CODE FROM SY_ORG_ROLE_USER_V ");
		String roleCode = userBean.getRoleCodeStr();//获取用户角色
		
		if(roleCode.contains("R_GW_SJFZR") || roleCode.contains("R_GW_SJZYFZR"))
		{   //若是司局负责人或司局主要负责人
			String tDeptCode = userBean.getTDeptCode();
			sql.append("WHERE TDEPT_CODE = '" + tDeptCode + "' ");
			sql.append("AND ROLE_CODE IN ('R_GW_SJFZR','R_GW_SJZYFZR')");
		}
		else
		{
			String deptCode = userBean.getDeptCode();
			sql.append("WHERE DEPT_CODE = '" + deptCode + "' ");
			if(roleCode.contains("R_GW_CSFZR") || roleCode.contains("R_GW_CSZYFZR")|| roleCode.contains("R_GW_MSCCSFZR"))
			{//若是处室负责人或处室主要负责人
				sql.append("AND ROLE_CODE IN ('R_GW_CSFZR','R_GW_CSZYFZR')");
			}
			else
			{//若不是是处室负责人或处室主要负责人
				sql.append("AND ROLE_CODE NOT IN ('R_GW_CSFZR','R_GW_CSZYFZR','R_GW_SJFZR','R_GW_SJZYFZR')");
			}
		}
		
		
		SqlExecutor executor = Transaction.getExecutor();
		List<Bean> query = executor.query(sql.toString());
		String userIds = "";
		for (Bean bean : query) {
			String userCode = bean.getStr("USER_CODE");
			userIds += userCode + ",";
		}
		if(userIds.length() > 0){
			userIds = userIds.substring(0,userIds.length() - 1);
		}
		return userIds;
	}
    
}