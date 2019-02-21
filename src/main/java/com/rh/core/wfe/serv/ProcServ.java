package com.rh.core.wfe.serv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.mind.MindUtils;
import com.rh.core.org.UserBean;
import com.rh.core.org.util.OrgConstant;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.WfOutBean;
import com.rh.core.serv.bean.WfParamBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.msg.MsgCenter;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfContext;
import com.rh.core.wfe.WfParam;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.WfProcessFactory;
import com.rh.core.wfe.attention.AttentionMsg;
import com.rh.core.wfe.db.WfProcInstDao;
import com.rh.core.wfe.db.WfProcInstHisDao;
import com.rh.core.wfe.def.WfLineDef;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.resource.GroupBean;
import com.rh.core.wfe.resource.WfBinderManager;
import com.rh.core.wfe.resource.WfeBinder;
import com.rh.core.wfe.util.ComparatorBtnSort;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 工作流服务
 * 
 * @author wanglong
 * 
 */
public class ProcServ extends CommonServ {

	private static Log log = LogFactory.getLog(ProcDefServ.class);

	/**
	 * 
	 * @param paramBean
	 *            请求参数
	 */
	private void initWfContext(ParamBean paramBean) {
		WfContext.cleanThreadData();
		if (paramBean.isNotEmpty(WfContext.APP_DATA)) {
			WfContext.setAppData(paramBean.getBean(WfContext.APP_DATA));
		}
	}

	/**
	 * 清理WfContext内的数据
	 */
	private void destoryWfContext() {
		WfContext.cleanThreadData();
	}

	/**
	 * 将工作流的信息加到返回前台的Bean数据中
	 * 
	 * @param servId
	 *            服务ID
	 * @param outBean
	 *            返回前台Bean , 一般是指具体的业务数据Bean
	 * @param paramBean
	 *            参数Bean
	 */
	public void addWfInfoToOut(String servId, Bean outBean, ParamBean paramBean) {
		// 如果有忽略流程参数，则不加载流程信息。
		if (paramBean.isNotEmpty(Constant.IGNORE_WF_INFO)) {
			outBean.set(Constant.IGNORE_WF_INFO, "true");
			return;
		}

		// 先判断outBean是挂载流程信息了
		if (outBean.isEmpty("S_WF_INST")) {
			return;
		}

		try {
			initWfContext(paramBean);
			WfContext.setAppData(outBean);

			String pid = outBean.getStr("S_WF_INST");
			String procRunning = outBean.getStr("S_WF_STATE");

			boolean procInstIsRunning = true;
			if (outBean.getInt("S_FLAG") == Constant.NO_INT) { // 如果数据被删除，则流程都被移動到歷史表。
				procInstIsRunning = false;
				procRunning = Constant.NO;
			} else {
				procInstIsRunning = this.procInstIsRunning(procRunning);
			}

			outBean.set("INST_IF_RUNNING", procRunning);
			outBean.set("PI_ID", pid);

			UserBean doUserBean = getDoUserBean(paramBean);
			if (!Context.isCurrentUser(doUserBean)) {
				outBean.set(OrgConstant.AGENT_USER, doUserBean.getCode());
				outBean.set(OrgConstant.AGENT_USER_BEAN, doUserBean);
			}

			WfAct wfAct = null;

			if (!paramBean.isEmpty("NI_ID")) { // 从参数中传过来了节点实例ID
				String nid = paramBean.getStr("NI_ID");
				wfAct = new WfAct(nid, procInstIsRunning);
			} else { // 查TO_USER_ID 得到最后的那个节点实例
				wfAct = this.getUserLastToDoWfAct(doUserBean, pid, procInstIsRunning);
			}

			if (wfAct != null && wfAct.canDuzhan()) { // 节点运行状态，同时运行节点大于1 , 如果是独占的话，只出独占的按钮
				UserBean realUser = getDoUserBean(paramBean);
				wfAct.duzhan(realUser);
				if (!paramBean.isEmpty("NI_ID")) { // 从参数中传过来了节点实例ID
					String nid = paramBean.getStr("NI_ID");
					wfAct = new WfAct(nid, procInstIsRunning);
				} else { // 查TO_USER_ID 得到最后的那个节点实例
					wfAct = this.getUserLastToDoWfAct(doUserBean, pid, procInstIsRunning);
				}
			}

			WfProcess process;

			if (wfAct != null) {
				process = wfAct.getProcess();
			} else {
				process = this.getWfProcess(pid, procInstIsRunning);
			}

			process.setServInstBean(outBean);
			if (wfAct != null) {
				Bean instBean = wfAct.getNodeInstBean();
				instBean.set("RUNING_COUNT", wfAct.getProcess().getRunningNodeInstCount());
				outBean.set("nodeInstBean", instBean);
				outBean.set("nodeDefBean", wfAct.getNodeDef());
			}

			// 设置提醒的标题，在分发到时候，有用到
			String bindTitle = process.getProcInstTitle();
			outBean.set("bindTitle", bindTitle);

			// 根据办理人员的类型，组织相关数据
			WfOut wfOutBean = null;

			if (paramBean.isNotEmpty("SEND_ID")) { // 优先处理分发，因为分发过去的不需要有其他的按钮
				// 分发接收用户，或其他有权限浏览文件的用户
				outBean.set("SEND_ID", paramBean.getStr("SEND_ID"));
				wfOutBean = new SendOutBean(process, outBean, paramBean);
			} else {
				// 流程当前办理人
				if (null != wfAct && wfAct.isUserDoing(doUserBean.getCode())) {
					wfOutBean = new DoingOutBean(process, outBean, paramBean);
				} else if (wfAct != null) {
					// 流经
					wfOutBean = new FlowOutBean(process, outBean, paramBean);

					if (process.isProcManage()) {
						wfOutBean.fillOutBean(wfAct);
						// 流程管理员
						wfOutBean = new AdminOutBean(process, outBean, paramBean);
					}
				} else if (process.isProcManage()) {
					// 流程管理员
					wfOutBean = new AdminOutBean(process, outBean, paramBean);
				} else {
					// 分发接收用户，或其他有权限浏览文件的用户
					wfOutBean = new BaseOutBean(process, outBean, paramBean);
				}
			}
			wfOutBean.setDoUser(doUserBean);
			wfOutBean.fillOutBean(wfAct);
			wfOutBean.filter(wfAct);

			// 对按钮进行排序
			@SuppressWarnings("unchecked")
			List<Bean> btnList = (List<Bean>) outBean.get("buttonBean");

			ComparatorBtnSort comparator = new ComparatorBtnSort();

			Collections.sort(btnList, comparator);
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * @param paramBean
	 *            参数bean
	 * @return 下一步的节点列表
	 */
	public OutBean getNextSteps(ParamBean paramBean) {
		try {
			initWfContext(paramBean);
			String procRunning = paramBean.getStr("S_WF_STATE");

			boolean procInstIsRunning = this.procInstIsRunning(procRunning);

			WfAct wfAct = null;
			if (!paramBean.isEmpty("NI_ID")) { // 从参数中传过来了节点实例ID
				String nid = paramBean.getStr("NI_ID");
				wfAct = new WfAct(nid, procInstIsRunning);
			} else {
				OutBean rtnBean = new OutBean();
				rtnBean.set("rtnStr", "不是当前节点,没有下一步的按钮");

				return rtnBean;
			}

			List<Bean> nextSteps = wfAct.getNextAvailableSteps(getDoUserBean(paramBean));

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnStr", "success");
			rtnBean.set("nextSteps", nextSteps);

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * @param paramBean
	 *            参数bean
	 * @return 返回前台Bean
	 */
	public OutBean cmLockFile(ParamBean paramBean) {
		try {
			initWfContext(paramBean);
			UserBean userBean = getDoUserBean(paramBean);

			Bean procBean = new Bean();
			procBean.setId(paramBean.getStr("PI_ID"));
			procBean.set("INST_LOCK", WfProcess.PROC_INST_LOCK);
			procBean.set("INST_LOCK_USER", userBean.getCode());
			procBean.set("INST_LOCK_TIME", DateUtils.getDatetime());

			WfProcInstDao.updateWfProcInst(procBean);

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", "success");

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * @param paramBean
	 *            参数bean
	 * @return 返回前台Bean
	 */
	public OutBean cmUnLockFile(ParamBean paramBean) {
		try {
			Bean procBean = new Bean();
			procBean.setId(paramBean.getStr("PI_ID"));
			procBean.set("INST_LOCK", WfProcess.PROC_INST_LOCK_NO); // 不锁定
			procBean.set("INST_LOCK_USER", "");
			procBean.set("INST_LOCK_TIME", "");

			WfProcInstDao.updateWfProcInst(procBean);

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", "success");

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 如果是发部门/角色， 点进去之后， 将TO_USER_ID 设置成这个人，
	 * 其他人进页面的时候，判断TO_USER_ID如果有了，则不出独占的按钮了
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @return 返回前台结果
	 */
	public OutBean duZhan(ParamBean paramBean) {
		try {
			String pid = paramBean.getStr("PI_ID");

			WfAct wfAct = null;
			if (!paramBean.isEmpty("NI_ID")) {
				String nid = paramBean.getStr("NI_ID");
				wfAct = new WfAct(nid, true);
			} else {
				wfAct = this.getUserLastToDoWfAct(getDoUserBean(paramBean), pid, true);
			}

			if (wfAct.canDuzhan()) { // 点了独占之后，添加TO_USER_ID 和 TO_USER_NAME
				UserBean realUser = getDoUserBean(paramBean);
				wfAct.duzhan(realUser);
			}

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", "success");

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 办结
	 * 
	 * @param paramBean
	 *            参数信息
	 * @return 返回前台参数
	 */
	public OutBean finish(ParamBean paramBean) {
		try {
			String pid = paramBean.getStr("PI_ID");

			Bean aProcInstBean = WfProcInstDao.findProcInstById(pid);

			WfParam wfParam = new WfParam();

			UserBean doneUser = getDoUserBean(paramBean);

			wfParam.setDoneUser(doneUser);

			UserBean currentUser = Context.getUserBean();
			// 当前用户和办理用户不一致，则设置成代理
			if (!currentUser.getCode().equals(wfParam.getDoneUser().getCode())) {
				wfParam.setIsAgent(true);
			}

			WfProcess wfProcess = new WfProcess(aProcInstBean);

			String servId = wfProcess.getServId();

			paramBean.set("wfProcess", wfProcess);
			ServMgr.act(servId, "beforeFinish", paramBean);

			wfProcess.finish(wfParam);

			// 启用部门内未启用的意见，常用于审批单不出部门就办结的情况。
			MindUtils.enableMindInDept(wfProcess.getDocId(), doneUser.getTDeptCode());

			ServMgr.act(servId, "afterFinish", paramBean);

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", "success");

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 退回起草人
	 * 
	 * @param paramBean
	 *            paramBean
	 * @return OutBean
	 */
	public OutBean returnToQcr(ParamBean paramBean) {
		try {
			String niId = paramBean.getStr("NI_ID");

			WfAct wfAct = new WfAct(niId, true);
			if (!wfAct.isRunning()) {
				return new OutBean().setError("流程实例已经结束，不能再次退回。");
			}

			WfAct firstAct = wfAct.getProcess().getFirstWfAct();
			String definedUsers = firstAct.getNodeInstBean().getStr("TO_USER_ID");
			String definedNodeCode = firstAct.getNodeInstBean().getStr("NODE_CODE");

			UserBean currUserBean = getDoUserBean(paramBean);
			WfParam wfParam = new WfParam();

			wfParam.setTypeTo(WfParam.TYPE_TO_USER);
			wfParam.setDoneUser(currUserBean);
			wfParam.setToUser(definedUsers);
			wfParam.setDoneType(WfeConstant.NODE_DONE_TYPE_END);
			wfParam.setDoneDesc(WfeConstant.NODE_DONE_TYPE_END_DESC);

			wfAct.toNextAndEndMe(definedNodeCode, wfParam);

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", "success");

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 在未办结的时候，取User 最后办理的节点实例
	 * 
	 * @param doUser
	 *            办理用户
	 * @param pid
	 *            节点实例ID
	 * @param isRunningData
	 *            流程数据是否保存在运行表
	 * @return User 最后办理的节点实例
	 */
	private WfAct getUserLastDoneWfAct(UserBean doUser, String pid, boolean isRunningData) {

		return this.getWfProcess(pid, isRunningData).getUserLastDoneWfAct(doUser);
	}

	/**
	 * 在 未办结的时候，取TO_USER_ID 的实例
	 * 
	 * @param doUser
	 *            办理用户
	 * @param pid
	 *            节点实例ID
	 * @param isRunningData
	 *            流程数据是否保存在运行表中
	 * @return User
	 *         最后办理的节点实例。可能有以下几种情况：1，现在正在办理中的；2，我已经办理完成的、最后一次办理的实例（用于处理收回的情况）；
	 */
	private WfAct getUserLastToDoWfAct(UserBean doUser, String pid, boolean isRunningData) {
		return this.getWfProcess(pid, isRunningData).getUserLastToDoWfAct(doUser);
	}

	/**
	 * 
	 * @param piId
	 *            流程实例ID
	 * @param isRunningData
	 *            流程数据是否保存在运行表中
	 * @return 流程实例
	 */
	private WfProcess getWfProcess(String piId, boolean isRunningData) {
		WfProcess process = new WfProcess(piId, isRunningData);

		return process;
	}

	/**
	 * 流程跟踪列表
	 * 
	 * @param paramBean
	 *            参数对象
	 * @return 流程跟踪列表
	 */
	public OutBean getWfTracking(ParamBean paramBean) {
		try {
			String pid = paramBean.getStr("PI_ID");
			String procRunning = paramBean.getStr("INST_IF_RUNNING");

			WfProcess wfProcess = new WfProcess(pid, procInstIsRunning(procRunning));

			List<Bean> trackingList = wfProcess.wfTracking();
			log.debug("流程跟踪 trackingList size " + trackingList.size());

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", BeanUtils.toLinkedMap(trackingList, "NI_ID"));

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * @param procRunning
	 *            从页面传来的 WF_INST_ID 字符串类型的 流程是否运行
	 * @return 流程是否运行
	 */
	private boolean procInstIsRunning(String procRunning) {
		boolean procIsRunning = true;

		if (procRunning.equals(String.valueOf(WfeConstant.WFE_PROC_INST_NOT_RUNNING))) {
			procIsRunning = false;
		}

		return procIsRunning;
	}

	/**
	 * 启动流程服务
	 * 
	 * @param paramBean
	 *            参数信息，包含要启动的流程定义和数据信息 及起草节点处理人信息
	 *            TO_USERS(GroupBean类型),为空时，取当前用户
	 * @return outBean 包含流程实例和节点实例
	 */
	public OutBean start(ParamBean paramBean) {
		try {
			WfParamBean wfParam = (WfParamBean) paramBean;
			GroupBean users = null;
			if (paramBean.contains("TO_USERS")) {
				users = (GroupBean) paramBean.get("TO_USERS");
			}

			Bean dataBean = wfParam.getDataBean();
			beforeStart(paramBean, dataBean);

			WfAct wfAct = WfProcessFactory.startProcess(wfParam.getDataServId(), dataBean, users);
			WfOutBean outBean = new WfOutBean();
			if (null != wfAct) {
				outBean.setWfProcInst(wfAct.getProcess().getProcInstBean()).setWfActInst(wfAct.getNodeInstBean());
				outBean.setOk();
			}

			afterStart(paramBean, outBean);

			return outBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 
	 * @param paramBean
	 *            前台参数Bean
	 * @return 合并后的新的节点实例
	 */
	public OutBean stopParallelWf(ParamBean paramBean) {
		try {
			String pid = paramBean.getStr("PI_ID");
			WfAct wfAct = null;

			if (!paramBean.isEmpty("NI_ID")) {
				String nid = paramBean.getStr("NI_ID");
				wfAct = new WfAct(nid, true);
			} else {
				wfAct = this.getUserLastToDoWfAct(getDoUserBean(paramBean), pid, true);
			}

			WfParam wfParam = new WfParam();
			wfParam.setDoneUser(getDoUserBean(paramBean));
			wfParam.setDoneType(WfeConstant.NODE_DONE_TYPE_STOP);
			wfParam.setDoneDesc(WfeConstant.NODE_DONE_TYPE_STOP_DESC);

			WfAct newWfAct = wfAct.stopParallel(wfParam);

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", "success" + newWfAct.getId());

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 当活动节点大于1， 终止其中一个活动的节点 , 不创建新的节点实例
	 * 
	 * @param paramBean
	 *            从前台传来参数
	 * @return 终止流程中节点
	 */
	public OutBean stopWfNode(ParamBean paramBean) {
		try {
			OutBean rtnBean = new OutBean();

			String nid = paramBean.getStr("NI_ID");

			WfAct wfAct = new WfAct(nid, true);

			if (wfAct.getProcess().getRunningNodeInstCount() == 1) {
				throw new TipException("必须保留一个活动环节");
			}

			if (!wfAct.isRunning()) {
				throw new TipException("节点已经办结状态，不允许此操作");
			}

			WfParam wfParam = new WfParam();
			wfParam.setDoneUser(getDoUserBean(paramBean));
			wfParam.setDoneType(WfeConstant.NODE_DONE_TYPE_STOP);
			wfParam.setDoneDesc(WfeConstant.NODE_DONE_TYPE_STOP_DESC);

			UserBean currentUser = Context.getUserBean();
			// 当前用户和办理用户不一致，则设置成代理
			if (!currentUser.getCode().equals(wfParam.getDoneUser().getCode())) {
				wfParam.setIsAgent(true);
			}

			if (wfAct.getNodeDef().getInt("NODE_IF_AUTOEND") == WfeConstant.NODE_AUTO_END_NO
					&& wfAct.getNodeInstBean().getStr("TO_USER_ID").equals(getDoUserBean(paramBean).getCode())) {
				wfAct.stop(wfParam);
			} else if (wfAct.getProcess().isProcManage()) { // 流程管理员 TODO
				wfAct.stop(wfParam);
			} else if (wfAct.getPrevWfAct().getNodeInstBean().getStr("DONE_USER_ID")
					.equals(getDoUserBean(paramBean).getCode())) {
				wfAct.stop(wfParam);
			} else {
				throw new TipException("没有权限终止此节点");
			}

			rtnBean.set("rtnstr", "success");
			rtnBean.set("refresh", this.setRefreshFlag(wfAct));

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	private boolean setRefreshFlag(WfAct wfAct) {
		boolean flag = false;
		if (wfAct.getProcess().getRunningNodeInstCount() == 1) {
			List<WfAct> runningList = wfAct.getProcess().getRunningWfAct();
			for (WfAct act : runningList) {
				if (act.getNodeDef().getInt("NODE_CHILD_TYPE") != 2
						&& act.getNodeDef().getInt("NODE_CHILD_TYPE") != 3) {
					flag = true;
				}
			}
		}

		return flag;

	}

	/**
	 * 送交下一节点
	 * 
	 * @param paramBean
	 *            参数
	 * @return 节点实例ID
	 */
	public OutBean toNext(ParamBean paramBean) {
		try {
			WfProcess process = new WfProcess(paramBean.getStr("PI_ID"), paramBean.getBoolean("INST_IF_RUNNING"));
			WfParam param = new WfParam();
			param.copyFrom(paramBean);
			int typeTo = paramBean.getInt("TO_TYPE");
			param.setTypeTo(typeTo);
			param.setDoneUser(getDoUserBean(paramBean));
			param.setToUser(paramBean.getStr("TO_USERS"));
			param.setToDept(paramBean.getStr("TO_DEPT"));
			param.setToRole(paramBean.getStr("TO_ROLE"));
			if (typeTo != WfParam.TYPE_TO_USER && typeTo != WfParam.TYPE_TO_DEPT_ROLE) {
				throw new TipException("需要设置送交类型");
			} else if (typeTo == WfParam.TYPE_TO_USER) {
				if (paramBean.getStr("TO_USERS").isEmpty()) {
					throw new TipException("需要设置送交用户");
				}
			} else if (typeTo == WfParam.TYPE_TO_DEPT_ROLE) {
				if (paramBean.getStr("TO_DEPT").isEmpty() || paramBean.getStr("TO_ROLE").isEmpty()) {
					throw new TipException("需要设置送交部门、角色");
				}
			}
			param.set("TO_USERS", process.getNextActors(param));

			addMsg(paramBean.getStr("PI_ID"), process.getNextActors(param), paramBean.getStr("NODE_CODE"),
					process.getProcInstTitle());

			process.toNext(param);

			return new OutBean().setOk();
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 启动流程之后的拦截方法，由子类重载
	 * 
	 * @param paramBean
	 *            参数信息
	 * @param outBean
	 *            输出信息
	 */
	protected void afterStart(ParamBean paramBean, OutBean outBean) {
		WfParamBean wfParam = (WfParamBean) paramBean;

		ServDefBean servDef = ServUtils.getServDef(wfParam.getDataServId());
		String servSrcId = servDef.getSrcId();

		Bean whereBean = new Bean();
		whereBean.set("SERV_ID", servSrcId);
		whereBean.set("DATA_ID", wfParam.getDataBean().getId());

		Bean wfActBean = outBean.getBean("_WF_ACT_INST_");
		Bean setBean = new Bean().set("WF_NI_ID", wfActBean.getId());

		ServDao.updates(ServMgr.SY_COMM_FILE, setBean, whereBean);
	}

	/**
	 * 取消办结
	 * 
	 * @param paramBean
	 *            参数信息
	 * @return 返回前台参数
	 */
	public OutBean undoFinish(ParamBean paramBean) {
		try {
			String pid = paramBean.getStr("PI_ID");

			// 取消办结，流程就是不在运行状态
			WfProcess wfProc = this.getWfProcess(pid, false);

			if (wfProc.canUndoFinish()) {
				wfProc.undoFinish();
			} else {
				throw new TipException("没有权限取消办结");
			}

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", "success");

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 
	 * 
	 * @param paramBean
	 *            前台传来
	 * @return 返回前台信息
	 */
	public OutBean withdraw(ParamBean paramBean) {
		try {
			String pid = paramBean.getStr("PI_ID");

			WfAct wfAct = null;

			if (!paramBean.isEmpty("NI_ID")) {
				String nid = paramBean.getStr("NI_ID");
				wfAct = new WfAct(nid, true);
			} else {
				wfAct = this.getUserLastDoneWfAct(getDoUserBean(paramBean), pid, true);
			}

			log.debug("收回，当前ni_id = " + wfAct.getId());
			WfParam wfParam = new WfParam();
			wfParam.setDoneType(WfeConstant.NODE_DONE_TYPE_WITHDRAW); // 设置办理类型是收回
			wfParam.setDoneDesc(WfeConstant.NODE_DONE_TYPE_WITHDRAW_DESC);
			wfParam.setTypeTo(WfeConstant.NODE_TO_USER);
			wfParam.setToUser(getDoUserBean(paramBean).getId());
			wfParam.setDoneUser(getDoUserBean(paramBean));

			WfAct newWfAct = wfAct.withdraw(wfParam);

			// 意见收回
			MindUtils.withDrawMind(wfAct.getId(), newWfAct.getId());

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", newWfAct.getId());

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 删除流程实例服务，需要提供流程实例ID和流程状态参数，不删除流程对应的数据信息 TODO 增加验证参数的机制
	 * 
	 * @param param
	 *            流程参数
	 * @return 执行结果
	 */
	public OutBean delete(ParamBean param) {
		try {
			WfParamBean wfParam = (WfParamBean) param;
			WfOutBean out = new WfOutBean();

			boolean isRunningData = wfParam.getProcSateRunning();
			// 是否是回收站数据
			if (wfParam.getInt("S_FLAG") == Constant.NO_INT) {
				isRunningData = false; // 运行数据为null
			}

			boolean falseDel = true;
			if (wfParam.isNotEmpty("falseDel")) {
				falseDel = wfParam.getBoolean("falseDel");
			}

			WfProcess wfProcess = new WfProcess(wfParam.getProcInstCode(), isRunningData);
			if (wfProcess != null) {
				if (falseDel) { // 假删除
					wfProcess.delete(getDoUserBean(param));
				} else {
					wfProcess.destory();
				}
			}
			return out;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 删除流程，同时删除流程对应的数据信息
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @return 删除状态
	 */
	public OutBean deleteDoc(ParamBean paramBean) {
		try {
			Bean proInstBean = new Bean();
			if (paramBean.getInt("INST_IF_RUNNING") == 1) { // 运行
				proInstBean = WfProcInstDao.findProcInstById(paramBean.getStr("PI_ID"));
			} else {
				proInstBean = WfProcInstHisDao.findProcInstById(paramBean.getStr("PI_ID"));
			}

			ParamBean servBean = new ParamBean(proInstBean.getStr("SERV_ID"), ServMgr.ACT_DELETE);
			servBean.set(Constant.PARAM_SERV_ID, proInstBean.getStr("SERV_ID"));
			servBean.setId(proInstBean.getStr("DOC_ID"));

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", "success");

			OutBean actBack = ServMgr.act(servBean);
			if (!actBack.isOk()) {
				rtnBean.set("rtnstr", actBack.getMsg());
			}

			return rtnBean;
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * @param paramBean
	 *            参数Bean
	 * @return 返回页面的树结构 串
	 */
	public OutBean getNextStepUsersForSelect(ParamBean paramBean) {
		try {

			String pid = paramBean.getStr("PI_ID");
			WfAct currWfAct = new WfAct(paramBean.getStr("NI_ID"), true);

			WfProcDef procDef = currWfAct.getProcess().getProcDef();
			String nextNodeCode = paramBean.getStr("NODE_CODE");
			WfNodeDef nextNodeDef = procDef.findNode(nextNodeCode);

			UserBean doUser = getDoUserBean(paramBean);
			WfBinderManager wfBinderManager = new WfBinderManager(nextNodeDef, currWfAct, doUser);

			WfLineDef lineBean = null;
			if (!StringUtils.isEmpty(paramBean.getStr("LINE_CODE"))) {// 优先使用lineCode查询
				lineBean = procDef.findLineDefByLineCode(paramBean.getStr("LINE_CODE"));
			}
			if (lineBean == null) {
				lineBean = procDef.findLineDef(currWfAct.getCode(), nextNodeDef.getStr("NODE_CODE"));
			}

			if (paramBean.isNotEmpty("NODE_USER")) {
				String nodeUser = paramBean.getStr("NODE_USER");
				Bean orgDefBean = new Bean();
				orgDefBean.set("NODE_USER_CODES", nodeUser);
				orgDefBean.set("NODE_USER_MODE", WfeConstant.NODE_BIND_MODE_ZHIDING);
				wfBinderManager.initBinderResource(orgDefBean);
			} else if (lineBean.isEnableOrgDef()) { // 如果启动了线组织资源定义，则使用线组织资源定义
				wfBinderManager.initBinderResource(lineBean.getOrgDefBean());
			} else { // 使用节点组织资源定义
				wfBinderManager.initBinderResource(nextNodeDef);
			}

			WfeBinder wfBinder = wfBinderManager.getWfeBinder();

			// 优先取按组过滤任务处理人
			if (wfBinder.getGroupBeanList().size() > 0) {
				WfProcess process = new WfProcess(pid, paramBean.getBoolean("INST_IF_RUNNING"));
				WfParam param = new WfParam();
				param.set("NI_ID", paramBean.getStr("NI_ID"));
				param.set("NODE_CODE", paramBean.getStr("NODE_CODE"));
				param.set("TO_USERS", wfBinder.getGroupBeanList());
				param.setDoneUser(getDoUserBean(paramBean));
				process.toNext(param);
				// 设置返回值中的送交人
				StringBuffer buffer = new StringBuffer();
				OutBean outBean = new OutBean();
				for (GroupBean groupBean : wfBinder.getGroupBeanList()) {
					buffer.append(groupBean.getUserNames()).append(",");
				}
				if (buffer.length() > 0) {
					buffer.setLength(buffer.length() - 1);
					outBean.set("TO_USERS", buffer);
				}

				return outBean.setOk();

			} else {
				String rtnTreeStr = wfBinder.getBinders();

				OutBean rtnBean = new OutBean();

				rtnBean.set("treeData", rtnTreeStr); // 树的数据
				rtnBean.set("multiSelect", wfBinder.isMutilSelect()); // 是否多选
				rtnBean.set("binderType", wfBinder.getBinderType()); // 角色还是用户
				rtnBean.set("roleCode", wfBinder.getRoleCode()); // 如果是角色，将角色code
																	// 带上
				// 是否需要自动选中用户
				rtnBean.set("autoSelect", wfBinder.isAutoSelect());
				rtnBean.set("readonly", wfBinder.isReadOnly());

				// if (Context.getSyConf("SY_WF_DIRECT_SEND", true)) { // 一个人直接送
				// OutBean sendRes = directSendOneUser(wfBinder, paramBean);
				// if (null != sendRes) {
				// if (sendRes.getMsg().indexOf(Constant.RTN_MSG_OK) >= 0) {
				// rtnBean.set("TO_USERS", sendRes.getStr("TO_USER_NAME"));
				// }
				// rtnBean.set("DIRECT_SEND_ONE_USER", sendRes.getMsg());
				// }
				// }

				return rtnBean;
			}
		} finally {
			destoryWfContext();
		}
	}

	/**
	 * 
	 * @param wfBinder
	 *            送交绑定的对象
	 * @param paramBean
	 *            参数Bean
	 * @return 直接送交一个人
	 */
	private OutBean directSendOneUser(WfeBinder wfBinder, ParamBean paramBean) {
		List<Bean> nodeList = wfBinder.getTreeBeanList();
		int userCount = 0;
		Bean nodeUser = new Bean();
		for (Bean node : nodeList) {
			if (node.getStr("NODETYPE").equalsIgnoreCase(WfeBinder.USER_NODE_PREFIX)) {
				userCount++;
				nodeUser = node;
				if (userCount > 1) { // 大于一个人了
					break;
				}
			}
		}

		if (userCount == 1) { // 只有一个人
			paramBean.set("TO_TYPE", WfParam.TYPE_TO_USER);
			paramBean.set("TO_USERS", nodeUser.getStr("CODE"));

			return toNext(paramBean).set("TO_USER_NAME", nodeUser.getStr("NAME"));
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param pid
	 *            流程实例ID
	 * @param list
	 *            具体送交人
	 * @param nodeCode
	 *            下个节点ID
	 * @param title
	 *            标题
	 */
	private void addMsg(String pid, List<GroupBean> list, String nodeCode, String title) {
		StringBuilder userIds = new StringBuilder();

		for (GroupBean groupBean : list) {
			Set<String> userIdSet = groupBean.getUserIds();

			for (String userId : userIdSet) {
				userIds.append(userId).append(",");
			}
		}
		if (userIds.length() > 0) {
			userIds.setLength(userIds.length() - 1);
		}

		Bean msgBean = new Bean();
		msgBean.set("PI_ID", pid);
		msgBean.set("TO_USERS", userIds.toString());
		msgBean.set("NEXT_NODE", nodeCode);
		msgBean.set("TITLE", title);

		AttentionMsg attentionMsg = new AttentionMsg(msgBean);
		MsgCenter.getInstance().addMsg(attentionMsg);
	}

	public OutBean getSrcToTarFlow(ParamBean paramBean) {
		OutBean rtnBean = new OutBean();

		String pid = paramBean.getStr("PI_ID");
		String srcNode = paramBean.getStr("SRC_NODE");
		String endNode = paramBean.getStr("END_NODE");
		String running = paramBean.getStr("INST_IF_RUNNING");
		WfProcess wfProcess = new WfProcess(pid, procInstIsRunning(running));

		List<Bean> trackingList = wfProcess.wfTracking();

		List<Bean> rtnList = new ArrayList<Bean>();

		LinkedHashMap<String, Bean> tmpMap = BeanUtils.toLinkedMap(trackingList, "NI_ID");
		
		for (Bean track : trackingList) {
			if (track.isEmpty("PRE_NI_ID")) {
				continue;
			}
			if (!track.getStr("NODE_CODE").equals(endNode)) {
				continue;
			}
			Bean preNodeInst = tmpMap.get(track.getStr("PRE_NI_ID"));
			if (!preNodeInst.getStr("NODE_CODE").equals(srcNode)) {
				continue;
			}
			rtnList.add(track);
		}
		rtnBean.setOk();
		rtnBean.setCount(rtnList.size());

		rtnBean.setData(rtnList);

		return rtnBean;

	}
}
