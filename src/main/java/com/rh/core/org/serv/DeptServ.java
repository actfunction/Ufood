package com.rh.core.org.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.org.util.OrgConstant;
import com.rh.core.plug.im.ImMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.PinyinUtils;

/**
 * 部门服务类
 * 
 * @author wanglong
 * 
 */
public class DeptServ extends CommonServ {

	/**
	 * 与RTX通用部门和用户
	 * 
	 * @param paramBean
	 *            参数信息
	 * @return 同步结果
	 */
	public OutBean syncIm(ParamBean paramBean) {
		String servId = paramBean.getServId();
		int count = 0;
		if (Context.appBoolean(APP.IM)) { // 只有启动了IM才进行同步
			// 添加部门信息
			ParamBean queryBean = new ParamBean();
			UserBean userBean = Context.getUserBean();
			queryBean.set("CMPY_CODE", userBean.getCmpyCode());
			queryBean.set(Constant.PARAM_ORDER, "DEPT_LEVEL,DEPT_SORT");
			List<Bean> deptList = ServDao.finds(servId, queryBean);
			for (Bean data : deptList) {
				if (ImMgr.getIm().saveDept(data)) {
					count++;
				}
			}
			// 添加用户信息
			queryBean = new ParamBean(ServMgr.SY_ORG_USER, ServMgr.ACT_FINDS);
			queryBean.set("CMPY_CODE", userBean.getCmpyCode());
			List<Bean> userList = ServMgr.act(queryBean).getDataList();
			for (Bean data : userList) {
				data.set("USER_PASSWORD_REAL", Context.getSyConf("SY_USER_PASSWORD_INIT", "123456"));
				if (ImMgr.getIm().saveUser(data)) {
					count++;
				}
			}
		}
		OutBean outBean = new OutBean();
		if (count > 0) {
			outBean.setOk(Context.getSyMsg("SY_SYNC_OK", String.valueOf(count)));
		} else {
			outBean.setError(Context.getSyMsg("SY_SYNC_ERROR"));
		}
		return outBean;
	}

	/**
	 * 删除之后更新IM处理
	 * 
	 * @param paramBean
	 *            参数信息
	 * @param outBean
	 *            删除结果信息
	 */
	protected void afterDelete(ParamBean paramBean, OutBean outBean) {
		if (Context.appBoolean(APP.IM)) { // 启动了IM进行同步
			ImMgr.getIm().deleteDept(paramBean.getId());
		}
	}

	/**
	 * 保存之前处理
	 * 
	 * @param paramBean
	 *            参数信息
	 */
	protected void beforeSave(ParamBean paramBean) {
		String servId = paramBean.getServId();
		Bean newData = paramBean.getSaveFullData(); // 获取全数据
		if (newData.isEmpty("DEPT_FULL_NAME")) { // 部门全称不设置缺省等于部门名称
			paramBean.set("DEPT_FULL_NAME", newData.getStr("DEPT_NAME"));
		}
		if (paramBean.contains("DEPT_NAME")) { // 计算部门简称
			paramBean.set("DEPT_SHORT_NAME", PinyinUtils.getHeadStr(paramBean.getStr("DEPT_NAME")));
		}
		if (paramBean.contains("DEPT_PCODE") || paramBean.contains("DEPT_TYPE")) { // 存在父部门或设置部门类型
			String curCode = newData.getStr("DEPT_CODE");
			if (newData.isEmpty("DEPT_PCODE")) { // 没有设置父部门
				paramBean.set("DEPT_TYPE", OrgConstant.DEPT_TYPE_ORG); // 没有父部门的缺省为机构
				paramBean.set("TDEPT_CODE", curCode); // 有效部门为自己
				paramBean.set("ODEPT_CODE", curCode); // 机构为自己
			} else {
				Bean pDept = ServDao.find(servId, newData.getStr("DEPT_PCODE"));
				if (newData.getInt("DEPT_TYPE") == OrgConstant.DEPT_TYPE_ORG) { // 当前部门为机构
					paramBean.set("TDEPT_CODE", curCode); // 有效部门为自己
					paramBean.set("ODEPT_CODE", curCode); // 机构为自己
				} else { // 当前部门不是机构
					if (pDept.getInt("DEPT_TYPE") == OrgConstant.DEPT_TYPE_ORG) { // 父部门为机构，则子部门为有效部门
						paramBean.set("TDEPT_CODE", curCode); // 当前有效部门为自己
					} else {
						paramBean.set("TDEPT_CODE", pDept.getStr("TDEPT_CODE")); // 有效部门继承父部门的
					}
					paramBean.set("ODEPT_CODE", pDept.getStr("ODEPT_CODE")); // 机构继承父部门的
				}
			}
		}
	}

	/**
	 * 保存之后处理
	 * 
	 * @param paramBean
	 *            参数信息
	 * @param outBean
	 *            参数信息
	 */
	protected void afterSave(ParamBean paramBean, OutBean outBean) {
		String servId = paramBean.getServId();
		if (!paramBean.getAddFlag()) { // 修改模式
			Bean oldBean = paramBean.getSaveOldData();
			Bean setBean = new Bean();
			SqlBean whereBean = new SqlBean();
			// 根据当前部门类型更新所有类型为部门的子孙部门

			if (paramBean.contains("DEPT_TYPE") && (paramBean.getInt("DEPT_TYPE") != oldBean.getInt("DEPT_TYPE"))) { // 变更了部门类型
				if (paramBean.getInt("DEPT_TYPE") == OrgConstant.DEPT_TYPE_ORG) { // 部门变机构
					// 找到所有直接子部门（类型为部门）
					whereBean.and("DEPT_TYPE", OrgConstant.DEPT_TYPE_DEPT).and("DEPT_PCODE", oldBean.getId());
					List<Bean> subList = ServDao.finds(servId, whereBean);
					for (Bean sub : subList) {
						whereBean = new SqlBean().and("DEPT_TYPE", OrgConstant.DEPT_TYPE_DEPT)
								.and("ODEPT_CODE", sub.getStr("ODEPT_CODE"))
								.andLikeRT("CODE_PATH", sub.getStr("CODE_PATH")); // 向下修改所有子部门及子的子孙部门
						setBean.set("ODEPT_CODE", outBean.getStr("ODEPT_CODE")).set("TDEPT_CODE", sub.getId()); // 将子部门设为有效部门
						ServDao.updates(servId, setBean, whereBean);
					}
				} else { // 机构变部门,找到本机构下所有子孙部门（类型为部门）
					whereBean.and("DEPT_TYPE", OrgConstant.DEPT_TYPE_DEPT)
							.and("ODEPT_CODE", oldBean.getStr("ODEPT_CODE"))
							.andLikeRT("CODE_PATH", oldBean.getStr("CODE_PATH")); // 向下修改所有子部门及子的子孙部门
					setBean.set("ODEPT_CODE", outBean.getStr("ODEPT_CODE")).set("TDEPT_CODE",
							outBean.getStr("TDEPT_CODE"));
					ServDao.updates(servId, setBean, whereBean);
				}
			} else if (paramBean.contains("TDEPT_CODE")
					&& !(paramBean.getStr("TDEPT_CODE").equals(oldBean.getStr("TDEPT_CODE")))) { // 变更了有效部门
				whereBean.and("DEPT_TYPE", OrgConstant.DEPT_TYPE_DEPT).and("ODEPT_CODE", oldBean.getStr("ODEPT_CODE"))
						.andLikeRT("CODE_PATH", oldBean.getStr("CODE_PATH"));
				setBean.set("ODEPT_CODE", outBean.getStr("ODEPT_CODE")).set("TDEPT_CODE", outBean.getStr("TDEPT_CODE"));
				ServDao.updates(servId, setBean, whereBean);
			}
		}
		if (Context.appBoolean(APP.IM)) { // 启动了IM进行同步
			ImMgr.getIm().saveDept(outBean);
		}

		/************************************ 同步到钉钉机构中 ***********************************/
		// try {
		// String accessToken = AuthHelper.getAccessToken();
		// Bean bean = paramBean.getSaveFullData();
		// if (bean.isEmpty("DEPT_PCODE")) {
		// return;
		// }
		// DeptBean deptBean = OrgMgr.getDept(bean.getStr("DEPT_PCODE"));
		// String parentId = deptBean.getStr("DEPT_SRC_TYPE3");
		// if (deptBean.isEmpty("DEPT_SRC_TYPE3")) {
		// throw new TipException("钉钉缺少父部门!");
		// }
		// if (paramBean.getAddFlag()) {
		// String ddDeptId = DepartmentHelper.createDepartment(accessToken,
		// bean.getStr("DEPT_NAME"), parentId,
		// bean.getStr("DEPT_SORT"), true);
		// ServDao.update(paramBean.getServId(),
		// new Bean().set("DEPT_SRC_TYPE3", ddDeptId).setId(paramBean.getId()));
		// } else {
		// DepartmentDetail departmentDetail =
		// DepartmentHelper.getDepartment(accessToken,
		// bean.getStr("DEPT_SRC_TYPE3"));
		// if (departmentDetail != null) {
		// DepartmentHelper.updateDepartment(accessToken,
		// Long.parseLong(bean.getStr("DEPT_SRC_TYPE3")),
		// bean.getStr("DEPT_NAME"), parentId, bean.getStr("DEPT_SORT"), true,
		// true, "", false, "", "",
		// false, "", "", "");
		// } else {
		// String ddDeptId = DepartmentHelper.createDepartment(accessToken,
		// bean.getStr("DEPT_NAME"), parentId,
		// bean.getStr("DEPT_SORT"), true);
		// ServDao.update(paramBean.getServId(),
		// new Bean().set("DEPT_SRC_TYPE3", ddDeptId).setId(paramBean.getId()));
		// }
		// }
		// } catch (Exception e) {
		// throw new TipException(e.getMessage());
		// }
	}

	/**
	 * 初始化部门简称
	 * 
	 * @param paramBean
	 *            参数
	 * @return 更新数量
	 */
	public OutBean initPinyin(ParamBean paramBean) {
		ServDefBean servDef = ServUtils.getServDef(paramBean.getServId());
		ParamBean param = new ParamBean(ServMgr.SY_ORG_DEPT_ALL, ServMgr.ACT_FINDS);
		param.setWhere(servDef.getServDefWhere());
		List<Bean> list = ServMgr.act(param).getDataList();
		List<Bean> dataList = new ArrayList<Bean>(list.size());
		for (Bean dept : list) {
			Bean data = new Bean();
			data.set("DEPT_SHORT_NAME", PinyinUtils.getHeadStr(dept.getStr("DEPT_NAME"))).set("DEPT_CODE",
					dept.getId());
			dataList.add(data);
		}
		String sql = "update SY_ORG_DEPT set DEPT_SHORT_NAME=#DEPT_SHORT_NAME# where DEPT_CODE=#DEPT_CODE#";
		int count = Transaction.getExecutor().executeBatchBean(sql, dataList);
		return new OutBean().setOk(Context.getSyMsg("SY_BATCHSAVE_OK", count));
	}
}
