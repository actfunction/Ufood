package com.rh.api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;

/**
 * 常量类
 * 
 * @author zhangym
 *
 */
public class DataStatsUtil {

	private static final String ORG_SERV = "SY_ORG_DEPT";

	public static float divFunc(float a, float b) {
		float rtn = 0;
		BigDecimal num = null;
		if (a > 0 && b > 0) {
			num = new BigDecimal(String.valueOf(a / b));
			num = num.divide(BigDecimal.ONE, 1, BigDecimal.ROUND_CEILING);
			rtn = num.floatValue();
		}
		if (rtn <= 0) {
			rtn = 0;
		}
		return rtn;
	}

	public static float mulFunc(float a, float b) {
		float rtn = 0;
		BigDecimal num = null;
		if (a > 0 && b > 0) {
			num = new BigDecimal(String.valueOf(a * b));
			num = num.setScale(1, BigDecimal.ROUND_HALF_DOWN);
			rtn = num.floatValue();
		}
		return rtn;
	}

	public static int parseInt(String num) {
		int n;
		if (num == null || num.length() <= 0 || num.equalsIgnoreCase("null")) {
			n = 0;
		} else {
			n = Integer.parseInt(num);
		}
		return n;
	}

	public static double parseDouble(String num) {
		double n;
		if (num == null || num.length() <= 0 || num.equalsIgnoreCase("null")) {
			n = 0;
		} else {
			n = Double.parseDouble(num);
		}
		return n;
	}

	public static float parseFloat(String num) {
		float n;
		if (num == null || num.length() <= 0 || num.equalsIgnoreCase("null")) {
			n = 0;
		} else {
			n = Float.parseFloat(num);
		}
		return n;
	}

	public static float sumFunc(float a, float b) {
		BigDecimal a1 = new BigDecimal(String.valueOf(a));
		BigDecimal b1 = new BigDecimal(String.valueOf(b));
		BigDecimal c = a1.add(b1);
		return c.floatValue();
	}

	/**
	 * 计算百分比
	 * 
	 * @param num
	 * @param total
	 * @param scale
	 * @return
	 */
	public static String getPercent(double num, double total, int scale) {
		if (total == 0.00 || num == 0.00) {
			return "0";
		}
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(scale);
		df.setRoundingMode(RoundingMode.HALF_UP);
		double accuracy_num = num / total * 100;
		return df.format(accuracy_num);
	}

	/**
	 * 获取部门下处室列表
	 * 
	 * @param deptCode
	 * @return
	 */
	public static List<Bean> getDeptList(String deptCode) {
		Bean queryBean = new Bean();
		queryBean.set("TDEPT_CODE", deptCode);
		return ServDao.finds(ORG_SERV, queryBean);
	}

	/**
	 * 求平均数
	 * 
	 * @param num
	 * @param total
	 * @param sacle
	 * @return
	 */
	public static String getAvg(Double num, Double total, int sacle) {
		if (total == 0.00 || num == 0.00) {
			return "";
		}
		return String.format("%." + sacle + "f", num / total);
	}

	/**
	 * 获取本周第一天
	 * 
	 * @return
	 */
	public static Date getWeekFirstDay() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return cal.getTime();
	}

	/**
	 * 获取本周最后一天
	 * 
	 * @return
	 */
	public static Date getWeekLastDay() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getWeekFirstDay());
		cal.add(Calendar.DAY_OF_WEEK, 7);
		return cal.getTime();
	}

	/**
	 * 获取指定日期下一天时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getNextDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		date = calendar.getTime();
		return date;
	}

	/**
	 * 日期格式字符串转成日期
	 * 
	 * @param dateStr
	 * @return
	 */
	public static Date strToDate(String dateStr) {
		SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sim.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 得到指定月的天数
	 * 
	 * @return
	 */
	public static int getMonthDays(String dateStr) {
		int year = Integer.parseInt(dateStr.substring(0, 4));
		int month = Integer.parseInt(dateStr.substring(5, 7));
		Calendar a = Calendar.getInstance();
		a.set(Calendar.YEAR, year);
		a.set(Calendar.MONTH, month - 1);
		a.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		a.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
		int maxDate = a.get(Calendar.DATE);
		return maxDate;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static String division(int a, int b) {
		String result = "";
		float num = (float) a / b;

		DecimalFormat df = new DecimalFormat("0.0");
		df.setRoundingMode(RoundingMode.CEILING);

		result = df.format(num);

		return result;

	}

	public static String getNum(double a) {
		String result = "";

		DecimalFormat df = new DecimalFormat("0.0");
		df.setRoundingMode(RoundingMode.CEILING);

		result = df.format(a);

		return result;

	}
}
