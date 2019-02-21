/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.util.encoder.Base64;
import com.rh.core.util.encoder.UrlBase64;

/**
 * 这些帮助函数让 Java 的某些常用功能变得更简单
 * 
 * @author wanglong
 */
public class Lang {
    /** log */
    private static Log log = LogFactory.getLog(Lang.class);
    
    /**
     * 私有 构建体方法
     */
    private Lang() {
    }


    /**
     * 实例化指定名称的类，并返回实例对象。如果指定的类名不存在，或者不是指定接口的实现类，则返回null。
     * @param <T>
     * 
     * @param interfaceCls 指定接口，或抽象类
     * @param clsName 需要被初始化的类的名称。
     * @return 初始化类的实例对象
     */
    public static <T> T createObject(Class<T> interfaceCls, String clsName) {
        T obj = null;
        @SuppressWarnings("unchecked")
        Class<T> cls = (Class<T>) loadClass(clsName);
        if (cls == null) {
            throw new RuntimeException("实现类：" + clsName + "不存在！");
        } else if (!interfaceCls.isAssignableFrom(cls)) {
            throw new RuntimeException("实现类：" + clsName + "不是 " + interfaceCls.getName()
                    + " 接口的实现类.");
        } else {
            try {
                obj = cls.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException("实现类：" + clsName + "无法实例化！", e);
            }
        }
        return obj;
    }
    
    /**
     * 装载一个对象并实例化
     * @param clsName 类型
     * @return 实例化的对象
     */
    public static Object createObject(String clsName) {
        try {
            return loadClass(clsName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * 使用当前线程的ClassLoader加载给定的类
     * 
     * @param className 类的全称
     * @return 给定的类
     */
    public static Class<?> loadClass(String className) {
        if ((className == null) || className.trim().length() == 0) {
            log.error("loadClass error, className is null. " );
            return null;
        }
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e1) {
            try {
                return Class.forName(className);
            } catch (Exception e2) {
                throw new RuntimeException(e2.getMessage(), e2);
            }
        }
    }

    /**
     * 反射执行特定的方法
     * @param className 实体类名称
     * @param methodName 方法名
     * @param objs 参数信息
     */
    public static void doMethod(String className, String methodName, Object... objs) {
        doClassMethod(loadClass(className), methodName, objs);
    }

    /**
     * 反射执行特定的方法
     * @param cls 实体类
     * @param mtdName 方法名
     * @param objs 参数信息
     */
    public static void doClassMethod(Class<?> cls, String mtdName, Object... objs) {
        Method method = null;
        Object newClass = null;
        try {
            if (objs.length > 0) {
                @SuppressWarnings("rawtypes")
                Class[] classes = new Class[objs.length];
                for (int i = 0; i < objs.length; i++) {
                    classes[i] = objs[i].getClass();
                }
                
                method = cls.getMethod(mtdName, classes);
            } else {
                method = cls.getMethod(mtdName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (method != null) {
            try {
                newClass = cls.newInstance();
                if (objs.length > 0) {
                    method.invoke(newClass, objs);
                } else {
                    method.invoke(newClass);
                }
            } catch (Exception e) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) (e.getCause());
                } else {
                    throw new RuntimeException("执行方法[" + cls.getName() + "." + mtdName + "]错误", e);
                }
            }
        }
    }

    /**
     * 得到随机的22位UUID
     * @return 22位UUID
     */
    public static String getUUID() {
        StringBuilder sb = new StringBuilder("0");
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replaceAll("-", "");
        sb.append(uuid);
        uuid = hexTo64(sb.toString());
        uuid = uuid.replaceAll("_", RandomStringUtils.randomAlphanumeric(2));
        uuid = uuid.replaceAll("-", RandomStringUtils.randomAlphanumeric(2));
        return uuid;
    }

    /** 下面代码用于将36位的UUID字符串转为22位的字符串，提升系统运行效率 */
    private static final char[] CHAR_MAP;
    static {
        CHAR_MAP = new char[64];
        for (int i = 0; i < 10; i++) {
            CHAR_MAP[i] = (char) ('0' + i);
        }
        for (int i = 10; i < 36; i++) {
            CHAR_MAP[i] = (char) ('a' + i - 10);
        }
        for (int i = 36; i < 62; i++) {
            CHAR_MAP[i] = (char) ('A' + i - 36);
        }
        CHAR_MAP[62] = '_';
        CHAR_MAP[63] = '-';
    }

    /**
     * 将16进制字符串转换为64进制
     * @param hex 16进制字符串
     * @return 64进制字符串
     */
    private static String hexTo64(String hex) {
        StringBuilder r = new StringBuilder();
        int index = 0;
        final int size = 3;
        int[] buff = new int[size];
        int l = hex.length();
        for (int i = 0; i < l; i++) {
            index = i % size;
            buff[index] = Integer.parseInt("" + hex.charAt(i), 16);
            if (index == 2) {
                r.append(CHAR_MAP[buff[0] << 2 | buff[1] >>> 2]);
                r.append(CHAR_MAP[(buff[1] & size) << 4 | buff[2]]);
            }
        }
        return r.toString();
    }

    /**
     * 转型为整型
     * @param obj 原始对象
     * @param def 缺省值
     * 
     * @return 整型
     */
    public static int to(Object obj, int def) {
        if (obj != null) {
            if (obj instanceof Integer) {
                return (Integer) obj;
            } else if (obj instanceof Number) {
                return ((Number) obj).intValue();
            } else if (obj instanceof Boolean) {
                return (Boolean) obj ? 1 : 0;
            } else if (obj instanceof Date) {
                return (int) ((Date) obj).getTime();
            } else {
                try {
                    return Integer.parseInt(obj.toString());
                } catch (Exception e) {
                    try {
                        return (new Double(Double.parseDouble(obj.toString()))).intValue();
                    } catch (Exception e2) {
                        return def;
                    }
                }
            }
        } else {
            return def;
        }
    }
    
    /**
     * 转型为长整型
     * @param obj 原始对象
     * @param def 缺省值
     * 
     * @return 长整型
     */
    public static long to(Object obj, long def) {
        if (obj != null) {
            if (obj instanceof Long) {
                return (Long) obj;
            } else if (obj instanceof Number) {
                return ((Number) obj).longValue();
            } else if (obj instanceof Boolean) {
                return (Boolean) obj ? 1 : 0;
            } else if (obj instanceof Date) {
                return ((Date) obj).getTime();
            } else {
                try {
                    return Long.parseLong(obj.toString());
                } catch (Exception e) {
                    try {
                        return (new Double(Double.parseDouble(obj.toString()))).longValue();
                    } catch (Exception e2) {
                        return def;
                    }
                }
            }
        } else {
            return def;
        }
    }

    /**
     * 转型为双精度浮点型
     * @param obj 原始对象
     * @param def 缺省值
     * 
     * @return 双精度浮点型
     */
    public static double to(Object obj, double def) {
        if (obj != null) {
            if (obj instanceof Double) {
                return (Double) obj;
            } else if (obj instanceof Float) {
                return ((Float) obj).doubleValue();
            } else if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            } else if (obj instanceof Boolean) {
                return (Boolean) obj ? 1 : 0;
            } else if (obj instanceof Date) {
                return ((Date) obj).getTime();
            } else {
                try {
                    return Double.parseDouble(obj.toString());
                } catch (Exception e) {
                    return def;
                }
            }
        } else {
            return def;
        }
    }

    /**
     * 转型为布尔值
     * @param obj 原始对象
     * @param def 缺省值
     * 
     * @return 布尔值
     */
    public static boolean to(Object obj, boolean def) {
        if (obj != null) {
            if (obj instanceof Boolean) {
                return ((Boolean) obj).booleanValue();
            } else if (obj instanceof Integer) {
                return ((Integer) obj).intValue() == 1;
            } else if (obj instanceof Long) {
                return ((Long) obj).longValue() == 1;
            } else if (obj instanceof Double) {
                return ((Double) obj).doubleValue() == 1;
            } else if (obj instanceof Date) {
                return ((Date) obj).getTime() == 1;
            } else {
                String str = obj.toString().toUpperCase();
                return str.equalsIgnoreCase("TRUE") || str.equalsIgnoreCase("YES") || str.equals("1");
            }
        } else {
            return def;
        }
    }

    /**
     * 转型为字符串
     * @param obj 原始对象
     * @param def 缺省值
     * 
     * @return 字符串
     */
    public static String to(Object obj, String def) {
        if (obj != null) {
            return obj.toString();
        } else {
            return def;
        }
    }

    /**
     * 合并多个数组对象
     * @param <T> 范对象
     * @param arrays 多个数组对象
     * @return 合并后的新数组对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayAppend(T[]... arrays) {
        int size = 0;
        for (T[] arr : arrays) {
            size += arr.length;
        }
        T[] copy = (T[]) new Object[size];
        int pos = 0;
        for (T[] arr : arrays) {
            System.arraycopy(arr, 0, copy, pos, arr.length);
            pos = arr.length;
        }
        return copy;
    }

    /**
     * 把一个字符串数据转换为指定分隔符的字符串，如果数组为空返回空字符串，如果长度为1返回
     * 第一个数据内容。逗号分隔。
     * @param array 字符串数组
     * @return 转换后的字符串
     */
    public static String arrayJoin(String[] array) {
        return arrayJoin(array, Constant.SEPARATOR);
    }
    
    /**
     * 把一个字符串数据转换为指定分隔符的字符串，如果数组为空返回空字符串，如果长度为1返回
     * 第一个数据内容。
     * @param array 字符串数组
     * @param sep 分隔符
     * @return 转换后的字符串
     */
    public static String arrayJoin(String[] array, String sep) {
        if (array == null || array.length == 0) {
            return "";
        } else if (array.length == 1) {
            return array[0];
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    sb.append(array[i]).append(sep);
                }
            }
            sb.setLength(sb.length() - sep.length());
            return sb.toString();
        }
    }
    
    /**
     * 判断一个数据是否包含执行值
     * @param array 字符串数组
     * @param value 指定值
     * @return 是否包含指定值
     */
    public static boolean arrayHas(String[] array, String value) {
        boolean has = false;
        if ((array != null) && (array.length > 0) && (value != null)) {
            for (String data : array) {
                if (data.equals(value)) {
                    has = true;
                    break;
                }
            }
        }
        return has;
    }
    
    /**
     * 对数据进行base64编码
     * @param data 数据
     * @return 编码后的数据
     */
    public static byte[] encodeBase64(byte[] data) {
        return Base64.encode(data);
    }
    
    /**
     * 对数据进行base64编码
     * @param data 数据
     * @return 编码后的数据
     */
    public static String encodeBase64(String data) {
        return new String(Base64.encode(data.getBytes()));
    }
    
    /**
     * 对数据进行base64解码
     * @param data 数据
     * @return 解码后的数据
     */
    public static byte[] decodeBase64(byte[] data) {
        return Base64.decode(data);
    }
    
    /**
     * 对数据进行base64解码
     * @param data 数据
     * @return 解码后的数据
     */
    public static String decodeBase64(String data) {
        try {
            return new String(Base64.decode(data), "UTF-8");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        return data;
    }
    
    /**
     * 对数据进行url base64编码
     * @param data 数据
     * @return 编码后的数据
     */
    public static byte[] encodeUrlBase64(byte[] data) {
        return UrlBase64.encode(data);
    }
    
    /**
     * 对数据进行url base64编码
     * @param data 数据
     * @return 编码后的数据
     */
    public static String encodeUrlBase64(String data) {
        return new String(UrlBase64.encode(data.getBytes()));
    }
    
    /**
     * 对数据进行url base64解码
     * @param data 数据
     * @return 解码后的数据
     */
    public static byte[] decodeUrlBase64(byte[] data) {
        return UrlBase64.decode(data);
    }
    
    /**
     * 对数据进行url base64解码
     * @param data 数据
     * @return 解码后的数据
     */
    public static String decodeUrlBase64(String data) {
        return new String(UrlBase64.decode(data.getBytes()));
    }
    
    /**
     * 二进制字节转16进制字符串
     * @param b 数据
     * @return 转换后数据
     */
    public static String byteTohex(byte b) {
        String stmp = (java.lang.Integer.toHexString(b & 0XFF));
        if (stmp.length() == 1) {
            stmp = "0" + stmp;
        }
        return stmp;
    }
    
    /**
     * 二进制字节数组转16进制字符串
     * @param b 数据
     * @return 转换后数据
     */
    public static String byteTohex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < b.length; n++) {
            sb.append(byteTohex(b[n]));
        }
        return sb.toString();
    }

    /**
     * 十六进制字节数组转2进制数组
     * @param b 数据
     * @return 转换后的数据
     */
    public static byte[] hexTobyte(byte[] b) {
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }
    
    /**
     * 
     * @param hexStr 16进制字符串
     * @return 解码后的字符串
     */
    public static String hexToStr(String hexStr) {
        try {
            return new String(hexTobyte(hexStr.getBytes()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage() + ": " + hexStr, e);
            return "";
        }
    }
    
    /**
     * 执行js格式的条件表达式，返回表达式执行的结果
     * @param jsCondition js格式的条件表达式
     * @return 表达式条件执行结果，报错或者不满足条件返回false，满足条件返回true
     */
    public static boolean isTrueScript(String jsCondition) {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("rhino");
        try {
            Object jsResult = engine.eval(jsCondition);
            if (null == jsResult) {
                return false;
            }
            return jsResult.toString().equalsIgnoreCase("true");
        } catch (Exception e) {
            log.error(e.getMessage() + ": " + jsCondition, e);
            return false;
        }
    }
    
    /**
     * 执行脚本，返回执行的结果
     * @param script 脚本语句
     * @param def 缺省值
     * @return 执行结果
     */
    public static String runScript(String script, String def) {
        Object value = runScript(script);
        if (value != null) {
            return String.valueOf(value);
        } else {
            return def;
        }
    }
    /**
     * 执行脚本，返回执行的结果
     * @param script 脚本语句
     * @return 执行结果
     */
    public static Object runScript(String script) {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("JavaScript");
        Object out = null;
        try {
            out = engine.eval(script);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return out;
    }
    
    /**
     * 执行js脚本，返回执行的结果，调用java中对象统一通过$.get方式获取
     * @param context 上下文信息
     * @param script 脚本语句
     * @return 表达式条件执行结果
     */
    public static String runScript(Bean context, String script) {
        ScriptEngineManager sem = new ScriptEngineManager();
        
        Set<Object> keys = context.keySet();
        for (Object key : keys) {
            if (key instanceof String) {
                String strKey = (String) key;
                sem.put(strKey, context.get(key));
            }
        }
        
        ScriptEngine engine = sem.getEngineByName("JavaScript");
        String out = "";
        try {
            //engine.put("$", context);
            Object result = engine.eval(script);
            if (result != null) {
                out = String.valueOf(result);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return out;
    }
    
//    public static String runScript(Bean context, String script) {
//        ScriptEngineManager sem = new ScriptEngineManager();
//        ScriptEngine engine = sem.getEngineByName("JavaScript");
//        String out = "";
//        try {
//            engine.put("$", context);
//            Object result = engine.eval(script);
//            if (result != null) {
//                out = String.valueOf(result);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//        return out;
//    }
    

    /**
     * 格式化数据，调整小数位数为指定的长度
     * @param inputNumber 需要被格式化的数字
     * @param pattern 要格式化的数字格式，支持#号和%号 例如输入 10000 #0.00 10000.00 # 10000 #,##0.00 10,000.00
     * @see java.text.DecimalFormat
     * @return 格式化后的结果
     */
    public static String formatNumber(String inputNumber, String pattern) {
        try {
            DecimalFormat nf = new DecimalFormat(pattern);
            return nf.format(Double.parseDouble(inputNumber));
        } catch (Exception e) {
            log.error("formatNumber error:" + inputNumber + " pattern:" + pattern, e);
            return inputNumber;
        }
    }

    /**
     * 根据指定小数位数格式化数据
     * 
     * @param inputNumber 需要被格式化的数字
     * @param dec 要保留的小数位数
     * 
     * @return 格式化后的结果
     */
    public static String formatNumber(String inputNumber, int dec) {
        StringBuilder pattern = new StringBuilder();
        pattern.append("0.");

        for (int i = 0; i < dec; i++) {
            pattern.append("0");
        }

        inputNumber = formatNumber(inputNumber, pattern.toString());

        if (dec == 0) {
            return inputNumber.substring(0, inputNumber.length() - 1);
        } else {
            return inputNumber;
        }
    }
    
    /**
     * 判断对象是否为数字
     * 
     * @param inputNumber 需要被格式化的数字
     * @return 是否为数字
     */
    public static boolean isNum(Object inputNumber) {
        if (inputNumber == null) {
            return false;
        } else {
            return inputNumber instanceof Number;
        }
    }
    
    /**
     * 返回Object对象列表
     * @param values 对象数组，支持多个
     * @return 对象列表
     */
    public static List<Object> asList(Object... values) {
        List<Object> list = new ArrayList<Object>();
        Collections.addAll(list, values);
        return list;
    }

    /**
     * 删除input字符串中的html格式
     * 
     * @param input - source
     * @param length - length
     * @return - no html tag summary
     */
    public static String getSummary(String input, int length) {
        if (input == null || input.trim().equals("")) {
            return "";
        }
        input = input.replace("\n", "");
        input = input.replace("\t", "");
        input = input.replace(" ", "");
        // 去掉所有html元素,
        String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(
                "<[^>]*>", "");
        str = str.replaceAll("[(/>)<]", "");
        int len = str.length();
        if (len <= length) {
            return str;
        } else {
            str = str.substring(0, length);
            str += "......";
        }
        return str;
    }
    
    
    /**
     * subString 
     * @param text - text
     * @param preTag - prefix str
     * @param postTag - post str
     * @return target content
     */
    public static String subString(String text, String preTag, String postTag) {
        String result = "";
        String pn = preTag + "(.+?)" + postTag;
        Pattern pattern = Pattern.compile(pn);
        Matcher mt = pattern.matcher(text);
        while (mt.find()) {
            result = mt.group(1);
        }
        return result;
    }
    
    
    /**
     * get file md5 check sum
     * 
     * @param is - input stream
     * @return md5 string
     * @throws NoSuchAlgorithmException - md5 checksum exception
     * @throws IOException - io exception
     */
    public static String getMd5checksum(InputStream is) throws NoSuchAlgorithmException, IOException {
        byte[] digest = createChecksum(is);
        String result = "";
        for (int i = 0; i < digest.length; i++) {
            result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     * create md5 checksum
     * 
     * @param fis - inputStream
     * @return checksum string
     * @throws NoSuchAlgorithmException - md5 checksum exception
     * @throws IOException - io exception
     */
    private static byte[] createChecksum(InputStream fis) throws NoSuchAlgorithmException, IOException {
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }
    
    /**
     * 获取mac地址
     * @return mac地址
     */
    public static String getMAC() {
        Enumeration<NetworkInterface> el;
        StringBuilder sbMac = new StringBuilder();
        try {
            el = NetworkInterface.getNetworkInterfaces();
            while (el.hasMoreElements()) {
                NetworkInterface ni = el.nextElement();
                byte[] mac = ni.getHardwareAddress();
                if (mac == null || mac.length == 0 || !ni.supportsMulticast()) { //只取有效的网卡
                    continue;
                }
                sbMac.append(byteTohex(mac[0])).append("-").append(byteTohex(mac[1])).append("-")
                    .append(byteTohex(mac[2])).append("-").append(byteTohex(mac[3])).append("-")
                    .append(byteTohex(mac[4])).append("-").append(byteTohex(mac[5])).append(" ");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return sbMac.toString();
    }
}
