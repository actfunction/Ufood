package com.rh.core.wfe.resource;

import com.rh.core.org.UserBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;

/**
 * 组织机构扩展过滤接口，用于处理不能通过“组织资源定义”功能设置接收用户的情况。 <br>
 * 在工作流送下一个节点时，先判断是否设置了扩展过滤接口，如果有则执行扩展过滤类，并取得最终接收人 ，<br>
 * 否则使用组织资源定义的结果，取得最终接收人。
 * 
 * @author wanglong
 */
public interface ExtendBinder {
    
    /**
     * 执行run方法，返回组织机构过滤结果。
     * 
     * @param currentWfAct 当前节点实例
     * @param nextNodeDef 下一节点的定义
     * @param doUser 节点办理人
     * @return 组织机构绑定结果对象ExtendBinderResult
     */
    ExtendBinderResult run(WfAct currentWfAct , WfNodeDef nextNodeDef, UserBean doUser);
}
