package com.rh.core.wfe.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.wfe.WfAct;

/**
 * 
 *
 */
public class WfContextHelper {
	private static Log log = LogFactory.getLog(WfContextHelper.class);

	private WfAct wfAct = null;

	private UserBean currentUser = null;

	private DeptBean currentDept = null;

	private DeptBean currentTopDept = null;

	private DeptBean initDept = null;

	private DeptBean initTopDept = null;

	private UserBean initUser = null;

	/**
	 * 
	 * @param aWfAct 节点实例
	 * @param currUser 当前用户
	 */
	public WfContextHelper(WfAct aWfAct, UserBean currUser) {
		this.wfAct = aWfAct;
		this.currentUser = currUser;
		this.initUser();
	}

	/**
	 * @param roleNames
	 *            指定角色的名称，多个角色之间使用","号分隔。
	 * @return 当前办理人是否属于指定角色。如果执行过程产生错误，返回false，否则返回执行结果。
	 */
	public boolean inRole(String roleNames) {
		try {
			return inRole(currentUser.getCode(), roleNames);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 判断指定用户是否属于指定角色。
	 * 
	 * @param userID
	 *            用户ID。
	 * @param roleNames
	 *            指定角色的名称，多个角色之间使用","号分隔。
	 * @return 当前办理人是否属于指定角色。如果执行过程产生错误，返回false，否则返回执行结果。
	 */
	public boolean inRole(String userID, String roleNames) {
		try {
			
			UserBean userBean = UserMgr.getUser(userID);
			
			for (String roleCode: roleNames.split(",")) {
				if (userBean.existInRole(roleCode)) {
					return true;
				}
			}
            return false;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 
	 * @return 当前用户所在机构编码
	 */
	public String getCurrentOrgCode() {
		 return currentUser.getODeptCode();
	}
	
	/**
	 * 
	 * @return 当前用户所在机构名称
	 */
	public String getCurrentOrgName() {
		 return currentUser.getODeptName();
	}
	
	/**
	 * @return 取得当前办理人的UserID
	 */
	public String currentUserId() {
		return currentUser.getCode();
	}

	/**
	 * @return 取得当前办理人的User对象
	 */
	public UserBean currentUser() {
		return this.currentUser;
	}

	/**
	 * @return 取得当前办理所在部门的ID
	 */
	public String currentDeptId() {
		return currentDept().getCode();
	}

	/**
	 * @return 取得当前办理人所在部门的Department对象
	 */
	public DeptBean currentDept() {
		if (this.currentDept == null) {
			try {
				currentDept = currentUser.getDeptBean();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return currentDept;
	}

	/**
	 * @return 如果当前用户所在部门的父部门存在则返回相应的ID，否则返回0。
	 */
	public String currentTopDeptId() {
		if (currentTopDept() != null) {
			return currentTopDept().getCode();
		}
		return "";
	}

	/**
	 * @return 取得当前办理用户所在部门的父部门，如果父部门存在则返回相应对象，否则返回null。
	 */
	public DeptBean currentTopDept() {
		if (currentTopDept == null) {
			currentTopDept = currentUser.getTDeptBean();
		}
		return currentTopDept;
	}

	/**
	 * 
	 * @return 本工作流的起草人的ID
	 */
	public String initUserId() {
		if (initUser() != null) {
			return initUser().getCode();
		}
		return "";
	}
	
	/**
	 * 
	 * @return 当前处室的层级
	 */
	public int currentDeptLevel() {
		if (currentDept() != null) {
			return currentDept().getLevel();
		}
		return 1;
	}
	

	/**
	 * 
	 * @return 本工作流的起草人User对象
	 */
	public UserBean initUser() {
		if (initUser == null) {
			try {
				WfAct initWfAct = wfAct.getProcess().getFirstWfAct();
				String toUser = initWfAct.getNodeInstBean().getStr("TO_USER_ID");
				initUser = UserMgr.getUser(toUser);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		return initUser;
	}

	/**
	 * 起草处室的ID
	 * 
	 * @return 起草处室的ID
	 */
	public String initDeptId() {
		if (initDept() != null) {
			return initDept().getCode();
		}
		return "";
	}

	/**
	 * 取得起草处室的Department对象。
	 * 
	 * @return 起草部门Bean
	 */
	public DeptBean initDept() {
		if (this.initDept == null) {
			try {
				initDept = initUser.getDeptBean();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return initDept;
	}

	/**
	 * @return 取得起草部门的ID
	 */
	public String initTopDeptId() {
		if (initTopDept() != null) {
			return initTopDept().getCode();
		}

		return "";
	}

	/**
	 * @return 取得起草部门的Department 对象。
	 */
	public DeptBean initTopDept() {
		if (initTopDept == null) {
			initTopDept = initUser.getTDeptBean();
		}
		return initTopDept;
	}

	/**
	 * @return 当前节点的<code>WfActivity</code>对象
	 */
	public WfAct wfActivity() {
		return this.wfAct;
	}
	
	/**
	 * 判断流程是否流经某个节点
	 * 
	 * @param nodeKey
	 *            节点的ID （Key），如N1，N2
	 * @return 流程是否流经某个节点
	 * @throws WfeGeneralException
	 */
	public boolean flowedOverNode(String nodeKey) {
		if (this.wfAct != null) {
			try {
				List<Bean> nodeInstList = wfAct.getProcess().wfTracking();

				for (Bean nodeInst : nodeInstList) {

					if (nodeInst.getStr("NODE_CODE").equals(nodeKey)) {
						return true;
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		return false;
	}

	/**
	 * 
	 * @return 是否锁定
	 */
	public boolean isLocked() {
		return this.wfAct.getProcess().isLocked();
	}
}
