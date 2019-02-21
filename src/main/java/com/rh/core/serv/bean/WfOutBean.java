package com.rh.core.serv.bean;

import com.rh.core.base.Bean;
import com.rh.core.serv.OutBean;

/**
 * 服务执行返回信息Bean
 * 
 * @author wanglong
 * 
 */
public class WfOutBean extends OutBean {
    /**
     * sid
     */
    private static final long serialVersionUID = 3517658363946650097L;

    /**
     * 对象构造方法
     */
    public WfOutBean() {
        super();
    }

    /**
     * 对象构造方法
     * 
     * @param outBean 数据对象
     */
    public WfOutBean(OutBean outBean) {
        super(outBean);
    }

    /**
     * 获取工作流流程实例信息
     * @return 流程实例
     */
    public Bean getWfProcInst() {
        return this.getBean("_WF_PROC_INST_");
    }
    
    /**
     * 设置工作流流程实例
     * @param procInstBean 
     * @return 当前对象
     */
    public WfOutBean setWfProcInst(Bean procInstBean) {
        this.set("_WF_PROC_INST_", procInstBean);
        return this;
    }

    /**
     * 获取工作流节点实例信息
     * @return 节点实例
     */
    public Bean getWfActInst() {
        return this.getBean("_WF_ACT_INST_");
    }
    
    /**
     * 设置工作流节点实例信息
     * @param actInstBean 节点实例
     * @return 当前对象
     */
    public WfOutBean setWfActInst(Bean actInstBean) {
        this.set("_WF_ACT_INST_", actInstBean);
        return this;
    }
    
    /**
     * 设置对象，支持级联设置
     * @param key   键值
     * @param obj   对象数据
     * @return this，当前Bean
     */
    public WfOutBean set(Object key, Object obj) {
        put(key, obj);
        return this;
    }
}
