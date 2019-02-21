package com.rh.core.wfe.serv;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.FileController;
import com.rh.core.wfe.util.WfBtnConstant;

/**
 * 流程管理员返回数据
 * 
 */
public class AdminOutBean extends WfOut {
    private static final String MODE_ADMIN = "MODE_ADMIN";

    /**
     * 
     * @param wfProc 流程实例
     * @param outBean 返回数据
     * @param paramBean 保持客户端提交数据的Bean
     */
    public AdminOutBean(WfProcess wfProc, Bean outBean, ParamBean paramBean) {
        super(wfProc, outBean, paramBean);
    }
    
    /**
     * 是否能显示管理员能看到的指定按钮
     * @param conf 系统配置管理员能看到的按钮
     * @param btnActName 按钮的ActName
     * @return 可以显示返回true；否则返回false；
     */
    private boolean canDisplayBtn(String conf, String btnActName) {
        if (StringUtils.isEmpty(conf)) {
            return true;
        }

        if (conf.indexOf(btnActName) >= 0) {
            return true;
        }

        return false;
    }

    @Override
    public void fillOutBean(WfAct wfAct) {
        /** 从系统配置中读取管理员能够显示的按钮 */
        String adminBtns = Context.getSyConf("SY_WF_ADMIN_BTN", "");

        if (canDisplayBtn(adminBtns, WfBtnConstant.BUTTON_FENFA)) {
            this.addFenFaBtn();
        }

        if (canDisplayBtn(adminBtns, WfBtnConstant.BUTTON_PRINT)) {
            this.addPrintBtn();
        }

        if (canDisplayBtn(adminBtns, WfBtnConstant.BUTTON_RELATE)) {
            this.addRelateBtn();
        }

        if (canDisplayBtn(adminBtns, WfBtnConstant.BUTTON_FINISH)) {
            this.addFinishBtn();
        }

        if (canDisplayBtn(adminBtns, WfBtnConstant.BUTTON_UNDO_FINISH)) {
            this.addUndoFinishBtn();
        }

        if (canDisplayBtn(adminBtns, WfBtnConstant.BUTTON_DELETE)) {
            addDeleteBtn();
        }

        if (canDisplayBtn(adminBtns, WfBtnConstant.BUTTON_SAVE)) {
            addSaveActBtn();
        }

        if (canDisplayBtn(adminBtns, WfBtnConstant.BUTTON_TO_GWMANAGE)) {
            addGwManagerBtn();
        }

        // 表单上的按钮，不在这加？？
        addFileController();

        // 输入项的控制 在服务上定义 管理员 能改哪些字段 PROC_MANAGE_EDIT
        addFieldCtrl();

        this.getOutBean().set(DISPLAY_MODE, MODE_ADMIN);
    }

    /**
     * 表单字段控制
     * 
     */
    private void addFieldCtrl() {
        Bean fieldCtrlBean = new Bean();

        String manageEditStr = this.getWfProc().getProcDef().getStr("PROC_MANAGE_EDIT");
        fieldCtrlBean.set("FIELD_EXCEPTION", manageEditStr);
        fieldCtrlBean.set("FIELD_UPDATE", "");
        fieldCtrlBean.set("FIELD_HIDDEN", "");
        fieldCtrlBean.set("FIELD_MUST", "");

        this.getOutBean().set("fieldControlBean", fieldCtrlBean);
    }

    /**
     * 添加文件类型控制
     */
    private void addFileController() {
        if (!super.getWfProc().isRunning()) {
            return;
        }

        String servId = super.getWfProc().getServId();

        ServDefBean servDefBean = ServUtils.getServDef(servId);

        FileController fileController = getServDefFileCtrl(servDefBean);
        
        if (this.getWfProc().isRunning() && !this.getWfProc().isLocked()) {
            //审批单未锁定情况下，可以查看、删除、修改文件
            for (Bean fileCtlBean : fileController.getFileControlBeanList()) {
                fileCtlBean.set("VALUE", 7);
            }
        }

        this.getAuthBean().set("nodeFileControl", fileController.getFileControlBeanList());
    }

    /**
     * 添加分发按钮
     */
    private void addFenFaBtn() {
        Bean fenFaBtn = this.getServACTs(WfBtnConstant.BUTTON_FENFA);
        this.addBtnBean(fenFaBtn);
    }

    /**
     * 添加打印按钮
     */
    private void addPrintBtn() {
        Bean printBtn = this.getServACTs(WfBtnConstant.BUTTON_PRINT);
        this.addBtnBean(printBtn);
    }

    /**
     * 添加相关文件按钮
     */
    private void addRelateBtn() {
        Bean relateBtn = this.getServACTs(WfBtnConstant.BUTTON_RELATE);
        this.addBtnBean(relateBtn);
    }

    /**
     * 添加办结按钮
     */
    private void addFinishBtn() {
        if (this.getWfProc().isRunning()) { // 还未办结的
            Bean finishBtn = this.getServACTs(WfBtnConstant.BUTTON_FINISH);
            this.addBtnBean(finishBtn);
        }
    }

    /**
     * 添加取消办结按钮
     */
    private void addUndoFinishBtn() {
        if (!this.existBtnBean(WfBtnConstant.BUTTON_UNDO_FINISH)) { // 取消办结
            if (!this.getWfProc().isRunning()) { // 已经办结的
                Bean undoFinishBtn = this
                        .getServACTs(WfBtnConstant.BUTTON_UNDO_FINISH);
                this.addBtnBean(undoFinishBtn);
            }
        }
    }

    /**
     * 添加保存的按钮
     */
    private void addSaveActBtn() {
        ServDefBean servDef = ServUtils.getServDef(this.getWfProc().getServId());
        Bean saveBean = servDef.getAct(WfBtnConstant.BUTTON_SAVE);
        if (null != saveBean) {
            this.addBtnBean(saveBean);
        }
    }

    /**
     * 添加公文管理按钮
     */
    private void addGwManagerBtn() {
        Bean gwManagerBtn = this.getServACTs(WfBtnConstant.BUTTON_TO_GWMANAGE);
        this.addBtnBean(gwManagerBtn);
    }

}
