package com.rh.sup.wf;

import java.util.ArrayList;
import java.util.List;

import com.rh.sup.util.SUUtils;
import org.apache.axis.utils.StringUtils;
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

/**
 * 提交成果形式及办理计划节点扩展类
 * @author admin
 */
public class OfficePlanWFBinder implements ExtendBinder{
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
		//获取前一节点ID
		String preNiId = nodeBean.getStr("PRE_NI_ID");
		Bean preNodeBean = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", preNiId));

		if("N42".equals(nodeBean.getStr("NODE_CODE")) || "N43".equals(nodeBean.getStr("NODE_CODE"))){
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

		}
		if("N1".equals(nodeBean.getStr("NODE_CODE")) || "N22".equals(nodeBean.getStr("NODE_CODE")) || "N24".equals(nodeBean.getStr("NODE_CODE"))){
			ArrayList<String> nextUser = new ArrayList<String>();
			//处理list拿到督查员ID
			StringBuilder sb = new StringBuilder();
			String userIds = "";
			SqlBean sqlBean = new SqlBean();

			if("1".equals(nodeBean.getStr("NODE_IF_RUNNING")) && !"N1".equals(nodeBean.getStr("NODE_CODE"))){
				
				while(!"N4".equals(preNodeBean.getStr("NODE_CODE"))){
					String preNiIdString = preNodeBean.getStr("PRE_NI_ID");
					Bean preNodeBeanInst = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", preNiIdString));
					preNodeBean = preNodeBeanInst;
				};
				String nFourNodeDoUserId = preNodeBean.getStr("TO_USER_ID");
				Bean nFourNodeDoUserBean = ServDao.find("SY_ORG_USER", new SqlBean().and("USER_CODE", nFourNodeDoUserId));
				sqlBean.selects("DEPT_CODE,D_USER_CODE,C_USER_CODE,DEPT_TYPE");
				//取到司内督查员所在部门，设置成where条件
				sqlBean.and("OFFICE_ID", servId);
				sqlBean.and("DEPT_CODE", nFourNodeDoUserBean.getStr("TDEPT_CODE"));
				//查询出当前需要的处理人
				List<Bean> userList = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
				userIds = SUUtils.getUserIdsDc(userList);
			}
			if("2".equals(nodeBean.getStr("NODE_IF_RUNNING")) || "N1".equals(nodeBean.getStr("NODE_CODE"))){
				//得到下一个节点的所有用户
				List<Bean> nodeList = currentWfAct.getProcess().getAllNodeInstList();
				for (Bean node : nodeList) {
					//DONE_TYPE=2(终止)，DONE_TYPE=4(收回)，DONE_TYPE=8(取消办结)
					if (!node.getStr("DONE_TYPE").equals("4") && !node.getStr("DONE_TYPE").equals("2") && !node.getStr("DONE_TYPE").equals("8")) {
						if("N4".equals(node.getStr("NODE_CODE")) || "N45".equals(node.getStr("NODE_CODE"))){
							nextUser.add(node.getStr("TO_USER_ID"));
						}
					}
					if(node.getStr("DONE_TYPE").equals("2")){
						SqlBean userDeptSqlBean = new SqlBean();
						userDeptSqlBean.selects("TDEPT_CODE");
						userDeptSqlBean.and("USER_CODE", node.getStr("TO_USER_ID"));
						Bean uBean = ServDao.find("SY_ORG_USER", userDeptSqlBean);
						for(Bean nodeInst : nodeList){
							userDeptSqlBean = new SqlBean();
							userDeptSqlBean.selects("TDEPT_CODE");
							userDeptSqlBean.and("USER_CODE", nodeInst.getStr("TO_USER_ID"));
							Bean uWfBean = ServDao.find("SY_ORG_USER", userDeptSqlBean);
							if(uBean.getStr("TDEPT_CODE").equals(uWfBean.getStr("TDEPT_CODE"))){
								nextUser.remove(nodeInst.getStr("TO_USER_ID"));
							}
						}
					}
				}
				//3.查询当前主办单位和牵头主办单位的督查员
				String [] deptType = {"1","2"};
				sqlBean = new SqlBean();
				sqlBean.and("OFFICE_ID", servId);
				sqlBean.and("S_FLAG", Constant.YES);
				sqlBean.andIn("DEPT_TYPE", deptType);
				List<Bean> applyDeptList = ServDao.finds(SUP_OFFICE_DEPT, sqlBean);
				List<Bean> temp = new ArrayList<>();
				temp.addAll(applyDeptList);
				if (applyDeptList.size() > 0) {				
					for (int i=0;i<temp.size();i++) {
						if (nextUser.contains(temp.get(i).getStr("C_USER_CODE"))) {
							applyDeptList.remove(temp.get(i));
						}
					}
				}
				userIds = SUUtils.getUserIdsDc(applyDeptList);
			}
			if("".equals(userIds)){
				result.setDeptIDs("empty");
			}
			//4.设置当前节点的处理人
			result.setUserIDs(userIds);
			result.setAutoSelect(true);
			
		}
		return result;
	}
}
