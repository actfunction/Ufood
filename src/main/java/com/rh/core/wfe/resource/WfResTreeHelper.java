package com.rh.core.wfe.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.util.Constant;

/**
 * 组织资源 形成 树结构
 * 
 */
public class WfResTreeHelper {
	private List<WfResTreeNode> rootNodeList = new ArrayList<WfResTreeNode>();
	private HashMap<String, WfResTreeNode> objs = new HashMap<String, WfResTreeNode>();

	private static final String DIRNODE = "DIR";
	private static final String LEAFNODE = "LEAF";

	/**
	 * 添加目录节点
	 * 
	 * @param id
	 *            节点标示ID
	 * @param name
	 *            节点名称
	 * @param pid
	 *            父节点ID
	 * @param sort
	 *            节点排序号
	 */
	public void addDirNode(String id, String name, String pid, int sort) {
		WfResTreeNode binderBean = new WfResTreeNode();
		binderBean.set("NAME", name);
		binderBean.set("NODETYPE", DIRNODE);
		binderBean.set("ID", id);
		binderBean.set("SORT", sort);

		if (objs.containsKey(id)) {
			return;
		}

		if (pid.length() == 0) {
			rootNodeList.add(binderBean);
		} else if (objs.containsKey(String.valueOf(pid))) {
			WfResTreeNode parentTree = (WfResTreeNode) objs.get(pid);
			parentTree.addChild(binderBean);
		}

		objs.put(id, binderBean);
	}

	/**
	 * 添加树叶子节点
	 * 
	 * @param id
	 *            节点标示ID
	 * @param name
	 *            节点名称
	 * @param pid
	 *            父节点ID
	 * @param sort
	 *            节点排序号
	 */
	public void addLeafNode(String id, String name, String pid, int sort) {
		WfResTreeNode binderBean = new WfResTreeNode();
		binderBean.set("NAME", name);
		binderBean.set("NODETYPE", LEAFNODE);
		binderBean.set("ID", id);
		binderBean.set("SORT", sort);

		if (objs.containsKey(id)) {
			return;
		}

		if (pid.length() == 0) {
			rootNodeList.add(binderBean);
		} else if (objs.containsKey(String.valueOf(pid))) {
			WfResTreeNode parentTree = (WfResTreeNode) objs.get(pid);
			parentTree.addChild(binderBean);
		}

		objs.put(id, binderBean);
	}

	/**
	 * 
	 * @param id
	 *            节点标示ID
	 * @param name
	 *            节点名称
	 * @param pid
	 *            父节点ID
	 * @param sort
	 *            节点排序号
	 * @param nodeType
	 *            节点类型，叶子节点还是 目录节点
	 */
	public void addNode(String id, String name, String pid, int sort,
			String nodeType) {
		WfResTreeNode binderBean = new WfResTreeNode();
		binderBean.set("NAME", name);
		binderBean.set("NODETYPE", nodeType);
		binderBean.set("ID", id);
		binderBean.set("SORT", sort);

		if (objs.containsKey(id)) {
			return;
		}

		if (pid.length() == 0) {
			rootNodeList.add(binderBean);
		} else if (objs.containsKey(String.valueOf(pid))) {
			WfResTreeNode parentTree = (WfResTreeNode) objs.get(pid);
			parentTree.addChild(binderBean);
		}

		objs.put(id, binderBean);
	}

	/**
	 * 
	 * @param treeBean 树节点数据
	 */
	public void addNode(Bean treeBean) {
		String id = treeBean.getStr("ID");
		String pid = treeBean.getStr("PID");
		
		WfResTreeNode binderBean = new WfResTreeNode();
		binderBean.set("NAME", treeBean.getStr("NAME"));
		binderBean.set("NODETYPE", treeBean.getStr("NODETYPE"));
		binderBean.set("TRANSID", treeBean.getStr("TRANSID"));

		if (treeBean.getStr("NODETYPE").equals(WfeBinder.USER_NODE_PREFIX)) {
			binderBean.set("LEAF", Constant.YES);
			binderBean.set("USER_IMG", treeBean.getStr("USER_IMG"));
		}
		binderBean.set("ID", id);
		binderBean.set("SORT", treeBean.getStr("SORT"));
		binderBean.set("PID", pid);
		if (objs.containsKey(treeBean.getStr("ID"))) {
			return;
		}

		if (pid.length() == 0) {
			rootNodeList.add(binderBean);
		} else if (objs.containsKey(String.valueOf(pid))) {
			WfResTreeNode parentTree = (WfResTreeNode) objs.get(pid);
			parentTree.addChild(binderBean);
		}

		objs.put(id, binderBean);
	}
	
	/**
	 * 
	 * @return 获取根节点列表
	 */
	public List<WfResTreeNode> getRootNodeList() {
		return rootNodeList;
	}
}
