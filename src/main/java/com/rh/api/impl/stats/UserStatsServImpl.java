package com.rh.api.impl.stats;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.Before;

import com.rh.api.impl.DataStatsServImpl;
import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.api.util.DataStatsUtil;

public class UserStatsServImpl extends DataStatsServImpl {

	/** log */
	private static Log log = LogFactory.getLog(UserStatsServImpl.class);
	
	/**服务ID */
	private static final String SERV_ID = "OA_GW_GONGWEN";
	
	/**节点公文人员机构信息关联服务ID */
	private static final String N_SERV_ID = "SY_WFE_NODE_INST_GW_V";
	
	/**用户服务ID */
	private static final String U_SERV_ID = "SY_ORG_USER";
	
	
	
	/**
	 * 个人公文办理情况
	 * @param reqData
	 */
	@Override
	public ApiOutBean getUserGwCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		List<Bean> dataList = new ArrayList<Bean>();
		
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		UserBean currUser = Context.getUserBean();
		String userCode = currUser.getId();
		Bean queryBean = new Bean();
		
		String where = " and node_btime >='" + bTime + "' and node_btime <='" + eTime + "'";

		//主办
		queryBean.set(Constant.PARAM_SELECT, "COUNT(DISTINCT(GW_ID)) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + " and (c_s_user='" + userCode + "' or to_user_id='" + userCode + "') and node_child_type='1'");
		Bean zbBean = ServDao.find(N_SERV_ID, queryBean);
		dataList.add(new Bean().set("name", "主办文件").set("count", zbBean.getStr("COUNT_")).set("unit", "件"));
		
		//流经
		queryBean.set(Constant.PARAM_WHERE, where + " and (c_s_user='" + userCode + "' or to_user_id='" + userCode + "') and node_child_type in ('2','3')");
		Bean ljBean = ServDao.find(N_SERV_ID, queryBean);
		dataList.add(new Bean().set("name", "流经处理").set("count", ljBean.getStr("COUNT_")).set("unit", "件"));
		
		//主办公文质量评价
		double quality = 0.00;
		dataList.add(new Bean().set("name", "主办公文质量评价").set("count", quality).set("unit", "平均"));
		
		//超期/延期
		queryBean.set(Constant.PARAM_WHERE, where + " and delay_time > 0 and (c_s_user='" + userCode + "' or to_user_id='" + userCode + "')");
		Bean cqBean = ServDao.find(N_SERV_ID, queryBean);
		dataList.add(new Bean().set("name", "超期/延期").set("count", cqBean.getStr("COUNT_")).set("unit", "件次"));
		
		outBean.setData(new Bean().set("data", dataList));
		return outBean;
	}
	
	/**
	 * 个人统计 环图
	 * @param reqData
	 */
	@Override
	public ApiOutBean getUserRingCount(Bean reqData){
		
		ApiOutBean outBean = new ApiOutBean();
		List<Bean> dataList = new ArrayList<Bean>();
		
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		UserBean currUser = Context.getUserBean();
		String userCode = currUser.getId();
		Bean queryBean = new Bean();
		
//		String where = " and s_user='" + userCode + "' and gw_begin_time >= '" + bTime + "' and gw_begin_time <= '" + eTime + "'";
		
		String where = " and (c_s_user='" + userCode + "' or to_user_id='" + userCode + "') and gw_begin_time>='" + bTime + "' and gw_begin_time <='" + eTime + "'";
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT(DISTINCT(GW_ID)) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + " and s_wf_state='1'");
		Bean wjBean = ServDao.find(N_SERV_ID, queryBean);
		int countWJ = wjBean.getInt("COUNT_");
		
		queryBean.set(Constant.PARAM_WHERE, where + " and s_wf_state='2'");
		Bean yjBean = ServDao.find(N_SERV_ID, queryBean);
		int countYJ = yjBean.getInt("COUNT_");
		
		dataList.add(new Bean().set("name", "已办" + countYJ + "件")
				.set("value", DataStatsUtil.getPercent(new Double(countYJ), new Double(countYJ + countWJ), 2)));
		dataList.add(new Bean().set("name", "未办" + countWJ + "件")
				.set("value", DataStatsUtil.getPercent(new Double(countWJ), new Double(countYJ + countWJ), 2)));
		
		outBean.setData(new Bean().set("data", dataList));
		return outBean;
	}
	
	/**
	 * 个人线图
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getUserGwDealSitu(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		int type = reqData.getInt("type");
		
		List<String> zbList = new ArrayList<String>();
		List<String> hqList = new ArrayList<String>();
		List<String> timeList = new ArrayList<String>();
		
		String where = " and NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'";
		
		if(type == 1){ //年度
			for(int i = 0 ; i < 12; i++){
				String monthStr = String.valueOf(i + 1);
				if(i + 1 <10){
					monthStr = "0" + monthStr;
				}
				String time = " and substr(NODE_BTIME,6,2) ='" + monthStr + "'";
				genRep(zbList, hqList, timeList, currUser, where, time);
			}
		} else if (type == 2){ //本周
			String weekDay = bTime.substring(0, 10);
			for(int i = 0 ; i < 7; i++){
				String time = " and substr(NODE_BTIME,1,10) ='" + weekDay + "'";
				genRep(zbList, hqList, timeList, currUser, where, time);
				weekDay = DateUtils.formatDate(DataStatsUtil.getNextDay(DataStatsUtil.strToDate(weekDay)));
			}
		} else if (type == 3){ //本月
			String montDay = bTime.substring(0, 10);
			int days = DataStatsUtil.getMonthDays(montDay);
			for(int i = 0 ; i < days; i++){
				String time = " and substr(NODE_BTIME,1,10) ='" + montDay + "'";
				genRep(zbList, hqList, timeList, currUser, where, time);
				montDay = DateUtils.formatDate(DataStatsUtil.getNextDay(DataStatsUtil.strToDate(montDay)));
			}
		}
		
		outBean.setData(new Bean().set("zbData", zbList).set("hqData", hqList).set("time", timeList));
		return outBean;
	}
	/**
	 * 根据条件分项统计
	 * @param where
	 * @param repBean
	 * @param time
	 */
	public void genRep(List<String> zbList, List<String> hqList, List<String> timeList, Bean user, String where, String time){
		ParamBean queryBean = new ParamBean();
		
		String userCode = user.getId();
		//主办数量
		queryBean.set(Constant.PARAM_SELECT, "COUNT(DISTINCT(GW_ID)) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + time + " and (c_s_user='" + userCode + "' or to_user_id='" + userCode + "') and node_child_type='1'");
		 Bean zbBean = ServDao.find(N_SERV_ID, queryBean);
		 int zbNum = zbBean.getInt("COUNT_");
		zbList.add(zbNum == 0 ? "" : String.valueOf(zbNum));
		
		queryBean.set(Constant.PARAM_WHERE, where + time + " and (c_s_user='" + userCode + "' or to_user_id='" + userCode + "') and node_child_type in ('2', '3')");
		Bean hqBean = ServDao.find(N_SERV_ID, queryBean);
		int hqNum = hqBean.getInt("COUNT_");
		hqList.add(hqNum == 0 ? "" : String.valueOf(hqNum));
		
		queryBean.set(Constant.PARAM_SELECT, "SUM(NODE_DAYS) DAYS");
		queryBean.set(Constant.PARAM_WHERE, where + time + " and (c_s_user='" + userCode + "' or to_user_id='" + userCode + "')");
		Bean timeBean = ServDao.find(N_SERV_ID, queryBean);
		if(timeBean != null){
			double days = timeBean.getDouble("DAYS");
			String avg = DataStatsUtil.getAvg(days/60, (double)(zbNum + hqNum), 2);
			timeList.add(avg);
		}
	}
	
	/**
	 * 公文处理数量统计
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getUserGwRepList(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		int type = reqData.getInt("type");
		
		List<Bean> dataList = new ArrayList<Bean>();
		Bean repBean = new Bean();
		
		String userCode = currUser.getCode();
		String where = " and NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'";
		
		if(type == 1){ //年度
			for(int i = 0 ; i < 12; i++){
				repBean = new Bean();
				String monthStr = String.valueOf(i + 1);
				repBean.set("SORT", monthStr);
				if(i + 1 <10){
					monthStr = "0" + monthStr;
				}
				String time = " and substr(NODE_BTIME,6,2) ='" + monthStr + "'";
				genRep(repBean, userCode, where, time);
				
				dataList.add(repBean);
			}
		} else if (type == 2){ //本周
			String weekDay = bTime.substring(0, 10);
			for(int i = 0 ; i < 7; i++){
				repBean = new Bean();
				repBean.set("SORT", i + 1);
				String time = " and substr(NODE_BTIME,1,10) ='" + weekDay + "'";
				genRep(repBean, userCode, where, time);
				dataList.add(repBean);
				weekDay = DateUtils.formatDate(DataStatsUtil.getNextDay(DataStatsUtil.strToDate(weekDay)));
			}
		} else if (type == 3){ //本月
			String montDay = bTime.substring(0, 10);
			int days = DataStatsUtil.getMonthDays(montDay);
			for(int i = 0 ; i < days; i++){
				repBean = new Bean();
				repBean.set("SORT", i + 1);
				String time = " and substr(NODE_BTIME,1,10) ='" + montDay + "'";
				genRep(repBean, userCode, where, time);
				dataList.add(repBean);
				montDay = DateUtils.formatDate(DataStatsUtil.getNextDay(DataStatsUtil.strToDate(montDay)));
			}
		}
		//合计
		repBean = new Bean();
		repBean.set("SORT", "合计");
		genRep(repBean, userCode, where, "");
		dataList.add(repBean);
		
		outBean.setData(new Bean().set("listData", dataList));
		return outBean;
	}
	public void genRep(Bean repBean, String userCode, String where, String time){
		Bean queryBean = new Bean();
		
		String swWhere = " and TMPL_TYPE_CODE='OA_GW_GONGWEN_SW'";
		String fwWhere = " and TMPL_TYPE_CODE='OA_GW_GONGWEN_FW'";
		String qbWhere = " and TMPL_TYPE_CODE='OA_GW_GONGWEN_QB'";
		
		String zbWhere = " and node_child_type='1'";
		String ljWhere = " and node_child_type in ('0','2', '3')";
		
		String qcWhere = " and c_s_user='" + userCode + "'";
		String jsWhere = " and to_user_id='" + userCode + "'";
		String allWhere = " and (c_s_user='" + userCode + "' or to_user_id='" + userCode + "')";
		
		String cqWhere = " and delay_time > 0";
		
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT(DISTINCT(GW_ID)) COUNT_, SUM(NODE_DAYS) DAYS");
		queryBean.set(Constant.PARAM_WHERE, where + time + allWhere);
		Bean allBean = ServDao.find(N_SERV_ID, queryBean);
		int allNum = allBean.getInt("COUNT_");
		double allTime= allBean.getDouble("DAYS");
		String avg = DataStatsUtil.getAvg(allTime/60, (double)allNum, 2);
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT(DISTINCT(GW_ID)) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + time + swWhere + zbWhere + jsWhere);
		Bean swZb = ServDao.find(N_SERV_ID, queryBean);
		int swZbNum = swZb.getInt("COUNT_");
		
		queryBean.set(Constant.PARAM_WHERE, where + time + swWhere + ljWhere + jsWhere);
		Bean swLj = ServDao.find(N_SERV_ID, queryBean);
		int swLjNum = swLj.getInt("COUNT_");
		
		queryBean.set(Constant.PARAM_WHERE, where + time + fwWhere + qcWhere);
		Bean fwQc = ServDao.find(N_SERV_ID, queryBean);
		int fwQcNum = fwQc.getInt("COUNT_");

		queryBean.set(Constant.PARAM_WHERE, where + time + fwWhere + ljWhere + jsWhere);
		Bean fwLj = ServDao.find(N_SERV_ID, queryBean);
		int fwLjNum = fwLj.getInt("COUNT_");
		
		queryBean.set(Constant.PARAM_WHERE, where + time + qbWhere + qcWhere);
		Bean qbQc = ServDao.find(N_SERV_ID, queryBean);
		int qbQcNum = qbQc.getInt("COUNT_");
		
		queryBean.set(Constant.PARAM_WHERE, where + time + qbWhere + ljWhere + jsWhere);
		Bean qbLj = ServDao.find(N_SERV_ID, queryBean);
		int qbLjNum = qbLj.getInt("COUNT_");
		
		queryBean.set(Constant.PARAM_WHERE, where + time + allWhere + cqWhere);
		Bean cqBean = ServDao.find(N_SERV_ID, queryBean);
		int cqNum = cqBean.getInt("COUNT_");
		
		int yqNum = 0;
		
		repBean.set("ALLNUM", allNum == 0 ? "" : allNum)
		.set("SWZB", swZbNum == 0 ? "" : swZbNum)
		.set("SWLJ", swLjNum == 0 ? "" : swLjNum)
		.set("FWNG", fwQcNum == 0 ? "" : fwQcNum)
		.set("FWLJ", fwLjNum == 0 ? "" : fwLjNum)
		.set("QBQC", qbQcNum == 0 ? "" : qbQcNum)
		.set("QBLJ", qbLjNum == 0 ? "" : qbLjNum)
		.set("AVG", avg)
		.set("CQNUM", cqNum == 0 ? "" : cqNum)
		.set("YQNUM", yqNum == 0 ? "" : yqNum);
	}
	
	/**
	 * 公文处理文件详情
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getUserGwList(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
		List<Bean> repList = new ArrayList<Bean>();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		String userCode = currUser.getCode();
		
		String where = " and node_btime >='" + bTime + "' and node_btime <='" + eTime + "'"
				+ " and (to_user_id='" + userCode + "' or c_s_user='" + userCode + "')";
		
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_WHERE, where);
		List<Bean> gwList = ServDao.finds(N_SERV_ID, queryBean);
		
		Bean repBean = null;
		for(int i = 0; i < gwList.size(); i++){
			repBean = new Bean();
			Bean gwBean = gwList.get(i);
			
			repBean.set("id", gwBean.getId());
			repBean.set("sort", i + 1);
			repBean.set("title", gwBean.getStr("GW_TITLE"));
			repBean.set("gwCode", gwBean.getStr("GW_FULL_CODE"));
			repBean.set("gwType", getGwType(gwBean.getStr("TMPL_TYPE_CODE")));
			repBean.set("nodeName", gwBean.getStr("NODE_NAME"));
			
			repBean.set("cqTime", "");
			repBean.set("isYq", "");
			repBean.set("quality", "");
			
			repList.add(repBean);
		}
		
		outBean.setData(new Bean().set("listData", repList));
		return outBean;
	}
	
	/**
	 * 获取公文类别
	 * @param typeCode
	 * @return
	 */
	public String getGwType(String typeCode){
 		if(typeCode.equals("OA_GW_GONGWEN_FW")){
 			return "发文";
 		} else if(typeCode.equals("OA_GW_GONGWEN_SW")){
 			return "收文";
 		} else if(typeCode.equals("OA_GW_GONGWEN_QB")){
 			return "签报";
 		}
 		return "";
	}
	
	public ApiOutBean getUserTabDataByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();

		// 主办
		String zbSql = "select  COUNT(DISTINCT(GW_ID)) GW_COUNT from SY_WFE_NODE_INST_GW_V  where NODE_BTIME >= '"
				+ beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime + " 23:59:59' " + "and C_S_USER = '"
				+ currUser.getId() + "' " + "AND NODE_CHILD_TYPE='1' ";
		Bean zbBean = Transaction.getExecutor().queryOne(zbSql);
		if (zbBean == null) {
			zbBean = new Bean();
		}

		// 协办会签
		String hqxbSql = "and NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime + " 23:59:59' "
				+ "and TO_USER_ID = '" + currUser.getId() + "' "
				+ "AND  (NODE_CHILD_TYPE='2' or NODE_CHILD_TYPE='3' ) ";
		List<Bean> hqxbCount = ServDao.finds("SY_WFE_NODE_INST_ALL_V", hqxbSql);

		// 平均处理时间
		String avgSql = "select sum(node_days) node_days from SY_WFE_NODE_INST_ALL_V  where NODE_BTIME >= '" + beginTime
				+ " 00:00:00' AND NODE_BTIME <= '" + endTime + " 23:59:59' " + "and TO_USER_ID = '" + currUser.getId()
				+ "' ";
		Bean avgBean = Transaction.getExecutor().queryOne(avgSql);
		if (avgBean == null) {
			avgBean = new Bean();
		}
		// 逾期时间
		String yuqiSql = "select count(delay_time) DELAY_TIME  from SY_WFE_NODE_INST_ALL_V where NODE_BTIME >= '"
				+ beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime + " 23:59:59' " + "and delay_time >0 "
				+ "and TO_USER_ID = '" + currUser.getId() + "'";
		Bean yuqiBean = Transaction.getExecutor().queryOne(yuqiSql);
		if (yuqiBean == null) {
			yuqiBean = new Bean();
		}
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		Map<String, String> zbData = new HashMap<String, String>();
		zbData.put("name", "主办文件");
		zbData.put("count", String.valueOf(zbBean.get("GW_COUNT")));
		zbData.put("unit", "件");
		dataList.add(zbData);

		Map<String, String> xbData = new HashMap<String, String>();
		xbData.put("name", "会签协办");
		xbData.put("count", String.valueOf(hqxbCount.size()));
		xbData.put("unit", "件");
		dataList.add(xbData);

		float gwCount = DataStatsUtil.parseFloat(zbBean.getStr("GW_COUNT")) + hqxbCount.size();
		Map<String, String> avgData = new HashMap<String, String>();
		avgData.put("name", "平均处理时间");
		avgData.put("count",
				String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.divFunc(avgBean.getInt("NODE_DAYS"), 60), gwCount)));
		avgData.put("unit", "小时/件");
		dataList.add(avgData);

		Map<String, String> yqData = new HashMap<String, String>();
		yqData.put("name", "逾期工作");
		String delayTime = yuqiBean.getStr("DELAY_TIME");
		yqData.put("count", (delayTime.length() > 0) ? delayTime : "0");
		yqData.put("unit", "件");
		dataList.add(yqData);

		List<List> tempList = new ArrayList<List>();
		tempList.add(dataList);

		outBean.setData(new Bean().set("data", tempList));

		return outBean;
	}

	@Override
	public ApiOutBean getUserStatsDataByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");
		int type = reqData.getInt("type");// 1:年 2:周 3:月

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		String periodField = "";
		int periodDays = 0;
		if (type == 1) {
			periodField = "BL_MONTH ";
			periodDays = 12;
		} else if (type == 2) {
			periodField = "TO_CHAR (TO_DATE (NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = 7;
		} else if (type == 3) {
			periodField = "TO_CHAR (TO_DATE (NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = DateUtils.getDayOfMonth(DateUtils.getMonth(), DateUtils.getYear());
		}

		
		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' " + "and TO_USER_ID = '" + currUser.getId() + "' ";

		String dataSql = "SELECT TMPL_TYPE_CODE, " + periodField + " OPERIOD, "
				+ "DECODE(node_child_type,  0, '流经',  1, '主办',  2, '流经',  3, '流经') operate, "
				+ "COUNT (DISTINCT gw_id) gw_count,sum(node_days) node_days " + "FROM SY_WFE_NODE_INST_GW_V " + strWhere
				+ "GROUP BY TMPL_TYPE_CODE,"+ periodField +",DECODE(node_child_type,  0, '流经',  1, '主办',  2, '流经',  3, '流经') "
				+ "ORDER BY " + periodField;
		List list = Transaction.getExecutor().query(dataSql);

		String yuqiSql = "SELECT  "+periodField+" OPERIOD,count(delay_time)  delay_time " + "FROM SY_WFE_NODE_INST_GW_V " + strWhere
				+ " and  delay_time >0 " + "GROUP BY "+periodField + " ORDER BY "+periodField;
		List yuqiList = Transaction.getExecutor().query(yuqiSql);

		List aList = new ArrayList();
		List bList = new ArrayList();
		List cList = new ArrayList();
		List dList = new ArrayList();
		// 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		for (int i = 1; i <= periodDays; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", String.valueOf(i));
			try {
				map.put("OPERIOD",  DateUtils.formatDate(DateUtils.addDays(sdf.parse(beginTime), i - 1)));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			aList.add(map);
		}
		// 合计用时、流经、主办
		if(type == 1){
			for (int i = 0; i < aList.size(); i++) {
				Map map = (Map) aList.get(i);
				for (int j = 0; j < list.size(); j++) {
					Bean tempBean = (Bean) list.get(j);
					if (DataStatsUtil.parseInt(String.valueOf(map.get("id"))) == DataStatsUtil
							.parseInt(tempBean.getStr("OPERIOD"))) {
						float totalTime = DataStatsUtil.sumFunc(
								DataStatsUtil.parseFloat(String.valueOf(map.get("totalTime"))),
								tempBean.getInt("NODE_DAYS"));
						if (totalTime > 0) {
							map.put("totalTime", totalTime);
						}
						if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
							if (tempBean.get("OPERATE").equals("流经")) {
								map.put("fwlj", tempBean.get("GW_COUNT"));
							} else if (tempBean.get("OPERATE").equals("主办")) {
								map.put("fwng", tempBean.get("GW_COUNT"));
							}
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
							if (tempBean.get("OPERATE").equals("流经")) {
								map.put("swzb", tempBean.get("GW_COUNT"));
							} else if (tempBean.get("OPERATE").equals("主办")) {
								map.put("swxb", tempBean.get("GW_COUNT"));
							}
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
							if (tempBean.get("OPERATE").equals("流经")) {
								map.put("qbqc", tempBean.get("GW_COUNT"));
							} else if (tempBean.get("OPERATE").equals("主办")) {
								map.put("qblj", tempBean.get("GW_COUNT"));
							}
						}
					}
				}
				bList.add(map);
			}
		}else{
			for (int i = 0; i < aList.size(); i++) {
				Map map = (Map) aList.get(i);
				for (int j = 0; j < list.size(); j++) {
					Bean tempBean = (Bean) list.get(j);
					if (String.valueOf(map.get("OPERIOD")).equals(tempBean.getStr("OPERIOD"))) {
						float totalTime = DataStatsUtil.sumFunc(
								DataStatsUtil.parseFloat(String.valueOf(map.get("totalTime"))),
								tempBean.getInt("NODE_DAYS"));
						if (totalTime > 0) {
							map.put("totalTime", totalTime);
						}
						if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
							if (tempBean.get("OPERATE").equals("流经")) {
								map.put("fwlj", tempBean.get("GW_COUNT"));
							} else if (tempBean.get("OPERATE").equals("主办")) {
								map.put("fwng", tempBean.get("GW_COUNT"));
							}
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
							if (tempBean.get("OPERATE").equals("流经")) {
								map.put("swzb", tempBean.get("GW_COUNT"));
							} else if (tempBean.get("OPERATE").equals("主办")) {
								map.put("swxb", tempBean.get("GW_COUNT"));
							}
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
							if (tempBean.get("OPERATE").equals("流经")) {
								map.put("qbqc", tempBean.get("GW_COUNT"));
							} else if (tempBean.get("OPERATE").equals("主办")) {
								map.put("qblj", tempBean.get("GW_COUNT"));
							}
						}
					}
				}
				bList.add(map); 
			}
		}
		    
		// 文件总数、平均时间、合计用时换算小时
		for (int i = 0; i < bList.size(); i++) {
			Map map = (Map) bList.get(i);
			int sum = DataStatsUtil.parseInt(String.valueOf(map.get("swzb")))
					+ DataStatsUtil.parseInt(String.valueOf(map.get("swxb")))
					+ DataStatsUtil.parseInt(String.valueOf(map.get("fwng")))
					+ DataStatsUtil.parseInt(String.valueOf(map.get("fwlj")))
					+ DataStatsUtil.parseInt(String.valueOf(map.get("qbqc")))
					+ DataStatsUtil.parseInt(String.valueOf(map.get("qblj")));
			map.put("fileCount", sum > 0 ? sum : "");
			float totalTime = DataStatsUtil.divFunc(DataStatsUtil.parseFloat(String.valueOf(map.get("totalTime"))), 60);
			map.put("totalTime", totalTime > 0 ? totalTime : "");
			float avgTime = DataStatsUtil.divFunc(DataStatsUtil.parseFloat(String.valueOf(map.get("totalTime"))),
					DataStatsUtil.parseInt(String.valueOf(map.get("fileCount"))));
			map.put("avgTime", avgTime > 0 ? avgTime : "");
			cList.add(map);
		}

		// 逾期件数
		if(type == 1){
			for (int i = 0; i < cList.size(); i++) {
				Map map = (Map) cList.get(i);
				for (int j = 0; j < yuqiList.size(); j++) {
					Bean tempBean = (Bean) yuqiList.get(j);
					if (DataStatsUtil.parseInt(String.valueOf(map.get("id"))) == DataStatsUtil
							.parseInt(tempBean.getStr("OPERIOD"))) {
						map.put("overCount", tempBean.get("DELAY_TIME"));
					}
				}
				dList.add(map);
			}	
		}else{
			for (int i = 0; i < cList.size(); i++) {
				Map map = (Map) cList.get(i);
				for (int j = 0; j < yuqiList.size(); j++) {
					Bean tempBean = (Bean) yuqiList.get(j);
					if (String.valueOf(map.get("OPERIOD")).equals(tempBean.getStr("OPERIOD"))) {
						map.put("overCount", tempBean.get("DELAY_TIME"));
					}
				}
				dList.add(map);
			}
		}
		
		// 合计
		Map total = new HashMap();
		for (int i = 0; i < dList.size(); i++) {
			Map map = (Map) dList.get(i);
			total.put("id", "合计");
			total.put("swzb", (int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("swzb"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("swzb")))));
			total.put("swxb", (int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("swxb"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("swxb")))));
			total.put("fwng", (int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("fwng"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("fwng")))));
			total.put("fwlj", (int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("fwlj"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("fwlj")))));
			total.put("qbqc", (int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("qbqc"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("qbqc")))));
			total.put("qblj", (int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("qblj"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("qblj")))));
			total.put("fileCount",
					(int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("fileCount"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("fileCount")))));
			total.put("totalTime",
					DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("totalTime"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("totalTime")))));
			total.put("overCount",
					(int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("overCount"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("overCount")))));
		}
		total.put("avgTime", DataStatsUtil.divFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("totalTime"))),
				DataStatsUtil.parseFloat(String.valueOf(total.get("fileCount")))));
		dList.add(total);

		outBean.setData(new Bean().set("data", dList));

		return outBean;
	}

	@Override
	public ApiOutBean getUserStatsCharBJLByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where GW_BEGIN_TIME >= '" + beginTime + " 00:00:00' AND GW_BEGIN_TIME <= '" + endTime
				+ " 23:59:59' " + "and S_USER = '" + currUser.getId() + "' ";

		String dataSql = "SELECT COUNT(S_WF_STATE) GW_COUNT,S_WF_STATE FROM (select DECODE (S_WF_STATE, 1, '未结', 2, '已结') S_WF_STATE from oa_gw_gongwen "
				+ strWhere + ")" + "GROUP BY S_WF_STATE ";
		List list = Transaction.getExecutor().query(dataSql);

		List listData = new ArrayList();
		Map<String, String> yjData = new HashMap<String, String>();
		yjData.put("name", "已办0件");
		Map<String, String> wjData = new HashMap<String, String>();
		wjData.put("name", "未办0件");
		// 已结 未结
		float gwCount = 0;
		for (int i = 0; i < list.size(); i++) {
			Bean tempBean = (Bean) list.get(i);
			gwCount = DataStatsUtil.sumFunc(gwCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
			if (tempBean.getStr("S_WF_STATE").equals("已结")) {
				yjData.put("name", "已办" + tempBean.getStr("GW_COUNT") + "件");
				yjData.put("yj", tempBean.getStr("GW_COUNT"));
			} else if (tempBean.getStr("S_WF_STATE").equals("未结")) {
				wjData.put("name", "未办" + tempBean.getStr("GW_COUNT") + "件");
				wjData.put("wj", tempBean.getStr("GW_COUNT"));
			}
		}
		
		float yjCount = yjData.get("yj")!=null&&yjData.get("yj").length()>0?DataStatsUtil.parseFloat(yjData.get("yj")):0;
		float wjCount = wjData.get("wj")!=null&&wjData.get("wj").length()>0?DataStatsUtil.parseFloat(wjData.get("wj")):0;
		wjData.put("value",  String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(wjCount,100),gwCount)));
		yjData.put("value",  String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(yjCount,100),gwCount)));
		listData.add(wjData);
		listData.add(yjData);

		Bean rtnBean = new Bean();
		rtnBean.put("title", "办结率");
		rtnBean.put("listData", listData);

		outBean.setData(new Bean().set("data", rtnBean));

		return outBean;
	}

	@Override
	public ApiOutBean getUserStatsCharWORKByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");
		int type = reqData.getInt("type");// 1:年 2:周 3:月

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		String periodField = "";
		int periodDays = 0;
		if (type == 1) {
			periodField = "BL_MONTH ";
			periodDays = 12;
		} else if (type == 2) {
			periodField = "TO_CHAR (TO_DATE (NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = 7;
		} else if (type == 3) {
			periodField = "TO_CHAR (TO_DATE (NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = DateUtils.getDayOfMonth(DateUtils.getMonth(), DateUtils.getYear());
		}

		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' " + "and TO_USER_ID = '" + currUser.getId() + "' and node_child_type<>'0' ";

		String dataSql = "SELECT " + periodField + " OPERIOD, "
				+ "DECODE(node_child_type,  1, '主办',  2, '会签',  3, '会签') operate, "
				+ "COUNT (DISTINCT gw_id) gw_count,sum(node_days) node_days " + "FROM SY_WFE_NODE_INST_GW_V " + strWhere
				+ "GROUP BY " + periodField + ",DECODE(node_child_type, 1, '主办',  2, '会签',  3, '会签') " + "ORDER BY "
				+ periodField;
		List list = Transaction.getExecutor().query(dataSql);

		Bean bean = new Bean();
		for (int i = 0; i < list.size(); i++) {
			Bean tempBean = (Bean) list.get(i);
			bean.put(tempBean.getStr("OPERIOD") + "-" + tempBean.getStr("OPERATE"), new Bean()
					.set("GW_COUNT", tempBean.getInt("GW_COUNT")).set("NODE_DAYS", tempBean.getInt("NODE_DAYS")));
		}

		String zbDataArr = "";
		String hqDataArr = "";
		String timeArr = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// 数据
		if (type == 1) {
			for (int j = 1; j <= periodDays; j++) {
				int time = 0;
				if (bean.containsKey(j + "-主办")) {
					zbDataArr += "," + bean.getBean(j + "-主办").getInt("GW_COUNT");
					time += bean.getBean(j + "-主办").getInt("NODE_DAYS");
				} else {
					zbDataArr += ",0";
				}
				if (bean.containsKey(j + "-会签")) {
					hqDataArr += "," + bean.getBean(j + "-会签").getInt("GW_COUNT");
					time += bean.getBean(j + "-会签").getInt("NODE_DAYS");
				} else {
					hqDataArr += ",0";
				}
				timeArr += "," + time;
			}
		} else {
			for (int j = 1; j <= periodDays; j++) {
				int time = 0;
				String curdate = "";
				try {
					curdate = DateUtils.formatDate(DateUtils.addDays(sdf.parse(beginTime), j - 1));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (bean.containsKey(curdate + "-主办")) {
					zbDataArr += "," + bean.getBean(curdate + "-主办").getInt("GW_COUNT");
					time += bean.getBean(curdate + "-主办").getInt("NODE_DAYS");
				} else {
					zbDataArr += ",0";
				}
				if (bean.containsKey(curdate + "-会签")) {
					hqDataArr += "," + bean.getBean(curdate + "-会签").getInt("GW_COUNT");
					time += bean.getBean(curdate + "-会签").getInt("NODE_DAYS");
				} else {
					hqDataArr += ",0";
				}
				timeArr += "," + time;
			}
		}

		outBean.setData(new Bean().set("zbData", "[" + zbDataArr.substring(1) + "]")
				.set("hqData", "[" + hqDataArr.substring(1) + "]").set("time", "[" + timeArr.substring(1) + "]"));

		return outBean;
	}

}
