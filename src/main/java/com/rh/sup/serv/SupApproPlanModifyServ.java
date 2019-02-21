package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.sup.util.SupConstant;

import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * 申请修改成果计划
 * @author kfzx-wutao
 *
 */
public class SupApproPlanModifyServ extends CommonServ {

	private final static String SUP_APPRO_OFFICE = "OA_SUP_APPRO_OFFICE";
	private final static String SUP_APPRO_BUREAU = "OA_SUP_APPRO_BUREAU";
	private final static String OA_SUP_APPRO_OFFICE_PLAN = "OA_SUP_APPRO_OFFICE_PLAN";
	private final static String HOST_DEPT_LIMIT_TIME = "HostDeptLimitTime";
	
	/**
	 * 督查处审核更新新旧状态
	 * @param paramBean
	 */
	public void updatePlanState(ParamBean paramBean){
		//获取id
		String ID = paramBean.getStr("ID");
		
		//获取服务id
		String DeptCode = paramBean.getStr("DeptCode");
		
		//获取当前用户userBean
		UserBean userBean = Context.getUserBean();
		
		//构建sqlbean
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("APPRO_ID", "OA_SUP_APPRO_OFFICE");
		
		sqlBean.and("DEPT_CODE", DeptCode);
		sqlBean.and("UPDATE_STATE", "1");
	
		//获取当前正在使用的办理情况
		Bean bean = ServDao.find(SupConstant.OA_SUP_APPRO_PLAN, sqlBean);

		Bean updateBean = ServDao.find(SupConstant.OA_SUP_APPRO_PLAN, ID);
		updateBean.set("UPDATE_STATE", "1");
		
		ServDao.update(SupConstant.OA_SUP_APPRO_PLAN, updateBean);
		
		bean.set("UPDATE_STATE", "2");
		
		ServDao.update(SupConstant.OA_SUP_APPRO_PLAN, bean);
		
	}
	
	/**
	 * 取出部门
	 * @param servId
	 * @return
	 */
	private String getDepeCode(String servId){
		
		//获取当前用户userBean
		UserBean userBean = Context.getUserBean();

		String DeptCode = null;
		//判断司内 还是 署发
		if (SupConstant.OA_SUP_APPRO_OFFICE.equals(servId)) {
			//取出机构id
			DeptCode = userBean.getTDeptCode();
		} else if (SupConstant.OA_SUP_APPRO_BUREAU.equals(servId)){
			//取出部门id
			DeptCode = userBean.getDeptCode();
		}
		return DeptCode;
	}
	
	/**
	 * 判断当前是否为牵头主办单位
	 *
	 * @param paramBean
	 * @return
	 */
	public OutBean isHead(ParamBean paramBean) {

		String approId = paramBean.getStr("approId");

		String result = "1";
		String servId = isOfficeOrBuerau(approId);

		UserBean userBean = Context.getUserBean();

		if (SUP_APPRO_OFFICE.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("OFFICE_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getTDeptCode());
			sqlBean.and("DEPT_TYPE", "1");
			Bean host = ServDao.find("OA_SUP_APPRO_OFFICE_HOST", sqlBean);
			if (host != null) {
				result = "2";
			}

		} else if (SUP_APPRO_BUREAU.equals(servId)) {
			SqlBean sqlBean = new SqlBean();
			sqlBean.and("BUREAU_ID", approId);
			sqlBean.and("DEPT_CODE", userBean.getDeptCode());
			sqlBean.and("PART_TYPE", "1");
			Bean host = ServDao.find("OA_SUP_APPRO_BUREAU_HOST", sqlBean);
			if (host != null) {
				result = "2";
			}

		} 
		return new OutBean().set("result", result);
	}
	
	
	@Override
	protected void beforeSave(ParamBean paramBean) {
		
		Set<Object> keySet = paramBean.keySet();
		Iterator<Object> iterator = keySet.iterator();
		while(iterator.hasNext()){
			String key = (String) iterator.next();
			if (key.startsWith("s_")) {
				paramBean.set(key, "");
			}
		}
		paramBean.set("UPDATE_STATE", "3");
		paramBean.setAddFlag(true);
	
	}
	

	/**
	 * 获取当前正在使用的办理情况pk
	 * @param paramBean
	 * @return
	 */
	public OutBean getPk(ParamBean paramBean){
		String ID = paramBean.getStr("ID");
		Bean find = ServDao.find(SupConstant.OA_SUP_APPRO_PLAN, ID);
		//获取服务id
		String servId = SupConstant.OA_SUP_APPRO_OFFICE;
		//获取立项id
		String approId = find.getStr("APPRO_ID");
		//获取当前用户userBean
		UserBean userBean = Context.getUserBean();
		//构建sqlbean
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("APPRO_ID", approId);
		sqlBean.and("DEPT_CODE", find.getStr("DEPT_CODE"));
		sqlBean.and("UPDATE_STATE", "1");

		//获取当前正在使用的办理情况
		Bean bean = ServDao.find(SupConstant.OA_SUP_APPRO_PLAN, sqlBean);
		return new OutBean().set("PK", bean.getStr("PLAN_ID"));	
	}

	/**
	 * 获取pkcode
	 * @param paramBean
	 * @return
	 */
	public OutBean getPkCode(ParamBean paramBean){

		//获取立项id
		String approId = paramBean.getStr("APPRO_ID");

		//获取服务id
		String servId = paramBean.getStr("pservId");

		//获取当前用户userBean
		UserBean userBean = Context.getUserBean();

		//构建sqlbean
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("APPRO_ID", approId);
		String DeptCode = getDepeCode(servId);
		sqlBean.and("DEPT_CODE", DeptCode);
		sqlBean.and("UPDATE_STATE", "1");

		//获取当前正在使用的办理情况
		Bean bean = ServDao.find(SupConstant.OA_SUP_APPRO_PLAN, sqlBean);
		return new OutBean().set("PK", bean.getStr("PLAN_ID"));


	}
	
	/**
	 * 办结时的操作
	 */
	@Override
	public void afterFinish(ParamBean paramBean) {
		//获取流程实例编码
		String S_WF_INST = paramBean.getStr("PI_ID");
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("S_WF_INST", S_WF_INST);
		//通过流程实例编码获取当前办理计划
		Bean plan = ServDao.find(SupConstant.OA_SUP_APPRO_PLAN, sqlBean);
		
		updatePlan(	plan.getStr("APPRO_ID"),plan.getStr("DEPT_CODE"));
		//更新当前通过的变为使用的
		plan.set("UPDATE_STATE", "1");
		ServDao.update(SupConstant.OA_SUP_APPRO_PLAN, plan);
		
		updateOffice(plan.getStr("APPRO_ID"),plan.getStr("LIMIT_DATE"),plan.getStr("NOT_LIMIT_DATE_REASON"));
		updatePlan(plan.getStr("APPRO_ID"),plan.getStr("LIMIT_DATE"),plan.getStr("NOT_LIMIT_DATE_REASON"));
		
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
	 * 更新当前计划为不使用计划
	 * @param approId
	 * @param deptCode
	 */
	private void updatePlan(String approId ,String deptCode){
		//创建条件
		SqlBean sqlBean = new SqlBean();
		sqlBean.and("APPRO_ID", approId);
		sqlBean.and("DEPT_CODE", deptCode);
		sqlBean.and("UPDATE_STATE", "1");
		//根据条件得出结果
		Bean plan = ServDao.find(SupConstant.OA_SUP_APPRO_PLAN, sqlBean);
		plan.set("UPDATE_STATE","2");
		//更新
		ServDao.update(SupConstant.OA_SUP_APPRO_PLAN, plan);
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
}
