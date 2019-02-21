package com.rh.core.wfe.resource;

import java.util.List;

import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;

/**
 * 
 * @author wanglong
 *
 */
public class SecretorExtWfBinder implements ExtendBinder {
	/**
	 * 秘书和负责的行领导对应的功能，自动获取领导的秘书
	 * @param currentWfAct 当前节点实例
	 * @param nextNodeDef 下个节点定义
	 * @param doUser 当前节点办理人
	 * @return 扩展匹配的结果
	 */
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		//获取当前人的  秘书
		String curUserCode = currentWfAct.getNodeInstBean().getStr("TO_USER_ID");
		
		List<UserBean> secretorList = UserMgr.getSecretor(curUserCode);
		if (secretorList.size() == 0) {
			throw new TipException("当前用户没有对应的秘书，请到用户关系中进行配置");
		}
		
		
		StringBuilder secretors = new StringBuilder();
	    for (UserBean userBean: secretorList) {
	    	secretors.append(userBean.getCode());
	    	secretors.append(",");
	    }
	    if (secretors.length() > 0) {
	    	secretors.setLength(secretors.length() - 1);
	    }
		
		ExtendBinderResult result = new ExtendBinderResult();
		result.setDeptIDs("");
		result.setRoleCodes("");
		result.setUserIDs(secretors.toString());
		result.setBindRole(false);
		
		return result;
	}
}
