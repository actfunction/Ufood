package com.rh.core.wfe.serv;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 
 * @author wanglong
 * 
 */
public abstract class WfBaseOut {
    private static final String CONF_RENAME_CODE = "SY_WF_BTN_RENAME_CODE";
    private static final String CONF_PREFIX_RENAME_CODE = "SY_WF_BTN_RENAME_";
    private static final String CONF_OTHER_GROUP = "SY_WF_BTN_GROUP_OTHER";
    
    /** 可以重命名的ActCode **/
    private Set<String> canRenameBtnSet = null;
    /** 其它组按钮的ActCode集合 **/
    private Set<String> otherGroupBtnSet = null;
    
    /** 按钮列表 */
    private List<Bean> btnBeanList = new ArrayList<Bean>();

    /**
     * 当前办理用户
     */
    private UserBean doUser = null;
    
    /**
     * 保存客户端提交参数的Bean
     */
    private ParamBean paramBean = null;
    /**
     * 
     */
    private Bean outBean = null;
    /**
     * 
     */
    private WfProcess wfProc = null; 
    
    /**
     * @param aWfProc 流程实例
     * @param outBean 信息输出Bean
     * @param paramBean 参数Bean
     */
    protected WfBaseOut(WfProcess aWfProc, Bean outBean, ParamBean paramBean) {
        if (!outBean.contains("buttonBean")) {
            outBean.set("buttonBean", btnBeanList);
        } else {
            btnBeanList = outBean.getList("buttonBean");
        }
        this.paramBean = paramBean;
        this.outBean = outBean;
        this.wfProc = aWfProc;
    }
    
    /**
     * @param bean 按钮Bean
     */
    public void addBtnBean(Bean bean) {
        if (bean != null && !existBtnBean(bean)) {            
            Bean newBean = bean.copyOf();
            
            //按钮是否属于其它组
            if (isOtherGroupBtn(newBean.getStr("ACT_CODE"))) {
                newBean.set(WfeConstant.PROC_BTN_GROUP, WfeConstant.PROC_BTN_GROUP_OTHER);
                newBean.set(WfeConstant.PROC_BTN_GROUP_NAME, WfeConstant.PROC_BTN_GROUP_OTHER_NAME);
            }
            
            //按钮是否需要改名
            if (canRenameBtn(newBean.getStr("ACT_CODE"))) {
                String key = CONF_PREFIX_RENAME_CODE + newBean.getStr("ACT_CODE");
                String value = Context.getSyConf(key, "");
                if (StringUtils.isNotEmpty(value)) {
                    newBean.set("ACT_NAME", value);
                }
            }
            
            // 使用节点重命名覆盖原有按钮名
            if (newBean.isNotEmpty("ACT_RENAME")) {
                newBean.set("ACT_NAME", newBean.getStr("ACT_RENAME"));
            }
            
            btnBeanList.add(newBean);
        }
    }
    
    /**
     * @param bean 按钮bean
     * @return 指定按钮是否存在与列表中
     */
    public boolean existBtnBean(Bean bean) {
        for (Bean btnBean : btnBeanList) {
            String actCode = btnBean.getStr("ACT_CODE");
            if (actCode.equalsIgnoreCase(bean.getStr("ACT_CODE"))) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * @param actCode ACT编码
     * @return 是否已经存在该按钮
     */
    public boolean existBtnBean(String actCode) {
        for (Bean act : this.btnBeanList) {
            if (act != null && act.getStr("ACT_CODE").equals(actCode)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * @param actCode ACT编码
     * @return 等于指定ActCode的ActBean
     */
    public Bean getBtnBean(String actCode) {
        for (Bean act : this.btnBeanList) {
            if (null != act && act.getStr("ACT_CODE").equals(actCode)) {
                return act;
            }
        }
        return null;
    }
    
    /**
     * 移出按钮
     * @param btnBean 按钮对象
     */
    public void removeBtnBean(Bean btnBean) {
        Iterator<Bean> itr = btnBeanList.iterator();
        while (itr.hasNext()) {
            Bean bBean = itr.next();
            if (bBean.getStr("ACT_CODE").equalsIgnoreCase(btnBean.getStr("ACT_CODE"))) {
                itr.remove();
            }
        }
    }
    
    /**
     * 移出按钮
     * @param actCode 按钮Code
     */
    public void removeBtnBean(String actCode) {
        Iterator<Bean> itr = btnBeanList.iterator();
        while (itr.hasNext()) {
            Bean bBean = itr.next();
            if (bBean.getStr("ACT_CODE").equalsIgnoreCase(actCode)) {
                itr.remove();
            }
        }
    }
    
    /**
     * 增加按钮组
     * @param beanList 按钮组
     */
    public void addBtnBeanList(List<Bean> beanList) {
        for (Bean bean : beanList) {
            //如果是删除的话，需要判断权限先
            this.addBtnBean(bean);
        }
    }
    
    /**
     * @return 办理用户
     */
    public UserBean getDoUser() {
        if (doUser == null) {
            return Context.getUserBean();
        }
        return doUser;
    }

    /**
     * @param aDoUser 办理用户
     */
    public void setDoUser(UserBean aDoUser) {
        this.doUser = aDoUser;
    }
    
    /**
     * @return 流程实例
     */
    public WfProcess getWfProc() {
        return wfProc;
    }

    /**
     * @return 返回数据Bean
     */
    public Bean getOutBean() {
        return outBean;
    }

    /**
     * @return the paramBean
     */
    public ParamBean getParamBean() {
        return paramBean;
    }
    
    /**
     * @param actCode 按钮的ActCode
     * @return 是否可以重命名
     */
    private boolean canRenameBtn(String actCode) {

        if (canRenameBtnSet == null) {
            String configStr = Context.getSyConf(CONF_RENAME_CODE,
                    "");
            if (configStr.length() > 0) {
                canRenameBtnSet = split2Set(configStr);
            } else {
                canRenameBtnSet = new HashSet<String>();
            }
        }

        return canRenameBtnSet.contains(actCode);
    }
    
    /**
     * 
     * @param actCodes 逗号分隔的字符串
     * @return Set对象
     */
    private Set<String> split2Set(String actCodes) {
        HashSet<String> set = new HashSet<String>();
        String[] configArr = actCodes.split(",");

        for (String actCode : configArr) {
            if (actCode.length() > 0) {
                set.add(actCode);
            }
        }

        return set;
    }
    
    /**
     * 维护 其他按钮组的按钮定义
     * @param actCode 按钮的ActCode
     * @return 其他按钮组的定义
     */
    private boolean isOtherGroupBtn(String actCode) {
        if (otherGroupBtnSet == null) {
            String configStr = Context.getSyConf(CONF_OTHER_GROUP, "");
            if (configStr.length() > 0) {
                otherGroupBtnSet = split2Set(configStr);
            } else {
                otherGroupBtnSet = new HashSet<String>();
            }
        }
        return otherGroupBtnSet.contains(actCode);
    }
}
