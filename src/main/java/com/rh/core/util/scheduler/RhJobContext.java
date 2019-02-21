/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */

package com.rh.core.util.scheduler;

import com.rh.core.base.Bean;

/**
 * 任务上下文
 * @author wanglong
 * 
 */
public class RhJobContext extends Bean {

    /**
     * sid
     */
    private static final long serialVersionUID = -4626184189792522858L;
    /** 任务设置公司变量 key */
    public static final String CONTEXT_CMPYS_KEY = "CONTEXT_CMPYS";
    /** 任务执行情况说明 key */
    public static final String CONTEXT_JOB_EXECUTED_DESC_KEY = "CURRENT_JOB_DESC";
    
    /** job context user schema key in bean */
    public static final String JOB_CONTEXT_USER = "JOB_CONTEXT_USER";
    
    private static final String CONTEXT_JOBDETAIL = "CONTEXT_JOBDETAIL";
    
    
    /**
     * 设置任务bean
     * @param rhJobDetail - job bean
     */
    public void setRhJobDetail(RhJobDetail rhJobDetail) {
        set(CONTEXT_JOBDETAIL, rhJobDetail);
    }
    
    /**
     * 获取任务bean
     * @return - 任务bean
     */
    public RhJobDetail getRhJobDetail() {
        return (RhJobDetail) get(CONTEXT_JOBDETAIL, new RhJobDetail());
    }

}
