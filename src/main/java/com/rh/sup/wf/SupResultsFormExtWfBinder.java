package com.rh.sup.wf;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;
import com.rh.sup.util.SUUtils;
import com.rh.sup.util.TodoUtils;

/**
 * 送交署内督查员，同时送交司局长
 * @author admin
 *
 */
public class SupResultsFormExtWfBinder implements ExtendBinder{
	//督查员角色
	private final static String ROLE_SNDCY="SUP001";
	//司局长角色
	private final static String ROLE_SJZ="SUP006";
	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		ExtendBinderResult result = new ExtendBinderResult();
		
		//1.取得流程实例对象
		WfProcess process =  currentWfAct.getProcess();
	
		Bean servBean = process.getServInstBean();
		//获取服务的ID
//		String servId = servBean.getId();
		TodoUtils util = new TodoUtils();
		SqlBean todoSqlBean = new SqlBean();
		todoSqlBean.and("TDEPT_CODE", doUser.getTDeptCode());
		todoSqlBean.and("ROLE_CODE", ROLE_SJZ);
		Bean bean = ServDao.find("SY_ORG_ROLE_USER", todoSqlBean);
		util.addTodo(servBean, process, bean.getStr("USER_CODE"));
		
		//获取处理人
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("ROLE_CODE", ROLE_SNDCY);
		List<Bean> list = ServDao.finds("SY_ORG_ROLE_USER", sqlBean);
		String userIds = SUUtils.getUserIds(list);
		result.setUserIDs(userIds);
		result.setAutoSelect(true);
		return result;
	}

}
