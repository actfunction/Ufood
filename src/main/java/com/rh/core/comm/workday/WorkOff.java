package com.rh.core.comm.workday;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

/**
 * 考勤管理工作日
 * @author wanglong
 *
 */
public class WorkOff {
    // 系统配置的，不是周末，却不用上班的日期
    private HashMap<String, String> noWorkDayMap = new HashMap<String, String>();
    
    // workDayMap
    private HashMap<String, String> workDayMap = new HashMap<String, String>();
    
    /** 开始日期*/
    private String bDate = "";
    
    /** 结束日期*/
    private String eDate = "";
    
    /**
     * 初始化考勤管理工作日对象。通过这个对象可以取得指定月份的考勤日期，也就是指定月份那几天需要上班。
     * 
     * @param bdate 开始时间
     * @param edate 结束时间
     * @param cmpyID 公司ID
     */
    public WorkOff(String bdate , String edate , String cmpyID) {
        bDate = bdate;
        eDate = edate;
        init(bDate, eDate, cmpyID);
    }
    
    /**
     * 
     * @param bdate 开始时间
     * @param edate 结束时间
     * @param cmpyID 公司ID
     */
    private void init(String bdate , String edate , String cmpyID) {
        String strWhere = "";
        strWhere += " and DAY_SPECIAL_DATE >= '" + bdate
                + "' and DAY_SPECIAL_DATE <= '" + edate + "'";
        if (cmpyID.length() > 0) {
            strWhere += " and S_CMPY = '" + cmpyID + "'";
        }
        Bean queryBean = new Bean();
        queryBean.set(Constant.PARAM_WHERE, strWhere);
        
        List<Bean> list = ServDao.finds(WorkDay.SY_COMM_WORK_DAY_SERV, queryBean); 
        
        for (Bean day: list) {
            String dateStr = day.getStr("DAY_SPECIAL_DATE");
            if (day.getInt("DAY_FLAG") == WorkDay.WORK_DAY) {
                workDayMap.put(dateStr, dateStr);
            } else if (day.getInt("DAY_FLAG") == WorkDay.WORK_DAY_NOT) {
                noWorkDayMap.put(dateStr, dateStr);
            }
        }
        getWorkOffDay();
    }
    
    /**
     * 表中没有记录的，按照休假计算
     */
    private void getWorkOffDay() {
        int dayCount = DateUtils.selectDateDiff(eDate, bDate);
        
        int weekNum = WorkDay.getDayOfWeek(bDate);
        int startNum = weekNum;
        dayCount += weekNum;

        for (; startNum <= dayCount; startNum++) {
            int a = startNum % 7;
            if (a == 0 || a == 1) {
                Date bDateObj = DateUtils.getDateFromString(bDate);
                Date newDateObj = DateUtils.addDays(bDateObj, startNum - weekNum);
                
                String nowDate = DateUtils.formatDate(newDateObj);
                if (!this.workDayMap.containsKey(nowDate)
                        && !this.noWorkDayMap.containsKey(nowDate)) {
                    noWorkDayMap.put(nowDate, nowDate);
                }
            }
        }
    }
    
    /**
     * 
     * @return 取得指定时间内不要上班的天数
     */
    public int getWorkOffCount() {
        return this.noWorkDayMap.size();
    }
    
    /**
     * @return 取得指定时间内工作日的数量
     */
    public int getWorkDayCount() {
        return this.workDayMap.size();
    }
    
    /**
     * @param date 日期
     * @return 指定日期是否是节假日
     */
    public boolean isWorkOffDay(String date) {
        return noWorkDayMap.containsKey(date);
    }
}
