package com.rh.msg.job;

import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

import com.rh.msg.MsgModifyUtil;
/*
 * kfzx-xuyj01
 * 将机构用户从temp表导入两张dept_user表
 */
public class MsgOrgUserDataInitJob extends RhJob{

	@Override
	protected void executeJob(RhJobContext arg0) {
		// TODO Auto-generated method stub
		//将机构用户从temp表导入两张dept_user表
		MsgModifyUtil.initialOrgUserData();
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}
}
