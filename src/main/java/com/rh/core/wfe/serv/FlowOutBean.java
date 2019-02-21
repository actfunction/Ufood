package com.rh.core.wfe.serv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.comm.mind.MindServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.WfUtils;

/**
 * 流经用户 能看意见，相关文件，修改痕迹，文稿
 * 
 * @author wanglong
 *
 */
public class FlowOutBean extends WfOut {
	/**
	 * 审批单查看模式：流经模式
	 */
	private static final String MODE_FLOW = "MODE_FLOW";

	private HashMap<String, Bean> mindCodeMap;

	/**
	 * 
	 * @param aWfProc
	 *            流程实例
	 * @param aOutBean
	 *            返回前台Bean
	 * @param aParamBean
	 *            参数
	 */
	public FlowOutBean(WfProcess aWfProc, Bean aOutBean, ParamBean aParamBean) {
		super(aWfProc, aOutBean, aParamBean);
	}

	@Override
	public void fillOutBean(WfAct wfAct) {
		// this.addFenFaProcBtn();

		addFlowProcBtn(wfAct);
		addTerminateOneBranch(wfAct);
		addNextStep(wfAct);

		String mindCode = wfAct.getNodeDef().getStr("MIND_CODE");
		String mindTerminal = wfAct.getNodeDef().getStr("MIND_TERMINAL");
		String mindCodeReguler = wfAct.getNodeDef().getStr("MIND_REGULAR");
		StringBuilder mindCodes = new StringBuilder();
		mindCodes.append(mindCode).append(",").append(mindTerminal).append(",").append(mindCodeReguler);

		mindCodeMap = new MindServ().getMindCodeBeanMap(mindCodes.toString());
		this.addMindCode(wfAct); // 普通意见和最终意见

		this.getOutBean().set(DISPLAY_MODE, MODE_FLOW);
	}

	/**
	 * 能走的节点
	 */
	private void addNextStep(WfAct wfAct) {
		List<Bean> nextSteps = new ArrayList<Bean>();
		wfAct.addParallelStep(this.getDoUser(), nextSteps);

		this.getOutBean().set("nextSteps", nextSteps);
	}

	/**
	 * 添加意见编码Bean
	 */
	private void addMindCode(WfAct wfAct) {
		String mindCode = wfAct.getNodeDef().getStr("MIND_CODE");

		// 执行过滤脚本
		String script = wfAct.getNodeDef().getStr("MIND_SCRIPT");
		boolean canWrite = this.execScript(script);

		Bean mindCodeBean = null;
		if (canWrite && StringUtils.isNotEmpty(mindCode)) {
			// 根据mindCode得到意见类型信息
			mindCodeBean = mindCodeMap.get(mindCode);
		}

		if (null == mindCodeBean) {
			mindCodeBean = new Bean();
		}

		mindCodeBean.set("MIND_MUST", wfAct.getNodeDef().getStr("MIND_NEED_FLAG"));

		this.getOutBean().set("mindCodeBean_flow", mindCodeBean);
	}

	/**
	 * 执行返回结果为bool值的js脚本
	 * 
	 * @param script
	 *            脚本
	 * @return 执行结果
	 */
	private boolean execScript(String script) {
		return WfUtils.execCondScript(script, getOutBean());
	}

}
