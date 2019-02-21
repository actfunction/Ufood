package com.rh.core.wfe.resource;

import java.util.List;

import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;

/**
 * 领导秘书 对应关系的扩展
 * @author wanglong
 *
 */
public class LeaderExtWfBinder implements ExtendBinder {

	/**
	 * 秘书和负责的行领导对应的功能，支持某个秘书,不能送别的行领导,只能送自己直接领导。
	 * @param currentWfAct 当前节点实例
	 * @param nextNodeDef 下个节点定义
	 * @param doUser 节点办理人
	 * @return 扩展匹配的结果
	 */
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		//获取当前人的领导列表
		String curUserCode = doUser.getCode();
		
		List<UserBean> leaderList = UserMgr.getLeaders(curUserCode);
		if (leaderList.size() == 0) {
			throw new TipException("当前用户没有对应的领导，请到用户关系中进行配置");
		}
		
		
		StringBuilder leaders = new StringBuilder();
	    for (UserBean userBean: leaderList) {
	    	leaders.append(userBean.getCode());
	    	leaders.append(",");
	    }
	    if (leaders.length() > 0) {
	    	leaders.setLength(leaders.length() - 1);
	    }
		
		ExtendBinderResult result = new ExtendBinderResult();
		result.setDeptIDs("");
		result.setRoleCodes("");
		result.setUserIDs(leaders.toString());
		result.setBindRole(false);
		
		return result;
	}
}
