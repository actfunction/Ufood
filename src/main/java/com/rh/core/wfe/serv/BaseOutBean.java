package com.rh.core.wfe.serv;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;

/**
 * 非流经用户 且没有管理员权限，不能看文稿，意见，修改痕迹，相关文件?
 * 
 * @author wanglong
 * 
 */
public class BaseOutBean extends WfOut {
    /**
     * 审批单显示模式：最低权限，分发、接收用户可以查看。
     */
    private static final String MODE_BASE = "MODE_BASE";

	/**
	 * 
	 * @param wfProc
	 *            流程实例
	 * @param outBean
	 *            返回前台Bean
	 * @param paramBean 参数           
	 */
	public BaseOutBean(WfProcess wfProc, Bean outBean, ParamBean paramBean) {
		super(wfProc, outBean, paramBean);
	}

	@Override
	public void fillOutBean(WfAct wfAct) {
		this.getOutBean().set(DISPLAY_MODE, MODE_BASE);
	}
}
