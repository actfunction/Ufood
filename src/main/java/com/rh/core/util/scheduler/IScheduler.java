/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */
package com.rh.core.util.scheduler;

import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;

/**
 * @author wanglong
 * 
 */
public abstract class IScheduler {

    /**
     * 触发器状态
     */
    public enum JobStatus {

        /**
         * 运行中
         */
        RUNNING,

        /**
         * 活动中
         */
        ALIVE,

        /**
         * 停止
         */
        STOP,

        /**
         * 已暂停
         */
        PAUSED
    }

    /**
     * 启动
     */
    public abstract void start();

    /**
     * 关闭
     */
    public abstract void shutdown();

    /**
     * 获取任务列表
     * @param param - 参数bean
     * @return - out bean
     */
    public abstract OutBean queryJob(ParamBean param);

    /**
     * byid
     * @param param - query bean
     * @return outbean
     */
    public abstract OutBean getJob(ParamBean param);

    /**
     * byid
     * @param param - query bean
     * @return outbean
     */
    public abstract OutBean getTrigger(ParamBean param);

    /**
     * 获取触发器列表
     * @param param - 参数bean
     * @return - out bean
     */
    public OutBean queryTrigger(ParamBean param) {
        param.setServId(ServMgr.SY_COMM_SCHED_TRIGGER);
        return new CommonServ().query(param);
    }

    /**
     * 获取当前任务调度实例名称
     * @return - str
     */
    public abstract String getSchedulerName();

    /**
     * 添加任务
     * @param rhJobDetail - 任务
     */
    public abstract void addJob(RhJobDetail rhJobDetail);

    /**
     * 更新任务
     * @param rhJobDetail - 任务
     */
    public abstract void updateJob(RhJobDetail rhJobDetail);

    /**
     * 删除多个任务
     * @param jobs - 多个任务
     */
    public abstract void removeJobs(String[] jobs);

    /**
     * 删除任务(单个)
     * @param job - 任务
     */
    public abstract void removeJob(String job);

    /**
     * 暂停任务
     * @param key - 主键
     */
    public abstract void pauseJobs(String[] key);

    /**
     * 恢复任务
     * @param key - 主键
     */
    public abstract void resumeJobs(String[] key);

    /**
     * 停止正在运行的任务
     * @param jobKey - 主键
     */
    public abstract void interruptJobs(String[] jobKey);

    /**
     * 触发器状态
     */
    public enum TriggerStatus {

        /**
         * 未知
         */
        UNKNOW,

        /**
         * 暂停
         */
        PAUSED,

        /**
         * 活动
         */
        ALIVE,

        /**
         * 停止
         */
        STOP,
    }

    /**
     * 查询指定job所关联的trigger
     * @param param - 参数bean
     * @param result - 结果bean
     */
    public abstract void queryTriggersOfJob(ParamBean param, OutBean result);

    /**
     * 添加触发器
     * @param triggerBean - 任务触发器
     */
    public abstract void addTrigger(RhTrigger triggerBean);

    /**
     * 删除触发器
     * @param trigger - 主键
     * 
     */
    public abstract void removeTrigger(String trigger);

    /**
     * 删除触发器
     * @param trigger - 主键
     */
    public abstract void removeTriggers(String[] trigger);

    /**
     * 更新触发器
     * @param triggerBean - 触发器bean
     */
    public abstract void updateTrigger(RhTrigger triggerBean);

    /**
     * 暂停触发器
     * @param triggers - 触发器
     */
    public abstract void pauseTriggers(String[] triggers);

    /**
     * 恢复触发器
     * @param triggers - 触发器
     */
    public abstract void resumeTriggers(String[] triggers);

}
