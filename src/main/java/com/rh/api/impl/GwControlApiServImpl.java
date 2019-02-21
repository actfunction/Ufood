package com.rh.api.impl;

import java.util.Calendar;
import java.util.Date;

import com.rh.api.serv.IGwControlApiServ;
import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

public class GwControlApiServImpl implements IGwControlApiServ{

	/**服务ID */
	private static final String SERV_ID = "OA_SERV_LOG";
	
	@Override
	public ApiOutBean getGwControlInfo(Bean reqData) {
		
		ApiOutBean outBean = new ApiOutBean();
		Bean resBean = new Bean();
		//年度
		int year = DateUtils.getYear();
		String bTime = year + "-01-01";
		String eTime = year + "-12-31";
		OutBean yearRep = getRepList(bTime, eTime);
		int scCountY = getScCount(bTime, eTime);
		int downLoadCountY = getDownCount(bTime, eTime);
		int printCountY	= getPrintCount(bTime, eTime);
		
		resBean.set("YEAR", yearRep.set("SC_COUNT", scCountY).set("DOWN_COUNT", downLoadCountY).set("PRINT_COUNT", printCountY));
		//本周
		bTime = DateUtils.formatDate(getWeekFirstDay());
		eTime = DateUtils.formatDate(getWeekLastDay());
		
		OutBean weekRep = getRepList(bTime, eTime);
		int scCountW = getScCount(bTime, eTime);
		int downLoadCountW = getDownCount(bTime, eTime);
		int printCountW	= getPrintCount(bTime, eTime);
		
		resBean.set("WEEK", weekRep.set("SC_COUNT", scCountW).set("DOWN_COUNT", downLoadCountW).set("PRINT_COUNT", printCountW));
		//本月
		Date date = new Date();
		bTime = DateUtils.formatDate(DateUtils.getMonthFirstDay(date));
		eTime = DateUtils.getMonthLastDay(DateUtils.formatDate(date));
		
		OutBean monthRep = getRepList(bTime, eTime);
		int scCountM = getScCount(bTime, eTime);
		int downLoadCountM = getDownCount(bTime, eTime);
		int printCountM	= getPrintCount(bTime, eTime);
		
		resBean.set("MONTH", monthRep.set("SC_COUNT", scCountM).set("DOWN_COUNT", downLoadCountM).set("PRINT_COUNT", printCountM));
		
		outBean.setData(resBean);
		return outBean;
	}

	/**
	 * 根据起止时间获取监控列表
	 * @param bTime
	 * @param eTime
	 * @return
	 */
	public static OutBean getRepList(String bTime, String eTime){
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SERV_ID, SERV_ID);
		queryBean.set(Constant.PARAM_WHERE, " and LOG_TIME <='" + eTime + "' and LOG_TIME >= '" + bTime + "'");
		queryBean.set(Constant.PARAM_ACT_CODE, "query");
		return ServMgr.act(queryBean);
	}
	
	/**
	 * 根据起止时间获取收藏次数
	 * @param bTime
	 * @param eTime
	 * @return
	 */
	public static int getScCount(String bTime, String eTime){
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_WHERE, " and LOG_TIME <='" + eTime + "' and LOG_TIME >= '" + bTime + "' and OPER_CODE='star'");
		return ServDao.count(SERV_ID, queryBean);
	}
	
	/**
	 * 根据起止时间获取下载次数
	 * @param bTime
	 * @param eTime
	 * @return
	 */
	public static int getDownCount(String bTime, String eTime){
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_WHERE, " and LOG_TIME <='" + eTime + "' and LOG_TIME >= '" + bTime + "' and OPER_CODE='download'");
		return ServDao.count(SERV_ID, queryBean);
	}
	
	/**
	 * 根据起止时间获取打印次数
	 * @param bTime
	 * @param eTime
	 * @return
	 */
	public static int getPrintCount(String bTime, String eTime){
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_WHERE, " and LOG_TIME <='" + eTime + "' and LOG_TIME >= '" + bTime + "' and OPER_CODE='print'");
		return ServDao.count(SERV_ID, queryBean);
	}
	
	/**
	 * 获取本周第一天
	 * @return
	 */
    public Date getWeekFirstDay() {  
        Calendar cal = Calendar.getInstance();  
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);  
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);  
        return cal.getTime();  
    }  
  
    /**
     * 获取本周最后一天
     * @return
     */
    public Date getWeekLastDay() {  
        Calendar cal = Calendar.getInstance();  
        cal.setTime(getWeekFirstDay());  
        cal.add(Calendar.DAY_OF_WEEK, 7);  
        return cal.getTime();  
    } 
}
