package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.AbstractLineEvent;
import com.rh.sup.serv.PointServ;
import com.rh.sup.serv.SupApproBureauServ;
import com.rh.sup.serv.SupApproGain;

/**
 *  督查处审批办理情况 修改办理状态为已审核
 */
public class PointGainUpdateState extends AbstractLineEvent{

    //办理状态 待审核 2
    private final String GAIN_CHECK_STATE ="2";
    //办理状态 已审核 3
    private final String GAIN_DONE_STATE ="3";

    @Override
    public void forward(WfAct wfAct, WfAct wfAct1, Bean bean) {
        // 将待审核状态修改为已审核状态
        WfProcess process = wfAct.getProcess(); // 获取当前流程
        Bean nodeBean = wfAct.getNodeInstBean();  // 获取当前节点实例
        String nodeId = nodeBean.getId(); // 获取节点实例ID
        Bean servBean = process.getServInstBean(); // 获取主单实例
        String approId = servBean.getId(); // 立项单主键
        SupApproGain gain = new SupApproGain();
        ParamBean paramBean = new ParamBean();
        paramBean.set("approId" ,approId);
        paramBean.set("curState", GAIN_CHECK_STATE);
        paramBean.set("upState", GAIN_DONE_STATE);
        gain.updateWfState(paramBean);

        // 发送定时任务 每个月再次生成代办
        paramBean.set("servId","OA_SUP_APPRO_POINT");
        paramBean.set("niId",nodeId);
        SupApproBureauServ bureauServ = new SupApproBureauServ();
        bureauServ.waitNextGain(paramBean);

        // 修改待办状态为 督查办结
        new PointServ() .updateToDoOperation(approId);
    }

    @Override
    public void backward(WfAct wfAct, WfAct wfAct1, Bean bean) {

    }
}
