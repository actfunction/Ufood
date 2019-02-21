package com.rh.core.wfe.db;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 流程定义数据库操作类
 * @author wanglong
 *
 */
public class WfProcDefDao {
	/**
	 * 工作流定义 服务 code
	 */
	public static final String SY_WFE_PROC_DEF_SERV = "SY_WFE_PROC_DEF";
	
	/**
	 * 工作流定义 公共流程
	 */
	public static final int SY_WFE_PROC_PUBLIC = 1;
	
	/**
	 * 工作流定义  非公共流程
	 */
	public static final int SY_WFE_PROC_NOT_PUBLIC = 2;
	
	
	/**
	 * 根据流程编码得到公共流程定义对象
	 * @param procEnName 流程英文名称
	 * @return 流程定义Bean
	 */
	public static Bean getWfPubProcDefBean(String procEnName) {
		Bean paramBean = new Bean();
		paramBean.set("EN_NAME", procEnName);
		paramBean.set("S_PUBLIC", SY_WFE_PROC_PUBLIC);
		
		Bean procDefBean = ServDao.find(SY_WFE_PROC_DEF_SERV, paramBean);

		if (null == procDefBean) {
            String errorMsg = Context.getSyMsg("SY_WF_PROC_PUB_NOT_EXIST", procEnName);
        	
            throw new RuntimeException(errorMsg);
		}

		return procDefBean;
	}
	
	/**
	 * 
	 * @param procCode 流程编码
	 */
	public static void delWfProcDefBeanByProcCode(String procCode) {
		Bean paramBean = new Bean();
		paramBean.set("PROC_CODE", procCode);

		ServDao.deletes(SY_WFE_PROC_DEF_SERV, paramBean);
	}
	
	/**
	 * 
	 * @param aProcEnName
	 *            流程编码
	 * @param cmpyID
	 *            公司ID
	 * @return 流程中节点定义对象
	 */
	public static Bean getWfProcBeanByEnName(String aProcEnName, String cmpyID) {
		Bean paramBean = new Bean();
		paramBean.set("EN_NAME", aProcEnName);
		paramBean.set("S_CMPY", cmpyID);
		Bean procDefBean = ServDao.find(SY_WFE_PROC_DEF_SERV,
				paramBean);

		if (null == procDefBean) {
            String errorMsg = Context.getSyMsg("SY_WF_PROC_ENNAME_NOT_EXIST", aProcEnName , cmpyID);
        	
            throw new RuntimeException(errorMsg);		
		}

		return procDefBean;
	}
	
	/**
	 * 
	 * @param servId
	 *            服务ID
	 * @param cmpyID
	 *            公司ID
	 * @return 流程中节点定义对象
	 */
	public static Bean getWfProcBeanByServId(String servId, String cmpyID) {
		Bean paramBean = new Bean();
		paramBean.set("SERV_ID", servId);
		paramBean.set("S_CMPY", cmpyID);
		Bean procDefBean = ServDao.find(SY_WFE_PROC_DEF_SERV,
				paramBean);

		if (null == procDefBean) {
            String errorMsg = "没找到流程定义绑定在服务" + servId;
        	
            throw new RuntimeException(errorMsg);		
		}

		return procDefBean;
	}
	
	   /**
     * 
     * @param aProcCode
     *            流程编码
     * @return 流程中节点定义对象
     */
    public static Bean getWfProcBeanByProcCode(String aProcCode) {
        Bean paramBean = new Bean();
        paramBean.set("PROC_CODE", aProcCode);
        Bean procDefBean = ServDao.find(SY_WFE_PROC_DEF_SERV,
                paramBean);

        return procDefBean;
    }
    
    /**
     * 判断流程编码是否已经使用
     * 
     * @param procCode 流程编码
     * @return true 流程编码已经使用;false 流程编码未使用
     */
    public static boolean procCodeIsUsed(String procCode) {
        Bean wfProcBean = ServDao.find(SY_WFE_PROC_DEF_SERV, new Bean().set("PROC_CODE", procCode));

        if (null == wfProcBean) {
            return false;
        }
        return true;
    }
	
	
	/**
	 * 判断流程编码是否已经使用
	 * 
	 * @param cmpyId
	 *            公司ID
	 * @param procEnName
	 *            流程编码
	 * @return true 流程编码已经使用;false 流程编码未使用
	 */
	public static boolean procCodeIsUsed(String cmpyId, String procEnName) {
		Bean paramBean = new Bean();
		paramBean.set("EN_NAME", procEnName);
		paramBean.set("S_CMPY", cmpyId);
		Bean wfProcBean = ServDao.find(SY_WFE_PROC_DEF_SERV,
				paramBean);

		if (null == wfProcBean) {
			return false;
		}
		return true;
	}
	
	/**
	 * @param cmpyCode 公司CODE
	 * @param servId 服务名
	 * @return 公司内挂载该服务的流程列表
	 */
	public static List<Bean> getProDefsByServId(String cmpyCode, String servId) {
		Bean queryBean = new Bean();
		queryBean.set("SERV_ID", servId);
		queryBean.set("S_CMPY", cmpyCode);
		queryBean.set("S_FLAG", Constant.YES_INT);

		return ServDao.finds(SY_WFE_PROC_DEF_SERV, queryBean);
	}
}
