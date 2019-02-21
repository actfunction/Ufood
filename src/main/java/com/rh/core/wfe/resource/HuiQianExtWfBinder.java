package com.rh.core.wfe.resource;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;

/**
 * 
 * @author wanglong
 *
 */
public class HuiQianExtWfBinder implements ExtendBinder {

	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		Bean dataBean = currentWfAct.getProcess().getServInstBean();
        String extCls = nextNodeDef.getStr("NODE_EXTEND_CLASS");
        String configStr = "";
        
        String[] classes = extCls.split(",,");
        if (classes.length == 2) {
            configStr = classes[1];
        }
		Bean configBean = JsonUtils.toBean(configStr);
		
		String depts = dataBean.getStr(configBean.getStr("fieldStr"));
		
		ExtendBinderResult result = new ExtendBinderResult();
		result.setDeptIDs(depts);
		result.setRoleCodes(configBean.getStr("roleCodes"));
		result.setUserIDs(configBean.getStr("userIDs"));
		if (configBean.getStr("bindRole").equals("true")) {
			result.setBindRole(true);
		} else {
			result.setBindRole(false);
		}
		result.setAutoSelect(true);
		return result;
	}
	
	/**
	 * 
	 * @param nodeDef 节点定义
	 * @return 字段定义
	 */
	public static String getConfigField(WfNodeDef nodeDef) {
        String extCls = nodeDef.getStr("NODE_EXTEND_CLASS");
        String configStr = "";
        
        String[] classes = extCls.split(",,");
        if (classes.length == 2) {
            configStr = classes[1];
        }
		Bean configBean = JsonUtils.toBean(configStr);
		
		String fieldStr = configBean.getStr("fieldStr").trim();
		
		return fieldStr;
	}
	
	/**
	 * 
	 * @param nodeDef 节点定义
	 * @return 节点是否会商节点
	 */
	public static boolean isNodeHuiShang(WfNodeDef nodeDef) {
		String extCls = nodeDef.getStr("NODE_EXTEND_CLASS");
		if (extCls.indexOf("HuiQianExtWfBinder") != -1) {
			return true;
		}
		
		return false;
	}
}
