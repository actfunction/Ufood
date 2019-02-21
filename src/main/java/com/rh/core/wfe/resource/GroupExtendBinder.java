/**
 * 
 */
package com.rh.core.wfe.resource;

import java.util.List;

import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;

/**
 * 组织机构扩展过滤接口，用于处理不能通过“组织资源定义”功能设置接收用户的情况。 <br>
 * 通过该接口过滤的用户直接作为任务处理人向下流转，不送到前台选择。
 * 会为每个GroupBean生成一个任务实例，多用于抢占式任务。
 * @author wanglong
 *
 */
public interface GroupExtendBinder {
    
    /**
     * 执行run方法，返回组织机构过滤结果。
     * 
     * @param currentWfAct 当前节点实例
     * @param nextNodeDef 下一节点的定义
     * @return 处理人组
     */
    List<GroupBean> run(WfAct currentWfAct, WfNodeDef nextNodeDef);
}
