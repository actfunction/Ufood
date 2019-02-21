package com.rh.core.wfe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.DateUtils;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.core.wfe.db.WfNodeInstHisDao;
import com.rh.core.wfe.db.WfNodeUserDao;
import com.rh.core.wfe.db.WfNodeUserHisDao;
import com.rh.core.wfe.db.WfProcInstDao;
import com.rh.core.wfe.db.WfProcInstHisDao;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.GroupBean;
import com.rh.core.wfe.serv.SubProcessFinisher;
import com.rh.core.wfe.serv.WfSubProcActHandler;
import com.rh.core.wfe.util.WfTodoProvider;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 工作流流程实例对象。处理所有对流程实例的操作，如办结、取消办结、结束并发流
 * 
 * @author wanglong
 */
public class WfProcess extends AbstractWfProcess {
	private static Log log = LogFactory.getLog(WfProcess.class);

	/**
	 * 表单锁定状态
	 */
	public static final int PROC_INST_LOCK = 1;

	/**
	 * 表单未锁定
	 */
	public static final int PROC_INST_LOCK_NO = 2;

	/**
	 * 流程实例对象
	 */
	private Bean procInstBean;

	/**
	 * 流程绑定对象，关联表SY_WFE_PROC_ACT ,设置了绑定的按钮，提醒标题
	 */
	private Bean bindBean;

	private List<Bean> runningNodeInstList = null;

	private List<Bean> allNodeInstList = null;

	/**
	 * 构造方法
	 * 
	 * @param aProcInstBean
	 *            流程实例对象
	 */
	public WfProcess(Bean aProcInstBean) {
		this.procInstBean = aProcInstBean;

	}

	/**
	 * 构造方法
	 * 
	 * @param procInstId
	 *            流程实例对象ID
	 * @param isRunningData
	 *            流程数据是否保存在运行表中
	 */
	public WfProcess(String procInstId, boolean isRunningData) {
		init(procInstId, isRunningData);
	}

	/**
	 * 初始化
	 * 
	 * @param procInstId
	 *            流程实例对象ID
	 * @param isRunningData
	 *            流程数据是否保存在运行表中
	 */
	private void init(String procInstId, boolean isRunningData) {
		this.setIsRunningData(isRunningData);
		if (this.isRunningData()) {
			this.procInstBean = WfProcInstDao.findProcInstById(procInstId);
		} else {
			this.procInstBean = WfProcInstHisDao.findProcInstById(procInstId);
		}
	}

	/**
	 * 
	 * @param procInstId
	 *            流程实例ID
	 * @param wfState
	 *            流程状态
	 */
	public WfProcess(String procInstId, int wfState) {
		init(procInstId, procInstIsRunning(wfState));
	}

	/**
	 * 创建起草点 工作流节点实例 起草新的 工作流 向 流程实例表，节点实例表，节点历史表 插入 构造的相关数据
	 * 
	 * @param startUsers
	 *            起草任务处理人 没设置用户时，取当前登录用户
	 * @return 起始节点实例
	 */
	public WfAct createStartWfNodeInst(GroupBean startUsers) {
		log.debug("create a start procees node");

		// 插入新的节点实例
		Bean nodeInstBean = new Bean();

		WfNodeDef startNodeBean = getProcDef().findStartNode();

		if (startNodeBean == null) {
			throw new RuntimeException("没有找到编号为 " + this.getCode() + " 公司ID为 " + this.getCmpyId() + " 流程的起始节点");
		}
		// 增加起始点实例数据
		nodeInstBean.set("NODE_CODE", startNodeBean.getStr("NODE_CODE"));
		nodeInstBean.set("NODE_NAME", startNodeBean.getStr("NODE_NAME"));
		nodeInstBean.set("PROC_CODE", this.getCode());
		nodeInstBean.set("PI_ID", this.getId());
		nodeInstBean.set("NODE_IF_RUNNING", WfeConstant.NODE_IS_RUNNING);
		nodeInstBean.set("NODE_BTIME", DateUtils.getDatetime());
		nodeInstBean.set("S_CMPY", this.getCmpyId());
		nodeInstBean.set("NODE_CHILD_TYPE", startNodeBean.getStr("NODE_CHILD_TYPE"));
		nodeInstBean.set("IS_SCORE", startNodeBean.getStr("IS_SCORE"));
		// 起草人
		if (startUsers == null || startUsers.getUserIds().size() <= 0) {
			startUsers = new GroupBean();
			startUsers.addUser(Context.getUserBean().getId());
		}
		if (startUsers.getUserIds().size() == 1) {
			// 送交给单个用户 设置 TO_USER_ID TO_TYPE=3
			String userId = startUsers.getUserIdStr();
			nodeInstBean.set("TO_TYPE", WfeConstant.NODE_INST_TO_SINGLE_USER);
			nodeInstBean.set("TO_USER_ID", userId);
			nodeInstBean.set("TO_USER_NAME", UserMgr.getUser(userId).getName());
		} else {
			// 送交给多个用户 TO_TYPE=1
			nodeInstBean.set("TO_TYPE", WfeConstant.NODE_INST_TO_MULTI_USER);
		}

		// 保存起始点实例数据到数据库
		nodeInstBean = WfNodeInstDao.insertWfNodeInst(nodeInstBean);
		log.debug("the new start node inst id is " + nodeInstBean.getId());

		WfAct wfAct = new WfAct(this, nodeInstBean);
		wfAct.addNewInstUsers(wfAct.getId(), startUsers);

		wfAct.updateServWfInfo(null);

		wfAct.sendTodo();

		log.debug("after update serv data , and the act inst is " + wfAct.getId());
		return wfAct;
	}

	/**
	 * 根据paramBean中提供的toUser toDept toRole信息及要送交的节点的信息，解析出要生成的节点实例的任务处理人。
	 * 
	 * @param paramBean
	 *            流程实例信息，含有选择的人、部门、角色信息
	 * @return 节点实例的任务处理人列表
	 */
	public List<GroupBean> getNextActors(WfParam paramBean) {
		WfAct wfAct = new WfAct(paramBean.getStr("NI_ID"), true);
		return wfAct.getNextActors(paramBean.getStr("NODE_CODE"), paramBean);
	}

	/**
	 * 流程运行到下一节点。结束当前任务，创建下一节点任务。下一节点的每个任务处理人是一个GroupBean。
	 * 
	 * @param paramBean
	 *            流程运行参数 NI_ID NODE_CODE TO_USERS(List&lt;GroupBean&gt;类型)
	 * @return 创建的下一节点的任务实例
	 */
	public List<WfAct> toNext(WfParam paramBean) {
		List<GroupBean> nextActors = paramBean.getList("TO_USERS");
		String nextNodeCode = paramBean.getStr("NODE_CODE");

		Bean nodeInstBean = WfNodeInstDao.findNodeInstById(paramBean.getStr("NI_ID"));
		// if (nodeInstBean.getInt("NODE_IF_RUNNING") ==
		// WfeConstant.NODE_NOT_RUNNING) { //节点已经结束
		// throw new RuntimeException("节点已经办理结束，不能再次送交，请刷新当前页面！");
		// }

		WfAct wfAct = new WfAct(this, nodeInstBean);

		UserBean doUser = paramBean.getDoneUser();

		WfParam wfParam = new WfParam();
		wfParam.setDoneUser(doUser);
		wfParam.setIsAgent(paramBean.isAgent());
		wfParam.set("LINE_CODE", paramBean.getStr("LINE_CODE"));
		if (paramBean.isNotEmpty("NODE_LIMIT_TIME")) {
			wfParam.set("NODE_LIMIT_TIME", paramBean.getStr("NODE_LIMIT_TIME") + " 23:59:59");
		}

		// 修改最终意见
		Set<String> allSendUsers = new HashSet<String>();
		for (GroupBean userGroup : nextActors) {
			allSendUsers.addAll(userGroup.getUserIds());
		}
		wfAct.updateMind(doUser.getId(), allSendUsers.toArray(new String[allSendUsers.size()]), nextNodeCode);

		// 送下一个节点之后办结 , 先做节点实例办结，再送交，因为在送交的时候，需要将流程信息回填到表单
		if (nextActors.get(0).getUserIds().size() > 0) {
			// 如果不是非自动办结节点，则自动办结
			if (wfAct.getNodeDef().getInt("NODE_IF_AUTOEND") != WfeConstant.NODE_AUTO_END_NO
					&& nodeInstBean.getInt("NODE_IF_RUNNING") == WfeConstant.NODE_IS_RUNNING) {
				wfAct.finish(wfParam);
			}
		}

		return wfAct.toNext(nextNodeCode, wfParam, nextActors);
	}

	/**
	 * 结束流程 , 办结 1,修改当前节点实例对象 和 当前流程实例对象 的数据 2,修改数据对象的state为办结状态
	 * 3,实例表的数据复制到实例历史表 4,实例表的数据进行真删除 5,插入 流经数据 表
	 * 
	 * @param paramBean
	 *            用户对象
	 */
	public void finish(WfParam paramBean) {
		List<WfAct> list = this.getRunningWfAct();

		if (list.size() > 1) {
			throw new TipException("有多个活动节点正在办理中，不允许办结。");
		}

		UserBean doneUser = paramBean.getDoneUser();
		paramBean.setDoneType(WfeConstant.NODE_DONE_TYPE_FINISH);
		paramBean.setDoneDesc(WfeConstant.NODE_DONE_TYPE_FINISH_DESC);

		// 先 结束活动节点实例 ， 因为最后这个点也要算时间，
		list.get(0).finish(paramBean);

		// 结束流程实例
		procInstBean.set("INST_IF_RUNNING", WfeConstant.NODE_NOT_RUNNING);
		procInstBean.set("INST_ETIME", DateUtils.getDatetime());
		procInstBean.set("END_USER_ID", doneUser.getCode());

		procInstBean.set("INST_MIN", WfProcInstDao.getProcSumTime(this.getId()));

		WfProcInstDao.updateWfProcInst(procInstBean);

		// 先更新表单实例的状态
		list.get(0).updateServWfInfo(paramBean);

		moveInstToHis();

		if (isSubProcess()) {
			WfAct parentWfAct = this.getParentWfAct();
			WfSubProcActHandler wfSubProcActHandler = new WfSubProcActHandler(parentWfAct);
			if (wfSubProcActHandler.needResume()) {
				parentWfAct.resume();
			}
			SubProcessFinisher subProcessFinisher = wfSubProcActHandler.getSubProcessFinisher(this);
			if (subProcessFinisher != null) {
				subProcessFinisher.afterFinish(this);
			}
		}

		WfTodoProvider.sendTodoFinish(this, doneUser);
	}

	/**
	 * 根据节点实例 反推 参数对象
	 * 
	 * @param wfAct
	 *            节点实例
	 * @return 参数对象
	 */
	private WfParam genWfParmByAct(WfAct wfAct) {
		WfParam wfParam = new WfParam();

		String userId = wfAct.getNodeInstBean().getStr("DONE_USER_ID");

		wfParam.setToUser(userId);
		wfParam.setTypeTo(WfParam.TYPE_TO_USER);
		wfParam.setDoneUser(UserMgr.getUser(userId));

		return wfParam;
	}

	/**
	 * @return 绑定对象
	 */
	public Bean getBindBean() {
		return bindBean;
	}

	/**
	 * @return 流程第一个节点实例对象
	 */
	public WfAct getFirstWfAct() {
		Bean theFirstNodeInstBean = WfNodeInstDao.findFirstNodeInstBeanByProcId(this.getId(), this.isRunningData());

		WfAct theFirstNodeInst = new WfAct(this, theFirstNodeInstBean, this.isRunningData());

		return theFirstNodeInst;
	}

	/**
	 * 
	 * @param nodeCode
	 *            节点编码
	 * @return 取得指定节点最后办理的WfAct对象
	 */
	public WfAct getNodeLastWfAct(String nodeCode) {
		List<Bean> list = WfNodeInstDao.findNodeInstList(this.getId(), nodeCode, this.isRunningData());
		if (list.size() > 0) {
			Bean bean = list.get(0);
			return new WfAct(this, bean);
		}
		return null;
	}

	/**
	 * 
	 * @param niId
	 *            节点实例ID
	 * @return 取得指定节点实例NI_ID的WfAct对象
	 */
	public WfAct getWfAct(String niId) {
		Bean nodeInstBean = getNodeInstBean(niId);
		if (nodeInstBean != null) {
			return new WfAct(this, nodeInstBean);
		}

		return null;
	}

	/**
	 * 取得最后一个办理节点实例（按照NODE_ETIME 结束时间 排序），仅用于取消办结。
	 * 
	 * @return 节点实例对象
	 */
	public WfAct getLastWfAct() {
		Bean theLastNodeInstBean = WfNodeInstDao.findLastNodeInstBeanByProcId(this.getId(), this.isRunningData());

		WfAct theLastNodeInst = new WfAct(this, theLastNodeInstBean);
		return theLastNodeInst;
	}

	/**
	 * @return 流程实例对象
	 */
	public Bean getProcInstBean() {
		return procInstBean;
	}

	/**
	 * @return 取得所有活动的节点实例对象
	 */
	public List<WfAct> getRunningWfAct() {
		List<Bean> nodeInstList = getRunningNodeInstList();
		List<WfAct> wfNodeInstList = new ArrayList<WfAct>();
		for (Bean nodeInstHisBean : nodeInstList) {

			WfAct wfNodeInst = new WfAct(this, nodeInstHisBean);

			wfNodeInstList.add(wfNodeInst);
		}

		return wfNodeInstList;
	}

	/**
	 * @return 正在运行的活动的节点实例数量
	 */
	public int getRunningNodeInstCount() {
		return getRunningNodeInstList().size();
	}

	/**
	 * 取消办结的时候，将数据从历史表移动到实例表
	 */
	private void moveHisToInst() {
		// 根据流程实例ID 恢复 流程实例表
		WfNodeUserHisDao.copyHisToNodeUser(this.getId());
		WfNodeInstHisDao.copyNodeInstHisBeansToInst(getId());
		procInstBean = WfProcInstHisDao.copyProcInstHisBeanToInst(this.procInstBean);

		// 删除流程实例 历史记录
		WfNodeUserHisDao.destroyHisNodeUser(this.getId());
		WfNodeInstHisDao.destroyNodeInstHisBeans(getId());
		WfProcInstHisDao.delProcInstBeanFromNodeInstHIS(procInstBean);
	}

	/**
	 * 办结之前移动数据到历史表
	 */
	private void moveInstToHis() {
		// 将流程实例对象复制到历史表
		WfProcInstHisDao.copyProcInstBeanToHis(procInstBean);
		// 删除流程实例表对象,真删
		WfProcInstDao.destroyById(procInstBean);

		// 将节点实例 列表 复制到 历史表
		WfNodeInstDao.copyNodeInstBeansToHis(this.getId());
		// 删除节点实例表真删
		WfNodeInstDao.destroyNodeInstBeans(this.getId());

		// 将节点实例人 复制到 历史表
		WfNodeUserDao.copyNodeUserToHis(this.getId());
		// 删除节点用户实例表 真删
		WfNodeUserDao.destroyNodeUserToHis(this.getId());
	}

	/**
	 * 
	 * @return 能否取消办结
	 */
	public boolean canUndoFinish() {
		UserBean currUser = Context.getUserBean();

		String doneUserCode = this.getLastWfAct().getNodeInstBean().getStr("DONE_USER_ID");

		if (this.isProcManage() || currUser.getCode().equals(doneUserCode)) {
			return true;
		}

		return false;
	}

	/**
	 * 取消办结 (需在知道流程实例ID的情况下取消) 1,历史实例表数据恢复到实例表 2,历史实例表数据 删除 3,修改实例表中相关字段的值 TODO
	 * 取消办结的参数，是不是自动取出来，而不是传进来
	 * 
	 * @return 取消办结之后，添加新的一个节点实例。
	 */
	public WfAct undoFinish() {
		// 从已经办结的 办结节点实例历史表取第最后一个处理的节点实例
		WfAct wfAct = getLastWfAct();

		moveHisToInst();

		// 重置流程实例 和 节点实例对象
		procInstBean.set("INST_IF_RUNNING", WfeConstant.WFE_PROC_INST_IS_RUNNING);
		procInstBean.set("INST_ETIME", "");
		procInstBean.set("END_USER_ID", "");

		WfProcInstDao.updateWfProcInst(procInstBean);

		// 修改原有节点的说明。
		wfAct.changeDoneType(WfeConstant.NODE_DONE_TYPE_UNDO, WfeConstant.NODE_DONE_TYPE_UNDO_DESC);

		WfParam paramBean = genWfParmByAct(wfAct);

		WfAct newWfAct = wfAct.toNext(wfAct.getCode(), paramBean);
		UserBean doUser = Context.getUserBean();

		if (isSubProcess()) {
			WfAct parentWfAct = this.getParentWfAct();
			WfSubProcActHandler wfSubProcActHandler = new WfSubProcActHandler(parentWfAct);
			if (wfSubProcActHandler.needStop()) {
				parentWfAct.stop(doUser, WfeConstant.NODE_DONE_TYPE_STOP, WfeConstant.NODE_DONE_TYPE_STOP_DESC);
			}
			SubProcessFinisher subProcessFinisher = wfSubProcActHandler.getSubProcessFinisher(this);
			if (subProcessFinisher != null) {
				subProcessFinisher.afterUndoFinish(this);
			}
		}

		return newWfAct;
	}

	/**
	 * 流程跟踪，节点实例列表 按照时间倒序
	 * 
	 * @return 节点实例列表 按照时间倒序
	 */
	public List<Bean> wfTracking() {
		List<Bean> wfNodeInstList = WfNodeInstDao.findNodeInstHisList(this.getProcInstBean());

		return wfNodeInstList;
	}

	/**
	 * 没办结的才能收回
	 * 
	 * @param doUser
	 *            办理用户
	 * @return 用户的最后的节点
	 */
	public WfAct getUserLastDoneWfAct(UserBean doUser) {
		Bean nodeInstBean = WfNodeInstDao.findUserLastDoneNodeInst(this.getId(), doUser.getCode());

		return new WfAct(nodeInstBean.getId(), true);
	}

	/**
	 * @param doUser
	 *            办理用户
	 * @return 用户的最后的节点
	 */
	public WfAct getUserLastToDoWfAct(UserBean doUser) {
		Bean nodeInstBean;
		if (this.isRunningData()) {
			nodeInstBean = WfNodeInstDao.findUserLastToDo(this.getId(), doUser.getCode());
		} else {
			nodeInstBean = WfNodeInstHisDao.findUserLastToDo(this.getId(), doUser.getCode());
		}
		if (null == nodeInstBean) {
			return null;
		}
		// 直接通过该节点实例构造 节点实例对象 ，
		return new WfAct(this, nodeInstBean);
	}

	/**
	 * 
	 * @return 替换后的流程提醒标题
	 */
	public String getProcInstTitle() {
		Bean bean = this.getServInstBean();
		ServDefBean servDef = ServUtils.getServDef(this.getServId());
		String result = ServUtils.replaceValues(servDef.getDataTitle(), this.getServId(), bean);
		return result;
	}

	/**
	 * 判断用户是否有删除权限，如果可以删除，且流程为未结，那么把流程信息从活动表移到历史表。
	 * 
	 * @param doUser
	 *            办理用户
	 */
	public void delete(UserBean doUser) {
		if (canDelete(doUser)) {
			if (this.isRunning() && this.isRunningData()) {
				moveInstToHis(); // 把流程信息放到历史表中
				WfTodoProvider.deleteTodo(this.getServId(), this.getDocId()); // 删除待办
			}
		} else {
			throw new RuntimeException("没有删除权限，不能执行删除操作");
		}
	}

	/**
	 * 彻底删除流程数据
	 */
	public void destory() {
		if (this.isRunningData()) {
			// 删除流程实例表对象,真删
			WfProcInstDao.destroyById(procInstBean);
			// 删除节点实例表真删
			WfNodeInstDao.destroyNodeInstBeans(this.getId());
			// 删除节点用户实例表 真删
			WfNodeUserDao.destroyNodeUserToHis(this.getId());

			WfTodoProvider.deleteTodo(this.getServId(), this.getDocId()); // 删除待办

			WfTodoProvider.deleteTodoHis(this.getServId(), this.getDocId()); // 删除已办
		} else {
			// 删除流程实例
			WfProcInstHisDao.delProcInstBeanFromNodeInstHIS(procInstBean);
			// 删除节点实例
			WfNodeInstHisDao.destroyNodeInstHisBeans(this.getId());
			// 删除节点实例人
			WfNodeUserHisDao.destroyHisNodeUser(this.getId());
		}
	}

	/**
	 * 恢复已经删除的流程信息。如果流程为已结，那么不做任何变化，如果流程为未结，则把流程信息由历史表复制到活动表，同时发送待办信息。
	 */
	public void restore() {
		if (this.isRunning()) {
			this.moveHisToInst(); // 恢复流程数据
			// 恢复待办
			List<WfAct> list = this.getRunningWfAct();
			for (WfAct wfAct : list) {
				wfAct.sendTodo();
			}
		}
	}

	/**
	 * TODO 特殊角色
	 * 
	 * @param doUser
	 *            办理用户
	 * @return 能否删除流程实例
	 */
	public boolean canDelete(UserBean doUser) {
		List<Bean> nodeInstList = WfNodeInstDao.getNextNodeInstList(this.getFirstWfAct().getId());
		// 起草节点 , 还没送出去
		if (this.getFirstWfAct().isRunning() && null != nodeInstList) {
			return true;
		}

		// 当前人所在地机构的层级小于 起草机构的 层级 才 能删除，
		UserBean procUserBean = UserMgr.getUser(this.getSUserId()); // 起草
		if (doUser.getODeptLevel() > procUserBean.getODeptLevel()) {
			return false;
		}

		// 流程管理员
		if (this.isProcManage()) {
			return true;
		}

		return false;
	}

	/**
	 * 
	 * @return 流程是够被锁定
	 */
	public boolean isLocked() {
		int lockState = this.getProcInstBean().getInt("INST_LOCK");

		if (PROC_INST_LOCK == lockState) {
			return true;
		}

		return false;
	}

	/**
	 * @return 取得正在运行的公文的list
	 */
	private List<Bean> getRunningNodeInstList() {
		if (this.runningNodeInstList == null) {
			this.runningNodeInstList = WfNodeInstDao.getRunningNodeInstByPiId(this.getId());
		}
		return this.runningNodeInstList;
	}

	/**
	 * 清除runningNodeInstList表的数据。
	 */
	protected void cleanRunnintNodeInstList() {
		this.runningNodeInstList = null;
	}

	/**
	 * 
	 * @param wfState
	 *            流程状态值
	 * @return 判断流程是否正在运行
	 */
	public static boolean procInstIsRunning(int wfState) {
		boolean procIsRunning = true;

		if (wfState == WfeConstant.WFE_PROC_INST_NOT_RUNNING) {
			procIsRunning = false;
		}

		return procIsRunning;
	}

	/**
	 * 
	 * @return 表单的紧急程度，从字段S_EMERGENCY 中取， 如果没有返回0
	 */
	public int getEmergency() {
		if (this.getServInstBean().isNotEmpty("S_EMERGENCY")) {
			return this.getServInstBean().getInt("S_EMERGENCY");
		}

		return 0;
	}

	/**
	 * 
	 * @return 该流程是否作为子流程运行
	 */
	public boolean isSubProcess() {
		String parentWfActId = this.getProcInstBean().getStr("INST_PARENT_NODE");
		return !parentWfActId.isEmpty();
	}

	/**
	 * 
	 * @return 父流程节点任务 该流程为主流程时，返回null
	 */
	public WfAct getParentWfAct() {
		String parentWfActId = this.getProcInstBean().getStr("INST_PARENT_NODE");
		if (parentWfActId.isEmpty()) {
			return null;
		} else {
			// 以后从流程状态表中查询
			boolean procIsRunning = true;
			Bean actBean = ServDao.find(ServMgr.SY_WFE_NODE_INST, parentWfActId);
			if (actBean == null) {
				actBean = ServDao.find(ServMgr.SY_WFE_NODE_INST_HIS, parentWfActId);
				procIsRunning = false;
			}
			return new WfAct(parentWfActId, procIsRunning);
		}
	}

	/**
	 * @return 能否取消办结
	 */
	public boolean canCancelFinish() {
		// 尚未办结
		if (this.isRunning()) {
			return false;
		} else {
			// 如果没有找到对应的流程的定义信息， 则也不能取消办结
			if (null == this.getProcDef() || this.getProcDef().isEmpty()) {
				return false;
			}

			String endUserCode = this.getProcInstBean().getStr("END_USER_ID");
			// 不是办结人，也不是流程管理员
			if (!Context.getUserBean().getCode().equals(endUserCode) && !this.isProcManage()) {
				return false;
			}
			// 子流程
			if (isSubProcess()) {
				WfAct parentWfAct = this.getParentWfAct();
				WfSubProcActHandler handler = new WfSubProcActHandler(parentWfAct);
				// 异步
				if (handler.isAsync()) {
					return true;
				}
				// 主流程已办结
				if (!parentWfAct.isRunning()) {
					return false;
				}
				// 主流程已经流转走了
				if (parentWfAct.getNextWfAct() != null) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 
	 * @param niId
	 *            节点实例ID
	 * @return 取得指定节点实例NI_ID的Bean对象
	 */
	public Bean getNodeInstBean(String niId) {
		List<Bean> nodeInstList = this.getAllNodeInstList();

		for (Bean bean : nodeInstList) {
			if (bean.getId().equals(niId)) {
				return bean;
			}
		}

		return null;
	}

	/**
	 * @return 取得所有的节点实例
	 */
	public List<Bean> getAllNodeInstList() {
		if (this.allNodeInstList == null) {
			this.allNodeInstList = WfNodeInstDao.findNodeInstHisList(this.getId(), this.isRunningData());
		}

		return this.allNodeInstList;
	}
}
