package com.rh.sup.util;

import com.rh.core.base.Bean;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.WfProcess;

public class TodoUtils{
	public String addTodo(Bean servBean, WfProcess process, String userCode) {
		
		try {
			servBean.get("S_CODE");
			//向司局长插入一条代办
			TodoServ serv = new TodoServ();
			TodoBean dataBean = new TodoBean();
			dataBean.setTitle(servBean.getStr("DC_SX"));
			dataBean.setDataCode(servBean.getStr("S_CODE"));
			dataBean.setOperation("司局长审批");
			dataBean.setSender(process.getSUserId());
			dataBean.setCodeName("督查申请立项单");
			dataBean.setEmergency(1);
			dataBean.setOwner(userCode);
			dataBean.setCode(process.getServId());
			dataBean.setObjectId1(servBean.getId());
			dataBean.setBenchFlag(1);
			dataBean.setCatalog(1);
			dataBean.setServId(process.getServId());
			dataBean.setFrom("wf");
			dataBean.setUrl("SU_SNLX_APPLY.byid.do?data={_PK_:"+servBean.getId()+"}");
			serv.addToDo(new ParamBean(dataBean));
		} catch (Exception e) {
			throw new RuntimeException("插入待办异常");
		}
		return "true";
	}
}
