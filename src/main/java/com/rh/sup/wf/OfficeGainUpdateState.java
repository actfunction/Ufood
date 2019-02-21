package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.AbstractLineEvent;
import com.rh.sup.serv.SupApproGain;

/**署发流程:办理情况
 * 督查处审批办理情况 修改办理状态为已审核
 * N24——>N45(送交下次办理)
 */
public class OfficeGainUpdateState extends AbstractLineEvent {
	
	//办理状态 待审核 2
    private final String GAIN_CHECK_STATE ="2";
    //办理状态 已审核 3
    private final String GAIN_DONE_STATE ="3";

	@Override
	public void forward(WfAct wfAct, WfAct wfAct1, Bean bean) {
		//将待审核状态修改为已审核状态
        WfProcess process = wfAct.getProcess();
        Bean servBean = process.getServInstBean();
        String approId = servBean.getId();
      	//String deptCode = Context.getUserBean().getDeptCode();
      	
        SupApproGain gain = new SupApproGain();
        ParamBean paramBean = new ParamBean();
        paramBean.set("approId",approId);
        paramBean.set("curState",GAIN_CHECK_STATE);
        paramBean.set("upState",GAIN_DONE_STATE);
//        paramBean.set("deptCode", deptCode);
//        gain.updateWfState(paramBean);

        // 定时任务
        ParamBean paramBeanTemp = new ParamBean();
        paramBeanTemp.set("servId","OA_SUP_APPRO_OFFICE");
        paramBeanTemp.set("approId",approId);
        Bean nodeBean = wfAct.getNodeInstBean();
        String nodeId = nodeBean.getId(); // 流程实例ID
        paramBeanTemp.set("niId", nodeId);
        ServMgr.act("OA_SUP_APPRO_BUREAU", "waitNextGain", paramBeanTemp);

	}
	
	
	@Override
	public void backward(WfAct wfAct, WfAct wfAct1, Bean bean) {
		
	}

}
