package com.rh.core.wfe;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.wfe.db.ServDataDao;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.def.WfProcDefManager;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 流程实例
 * 
 */
public abstract class AbstractWfProcess {
//	private static Log log = LogFactory.getLog(AbstractWfProcess.class);
    private static final String ROLE_SUPER_WF_ADMIN = "RSUPER_WF_ADMIN";
	
    /**
     * 表单业务数据对象
     */
    private Bean servInstBean;
    
    private WfProcDef procDef = null;
    
    /** 数据是否来自运行数据表 **/
    private Boolean isRunningData = null;
    
    /**
     * 
     * @return 流程定义
     */
    public WfProcDef getProcDef() {
        if (procDef == null) {
            procDef = WfProcDefManager.getWorkflowDef(this.getProcInstBean().getStr("PROC_CODE")); 
        }
        
        return procDef;
    }
    
    /**
     * @return 表单数据对象
     */
    public Bean getServInstBean() {
        if (servInstBean == null) {
        	servInstBean = ServDataDao.findServInst(getProcInstBean().getStr("SERV_ID"), this
                    .getProcInstBean().getStr("DOC_ID"));
        }
        return servInstBean;
    }
    
    /**
     * 
     * @param aServInstBean 表单数据对象
     */
    public void setServInstBean(Bean aServInstBean) {
    	this.servInstBean = aServInstBean;
    }
    
    /**
     * @return 表单实例ID
     */
    public String getDocId() {
        return getProcInstBean().getStr("DOC_ID");
    }
    
    /**
     * @return 流程实例对象
     */
    public abstract Bean getProcInstBean();
    
    /**
     * 取得工作流流程实例的ID
     * 
     * @return 工作流流程实例的ID
     */
    public String getId() {
    	// return getProcInstBean().getId();
        return getProcInstBean().getStr("PI_ID");
    }
    
    /**
     * @return 公司ID
     */
    public String getCmpyId() {
        return getProcInstBean().getStr("S_CMPY");
    }
    
    /**
     * @return 起草人ID
     */
    public String getSUserId() {
        return getProcInstBean().getStr("S_USER");
    }
    
    /**
     * @return 流程 中 表单ID (区分是公文还是表单)
     */
    public String getServId() {
        return getProcInstBean().getStr("SERV_ID");
    }
    
    /**
     * 
     * @return 当前流程对应的审批单的服务定义
     */
    public ServDefBean getServDef() {
        ServDefBean servDef = ServUtils.getServDef(this.getServId());
        return servDef;
    }
    
    /**
     * @return 流程编码，来自流程定义
     */
    public String getCode() {
        return getProcInstBean().getStr("PROC_CODE");
    }
    
    /**
     * @return 流程运行状态
     */
    public int getRunningStatus() {
        return getProcInstBean().getInt("INST_IF_RUNNING");
    }
    
    /**
     * 流程是否运行状态
     * @return 流程是否运行状态
     */
    public boolean isRunning() {
        int procIsRunning = getRunningStatus();
        
        if (WfeConstant.WFE_PROC_INST_IS_RUNNING == procIsRunning) {
            return true;
        }
        
        return false;
    }
    
    /**
     * @return 是否是流程管理人
     */
    public boolean isProcManage() {
        String procMgr = this.getProcDef().getStr("PROC_MANAGE");
        if (Context.getUserBean().existInRole(procMgr)) {
            return true;
        }
        
        if (Context.getUserBean().existInRole(ROLE_SUPER_WF_ADMIN)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 
     * @return 是否读历史数据表
     */
    public boolean isRunningData() {
        if (isRunningData == null) {
            if (this.isRunning()) {
                isRunningData = true;
            } else {
                isRunningData = false;
            }
        }

        return isRunningData;
    }
    
    /**
     * @param isRunningData 是否读运行数据表数据
     */
    public void setIsRunningData(boolean isRunningData) {
        this.isRunningData = isRunningData;
    }  
	
}
