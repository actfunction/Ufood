package com.rh.msg.job;

import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

import com.rh.msg.MsgModifyUtil;
/*
 * kfzx-xuyj01
 * 初始化人员、机构、重构机构树,人员机构数据
 */
public class MsgDataInitJob extends RhJob{

	@Override
	protected void executeJob(RhJobContext arg0) {
		// TODO Auto-generated method stub
		//将用户从temp表导入两张user表
		MsgModifyUtil.initialUserData();
		//将机构从temp表导入两张dept表
		MsgModifyUtil.initialOrgData();
		//重构机构树 codepath tdept odept
		MsgModifyUtil.modifyCodePathByRoot();
		//将机构用户从temp表导入两张dept_user表
		MsgModifyUtil.initialOrgUserData();
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}
	
}
