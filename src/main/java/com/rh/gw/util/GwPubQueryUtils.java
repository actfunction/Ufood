package com.rh.gw.util;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;

import com.rh.gw.gdjh.util.DateUtils;


/**
 * 公文公共查询方法工具类
 * 
 * @author kfzx-linll
 * @date 2018/12/21
 */
public class GwPubQueryUtils {
	// 记录历史
	private static Log log = LogFactory.getLog(GwPubQueryUtils.class);
    
	// 公文主数据表
	private static final String OA_GW_GONGWEN = "OA_GW_GONGWEN";
	
	// 意见表
	private static final String SY_COMM_MIND = "SY_COMM_MIND";
	
	
	/**
	 * 公文查询方法，提供给归档使用
	 * 
	 * @param dataId 公文主数据的唯一ID
	 * @return String类型日期 YYYYMMDD
	 */
	public String gwQueryForGD(String dataId) {
		// 公文主数据表
		Bean bean = ServDao.find(OA_GW_GONGWEN, dataId);
		
		String time = "";
		
		switch (bean.getStr("TMPL_CODE")) {
			case "OA_GW_GONGWEN_ICBC_XZFW" :
				// 行政公文:最后一次签发时间
				if ("GW_SIGN_TIME".equals(bean.getStr("GW_SIGN_TIME")) || null != bean.getStr("GW_SIGN_TIME")) {
					Date date = DateUtils.strToDateLong(bean.getStr("GW_SIGN_TIME"), DateUtils.FORMAT_DATE_8);
					time = DateFormatUtils.format(date, DateUtils.FORMAT_DATE_8);
				} else {
					time = DateFormatUtils.format(DateUtils.getDate(), DateUtils.FORMAT_DATE_8);
				}
				
				log.debug("行政公文的最后一次签发时间：" + time);
				return time;
			case "OA_GW_GONGWEN_ICBC_YWFW" :
				/**
				 * 业务公文:取最后一次签发时间; 终止的话没有签发时间,取办结时间
				 */
				if ("4".equals(bean.getStr("S_FLAG"))) {
					// 业务公文被终止,终止没有签发时间,取办结时间
					if ("GW_END_TIME".equals(bean.getStr("GW_END_TIME")) || null != bean.getStr("GW_END_TIME")) {
						Date date = DateUtils.strToDateLong(bean.getStr("GW_END_TIME"), DateUtils.FORMAT_DATE_8);
						time = DateFormatUtils.format(date, DateUtils.FORMAT_DATE_8);
					} else {
						time = DateFormatUtils.format(DateUtils.getDate(), DateUtils.FORMAT_DATE_8);
					}
				} else {
					// 业务公文没有被终止,取最后一次签发时间
					if ("GW_SIGN_TIME".equals(bean.getStr("GW_SIGN_TIME")) || null != bean.getStr("GW_SIGN_TIME")) {
						Date date = DateUtils.strToDateLong(bean.getStr("GW_SIGN_TIME"), DateUtils.FORMAT_DATE_8);
						time = DateFormatUtils.format(date, DateUtils.FORMAT_DATE_8);
					} else {
						time = DateFormatUtils.format(DateUtils.getDate(), DateUtils.FORMAT_DATE_8);
					}
				}
				log.debug("业务公文的最后一次签发时间" + time);
				return time;
			case "OA_GW_GONGWEN_ICBCSW" :
				// 收文:成文时间
				if ("GW_CW_TIME".equals(bean.getStr("GW_CW_TIME")) || null != bean.getStr("GW_CW_TIME")) {
					Date date = DateUtils.strToDateLong(bean.getStr("GW_CW_TIME"), DateUtils.FORMAT_DATE_8);
					time = DateFormatUtils.format(date, DateUtils.FORMAT_DATE_8);
				} else {
					time = DateFormatUtils.format(DateUtils.getDate(), DateUtils.FORMAT_DATE_8);
				}
				
				log.debug("收文的成文时间：" + time);
				return time;
			case "OA_GW_GONGWEN_ICBCQB" :
				// 签报:署领导最后一次审批时间
				Bean mindBean = ServDao.find(SY_COMM_MIND, dataId); // 查询意见表
				
				if ("MIND_TIME" == mindBean.getStr("MIND_TIME") || null != mindBean.getStr("MIND_TIME")) {
					Date date = DateUtils.strToDateLong(mindBean.getStr("MIND_TIME"), DateUtils.FORMAT_DATE_8);
					time = DateFormatUtils.format(date, DateUtils.FORMAT_DATE_8);
				} else {
					time = DateFormatUtils.format(DateUtils.getDate(), DateUtils.FORMAT_DATE_8);
				}
				
				log.debug("署领导最后一次审批时间：" + time);
				return time;
//			case "" :
//				// 白头公文:主办司局负责人最后一次审批时间
//				if ("GW_CW_TIME".equals(bean.getStr("GW_CW_TIME")) || null != bean.getStr("GW_CW_TIME")) {
//					time = DateFormatUtils.format(new Date(), DateUtils.FORMAT_DATE_8);
//				} else {
//					time = DateFormatUtils.format(DateUtils.getDate(), DateUtils.FORMAT_DATE_8);
//				}
//				
//				log.debug("署领导最后一次审批时间：" + time);
//				return time;
		}
		
		return DateFormatUtils.format(DateUtils.getDate(), DateUtils.FORMAT_DATE_8);
	}
	
}
