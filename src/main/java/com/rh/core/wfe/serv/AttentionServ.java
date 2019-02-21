package com.rh.core.wfe.serv;

import com.rh.core.base.Bean;
import com.rh.core.comm.entity.EntityMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ParamBean;

/**
 * 关注服务类
 * @author wanglong
 *
 */
public class AttentionServ extends CommonServ {

    /**
     * 查询前添加查询条件
     * 
     * @param paramBean 
     */
    public void beforeQuery(ParamBean paramBean) {
        StringBuilder strWhere = new StringBuilder();
        
        if (paramBean.isNotEmpty("PI_ID")) {
            strWhere.append(" and PI_ID = '").append(paramBean.getStr("PI_ID")).append("'");
        }
        
        paramBean.set("_extWhere", strWhere.toString());
    }
    
    /**
     * @param paramBean 参数Bean
     */
    public void beforeSave(ParamBean paramBean) {
        Bean entity = EntityMgr.getEntity(paramBean.getStr("DATA_ID"));
        
        if (null != entity) {
            paramBean.set("SERV_ID", entity.getStr("SERV_ID"));
            paramBean.set("TITLE", entity.getStr("TITLE"));
        }
        
    }
}
