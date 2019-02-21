package com.rh.core.util.var;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量替换管理类
 * @author wanglong
 * @version $Id$
 */
public class VarMgr {
	
    /**
     * 变量替换，适用缺省采用@分隔的系统变量
     * @param src 源字符串
     * @param var 变量
     * @return 替换结果字符串
     */
    public static String replace(String src, Var var) {
        return replace(src, "@", var);
    }
    
    /**
     * 变量替换，适用指定分隔符的系统变量
     * @param src 源字符串
     * @param varSep 变量分隔符
     * @param var 变量
     * @return 替换结果字符串
     */
    public static String replace(String src, String varSep, Var var) {
        String pn = varSep + "((\\w|_|[\u4e00-\u9fa5])*)" + varSep;
        Pattern pattern = Pattern.compile(pn, Pattern.CASE_INSENSITIVE); //不区分大小写
        Matcher mt = pattern.matcher(src);
        StringBuffer sb = new StringBuffer();
        while (mt.find()) {
            mt.appendReplacement(sb, var.get(mt.group(1)));
        }
        mt.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 变量替换，适用指定分隔符的系统变量
     * @param src 源字符串
     * @return 替换结果字符串
     */
	public static String replaceSysVar(String src) {
	    String pn = "@(\\w|_|[\u4e00-\u9fa5])*@";
        Pattern pattern = Pattern.compile(pn, Pattern.CASE_INSENSITIVE); //不区分大小写
        Matcher mt = pattern.matcher(src);
        StringBuffer sb = new StringBuffer();
        while (mt.find()) {
            String key = mt.group(0);
            if (key.startsWith("@DATE")) {
                mt.appendReplacement(sb, DateVar.getInst().get(key));
            } else if (key.startsWith("@C_")) { //系统配置
                mt.appendReplacement(sb, ConfVar.getInst().get(key));
            } else {
                mt.appendReplacement(sb, OrgVar.getInst().get(key));
            }
        }
        mt.appendTail(sb);
        return sb.toString();
	}
	
	/**
	 * 获取组织用户相关的的系统变量
	 * @return 组织用户相关系统变量
	 */
	public static Map<String, String> getOrgMap() {
	    return OrgVar.getInst().getMap();
	}
	
	/**
     * 获取系统配置相关的的系统变量
     * @return 系统配置相关系统变量
     */
    public static Map<String, String> getConfMap() {
        return ConfVar.getInst().getMap();
    }
    
    /**
     * 获取日期相关的的系统变量
     * @return 日期相关系统变量
     */
    public static Map<String, String> getDateMap() {
        return DateVar.getInst().getMap();
    }
}
