/*
 * Copyright (c) 2012 Ruaho All rights reserved.
 */

package com.rh.core.comm.schedule.serv;

import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * @author wanglong
 * 
 */
public class SchedHistoryServ extends CommonServ {

	/** schedule history service */
	protected static final String JOB_HISTORY_SERVICE = "SY_COMM_SCHED_HIS";

	/**
	 * 构造函数
	 */
	public SchedHistoryServ() {
		super();
	}

	/**
	 * @see com.rh.core.serv.CommonServ#query(com.rh.core.base.Bean
	 * @param paramBean 参数Bean
	 * @return 结果Bean
	 * 
	 */
	public OutBean query(ParamBean paramBean) {
		paramBean.set("serv", JOB_HISTORY_SERVICE);
		return super.query(paramBean);

	}
}
