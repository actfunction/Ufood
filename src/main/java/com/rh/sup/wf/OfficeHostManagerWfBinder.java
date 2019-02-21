package com.rh.sup.wf;

import java.util.List;
import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;

import com.rh.sup.util.SUUtils;

/**
 * 提交合并到牵头主办单位节点扩展类
 * @author admin
 */
public class OfficeHostManagerWfBinder implements ExtendBinder{
	private static String SUP_OFFICE_DEPT = "OA_SUP_APPRO_OFFICE_HOST";
	
	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		ExtendBinderResult result = new ExtendBinderResult();
		//1.取得流程实例对象
		WfProcess process = currentWfAct.getProcess();
		Bean servbean = process.getServInstBean();
		Bean nodeBean = currentWfAct.getNodeInstBean();

		//2.获取服务的ID
		String servId = servbean.getId();
		if("N5".equals(nodeBean.getStr("NODE_CODE")) || "N52".equals(nodeBean.getStr("NODE_CODE"))){
			Bean userBean = ServDao.find("SY_ORG_USER", new SqlBean().and("USER_CODE", nodeBean.getStr("TO_USER_ID")));
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects("DEPT_CODE,D_USER_CODE,C_USER_CODE,DEPT_TYPE");
			//取到司内督查员所在部门，设置成where条件
			sqlBean.and("OFFICE_ID", servId);
			sqlBean.and("DEPT_CODE", userBean.getStr("TDEPT_CODE"));
			//查询出当前需要的处理人
			List<Bean> userList = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
			String userIds = SUUtils.getUserIdsDc(userList);
			
			//设置当前节点的处理人
			result.setUserIDs(userIds);
			result.setAutoSelect(true);
		}else{
			//3.查询当前主办单位和牵头主办单位的督查员
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("OFFICE_ID", servId);
			sqlBean.and("S_FLAG", Constant.YES);
			sqlBean.and("DEPT_TYPE", "1");//1即为主办单位
			List<Bean> applyDeptList = ServDao.finds(SUP_OFFICE_DEPT, sqlBean);
			
			//处理list拿到督查员ID
			String userIds = SUUtils.getUserIdsDc(applyDeptList);
			
			//4.设置当前节点的处理人
			result.setUserIDs(userIds);
			result.setAutoSelect(true);
		}
		return result;
	}
}
