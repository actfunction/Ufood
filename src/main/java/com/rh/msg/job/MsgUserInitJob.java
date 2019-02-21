package com.rh.msg.job;

import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

import com.rh.msg.MsgModifyUtil;
/*
 * kfzx-xuyj01
 * 将用户从temp表导入两张user表
 */
public class MsgUserInitJob extends RhJob{
	@Override
	protected void executeJob(RhJobContext arg0) {
		// TODO Auto-generated method stub
		//将用户从temp表导入两张user表
		MsgModifyUtil.initialUserData();
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}
}
