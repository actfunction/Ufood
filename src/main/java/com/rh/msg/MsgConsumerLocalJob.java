package com.rh.msg;

import com.rh.core.base.Context;
import com.rh.core.util.scheduler.RhJobContext;
import com.rh.core.util.scheduler.RhLocalJob;

public class MsgConsumerLocalJob extends RhLocalJob {
	private static boolean isFirst = true;

	@Override
	protected void executeJob(RhJobContext paramRhJobContext) {
		// TODO Auto-generated method stub
		String isOn = Context.getSyConf("MSG_CONUSMER_INIT_SWITCH", "1");//开关
		if (isFirst&&"1".equals(isOn)) {
			MsgConsumer.startConsumer();
			isFirst = false;
		}

	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}

}
