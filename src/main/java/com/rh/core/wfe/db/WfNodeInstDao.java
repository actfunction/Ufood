package com.rh.core.wfe.db;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 节点实例 数据库操作类
 * 
 * @author wanglong
 * 
 */
public class WfNodeInstDao {

	private static Log log = LogFactory.getLog(WfNodeInstDao.class);

	/**
	 * 工作流节点实例 服务 code
	 */
	public static final String SY_WFE_NODE_INST_SERV = "SY_WFE_NODE_INST";

	/**
	 * 办结的时候，通过流程实例对象 删除节点实例对象 ， 真删
	 * 
	 * @param procInstId
	 *            流程实例对象ID
	 */
	public static void destroyNodeInstBeans(String procInstId) {
		Bean paramBean = new Bean();
		paramBean.set("PI_ID", procInstId);

		ServDao.destroys(SY_WFE_NODE_INST_SERV, paramBean);
	}

	/**
	 * 通过流程实例对象 删除节点实例对象 ， 假删
	 * 
	 * @param procInstId
	 *            流程实例对象ID
	 */
	public static void deleteNodeInstBeans(String procInstId) {
		Bean paramBean = new Bean();
		paramBean.set("PI_ID", procInstId);

		ServDao.deletes(SY_WFE_NODE_INST_SERV, paramBean);
	}

	/**
	 * 
	 * @param curNodeInst
	 *            当前节点实例对象
	 * @return 前一个节点实例
	 */
	public static Bean getPreNode(WfAct curNodeInst) {
		String preNodeInstId = curNodeInst.getNodeInstBean().getStr("PRE_NI_ID");
		Bean preNodeInstBean = ServDao.find(SY_WFE_NODE_INST_SERV, preNodeInstId);

		return preNodeInstBean;
	}

	/**
	 * 
	 * @param preNodeInst
	 *            前一个节点实例ID
	 * @return 根据前一个节点实例ID，取得由它送出的下一个节点的实例
	 */
	public static List<Bean> getNextNodeInstList(String preNodeInst) {
		Bean paramBean = new Bean();
		paramBean.set("PRE_NI_ID", preNodeInst);
		paramBean.set(Constant.PARAM_ORDER, " NODE_BTIME DESC");

		log.debug("PRE_NI_ID " + preNodeInst);
		List<Bean> nodeInstList = ServDao.finds(SY_WFE_NODE_INST_SERV, paramBean);

		return nodeInstList;
	}

	/**
	 * @param pid
	 *            流程实例ID
	 * @param nodeCode
	 *            节点编码
	 * @param toUserId
	 *            接收人
	 * @return 指定节点和办理人活动的节点列表
	 */
	public static List<Bean> getListByPreNodeInstAndToUser(String pid, String nodeCode, String toUserId) {
		Bean paramBean = new Bean();
		paramBean.set("PI_ID", pid);
		paramBean.set("NODE_CODE", nodeCode);
		paramBean.set("TO_USER_ID", toUserId);
		paramBean.set("NODE_IF_RUNNING", WfeConstant.NODE_IS_RUNNING);
		paramBean.set(Constant.PARAM_ORDER, " NODE_BTIME DESC");

		List<Bean> nodeInstList = ServDao.finds(SY_WFE_NODE_INST_SERV, paramBean);

		return nodeInstList;
	}

	/**
	 * 
	 * @param preNodeInst
	 *            当前节点实例ID
	 * @return 由前一个节点实例 送出 的 所有 活动的 节点实例
	 */
	public static List<Bean> getLiveNodeInstListByPreNodeInst(String preNodeInst) {
		Bean paramBean = new Bean();
		paramBean.set("PRE_NI_ID", preNodeInst);
		paramBean.set("NODE_IF_RUNNING", WfeConstant.NODE_IS_RUNNING);

		List<Bean> liveNodeInstList = ServDao.finds(SY_WFE_NODE_INST_SERV, paramBean);

		return liveNodeInstList;
	}

	/**
	 * 未办结 根据流程实例ID 取得 流程活动的节点
	 * 
	 * @param piId
	 *            流程实例ID
	 * @return 流程活动的节点列表
	 */
	public static List<Bean> getRunningNodeInstByPiId(String piId) {
		Bean paramBean = new Bean();
		paramBean.set("PI_ID", piId);
		paramBean.set("NODE_IF_RUNNING", WfeConstant.WFE_NODE_INST_IS_RUNNING);

		return ServDao.finds(SY_WFE_NODE_INST_SERV, paramBean);
	}

	/**
	 * @param piId
	 *            流程实例ID
	 * @return 取得指定流程未结节点数量
	 */
	public static int getRunningNodeInstCount(String piId) {
		SqlBean bean = new SqlBean();
		bean.and("PI_ID", piId);
		bean.and("NODE_IF_RUNNING", WfeConstant.WFE_NODE_INST_IS_RUNNING);
		return ServDao.count(SY_WFE_NODE_INST_SERV, bean);
	}

	/**
	 * 未办结 取得 节点实例的 历史信息
	 * 
	 * @param piId
	 *            流程实例ID
	 * @return 节点实例的 历史信息
	 */
	public static List<Bean> getNodeInstHisByNoFinishPiId(String piId) {
		Bean paramBean = new Bean();
		paramBean.set("PI_ID", piId);

		// 还处于活动的节点， NODE_ETIME 为 NULL,
		// oracle认为 null 最大。升序排列，默认情况下，null值排后面。降序排序，默认情况下，null值排前面。
		// 而 mysql 认为 null 最小， 所以这里改成按 NODE_BTIME 去排序
		paramBean.set(Constant.PARAM_ORDER, " NODE_BTIME DESC");

		return ServDao.finds(SY_WFE_NODE_INST_SERV, paramBean);
	}

	/**
	 * 插入流程节点实例信息
	 * 
	 * @param nodeBean
	 *            节点信息
	 * @return Bean 节点实例
	 */
	public static Bean insertWfNodeInst(Bean nodeBean) {
		Bean nodeInstBean = ServDao.create(SY_WFE_NODE_INST_SERV, nodeBean);

		return nodeInstBean;
	}

	/**
	 * 通过节点实例ID 取得 节点实例对象
	 * 
	 * @param niId
	 *            节点实例ID
	 * @return 节点实例对象
	 */
	public static Bean findNodeInstById(String niId) {
		Bean aNodeInstBean = ServDao.find(SY_WFE_NODE_INST_SERV, niId);

		if (null == aNodeInstBean) {
			String errorMsg = Context.getSyMsg("SY_WF_NODE_INST_ID_ERROR", niId);

			throw new RuntimeException(errorMsg);
		}
		
		return aNodeInstBean;
	}

	/**
	 * 更新流程节点实例信息
	 * 
	 * @param nodeBean
	 *            节点信息
	 */
	public static void updateWfNodeInst(Bean nodeBean) {

		ServDao.update(SY_WFE_NODE_INST_SERV, nodeBean);

	}

	/**
	 * 办结的时候，复制节点实例列表 到 节点实例历史表
	 * 
	 * @param procInstId
	 *            节点实例对象ID
	 */
	public static void copyNodeInstBeansToHis(String procInstId) {
		log.debug("copy the node inst data to the history table");

		String sqlStr = "insert into " + WfNodeInstHisDao.SY_WFE_NODE_INST_HIS_SERV + " (select * from "
				+ SY_WFE_NODE_INST_SERV + " where PI_ID = '" + procInstId + "')";

		Context.getExecutor().execute(sqlStr);
	}

	/**
	 * 取得流程的节点实例历史列表
	 * 
	 * @param procInstBean
	 *            流程实例
	 * @return 流程实例历史表
	 */
	public static List<Bean> findNodeInstHisList(Bean procInstBean) {

		// 未办结查询实例表，已办结查询实例历史表
		if (procInstBean.getInt("INST_IF_RUNNING") == WfeConstant.WFE_PROC_INST_IS_RUNNING) {
			return getNodeInstHisByNoFinishPiId(procInstBean.getId());
		} else {
			return WfNodeInstHisDao.getNodeInstHisByFinishPiId(procInstBean.getId());
		}
	}

	/**
	 * 
	 * @param procInstId
	 *            流程实例ID
	 * @param nodeCode
	 *            节点编码
	 * @param procIsRunning
	 *            流程数据在运行表
	 * @return 指定节点的流程节点实例Bean列表，按照结束时间排序。
	 */
	public static List<Bean> findNodeInstList(String procInstId, String nodeCode, boolean procIsRunning) {
		if (procIsRunning) {
			Bean paramBean = new Bean();
			paramBean.set("PI_ID", procInstId);
			paramBean.set("NODE_CODE", nodeCode);

			paramBean.set(Constant.PARAM_ORDER, " NODE_ETIME DESC");

			return ServDao.finds(SY_WFE_NODE_INST_SERV, paramBean);
		} else {
			Bean paramBean = new Bean();
			paramBean.set("PI_ID", procInstId);
			paramBean.set("NODE_CODE", nodeCode);

			paramBean.set(Constant.PARAM_ORDER, " NODE_ETIME DESC");

			return ServDao.finds(ServMgr.SY_WFE_NODE_INST_HIS, paramBean);
		}
	}

	/**
	 * 取得流程的节点实例历史列表
	 * 
	 * @param procInstId
	 *            流程实例ID
	 * @param procIsRunning
	 *            流程是否运行状态
	 * @return 流程实例历史表
	 */
	public static List<Bean> findNodeInstHisList(String procInstId, boolean procIsRunning) {

		// 未办结查询实例表，已办结查询实例历史表
		if (procIsRunning) {
			return getNodeInstHisByNoFinishPiId(procInstId);
		} else {
			return WfNodeInstHisDao.getNodeInstHisByFinishPiId(procInstId);
		}
	}

	/**
	 * 查询流程中的第一个节点实例
	 * 
	 * @param procInstId
	 *            流程实例对象ID
	 * @param procIsRunning
	 *            流程是否运行状态
	 * @return nodeInstBean
	 */
	public static Bean findFirstNodeInstBeanByProcId(String procInstId, boolean procIsRunning) {
		List<Bean> nodeInstBeanList = findNodeInstHisList(procInstId, procIsRunning);
		int count = nodeInstBeanList.size();
		if (count == 0) {
			String errorMsg = Context.getSyMsg("SY_WF_NOT_FOUND_NODE_INST", procInstId);

			throw new RuntimeException(errorMsg);
		}
		return nodeInstBeanList.get(count - 1);
	}

	/**
	 * 查询流程中的最后的节点实例
	 * 
	 * @param procInstId
	 *            流程实例对象ID
	 * @param procIsRunning
	 *            流程是否运行状态
	 * @return nodeInstBean
	 */
	public static Bean findLastNodeInstBeanByProcId(String procInstId, boolean procIsRunning) {
		List<Bean> nodeInstBeanList = findNodeInstHisList(procInstId, procIsRunning);
		int count = nodeInstBeanList.size();
		if (count == 0) {
			String errorMsg = Context.getSyMsg("SY_WF_NOT_FOUND_NODE_INST", procInstId);

			throw new RuntimeException(errorMsg);
		}
		return nodeInstBeanList.get(0);
	}

	/**
	 * 送给我办理的最后一条记录（离当前时间最近的记录） ， 收回的是要取done_user
	 * 
	 * @param procInstId
	 *            流程实例ID
	 * @param userCode
	 *            用户编码
	 * @return 用户最近的节点实例Bean
	 */
	public static Bean findUserLastDoneNodeInst(String procInstId, String userCode) {
		Bean paramBean = new Bean();
		paramBean.set("PI_ID", procInstId);
		paramBean.set("DONE_USER_ID", userCode);

		paramBean.set(Constant.PARAM_ORDER, " NODE_ETIME DESC");

		List<Bean> nodeInstBeanList = ServDao.finds(SY_WFE_NODE_INST_SERV, paramBean);

		if (nodeInstBeanList.size() == 0) {
			throw new RuntimeException("没有找到该流程流过的记录");
		}

		return nodeInstBeanList.get(0);
	}

	/**
	 * 送给我办理的最后一条记录（离当前时间最近的记录） ， 收回的是要取done_user
	 * 
	 * @param procInstId
	 *            流程实例ID
	 * @param userCode
	 *            用户编码
	 * @return 用户最近的节点实例Bean
	 */
	public static Bean findUserLastToDo(String procInstId, String userCode) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("PI_ID", procInstId);
		sqlBean.and("TO_USER_ID", userCode);
		sqlBean.desc("S_ATIME");

		List<Bean> nodeInstBeanList = ServDao.finds(ServMgr.SY_WFE_NODE_USERS, sqlBean);

		if (nodeInstBeanList.size() > 0) {
			Bean rtn = nodeInstBeanList.get(0);

			Bean nodeInst = findNodeInstById(rtn.getStr("NI_ID"));

			return nodeInst;
		}

		return null;
	}

	/**
	 * 根据自己的where 查询返回节点实例列表
	 * 
	 * @param strWhere
	 *            查询语句
	 * @return 节点实例列表
	 */
	public static List<Bean> findNodeInstListByWhere(String strWhere) {
		Bean paramBean = new Bean();
		paramBean.set(Constant.PARAM_WHERE, strWhere);
		paramBean.set(Constant.PARAM_ORDER, " NODE_ETIME DESC");

		return ServDao.finds(SY_WFE_NODE_INST_SERV, paramBean);
	}

	/**
	 * 根据 PID 和 节点编码 查询 节点实例列表
	 * 
	 * @param procInstId
	 *            流程实例ID
	 * @param nodeCode
	 *            节点CODE
	 * @return 节点实例列表
	 */
	public static List<Bean> findListByPidAndNodeCode(String procInstId, String nodeCode) {
		Bean paramBean = new Bean();
		paramBean.set("PI_ID", procInstId);
		paramBean.set("NODE_CODE", nodeCode);
		paramBean.set(Constant.PARAM_ORDER, " NODE_ETIME DESC");

		return ServDao.finds(SY_WFE_NODE_INST_SERV, paramBean);
	}

	/**
	 * 
	 * @param procInstId
	 *            流程实例ID
	 * @return 是否已经会商
	 */
	public static boolean isParalled(String procInstId) {
		StringBuilder sqlStr = new StringBuilder();
		sqlStr.append("select PRE_NI_ID from ");

		sqlStr.append(SY_WFE_NODE_INST_SERV);
		sqlStr.append(" where PI_ID = '");
		sqlStr.append(procInstId);
		sqlStr.append("' and NODE_IF_RUNNING = 2 group by PRE_NI_ID having count(PRE_NI_ID) > 1");

		List<Bean> nodeInstList = Context.getExecutor().query(sqlStr.toString());

		if (nodeInstList.size() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * 更新流程节点实例信息
	 * 
	 * @param nodeBean
	 *            节点信息
	 */
	public static void updateWfNodeInst(Bean nodeBean, boolean isRunningData) {
		if (isRunningData) {
			ServDao.update(SY_WFE_NODE_INST_SERV, nodeBean);
		} else {
			ServDao.update(WfNodeInstHisDao.SY_WFE_NODE_INST_HIS_SERV, nodeBean);
		}
	}
}
