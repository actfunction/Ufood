package com.rh.sup.wf;

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

import java.util.List;

/**
 * 司内督查办结扩展类
 * @author zhaosheng
 *
 */
public class BureauMainMergeWfBinder implements ExtendBinder{
	private static String BUREAU_STAFF = "OA_SUP_APPRO_BUREAU_HOST";

	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		ExtendBinderResult result = new ExtendBinderResult();
		
		//1.取得流程实例对象
		WfProcess process =  currentWfAct.getProcess();
	
		Bean servBean = process.getServInstBean();
		//获取服务的ID
		String servId = servBean.getId();
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("BUREAU_ID", servId);
		sqlBean.and("S_FLAG", Constant.YES);
		sqlBean.and("DEPT_TYPE", "1");
		//查询牵头承办人
		List<Bean> applyDeptList = ServDao.finds(BUREAU_STAFF, sqlBean);
		String userIds = SUUtils.getUserIds(applyDeptList);
		result.setUserIDs(userIds);
		result.setAutoSelect(true);
		return result;
	}

}
