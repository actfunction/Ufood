package com.rh.core.wfe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.entity.EntityMgr;
import com.rh.core.comm.mind.DisabledMindUpdater;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.comm.workday.WorkTime;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.JavaScriptEngine;
import com.rh.core.util.Lang;
import com.rh.core.wfe.condition.IfFlowCondition;
import com.rh.core.wfe.condition.MindContextHelper;
import com.rh.core.wfe.condition.SimpleFlowCondition;
import com.rh.core.wfe.db.ServDataDao;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.core.wfe.db.WfNodeInstHisDao;
import com.rh.core.wfe.def.WfLineDef;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.GroupBean;
import com.rh.core.wfe.resource.HuiQianExtWfBinder;
import com.rh.core.wfe.serv.WfSubProcActHandler;
import com.rh.core.wfe.util.AbstractLineEvent;
import com.rh.core.wfe.util.WfTodoProvider;
import com.rh.core.wfe.util.WfUserState;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 工作流节点实例对象
 * 
 * @author wanglong
 */
public class WfAct extends AbstractWfAct {

	private Bean nodeInstBean;

	/**
	 * @param nid
	 *            流程节点实例
	 * @param isRunningData
	 *            流程是否已经结束
	 */
	public WfAct(String nid, boolean isRunningData) {
		this.setIsRunningData(isRunningData);
		init(nid);
	}

	/**
	 * @param wfProcess
	 *            流程实例对象
	 * @param nodeInst
	 *            流程节点实例对象
	 */
	protected WfAct(WfProcess wfProcess, Bean nodeInst) {
		this.setProcess(wfProcess);
		this.nodeInstBean = nodeInst;
	}

	/**
	 * @param wfProcess
	 *            流程实例对象
	 * @param nodeInst
	 *            流程节点实例对象
	 * @param isHisData
	 *            数据保存在历史表
	 */
	protected WfAct(WfProcess wfProcess, Bean nodeInst, boolean isHisData) {
		this.setIsRunningData(isHisData);
		this.setProcess(wfProcess);
		this.nodeInstBean = nodeInst;
	}

	/**
	 * @param wfProcess
	 *            流程实例对象
	 * @param nid
	 *            节点实例ID
	 */
	public WfAct(WfProcess wfProcess, String nid) {
		this.setProcess(wfProcess);
		init(nid);
	}

	/**
	 * @param wfProcess
	 *            流程实例对象
	 * @param nid
	 *            节点实例ID
	 * @param procRunning
	 *            流程是否在运行表中。用于兼容已删除审批单查看流程跟踪功能。
	 */
	public WfAct(WfProcess wfProcess, String nid, boolean procRunning) {
		this.setIsRunningData(procRunning);
		this.setProcess(wfProcess);
		init(nid);
	}

	/**
	 * @param doUser
	 *            办理用户
	 * @param nextSteps
	 *            下一步的列表
	 * @param lineBean
	 *            连线定义
	 * @param nodeBean
	 *            节点定义
	 * @return 检查条件表单式执行结果
	 */
	private boolean checkLineCond(UserBean doUser, List<Bean> nextSteps, Bean lineBean, Bean nodeBean) {
		// UserBean userBean = Context.getUserBean();

		if (!lineBean.isEmpty("LINE_CONDS_SCRIPT")) {
			IfFlowCondition flowCondition = new SimpleFlowCondition(this, doUser);
			Bean servDataBean = this.getProcess().getServInstBean();
			// 表字段作为条件流变量放到运行环境中
			flowCondition.setVarMap(addServItem2VarMap(flowCondition, servDataBean));

			// 将条件流中设置的串取出来，并做相应的变量替换
			String lineConStr = lineBean.getStr("LINE_CONDS_SCRIPT");

			// 替换表单中的值
			lineConStr = ServUtils.replaceValues(lineConStr, this.getProcess().getServId(), servDataBean);

			// 替换 系统 变量
			lineConStr = ServUtils.replaceSysVars(lineConStr);

			return flowCondition.check(lineConStr);
		}

		return true;
	}

	/**
	 * 把服务中的数据库字段的名称和值之间放到条件流判断上下文环境中
	 * 
	 * @param flowCondition
	 *            条件流判断对象
	 * @param servDataBean
	 *            服务实体Bean
	 * @return 需要放到条件流判断上下文环境中的HashMap
	 */
	private HashMap<String, Object> addServItem2VarMap(IfFlowCondition flowCondition, Bean servDataBean) {
		HashMap<String, Object> varMap = new HashMap<String, Object>();
		Iterator<Object> keys = servDataBean.keySet().iterator();

		ServDefBean servDef = ServUtils.getServDef(this.getProcess().getServId());
		List<Bean> itemList = servDef.getViewItems();

		while (keys.hasNext()) {
			Object name = keys.next();
			if (name instanceof String) {
				for (Bean item : itemList) {
					if (item.getStr("ITEM_CODE").equals(name)
							&& item.get("ITEM_FIELD_TYPE").equals(ServConstant.ITEM_FIELD_TYPE_STR)) {
						varMap.put((String) name, servDataBean.getStr(name));
						break;
					}

					if (item.getStr("ITEM_CODE").equals(name)
							&& item.get("ITEM_FIELD_TYPE").equals(ServConstant.ITEM_FIELD_TYPE_NUM)) {
						varMap.put((String) name, servDataBean.getInt(name));
						break;
					}
				}
			}
		}

		return varMap;
	}

	/**
	 * 
	 * @return 未汇合节点数量
	 */
	private int getNotConvergeNodeCount() {
		if (this.isConvergeNode()) {
			int count = WfNodeInstDao.getRunningNodeInstCount(this.getProcess().getId());
			if (count > 0) {
				count = count - 1;
			}
			return count;
		}

		return 0;
	}

	/**
	 * 自动在合并点合并并发流 并发流，如果是合并节点 ， TO_USER_ID 是同一个， NodeCode是同一个
	 */
	private void autoConvergeNewNode() {
		if (this.isConvergeNode()) {
			// 判断是不是有并发来的节点了
			String newPreNid = this.getNodeInstBean().getStr("NODE_CODE");
			String newToUserId = this.getNodeInstBean().getStr("TO_USER_ID");

			List<Bean> nodeInstList = WfNodeInstDao.getListByPreNodeInstAndToUser(this.getProcess().getId(), newPreNid,
					newToUserId);

			// 将后面的那条节点实例 办结
			if (nodeInstList.size() > 1) {
				WfParam paramBean = new WfParam();
				paramBean.setDoneType(WfeConstant.NODE_DONE_TYPE_CONVERGE);
				paramBean.setDoneDesc(WfeConstant.NODE_DONE_TYPE_CONVERGE_DESC);

				UserBean toUserBean = UserMgr.getUser(newToUserId);
				paramBean.setDoneUser(toUserBean);

				for (int i = 1; i < nodeInstList.size(); i++) {
					Bean nodeInstTemp = nodeInstList.get(i);
					WfAct wfActTemp = new WfAct(this.getProcess(), nodeInstTemp);

					wfActTemp.finish(paramBean);
				}
			}
		}
	}

	/**
	 * 
	 * @return 能否终止并发
	 */
	public boolean canStopParallel() {
		if (isConvergeNode() && isRunning()) {
			if (this.getProcess().getRunningNodeInstCount() > 1) { // 多个活动节点
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @return 能否终止某一条分支流程
	 */
	public String canStopOneParallel() {
		String nodes = "";
		List<WfAct> nodeInstList = this.getProcess().getRunningWfAct();
		if (nodeInstList.size() > 0) {
			for (WfAct wfAct : nodeInstList) {
				if (wfAct.getNodeDef().getInt("NODE_CHILD_TYPE") == 2
						|| wfAct.getNodeDef().getInt("NODE_CHILD_TYPE") == 3) {
					nodes += "," + wfAct.getNodeInstBean().getId();
				}
			}
		}
		return nodes.length() > 1 ? nodes.substring(1) : nodes;
	}

	/**
	 * 公文打开了，也不能收回，需要记公文查看的记录
	 * 
	 * @return 是否能够收回
	 */
	public boolean canWithdraw() {
		if (!this.getProcess().isRunning()) { // 流程已经办结
			return false;
		}

		// 从本节点出去的节点 , 看这些点的状态有没有已办 , 或者有没有打开时间 ， 如果有， 返回不能收回
		List<Bean> nodeInstFromThis = WfNodeInstDao.getNextNodeInstList(this.getId());

		if (nodeInstFromThis.size() == 0) { // 从本节点还没出去过 , 不显示收回
			return false;
		}
		if (this.getNodeInstBean().getInt("DONE_TYPE") == 4) {
			return false;
		}

		for (Bean aNodeInstBean : nodeInstFromThis) {
			if (aNodeInstBean.getInt("NODE_IF_RUNNING") == WfeConstant.NODE_NOT_RUNNING) {
				return false;
			}

			// 取消办理人打开待办无法收回的功能
			// if (aNodeInstBean.getStr("OPEN_TIME").length() > 0) {
			// return false;
			// }
		}

		return true;
	}

	/**
	 * 修改节点办理类型说明
	 * 
	 * @param type
	 *            办理类型
	 * @param desc
	 *            说明
	 */
	protected void changeDoneType(int type, String desc) {
		nodeInstBean.set("DONE_TYPE", type);
		nodeInstBean.set("DONE_DESC", desc);
		WfNodeInstDao.updateWfNodeInst(nodeInstBean);
	}

	/**
	 * 在合并节点 结束并发流程，添加新的节点实例 状态为正常结束
	 * 
	 * @param doUser
	 *            办理用户
	 * @return 结束并发流程后的节点实例
	 */
	private WfAct completeParallel(UserBean doUser) {
		WfParam paramBean = new WfParam();
		paramBean.setToUser(doUser.getId());
		paramBean.setTypeTo(WfParam.TYPE_TO_USER);

		// 自己送自己 linecode 为空
		Bean lineDefBean = new Bean();
		lineDefBean.set("LINE_CODE", "");

		GroupBean groupUser = getNextActors(getCode(), paramBean).get(0);

		WfAct wfAct = createNewNodeInstBean(paramBean, getNodeDef(), lineDefBean, groupUser);

		// 往下送交的时候，发送待办
		wfAct.sendTodo(doUser.getCode());

		return wfAct;
	}

	/**
	 * 设置限定时间
	 * 
	 * @param nextNodeDefBean
	 *            下一个节点定义
	 * @param nextNodeInstBean
	 *            下一个节点实例
	 */
	private void setTimeLimit(WfNodeDef nextNodeDefBean, Bean nextNodeInstBean) {
		// 限定时间 = 当前的时间 + 流程节点定义的 节点超时时间 NODE_TIMEOUT , 如果设置了超时时间
		int emerValue = this.getProcess().getEmergency();

		Bean timeOutBean = nextNodeDefBean.getEmerGency(emerValue);

		if (timeOutBean.isNotEmpty("TIMEOUT") && timeOutBean.getInt("TIMEOUT") > 0) { // 设置了超时值
			int timeOut = timeOutBean.getInt("TIMEOUT"); // 超时时间 小时

			WorkTime workTime = new WorkTime();
			String limitTime = workTime.addMinute(DateUtils.getDatetime(), timeOut * 60);

			nextNodeInstBean.set("NODE_LIMIT_TIME", limitTime);
		}
	}

	/**
	 * 插入新的节点实例对象。节点处理人用paramBean的TO_USERS属性设置，类型为GroupBean。
	 * 
	 * @param paramBean
	 *            要送交的用户Bean
	 * @param nextNodeDefBean
	 *            下个节点定义Bean
	 * @param lineDefBean
	 *            当前节点和下个节点之间的连线对象
	 * @param groupUser
	 *            任务处理人
	 * @return 节点实例
	 */
	private WfAct createNewNodeInstBean(WfParam paramBean, WfNodeDef nextNodeDefBean, Bean lineDefBean,
			GroupBean groupUser) {
		// 添加新的节点实例信息
		Bean nextNodeInstBean = new Bean();

		nextNodeInstBean.set("PRE_NI_ID", this.getId()); // 上一个节点ID
		nextNodeInstBean.set("PI_ID", nodeInstBean.getStr("PI_ID")); // 流程实例ID
		nextNodeInstBean.set("NODE_CODE", nextNodeDefBean.getStr("NODE_CODE"));
		nextNodeInstBean.set("NODE_NAME", nextNodeDefBean.getStr("NODE_NAME"));
		nextNodeInstBean.set("PROC_CODE", nextNodeDefBean.getStr("PROC_CODE"));
		nextNodeInstBean.set("NODE_CHILD_TYPE", nextNodeDefBean.getStr("NODE_CHILD_TYPE"));
		nextNodeInstBean.set("IS_SCORE", nextNodeDefBean.getStr("IS_SCORE"));
		nextNodeInstBean.set("NODE_IF_RUNNING", WfeConstant.NODE_IS_RUNNING);

		// 取消办结的时候，从最后一个点，创建新的，这个时候，没有连线信息
		if (null != lineDefBean && lineDefBean.getStr("LINE_CODE").length() > 0) {
			if (isBack(nextNodeDefBean.getStr("NODE_CODE"), lineDefBean)) {
				nextNodeInstBean.set("PRE_LINE_CODE", "R" + lineDefBean.getStr("LINE_CODE"));
			} else {
				nextNodeInstBean.set("PRE_LINE_CODE", lineDefBean.getStr("LINE_CODE"));
			}
		}

		// 开始时间为上一个节点结束时间
		nextNodeInstBean.set("NODE_BTIME", DateUtils.getDatetime());

		// 设置节点限定完成时间
		if (paramBean.isNotEmpty("NODE_LIMIT_TIME")) {
			nextNodeInstBean.set("NODE_LIMIT_TIME", paramBean.getStr("NODE_LIMIT_TIME"));
		} else {
			setTimeLimit(nextNodeDefBean, nextNodeInstBean);
		}

		nextNodeInstBean.set("NODE_IF_RUNNING", WfeConstant.NODE_IS_RUNNING);

		nextNodeInstBean.set("S_CMPY", Context.getUserBean().getCmpyCode());
		String niId = Lang.getUUID();
		nextNodeInstBean.set("NI_ID", niId);

		// 任务处理人
		// GroupBean groupUser = (GroupBean) paramBean.get("TO_USERS");
		if (groupUser.getUserIds().size() == 1) {
			// 送交给单个用户 设置 TO_USER_ID TO_TYPE=3
			String userId = groupUser.getUserIdStr();
			nextNodeInstBean.set("TO_TYPE", WfeConstant.NODE_INST_TO_SINGLE_USER);
			nextNodeInstBean.set("TO_USER_ID", userId);
			nextNodeInstBean.set("TO_USER_NAME", UserMgr.getUser(userId).getName());
			addNewInstUser(niId, userId);
		} else {
			// 送交给多个用户 TO_TYPE=1
			nextNodeInstBean.set("TO_TYPE", WfeConstant.NODE_INST_TO_MULTI_USER);
			addNewInstUsers(niId, groupUser);
		}
		nextNodeInstBean = WfNodeInstDao.insertWfNodeInst(nextNodeInstBean);

		WfAct nextWfAct = new WfAct(this.getProcess(), nextNodeInstBean);
		nextWfAct.updateServWhenEnter(paramBean);

		return nextWfAct;
	}

	/**
	 * 为任务分配一组用户，执行持久化
	 * 
	 * @param nid
	 *            节点实例ID
	 * @param users
	 *            用户组
	 */
	protected void addNewInstUsers(String nid, GroupBean users) {
		Iterator<String> userIter = users.getUserIds().iterator();
		while (userIter.hasNext()) {
			addNewInstUser(nid, userIter.next());
		}
	}

	/**
	 * 为任务添加一个处理人，执行持久化
	 * 
	 * @param nid
	 *            节点实例ID
	 * @param userId
	 *            用户
	 */
	protected void addNewInstUser(String nid, String userId) {
		UserBean user = UserMgr.getUser(userId);
		Bean addUser = new Bean();
		addUser.set("TO_USER_ID", userId);
		addUser.set("TO_USER_NAME", user.getName());
		addUser.set("TO_DEPT_ID", user.getDeptCode());
		addUser.set("TO_DEPT_NAME", user.getFullDeptNames());
		addUser.set("NI_ID", nid);
		addUser.set("PI_ID", this.getProcess().getId());
		ServDao.create(ServMgr.SY_WFE_NODE_USERS, addUser);
	}

	/**
	 * 结束节点实例，办理类型为：1正常结束。
	 * 
	 * @param paramBean
	 *            参数bean
	 */
	public void finish(WfParam paramBean) {
		log.debug("finish node inst " + nodeInstBean.getId());
		Bean updateNodeInst = new Bean();
		updateNodeInst.setId(nodeInstBean.getId());
		updateNodeInst.set("DONE_TYPE", paramBean.getDoneType());
		updateNodeInst.set("DONE_DESC", paramBean.getDoneDesc());

		// 结束当前节点信息 , DONE_USER, NODE_ETIME , DONE_TYPE
		String endTime = DateUtils.getDatetime();
		WorkTime workTime = new WorkTime();
		long nodeMinute = 0; // 如果是 起草 和 自动合并的 节点，时间算成0
		if (paramBean.getDoneType() != WfeConstant.NODE_DONE_TYPE_CONVERGE || nodeInstBean.isEmpty("PRE_NI_ID")) {
			nodeMinute = workTime.calWorktime("", nodeInstBean.getStr("NODE_BTIME"), endTime);
		}
		long delayMinute = 0; // 如果没在 NODE_LIMIT_TIME 以内，则算这个
		if (nodeInstBean.isNotEmpty("NODE_LIMIT_TIME")) { // 设置了超期时间
			// 当前时间大于 限定时间
			if (DateUtils.getDiffTime(nodeInstBean.getStr("NODE_LIMIT_TIME"), endTime) > 0) {
				delayMinute = workTime.calWorktime("", nodeInstBean.getStr("NODE_LIMIT_TIME"), endTime);
			}
		}

		updateNodeInst.set("NODE_ETIME", endTime);

		updateNodeInst.set("NODE_DAYS", nodeMinute); // 办理这个用的总时间
		updateNodeInst.set("DELAY_TIME", delayMinute); // 延期了多长时间

		// 补充办理人的信息
		UserBean doneUser = paramBean.getDoneUser();

		updateNodeInst.set("DONE_USER_ID", doneUser.getCode());
		updateNodeInst.set("DONE_USER_NAME", doneUser.getName());
		// 填入 部门的层级信息
		updateNodeInst.set("DONE_DEPT_IDS", doneUser.getTDeptCode());
		updateNodeInst.set("DONE_DEPT_NAMES", doneUser.getFullDeptNames());

		UserBean currentUser = Context.getUserBean();

		//
		if (paramBean.isAgent() && !currentUser.getCode().equals(doneUser.getCode())) {
			// 如果当前办理用户与流程的办理用户不是一个人，那么认为当前办理用户为代替办理人
			updateNodeInst.set("SUB_USER_ID", currentUser.getCode());
			updateNodeInst.set("SUB_USER_NAME", currentUser.getName());
			updateNodeInst.set("SUB_DEPT_IDS", currentUser.getTDeptCode());
			updateNodeInst.set("SUB_DEPT_NAMES", currentUser.getFullDeptNames());
		}

		// 设置当前节点为结束
		updateNodeInst.set("NODE_IF_RUNNING", WfeConstant.NODE_NOT_RUNNING);

		// 更新当前节点信息
		WfNodeInstDao.updateWfNodeInst(updateNodeInst);

		// 清除缓存的数据，避免出现状态不一致的情况。
		this.getProcess().cleanRunnintNodeInstList();

		this.updateServWhenFinish(paramBean);

		// 结束待办
		this.finishTodo(paramBean);
	}

	/**
	 * 查到并发出来的还在活动的节点实例，将这些实例都 结束
	 * 
	 * @param paramBean
	 *            参数Bean
	 */
	private void finishParallelNodeInst(WfParam paramBean) {
		List<WfAct> runningWfActs = this.getProcess().getRunningWfAct();
		log.debug("查到并发出来的还在活动的节点实例，将这些实例都 结束 nodeInstBeanList.size = " + runningWfActs.size());

		// nodeCode和本节点一样的，就不是终止，是正常结束
		for (WfAct wfAct : runningWfActs) {
			wfAct.finish(paramBean);
		}
	}

	/**
	 * 完成待办事件
	 * 
	 * @param paramBean
	 *            参数Bean
	 */
	public void finishTodo(WfParam paramBean) {
		WfTodoProvider.finishTodo(this, paramBean);
	}

	/**
	 * 得到当前人员可走的下一步的节点
	 * 
	 * @param doUser
	 *            办理用户
	 * @return 下一步节点集合
	 */
	public List<Bean> getNextAvailableSteps(UserBean doUser) {
		List<Bean> nextSteps = new ArrayList<Bean>();

		if (this.isRunning()) { // 节点运行着
			// 增加通过工作流节点定义产生的按钮
			addNodeDefBtns(doUser, nextSteps);

			// 添加返回xxx的按钮， 因为节点上记载了前一个节点的节点ID
			addReturnBtns(nextSteps);
		}

		return nextSteps;
	}

	/**
	 * 获取增加会签的节点
	 * 
	 * @param doUser
	 *            办理用户
	 * @return 会签环节
	 */
	public List<Bean> addParallelStep(UserBean doUser, List<Bean> nextSteps) {
		List<WfAct> runningList = this.getProcess().getRunningWfAct();

		if (runningList != null && runningList.size() > 0) {
			for (WfAct wfAct : runningList) {
				if (wfAct.getNodeDef().getInt("NODE_CHILD_TYPE") == 2
						|| wfAct.getNodeDef().getInt("NODE_CHILD_TYPE") == 3) {
					List<WfLineDef> nextLineDefBeanList = this.getProcess().getProcDef()
							.findLineDefList(this.getCode());
					for (WfLineDef lineDef : nextLineDefBeanList) {
						if (lineDef.getInt("IF_PARALLEL") == 1) {
							WfNodeDef nodeDefBean = this.getProcess().getProcDef()
									.findNode(lineDef.getStr("TAR_NODE_CODE"));
							Bean stepBean = new Bean();
							stepBean.set("NODE_CODE", nodeDefBean.getStr("NODE_CODE"));
							if (lineDef.getStr("SEND_TEXT").isEmpty()) {
								stepBean.set("NODE_NAME", nodeDefBean.getStr("NODE_NAME"));
							} else {
								stepBean.set("NODE_NAME", lineDef.getStr("SEND_TEXT"));
							}
							stepBean.set("ACT_CSS", nodeDefBean.getStr("NODE_CODE"));
							if (!lineDef.isEmpty("CONFIRM_MSG")) {
								stepBean.set("CONFIRM_MSG", lineDef.getStr("CONFIRM_MSG"));
							}
							stepBean.set("LINE_CODE", lineDef.getStr("LINE_CODE"));
							nextSteps.add(stepBean);
						}
					}
					break;
				}
			}
		}

		return nextSteps;
	}

	/**
	 * 增加通过工作流节点定义产生的按钮
	 * 
	 * @param doUser
	 *            办理用户
	 * @param nextSteps
	 *            工作流按钮集合
	 */
	private void addNodeDefBtns(UserBean doUser, List<Bean> nextSteps) {
		List<WfLineDef> nextLineDefBeanList = this.getProcess().getProcDef().findLineDefList(this.getCode());

		boolean canStopParl = this.canStopParallel();

		for (WfLineDef lineBean : nextLineDefBeanList) {
			WfNodeDef nodeDefBean = this.getProcess().getProcDef().findNode(lineBean.getStr("TAR_NODE_CODE"));

			// 中止并发流之前，不能送交给非并发点
			if (canStopParl && lineBean.getInt("IF_PARALLEL") != Constant.YES_INT) {
				continue;
			}

			if (HuiQianExtWfBinder.isNodeHuiShang(nodeDefBean)) { // 页面上没选会商部门的，流程中就不显示会商的节点。
				String huishangField = HuiQianExtWfBinder.getConfigField(nodeDefBean);

				Bean servData = this.getProcess().getServInstBean();

				String depts = servData.getStr(huishangField);

				if (depts.length() == 0) {
					continue;
				}
			}

			// 检查条件表单式执行结果
			if (checkLineCond(doUser, nextSteps, lineBean, nodeDefBean)) {
				Bean stepBean = new Bean();
				stepBean.set("NODE_CODE", nodeDefBean.getStr("NODE_CODE"));
				if (lineBean.getStr("SEND_TEXT").isEmpty()) {
					stepBean.set("NODE_NAME", nodeDefBean.getStr("NODE_NAME"));
				} else {
					stepBean.set("NODE_NAME", lineBean.getStr("SEND_TEXT"));
				}
				stepBean.set("ACT_CSS", nodeDefBean.getStr("NODE_CODE"));
				if (!lineBean.isEmpty("CONFIRM_MSG")) {
					stepBean.set("CONFIRM_MSG", lineBean.getStr("CONFIRM_MSG"));
				}
				stepBean.set("LINE_CODE", lineBean.getStr("LINE_CODE"));
				nextSteps.add(stepBean);
			} else {
				if (lineBean.isNotEmpty("COND_MSG")) {
					Bean stepBean = new Bean();
					stepBean.set("NODE_CODE", nodeDefBean.getStr("NODE_CODE"));
					stepBean.set("NODE_NAME", nodeDefBean.getStr("NODE_NAME"));
					stepBean.set("ACT_CSS", nodeDefBean.getStr("NODE_CODE"));
					// 设置不满足条件
					stepBean.set("NOT_MEET_COND", "true");
					// 设置提示信息
					stepBean.set("COND_MSG", lineBean.get("COND_MSG"));
					stepBean.set("LINE_CODE", lineBean.getStr("LINE_CODE"));
					nextSteps.add(stepBean);
				}
			}
		}
	}

	/**
	 * 往nextSteps变量中增加返回×××按钮
	 * 
	 * @param nextSteps
	 *            工作流按钮集合
	 */
	private void addReturnBtns(List<Bean> nextSteps) {
		List<Bean> returnNodeBeans = this.getReturns();
		if (null != returnNodeBeans && returnNodeBeans.size() > 0) {
			for (Bean nodeInstTemp : returnNodeBeans) {
				Bean backStepBean = new Bean();
				backStepBean.set("NODE_CODE", nodeInstTemp.getStr("NODE_CODE"));
				// 获取上一任务的doneUser
				String doneUser = "";
				String doneUserId = "";
				final int doneType = nodeInstTemp.getInt("DONE_TYPE");
				if (doneType == WfeConstant.NODE_DONE_TYPE_WITHDRAW) {
					// 收回
					doneUserId = nodeInstTemp.getStr("TO_USER_ID");
					doneUser = nodeInstTemp.getStr("TO_USER_NAME");
				} else {
					if (nodeInstTemp.getInt("NODE_IF_RUNNING") == Constant.YES_INT) { // 节点未办结
						doneUserId = nodeInstTemp.getStr("TO_USER_ID");
						doneUser = nodeInstTemp.getStr("TO_USER_NAME");
					} else { // 节点已办结
						doneUserId = nodeInstTemp.getStr("DONE_USER_ID");
						doneUser = nodeInstTemp.getStr("DONE_USER_NAME");
					}
				}
				// 如果用户找不到，则忽略
				if (StringUtils.isEmpty(doneUserId)) {
					continue;
				}
				// 找不到指定用户，则忽略
				try {
					UserBean rtnUser = UserMgr.getUser(doneUserId);
					if (rtnUser == null) {
						continue;
					}
				} catch (Exception e) {
					continue;
				}
				// 去掉用户姓名签名的空格
				doneUser = doneUser.replaceAll(" ", "");

				// 设置NODE_NAME，优先取连线上的设定
				Bean lineDef = null;
				String lineCode = this.getNodeInstBean().getStr("PRE_LINE_CODE");
				if (!StringUtils.isEmpty(this.getNodeInstBean().getStr("PRE_LINE_CODE"))) {// 优先使用lineCode查询
					lineDef = this.getProcess().getProcDef().findReturnLineDefByLineCode(nodeInstTemp.getStr("NODE_CODE"), this.getCode(), lineCode);
				}
				if (lineDef == null) {
					lineDef = this.getProcess().getProcDef().findLineDef(nodeInstTemp.getStr("NODE_CODE"), this.getCode());
				}
				
				if (lineDef != null && !lineDef.getStr("RETURN_TEXT").isEmpty()) {
					backStepBean.set("NODE_NAME", lineDef.getStr("RETURN_TEXT"));
				} else {
					// 返回#NODE_NAME#(#USER_NAME#)
					String confBackName = Context.getSyConf("SY_WF_BACK_NODE_NAME", "返回#USER_NAME#");

					if (confBackName.indexOf("#USER_NAME#") >= 0) {
						confBackName = confBackName.replaceAll("#USER_NAME#", doneUser);
					}
					if (confBackName.indexOf("#NODE_NAME#") >= 0) {
						WfNodeDef nodeDefBean = this.getProcess().getProcDef()
								.findNode(nodeInstTemp.getStr("NODE_CODE"));
						String nodeName = nodeDefBean.getStr("NODE_NAME");

						confBackName = confBackName.replaceAll("#NODE_NAME#", nodeName);
					}

					backStepBean.set("NODE_NAME", confBackName);
				}
				backStepBean.set("NODE_USER", doneUserId);
				backStepBean.set("NODE_USER_NAME", doneUser);
				backStepBean.set("ACT_CSS", nodeInstTemp.getStr("NODE_CODE"));
				if (lineDef != null) {
					backStepBean.set("LINE_CODE", lineDef.getStr("LINE_CODE"));
				}
				nextSteps.add(backStepBean);
			}
		}
	}

	/**
	 * 在返回按钮中 得到能够返回的节点的列表
	 * 
	 * @return 能够返回的节点实例列表
	 */
	private List<Bean> getReturns() {
		List<Bean> rtnWfActBeans = new ArrayList<Bean>();
		// 取得流程定义中能够返回的线的列表
		List<WfLineDef> lineCanReturnList = this.getProcess().getProcDef().findReturnLineDefList(this.getCode());

		if (lineCanReturnList.size() < 1) {
			return null;
		}

		// 取得 能够返回的 流程实例中的节点实例
		StringBuffer strWhere = new StringBuffer();
		strWhere.append(" and PI_ID = '");
		strWhere.append(this.getProcess().getId());
		strWhere.append("' and PRE_LINE_CODE in ('0'");

		for (Bean lineBean : lineCanReturnList) {
			strWhere.append(",'");
			strWhere.append(lineBean.getStr("LINE_CODE"));
			strWhere.append("'");
		}
		strWhere.append(")");

		List<Bean> nodeInstList = WfNodeInstDao.findNodeInstListByWhere(strWhere.toString());

		if (nodeInstList.size() < 1) {
			return null;
		}

		// 取得所有从可以返回的节点实例对象
		List<String> nodeInstIds = new ArrayList<String>();
		for (Bean nodeInstTemp : nodeInstList) {
			nodeInstIds.add(nodeInstTemp.getStr("PRE_NI_ID"));
		}

		String nodeInstId = null;
		// 判断上一个节点是否在能返回的列表里面 ， 是则返回
		if (nodeInstIds.contains(this.getNodeInstBean().getStr("PRE_NI_ID"))) {
			nodeInstId = this.getNodeInstBean().getStr("PRE_NI_ID");
		} else if (!this.getNodeInstBean().getStr("PRE_NI_ID").isEmpty()) {
			nodeInstId = getValidSrcActID(this.getNodeInstBean().getStr("PRE_NI_ID"), nodeInstIds);
		}

		if (nodeInstId != null) {
			Bean rtnNodeInstBean = WfNodeInstDao.findNodeInstById(nodeInstId);
			rtnWfActBeans.add(rtnNodeInstBean);
		}

		return rtnWfActBeans;
	}

	/**
	 * 递归查询上一个能返回的节点
	 * 
	 * @param nodeInstId
	 *            节点实例ID
	 * @param nodeInstIds
	 *            节点实例ID列表
	 * @return 有效的节点实例ID
	 */
	private String getValidSrcActID(String nodeInstId, List<String> nodeInstIds) {
		Bean nodeInstTemp = WfNodeInstDao.findNodeInstById(nodeInstId);

		// 通过srcNodeCode 找到流程中的节点实例
		if (nodeInstIds.contains(nodeInstTemp.getStr("PRE_NI_ID"))) {
			return nodeInstTemp.getStr("PRE_NI_ID");
		} else {
			// 没有前节点，则返回空
			if (nodeInstTemp.isEmpty("PRE_NI_ID")) {
				return null;
			} else {
				return getValidSrcActID(nodeInstTemp.getStr("PRE_NI_ID"), nodeInstIds);
			}
		}

	}

	/**
	 * 节点实例对象
	 * 
	 * @return 节点实例对象
	 */
	public Bean getNodeInstBean() {
		return nodeInstBean;
	}

	/**
	 * 流程未办结时，通过当前节点实例对象 得到 上一个节点实例
	 * 
	 * @return 上一个节点实例对象
	 */
	public WfAct getPrevWfAct() {
		Bean preNodeInstBean = WfNodeInstDao.getPreNode(this);

		WfAct preNodeInst = null;
		if (null != preNodeInstBean) {
			preNodeInst = new WfAct(this.getProcess(), preNodeInstBean);
		}

		return preNodeInst;
	}

	/**
	 * 流程未办结时，通过当前节点实例对象 得到 上一个节点实例
	 *
	 * @return 上一个节点实例对象
	 */
	public WfAct getPreWfAct() {
		String prevNIID = this.getNodeInstBean().getStr("PRE_NI_ID");
		return this.getProcess().getWfAct(prevNIID);
	}

	/**
	 * 流程未办结时，通过当前节点实例对象 得到 下一节点实例
	 * 
	 * @return 下一个节点实例对象
	 */
	public List<WfAct> getNextWfAct() {
		List<Bean> nodeInstList = WfNodeInstDao.getNextNodeInstList(getId());
		if (nodeInstList == null || nodeInstList.size() < 1) {
			return null;
		} else {
			List<WfAct> nextWfActs = new ArrayList<WfAct>(nodeInstList.size());
			for (Bean nodeInst : nodeInstList) {
				nextWfActs.add(new WfAct(getProcess(), nodeInst));
			}
			return nextWfActs;
		}
	}

	/**
	 * 节点实例是否 已经 超过 节点所限定的完成时间
	 * 
	 * @param strLimitTime
	 *            节点限定完成时间
	 * @return 是否已经过期
	 */
	public boolean hasExpired(String strLimitTime) {
		String nowDate = DateUtils.getDate();

		if ((strLimitTime == null) || (strLimitTime.length() < 10)) {
			return false;
		}

		int iRtn = nowDate.compareTo(strLimitTime.substring(0, 10));

		if (iRtn > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 初始化流程节点实例
	 * 
	 * @param niId
	 *            节点实例ID
	 */
	private void init(String niId) {
		if (this.isRunningData()) {
			this.nodeInstBean = WfNodeInstDao.findNodeInstById(niId);
		} else {
			this.nodeInstBean = WfNodeInstHisDao.findNodeInstById(niId);
		}
	}

	/**
	 * 节点是否活动
	 * 
	 * @return 节点是否运行状态
	 */
	public boolean isRunning() {
		if (this.getNodeInstBean().getInt("NODE_IF_RUNNING") == WfeConstant.NODE_IS_RUNNING) {
			return true;
		}
		return false;
	}

	/**
	 * 发送待办事件
	 * 
	 * @param doneUser
	 *            办理人
	 */
	public void sendTodo(String doneUser) {
		WfTodoProvider.sendTodoForToNext(this, doneUser, 0);
	}

	/**
	 * 发送待办事件
	 * 
	 * @param doneUser
	 *            办理人
	 * @param niCount
	 *            数量
	 */
	public void sendTodo(String doneUser, int niCount) {
		WfTodoProvider.sendTodoForToNext(this, doneUser, niCount);
	}

	/**
	 * 发送待办事件
	 */
	public void sendTodo() {
		sendTodo(Context.getUserBean().getCode());
	}

	/**
	 * 在合并节点 终止并发流程，没有完全收完，将所有活动节点都结束。 状态为终止
	 * 
	 * @param wfParam
	 *            办理用户
	 * @return 合并后的节点实例
	 */
	public WfAct stopParallel(WfParam wfParam) {
		if (canStopParallel()) {
			UserBean doUser = wfParam.getDoneUser();

			finishParallelNodeInst(wfParam);

			// 结束并发
			WfAct newWfAct = completeParallel(doUser);

			// 回写表单信息 在这里调用 newWfAct.updateServInstWfInfo() 取的活动节点的列表还是所有的并发的
			newWfAct.updateServWfInfo(new WfParam());

			return newWfAct;
		} else {
			throw new TipException("不是合并节点，不能结束并发流程");
		}
	}

	/**
	 * 当活动节点大于1， 终止其中一个活动的节点 , 不创建新的节点实例
	 * 
	 * @param doUser
	 *            办理用户
	 */
	public void stop(UserBean doUser) {
		if (this.getProcess().getRunningWfAct().size() > 1) { // 活动节点数大于1
			stop(doUser, WfeConstant.NODE_DONE_TYPE_STOP, WfeConstant.NODE_DONE_TYPE_STOP_DESC);
		}
	}

	/**
	 * 强制结束当前节点，不会去判断当前活动节点数是否大于1
	 * 
	 * @param doUser
	 *            办理用户
	 * @param doneType
	 *            节点结束类型
	 * @param doneDesc
	 *            节点结束说明
	 */
	public void stop(UserBean doUser, int doneType, String doneDesc) {
		WfParam paramBean = new WfParam();
		paramBean.setDoneType(doneType);
		paramBean.setDoneDesc(doneDesc);

		paramBean.setDoneUser(doUser);

		this.stop(paramBean);
	}

	/**
	 * 参数Bean中至少需要包含doneUser,doneType,doneDesc参数
	 * 
	 * @param paramBean
	 *            参数Bean
	 */
	public void stop(WfParam paramBean) {
		this.finish(paramBean);
		this.updateServWfInfo(paramBean);
	}

	/**
	 * TODO
	 * 节点的办理类型为手工结束的时候，已经往下下一个节点送文件了，但是当前节点还是活动的，如果自己不需要办理了，则要手工办结。用于同时送不同的节点。
	 * 
	 * @return 能否手动结束某个节点实例
	 */
	public boolean canHandStopNode() {
		if (this.isRunning() && this.getProcess().getRunningWfAct().size() > 1) {
			// 是不是还需要查存在不存在 以这个点 为 pre_node的点存在
			if (this.getNodeDef().getInt("NODE_IF_AUTOEND") == WfeConstant.NODE_AUTO_END_NO) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param nextNodeCode
	 *            下个节点编码
	 * @param lineDefBean
	 *            线定义
	 * @return 是否是退回
	 */
	private boolean isBack(String nextNodeCode, Bean lineDefBean) {
		String targetNodeCode = lineDefBean.getStr("TAR_NODE_CODE");
		if (targetNodeCode.equals(nextNodeCode)) {
			return false;
		}

		return true;
	}

	/**
	 * 根据paramBean中提供的toUser toDept toRole信息及要送交的节点的信息，解析出要生成的节点实例的任务处理人。
	 * 
	 * @param nextNodeCode
	 *            下一节点code
	 * @param paramBean
	 *            流程实例信息，含有选择的人、部门、角色信息
	 * @return 节点实例的任务处理人列表
	 */
	public List<GroupBean> getNextActors(String nextNodeCode, WfParam paramBean) {
		int typeTo = paramBean.getTypeTo();
		Bean lineDefBean = this.getProcess().getProcDef().findLineDef(this.getCode(), nextNodeCode);

		// 解析出处理人
		List<GroupBean> groupList = new ArrayList<GroupBean>();

		if (typeTo == WfParam.TYPE_TO_USER) {
			// 选择的是用户
			String users = paramBean.getToUser();
			String[] nextUserArr = users.split(",");

			// 取消办结 没有连线信息
			if (lineDefBean == null || lineDefBean.getInt("IF_PARALLEL") != WfeConstant.NODE_IS_PARALLEL) {
				// 取消办结 或 非并发
				GroupBean group = new GroupBean();
				group.addUsers(nextUserArr);
				groupList.add(group);
			} else {
				// 并发 为每个用户生成一个任务
				for (int i = 0; i < nextUserArr.length; i++) {
					GroupBean group = new GroupBean();
					group.addUser(nextUserArr[i]);
					groupList.add(group);
				}
			}
		} else {
			// 选择的是部门、角色 获取用户
			String depts = paramBean.getToDept();
			String[] deptArr = depts.split(",");
			List<UserBean> users = UserMgr.getUsersByDept(depts, paramBean.getToRole());
			// 并发节点的话，为每个部门生成一个任务
			if (lineDefBean.getInt("IF_PARALLEL") == WfeConstant.NODE_IS_PARALLEL) {
				Map<String, List<String>> deptUserMap = new HashMap<String, List<String>>();
				for (int i = 0; i < deptArr.length; i++) {
					deptUserMap.put(deptArr[i], new ArrayList<String>());
				}
				for (UserBean user : users) {
					if (deptUserMap.containsKey(user.getDeptCode())) {
						deptUserMap.get(user.getDeptCode()).add(user.getId());
					} else if (deptUserMap.containsKey(user.getTDeptCode())) {
						deptUserMap.get(user.getTDeptCode()).add(user.getId());
					}
				}
				for (String deptCode : deptUserMap.keySet()) {
					GroupBean group = new GroupBean();
					group.addUsers(deptUserMap.get(deptCode));
					groupList.add(group);
				}
			} else {
				GroupBean group = new GroupBean();
				for (UserBean user : users) {
					group.addUser(user.getId());
				}
				groupList.add(group);
			}
		}

		return groupList;
	}

	/**
	 * 流程转到下个节点 更新当前节点实例信息， 插入新的节点实例
	 * 
	 * @param nextNodeCode
	 *            下个节点定义编码
	 * @param paramBean
	 *            参数bean对象. 通过toUser toDept toRole设置了处理人信息。
	 * @return 当前节点实例对象
	 */
	public WfAct toNext(String nextNodeCode, WfParam paramBean) {
		GroupBean users = getNextActors(nextNodeCode, paramBean).get(0);
		// paramBean.set("TO_USERS", users);
		WfAct nextWfAct = toNextSingle(nextNodeCode, paramBean, users);
		nextWfAct.updateServWfInfo(paramBean);

		return nextWfAct;
	}

	/**
	 * 
	 * @param nextNodeCode
	 *            下一个节点的编号
	 * @param paramBean
	 *            流程参数
	 * @param groupUser
	 *            办理用户
	 * @return 送交给目标用户之后的流程实例列表
	 */
	private WfAct toNextSingle(String nextNodeCode, WfParam paramBean, GroupBean groupUser) {
		Bean lineDefBean = null;
		// 如果传入了线code,则优先使用lineCode查找线定义
		if (!StringUtils.isEmpty(paramBean.getStr("LINE_CODE"))) {// 优先使用lineCode查询
			lineDefBean = this.getProcess().getProcDef().findLineDefByLineCode(paramBean.getStr("LINE_CODE"));
		}
		if (lineDefBean == null) {
			lineDefBean = this.getProcess().getProcDef().findLineDef(this.getCode(), nextNodeCode);
		}
		

		WfNodeDef nodeDef = this.getProcess().getProcDef().findNode(nextNodeCode);

		WfAct nextWfInst = createNewNodeInstBean(paramBean, nodeDef, lineDefBean, groupUser);

		// 送交下个节点的时候，需要判断，下个节点是不是合并点，如果是，调用自动合并的方法
		nextWfInst.autoConvergeNewNode();

		// 取得未汇合的节点数量
		int niCount = nextWfInst.getNotConvergeNodeCount();

		// 是否只显示汇合后的最后一条待办
		String showLastTodo = Context.getSyConf(WfeConstant.CONF_WF_CONVERGE_LAST_TODO, "false");
		if (showLastTodo.equals("true")) {
			if (niCount == 0) {
				// 往下送交的时候，发送待办
				nextWfInst.sendTodo(paramBean.getDoneUser().getCode(), niCount);
			}
		} else {
			// 往下送交的时候，发送待办
			nextWfInst.sendTodo(paramBean.getDoneUser().getCode(), niCount);
		}

		if (null != lineDefBean) {
			if (!isBack(nextNodeCode, lineDefBean)) {
				// 如果是退回，则不执行更新操作。
				updateServByLineDef(lineDefBean, paramBean);
				callLineEvent(nextWfInst, lineDefBean);
			} else {
				callLineEvent(nextWfInst, lineDefBean);
			}
		}

		return nextWfInst;
	}

	/**
	 * 
	 * @param nextNodeCode
	 *            下一个节点的编号
	 * @param wfParam
	 *            流程参数
	 * @param nextActors
	 *            目标用户
	 * @return 送交给目标用户之后的任务实例列表
	 */
	public List<WfAct> toNext(String nextNodeCode, WfParam wfParam, List<GroupBean> nextActors) {
		List<WfAct> wfActs = new ArrayList<WfAct>();
		if (nextActors == null || nextActors.size() == 0) {
			return wfActs;
		}
		// 处理子流程节点
		if (this.getProcess().getProcDef().getSubProcNodeDefBeans().containsKey(nextNodeCode)) {
			// 送交子流程节点，送交人：取当前任务办理人
			GroupBean users = new GroupBean();
			users.addUser(wfParam.getDoneUser().getId());
			// wfParam.set("TO_USERS", users);
			WfAct subProcAct = this.toNextSingle(nextNodeCode, wfParam, users);
			// 启动子流程的运行
			WfSubProcActHandler actHandler = new WfSubProcActHandler(subProcAct);
			actHandler.startSubProcess(nextActors);
			// 主流程可否继续运行
			if (!actHandler.canHandle()) {
				subProcAct.stop(wfParam.getDoneUser(), WfeConstant.NODE_DONE_TYPE_STOP,
						WfeConstant.NODE_DONE_TYPE_STOP_DESC);
			}
			// 返回值取主流程的任务
			wfActs.add(subProcAct);

		} else {
			for (GroupBean groupUser : nextActors) {
				// wfParam.set("TO_USERS", groupUser);
				WfAct nextWfAct = this.toNextSingle(nextNodeCode, wfParam, groupUser);
				wfActs.add(nextWfAct);
			}
		}

		wfActs.get(wfActs.size() - 1).updateServWfInfo(wfParam);

		return wfActs;
	}

	/**
	 * 调用线事件的监听类
	 * 
	 * @param nextWfAct
	 *            下一个节点的ID
	 * @param lineDefBean
	 *            线定义Bean
	 */
	private void callLineEvent(WfAct nextWfAct, Bean lineDefBean) {
		if (lineDefBean.isEmpty("LINE_EVENT")) {
			return;
		}

		String lineEvent = lineDefBean.getStr("LINE_EVENT");
		AbstractLineEvent lineEventObj = (AbstractLineEvent) Lang.createObject(AbstractLineEvent.class, lineEvent);
		if (lineEventObj == null) {
			return;
		}
		if (isBack(nextWfAct.getCode(), lineDefBean)) {
			lineEventObj.backward(this, nextWfAct, lineDefBean);
		} else {
			lineEventObj.forward(this, nextWfAct, lineDefBean);
		}

	}

	/**
	 * 更新意见信息
	 * 
	 * @param doneUser
	 *            当前任务办理用户
	 * @param toUsers
	 *            所有接收用户ID
	 * @param nextNodeCode
	 *            下个节点编码
	 */
	public void updateMind(String doneUser, String[] toUsers, String nextNodeCode) {
		UserBean doUser = UserMgr.getUser(doneUser);
		boolean isOutDept = false;

		String srcNodeCode = this.getCode();
		Bean lineDef = this.getProcess().getProcDef().findLineDef(srcNodeCode, nextNodeCode);
		if (null != lineDef && lineDef.getInt("IF_OUT_DEPT") == 1) { // 线上配置了，强制出部门
			isOutDept = true;
		} else {
			String tDeptCode = doUser.getTDeptCode();
			for (String user : toUsers) {
				UserBean toUser = UserMgr.getUser(user.trim());
				if (!tDeptCode.equals(toUser.getTDeptCode())) {
					isOutDept = true;
				}
			}
		}
		DisabledMindUpdater mindUpdate = new DisabledMindUpdater(this, doUser);
		if (isOutDept) {
			mindUpdate.toOtherDept();
		} else {
			mindUpdate.toNextNode();
		}
	}

	/**
	 * 
	 * @param doneUser
	 *            办理用户
	 */
	public void updateServWhenMindSave(UserBean doneUser) {
		List<Bean> list = this.getNodeDef().getUpdateExpressWhenMindSave();

		WfParam paramBean = new WfParam();
		paramBean.setDoneUser(doneUser);
		execUpdateExpress(list, paramBean);
	}

	/**
	 * 
	 * @param paramBean
	 *            流程办理参数
	 */
	private void updateServWhenEnter(WfParam paramBean) {
		List<Bean> list = this.getNodeDef().getUpdateExpressWhenEnter();
		execUpdateExpress(list, paramBean);
	}

	/**
	 * 
	 * @param paramBean
	 *            流程办理参数
	 */
	private void updateServWhenFinish(WfParam paramBean) {
		List<Bean> list = this.getNodeDef().getUpdateExpressWhenFinish();
		execUpdateExpress(list, paramBean);
	}

	/**
	 * 根据线上定义的节点，更新服务字段
	 * 
	 * @param lineDefBean
	 *            线定义Bean
	 * @param paramBean
	 *            参数
	 */
	private void updateServByLineDef(Bean lineDefBean, WfParam paramBean) {
		if (lineDefBean == null || lineDefBean.isEmpty("UPDATE_EXPRESS_LIST")) {
			return;
		}

		// 更新表达式
		List<Bean> exprList = lineDefBean.getList("UPDATE_EXPRESS_LIST");

		execUpdateExpress(exprList, paramBean);
	}

	/**
	 * @param exprList
	 *            表达式Bean列表。表达式Bean有3个属性：UPDATE_CONDS（更新条件），UPDATE_FIELD（更新字段名），
	 *            UPDATE_VALUE（更新表达式）。
	 * @param paramBean
	 *            参数
	 */
	private void execUpdateExpress(List<Bean> exprList, WfParam paramBean) {
		if (exprList == null || exprList.size() == 0) {
			return;
		}
		boolean needUpdate = false;
		JavaScriptEngine jsEng = new JavaScriptEngine(this.getProcess().getServInstBean());
		jsEng.addVar("param", paramBean);

		MindContextHelper mindContext = new MindContextHelper(this, paramBean.getDoneUser());
		jsEng.addVar("mindContext", mindContext);
		Bean dataBean = this.getProcess().getServInstBean();
		for (int i = 0; i < exprList.size(); i++) {
			Bean expBean = exprList.get(i);
			String conds = ServUtils.replaceSysVars(expBean.getStr("UPDATE_CONDS"));
			if ((conds.length() == 0) || jsEng.isTrueScript(conds)) {
				String field = expBean.getStr("UPDATE_FIELD");
				String valueExp = ServUtils.replaceSysVars(expBean.getStr("UPDATE_VALUE"));
				String value = jsEng.runScript(valueExp);

				dataBean.set(field, value);
				needUpdate = true;
			}
		}

		if (needUpdate) {
			updateServInst(dataBean);
		}
	}

	/**
	 * 流程转到下个节点 和 完成本节点
	 * 
	 * @param nextNodeCode
	 *            下个节点定义编码
	 * @param paramBean
	 *            参数bean 通过toUser toDept toRole设置了处理人信息
	 * @return 下个节点实例对象
	 */
	public WfAct toNextAndEndMe(String nextNodeCode, WfParam paramBean) {
		finish(paramBean);
		return toNext(nextNodeCode, paramBean);
	}

	/**
	 * 打开页面的时候，设置上打开时间
	 */
	public void updateInstOpenTime() {
		Bean updateBean = new Bean(this.getId());
		updateBean.set("OPEN_TIME", DateUtils.getDatetime());
		ServDao.update(WfNodeInstDao.SY_WFE_NODE_INST_SERV, updateBean);

		Bean entityBean = new Bean();
		WfUserState userStatus = new WfUserState(this.getProcess());
		// 将活动的节点 的userCode 填到WF_USER
		entityBean.set("S_WF_USER", userStatus.getUserCodes());
		// 将活动节点的用户状态放到S_WFE_STATUS字段中
		entityBean.set("S_WF_USER_STATE", userStatus.getUserState());
		// 活动节点名称
		entityBean.set("S_WF_NODE", userStatus.getNodeNames());

		EntityMgr.update(this.getProcess().getDocId(), entityBean);
	}

	/**
	 * 把当前节点的工作流相关信息，更新到业务表单表中。
	 * 
	 * @param wfParam
	 *            工作流参数对象 doneUser
	 */
	protected void updateServWfInfo(WfParam wfParam) {
		log.debug("update the serv inst , the node inst id is " + this.getId());

		// 新建一个Bean, 只update对应的4个字段
		Bean servInstBean = new Bean();
		// 如果流程正在执行过程中，则修改活动点数据，否则清空活动点数据。
		if (this.getProcess().isRunning()) {
			WfUserState userStatus = new WfUserState(this.getProcess());
			// 将活动的节点 的userCode 填到WF_USER
			servInstBean.set("S_WF_USER", userStatus.getUserCodes());
			// 将活动节点的用户状态放到S_WFE_STATUS字段中
			servInstBean.set("S_WF_USER_STATE", userStatus.getUserState());
			// 活动节点名称
			servInstBean.set("S_WF_NODE", userStatus.getNodeNames());
		} else {
			// 设置办结人姓名
			servInstBean.set("S_WF_USER", wfParam.getDoneUser().getCode());
			// 清空数据
			servInstBean.set("S_WF_USER_STATE", "");
			// 清空数据
			servInstBean.set("S_WF_NODE", "");
		}

		// 这里记录保存 流程的ID
		servInstBean.set("S_WF_INST", this.getProcess().getId());

		servInstBean.set("S_WF_STATE", this.getProcess().getProcInstBean().get("INST_IF_RUNNING"));

		updateServInst(servInstBean);
	}

	/**
	 * 更新审批单数据
	 * 
	 * @param servInstBean
	 *            审批单服务对应实例数据
	 */
	private void updateServInst(Bean servInstBean) {
		try {
			ServDataDao.updateServInstBean(this.getProcess().getServId(), this.getProcess().getDocId(), servInstBean);
		} catch (Exception e) {
			log.debug("update servBean occur problem , servId " + this.getProcess().getServId() + " docId "
					+ this.getProcess().getDocId(), e);
		}
	}

	/**
	 * 流程收回 考虑并发流怎么收回？
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @return 节点实例对象
	 */
	public WfAct withdraw(WfParam paramBean) {
		// 从本节点出去的节点数大于1，则是并发
		// 从本节点出去的下个节点的实例列表
		List<Bean> nodeInstFromThis = WfNodeInstDao.getNextNodeInstList(this.getId());

		if (nodeInstFromThis == null || nodeInstFromThis.size() == 0) {
			throw new TipException("没有可供收回的活动点");
		}

		// 终止活动点
		for (int i = 0; i < nodeInstFromThis.size(); i++) {
			WfAct wfAct = new WfAct(this.getProcess(), nodeInstFromThis.get(i));
			if (!wfAct.isRunning()) { // 如果是运行状态
				throw new TipException("节点已办理，不能收回");
			}

			wfAct.finish(paramBean);
			// 收回的时候，那个被收回的人，待办硬删除
			WfTodoProvider.destroyTodo(wfAct);
		}

		// 收回
		WfAct wfAct = new WfAct(this.getProcess(), nodeInstFromThis.get(0));
		paramBean.put("LINE_CODE", nodeInstFromThis.get(0).getStr("PRE_LINE_CODE"));// 考虑到到目标节点有多条线的情况，这里设置lineCode
		// 以当前节点定义为目标节点
		WfAct newWfAct = wfAct.toNext(this.getCode(), paramBean);

		newWfAct.updateServWfInfo(paramBean);

		return newWfAct;
	}

	/**
	 * 开始处理文件 ， 送部门和角色的时候，TO_USER_ID 为空的时候
	 * 
	 * @return 能否独占操作
	 */
	public boolean canDuzhan() {
		if (!this.isRunning()) {
			return false;
		}

		/** TO_USER_ID 没值的话， 一定是需要独占才能开始处理工作 */
		if (this.getNodeInstBean().isEmpty("TO_USER_ID")) {
			return true;
		}

		return false;
	}

	/**
	 * @param userId
	 *            指定用户ID
	 * @return 是否当前节点是活动的，且指定用户是此节点的办理人。
	 */
	public boolean isUserDoing(String userId) {
		if (!this.isRunning()) {
			return false;
		}

		if (!this.getNodeInstBean().isEmpty("TO_USER_ID")) { // 用户不是空的
			// 这个人点了独占，但是还没送出
			if (this.getNodeInstBean().getStr("TO_USER_ID").equals(userId)) {
				return true;
			}

			return false;
		} else { // 用户是空的
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("NI_ID", this.getId());
			List<Bean> nodeUsers = ServDao.finds(ServMgr.SY_WFE_NODE_USERS, sqlBean);

			for (Bean nodeUser : nodeUsers) {
				if (nodeUser.getStr("TO_USER_ID").equals(userId)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 独占 ，1，将TO_USER_ID 填到节点实例中去,2，修改流程的状态信息， 3，删除其他人的待办信息，
	 * 
	 * @param userBean
	 *            办理人
	 */
	public void duzhan(UserBean userBean) {
		// 填TO_USER_ID
		Bean nodeInst = new Bean();
		nodeInst.setId(this.getId());
		nodeInst.set("TO_USER_ID", userBean.getCode());
		nodeInst.set("TO_USER_NAME", userBean.getName());

		// 更新节点实例
		WfNodeInstDao.updateWfNodeInst(nodeInst);

		WfParam wfParam = new WfParam();
		this.updateServWfInfo(wfParam);

		// 删除其他人的待办
		// SqlBean sqlBean = new SqlBean();
		// sqlBean.and("TODO_OBJECT_ID2", this.getId());
		// sqlBean.andNotIn("OWNER_CODE", userBean.getCode());
		//
		// TodoUtils.destroys(sqlBean);

		Bean delBean = new Bean();

		String strWhere = " and TODO_OBJECT_ID2 = '" + this.getId() + "' and OWNER_CODE != '" + userBean.getCode()
				+ "'";
		delBean.set(Constant.PARAM_WHERE, strWhere);

		ServDao.destroys(ServMgr.SY_COMM_TODO, delBean);
	}

	/**
	 * 流程节点恢复 前提：只有未办结的流程才可以调用；同时，该节点为最新运行的节点 1、更新节点实例表 2、将任务从已办转移到待办
	 */
	public void resume() {
		// 恢复任务节点
		nodeInstBean = WfNodeInstDao.findNodeInstById(this.getId());
		nodeInstBean.set("NODE_IF_RUNNING", WfeConstant.NODE_IS_RUNNING);
		nodeInstBean.set("NODE_ETIME", null);
		nodeInstBean.set("NODE_DAYS", null);

		nodeInstBean.set("NODE_USER_ID", null);
		nodeInstBean.set("NODE_USER_NAME", null);
		nodeInstBean.set("DONE_DEPT_IDS", null);
		nodeInstBean.set("DONE_DEPT_NAMES", null);

		nodeInstBean.set("SUB_USER_ID", null);
		nodeInstBean.set("SUB_USER_NAME", null);
		nodeInstBean.set("SUB_DEPT_IDS", null);
		nodeInstBean.set("SUB_DEPT_NAMES", null);

		nodeInstBean.set("DONE_TYPE", null);
		nodeInstBean.set("DONE_DESC", null);

		nodeInstBean.set("OPEN_TIME", null);

		nodeInstBean.set("REMIND_LOG", null);
		nodeInstBean.set("DELAY_TIME", null);

		WfNodeInstDao.updateWfNodeInst(nodeInstBean);

		// 恢复 待办
		SqlBean todoSql = new SqlBean();
		todoSql.and("TODO_OBJECT_ID2", this.getId()).and("OWNER_CODE", this.getNodeInstBean().getStr("TO_USER_ID"));
		Bean todoBean = ServDao.find(ServMgr.SY_COMM_TODO_HIS, todoSql);
		TodoUtils.insert(new TodoBean(todoBean));
		ServDao.destroy(ServMgr.SY_COMM_TODO_HIS, todoBean);
	}
}
