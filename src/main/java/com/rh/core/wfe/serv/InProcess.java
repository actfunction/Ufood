package com.rh.core.wfe.serv;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.db.ServDataDao;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 在办文件
 *
 */
public class InProcess extends CommonServ {
	/**
	 * 查询前添加查询条件
	 * 
	 * @param paramBean 
	 */
    public void beforeQuery(ParamBean paramBean) {
    	UserBean userBean = Context.getUserBean();
    	
    	//在办的，查的是实例表的  , 角色的过滤条件是部门 + 角色
    	StringBuilder strWhere = new StringBuilder(" and "); 
    	strWhere.append(ServDataDao.SERV_DATA_PROC_ID);
    	strWhere.append(" in (select distinct(PI_ID) from ");
    	strWhere.append(WfNodeInstDao.SY_WFE_NODE_INST_SERV);
    	strWhere.append(" where (TO_ROLE_ID in (");
    	strWhere.append(userBean.getRoleCodeQuotaStr());
    	strWhere.append(") and TO_DEPT_ID = '");
    	strWhere.append(userBean.getDeptCode());
    	strWhere.append("') and NODE_IF_RUNNING = ");
    	strWhere.append(WfeConstant.NODE_IS_RUNNING);
    	
    	strWhere.append(" union select distinct(PI_ID) from ");
    	strWhere.append(WfNodeInstDao.SY_WFE_NODE_INST_SERV);
    	strWhere.append(" where TO_USER_ID = '");
    	strWhere.append(userBean.getCode());
    	strWhere.append("' and NODE_IF_RUNNING = ");
    	strWhere.append(WfeConstant.NODE_IS_RUNNING);
    	strWhere.append(")");
    	
    	paramBean.set("_extWhere", strWhere.toString());
    }
    
    
}
