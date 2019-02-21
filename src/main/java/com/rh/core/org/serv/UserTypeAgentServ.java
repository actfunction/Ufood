package com.rh.core.org.serv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.Strings;

/**
 * 用户业务委托办理服务类（支持按业务类型分别委托）
 * 
 * @author wanglong
 */
public class UserTypeAgentServ extends CommonServ {
	/** 委托状态：2 未启动 */
	private static final int AGT_STATUS_INIT = 0;
	/** 委托状态：1 委托中 */
	private static final int AGT_STATUS_RUN = 1;
	/** 委托状态：2 已停止 */
	private static final int AGT_STATUS_IDLE = 2;
	/** 主委托服务 */
	private static final String AGENT_MAIN_SERV = "SY_ORG_USER_TYPE_AGENT";
	/** 委托他人服务id */
	private static final String AGENT_FROM_SERV = "SY_ORG_USER_TYPE_AGENT_FROM";
	/** SUB_CODES中用户code和日期的分隔符 */
	private static final String SEPARATOR_CODE_AND_DATE = "#";

	/** 有效性检验结果：1 通过 */
	private static final int VALID_PASS = 1;
	/** 有效性检验结果：2 未通过，被委托人处于委托状态 */
	private static final int VALID_AGT = 2;
	/** 有效性检验结果：3 未通过，该委托已存在 */
	private static final int VALID_EXIST = 3;
	/** 有效性检验结果：4 未通过，被委托人已经处于被委托状态 */
	private static final int VALID_EXIST_2 = 4;
	/** 全部业务类型 */
	private static final String ALLTYPE = "_ALL_";
	/** 其它业务类型 */
	private static final String OTHERTYPE = "_OTHER_";

	/**
	 * (已停止使用)
	 * 
	 * 显示当前用户委托中的记录的卡片页及关联服务
	 * 
	 * @param paramBean
	 *            入参
	 * @return OutBean 跳转页
	 */
	public OutBean show(ParamBean paramBean) {
		ParamBean query = new ParamBean();
		query.set(Constant.PARAM_WHERE, " and S_USER='" + Context.getUserBean().getCode() + "'");
		Bean res = ServDao.find(paramBean.getServId(), query);
		String pkCode = "";
		if (res != null) {
			pkCode = res.getId();
		}
		OutBean outBean = new OutBean();
		return outBean
				.setToDispatcher("/sy/base/view/stdCardView.jsp?sId=" + paramBean.getServId() + "&pkCode=" + pkCode);
	}

	@Override
	protected void afterSave(ParamBean paramBean, OutBean outBean) {
		// 变更前数据
		Bean oldMainData = paramBean.getSaveOldData();
		int oldStatus = oldMainData.getInt("AGT_STATUS");
		String oldSdt = oldMainData.getStr("AGT_BEGIN_DATE");
		String oldEdt = oldMainData.getStr("AGT_END_DATE");
		// 变更后数据
		int newStatus = outBean.getInt("AGT_STATUS");
		String newSdt = outBean.getStr("AGT_BEGIN_DATE");
		String newEdt = outBean.getStr("AGT_END_DATE");
		// 主委托时间改变后，子委托需要重新计算有效时间
		if ((!(oldStatus == AGT_STATUS_RUN) && !(newStatus == AGT_STATUS_RUN))
				&& (!oldEdt.equals(newEdt) || !oldSdt.equals(newSdt))) {
			SqlBean sql;
			sql = new SqlBean();
			sql.and("AGT_ID", outBean.getStr("AGT_ID")).and("S_FLAG", Constant.YES_INT);
			// .and("FROM_USER_CODE", outBean.getStr("USER_CODE"));
			/** 查询主委托的所有子委托 **/
			List<Bean> agList = ServDao.finds(AGENT_FROM_SERV, sql);
			// 循环——修改一个委托
			for (Bean agent : agList) {
				String userCode = agent.getStr("USER_CODE");
				String fromUserCode = agent.getStr("FROM_USER_CODE");
				// 本人
				if (userCode.equals(fromUserCode)) {
					agent.set("AGT_BEGIN_DATE", newSdt);
					agent.set("AGT_END_DATE", newEdt);
					agent.set("VALID_BEGIN_DATE", newSdt);
					agent.set("VALID_END_DATE", newEdt);
					// 转办
				} else {
					agent.set("AGT_BEGIN_DATE", newSdt);
					agent.set("AGT_END_DATE", newEdt);
					// 判断是否修改有效时间，根据上一个送交节点的有效时间累积计算当前节点的有效时间
					String lastReAgtId = agent.getStr("LAST_REAGT_ID");
					/** 获取转办委托的上一个节点 **/
					Bean lastNode = ServDao.find(AGENT_FROM_SERV, lastReAgtId);
					String lastValidSdt = lastNode.getStr("VALID_BEGIN_DATE"); // 有效开始时间
					String lastValidEdt = lastNode.getStr("VALID_END_DATE"); // 有效结束时间
					if (DateUtils.isBefore(lastValidSdt, newSdt)) {
						lastValidSdt = newSdt;
					}
					if (DateUtils.isBefore(newEdt, lastValidEdt)) {
						lastValidEdt = newEdt;
					}
					agent.set("VALID_BEGIN_DATE", lastValidSdt);
					agent.set("VALID_END_DATE", lastValidEdt);
				}
				agent.set("newDateFlag", true);
				modifyAgent("single", agent, agent.getInt("AGT_STATUS"), AGT_STATUS_IDLE);
			}
		}
	}

	/**
	 * 委托调度方法（方便加同步）
	 * 
	 * @param paramBean
	 *            入参
	 * @return 结果信息
	 */
	public OutBean doAgentAction(ParamBean paramBean) {
		OutBean res;
		String action = paramBean.getStr("action");
		// 启用主/子委托
		if (action.equals("startAllAgent")) {
			res = startAllAgent(paramBean);
			// 停止主/子委托
		} else if (action.equals("stopAllAgent")) {
			res = stopAllAgent(paramBean);
			// 通过用户code停止主/子委托
		} else if (action.equals("stopAllAgentByUserCode")) {
			res = stopAllAgentByUserCode(paramBean);
			// 清除主/子委托
		} else if (action.equals("delPlan")) {
			res = delAllAgent(paramBean);
			// 添加子委托
		} else if (action.equals("addAgent")) {
			res = addAgent(paramBean);
			// 修改子委托（重启、修改）
		} else if (action.equals("modifyAgent")) {
			ParamBean param = new ParamBean();
			param.setId(paramBean.getId());
			/** 按主键查询子委托 **/
			Bean agent = ServDao.find(AGENT_FROM_SERV, param);
			res = modifyAgent("single", agent, agent.getInt("AGT_STATUS"), paramBean.getInt("nextStatus"));
			// 停止子委托
		} else if (action.equals("stopAgent")) {
			ParamBean param = new ParamBean();
			param.setId(paramBean.getId());
			/** 按主键查询子委托 **/
			Bean agent = ServDao.find(AGENT_FROM_SERV, param);
			res = stopAgent(agent);
			// 清除子委托
		} else if (action.equals("delAgent")) {
			ParamBean param = new ParamBean();
			param.setId(paramBean.getId());
			/** 按主键查询子委托 **/
			Bean agent = ServDao.find(AGENT_FROM_SERV, param);
			res = delAgent(agent);
			// 计算当前用户委托状态并清除过期委托
		} else if (action.equals("computeAgtStatus")) {
			res = computeAgtStatus(paramBean);
			// 无对应方法
		} else {
			res = new OutBean();
			res.setWarn("未找到对应方法");
		}
		return res;
	}

	/**
	 * 启动所有委托
	 * 
	 * @param mainAgtBean
	 *            主委托BEAN
	 * @return 结果信息
	 */
	private OutBean startAllAgent(ParamBean mainAgtBean) {
		OutBean out = new OutBean().setOk("启动完成");
		String fromUserCode = Context.getUserBean().getCode();

		SqlBean sql;
		// 检查是否还有主委托未停止
		sql = new SqlBean();
		sql.and("AGT_STATUS", AGT_STATUS_RUN).and("S_FLAG", Constant.YES_INT).and("USER_CODE", fromUserCode);
		/** 查询我的所有运行中的主委托 **/
		int runCount = ServDao.count(AGENT_MAIN_SERV, sql);
		if (runCount > 0) {
			return out.setError("请先停止其他委托");
		}

		// 修改主委托状态为启动
		sql = new SqlBean();
		mainAgtBean.set("AGT_STATUS", AGT_STATUS_RUN);
		mainAgtBean.set("REAL_END_DATE", "");
		/** 更新该条主委托为运行 **/
		ServDao.update(AGENT_MAIN_SERV, mainAgtBean);

		// 修改自己的用户委托状态为委托中
		Bean fromUserState = UserMgr.getUserStateOrCreate(fromUserCode);
		fromUserState.set("USER_AGT_FLAG", Constant.YES_INT);
		UserMgr.saveUserState(fromUserState);

		// ??未实现//提醒其还有多少条未转办委托

		// 查出所有非运行的子委托
		sql = new SqlBean();
		sql.and("AGT_ID", mainAgtBean.getId()).andNot("AGT_STATUS", AGT_STATUS_RUN).and("S_FLAG", Constant.YES_INT);
		// and("FROM_USER_CODE", fromUserCode);
		/** 按主委托主键查询我的所有未启动的子委托 **/
		List<Bean> agList = ServDao.finds(AGENT_FROM_SERV, sql);

		if (agList.size() > 0) {
			// 循环——修改一个委托
			for (Bean agent : agList) {
				out = modifyAgent("batch", agent, agent.getInt("AGT_STATUS"), AGT_STATUS_RUN);
				if (!out.isOk()) {
					out.setWarn("启动完成，部分委托失败");
				}
			}
		}

		// 启动时根据是否交办现有待办处理本人待办的BenchFlag字段
		if (out.isOkOrWarn()) {
			if (mainAgtBean.getInt("AGT_CURRTODO_FLAG") == Constant.NO_INT) {
				Bean setBean = new Bean();
				setBean.set("TODO_BENCH_FLAG", Constant.NO_INT);
				Bean whereBean = new Bean();
				whereBean.set(Constant.PARAM_WHERE, " and TODO_CATALOG <= 1  and OWNER_CODE ='" + fromUserCode + "'");
				ServDao.updates("SY_COMM_TODO", setBean, whereBean);
			}

			if (mainAgtBean.getInt("AGT_CURRTODO_FLAG") == Constant.YES_INT) {
				Bean setBean = new Bean();
				setBean.set("TODO_BENCH_FLAG", Constant.YES_INT);
				Bean whereBean = new Bean();
				whereBean.set(Constant.PARAM_WHERE, " and TODO_CATALOG <= 1  and OWNER_CODE ='" + fromUserCode + "'");
				ServDao.updates("SY_COMM_TODO", setBean, whereBean);
			}
		}

		return out;
	}

	/**
	 * 停止所有委托
	 * 
	 * @param mainAgtBean
	 *            主委托BEAN
	 * @return 结果信息
	 */
	private OutBean stopAllAgent(ParamBean mainAgtBean) {
		OutBean out = new OutBean();
		SqlBean sql;
		int currAgtStatus = mainAgtBean.getInt("AGT_STATUS");

		if (currAgtStatus == AGT_STATUS_RUN) {
			// 查询所有运行中子委托
			String userCode = mainAgtBean.isEmpty("USER_CODE") ? Context.getUserBean().getCode()
					: mainAgtBean.getStr("USER_CODE");
			sql = new SqlBean();
			sql.and("AGT_ID", mainAgtBean.getId()).and("S_FLAG", Constant.YES_INT).and("AGT_STATUS", AGT_STATUS_RUN);
			// .and("FROM_USER_CODE", fromUserCode);
			/** 按主委托主键查询我的所有运行中的子委托 **/
			List<Bean> agList = ServDao.finds(AGENT_FROM_SERV, sql);

			// 循环——取消一个委托
			for (Bean agent : agList) {
				agent.set("REAL_END_DATE", DateUtils.getDate());
				stopAgent(agent);
			}

			// 修改主委托状态为已停止
			mainAgtBean.set("AGT_STATUS", AGT_STATUS_IDLE);
			mainAgtBean.set("REAL_END_DATE", DateUtils.getDate());
			/** 更新该主委托为未启动 **/
			ServDao.update(AGENT_MAIN_SERV, mainAgtBean);

			// 修改委托人的用户委托状态为未委托
			Bean fromUserState = UserMgr.getUserStateOrCreate(userCode);
			fromUserState.set("USER_AGT_FLAG", Constant.NO_INT);
			UserMgr.saveUserState(fromUserState);
		} else if (currAgtStatus == AGT_STATUS_INIT) {
			// 查询所有未启动子委托
			sql = new SqlBean();
			sql.and("AGT_ID", mainAgtBean.getId());
			/** 按主委托主键查询我的所有未启动的子委托 **/
			List<Bean> agList = ServDao.finds(AGENT_FROM_SERV, sql);

			// 循环——真删除一个委托
			for (Bean agent : agList) {
				ServDao.delete(AGENT_FROM_SERV, agent);
			}

			/** 真删除该主委托 */
			ServDao.delete(AGENT_MAIN_SERV, mainAgtBean);
		}

		return out.setOk("取消成功");
	}

	/**
	 * 通过用户code停止主/子委托
	 * 
	 * @param paramBean
	 *            BEAN
	 * @return 结果信息
	 */
	private OutBean stopAllAgentByUserCode(ParamBean paramBean) {

		OutBean out = new OutBean();
		List<Bean> agents = findMainAgent(paramBean);
		if (agents.size() == 0) {
			Bean currUserState = UserMgr.getUserStateOrCreate(paramBean.getStr("USER_CODE"));
			currUserState.set("USER_AGT_FLAG", Constant.NO_INT);
			UserMgr.saveUserState(currUserState);
		} else {
			for (Bean agent : agents) {
				ParamBean agt = new ParamBean();
				agt.copyFrom(agent);
				stopAllAgent(agt);
			}
		}

		return out.setOk("取消成功");
	}

	/**
	 * 获得运行中的主委托列表
	 * 
	 * @param paramBean
	 *            参数
	 * @return agents 主委托列表
	 */
	private List<Bean> findMainAgent(ParamBean paramBean) {
		SqlBean sql = new SqlBean();
		sql.and("USER_CODE", paramBean.getStr("USER_CODE"));
		sql.and("AGT_STATUS", AGT_STATUS_RUN);
		sql.and("S_FLAG", Constant.YES_INT);
		List<Bean> agents = ServDao.finds(AGENT_MAIN_SERV, sql);
		return agents;
	}

	/**
	 * 清除所有委托
	 * 
	 * @param mainAgtBean
	 *            主委托BEAN
	 * @return 结果信息
	 */
	private OutBean delAllAgent(ParamBean mainAgtBean) {
		OutBean out = new OutBean();
		mainAgtBean.set("S_FLAG", Constant.NO_INT);
		stopAllAgent(mainAgtBean);
		return out.setOk("清除成功");
	}

	/**
	 * 添加一个委托(单独调用)
	 * 
	 * @param agent
	 *            子委托BEAN
	 * @return 结果信息
	 */
	private OutBean addAgent(Bean agent) {
		SqlBean sql;
		OutBean out = new OutBean().setOk();
		String userCode = agent.getStr("USER_CODE");
		String fromUserCode = Context.getUserBean().getCode();
		String toUserCode = agent.getStr("TO_USER_CODE");
		agent.set("FROM_USER_CODE", fromUserCode);
		String agtTypeStr = agent.getStr("AGT_TYPE_CODE");
		String[] agtTypes = agtTypeStr.split(Constant.SEPARATOR);
		// 本人
		if (userCode.equals(fromUserCode)) {
			userCode = fromUserCode;
			// 处理全部类型
			if (agtTypeStr.indexOf(ALLTYPE) >= 0) {
				agtTypeStr = ALLTYPE;
				agtTypes = agtTypeStr.split(Constant.SEPARATOR);
				// 清除除全部委托以外的业务委托
				Bean delBean = new SqlBean();
				delBean.set("AGT_ID", agent.getStr("AGT_ID")).set("TO_USER_CODE", toUserCode)
						.set("FROM_USER_CODE", fromUserCode).set("USER_CODE", userCode).set("S_FLAG", Constant.YES_INT);
				ServDao.deletes(AGENT_FROM_SERV, delBean);
			}
			// 检查是否已委托全部类型
			sql = new SqlBean();
			sql.and("AGT_ID", agent.getStr("AGT_ID")).and("TO_USER_CODE", toUserCode)
					.and("FROM_USER_CODE", fromUserCode).and("USER_CODE", userCode).and("AGT_TYPE_CODE", ALLTYPE)
					.and("S_FLAG", Constant.YES_INT);
			int count = ServDao.count(AGENT_FROM_SERV, sql);
			if (count > 0) {
				return out.setWarn("被委托已被委托全部业务");
			}
			// 循环类型
			for (String agtType : agtTypes) {
				agent.set("AGT_TYPE_CODE", agtType);
				// 检验有效性
				int checkRes = checkValid(agent, false);
				if (checkRes == VALID_PASS) {

					// 添加记录
					String randomPk = Lang.getUUID();
					agent.set("RE_AGT_ID", randomPk);
					agent.set("SRC_AGT_ID", agent.getStr("AGT_ID"));
					agent.set("REAGT_ID_PATH", randomPk);
					agent.set("LAST_REAGT_ID", "");
					agent.set("AGT_STATUS", agent.getInt("MAIN_AGT_STATUS"));
					agent.set("AGT_USER_PATH", userCode);
					agent.set("AGT_BEGIN_DATE", agent.getStr("AGT_BEGIN_DATE"));
					agent.set("AGT_END_DATE", agent.getStr("AGT_END_DATE"));
					agent.set("VALID_BEGIN_DATE", agent.getStr("AGT_BEGIN_DATE"));
					agent.set("VALID_END_DATE", agent.getStr("AGT_END_DATE"));
					if (agent.getInt("MAIN_AGT_STATUS") == AGT_STATUS_IDLE) {
						agent.set("REAL_END_DATE", DateUtils.getDate());
					}
					/** 添加子委托 **/
					ServDao.save(AGENT_FROM_SERV, agent);

					// 处理被委托用户的代理状态、代理用户、代理业务
					String validBeginDate = agent.getStr("VALID_BEGIN_DATE");
					String validEndDate = agent.getStr("VALID_END_DATE");
					treatAgentStatus("add", toUserCode, userCode, fromUserCode, agtType, validBeginDate, validEndDate,
							"", agent);

					if (out.isOk()) {
						out.setOk("添加成功");
					}
				} else if (checkRes == VALID_AGT) {
					out.setWarn("添加失败，被委托人已处于委托状态或是本人，请选择其他被委托人");
					break;
				} else {
					out.setWarn("完成，存在委托-业务关系重复，请重新设定");
					continue;
				}
			}
			// 转办
		} else {
			String lastReAgtId = agent.getStr("RE_AGT_ID");
			String randomPk = Lang.getUUID();
			agent.remove(Constant.KEY_ID);
			agent.set("RE_AGT_ID", randomPk);
			agent.set("REAGT_ID_PATH", agent.getStr("REAGT_ID_PATH") + Constant.SEPARATOR + randomPk);
			agent.set("LAST_REAGT_ID", lastReAgtId);
			agent.set("AGT_STATUS", agent.getInt("MAIN_AGT_STATUS"));
			agent.set("AGT_USER_PATH", agent.getStr("AGT_USER_PATH") + Constant.SEPARATOR + fromUserCode);
			agent.set("AGT_BEGIN_DATE", agent.getStr("AGT_BEGIN_DATE"));
			agent.set("AGT_END_DATE", agent.getStr("AGT_END_DATE"));
			if (agent.getInt("MAIN_AGT_STATUS") == AGT_STATUS_IDLE) {
				agent.set("REAL_END_DATE", DateUtils.getDate());
			}
			String validBeginDate = agent.getStr("VALID_BEGIN_DATE");
			String validEndDate = agent.getStr("VALID_END_DATE");
			if (DateUtils.isBefore(validBeginDate, agent.getStr("AGT_BEGIN_DATE"))) {
				validBeginDate = agent.getStr("AGT_BEGIN_DATE");
			}
			if (DateUtils.isBefore(agent.getStr("VALID_END_DATE"), validEndDate)) {
				validEndDate = agent.getStr("VALID_END_DATE");
			}
			if (DateUtils.isBefore(validEndDate, validBeginDate)) {
				return out.setWarn("转办时间不合适，请重新计划！");
			}
			agent.set("VALID_BEGIN_DATE", validBeginDate);
			agent.set("VALID_END_DATE", validEndDate);

			// 检验有效性
			int checkRes = checkValid(agent, false);
			if (checkRes == VALID_PASS) {

				/** 添加子委托 **/
				ServDao.save(AGENT_FROM_SERV, agent);

				// 处理被委托用户的代理状态、代理用户、代理业务
				treatAgentStatus("add", toUserCode, userCode, fromUserCode, agtTypeStr, validBeginDate, validEndDate,
						"", agent);

				out.setOk("添加成功");
			} else if (checkRes == VALID_AGT) {
				out.setWarn("转办失败，被委托人已处于委托状态或是本人，请选择其他被委托人");
			} else {
				out.setWarn("转办失败，存在委托-业务关系重复，请重新设定");
			}
		}
		return out;
	}

	/**
	 * 修改一个委托
	 * 
	 * @param batchFlag
	 *            批量修改标识
	 * @param agent
	 *            子委托BEAN
	 * @param currAgtStatus
	 *            改前状态
	 * @param nextAgtStatus
	 *            改后状态
	 * @return 出参
	 */
	private OutBean modifyAgent(String batchFlag, Bean agent, int currAgtStatus, int nextAgtStatus) {
		String toUserCode = agent.getStr("TO_USER_CODE");
		String userCode = agent.getStr("USER_CODE");
		String fromUserCode = agent.getStr("FROM_USER_CODE");
		String agtType = agent.getStr("AGT_TYPE_CODE");
		String validBeginDate = agent.getStr("VALID_BEGIN_DATE");
		String validEndDate = agent.getStr("VALID_END_DATE");

		// 单条修改
		if (batchFlag.equals("single")) {
			// 改前状态及改后状态是未启动
			if (!(currAgtStatus == AGT_STATUS_RUN) && !(nextAgtStatus == AGT_STATUS_RUN)) {
				// 检验有效性
				int checkRes = checkValid(agent, false);
				if (checkRes == VALID_PASS) {

					/** 修改子委托 **/
					ServDao.update(AGENT_FROM_SERV, agent);

					return new OutBean().setOk("修改成功");
				} else if (checkRes == VALID_AGT) {
					return new OutBean().setWarn("修改失败，被委托人已处于委托状态或是本人，请选择其他被委托人");
				} else {
					return new OutBean().setWarn("修改失败，委托-业务关系已经存在，请重新设定");
				}
				// 改前状态是未启动、改后状态是启动（恢复委托或恢复转办情况）
			} else if (!(currAgtStatus == AGT_STATUS_RUN) && nextAgtStatus == AGT_STATUS_RUN) {
				// 检验有效性
				int checkRes = checkValid(agent, true);
				if (checkRes == VALID_PASS) {

					// 保存状态为启动
					agent.set("AGT_STATUS", nextAgtStatus);
					agent.set("REAL_END_DATE", "");
					/** 修改子委托 **/
					ServDao.update(AGENT_FROM_SERV, agent);

					// 处理被委托用户的代理状态、代理用户、代理业务
					treatAgentStatus("add", toUserCode, userCode, fromUserCode, agtType, validBeginDate, validEndDate,
							"", agent);

					return new OutBean().setOk("成功");
				} else if (checkRes == VALID_AGT) {
					return new OutBean().setWarn("修改失败，被委托人已处于委托状态或是本人，请选择其他被委托人");
				} else {
					return new OutBean().setWarn("修改失败，委托-业务关系已经存在，请重新设定");
				}
			}
			return new OutBean().setOk("成功");
			// 批量修改（启动所有情况）
		} else {
			// 状态要变成启动
			if (nextAgtStatus == AGT_STATUS_RUN) {
				// 检验有效性
				int checkRes = checkValid(agent, true);
				if (checkRes == VALID_PASS) {

					// 保存状态为启动
					agent.set("AGT_STATUS", nextAgtStatus);
					agent.set("REAL_END_DATE", "");
					/** 修改子委托 **/
					ServDao.update(AGENT_FROM_SERV, agent);

					// 处理被委托用户的代理状态、代理用户、代理业务
					treatAgentStatus("add", toUserCode, userCode, fromUserCode, agtType, validBeginDate, validEndDate,
							"", agent);

					return new OutBean().setOk("成功");
				} else {
					return new OutBean().setWarn("完成，部分委托校验未通过");
				}
			}
			return new OutBean().setOk("成功");
		}
	}

	/**
	 * 取消一个委托
	 * 
	 * @param agent
	 *            子委托BEAN
	 * @return 结果信息
	 */
	private OutBean stopAgent(Bean agent) {
		OutBean out = new OutBean();
		SqlBean sql;
		String userCode = agent.getStr("USER_CODE");
		String fromUserCode = agent.getStr("FROM_USER_CODE");
		// 本人
		if (userCode.equals(fromUserCode)) {
			// 根据主键查询所有子孙委托
			String reAgtId = agent.getStr("RE_AGT_ID");
			sql = new SqlBean();
			sql.andLikeRT("REAGT_ID_PATH", reAgtId).and("S_FLAG", Constant.YES_INT);
			/** 按子委托主键查询所有子委托 **/
			List<Bean> agList = ServDao.finds(AGENT_FROM_SERV, sql);

			// 循环——单个委托
			for (Bean agt : agList) {

				// 保存状态为已停止
				agt.set("AGT_STATUS", AGT_STATUS_IDLE);
				agt.set("REAL_END_DATE", DateUtils.getDate());
				if (!agt.getStr("FROM_USER_CODE").equals(userCode)) {
					agt.set("S_FLAG", Constant.NO_INT);
				}
				/** 修改子委托 **/
				ServDao.update(AGENT_FROM_SERV, agt);

				// 处理被委托用户的代理状态、代理用户、代理业务
				String toUserCode = agt.getStr("TO_USER_CODE");
				String agtType = agt.getStr("AGT_TYPE_CODE");
				String validBeginDate = agt.getStr("VALID_BEGIN_DATE");
				String validEndDate = agt.getStr("VALID_END_DATE");
				String srcAgtId = agt.getStr("SRC_AGT_ID");
				treatAgentStatus("del", toUserCode, userCode, fromUserCode, agtType, validBeginDate, validEndDate,
						srcAgtId, agt);

			}
			// 转办
		} else {
			// 根据主键查询所有子孙委托
			String reAgtId = agent.getStr("RE_AGT_ID");
			sql = new SqlBean();
			sql.andLike("REAGT_ID_PATH", reAgtId).and("S_FLAG", Constant.YES_INT);
			/** 按子委托主键查询所有子委托 **/
			List<Bean> agList = ServDao.finds(AGENT_FROM_SERV, sql);

			// 循环——单个委托
			for (Bean agt : agList) {

				// 保存状态为已停止、假删除
				agt.set("AGT_STATUS", AGT_STATUS_IDLE);
				agt.set("S_FLAG", Constant.NO_INT);
				agt.set("REAL_END_DATE", DateUtils.getDate());
				/** 修改子委托 **/
				ServDao.update(AGENT_FROM_SERV, agt);

				// 处理被委托用户的代理状态、代理用户、代理业务
				String toUserCode = agt.getStr("TO_USER_CODE");
				String agtType = agt.getStr("AGT_TYPE_CODE");
				String validBeginDate = agt.getStr("VALID_BEGIN_DATE");
				String validEndDate = agt.getStr("VALID_END_DATE");
				String srcAgtId = agt.getStr("SRC_AGT_ID");
				treatAgentStatus("del", toUserCode, userCode, fromUserCode, agtType, validBeginDate, validEndDate,
						srcAgtId, agt);

			}
		}
		return out.setOk("停止成功");
	}

	/**
	 * 删除一个委托(必须先取消)
	 * 
	 * @param agent
	 *            子委托BEAN
	 * @return 结果信息
	 */
	private OutBean delAgent(Bean agent) {
		OutBean out = new OutBean();
		agent.set("REAL_END_DATE", DateUtils.getDate());
		// 判断状态是否是未启动
		if (agent.getInt("AGT_STATUS") != AGT_STATUS_RUN) {
			// 假删除记录
			agent.set("S_FLAG", Constant.NO_INT);
			/** 修改子委托 **/
			ServDao.update(AGENT_FROM_SERV, agent);
		} else {
			// 取消一个委托
			stopAgent(agent);
			// 假删除记录
			agent.set("S_FLAG", Constant.NO_INT);
			agent.set("AGT_STATUS", AGT_STATUS_IDLE);
			/** 修改子委托 **/
			ServDao.update(AGENT_FROM_SERV, agent);
		}
		return out.setOk("清除成功");
	}

	/**
	 * 检验有效性（被委托人不能处于委托状态、发出人-被委托人-业务唯一性）
	 * 
	 * @param agent
	 *            子委托BEAN
	 * @param cheackRun
	 *            是否限定委托中
	 * @return 是否有效标识
	 */
	private int checkValid(Bean agent, boolean cheackRun) {
		if (!agent.containsKey("newDateFlag")) {
			String agtId = agent.getStr("AGT_ID");
			String toUserCode = agent.getStr("TO_USER_CODE");
			String fromUserCode = agent.getStr("FROM_USER_CODE");
			String userCode = agent.getStr("USER_CODE");
			String agtType = agent.getStr("AGT_TYPE_CODE");

			// 检查被委托人不能处于委托状态
			if (userCode.equals(toUserCode)) {
				return VALID_AGT; // 被委托人不能是自己
			}
			Bean toUserState = UserMgr.getUserStateOrCreate(toUserCode);
			if (toUserState.getInt("USER_AGT_FLAG") == Constant.YES_INT) {
				return VALID_AGT; // 被委托人不能处于委托状态
			}

			// 检查发出人-被委托人-业务唯一性
			SqlBean sql;
			sql = new SqlBean();
			// sql.and("AGT_ID", agtId).and("TO_USER_CODE",
			// toUserCode).and("FROM_USER_CODE", fromUserCode)
			// .and("USER_CODE", userCode).and("AGT_TYPE_CODE",
			// agtType).and("S_FLAG", Constant.YES_INT);
			sql.and("AGT_ID", agtId).and("FROM_USER_CODE", fromUserCode).and("USER_CODE", userCode)
					.and("AGT_TYPE_CODE", agtType).and("S_FLAG", Constant.YES_INT);
			if (cheackRun) {
				sql.and("AGT_STATUS", AGT_STATUS_RUN);
			}
			/** 按主委托主键查询 **/
			int count = ServDao.count(AGENT_FROM_SERV, sql);
			if (count > 0) {
				return VALID_EXIST; // 发出人-被委托人-业务唯一性
			}
			sql = new SqlBean();
			sql.and("TO_USER_CODE", toUserCode).and("S_FLAG", Constant.YES_INT).and("AGT_STATUS", AGT_STATUS_RUN);
			/** 按主委托主键查询 **/
			count = ServDao.count(AGENT_FROM_SERV, sql);
			if (count > 0) {
				return VALID_EXIST_2; // 发出人-被委托人-业务唯一性
			}

		}
		return VALID_PASS;
	}

	/**
	 * 处理被委托用户的代理状态、代理用户、（代理业务）
	 * 
	 * @param actflag
	 *            动作标识
	 * @param toUserCode
	 *            被委托用户
	 * @param userCode
	 *            委托用户
	 * @param fromUserCode
	 *            发出用户
	 * @param typeCode
	 *            业务类型
	 * @param validStartDate
	 *            有效开始日期
	 * @param validEndDate
	 *            有效结束日期
	 * @param srcAgtId
	 *            所属原始主委托主键
	 * @param agent
	 *            子委托Bean
	 */
	private void treatAgentStatus(String actflag, String toUserCode, String userCode, String fromUserCode,
			String typeCode, String validStartDate, String validEndDate, String srcAgtId, Bean agent) {
		String today = DateUtils.getDate(); // 精确到日

		// 委托人信息
		Bean userState = UserMgr.getUserStateOrCreate(userCode);
		int userAgtFlag = userState.getInt("USER_AGT_FLAG");

		// 转交人信息
		Bean fromUserState = UserMgr.getUserState(fromUserCode);
		int fromUserAgtFlag = fromUserState.getInt("USER_AGT_FLAG");

		// 被委托人信息
		Bean toUserState = UserMgr.getUserState(toUserCode);
		String subCodeStr = toUserState.getStr("SUB_CODES");
		String[] subCodes = subCodeStr.split(Constant.SEPARATOR);

		// 添见
		if (actflag.equals("add")) {

			// do、nothing、remove
			String validAgtFlag = "do";
			// 本人
			if (userCode.equals(fromUserCode)) {
				if (fromUserAgtFlag == Constant.NO_INT) {
					validAgtFlag = "remove";
				}
				// 转办
			} else {
				if (userAgtFlag == Constant.NO_INT) {
					validAgtFlag = "remove";
				} else if (fromUserAgtFlag == Constant.NO_INT) {
					validAgtFlag = "nothing";
				}
			}

			// subCodeStr不为空
			if (subCodeStr.length() > 0) {

				boolean existFlag = false; // 假设被委托用户不存在于subCodeStr中
				for (String sub : subCodes) {
					String[] subArr = sub.split(SEPARATOR_CODE_AND_DATE);
					String uCode = subArr[0]; // 获取uCode
					String sdt = subArr[1]; // 获取开始时间
					String edt = subArr[2]; // 获取结束时间

					// 处理所有超期
					if (DateUtils.isBefore(edt, today)) {
						subCodeStr = Strings.removeValue(subCodeStr, sub);
						continue;
					}

					// subCodeStr包含usercode
					if (uCode.equals(userCode)) {
						if (validAgtFlag.equals("do")) {
							// 处理有效时间(有效时间段求并集，按设计是不会出现不相交的情况)
							if (DateUtils.isBefore(validStartDate, sdt)) {
								sdt = validStartDate;
							}
							if (DateUtils.isBefore(edt, validEndDate)) {
								edt = validEndDate;
							}
							subCodeStr = Strings.replace(subCodeStr, sub,
									uCode + SEPARATOR_CODE_AND_DATE + sdt + SEPARATOR_CODE_AND_DATE + edt);
						} else if (validAgtFlag.equals("remove")) {
							subCodeStr = Strings.removeValue(subCodeStr, sub);
						}
						existFlag = true;
					}
				}

				// subCodeStr不包含usercode
				if (!existFlag) {
					if (validAgtFlag.equals("do")) {
						subCodeStr = Strings.addValue(subCodeStr, userCode + SEPARATOR_CODE_AND_DATE + validStartDate
								+ SEPARATOR_CODE_AND_DATE + validEndDate);
					}
				}

				// subCodeStr为空
			} else {
				if (validAgtFlag.equals("do")) {
					subCodeStr = Strings.addValue(subCodeStr, userCode + SEPARATOR_CODE_AND_DATE + validStartDate
							+ SEPARATOR_CODE_AND_DATE + validEndDate);
				}
			}
			// 删除
		} else {
			// 获取委托结束方式标识（true：自动结束 false：手动结束）
			boolean endFlag = agent.get("endFlag", true);

			if (!endFlag) {
				return;
			}

			// subCodeStr不为空
			if (subCodeStr.length() > 0) {

				String oldUserSub = "";
				for (String sub : subCodes) {
					String[] subArr = sub.split(SEPARATOR_CODE_AND_DATE);
					String uCode = subArr[0]; // 获取uCode
					String edt = subArr[2]; // 获取结束时间

					// 处理所有超期
					if (DateUtils.isBefore(edt, today)) {
						subCodeStr = Strings.removeValue(subCodeStr, sub);
					}

					// 暂存被委托人未修改的sub
					if (uCode.equals(userCode)) {
						oldUserSub = sub;
					}
				}

				if (subCodeStr.indexOf(userCode) >= 0) {
					// 查询委托人委托给被委托人当前所有未过期的有效委托（启动中、未删除）
					SqlBean sql = new SqlBean();
					sql.and("TO_USER_CODE", toUserCode).and("USER_CODE", userCode).and("S_FLAG", Constant.YES_INT)
							.and("AGT_STATUS", AGT_STATUS_RUN).andGTE("VALID_END_DATE", today);
					if (!srcAgtId.isEmpty()) {
						sql.and("SRC_AGT_ID", srcAgtId);
					}
					/** 按主委托主键查询 **/
					List<Bean> agList = ServDao.finds(AGENT_FROM_SERV, sql);

					if (agList.size() > 0) {
						// 重新计算有效日期
						String sdt = "";
						String edt = "";
						for (Bean agt : agList) {
							String vSdt = agt.getStr("VALID_BEGIN_DATE");
							String vEdt = agt.getStr("VALID_END_DATE");
							if (sdt.length() == 0 || DateUtils.isBefore(vSdt, sdt)) {
								sdt = vSdt;
							}
							if (edt.length() == 0 || DateUtils.isBefore(edt, vEdt)) {
								edt = vEdt;
							}
						}
						subCodeStr = Strings.replace(subCodeStr, oldUserSub,
								userCode + SEPARATOR_CODE_AND_DATE + sdt + SEPARATOR_CODE_AND_DATE + edt);
					} else {
						subCodeStr = Strings.removeValue(subCodeStr, oldUserSub);
					}
				}
			}
		}

		// 重新保存被委托用户状态userStateBean
		toUserState.set("SUB_CODES", subCodeStr);
		UserMgr.saveUserState(toUserState);
	}

	/**
	 * 校准程序：重新计算用户委托情况
	 * 
	 * @param paramBean
	 *            参数
	 * @return 出参
	 */
	public OutBean computeAgtStatus(ParamBean paramBean) {
		SqlBean sql;

		// 清除过期委托
		String today = DateUtils.getDate(); // 精确到日
		sql = new SqlBean();
		sql.and("AGT_END_TYPE", 2).and("S_FLAG", Constant.YES_INT).and("AGT_STATUS", AGT_STATUS_RUN)
				.andLT("AGT_END_DATE", today);
		List<Bean> overDateAgents = ServDao.finds(AGENT_MAIN_SERV, sql);
		for (Bean agent : overDateAgents) {
			ParamBean agt = new ParamBean();
			agt.copyFrom(agent);
			stopAllAgent(agt);
		}
		// 更新用户被委托状态
		if (paramBean.contains("loginFlag") && paramBean.getBoolean("loginFlag")) {
			// 查询用户所有被委托事务
			String currUserCode = paramBean.isEmpty("USER_CODE") ? Context.getUserBean().getCode()
					: paramBean.getStr("USER_CODE");
			sql = new SqlBean();
			sql.and("TO_USER_CODE", currUserCode).and("S_FLAG", Constant.YES_INT).and("AGT_STATUS", AGT_STATUS_RUN)
					.andGTE("VALID_END_DATE", today);
			List<Bean> agents = ServDao.finds(AGENT_FROM_SERV, sql);
			// 先清除SUB_CODES
			Bean currUserState = UserMgr.getUserStateOrCreate(currUserCode);
			currUserState.set("SUB_CODES", "");
			UserMgr.saveUserState(currUserState);

			// 再重新根据被委托事务生成subcodes
			for (Bean agent : agents) {
				treatAgentStatus("add", currUserCode, agent.getStr("USER_CODE"), agent.getStr("FROM_USER_CODE"),
						agent.getStr("AGT_TYPE_CODE"), agent.getStr("VALID_BEGIN_DATE"), agent.getStr("VALID_END_DATE"),
						agent.getStr("SRC_AGT_ID"), agent);
			}
		}
		return getAgtUser(paramBean);
	}

	/**
	 * 获取代理用户列表
	 * 
	 * @param paramBean
	 *            入参
	 * @return 出参
	 */
	public OutBean getAgtUser(ParamBean paramBean) {
		String currUserCode = paramBean.getStr("FROM_USER_CODE");
		boolean existJiangang = paramBean.getBoolean("existJiangang");
		String subCodeStr = "";

		if (existJiangang) { // 存在兼岗
			String[] jianGroup = currUserCode.split(Constant.SEPARATOR);
			for (String uCode : jianGroup) {
				Bean toUserState = UserMgr.getUserStateOrCreate(uCode);
				subCodeStr = Strings.addValue(subCodeStr, toUserState.getStr("SUB_CODES"));
			}
			subCodeStr = Strings.removeSame(subCodeStr);
		} else { // 不存在兼岗
			if (currUserCode.isEmpty()) {
				if (Context.getUserBean() == null || Context.getUserBean().getCode().isEmpty()) {
					return new OutBean().setOk().setData(new ArrayList<Bean>());
				}
				currUserCode = Context.getUserBean().getCode();
			}
			Bean toUserState = UserMgr.getUserStateOrCreate(currUserCode);
			if (toUserState == null) {
				return new OutBean().setOk().setData(new ArrayList<Bean>());
			}
			// 获取被委托用户SUB_CODES
			subCodeStr = toUserState.getStr("SUB_CODES");
		}

		// 转换SUB_CODES为数组
		String[] subCodes = subCodeStr.split(Constant.SEPARATOR);
		if (subCodeStr.length() > 0) {
			List<Bean> subList = new ArrayList<Bean>();
			String today = DateUtils.getDate();
			for (String sub : subCodes) {
				String[] subArr = sub.split(SEPARATOR_CODE_AND_DATE);
				// 获取uCode
				String uCode = subArr[0];
				// 获取开始时间
				String sdt = subArr[1];
				// 获取结束时间
				String edt = subArr[2];
				if (DateUtils.isBefore(edt, today) || DateUtils.isBefore(today, sdt)) {
					continue;
				}
				Bean user = new Bean().set("aCode", uCode).set("aName", UserMgr.getUser(uCode).getName())
						.set("beginDate", sdt).set("endDate", edt);
				subList.add(user);
			}
			return new OutBean().setOk().setData(subList);
		} else {
			return new OutBean().setOk().setData(new ArrayList<Bean>());
		}
	}

	/**
	 * 生成查询指定用户委托办理记录的where条件
	 * 
	 * @param paramBean
	 *            入参
	 * @return 出参
	 */
	public OutBean getTodoAgentWhereByUserCode(ParamBean paramBean) {
		String today = DateUtils.getDate();
		String userCode = "";
		String toUserCode = "";
		SqlBean sql;
		String inSql = "";
		String notInSql = "";
		boolean otherFlag = false; // 是否包含其他业务标识
		StringBuffer wSb = new StringBuffer();
		boolean existJiangang = paramBean.getBoolean("existJiangang");

		if (!paramBean.containsKey("hisFlag") // 查委托
				|| !paramBean.getBoolean("hisFlag")) {
			toUserCode = paramBean.getStr("TO_USER_CODE").isEmpty() ? Context.getUserBean().getCode()
					: paramBean.getStr("TO_USER_CODE");
			Bean toUserState = UserMgr.getUserStateOrCreate(toUserCode);
			if (toUserState == null) {
				return new OutBean().setOk().setData(" and 1 = 2");
			}
			String subCodeStr = toUserState.getStr("SUB_CODES");
			userCode = paramBean.getStr("AGT_USER_CODE"); // 委托用户

			if (subCodeStr.indexOf(userCode) < 0) {
				return new OutBean().setError();
			}

			// 获取委托用户的委托业务串
			sql = new SqlBean();
			sql.and("USER_CODE", userCode);
			if (existJiangang) { // 存在兼岗
				sql.andIn("TO_USER_CODE", Strings.replace(toUserCode, ",", "','"));
			} else { // 不存在兼岗
				sql.and("TO_USER_CODE", toUserCode);
			}
			sql.and("S_FLAG", Constant.YES_INT);
			sql.and("AGT_STATUS", Constant.YES_INT);
			sql.andLTE("VALID_BEGIN_DATE", today);
			sql.andGTE("VALID_END_DATE", today);
			/****/
			List<Bean> agList = ServDao.finds(AGENT_FROM_SERV, sql);

			// 处理全部业务、一般业务
			for (Bean agent : agList) {
				String typeCode = agent.getStr("AGT_TYPE_CODE");
				if (typeCode.equals(ALLTYPE)) {
					otherFlag = false;
					break;
				}
				if (typeCode.equals(OTHERTYPE)) {
					otherFlag = true;
					continue;
				}
				if (inSql.length() == 0) {
					inSql = DictMgr.getName("SY_ORG_USER_AGT_TYPE_DETAIL", typeCode);
				} else {
					inSql = inSql + Constant.SEPARATOR + DictMgr.getName("SY_ORG_USER_AGT_TYPE_DETAIL", typeCode);
				}
			}

			// 处理“其它业务”
			if (otherFlag) {
				List<Bean> typeDetailList = DictMgr.getItemList("SY_ORG_USER_AGT_TYPE_DETAIL");
				for (Bean detail : typeDetailList) {
					String name = detail.getStr("NAME");
					if (!name.equals(ALLTYPE) && !name.equals(OTHERTYPE) && inSql.indexOf(name) < 0) {
						if (notInSql.length() == 0) {
							notInSql = name;
						} else {
							notInSql = notInSql + Constant.SEPARATOR + name;
						}
					}
				}
			}

			wSb.append(" and TODO_BENCH_FLAG = 1");
		} else { // 查委托历史（办理情况）
			userCode = paramBean.getStr("ownerCode");
			String agtTypeCode = paramBean.getStr("agtTypeCode");
			toUserCode = paramBean.getStr("toUserCode");
			String beginDate = paramBean.getStr("beginDate");
			String endDate = paramBean.getStr("realEndDate");
			if (endDate.isEmpty()) {
				endDate = today;
			}
			if (!toUserCode.isEmpty()) {
				wSb.append(" and AGT_USER_CODE = '" + toUserCode + "'");
			} else {
				wSb.append(" and AGT_USER_CODE is not null");
			}
			if (!beginDate.isEmpty()) {
				wSb.append(" and TODO_FINISH_TIME >= '" + beginDate + " 00:00:00'");
			}
			if (!endDate.isEmpty()) {
				wSb.append(" and TODO_FINISH_TIME <= '" + endDate + " 23:59:59'");
			}

			if (agtTypeCode.equals(ALLTYPE)) {
				agtTypeCode = "";
			}

			if (!agtTypeCode.isEmpty()) {
				if (agtTypeCode.equals(OTHERTYPE)) { // 处理“其它业务”
					List<Bean> typeDetailList = DictMgr.getItemList("SY_ORG_USER_AGT_TYPE_DETAIL");
					for (Bean detail : typeDetailList) {
						String name = detail.getStr("NAME");
						if (!name.equals(ALLTYPE) && !name.equals(OTHERTYPE)) {
							if (notInSql.length() == 0) {
								notInSql = name;
							} else {
								notInSql = notInSql + Constant.SEPARATOR + name;
							}
						}
					}
					otherFlag = true;
				} else { // 处理一般业务
					inSql = DictMgr.getName("SY_ORG_USER_AGT_TYPE_DETAIL", agtTypeCode);
				}
			}
		}

		// 获取委托记录
		wSb.append(" and OWNER_CODE ='" + userCode + "'");
		if (otherFlag) { // 其他业务sql
			if (StringUtils.isNotEmpty(notInSql)) {
				wSb.append(" and TODO_CODE not in (");
				wSb.append(notInSql);
				wSb.append(")");
			}
		} else { // 一般业务sql
			if (StringUtils.isNotEmpty(inSql)) {
				// wSb.append(" and TODO_CODE in (");
				// wSb.append(inSql);
				// wSb.append(")");
				String[] inSqlArr = inSql.split(",");
				wSb.append(" and ( ");
				String tmpStr = "";
				for (String sqlTmp : inSqlArr) {
					tmpStr += " or TODO_CODE like '" + sqlTmp + "%'";
				}
				wSb.append(tmpStr.substring(4));
				wSb.append(")");
			}
		}

		return new OutBean().setOk().setData(wSb.toString());

	}
}