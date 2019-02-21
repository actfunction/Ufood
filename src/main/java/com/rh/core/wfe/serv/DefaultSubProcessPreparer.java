/**
 * 
 */
package com.rh.core.wfe.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.GroupBean;

/**
 * 子流程启动
 * @author wanglong
 *
 */
public class DefaultSubProcessPreparer extends AbstractSubProcessPreparer {
    
    
    /* (non-Javadoc)
     * @see com.rh.core.wfe.serv.SubProcessPreparer#prepareData(java.util.List)
     */
    @Override
    public List<Bean> prepareData(List<GroupBean> startUsers) {
        WfNodeDef parentNodeDef = parentAct.getNodeDef();
        WfProcess parentProcess = parentAct.getProcess();
        
        if (parentNodeDef.getInt("CREATE_DATA_FLAG") == Constant.YES_INT) {
            return createDataFromLinkConfig(parentProcess.getServId(), parentProcess.getServInstBean(), 
                    parentNodeDef.getStr("SUB_SERVICE_ID"), startUsers.size());
        } else {
            return getDataFromLinkConfig(parentProcess.getServId(), parentProcess.getServInstBean(), 
                    parentNodeDef.getStr("SUB_SERVICE_ID"));
        }
    }
}
