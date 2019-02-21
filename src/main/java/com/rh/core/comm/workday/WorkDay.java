package com.rh.core.comm.workday;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

/**
 * 
 * @author wanglong
 *
 */
public class WorkDay {
    /** 工作日的 服务  */
    public static final String SY_COMM_WORK_DAY_SERV = "SY_COMM_WORK_DAY";
    
    /** 工作日 */
    public static final int WORK_DAY = 1;
    
    /** 非工作日 */
    public static final int WORK_DAY_NOT = 2;
    
    
    // 系统配置的，不是周末，却不用上班的日期
    private HashMap<String, String> noWorkDayMap = new HashMap<String, String>();
    
    // workDayMap
    private HashMap<String, String> workDayMap = new HashMap<String, String>();
    
    // 参考系统设置的，指定月份所有上班的日期
    private HashMap<String, String> availableDays = new HashMap<String, String>();
    
    /** 年 */
    private int mYear = 0;
    
    /** 月 */
    private int mMonth = 0;
    
    /**
     * 初始化考勤管理工作日对象。通过这个对象可以取得指定月份的考勤日期，也就是指定月份那几天需要上班。
     * 
     * @param year 年
     * @param month 月
     * @param cmpyID 公司ID
     */
    public WorkDay(int year , int month , long cmpyID) {
        mYear = year;
        mMonth = month;
        init(year, month, cmpyID);
    }
    
    /**
     * 
     * @param year 年 
     * @param month 月 
     * @param cmpyID 公司ID
     */
    private void init(int year , int month , long cmpyID) {
        String strWhere = "";
        strWhere += " and DAY_SPECIAL_DATE like '" + year + "-"
                + convert(month) + "%'";
        
        if (cmpyID > 0) {
            strWhere += " and DAY_CMPY_ID = " + cmpyID;
        }
        
        Bean queryBean = new Bean();
        queryBean.set(Constant.PARAM_WHERE, strWhere);
        
        List<Bean> dayList = ServDao.finds(SY_COMM_WORK_DAY_SERV, queryBean);
        
        for (Bean day: dayList) {
            if (day.getInt("DAY_FLAG") == WORK_DAY) {
                workDayMap.put(day.getStr("DAY_SPECIAL_DATE"), day.getStr("DAY_SPECIAL_DATE"));
            } else if (day.getInt("DAY_FLAG") == WORK_DAY_NOT) {
                noWorkDayMap.put(day.getStr("DAY_SPECIAL_DATE"), day.getStr("DAY_SPECIAL_DATE"));
            }
        }
        
        getWorkDayInMonth();
    }
    
    /**
     * 
     * @param month 月
     * @return 转换后的月份，小于10 前面加0
     */
    private String convert(int month) {
        if (month < 10) {
            return "0" + month;
        } else {
            return String.valueOf(month);
        }
    }
    
    /**
     * 判断指定日期是否是工作日。
     * 
     * @param strDate 这个日期必须是初始化对象时指定年月内的日期。
     * @return 判断指定日期是否是工作日。
     */
    public boolean isAvailableWorkDay(String strDate) {
        return this.availableDays.containsKey(strDate);
    }
    
    /**
     * 
     * @param dateStr 日期
     * @return 日期对应在周几
     */
    public static int getDayOfWeek(String dateStr) {
        Calendar cale = Calendar.getInstance();
        String[] items = dateStr.split("-");
        cale.set(Integer.parseInt(items[0]), Integer.parseInt(items[1]) - 1, Integer.parseInt(items[2]));
        
        return cale.get(Calendar.DAY_OF_WEEK);
    }
    
    
    /**
     * 获取月内的工作日
     */
    private void getWorkDayInMonth() {
        int dayCount = DateUtils.getDayOfMonth(mMonth, mYear);
        
        String firstDay = mYear + "-" + convert(mMonth) + "-" + "01";
        
        int weekNum = getDayOfWeek(firstDay);
        int startNum = weekNum;
        dayCount += weekNum;

        for (; startNum < dayCount; startNum++) {
            int a = startNum % 7;

            if (a != 0 && a != 1) {
                String nowDate = mYear + "-" + convert(mMonth) + "-"
                        + convert(startNum - weekNum + 1);
                if (!noWorkDayMap.containsKey(nowDate)) {
                    this.availableDays.put(nowDate, nowDate);
                }
            } else {
                String nowDate = mYear + "-" + convert(mMonth) + "-"
                        + convert(startNum - weekNum + 1);
                if (this.workDayMap.containsKey(nowDate)) {
                    this.availableDays.put(nowDate, nowDate);
                }
            }
        }
        
        // return days;
    }
    

    
    /**
     * @return 取得指定年月应该上班的日期
     * 
     */
    public HashMap<String, String> getAvailableDays() {
        return this.availableDays;
    }
    
    /**
     * 取得指定月份需要上班的天数
     * 
     * @return 取得指定月份需要上班的天数
     */
    public int getWorkDayCount() {
        return this.availableDays.size();
    }
    
    /**
     * 
     * @return 月份
     */
    public int getMonth() {
        return mMonth;
    }
    
    /**
     * 
     * @return 年
     */
    public int getYear() {
        return mYear;
    }
    
    /**
     * 取得本月工作日的开始日期
     * 
     * @return 取得本月工作日的开始日期
     */
    public String getBeginDate() {
        return (String) availableDays.get(0);
    }
    
    /**
     * 取得本月工作日的结束日期
     * 
     * @return 取得本月工作日的结束日期
     */
    public String getEndDate() {
        return (String) availableDays.get(availableDays.size() - 1);
    }
    
    /**
     * 取得这个月共有多少天
     * 
     * @return 取得这个月共有多少天
     */
    public int getMonthDayCount() {
        return DateUtils.getDayOfMonth(this.mMonth, this.mYear);
    }
    
}

