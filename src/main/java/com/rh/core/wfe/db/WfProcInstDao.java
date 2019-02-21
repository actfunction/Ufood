package com.rh.core.wfe.db;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 流程实例 数据库操作类
 * @author wanglong
 *
 */
public class WfProcInstDao {
	/**
	 * 工作流 实例 服务 code
	 */
	public static final String SY_WFE_PROC_INST_SERV = "SY_WFE_PROC_INST";
	
	/**
	 * 
	 * @param piId 流程实例ID
	 * @return 该流程处理的总时间
	 */
	public static int getProcSumTime(String piId) {
	    Bean queryBean = new Bean();
	    queryBean.set(Constant.PARAM_SELECT, "SUM(NODE_DAYS)");
	    queryBean.set(Constant.PARAM_WHERE, " and PI_ID = '" + piId + "'");
	    
	    Bean dataBean = ServDao.find(WfNodeInstDao.SY_WFE_NODE_INST_SERV, queryBean);
	    
	    return dataBean.getInt("SUM(NODE_DAYS)");
	}
	
	/**
	 * 插入流程实例信息
	 * 
	 * @param procBean
	 *            流程信息
	 * @return Bean 流程实例信息
	 */
	public static Bean insertWfProcInst(Bean procBean) {
		Bean procInstBean = ServDao.create(SY_WFE_PROC_INST_SERV, procBean);

		return procInstBean;
	}
	
	/**
	 * 更新流程实例信息
	 * 
	 * @param procBean
	 *            流程实例信息
	 */
	public static void updateWfProcInst(Bean procBean) {
		ServDao.update(SY_WFE_PROC_INST_SERV, procBean);
	}
	
	/**
	 * 通过流程实例ID 取得 流程实例对象
	 * 
	 * @param piId
	 *            流程实例ID
	 * @return 流程实例对象
	 */
	public static Bean findProcInstById(String piId) {
		Bean aProcInstBean = ServDao.find(SY_WFE_PROC_INST_SERV, piId);

		if (null == aProcInstBean) {
            String errorMsg = Context.getSyMsg("SY_WF_PROC_INST_NOT_EXIST", piId);
        	
            throw new RuntimeException(errorMsg);			
		}

		return aProcInstBean;
	}
	
	/**
	 * 在办结的时候，删除流程实例对象 ， 真删
	 * 
	 * @param procInstBean
	 *            流程实例对象
	 */
	public static void destroyById(Bean procInstBean) {
	    ServDao.destroy(SY_WFE_PROC_INST_SERV, procInstBean.getId());
	}
	
	/**
	 * 删除流程实例对象 ， 假删
	 * 
	 * @param procInstId
	 *            流程实例ID
	 */
	public static void deleteById(String procInstId) {
		ServDao.delete(SY_WFE_PROC_INST_SERV, procInstId);
	}
}
