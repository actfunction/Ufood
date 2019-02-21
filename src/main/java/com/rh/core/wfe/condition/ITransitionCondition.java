/**
 * 
 */
package com.rh.core.wfe.condition;

import com.rh.core.wfe.WfAct;

/**
 * 判断流程节点能否继续流转。目前只有子流程节点进行这种判断。
 * @author wanglong
 *
 */
public interface ITransitionCondition {
    
    /**
     * 判断流程节点能否继续流转
     * @param wfAct 要进行判断的任务节点
     * @return 能继续向下流转，true；否则，false
     */
    boolean check(WfAct wfAct);
    
}
