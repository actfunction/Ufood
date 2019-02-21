/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.rh.core.util.Constant;
import com.rh.core.util.lang.ListHandler;
import com.rh.core.util.lang.ValidCallback;

/**
 * 处理Bean的一些辅助方法。
 * 
 * @author wanglong
 * @version $Id$
 */
public class BeanUtils {
    /** 键值对应的正则pattern */
    public static final String KEY_PATTERN = "#((\\w|_|[\u4e00-\u9fa5])*)#";
   
    /**
     * 私有构建体方法，防止被实例化
     */
    private BeanUtils() {
    }
    
   /**
    * 将在一个数据bean中所有属性键值的内容传递到另外一个bean
    * @param src   源bean
    * @param tar   目标bean
    */
   public static void trans(Bean src, Bean tar)  {
       trans(src, tar, null);
   }
   
    /**
     * 将在数组中设定属性键值的内容传递到另外一个bean
     * @param src	源bean
     * @param tar   目标bean
     * @param keys	键值数组 null表示传全部src中的数据
     */
    public static void trans(Bean src, Bean tar, String[] keys)  {
    	if (keys != null) {
	    	for (String key : keys) {
	    		tar.set(key, src.get(key));
	    	}
    	} else {
    		tar.putAll(src);
    	}
    }
    
    /**
     * 将bean的list转为以Id为键的hash
     * @param <T> 范对象
     * @param list	bean的list
     * @return	以Id为键的hash
     */
    public static <T> HashMap<Object, Bean> listToHash(List<Bean> list) {
    	HashMap<Object, Bean> hash = new HashMap<Object, Bean>();
    	for (Bean t : list) {
    		hash.put(t.getId(), t);
    	}
    	return hash;
    }
    
    /**
     * 将列表转为以指定键值为键的带顺序的map，供列表和基于键值的双重调用。
     * @param dataList  List类型的bean列表
     * @param keyName 键值字段的名称
     * @return 带顺序的Map
     */
    public static LinkedHashMap<String, Bean> toLinkedMap(List<Bean> dataList, String keyName) {
        return toLinkedMap(dataList, keyName, null);
    }
    /**
     * 将列表转为以指定键值为键的带顺序的map，供列表和基于键值的双重调用。
     * @param dataList  List类型的bean列表
     * @param keyName 键值字段的名称
     * @param qc 有效性检查回调方法
     * @return 带顺序的Map
     */
    public static LinkedHashMap<String, Bean> toLinkedMap(List<Bean> dataList, String keyName, 
            ValidCallback qc) {
        LinkedHashMap<String, Bean> linkMap = new LinkedHashMap<String, Bean>(dataList.size());
        for (Bean data : dataList) {
            if ((qc == null) || qc.valid(data)) {
                linkMap.put(data.getStr(keyName), data);
            }
        }
        return linkMap;
    }
    
    /**
     * 将LinkedHashMap转为以带顺序的List。
     * @param dataList  LinkedHashMap类型的bean列表
     * @return 带顺序的List
     */
    public static List<Bean> toList(LinkedHashMap<String, Bean> dataList) {
        List<Bean> list = new ArrayList<Bean>(dataList.size());
        for (String key : dataList.keySet()) {
            list.add(dataList.get(key));
        }
        return list;
    }
    
    /**
     * 遍历列表的数据
     * @param list 列表数据
     * @param ls 遍历处理器
     */
    public static void handelList(List<Bean> list, ListHandler<Bean> ls) {
        for (Bean data : list) {
            ls.handle(data);
        }
    }
    
    /**
     * 遍历列表的数据
     * @param list 列表数据
     * @param ls 遍历处理器
     */
    public static void handelSet(Set<Bean> list, ListHandler<Bean> ls) {
        for (Bean data : list) {
            ls.handle(data);
        }
    }
    
    /**
     * 将两个列表基于给定的键值进行合并，生成一个新的列表
     * @param oldList 原列表
     * @param newList 要合并入的列表
     * @param key 键值
     * @return 一个合并后的新列表
     */
    public static List<Bean> mergeList(List<Bean> oldList, List<Bean> newList, String key) {
        return mergeList(oldList, newList, key, null);
    }
    
    /**
     * 将两个列表基于给定的键值进行合并，生成一个新的列表
     * @param oldList 原列表
     * @param newList 要合并入的列表
     * @param key 键值字段
     * @param sortField 排序字段,null表示不排序
     * @return 一个合并后的新列表
     */
    public static List<Bean> mergeList(List<Bean> oldList, List<Bean> newList, String key, 
            final String sortField) {
        List<Bean> list = new  ArrayList<Bean>();
        list.addAll(oldList); //先装载全部原始数据
        List<Bean> more = new ArrayList<Bean>();
        for (Bean nItem : newList) {
            int index = -1;
            for (int i = 0; i < oldList.size(); i++) {
                Bean oItem = oldList.get(i);
                if (nItem.getStr(key).equals(oItem.getStr(key))) { //用新的item
                    list.set(i, nItem);
                    index = i;
                }
            }
            if (index < 0) { //没有匹配上的存起来
                more.add(nItem);
            }
        }
        list.addAll(more); //将新列表中没有匹配上的合并入结果列表
        if ((sortField != null) && (sortField.length() > 0)) { //排序
            sort(list, sortField);
        }
        return list;
    }
    
    /**
     * 排序列表，按照Bean中指定字段（数字型）由小到达排列
     * @param list 列表
     * @param sortField 排序字段
     */
    public static void sort(List<Bean> list, final String sortField) {
        Collections.sort(list, new Comparator<Bean>() {
            public int compare(Bean item1, Bean item2) {
                if (item1.getInt(sortField) > item2.getInt(sortField)) {
                    return 1;
                } else if (item1.getInt(sortField) < item2.getInt(sortField)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }
    
    /**
     * 排序列表，按照Bean中指定字段（字符型）由小到达排列
     * @param list 列表
     * @param sortField 排序字段（字符型）
     */
    public static void sortStr(List<Bean> list, final String sortField) {
        Collections.sort(list, new Comparator<Bean>() {
            public int compare(Bean item1, Bean item2) {
                if (item1.getStr(sortField).compareTo(item2.getStr(sortField)) > 0) {
                    return 1;
                } else if (item1.getStr(sortField).compareTo(item2.getStr(sortField)) < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }   
    
    /**
     * 合并两个数据bean，生成一个新的包含两个数据bean最新的数据
     * @param oldBean 原数据bean
     * @param newBean 需要被合入的数据bean，如果有相同的键值，则此数据bean将替代原数据bean中的数据
     * @return 合并后的新数据bean
     */
    public static Bean mergeBean(Bean oldBean, Bean newBean) {
        Bean mergeBean;
        if (oldBean != null) {
            mergeBean = oldBean.copyOf();
            trans(newBean, mergeBean); //传递新值
        } else {
            mergeBean = newBean.copyOf();
        }
        return mergeBean;
    }
    
    /**
     * 对原始字符串中以##包含的字段名称进行替换，替换的值来自于数据bean中的数据，替换规则为键值对照。
     * 例如src为：“你好，#TEST_NAME#”，bean中TEST_NAME键值为"world"，替换后为：“你好，world”
     * @param src 需要被替换的字符串
     * @param dataBean 包含替换数据的bean
     * @return 替换后的字符串
     */
    public static String replaceValues(String src, Bean dataBean) {
        Pattern pattern = Pattern.compile(KEY_PATTERN, Pattern.CASE_INSENSITIVE); //不区分大小写
        Matcher mt = pattern.matcher(src);
        StringBuffer sb = new StringBuffer();
        while (mt.find()) {
            String temp = dataBean.getStr(mt.group(1));
            temp = StringUtils.replace(temp, "$", "\\$");
            mt.appendReplacement(sb, temp);
        }
        mt.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 如果原始字符串中以##包含的字段名称在dataBean中存在数据，则返回true,否则返回false。
     * @param src 需要处理的字符串
     * @param dataBean 数据bean
     * @return 数据中是否存在字符串中设定的键值
     */
    public static boolean containsValue(String src, Bean dataBean) {
        if (src == null || src.length() == 0) { //如果src不存在，直接返回false
            return false;
        }
        Pattern pattern = Pattern.compile(KEY_PATTERN, Pattern.CASE_INSENSITIVE); //不区分大小写
        Matcher mt = pattern.matcher(src);
        boolean result = false;
        while (mt.find()) {
            String key = mt.group(1);
            int pos = key.indexOf("__NAME"); //支持字典名称模式
            if (pos > 0) {
                key = key.substring(0, pos);
            }
            if (dataBean.contains(key)) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    /**
     * 对原始字符串中以##包含的字段名称进行抓取，对于抓取的结果生成逗号分隔的字符串列表
     * @param src 需要被抓取的字符串
     * @return 键值字符串列表，逗号分隔
     */
    public static String getFieldCodes(String src) {
        Pattern pattern = Pattern.compile(KEY_PATTERN, Pattern.CASE_INSENSITIVE); //不区分大小写
        Matcher mt = pattern.matcher(src);
        StringBuilder sb = new StringBuilder();
        while (mt.find()) {
            String value = mt.group(1);
            int pos = value.indexOf("__NAME");
            if (pos > 0) {
                value = value.substring(0, pos);
            }
            sb.append(value).append(Constant.SEPARATOR);
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}