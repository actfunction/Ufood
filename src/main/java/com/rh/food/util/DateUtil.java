package com.rh.food.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;


/**
 * Date util now...
 * 
 * @author
 */
public class DateUtil {
	public static final String defaultDatePattern = "yyyy-MM-dd";
	public static final String DATE_FORMAT_24HOUR_PATTERN = "yyyy-MM-dd HH:mm:ss";

	public static String getDatePattern() {
		return defaultDatePattern;
	}

	public static String getDate24HourPatter() {
		return DATE_FORMAT_24HOUR_PATTERN;
	}

	public static String getToday() {
		Date today = new Date();
		return format(today);
	}

	public static Date getTodayDate() {
		Date today = new Date();
		return today;
	}

	/**
	 * 得到当前日期的字符串
	 * 
	 * @return
	 */
	public static String getNow() {
		Date now = new Date();
		return format(now, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 得到当前日期的字符串
	 * 
	 * @return
	 */
	public static String getNow(String pattern) {
		Date now = new Date();
		return format(now, pattern);
	}
	/**
	 * 
	 * @return
	 */
	public static String getNow2() {
		Date now = new Date();
		return format(now, "yyyy-MM-dd HH:00:00");
	}
	/**
	 * 
	 * @return
	 */
	public static String getTodayBegin() {
		Date now = new Date();
		return format(now, "yyyy-MM-dd 00:00:00");
	}

	public static String getDefaultNow() {
		Date now = new Date();
		return format(now, "yyyyMMddHHmmssSSS");
	}

	public static String getTimestamp() {
		Date now = new Date();
		return format(now, "yyyyMMddHHmmss");
	}

	public static String getTodayA() {
		Date now = new Date();
		return format(now, "MMdd");
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String format(Date date) {
		return date == null ? "" : format(date, getDatePattern());
	}

	public static String format(Date date, String pattern) {
		return date == null ? "" : new SimpleDateFormat(pattern).format(date);
	}
	/**
	 * 
	 * @param strDate
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String strDate) throws ParseException {
		return StringUtils.isBlank(strDate) ? null : parse(strDate, getDatePattern());
	}

	/**
	 * parse string with pattern
	 * 
	 * @param strDate
	 * @param pattern
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String strDate, String pattern) throws ParseException {
		return StringUtils.isBlank(strDate) ? null : new SimpleDateFormat(pattern).parse(strDate);
	}

	public static Date parse2(String strDate) throws ParseException {
		return StringUtils.isBlank(strDate) ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(strDate);
	}

	/**
	 * @param date
	 * @param n
	 * @return
	 */
	public static String addMonthEndStr(int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, n);
		return format(cal.getTime(), "yyyyMM") + "01000000";
	}

	/**
	 * this will add day to the this date
	 * 
	 * @param date
	 * @param n
	 * @return
	 */
	public static String addDay(int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, n);
		return format(cal.getTime(), "yyyyMMddHHmmss");
	}

	public static Calendar addDayIgnore(Date date, int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DATE, n);
		return cal;
	}

	public static Calendar addDayIgnore(Calendar cal, int n) {
		Calendar cal2 = cal;
		cal2.add(Calendar.DATE, n);
		return cal2;
	}

	/**
	 * this will add day to the this date
	 * 
	 * @param date
	 * @param n
	 * @return
	 * @throws ParseException
	 */
	public static String addDay(String oldtime, int n) throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(parse(oldtime, "yyyyMMddHHmmss"));
		cal.add(Calendar.DATE, n);
		return format(cal.getTime(), "yyyyMMddHHmmss");
	}

	/**
	 * this will add month to this date
	 * 
	 * @param date
	 * @param n
	 * @return
	 */
	public static String addMonthTimeStr(Date date, int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, n);
		return format(cal.getTime(), "yyyyMMddHHmmss");
	}

	/**
	 * this will add month to this date
	 * 
	 * @param date
	 * @param n
	 * @return
	 */
	public static Date addMonth(Date date, int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, n);
		return cal.getTime();
	}

	/**
	 * this will add day to the this date
	 * 
	 * @param date
	 * @param n
	 * @return
	 */
	public static Date addDay(Date date, int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, n);
		return cal.getTime();
	}

	/**
	 * Add hour min now...
	 * 
	 * @param date
	 * @param hour
	 * @param min
	 * @return
	 */
	public static Date addHourMin(Date date, int hour, int min) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR, hour);
		cal.add(Calendar.MINUTE, min);
		return cal.getTime();
	}

	public static String addHourMin(String strDate, int hour, int min, String pattern) throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(parse(strDate, pattern));
		cal.add(Calendar.HOUR, hour);
		cal.add(Calendar.MINUTE, min);
		return format(cal.getTime(), pattern);
	}

	/**
	 * this just return midnight of date...
	 * 
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static Date getDMYDate(Date date) {
		Date result = date;
		try {
			date = parse(format(date));
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * return sunday of this week and dayOfWeek should be like: Calendar.SUNDAY
	 * 
	 * @param date
	 * @return
	 */
	public static Date getWeekDay(Date date, int dayOfWeek) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		return cal.getTime();
	}

	/**
	 * This will return Sunday of next week....
	 * 
	 * @param date
	 * @return
	 */
	public static String weekdayOfNextWeek(Date date, int weekday) {
		Date thisWeekDay = DateUtil.getWeekDay(date, weekday);
		Date nextWeekDay = DateUtil.addDay(thisWeekDay, 7);
		return format(nextWeekDay, "EEE dd MMM, yyyy");
	}

	/**
	 * This will return Sunday of next week....
	 * 
	 * @param date
	 * @return
	 */
	public static String weekdayOfThisWeek(Date date, int weekday) {
		Date thisWeekDay = DateUtil.getWeekDay(date, weekday);
		return format(thisWeekDay, "EEE dd MMM, yyyy");
	}

	/**
	 * return week of day by date
	 * 
	 * @param date
	 * @return
	 */
	public static int getWeekOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 
	 * @param myDate
	 * @return
	 */
	public static String formatDate3(Date myDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = formatter.format(myDate);
		return strDate;
	}

	/**
	 * 
	 * @param myDate
	 * @return
	 */
	public static String formatDate4(Date myDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String strDate = formatter.format(myDate);
		return strDate;
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate5(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		return dateFormat.format(date);
	}

	/**
	 * 
	 * @param myDate
	 * @return
	 */
	public static String formatDatePath(Date myDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/M/d/");
		String strDate = formatter.format(myDate);
		return strDate + System.currentTimeMillis();
	}

	/**
	 * 时间差
	 * 
	 * @param d1
	 * @param d2
	 * @return 毫秒级别
	 */
	public static long difTime(Date d1, Date d2) {
		return d1.getTime() - d2.getTime();
	}

	/**
	 * 比较两个时间差
	 * </pre>
	 */
	public static long diffTime(String time1, String time2, String format) throws ParseException {
		return parse(time1, format).getTime() - parse(time2, format).getTime();
	}

	/**
	 * 时间差
	 * 
	 * @param d1
	 * @param d2
	 * @return 毫秒级别
	 */
	public static long difNowTime(Date d1) {
		return d1.getTime() - new Date().getTime();
	}

	/**
	 * 时间差
	 * 
	 * @param d1
	 * @param d2
	 * @return 毫秒级别
	 */
	public static long difNowTime(String time, String format) throws ParseException {
		return parse(time, format).getTime() - new Date().getTime();
	}

	/**
	 * 
	 * <pre>
	 * Author: Liu Xiao
	 * &#64;param time 必须格式化日期字符串 例如：2014-02-12 00:00:00
	 * &#64;return
	 * </pre>
	 */
	public static long difNowTime(String time) {
		try {
			return parse(time, DATE_FORMAT_24HOUR_PATTERN).getTime() - new Date().getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 取时间大的
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static String maxDate(String s1, String s2) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			if (sf.parse(s1).getTime() >= sf.parse(s2).getTime()) {
				return s1;
			} else {
				return s2;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return s2;
	}

	public static String getExpiryDate(int day) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(parse(addDay(1), "yyyyMMddHHmmss"));
			cal.add(Calendar.DATE, day);
			return format(cal.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static java.sql.Timestamp getNowtimeStamp() {
		return new java.sql.Timestamp((new Date()).getTime());
	}

	public static String addMonthDefault(int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, n);
		return format(cal.getTime(), getDatePattern());
	}

	public static String getDayOfWeek(Date date) {
		String weekStr = "";
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		switch (cal.get(Calendar.DAY_OF_WEEK) - 1) {
		case 0:
			weekStr = "星期天";
			break;
		case 1:
			weekStr = "星期一";
			break;
		case 2:
			weekStr = "星期二";
			break;
		case 3:
			weekStr = "星期三";
			break;
		case 4:
			weekStr = "星期四";
			break;
		case 5:
			weekStr = "星期五";
			break;
		case 6:
			weekStr = "星期六";
			break;
		default:
			break;
		}
		return weekStr;
	}

	public static int[] getFormatTime(Date day) {
		long differTime = (new Date()).getTime() - day.getTime();
		int[] times = new int[3];
		// 毫秒转化为秒
		int totalsecends = (int) (differTime / 1000);
		// 小时
		times[0] = (totalsecends % (3600 * 24)) / 3600;
		// 分钟
		times[1] = ((totalsecends % (3600 * 24)) % 3600) / 60;
		// 秒数
		times[2] = ((totalsecends % (3600 * 24)) % 3600) % 60;
		return times;
	}

	/** 返回格式为 yyyy-MM-dd的第二天日期 */
	public static String getTomorrow() {
		return format(addDayIgnore(new Date(), 1).getTime());
	}

	/** 返回格式为 yyyy-MM-dd的下周一的日期 */
	public static String getNextMonday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_WEEK, 7 - 1);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return format(cal.getTime());
	}

	/** 返回格式为 yyyy-MM-dd的xNext下周一的日期 */
	public static String getXNextMonday(int xNext) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_WEEK, (7 * xNext));
		cal.set(Calendar.DAY_OF_WEEK, 2);
		return format(cal.getTime());
	}

	/** 返回格式为 yyyy-MM-dd的下个月一的日期 */
	public static String getFirstDayofNextMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return format(cal.getTime());
	}

	/**
	 * 计算几天以后
	 * 
	 * @author
	 * @date 2013-7-10下午05:38:42
	 * @param day
	 *            天数
	 * @return
	 */
	public static Timestamp getExpireTime(int day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, day);
		String expireTime = DateUtil.format(cal.getTime(), "yyyy-MM-dd HH:mm:ss");
		return Timestamp.valueOf(expireTime);
	}

	/**
	 * 当前日期减去,参数日期,得天数
	 * 
	 * @author 
	 * @date 2013-7-11上午09:24:51
	 * @return
	 */
	public static long dataMinus(Timestamp time) {
		Date date = new Date();
		long day = (date.getTime() - time.getTime()) / (24 * 60 * 60 * 1000);
		return day;
	}

	/**
	 * 得到本月的第一天
	 * 
	 * @author 
	 * @date 2013-10-14上午09:24:51
	 * @return
	 */
	public static String getMonthFirstDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		return DateUtil.format(calendar.getTime(), "yyyy-MM-dd");
	}

	/**
	 * 得到本月的最后一天
	 * 
	 * @author 
	 * @date 2013-10-14上午09:24:51
	 * @return
	 */
	public static String getMonthLastDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return DateUtil.format(calendar.getTime(), "yyyy-MM-dd 23:59:59");
	}

	/**
	 * 根据字符串转时间戳
	 * 
	 * @author 
	 * @date 2013-11-27下午05:27:27
	 * @param str
	 *            yyyy-MM-dd hh:mm:ss
	 * @return
	 */
	public static Timestamp getTimestamp(String str) {
		return Timestamp.valueOf(str);
	}

	/**
	 * 在日期参数上，增加天数
	 * 
	 * @author 
	 * @date 2015-05-07上午09:18:50
	 * @param strDate
	 *            日期参数
	 * @param day
	 *            天数
	 * @param pattern
	 *            返回日期格式
	 * @return
	 * @throws ParseException
	 */
	public static String addDay(String strDate, int day, String pattern) throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(parse(strDate, pattern));
		cal.add(Calendar.DATE, day);
		return format(cal.getTime(), pattern);
	}

	/**
	 * 在日期参数上，增加秒数
	 * 
	 * @author 
	 * @date 2013-11-27下午06:18:50
	 * @param strDate
	 *            日期参数
	 * @param seconds
	 *            秒数
	 * @param pattern
	 *            返回日期格式
	 * @return
	 * @throws ParseException
	 */
	public static String addSeconds(String strDate, int seconds, String pattern) throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(parse(strDate, pattern));
		cal.add(Calendar.SECOND, seconds);
		return format(cal.getTime(), pattern);
	}

	/**
	 * 取时间大的
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static String maxDate(String s1, String s2, String dateFormat) {
		SimpleDateFormat sf = new SimpleDateFormat(dateFormat);
		try {
			if (sf.parse(s1).getTime() >= sf.parse(s2).getTime()) {
				return s1;
			} else {
				return s2;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return s2;
	}

	/** 返回格式为 yyyy-MM-dd 本周一 */
	public static String getThisMonday() {
		return getNDay(0, Calendar.MONDAY);
	}

	/** 返回格式为 yyyy-MM-dd的 上周一 */
	public static String getLastMonday() {
		return getNDay(-1, Calendar.MONDAY);
	}

	/** 返回格式为 yyyyMMdd 本周一 */
	public static String getThisMonday2() {
		return getNDay2(0, Calendar.MONDAY);
	}

	/** 返回格式为 yyyyMMdd的 上周一 */
	public static String getLastMonday2() {
		return getNDay2(-1, Calendar.MONDAY);
	}

	/** 返回格式为 yyyy-MM-dd的 上上周一 */
	public static String getBeforeLastMonday() {
		return getNDay(-2, Calendar.MONDAY);
	}

	/** 返回格式为 yyyy-MM-dd的下周一的日期 */
	public static String getNextMonday2() {
		return getNDay(1, Calendar.MONDAY);
	}

	/**
	 * 由于中国将周日计算在上周，所以需要设定week*7-1
	 * 
	 * @param week
	 *            为推迟的周数，-1向前推迟一周，0本周，1下周，依次类推
	 * @param dayOfWeek
	 *            想周几，这里就传几Calendar.MONDAY（TUESDAY...）
	 * @return
	 */
	public static String getNDay(int week, int dayOfWeek) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, week * 7 - 1);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		return new SimpleDateFormat(defaultDatePattern).format(cal.getTime());
	}

	/**
	 * 由于中国将周日计算在上周，所以需要设定week*7-1
	 * 
	 * @param week
	 *            为推迟的周数，-1向前推迟一周，0本周，1下周，依次类推
	 * @param dayOfWeek
	 *            想周几，这里就传几Calendar.MONDAY（TUESDAY...）
	 * @return
	 */
	public static String getNDay2(int week, int dayOfWeek) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, week * 7 - 1);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		return new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
	}

	/**
	 * 美式算法，周日---周六算一周，周日算一周的第一天
	 * 
	 * <pre>
	 * </pre>
	 */
	public static String getDay(int week, int dayOfWeek) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, week * 7);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		return new SimpleDateFormat(defaultDatePattern).format(cal.getTime());
	}

	/** 获取当期日期 日期格式: 20130106 */
	public static String getDayOfToday() {
		Date date = new Date();
		return new SimpleDateFormat("yyyyMMdd").format(date);
	}

	/**
	 * 返回日期之间的天数
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws ParseException
	 */
	public static int dateDiff(String fromDate, String toDate) throws ParseException {
		int days = 0;

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		Date from = df.parse(fromDate);
		Date to = df.parse(toDate);
		days = (int) Math.abs((to.getTime() - from.getTime()) / (24 * 60 * 60 * 1000));
		return days;
	}

	/**
	 * 返回日期之间的天数
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static int dateDiff(String fromDate, String toDate, String pattern) throws ParseException {
		int days = 0;

		SimpleDateFormat df = new SimpleDateFormat(pattern);
		Date from = df.parse(fromDate);
		from.setHours(0);
		from.setMinutes(0);
		from.setSeconds(0);
		Date to = df.parse(toDate);
		to.setHours(0);
		to.setMinutes(0);
		to.setSeconds(0);
		days = (int) Math.abs((to.getTime() - from.getTime()) / (24 * 60 * 60 * 1000));
		return days;
	}

	/**
	 * 返回日期之间的天数，始终返回正数
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static int dateDiff(Date from, Date to) {
		if (from == null || to == null)
			return 0;
		int days = 0;

		from.setHours(0);
		from.setMinutes(0);
		from.setSeconds(0);
		to.setHours(0);
		to.setMinutes(0);
		to.setSeconds(0);
		days = (int) Math.abs((to.getTime() - from.getTime()) / (24 * 60 * 60 * 1000));
		return days;
	}

	/**
	 * 返回日期之间的天数,如果开始日期小于结束日期,或者日期中有null返回负数
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static int dateDiffReal(Date from, Date to) {
		if (from == null || to == null)
			return -1;
		int days = -1;
		from.setHours(0);
		from.setMinutes(0);
		from.setSeconds(0);
		to.setHours(0);
		to.setMinutes(0);
		to.setSeconds(0);
		days = (int) ((to.getTime() - from.getTime()) / (24 * 60 * 60 * 1000));
		return days;
	}

	/**
	 * 获取本周日的时间 格式 yyyy-MM -dd HH:mm:ss
	 * 
	 * </pre>
	 * 
	 * @throws Exception
	 */
	public static String getThisWeekEnd() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_WEEK, 6);
		cal.set(Calendar.DAY_OF_WEEK, 1);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return format(cal.getTime(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 获取当天剩余时间，单位：秒
	 * 
	 * @return
	 */
	public static final int getTodayRemainingTime() {
		return getTodayRemainingTime(0);
	}

	/**
	 * 获取N天至今剩余时间，单位：秒
	 * 
	 * @return
	 */
	public static final int getTodayRemainingTime(int days) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
		cal.add(Calendar.DATE, days);
		long tt = cal.getTimeInMillis();
		long ct = Calendar.getInstance().getTimeInMillis();
		return (int) ((tt - ct) / 1000);
	}

	/**
	 * 计算当前时间到下一个整点的秒差值
	 * 
	 * </pre>
	 */
	public static int getDifNowFromNextHour() {
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		return (60 - cal.get(Calendar.MINUTE)) * 60 - cal.get(Calendar.SECOND);
	}

	/**
	 * 获取当前小时点
	 * 
	 * <pre>
	 * Author: Wei Rongting
	 * &#64;return
	 * Modifications:
	 * Modifier rongting.wei; 2013-12-16; Create new Method getCurrHour
	 * </pre>
	 */
	public static int getCurrHour() {
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	/***
	 * 返回当前日期,yyyy-MM-dd
	 * 
	 * @author xianhua.meng
	 * @date 2014-3-2上午10:59:17
	 * @return
	 */
	public static String getCurrDate() {
		Date now = new Date();
		return format(now, defaultDatePattern);
	}

	/**
	 * 返回当前时间的秒数
	 * 
	 * </pre>
	 */
	public static long getCurrTime() {
		Date now = new Date();
		return (long) (now.getTime() / 1000);
	}

	/***
	 * 返回当前日期（加几天/减几天） yyyy-MM-dd（默认返回格式）
	 * 
	 * @date 2014-3-2上午10:59:17
	 * @return
	 */
	public static String addDay(int n, String format) {
		if ("".equals(format)) {
			format = defaultDatePattern;
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, n);
		return format(cal.getTime(), format);
	}

	/**
	 * 获取本周周日的时间
	 * 
	 * @return 周日时间 eg1: 当前时间2014-04-06 返回 2014-04-13 eg2: 当前时间2014-04-13 返回
	 *         2014-04-13
	 */
	public static String getSundayOfThisWeek() {
		Calendar c = Calendar.getInstance();
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayOfWeek == 0) {
			dayOfWeek = 7;
		}
		c.add(Calendar.DATE, -dayOfWeek + 7);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}

	/**
	 * 获取本周周日的时间,如果是周日则获取下周日时间.(redis过期时间专用)
	 * 
	 * @return 周日时间 eg1: 当前时间2014-04-07 返回 2014-04-13 eg1: 当前时间2014-04-13 返回
	 *         2014-04-20
	 */
	public static String getSundayOfExpire() {
		Calendar c = Calendar.getInstance();
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayOfWeek == 0) {
			dayOfWeek = 7;
			c.add(Calendar.DATE, dayOfWeek);
		} else {
			c.add(Calendar.DATE, -dayOfWeek + 7);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}

	/**
	 * 得到上个月的第一天
	 * 
	 * @date 2014-05-24下午13:36:51
	 * @return
	 */
	public static String getPrevMonthFirstDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);// 上个月
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));// 第一天
		return DateUtil.format(calendar.getTime(), "yyyy-MM-dd 00:00:00");
	}

	/**
	 * 得到上个月的最后一天
	 * 
	 * @date 2014-05-24下午13:36:51
	 * @return
	 */
	public static String getPrevMonthLastDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);// 上个月
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));// 最后一天
		return DateUtil.format(calendar.getTime(), "yyyy-MM-dd 23:59:59");
	}

	/** 获得上个月的月份(01~12)，不足两位前面补0 */
	public static String getLastMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);// 上个月
		int month = calendar.get(Calendar.MONTH) + 1;
		return month < 10 ? "0" + month : "" + month;
	}

	/** 获得本月的月份(01~12)，不足两位前面补0 */
	public static String getThisMonth() {
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH) + 1;
		return month < 10 ? "0" + month : "" + month;
	}

	/** 获得上上个月的月份(01~12)，不足两位前面补0 */
	public static String getLastLastMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -2);// 上个月
		int month = calendar.get(Calendar.MONTH) + 1;
		return month < 10 ? "0" + month : "" + month;
	}

	/** 获取现在时间到下下月一日凌晨之间的秒数 */
	public static int getSecondToDayofNextTwoMonth() {
		try {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, 2);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			return (int) (DateUtil.difNowTime(format(cal.getTime()), DateUtil.getDatePattern()) / 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/** 获取今天日期 */
	public static String getNow3() {
		Date now = new Date();
		return format(now, "yyyyMMdd");
	}

	/** 获取昨天日期 */
	public static String getYesterday() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		return format(cal.getTime(), "yyyyMMdd");
	}

	/** 获取现在时间到明天凌晨之间的秒数 */
	public static int getSecondToTomorrow() {
		try {
			return (int) (difNowTime(DateUtil.getTomorrow(), getDatePattern()) / 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String getDate(Timestamp time, String format) {
		String str = "";
		if ("".equals(format)) {
			format = DateUtil.getDatePattern();
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		str = sdf.format(time);
		return str;
	}

	/** 获取当前日期和小时 yyyyMMddHH */
	public static String getDayAndCurrentHour() {
		Date now = new Date();
		return format(now, "yyyyMMddHH");
	}

	/** 今天在今年的第几个周，周三算作一周的第一天 */
	public static int getWeekFlag() {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(4);
		return cal.get(Calendar.WEEK_OF_YEAR);
	}

	/** 今天的上周在今年的第几个周，周三算作一周的第一天 */
	public static int getLastWeekFlag() {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(4);
		cal.add(Calendar.DATE, -7);
		return cal.get(Calendar.WEEK_OF_YEAR);
	}

	/** 今天在今年的第几个月 */
	public static int getMonthFlag() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.MONTH);
	}

	/**
	 * 获取第二天的零点5秒
	 * 
	 * @return
	 */
	public static Date getNextDayZero() {
		Calendar cal = Calendar.getInstance();
		setTimeToZero(cal);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		return cal.getTime();
	}

	public static Date getNextSecond() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 5);
		return cal.getTime();
	}

	/**
	 * 是否是周一
	 * 
	 * @return
	 */
	public static boolean todayIsMonday() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
	}

	/**
	 * 是否是每个月的第一天
	 * 
	 * @return
	 */
	public static boolean todayIsFirstDayInMonth() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_MONTH) == 1;
	}

	private static void setTimeToZero(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 5);
		cal.set(Calendar.MILLISECOND, 0);
	}

	/** 获取当年本周的字符串(周一 ~周日),201516 */
	public static String getCurrentWeekStr() {
		String s = "0";
		try {
			Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setFirstDayOfWeek(Calendar.MONDAY);
			calendar.setTime(date);

			int y = calendar.get(Calendar.YEAR);
			int m = calendar.get(Calendar.MONTH) + 1;

			int w = calendar.get(Calendar.WEEK_OF_YEAR);

			if (w == 1 && m > 11) {// 一年中的最后几天,归为次年的第一个周期
				y = y + 1;
			}

			s = String.valueOf(y) + String.valueOf(w);
		} catch (Exception e) {
		}
		return s;
	}

	/** 获取当年上周的字符串(周一 ~周日),201515 */
	public static String getLastWeekStr() {
		String s = "0";
		try {
			s = getCurrentWeekStr();

			int y = Integer.parseInt(s.substring(0, 4));
			int w = Integer.parseInt(s.substring(4, s.length()));

			if (w == 1) {// 新年第一个周期
				y = y - 1;
				w = 52;
			} else {
				w--;
			}

			return String.valueOf(y) + String.valueOf(w);
		} catch (Exception e) {
			;
		}
		return s;
	}

	/** 返回格式为 yyyy-MM-dd的上周六的日期 */
	public static String getLastSaturday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_WEEK, -6);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		return format(cal.getTime());
	}

	/** 返回格式为 yyyy-MM的本月 */
	public static String getCurrentMonth() {
		Date now = new Date();
		return format(now, "yyyy-MM");
	}

	/** 返回格式为 yyyy-MM-dd的 上上周六 */
	public static String getBeforeLastSaturday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_WEEK, -13);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		return format(cal.getTime());
	}

	/**
	 * 获取到期日期的超时秒数,0点
	 * 
	 * @param day
	 * @return
	 */
	public static int getExpireDate(int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DAY_OF_MONTH, day);
		return (int) ((cal.getTime().getTime() - new Date().getTime()) / 1000);
	}

	public static void main(String[] args) throws ParseException {
		System.out.println(getExpireDate(10));
		System.out.println(getLastSaturday());
		System.out.println(getBeforeLastSaturday());
		System.out.println(getExpireDate(0));
		System.out.println(System.currentTimeMillis());
		System.out.println(difNowTime("2017-07-27 10:38:00", "yyyy-MM-dd hh:mm:ss"));
	}
}