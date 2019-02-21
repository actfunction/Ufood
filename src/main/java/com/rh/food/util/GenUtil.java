package com.rh.food.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.pentaho.di.core.util.StringUtil;

import com.rh.core.base.Bean;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;

public class GenUtil {
	private static AtomicInteger count = new AtomicInteger();
	private static final String DEFAULT_DIGITS = "0";

	private static final String DEFAULT_PREFIX = "";// 前缀
	private static final int DEFAULT_LENGTH = 4;
	private static final int DEFAULT_TYPE = 1;// 默认类型0序号,1表自增id

	/**
	 * 传入相应的前缀生成自增的ID、序号
	 * 
	 * @param end 上一次结束的ID编号后四位数字
	 * @return 补充后的结果
	 */
	public static String getQualityNum(int end) {
		return getQualityNum(DEFAULT_PREFIX, DEFAULT_LENGTH, DEFAULT_DIGITS, end, 0);
	}

	/**
	 * 传入相应的前缀生成自增的ID、序号
	 * 
	 * @param prefix 传入前缀
	 * @param end    上一次结束的ID编号后四位数字
	 * @return 补充后的结果
	 */
	public static String getQualityNum(String prefix, int end) {
		return getQualityNum(prefix, DEFAULT_LENGTH, DEFAULT_DIGITS, end, DEFAULT_TYPE);
	}

	/**
	 * 传入相应的前缀生成自增的ID、序号
	 *
	 * @param prefix 传入前缀
	 * @param length 需要补充到的位数
	 * @param add    需要补充的数字, 补充默认数字[0]
	 * @param end    上一次结束的ID编号
	 * @return 生成自增的ID
	 */
	public static String getQualityNum(String prefix, int length, String add, int end, int type) {

		count.set(end);
		count.incrementAndGet();
		Integer i1 = count.get();
		StringBuffer sb = new StringBuffer();
		sb.append(prefix);
		if (DEFAULT_TYPE == type) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			sb.append(sdf.format(new Date()));
		}

		int idlen = String.valueOf(i1).length();// 自增后ID编号长度
		if (idlen > length) {
			i1 = 1;
			idlen = String.valueOf(i1).length();
		}
		for (int i = 1; i <= length - idlen; i++) {
			sb.append(add);
		}
		sb.append(i1);
		// //进行拼接并返回
		return sb.toString();
	}

	/**
	 * 生成自增的ID
	 *
	 * @param autoId id
	 * @return 生成自增的ID后四位
	 */
	public static String generateNum(String servName, String pkcol, String ordercol, String prefix) {
		SqlBean sqlBean = new SqlBean();
		sqlBean.selects(pkcol);
		sqlBean.and("rownum", 1);
		sqlBean.desc(ordercol);
		Bean result = (Bean) ServDao.find(servName, sqlBean);

		if (null != result && !StringUtil.isEmpty((String) result.get(pkcol))) {
			String autoId = (String) result.get(pkcol);
			int end = Integer.valueOf(autoId.substring(autoId.length() - 4, autoId.length()));
			return getQualityNum(prefix, end);
		} else {
			return getQualityNum(prefix, 0);// 从0001开始
		}

	}


	/** 
	 *       传入相应的前缀生成自增的ID
	 *
	 * @param prefix 传入前缀
	 * @param seqName 序列名
	 * @param colName 别名
	 * @return 生成自增的ID
	 */
	public static String  getAutoIdNumner(String prefix,String seqName,String colName) {
		Bean result=Transaction.getExecutor().queryOne("SELECT  '"+prefix+"'||to_char(sysdate,'YYYYMMDD')||lpad("+seqName+".NEXTVAL,9,'0') as "+colName+"  from dual");
       if(null != result) {
       	return (String) result.get(colName);
       }
		return null;
	} 

}
