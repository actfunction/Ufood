/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv.send;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;

/**
 * send scheme service extends <CODE>CommonServ</CODE>
 * @author wanglong
 * 
 */
public class SendSchemeServ extends SendCommonServ {

	/** 分发服务Id */
	private static final String SCHEME_SERVICE = "SY_SERV_SEND";

	/**
	 * 提供保存服务，有主键保存修改内容，没主键添加新数据。
	 * @param paramBean 参数Bean，自动根据getId()判断是添加保存还是修改保存
	 * @return 保存结果
	 */
	public OutBean save(ParamBean paramBean) {
		// save scheme data
		OutBean scheme = super.save(paramBean);
		// save scheme detail data
		List<Bean> detailList = new ArrayList<Bean>();
		String[] usersArray = getUsersParam(paramBean);
		for (String user : usersArray) {
			detailList.add(createDetail(scheme.getId(), user));
		}
		String role = getRoleParam(paramBean);
		String[] deptsArray = getDeptsParam(paramBean);
        if (deptsArray != null) {
            for (String dept : deptsArray) {
    			detailList.add(createDetail(scheme.getId(), dept, role));
    		}
		}
		// delete the detail data first, if is update model
		if (0 < paramBean.getId().length()) {
			ServDao.deletes(ServMgr.SY_COMM_SEND_DETAIL, new Bean().set("SCHEME_ID", paramBean.getId()));
		}
		ServDao.creates(ServMgr.SY_COMM_SEND_DETAIL, detailList);
		return scheme;
	}

	/**
	 * 提供基于主键的查询服务
	 * @param paramBean 参数Bean
	 * @return 查询结果
	 */
	public OutBean byid(ParamBean paramBean) {
		paramBean.set("serv", SCHEME_SERVICE);
		// get scheme data
		OutBean scheme = super.byid(paramBean);
		// get scheme details data
		List<Bean> details = ServDao.finds(ServMgr.SY_COMM_SEND_DETAIL,
				new Bean().set("SCHEME_ID", paramBean.getId()));
		// set into scheme bean
		setOutput(scheme, details);
		return scheme;
	}

	/**
	 * create and return scheme detail bean
	 * @param detailId detail id
	 * @param userCode user code
	 * @return <code>Bean</code>
	 */
	public Bean createDetail(String detailId, String userCode) {
		return new Bean().set("SCHEME_ID", detailId).set("DETAIL_TYPE", "1").set("ROLE_USER_CODE", userCode);
	}

	/**
	 * create and return scheme detail bean
	 * @param detailId detail id
	 * @param deptCode dept code
	 * @param role role code
	 * @return <code>Bean</code>
	 */
	public Bean createDetail(String detailId, String deptCode, String role) {
		return new Bean().set("SCHEME_ID", detailId).set("DETAIL_TYPE", "2").set("ROLE_USER_CODE", role)
				.set("S_DEPT", deptCode);
	}

	/**
	 * 提供基于主键的删除服务
	 * @param paramBean 参数Bean
	 * @return 删除结果
	 */
	public OutBean delete(ParamBean paramBean) {
		ServDao.deletes(ServMgr.SY_COMM_SEND_DETAIL, new Bean().set("SCHEME_ID", paramBean.getId()));
		return super.delete(paramBean);

	}

}
