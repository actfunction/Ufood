package com.rh.msg.job;

import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

import com.rh.msg.MsgModifyUtil;
/*
 * kfzx-xuyj01
 * 初始化机构数据并重构机构树
 */
public class MsgOrgInitJob extends RhJob{

	@Override
	protected void executeJob(RhJobContext arg0) {
		//将机构从temp表导入两张dept表
		MsgModifyUtil.initialOrgData();
		//重构机构树 codepath tdept odept
		MsgModifyUtil.modifyCodePathByRoot();
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}
}
