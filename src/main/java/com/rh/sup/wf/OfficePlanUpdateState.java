package com.rh.sup.wf;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.AbstractLineEvent;
import com.rh.sup.serv.SupApproPlanServ;

/**署发流程:成果体现及办理计划
 * 督查处审批办理计划情况 修改办理计划状态为已审核
 * N22——>N45
 */
public class OfficePlanUpdateState extends AbstractLineEvent {
	
	//办理计划状态  待审核 2
    private final String PLAN_CHECK_STATE ="2";
    //办理计划状态  已审核 3
    private final String PLAN_DONE_STATE ="3";

	@Override
	public void forward(WfAct wfAct, WfAct wfAct1, Bean bean) {
		//将待审核状态修改为已审核状态
        WfProcess process = wfAct.getProcess();
        Bean servBean = process.getServInstBean();
        String approId = servBean.getId();
      	String deptCode = Context.getUserBean().getDeptCode();
      	
        SupApproPlanServ gain = new SupApproPlanServ();
        ParamBean paramBean = new ParamBean();
        paramBean.set("approId",approId);
        paramBean.set("curState",PLAN_CHECK_STATE);
        paramBean.set("upState",PLAN_DONE_STATE);
        paramBean.set("deptCode", deptCode);
        gain.updatePlanWfState(paramBean);

	}

	
	@Override
	public void backward(WfAct wfAct, WfAct wfAct1, Bean bean) {
		
	}
	
}