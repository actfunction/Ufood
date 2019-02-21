package com.rh.core.util.scheduler;

/*
 * Copyright (c) 2013 Ruaho All rights reserved.
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.util.DateUtils;

/**
 * @author wanglong
 * 
 */
public class RhTrigger extends Bean {

    /**
     * 
     */
    private static final long serialVersionUID = 7543668826249794021L;
    /** job trigger crontab expression (for crontab expression trigger) */
    public static final String TRIG_CRON_EXPRESSION = "CRONTAB_EXPRESSTION";
    /** job trigger type (simple/crontab expression) */
    public static final String TRIG_TYPE = "TRIGGER_TYPE";
    /** job trigger start time(for simple trigger) */
    public static final String TRIG_START_TIME = "START_TIME";
    /** job trigger end time(for simple trigger) */
    public static final String TRIG_END_TIME = "END_TIME";
    /** job trigger repeat count(for simple trigger) */
    public static final String TRIG_REPEAT_COUNT = "SIMPLE_TRIGGER_REPEAT_COUNT";
    /** job trigger interval(for simpler trigger) */
    public static final String TRIG_INTERVAL = "SIMPLE_TRIGGER_INTERVAL";
    /** job trigger name */
    public static final String TRIG_PK = "TRIGGER_CODE";
    /** job trigger state */
    public static final String TRIG_STATE = "TRIGGER_STATE";
    /** job trigger group */
    public static final String TRIG_GROUP = "TRIGGER_GROUP";
    /** previous fire time of trigger */
    public static final String TRIG_PREV_FIRE_TIME = "PREV_FIRE_TIME";
    /** next fire time of trigger */
    public static final String TRIG_NEXT_FIRE_TIME = "NEXT_FIRE_TIME";
    /** job trigger description */
    public static final String TRIG_DESC = "DESCRIPTION";

    /** job code */
    public static final String JOB_CODE = RhJobDetail.JOB_PK;

    /**
     * 
     */
    public RhTrigger() {
    }

    /**
     * 构造函数
     * @param root - bean
     */
    public RhTrigger(Bean root) {
        super(root);
    }

    /**
     * 设置 jobcode
     * @param jobCode - job code
     */
    public void setJobCode(String jobCode) {
        set(JOB_CODE, jobCode);
    }

    /**
     * get job code
     * @return - job code
     */
    public String getJobCode() {
        return getStr(JOB_CODE);
    }

    /**
     * 设置执行表达式
     * @param expression - 表达式
     */
    public void setCronExpression(String expression) {
        set(TRIG_CRON_EXPRESSION, expression);
        if (0 == getType()) {
            setType(2);
        }
    }

    /**
     * 获取表达式
     * @return - crontab表达式
     */
    public String getCronExpression() {
        return getStr(TRIG_CRON_EXPRESSION);
    }

    /**
     * 设置表达式类型 1:简单,2:crontab表达式
     * @param type - 表达式类型
     */
    public void setType(int type) {
        set(TRIG_TYPE, type);
    }

    /**
     * 获取触发器类型
     * @return - 触发器类型
     */
    public int getType() {
        return getInt(TRIG_TYPE);
    }

    /**
     * 设置开始时间
     * @param startTime - 开始时间
     */
    public void setStartTime(Date startTime) {
        setStartTime(DateUtils.formatDatetime(startTime));
    }

    /**
     * 设置开始时间
     * @param time - 开始时间
     */
    public void setStartTime(String time) {
        set(TRIG_START_TIME, time);
    }

    /**
     * 获取开始时间
     * @return - 开始时间
     */
    public Date getStartTime() {
        Object timeObj = get(TRIG_START_TIME);
        if (null == timeObj) {
            return new Date();
        } else {
            return getTime(timeObj);
        }
    }

    /**
     * 设置结束时间
     * @param time - 结束时间
     */
    public void setEndTime(Date time) {
        setEndTime(DateUtils.formatDatetime(time));
    }

    /**
     * 设置结束时间
     * @param time - 结束时间
     */
    public void setEndTime(String time) {
        set(TRIG_END_TIME, time);
    }

    /**
     * 获取结束时间
     * @return - 结束时间
     */
    public Date getEndTime() {
        Object timeObj = get(TRIG_END_TIME);
        return getTime(timeObj);
    }

    /**
     * 设置重复次数 (简单触发器类型下有效)
     * @param repeat - repeat
     */
    public void setRepeatCount(int repeat) {
        set(TRIG_REPEAT_COUNT, repeat);
    }

    /**
     * 获取重复执行次数
     * @return - 重复执行次数
     */
    public int getRepeatCount() {
        return get(TRIG_REPEAT_COUNT, 0);
    }

    /**
     * 执行间隔(简单触发器类型下有效)
     * @param interval - 间隔时间 (s)
     */
    public void setInterval(int interval) {
        set(TRIG_INTERVAL, interval);
        if (0 == getType()) {
            setType(1);
        }
    }

    /**
     * 获取执行间隔
     * @return - 执行间隔(s)
     */
    public int getInterval() {
        return get(TRIG_INTERVAL, 0);
    }

    /**
     * 设置触发器代码
     * @param code - trigger code
     */
    public void setCode(String code) {
        set(TRIG_PK, code);
    }

    /**
     * 获取触发器代码
     * @return - 触发器代码
     */
    public String getCode() {
        String str = getStr(TRIG_PK);
        if (null == str || 0 == str.length()) {
            return getId();
        } else {
            return str;
        }
    }

    /**
     * 设置触发器状态
     * @param state - state
     */
    public void setState(String state) {
        set(TRIG_STATE, state);
    }

    /**
     * 获取触发器状态
     * @return - 状态
     */
    public String getState() {
        return getStr(TRIG_STATE);
    }

    /**
     * 设置触发器组
     * @param group - 分组
     */
    public void setGroup(String group) {
        set(TRIG_GROUP, group);
    }

    /**
     * 获取触发器分组
     * @return - 分组
     */
    public String getGroup() {
        return get(TRIG_GROUP, "DEFAULT");
    }

    /**
     * 设置上次触发时间
     * @param time - 上次触发时间
     */
    public void setPrevFireTime(Date time) {
        setPrevFireTime(DateUtils.formatDatetime(time));
    }

    /**
     * 设置上次触发时间
     * @param time - 上次触发时间
     */
    public void setPrevFireTime(String time) {
        set(TRIG_PREV_FIRE_TIME, time);
    }

    /**
     * 获取上次触发时间
     * @return - 上次触发时间
     */
    public Date getPrevFireTime() {
        Object timeObj = get(TRIG_PREV_FIRE_TIME);
        return getTime(timeObj);
    }

    /**
     * 设置下次触发时间
     * @param time - 下次触发时间
     */
    public void setNextFireTime(Date time) {
        setNextFireTime(DateUtils.formatDatetime(time));
    }

    /**
     * 设置下次触发时间
     * @param time - 下次触发时间
     */
    public void setNextFireTime(String time) {
        set(TRIG_NEXT_FIRE_TIME, time);
    }

    /**
     * 获取下次触发时间
     * @return - 下次触发时间
     */
    public Date getNextFireTime() {
        Object timeObj = get(TRIG_NEXT_FIRE_TIME);
        return getTime(timeObj);
    }

    /**
     * 设置说明
     * @param desc - 说明
     */
    public void setDescription(String desc) {
        set(TRIG_DESC, desc);
    }

    /**
     * 获取说明
     * @return - 说明
     */
    public String getDescription() {
        return getStr(TRIG_DESC);
    }

    /** date format expresstion **/
    private static final String DATE_FORMAT_PATTEN = "yyyy-MM-dd HH:mm:ss";

    /**
     * get Date
     * @param timeParam time string ,format: yyyy-mm-dd HH:MM:SS
     * @return <CODE>Date</CODE>
     */
    protected Date parseDate(String timeParam) {
        Date trigTime = null;
        if (0 < timeParam.length()) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTEN, Locale.getDefault());

                trigTime = format.parse(timeParam);
            } catch (ParseException e) {
                throw new TipException("date format must be '" + DATE_FORMAT_PATTEN + "', date:" + timeParam);
            }
        }
        return trigTime;

    }

    /**
     * get date String
     * @param date <CODE>Date</CODE>
     * @return date str
     */
    protected String formatDate(Date date) {
        String timeString = "";
        if (null != date) {
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTEN, Locale.getDefault());
            timeString = format.format(date);
        }
        return timeString;

    }

    /**
     * get datetime
     * @param timeObj - time obj
     * @return - datetime
     */
    private Date getTime(Object timeObj) {
        if (null == timeObj) {
            return null;
        }
        if (timeObj instanceof Date) {
            return (Date) timeObj;
        } else if (timeObj instanceof Integer) {
            return new Date((Integer) timeObj);
        } else {
            return parseDate((String) timeObj);
        }
    }
}
