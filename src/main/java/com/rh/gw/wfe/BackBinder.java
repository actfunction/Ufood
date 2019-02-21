package com.rh.gw.wfe;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 
 * 
 * @author 
 * @date 2018/12/19
 */
public class BackBinder implements ExtendBinder {

	@Override
	public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
		ExtendBinderResult result = new ExtendBinderResult();

		List<Bean> nodeInstList = currentWfAct.getProcess().getAllNodeInstList();
		String userCode = "";
		for (Bean nodeInst : nodeInstList) {
			if (nodeInst.getStr("NODE_CODE").equals(nextNodeDef.getStr("NODE_CODE"))
					&& nodeInst.getInt("NODE_IF_RUNNING") == WfeConstant.NODE_NOT_RUNNING) {
				userCode = nodeInst.getStr("DONE_USER_ID");
				break;
			}
		}

		result.setUserIDs(userCode);
		result.setBindRole(false);
		result.setAutoSelect(false);
		return result;
	}
}
