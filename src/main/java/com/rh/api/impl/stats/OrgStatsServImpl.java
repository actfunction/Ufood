package com.rh.api.impl.stats;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.rh.core.util.DateUtils;
import com.rh.api.util.DataStatsUtil;

public class OrgStatsServImpl extends DataStatsServImpl {
	
	/** log */
	private static Log log = LogFactory.getLog(OrgStatsServImpl.class);
	
	
	//
	public ApiOutBean getOrgTabDataByStime(Bean reqData) {
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();

		String strWhere = "where GW_BEGIN_TIME >= '"
						+ beginTime + " 00:00:00' AND GW_BEGIN_TIME <= '" + endTime + " 23:59:59' " + "and C_S_CMPY = '"
						+ currUser.getCmpyCode()+ "' ";
		// 公文处理数量及时效
		String slsxSql = "select TMPL_TYPE_CODE,COUNT (DISTINCT gw_id) gw_count,sum(node_days) node_days FROM SY_WFE_NODE_INST_GW_V "
					 + strWhere+" GROUP BY TMPL_TYPE_CODE ";
		List slsxSqlList = Transaction.getExecutor().query(slsxSql);
		List<Map<String, String>> slsxDataList = new ArrayList<Map<String, String>>();
		float slsxTotalTime = 0;
		float slsxCount = 0;
		Map<String, String> fwMap = new HashMap<String, String>();
		fwMap.put("name", "发文总量");
		fwMap.put("unit", "件");
		Map<String, String>	swMap = new HashMap<String, String>();
		swMap.put("name", "收文总量");
		swMap.put("unit", "件");
		Map<String, String> qbMap = new HashMap<String, String>();
		qbMap.put("name", "签报总量");
		qbMap.put("unit", "件");
		if(slsxSqlList==null || slsxSqlList.size()<=0){
			fwMap.put("count","0");
			swMap.put("count","0");
			qbMap.put("count","0");
		}else{
			for(int i = 0; i < slsxSqlList.size(); i++){
				Bean tempBean = (Bean)slsxSqlList.get(i);
				slsxTotalTime = DataStatsUtil.sumFunc(slsxTotalTime,DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
				slsxCount =DataStatsUtil.sumFunc(slsxCount,DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
				if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")){
					fwMap.put("count", String.valueOf(tempBean.get("GW_COUNT")));
				}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")){
					swMap.put("count", String.valueOf(tempBean.get("GW_COUNT")));
				}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")){
					qbMap.put("count", String.valueOf(tempBean.get("GW_COUNT")));
				}
			}
		}
		slsxDataList.add(swMap);
		slsxDataList.add(fwMap);
		slsxDataList.add(qbMap);
		///////平均用时
		Map<String, String> avgMap = new HashMap<String, String>();
		avgMap.put("unit", "天/件");
		avgMap.put("name", "平均用时");
		float days = DataStatsUtil.divFunc(DataStatsUtil.divFunc(slsxTotalTime,60), 24);
		avgMap.put("count",String.valueOf(DataStatsUtil.divFunc(days,slsxCount)));
		slsxDataList.add(avgMap);
		List<List> slsxRtnList = new ArrayList<List>();
		slsxRtnList.add(slsxDataList);
		
		Bean slsxRtnBean = new Bean();
		slsxRtnBean.set("data", slsxRtnList);
		slsxRtnBean.set("title", "公文处理数量及时效");

		//公文实效质量评价
		//超期率
		String cql_val0 = "18.36%";
		String cql_val1 = "6.39%";
		Bean sxzlBean_cql = new Bean();
		sxzlBean_cql.set("title", "超期度");
		sxzlBean_cql.set("data_0", cql_val0);
		sxzlBean_cql.set("unit_0", "主办");
		sxzlBean_cql.set("data_1", cql_val1);
		sxzlBean_cql.set("unit_1", "会签");
		//延期率
		String yql_val0 = "18.36%";
		String yql_val1 = "6.39%";
		Bean sxzlBean_yql = new Bean();
		sxzlBean_yql.set("title", "延期率");
		sxzlBean_yql.set("data_0", yql_val0);
		sxzlBean_yql.set("unit_0", "主办");
		sxzlBean_yql.set("data_1", yql_val1);
		sxzlBean_yql.set("unit_1", "会签");
		//质量平均分	
		String zlpjf_val = "8.8";
		Bean sxzlBean_zlpjf = new Bean();
		sxzlBean_zlpjf.set("title", "质量平均分");
		sxzlBean_zlpjf.set("data_0",zlpjf_val);

		List<Bean> sxzlRtnList = new ArrayList<Bean>();
		sxzlRtnList.add(sxzlBean_cql);
		sxzlRtnList.add(sxzlBean_yql);
		sxzlRtnList.add(sxzlBean_zlpjf);
		Bean sxzlRtnBean = new Bean();
		sxzlRtnBean.set("data", sxzlRtnList);
		sxzlRtnBean.set("title", "公文实效质量评价");
		
		// 请示件办理情况
		String qsjWhere = strWhere + " AND GW_FILE_TYPE = '请示' AND TMPL_TYPE_CODE = 'OA_GW_GONGWEN_SW' " ;
		String qsjSql = "select COUNT (DISTINCT gw_id) gw_count,S_WF_STATE FROM SY_WFE_NODE_INST_GW_V "
					 + qsjWhere+" GROUP BY S_WF_STATE ";
		List<Bean> qsjSqlList = Transaction.getExecutor().query(qsjSql);
		List<Map<String, String>> qsjDataList = new ArrayList<Map<String, String>>();
		String yj = "0";
		String wj = "0";
		for(int i = 0; i < qsjSqlList.size(); i++){
			Bean tempBean = (Bean)qsjSqlList.get(i);
			if(tempBean.getStr("S_WF_STATE").equals("2")){//已结
				yj = tempBean.getStr("GW_COUNT");
			}else if(tempBean.getStr("S_WF_STATE").equals("1")){//未结
				wj = tempBean.getStr("GW_COUNT");
			}
		}
		////请示件储量
		float qsjCount = DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(wj), DataStatsUtil.parseFloat(yj));
		Map<String, String> qsjCountMap = new HashMap<String, String>();
		qsjCountMap.put("count", String.valueOf((int)qsjCount));
		qsjCountMap.put("unit", "件");
		qsjCountMap.put("name", "请示件数量");
		qsjDataList.add(qsjCountMap);
		////未结数量
		Map<String, String> qsjWjMap = new HashMap<String, String>();
		qsjWjMap.put("count", String.valueOf(wj));
		qsjWjMap.put("unit", "件");
		qsjWjMap.put("name", "未办结");
		qsjDataList.add(qsjWjMap);
		////已结数量
//		Map<String, String> qsjYjMap = new HashMap<String, String>();
//		qsjYjMap.put("count", String.valueOf(yj));
//		qsjYjMap.put("unit", "件");
//		qsjYjMap.put("name", "已办结");
//		qsjDataList.add(qsjYjMap);
		
		////超期率  假数据
		Map<String, String> chaoqiMap = new HashMap<String, String>();
		chaoqiMap.put("count", "8");
		chaoqiMap.put("unit", "件");
		chaoqiMap.put("name", "超期");
		qsjDataList.add(chaoqiMap);
		//延期
		Map<String, String> yanqiMap = new HashMap<String, String>();
		yanqiMap.put("count", "8");
		yanqiMap.put("unit", "件");
		yanqiMap.put("name", "延期");
		qsjDataList.add(yanqiMap);
		
		
		List<List> qsjRtnList = new ArrayList<List>();
		qsjRtnList.add(qsjDataList);
		
		Bean qsjRtnBean = new Bean();
		qsjRtnBean.set("data", qsjRtnList);
		qsjRtnBean.set("title", "请示件办理情况");
		
		
		//公文办理时间分布情况
		String blsjSql = "SELECT  time,count(node_btime) count FROM (SELECT node_btime,"+
		"CASE "+
		"WHEN to_number(substr(node_btime,11,3)) between 18 and 24  THEN 'free' " +
		"WHEN to_number(substr(node_btime,11,3)) between 8 and 12  THEN 'work' " +
		"WHEN to_number(substr(node_btime,11,3)) between 12 and 18  THEN 'work' " +
		"WHEN to_number(substr(node_btime,11,3)) between 0 and 8  THEN 'free' " +
		"ELSE '' END  time " +
		"FROM SY_WFE_NODE_INST_GW_V "+strWhere+") TABLE_TEMP " +
		"GROUP BY TABLE_TEMP.time ";
		float workCount = 0;
		float freeCount = 0;
		List blsjSqlList = Transaction.getExecutor().query(blsjSql);
		List<Map<String, String>> blsjDataList = new ArrayList<Map<String, String>>();
		for(int i = 0; i < blsjSqlList.size(); i++){
			Bean tempBean = (Bean)blsjSqlList.get(i);
			if(tempBean.getStr("TIME").equals("work")){
				workCount = DataStatsUtil.parseFloat(tempBean.getStr("COUNT"));
			}else if(tempBean.getStr("TIME").equals("free")){
				freeCount =DataStatsUtil.parseFloat(tempBean.getStr("COUNT"));
			}
		}
		float sumCount = DataStatsUtil.sumFunc(workCount, freeCount);
		Map<String, String> blsjWorkMap = new HashMap<String, String>();
		blsjWorkMap.put("name", "work");
		blsjWorkMap.put("unit", "");
		blsjWorkMap.put("width", "50%");
		blsjWorkMap.put("count", String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(workCount,100), sumCount))+"%");
		Map<String, String> blsjFreeMap = new HashMap<String, String>();
		blsjFreeMap.put("name", "free");
		blsjFreeMap.put("unit", "");
		blsjFreeMap.put("width", "50%");
		blsjFreeMap.put("count",  String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(freeCount,100),sumCount))+"%");
		blsjDataList.add(blsjWorkMap);
		blsjDataList.add(blsjFreeMap);
		
		
		Bean blsjRtnBean = new Bean();
		blsjRtnBean.set("data", blsjDataList);
		blsjRtnBean.set("title", "公文办理时间分布情况");
		
		
		Bean rtnBean = new Bean();
		rtnBean.set("slsx", slsxRtnBean);
		rtnBean.set("qsj", qsjRtnBean);
		rtnBean.set("blsj", blsjRtnBean);
		rtnBean.set("sxzl", sxzlRtnBean);
		
		outBean.setData(rtnBean);
		return outBean;
	}
	
	//文件数量及时效 table数据
	public ApiOutBean getOrgStatsDataSLSXByStime(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' and a.to_tdept_code = B.DEPT_CODE " + "and C_S_CMPY = '"
						+ currUser.getCmpyCode()+ "' ";

		String dataSql = "SELECT a.TMPL_TYPE_CODE,a.to_tdept_code dept_code,B.DEPT_NAME,"+
						 "DECODE (a.node_child_type, 0, '流经', 1, '主办', 2, '流经',3, '流经') operate,"+
						 "COUNT (DISTINCT a.gw_id) gw_count,"+
						 "SUM (a.node_days) node_days " + "FROM SY_WFE_NODE_INST_GW_V  a,sy_org_dept b " + strWhere +
						 "GROUP BY a.TMPL_TYPE_CODE,a.to_tdept_code, B.DEPT_NAME,DECODE(node_child_type,  0, '流经',  1, '主办',  2, '流经',  3, '流经') "+
						 "ORDER BY A.to_tdept_code ";
		List list = Transaction.getExecutor().query(dataSql);
		
		List aList = new ArrayList<HashMap>();
		List bList = new ArrayList<HashMap>();
		for(int i = 0; i < list.size(); i++){
			Bean tempBean = (Bean)list.get(i);
			Map<String,String> tempMap = new HashMap<String,String>();
			tempMap.put("deptname", tempBean.getStr("DEPT_NAME"));
			if(!aList.contains(tempMap)){
				aList.add(tempMap);
			}
		}
		//文件处理总数、收文主办\流经、发文主办\流经、签报主办\流经、平均时效主办\流经
		for(int i = 0; i < aList.size(); i++){
			float zbTime = 0;
			float ljTime = 0;
			float zbCount = 0;
			float ljCount = 0;
			Map map = (HashMap)aList.get(i);
			map.put("id", String.valueOf(i+1));
			for(int j = 0; j < list.size(); j++){
				Bean tempBean = (Bean)list.get(j);
				if(map.get("deptname").equals(tempBean.getStr("DEPT_NAME"))){
					if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")){
						if(tempBean.get("OPERATE").equals("流经")){
							map.put("fwlj", tempBean.get("GW_COUNT"));
							ljTime = DataStatsUtil.sumFunc(ljTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							ljCount = DataStatsUtil.sumFunc(ljCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.get("OPERATE").equals("主办")){
							map.put("fwzb", tempBean.get("GW_COUNT"));
							zbTime = DataStatsUtil.sumFunc(zbTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							zbCount = DataStatsUtil.sumFunc(zbCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
					}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")){
						if(tempBean.get("OPERATE").equals("流经")){
							map.put("swlj", tempBean.get("GW_COUNT"));
							ljTime = DataStatsUtil.sumFunc(ljTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							ljCount = DataStatsUtil.sumFunc(ljCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.get("OPERATE").equals("主办")){
							map.put("swzb", tempBean.get("GW_COUNT"));
							zbTime = DataStatsUtil.sumFunc(zbTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							zbCount = DataStatsUtil.sumFunc(zbCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
					}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")){
						if(tempBean.get("OPERATE").equals("流经")){
							map.put("qblj", tempBean.get("GW_COUNT"));
							ljTime = DataStatsUtil.sumFunc(ljTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							ljCount = DataStatsUtil.sumFunc(ljCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.get("OPERATE").equals("主办")){
							map.put("qbzb", tempBean.get("GW_COUNT"));
							zbTime = DataStatsUtil.sumFunc(zbTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							zbCount = DataStatsUtil.sumFunc(zbCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
					}	
				}
				float sxzb = DataStatsUtil.divFunc(DataStatsUtil.divFunc(zbTime, zbCount),60);
				float sxlj = DataStatsUtil.divFunc(DataStatsUtil.divFunc(ljTime, ljCount),60);
				map.put("sxzb", sxzb>0?sxzb:"");
				map.put("sxlj",sxlj>0?sxlj:"");
				map.put("filecount",(int)DataStatsUtil.sumFunc(zbCount, ljCount));
			}
			bList.add(map);	
		}
		//合计
		Map total = new HashMap();
		for(int i = 0 ;i < bList.size(); i ++){
			Map map = (Map)bList.get(i);
			total.put("id", bList.size()+1);
			total.put("deptname", "合计");
			total.put("filecount", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("filecount"))),DataStatsUtil.parseFloat(String.valueOf(map.get("filecount")))));
			total.put("swzb", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("swzb"))),DataStatsUtil.parseFloat(String.valueOf(map.get("swzb")))));
			total.put("swlj", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("swlj"))),DataStatsUtil.parseFloat(String.valueOf(map.get("swlj")))));
			total.put("fwzb", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("fwzb"))),DataStatsUtil.parseFloat(String.valueOf(map.get("fwzb")))));
			total.put("fwlj", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("fwlj"))),DataStatsUtil.parseFloat(String.valueOf(map.get("fwlj")))));
			total.put("qbzb",(int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("qbzb"))),DataStatsUtil.parseFloat(String.valueOf(map.get("qbzb")))));
			total.put("qblj",(int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("qblj"))),DataStatsUtil.parseFloat(String.valueOf(map.get("qblj")))));
			total.put("sxzb", DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("sxzb"))),DataStatsUtil.parseFloat(String.valueOf(map.get("sxzb")))));
			total.put("sxlj", DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("sxlj"))),DataStatsUtil.parseFloat(String.valueOf(map.get("sxlj")))));
		}
		bList.add(total);
		outBean.setData(new Bean().set("data", bList));

		return outBean;
	}
	
	//请示件 table数据
	public ApiOutBean getOrgStatsDataQSJByStime(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' AND GW_FILE_TYPE = '请示' AND TMPL_TYPE_CODE = 'OA_GW_GONGWEN_SW' and a.to_tdept_code = B.DEPT_CODE "+ "and C_S_CMPY = '"
						+ currUser.getCmpyCode()+ "' ";

		String dataSql = "SELECT a.to_tdept_code dept_code,B.DEPT_NAME,"+
						 "DECODE (a.S_WF_STATE, 1, '未结', 2, '已结') S_WF_STATE,"+
						 "COUNT (DISTINCT a.gw_id) gw_count,"+
						 "SUM (a.node_days) node_days , DECODE (A.DELAY_TIME,0, '未超期', '超期') DELAY_TIME" + " FROM SY_WFE_NODE_INST_GW_V  a,sy_org_dept b " + strWhere +
						 "GROUP BY a.TMPL_TYPE_CODE,a.to_tdept_code, B.DEPT_NAME,DECODE (a.S_WF_STATE, 1, '未结', 2, '已结'),DECODE (A.DELAY_TIME,0, '未超期', '超期')  "+
						 "ORDER BY A.to_tdept_code ";
		List list = Transaction.getExecutor().query(dataSql);
		
		List aList = new ArrayList<HashMap>();
		List bList = new ArrayList<HashMap>();
		for(int i = 0; i < list.size(); i++){
			Bean tempBean = (Bean)list.get(i);
			Map<String,String> tempMap = new HashMap<String,String>();
			tempMap.put("deptname", tempBean.getStr("DEPT_NAME"));
			if(!aList.contains(tempMap)){
				aList.add(tempMap);
			}
		}
		//文件处理总数、收文主办\流经、发文主办\流经、签报主办\流经、平均时效主办\流经
		for(int i = 0; i < aList.size(); i++){
			float yjTime = 0;
			float yjCount = 0;
			float yjCount_cq = 0;
			float yjCount_wcq = 0;
			float wjTime = 0;
			float wjCount = 0;
			float wjCount_cq = 0;
			float wjCount_wcq = 0;
			
			Map map = (HashMap)aList.get(i);
			map.put("id", String.valueOf(i+1));
			for(int j = 0; j < list.size(); j++){
				Bean tempBean = (Bean)list.get(j);
				if(map.get("deptname").equals(tempBean.getStr("DEPT_NAME"))){
					if(tempBean.get("S_WF_STATE").equals("未结")){
						if(tempBean.get("DELAY_TIME").equals("超期")){
							map.put("chaoqijs_wj", tempBean.get("GW_COUNT"));
							wjTime = DataStatsUtil.sumFunc(wjTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							wjCount_cq = DataStatsUtil.sumFunc(wjCount_cq, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.get("DELAY_TIME").equals("未超期")){
							wjTime = DataStatsUtil.sumFunc(wjTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							wjCount_wcq = DataStatsUtil.sumFunc(wjCount_wcq, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
							}
					}else if(tempBean.get("S_WF_STATE").equals("已结")){
						if(tempBean.get("DELAY_TIME").equals("超期")){
							map.put("chaoqijs_yj", tempBean.get("GW_COUNT"));
							yjTime = DataStatsUtil.sumFunc(yjTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							yjCount_cq = DataStatsUtil.sumFunc(yjCount_cq, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.get("DELAY_TIME").equals("未超期")){
							yjTime = DataStatsUtil.sumFunc(yjTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
							yjCount_wcq = DataStatsUtil.sumFunc(yjCount_wcq, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
					}	
				}
				
				map.put("jianshu_wj", (int)DataStatsUtil.sumFunc(wjCount_cq,wjCount_wcq));
				map.put("jianshu_yj", (int)DataStatsUtil.sumFunc(yjCount_cq,yjCount_wcq));
				float jianshu_wj = DataStatsUtil.parseFloat(String.valueOf(map.get("jianshu_wj")));
				float jianshu_yj = DataStatsUtil.parseFloat(String.valueOf(map.get("jianshu_yj")));
				map.put("chaoqilv_wj",String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(wjCount_cq,100), jianshu_wj))+"%");
				map.put("chaoqilv_yj",String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(yjCount_cq,100), jianshu_yj))+"%");
				map.put("avgtime_wj",DataStatsUtil.divFunc(wjTime,jianshu_wj));
				map.put("avgtime_yj",DataStatsUtil.divFunc(yjTime,jianshu_yj));
				map.put("wjTime", wjTime);
				map.put("yjTime", yjTime);
				map.put("filecount",(int)DataStatsUtil.sumFunc(jianshu_wj, jianshu_yj));
			}
			bList.add(map);	
		}
		//合计
		Map total = new HashMap();
		for(int i = 0 ;i < bList.size(); i ++){
			Map map = (Map)bList.get(i);
			total.put("id", bList.size()+1);
			total.put("deptname", "合计");
			total.put("filecount", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("filecount"))),DataStatsUtil.parseFloat(String.valueOf(map.get("filecount")))));
			total.put("chaoqijs_wj", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("chaoqijs_wj"))),DataStatsUtil.parseFloat(String.valueOf(map.get("chaoqijs_wj")))));
			total.put("chaoqijs_yj", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("chaoqijs_yj"))),DataStatsUtil.parseFloat(String.valueOf(map.get("chaoqijs_yj")))));
			total.put("jianshu_wj", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("jianshu_wj"))),DataStatsUtil.parseFloat(String.valueOf(map.get("jianshu_wj")))));
			total.put("jianshu_yj", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("jianshu_yj"))),DataStatsUtil.parseFloat(String.valueOf(map.get("jianshu_yj")))));
			total.put("wjTime", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("wjTime"))),DataStatsUtil.parseFloat(String.valueOf(map.get("wjTime")))));
			total.put("yjTime", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("yjTime"))),DataStatsUtil.parseFloat(String.valueOf(map.get("yjTime")))));
			}
		float cqjsTotal_wj = DataStatsUtil.parseFloat(String.valueOf(total.get("chaoqijs_wj")));
		float jsTotal_wj = DataStatsUtil.parseFloat(String.valueOf(total.get("jianshu_wj")));
		total.put("chaoqilv_wj" ,String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(cqjsTotal_wj,100), jsTotal_wj))+"%");
		float cqjsTotal_yj = DataStatsUtil.parseFloat(String.valueOf(total.get("chaoqijs_yj")));
		float jsTotal_yj = DataStatsUtil.parseFloat(String.valueOf(total.get("jianshu_yj")));
		total.put("chaoqilv_yj" ,String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(cqjsTotal_yj,100), jsTotal_yj))+"%");
		total.put("avgtime_wj",DataStatsUtil.divFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("wjTime"))),DataStatsUtil.parseFloat(String.valueOf(total.get("jianshu_wj")))));
		total.put("avgtime_yj",DataStatsUtil.divFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("yjTime"))),DataStatsUtil.parseFloat(String.valueOf(total.get("jianshu_yj")))));

		bList.add(total);
		outBean.setData(new Bean().set("data", bList));

		return outBean;
	}
	
	//办理时间 table数据
	public ApiOutBean getOrgStatsDataBLSJByStime(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' and a.to_tdept_code = B.DEPT_CODE " + "and C_S_CMPY = '"
						+ currUser.getCmpyCode()+ "' ";

		String dataSql = "SELECT  dept_code,DEPT_NAME, COUNT (node_btime) COUNT, time from  (SELECT node_btime, a.to_tdept_code dept_code, DEPT_NAME,"+
				"CASE "+
				"WHEN to_number(substr(node_btime,11,3)) between 18 and 24  THEN 'evening' " +
				"WHEN to_number(substr(node_btime,11,3)) between 8 and 12  THEN 'forenoon' " +
				"WHEN to_number(substr(node_btime,11,3)) between 12 and 18  THEN 'afternoon' " +
				"WHEN to_number(substr(node_btime,11,3)) between 0 and 8  THEN 'morning' " +
				"ELSE '' END  time " +
				"FROM SY_WFE_NODE_INST_GW_V   a,sy_org_dept b " + strWhere + ") TABLE_TEMP "+
				"GROUP BY TABLE_TEMP.dept_code, TABLE_TEMP.DEPT_NAME, TABLE_TEMP.time "+
				"ORDER BY dept_name, TABLE_TEMP.time";
		List list = Transaction.getExecutor().query(dataSql);
		
		List aList = new ArrayList<HashMap>();
		List bList = new ArrayList<HashMap>();
		for(int i = 0; i < list.size(); i++){
			Bean tempBean = (Bean)list.get(i);
			Map<String,String> tempMap = new HashMap<String,String>();
			tempMap.put("deptname", tempBean.getStr("DEPT_NAME"));
			if(!aList.contains(tempMap)){
				aList.add(tempMap);
			}
		} 
		for(int i = 0; i < aList.size(); i++){
			Map map = (HashMap)aList.get(i);
			map.put("id", String.valueOf(i+1));
			float workCount = 0;
			float freeCount = 0;
			for(int j = 0; j < list.size(); j++){
				Bean tempBean = (Bean)list.get(j);
				if(map.get("deptname").equals(tempBean.getStr("DEPT_NAME"))){
					if(tempBean.getStr("TIME").equals("forenoon")){//上午
						map.put("forenoon", tempBean.getStr("COUNT"));
						workCount = DataStatsUtil.sumFunc(workCount, DataStatsUtil.parseFloat(tempBean.getStr("COUNT")));
					}else if(tempBean.getStr("TIME").equals("afternoon")){//下午
						map.put("afternoon", tempBean.getStr("COUNT"));
						workCount = DataStatsUtil.sumFunc(workCount, DataStatsUtil.parseFloat(tempBean.getStr("COUNT")));
					}else if(tempBean.getStr("TIME").equals("morning")){//凌晨
						map.put("morning", tempBean.getStr("COUNT"));
						freeCount = DataStatsUtil.sumFunc(freeCount, DataStatsUtil.parseFloat(tempBean.getStr("COUNT")));
					}else if(tempBean.getStr("TIME").equals("evening")){//晚上
						map.put("evening", tempBean.getStr("COUNT"));
						freeCount = DataStatsUtil.sumFunc(freeCount, DataStatsUtil.parseFloat(tempBean.getStr("COUNT")));
					}
				}
				float gwCount = DataStatsUtil.sumFunc(workCount, freeCount);
				map.put("worktime",String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(workCount, 100), gwCount))+"%");
				map.put("freetime",String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(freeCount, 100), gwCount))+"%");
				map.put("filecount", (int)gwCount);
			}
			bList.add(map);
		}
		
		//合计
		Map total = new HashMap();
		for(int i = 0 ;i < bList.size(); i ++){
			Map map = (Map)bList.get(i);
			total.put("id", bList.size()+1);
			total.put("deptname", "合计");
			total.put("filecount", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("filecount"))),DataStatsUtil.parseFloat(String.valueOf(map.get("filecount")))));
			total.put("forenoon", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("forenoon"))),DataStatsUtil.parseFloat(String.valueOf(map.get("forenoon")))));
			total.put("afternoon", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("afternoon"))),DataStatsUtil.parseFloat(String.valueOf(map.get("afternoon")))));
			total.put("morning", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("morning"))),DataStatsUtil.parseFloat(String.valueOf(map.get("morning")))));
			total.put("evening", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("evening"))),DataStatsUtil.parseFloat(String.valueOf(map.get("evening")))));
		}
		float gwTotalCount = DataStatsUtil.parseFloat(String.valueOf(total.get("filecount")));
		float forenoonTotal = DataStatsUtil.parseFloat(String.valueOf(total.get("forenoon")));
		float afternoonTotal = DataStatsUtil.parseFloat(String.valueOf(total.get("afternoon")));
		float workTotalCount = DataStatsUtil.sumFunc(forenoonTotal, afternoonTotal);
		total.put("worktime",String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(workTotalCount, 100), gwTotalCount))+"%");
		float moningTotal = DataStatsUtil.parseFloat(String.valueOf(total.get("morning")));
		float eveningTotal = DataStatsUtil.parseFloat(String.valueOf(total.get("evening")));
		float freeTotalCount = DataStatsUtil.sumFunc(moningTotal, eveningTotal);
		total.put("freetime",String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(freeTotalCount, 100), gwTotalCount))+"%");

		bList.add(total);
		outBean.setData(new Bean().set("data", bList));
				
		return outBean;
	}
	
	//排行榜列表
	public ApiOutBean getOrgStatsDataPHBByStime(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' and a.to_tdept_code = B.DEPT_CODE " + "and C_S_CMPY = '"
						+ currUser.getCmpyCode()+ "' and a.node_child_type = '1' ";

		String dataSql = "SELECT a.TMPL_TYPE_CODE,a.to_tdept_code dept_code,B.DEPT_NAME,DECODE (a.node_child_type, 0, '流经', 1, '主办', 2, '流经',3, '流经') operate,"+
						 "COUNT (DISTINCT a.gw_id) gw_count, SUM (a.node_days) node_days "+"FROM SY_WFE_NODE_INST_GW_V  a,sy_org_dept b " + strWhere +
						 "GROUP BY a.TMPL_TYPE_CODE,a.to_tdept_code, B.DEPT_NAME,DECODE (a.node_child_type, 0, '流经', 1, '主办', 2, '流经',3, '流经')"+
						 " ORDER BY A.to_tdept_code ";
		List list = Transaction.getExecutor().query(dataSql);
		
		List aList = new ArrayList<HashMap>();
		List bList = new ArrayList<HashMap>();
		for(int i = 0; i < list.size(); i++){
			Bean tempBean = (Bean)list.get(i);
			Map<String,String> tempMap = new HashMap<String,String>();
			tempMap.put("deptname", tempBean.getStr("DEPT_NAME"));
			if(!aList.contains(tempMap)){
				aList.add(tempMap);
			}
		}
		
		for(int i = 0; i < aList.size(); i++){
			Map map = (HashMap)aList.get(i);
			map.put("id", String.valueOf(i+1));
			float totalTime = 0;
			float totalCount = 0;
			for(int j = 0; j < list.size(); j++){
				Bean tempBean = (Bean)list.get(j);
				if(map.get("deptname").equals(tempBean.getStr("DEPT_NAME"))){
					if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")){
						map.put("fwcount", tempBean.getStr("GW_COUNT"));
						totalTime = DataStatsUtil.sumFunc(totalTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
						totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
					}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")){
						map.put("swcount", tempBean.getStr("GW_COUNT"));
						totalTime = DataStatsUtil.sumFunc(totalTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
						totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
					}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")){
						map.put("qbcount", tempBean.getStr("GW_COUNT"));
						totalTime = DataStatsUtil.sumFunc(totalTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
						totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
					}
				}
				map.put("avgtime",DataStatsUtil.divFunc(DataStatsUtil.divFunc(totalTime,totalCount),60));
				map.put("totalTime",totalTime);
				map.put("totalCount",totalCount);
				map.put("zlpj","10.00");//假数据
				map.put("qsjyuqi","12");//假数据
				map.put("beicuiban","21");//假数据
				map.put("beiduban","9");//假数据
			}
			bList.add(map);
		}
		
		//合计
		Map total = new HashMap();
		for(int i = 0 ;i < bList.size(); i ++){
			Map map = (Map)bList.get(i);
			total.put("id", bList.size()+1);
			total.put("deptname", "合计");
			total.put("fwcount", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("fwcount"))),DataStatsUtil.parseFloat(String.valueOf(map.get("fwcount")))));
			total.put("swcount", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("swcount"))),DataStatsUtil.parseFloat(String.valueOf(map.get("swcount")))));
			total.put("qbcount", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("qbcount"))),DataStatsUtil.parseFloat(String.valueOf(map.get("qbcount")))));
			total.put("zlpj", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("zlpj"))),DataStatsUtil.parseFloat(String.valueOf(map.get("zlpj")))));
			total.put("qsjyuqi", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("qsjyuqi"))),DataStatsUtil.parseFloat(String.valueOf(map.get("qsjyuqi")))));
			total.put("beicuiban", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("beicuiban"))),DataStatsUtil.parseFloat(String.valueOf(map.get("beicuiban")))));
			total.put("beiduban", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("beiduban"))),DataStatsUtil.parseFloat(String.valueOf(map.get("beiduban")))));
			total.put("totalCount", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("totalCount"))),DataStatsUtil.parseFloat(String.valueOf(map.get("totalCount")))));
			total.put("totalTime", (int)DataStatsUtil.sumFunc(DataStatsUtil.parseFloat(String.valueOf(total.get("totalTime"))),DataStatsUtil.parseFloat(String.valueOf(map.get("totalTime")))));
		}
		float totalCount = DataStatsUtil.parseFloat(String.valueOf(total.get("totalCount")));
		float totalTime = DataStatsUtil.parseFloat(String.valueOf(total.get("totalTime")));
		total.put("avgtime",DataStatsUtil.divFunc(DataStatsUtil.divFunc(totalTime, totalCount),60));

		bList.add(total);
		outBean.setData(new Bean().set("data", bList));
		return outBean;
	}
	
	//办结率char
	public ApiOutBean getOrgStatsCharBJLByStime(Bean reqData){
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where GW_BEGIN_TIME >= '" + beginTime + " 00:00:00' AND GW_BEGIN_TIME <= '" + endTime
				+ " 23:59:59' " + "and S_CMPY = '" + currUser.getCmpyCode() + "' ";

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
		rtnBean.put("title", "文件办结率");
		rtnBean.put("listData", listData);

		outBean.setData(new Bean().set("data", rtnBean));

		return outBean;
	}
	//正在办理的文件
	public ApiOutBean getOrgStatsCharRunningByStime(Bean reqData){
		// TODO Auto-generated method stub
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' " + "and A_S_CMPY = '" + currUser.getCmpyCode() + "' and node_if_running = '1'";

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
	
	//数量时效 Tab
	public ApiOutBean getOrgStatsTabGWCountByStime(Bean reqData){  
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);
		
		UserBean currUser = Context.getUserBean();

		String strWhere = "where GW_BEGIN_TIME >= '" + beginTime + " 00:00:00' AND GW_BEGIN_TIME <= '" + endTime
				+ " 23:59:59' " + "and C_S_CMPY = '" + currUser.getCmpyCode() + "' ";
		// 主办文件table表
		String sql = "select TMPL_TYPE_CODE ,COUNT (DISTINCT gw_id) gw_count,sum(node_days) node_days FROM SY_WFE_NODE_INST_GW_V "
				+ strWhere + " GROUP BY TMPL_TYPE_CODE ORDER BY TMPL_TYPE_CODE";
		List list = Transaction.getExecutor().query(sql);

		List aList = new ArrayList();
		
		Map<String, String> swCount = new HashMap<String, String>();
		swCount.put("name", "收文总量");
		swCount.put("unit", "件");
		swCount.put("count", "0");
		swCount.put("color", "rgb(206,75,21)");
		Map<String, String> fwCount  = new HashMap<String, String>();
		fwCount.put("name", "发文总量");
		fwCount.put("unit", "件");
		fwCount.put("count", "0");
		fwCount.put("color", "rgb(153,155,73)");
		Map<String, String> qbCount = new HashMap<String, String>();
		qbCount.put("name", "签报总量");
		qbCount.put("unit", "件");
		qbCount.put("count", "0");
		qbCount.put("color", "rgb(163,163,163)");
		Map<String, String> avgTimeMap = new HashMap<String, String>();
		avgTimeMap.put("name", "平均用时");
		avgTimeMap.put("unit", "天/件");
		avgTimeMap.put("count", "0");
		avgTimeMap.put("color", "rgb(120,145,167)");
		
		float totalTime = 0;
		float totalCount = 0;  
		
		for (int i = 0; i < list.size(); i++) {
			Bean tempBean = (Bean) list.get(i);
			totalTime = DataStatsUtil.sumFunc(totalTime,      
					DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
			totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
			if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")) {
				fwCount.put("count", String.valueOf(tempBean.get("GW_COUNT")));
			} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")) {
				swCount.put("count", String.valueOf(tempBean.get("GW_COUNT")));
			} else if (tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")) {
				qbCount.put("count", String.valueOf(tempBean.get("GW_COUNT")));
			}
		}
		if(totalCount > 0){
			avgTimeMap.put("count",String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.divFunc(totalTime, totalCount),60)));
		}
		
		aList.add(fwCount);
		aList.add(swCount); 
		aList.add(qbCount);
		aList.add(avgTimeMap);
		
		List<List> rtnList = new ArrayList<List>();  
		rtnList.add(aList);
		outBean.setData(new Bean().set("data", rtnList));

		return outBean;
	} 
	
	//数量时效char
	public ApiOutBean getOrgStatsCharSLSXGWCountByStime(Bean reqData){    
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");
		

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);
		
		int type = reqData.getInt("type");// 1:年 2:周 3:月
		String periodField = "";
		int periodDays = 0;
		if (type == 1) {
			periodField = "GW_MONTH ";
			periodDays = 12;
		} else if (type == 2) {
			periodField = "TO_CHAR (TO_DATE (GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = 7;
		} else if (type == 3) {
			periodField = "TO_CHAR (TO_DATE (GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = DateUtils.getDayOfMonth(DateUtils.getMonth(), DateUtils.getYear());
		}
		UserBean currUser = Context.getUserBean();

		String strWhere = "where GW_BEGIN_TIME >= '"
				+ beginTime + " 00:00:00' AND GW_BEGIN_TIME <= '" + endTime + " 23:59:59' " + "and C_S_CMPY = '"
				+ currUser.getCmpyCode()+ "' ";
		
		String sql = "select "+periodField +" OPERIOD,TMPL_TYPE_CODE,COUNT (DISTINCT gw_id) gw_count, SUM (node_days) node_days FROM SY_WFE_NODE_INST_GW_V "
				+ strWhere + " GROUP BY "+periodField+",TMPL_TYPE_CODE  ORDER BY " +periodField+",TMPL_TYPE_CODE";
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
		
		//发文数量、收文数量、签报数量，平均时效
		String fwArr = "";
		String swArr = "";
		String qbArr = "";
		String avgArr = "";
		if(type == 1){
			for (int i = 0; i < aList.size(); i++) {
				float fw = 0;
				float sw = 0;
				float qb = 0;
				float totalCount = 0;
				float totalTime = 0;
				Map map = (Map) aList.get(i);
				for (int j = 0; j < list.size(); j++) {
					Bean tempBean = (Bean) list.get(j);
					if (DataStatsUtil.parseInt(String.valueOf(map.get("id"))) == DataStatsUtil
							.parseInt(tempBean.getStr("OPERIOD"))) {
						totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						totalTime = DataStatsUtil.sumFunc(totalTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
						if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")){
							fw = DataStatsUtil.sumFunc(fw, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")){
							sw = DataStatsUtil.sumFunc(sw, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")){
							qb = DataStatsUtil.sumFunc(qb, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
					}
				}
				fwArr +=","+(int)fw;
				swArr +=","+(int)sw;
				qbArr +=","+(int)qb;
				avgArr +=","+DataStatsUtil.divFunc(DataStatsUtil.divFunc(totalTime, totalCount), 60); 
			}
		}else{
			for (int i = 0; i < aList.size(); i++) {
				float fw = 0;
				float sw = 0;
				float qb = 0;
				float totalCount = 0;
				float totalTime = 0;
				Map map = (Map) aList.get(i);
				for (int j = 0; j < list.size(); j++) {
					Bean tempBean = (Bean) list.get(j);
					if (String.valueOf(map.get("OPERIOD")).equals(tempBean.getStr("OPERIOD"))) {
						totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						totalTime = DataStatsUtil.sumFunc(totalTime, DataStatsUtil.parseFloat(tempBean.getStr("NODE_DAYS")));
						if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_FW")){
							fw = DataStatsUtil.sumFunc(fw, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_SW")){
							sw = DataStatsUtil.sumFunc(sw, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.get("TMPL_TYPE_CODE").equals("OA_GW_GONGWEN_QB")){
							qb = DataStatsUtil.sumFunc(qb, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
					}
				}
				fwArr +=","+(int)fw;
				swArr +=","+(int)sw;
				qbArr +=","+(int)qb;
				avgArr +=","+DataStatsUtil.divFunc(DataStatsUtil.divFunc(totalTime, totalCount), 60); 
			}
		}
		outBean.setData(new Bean().set("fwData", "[" + fwArr.substring(1) + "]")
								  .set("swData", "[" + swArr.substring(1) + "]")
								  .set("qbData", "[" + qbArr.substring(1) + "]")
								  .set("avgData", "[" + avgArr.substring(1) + "]")); 

		return outBean;
	}
	
	//请示件数据 Tab
	public ApiOutBean getOrgStatsQSJGWCountByStime(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();

		String strWhere = "where GW_BEGIN_TIME >= '"
						+ beginTime + " 00:00:00' AND GW_BEGIN_TIME <= '" + endTime + " 23:59:59' " + "and C_S_CMPY = '"
						+ currUser.getCmpyCode()+ "'  AND GW_FILE_TYPE = '请示' AND TMPL_TYPE_CODE = 'OA_GW_GONGWEN_SW' ";
		//请示件TAB数量
		String sql = "select COUNT (DISTINCT gw_id) gw_count,S_WF_STATE,DECODE (DELAY_TIME,0, '未超期', '超期') DELAY_TIME FROM SY_WFE_NODE_INST_GW_V "
					 + strWhere+" GROUP BY S_WF_STATE ,DECODE (DELAY_TIME,0, '未超期', '超期') ";
		List list = Transaction.getExecutor().query(sql);
		List aList = new ArrayList();
		
		Map<String, String> qsjCount = new HashMap<String, String>();
		qsjCount.put("name", "请示件数量");
		qsjCount.put("count", "0");
		qsjCount.put("unit", "");
		qsjCount.put("color", "rgb(206,75,21)");
		Map<String, String> yjCount  = new HashMap<String, String>();
		yjCount.put("name", "已办结件数");
		yjCount.put("count", "0");
		yjCount.put("unit", "");
		yjCount.put("color", "rgb(153,155,73)");
		Map<String, String> wjCount = new HashMap<String, String>();
		wjCount.put("name", "未办结件数");
		wjCount.put("count", "0");
		wjCount.put("unit", "");
		wjCount.put("color", "rgb(163,163,163)");
		Map<String, String> chaoqilvMap = new HashMap<String, String>();
		chaoqilvMap.put("name", "超期率");
		chaoqilvMap.put("count", "0");
		chaoqilvMap.put("unit", "");
		chaoqilvMap.put("color", "rgb(120,145,167)");
		
		float totalCount = 0;  
		float yj = 0;
		float wj = 0;
		float cq = 0;
		
		for (int i = 0; i < list.size(); i++) {
			Bean tempBean = (Bean) list.get(i);     
			totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
			if(tempBean.get("DELAY_TIME").equals("超期")){
				cq = DataStatsUtil.sumFunc(cq, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
			}
			if(tempBean.getStr("S_WF_STATE").equals("2")){//已结
				yj += DataStatsUtil.sumFunc(yj, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
			}else if(tempBean.getStr("S_WF_STATE").equals("1")){//未结
				wj = DataStatsUtil.sumFunc(wj, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
			}
		}
		qsjCount.put("count", String.valueOf((int)totalCount));
		yjCount.put("count", String.valueOf((int)yj));
		wjCount.put("count", String.valueOf((int)wj));
		chaoqilvMap.put("count", String.valueOf(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(cq,100), totalCount))+"%");
		
		aList.add(qsjCount);
		aList.add(yjCount); 
		aList.add(wjCount);
		aList.add(chaoqilvMap); 
		
		List<List> rtnList = new ArrayList<List>();  
		rtnList.add(aList);
		outBean.setData(new Bean().set("data", rtnList));
		
		return outBean;
	}
	//请示件char数据
	public ApiOutBean getOrgStatsCharQSJGWCountByStime(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");
		

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);
		
		int type = reqData.getInt("type");// 1:年 2:周 3:月
		String periodField = "";
		int periodDays = 0;
		if (type == 1) {
			periodField = "GW_MONTH ";
			periodDays = 12;
		} else if (type == 2) {
			periodField = "TO_CHAR (TO_DATE (GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = 7;
		} else if (type == 3) {
			periodField = "TO_CHAR (TO_DATE (GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = DateUtils.getDayOfMonth(DateUtils.getMonth(), DateUtils.getYear());
		}
		UserBean currUser = Context.getUserBean();

		String strWhere = "where GW_BEGIN_TIME >= '"
				+ beginTime + " 00:00:00' AND GW_BEGIN_TIME <= '" + endTime + " 23:59:59' " + "and C_S_CMPY = '"
				+ currUser.getCmpyCode()+ "'  AND GW_FILE_TYPE = '请示' AND TMPL_TYPE_CODE = 'OA_GW_GONGWEN_SW' ";
		
		String sql = "select "+periodField +" OPERIOD ,COUNT (DISTINCT gw_id) gw_count, S_WF_STATE,DECODE (DELAY_TIME,0, '未超期', '超期') DELAY_TIME FROM SY_WFE_NODE_INST_GW_V "
				+ strWhere + " GROUP BY S_WF_STATE,"+periodField+",DECODE (DELAY_TIME,0, '未超期', '超期')  ORDER BY " +periodField;
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
		
		//请示件数量，已结数量，未结数量，超期率

		String qsjArr = "";
		String yjArr = "";
		String wjArr = "";
		String chaoqilvArr = "";
		if(type == 1){
			for (int i = 0; i < aList.size(); i++) {
				float yj = 0;
				float wj = 0;
				float totalCount = 0;
				float cq = 0;
				Map map = (Map) aList.get(i);
				for (int j = 0; j < list.size(); j++) {
					Bean tempBean = (Bean) list.get(j);
					if (DataStatsUtil.parseInt(String.valueOf(map.get("id"))) == DataStatsUtil
							.parseInt(tempBean.getStr("OPERIOD"))) {
						totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						if(tempBean.get("DELAY_TIME").equals("超期")){
							cq = DataStatsUtil.sumFunc(cq, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
						if(tempBean.getStr("S_WF_STATE").equals("2")){//已结
							yj += DataStatsUtil.sumFunc(yj, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.getStr("S_WF_STATE").equals("1")){//未结
							wj = DataStatsUtil.sumFunc(wj, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
					}
				}
				qsjArr +=","+(int)totalCount;
				yjArr +=","+(int)yj;
				wjArr +=","+(int)wj;
				chaoqilvArr +=","+DataStatsUtil.divFunc(DataStatsUtil.mulFunc(cq,100), totalCount); 
			}
		}else{
			for (int i = 0; i < aList.size(); i++) {
				float yj = 0;
				float wj = 0;
				float totalCount = 0;
				float cq = 0;
				Map map = (Map) aList.get(i);
				for (int j = 0; j < list.size(); j++) {
					Bean tempBean = (Bean) list.get(j);
					if (String.valueOf(map.get("OPERIOD")).equals(tempBean.getStr("OPERIOD"))) {
						totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						if(tempBean.get("DELAY_TIME").equals("超期")){
							cq = DataStatsUtil.sumFunc(cq, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
						if(tempBean.getStr("S_WF_STATE").equals("2")){//已结
							yj += DataStatsUtil.sumFunc(yj, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}else if(tempBean.getStr("S_WF_STATE").equals("1")){//未结
							wj = DataStatsUtil.sumFunc(wj, DataStatsUtil.parseFloat(tempBean.getStr("GW_COUNT")));
						}
					}
				}
				qsjArr +=","+(int)totalCount;
				yjArr +=","+(int)yj;
				wjArr +=","+(int)wj;
				chaoqilvArr +=","+DataStatsUtil.divFunc(DataStatsUtil.mulFunc(cq,100), totalCount);
			}
		}
		outBean.setData(new Bean().set("qsjData", "[" + qsjArr.substring(1) + "]")
								  .set("bjData", "[" + yjArr.substring(1) + "]")
								  .set("wbjData", "[" + wjArr.substring(1) + "]")
								  .set("cqData", "[" + chaoqilvArr.substring(1) + "]")); 

		return outBean;
	}
	
	//办理时间  char数据
	public ApiOutBean getOrgStatsCharBLSJByStime(Bean reqData){
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");

		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);

		UserBean currUser = Context.getUserBean();
		String strWhere = "where NODE_BTIME >= '" + beginTime + " 00:00:00' AND NODE_BTIME <= '" + endTime
				+ " 23:59:59' " + "and C_S_CMPY = '" + currUser.getCmpyCode()+ "' ";

		String dataSql = "SELECT  COUNT (node_btime) COUNT, time from  (SELECT node_btime,"+
				"CASE "+
				"WHEN to_number(substr(node_btime,11,3)) between 18 and 24  THEN '4_evening' " +
				"WHEN to_number(substr(node_btime,11,3)) between 8 and 12  THEN '1_forenoon' " +
				"WHEN to_number(substr(node_btime,11,3)) between 12 and 18  THEN '2_afternoon' " +
				"WHEN to_number(substr(node_btime,11,3)) between 0 and 8  THEN '3_morning' " +
				"ELSE '' END  time " +
				"FROM SY_WFE_NODE_INST_GW_V a " + strWhere + ") TABLE_TEMP "+
				"GROUP BY TABLE_TEMP.time "+
				"ORDER BY TABLE_TEMP.time";
		List list = Transaction.getExecutor().query(dataSql);
		
		List blsjDataArr = new ArrayList();
		List blsjPerArr = new ArrayList();
		float totalCount = 0;
		float workCount = 0;
		float freeCount = 0;
		for(int i = 0; i < list.size(); i++){
			Bean tempBean = (Bean) list.get(i);
			totalCount = DataStatsUtil.sumFunc(totalCount, DataStatsUtil.parseFloat(tempBean.getStr("COUNT")));
		}
		float evening = 0;
		float morning = 0;
		float forenoon = 0;
		float afternoon = 0;
		for(int i = 0; i < list.size(); i++){
			Bean tempBean = (Bean) list.get(i);
			if(tempBean.getStr("TIME").indexOf("forenoon") != -1){
				forenoon = DataStatsUtil.parseFloat(tempBean.getStr("COUNT"));
				workCount = DataStatsUtil.sumFunc(workCount, DataStatsUtil.parseFloat(tempBean.getStr("COUNT")));
			}else if(tempBean.getStr("TIME").indexOf("afternoon") != -1){
				afternoon = DataStatsUtil.parseFloat(tempBean.getStr("COUNT"));
				workCount = DataStatsUtil.sumFunc(workCount, DataStatsUtil.parseFloat(tempBean.getStr("COUNT")));
			}else if(tempBean.getStr("TIME").indexOf("morning") != -1){
				morning = DataStatsUtil.parseFloat(tempBean.getStr("COUNT"));
				freeCount = DataStatsUtil.sumFunc(freeCount, DataStatsUtil.parseFloat(tempBean.getStr("COUNT")));
			}else if(tempBean.getStr("TIME").indexOf("evening") != -1){
				evening = DataStatsUtil.parseFloat(tempBean.getStr("COUNT"));
				freeCount = DataStatsUtil.sumFunc(freeCount, DataStatsUtil.parseFloat(tempBean.getStr("COUNT")));
			}
		}
		blsjDataArr.add(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(forenoon,100), totalCount));
		blsjDataArr.add(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(afternoon,100), totalCount));
		blsjDataArr.add(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(morning,100), totalCount));
		blsjDataArr.add(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(evening,100), totalCount));
		
		blsjPerArr.add(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(workCount,100), totalCount)+"%");
		blsjPerArr.add(DataStatsUtil.divFunc(DataStatsUtil.mulFunc(freeCount,100), totalCount)+"%");
		  
		List rtnList = new ArrayList<Bean>();
		Bean rtnDataBean = new Bean();
		rtnDataBean.put("data",blsjDataArr);
		Bean rtnPerBean = new Bean();
		rtnPerBean.put("data", blsjPerArr);
		rtnList.add(rtnPerBean);
		rtnList.add(rtnDataBean); 
		
		outBean.setData(new Bean().set("data", rtnList));
				
		return outBean; 
	}
	
	/**
	 * 公文时效质量评价
	 */
	public ApiOutBean getOrgStatsGWSXByStime(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String stime = reqData.getStr("stime");
		String beginTime = stime.substring(0, stime.indexOf("至"));
		String endTime = stime.substring(stime.indexOf("至") + 1);
		UserBean userBean = Context.getUserBean();
		String OdeptCode = userBean.getODeptCode();
		Calendar cld=Calendar.getInstance();
		int year = cld.get(cld.YEAR);
		int type = reqData.getInt("type");// 1:年 2:周 3:月
		Bean result = new Bean();
		//分类统计数据
		List tjList = new ArrayList();
		Map<String, String> fwItem = new HashMap<String, String>();
		fwItem.put("name", "发文");
		fwItem.put("unit", "件");
		fwItem.put("count", "0");
		fwItem.put("color", "rgb(206,75,21)");
		Map<String, String> qbItem = new HashMap<String, String>();
		qbItem.put("name", "签报");
		qbItem.put("unit", "件");
		qbItem.put("count", "0");
		qbItem.put("color", "rgb(206,75,21)");
		Map<String, String> cqlItem = new HashMap<String, String>();
		cqlItem.put("name", "超期度");
		cqlItem.put("unit", "平均");
		cqlItem.put("count", "0");
		cqlItem.put("color", "rgb(206,75,21)");
		Map<String, String> yqlItem = new HashMap<String, String>();
		yqlItem.put("name", "延期率");
		yqlItem.put("unit", "平均");
		yqlItem.put("count", "0");
		yqlItem.put("color", "rgb(206,75,21)");
		Map<String, String> zlpfItem = new HashMap<String, String>();
		zlpfItem.put("name", "质量评分");
		zlpfItem.put("unit", "平均");
		zlpfItem.put("count", "0");
		zlpfItem.put("color", "rgb(206,75,21)");
		
		if(type == 1){
			//发文
			String dataSql = "select count(t.PI_ID) num from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
				"where t.serv_id = 'OA_GW_GONGWEN_GSFW' and t.inst_btime like '"+year+"%' and o.s_odept = '"+userBean.getODeptCode()+"'";
			List<Bean> list = Transaction.getExecutor().query(dataSql);
			fwItem.put("count", list.get(0).getStr("NUM"));
			//签报
			String dataSql_qb = "select count(t.PI_ID) num from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
					"where t.serv_id = 'OA_GW_GONGWEN_QBQS' and t.inst_btime like '"+year+"%' and o.s_odept = '"+userBean.getODeptCode()+"'";
			List<Bean> list_qb = Transaction.getExecutor().query(dataSql_qb);
			qbItem.put("count", list_qb.get(0).getStr("NUM"));
		}else if(type == 2){
			String dataSql = "select count(t.PI_ID) num from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
					"where t.serv_id = 'OA_GW_GONGWEN_GSFW' and t.inst_btime > '"+beginTime+"' and o.s_odept = '"+userBean.getODeptCode()+"'";
			List<Bean> list = Transaction.getExecutor().query(dataSql);
			fwItem.put("count", list.get(0).getStr("NUM"));
			
			String dataSql_qb = "select count(t.PI_ID) num from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
					"where t.serv_id = 'OA_GW_GONGWEN_QBQS' and t.inst_btime > '"+beginTime+"' and o.s_odept = '"+userBean.getODeptCode()+"'";
			List<Bean> list_qb = Transaction.getExecutor().query(dataSql_qb);
			qbItem.put("count", list_qb.get(0).getStr("NUM"));
			
		}else if(type == 3){
			String dataSql = "select count(t.PI_ID) num from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
					"where t.serv_id = 'OA_GW_GONGWEN_GSFW' and t.inst_btime > '"+beginTime+"' and o.s_odept = '"+userBean.getODeptCode()+"'";
			List<Bean> list = Transaction.getExecutor().query(dataSql);
			fwItem.put("count", list.get(0).getStr("NUM"));
			
			String dataSql_qb = "select count(t.PI_ID) num from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
					"where t.serv_id = 'OA_GW_GONGWEN_QBQS' and t.inst_btime > '"+beginTime+"' and o.s_odept = '"+userBean.getODeptCode()+"'";
			List<Bean> list_qb = Transaction.getExecutor().query(dataSql_qb);
			qbItem.put("count", list_qb.get(0).getStr("NUM"));
			
		}
		
		
		tjList.add(fwItem);
		tjList.add(qbItem);
		tjList.add(cqlItem);
		tjList.add(yqlItem);
		tjList.add(zlpfItem);
		//图表数据
//		String periodField = "";
		int periodDays = 0;
		ArrayList<Integer> list_fw = new ArrayList<Integer>();
		int[] x_fw = null;
		int[] x_qb = null;
		if (type == 1) {
//			periodField = "GW_MONTH ";
			periodDays = 12;
			x_fw = new int[periodDays];
			x_qb = new int[periodDays];
			//发文
			String dataSql = "select count(substr(t.INST_BTIME,6,2)) num,substr(t.INST_BTIME,6,2) as monthStr from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
			"where t.serv_id = 'OA_GW_GONGWEN_GSFW' and t.inst_btime like '"+year+"%' and o.s_odept = '"+userBean.getODeptCode()+"' group by substr(t.INST_BTIME,6,2)";
			List list = Transaction.getExecutor().query(dataSql);
			for(int i=0;i<list.size();i++){
				Bean tmpBean = (Bean) list.get(i);
				int month = tmpBean.getInt("MONTHSTR");
				int num = tmpBean.getInt("NUM");
				x_fw[month-1] = num;
			}
			//签报
			String dataSql_qb = "select count(substr(t.INST_BTIME,6,2)) num,substr(t.INST_BTIME,6,2) as monthStr from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
			"where t.serv_id = 'OA_GW_GONGWEN_QBQS' and t.inst_btime like '"+year+"%' and o.s_odept = '"+userBean.getODeptCode()+"' group by substr(t.INST_BTIME,6,2)";
			List list_qb = Transaction.getExecutor().query(dataSql_qb);
			for(int i=0;i<list_qb.size();i++){
				Bean tmpBean = (Bean) list_qb.get(i);
				int month = tmpBean.getInt("MONTHSTR");
				int num = tmpBean.getInt("NUM");
				x_qb[month-1] = num;
			}
		} else if (type == 2) {
//			periodField = "TO_CHAR (TO_DATE (GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = 7;
			x_fw = new int[periodDays];
			x_qb = new int[periodDays];
			//收文
			String dataSql = "select count(substr(t.INST_BTIME,9,2)) num,substr(t.INST_BTIME,9,2) as dayStr from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
			"where t.serv_id = 'OA_GW_GONGWEN_GSFW' and t.inst_btime > '"+beginTime+"' and o.s_odept = '"+userBean.getODeptCode()+"' group by substr(t.INST_BTIME,9,2)";
			List list = Transaction.getExecutor().query(dataSql);
			for(int i=0;i<list.size();i++){
				Bean tmpBean = (Bean) list.get(i);
				int day = tmpBean.getInt("DAYSTR");
				int num = tmpBean.getInt("NUM");
				x_fw[day-1] = num;
			}
			//签报
			String dataSql_qb = "select count(substr(t.INST_BTIME,9,2)) num,substr(t.INST_BTIME,9,2) as dayStr from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
			"where t.serv_id = 'OA_GW_GONGWEN_QBQS' and t.inst_btime > '"+beginTime+"' and o.s_odept = '"+userBean.getODeptCode()+"' group by substr(t.INST_BTIME,9,2)";
			List list_qb = Transaction.getExecutor().query(dataSql_qb);
			for(int i=0;i<list_qb.size();i++){
				Bean tmpBean = (Bean) list_qb.get(i);
				int day = tmpBean.getInt("DAYSTR");
				int num = tmpBean.getInt("NUM");
				x_qb[day-1] = num;
			}
		}else if (type == 3) {
//			periodField = "TO_CHAR (TO_DATE (GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'YYYY-MM-DD') ";
			periodDays = DateUtils.getDayOfMonth(DateUtils.getMonth(), DateUtils.getYear());
			x_fw = new int[periodDays];
			x_qb = new int[periodDays];
			//收文
			String dataSql = "select count(substr(t.INST_BTIME,9,2)) num,substr(t.INST_BTIME,9,2) as dayStr from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
			"where t.serv_id = 'OA_GW_GONGWEN_GSFW' and t.inst_btime > '"+beginTime+"' and o.s_odept = '"+userBean.getODeptCode()+"' group by substr(t.INST_BTIME,9,2)";
			List list = Transaction.getExecutor().query(dataSql);
			for(int i=0;i<list.size();i++){
				Bean tmpBean = (Bean) list.get(i);
				int day = tmpBean.getInt("DAYSTR");
				int num = tmpBean.getInt("NUM");
				x_fw[day-1] = num;
			}
			//签报
			String dataSql_qb = "select count(substr(t.INST_BTIME,9,2)) num,substr(t.INST_BTIME,9,2) as dayStr from SY_WFE_PROC_INST_ALL_V t left join OA_GW_GONGWEN o on t.DOC_ID = o.gw_id "+
			"where t.serv_id = 'OA_GW_GONGWEN_QBQS' and t.inst_btime > '"+beginTime+"' and o.s_odept = '"+userBean.getODeptCode()+"' group by substr(t.INST_BTIME,9,2)";
			List list_qb = Transaction.getExecutor().query(dataSql_qb);
			for(int i=0;i<list_qb.size();i++){
				Bean tmpBean = (Bean) list_qb.get(i);
				int day = tmpBean.getInt("DAYSTR");
				int num = tmpBean.getInt("NUM");
				x_qb[day-1] = num;
			}
		}
		StringBuilder sb_fw = new StringBuilder();
		for(int val:x_fw){
			sb_fw.append("," + val);
		}
		StringBuilder sb_qb = new StringBuilder();
		for(int val:x_qb){
			sb_qb.append("," + val);
		}
		
		
		Bean chartBean = new Bean();
		chartBean.set("fwData", "["+sb_fw.substring(1)+"]");
		chartBean.set("qbData", "["+sb_qb.substring(1)+"]");
		chartBean.set("cqData", "");
		chartBean.set("yqData", "");
		chartBean.set("avgData", "");
		
		//table数据
		//发文
		ArrayList fwTableData = new ArrayList();
		Bean dataBean1 = new Bean();
		dataBean1.set("id", "1");
		dataBean1.set("deptname", "信息部");
		//签报
		
		ArrayList qbTableData = new ArrayList();
		Bean dataBean2 = new Bean();
		dataBean2.set("id", "1");
		dataBean2.set("deptname", "销售部");
		
		fwTableData.add(dataBean1);
		qbTableData.add(dataBean2);
		Bean tableDataBean = new Bean();
		tableDataBean.set("fw", fwTableData);
		tableDataBean.set("qb", qbTableData);

		List<List> rtnList = new ArrayList<List>();  
		rtnList.add(tjList);
		
		result.set("TJ", new Bean().set("data", rtnList));
		result.set("CHART", chartBean);
		result.set("TABLE", tableDataBean);
		outBean.setData(result);
		return outBean;
	}
}
