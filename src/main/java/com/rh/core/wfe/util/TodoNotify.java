package com.rh.core.wfe.util;

import com.rh.core.base.Bean;
import com.rh.core.wfe.WfAct;

/**
 * 待办提醒
 *
 */
public interface TodoNotify {

    /**
     * 
     * @param dataBean 数据Bean
     * @param wfAct 节点实例
     */
    void send(Bean dataBean, WfAct wfAct);
    
    void send(Bean dataBean);
}
