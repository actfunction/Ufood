package com.rh.msg.job;

import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

import com.rh.msg.MsgModifyUtil;
/*
 * kfzx-xuyj01
 * 只将temp表数据导入统一认证和系统表
 */
public class MsgOrgNoCodingInitJob extends RhJob{

	@Override
	protected void executeJob(RhJobContext arg0) {
		// TODO Auto-generated method stub
		MsgModifyUtil.initialOrgData();
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}


}
