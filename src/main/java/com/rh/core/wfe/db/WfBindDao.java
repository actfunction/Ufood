package com.rh.core.wfe.db;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;

/**
 * 绑定数据  数据库操作类
 * @author wanglong
 *
 */
public class WfBindDao {
	/**
	 * 工作流资源绑定 服务 code
	 */
	public static final String SY_WFE_BINDING_SERV = "SY_WFE_BINDING";
	
	/**
	 * 
	 * @param procCode
	 *            流程编码
	 * @param servId
	 *            服务编码
	 * @param cmpyId
	 *            公司ID
	 * @return 绑定数据
	 */
	public static Bean findBindBean(String procCode, String servId,
			String cmpyId) {
		Bean paramBean = new Bean();
		paramBean.set("PROC_CODE", procCode);
		paramBean.set("SERV_ID", servId);
		paramBean.set("S_CMPY", cmpyId);

		Bean bindBean = ServDao.find(SY_WFE_BINDING_SERV, paramBean);

		if (null == bindBean) {
			throw new RuntimeException("没有查询到流程编码为 " + procCode + " 表单为 "
					+ servId + "公司ID为 " + cmpyId + " 的绑定 定义");
		}

		return bindBean;
	}
	
	/**
	 * 
	 * @param procCode
	 *            流程编码
	 * @param cmpyId
	 *            公司ID
	 * @return 绑定数据
	 */
	public static List<Bean> findBindList(String procCode, String cmpyId) {
		Bean paramBean = new Bean();
		paramBean.set("PROC_CODE", procCode);
		paramBean.set("S_CMPY", cmpyId);

		List<Bean> bindBeanList = ServDao.finds(SY_WFE_BINDING_SERV, paramBean);

		return bindBeanList;
	}
}
