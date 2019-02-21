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
 * 督查办理扩展类
 * @author admin
 *
 */
public class SupApplyManagerExtWfBinder implements ExtendBinder{
	private static String SU_APPLY_DEPT = "SU_APPLY_DEPT";
	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {

		ExtendBinderResult result = new ExtendBinderResult();
		
		//1.取得流程实例对象
		WfProcess process =  currentWfAct.getProcess();
		Bean nFourNodeBean = new Bean();
//		String roleCode = nextNodeDef.getStr("NODE_ROLE_CODES");
		Bean nodeBean = currentWfAct.getNodeInstBean();
		Bean servBean = process.getServInstBean();
		//获取前一节点ID
		String preNiId = nodeBean.getStr("PRE_NI_ID");
		//获取前一节点实例
		Bean preNodeBean = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("PRE_NI_ID", preNiId));
		//
		if(preNodeBean != null){
			//获取前一节点的前一节点ID
			String nFourNiId = process.getNodeInstBean(preNiId).getStr("PRE_NI_ID");
			//获取前一节点的前一节点的实例
			nFourNodeBean = process.getNodeInstBean(nFourNiId);
			if(nFourNodeBean != null){
				//根据流程的节点NODE_CODE判断去查哪些人，如果是提交成果形式及办理计划（N4）或者督查办理（N45）
				if("N4".equals(nFourNodeBean.getStr("NODE_CODE")) || "N45".equals(nFourNodeBean.getStr("NODE_CODE"))){
					String nFourNodeDoUserId = nFourNodeBean.getStr("DONE_USER_ID");
					//获取节点办理人
					Bean nFourNodeDoUserBean = ServDao.find("SY_BASE_USER_V", new SqlBean().and("USER_CODE", nFourNodeDoUserId));
					SqlBean sqlBean = new SqlBean();
					sqlBean.selects("USER_CODE,USER_NAME,DEPT_CODE,ROLE_CODE");
					//取到办理人所在部门，设置成where条件
					sqlBean.and("TDEPT_CODE", nFourNodeDoUserBean.getStr("TDEPT_CODE"));
					sqlBean.and("ROLE_CODE", "SUP003");
					//查询出当前需要的处理人
					List<Bean> userList = ServDao.finds("SY_ORG_ROLE_USER", sqlBean);
					String userIds = SUUtils.getUserIds(userList);
					result.setUserIDs(userIds);
					result.setAutoSelect(true);
				}
				//如果是督查立项节点（N1），直接送到督查办理，则走如下逻辑
				if("N1".equals(preNodeBean.getStr("NODE_CODE"))){
					//获取该单子的ID
					String servId = servBean.getId();
					SqlBean sqlBean = new SqlBean();
					sqlBean.and("APPLY_ID", servId);
					sqlBean.and("S_FLAG", Constant.YES);
					//获取该申请单关联的主办单位表数据
					List<Bean> applyDeptList = ServDao.finds(SU_APPLY_DEPT, sqlBean);
					String userIds = SUUtils.getUserIds(applyDeptList);
					//设置处理人
					result.setUserIDs(userIds);
					result.setAutoSelect(true);
				}
			}
		}
		return result;
	}
}
