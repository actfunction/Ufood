package com.rh.food.serv;

import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

import com.rh.food.util.GenUtil;


/**
 * 审计管理分系统行政办公管理子系统
 * @author: kfzx-cuiyc
 * @date: 2018年12月6日 下午2:39:51
 * @version: V1.0
 * @description: 公文交换机构信息维护类
 */
public class OaGwDeptQueueInfoServ extends CommonServ{

	@Override
	public OutBean save(ParamBean paramBean) {
		
		
		//设置交换机构id
		String newDqiId = GenUtil.getAutoIdNumner("GDQIS", "OA_GW_DEPT_QUEUE_INFO_SEQ", "NUM");
		System.out.println("交换机构id为："+newDqiId);
		
		return super.save(paramBean);
	}

	
}
