package com.rh.core.wfe.def;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.wfe.db.WfProcDefDao;

/**
 * 工作流迁移
 *
 */
public class MigrateProcData {
    /**
     * 构建体方法
     */
    public MigrateProcData() {
        
    }
    
    /**
     * 迁移
     * @param cmpyID 公司编码
     * @param procCode 流程编码
     */
    public void migrate(String cmpyID , String procCode) {
        Bean procDef = WfProcDefDao.getWfProcBeanByProcCode(procCode);
        
        MigrateNodeData nodeData = new MigrateNodeData(cmpyID, procCode);
        try {
            String xml = nodeData.migrate(procDef.getStr("PROC_XML"));
            
            procDef.set("PROC_XML", xml);
            String servId = Context.getThread(Context.THREAD.SERVID, WfProcDefDao.SY_WFE_PROC_DEF_SERV);
            ServDao.save(servId, procDef);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
