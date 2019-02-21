package com.rh.core.wfe.resource;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;

/**
 * 组织资源树节点
 *
 */
public class WfResTreeNode extends Bean {
	private static final long serialVersionUID = 7609409540809694983L;

	/**
	 * 添加树的孩子节点
	 * @param treeNodeBean 树节点
	 */
	public void addChild(WfResTreeNode treeNodeBean) {
    	if (this.get("CHILD") != null) {
    		@SuppressWarnings("unchecked")
			List<WfResTreeNode> children = (List<WfResTreeNode>) this.get("CHILD");
    		children.add(treeNodeBean);
    	} else {
    		List<WfResTreeNode> children = new ArrayList<WfResTreeNode>();
    		children.add(treeNodeBean);
    		this.set("CHILD", children);
    	}
    }
    
    
}
