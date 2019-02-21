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

public class MainApplyDeptManagerExtWfBinder implements ExtendBinder{
	private static String SU_APPLY_DEPT = "SU_APPLY_DEPT";

	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		ExtendBinderResult result = new ExtendBinderResult();
		
		//1.取得流程实例对象
		WfProcess process =  currentWfAct.getProcess();
	
		Bean servBean = process.getServInstBean();
		//获取服务的ID
		String servId = servBean.getId();
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("APPLY_ID", servId);
		sqlBean.and("S_FLAG", Constant.YES);
		sqlBean.and("APPLY_TYPE", "主办单位");
		//查询牵头主办单位督查员
		List<Bean> applyDeptList = ServDao.finds(SU_APPLY_DEPT, sqlBean);
		String userIds = SUUtils.getUserIds(applyDeptList);
		result.setUserIDs(userIds);
		result.setAutoSelect(true);
		return result;
	}

}
