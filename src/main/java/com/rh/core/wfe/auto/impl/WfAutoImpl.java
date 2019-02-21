package com.rh.core.wfe.auto.impl;

import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.auto.WfAuto;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.def.WfServCorrespond;

public class WfAutoImpl implements WfAuto {

	@Override
	public OutBean startup(ParamBean paramBean) {
		OutBean result = new OutBean();
		return result;
	}

	@Override
	public OutBean getStartNode(ParamBean paramBean) {
		OutBean result = new OutBean();
		WfProcDef wfProcDef = WfServCorrespond.getProcDef(paramBean.getStr("servId"), null);
		WfNodeDef startNode = wfProcDef.findStartNode();
		result.set("startNode", startNode);
		return result;
	}

	@Override
	public OutBean send(ParamBean paramBean) {
		OutBean result = new OutBean();
		return result;
	}

	@Override
	public OutBean getNodeBinder(ParamBean paramBean) {
		OutBean result = new OutBean();
		return result;
	}

	@Override
	public OutBean getLine(ParamBean paramBean) {
		OutBean result = new OutBean();
		return result;
	}

	@Override
	public OutBean getLineCond(ParamBean paramBean) {
		OutBean result = new OutBean();
		return result;
	}

	@Override
	public OutBean goBack(ParamBean paramBean) {
		OutBean result = new OutBean();
		return result;
	}

	@Override
	public OutBean end(ParamBean paramBean) {
		OutBean result = new OutBean();
		return result;
	}

}
