package com.rh.core.wfe.serv;

import java.util.HashMap;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.mind.MindServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.send.SendUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.WfBtnConstant;

/**
 * 用于处理审批单分发给指定用户，用户打开带有分发ID的审批单时的场景。
 * @author wanglong 
 *
 */
public class SendOutBean extends WfOut {
    /**
     * 审批单显示模式：最低权限，分发、接收用户可以查看。
     */
    private static final String MODE_BASE = "MODE_SEND";

    /**
     * 
     * @param wfProc
     *            流程实例
     * @param outBean
     *            返回前台Bean
     * @param paramBean 参数           
     */
    public SendOutBean(WfProcess wfProc, Bean outBean, ParamBean paramBean) {
        super(wfProc, outBean, paramBean);
    }

    @Override
    public void fillOutBean(WfAct wfAct) {
        this.addFenfaProcBtn();
        
        this.getOutBean().set(DISPLAY_MODE, MODE_BASE);
    }
    
    /**
     * 增加分发处理按钮，如：签收、退回、转发、签收并转收文登记
     */
    private void addFenfaProcBtn() {
        if (this.getParamBean().isEmpty("SEND_ID")) {
            return;
        }

        String sendId = this.getParamBean().getStr("SEND_ID");
        
        // 根据分发明细ID取得对应的分发明细记录
        Bean sendDtlBean = ServDao.find(ServMgr.SY_COMM_SEND_DETAIL, sendId);
        
        //不是指定分发明细的签收人，则返回
        if (!SendUtils.isReciver(sendDtlBean, getDoUser()
                .getCode())) {
            return;
        }
        
        // 已经签收，则增加转发按钮
        if (SendUtils.isRecved(sendDtlBean)) {
            Bean zhuanFa = getServACTs(WfBtnConstant.BUTTON_ZHUANFA);
            String showtrans = Context.getSyConf("SY_WF_FENFA_TRANS", "TRUE");
            if (null != zhuanFa && showtrans.equalsIgnoreCase("true")) {
                zhuanFa.set("SEND_ID", sendId);
                this.addBtnBean(zhuanFa);
            }
            return;
        }

        // 如果是已签收、已退回、已收回则返回
        if (!SendUtils.isNotFinished(sendDtlBean)) {
            return;
        }
        
        //增加签收按钮
        Bean qianShou = getServACTs(WfBtnConstant.BUTTON_QIANSHOU);
        if (null != qianShou) {
            qianShou.set("SEND_ID", sendId);
            this.addBtnBean(qianShou);
        }
        
        //增加退回按钮
		// Bean tuiHui = getServACTs(WfBtnConstant.BUTTON_TUIHUI);
		// if (null != tuiHui) {
		// tuiHui.set("SEND_ID", sendId);
		// this.addBtnBean(tuiHui);
		// }
        
        //增加分发意见
        this.addFenfaMind(sendDtlBean);
        
    }
    
    /**
     * 增加分发意见
     * @param sendDtlBean 分发Bean
     */
    private void addFenfaMind(Bean sendDtlBean) {
        if (sendDtlBean.isEmpty("MIND_CODE")) {
            return;
        }
        
        String mindCode = sendDtlBean.getStr("MIND_CODE");
        HashMap<String, Bean> mindCodeMap = new MindServ().getMindCodeBeanMap(mindCode);
        Bean mindCodeBean = mindCodeMap.get(mindCode);
        
        if (mindCodeBean != null) {
            mindCodeBean.set("MIND_MUST", "1");
            this.getOutBean().set("mindCodeBean", mindCodeBean);
        }
        

    }
}
