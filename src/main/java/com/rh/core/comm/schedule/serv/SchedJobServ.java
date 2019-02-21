/*
 * Copyright (c) 2012 Ruaho All rights reserved.
 */
package com.rh.core.comm.schedule.serv;

import com.rh.core.base.Context;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.scheduler.IScheduler;
import com.rh.core.util.scheduler.RhJobDetail;
import com.rh.core.util.scheduler.SchedulerMgr;

/**
 * 计划调度服务类
 * @author wanglong
 */
public class SchedJobServ extends SchedBaseServ {

    /** job service id */
    private static final String CURRENT_SERVICE = "SY_COMM_SCHED";

    private static IScheduler scheduler = SchedulerMgr.getGlobalScheduler();

    /**
     * @see com.rh.core.serv.CommonServ#query(com.rh.core.base.Bean)
     * @param param bean
     * @return result bean
     */
    public OutBean query(ParamBean param) {
        // 通知scheduler实现类
        return scheduler.queryJob(param);
    }

    /**
     * 添加一个任务
     * @param param 参数bean，包含job信息
     * @return 添加结果
     */
    public OutBean save(ParamBean param) {
        param.set("serv", CURRENT_SERVICE);

        OutBean outBean;
        param.set("SCHED_NAME", scheduler.getSchedulerName());
        outBean = super.save(param);

        if (!param.getAddFlag()) { // edit & save
            // 通知scheduler实现类
            scheduler.updateJob(new RhJobDetail(outBean));
        } else { // add
            // 通知scheduler实现类
            scheduler.addJob(new RhJobDetail(outBean));
        }

        return outBean;
    }

    /**
     * 删除指定job
     * @param param 参数Bean， 包含JobName
     * @return 删除结果
     */
    public OutBean delete(ParamBean param) {
        String[] ids = getIds(param);
        OutBean outBean = super.delete(param);
        // 通知scheduler实现类
        scheduler.removeJobs(ids);

        return outBean;

    }

    /**
     * 查询指定job
     * @param param 参数Bean， 包含JobName
     * @return 查询结果
     */
    public OutBean byid(ParamBean param) {
        // 如果为新建
        if (0 == param.getId().length()) {
            return  super.byid(param);
        }
        return scheduler.getJob(param);
    }

    /**
     * 暂停job
     * @param param 参数Bean，包含jobName
     * @return 处理结果
     */
    public OutBean pauseJob(ParamBean param) {
        String[] ids = getIds(param);
        scheduler.pauseJobs(ids);
        
        OutBean outBean = new OutBean(param);
        outBean.setOk(Context.getSyMsg(SY_PAUSE_OPERATION_SUCCESSFUL));
        return outBean;
    }

    /**
     * 恢复job
     * @param param 参数Bean， 包含job name
     * @return 处理结果
     */
    public OutBean resumeJob(ParamBean param) {
        String[] ids = getIds(param);
        //通知scheduler实现类
        scheduler.resumeJobs(ids);
     
        OutBean out = new OutBean();
        out.setOk(Context.getSyMsg(SY_START_OPERATION_SUCCESSFUL));
        return out;
    }

    /**
     * 终止任务
     * @param param 参数Bean，包含jobName
     * @return 处理结果
     */
    public OutBean interruptJobs(ParamBean param) {
        String[] ids = getIds(param);
        //通知scheduler实现类
        scheduler.interruptJobs(ids);
     
        OutBean outBean = new OutBean(param);
        outBean.setOk(Context.getSyMsg(SY_PAUSE_OPERATION_SUCCESSFUL));
        return outBean;
    }

    /**
     * 任务初始化
     */
    public void init() {
        scheduler.start();
    }
  

}
