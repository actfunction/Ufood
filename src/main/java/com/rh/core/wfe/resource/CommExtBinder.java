package com.rh.core.wfe.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.util.WfeConstant;

public class CommExtBinder implements ExtendBinder {

	private static Log log = LogFactory.getLog(CommExtBinder.class);
	
	private Bean dataBean = new Bean();
	
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		// 表单数据对象
		dataBean = currentWfAct.getProcess().getServInstBean();
		List<Bean> custList = nextNodeDef.findCustomBeanList();

		String deptCodes = nextNodeDef.getStr("NODE_DEPT_CODES");
		
		String roleCodes = "";
		String userCodes = "";
		
		String tRole = ""; //临时角色
		String tUser = ""; //临时用户
		boolean tTrue = true;

		for (Bean custBean : custList) {
			String var = custBean.getStr("BL_CODE");
			String opt = custBean.getStr("BL_CAOZUO");
			String value = custBean.getStr("BL_VALUE");
			String user = custBean.getStr("BL_USER");
			String role = custBean.getStr("BL_ROLE");
			String luoji = custBean.getStr("BL_LUOJI");
			
			boolean istrue = condition(var, opt, value);// 判断扩展条件

			if (luoji.length() > 0) { // 处理逻辑运算“与”
				if (luoji.equals("START")) { //开始
					tTrue = true;
					if (istrue) {
						tRole = fillVal(role, tRole);
						tUser = fillVal(user, tUser);
					} else {
						tTrue = false;
					}
				} else if (luoji.equals("DO")) { //+中间条件
					if (istrue && tTrue) {
						tRole = fillVal(role, tRole);
						tUser = fillVal(user, tUser);
					} else {
						tTrue = false;
					}
				} else if (luoji.equals("END")) { //结束
					if (istrue && tTrue) {
						tRole = fillVal(role, tRole);
						tUser = fillVal(user, tUser);

						roleCodes = fillVal(tRole, roleCodes);
						userCodes = fillVal(tUser, userCodes);
					} else {
						tTrue = false;
					}
					tRole = "";
					tUser = "";
				}
			} else if (istrue) { // 处理普通运算
				roleCodes = fillVal(role, roleCodes);
				userCodes = fillVal(user, userCodes);
			}
		}
		
		if (roleCodes.length() > 0) { //绑定角色:获取角色下人员
			
			List<UserBean> userBeanList = UserMgr.getUsersByRole(roleCodes);
			
			if (userBeanList != null && userBeanList.size() > 0) {
				for (UserBean user : userBeanList) {
					userCodes = fillVal(user.getCode(),userCodes);
				}
			}

		} else if (roleCodes.length() == 0 && userCodes.length() == 0) { //不符合扩展条件:读取组织资源			
			roleCodes = nextNodeDef.getStr("NODE_ROLE_CODES");
			userCodes = nextNodeDef.getStr("NODE_USER_CODES");
			Bean rtnDept = getDeptCodeList(currentWfAct,nextNodeDef,doUser);
			deptCodes = getDeptList(rtnDept);
		}
		
		ExtendBinderResult result = new ExtendBinderResult();
		result.setDeptIDs(deptCodes);
		result.setRoleCodes(roleCodes);
		result.setUserIDs(userCodes);
		result.setBindRole(false);
		result.setAutoSelect(false);
		return result;
	}

	private boolean condition(String vars, String opt, String values) {
		String[] avars = vars.split(",");
		String[] aval = values.split(",");
		boolean cond = false;
		try {
			for (int i = 0; i < avars.length; i++) {
				String var = dataBean.getStr(avars[i]);
				
				if ("等于".equals(opt)) {

					if (var.equals(getStr(aval, i))) {
						cond = true;
					} else {
						cond = false;
						break;
					}

				} else if ("不等于".equals(opt)) {
					
					if (!var.equals(getStr(aval, i))) {
						cond = true;
					} else {
						cond = false;
						break;
					}

				} else if ("大于".equals(opt)) {

					double dvar = Double.parseDouble(var.trim());
					
					if (dvar > getDou(aval, i)) {
						cond = true;
					} else {
						cond = false;
						break;
					}

				} else if ("小于".equals(opt)) {

					double dvar = Double.parseDouble(var.trim());

					if (dvar < getDou(aval, i)) {
						cond = true;
					} else {
						cond = false;
						break;
					}
				}
			}
		} catch (Exception e) {
			log.error("流程节点条件错误： " + vars + " " + opt + " " + values, e);
			return false;
		}
		
		return cond;
	}

	private String getStr(String aval[], int i) {
		if (aval.length > i) {
			return aval[i];
		} else {
			return aval[aval.length-1];
		}
	}

	private Double getDou(String aval[], int i) {
		if (aval.length > i) {
			return Double.parseDouble(aval[i]);
		} else {
			return Double.parseDouble(aval[aval.length-1]);
		}
	}
	
	private String fillVal(String fromVal, String toVal) {
		if (fromVal.length() > 0) {
			toVal += toVal.length() > 0 ? "," + fromVal : fromVal;
		}
		return toVal;
	}
	
	/**
	 * @return 节点上定义组织资源 的部门 串
	 */
	private Bean getDeptCodeList(WfAct curWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		int mode = nextNodeDef.getInt("NODE_DEPT_MODE");
		String resCode = nextNodeDef.getStr("NODE_DEPT_CODES");
		
		if (mode == WfeConstant.NODE_BIND_MODE_ALL) { // 全部部门
			String rootOdept = Context.getSyConf("ROOT_ODEPT_CODE", "ruaho0001");
			// 本机构下所有部门列表
			List<DeptBean> deptList = this.getSubDeptListForAll(doUser.getCmpyCode(), rootOdept);
			return new Bean().set(Constant.RTN_DATA, deptList);
			
		} else if (mode == WfeConstant.NODE_BIND_MODE_PREDEF) { // 预定义
			List<DeptBean> deptList = new ArrayList<DeptBean>();

			if (resCode.equals(WfeBinder.PRE_DEF_SELF_DEPT)) {
				// 本处室下所有子目录
				deptList = this.getSubDeptList(doUser.getCmpyCode(), doUser.getDeptCode());
				
			} else if (resCode.equals(WfeBinder.PRE_DEF_SELF_DEPT_LEVEL)) {
				// 本部门下所有的子目录
				deptList = this.getSubDeptList(doUser.getCmpyCode(), doUser.getTDeptCode());
				
			} else if (resCode.equals(WfeBinder.PRE_DEF_HIGHER_DEPT_LEVEL)) {
				// 上级机构
				deptList = getParentLevelDeptList(doUser);
				
			} else if (resCode.equals(WfeBinder.PRE_DEF_INIT_TOP_DEPT)) {
				// 拟稿部门 通过 起草节点的 DONE_USER_ID 取用户
				UserBean firstUser = getFirstActDoneUser(curWfAct);
				deptList = getSubDeptList(firstUser.getCmpyCode(), firstUser.getTDeptCode());
				
			} else if (resCode.equals(WfeBinder.PRE_DEF_INIT_DEPT)) {
				// 拟稿处室
				UserBean firstUser = getFirstActDoneUser(curWfAct);
				deptList = getSubDeptList(firstUser.getCmpyCode(), firstUser.getDeptCode());
				
			} else if (resCode.equals(WfeBinder.PRE_DEF_INIT_ORG)) {
				// 拟稿机构
				UserBean firstUser = getFirstActDoneUser(curWfAct);
				deptList = getSubDeptList(firstUser.getCmpyCode(), firstUser.getODeptCode());
				
			} else if (resCode.equals(WfeBinder.PRE_DEF_SUB_ORG)) {
				// 下级机构 , 因为是下级机构，这里就暂时先不加自己
				String sql = OrgMgr.getSubOrgDeptsSql(doUser.getCmpyCode(), doUser.getODeptCode());
				return new Bean().set("SQL_MODE", sql);
			}

			return new Bean().set(Constant.RTN_DATA, deptList);
		} else if (mode == WfeConstant.NODE_BIND_MODE_ZHIDING) { // 指定
			List<DeptBean> deptList = new ArrayList<DeptBean>();
			for (String deptCode : resCode.split(",")) {
				deptList.add(OrgMgr.getDept(deptCode));
			}
			return new Bean().set(Constant.RTN_DATA, deptList);
		}
		return null;
	}
	
	/**
	 * 取得指定部门下的所有子部门
	 * 
	 * @param aCmpyCode
	 *            公司ID
	 * @param deptCode
	 *            部门ID
	 * @return 部门串
	 */
	private List<DeptBean> getSubDeptListForAll(String aCmpyCode, String deptCode) {
		
		List<DeptBean> deptList = OrgMgr.getAllDepts(aCmpyCode);
		DeptBean deptBean = OrgMgr.getDept(deptCode);
		deptList.add(0, deptBean);

		return deptList;
	}
	
	/**
	 * 取得指定部门下的所有子部门
	 * 
	 * @param aCmpyCode
	 *            公司ID
	 * @param deptCode
	 *            部门ID
	 * @return 部门串
	 */
	private List<DeptBean> getSubDeptList(String aCmpyCode, String deptCode) {
		
		List<DeptBean> deptList = OrgMgr.getChildDepts(aCmpyCode, deptCode);
		DeptBean deptBean = OrgMgr.getDept(deptCode);
		deptList.add(0, deptBean);

		return deptList;
	}
	
	/**
	 * @return 拟稿人信息
	 */
	private UserBean getFirstActDoneUser(WfAct wfAct) {
		Bean fistNodeInstBean = wfAct.getProcess().getFirstWfAct().getNodeInstBean();
		String doneUserCode = fistNodeInstBean.getStr("TO_USER_ID");
		UserBean doneUserBean = UserMgr.getUser(doneUserCode);
		return doneUserBean;
	}
	
	/**
	 * 获取 上级部门 子部门列表
	 * 
	 * @param userBean
	 *            用户Bean
	 * @return 部门串
	 */
	private List<DeptBean> getParentLevelDeptList(UserBean userBean) {
		// 本机构
		DeptBean odeptBean = userBean.getODeptBean();
		String ppDeptCode = "";
		DeptBean ppdeptBean = null;
		if (null == odeptBean.getPcode() || odeptBean.getPcode() == "") {
			ppDeptCode = odeptBean.getCode();
			ppdeptBean = odeptBean;
		} else {
			// 获取父部门
			ppdeptBean = OrgMgr.getDept(odeptBean.getPcode());
			ppDeptCode = ppdeptBean.getCode();
		}
		// 加上该部门的所有子部门
		List<DeptBean> deptList = OrgMgr.getChildDepts(userBean.getCmpyCode(), ppDeptCode);

		deptList.add(0, ppdeptBean);

		return deptList;
	}
	
	/**
	 * 
	 * @param rtnDept
	 *            节点上定义的部门信息
	 * @return 部门列表
	 */
	private String getDeptList(Bean rtnDept) {
		List<DeptBean> deptBeanList = rtnDept.getList(Constant.RTN_DATA);
		String deptCodeStr = "";
		if (null != deptBeanList && deptBeanList.size() > 0) {
			deptCodeStr = getDeptCodeStr(deptBeanList);
		}
		return deptCodeStr;
	}
	
	/**
	 * @param deptBeanList
	 *            部门列表
	 * @return 部门编码串
	 */
	private String getDeptCodeStr(List<DeptBean> deptBeanList) {
		StringBuffer deptCodeStr = new StringBuffer();
		for (DeptBean deptBean : deptBeanList) {

			deptCodeStr.append(deptBean.getCode());
			deptCodeStr.append(",");
		}
		return deptCodeStr.toString();
	}

}
