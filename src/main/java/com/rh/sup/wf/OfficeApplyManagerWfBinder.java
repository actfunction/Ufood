package com.rh.sup.wf;

import java.util.ArrayList;
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
 * 督查办理节点扩展类
 * @author admin
 */
public class OfficeApplyManagerWfBinder implements ExtendBinder{
	private static String SUP_OFFICE_DEPT = "OA_SUP_APPRO_OFFICE_HOST";
	
	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		ExtendBinderResult result = new ExtendBinderResult();
		
		//取得流程实例对象
		WfProcess process =  currentWfAct.getProcess();
		Bean nodeBean = currentWfAct.getNodeInstBean();
		Bean servBean = process.getServInstBean();
		//获取该单子的ID
		String servId = servBean.getId();
		//获取前一节点ID
		String preNiId = nodeBean.getStr("PRE_NI_ID");
		//获取前一节点实例(NI_ID:节点实例主键)
		Bean preNodeBean = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", preNiId));
		if("N46".equals(nodeBean.getStr("NODE_CODE")) || "N47".equals(nodeBean.getStr("NODE_CODE"))){
			Bean userBean = ServDao.find("SY_ORG_USER", new SqlBean().and("USER_CODE", nodeBean.getStr("TO_USER_ID")));
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects("DEPT_CODE,D_USER_CODE,C_USER_CODE,DEPT_TYPE");
			//取到司内督查员所在部门，设置成where条件
			sqlBean.and("OFFICE_ID", servId);
			sqlBean.and("DEPT_CODE", userBean.getStr("TDEPT_CODE"));
			//查询出当前需要的处理人
			List<Bean> userList = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
			String userIds = SUUtils.getUserIdsDc(userList);

			if("".equals(userIds)){
				result.setDeptIDs("empty");
			}
			//设置当前节点的处理人
			result.setUserIDs(userIds);
			result.setAutoSelect(true);
			return result;
		}
		if("N3".equals(nodeBean.getStr("NODE_CODE"))){
			String [] deptType = {"1","2"};
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("OFFICE_ID", servId);
			sqlBean.and("S_FLAG", Constant.YES);
			sqlBean.andIn("DEPT_TYPE", deptType);
			//获取该申请单关联的主办单位表数据
			List<Bean> applyDeptList = ServDao.finds(SUP_OFFICE_DEPT, sqlBean);
			//处理list拿到督查员ID
			String userIds = SUUtils.getUserIdsDc(applyDeptList);
			if("".equals(userIds)){
				result.setDeptIDs("empty");
			}
			//String userIds = SUUtils.getUserIds(applyDeptList);
			//设置处理人
			result.setUserIDs(userIds);
			result.setAutoSelect(true);
			return result;
		}
		if("N1".equals(nodeBean.getStr("NODE_CODE")) || "N22".equals(nodeBean.getStr("NODE_CODE")) || "N24".equals(nodeBean.getStr("NODE_CODE"))){
			if("2".equals(nodeBean.getStr("NODE_IF_RUNNING")) || "N1".equals(nodeBean.getStr("NODE_CODE"))){
				ArrayList<String> nextUser = new ArrayList<String>();
				
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
			
				String [] deptType = {"1","2"};
				SqlBean sqlBean = new SqlBean();
				sqlBean.and("OFFICE_ID", servId);
				sqlBean.and("S_FLAG", Constant.YES);
				sqlBean.andIn("DEPT_TYPE", deptType);
				//获取该申请单关联的主办单位表数据
				List<Bean> applyDeptList = ServDao.finds(SUP_OFFICE_DEPT, sqlBean);
				List<Bean> temp = new ArrayList<>();
				temp.addAll(applyDeptList);
				//处理list拿到督查员ID
				if (applyDeptList.size() > 0) {
					for (int i=0;i<temp.size();i++) {
						if (nextUser.contains(temp.get(i).getStr("C_USER_CODE"))) {
							applyDeptList.remove(temp.get(i));
						}
					}
				}
				String userIds = SUUtils.getUserIdsDc(applyDeptList);
				if("".equals(userIds)){
					result.setDeptIDs("empty");
				}
				//String userIds = SUUtils.getUserIds(applyDeptList);
				//设置处理人
				result.setUserIDs(userIds);
				result.setAutoSelect(true);
				return result;
			}
		}
		String userIds = "";
		if(preNodeBean != null){
			//根据流程的节点NODE_CODE判断去查哪些人，如果是提交成果形式及办理计划（N4）或者督查办理（N45）
			if("N4".equals(preNodeBean.getStr("NODE_CODE")) || "N45".equals(preNodeBean.getStr("NODE_CODE"))){
				if("1".equals(nodeBean.getStr("NODE_IF_RUNNING"))){
					String nFourNodeDoUserId = preNodeBean.getStr("TO_USER_ID");
					//获取节点督查员
					Bean nFourNodeDoUserBean = ServDao.find("SY_ORG_USER", new SqlBean().and("USER_CODE", nFourNodeDoUserId));
					SqlBean sqlBean = new SqlBean();
					sqlBean.selects("DEPT_CODE,D_USER_CODE,C_USER_CODE,DEPT_TYPE");
					//取到司内督查员所在部门，设置成where条件
					sqlBean.and("OFFICE_ID", servId);
					sqlBean.and("DEPT_CODE", nFourNodeDoUserBean.getStr("TDEPT_CODE"));
					//查询出当前需要的处理人
					List<Bean> userList = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
					userIds = SUUtils.getUserIdsDc(userList);
				}
				
			}
			if("N23".equals(preNodeBean.getStr("NODE_CODE"))){
				if("1".equals(nodeBean.getStr("NODE_IF_RUNNING"))){
					while(!"N4".equals(preNodeBean.getStr("NODE_CODE"))){
						String preNiIdString = preNodeBean.getStr("PRE_NI_ID");
						Bean preNodeBeanInst = ServDao.find("SY_WFE_NODE_INST", new SqlBean().and("NI_ID", preNiIdString));
						preNodeBean = preNodeBeanInst;
					};
					String nFourNodeDoUserId = preNodeBean.getStr("DONE_USER_ID");
					//获取节点督查员
					Bean nFourNodeDoUserBean = ServDao.find("SY_ORG_USER", new SqlBean().and("USER_CODE", nFourNodeDoUserId));
					SqlBean sqlBean = new SqlBean();
					sqlBean.selects("DEPT_CODE,D_USER_CODE,C_USER_CODE,DEPT_TYPE");
					//取到司内督查员所在部门，设置成where条件
					sqlBean.and("OFFICE_ID", servId);
					sqlBean.and("DEPT_CODE", nFourNodeDoUserBean.getStr("TDEPT_CODE"));
					//查询出当前需要的处理人
					List<Bean> userList = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
					userIds = SUUtils.getUserIdsDc(userList);
				}
			}
		}else{
			
		}
		if("".equals(userIds)){
			result.setDeptIDs("empty");
		}
		//4.设置当前节点的处理人
		result.setUserIDs(userIds);
		result.setAutoSelect(true);
		return result;
	}
}
