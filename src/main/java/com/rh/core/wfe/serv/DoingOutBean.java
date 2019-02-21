package com.rh.core.wfe.serv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.mind.DisabledMindUpdater;
import com.rh.core.comm.mind.MindServ;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.flow.FlowMgr;
import com.rh.core.util.JavaScriptEngine;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.condition.MindContextHelper;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.util.FileController;
import com.rh.core.wfe.util.WfBtnConstant;
import com.rh.core.wfe.util.WfUtils;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 正办理用户返回Bean
 * 
 */
public class DoingOutBean extends WfOut {
	/**
	 * 返回前台的模式
	 */
	public static final String MODE_DOING = "MODE_DOING";

	private WfAct wfAct;
	private DisabledMindUpdater mindUpdater = null;
	private HashMap<String, Bean> mindCodeMap;

	@Override
	public void fillOutBean(WfAct aWfAct) {
		this.wfAct = aWfAct;

		int lockState = this.getWfProc().getProcInstBean().getInt("INST_LOCK");
		this.getOutBean().set("_INST_LOCK", lockState);

		if (wfAct.canDuzhan()) { // 节点运行状态，同时运行节点大于1 , 如果是独占的话，只出独占的按钮
			this.addDuZhanBtn(); // 独占 ， 开始处理文件
			return;
		}

		mindUpdater = new DisabledMindUpdater(wfAct, this.getDoUser());

		addOpenTime(); // 添加打开的时间，如果打开过，则不能收回了

		addFormDefBtn(); // 表单定义的按钮
		addWfDefBtn(); // 节点上定义的公共的按钮

		if (wfAct.getProcess().canDelete(getDoUser())) {
			// 如果为起草点，且没有送出，则显示删除按钮。
			this.addDeleteBtn();
		}

		String mindCode = wfAct.getNodeDef().getStr("MIND_CODE");
		String mindTerminal = wfAct.getNodeDef().getStr("MIND_TERMINAL");
		String mindCodeReguler = wfAct.getNodeDef().getStr("MIND_REGULAR");
		StringBuilder mindCodes = new StringBuilder();
		mindCodes.append(mindCode).append(",").append(mindTerminal).append(",").append(mindCodeReguler);

		mindCodeMap = new MindServ().getMindCodeBeanMap(mindCodes.toString());
		addMindCode(); // 普通意见和最终意见
		addMindTerminal(); // 最终意见的意见编码Bean
		addMindRegular(); // 增加固定意见
		// addHandMindFlag(); // 是否支持手写意见
		// addMindMust(); //是否必填意见
		addUserDoingFlag(); // 当前节点执行状态
		addFieldCtrl(); // 输入项的控制
		addIfDraftNode(); // 是否起草点

		addFileController(); // 文件类型权限控制

		if (!(wfAct.getProcess().getRunningNodeInstCount() > 1 && wfAct.canStopParallel())) {
			// 汇合点，且活动点数量大于1时，不能送下一步，其它情况可以送下一步。
			addNextStep(); // 下一步能走到节点
		}
		
		if (wfAct.canStopParallel()) {
			List<Bean> nextSteps = new ArrayList<Bean>();
			wfAct.addParallelStep(getDoUser(), nextSteps);
			this.getOutBean().set("nextSteps", nextSteps);
		}

		operateLockBtn(); // 显示 锁定/解锁 文件

		// 节点定义转换成的按钮
		this.addFinishBtn(); // 办结按钮
		this.addRtnQcrBtn(); // 添加退回起草人按钮
		this.addInputDisBtn(); // 添加输入讨论意见按钮
		this.addFinishCurrBtn(); // 结束当前工作
		this.addTerminateParall(); // 终止并发
		this.addSaveAndSend(); // 完成并发送
		this.addTerminateOneBranch(wfAct); // 添加可以终止的并发节点

		// 补登意见
		addBuDengMindParam();

		// 填入流经的记录
		addFlowRecord();

		// 增加用户自定义变量
		addCustomVarList();

		this.getOutBean().set(DISPLAY_MODE, MODE_DOING);
		this.appendViewItem();
	}

	/**
	 * 完成并发送
	 */
	private void addSaveAndSend() {
		// if (Context.getSyConf("SY_WF_NEXT_STEP", "").equals("saveAndSend")) {
		// 根据工作流按钮渲染模式系统配置状态值决定是否添见完成并发送按钮
		String renderMode = Context.getSyConf("SY_WF_BTN_RENDER", "");
		if (renderMode.equals(WfeConstant.FLAT_GROUP) || renderMode.equals(WfeConstant.FLAT_BAR)
				|| renderMode.equals(WfeConstant.GROUP_GROUP) || renderMode.equals(WfeConstant.GROUP_BAR)) {
			Bean saveSendBtn = this.getServACTs(WfBtnConstant.BUTTON_SAVE_SEND);
			this.addBtnBean(saveSendBtn);
		}
	}

	/**
	 * 记录办理人第一次打开本节点实例的时间，如果打开过，则不能收回了。
	 */
	private void addOpenTime() {
		if (wfAct.getNodeInstBean().isEmpty("OPEN_TIME")) {
			wfAct.updateInstOpenTime();
		}
	}

	/**
	 * 是否是起草点
	 */
	private void addIfDraftNode() {
		boolean isDraftNode = false;
		if (wfAct.getNodeDef().getInt("NODE_TYPE") == WfeConstant.NODE_TYPE_DRAFT) {
			isDraftNode = true;
		}

		this.getAuthBean().set("isDraftNode", isDraftNode);
	}

	/**
	 * 添加流经的记录
	 */
	private void addFlowRecord() {
		// 流经记录的Bean
		// Bean flowBean = new Bean();
		// flowBean.set("SERV_ID", this.getWfProc().getServId()); // 流经服务ID
		// flowBean.set("DATA_ID", this.getWfProc().getDocId()); // 流经数据ID

		// flowBean.set("FLOW_TYPE", FlowDao.FLOW_TYPE_WF); // 流经类型是流程

		if (this.wfAct.getNodeInstBean().getInt("TO_TYPE") == WfeConstant.NODE_INST_TO_SINGLE_USER) { // 送单个人
			// addUser2Flow(flowBean, getDoUser());
			FlowMgr.addUserFlow(this.getWfProc().getDocId(), getDoUser());
		} else { // 送多个人 从节点用户中取发送的用户
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("NI_ID", this.wfAct.getId());
			List<Bean> nodeUsers = ServDao.finds(ServMgr.SY_WFE_NODE_USERS, sqlBean);

			for (Bean nodeUser : nodeUsers) {
				UserBean userBean = UserMgr.getUser(nodeUser.getStr("TO_USER_ID"));
				FlowMgr.addUserFlow(this.getWfProc().getDocId(), userBean);
			}
		}
	}

	/**
	 * 增加补登意见的参数
	 */
	private void addBuDengMindParam() {
		// 是否有补登意见按钮
		Bean bdMindAct = getBtnBean(WfBtnConstant.BUTTON_BUDENG);
		if (null != bdMindAct) {
			// 补登意见的时候，是需要带上领导的，即给谁补登
			bdMindAct.set("leaders", getLeaders(bdMindAct));
		}
	}

	/**
	 * 取到当前人的领导 , 在补登意见的按钮上设置
	 * {'MIND_CODE':'PS-0001','ROLE_CODE':'RBMLD','ACL_LEVEL':'1'}
	 * 
	 * @param bdMindBtn
	 *            补登意见的定义
	 * @return 得到用户的领导 形成 串，用于补登意见 {{'liyanwei','李延伟'},{'cuihf','崔海峰'}}
	 */
	private String getLeaders(Bean bdMindBtn) {

		List<Bean> rtnBeanList = new ArrayList<Bean>();

		// 取到节点上的补登意见的定义
		if (bdMindBtn.isEmpty("WFE_PARAM")) {
			return "";
		}

		String bdMindParam = bdMindBtn.getStr("WFE_PARAM");

		Bean param = null;
		try {
			param = JsonUtils.toBean(bdMindParam);
		} catch (Exception e) {
			StringBuilder error = new StringBuilder();
			error.append("Workflow define error. Budeng Mind Button Value is '");
			error.append(bdMindParam);
			error.append("', workflowCode:");
			error.append(this.wfAct.getNodeDef().getStr("PROC_CODE"));
			error.append(",NODE_CODE:");
			error.append(this.wfAct.getCode());
			log.error(error.toString());
			return "";
		}

		List<UserBean> users;
		UserBean user = this.getDoUser();

		if (param.getInt("ACL_LEVEL") == WfeConstant.BUDENG_SCOPE_DEPT) { // 本部门
			List<DeptBean> subDepts = OrgMgr.getChildDepts(user.getCmpyCode(), user.getTDeptCode());

			StringBuilder strdept = new StringBuilder(user.getTDeptCode());
			for (DeptBean dept : subDepts) {
				strdept.append(",").append(dept.getCode());
			}

			users = UserMgr.getUsersByDept(strdept.toString(), param.getStr("ROLE_CODE"));
		} else { // 本机构
			users = UserMgr.getUsersInOdept(user.getODeptCode(), param.getStr("ROLE_CODE"));
		}

		for (UserBean leader : users) {
			Bean leaderBean = new Bean();
			leaderBean.set("userCode", leader.getCode());
			leaderBean.set("userName", leader.getName());
			rtnBeanList.add(leaderBean);
		}

		return JsonUtils.toJson(rtnBeanList);
	}

	/**
	 * 节点上定义的公共的按钮 , 如分发 ， 打印，相关文件
	 */
	private void addWfDefBtn() {
		List<Bean> wfCmButton = wfAct.getNodeDef().getWfButton();
		this.addBtnBeanList(wfCmButton);
	}

	/**
	 * 操作 锁定文件的按钮
	 */
	private void operateLockBtn() {
		// 从流程中取是否已经lock的状态 ， 如果已经lock了，则移出 锁定按钮
		int lockState = this.getWfProc().getProcInstBean().getInt("INST_LOCK");
		this.getOutBean().set("_INST_LOCK", lockState);

		if (WfProcess.PROC_INST_LOCK == lockState) { // 已经锁定
			Bean lockBean = this.getServACTs(WfBtnConstant.BUTTON_LOCK);

			Bean unLockBean = this.getServACTs(WfBtnConstant.BUTTON_LOCK_UN);

			// 如果当前节点设置了锁定，而且是锁定状态的
			if (existBtnBean(lockBean) && !existBtnBean(unLockBean)) {
				this.addBtnBean(unLockBean);
			}

			removeBtnBean(lockBean);
		} else { // 没锁定， 去掉解锁按钮
			Bean unLockBean = this.getServACTs(WfBtnConstant.BUTTON_LOCK_UN);

			removeBtnBean(unLockBean);
		}
	}

	/**
	 * 表单上定义的按钮
	 */
	private void addFormDefBtn() {
		// 对于在办用户，默认增加“保存”按钮
		if (this.wfAct.getNodeDef().isEntirelyControl()) {
			ServDefBean def = wfAct.getProcess().getServDef();
			Bean saveAct = def.getAct(ServMgr.ACT_SAVE);
			this.addBtnBean(saveAct.copyOf());
		}

		List<Bean> formButton = wfAct.getNodeDef().getFormButton();

		this.addBtnBeanList(formButton);
	}

	/**
	 * 文件类型权限的控制，从节点定义上取
	 */
	private void addFileController() {
		String fileControl = wfAct.getNodeDef().getFileControl();

		FileController fileController = new FileController(fileControl);

		this.getAuthBean().set("nodeFileControl", fileController.getFileControlBeanList());
	}

	/**
	 * 表单字段控制
	 * 
	 */
	private void addFieldCtrl() {
		WfNodeDef nodeDef = wfAct.getNodeDef();

		Bean fieldCtrlBean = new Bean();

		fieldCtrlBean.set("FIELD_CONTROL", nodeDef.isEntirelyControl());
		fieldCtrlBean.set("FIELD_EXCEPTION", nodeDef.getFieldException());
		fieldCtrlBean.set("FIELD_HIDDEN", nodeDef.getFieldHidden());
		fieldCtrlBean.set("FIELD_MUST", nodeDef.getFieldMust());
		fieldCtrlBean.set("FIELD_DISPLAY", nodeDef.getFieldDisplay());

		for (int i = 0; i < WfNodeDef.GROUP_FIELDS.length; i++) {
			fieldCtrlBean.set(WfNodeDef.GROUP_FIELDS[i], nodeDef.getStr(WfNodeDef.GROUP_FIELDS[i]));
		}

		this.getOutBean().set("fieldControlBean", fieldCtrlBean);
	}

	/**
	 * 
	 * @param wfProc
	 *            流程实例
	 * @param outBean
	 *            返回数据
	 * @param paramBean
	 *            保持客户端提交数据的Bean
	 */
	public DoingOutBean(WfProcess wfProc, Bean outBean, ParamBean paramBean) {
		super(wfProc, outBean, paramBean);
	}

	/**
	 * 能走的节点
	 */
	private void addNextStep() {
		List<Bean> nextSteps = wfAct.getNextAvailableSteps(this.getDoUser());

		this.addRtnHisBtn(nextSteps); // 添加退回历史环节按钮

		this.getOutBean().set("nextSteps", nextSteps);
	}

	/**
	 * 添加意见编码Bean
	 */
	private void addMindCode() {
		String mindCode = wfAct.getNodeDef().getStr("MIND_CODE");

		// 执行过滤脚本
		String script = wfAct.getNodeDef().getStr("MIND_SCRIPT");
		boolean canWrite = this.execScript(script);

		Bean mindCodeBean = null;
		if (canWrite && StringUtils.isNotEmpty(mindCode)) {
			// 根据mindCode得到意见类型信息
			mindCodeBean = mindCodeMap.get(mindCode);
		}

		if (null == mindCodeBean) {
			mindCodeBean = new Bean();
		}

		mindCodeBean.set("MIND_MUST", wfAct.getNodeDef().getStr("MIND_NEED_FLAG"));

		this.getOutBean().set("mindCodeBean", mindCodeBean);
	}

	/**
	 * 增加固定意见定义
	 */
	private void addMindRegular() {
		Bean regularMind = null;

		// 执行意见类型过滤脚本
		String script = wfAct.getNodeDef().getStr("MIND_REGULAR_SCRIPT");
		boolean canWrite = this.execScript(script);

		final String mindCode = wfAct.getNodeDef().getStr("MIND_REGULAR");
		if (canWrite && StringUtils.isNotEmpty(mindCode)) {
			// 取得指定意见Bean
			regularMind = mindCodeMap.get(mindCode);
		}

		if (regularMind == null) {
			List<Bean> list = mindUpdater.getRegularMindList();

			if (list.size() > 0) {
				regularMind = new MindServ().getMindCode(list.get(0).getStr("MIND_CODE"));
				regularMind.set("READ_ONLY", "true");
			} else {
				regularMind = new Bean();
			}
		}

		// 固定意见是否必填
		regularMind.set("MIND_MUST", wfAct.getNodeDef().getStr("MIND_REGULAR_MUST"));

		this.getOutBean().set("regularMind", regularMind);
	}

	/**
	 * 执行返回结果为bool值的js脚本
	 * 
	 * @param script
	 *            脚本
	 * @return 执行结果
	 */
	private boolean execScript(String script) {
		return WfUtils.execCondScript(script, getOutBean());
	}

	/**
	 * 添加最终意见编码Bean
	 */
	private void addMindTerminal() {
		final String mindCode = wfAct.getNodeDef().getStr("MIND_TERMINAL");
		// 意见类型过滤脚本
		String script = wfAct.getNodeDef().getStr("MIND_TERMINAL_SCRIPT");
		boolean canWrite = this.execScript(script);

		// 根据mindCode得到意见类型信息
		Bean mindTerminal = null;
		if (canWrite && StringUtils.isNotEmpty(mindCode)) {
			mindTerminal = mindCodeMap.get(mindCode);
		}

		if (null == mindTerminal) {
			List<Bean> list = mindUpdater.getTerminalMindList();

			if (list.size() > 0) {
				mindTerminal = new MindServ().getMindCode(list.get(0).getStr("MIND_CODE"));
				mindTerminal.set("READ_ONLY", "true");
			} else {
				mindTerminal = new Bean();
			}
		}

		mindTerminal.set("MIND_MUST", wfAct.getNodeDef().getStr("MIND_TERMINAL_MUST"));

		this.getOutBean().set("mindTerminal", mindTerminal);
	}

	/**
	 * 是否支持手写意见
	 */
	// private void addHandMindFlag() {
	// boolean canHandMind = false;
	// if (wfAct.getNodeDef().getInt("MIND_IF_HAND") == 1) {
	// canHandMind = true;
	// }
	//
	// this.getAuthBean().set("canHandWrite", canHandMind);
	// }

	/**
	 * 当前用户 执行状态
	 */
	private void addUserDoingFlag() {
		this.getAuthBean().set("userDoInWf", true);
	}

	/**
	 * 添加办结按钮
	 */
	private void addFinishBtn() {
		if (wfAct.canEndTheProcess()) {
			Bean finishBtn = this.getServACTs(WfBtnConstant.BUTTON_FINISH);
			finishBtn.set("ACT_NAME", this.wfAct.getNodeDef().getStr("PROC_END_NAME"));
			this.addBtnBean(finishBtn);
		}
	}

	/**
	 * 添加退回起草人按钮
	 */
	private void addRtnQcrBtn() {
		if (wfAct.canRtnQcr()) {
			Bean finishBtn = this.getServACTs(WfBtnConstant.BUTTON_RTNQCR);
			finishBtn.set("ACT_NAME", this.wfAct.getNodeDef().getStr("RTN_QCR_NAME"));
			this.addBtnBean(finishBtn);
		}
	}

	private void addInputDisBtn() {
		if (wfAct.canInputDiscuss()) {
			Bean inputDisBtn = this.getServACTs(WfBtnConstant.BUTTON_INPUT_DISCUSS);
			this.addBtnBean(inputDisBtn);
		}
	}

	private void addRtnHisBtn(List<Bean> nextSteps) {
		if (wfAct.canRtnHisNode()) {
			String rtnHisBtnName = Context.getSyConf("SY_WF_RTN_HIS_NAME", "退回");
			String nodes = wfAct.getNodeDef().getStr("NODE_RTN_NODES");
			List<Bean> instList = wfAct.getProcess().getAllNodeInstList();
			String[] nodeArr = nodes.split(",");
			for (String node : nodeArr) {
				if (node.equals(wfAct.getNodeDef().getStr("NODE_CODE"))) {
					continue;
				}

				for (Bean inst : instList) {
					if (node.equals(inst.getStr("NODE_CODE"))) {
						Bean actBean = new Bean();
						actBean.set("ACT_CSS", node);
						actBean.set("NODE_CODE", node);
						actBean.set("NODE_NAME", rtnHisBtnName + inst.getStr("NODE_NAME"));
						actBean.set("NODE_USER", inst.getStr("DONE_USER_ID"));
						actBean.set("NODE_USER_NAME", inst.getStr("DONE_USER_NAME"));
						nextSteps.add(actBean);
						break;
					}
				}
			}

		}
	}

	/**
	 * 添加开始处理文件 按钮
	 */
	private void addDuZhanBtn() {
		Bean duzhanBtn = this.getServACTs(WfBtnConstant.BUTTON_DUZHAN);
		this.addBtnBean(duzhanBtn);
	}

	/**
	 * 添加终止并发
	 */
	private void addTerminateParall() {
		if (wfAct.canStopParallel()) {
			Bean stopParallBtn = this.getServACTs(WfBtnConstant.BUTTON_STOP_PARALLEL);
			this.addBtnBean(stopParallBtn);
		}
	}

	/**
	 * 结束当前工作
	 */
	private void addFinishCurrBtn() {
		if (wfAct.canHandStopNode()) { // 节点运行状态，同时运行节点大于1
			Bean handFinishButton = this.getServACTs(WfBtnConstant.BUTTON_STOP_NODE);
			handFinishButton.set("NI_ID", wfAct.getId());

			this.addBtnBean(handFinishButton);
		}
	}

	/**
	 * 增加自定义变量
	 */
	private void addCustomVarList() {
		Object obj = this.wfAct.getNodeDef().get(WfeConstant.CUSTOM_VARS);
		if (obj != null) {
			this.getOutBean().set(WfeConstant.CUSTOM_VARS, obj);
		}
	}

	/**
	 * 从工作流定义中取得“查看审批单”更新字段表达式，并把字段及其执行结果放到OutBean中，不更新数据库中的数据。
	 */
	private void appendViewItem() {
		List<Bean> list = this.wfAct.getNodeDef().getUpdateExpressWhenView();
		execUpdateExpress(list);
	}

	/**
	 * @param exprList
	 *            表达式Bean列表。表达式Bean有3个属性：UPDATE_CONDS（更新条件），UPDATE_FIELD（更新字段名），
	 *            UPDATE_VALUE（更新表达式）。
	 */
	private void execUpdateExpress(List<Bean> exprList) {
		if (exprList == null || exprList.size() == 0) {
			return;
		}

		// Bean dataBean = this.wfAct.getProcess().getServInstBean();
		// boolean needUpdate = false;
		JavaScriptEngine jsEng = new JavaScriptEngine(this.getOutBean());
		// jsEng.addVar("param", paramBean);

		MindContextHelper mindContext = new MindContextHelper(this.wfAct, this.getDoUser());
		jsEng.addVar("mindContext", mindContext);

		for (int i = 0; i < exprList.size(); i++) {
			Bean expBean = exprList.get(i);
			String conds = expBean.getStr("UPDATE_CONDS");

			if (jsEng.isTrueScript(conds)) {
				String field = expBean.getStr("UPDATE_FIELD");
				String valueExp = expBean.getStr("UPDATE_VALUE");
				String value = jsEng.runScript(valueExp);

				this.getOutBean().set(field, value);
				// needUpdate = true;
			}
		}

		// if (needUpdate) {
		// updateServInst(dataBean);
		// }
	}
}
