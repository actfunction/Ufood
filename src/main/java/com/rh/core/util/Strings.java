/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * 字符串操作的帮助函数
 * 
 */
public abstract class Strings {

    /**
     * 复制字符串
     * 
     * @param cs 字符串
     * @param num 数量
     * @return 新字符串
     */
    public static String dup(CharSequence cs, int num) {
        if (isEmpty(cs) || num <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(cs.length() * num);
        for (int i = 0; i < num; i++) {
            sb.append(cs);
        }
        return sb.toString();
    }

    /**
     * 复制字符
     * 
     * @param c 字符
     * @param num 数量
     * @return 新字符串
     */
    public static String dup(char c, int num) {
        if (c == 0 || num < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder(num);
        for (int i = 0; i < num; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 将字符串首字母大写
     * 
     * @param s 字符串
     * @return 首字母大写后的新字符串
     */
    public static String capitalize(CharSequence s) {
        if (null == s) {
            return null;
        }
        int len = s.length();
        if (len == 0) {
            return "";
        }
        char char0 = s.charAt(0);
        if (Character.isUpperCase(char0)) {
            return s.toString();
        }
        return new StringBuilder(len).append(Character.toUpperCase(char0)).append(s.subSequence(1, len))
                .toString();
    }

    /**
     * 将字符串首字母小写
     * 
     * @param s 字符串
     * @return 首字母小写后的新字符串
     */
    public static String lowerFirst(CharSequence s) {
        if (null == s) {
            return null;
        }
        int len = s.length();
        if (len == 0) {
            return "";
        }
        char c = s.charAt(0);
        if (Character.isLowerCase(c)) {
            return s.toString();
        }
        return new StringBuilder(len).append(Character.toLowerCase(c)).append(s.subSequence(1, len))
                .toString();
    }

    /**
     * 检查两个字符串的忽略大小写后是否相等.
     * 
     * @param s1 字符串A
     * @param s2 字符串B
     * @return true 如果两个字符串忽略大小写后相等,且两个字符串均不为null
     */
    public static boolean equalsIgnoreCase(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equalsIgnoreCase(s2);
    }

    /**
     * 检查两个字符串是否相等.
     * 
     * @param s1 字符串A
     * @param s2 字符串B
     * @return true 如果两个字符串相等,且两个字符串均不为null
     */
    public static boolean equals(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

    /**
     * 判断字符串是否以特殊字符开头
     * 
     * @param s 字符串
     * @param c 特殊字符
     * @return 是否以特殊字符开头
     */
    public static boolean startsWithChar(String s, char c) {
        return null != s ? (s.length() == 0 ? false : s.charAt(0) == c) : false;
    }

    /**
     * 判断字符串是否以特殊字符结尾
     * 
     * @param s 字符串
     * @param c 特殊字符
     * @return 是否以特殊字符结尾
     */
    public static boolean endsWithChar(String s, char c) {
        return null != s ? (s.length() == 0 ? false : s.charAt(s.length() - 1) == c) : false;
    }

    /**
     * @param cs 字符串
     * @return 是不是为空字符串
     */
    public static boolean isEmpty(CharSequence cs) {
        return null == cs || cs.length() == 0;
    }

    /**
     * @param cs 字符串
     * @return 是不是为空白字符串
     */
    public static boolean isBlank(CharSequence cs) {
        if (null == cs) {
            return true;
        }
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!(Character.isWhitespace(cs.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 去掉字符串前后空白
     * 
     * @param cs 字符串
     * @return 新字符串
     */
    public static String trim(CharSequence cs) {
        if (null == cs) {
            return null;
        }
        if (cs instanceof String) {
            return ((String) cs).trim();
        }
        int length = cs.length();
        if (length == 0) {
            return cs.toString();
        }
        int l = 0;
        int last = length - 1;
        int r = last;
        for (; l < length; l++) {
            if (!Character.isWhitespace(cs.charAt(l))) {
                break;
            }
        }
        for (; r > l; r--) {
            if (!Character.isWhitespace(cs.charAt(r))) {
                break;
            }
        }
        if (l > r) {
            return "";
        } else if (l == 0 && r == last) {
            return cs.toString();
        }
        return cs.subSequence(l, r + 1).toString();
    }

    /**
     * 将字符串按半角逗号，拆分成数组，空元素将被忽略
     * 
     * @param s 字符串
     * @return 字符串数组
     */
    public static String[] splitIgnoreBlank(String s) {
        return Strings.splitIgnoreBlank(s, ",");
    }

    /**
     * 根据一个正则式，将字符串拆分成数组，空元素将被忽略
     * 
     * @param s 字符串
     * @param regex 正则式
     * @return 字符串数组
     */
    public static String[] splitIgnoreBlank(String s, String regex) {
        if (null == s) {
            return null;
        }
        String[] ss = s.split(regex);
        List<String> list = new LinkedList<String>();
        for (String st : ss) {
            if (isBlank(st)) {
                continue;
            }
            list.add(trim(st));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 将一个整数转换成最小长度为某一固定数值的十进制形式字符串
     * 
     * @param d 整数
     * @param width 宽度
     * @return 新字符串
     */
    public static String fillDigit(int d, int width) {
        return Strings.alignRight(String.valueOf(d), width, '0');
    }

    /**
     * 将一个整数转换成最小长度为某一固定数值的十六进制形式字符串
     * 
     * @param d 整数
     * @param width 宽度
     * @return 新字符串
     */
    public static String fillHex(int d, int width) {
        return Strings.alignRight(Integer.toHexString(d), width, '0');
    }

    /**
     * 将一个整数转换成最小长度为某一固定数值的二进制形式字符串
     * 
     * @param d 整数
     * @param width 宽度
     * @return 新字符串
     */
    public static String fillBinary(int d, int width) {
        return Strings.alignRight(Integer.toBinaryString(d), width, '0');
    }

    /**
     * 将一个整数转换成固定长度的十进制形式字符串
     * 
     * @param d 整数
     * @param width 宽度
     * @return 新字符串
     */
    public static String toDigit(int d, int width) {
        return Strings.cutRight(String.valueOf(d), width, '0');
    }

    /**
     * 将一个整数转换成固定长度的十六进制形式字符串
     * 
     * @param d 整数
     * @param width 宽度
     * @return 新字符串
     */
    public static String toHex(int d, int width) {
        return Strings.cutRight(Integer.toHexString(d), width, '0');
    }

    /**
     * 将一个整数转换成固定长度的二进制形式字符串
     * 
     * @param d 整数
     * @param width 宽度
     * @return 新字符串
     */
    public static String toBinary(int d, int width) {
        return Strings.cutRight(Integer.toBinaryString(d), width, '0');
    }

    /**
     * 保证字符串为一固定长度。超过长度，切除，否则补字符。
     * 
     * @param s 字符串
     * @param width 长度
     * @param c 补字符
     * @return 修饰后的字符串
     */
    public static String cutRight(String s, int width, char c) {
        if (null == s) {
            return null;
        }
        int len = s.length();
        if (len == width) {
            return s;
        }
        if (len < width) {
            return Strings.dup(c, width - len) + s;
        }
        return s.substring(len - width, len);
    }

    /**
     * 在字符串左侧填充一定数量的特殊字符
     * 
     * @param cs 字符串
     * @param width 字符数量
     * @param c 字符
     * @return 新字符串
     */
    public static String alignRight(CharSequence cs, int width, char c) {
        if (null == cs) {
            return null;
        }
        int len = cs.length();
        if (len >= width) {
            return cs.toString();
        }
        return new StringBuilder().append(dup(c, width - len)).append(cs).toString();
    }

    /**
     * 在字符串右侧填充一定数量的特殊字符
     * 
     * @param cs 字符串
     * @param width 字符数量
     * @param c 字符
     * @return 新字符串
     */
    public static String alignLeft(CharSequence cs, int width, char c) {
        if (null == cs) {
            return null;
        }
        int length = cs.length();
        if (length >= width) {
            return cs.toString();
        }
        return new StringBuilder().append(cs).append(dup(c, width - length)).toString();
    }

    /**
     * @param cs 字符串
     * @param lc 左字符
     * @param rc 右字符
     * @return 字符串是被左字符和右字符包裹 -- 忽略空白
     */
    public static boolean isQuoteByIgnoreBlank(CharSequence cs, char lc, char rc) {
        if (null == cs) {
            return false;
        }
        int len = cs.length();
        if (len < 2) {
            return false;
        }
        int l = 0;
        int last = len - 1;
        int r = last;
        for (; l < len; l++) {
            if (!Character.isWhitespace(cs.charAt(l))) {
                break;
            }
        }
        if (cs.charAt(l) != lc) {
            return false;
        }
        for (; r > l; r--) {
            if (!Character.isWhitespace(cs.charAt(r))) {
                break;
            }
        }
        return l < r && cs.charAt(r) == rc;
    }

    /**
     * @param cs 字符串
     * @param lc 左字符
     * @param rc 右字符
     * @return 字符串是被左字符和右字符包裹
     */
    public static boolean isQuoteBy(CharSequence cs, char lc, char rc) {
        if (null == cs) {
            return false;
        }
        int length = cs.length();
        return length > 1 && cs.charAt(0) == lc && cs.charAt(length - 1) == rc;
    }

    /**
     * 获得一个字符串集合中，最长串的长度
     * 
     * @param coll 字符串集合
     * @return 最大长度
     */
    public static int maxLength(Collection<? extends CharSequence> coll) {
        int re = 0;
        if (null != coll) {
            for (CharSequence s : coll) {
                if (null != s) {
                    re = Math.max(re, s.length());
                }
            }
        }
        return re;
    }

    /**
     * 获得一个字符串数组中，最长串的长度
     * @param <T> 字符串数组
     * @param array 字符串数组
     * @return 最大长度
     */
    public static <T extends CharSequence> int maxLength(T[] array) {
        int re = 0;
        if (null != array) {
            for (CharSequence s : array) {
                if (null != s) {
                    re = Math.max(re, s.length());
                }
            }
        }
        return re;
    }

    /**
     * 截去第一个字符
     * <p>
     * 比如:
     * <ul>
     * <li>removeFirst("12345") => 2345
     * <li>removeFirst("A") => ""
     * </ul>
     * 
     * @param str 字符串
     * @return 新字符串
     */
    public static String removeFirst(CharSequence str) {
        if (str == null) {
            return null;
        }
        if (str.length() > 1) {
            return str.subSequence(1, str.length()).toString();
        }
        return "";
    }

    /**
     * 如果str中第一个字符和 c一致,则删除,否则返回 str
     * <p>
     * 比如:
     * <ul>
     * <li>removeFirst("12345",1) => "2345"
     * <li>removeFirst("ABC",'B') => "ABC"
     * <li>removeFirst("A",'B') => "A"
     * <li>removeFirst("A",'A') => ""
     * </ul>
     * 
     * @param str 字符串
     * @param c 第一个个要被截取的字符
     * @return 新字符串
     */
    public static String removeFirst(String str, char c) {
        return (Strings.isEmpty(str) || c != str.charAt(0)) ? str : str.substring(1);
    }

    /**
     * 判断一个字符串数组是否包括某一字符串
     * @param <T> 实际数组类型
     * @param ss 对象数组
     * @param s 对象
     * @return 是否包含
     */
    public static <T> boolean isin(T[] ss, T s) {
        if (null == ss || null == s || ss.length == 0) {
            return false;
        }
        for (T w : ss) {
            if (s.equals(w)) {
                return true;
            }
        }
        return false;
    }

    /** 邮件的正则定义 */
    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)" 
                    + "|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");

    /**
     * 检查一个字符串是否为合法的电子邮件地址
     * 
     * @param input 需要检查的字符串
     * @return true 如果是有效的邮箱地址
     */
    public static final boolean isEmail(CharSequence input) {
        return EMAIL_PATTERN.matcher(input).matches();
    }

    /**
     * 将一个字符串由驼峰式命名变成分割符分隔单词
     * 
     * <pre>
     *  lowerWord("helloWorld", '-') => "hello-world"
     * </pre>
     * 
     * @param cs 字符串
     * @param c 分隔符
     * 
     * @return 转换后字符串
     */
    public static String lowerWord(CharSequence cs, char c) {
        StringBuilder sb = new StringBuilder();
        int len = cs.length();
        for (int i = 0; i < len; i++) {
            char ch = cs.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    sb.append(c);
                }
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * 将一个字符串某一个字符后面的字母变成大写，比如
     * 
     * <pre>
     *  upperWord("hello-world", '-') => "helloWorld"
     * </pre>
     * 
     * @param cs 字符串
     * @param c 分隔符
     * 
     * @return 转换后字符串
     */
    public static String upperWord(CharSequence cs, char c) {
        StringBuilder sb = new StringBuilder();
        int len = cs.length();
        for (int i = 0; i < len; i++) {
            char ch = cs.charAt(i);
            if (ch == c) {
                do {
                    i++;
                    if (i >= len) {
                        return sb.toString();
                    }
                    ch = cs.charAt(i);
                } while (ch == c);
                sb.append(Character.toUpperCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * 将一个字符串出现的HMTL元素进行转义，比如
     * 
     * <pre>
     *  escapeHtml("&lt;script&gt;alert("hello world");&lt;/script&gt;") 
     *  => "&amp;lt;script&amp;gt;alert(&amp;quot;hello world&amp;quot;);&amp;lt;/script&amp;gt;"
     * </pre>
     * 
     * 转义字符对应如下
     * <ul>
     * <li>& => &amp;amp;
     * <li>< => &amp;lt;
     * <li>>=> &amp;gt;
     * <li>' => &amp;#x27;
     * <li>" => &amp;quot;
     * </ul>
     * 
     * @param cs 字符串
     * 
     * @return 转换后字符串
     */
    public static String escapeHtml(CharSequence cs) {
        if (null == cs) {
            return null;
        }
        char[] cas = cs.toString().toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : cas) {
            switch (c) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '\'':
                sb.append("&#x27;");
                break;
            case '\\':
                sb.append("&#92;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            case ' ':
                sb.append("&nbsp;");
                break;
            case '\n':
                sb.append("<br>");
                break;
            case '\r':
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * 将html转换成标准字符输出
     * 例如："123&nbsp;abc" = "123 abc"
     * @param htmlString 需要转换的html字符串
     * @return 转换后的标准字符串
     */
    public static String unescapeHtml(String htmlString) {
        return StringEscapeUtils.unescapeHtml(htmlString);
    }
    
    /**
     * 
     * @param cs 文件名
     * @return 去掉文件名中的特殊字符。
     */
    public static String escapeFilenameSepcChar(CharSequence cs) {
        if (null == cs) {
            return null;
        }
        char[] cas = cs.toString().toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : cas) {
            switch (c) {
            case '*':
                break;
            case '<':
                sb.append("《");
                break;
            case '>':
                sb.append("》");
                break;
            case ':':
                sb.append("：");
                break;
            case '\\':
                break;
            case '/':
                break;
            case '|':
                break;
            case '?':
                sb.append("？");
                break;
            case '\"':
                break;
            case '\n':
                break;
            case '\r':
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * 用新字符串替换旧字符串.
     *
     * @param line the String to search to perform replacements on
     * @param oldString the String that should be replaced by newString
     * @param newString the String that will replace all instances of oldString
     *
     * @return a String will all instances of oldString replaced by newString
     */
    public static final String replace(String line, String oldString, String newString) {
        if (line == null) {
            return null;
        }
        int i = 0;
        i = line.indexOf(oldString, i);
        if (i >= 0) {
            char[] line2 = line.toCharArray();
            char[] newString2 = newString.toCharArray();
            int oLength = oldString.length();
            StringBuffer buf = new StringBuffer(line2.length);
            buf.append(line2, 0, i).append(newString2);
            i += oLength;
            int j = i;
            while ((i = line.indexOf(oldString, i)) > 0) {
                buf.append(line2, j, i - j).append(newString2);
                i += oLength;
                j = i;
            }
            buf.append(line2, j, line2.length - j);
            return buf.toString();
        }
        return line;
    }

    /**
     * 用新字符串替换行中所有的旧的字符串 ， 忽略大小写
     * added feature that matches of newString in oldString ignore case.
     *
     * @param line the String to search to perform replacements on
     * @param oldString the String that should be replaced by newString
     * @param newString the String that will replace all instances of oldString
     *
     * @return a String will all instances of oldString replaced by newString
     */
    public static final String replaceIgnoreCase(String line, String oldString, String newString) {
        if (line == null) {
            return null;
        }
        String lcLine = line.toLowerCase();
        String lcOldString = oldString.toLowerCase();
        int i = 0;
        i = lcLine.indexOf(lcOldString, i);
        if (i >= 0) {
            char[] line2 = line.toCharArray();
            char[] newString2 = newString.toCharArray();
            int oLength = oldString.length();
            StringBuffer buf = new StringBuffer(line2.length);
            buf.append(line2, 0, i).append(newString2);
            i += oLength;
            int j = i;
            while ((i = lcLine.indexOf(lcOldString, i)) > 0) {
                buf.append(line2, j, i - j).append(newString2);
                i += oLength;
                j = i;
            }
            buf.append(line2, j, line2.length - j);
            return buf.toString();
        }
        return line;
    }
    
    /**
     * 从原始字符串中获取分隔符前第一部分的字符串，缺省使用","作为分隔符
     * @param src 源字符串
     * @return 处理后的字符串
     */
    public static final String getFirstBySep(String src) {
        return getFirstBySep(src, Constant.SEPARATOR);
    }
    
    /**
     * 从原始字符串中获取分隔符前第一部分的字符串，如果找不到分隔符，则返回全部字符串
     * @param src 源字符串
     * @param sep 分隔符
     * @return 处理后的字符串
     */
    public static final String getFirstBySep(String src, String  sep) {
        int pos = src.indexOf(sep);
        if (pos == 0) {
            return "";
        } else if (pos > 0) {
            return src.substring(0, pos);
        } else {
            return  src;
        }
    }
    
    /**
     * 将对象数组转换为指定分隔符进行分隔的字符串
     * @param objs 对象数组
     * @param sep 分隔符号
     * @return 逗号分隔字符串
     */
    public static final String toString(Object[] objs, String sep) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : objs) {
            sb.append(obj).append(sep);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
    
    /**
     * 将对象数组转换为逗号进行分隔的字符串
     * @param objs 对象数组
     * @return 逗号分隔字符串
     */
    public static final String toString(Object[] objs) {
        return toString(objs, ",");
    }
    
    /**
     * 将对象数组转换为指定分隔符进行分隔的字符串
     * @param objs 对象数组
     * @param sep 分隔符号
     * @return 逗号分隔字符串
     */
    public static final String toString(List<String> objs, String sep) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : objs) {
            sb.append(obj).append(sep);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - sep.length());
        }
        return sb.toString();
    }
    
    /**
     * 将对象数组转换为逗号进行分隔的字符串
     * @param objs 对象数组
     * @return 逗号分隔字符串
     */
    public static final String toString(List<String> objs) {
        return toString(objs, ",");
    }

    /**
     * 去除重复的字符串
     * @param src 源字符串数组
     * @return 去除重复后的字符串数组
     */
    public static String[] removeSame(String[] src) {
        List<String> out = new LinkedList<String>();
        for (String s : src) {
            if (!out.contains(s)) {
                out.add(s);
            }
        }
        return (String[]) out.toArray();
    }
    
    /**
     * 去除字符串列表中的重复数据，采用逗号作为分隔符号
     * @param src 源字符串数组
     * @return 去除重复后的字符串列表
     */
    public static String removeSame(String src) {
        return removeSame(src, Constant.SEPARATOR);
    }
    
    /**
     * 去除字符串列表中的重复数据
     * @param src 源字符串数组
     * @param sep 分隔符号
     * @return 去除重复后的字符串列表
     */
    public static String removeSame(String src, String sep) {
        String[] srcs = src.split(sep);
        List<String> out = new LinkedList<String>();
        StringBuilder sb = new StringBuilder();
        for (String s : srcs) {
            if (!out.contains(s)) {
                out.add(s);
                sb.append(s).append(sep);
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
    
    /**
     * 去除字符串列表中指定的数据，采用逗号作为分隔符号
     * @param src 源字符串数组
     * @param value 需要被去除的值
     * @return 去除重复后的字符串列表
     */
    public static String removeValue(String src, String value) {
        return removeValue(src, value, Constant.SEPARATOR);
    }
    
    /**
     * 去除字符串列表中的指定的数据
     * @param src 源字符串数组
     * @param value 需要被去除的值
     * @param sep 分隔符
     * @return 去除重复后的字符串列表
     */
    public static String removeValue(String src, String value, String sep) {
        if (src.length() == 0 || value.length() == 0) {
            return src;
        }
        String[] srcs = src.split(sep);
        String[] values = value.split(sep);
        StringBuilder sb = new StringBuilder();
        for (String s : srcs) {
            if (!Lang.arrayHas(values, s)) {
                sb.append(s).append(sep);
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
    
    /**
     * 在字符串列表中增加指定的数据，逗号分隔
     * @param src 源字符串数组
     * @param value 需要被增加的值
     * @return 增加指定值后的字符串列表
     */
    public static String addValue(String src, String value) {
        return addValue(src, value, Constant.SEPARATOR);
    }
    
    /**
     * 在字符串列表中增加指定的数据
     * @param src 源字符串数组
     * @param value 需要被增加的值
     * @param sep 分隔符
     * @return 增加指定值后的字符串列表
     */
    public static String addValue(String src, String value, String sep) {
        if (src.length() == 0) {
            return value;
        } else {
            return src + sep + value;
        }
    }
    
    /**
     * 在字符串列表中是否包含指定的数据
     * @param src 源字符串数组
     * @param value 指定值
     * @return 是否包含
     */
    public static boolean containsValue(String src, String value) {
        return containsValue(src, value, Constant.SEPARATOR);
    }
    
    /**
     * 在字符串列表中是否包含指定的数据
     * @param src 源字符串数组
     * @param value 指定值
     * @param sep 分隔符
     * @return 是否包含
     */
    public static boolean containsValue(String src, String value, String sep) {
        if (src.length() == 0 || value.length() == 0) {
            return false;
        } else {
            return (sep + src + sep).indexOf(sep + value + sep) >= 0;
        }
    }
    
    /**
     * 数字中文
     */
	static final String[] CN_NUM = { "零", "一", "二", "三", "四", "五", "六", "七",
			"八", "九" };
	/**
	 * 单位 中文
	 */
	static final String[][] CN_UNIT = { { "", "十", "百", "千" },
			{ "万", "憶", "兆", "吉" } };

	/**
	 * 
	 * @param num 数字 eg: 123
	 * @return 中文数字  eg: 一百二十三
	 */
	public static String toCnNum(String num) {

		String chkNum = num.replaceAll("^[0]*(.*?)", "$1")
				.replaceAll("\\s+", "").replaceAll(",", "");

		int len = chkNum.length() % 4 == 0 ? chkNum.length() / 4 : chkNum
				.length() / 4 + 1;

		String[] group = new String[len];
		for (int i = chkNum.length() - 4, j = len - 1; j >= 0; i -= 4, j--) {
			group[j] = chkNum.substring(Math.max(0, i), i + 4);
		}
		// System.out.println(Arrays.toString(group));

		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < len; i++) {
			int glen = group[i].length();
			for (int j = 0, k = 0; j < glen; j++) {
				char c = group[i].charAt(j);
				if (c > '0') {
					if (j == 0
							&& i > 0
							&& group[i - 1].charAt(group[i - 1].length() - 1) == '0') { // 修改这里
						buf.append(CN_NUM[0]);
					} else if (k < j) {
						buf.append(CN_NUM[0]);
					}
					if (j == 2 && c == '1') {
						buf.append(CN_UNIT[0][glen - j - 1]);
					} else {
						buf.append(CN_NUM[(int) (c - '0')]).append(
								CN_UNIT[0][glen - j - 1]);
					}
					k++;
				}
			}

			if (i < len - 1 && Integer.valueOf(group[i]) > 0) {
				buf.append(CN_UNIT[1][len - 2 - i]);
			}
		}

		return buf.toString();
	}
	
    /**
     * 将一个字符串出现的尖括号进行转义
     * 
     * 转义字符对应如下
     * <ul>
     * <li>< => &amp;lt;
     * <li>>=> &amp;gt;
     * </ul>
     * 
     * @param cs 字符串
     * 
     * @return 转换后字符串
     */
    public static String escapeAngle(CharSequence cs) {
        if (null == cs) {
            return null;
        }
        char[] cas = cs.toString().toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : cas) {
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * 去掉不可见的Ascii字符避免前台出现错误
     * @param srcStr 原始字符串
     * @return 移除不可见字符
     */
    public static String removeInvisibleChar(String srcStr) {
        //除tab \n\r之外，去掉其它字符
        final String regex = "[\\x00-\\x09\\x0b\\x0c\\x0e-\\x1f\\x7f]";
        
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        
        Matcher mt = pattern.matcher(srcStr);
        StringBuffer sb = new StringBuffer();
        while (mt.find()) {
            mt.appendReplacement(sb, "");
        }
        mt.appendTail(sb);
        return sb.toString();
    }   
}
