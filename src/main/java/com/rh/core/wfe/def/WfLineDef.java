package com.rh.core.wfe.def;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;

/**
 * 流程线定义Bean
 * @author wanglong
 * 
 */
public class WfLineDef extends Bean {

    private static final long serialVersionUID = -4283031471203898429L;

    /**
     * 
     * @param lineDefBean 线定义Bean
     */
    public WfLineDef(Bean lineDefBean) {
        this.copyFrom(lineDefBean);

        String express = this.getStr("UPDATE_EXPRESS");
        List<Bean> exprList = JsonUtils.toBeanList(express);
        this.set("UPDATE_EXPRESS_LIST", exprList);
        if (this.isNotEmpty("ORG_DEF")) {
            Bean orgDefBean = JsonUtils.toBean(this.getStr("ORG_DEF"));
            this.set("ORG_DEF", orgDefBean);
            if (orgDefBean != null && orgDefBean.getBoolean("ENABLE_ORG_DEF")) {
                this.set("ENABLE_ORG_DEF", Constant.YES_INT);
            }
        }
    }

    /**
     * 
     * @return 是否启用组织资源定义
     */
    public boolean isEnableOrgDef() {
        return this.getBoolean("ENABLE_ORG_DEF");
    }

    /**
     * 
     * @return 组织资源定义Bean
     */
    public Bean getOrgDefBean() {
        return this.getBean("ORG_DEF");
    }
}
