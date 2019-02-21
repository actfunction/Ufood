package com.rh.gw.serv;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.WfParamBean;
import com.rh.core.util.DateUtils;
import com.rh.core.wfe.resource.GroupBean;

import com.rh.gw.util.GwExtTabUtils;

/***
 * 
 * 收文拓展类
 * 
 * @author zupke
 * @version 1.0
 */
public class ShouWenServ extends GwExtServ {

	/**
	 * 发文转收文-刚转的sw为4， 当多角色进行抢占操作以后就变成 ""
	 */
	private static final String FW_TO_SW = "4";

	/**
	 * 根据前后页签的不同来删除数据库
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean deleteRalateTab(ParamBean paramBean) {
		GwExtTabUtils gwUtil = new GwExtTabUtils();
		return gwUtil.deleteRalateTab(paramBean);
	}

	/**
	 * 根据流程实例ID和节点ID获得自定义变量
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean getTabs(ParamBean paramBean) {
		GwExtTabUtils gwUtil = new GwExtTabUtils();
		return gwUtil.getTabs(paramBean);
	}

	/**
	 * 更新督察赋权
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean updateDCFQ(ParamBean paramBean) {
		OutBean out = new OutBean();
		String dataId = paramBean.getStr("dataId");// 公文id
		int DCFQ = paramBean.getInt("DCFQ");
		SqlExecutor executor = Transaction.getExecutor();
		if (!dataId.equals("")) {
			String sql = "UPDATE OA_GW_GONGWEN SET GW_GONGWEN_DCFQ=" + DCFQ + " WHERE GW_ID='" + dataId + "'";
			try {
				int i = executor.execute(sql);
				out.set("result", i);
				out.set("DCFQ", DCFQ);
			} catch (Exception e) {
				out.setError("更新失败");
			}
		}
		return out;
	}

	/**
	 * 根据dataId,pId,nId来删除相关的数据，以及重新启动流程
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean restratProcess(ParamBean paramBean) {
		OutBean out = new OutBean();

		out.set("old", paramBean);
		// 获取所需信息
		String dataId = paramBean.getStr("dataId");
		String pId = paramBean.getStr("pId");
		// String nId = paramBean.getStr("nId");
		String servId = paramBean.getStr("servId");

		String deleteProcess = "delete from SY_WFE_PROC_INST_HIS where PI_ID='" + pId + "'";
		String deleteNode = "delete from SY_WFE_NODE_INST_HIS where PI_ID='" + pId + "'";
		String deleteMind = "delete from SY_COMM_MIND where DATA_ID='" + dataId + "'";
		String selectData = "select * from OA_GW_GONGWEN where GW_ID='" + dataId + "'";
		String updateData = "update OA_GW_GONGWEN set S_WF_INST=NULL,S_WF_USER=NULL,S_WF_NODE=NULL,S_WF_STATE=NULL where GW_ID='"
				+ dataId + "'";

		SqlExecutor executor = Transaction.getExecutor();
		try {
			Transaction.begin();
			boolean flag = false;

			// 删除公文相应的意见
			int executeMind = executor.execute(deleteMind);
			out.set("deleteMind", executeMind);

			// 删除公文相应的流程实例
			int executeProcess = executor.execute(deleteProcess);
			out.set("deleteProcess", executeProcess);
			if (executeProcess > 0) {

				// 删除公文相应的节点实例
				int executeNode = executor.execute(deleteNode);
				out.set("deleteNode", executeNode);
				if (executeNode > 0) {

					// 置空公文流程相关字段
					int putNull = executor.execute(updateData);
					out.set("putNull", putNull);
					if (putNull > 0) {
						Bean dataBean = executor.queryOne(selectData);
						dataBean.setId(dataId);
						dataBean.set("S_USER", paramBean.get("uId"));
						flag = startWf(servId, dataBean);
						out.set("startWf", flag);
					}

				}
			}
			if (!flag) {
				Transaction.rollback();
			}
			Transaction.commit();
		} catch (Exception e) {
			Transaction.rollback();
		} finally {
			Transaction.end();
		}
		return out;
	}

	/**
	 * 
	 * 根据deptcode来获得deptname
	 * 
	 */
	public OutBean getDeptName(ParamBean paramBean) {
		OutBean out = new OutBean();

		String deptCodes = paramBean.getStr("deptCodes");
		deptCodes = deptCodes.replace(",", "','");
		SqlExecutor executor = Transaction.getExecutor();
		if (!deptCodes.equals("")) {
			String sql = "select DEPT_NAME " + "FROM SY_ORG_DEPT " + "WHERE DEPT_CODE " + "IN ('" + deptCodes + "')";
			try {
				List<Bean> list = executor.query(sql);
				StringBuffer deptNames = new StringBuffer();
				for (Bean bean : list) {
					deptNames.append(bean.getStr("DEPT_NAME") + ",");
				}
				out.set("deptName", deptNames.substring(0, deptNames.length() - 1).toString());
			} catch (Exception e) {
				out.setError("查询失败");
			}
		}
		return out;
	}

	/**
	 * 删除相关人员的代办和节点
	 * 
	 */
	@Override
	protected void beforeByid(ParamBean paramBean) {
		if (paramBean.contains("_PK_")) {
			String pkCode = paramBean.getId(); // 数据id
			String servID = paramBean.getServId(); // 服务id
			Bean swBean = ServDao.find(servID, pkCode);
			String fwToSw = swBean.getStr("IS_FW_TO_SW");
			if (swBean != null && fwToSw.equals(FW_TO_SW)) {
				String todoSql = "AND TODO_OBJECT_ID1 ='" + pkCode + "'"; // 得到的是代办的数据
				List<Bean> todolist = ServDao.finds("SY_COMM_TODO", new Bean().set("_WHERE_", todoSql));
				for (Bean todoBean : todolist) {
					UserBean userBean = Context.getUserBean(); // 得到当前节点登录人
					String nodeId = todoBean.getStr("TODO_OBJECT_ID2");
					Bean nodeBean = ServDao.find("OA_GW_WFE_NODE", nodeId); // 节点信息
					String userCode = todoBean.getStr("OWNER_CODE"); // 当前代办数据的用户编码
					String currUser = userBean.getId();
					if (userCode.equals(currUser)) { // 如果是当前人就修改GW数据
						swBean.setId(pkCode);
						swBean.set("S_USER", currUser);
						swBean.set("IS_FW_TO_SW", fwToSw + "1");
						swBean.set("S_UNAME", userBean.getName());
						swBean.set("S_WF_USER", userBean.getId());
						swBean.set("S_DEPT", userBean.getDeptCode());
						swBean.set("S_DNAME", userBean.getDeptName());
						swBean.set("S_TDEPT", userBean.getTDeptCode());
						swBean.set("S_TNAME", userBean.getTDeptName());
						swBean.set("S_WF_INST", nodeBean.getStr("PI_ID"));
						String userState = "[{D:" + userBean.getStr("S_WF_NODE") + ",U:"
								+ userBean.getStr("S_USER") + ",N:" + userBean.getName() + ",O:N}]";
						swBean.set("S_WF_USER_STATE", userState);
						ServDao.save("OA_GW_GONGWEN", swBean); // 执行保存方法
					} else { // 如果不是当前人就对待办和节点进行处理
						// 结束其他人的代办
						TodoUtils.endById(todoBean.getId());
						nodeBean.set("NODE_IF_RUNNING", 2); // 修改节点
						nodeBean.set("NODE_ETIME", DateUtils.getDatetime());
						nodeBean.set("NODE_DESC", "被抢占");
						nodeBean.set("DONE_USER_ID", currUser);
						nodeBean.set("DONE_USER_NAME", userBean.getName());
						nodeBean.set("DONE_DEPT_IDS", userBean.getDeptCode());
						nodeBean.set("DONE_DEPT_NAMES", userBean.getDeptName());
						ServDao.save("OA_GW_WFE_NODE", nodeBean); // 执行保存方法
					}
				}
			}
		}
		super.beforeByid(paramBean);
	}

	@Override
	protected void beforeStartWf(WfParamBean wfParam, Bean dataBean) {
		String sUser = dataBean.getStr("S_USER");
		Set<String> setUser = new HashSet<String>();
		setUser.add(sUser);
		GroupBean groupBean = new GroupBean();
		groupBean.setUserIds(setUser);
		wfParam.set("TO_USERS", groupBean);
		super.beforeStartWf(wfParam, dataBean);
	}
}
