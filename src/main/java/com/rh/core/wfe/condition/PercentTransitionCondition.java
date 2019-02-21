/**
 * 
 */
package com.rh.core.wfe.condition;

import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.serv.WfSubProcActHandler;

/**
 * 使用子流程完成百分比判断主流程是否能继续往下运行。完成百分比的条件在节点的"TRANSITION_CONDITION_PERCENT"配置。
 * @author wanglong
 *
 */
public class PercentTransitionCondition implements ITransitionCondition {

    /* 
     * @see com.rh.core.wfe.condition.IActTransitionCondition#check(com.rh.core.wfe.WfAct)
     */
    @Override
    public boolean check(WfAct wfAct) {
        WfNodeDef nodeDef = wfAct.getNodeDef();
        Double finishPercent = nodeDef.getDouble("TRANSITION_CONDITION_PERCENT");
        if (finishPercent < 1E-5) {
            return true;
        } else {
            WfSubProcActHandler handler = new WfSubProcActHandler(wfAct);
            //查询已经办结的子流程的数量
            int finishedSubProcess = handler.getFinishedSubProcessCount();
            int runningSubProcess = handler.getRunningSubProcessCount();
            if (finishedSubProcess / ((finishedSubProcess + runningSubProcess) * 1.0) >= finishPercent) {
                return true;
            }
        }
        return false;
    }

}
