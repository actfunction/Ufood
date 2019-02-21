package com.rh.core.wfe.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.org.util.OrgConstant;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 工作流组织资源绑定
 */
public class WfBinderManager {

	private static Log log = LogFactory.getLog(WfBinderManager.class);

	// 节点定义
	private WfNodeDef wfNodeDef;

	// 节点实例
	private WfAct wfAct;

	// 办理人对象
	private UserBean doUser;

	// 名称增加类型
	private Bean addWfeConfig = null;
	
	// 如果送交人员为空，是否送交全部
	private String isSendAll = null;
	
	// 如果送交人员为null，是否送交全部
	private boolean ifNullSendAll = true;

	// 名称增加类型
	private static String wfeConfig = "addWfeConfig";

	// 办理人所在公司
	private String cmpyCode = "";

	// 资源绑定类型：ROLE角色 ROLE用户
	private String bindMode = WfeBinder.NODE_BIND_USER;

	private BinderResource deptBinder = new BinderResource();

	private BinderResource userBinder = new BinderResource();

	private BinderResource roleBinder = new BinderResource();

	private String extendCls = "";

	private WfeBinder binder = new WfeBinder();

	/** 显示组织机构树是默认向上取几级 **/
	private int topLevel = 1;

	/**
	 * @param aWfNodeDef
	 *            节点定义
	 * @param aWfAct
	 *            节点实例
	 * @param aDoUser
	 *            办理人对象
	 */
	public WfBinderManager(WfNodeDef aWfNodeDef, WfAct aWfAct, UserBean aDoUser) {
		this.wfNodeDef = aWfNodeDef;
		this.wfAct = aWfAct;
		this.doUser = aDoUser;
		this.ifNullSendAll = getSendAllFlag();
		this.addWfeConfig = getwfeConfig(aWfNodeDef);
		this.cmpyCode = this.doUser.getCmpyCode();

		if (this.doUser.getODeptLevel() > 1) {
			topLevel = this.doUser.getODeptLevel() - 1;
		}
	}
	
	/**
	 * 如果送交人为空，则是否送交全部人员
	 * 
	 * @return true/false
	 */
	private boolean getSendAllFlag() {
		if (null != this.isSendAll) {
			return Boolean.parseBoolean(this.isSendAll);
		} else {
			boolean ifNullSendAll = Context.getSyConf("IF_NULL_SEND_ALL", true);
			this.isSendAll = String.valueOf(ifNullSendAll);
			return ifNullSendAll;
		}
	}

	/**
	 * 获取增加名称类型，即显示送交名称时，自动加上需要增加的名称类型
	 * 
	 * @param aWfNodeDef
	 * @return
	 */
	private Bean getwfeConfig(WfNodeDef aWfNodeDef) {
		String extCls = aWfNodeDef.getStr("NODE_EXTEND_CLASS");
		String configStr = "";

		String[] classes = extCls.split(",,");
		if (classes.length == 2) {
			configStr = classes[1];
		}
		Bean configBean = JsonUtils.toBean(configStr);

		// 如果没有值，则直接返回 null
		if (configBean.contains(wfeConfig)) {
			return configBean.getBean(wfeConfig);
		}

		return null;
	}

	/**
	 * 添加绑定的人
	 * 
	 * @param resCodes
	 *            人员列表
	 */
	private void addUser(String resCodes) {
		String definedUsers = "";

		if (resCodes.equals(WfeConstant.USER_YUDING_DRAFT_USER)) {
			// 起草人
			WfAct firstAct = this.wfAct.getProcess().getFirstWfAct();
			definedUsers = firstAct.getNodeInstBean().getStr("TO_USER_ID");
		} else if (resCodes.equals(WfeConstant.USER_YUDING_CURRENT_USER)) {
			// 当前用户
			definedUsers = this.doUser.getCode();
		} else if (resCodes.equals(WfeConstant.USER_YUDING_TARGET_NODE_LAST_USER)) {
			// 指定节点的最后一个办理用户。
			WfAct lastAct = this.wfAct.getProcess().getNodeLastWfAct(this.wfNodeDef.getStr("NODE_CODE"));
			if (lastAct != null) {
				Bean nodeInstBean = lastAct.getNodeInstBean();
				if (nodeInstBean.isNotEmpty("TO_USER_ID")) {
					definedUsers = nodeInstBean.getStr("TO_USER_ID");
				}
			}

			// 未找到办理用户则报错
			if (definedUsers.length() == 0) {
				throw new TipException("未找到办理用户");
			}
		} else {
			// 指定用户
			definedUsers = resCodes;
		}

		for (String userCode : definedUsers.split(",")) {
			UserBean userBean = UserMgr.getUser(userCode);

			Bean binderBean = new Bean();
			binderBean.set("CODE", userBean.getCode());
			binderBean.set("NAME", userBean.getName());
			binderBean.set("NODETYPE", WfeBinder.USER_NODE_PREFIX);
			binderBean.set("ID", WfeBinder.USER_NODE_PREFIX + ":" + userBean.getCode());
			binderBean.set("SORT", userBean.getSort());
			binderBean.set("LEVEL", 999);
			binderBean.set("PID", WfeBinder.DEPT_NODE_PREFIX + ":" + userBean.getDeptCode());

			binder.addTreeBean(binderBean);

			this.recursivePDept(userBean.getDeptCode());
		}
	}

	/**
	 * 添加 返回树结构的节点
	 * 
	 * @param userBeanList
	 *            用户对象列表
	 */
	private void addBindNode(List<Bean> userBeanList) {
		for (Bean userBean : userBeanList) {
			Bean binderBean = new Bean();
			binderBean.set("CODE", userBean.getStr("USER_CODE"));
			binderBean.set("NAME", userBean.getStr("USER_NAME"));
			binderBean.set("NODETYPE", WfeBinder.USER_NODE_PREFIX);
			binderBean.set("ID", WfeBinder.USER_NODE_PREFIX + ":" + userBean.getStr("USER_CODE"));
			binderBean.set("TRANSID", userBean.getStr("TRANSID"));
			binderBean.set("SORT", userBean.getStr("USER_SORT"));
			binderBean.set("LEVEL", 999);
			binderBean.set("PID", WfeBinder.DEPT_NODE_PREFIX + ":" + userBean.getStr("DEPT_CODE"));
			binderBean.set("USER_IMG", userBean.getStr("USER_IMG_SRC"));

			binder.addTreeBean(binderBean);

			this.recursivePDept(userBean.getStr("DEPT_CODE"));
		}
	}

	/**
	 * @param deptCodeStr
	 *            过滤部门编码串
	 * @param roleCodeStr
	 *            过滤角色串
	 * @param isSql
	 *            是否sql语句
	 */
	private void addUserInDeptRole(String deptCodeStr, String roleCodeStr, boolean isSql) {
		List<Bean> userList = new ArrayList<Bean>();
		if (isSql) {
			userList = UserMgr.getUsersBeanbyDeptSql(deptCodeStr, roleCodeStr);
		} else {
			if (!StringUtils.isEmpty(deptCodeStr) && !StringUtils.isEmpty(roleCodeStr)) { // 过滤部门
																							// +角色
				userList = UserMgr.getUserListByDept(deptCodeStr, roleCodeStr);
			} else if (!deptCodeStr.isEmpty()) { // 过滤部门

				if (null != this.addWfeConfig) {
					userList = this.getUserListByWfeConfig(deptCodeStr, this.addWfeConfig);
					// userList = UserMgr.getUserListByWfeConfig(deptCodeStr,
					// this.addWfeConfig);
				} else {
					userList = UserMgr.getUserListByDept(deptCodeStr);
				}
			} else if (!StringUtils.isEmpty(roleCodeStr)) { // 过滤角色
				userList = UserMgr.getUserListByRole(roleCodeStr, this.cmpyCode);
			} else {
				// 返回所有人
				// List<DeptBean> deptList =
				// OrgMgr.getAllDepts(Context.getUserBean().getCmpyCode());
				if (this.ifNullSendAll) {
					DeptBean deptOrg = OrgMgr.getParentOrg(this.doUser.getDeptCode());
					List<DeptBean> deptList = OrgMgr.getChildDepts(this.cmpyCode, deptOrg.getCode());
					StringBuilder deptStr = new StringBuilder();
					for (DeptBean deptBean : deptList) {
						deptStr.append(deptBean.getCode());
						deptStr.append(",");
					}
					String deptString = deptStr.toString().substring(0, deptStr.length() - 1);

					userList = UserMgr.getUserListByDept(deptString);
				}
			}
		}

		// 获取兼岗用户
		List<Bean> jgUserList = null;
		if (deptCodeStr.indexOf(Constant.SEPARATOR) > 0) {
			deptCodeStr = deptCodeStr.replaceAll(Constant.SEPARATOR, "'" + Constant.SEPARATOR + "'");
		}
		if (isSql) {
			jgUserList = ServDao.finds("SY_USER_JG_INFO",
					" and DEPT_CODES in (select DEPT_CODE from SY_ORG_DEPT where 1 = 1 " + deptCodeStr
							+ ") and USER_CODE in (select distinct USER_CODE from SY_ORG_ROLE_USER where  ROLE_CODE in ('"
							+ roleCodeStr.replaceAll(",", "','") + "'))");
		} else {
			jgUserList = ServDao.finds("SY_USER_JG_INFO",
					" and DEPT_CODES in ('" + deptCodeStr
							+ "') and USER_CODE in (select distinct USER_CODE from SY_ORG_ROLE_USER where  ROLE_CODE in ('"
							+ roleCodeStr.replaceAll(",", "','") + "'))");
		}

		if (jgUserList != null && jgUserList.size() > 0) {
			for (Bean jgUser : jgUserList) {
				userList.add(UserMgr.getUser(jgUser.getStr("USER_CODE")));
			}
		}

		addBindNode(userList);
	}

	/**
	 * 做成Bean类型的，可以通过servId,act,和其他参数来调用服务扩展类 来进行功能性的扩展，这样将来需要增加其他类型的调用时，只需要
	 * 指明调用哪个服务的扩展类里的哪个方法，调用哪些参数就可以了。
	 * addWfeConfig的示例：{'serv':'服务编码','act':'方法名','参数1':'','参数2':''}
	 * 
	 * @param deptCodeStr
	 *            部门编码
	 * @param addWfeConfig
	 *            Bean类型的参数
	 * @return List<Bean>类型的用户数据
	 */
	private List<Bean> getUserListByWfeConfig(String deptCodeStr, Bean addWfeConfig) {

		ParamBean wfeConfigBean = new ParamBean(addWfeConfig);
		wfeConfigBean.set("deptCodeStr", deptCodeStr);
		return ServMgr.act(wfeConfigBean).getDataList();
	}

	/**
	 * @return 节点上定义的部门 串
	 */
	private Bean getDeptCodeList() {
		if (deptBinder.getMode() == WfeConstant.NODE_BIND_MODE_ALL) { // 全部部门
			String rootOdept = Context.getSyConf("ROOT_ODEPT_CODE", "ruaho0001");
			// 本机构下所有部门列表
			List<DeptBean> deptList = this.getSubDeptListForAll(this.cmpyCode, rootOdept);

			return new Bean().set(Constant.RTN_DATA, deptList);
		} else if (deptBinder.getMode() == WfeConstant.NODE_BIND_MODE_PREDEF) { // 预定义
			List<DeptBean> deptList = new ArrayList<DeptBean>();
			log.debug("the dept mode is predef " + deptBinder.getResCodes() + deptBinder.getMode());

			if (deptBinder.getResCodes().equals(WfeBinder.PRE_DEF_SELF_DEPT)) {
				// 本处室下所有子目录
				log.debug("the dept mode is predef 本部门" + deptBinder.getResCodes());
				deptList = this.getSubDeptList(this.cmpyCode, this.doUser.getDeptCode());
			} else if (deptBinder.getResCodes().equals(WfeBinder.PRE_DEF_SELF_DEPT_LEVEL)) {
				// 本部门下所有的子目录
				deptList = this.getSubDeptList(this.doUser.getCmpyCode(), this.doUser.getTDeptCode());
			} else if (deptBinder.getResCodes().equals(WfeBinder.PRE_DEF_HIGHER_DEPT_LEVEL)) {
				// 上级机构
				deptList = this.getParentLevelDeptList(this.doUser);
				if (this.topLevel > 1) {
					topLevel = topLevel - 1;
				}
			} else if (deptBinder.getResCodes().equals(WfeBinder.PRE_DEF_INIT_TOP_DEPT)) {
				// 拟稿部门 通过 起草节点的 DONE_USER_ID 取用户
				UserBean firstUser = getFirstActDoneUser();
				deptList = this.getSubDeptList(firstUser.getCmpyCode(), firstUser.getTDeptCode());
			} else if (deptBinder.getResCodes().equals(WfeBinder.PRE_DEF_INIT_DEPT)) {
				// 拟稿处室
				UserBean firstUser = getFirstActDoneUser();
				deptList = this.getSubDeptList(firstUser.getCmpyCode(), firstUser.getDeptCode());
			} else if (deptBinder.getResCodes().equals(WfeBinder.PRE_DEF_INIT_ORG)) {
				// 拟稿机构
				UserBean firstUser = getFirstActDoneUser();
				deptList = this.getSubDeptList(firstUser.getCmpyCode(), firstUser.getODeptCode());
			} else if (deptBinder.getResCodes().equals(WfeBinder.PRE_DEF_SUB_ORG)) {
				topLevel = topLevel + 1;
				// 下级机构 , 因为是下级机构，这里就暂时先不加自己
				String sql = OrgMgr.getSubOrgDeptsSql(this.doUser.getCmpyCode(), this.doUser.getODeptCode());

				return new Bean().set("SQL_MODE", sql);
			}

			return new Bean().set(Constant.RTN_DATA, deptList);
		} else if (deptBinder.getMode() == WfeConstant.NODE_BIND_MODE_ZHIDING) { // 指定
			List<DeptBean> deptList = new ArrayList<DeptBean>();
			for (String deptCode : deptBinder.getResCodes().split(",")) {
				deptList.add(OrgMgr.getDept(deptCode));
			}

			return new Bean().set(Constant.RTN_DATA, deptList);
		}

		return null;
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
			log.debug("获取用户所在上级部门----getParentSubDeptList-----------" + ppdeptBean.getCode());
		}

		// 加上该部门的所有子部门
		List<DeptBean> deptList = OrgMgr.getChildDepts(userBean.getCmpyCode(), ppDeptCode);

		deptList.add(0, ppdeptBean);

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
	private List<DeptBean> getSubDeptListForAll(String aCmpyCode, String deptCode) {
		log.debug("获取本部门的code----deptCode-----------" + deptCode);

		// 加上该部门的所有子部门
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
		log.debug("获取本部门的code----deptCode-----------" + deptCode);

		// 加上该部门的所有子部门
		List<DeptBean> deptList = OrgMgr.getChildDepts(aCmpyCode, deptCode);

		DeptBean deptBean = OrgMgr.getDept(deptCode);
		deptList.add(0, deptBean);

		return deptList;
	}

	/**
	 * @return 拟稿人信息
	 */
	private UserBean getFirstActDoneUser() {
		Bean fistNodeInstBean = getFirstWfAct().getNodeInstBean();
		String doneUserCode = fistNodeInstBean.getStr("TO_USER_ID");
		UserBean doneUserBean = UserMgr.getUser(doneUserCode);
		return doneUserBean;
	}

	/**
	 * @return 第一个节点实例
	 */
	private WfAct getFirstWfAct() {
		return wfAct.getProcess().getFirstWfAct();
	}

	/**
	 * @return 节点绑定
	 */
	public WfeBinder getWfeBinder() {
		// 取得线定义
		Bean lineDef = this.wfAct.getProcess().getProcDef().findLineDef(this.wfAct.getCode(),
				this.wfNodeDef.getStr("NODE_CODE"));

		// 如果处理类型是并发流则，可以多选
		if (lineDef != null && lineDef.getInt("IF_PARALLEL") == WfeConstant.NODE_IS_PARALLEL) {
			binder.setMutilSelect(true);
		}

		binder.setAutoSelect(Context.getSyConf("SY_WFE_AUTOSELECT_SETTING", false));

		if (extendCls.length() > 0) {
			// 执行扩展组织机构过滤类
			addExtendBinder();

		} else if (this.bindMode.equals(WfeBinder.NODE_BIND_ROLE)) {
			// 送角色 过滤范围：部门＋角色 ，选取部门+角色
			if (StringUtils.isEmpty(roleBinder.getResCodes())) {
				throw new RuntimeException("流程定义有错误，没有指定角色");
			}

			Bean rtnDept = getDeptCodeList();
			if (rtnDept.isEmpty("SQL_MODE")) {
				String deptCodeStr = getDeptList(rtnDept);

				addDeptRole(deptCodeStr, roleBinder.getResCodes(), false);
			} else { // sql语句
				String sql = rtnDept.getStr("SQL_MODE");

				addDeptRole(sql, roleBinder.getResCodes(), true);
			}

		} else if (this.bindMode.equals(WfeBinder.NODE_BIND_USER)) { // 绑定的人
			if (userBinder.getMode() == WfeConstant.NODE_BIND_MODE_ZHIDING
					|| userBinder.getMode() == WfeConstant.NODE_BIND_MODE_PREDEF) {
				// 过滤范围：指定用户；选取：用户
				binder.setBinderType(WfeBinder.NODE_BIND_USER);
				binder.setReadOnly(true);
				addUser(userBinder.getResCodes());
			} else {
				// 过滤范围："部门 ＋ 角色"；选取：用户
				binder.setBinderType(WfeBinder.NODE_BIND_USER);

				String roleCodeStr = "";
				if (roleBinder.getMode() == WfeConstant.NODE_BIND_MODE_ZHIDING) {
					roleCodeStr = roleBinder.getResCodes();
				}

				Bean rtnDept = getDeptCodeList();
				if (rtnDept.isEmpty("SQL_MODE")) {
					String deptCodeStr = getDeptList(rtnDept);

					addUserInDeptRole(deptCodeStr, roleCodeStr, false);
				} else { // sql语句
					String sql = rtnDept.getStr("SQL_MODE");

					addUserInDeptRole(sql, roleBinder.getResCodes(), true);
				}
			}
		}

		return binder;
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
	 * 增加扩展组织资源绑定类的执行结果
	 * class,,{'fieldStr':'','roleCodes':'','userIDs':'','bindRole':'false'}
	 */
	private void addExtendBinder() {
		String extCls = extendCls;
		// String configStr = "";

		String[] classes = extCls.split(",,");
		if (classes.length == 2) {
			extCls = classes[0];
			// configStr = classes[1];
		}

		Class<?> clz = null;
		try {
			clz = Class.forName(extCls);
		} catch (Exception e) {
			log.error("Class not found:" + extCls);
			throw new RuntimeException(e.getMessage(), e);
		}
		if (ExtendBinder.class.isAssignableFrom(clz)) {
			try {
				ExtendBinder extBinder = (ExtendBinder) clz.newInstance();
				ExtendBinderResult result = extBinder.run(this.wfAct, this.wfNodeDef, this.doUser);
				binder.setAutoSelect(result.isAutoSelect());
				binder.setReadOnly(result.isReadOnly());
				if (result.isBindRole()) {
					// 选取部门+角色
					addDeptRole(result.getDeptIDs(), result.getRoleCodes(), false);
				} else {
					// 选取用户
					binder.setBinderType(WfeBinder.NODE_BIND_USER);
					if (StringUtils.isNotEmpty(result.getUserIDs())) {
						// 指定用户
						addUser(result.getUserIDs());
					} else {
						// 指定部门或角色
						addUserInDeptRole(result.getDeptIDs(), result.getRoleCodes(), false);
					}
				}
			} catch (Exception e) {
				log.error("Class not found:" + extCls);
				throw new RuntimeException(e.getMessage(), e);
			}
		} else if (GroupExtendBinder.class.isAssignableFrom(clz)) {
			try {
				GroupExtendBinder groupExtendBinder = (GroupExtendBinder) clz.newInstance();
				List<GroupBean> groupList = groupExtendBinder.run(this.wfAct, this.wfNodeDef);
				binder.setGroupBeanList(groupList);
			} catch (Exception e) {
				log.error("Class not found:" + extCls);
				throw new RuntimeException(e.getMessage(), e);
			}
		}

	}

	/**
	 * 定义为送角色 ， 通过角色下的人，找到人的部门，也就是哪些部门下有这些角色
	 * 
	 * @param deptCodeStr
	 *            部门串, 或者查询部门的sql
	 * @param roleCodeStr
	 *            角色编码
	 * @param isSql
	 *            是否传递的是 sql 语句
	 */
	private void addDeptRole(String deptCodeStr, String roleCodeStr, boolean isSql) {
		binder.setMutilSelect(false); // 送角色，单选
		binder.setBinderType(WfeBinder.NODE_BIND_ROLE);
		binder.setRoleCode(roleCodeStr);

		List<UserBean> userList = new ArrayList<UserBean>();
		if (isSql) {
			userList = UserMgr.getUsersByDeptSql(deptCodeStr, roleCodeStr);
		} else {
			if (!StringUtils.isEmpty(deptCodeStr)) { // 过滤部门 +角色
				userList = UserMgr.getUsersByDept(deptCodeStr, roleCodeStr);
			} else {
				// 返回角色 下所有的人
				userList = UserMgr.getUsersByRole(roleCodeStr);
			}
		}

		for (UserBean userBean : userList) {
			recursivePDept(userBean.getDeptCode());
		}
	}

	/**
	 * 递归父部门
	 * 
	 * @param deptCode
	 *            部门编码
	 */
	private void recursivePDept(String deptCode) {
		DeptBean deptBean = OrgMgr.getDept(deptCode);

		Bean newDeptBean = new Bean();
		newDeptBean.set("CODE", deptBean.getCode());
		newDeptBean.set("NAME", deptBean.getName());
		newDeptBean.set("NODETYPE", WfeBinder.DEPT_NODE_PREFIX);
		newDeptBean.set("ID", WfeBinder.DEPT_NODE_PREFIX + ":" + deptBean.getCode());
		newDeptBean.set("SORT", deptBean.getSort());
		newDeptBean.set("LEVEL", deptBean.getLevel());

		binder.addTreeBean(newDeptBean);

		// 往上找到机构，就不找了，
		if (deptBean.getType() == OrgConstant.DEPT_TYPE_ORG) {
			return;
		}

		// 如果有父部门，递归
		if (!StringUtils.isEmpty(deptBean.getPcode())) {
			newDeptBean.set("PID", WfeBinder.DEPT_NODE_PREFIX + ":" + deptBean.getPcode());
			recursivePDept(deptBean.getPcode());
		}
	}

	/**
	 * 初始化绑定资源信息
	 * 
	 * @param orgDefBean
	 *            组织资源定义Bean（可能来自线定义，也可能来自节点定义）
	 */
	public void initBinderResource(Bean orgDefBean) {
		bindMode = (String) orgDefBean.get("NODE_BIND_MODE", bindMode);

		deptBinder.setResCodes(orgDefBean.getStr("NODE_DEPT_CODES"));
		deptBinder.setMode(orgDefBean.getInt("NODE_DEPT_MODE"));
		deptBinder.setScripts(orgDefBean.getStr("NODE_DEPT_WHERE"));

		userBinder.setResCodes(orgDefBean.getStr("NODE_USER_CODES"));
		userBinder.setMode(orgDefBean.getInt("NODE_USER_MODE"));
		userBinder.setScripts(orgDefBean.getStr("NODE_USER_WHERE"));

		roleBinder.setResCodes(orgDefBean.getStr("NODE_ROLE_CODES"));
		roleBinder.setMode(orgDefBean.getInt("NODE_ROLE_MODE"));
		roleBinder.setScripts(orgDefBean.getStr("NODE_ROLE_WHERE"));

		extendCls = orgDefBean.getStr("NODE_EXTEND_CLASS");
	}

	/**
	 * 绑定资源
	 */
	private class BinderResource {

		private String resCodes = "";

		private int mode = WfeConstant.NODE_BIND_MODE_ALL; // 默认给全部

		private String scripts = "";

		/**
		 * @return 绑定类型
		 */
		public int getMode() {
			return mode;
		}

		/**
		 * @return 是否存在
		 */
		public String getResCodes() {
			return resCodes;
		}

		/**
		 * @return 过滤条件
		 */
		@SuppressWarnings("unused")
		public String getScripts() {
			return scripts;
		}

		/**
		 * 绑定类型
		 * 
		 * @param bindMode
		 *            绑定类型
		 */
		public void setMode(int bindMode) {
			this.mode = bindMode;
		}

		/**
		 * @param resCode
		 *            是否存在
		 */
		public void setResCodes(String resCode) {
			this.resCodes = resCode;
		}

		/**
		 * @param script
		 *            过滤条件
		 */
		public void setScripts(String script) {
			this.scripts = script;
		}
	}
}
