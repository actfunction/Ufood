package com.rh.core.base.start;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.scheduler.RhJobDetail;
import com.rh.core.util.scheduler.RhTrigger;
import com.rh.core.util.scheduler.SchedulerMgr;

public class LocalSchedulLoader {
    /**
     * 任务计划管理器启动 启动失败抛出异常
     */
    public void start() {
        SchedulerMgr.getLocalScheduler().start();
        
        /**
         * {
         *     code:"", // 可以不填，不填就用classname
         *     class:"",
         *     desc:"",
         *     cron:""
         * }
         */
        
        String strJobs = Context.app("localSchedulJobs","");
        if(StringUtils.isEmpty(strJobs)) {
            return;
        }
        
        // 添加本地任务
        List<Bean> jobs = JsonUtils.toBeanList(strJobs);
        for(Bean bean:jobs) {
            try {
                RhJobDetail job = new RhJobDetail();
                job.setJobClass(bean.getStr("class"));
                if(bean.isEmpty("code")) {
                    job.setJobCode(bean.getStr("class"));
                } else {
                    job.setJobCode(bean.getStr("code"));
                }
                
                RhTrigger trigger = new RhTrigger();
                trigger.setCode(job.getJobCode());
                trigger.setCronExpression(bean.getStr("cron"));
                trigger.setJobCode(job.getJobCode());
                trigger.setDescription(bean.getStr("cron"));
    
                SchedulerMgr.getLocalScheduler().addJob(job);
                SchedulerMgr.getLocalScheduler().addTrigger(trigger);
            } catch (Exception e) {
                e.printStackTrace();
            }
        
        }

    }

    /**
     * 任务计划管理器停止 停止失败抛出异常
     */
    public void stop() {
        SchedulerMgr.getLocalScheduler().shutdown();
    }
}
