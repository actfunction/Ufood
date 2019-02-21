package com.rh.core.comm.workday;

import java.util.Date;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

/**
 * 时间计算
 * @author wanglong
 *
 */
public class WorkTime {
    private static final int MINUTE = 60 * 1000;
    
    private String morningUpTime = "";
    
    private String morningDownTime = "";
    
    private String afternoonUpTime = "";
    
    private String afternoonDownTime = "";
    
    /**
     * 构造方法
     */
    public WorkTime() {
        this.morningUpTime = this.getMorningWorkTime("up");
        this.morningDownTime = this.getMorningWorkTime("down");
        this.afternoonUpTime = this.getAfternoonWorkTime("up");
        this.afternoonDownTime = this.getAfternoonWorkTime("down");
    }
    
    /**
     * 
     * @param datetime 日期时间  如 2012-03-20 15:38:33
     * @param minute 分钟 
     * @return 日期时间  + 工作日时间  
     */
    public String addMinute(String datetime, int minute) {
        //dateTime这天剩余的时间  > minute
        String date = datetime.substring(0, 10);
        String beginTime = datetime.substring(11, 19);
        
        long toDownMin = this.calMinuteCount(date, beginTime, afternoonDownTime); //到下班时间之间的差
        
        long differMin = minute - toDownMin;
        if (differMin < 0) { //当天内就需要
            return dangTianTime(datetime, minute);
        }
        
        //minute - (dateTime 一天剩余的时间) / 每天小时，整天数
        long exactlyDay = differMin / this.getEveryDayWorkMinutes();
        //minute - (dateTime 一天剩余的时间) - 整天数 余下不足一天的时间
        long overMin = differMin % this.getEveryDayWorkMinutes();
        
        long queryNum = exactlyDay + 1;
        
        String lastDay = getTheLastDay(date, queryNum); 

        if (overMin == 0) {
            return lastDay + " " + this.morningUpTime; //新的一天的早上提醒
        } else {
            String dateStr = lastDay + " " + this.morningUpTime;

            String overMinStr = String.valueOf(overMin);
            
            return dangTianTime(dateStr, Integer.parseInt(overMinStr));
        }
    }

    /**
     * 
     * @param date 天
     * @param queryNum 查询的条数
     * @return 上班的最后的那天
     */
    protected String getTheLastDay(String date, long queryNum) {
        Bean queryBean = new Bean();
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and DAY_SPECIAL_DATE > '").append(date).append("'");
        strWhere.append(" and DAY_FLAG = ").append(WorkDay.WORK_DAY);
        
        queryBean.set(Constant.PARAM_WHERE, strWhere);
        queryBean.set(Constant.PARAM_ORDER, "DAY_SPECIAL_DATE ASC");
        queryBean.set(Constant.PARAM_ROWNUM, queryNum);    
        
        List<Bean> workDays = ServDao.finds(WorkDay.SY_COMM_WORK_DAY_SERV, queryBean);
        String lastDay = workDays.get(workDays.size() - 1).getStr("DAY_SPECIAL_DATE");
        return lastDay;
    }
    
    /**
     * 
     * @param datetime 时间 格式  2012-03-20 15:38:33
     * @param minute 处理的分钟时间
     * @return 加过之后的
     */
    private String dangTianTime(String datetime, int minute) {
        String date = datetime.substring(0, 10);
        String beginTime = datetime.substring(11, 19);
        
        Date oldDate = DateUtils.getDateFromString(datetime);
        
        
        if (DateUtils.compareOnlyByTime(morningDownTime, beginTime) >= 0) { //开始时间在上午
            if (DateUtils.compareOnlyByTime(morningUpTime, beginTime) >= 0) { //上班之前，重置开始时间为上班时间
                beginTime = morningUpTime;
                
                oldDate = DateUtils.getDateFromString(date + " " + beginTime);
            }
            
            //上午时间就够了
            long toMorningDownMin = this.calMinuteCount(date, beginTime, morningDownTime); //到上午下班时间之间的差
            
            if (minute - toMorningDownMin < 0) { //上午就需要
                Date newDate = DateUtils.addMinutes(oldDate, minute);
                
                return DateUtils.formatDatetime(newDate);
            } else { //下午还有时间可以用
                Date newDate = DateUtils.addMinutes(oldDate, minute + getNoonOffMin());
                
                return DateUtils.formatDatetime(newDate);
            } 
        } else if (DateUtils.compareOnlyByTime(beginTime, morningDownTime) >= 0 
                && DateUtils.compareOnlyByTime(afternoonUpTime, beginTime) >= 0) { //开始时间在中午休息时间
            String newBegin = date + " " + afternoonUpTime;
            
            Date newDate = DateUtils.addMinutes(DateUtils.getDateFromString(newBegin), minute);
            
            return DateUtils.formatDatetime(newDate);
        } else if (DateUtils.compareOnlyByTime(beginTime, afternoonUpTime) >= 0) { //开始时间在，下午上班时间，当天就能搞完
            Date newDate = DateUtils.addMinutes(oldDate, minute);
            
            return DateUtils.formatDatetime(newDate);
        } else { //开始时间在下班之后
            return datetime;
        }
    }

    /**
     * 
     * @return 中午休息的分钟数
     */
    private int getNoonOffMin() {
        long min = DateUtils.getDiffTime("2000-01-01 " + this.morningDownTime, 
                "2000-01-01 " + this.afternoonUpTime) / MINUTE;
        String minStr = String.valueOf(min);
        
        
        return Integer.parseInt(minStr);
    }

    /**
     * 
     * @param cmpyId 公司ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 上班的时间
     */
    public long calWorktime(String cmpyId , String beginTime , String endTime) {
        if (endTime.length() == 0) {
            return 0L;
        }
        String beginDate = beginTime.substring(0, 10);
        String endDate = endTime.substring(0, 10);
        int count = 0;
        WorkOff workday = new WorkOff(beginDate, endDate, cmpyId);
        long worktime = 0;
        if (beginDate.equals(endDate)) {
            if (workday.isWorkOffDay(beginDate)) {
                return 0;
            } else {
                return calMinuteCount(beginDate, beginTime.substring(11),
                        endTime.substring(11));
            }
            
        } else if (workday.getWorkDayCount() == 0) {
            // 开始和结束之间的都是休息日
            return 0;
        } else if (workday.isWorkOffDay(beginDate)
                && workday.isWorkOffDay(endDate)) {
            // 开始时间和结束时间都是休息日
            worktime = 0;
        } else if (workday.isWorkOffDay(beginDate)) {
            // 开始时间是休息日
            worktime = calMinuteCount(endDate, "00:00:00", endTime.substring(11));
            count = 1;
        } else if (workday.isWorkOffDay(endDate)) {
            // 结束日期是休息日
            worktime = calMinuteCount(beginDate, beginTime.substring(11), "23:59:59");
            count = 1;
        } else {
            // 开始时间和结束时间都不是休息日
            worktime = calMinuteCount(beginDate, beginTime.substring(11), "23:59:59")
                    + calMinuteCount(endDate, "00:00:00", endTime.substring(11));
            count = 2;
        }
        
        return worktime + (workday.getWorkDayCount() - count) * getEveryDayWorkMinutes();
        
    }
    
    /**
     * 'workTime':'08:30:00,11:30:00,13:00:00,17:00:00'
     * @param date 指定日期，如：2012-09-10
     * @param beginTime 指定日期的开始时间，如:13:00:00
     * @param endTime 指定日期的结束时间，如:18:00:00
     * @return 获取指定日期， 开始/结束时间 中的  工作时间
     */
    public long calMinuteCount(String date , String beginTime , String endTime) {
        WorkOff workday = new WorkOff(date, date, "");
        if (workday.isWorkOffDay(date)) {
            return 0;
        }
        
        return getOneDayWorkingMinutes(date, beginTime, endTime);
    }
    
    /**
     * 
     * @param date 任意日期
     * @param beginTime 指定日期的开始时间，如:13:00:00
     * @param endTime 指定日期的结束时间，如:18:00:00
     * @return 取得一天内指定时间内的上班时间，单位为分钟数。不管是否是工作日
     */
    private long getOneDayWorkingMinutes(String date, String beginTime, String endTime) {
        String begin = date + " " + beginTime;
        String end = date + " " + endTime;
        
        // 得到上午上班时间点
        String morWorkTime = date + " " + getMorningWorkTime("up");
        
        // 得到上午下班时间点
        String morWorkOffTime = date + " " + getMorningWorkTime("down");
        
        
        // 得到下午上班时间点
        String afterWorkTime = date + " " + getAfternoonWorkTime("up");
        
        // 得到下午下班时间点
        String afterWorkOffTime = date + " " + getAfternoonWorkTime("down");
        
        
        // 如果开始时间大于下午下班时间，或者结束时间小于上班上班时间，则返回0
        if (DateUtils.getDiffTime(begin, afterWorkOffTime) < 0
                || DateUtils.getDiffTime(end, morWorkTime) > 0) {
            return 0;
        }
        
        String tempBegin = begin;
        String tempEnd = end;
        
        long interval = 0;
        // 如果开始时间早于上午上班时间，则将早上的上班时间付给开始时间
        if (DateUtils.getDiffTime(begin, morWorkTime) > 0) {
            tempBegin = morWorkTime;
        } else if (DateUtils.getDiffTime(begin, morWorkOffTime) <= 0
                && DateUtils.getDiffTime(begin, afterWorkTime) >= 0) { // 如果开始时间在上午下班时间和下午上班时间之间
            tempBegin = afterWorkTime;
        }
        
        // 如果结束时间大于下午下班时间，则将下午下班的时间付给结束时间
        if (DateUtils.getDiffTime(end, afterWorkOffTime) < 0) {
            tempEnd = afterWorkOffTime;
        } else if (DateUtils.getDiffTime(end, morWorkOffTime) < 0
                && DateUtils.getDiffTime(end, afterWorkTime) > 0) { // 如果结束时间在上午下班时间和下午上班时间之间
            tempEnd = morWorkOffTime;
        }
        
        // 如果开始时间和结束之间跨越中午休息时间，则需要计算中午休息的时间
        long sec = 0;
        if (DateUtils.getDiffTime(begin, morWorkOffTime) > 0
                && DateUtils.getDiffTime(end, afterWorkTime) < 0) {
            sec = DateUtils.getDiffTime(morWorkOffTime, afterWorkTime);
        }
        
        // 如果开始时间大于结束时间，则返回0
        if (DateUtils.getDiffTime(tempBegin, tempEnd) <= 0) {
            return 0;
        }
        
        interval = DateUtils.getDiffTime(tempBegin, tempEnd) - sec;
        
        return Math.round((interval * 1.0) / MINUTE);
    }
    
    /**
     * 
     * @param flag 取上午上班时间和下班时间的标示，如up为上班时间，down为下班时间
     * @return 根据标示取得上午的上班和下班时间
     */
    private String getMorningWorkTime(String flag) {
        String workTime = getWorkTime();
        
        String[] workArr = workTime.split(",");
        
        if (flag.equals("up")) {
            return workArr[0];
        } else {
            return workArr[1];
        }
    }
    
    /**
     * 
     * @param flag 取下午上班时间和下班时间的标示，如up为上班时间，down为下班时间
     * @return 根据标示取得下午的上班和下班时间
     */
    private String getAfternoonWorkTime(String flag) {
        String workTime = getWorkTime();
        
        String[] workArr = workTime.split(",");
        
        if (flag.equals("up")) {
            return workArr[2];
        } else {
            return workArr[3];
        }
    }
    
    /**
     * 取得每天的上班分钟数
     * 
     * @param workTimes
     * @return 取得每天的上班分钟数
     */
    private long getEveryDayWorkMinutes() {
        String nowDate = DateUtils.getDate();
        return getOneDayWorkingMinutes(nowDate, this.getMorningWorkTime("up"), this.getAfternoonWorkTime("down"));
    }
    
    /**
     * 
     * @return 上班时间的设定值
     */
    private String getWorkTime() {
        return Context.getSyConf("SY_COMM_WORK_TIME", "08:30:00,11:30:00,13:00:00,17:00:00");
    }
    
    
    /**
     * 
     * @return 上午上班时间
     */
    public String getMorningUpTime() {
        return morningUpTime;
    }

    /**
     * 
     * @return 上午下班时间
     */
    public String getMorningDownTime() {
        return morningDownTime;
    }

    /**
     * 
     * @return 下午上班时间
     */
    public String getAfternoonUpTime() {
        return afternoonUpTime;
    }

    /**
     * 
     * @return 下午下班时间
     */
    public String getAfternoonDownTime() {
        return afternoonDownTime;
    }
}
