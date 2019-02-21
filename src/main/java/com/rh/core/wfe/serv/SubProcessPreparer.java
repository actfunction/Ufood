package com.rh.core.wfe.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.resource.GroupBean;

/**
 * 子流程数据准备接口
 * @author wanglong
 *
 */
public interface SubProcessPreparer {

    /**
     * 准备流程运行需要的数据。可以根据自己的业务需要进行扩展。
     * 默认实现是根据关联服务进行新建；如果要使用已经存在的数据，需自行扩展。
     * @param startUsers 起草节点处理人
     * @return 流程运行的服务数据Bean。与startUsers列表的顺序必须是一致的，列表大小必须是一样的。
     */
    List<Bean> prepareData(List<GroupBean> startUsers);

    /**
     * 
     * @return 父流程节点实例
     */
    WfAct getParentAct();

    /**
     * @param parentAct 父流程节点实例
     */
    void setParentAct(WfAct parentAct);
    
    /**
     * 根据子流程数据，确定要使用的子流程定义。可以根据自己的业务需要进行扩展。
     * @param servId 子流程服务
     * @param dataBean 数据信息
     * @return 子流程定义
     */
    WfProcDef getProcDef(String servId, Bean dataBean);
}
