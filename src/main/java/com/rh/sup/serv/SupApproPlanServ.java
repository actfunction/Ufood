package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.DateUtils;
import com.rh.sup.util.SupConstant;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 成果体现扩展类
 * 
 * @author admin
 */
public class SupApproPlanServ extends CommonServ {
	private final static String SUP_APPRO_OFFICE = "OA_SUP_APPRO_OFFICE";
	private final static String SUP_APPRO_BUREAU = "OA_SUP_APPRO_BUREAU";

	/*
	 * 获取立项单中填写的主办单位和其他主办单位
	 */
	public OutBean getDept(ParamBean paramBean) {

		OutBean outBean = new OutBean();
		// 获取父服务编码
		String servId = paramBean.getStr("servId");
		if (SUP_APPRO_OFFICE.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("OFFICE_ID", paramBean.getStr("APPRO_ID"));
			sqlBean.and("DEPT_TYPE", "1");
			List<Bean> hostList = ServDao.finds("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
			outBean.set("hostList", hostList);
		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("BUREAU_ID", paramBean.getStr("APPRO_ID"));
			sqlBean.and("DEPT_TYPE", "1");
			List<Bean> hostList = ServDao.finds("OA_SUP_APPRO_BUREAU_HOST", sqlBean);
			outBean.set("hostList", hostList);
		}
		return outBean;
	}

	/*
	 * 判断当前是否为牵头单位
	 */
	public OutBean isLead(ParamBean paramBean) {
		String servId = paramBean.getStr("servId");
		String approId = paramBean.getStr("APPRO_ID");
		String result = "2";

		// 获取当前用户的信息
		UserBean userBean = Context.getUserBean();
		if (SUP_APPRO_OFFICE.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("OFFICE_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getTDeptCode());
			sqlBean.and("DEPT_TYPE", "1");
			Bean host = ServDao.find(SupConstant.OA_SUP_APPRO_OFFICE_HOST, sqlBean);
			if (host != null) {
				result = "1";
			}
		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("BUREAU_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getDeptCode());
			sqlBean.and("DEPT_TYPE", "1");
			Bean host = ServDao.find(SupConstant.OA_SUP_APPRO_BUREAU_HOST, sqlBean);
			if (host != null) {
				result = "1";
			}
		}
		return new OutBean().set("result", result);
	}

	/**
	 * 判断是否是牵头主办单位
	 * 
	 * @param deptCode
	 * @param approId
	 * @return
	 */
	public boolean isLead(String deptCode, String approId) {

		String servId = isOfficeOrBuerau(approId);

		boolean boo = false;

		// 获取当前用户的信息
		if (SUP_APPRO_OFFICE.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("OFFICE_ID", approId);
			sqlBean.and("DEPT_CODE", deptCode);
			sqlBean.and("DEPT_TYPE", "1");
			Bean host = ServDao.find(SupConstant.OA_SUP_APPRO_OFFICE_HOST, sqlBean);
			if (host != null) {
				boo = true;
			}
		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("BUREAU_ID", approId);
			sqlBean.and("DEPT_CODE", deptCode);
			sqlBean.and("DEPT_TYPE", "1");
			Bean host = ServDao.find(SupConstant.OA_SUP_APPRO_BUREAU_HOST, sqlBean);
			if (host != null) {
				boo = true;
			}
		}
		return boo;
	}
	
	
	/*
	 * 获取计划内容数据中展示的数据条数count
	 */
	public OutBean getPlanCount(ParamBean paramBean){
		//获取服务ID值
		String servId = paramBean.getStr("servId");
		OutBean outbean = new OutBean();
		//判断当前是署发还是司内流程
		if (SUP_APPRO_OFFICE.equals(servId)) {
			//署发
			String sql1 = "SELECT " + "COUNT(*) "
					+ "FROM SUP_APPRO_PLAN_CONTENT RIGHT JOIN "
					+ "( SELECT SUP_APPRO_PLAN.* FROM SUP_APPRO_PLAN LEFT JOIN SUP_APPRO_OFFICE "
					+ "ON SUP_APPRO_PLAN.APPRO_ID = SUP_APPRO_OFFICE.ID " + "WHERE SUP_APPRO_OFFICE.ID = '"
					+ paramBean.get("ID") + "' " + ") SUP_APPRO_PLAN "
					+ "ON SUP_APPRO_PLAN.PLAN_ID = SUP_APPRO_PLAN_CONTENT.PLAN_ID";
			System.out.println(sql1);
			//获取当前的办理阶段内容的数据条数
			Bean count = Transaction.getExecutor().query(sql1).get(0);
			outbean.set("count", count);
		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			//司内
			String sql2 = "SELECT " + "COUNT(*) "
					+ "FROM SUP_APPRO_PLAN_CONTENT RIGHT JOIN "
					+ "( SELECT SUP_APPRO_PLAN.* FROM SUP_APPRO_PLAN LEFT JOIN SUP_APPRO_BUREAU "
					+ "ON SUP_APPRO_PLAN.APPRO_ID = SUP_APPRO_BUREAU.ID " + "WHERE SUP_APPRO_BUREAU.ID = '"
					+ paramBean.get("ID") + "' " + ") SUP_APPRO_PLAN "
					+ "ON SUP_APPRO_PLAN.PLAN_ID = SUP_APPRO_PLAN_CONTENT.PLAN_ID";
			//获取当前的办理阶段内容的数据条数
			Bean count = Transaction.getExecutor().query(sql2).get(0);
			outbean.set("count", count);
		}
		return outbean;
	}
	
	
	/*
	 * 获取署发流程计划内容展示的数据
	 */
	public OutBean getOfficePlanContent(ParamBean param) {
		// 联合查询获取所有内容
		String sqlList = "SELECT " + "SUP_APPRO_PLAN_CONTENT.BEIGN_DATE," + "SUP_APPRO_PLAN_CONTENT.END_DATE,"
				+ "SUP_APPRO_PLAN_CONTENT.DETAIL_TEXT," + "SUP_APPRO_PLAN.DEPT_NAME," + "SUP_APPRO_PLAN.CHARGE_NAME,"
				+ "SUP_APPRO_PLAN.USER_NAME," + "SUP_APPRO_PLAN.LIMIT_DATE, " + "SUP_APPRO_PLAN.DEPT_CODE, "
				+ "SUP_APPRO_PLAN.PLAN_ID, SUP_APPRO_PLAN.PLAN_STATE " + "FROM SUP_APPRO_PLAN_CONTENT RIGHT JOIN "
				+ "( SELECT SUP_APPRO_PLAN.* FROM SUP_APPRO_PLAN LEFT JOIN SUP_APPRO_OFFICE "
				+ "ON SUP_APPRO_PLAN.APPRO_ID = SUP_APPRO_OFFICE.ID " + "WHERE SUP_APPRO_OFFICE.ID = '"
				+ param.get("ID") + "' " + ") SUP_APPRO_PLAN "
				+ "ON SUP_APPRO_PLAN.PLAN_ID = SUP_APPRO_PLAN_CONTENT.PLAN_ID ORDER BY END_DATE,BEIGN_DATE";

		List<Bean> planList = Transaction.getExecutor().query(sqlList);
		// 获取当前角色是否为牵头司，
		String deptType = "";
		// 判断当前节点为督察处，或办公厅时，查询所有数据，强制设置deptType为1
		String nodeCode = param.getStr("nodeCode");
		if ("N22".equals(nodeCode) || "N23".equals(nodeCode) || "N24".equals(nodeCode) || "N25".equals(nodeCode)
				|| "N26".equals(nodeCode) || "N27".equals(nodeCode) || "N28".equals(nodeCode)) {
			deptType = "1";
		} else {
			// 判断当前角色是否为牵头司，是则查看所有的，不是则只查看所属司下的计划内容
			String sql = "SELECT DEPT_TYPE FROM SUP_APPRO_OFFICE_DEPT WHERE DEPT_CODE = '" + param.get("Code")
					+ "' AND OFFICE_ID='" + param.get("ID") + "'";
			List<Bean> resultType = Transaction.getExecutor().query(sql);
			if (resultType != null && !resultType.isEmpty()) {
				deptType = resultType.get(0).get("DEPT_TYPE").toString();
			}
		}

		// 办公厅查看时，查询出牵头司
		String leaderDeptCode = "";
		String leaderSql = "SELECT DEPT_CODE FROM SUP_APPRO_OFFICE_DEPT WHERE OFFICE_ID = '" + param.get("ID") + "' AND DEPT_TYPE = '1'";
		List<Bean> resultType = Transaction.getExecutor().query(leaderSql);
		if (resultType != null && !resultType.isEmpty()) {
			leaderDeptCode = resultType.get(0).getStr("DEPT_CODE");
		}

		Map<String, ParamBean> map = new HashMap<>();
		for (Bean bean : planList) {
			if (!"1".equals(deptType)) {
				if (!param.get("Code").equals(bean.get("DEPT_CODE"))) {
					continue;
				}
			}

			if (map.get(bean.get("DEPT_CODE")) == null) {
				ParamBean paramBean = new ParamBean();
				paramBean.set("DEPT_NAME", bean.get("DEPT_NAME").toString());
				// 判断如果是牵头司，加上标记
				if(leaderDeptCode.equals(bean.getStr("DEPT_CODE"))){
					paramBean.set("DEPT_NAME", bean.get("DEPT_NAME").toString() + "(牵头)");
				}

				paramBean.set("PLAN_ID", bean.get("PLAN_ID").toString());
				paramBean.set("CHARGE_NAME", bean.get("CHARGE_NAME").toString());
				paramBean.set("USER_NAME", bean.get("USER_NAME").toString());
				paramBean.set("LIMIT_DATE", bean.get("LIMIT_DATE").toString());
				paramBean.set("DEPT_CODE", bean.get("DEPT_CODE").toString());
				paramBean.set("PLAN_STATE", bean.get("PLAN_STATE").toString());
				List<Bean> contentList = new ArrayList<>();
				paramBean.set("contentList", contentList);
				map.put(bean.get("DEPT_CODE").toString(), paramBean);
			}
			ParamBean paramBean2 = new ParamBean();
			paramBean2.set("DETAIL_TEXT", bean.get("DETAIL_TEXT").toString());
			paramBean2.set("BEIGN_DATE", bean.get("BEIGN_DATE").toString());
			paramBean2.set("END_DATE", bean.get("END_DATE").toString());
			ParamBean paramBean = map.get(bean.get("DEPT_CODE").toString());
			List<Bean> contentList = (List<Bean>) paramBean.get("contentList");
			contentList.add(paramBean2);
		}

		List<Bean> resultList = new ArrayList<>();
		for (String code : map.keySet()) {
			resultList.add(map.get(code));
		}
		OutBean outBean = new OutBean();
		outBean.set("planList", resultList);
		return outBean;
	}

	/*
	 * 获取司内流程计划内容展示的数据
	 */
	public OutBean getBureauPlanContent(ParamBean param) {
		// 联合查询获取所有内容
		String sqlList = "SELECT SUP_APPRO_PLAN_CONTENT.BEIGN_DATE,SUP_APPRO_PLAN_CONTENT.END_DATE,"
				+ "SUP_APPRO_PLAN_CONTENT.DETAIL_TEXT,SUP_APPRO_PLAN.DEPT_NAME,SUP_APPRO_PLAN.CHARGE_NAME,"
				+ "SUP_APPRO_PLAN.USER_NAME,SUP_APPRO_PLAN.LIMIT_DATE, SUP_APPRO_PLAN.DEPT_CODE, "
				+ "SUP_APPRO_PLAN.PLAN_ID, SUP_APPRO_PLAN.PLAN_STATE FROM SUP_APPRO_PLAN_CONTENT RIGHT JOIN "
				+ "( SELECT SUP_APPRO_PLAN.* FROM SUP_APPRO_PLAN LEFT JOIN SUP_APPRO_BUREAU "
				+ "ON SUP_APPRO_PLAN.APPRO_ID = SUP_APPRO_BUREAU.ID WHERE SUP_APPRO_BUREAU.ID = '"
				+ param.get("ID") + "' and  SUP_APPRO_PLAN.PLAN_STATE in('2','3') and SUP_APPRO_PLAN.UPDATE_STATE = '1' ) SUP_APPRO_PLAN "
				+ "ON SUP_APPRO_PLAN.PLAN_ID = SUP_APPRO_PLAN_CONTENT.PLAN_ID ORDER BY END_DATE,BEIGN_DATE";

		System.out.println(sqlList);
		List<Bean> planList = Transaction.getExecutor().query(sqlList);
		System.out.println(planList.size());



		// 判断当前节点为督察处时，查询所有数据
		String deptType = "";
		String nodeCode = param.getStr("nodeCode");
		if ("N22".equals(nodeCode) || "N23".equals(nodeCode) || "N24".equals(nodeCode) || "N25".equals(nodeCode)
				|| "N26".equals(nodeCode) || "N27".equals(nodeCode) || "N28".equals(nodeCode)) {
			deptType = "1";
		} else {
			// 判断当前角色是否为牵头司，是则查看所有的，不是则只查看所属司下的计划内容
			String sql = "SELECT DEPT_TYPE FROM SUP_APPRO_BUREAU_STAFF WHERE DEPT_CODE = '" + param.get("Code")
					+ "' AND BUREAU_ID='" + param.get("ID") + "'";
			List<Bean> resultType = Transaction.getExecutor().query(sql);

			if(!resultType.isEmpty()){
				deptType = resultType.get(0).get("DEPT_TYPE").toString();
				if (resultType != null && !resultType.isEmpty()) {
					deptType = resultType.get(0).get("DEPT_TYPE").toString();
				}
			}

		}

		// 办公厅查看时，查询出牵头司
		String leaderDeptCode = "";
		String leaderSql = "SELECT DEPT_CODE FROM SUP_APPRO_BUREAU_STAFF WHERE BUREAU_ID = '" + param.get("ID") + "' AND DEPT_TYPE = '1'";
		List<Bean> resultType = Transaction.getExecutor().query(leaderSql);
		if (resultType != null && !resultType.isEmpty()) {
			leaderDeptCode = resultType.get(0).getStr("DEPT_CODE");
		}


		Map<String, ParamBean> map = new HashMap<>();
		for (Bean bean : planList) {
			if (!"1".equals(deptType)) {
				if (!param.get("Code").equals(bean.get("DEPT_CODE"))) {
					continue;
				}
			}

			if (map.get(bean.get("DEPT_CODE")) == null) {
				ParamBean paramBean = new ParamBean();
				paramBean.set("DEPT_NAME", bean.get("DEPT_NAME").toString());
				if (leaderDeptCode.equals(bean.get("DEPT_CODE"))) {
					paramBean.set("DEPT_NAME", bean.get("DEPT_NAME").toString() + "(牵头)");
				}

				paramBean.set("PLAN_ID", bean.get("PLAN_ID").toString());
				paramBean.set("CHARGE_NAME", bean.get("CHARGE_NAME").toString());
				paramBean.set("USER_NAME", bean.get("USER_NAME").toString());
				paramBean.set("LIMIT_DATE", bean.get("LIMIT_DATE").toString());
				paramBean.set("DEPT_CODE", bean.get("DEPT_CODE").toString());
				paramBean.set("PLAN_STATE", bean.get("PLAN_STATE").toString());
				List<Bean> contentList = new ArrayList<>();
				paramBean.set("contentList", contentList);
				map.put(bean.get("DEPT_CODE").toString(), paramBean);
			}
			ParamBean paramBean2 = new ParamBean();
			paramBean2.set("DETAIL_TEXT", bean.get("DETAIL_TEXT").toString());
			paramBean2.set("BEIGN_DATE", bean.get("BEIGN_DATE").toString());
			paramBean2.set("END_DATE", bean.get("END_DATE").toString());
			ParamBean paramBean = map.get(bean.get("DEPT_CODE").toString());
			List<Bean> contentList = (List<Bean>) paramBean.get("contentList");
			contentList.add(paramBean2);
		}

		List<Bean> resultList = new ArrayList<>();
		for (String code : map.keySet()) {
			resultList.add(map.get(code));
		}

		// System.out.println("deptType====="+deptType);
		OutBean outBean = new OutBean();
		outBean.set("planList", resultList);
		return outBean;
	}
	/*
	 * 获取司内流程计划内容展示的数据
	 */
	public OutBean getBureauChceckPlanContent(ParamBean param) {
		// 联合查询获取所有内容
		String sqlList = "SELECT SUP_APPRO_PLAN_CONTENT.BEIGN_DATE,SUP_APPRO_PLAN_CONTENT.END_DATE,"
				+ "SUP_APPRO_PLAN_CONTENT.DETAIL_TEXT,SUP_APPRO_PLAN.DEPT_NAME,SUP_APPRO_PLAN.CHARGE_NAME,"
				+ "SUP_APPRO_PLAN.USER_NAME,SUP_APPRO_PLAN.LIMIT_DATE, SUP_APPRO_PLAN.DEPT_CODE, "
				+ "SUP_APPRO_PLAN.PLAN_ID, SUP_APPRO_PLAN.PLAN_STATE FROM SUP_APPRO_PLAN_CONTENT RIGHT JOIN "
				+ "( SELECT SUP_APPRO_PLAN.* FROM SUP_APPRO_PLAN LEFT JOIN SUP_APPRO_BUREAU "
				+ "ON SUP_APPRO_PLAN.APPRO_ID = SUP_APPRO_BUREAU.ID WHERE SUP_APPRO_BUREAU.ID = '"
				+ param.get("ID") + "' and SUP_APPRO_PLAN.UPDATE_STATE = '1' ) SUP_APPRO_PLAN "
				+ "ON SUP_APPRO_PLAN.PLAN_ID = SUP_APPRO_PLAN_CONTENT.PLAN_ID ORDER BY END_DATE,BEIGN_DATE";

		System.out.println(sqlList);
		List<Bean> planList = Transaction.getExecutor().query(sqlList);
		System.out.println(planList.size());



		// 判断当前节点为督察处时，查询所有数据
		String deptType = "";
		String nodeCode = param.getStr("nodeCode");
		if ("N22".equals(nodeCode) || "N23".equals(nodeCode) || "N24".equals(nodeCode) || "N25".equals(nodeCode)
				|| "N26".equals(nodeCode) || "N27".equals(nodeCode) || "N28".equals(nodeCode)) {
			deptType = "1";
		} else {
			// 判断当前角色是否为牵头司，是则查看所有的，不是则只查看所属司下的计划内容
			String sql = "SELECT DEPT_TYPE FROM SUP_APPRO_BUREAU_STAFF WHERE DEPT_CODE = '" + param.get("Code")
					+ "' AND BUREAU_ID='" + param.get("ID") + "'";
			List<Bean> resultType = Transaction.getExecutor().query(sql);

			if(!resultType.isEmpty()){
				deptType = resultType.get(0).get("DEPT_TYPE").toString();
				if (resultType != null && !resultType.isEmpty()) {
					deptType = resultType.get(0).get("DEPT_TYPE").toString();
				}
			}

		}

		String leaderDeptCode = "";
		String leaderSql = "SELECT DEPT_CODE FROM SUP_APPRO_BUREAU_STAFF WHERE BUREAU_ID = '" + param.get("ID") + "' AND DEPT_TYPE = '1'";
		List<Bean> resultType = Transaction.getExecutor().query(leaderSql);
		if (resultType != null && !resultType.isEmpty()) {
			leaderDeptCode = resultType.get(0).getStr("DEPT_CODE");
		}

		Map<String, ParamBean> map = new HashMap<>();
		for (Bean bean : planList) {
			if (!"1".equals(deptType)) {
				if (!param.get("Code").equals(bean.get("DEPT_CODE"))) {
					continue;
				}
			}

			if (map.get(bean.get("DEPT_CODE")) == null) {
				ParamBean paramBean = new ParamBean();
				paramBean.set("DEPT_NAME", bean.get("DEPT_NAME").toString());
				if (leaderDeptCode.equals(bean.get("DEPT_CODE"))) {
					paramBean.set("DEPT_NAME", bean.get("DEPT_NAME").toString() + "(牵头)");
				}

				paramBean.set("PLAN_ID", bean.get("PLAN_ID").toString());
				paramBean.set("CHARGE_NAME", bean.get("CHARGE_NAME").toString());
				paramBean.set("USER_NAME", bean.get("USER_NAME").toString());
				paramBean.set("LIMIT_DATE", bean.get("LIMIT_DATE").toString());
				paramBean.set("DEPT_CODE", bean.get("DEPT_CODE").toString());
				paramBean.set("PLAN_STATE", bean.get("PLAN_STATE").toString());
				List<Bean> contentList = new ArrayList<>();
				paramBean.set("contentList", contentList);
				map.put(bean.get("DEPT_CODE").toString(), paramBean);
			}
			ParamBean paramBean2 = new ParamBean();
			paramBean2.set("DETAIL_TEXT", bean.get("DETAIL_TEXT").toString());
			paramBean2.set("BEIGN_DATE", bean.get("BEIGN_DATE").toString());
			paramBean2.set("END_DATE", bean.get("END_DATE").toString());
			ParamBean paramBean = map.get(bean.get("DEPT_CODE").toString());
			List<Bean> contentList = (List<Bean>) paramBean.get("contentList");
			contentList.add(paramBean2);
		}

		List<Bean> resultList = new ArrayList<>();
		for (String code : map.keySet()) {
			resultList.add(map.get(code));
		}

		// System.out.println("deptType====="+deptType);
		OutBean outBean = new OutBean();
		outBean.set("planList", resultList);
		return outBean;
	}
	/*
	 * 拿到建议完成时限或者未完成时限要求的字段自动覆盖到主表中
	 */
	public OutBean saveTimeOrReanson(ParamBean paramBean) {
		SqlBean updateSqlBean = new SqlBean();
		updateSqlBean.set("LIMIT_DATE", paramBean.getStr("time"));
		updateSqlBean.set("NOT_LIMIT_TIME_REASON", paramBean.getStr("reason"));
		updateSqlBean.and("ID", paramBean.getStr("APPRO_ID"));
		ServDao.update("OA_SUP_APPRO_OFFICE", updateSqlBean);
		return new OutBean().setOk();
	}

	/*
	 * 署发流程中更新计划办理状态
	 */
	public void updatePlanWfState(ParamBean paramBean) {
		// 立项单主键
		String approId = paramBean.getStr("approId");
		// 当前计划办理状态
		String curState = paramBean.getStr("curState");
		// 更新后的计划办理状态
		String upState = paramBean.getStr("upState");
		// 办理机构
		String deptCode = paramBean.getStr("deptCode");
		// 流程实例ID
		String niId = paramBean.getStr("niId");

		// 如果参数中有流程实例ID，则判断上个节点处理人机构
		if (StringUtils.isNotEmpty(niId)) {
			deptCode = getWFDeptCode(niId);
		}
		SqlBean sql = new SqlBean();
		sql.and("APPRO_ID", approId);
		sql.and("PLAN_STATE", curState);
		if (!deptCode.equals("")) {
			sql.and("DEPT_CODE", deptCode);
		}
		Bean plan = ServDao.find("OA_SUP_APPRO_OFFICE_PLAN", sql);
		if (plan != null) {
			plan.set("PLAN_STATE", upState);
			ServDao.update("OA_SUP_APPRO_OFFICE_PLAN", plan);
			boolean lead = isLead(deptCode, approId);
			if (lead && upState.equals("3")) {
				updateOffice(approId, plan.getStr("LIMIT_DATE"), plan.getStr("NOT_LIMIT_DATE_REASON"));
				updatePlan(approId, plan.getStr("LIMIT_DATE"), plan.getStr("NOT_LIMIT_DATE_REASON"));
			}
		}
	}

	/**
	 * 获取上一节点处理人的DEPT_CODE
	 * @param niId 当前流程实例编码
	 * @return
	 */
	private String getWFDeptCode(String niId){
		//获取当前流程实例
		Bean nowNodeInst = ServDao.find("SY_WFE_NODE_INST", niId);
		String servId = nowNodeInst.getStr("PROC_CODE");
		//获取流程实例个数
		String piID = nowNodeInst.getStr("PI_ID");
		String plansql = "SELECT * FROM SY_WFE_NODE_INST WHERE PI_ID = '" + piID + "'";
		List<Bean> beans = Transaction.getExecutor().query(plansql);
		Bean preNodeInst = new Bean();
		for(int i=0;i<beans.size();i++){
			//获取上一流程实例id
			String preId = nowNodeInst.getStr("PRE_NI_ID");
			//获取上一流程实例
			preNodeInst = ServDao.find("SY_WFE_NODE_INST", preId);
			//获取上一流程所在节点
			String nodeCode = preNodeInst.getStr("NODE_CODE");
			if (servId.contains(SupConstant.OA_SUP_APPRO_BUREAU)) {
				if ("N22".equals(nodeCode)) {
					break;
				}
			} else if (servId.contains(SupConstant.OA_SUP_APPRO_OFFICE)) {
				if ("N4".equals(nodeCode)) {
					break;
				}
			}
			
			nowNodeInst = preNodeInst;
		}
		//获取上一流程实例操作用户
		String userCode = preNodeInst.getStr("DONE_USER_ID");
		//获取版本
		String procType = preNodeInst.getStr("PROC_CODE");
		// 上个处理人
		UserBean userBean = UserMgr.getUser(userCode);
		//构建返回值
		String deptCode = "";
		//判断署发和司内
		if (procType.contains(SupConstant.OA_SUP_APPRO_BUREAU)) {
			deptCode = userBean.getDeptCode();
		} else if (procType.contains(SupConstant.OA_SUP_APPRO_OFFICE)) {
			deptCode = userBean.getTDeptCode();
		}
		return deptCode;
	}
	
	/*
	 * 回显部门信息等信息数据
	 */
	public OutBean getLinkDept(ParamBean paramBean) {
		// 获取条件值
		String approId = paramBean.getStr("APPRO_ID");
		String servId = paramBean.getStr("servId");
		// 获取主键参数
		String ID = paramBean.getStr("ID");
		Bean planBean = ServDao.find("OA_SUP_APPRO_OFFICE_PLAN", ID);

		// 获取当前用户bean
		UserBean userBean = Context.getUserBean();
		// 构建deptCode
		String deptCode = null;
		// 判断当前是否是新建状况 如果是显示用的信息 不是的话显示表里面的数据
		if (planBean == null) {
			if (SUP_APPRO_OFFICE.equals(servId)) {
				deptCode = userBean.getTDeptCode();
			} else if (SUP_APPRO_BUREAU.equals(servId)) {
				deptCode = userBean.getDeptCode();
			}
			// 回显部门名称
			String deptName = DictMgr.getName("SY_ORG_DEPT_ALL", deptCode);
			OutBean outBean = new OutBean();
			outBean.set("deptName", deptName);

			//根据主键得到查询结果
			Bean find = ServDao.find(servId, approId);
			outBean.set("limitDate", find.getStr("LIMIT_DATE"));
			outBean.set("notLimitTimeReason", find.getStr("NOT_LIMIT_TIME_REASON"));
			
			return outBean;
		} else {
			deptCode = planBean.getStr("DEPT_CODE");
			// 回显部门名称
			String deptName = DictMgr.getName("SY_ORG_DEPT_ALL", deptCode);
			OutBean outBean = new OutBean();
			outBean.set("deptName", deptName);
			outBean.set("limitDate", planBean.getStr("LIMIT_DATE"));
			outBean.set("notLimitTimeReason", planBean.getStr("NOT_LIMIT_DATE_REASON"));
			
			return outBean;

		}
	}

	/*
	 * 校验数据
	 */
	public OutBean getCheckParam(ParamBean paramBean) {
		OutBean outBean = new OutBean();
		String planId = paramBean.getStr("PLAN_ID"); // 计划ID
		String approId = paramBean.getStr("APPRO_ID"); // 外键APPRO_ID
		String servId = paramBean.getStr("servId"); // 服务id
		String deptCode = paramBean.getStr("DEPT_CODE"); // 部门id

		// 根据planId获取计划单
		String plansql = "SELECT * FROM SUP_APPRO_PLAN WHERE PLAN_ID = '" + planId + "'";
		List<Bean> planList = Transaction.getExecutor().query(plansql);
		StringBuffer sb = new StringBuffer();
		if (planList.size() == 0) {
			sb.append("未找到计划单！");
			outBean.set("ERROR", sb.toString());
			return outBean;
		}
		Bean plan = planList.get(0);
		if (StringUtils.isBlank(plan.getStr("RES_STEP"))) {
			sb.append("成果体现/具体工作举措未填写！");
		}
		if (StringUtils.isBlank(plan.getStr("CHARGE_NAME"))) {
			sb.append("责任人不存在！");
		}
		// 根据planId获取content详情
		String contentsql = "SELECT * FROM SUP_APPRO_PLAN_CONTENT WHERE PLAN_ID = '" + planId + "'";
		List<Bean> contentList = Transaction.getExecutor().query(contentsql);
		if (contentList.size() == 0) {
			sb.append("计划单内容不存在！");
		}
		switch (servId) {
		case "OA_SUP_APPRO_OFFICE": {
			// 根据planId获取所属office
			String officesql = "SELECT * FROM SUP_APPRO_OFFICE WHERE ID = '" + approId + "'";
			Bean office = Transaction.getExecutor().query(officesql).get(0);
			// 判断当前角色是否为牵头司，是则查看所有的，不是则只查看所属司下的计划内容
			String sql = "SELECT DEPT_TYPE FROM SUP_APPRO_OFFICE_DEPT WHERE DEPT_CODE = '" + deptCode
					+ "' AND OFFICE_ID='" + office.get("ID") + "'";
			List<Bean> resultType = Transaction.getExecutor().query(sql);
			String deptType = resultType.get(0).get("DEPT_TYPE").toString();
			if (deptType == "1") {
				if ("".equals(plan.getStr("LIMIT_DATE")) && "".equals(plan.getStr("NOT_LIMIT_DATE_REASON"))) {
					sb.append("完成时限或未完成时限原因必填一个！");
				}
			}
			break;
		}
		case "OA_SUP_APPRO_BUREAU": {
			// 根据planId获取所属office
			String officesql = "SELECT * FROM SUP_APPRO_BUREAU WHERE ID = '" + approId + "'";
			Bean bureau = Transaction.getExecutor().query(officesql).get(0);
			// 判断当前角色是否为牵头司，是则查看所有的，不是则只查看所属司下的计划内容
			String sql = "SELECT DEPT_TYPE FROM SUP_APPRO_BUREAU_STAFF WHERE DEPT_CODE = '" + deptCode
					+ "' AND BUREAU_ID='" + bureau.get("ID") + "'";
			List<Bean> resultType = Transaction.getExecutor().query(sql);
			String deptType = resultType.get(0).get("DEPT_TYPE").toString();
			if (deptType == "1") {
				if ("".equals(plan.getStr("LIMIT_DATE")) && "".equals(plan.getStr("NOT_LIMIT_DATE_REASON"))) {
					sb.append("完成时限或未完成时限原因必填一个！");
				}
			}
			break;
		}
		}
		outBean.set("ERROR", sb.toString());
		return outBean;
	}

	/*
	 * 更新署发主办单位更新时间
	 */
	public void updateHostPlanUpdate(ParamBean paramBean) {
		String approId = paramBean.getStr("approId");// 立项单主键
		String curState = paramBean.getStr("curState");// 当前办理状态
		String deptCode = paramBean.getStr("deptCode");// 办理机构
		SqlBean sql = new SqlBean();
		sql.and("APPRO_ID", approId);
		sql.and("PLAN_STATE", curState);
		sql.and("DEPT_CODE", deptCode);

		Bean plan = ServDao.find("OA_SUP_APPRO_OFFICE_PLAN", sql);
		if (plan != null) {
			plan.set("UPDATE_DATE", DateUtils.getStringFromDate(new Date(), DateUtils.FORMAT_DATE));
			ServDao.update("OA_SUP_APPRO_OFFICE_PLAN", plan);
		}
	}

	/*
	 * 署发： 主办单位主要负责同志审核情况 信息
	 */
	public OutBean updateHostPlanCase(ParamBean paramBean) {
		// 获取条件信息
		UserBean userBean = Context.getUserBean();

		String approId = paramBean.getStr("approId");// 立项单主键
		String curState = paramBean.getStr("curState");// 当前办理状态
		String deptCode = paramBean.getStr("deptCode");// 办理机构
		SqlBean sql = new SqlBean();
		sql.and("APPRO_ID", approId);
		sql.and("PLAN_STATE", curState);
		sql.and("DEPT_CODE", deptCode);

		Bean plan = ServDao.find("OA_SUP_APPRO_OFFICE_PLAN", sql);
		plan.set("CHARGE_CASE", userBean.getCode());
		ServDao.update("OA_SUP_APPRO_OFFICE_PLAN", plan);

		return new OutBean();
	}

	/**
	 * 根据方法名来执行方法
	 * 
	 * @param paramBean
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public OutBean updatePlanMethods(ParamBean paramBean) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		// 取出方法要执行的方法
		String Methods = paramBean.getStr("Methods");

		// 取出要执行的方法
		String[] split = Methods.split(",");

		// 获取当前类的字节码文件
		Class<? extends SupApproPlanServ> gainClass = this.getClass();

		// 循环遍历方法名
		for (String string : split) {
			// 获取方法对象
			Method method = gainClass.getMethod(string, ParamBean.class);
			// 执行方法
			method.invoke(this, paramBean);
		}

		return new OutBean().setOk();
	}

	/**
	 * 更新主单信息
	 * 
	 * @param approId
	 * @param limitDate
	 * @param notLimitDateReason
	 */
	private void updateOffice(String approId, String limitDate, String notLimitDateReason) {
		// 获取署发立项
		Bean find = ServDao.find(SupConstant.OA_SUP_APPRO_OFFICE, approId);
		// 假如署发立项没有 查询司内立项
		if (find == null) {
			// 查询司内立项
			find = ServDao.find(SupConstant.OA_SUP_APPRO_BUREAU, approId);
			// 赋值
			find.set("LIMIT_DATE", limitDate);
			find.set("NOT_LIMIT_TIME_REASON", notLimitDateReason);
			// 更新司内
			ServDao.update(SupConstant.OA_SUP_APPRO_BUREAU, find);
			return;
		}
		// 赋值
		find.set("LIMIT_DATE", limitDate);
		find.set("NOT_LIMIT_TIME_REASON", notLimitDateReason);
		// 更新署发
		ServDao.update(SupConstant.OA_SUP_APPRO_OFFICE, find);
	}

	/**
	 * 更新计划
	 * 
	 * @param approId
	 * @param limitDate
	 * @param notLimitDateReason
	 */
	private void updatePlan(String approId, String limitDate, String notLimitDateReason) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("APPRO_ID", approId);
		List<Bean> finds = ServDao.finds(SupConstant.OA_SUP_APPRO_PLAN, sqlBean);
		finds.forEach(BeanAdapter -> {
			BeanAdapter.set("LIMIT_DATE", limitDate);
			BeanAdapter.set("NOT_LIMIT_DATE_REASON", notLimitDateReason);
			ServDao.update(SupConstant.OA_SUP_APPRO_PLAN, BeanAdapter);
		});
	}

	/**
	 * 判断立项单主键判断署发还是司内
	 * 
	 * @param approId
	 * @return
	 */
	private String isOfficeOrBuerau(String approId) {
		// 获取署发立项
		Bean find = ServDao.find(SupConstant.OA_SUP_APPRO_OFFICE, approId);

		// 构建返回值
		String result = "";

		// 假如署发立项没有 查询司内立项
		if (find == null) {
			// 查询司内立项
			find = ServDao.find(SupConstant.OA_SUP_APPRO_BUREAU, approId);
			if (find != null) {
				result = SupConstant.OA_SUP_APPRO_BUREAU;
			}
		} else {
			result = SupConstant.OA_SUP_APPRO_OFFICE;
		}
		return result;
	}

	/**
	 * 判断当前节点是否填值
	 * @param paramBean
	 * @return
	 */
	public OutBean isMaintain(ParamBean paramBean){
		
		// 获取服务id
		String servId = paramBean.getStr("servId");

		// 获取节点id
		String NID = paramBean.getStr("NID");

		// 获取主单id
		String approId = paramBean.getStr("APPRO_ID");

		// 获取当前用户信息
		UserBean userBean = Context.getUserBean();

		// 构建返回值信息
		OutBean outBean = new OutBean();
		outBean.set("isMaintain", true);

		// 根据条件判断
		if (SUP_APPRO_OFFICE.equals(servId)) {
			// 获取当前填写办理情况
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("APPRO_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getTDeptCode());
			sqlBean.and("PLAN_STATE", "1");
			Bean supApproPaln = ServDao.find(SupConstant.OA_SUP_APPRO_PLAN, sqlBean);

			// 判断当前是否保存，未保存 就找不到状态为1 的办理情况 当办理情况不为空的时候判断 该节点能维护的信息是否有值
			if (supApproPaln == null || supApproPaln.isEmpty()) {
				outBean.set("isMaintain", false);
			}

		} else if (SUP_APPRO_BUREAU.equals(servId)) {

			SqlBean sqlBean = new SqlBean();

			sqlBean.and("APPRO_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getDeptCode());
			sqlBean.and("PLAN_STATE", "1");
			Bean supApproPaln = ServDao.find(SupConstant.OA_SUP_APPRO_PLAN, sqlBean);
			// 判断当前是否保存，未保存 就找不到状态为1 的办理情况 当办理情况不为空的时候判断 该节点能维护的信息是否有值
			if (supApproPaln == null || supApproPaln.isEmpty()) {
				outBean.set("isMaintain", false);
			}
		} 
		return outBean;
	}
	
	/**
	 * 获取当前督察处审核的部门名称
	 * @param paramBean
	 * @return
	 */
	public OutBean getDispostDeptName(ParamBean paramBean){
		//获取当前流程实例
		String niId = paramBean.getStr("niId");
		//上个流程实例处理的部门
		String deptCode = getWFDeptCode(niId);
		
		return new OutBean().set("deptName", DictMgr.getName("SY_ORG_DEPT_ALL", deptCode));
	}
}
