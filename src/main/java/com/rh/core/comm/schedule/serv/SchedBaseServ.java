/*
 * Copyright (c) 2012 Ruaho All rights reserved.
 */
package com.rh.core.comm.schedule.serv;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.util.Constant;

/**
 * 计划调度服务类
 * @author wanglong
 */
public class SchedBaseServ extends CommonServ {

	/** system pause of operation code */
	public static final String SY_PAUSE_OPERATION_SUCCESSFUL = "SY_PAUSE_OPERATION_SUCCESSFUL";
	/** system start of operation code */
	public static final String SY_START_OPERATION_SUCCESSFUL = "SY_START_OPERATION_SUCCESSFUL";

	/**
	 * get ids
	 * @param paramBean param bean
	 * @return id array
	 */
	protected String[] getIds(Bean paramBean) {
		String[] ids = paramBean.getId().trim().split(Constant.SEPARATOR);
		return ids;
	}

}
