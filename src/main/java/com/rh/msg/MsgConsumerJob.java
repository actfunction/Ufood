package com.rh.msg;

import com.rh.core.util.scheduler.RhJob;
import com.rh.core.util.scheduler.RhJobContext;

public class MsgConsumerJob extends RhJob{

	@Override
	protected void executeJob(RhJobContext arg0) {
		// TODO Auto-generated method stub
		MsgConsumer.startConsumer();
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}

}
