/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */
package com.rh.core.util.scheduler;

import com.rh.core.base.Bean;

/**
 * @author wanglong
 * 
 */
public class RhJobDetail extends Bean {

    /**
     * sid
     */
    private static final long serialVersionUID = 6463966027089408372L;
    /** job schema key in bean */
    public static final String JOB = "JOB_CLASS_NAME";
    /** job name schema key in bean */
    public static final String JOB_PK = "JOB_CODE";
    /** job group name */
    public static final String JOB_GROUP = "JOB_GROUP";
    /** job description schema key in bean */
    public static final String JOB_DESC = "DESCRIPTION";
    /** job data schema key in bean */
    public static final String JOB_DATA = "JOB_DATA";
    /** job recovery schema key in bean */
    public static final String JOB_RECOVERY = "REQUESTS_RECOVERY";

    /**
     * 
     */
    public RhJobDetail() {

    }

    /**
     * value of RhJobDetail
     * @param bean - bean
     */
    public RhJobDetail(Bean bean) {
        super(bean);
    }

    /**
     * 设置任务组
     * @param group - group
     */
    public void setJobGroup(String group) {
        set(JOB_GROUP, group);
    }

    /**
     * 获取任务组
     * @return - 任务所属组,默认为:DEFAULT
     */
    public String getJobGroup() {
        return get(JOB_GROUP, "");
    }

    /**
     * 指定实现类
     * @param classStr - 任务实现类
     */
    public void setJobClass(String classStr) {
        set(JOB, classStr);
    }

    /**
     * 获取任务实现类
     * @return - 实现类路径
     */
    public String getJobClass() {
        return getStr(JOB);
    }

    /**
     * 设置任务code
     * @param code - 主键
     */
    public void setJobCode(String code) {
        set(JOB_PK, code);
    }

    /**
     * 获取任务code
     * @return - 任务主键
     */
    public String getJobCode() {
        return getStr(JOB_PK);
    }

    /**
     * 设置任务说明
     * @param desc - desc str
     */
    public void setJobDesc(String desc) {
        set(JOB_DESC, desc);
    }

    /**
     * 获取任务说明
     * @return - desc str
     */
    public String getJobDesc() {
        return get(JOB_DESC, "");
    }

    /**
     * 任务配置数据
     * @param data - data map
     */
    public void setJobData(String data) {
        set(JOB_DATA, data);
    }

    /**
     * 获取任务配置数据
     * @return - data map
     */
    public Bean getJobData() {

        Object dataObj = get(JOB_DATA);
        if (dataObj instanceof Bean) {
            return (Bean) dataObj;
        } else {
            String dataStr = (String) dataObj;
            Bean data = stringToBean(dataStr);
            return data;
        }
    }

    /**
     * 设置任务是否为中断恢复
     * 
     * 中断恢复概念:倘若任务调度系统中断后，重新启动。 触发器会执行所有中断时间内错过的任务.
     * 
     * @param recovery - 是否中断恢复
     */
    public void setRecovery(int recovery) {
        set(JOB_RECOVERY, recovery);
    }

    /**
     * 获取是否配置中断恢复
     * @return - 是否中断恢复
     */
    public boolean getRecovery() {
        Object resultObj = get(JOB_RECOVERY);
        if (resultObj instanceof Integer) {
            return 1 == (Integer) resultObj;
        } else if (resultObj instanceof Boolean) {
            return (Boolean) resultObj;
        } else {
            return false;
        }
    }

    /**
     * valueOf map
     * @param dataStr format key1=value1,key2=value2
     * @return map
     */
    protected Bean stringToBean(String dataStr) {
        Bean data = new Bean();
        if (null != dataStr && 0 < dataStr.length()) {
            String[] items = dataStr.split(",");
            for (String item : items) {
                String[] tmp = item.split("=");
                if (2 == tmp.length) {
                    data.put(tmp[0].trim(), tmp[1].trim());
                }
            }
        }
        return data;
    }
}
