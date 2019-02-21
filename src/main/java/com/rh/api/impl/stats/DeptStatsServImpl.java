package com.rh.api.impl.stats;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.api.impl.DataStatsServImpl;
import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.api.util.DataStatsUtil;

public class DeptStatsServImpl extends DataStatsServImpl {

	/** log */
	private static Log log = LogFactory.getLog(DeptStatsServImpl.class);
	
	/**服务ID */
	private static final String SERV_ID = "OA_GW_GONGWEN";
	
	/**节点公文人员机构信息关联服务ID */
	private static final String N_SERV_ID = "SY_WFE_NODE_INST_GW_V";
	
	/**用户服务ID */
	private static final String U_SERV_ID = "SY_ORG_USER";
	
	/**
	 * 部门概览环形图
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getRingCount(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		List<Bean> ringList = new ArrayList<Bean>();
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		ParamBean queryBean = new ParamBean();
		String where = " and GW_BEGIN_TIME >='" + bTime + "' and GW_BEGIN_TIME <='" + eTime + "' and S_TDEPT='"+ currUser.getTDeptCode() + "'";
		
		
		queryBean.set(Constant.PARAM_WHERE, where + " and S_WF_STATE ='2'");
		int countYJ = ServDao.count(SERV_ID, queryBean);
		
		queryBean.set(Constant.PARAM_WHERE, where + " and S_WF_STATE ='1'");
		int countWJ = ServDao.count(SERV_ID, queryBean);
		
		ringList.add(new Bean().set("name", "已结" + countYJ + "件")
				.set("value", DataStatsUtil.getPercent(new Double(countYJ), new Double(countYJ + countWJ), 2)));
		ringList.add(new Bean().set("name", "未结" + countWJ + "件")
				.set("value", DataStatsUtil.getPercent(new Double(countWJ), new Double(countYJ + countWJ), 2)));
		
		outBean.setData(new Bean().set("data", ringList));
		return outBean;
	}
	
	/**
	 * 部门正在办理公文
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getDealCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		List<Bean> dealList = new ArrayList<Bean>();
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		String where = " and S_WF_INST in (select PI_ID from SY_WFE_NODE_INST_ALL_V "
				+ "where NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'"
						+ " and node_if_running = '1'"
						+ " and to_user_id in (select user_code from SY_BASE_USER_V where "
						+ "tdept_code='" + currUser.getTDeptCode() + "'))";
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SELECT, "COUNT(GW_ID) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_FW'");
		Bean countFW = ServDao.find(SERV_ID, queryBean);
		
		queryBean.set(Constant.PARAM_WHERE, where + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_SW'");
		Bean countSW = ServDao.find(SERV_ID, queryBean);
		
		queryBean.set(Constant.PARAM_WHERE, where + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_QB'");
		Bean countQB = ServDao.find(SERV_ID, queryBean);
		
		dealList.add(new Bean().set("name", "发文").set("value", countFW.getInt("COUNT_")));
		dealList.add(new Bean().set("name", "收文").set("value", countSW.getInt("COUNT_")));
		dealList.add(new Bean().set("name", "签报").set("value", countQB.getInt("COUNT_")));
		
		outBean.setData(new Bean().set("data",  dealList));
		return outBean;
	}
	
	/**
	 * 部门主办/会签协办文件情况
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getGwCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		List<Bean> gwList = new ArrayList<Bean>();
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		String type = reqData.getStr("type");
		
		
		String where = " and S_WF_INST in (select PI_ID from SY_WFE_NODE_INST_ALL_V "
				+ "where NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'"
						+ " and node_child_type in ('" + type.replaceAll(",", "','") + "')"
						+ " and to_user_id in (select user_code from SY_BASE_USER_V where "
						+ "tdept_code='" + currUser.getTDeptCode() + "'))";
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SELECT, "COUNT(GW_ID) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where);
		Bean countALL = ServDao.find(SERV_ID, queryBean);
		
		queryBean.set(Constant.PARAM_WHERE, where + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_FW'");
		Bean countFW = ServDao.find(SERV_ID, queryBean);
		
		queryBean.set(Constant.PARAM_WHERE, where + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_SW'");
		Bean countSW = ServDao.find(SERV_ID, queryBean);
		
		queryBean.set(Constant.PARAM_WHERE, where + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_QB'");
		Bean countQB = ServDao.find(SERV_ID, queryBean);
		
		gwList.add(new Bean().set("name", "文件总数").set("count", countALL.getInt("COUNT_")).set("unit", "件"));
		gwList.add(new Bean().set("name", "收文").set("count", countSW.getInt("COUNT_")).set("unit", "件"));
		gwList.add(new Bean().set("name", "发文").set("count", countFW.getInt("COUNT_")).set("unit", "件"));
		gwList.add(new Bean().set("name", "签报").set("count", countQB.getInt("COUNT_")).set("unit", "件"));
		
		outBean.setData(new Bean().set("data",  gwList));
		return outBean;
	}
	/**
	 * 主办/会签公文时效
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getGwAgingCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		List<Bean> gwList = new ArrayList<Bean>();
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		String type = reqData.getStr("type");
		
		String where = " and S_WF_INST in (select PI_ID from SY_WFE_NODE_INST_ALL_V "
				+ "where NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'"
						+ " and node_child_type in ('" + type.replaceAll(",", "','") + "')"
						+ " and to_user_id in (select user_code from SY_BASE_USER_V where "
						+ "tdept_code='" + currUser.getTDeptCode() + "'))";
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SELECT, "COUNT(GW_ID) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_FW'");
		Bean countFW = ServDao.find(SERV_ID, queryBean);
		
		queryBean.set(Constant.PARAM_WHERE, where + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_QB'");
		Bean countQB = ServDao.find(SERV_ID, queryBean);
		
		gwList.add(new Bean().set("name", "发文").set("count", countFW.getInt("COUNT_")).set("unit", "件"));
		gwList.add(new Bean().set("name", "签报").set("count", countQB.getInt("COUNT_")).set("unit", "件"));
		gwList.add(new Bean().set("name", "超期度").set("count", "8.23%").set("unit", "平均"));
		gwList.add(new Bean().set("name", "延期率").set("count", "4.09%").set("unit", "平均"));
		if(type.equals("1")){
			gwList.add(new Bean().set("name", "质量评分").set("count", "9.88").set("unit", "平均"));
		}

		outBean.setData(new Bean().set("data",  gwList));
		return outBean;
	}
	
	/**
	 * 文件催办、督办
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getGwBlCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		List<Bean> blList = new ArrayList<Bean>();
		
		
		
		
		
		blList.add(new Bean().set("name", "被催办").set("count", "94").set("unit", "次"));
		blList.add(new Bean().set("name", "被督办").set("count", "18").set("unit", "次"));
		outBean.setData(new Bean().set("data", blList));
		
		return outBean;
	}
	
	/**
	 * 部门主办环形图
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getZbRingCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		List<Bean> ringList = new ArrayList<Bean>();
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		String where = " and S_WF_INST in (select PI_ID from SY_WFE_NODE_INST_ALL_V "
				+ "where NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'"
						+ " and node_child_type='1'"
						+ " and to_user_id in (select user_code from SY_BASE_USER_V where "
						+ "tdept_code='" + currUser.getTDeptCode() + "'))";
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SELECT, "COUNT(GW_ID) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + " and S_WF_STATE='1'");
		Bean countWj = ServDao.find(SERV_ID, queryBean); //主办文件总数
		int countWJ = countWj.getInt("COUNT_");
		
		queryBean.set(Constant.PARAM_WHERE, where + " and S_WF_STATE='2'");
		Bean countYj = ServDao.find(SERV_ID, queryBean);
		int countYJ = countYj.getInt("COUNT_");
		
		ringList.add(new Bean().set("name", "已结" + countYJ + "件")
				.set("value", DataStatsUtil.getPercent(new Double(countYJ), new Double(countYJ + countWJ), 2)));
		ringList.add(new Bean().set("name", "未结" + countWJ + "件")
				.set("value", DataStatsUtil.getPercent(new Double(countWJ), new Double(countYJ + countWJ), 2)));
		
		outBean.setData(new Bean().set("data", ringList));
		return outBean;
	}
	
	/**
	 * 部门柱状图
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getZbBarCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		List<String> xRepList = new ArrayList<String>();
		List<Integer> yRepListFW = new ArrayList<Integer>();
		List<Integer> yRepListQB = new ArrayList<Integer>();
		
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		String tdeptCode = currUser.getTDeptCode();
		
		String where = " and S_WF_INST in (select PI_ID from SY_WFE_NODE_INST_ALL_V "
				+ "where NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'"
						+ " and node_child_type ='1'"
						+ " and to_user_id in (select user_code from SY_BASE_USER_V where "
						+ "dept_code='";
		
		List<Bean> deptList = DataStatsUtil.getDeptList(tdeptCode);
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SELECT, "COUNT(GW_ID) COUNT_");
		for(Bean dept : deptList){
			xRepList.add(dept.getStr("DEPT_NAME"));
			
			queryBean.set(Constant.PARAM_WHERE, where  + dept.getStr("DEPT_CODE") + "')) and TMPL_TYPE_CODE='OA_GW_GONGWEN_FW'");
			Bean countFW = ServDao.find(SERV_ID, queryBean);
			yRepListFW.add(countFW.getInt("COUNT_"));
			
			queryBean.set(Constant.PARAM_WHERE, where  + dept.getStr("DEPT_CODE") + "')) and TMPL_TYPE_CODE='OA_GW_GONGWEN_QB'");
			Bean countQB = ServDao.find(SERV_ID, queryBean);
			yRepListQB.add(countQB.getInt("COUNT_"));
		}
		
		outBean.setData(new Bean().set("x", xRepList).set("fwData", yRepListFW).set("qbData", yRepListQB));
		return outBean;
	}
	
	/**
	 * 主办统计列表
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getZbCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		int type = reqData.getInt("type");
		
		List<Bean> list = new ArrayList<Bean>();
		
		String where = " and NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'"
				+ " and node_child_type ='1' and to_tdept_code ='" + currUser.getTDeptCode() + "'";
		
		Bean repBean = null;
		if(type == 1){ //年度
			for(int i = 0 ; i < 12; i++){
				repBean = new Bean();
				int sort = i + 1;
				repBean.set("id", sort);
				String monthStr = String.valueOf(sort);
				if(sort <10){
					monthStr = "0" + monthStr;
				}
				String time = " and substr(NODE_BTIME,6,2) ='" + monthStr + "'";
				genRep(where, repBean, time);
				list.add(repBean);
			}
		} else if (type == 2){ //本周
			String weekDay = bTime.substring(0, 10);
			for(int i = 0 ; i < 7; i++){
				repBean = new Bean();
				int sort = i + 1;
				repBean.set("id", sort);
				String time = " and substr(NODE_BTIME,1,10) ='" + weekDay + "'";
				genRep(where, repBean, time);
				list.add(repBean);
				weekDay = DateUtils.formatDate(DataStatsUtil.getNextDay(DataStatsUtil.strToDate(weekDay)));
			}
		} else if (type == 3){ //本月
			String montDay = bTime.substring(0, 10);
			int days = DataStatsUtil.getMonthDays(montDay);
			for(int i = 0 ; i < days; i++){
				repBean = new Bean();
				int sort = i+1;
				repBean.set("id", sort);
				String time = " and substr(NODE_BTIME,1,10) ='" + montDay + "'";
				genRep(where, repBean, time);
				list.add(repBean);
				montDay = DateUtils.formatDate(DataStatsUtil.getNextDay(DataStatsUtil.strToDate(montDay)));
			}
		}
		//合计
		repBean = new Bean();
		repBean.set("id", "合计");
		genRep(where, repBean, "");
		list.add(repBean);
		
		outBean.setData(new Bean().set("listData", list));
		return outBean;
	}
	/**
	 * 根据条件分项统计
	 * @param where
	 * @param repBean
	 * @param time
	 */
	public void genRep(String where, Bean repBean, String time){
		ParamBean queryBean = new ParamBean();
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT (DISTINCT gw_id) COUNT_,sum(node_days) DAYS");
		queryBean.set(Constant.PARAM_WHERE, where + time);
		Bean countALL = ServDao.find(N_SERV_ID, queryBean);
		repBean.set("all", countALL.getInt("COUNT_") == 0 ? "" : countALL.getInt("COUNT_"));
		repBean.set("avg", DataStatsUtil.getAvg(countALL.getDouble("DAYS") / 60.00,countALL.getDouble("COUNT_"), 2));
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT (DISTINCT gw_id) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + time + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_SW'");
		Bean countSW = ServDao.find(N_SERV_ID, queryBean);
		repBean.set("sw", countSW.getInt("COUNT_") == 0 ? "" : countSW.getInt("COUNT_"));
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT (DISTINCT gw_id) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + time + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_FW'");
		Bean countFW = ServDao.find(N_SERV_ID, queryBean);
		repBean.set("fw", countFW.getInt("COUNT_") == 0 ? "" : countFW.getInt("COUNT_"));
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT (DISTINCT gw_id) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + time + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_QB'");
		Bean countQB = ServDao.find(N_SERV_ID, queryBean);
		repBean.set("qb", countQB.getInt("COUNT_") == 0 ? "" : countQB.getInt("COUNT_"));
		
		repBean.set("cq", "");
		repBean.set("yq", "");
	}
	
	/**
	 * 协办会签统计
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getHqBarCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		List<String> xRepList = new ArrayList<String>();
		List<Integer> yRepListXB = new ArrayList<Integer>();
		List<Integer> yRepListHQ = new ArrayList<Integer>();
		
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		String tdeptCode = currUser.getTDeptCode();
		
		String where = " and S_WF_INST in (select PI_ID from SY_WFE_NODE_INST_ALL_V "
				+ "where NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'";
		
		List<Bean> deptList = DataStatsUtil.getDeptList(tdeptCode);
		ParamBean queryBean = new ParamBean();
		queryBean.set(Constant.PARAM_SELECT, "COUNT (DISTINCT gw_id) COUNT_");
		for(Bean dept : deptList){
			xRepList.add(dept.getStr("DEPT_NAME"));
			
			queryBean.set(Constant.PARAM_WHERE, where +" and node_child_type ='2' and to_user_id in (select user_code from SY_BASE_USER_V where dept_code='"  + dept.getStr("DEPT_CODE") + "'))");
			Bean countXB = ServDao.find(SERV_ID, queryBean);
			yRepListXB.add(countXB.getInt("COUNT_"));
			
			queryBean.set(Constant.PARAM_WHERE, where +" and node_child_type ='3' and to_user_id in (select user_code from SY_BASE_USER_V where dept_code='"  + dept.getStr("DEPT_CODE") + "'))");
			Bean countHQ = ServDao.find(SERV_ID, queryBean);
			yRepListHQ.add(countHQ.getInt("COUNT_"));
		}
		
		outBean.setData(new Bean().set("x", xRepList).set("hqData", yRepListXB).set("xbData", yRepListHQ));
		return outBean;
	}

	/**
	 * 协办统计列表
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getHqCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		int type = reqData.getInt("type");
		
		List<Bean> list = new ArrayList<Bean>();
		
		String whereXB = " and NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'"
				+ " and node_child_type ='2' and to_tdept_code ='" + currUser.getTDeptCode() + "'";
		String whereHQ = " and NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'"
				+ " and node_child_type ='3' and to_tdept_code ='" + currUser.getTDeptCode() + "'";
		String where = " and NODE_BTIME >='" + bTime + "' and NODE_BTIME <='" + eTime + "'"
				+ " and node_child_type in ('2','3') and to_tdept_code ='" + currUser.getTDeptCode() + "'";
		
		Bean repBean = null;
		if(type == 1){ //年度
			for(int i = 0 ; i < 12; i++){
				repBean = new Bean();
				int sort = i + 1;
				repBean.set("id", sort);
				String monthStr = String.valueOf(sort);
				if(sort <10){
					monthStr = "0" + monthStr;
				}
				String time = " and substr(NODE_BTIME,6,2) ='" + monthStr + "'";
				genRep(where, whereXB, whereHQ, repBean, time);
				list.add(repBean);
			}
		} else if (type == 2){ //本周
			String weekDay = bTime.substring(0, 10);
			for(int i = 0 ; i < 7; i++){
				repBean = new Bean();
				int sort = i + 1;
				repBean.set("id", sort);
				String time = " and substr(NODE_BTIME,1,10) ='" + weekDay + "'";
				genRep(where, whereXB, whereHQ, repBean, time);
				list.add(repBean);
				weekDay = DateUtils.formatDate(DataStatsUtil.getNextDay(DataStatsUtil.strToDate(weekDay)));
			}
		} else if (type == 3){ //本月
			String montDay = bTime.substring(0, 10);
			int days = DataStatsUtil.getMonthDays(montDay);
			for(int i = 0 ; i < days; i++){
				repBean = new Bean();
				int sort = i+1;
				repBean.set("id", sort);
				String time = " and substr(NODE_BTIME,1,10) ='" + montDay + "'";
				genRep(where, whereXB, whereHQ, repBean, time);
				list.add(repBean);
				montDay = DateUtils.formatDate(DataStatsUtil.getNextDay(DataStatsUtil.strToDate(montDay)));
			}
		}
		//合计
		repBean = new Bean();
		repBean.set("id", "合计");
		genRep(where, repBean, "");
		list.add(repBean);
		
		outBean.setData(new Bean().set("listData", list));
		return outBean;
	}
	/**
	 * 根据条件分项统计
	 * @param where
	 * @param repBean
	 * @param time
	 */
	public void genRep(String where, String xbWhere, String hqWhere, Bean repBean, String time){
		ParamBean queryBean = new ParamBean();
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT (DISTINCT gw_id) COUNT_,sum(node_days) DAYS");
		queryBean.set(Constant.PARAM_WHERE, where + time);
		Bean countALL = ServDao.find(N_SERV_ID, queryBean);
		repBean.set("all", countALL.getInt("COUNT_") == 0 ? "" : countALL.getInt("COUNT_"));
		repBean.set("avg", DataStatsUtil.getAvg(countALL.getDouble("DAYS") / 60.00,countALL.getDouble("COUNT_"), 2));
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT (DISTINCT gw_id) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, xbWhere + time + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_SW'");
		Bean countSW = ServDao.find(N_SERV_ID, queryBean);
		repBean.set("sw", countSW.getInt("COUNT_") == 0 ? "" : countSW.getInt("COUNT_"));
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT (DISTINCT gw_id) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, hqWhere + time + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_FW'");
		Bean countFW = ServDao.find(N_SERV_ID, queryBean);
		repBean.set("fw", countFW.getInt("COUNT_") == 0 ? "" : countFW.getInt("COUNT_"));
		
		queryBean.set(Constant.PARAM_SELECT, "COUNT (DISTINCT gw_id) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, hqWhere + time + " and TMPL_TYPE_CODE='OA_GW_GONGWEN_QB'");
		Bean countQB = ServDao.find(N_SERV_ID, queryBean);
		repBean.set("qb", countQB.getInt("COUNT_") == 0 ? "" : countQB.getInt("COUNT_"));
		
		repBean.set("cq", "");
		repBean.set("yq", "");
	}
	
	/**
	 * 部门文件处理情况
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getGwDealSitu(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
		List<Bean> repList = new ArrayList<Bean>();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		
		String tdeptCode = currUser.getTDeptCode();
		String where = " and node_btime >='" + bTime + "' and node_btime <='" + eTime + "'"
				+ " and to_tdept_code='" + tdeptCode + "'";
		
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_WHERE, " and TDEPT_CODE='" + tdeptCode + "'");
		List<Bean> userList = ServDao.finds(U_SERV_ID, queryBean);
		
		Bean repBean = null;
		for(Bean user : userList){
			repBean = new Bean();
			repBean.set("deptName", user.getStr("DEPT_NAME"));
			repBean.set("userName", user.getStr("USER_NAME"));
			
			String userWhere = " and to_user_id='" + user.getStr("USER_CODE") + "'";
			
			genRep(where + userWhere, repBean);
			
			repList.add(repBean);
		}
		//合计
		repBean = new Bean();
		repBean.set("deptName", currUser.getTDeptName());
		repBean.set("userName", "合计");
		genRep(where, repBean);
		repList.add(repBean);
		
		outBean.setData(new Bean().set("dataList", repList));
		return outBean;
	}
	/**
	 * 分项统计
	 * @param user
	 * @param where
	 * @param repBean
	 */
	public void genRep(String where, Bean repBean){
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_SELECT, "count(distinct gw_id) COUNT_");
		queryBean.set(Constant.PARAM_WHERE, where + " and node_child_type ='1'");
		Bean zbBean = ServDao.find(N_SERV_ID, queryBean);
		int zbNum = zbBean.getInt("COUNT_");
		
		queryBean .set(Constant.PARAM_WHERE, where + " and node_child_type in ('2', '3')");
		Bean hqBean = ServDao.find(N_SERV_ID, queryBean);
		int hqNum = hqBean.getInt("COUNT_");
		
		queryBean.set(Constant.PARAM_SELECT, "sum(node_days) DAYS");
		queryBean.set(Constant.PARAM_WHERE, where + " and node_child_type <> 0");
		Bean days = ServDao.find(N_SERV_ID, queryBean);
		String avgTime = DataStatsUtil.getAvg(days.getDouble("DAYS"), (double)(zbNum + hqNum), 2);
		
		double score = 0.00;
		int cqNum = 0;
		int cbNum = 0;
		int dbNum = 0;
		
		repBean.set("zbNum", zbNum == 0 ? "" : zbNum);
		repBean.set("hqNum", hqNum == 0 ? "" : hqNum);
		repBean.set("avgTime", avgTime);
		repBean.set("score", score == 0 ? "" : score);
		repBean.set("cqNum", cqNum == 0 ? "" : cqNum);
		repBean.set("cbNum", cbNum == 0 ? "" : cbNum);
		repBean.set("dbNum", dbNum == 0 ? "" : dbNum);
	}
	
	/**
	 * 排行榜统计
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getGwPhbCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		UserBean currUser = Context.getUserBean();
		
		List<Bean> zbList = new ArrayList<Bean>();
		List<Bean> xbList = new ArrayList<Bean>();
		List<Bean> timeList = new ArrayList<Bean>();
		List<Bean> cqList = new ArrayList<Bean>();
		
		List<Bean> phbList = new ArrayList<Bean>();
		
		String tdeptCode = currUser.getTDeptCode();
		//主办文件排行
		String zbSql = "select count(distinct gw_id) COUNT_, user_name from sy_wfe_node_inst_gw_v a, sy_org_user b where node_btime>='" + bTime + "' and node_btime<='" + eTime + "'"
				+ " and to_tdept_code='" + tdeptCode + "' and a.to_user_id=b.user_code and a.node_child_type='1' group by user_name order by COUNT_ desc";
		List<Bean> zbRepList = Context.getExecutor().query(zbSql);
		setPhbList(zbList, zbRepList);
		
		String hqSql = "select count(distinct gw_id) COUNT_, user_name from sy_wfe_node_inst_gw_v a, sy_org_user b where node_btime>='" + bTime + "' and node_btime<='" + eTime + "'"
				+ " and to_tdept_code='" + tdeptCode + "' and a.to_user_id=b.user_code and node_child_type in ('2', '3') group by user_name order by COUNT_ desc";
		List<Bean> hqRepList = Context.getExecutor().query(hqSql);
		setPhbList(xbList, hqRepList);
		
		String timeSql = "select round(sum(node_days)/(60*count(distinct gw_id)), 2) COUNT_, user_name from sy_wfe_node_inst_gw_v a, sy_org_user b where node_btime >='" + bTime + "' and node_btime <='" + eTime + "'"
				+ " and to_tdept_code='" + tdeptCode + "' and a.to_user_id=b.user_code and node_child_type in ('1','2','3') group by user_name order by COUNT_ desc";
		List<Bean> timeRepList = Context.getExecutor().query(timeSql);
		setPhbList(timeList, timeRepList);
		
		String cqSql = "select count(distinct gw_id) COUNT_, user_name from sy_wfe_node_inst_gw_v a, sy_org_user b where node_btime>='" + bTime + "' and node_btime<='" + eTime + "'"
				+ " and to_tdept_code='" + tdeptCode + "' and a.to_user_id=b.user_code and node_child_type in ('1','2','3') and a.delay_time > 0 group by user_name order by COUNT_ desc";
		List<Bean> cqRepList = Context.getExecutor().query(cqSql);
		setPhbList(cqList, cqRepList);
		
		phbList.add(new Bean().set("id", "rankZb").set("title", "主办文件（件）").set("content", zbList));
		phbList.add(new Bean().set("id", "rankHq").set("title", "协办会签（件）").set("content", xbList));
		phbList.add(new Bean().set("id", "rankAvgTime").set("title", "平均处理时间（小时/件）").set("content", timeList));
		phbList.add(new Bean().set("id", "rankCq").set("title", "超期/延期（件）").set("content", cqList));
		outBean.setData(new Bean().set("phbList", phbList));
		
		return outBean;
	}
	/**
	 * 参数结构设置
	 * @param phbList
	 * @param dataList
	 */
	public void setPhbList(List<Bean> phbList, List<Bean> dataList){
		int size = dataList.size();
		if(size >= 5){
			size = 5;
		}
		for(int i = 0; i < size; i++){
			Bean data = dataList.get(i);
			phbList.add(new Bean().set("name", data.getStr("USER_NAME")).set("value", data.getStr("COUNT_")));
		}
	}
	/**
	 * 部门公文时效质量情况
	 * @param reqData
	 * @return
	 */
	@Override
	public ApiOutBean getGwSxzlCount(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
		List<Bean> repList = new ArrayList<Bean>();
			
		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		String type = reqData.getStr("type");
		
		String tdeptCode = currUser.getTDeptCode();
		String where = "and gw_id in (select gw_id from SY_WFE_NODE_INST_GW_V where node_btime >='" + bTime + "' and node_btime <='" + eTime + "'"
				+ " and to_tdept_code='" + tdeptCode + "' and node_child_type in ('" + type.replaceAll(",", "','") + "'))";
		
		Bean queryBean = new Bean();
		queryBean.set(Constant.PARAM_WHERE, where);
		List<Bean> gwList = ServDao.finds(SERV_ID, queryBean);
		
		Bean repBean = null;
		for(int i = 0; i < gwList.size(); i++){
			repBean = new Bean();
			Bean gwBean = gwList.get(i);
			
			repBean.set("id", gwBean.getId());
			repBean.set("sort", i + 1);
			repBean.set("title", gwBean.getStr("GW_TITLE"));
			repBean.set("gwCode", gwBean.getStr("GW_FULL_CODE"));
			repBean.set("gwType", getGwType(gwBean.getStr("TMPL_TYPE_CODE")));
			repBean.set("userName", gwBean.getStr("S_UNAME"));
			
			repBean.set("cqTime", "");
			repBean.set("cqPercent", "");
			repBean.set("quality", "");
			
			repList.add(repBean);
		}
		
		outBean.setData(new Bean().set("dataList", repList));
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
	
	

	public ApiOutBean getDeptTabDataByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();

		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' " + "and TO_TDEPT_CODE = '" + currUser.getTDeptCode() + "' " + "AND NODE_CHILD_TYPE<>'0'";
		// 主办、会签协办
		String sql = "select  TMPL_TYPE_CODE,COUNT (DISTINCT gw_id) gw_count ,SUM(NODE_DAYS) node_days ,DECODE (node_child_type,  0,'流经' , 1, '主办',  2, '会签',  3, '会签') OPERATE"
				+ " from SY_WFE_NODE_INST_GW_V  " + strWhere
				+ " GROUP BY TMPL_TYPE_CODE ,DECODE (node_child_type,  0,'流经' , 1, '主办',  2, '会签',  3, '会签')";
		List<Bean> sqlList = Transaction.getExecutor().query(sql);

		String yuqiSql = "SELECT count(delay_time) delay_time " + "FROM SY_WFE_NODE_INST_GW_V " + strWhere
				+ " and  delay_time >0";
		Bean yuqiBean = Transaction.getExecutor().queryOne(yuqiSql);
		if (yuqiBean == null) {
			yuqiBean = new Bean();
		}

		List<Map<String, String>> zbDataList = new ArrayList<Map<String, String>>();
		List<Map<String, String>> hqxbDataList = new ArrayList<Map<String, String>>();
		List<Map<String, String>> gzsxDataList = new ArrayList<Map<String, String>>();
		float zbTotalTime = 0;
		float hqxbTotalTime = 0;
		float zbCount = 0;
		float hqxbCount = 0;
		// 协办会签
		Map<String, String> hqxbTotalMap = new HashMap<String, String>();
		hqxbTotalMap.put("name", "协办会签总数");
		hqxbTotalMap.put("unit", "件");
		hqxbTotalMap.put("count", "0");
		Map<String, String> swxbMap = new HashMap<String, String>();
		swxbMap.put("name", "收文协办");
		swxbMap.put("unit", "件");
		swxbMap.put("count", "0");
		Map<String, String> fwhqMap = new HashMap<String, String>();
		fwhqMap.put("name", "发文会签");
		fwhqMap.put("unit", "件");
		fwhqMap.put("count", "0");
		Map<String, String> qbhqMap = new HashMap<String, String>();
		qbhqMap.put("name", "签报会签");
		qbhqMap.put("unit", "件");
		qbhqMap.put("count", "0");

		// 主办文件
		Map<String, String> zbTotalMap = new HashMap<String, String>();
		zbTotalMap.put("name", "主办文件总数");
		zbTotalMap.put("unit", "件");
		zbTotalMap.put("count", "0");
		Map<String, String> swcbMap = new HashMap<String, String>();
		swcbMap.put("name", "收文承办");
		swcbMap.put("unit", "件");
		swcbMap.put("count", "0");
		Map<String, String> fwngMap = new HashMap<String, String>();
		fwngMap.put("name", "发文拟稿");
		fwngMap.put("unit", "件");
		fwngMap.put("count", "0");
		Map<String, String> qbngMap = new HashMap<String, String>();
		qbngMap.put("name", "签报");
		qbngMap.put("unit", "件");
		qbngMap.put("count", "0");

		for (int i = 0; i < sqlList.size(); i++) {
			Bean tempBean = (Bean) sqlList.get(i);
			if (tempBean.get("OPERATE").equals("会签")) {
				hqxbTotalTime = DataStatsUtil.sumFunc(hqxbTotalTime,
						DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
				hqxbCount = DataStatsUtil.sumFunc(hqxbCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
				if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
					fwhqMap.put("count", String.valueOf(tempBean.get("GW_COUNT")));
				} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
					swxbMap.put("count", String.valueOf(tempBean.get("GW_COUNT")));
				} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
					qbhqMap.put("count", String.valueOf(tempBean.get("GW_COUNT")));
				}

			} else if (tempBean.get("OPERATE").equals("主办")) {
				zbTotalTime = DataStatsUtil.sumFunc(zbTotalTime,
						DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
				zbCount = DataStatsUtil.sumFunc(zbCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
				if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
					fwngMap.put("count", String.valueOf(tempBean.get("GW_COUNT")));
				} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
					swcbMap.put("count", String.valueOf(tempBean.get("GW_COUNT")));
				} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
					qbngMap.put("count", String.valueOf(tempBean.get("GW_COUNT")));
				}
				fwngMap.put("totalTime",
						String.valueOf(DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(fwngMap.get("totalTime")),
								DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")))));
				swcbMap.put("totalTime",
						String.valueOf(DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(swcbMap.get("totalTime")),
								DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")))));
				qbngMap.put("totalTime",
						String.valueOf(DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(qbngMap.get("totalTime")),
								DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")))));

			}
		}
		hqxbDataList.add(qbhqMap);
		hqxbDataList.add(swxbMap);
		hqxbDataList.add(fwhqMap);

		zbDataList.add(fwngMap);
		zbDataList.add(swcbMap);
		zbDataList.add(qbngMap);

		for (int i = 0; i < zbDataList.size(); i++) {
			Map<String, String> map = (HashMap) zbDataList.get(i);
			zbTotalMap.put("count",
					String.valueOf((int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(zbTotalMap.get("count")),
							DataStatsUtil.parseFloat(map.get("count")))));
		}
		zbDataList.add(0, zbTotalMap);

		for (int i = 0; i < hqxbDataList.size(); i++) {
			Map<String, String> map = (HashMap) hqxbDataList.get(i);
			hqxbTotalMap.put("count",
					String.valueOf((int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(hqxbTotalMap.get("count")),
							DataStatsUtil.parseFloat(map.get("count")))));
		}
		hqxbDataList.add(0, hqxbTotalMap);

		List<List> zbRtnList = new ArrayList<List>();
		zbRtnList.add(zbDataList);
		Bean zbBean = new Bean();
		zbBean.set("data", zbRtnList);
		zbBean.set("title", "主办文件");

		List<List> hqxbRtnList = new ArrayList<List>();
		hqxbRtnList.add(hqxbDataList);
		Bean hqxbBean = new Bean();
		hqxbBean.set("data", hqxbRtnList);
		hqxbBean.set("title", "协办文件");

		// 工作时效
		Map<String, String> wjclMap = new HashMap<String, String>();
		wjclMap.put("name", "文件处理总时间");
		wjclMap.put("count",
				String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.sumFunc(zbTotalTime, hqxbTotalTime), 60)));
		wjclMap.put("unit", "小时");
		gzsxDataList.add(wjclMap);
		Map<String, String> zbAveTimeMap = new HashMap<String, String>();
		zbAveTimeMap.put("name", "主办文件平均时间");
		String tmp = String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.divFunc(zbTotalTime, zbCount), 60));
		zbAveTimeMap.put("count", tmp);
		zbAveTimeMap.put("unit", "件/小时");
		gzsxDataList.add(zbAveTimeMap);
		Map<String, String> hqxbAveTimeMap = new HashMap<String, String>();
		hqxbAveTimeMap.put("name", "会签协办平均时间");
		String tmpC = String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.divFunc(hqxbTotalTime, hqxbCount), 60));
		hqxbAveTimeMap.put("count", tmpC);
		hqxbAveTimeMap.put("unit", "件/小时");
		gzsxDataList.add(hqxbAveTimeMap);
		Map<String, String> yuqiMap = new HashMap<String, String>();
		yuqiMap.put("name", "部门逾期工作");
		yuqiMap.put("count", yuqiBean.getStr("DELAY_TIME"));
		yuqiMap.put("color", "rgb(189,34,25)");
		yuqiMap.put("unit", "件");
		gzsxDataList.add(yuqiMap);

		/// 催办和督办 假数据
		List<Map<String, String>> cbdbRtnList = new ArrayList<Map<String, String>>();
		Map<String, String> cbMap = new HashMap<String, String>();
		cbMap.put("name", "文件办理被催办");
		cbMap.put("count", "94");
		cbMap.put("color", "rgb(237,171,38)");
		cbMap.put("fontsize", "36px");
		cbMap.put("unit", "件");
		cbdbRtnList.add(cbMap);
		Map<String, String> dbMap = new HashMap<String, String>();
		dbMap.put("name", "公文办理被督办");
		dbMap.put("count", "18");
		dbMap.put("color", "rgb(189,34,25)");
		dbMap.put("fontsize", "36px");
		dbMap.put("unit", "次");
		cbdbRtnList.add(dbMap);

		List<List> gzsxRtnList = new ArrayList<List>();
		gzsxRtnList.add(gzsxDataList);
		gzsxRtnList.add(cbdbRtnList);
		Bean yuqiRtnBean = new Bean();
		yuqiRtnBean.set("data", gzsxRtnList);
		yuqiRtnBean.set("title", "工作时效");

		Bean rtnBean = new Bean();
		rtnBean.set("zb", zbBean);
		rtnBean.set("hqxb", hqxbBean);
		rtnBean.set("gzsx", yuqiRtnBean);

		outBean.setData(rtnBean);
		return outBean;
	}

	// 主办公文
	public ApiOutBean getDeptStatsDataZBByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");
		

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);
		
		int type = reqData.getInt("type");// 1:年 2:周 3:月
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

		String strWhere = "where node_btime >= '" + beginTime + " 00:00:00' AND node_btime <= '" + endTime
				+ " 23:59:59' " + "and TO_TDEPT_CODE = '" + currUser.getTDeptCode() + "' AND NODE_CHILD_TYPE='1' ";
		// 主办文件table表
		String sql = "select TMPL_TYPE_CODE,"+periodField +" OPERIOD ,COUNT (DISTINCT gw_id) gw_count,sum(node_days) node_days FROM SY_WFE_NODE_INST_GW_V "
				+ strWhere + " GROUP BY TMPL_TYPE_CODE,"+periodField+" ORDER BY " +periodField;
		List list = Transaction.getExecutor().query(sql);

		List aList = new ArrayList();
		List bList = new ArrayList();
		List cList = new ArrayList();
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
		
		// 发文数量、收文数量、签报数量，合计用时、文件总数量
		if(type == 1){
			for (int i = 0; i < aList.size(); i++) {
				Map map = (Map) aList.get(i);
				for (int j = 0; j < list.size(); j++) {
					Bean tempBean = (Bean) list.get(j);
					if (DataStatsUtil.parseInt(String.valueOf(map.get("id"))) == DataStatsUtil
							.parseInt(tempBean.getStr("OPERIOD"))) {
						float totaltime = DataStatsUtil.sumFunc(
								DataStatsUtil.parseFloat(String.valueOf(map.get("totaltime"))),
								tempBean.getInt("NODE_DAYS"));
						if (totaltime > 0) {
							map.put("totaltime", totaltime);
						}
						if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
							map.put("fwcount", tempBean.get("GW_COUNT"));
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
							map.put("swcount", tempBean.get("GW_COUNT"));
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
							map.put("qbcount", tempBean.get("GW_COUNT"));
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
						float totaltime = DataStatsUtil.sumFunc(
								DataStatsUtil.parseFloat(String.valueOf(map.get("totaltime"))),
								tempBean.getInt("NODE_DAYS"));
						if (totaltime > 0) {
							map.put("totaltime", totaltime);
						}
						if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
							map.put("fwcount", tempBean.get("GW_COUNT"));
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
							map.put("swcount", tempBean.get("GW_COUNT"));
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
							map.put("qbcount", tempBean.get("GW_COUNT"));
						}
					}
				}
				bList.add(map);
			}
		}
		
		// 文件总数、平均时间、合计用时换算小时
		for (int i = 0; i < bList.size(); i++) {
			Map map = (Map) bList.get(i);
			int sum = DataStatsUtil.parseInt(String.valueOf(map.get("fwcount")))
					+ DataStatsUtil.parseInt(String.valueOf(map.get("swcount")))
					+ DataStatsUtil.parseInt(String.valueOf(map.get("qbcount")));
			map.put("filecount", sum > 0 ? sum : "");
			float totaltime = DataStatsUtil.divFunc(DataStatsUtil.parseFloat(String.valueOf(map.get("totaltime"))), 60);
			map.put("totaltime", totaltime > 0 ? totaltime : "");
			float avgTime = DataStatsUtil.divFunc(DataStatsUtil.parseFloat(String.valueOf(map.get("totaltime"))),
					DataStatsUtil.parseInt(String.valueOf(map.get("filecount"))));
			map.put("avgtime", avgTime > 0 ? avgTime : "");
			cList.add(map);
		}
		// 合计
		Map total = new HashMap();
		for (int i = 0; i < cList.size(); i++) {
			Map map = (Map) cList.get(i);
			total.put("id", "合计");
			total.put("swcount",
					(int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("swcount"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("swcount")))));
			total.put("fwcount",
					(int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("fwcount"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("fwcount")))));
			total.put("qbcount",
					(int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("qbcount"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("qbcount")))));
			total.put("filecount",
					(int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("filecount"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("filecount")))));
			total.put("avgtime", DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("avgtime"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("avgtime")))));
			total.put("totaltime",
					DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("totaltime"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("totaltime")))));
		}
		cList.add(total);

		outBean.setData(new Bean().set("data", cList));

		return outBean;
	}

	// 协办会签
	@Override
	public ApiOutBean getDeptStatsDataHQByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		int type = reqData.getInt("type");// 1:年 2:周 3:月
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
				+ " 23:59:59' " + "and TO_TDEPT_CODE = '" + currUser.getTDeptCode()
				+ "' AND  (NODE_CHILD_TYPE='2' or NODE_CHILD_TYPE='3' )";

		String dataSql = "SELECT TMPL_TYPE_CODE,"+periodField+" OPERIOD, "
				+ "DECODE(node_child_type,  0, '流经',  1, '主办',  2, '协办',  3, '会签') operate, "
				+ "COUNT (DISTINCT gw_id) gw_count,sum(node_days) node_days " + "FROM SY_WFE_NODE_INST_GW_V " + strWhere
				+ "GROUP BY TMPL_TYPE_CODE,"+periodField+",DECODE(node_child_type,  0, '流经',  1, '主办',  2, '协办',  3, '会签') "
				+ "ORDER BY "+periodField;
		List list = Transaction.getExecutor().query(dataSql);

		String yuqiSql = "SELECT  "+periodField+" OPERIOD,count(delay_time)  delay_time " + "FROM SY_WFE_NODE_INST_GW_V " + strWhere
				+ " and  delay_time >0" + "GROUP BY " +periodField+ " ORDER BY "+periodField;
		List yuqiList = Transaction.getExecutor().query(yuqiSql);

		List aList = new ArrayList();
		List bList = new ArrayList();
		List cList = new ArrayList();
		List dList = new ArrayList();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// 月份
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
		// 合计用时、会签、主办
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
							map.put("totaltime", totalTime);
						}
						if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
							if (tempBean.get("OPERATE").equals("会签")) {
								map.put("hqfw", tempBean.get("GW_COUNT"));
							}
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
							if (tempBean.get("OPERATE").equals("协办")) {
								map.put("xbsw", tempBean.get("GW_COUNT"));
							}
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
							if (tempBean.get("OPERATE").equals("会签")) {
								map.put("hqqb", tempBean.get("GW_COUNT"));
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
							map.put("totaltime", totalTime);
						}
						if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
							if (tempBean.get("OPERATE").equals("会签")) {
								map.put("hqfw", tempBean.get("GW_COUNT"));
							}
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
							if (tempBean.get("OPERATE").equals("协办")) {
								map.put("xbsw", tempBean.get("GW_COUNT"));
							}
						} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
							if (tempBean.get("OPERATE").equals("会签")) {
								map.put("hqqb", tempBean.get("GW_COUNT"));
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
			int sum = DataStatsUtil.parseInt(String.valueOf(map.get("hqfw")))
					+ DataStatsUtil.parseInt(String.valueOf(map.get("hqqb")))
					+ DataStatsUtil.parseInt(String.valueOf(map.get("xbsw")));
			map.put("filecount", sum > 0 ? sum : "");
			float totalTime = DataStatsUtil.divFunc(DataStatsUtil.parseFloat(String.valueOf(map.get("totaltime"))), 60);
			map.put("totaltime", totalTime > 0 ? totalTime : "");
			float avgTime = DataStatsUtil.divFunc(DataStatsUtil.parseFloat(String.valueOf(map.get("totaltime"))),
					DataStatsUtil.parseInt(String.valueOf(map.get("filecount"))));
			map.put("avgtime", avgTime > 0 ? avgTime : "");
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
						map.put("overcount", tempBean.get("DELAY_TIME"));
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
						map.put("overcount", tempBean.get("DELAY_TIME"));
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
			total.put("hqfw", (int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("hqfw"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("hqfw")))));
			total.put("xbsw", (int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("xbsw"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("xbsw")))));
			total.put("hqqb", (int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("hqqb"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("hqqb")))));
			total.put("filecount",
					(int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("filecount"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("filecount")))));
			total.put("avgtime", DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("avgtime"))),
					DataStatsUtil.parseFloat(String.valueOf(map.get("avgtime")))));
			total.put("totaltime",
					DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("totaltime"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("totaltime")))));
			total.put("overcount",
					(int) DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("overcount"))),
							DataStatsUtil.parseFloat(String.valueOf(map.get("overcount")))));
		} 
		dList.add(total);

		outBean.setData(new Bean().set("data", dList));

		return outBean;
	}

	//排行榜数据
	@Override
	public ApiOutBean getDeptStatsDataPHBByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' and node_child_type <>'0' ";

		String sql = " SELECT B.USER_NAME,B.USER_IMG_SRC,C.DEPT_NAME,a.* FROM "
				+ " (SELECT TO_USER_ID,TO_DEPT_CODE,DECODE (a.node_child_type,0, '流经', 1, '主办', 2, '会签',3, '会签') operate,"
				+ " COUNT (DISTINCT a.gw_id) gw_count,SUM (a.node_days) node_days FROM SY_WFE_NODE_INST_GW_V a "
				+ strWhere
				+ " GROUP BY TO_USER_ID,TO_DEPT_CODE,DECODE (a.node_child_type, 0, '流经',1, '主办',2, '会签', 3, '会签')) a, "
				+ " sy_base_user_v b,sy_org_dept c " + " WHERE b.tdept_code = '" + currUser.getTDeptCode()
				+ "' AND A.TO_USER_ID = b.USER_CODE AND A.TO_DEPT_CODE = C.DEPT_CODE ORDER BY c.dept_sort, B.USER_SORT ";
		List list = Transaction.getExecutor().query(sql);

		List aList = new ArrayList<HashMap>();
		List bList = new ArrayList<HashMap>();
		for (int i = 0; i < list.size(); i++) {
			Bean tempBean = (Bean) list.get(i);
			Map<String, String> tempMap = new HashMap<String, String>();
			tempMap.put("username", "<img style='margin-right: 5px;' width='20' src='http://cochat.cn/file/ICON_"
					+ tempBean.getStr("USER_IMG_SRC") + "'>" + tempBean.getStr("USER_NAME"));
			tempMap.put("usercode", tempBean.getStr("TO_USER_ID"));
			if (!aList.contains(tempMap)) {
				aList.add(tempMap);
			}
		}

		for (int i = 0; i < aList.size(); i++) {
			Map map = (Map) aList.get(i);
			float totalTime = 0;
			float gwcount = 0;
			for (int j = 0; j < list.size(); j++) {
				Bean tempBean = (Bean) list.get(j);
				if (String.valueOf(map.get("usercode")).equals(tempBean.getStr("TO_USER_ID"))) {
					map.put("deptname", tempBean.getStr("DEPT_NAME"));
					totalTime = DataStatsUtil.sumFunc(totalTime,
							DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
					gwcount = DataStatsUtil.sumFunc(gwcount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
					if (tempBean.get("OPERATE").equals("主办")) {
						map.put("zhubangw", tempBean.get("GW_COUNT"));
					} else if (tempBean.get("OPERATE").equals("会签")) {
						map.put("xiebanhuiqian", tempBean.get("GW_COUNT"));
					}
					map.put("totaltime", DataStatsUtil.divFunc(totalTime, 60));
					map.put("avgtime", DataStatsUtil.divFunc(DataStatsUtil.divFunc(totalTime, gwcount), 60));
					map.put("yuqijianshu", "1");
					map.put("beicuibancishi", "12");
					map.put("beidubanjianshu", "4");

				}
			}
			bList.add(map);
		}
		outBean.setData(new Bean().set("data", bList));

		return outBean;
	}

	// 办结率
	@Override
	public ApiOutBean getDeptStatsCharBJLByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);
		
		UserBean currUser = Context.getUserBean();
		
		String strWhere = "where GW_BEGIN_TIME >= '" + beginTime + " 00:00:00' AND GW_BEGIN_TIME <= '" + endTime
				+ " 23:59:59' " + "and S_TDEPT = '" + currUser.getTDeptCode() + "' ";

		String dataSql = "SELECT COUNT(S_WF_STATE) GW_COUNT,S_WF_STATE FROM (select DECODE (S_WF_STATE, 1, '未结', 2, '已结') S_WF_STATE from oa_gw_gongwen "
				+ strWhere + ")" + " GROUP BY S_WF_STATE ";
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
				yjData.put("name", "已结" + tempBean.getStr("GW_COUNT") + "件");
				yjData.put("yj", tempBean.getStr("GW_COUNT"));
			} else if (tempBean.getStr("S_WF_STATE").equals("未结")) {
				wjData.put("name", "未结" + tempBean.getStr("GW_COUNT") + "件");
				wjData.put("wj", tempBean.getStr("GW_COUNT"));
			}
		}
		float yjCount = yjData.get("yj")!=null&&yjData.get("yj").length()>0?DataStatsUtil.parseFloat(yjData.get("yj")):0;
		float wjCount = wjData.get("wj")!=null&&wjData.get("wj").length()>0?DataStatsUtil.parseFloat(wjData.get("wj")):0;
		wjData.put("value",  String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(wjCount,100),gwCount)));
		yjData.put("value",  String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(yjCount,100),gwCount)));
		listData.add(yjData);
		listData.add(wjData);

		Bean rtnBean = new Bean();
		rtnBean.put("title", "部门文件办结率");
		rtnBean.put("listData", listData);

		outBean.setData(new Bean().set("data", rtnBean));

		return outBean;
	}

	// 正在办理的文件
	@Override
	public ApiOutBean getDeptStatsCharRunningByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' " + "and TO_TDEPT_CODE = '" + currUser.getTDeptCode() + "' and node_if_running = '1'";

		String dataSql = "SELECT TMPL_TYPE_CODE,COUNT (DISTINCT gw_id) gw_count from SY_WFE_NODE_INST_GW_V " + strWhere
				+ " GROUP BY TMPL_TYPE_CODE ";
		List list = Transaction.getExecutor().query(dataSql);
		List listData = new ArrayList();
		Map<String, String> swData = new HashMap<String, String>();
		swData.put("name", "收文");
		swData.put("value", "0");
		Map<String, String> fwData = new HashMap<String, String>();
		fwData.put("name", "发文");
		fwData.put("value", "0");
		Map<String, String> qbData = new HashMap<String, String>();
		qbData.put("name", "签报");
		qbData.put("value", "0");
		// 已结 未结
		for (int i = 0; i < list.size(); i++) {
			Bean tempBean = (Bean) list.get(i);
			if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
				fwData.put("value", tempBean.getStr("GW_COUNT"));
			} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
				swData.put("value", tempBean.getStr("GW_COUNT"));
			} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
				qbData.put("value", tempBean.getStr("GW_COUNT"));
			}
		}
		listData.add(fwData);
		listData.add(swData);
		listData.add(qbData);

		Bean rtnBean = new Bean();
		rtnBean.put("data", listData);

		outBean.setData(new Bean().set("data", rtnBean));

		return outBean;
	}
	//主办公文 char
	@Override
	public ApiOutBean getDeptStatsCharZBGWCountByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();

		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' " + "and a.to_tdept_code = B.DEPT_CODE " + "AND C_S_CMPY = '" + currUser.getCmpyCode()
				+ "' "
				+ "and NODE_CHILD_TYPE = '1'  and (a.tmpl_type_code = 'OA_GW_GONGWEN_FW' OR A.TMPL_TYPE_CODE = 'OA_GW_GONGWEN_QB') ";

		String dataSql = "SELECT A.TMPL_TYPE_CODE,a.to_tdept_code dept_code, B.DEPT_NAME,COUNT (DISTINCT gw_id) gw_count from SY_WFE_NODE_INST_GW_V a, sy_org_dept b "
				+ strWhere + " GROUP BY a.TMPL_TYPE_CODE,a.to_tdept_code, B.DEPT_NAME ORDER BY a.to_tdept_code  ";
		List list = Transaction.getExecutor().query(dataSql);

		Bean bean = new Bean();
		for (int i = 0; i < list.size(); i++) {
			Bean tempBean = (Bean) list.get(i);
			bean.put(tempBean.getStr("DEPT_NAME") + "-" + tempBean.get("TMPL_TYPE_CODE"),
					new Bean().set("GW_COUNT", tempBean.getInt("GW_COUNT")));
		}

		List<DeptBean> deptList = OrgMgr.getDepts(currUser.getCmpyCode());
		String deptArr = "";
		String qbArr = "";
		String fwArr = "";
		for (int i = 0; i < deptList.size(); i++) {
			DeptBean deptBean = (DeptBean) deptList.get(i);
			deptArr += ",\"" + deptBean.get("DEPT_NAME") + "\"";
			if (bean.containsKey(deptBean.get("DEPT_NAME") + "-" + "OA_GW_GONGWEN_QB")) {
				qbArr += "," + bean.getBean(deptBean.get("DEPT_NAME") + "-" + "OA_GW_GONGWEN_QB").getInt("GW_COUNT");
			} else {
				qbArr += "," + "0";
			}
			if (bean.containsKey(deptBean.get("DEPT_NAME") + "-" + "OA_GW_GONGWEN_FW")) {
				fwArr += "," + bean.getBean(deptBean.get("DEPT_NAME") + "-" + "OA_GW_GONGWEN_FW").getInt("GW_COUNT");
			} else {
				fwArr += "," + "0";
			}
		}

		outBean.setData(new Bean().set("x", "[" + deptArr.substring(1) + "]")
				.set("qbData", "[" + qbArr.substring(1) + "]").set("fwData", "[" + fwArr.substring(1) + "]"));
		return outBean;
	}
	
	//会签协办  char数据       
	@Override
	public ApiOutBean getDeptStatsCharHQGWCountByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();

		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' " + "and a.to_tdept_code = B.DEPT_CODE " + "AND C_S_CMPY = '" + currUser.getCmpyCode()
				+ "' "
				+ "and (NODE_CHILD_TYPE = '2' OR NODE_CHILD_TYPE = '3') ";

		String dataSql = "SELECT DECODE (a.node_child_type, 2, '协办',3, '会签') OPERATE,a.to_tdept_code dept_code, B.DEPT_NAME,COUNT (DISTINCT gw_id) gw_count from SY_WFE_NODE_INST_GW_V a, sy_org_dept b "
				+ strWhere + " GROUP BY DECODE (a.node_child_type, 2, '协办',3, '会签'),a.to_tdept_code, B.DEPT_NAME  ORDER BY a.to_tdept_code ";
		List list = Transaction.getExecutor().query(dataSql);
  
		Bean bean = new Bean();
		for (int i = 0; i < list.size(); i++) {
			Bean tempBean = (Bean) list.get(i);
			bean.put(tempBean.getStr("DEPT_NAME") + "-" + tempBean.get("OPERATE"),
					new Bean().set("GW_COUNT", tempBean.getInt("GW_COUNT")));
		}

		List<DeptBean> deptList = OrgMgr.getDepts(currUser.getCmpyCode());
		String deptArr = ""; 
		String hqArr = "";
		String xbArr = "";
		for (int i = 0; i < deptList.size(); i++) {
			DeptBean deptBean = (DeptBean) deptList.get(i);
			deptArr += ",\"" + deptBean.get("DEPT_NAME") + "\"";
			if (bean.containsKey(deptBean.get("DEPT_NAME") + "-" + "会签")) {
				hqArr += "," + bean.getBean(deptBean.get("DEPT_NAME") + "-" + "会签").getInt("GW_COUNT");
			} else {
				hqArr += "," + "0";
			}
			if (bean.containsKey(deptBean.get("DEPT_NAME") + "-" + "协办")) {
				xbArr += "," + bean.getBean(deptBean.get("DEPT_NAME") + "-" + "协办").getInt("GW_COUNT");
			} else {
				xbArr += "," + "0";
			}
		}

		outBean.setData(new Bean().set("x", "[" + deptArr.substring(1) + "]")
				.set("hqData", "[" + hqArr.substring(1) + "]").set("xbData", "[" + xbArr.substring(1) + "]"));
		return outBean;
	}

}
