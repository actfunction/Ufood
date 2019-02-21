package com.rh.core.wfe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.DateUtils;
import com.rh.core.wfe.db.WfProcInstDao;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.def.WfServCorrespond;
import com.rh.core.wfe.resource.GroupBean;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 创建工作流
 * 
 * @author wanglong
 */
public class WfProcessFactory {
	private static Log log = LogFactory.getLog(WfProcessFactory.class);
	
	
	/**
     * cache{Serv_ID:List<WfProcDef>wfProcDef}
     * @param servId 服务ID
     * @param dataBean 数据Bean
     * @param startUsers 起草任务处理人   没设置用户时，取当前登录用户
     * @return 启动流程之后，的第一个节点实例
     */
    public static WfAct startProcess(String servId, Bean dataBean, GroupBean startUsers) {
        return startProcess(servId, dataBean, startUsers, null);
    }

	
	/**
     * cache{Serv_ID:List<WfProcDef>wfProcDef}
     * @param servId 服务ID
     * @param dataBean 数据Bean
     * @param startUsers 起草任务处理人   没设置用户时，取当前登录用户
     * @param parentWfAct 父流程任务节点
     * @return 启动流程之后，的第一个节点实例
     */
    public static WfAct startProcess(String servId, Bean dataBean, GroupBean startUsers, WfAct parentWfAct) {
        if (!dataBean.containsKey("S_WF_INST")) { //包含可挂接流程
            log.info("该表单不能挂接流程，没有相关字段, servId = " + servId + " dataId = " + dataBean.getId());
            return null;
        }
        
        WfProcDef wfProcDef = WfServCorrespond.getProcDef(servId, dataBean);
        
        if (null == wfProcDef) {
            log.info("该表单没能获取到符合条件的工作流定义, servId = " + servId + " dataId = " + dataBean.getId());
            return null;
        }
        
        return startProcess(wfProcDef, dataBean, startUsers);
    }
    
    
    /**
     * 
     * @param wfProcDef 流程定义
     * @param dataBean 数据Bean
     * @param startUsers 起草任务处理人   没设置用户时，取当前登录用户
     * @return 节点实例
     */
    public static WfAct startProcess(WfProcDef wfProcDef, Bean dataBean, GroupBean startUsers) {
        return startProcess(wfProcDef, dataBean, startUsers, null);
    }
       
    /**
     * 
     * @param wfProcDef 流程定义
     * @param dataBean 数据Bean
     * @param startUsers 起草任务处理人   没设置用户时，取当前登录用户
     * @param parentWfAct 父流程任务节点
     * @return 节点实例
     */
    public static WfAct startProcess(WfProcDef wfProcDef, Bean dataBean, GroupBean startUsers, WfAct parentWfAct) {
        
        ServDefBean  servDef = ServUtils.getServDef(wfProcDef.getServId());
        if (!servDef.containsItem("S_WF_INST")) { //包含流程字段
            log.info("该表单不能挂接流程，没有相关字段, servId = " + wfProcDef.getStr("SERV_ID") 
                    + " dataId = " + dataBean.getId());
            throw new TipException("该服务没有定义流程相关字段，不能启动流程。");
        }
        log.debug("--------------startProcess--------------");

        Bean aProcInstBean = new Bean();
        if (parentWfAct != null) {
            aProcInstBean.set("INST_PARENT_NODE", parentWfAct.getId());
        }
        aProcInstBean.set("PROC_CODE", wfProcDef.getStr("PROC_CODE"));

        if (null == wfProcDef.getStr("SERV_ID")
                || wfProcDef.getStr("SERV_ID").length() < 1) {
            throw new RuntimeException("流程没有关联表单");
        }

        aProcInstBean.set("SERV_ID", wfProcDef.getStr("SERV_ID"));
        aProcInstBean.set("S_CMPY", wfProcDef.getStr("S_CMPY"));

        // 起草的时候，
        aProcInstBean.set("DOC_ID", dataBean.getId());

        aProcInstBean.set("INST_IF_RUNNING",
                WfeConstant.WFE_NODE_INST_IS_RUNNING);
        aProcInstBean.set("INST_BTIME", DateUtils.getDatetime());

        aProcInstBean = WfProcInstDao.insertWfProcInst(aProcInstBean);

        // 创建一条流程实例记录
        WfProcess wfProc = new WfProcess(aProcInstBean);

        // 创建起始点的实例
        WfAct wfAct = wfProc.createStartWfNodeInst(startUsers);

        log.debug("create start node complete");

        return wfAct;
    }
}
