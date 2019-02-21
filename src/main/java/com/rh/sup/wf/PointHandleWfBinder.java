package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;
import com.rh.sup.util.SUUtils;

import java.util.List;


/**
 * 要点类督查办理扩展类   跨节点退回、寻找原办理人
 * @author
 *
 */
public class PointHandleWfBinder implements ExtendBinder{
	// 省厅督察员的code
	private static String SHENGTI_ROLE_CODE = "SUP025";

	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser){

		ExtendBinderResult result = new ExtendBinderResult();

		// 获得当前节点实例的信息
		Bean nodeBean = currentWfAct.getNodeInstBean();
		String nodeCode = nodeBean.getStr("NODE_CODE");
		// 如果是由"N24" 督查处审批办理情况  发过来的待办需要找上一节点原办理人
		if ("N36".equals(nodeCode)){
			// 获得上一节点 的 id
			// 上一节点实例
			Bean preNodeBean = findPreNodeBean(nodeBean);
			String doneUserId = preNodeBean.getStr("DONE_USER_ID");
			result.setUserIDs(doneUserId);
			result.setAutoSelect(true);
		}else if ("N26".equals(nodeCode)){
			// N26 督查处复核办结  跨节点退回 查找前两节点的原办理人
			Bean preNodeBean = findPreNodeBean(nodeBean);
			Bean preNodeBean2 = findPreNodeBean(preNodeBean);
			String doneUserId = preNodeBean2.getStr("DONE_USER_ID");
			result.setUserIDs(doneUserId);
			result.setAutoSelect(true);
		}else if ("N27".equals(nodeCode)){
			// N27  督查办结  跨节点退回 查找前三节点的原办理人
			Bean preNodeBean = findPreNodeBean(nodeBean);
			Bean preNodeBean2 = findPreNodeBean(preNodeBean);
			Bean preNodeBean3 =findPreNodeBean(preNodeBean2);
			String doneUserId = preNodeBean3.getStr("DONE_USER_ID");
			result.setUserIDs(doneUserId);
			result.setAutoSelect(true);
		}else if ("N28".equals(nodeCode)){
			// N28 督查归档 跨节点退回 查找前四节点的原办理人
			Bean preNodeBean = findPreNodeBean(nodeBean);
			Bean preNodeBean2 = findPreNodeBean(preNodeBean);
			Bean preNodeBean3 =findPreNodeBean(preNodeBean2);
			Bean preNodeBean4 =findPreNodeBean(preNodeBean3);
			String doneUserId = preNodeBean4.getStr("DONE_USER_ID");
			result.setUserIDs(doneUserId);
			result.setAutoSelect(true);
		} else{
			// 查询省厅督察员的所有人
			SqlBean sql = new SqlBean();
			sql.and("ROLE_CODE",SHENGTI_ROLE_CODE);
			List<Bean> userList = ServDao.finds("SY_ORG_ROLE_USER",sql);
			String userIds = SUUtils.getUserIds(userList);
			result.setUserIDs(userIds);
			result.setAutoSelect(true);
		}
		return  result;
	}

	/**
	 *   获取前一节点的实例信息
	 * @param nodeBean
	 * @return
	 */
	public Bean findPreNodeBean(Bean nodeBean){
		String preNodeId = nodeBean.getStr("PRE_NI_ID");
		Bean preNodeBean = ServDao.find("SY_WFE_NODE_INST",new SqlBean().and("NI_ID",preNodeId));
		return preNodeBean;
	}

}
