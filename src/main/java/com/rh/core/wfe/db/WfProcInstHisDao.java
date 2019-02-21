package com.rh.core.wfe.db;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;

/**
 * 流程实例历史表  数据库操作
 * @author wanglong
 *
 */
public class WfProcInstHisDao {
	/**
	 * 工作流实例历史 服务 code
	 */
	public static final String SY_WFE_PROC_INST_HIS_SERV = "SY_WFE_PROC_INST_HIS";
	
	
    /**
     * 通过流程实例ID 取得 流程实例对象
     * 
     * @param piId
     *            流程实例ID
     * @return 流程实例对象
     */
    public static Bean findProcInstById(String piId) {
        Bean aProcInstBean = ServDao.find(SY_WFE_PROC_INST_HIS_SERV, piId);

        if (null == aProcInstBean) {
            String errorMsg = Context.getSyMsg("SY_WF_PROC_INST_HIS_NOT_EXIST", piId);
        	
            throw new RuntimeException(errorMsg);        	
        }

        return aProcInstBean;
    }	
	
	
	/**
	 * 办结的时候，复制流程实例对象到历史表
	 * 
	 * @param procInstBean
	 *            流程实例对象
	 * @return 流程实例历史对象
	 */
	public static Bean copyProcInstBeanToHis(Bean procInstBean) {
		Bean procInstHisBean = ServDao.create(
				SY_WFE_PROC_INST_HIS_SERV, procInstBean);
		return procInstHisBean;
	}
	
	/**
	 * 取消办结的时候，复制流程历史实例对象到流程实例表
	 * 
	 * @param procInstHisBean
	 *            流程实例对象
	 * @return 流程实例历史对象
	 */
	public static Bean copyProcInstHisBeanToInst(Bean procInstHisBean) {
		Bean procInstBean = ServDao.create(WfProcInstDao.SY_WFE_PROC_INST_SERV, procInstHisBean);
		return procInstBean;
	}
	
	/**
	 * 取消办结的时候，删除流程实例历史对象 ， 真删
	 * 
	 * @param procInstBean
	 *            流程实例历史对象
	 */
	public static void delProcInstBeanFromNodeInstHIS(Bean procInstBean) {
		ServDao.destroy(SY_WFE_PROC_INST_HIS_SERV, procInstBean.getId());
	}
	
	
}
