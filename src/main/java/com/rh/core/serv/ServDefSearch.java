/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.util.ServUtils;

/**
 * 服务全文检索定义类
 * 
 * @author wanglong
 */
public class ServDefSearch extends CommonServ {
    /** 服务主键 */
    public static final String SERV_ID_SEARCH = "SY_SERV_SEARCH";
    
    /**
     * byid时判断是否存在数据，如果不存在就新添加一条
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterByid(ParamBean paramBean, OutBean outBean) {       
        if (outBean.getId().length() == 0) { //如果没有找到数据，就新添加一条
            Bean dataBean = new Bean();
            dataBean.set("SERV_ID", paramBean.getId());
            ServDao.create(SERV_ID_SEARCH, dataBean);
            outBean.copyFrom(dataBean);
            outBean.setOk();
        }
    }
    
    /**
     * 修改服务定义后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk()) {
            if (!outBean.isEmpty("SERV_ID")) {
                ServUtils.clearServCache(outBean.getStr("SERV_ID"));
            }
        }
    }

    /**
     * 对指定服务进行检索
     * @param paramBean 参数
     * @return 检索结果
     */
    public OutBean index(ParamBean paramBean) {
        ServDefBean servDef = ServUtils.getServDef(paramBean.getId());
        Bean search = servDef.getSearchDef();
        int count = ServUtils.doIndex(search, 20);
        OutBean outBean = new OutBean();
        outBean.setOk(Context.getSyMsg("SY_BATCHSAVE_OK", String.valueOf(count)));
        return outBean;
    }
    
    /**
     * 清除指定服务的所有索引
     * @param paramBean 参数
     * @return 检索结果
     */
    public OutBean deleteIndex(ParamBean paramBean) {
        try {
            ServUtils.getIndexServ().deleteAll(paramBean.getId());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        OutBean outBean = new OutBean();
        outBean.setOk();
        return outBean;
    }    
}
