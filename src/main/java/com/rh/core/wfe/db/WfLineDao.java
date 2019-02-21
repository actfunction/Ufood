package com.rh.core.wfe.db;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 工作流连线 数据库 操作类
 * 
 * @author wanglong
 */
public class WfLineDao {
    
    /**
     * 工作流连线定义 服务 code
     */
    public static final String SY_WFE_LINE_DEF_SERV = "SY_WFE_LINE_DEF";
        
    /**
     * 
     * @param lineBean 线定义
     * @return 线定义bean
     */
    public static Bean insertLineDef(Bean lineBean) {
        Bean lineDefBean = ServDao.create(SY_WFE_LINE_DEF_SERV, lineBean);
        return lineDefBean;
    }
    
    /**
     * 根据流程编码得到连线列表
     * 
     * @param procCode 流程编码
     * @param cmpyId 公司ID
     * @return 流程连线列表
     */
    public static List<Bean> getLineListByProcCode(String procCode ,
        String cmpyId) {
        Bean paramBean = new Bean();
        paramBean.set("PROC_CODE", procCode);
        paramBean.set("S_CMPY", cmpyId);
        paramBean.set(Constant.PARAM_ORDER, " LINE_SORT ASC");

        return ServDao.finds(SY_WFE_LINE_DEF_SERV, paramBean);
    }
    
	/**
	 * 
	 * @param procCode 流程编码
	 */
	public static void deleteLineDefByProcCode(String procCode) {
		Bean paramBean = new Bean();
		paramBean.set("PROC_CODE", procCode);
		
		ServDao.deletes(SY_WFE_LINE_DEF_SERV, paramBean);
	}
            
}
