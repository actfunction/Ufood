package com.rh.gw.gdjh.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rh.gw.gdjh.exception.BaseException;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 日期公用类
 */
public class DateUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

    /**
     * 默认生效时间 -- 今天
     */
    public static final String DEFAULT_EFFECTIVE_DATE = getChar10();
    /**
     * 默认失效时间 -- 2999-12-31
     */
    public static final String DEFAULT_EXPIRATION_DATE = "2999-12-31";

    /** yyyyMMddHHmmss时间格式 */
    public static final String FORMAT14 = "yyyyMMddHHmmss";

    /** yyyyMMddHHmmssSSS时间格式 */
    public static final String FORMAT17 = "yyyyMMddHHmmssSSS";

    /** yyyy-MM-dd HH:mm:ss时间格式 */
    public static final String FORMAT19 = "yyyy-MM-dd HH:mm:ss";

    /** yyyy-MM-dd HH:mm:ss时间格式 */
    public static final String FORMAT21 = "yyyy-MM-dd HH:mm:ss.S";

    /** yyyy-MM-dd HH:mm:ss.SSS时间格式 */
    public static final String FORMAT23 = "yyyy-MM-dd HH:mm:ss.SSS";

    /** yyyy-MM-dd HH:mm:ss.SSSSS时间格式 */
    public static final String FORMAT25 = "yyyy-MM-dd HH:mm:ss.SSSSS";

    /** yyyy-MM-dd时间格式 */
    public static final String FORMAT_DAY = "yyyy-MM-dd";

    /** yyyyMMdd HH:mm:ss时间格式 */
    public static final String FORMAT_DATE_TIME_17 = "yyyyMMdd HH:mm:ss";
    /** yyyy-MM-dd时间格式 */
    public static final String FORMAT_DATE_8 = "yyyyMMdd";

    /** yyyy-MM-dd时间格式 */
    public static final String FORMAT_TIME_6 = "HHmmss";

    public static final String DATE_PATTERN = "\\d{4}\\-\\d{1,2}\\-\\d{1,2}";

    public static final String TIMESTAMP_PATTERN_MM = "\\d{4}\\-\\d{1,2}\\-\\d{1,2} \\d{1,2}\\:\\d{1,2}\\:\\d{1,2}.\\d{1} CST";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String TIME_PATTERN = "\\d{1,2}\\:\\d{1,2}\\:\\d{1,2}";

    public static final String TIME_FORMAT = "HH:mm:ss";

    public static final String TIMESTAMP_PATTERN = "\\d{4}\\-\\d{1,2}\\-\\d{1,2} \\d{1,2}\\:\\d{1,2}\\:\\d{1,2}";

    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 工具类是静态成员的集合，注定不会被实例化。因此，不应该有公共的构造函数。 所以这里定义一个私有的构造函数，限制其实例化。
     */
    private DateUtils() {
    }

    public static String getChar10() {
        return DateFormatUtils.format(new Date(), "yyyy-MM-dd");
    }
    
    /**
     * 获取当前8位格式日期
     */
    public static String getChar8() {
        return DateFormatUtils.format(new Date(), "yyyyMMdd");
    }

    /**
     * 将长时间格式时间转换为字符串
     * 
     * @param dateDate
     * @return
     */
    public static String dateToStrLong(java.util.Date dateDate, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(dateDate);
    }

    /**
     * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     * 
     * @param strDate
     * @return
     */
    public static Date strToDateLong(String strDate, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        return formatter.parse(strDate, pos);
    }

    public static String formatDateString(String dateString, String sourceFormat, String targetFormat) {
        String returnString = null;
        try {
            returnString = DateUtils.dateToStrLong(
                    org.apache.commons.lang.time.DateUtils.parseDate(dateString, new String[] { sourceFormat }),
                    targetFormat);
        } catch (ParseException e) {
            LOGGER.error("formatDateString error:", e);
        }
        return returnString;
    }

    /**
     * 
     * 功能描述: <br>
     * 不同日期字符串转换成日期对象 〈功能详细描述〉
     * 
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String dateStr) throws ParseException {
        Pattern pattern = Pattern.compile(DATE_PATTERN);
        Matcher matcher = pattern.matcher(dateStr);
        SimpleDateFormat sdf;
        Date date = null;

        Pattern patternMM = Pattern.compile(TIMESTAMP_PATTERN_MM);

        Matcher matcherMM = patternMM.matcher(dateStr);
        if (matcher.matches()) {
            sdf = new SimpleDateFormat(DATE_FORMAT);
            date = sdf.parse(dateStr);
        } else if (matcherMM.matches()) {
            sdf = new SimpleDateFormat(DATE_FORMAT);
            date = sdf.parse(dateStr);
        } else {
            pattern = Pattern.compile(TIME_PATTERN);
            matcher = pattern.matcher(dateStr);
            if (matcher.matches()) {
                sdf = new SimpleDateFormat(TIME_FORMAT);
                date = sdf.parse(dateStr);
            } else {
                pattern = Pattern.compile(TIMESTAMP_PATTERN);
                matcher = pattern.matcher(dateStr);
                if (matcher.matches()) {
                    sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
                    date = sdf.parse(dateStr);
                }
            }
        }

        return date;
    }

    public static Date parseDate(String dateStr, String dateFormat) throws ParseException {
        SimpleDateFormat sdf;
        if (dateFormat == null) {
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        } else {
            sdf = new SimpleDateFormat(dateFormat);
        }

        return sdf.parse(dateStr);
    }

    /**
     * 获取指定日期 向前或向后滚动特定分钟后的日期
     * 
     * @param dateNow String 当前日期
     * @param rollMinute String 待滚动的分钟
     * @return String 指定日期 +/- 特定天数 后的日期（格式yyyy-MM-dd HH:mm:ss）
     */
    public static String rollHour(String dateNow, int rollHour) {
        String dateReturn = "";
        if (dateNow == null || dateNow.trim().length() < 19) {
            return dateReturn;
        }
        try {
            SimpleDateFormat sf = new SimpleDateFormat(FORMAT19);
            dateReturn = sf.format(addHour(sf.parse(dateNow.trim()), rollHour));
        } catch (ParseException e) {
            throw new BaseException("DateUtils.rollMinute方法异常", e);
        }
        return dateReturn;
    }

    /**
     * 指定时间向前或者向后挪动rollMinute分钟
     * 
     * @param date
     * @param rollMinute
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static Date rollMinute(Date date, int rollMinute) {

        if (date == null) {
            return null;
        }

        String dateNow = dateToStrLong(date, FORMAT19);
        String dateReturn;

        try {
            SimpleDateFormat sf = new SimpleDateFormat(FORMAT19);
            Calendar c = Calendar.getInstance();
            c.setTime(sf.parse(dateNow));
            c.add(Calendar.MINUTE, rollMinute);
            dateReturn = sf.format(c.getTime());
        } catch (ParseException e) {
            throw new BaseException("DateUtils.rollMinute方法异常", e);
        }
        return strToDateLong(dateReturn, FORMAT19);
    }

    public static String getChar19() {
        return DateFormatUtils.format(new Date(), FORMAT19);
    }

    public static Date addHour(Date src, int rollHour) {
        Calendar c = Calendar.getInstance();
        c.setTime(src);
        c.add(Calendar.HOUR_OF_DAY, rollHour);

        return c.getTime();
    }

    public static String formatChar10(String char8) {
        if (char8 == null || char8.length() == 0) {
            return char8;
        }
        return char8.substring(0, 4) + "-" + char8.substring(4, 6) + "-" + char8.substring(6);
    }

    public static String formatChar14(String char19) {
        if (char19 == null || char19.length() == 0) {
            return char19;
        }
        return char19.substring(0, 4) + char19.substring(5, 7) + char19.substring(8, 10) + char19.substring(11, 13)
                + char19.substring(14, 16) + char19.substring(17);
    }

    /**
     * 时间克隆方法 如果时间对象为数据库时间格式 则返回数据库时间格式的对象
     * 
     * @param date
     * @return
     */
    public static Date clone(Date date) {
        if (date == null) {
            return null;
        }

        if (date instanceof Timestamp) {
            return new Timestamp(date.getTime());
        }

        return new Date(date.getTime());
    }

    /**
     * 
     * 功能描述: 获取相对于当前时间一段时间的指定时间<br>
     * 〈功能详细描述〉
     * 
     * @param field
     * @param amount
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static Date getDate(int field, int amount) {
        return getDate(new Date(), field, amount);
    }

    public static Date getDate(Date date, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * 得到当前日期
     */
    public static Date getDate() {
        return new Date();
    }

    /**
     * 
     * 功能描述: 获取格式化的日期<br>
     * 〈功能详细描述〉
     * 
     * @param date
     * @param format
     * @return
     * @see [相关类/方法](可选)
     * @since 2.4.0
     */
    public static Date getDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        return sdf.parse(sdf.format(date), pos);
    }
    
    /**
     * 得到最大日期
     */
    public static Date getMaxDate() {
        Date maxDate = null;
        try {
            maxDate = parseDate("9999", "yyyy");
        } catch (ParseException e) {
            LOGGER.error("得到最大日期失败！",e);
        }
        return maxDate;
    }

}
