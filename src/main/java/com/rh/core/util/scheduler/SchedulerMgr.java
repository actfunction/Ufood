/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.util.scheduler;

import com.rh.core.base.BaseContext;
import com.rh.core.util.Lang;

/**
 * 
 * @author wanglong
 */
public class SchedulerMgr {

    /** 系统全局任务调度 */
    private static IScheduler globalScheduler = null;

    /** 本地临时计划任务,所谓临时即该类任务信息仅被存储在内存中 */
    private static IScheduler localTempScheduler = null;

    /**
     * job cmpys status schema key in bean 所属公司? 1:不指定,2:全公司,3:自定义 <br>
     * 任务所属公司配置,指定后可从任务上下文环境中读取.<br>
     * */
    public static final String JOB_CMPYS_STATUS = "JOB_CMPYS_STATUS";
    /** job cmpys schema key in bean */
    public static final String JOB_CMPYS = "JOB_CMPYS";

    /**
     * 获取任务调度对象(全局)
     * @return - 任务调度对象
     */
    public static IScheduler getGlobalScheduler() {
        synchronized (JOB_CMPYS_STATUS) {
            if (globalScheduler == null) {
                String className = BaseContext.getInitConfig("rh.scheduler.global",
                        "com.rh.core.util.scheduler.imp.GlobalQuartzScheduler");
                if (!className.equalsIgnoreCase("none")) {
                    globalScheduler = (IScheduler) Lang.createObject(IScheduler.class, className);
                }
            }
        }
        return globalScheduler;
    }

    /**
     * 获取任务调度对象(本地临时)
     * @return - 任务调度对象
     */
    public static IScheduler getLocalScheduler() {
        synchronized (JOB_CMPYS) {
            if (localTempScheduler == null) {
                localTempScheduler = (IScheduler) Lang.createObject(IScheduler.class,
                        BaseContext.getInitConfig("rh.scheduler.local",
                                "com.rh.core.util.scheduler.imp.LocalQuartzScheduler"));
            }
        }

        return localTempScheduler;
    }

}
