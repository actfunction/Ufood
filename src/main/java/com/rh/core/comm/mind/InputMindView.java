package com.rh.core.comm.mind;

import java.io.File;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.freemarker.FreeMarkerUtils;

/**
 * 意见输入框
 * @author wanglong
 * mindType.REGULAR
 * mindType.TERMINAL
 * mindType.GENERAL
 *
 */
public class InputMindView {

    
    private Bean mindtypes = new Bean();
    
    private Bean regularMind = new Bean();
    
    private Bean terminalMind = new Bean();
    
    private Bean generalMind = new Bean();
    
    /** 是否移动设备访问  **/
    private boolean isMobile = false;
    
    
    /**
     * @param mindtypes 意见类型
     */
    public InputMindView(Bean mindtypes) {
        this.mindtypes = mindtypes;
    }
    
    /**
     * 
     * @return 可输入意见编码
     */
    private Bean getMindCodes() {
        Bean bean = new Bean();
        bean.set("canRegularMind", false);
        bean.set("canTerminalMind", false);
        bean.set("canGeneralMind", false);
        
        this.regularMind = JsonUtils.toBean(mindtypes.getStr(MindServ.MIND_TYPE_REGULAR));
        this.terminalMind = JsonUtils.toBean(mindtypes.getStr(MindServ.MIND_TYPE_TERMINAL));
        this.generalMind = JsonUtils.toBean(mindtypes.getStr(MindServ.MIND_TYPE_GENERAL));
        
        if (regularMind.isNotEmpty("CODE_ID")) {
            bean.set("canRegularMind", true);
        }
        
        if (terminalMind.isNotEmpty("CODE_ID")) {
            bean.set("canTerminalMind", true);
        }
        
        if (generalMind.isNotEmpty("CODE_ID")) {
            bean.set("canGeneralMind", true);
        }
        
        return bean;
    }
    
    /**
     * @return 输出意见列表
     */
    public String output() {
        Bean bean = getMindCodes(); 
        
        bean.set("regularMind", this.regularMind);
        bean.set("terminalMind", this.terminalMind);
        bean.set("generalMind", this.generalMind);
        
        String fileStr = Context.appStr(Context.APP.SYSPATH) + File.separator
                + "sy" + File.separator + "comm" + File.separator + "mind"
                + File.separator;
        
        if (this.isMobile()) {
            fileStr += "mindInput-mb.ftl";
        } else {
            fileStr += "mindInput.ftl";
        }
        
        String str = FreeMarkerUtils.parseText(fileStr, bean);
        
        return str;
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
