package com.rh.core.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音操作工具类
 */

public class PinyinUtils {
    
    /**
     * 将字符串转换成拼音字符串
     * 
     * @param src 字符串
     * @return 拼音数组
     */
    public static String getPinyinStr(String src) {
        return Lang.arrayJoin(getPinyin(src), "");
    }

    /**
     * 将字符串转换成拼音数组
     * 
     * @param src 字符串
     * @return 拼音数组
     */
    public static String[] getPinyin(String src) {
        return getPinyin(src, false, null);
    }

    /**
     * 将字符串转换成拼音数组
     * 
     * @param src 字符串
     * @param separator 多音字拼音之间的分隔符
     * @return 拼音数组
     */
    public static String[] getPinyin(String src, String separator) {

        return getPinyin(src, true, separator);
    }

    /**
     * 将字符串转换成拼音数组
     * 
     * @param src 字符串
     * @param isPolyphone 是否查出多音字的所有拼音
     * @param separator 多音字拼音之间的分隔符
     * @return 拼音数组
     */
    public static String[] getPinyin(String src, boolean isPolyphone, String separator) {
        // 判断字符串是否为空
        if ("".equals(src) || null == src) {
            return null;
        }
        char[] srcChar = src.toCharArray();
        int srcCount = srcChar.length;
        String[] srcStr = new String[srcCount];

        for (int i = 0; i < srcCount; i++) {
            srcStr[i] = charToPinyin(srcChar[i], isPolyphone, separator);
        }
        return srcStr;
    }

    /**
     * 将单个字符转换成拼音
     * 
     * @param src 字符
     * @param isPolyphone 是否查出多音字的所有拼音
     * @param separator 多音字拼音之间的分隔
     * @return 拼音
     */
    public static String charToPinyin(char src, boolean isPolyphone, String separator) {
        // 创建汉语拼音处理类
        HanyuPinyinOutputFormat defaultFormat = new
                HanyuPinyinOutputFormat();
        // 输出设置，大小写，音标方式
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        StringBuffer tempPinying = new StringBuffer();

        
        if (src == 12288 || src == 32) { //忽略中英文空格
            return "";
        } else if (src > 128) { // 如果是中文
            try {
                // 转换得出结果
                String[] strs = PinyinHelper.toHanyuPinyinStringArray(src, defaultFormat);
                if (strs == null) {
                    tempPinying.append(src);
                } else {
                    // 是否查出多音字，默认是查出多音字的第一个字符
                    if (isPolyphone && null != separator) {
                        for (int i = 0; i < strs.length; i++) {
                            tempPinying.append(strs[i]);
                            if (strs.length != (i + 1)) {
                                // 多音字之间用特殊符号间隔起来
                                tempPinying.append(separator);
                            }
                        }
                    } else {
                        tempPinying.append(strs[0]);
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        } else if (src > 32){ //忽略空格等特殊字符
            tempPinying.append(src);
        }

        return tempPinying.toString();

    }

    /**
     * 取汉字字符的首字母
     * @param src 字符
     * @param isCapital 是否是大写
     * @return 首字母拼音
     */
    public static char[] getHeadByChar(char src, boolean isCapital) {
        if (src == 12288 || src == 32) { //忽略中英文空格
            return null;
        }
        // 如果不是汉字直接返回
        if (src <= 128) {
            return new char[]{ src };
        }
        // 获取所有的拼音
        String[] pinyingStr = PinyinHelper.toHanyuPinyinStringArray(src);
        if (pinyingStr == null) {
            return new char[]{ src };
        }
        // 创建返回对象
        int polyphoneSize = pinyingStr.length;
        char[] headChars = new char[polyphoneSize];
        int i = 0;
        // 截取首字符
        for (String s : pinyingStr) {
            char headChar = s.charAt(0);
            // 首字母是否大写，默认是小写
            if (isCapital) {
                headChars[i] = Character.toUpperCase(headChar);
            } else {
                headChars[i] = headChar;
            }
            i++;
        }

        return headChars;
    }

    /**
     * 取汉字的首字母(默认是小写)
     * @param src 字符
     * @return 首字母拼音
     */
    public static char[] getHeadByChar(char src) {
        return getHeadByChar(src, false);
    }

    /**
     * 查找字符串首字母，缺省大写，多音字返回第一个
     * @param src 字符串
     * @return 首字母拼音
     */
    public static String getHeadStr(String src) {
        return Lang.arrayJoin(getHead(src), "");
    }
    
    /**
     * 查找字符串首字母，缺省大写，多音字返回第一个
     * @param src 字符串
     * @return 首字母拼音
     */
    public static String[] getHead(String src) {
        return getHead(src, false);
    }

    /**
     * 查找字符串首字母
     * @param src 字符串
     * @param isCapital 是否大写
     * @return 首字母拼音
     */
    public static String[] getHead(String src, boolean isCapital) {
        return getHead(src, isCapital, null);
    }

    /**
     * 查找字符串首字母
     * @param src 字符串
     * @param isCapital 是否大写
     * @param separator 多音字分隔符
     * @return 首字母拼音
     */
    public static String[] getHead(String src, boolean isCapital, String separator) {
        char[] chars = src.toCharArray();
        String[] headString = new String[chars.length];
        int i = 0;
        for (char ch : chars) {

            char[] chs = getHeadByChar(ch, isCapital);
            if (chs != null) {
                StringBuffer sb = new StringBuffer();
                if (null != separator) {
                    int j = 1;
    
                    for (char ch1 : chs) {
                        sb.append(ch1);
                        if (j != chs.length) {
                            sb.append(separator);
                        }
                        j++;
                    }
                } else {
                    sb.append(chs[0]);
                }
                headString[i] = sb.toString();
                i++;
            }
        }
        return headString;
    }

    /**
     * 测试方法
     * @param args 参数
     */
    public static void main(String[] args) {
        System.out.println(getHeadStr("吴艳（办）"));
        System.out.println(getPinyinStr("吴艳（办）"));
    }

}
