package com.rh.msg.job;

import com.rh.core.base.Context;
import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

import com.rh.msg.MsgModifyUtil;
/*
 * kfzx-xuyj01
 * 按照OA_MSG_ROOTORG这个配置项重构机构树
 */
public class MsgOrgCodingJob extends RhJob{

	@Override
	protected void executeJob(RhJobContext arg0) {
		//修复特殊机构
		MsgModifyUtil.modifySpecialDept();
		//重构机构树 codepath tdept odept
		String root = Context.getSyConf("OA_MSG_ROOTORG", "cnao0001");
		if ("0".equals(root)) {
			MsgModifyUtil.modifyCodePathByRoot();
		}else {
			MsgModifyUtil.modifyCodePathByRoot(root);
		}
		MsgModifyUtil.cleanAllServDeptCache();
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}
}
