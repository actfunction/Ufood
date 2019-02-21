package com.rh.core.util.var;

import java.util.HashMap;
import java.util.Map;

import com.rh.core.util.DateUtils;


/**
 * 日期时间实现变量的接口类。
 * 
 * @author wanglong
 * @version $Id$
 */
public class DateVar implements Var {
    /** 单例 */
    private static DateVar inst = null;
    /**
     * 私有构建体，单例模式
     */
    private DateVar() {
        
    }
    
    /**
     * 单例方法
     * @return 获取日期变量类
     */
    public static DateVar getInst() {
        if (inst == null) {
            inst = new DateVar();
        }
        return inst;
    }
    
    /**
     * 获取变量值
     * @param key 键值
     * @return 变量值
     */
    public String get(String key) {
        if (key.equals("@DATE@")) {
            return DateUtils.getDate();
        } else if (key.equals("@DATETIME@")) {
            return DateUtils.getDatetime();
        } else if (key.equals("@DATETIME_ZD@")) {
            return DateUtils.getDatetimeZd();
        } else if (key.equals("@DATETIME_TS@")) {
            return DateUtils.getDatetimeTS();
        } else if (key.startsWith("@DATE_DIFF_")) { //处理差额日期
            int diff;
            int period;
            if (key.equals("@DATE_DIFF_@")) {
                diff = 0;
                period = 1;
            } else {
                //正差额样例：@DATE_DIFF_DD30_P@ --POSITIVE缩写
                //负差额样例：@DATE_DIFF_DD30_N@ --NEGATIVE缩写
                if (key.startsWith("@DATE_DIFF_DD")) { // 按天求差额                    
                    period = 1;
                } else if (key.startsWith("@DATE_DIFF_MM")) { // 按月求差额
                    period = 2;
                } else if (key.startsWith("@DATE_DIFF_YY")) { // 按年求差额
                    period = 3;
                } else {
                    return key;
                }
                diff = Integer.parseInt(key.substring(13, key.length() - 3));
                if (key.substring(key.length() - 3, key.length() - 1).equals("_N")) {
                    return DateUtils.getCertainDate(DateUtils.getDate(), -diff, period);
                }
            }
            return DateUtils.getCertainDate(DateUtils.getDate(), diff, period);
        } else if (key.equals("@DATE_YEAR@")) {
            return String.valueOf(DateUtils.getYear());
        } else if (key.equals("@DATE_MONTH@")) {
            return String.valueOf(DateUtils.getMonth());
        } else if (key.startsWith("@DATE_YEARMONTH")) {
            int monthDiff;
            if (key.equals("@DATE_YEARMONTH@")) {
                monthDiff = 0;
            } else {
                monthDiff = Integer.parseInt(key.substring(16, 17));
            }
            return DateUtils.getYearMonth(-monthDiff);
        } else if (key.equals("@DATE_YEARMONTH_B1@")) {
            return DateUtils.getYearMonth(-1);
        } else if (key.equals("@DATE_CN@")) {
            return DateUtils.getDateCN();
        } else if (key.equals("@DATE_WEEK_DAY_CN@")) {
            return DateUtils.getChineseDayOfWeek();
        } else {
            return key;
        }
    }
    
    /**
     * 获取日期相关系统变量
     * @return 指定key的value
     */
    public Map<String, String> getMap() {
        Map<String, String> dateMap = new HashMap<String, String>();
        dateMap.put("@DATE@", DateUtils.getDate()); //当前日期：2012-12-12
        dateMap.put("@DATE_YEAR@", String.valueOf(DateUtils.getYear())); //本年：2012
        dateMap.put("@DATE_MONTH@", String.valueOf(DateUtils.getMonth())); //本月：12
        dateMap.put("@DATE_YEARMONTH@", DateUtils.getYearMonth()); //本年月：2012-12
        dateMap.put("@DATE_CN@", DateUtils.getDateCN()); //中文日期：2012年12月12日
        dateMap.put("@DATE_WEEK_DAY_CN@", DateUtils.getChineseDayOfWeek()); //中文星期：星期一
        return dateMap;
    }
}