package com.rh.core.wfe.serv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfFilter;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.FileController;
import com.rh.core.wfe.util.WfBtnConstant;
import com.rh.core.wfe.util.WfeConstant;


/**
 * 返回前台数据
 * 
 */
public abstract class WfOut extends WfBaseOut {

    /**
     * 审批单查看模式
     */
    public static final String DISPLAY_MODE = "WF_DISPLAY_MODE";
    
    /** log对象 **/
    protected Log log = LogFactory.getLog(this.getClass());

    private FileController fileController = null;

    /** 权限Bean */
    private Bean authBean = new Bean();

    /**
     * 
     * @param aWfProc 流程实例
     * @param aOutBean 返回前台Bean
     * @param aParamBean 保存客户端提交参数的Bean
     */
    public WfOut(WfProcess aWfProc, Bean aOutBean, ParamBean aParamBean) {
        super(aWfProc, aOutBean, aParamBean);
        initAuthBean();
        initFieldCtrl();
        initMindCode();
        this.addFileControl();
        this.addPrintAuditBtn();
    }



    /**
     * 初始化权限Bean
     */
    private void initAuthBean() {
        this.getOutBean().set("authBean", authBean);
        authBean.set("userDoInWf", false);
//        authBean.set("canHandWrite", false);
//        authBean.set("mindMust", false);
        
        authBean.set("isDraftNode", false);
//        authBean.set("isShowFenfaList", SendUtils.isShowFenfaList(this.getWfProc()
//                .getProcInstBean().getStr("S_CMPY"), this.getWfProc().getServId(),
//                this.getWfProc().getDocId()));
//        authBean.set("isShowFenfaList", false);
        authBean.set("lockState", this.getWfProc().getProcInstBean().getInt("INST_LOCK")); // 是否锁定了\
        authBean.set("INST_LOCK_USER", this.getWfProc().getProcInstBean().getStr("INST_LOCK_USER")); // 锁定人
        authBean.set("INST_LOCK_TIME", this.getWfProc().getProcInstBean().getStr("INST_LOCK_TIME")); // 锁定时间
    }
    
    /**
     * 
     * @param servBean 服务定义Bean
     * @return 文件权限定义列表
     */
    protected FileController getServDefFileCtrl(ServDefBean servBean) {
        List<Bean> fileItemList = servBean.getFileSelfItems();

        List<Bean> fileDef = new ArrayList<Bean>();

        for (Bean bean : fileItemList) {
            String config = bean.getStr("ITEM_INPUT_CONFIG");
            Bean configBean = JsonUtils.toBean(config);
            configBean.set("ID", bean.getStr("ITEM_CODE"));
            configBean.set("NAME", bean.getStr("ITEM_NAME"));
            fileDef.add(configBean);
        }
        
        FileController fileCtrl = new FileController(fileDef);
        
        fileCtrl.reserveMinPermission(); 

        return fileCtrl;
    }

    /**
     * 添加文件控制
     */
    private void addFileControl() {
        String servId = this.getWfProc().getServId();

        ServDefBean servDefBean = ServUtils.getServDef(servId);

        fileController = getServDefFileCtrl(servDefBean);
        
        authBean.set("nodeFileControl", fileController.getFileControlBeanList());
        
        String userCode = getDoUser().getCode();
        
        String roleCodeStr = Context.getSyConf("SY_WFE_FILE_DOWNLOAD_ROLE", "");
        
        if (StringUtils.isEmpty(roleCodeStr)) {
            return;
        }
        
        //如果用户有“公文管理”权限则有 “下载”按钮
        if (UserMgr.existInRoles(userCode, roleCodeStr)) {
           fileController.setReadAndDownload(); //设置为“下载”
        }
        
    }

    /**
     * 表单字段控制
     * 
     */
    private void initFieldCtrl() {
        Bean fieldCtrlBean = new Bean();

        fieldCtrlBean.set("FIELD_CONTROL", false);
        fieldCtrlBean.set("FIELD_EXCEPTION", "");
        fieldCtrlBean.set("FIELD_UPDATE", "");
        fieldCtrlBean.set("FIELD_HIDDEN", "");
        fieldCtrlBean.set("FIELD_MUST", "");

        this.getOutBean().set("fieldControlBean", fieldCtrlBean);
    }

    /**
     * 添加意见编码Bean
     */
    private void initMindCode() {
        this.getOutBean().set("mindCodeBean", "");
    }

    /**
     * 向OutBean中添加符合格式的数据
     * 
     * @param wfAct 节点实例
     */
    public abstract void fillOutBean(WfAct wfAct);

    /**
     * 使用扩展类来过滤输出到客户端的数据
     * 
     * @param wfAct 节点实例对象
     */
    public void filter(WfAct wfAct) {
        addActProcButton(); // 加入流程公共按钮
        
        String expandClass = getWfProc().getProcDef().getStr("EXPAND_CLASS");

        if (!StringUtils.isEmpty(expandClass)) {
            WfFilter wfBtnFilter = null;
            try {
                wfBtnFilter = (WfFilter) Lang.loadClass(expandClass)
                        .newInstance();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            wfBtnFilter.doButtonFilter(wfAct, this);
        }
    }

    /**
     * 流程上定义的按钮，在任意节点都能显示的全局按钮，如流程跟踪，收藏夹
     */
    private void addActProcButton() {
        log.debug("流程上定义的按钮，在任意节点都能显示的");
        List<Bean> procButton = this.getWfProc().getProcDef().getProActs();
        this.addBtnBeanList(procButton);
    }

    /**
     * 处理流经过用户的处理按钮，如：收回、取消办结
     * 
     * @param wfAct 节点实例
     */
    protected void addFlowProcBtn(WfAct wfAct) {
        if (!this.getWfProc().isRunning() && this.getWfProc().canCancelFinish()) { // 已经办结的流程
            Bean undo = getServACTs(WfBtnConstant.BUTTON_UNDO_FINISH);
            this.addBtnBean(undo);
            log.debug("添加取消办结按钮");
        }

        // 流程运行状态，节点已经办结状态，且
        if (this.getWfProc().isRunning() && wfAct.canWithdraw()) {
            Bean withdrawButton = getServACTs(WfBtnConstant.BUTTON_WITHDRAW);
            withdrawButton.set("NI_ID", wfAct.getId());
            this.addBtnBean(withdrawButton);

            log.debug("添加 收回 按钮");
        }
    }

    /**
     * 
     * @param actCode act编码
     * @return ACT Bean
     */
    public Bean getServACTs(String actCode) {
        ServDefBean servDef = ServUtils.getServDef(ServMgr.SY_WFE_PROC_DEF_ACT);
        return servDef.getAct(actCode);
    }
    
    /**
     * 
     * @return 权限Bean
     */
    public Bean getAuthBean() {
        return authBean;
    }

    /**
     * @return 文件权限控制类
     */
    public FileController getFileController() {
        return fileController;
    }
    
    /**
     * 增加删除按钮
     */
    public void addDeleteBtn() {
        if (isGteSUserODeptLevel()) {
            ServDefBean servDef = ServUtils.getServDef(this.getWfProc().getServId());
            Bean deleteBean = servDef.getAct(WfBtnConstant.BUTTON_DELETE);

            this.addBtnBean(deleteBean);
        }
    }
    
    /**
     * 增加“打印审批单”按钮
     */
    private void addPrintAuditBtn() {
    	
    	//如果没有流程，不添加打印审批单按钮。直接跳出，不添加按钮。解决老数据中没有流程定义，却显示“打印审批单”按钮的bug
		if (null == this.getWfProc().getProcDef()
				|| this.getWfProc().getProcDef().isEmpty()) {
			return;
		}
        String roleCodeStr = Context.getSyConf("SY_WFE_PRINT_AUDIT_ROLE", "");
        if (StringUtils.isEmpty(roleCodeStr)) {
            return;
        }
        String userCode = getDoUser().getCode();
        if (UserMgr.existInRoles(userCode, roleCodeStr)) {
            // “打印审批单”按钮
            Bean printAudit = this.getServACTs(WfBtnConstant.BUTTON_PRINTAUDIT);
            this.addBtnBean(printAudit);
        }
    }
    /**
     * 
     * @return 当前用户的机构级别是否大于等于拟稿用户的机构级别，即
     */
    public boolean isGteSUserODeptLevel() {
        try {
            UserBean sUserBean = UserMgr.getUser(this.getWfProc().getSUserId()); // 起草
            if (this.getDoUser().getODeptLevel() <= sUserBean.getODeptLevel()) {
                return true;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return false;
    }
    
    
    /**
	 * 添加终止某一条并发的变量
	 */
	public void addTerminateOneBranch(WfAct wfAct) {
		String stopNodes = wfAct.canStopOneParallel();
		if (stopNodes != null && stopNodes.length() > 0) {
			this.getOutBean().set(WfeConstant.TERMINATE_ONE_BRANCH, stopNodes);
		}
	}
}
