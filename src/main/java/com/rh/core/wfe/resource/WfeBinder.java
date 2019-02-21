package com.rh.core.wfe.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;

/**
 * 存放节点可办理用户的组织关系，及其操作的类
 *
 */
public class WfeBinder {
	/**
	 * 用户ID的前缀
	 */
	public static final String USER_NODE_PREFIX = "usr";

	/**
	 * 部门ID前缀
	 */
	public static final String DEPT_NODE_PREFIX = "dept";

	/**
	 * 角色ID前缀
	 */
	public static final String ROLE_NODE_PREFIX = "role";

	/**
	 * 本部门
	 */
	public static final String PRE_DEF_SELF_DEPT = "s";

	/**
	 * 本级部门
	 */
	public static final String PRE_DEF_SELF_DEPT_LEVEL = "s0";

	/**
	 * 上级部门
	 */
	public static final String PRE_DEF_HIGHER_DEPT_LEVEL = "higher";

	/**
	 * 拟稿部门
	 */
	public static final String PRE_DEF_INIT_TOP_DEPT = "INIT_TOP_DEPT";

	/**
	 * 拟稿处室
	 */
	public static final String PRE_DEF_INIT_DEPT = "INIT_DEPT";

	/**
	 * 拟稿机构
	 */
	public static final String PRE_DEF_INIT_ORG = "INIT_ORG";

	/** 下级机构 **/
	public static final String PRE_DEF_SUB_ORG = "SUB_ORG";

	/**
	 * 绑定类型 人
	 */
	public static final String NODE_BIND_USER = "USER";

	/**
	 * 绑定类型 角色
	 */
	public static final String NODE_BIND_ROLE = "ROLE";

	private String binderType; // role, user

	private String roleCode; // 如果binderType是角色，就将角色带上

	/** 是否需要自动选中用户 */
	private boolean autoSelect = false;

	/** 能否多选 */
	private boolean multiSelect = false;

	/** 能否只读 */
	private boolean readonly = false;

	/** 返回的过滤之后的树的节点列表 */
	private List<Bean> treeBeanList = new ArrayList<Bean>();

	private HashMap<String, Bean> treeNodeMap = new HashMap<String, Bean>();

	/** 返回过滤后的组的信息 由GroupExtendBinder返回 */
	private List<GroupBean> groupBeanList = new ArrayList<GroupBean>();

	/**
	 * 添加节点
	 * 
	 * @param binderBean
	 *            节点数据Bean
	 */
	public void addTreeBean(Bean binderBean) {
		if (!treeNodeMap.containsKey(binderBean.getStr("ID"))) {
			this.treeBeanList.add(binderBean);
			treeNodeMap.put(binderBean.getStr("ID"), binderBean);
		}
	}

	/**
	 * 将对象转成json串给前台，用于树的显示
	 * 
	 * @return 树上组织资源的字符串
	 */
	public String getBinders() {
		// 将treebeanList 按照层级level进行排序
		ComparatorTreeLevel comparator = new ComparatorTreeLevel();
		Collections.sort(treeBeanList, comparator);

		// 将树组织起来，添加子节点
		WfResTreeHelper wfResTree = new WfResTreeHelper();
		for (Bean treeBean : treeBeanList) {
			wfResTree.addNode(treeBean);
		}

		// 获取根节点列表
		List<WfResTreeNode> treeNodeList = wfResTree.getRootNodeList();
		if (treeNodeList != null && treeNodeList.size() > 1) {
			WfResTreeNode rootBean = new WfResTreeNode();
			rootBean.set("SORT", "0");
			rootBean.set("NAME", Context.getUserBean().getCmpyName());
			rootBean.set("NODETYPE", "dept");
			rootBean.set("ID", "dept: " + Lang.getUUID());
			List<WfResTreeNode> tmpList = new ArrayList<WfResTreeNode>();
			for (WfResTreeNode node : treeNodeList) {
				tmpList.add(node);
			}
			rootBean.set("CHILD", tmpList);
			treeNodeList = new ArrayList<WfResTreeNode>();
			treeNodeList.add(rootBean);
		}
		// 将树上每一级内的兄弟按照sort排序
		BinderBeanSort.sortTheBindList(treeNodeList);

		return JsonUtils.toJson(treeNodeList);
	}

	/**
	 * @return 是否自动选中用户
	 */
	public boolean isAutoSelect() {
		return autoSelect;
	}

	/**
	 * @param aAutoSelect
	 *            是否自动选中用户
	 */
	public void setAutoSelect(boolean aAutoSelect) {
		this.autoSelect = aAutoSelect;
	}

	/**
	 * 
	 * @return 角色编码
	 */
	public String getRoleCode() {
		return roleCode;
	}

	/**
	 * 
	 * @param aRoleCode
	 *            角色编码
	 */
	public void setRoleCode(String aRoleCode) {
		this.roleCode = aRoleCode;
	}

	/**
	 * @return 绑定类型
	 */
	public String getBinderType() {
		return binderType;
	}

	/**
	 * @param aBinderType
	 *            绑定类型
	 */
	public void setBinderType(String aBinderType) {
		this.binderType = aBinderType;
	}

	/**
	 * 
	 * @return 能否多选
	 */
	public boolean isMutilSelect() {
		return multiSelect;
	}

	/**
	 * 
	 * @param mutilSelect
	 *            能否多选
	 */
	public void setMutilSelect(boolean mutilSelect) {
		this.multiSelect = mutilSelect;
	}

	/**
	 * @return 选中用户是否只读
	 */
	public boolean isReadOnly() {
		return readonly;
	}

	/**
	 * @param aAutoSelect
	 *            选中用户是否只读
	 */
	public void setReadOnly(boolean aReadOnly) {
		this.readonly = aReadOnly;
	}

	/**
	 * 
	 * @return 树的节点list
	 */
	public List<Bean> getTreeBeanList() {

		return treeBeanList;
	}

	/**
	 * @return 处理人组
	 */
	public List<GroupBean> getGroupBeanList() {
		return groupBeanList;
	}

	/**
	 * 设置处理人组
	 * 
	 * @param groupBeanList
	 *            处理人组
	 */
	public void setGroupBeanList(List<GroupBean> groupBeanList) {
		this.groupBeanList = groupBeanList;
	}

}

/**
 * 树按层排序
 *
 */
class ComparatorTreeLevel implements Comparator<Bean> {
	/**
	 * 实现接口的方法
	 * 
	 * @param arg0
	 *            比较的对象
	 * @param arg1
	 *            比较的对象
	 * @return 比较结果
	 */
	public int compare(Bean arg0, Bean arg1) {
		Bean bean1 = (Bean) arg0;
		Bean bean2 = (Bean) arg1;

		// 比较排序
		if (bean1.getInt("LEVEL") > bean2.getInt("LEVEL")) {
			return 1;
		}

		if (bean1.getInt("LEVEL") == bean2.getInt("LEVEL")) {
			return 0;
		}

		return -1;
	}
}