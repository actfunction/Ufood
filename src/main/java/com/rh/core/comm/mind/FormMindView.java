package com.rh.core.comm.mind;

import java.io.File;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.freemarker.FreeMarkerUtils;

/**
 * 输出意见列表的类
 * 
 * @author wanglong
 */
public class FormMindView {
    private static final String CONF_RENAME_CODE = "CM_MIND_DEL_SELF";
    
    private boolean userCanDelSelfMind = false;
    
    private UserMind userMind = null;
    
    private String sortType = "";
    
    /** 是否移动设备访问  **/
    private boolean isMobile = false;
    
    /**
     * @param userBean 用户对象
     * @param servId 服务ID
     * @param dataId 数据ID
     */
    public FormMindView(UserBean userBean , String servId , String dataId) {
        userMind = new UserMind(userBean);
        userMind.query(servId, dataId);
    }
    
    /**
     * @param userBean 用户对象
     * @param servId 服务ID
     * @param dataId 数据ID
     * @param sortType 排序类型
     */
    public FormMindView(UserBean userBean , String servId , String dataId, String sortType) {
        userMind = new UserMind(userBean);
        userMind.query(servId, dataId, sortType);
        this.sortType = sortType;
    }
    
    /**
     * @param odeptCode 机构编码
     * @param canCopy 是否能复制意见
     * @param userDoInWf 当前人是否正在办理
     * @param paramBean 参数Bean
     * @return 输出意见列表
     */
    public String output(String odeptCode, boolean canCopy, boolean userDoInWf, ParamBean paramBean) {
        Bean bean = new Bean();
        bean.set("userMind", userMind);
        bean.set("mindTypeList", userMind.getMindTypeList());
        bean.set("sortType", sortType);
        bean.set("odeptList", userMind.getOdeptList());
        bean.set("odeptCode", odeptCode); //指定的机构
        bean.copyFrom(paramBean); 
        
        bean.set("canCopy", canCopy); //设置是否显示复制的按钮
        bean.set("userDoInWf", userDoInWf);
        
        bean.set("userBean", this.userMind.getViewUser());
        bean.set("DEL_SELF_MIND", isUserCanDelSelfMind());
        String fileStr = Context.appStr(Context.APP.SYSPATH) + File.separator
                + "sy" + File.separator + "comm" + File.separator + "mind"
                + File.separator;
        if (this.isMobile()) {
//            fileStr += "mindList-mb.ftl";
        } else {
            fileStr += "mindList.ftl";
        }
        String str = FreeMarkerUtils.parseText(fileStr, bean);
        
        return str;
    }
    
    /**
     * 意见列表的表头
     * @param odeptCode 机构编码
     * @param paramBean 参数Bean
     * @return 意见列表的表头
     */
    public String mindListTitle(String odeptCode, ParamBean paramBean) {
        Bean bean = new Bean();
        bean.set("userMind", userMind);
        bean.set("mindTypeList", userMind.getMindTypeList());
        bean.set("sortType", sortType);        
        bean.set("odeptList", userMind.getOdeptList());
        bean.set("odeptCode", odeptCode);
        bean.copyFrom(paramBean);
        boolean existCurOdeptMind = false;
        if (userMind.getMindList(odeptCode).size() > 0) {
            existCurOdeptMind = true;
        }
        bean.set("canCopy", paramBean.getBoolean("CAN_COPY")); //设置是否显示复制的按钮
        bean.set("userDoInWf", paramBean.getBoolean("userDoInWf")); //设置是否当前正在办理
        bean.set("existCurOdeptMind", existCurOdeptMind); //本机构的意见条数，决定是否显示本机构的表头
        bean.set("userBean", this.userMind.getViewUser());
        bean.set("DEL_SELF_MIND", isUserCanDelSelfMind());
        
        String fileStr = Context.appStr(Context.APP.SYSPATH) + File.separator
                + "sy" + File.separator + "comm" + File.separator + "mind"
                + File.separator;
        if (this.isMobile) {
//            fileStr += "mindListTitle-mb.ftl";
        } else {
            fileStr += "mindListTitle.ftl";
        }
        String str = FreeMarkerUtils.parseText(fileStr, bean);
        
        return str;
    }
    
    /**
     * @return 取得意见列表的数量
     */
    public int getMindTypeListSize() {
        return userMind.getMindTypeList().size();
    }
    
    /**
     * @return 取得 签过意见的 机构的 列表
     */
    public int getMindOdeptListSize() {
        return userMind.getOdeptList().size();
    }
    
    /**
     * 
     * @return 获取机构的列表
     */
    public List<Bean> getOdeptList() {
        return userMind.getOdeptList();
    }
    
    
    /**
     * 
     * @return 用户是否能删除自己所填写的所有意见，即使不是当前节点填写的
     */
    private boolean isUserCanDelSelfMind() {
        userCanDelSelfMind = Context.getSyConf(CONF_RENAME_CODE, false);
        
        return userCanDelSelfMind;
    }

    /**
     * 
     * @return 是否移动设备访问
     */
    public boolean isMobile() {
        return isMobile;
    }
    
    /**
     * 
     * @param isMobile 是否移动设备访问
     */
    public void setMobile(boolean isMobile) {
        this.isMobile = isMobile;
    }
}
