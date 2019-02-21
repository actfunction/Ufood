package com.rh.api.impl.stats;

import java.util.ArrayList;
import java.util.List;

import com.rh.api.impl.DataStatsServImpl;
import com.rh.api.bean.ApiOutBean;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.DateUtils;
import com.rh.api.util.DataStatsUtil;

public class StatsServImpl extends DataStatsServImpl {

	private static final String _OA_GW_GONGWEN_FW = "OA_GW_GONGWEN_FW";

	private static final String _OA_GW_GONGWEN_SW = "OA_GW_GONGWEN_SW";

	private static final String _OA_GW_GONGWEN_QB = "OA_GW_GONGWEN_QB";

	@Override
	public ApiOutBean getUserStatsData(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		UserBean currUser = Context.getUserBean();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(
				"select TMPL_TYPE_CODE, NODE_CHILD_TYPE, count(distinct GW_ID) TOTAL_COUNT, sum(SCORE)/COUNT(DISTINCT GW_ID) SCORE, ");
		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append("BL_MONTH");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append("to_char(to_date(NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') NODE_BTIME");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append("to_char(to_date(NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') NODE_BTIME");
		}
		sqlBuilder.append(" from SY_WFE_NODE_INST_GW_V where TO_USER_ID = '").append(currUser.getId())
				.append("' and NODE_BTIME >= '").append(reqData.getStr("bTime")).append("' and NODE_BTIME <= '")
				.append(reqData.getStr("eTime")).append("'");
		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append(
					" group by TMPL_TYPE_CODE,NODE_CHILD_TYPE,BL_MONTH order by TMPL_TYPE_CODE, NODE_CHILD_TYPE, BL_MONTH");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append(
					" group by TMPL_TYPE_CODE,NODE_CHILD_TYPE,to_char(to_date(NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') order by TMPL_TYPE_CODE, NODE_CHILD_TYPE, NODE_BTIME");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append(
					" group by TMPL_TYPE_CODE,NODE_CHILD_TYPE,to_char(to_date(NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') order by TMPL_TYPE_CODE, NODE_CHILD_TYPE, NODE_BTIME");
		}

		List<Bean> resultList = Transaction.getExecutor().query(sqlBuilder.toString());

		sqlBuilder = new StringBuilder();
		sqlBuilder.append("select count(distinct DATA_ID) TOTAL_COUNT, IS_DELAY, ");
		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append("TO_NUMBER(TO_CHAR(TO_DATE(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'MM')) BL_MONTH");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append("to_char(to_date(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') BEGIN_TIME");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append("to_char(to_date(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') BEGIN_TIME");
		}

		sqlBuilder.append(" from OA_GW_DELAY_RECORD where S_USER = '").append(currUser.getId())
				.append("' and BEGIN_TIME >= '").append(reqData.getStr("bTime")).append("' and BEGIN_TIME <= '")
				.append(reqData.getStr("eTime")).append("'");
		;
		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append(
					" group by IS_DELAY,TO_NUMBER(TO_CHAR(TO_DATE(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'MM')) order by IS_DELAY");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append(
					" group by IS_DELAY,to_char(to_date(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') order by IS_DELAY");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append(
					" group by IS_DELAY,to_char(to_date(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') order by IS_DELAY");
		}

		List<Bean> delayList = Transaction.getExecutor().query(sqlBuilder.toString());

		Bean rtnBean = new Bean();
		if (reqData.getStr("type").equals("y")) {
			rtnBean.set("table1", genUserYearData(resultList, delayList));
		} else if (reqData.getStr("type").equals("m")) {
			rtnBean.set("table1",
					genUserMonthData(resultList, delayList, reqData.getStr("bTime"), reqData.getStr("eTime")));
		} else if (reqData.getStr("type").equals("w")) {
			rtnBean.set("table1",
					genUserDayData(resultList, delayList, reqData.getStr("bTime"), reqData.getStr("eTime")));
		}

		ParamBean queryBean = new ParamBean();

		queryBean.setQuerySearchWhere(" and GW_ID in (select GW_ID from SY_WFE_NODE_INST_GW_V where TO_USER_ID = '"
				+ currUser.getId() + "' and NODE_BTIME >= '" + reqData.getStr("bTime") + "' and NODE_BTIME <= '"
				+ reqData.getStr("eTime") + "' and NODE_CHILD_TYPE in (1,2,3))");
		queryBean.set("_linkWhere", " and 1 = 1");
		queryBean.setQueryNoPageFlag(true);
		OutBean dataBean = ServMgr.act("OA_GW_GONGWEN", ServMgr.ACT_QUERY, queryBean);

		rtnBean.set("table2", dataBean.getDataList());
		outBean.setData(rtnBean);

		return outBean;
	}

	private List<Bean> genUserYearData(List<Bean> dataList, List<Bean> delayList) {
		List<Bean> rtnList = new ArrayList<Bean>();
		Bean data;

		for (int i = 1; i <= 12; i++) {
			data = new Bean();
			data.set("sort", i);
			int gwCount = 0;
			double score = 0.0f;
			for (Bean b : dataList) {
				if (b.getInt("BL_MONTH") != i) {
					continue;
				}
				if (_OA_GW_GONGWEN_FW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 3) {
						data.set("hq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zbf", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				} else if (_OA_GW_GONGWEN_SW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 2) {
						data.set("xb", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zb", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				} else if (_OA_GW_GONGWEN_QB.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 3) {
						data.set("hqq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zbq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				}
			}

			for (Bean b : delayList) {
				if (b.getInt("BL_MONTH") != i) {
					continue;
				}
				if (b.getInt("IS_DELAY") == 2) {
					data.set("CQ", b.getInt("TOTAL_COUNT"));
				} else if (b.getInt("IS_DELAY") == 1) {
					data.set("YQ", b.getInt("TOTAL_COUNT"));
				}
			}
			data.set("ALL_NUM", gwCount);
			data.set("ZLF", score / 2);
			data.set("CS", 0);
			rtnList.add(data);
		}

		return rtnList;
	}

	private List<Bean> genUserMonthData(List<Bean> dataList, List<Bean> delayList, String bTime, String eTime) {

		List<Bean> rtnList = new ArrayList<Bean>();
		Bean data;

		int days = DateUtils.getDiffDays(bTime + " 00:00:00", eTime + " 23:59:59");
		for (int i = 0; i < days; i++) {
			String currDate = DateUtils.getDateAdded(i, bTime);
			if (DateUtils.getDiffTime(currDate + " 00:00:00", eTime + " 00:00:00") < 0) {
				break;
			}
			data = new Bean();
			data.set("sort", currDate);
			int gwCount = 0;
			double score = 0.0f;
			for (Bean b : dataList) {
				String ymd = b.getStr("NODE_BTIME").substring(0, 10);
				if (!ymd.equals(currDate)) {
					continue;
				}
				if (_OA_GW_GONGWEN_FW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 3) {
						data.set("hq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zbf", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				} else if (_OA_GW_GONGWEN_SW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 2) {
						data.set("xb", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zb", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				} else if (_OA_GW_GONGWEN_QB.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 3) {
						data.set("hqq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zbq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				}
			}

			for (Bean b : delayList) {
				String ymd = b.getStr("BEGIN_TIME").substring(0, 10);
				if (!ymd.equals(currDate)) {
					continue;
				}
				if (b.getInt("IS_DELAY") == 2) {
					data.set("CQ", b.getInt("TOTAL_COUNT"));
				} else if (b.getInt("IS_DELAY") == 1) {
					data.set("YQ", b.getInt("TOTAL_COUNT"));
				}
			}
			data.set("ALL_NUM", gwCount);
			data.set("ZLF", score / 2);
			data.set("CS", 0);
			rtnList.add(data);
		}

		return rtnList;
	}

	private List<Bean> genUserDayData(List<Bean> dataList, List<Bean> delayList, String bTime, String eTime) {
		return genUserMonthData(dataList, delayList, bTime, eTime);
	}

	@Override
	public ApiOutBean getDeptStatsDataTodo(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select TMPL_TYPE_CODE, NODE_CHILD_TYPE, count(distinct GW_ID) TOTAL_COUNT");
		sqlBuilder
				.append(" from SY_WFE_NODE_INST_GW_V where PI_ID in (select PI_ID from sy_wfe_node_inst where NODE_IF_RUNNING = 1) and TO_TDEPT_CODE = '")
				.append(currUser.getTDeptCode()).append("' group by TMPL_TYPE_CODE, NODE_CHILD_TYPE");

		List<Bean> dataList = Transaction.getExecutor().query(sqlBuilder.toString());

		Bean rtnBean = new Bean();

		int total_count = 0;
		int zb_count = 0;
		int xb_count = 0;

		List<Bean> zblist = new ArrayList<Bean>();
		List<Bean> xblist = new ArrayList<Bean>();
		String flag = "";
		for (int i = 0, len = dataList.size(); i < len; i++) {
			Bean b = dataList.get(i);
			if (_OA_GW_GONGWEN_SW.equals(b.getStr("TMPL_TYPE_CODE"))) {
				Bean tb = new Bean();
				if (1 == b.getInt("NODE_CHILD_TYPE")) {
					tb.set("name", "收文");
					tb.set("value", b.getInt("TOTAL_COUNT"));
					zb_count += b.getInt("TOTAL_COUNT");
					total_count += b.getInt("TOTAL_COUNT");
					zblist.add(tb);
					flag += "zb";
				} else if (2 == b.getInt("NODE_CHILD_TYPE")) {
					tb.set("name", "收文");
					tb.set("value", b.getInt("TOTAL_COUNT"));
					xb_count += b.getInt("TOTAL_COUNT");
					total_count += b.getInt("TOTAL_COUNT");
					xblist.add(tb);
					flag += "|xb";
				}
			}
		}

		if (flag.indexOf("zb") < 0) {
			Bean tb = new Bean();
			tb.set("name", "收文");
			tb.set("value", 0);
			zblist.add(tb);
		}
		if (flag.indexOf("xb") < 0) {
			Bean tb = new Bean();
			tb.set("name", "收文");
			tb.set("value", 0);
			xblist.add(tb);
		}
		flag = "";

		for (int i = 0, len = dataList.size(); i < len; i++) {
			Bean b = dataList.get(i);
			if (_OA_GW_GONGWEN_FW.equals(b.getStr("TMPL_TYPE_CODE"))) {
				Bean tb = new Bean();
				if (1 == b.getInt("NODE_CHILD_TYPE")) {
					tb.set("name", "发文");
					tb.set("value", b.getInt("TOTAL_COUNT"));
					zb_count += b.getInt("TOTAL_COUNT");
					total_count += b.getInt("TOTAL_COUNT");
					zblist.add(tb);
					flag += "zb";
				} else if (3 == b.getInt("NODE_CHILD_TYPE")) {
					tb.set("name", "发文");
					tb.set("value", b.getInt("TOTAL_COUNT"));
					xb_count += b.getInt("TOTAL_COUNT");
					total_count += b.getInt("TOTAL_COUNT");
					xblist.add(tb);
					flag += "|xb";
				}
			}
		}

		if (flag.indexOf("zb") < 0) {
			Bean tb = new Bean();
			tb.set("name", "发文");
			tb.set("value", 0);
			zblist.add(tb);
		}
		if (flag.indexOf("xb") < 0) {
			Bean tb = new Bean();
			tb.set("name", "发文");
			tb.set("value", 0);
			xblist.add(tb);
		}
		flag = "";

		for (int i = 0, len = dataList.size(); i < len; i++) {
			Bean b = dataList.get(i);
			if (_OA_GW_GONGWEN_QB.equals(b.getStr("TMPL_TYPE_CODE"))) {
				Bean tb = new Bean();
				if (1 == b.getInt("NODE_CHILD_TYPE")) {
					tb.set("name", "签报");
					tb.set("value", b.getInt("TOTAL_COUNT"));
					zb_count += b.getInt("TOTAL_COUNT");
					total_count += b.getInt("TOTAL_COUNT");
					zblist.add(tb);
					flag += "zb";
				} else if (3 == b.getInt("NODE_CHILD_TYPE")) {
					tb.set("name", "签报");
					tb.set("value", b.getInt("TOTAL_COUNT"));
					xb_count += b.getInt("TOTAL_COUNT");
					total_count += b.getInt("TOTAL_COUNT");
					xblist.add(tb);
					flag += "|xb";
				}
			}
		}

		if (flag.indexOf("zb") < 0) {
			Bean tb = new Bean();
			tb.set("name", "签报");
			tb.set("value", 0);
			zblist.add(tb);
		}

		if (flag.indexOf("xb") < 0) {
			Bean tb = new Bean();
			tb.set("name", "签报");
			tb.set("value", 0);
			xblist.add(tb);
		}

		Bean tb = new Bean();
		tb.set("name", "主办");
		tb.set("value", zb_count);
		zblist.add(0, tb);

		tb = new Bean();
		tb.set("name", "协办");
		tb.set("value", xb_count);
		xblist.add(0, tb);

		rtnBean.set("TOTAL_COUNT", total_count);
		rtnBean.set("zbList", zblist);
		rtnBean.set("xbList", xblist);

		ParamBean queryBean = new ParamBean();

		queryBean.setQuerySearchWhere(
				" and GW_ID in (SELECT DISTINCT GW_ID FROM SY_WFE_NODE_INST_GW_V WHERE NODE_IF_RUNNING = 1 AND TO_TDEPT_CODE = '"
						+ currUser.getTDeptCode() + "')");
		queryBean.set("_linkWhere", " and 1 = 1");
		queryBean.setQueryNoPageFlag(true);
		OutBean dataBean = ServMgr.act("OA_GW_GONGWEN", ServMgr.ACT_QUERY, queryBean);

		rtnBean.set("dataList", dataBean.getDataList());

		outBean.setData(rtnBean);

		return outBean;
	}

	@Override
	public ApiOutBean getDeptStatsDataTodoHis(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		UserBean currUser = Context.getUserBean();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(
				"select TMPL_TYPE_CODE, NODE_CHILD_TYPE, count(distinct GW_ID) TOTAL_COUNT, sum(SCORE)/COUNT(DISTINCT GW_ID) SCORE, ");
		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append("BL_MONTH");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append("to_char(to_date(NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') NODE_BTIME");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append("to_char(to_date(NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') NODE_BTIME");
		}
		sqlBuilder.append(" from SY_WFE_NODE_INST_GW_V where TO_TDEPT_CODE = '").append(currUser.getTDeptCode())
				.append("' and NODE_BTIME >= '").append(reqData.getStr("bTime")).append("' and NODE_BTIME <= '")
				.append(reqData.getStr("eTime")).append("' and S_WF_STATE = 2");
		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append(
					" group by TMPL_TYPE_CODE,NODE_CHILD_TYPE,BL_MONTH order by TMPL_TYPE_CODE, NODE_CHILD_TYPE, BL_MONTH");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append(
					" group by TMPL_TYPE_CODE,NODE_CHILD_TYPE,to_char(to_date(NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') order by TMPL_TYPE_CODE, NODE_CHILD_TYPE, NODE_BTIME");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append(
					" group by TMPL_TYPE_CODE,NODE_CHILD_TYPE,to_char(to_date(NODE_BTIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') order by TMPL_TYPE_CODE, NODE_CHILD_TYPE, NODE_BTIME");
		}

		List<Bean> resultList = Transaction.getExecutor().query(sqlBuilder.toString());

		sqlBuilder = new StringBuilder();
		sqlBuilder.append("select count(distinct DATA_ID) TOTAL_COUNT, IS_DELAY, ");
		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append("TO_NUMBER(TO_CHAR(TO_DATE(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'MM')) BL_MONTH");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append("to_char(to_date(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') BEGIN_TIME");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append("to_char(to_date(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') BEGIN_TIME");
		}

		sqlBuilder.append(" from OA_GW_DELAY_RECORD where S_TDEPT = '").append(currUser.getTDeptCode())
				.append("' and BEGIN_TIME >= '").append(reqData.getStr("bTime")).append("' and BEGIN_TIME <= '")
				.append(reqData.getStr("eTime"))
				.append("' and DATA_ID in (select gw_id from oa_gw_gongwen where S_WF_STATE = 2)");
		;
		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append(
					" group by IS_DELAY,TO_NUMBER(TO_CHAR(TO_DATE(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'MM')) order by IS_DELAY");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append(
					" group by IS_DELAY,to_char(to_date(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') order by IS_DELAY");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append(
					" group by IS_DELAY,to_char(to_date(BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') order by IS_DELAY");
		}

		List<Bean> delayList = Transaction.getExecutor().query(sqlBuilder.toString());

		Bean rtnBean = new Bean();
		if (reqData.getStr("type").equals("y")) {
			rtnBean.set("table1", genDeptYearData(resultList, delayList));
		} else if (reqData.getStr("type").equals("m")) {
			rtnBean.set("table1",
					genDeptMonthData(resultList, delayList, reqData.getStr("bTime"), reqData.getStr("eTime")));
		} else if (reqData.getStr("type").equals("w")) {
			rtnBean.set("table1",
					genDeptDayData(resultList, delayList, reqData.getStr("bTime"), reqData.getStr("eTime")));
		}

		ParamBean queryBean = new ParamBean();

		queryBean.setQuerySearchWhere(" and GW_ID in (select GW_ID from SY_WFE_NODE_INST_GW_V where TO_TDEPT_CODE = '"
				+ currUser.getTDeptCode() + "' and NODE_BTIME >= '" + reqData.getStr("bTime") + "' and NODE_BTIME <= '"
				+ reqData.getStr("eTime") + "' and NODE_CHILD_TYPE in (1,2,3) and S_WF_STATE = 2)");
		queryBean.set("_linkWhere", " and 1 = 1");
		queryBean.setQueryNoPageFlag(true);
		OutBean dataBean = ServMgr.act("OA_GW_GONGWEN", ServMgr.ACT_QUERY, queryBean);

		rtnBean.set("table2", dataBean.getDataList());
		outBean.setData(rtnBean);

		return outBean;
	}

	private Bean genDeptYearData(List<Bean> dataList, List<Bean> delayList) {
		Bean rtnBean = new Bean();
		List<Bean> rtnList = new ArrayList<Bean>();
		Bean chartData = new Bean();
		List<Integer> data1 = new ArrayList<Integer>();
		List<Integer> data2 = new ArrayList<Integer>();
		List<Integer> data3 = new ArrayList<Integer>();
		Bean data;

		int fwTotal = 0;
		int swTotal = 0;
		int qbTotal = 0;

		for (int i = 1; i <= 12; i++) {
			data = new Bean();
			data.set("sort", i);
			int gwCount = 0;
			int fwCount = 0;
			int swCount = 0;
			int qbCount = 0;
			double score = 0.0f;
			for (Bean b : dataList) {
				if (b.getInt("BL_MONTH") != i) {
					continue;
				}
				if (_OA_GW_GONGWEN_FW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 3) {
						data.set("hq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						fwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zbf", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						fwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				} else if (_OA_GW_GONGWEN_SW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 2) {
						data.set("xb", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						swCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zb", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						swCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				} else if (_OA_GW_GONGWEN_QB.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 3) {
						data.set("hqq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						qbCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zbq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						qbCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				}
			}
			data1.add(swCount);
			data2.add(fwCount);
			data3.add(qbCount);
			fwTotal += fwCount;
			swTotal += swCount;
			qbTotal += qbCount;

			for (Bean b : delayList) {
				if (b.getInt("BL_MONTH") != i) {
					continue;
				}
				if (b.getInt("IS_DELAY") == 2) {
					data.set("CQ", b.getInt("TOTAL_COUNT"));
				} else if (b.getInt("IS_DELAY") == 1) {
					data.set("YQ", b.getInt("TOTAL_COUNT"));
				}
			}
			data.set("ALL_NUM", gwCount);
			data.set("ZLF", score / 2);
			data.set("CS", 0);
			rtnList.add(data);
		}

		chartData.set("fwTotal", fwTotal);
		chartData.set("swTotal", swTotal);
		chartData.set("qbTotal", qbTotal);
		chartData.set("data1", data1);
		chartData.set("data2", data2);
		chartData.set("data3", data3);
		rtnBean.set("list", rtnList);
		rtnBean.set("chartData", chartData);

		return rtnBean;
	}

	private Bean genDeptMonthData(List<Bean> dataList, List<Bean> delayList, String bTime, String eTime) {
		Bean rtnBean = new Bean();
		List<Bean> rtnList = new ArrayList<Bean>();
		Bean data;

		Bean chartData = new Bean();
		List<Integer> data1 = new ArrayList<Integer>();
		List<Integer> data2 = new ArrayList<Integer>();
		List<Integer> data3 = new ArrayList<Integer>();

		int days = DateUtils.getDiffDays(bTime + " 00:00:00", eTime + " 23:59:59");
		int fwTotal = 0;
		int swTotal = 0;
		int qbTotal = 0;
		for (int i = 0; i < days; i++) {
			String currDate = DateUtils.getDateAdded(i, bTime);
			if (DateUtils.getDiffTime(currDate + " 00:00:00", eTime + " 00:00:00") < 0) {
				break;
			}
			data = new Bean();
			data.set("sort", currDate);
			int gwCount = 0;
			int fwCount = 0;
			int swCount = 0;
			int qbCount = 0;
			double score = 0.0f;
			for (Bean b : dataList) {
				String ymd = b.getStr("NODE_BTIME").substring(0, 10);
				if (!ymd.equals(currDate)) {
					continue;
				}
				if (_OA_GW_GONGWEN_FW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 3) {
						data.set("hq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						fwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zbf", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						fwCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				} else if (_OA_GW_GONGWEN_SW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 2) {
						data.set("xb", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						swCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zb", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						swCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				} else if (_OA_GW_GONGWEN_QB.equals(b.getStr("TMPL_TYPE_CODE"))) {
					if (b.getInt("NODE_CHILD_TYPE") == 3) {
						data.set("hqq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						qbCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					} else if (b.getInt("NODE_CHILD_TYPE") == 1) {
						data.set("zbq", b.getInt("TOTAL_COUNT"));
						gwCount += b.getInt("TOTAL_COUNT");
						qbCount += b.getInt("TOTAL_COUNT");
						score += b.getDouble("SCORE");
					}
				}
			}

			data1.add(swCount);
			data2.add(fwCount);
			data3.add(qbCount);
			fwTotal += fwCount;
			swTotal += swCount;
			qbTotal += qbCount;

			for (Bean b : delayList) {
				String ymd = b.getStr("BEGIN_TIME").substring(0, 10);
				if (!ymd.equals(currDate)) {
					continue;
				}
				if (b.getInt("IS_DELAY") == 2) {
					data.set("CQ", b.getInt("TOTAL_COUNT"));
				} else if (b.getInt("IS_DELAY") == 1) {
					data.set("YQ", b.getInt("TOTAL_COUNT"));
				}
			}
			data.set("ALL_NUM", gwCount);
			data.set("ZLF", score / 2);
			data.set("CS", 0);
			rtnList.add(data);
		}

		chartData.set("fwTotal", fwTotal);
		chartData.set("swTotal", swTotal);
		chartData.set("qbTotal", qbTotal);
		chartData.set("data1", data1);
		chartData.set("data2", data2);
		chartData.set("data3", data3);
		rtnBean.set("list", rtnList);
		rtnBean.set("chartData", chartData);

		return rtnBean;
	}

	private Bean genDeptDayData(List<Bean> dataList, List<Bean> delayList, String bTime, String eTime) {
		return genDeptMonthData(dataList, delayList, bTime, eTime);
	}

	@Override
	public ApiOutBean getOrgGwCount(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
		List<Bean> deptList = ServDao.finds("SY_ORG_DEPT",
				" and DEPT_PCODE = '" + currUser.getODeptCode() + "' order by DEPT_SORT");

		StringBuilder sqlBuilder = new StringBuilder();

		sqlBuilder.append("SELECT S_TDEPT,TMPL_TYPE_CODE,COUNT(GW_ID) TOTAL_COUNT,");

		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append("TO_NUMBER(TO_CHAR(TO_DATE(GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'MM')) GW_TIME");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append("to_char(to_date(GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') GW_TIME");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append("to_char(to_date(GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd') GW_TIME");
		}
		sqlBuilder.append(" from OA_GW_GONGWEN where S_ODEPT = '").append(currUser.getODeptCode())
				.append("' and GW_BEGIN_TIME >= '").append(reqData.getStr("bTime")).append("' and GW_BEGIN_TIME <= '")
				.append(reqData.getStr("eTime")).append("'");
		if (reqData.getStr("type").equals("y")) {
			sqlBuilder.append(
					" group by S_TDEPT,TMPL_TYPE_CODE,TO_NUMBER(TO_CHAR(TO_DATE(GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'), 'MM'))");
		} else if (reqData.getStr("type").equals("m")) {
			sqlBuilder.append(
					" group by S_TDEPT,TMPL_TYPE_CODE,to_char(to_date(GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd')");
		} else if (reqData.getStr("type").equals("w")) {
			sqlBuilder.append(
					" group by S_TDEPT,TMPL_TYPE_CODE,to_char(to_date(GW_BEGIN_TIME, 'yyyy-mm-dd HH24:mi:ss'),'yyyy-mm-dd')");
		}

		List<Bean> groupList = Transaction.getExecutor().query(sqlBuilder.toString());

		Bean rtnBean = new Bean();

		if (reqData.getStr("type").equals("y")) {
			rtnBean.set("table1", genOrgYearData(deptList, groupList));
		} else if (reqData.getStr("type").equals("m")) {
			rtnBean.set("table1",
					genOrgMonthData(deptList, groupList, reqData.getStr("bTime"), reqData.getStr("eTime")));
		} else if (reqData.getStr("type").equals("w")) {
			rtnBean.set("table1", genOrgDayData(deptList, groupList, reqData.getStr("bTime"), reqData.getStr("eTime")));
		}

		outBean.setData(rtnBean);

		return outBean;
	}

	@Override
	public ApiOutBean getOrgGwDelay(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		String bTime = reqData.getStr("bTime");
		String eTime = reqData.getStr("eTime");
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder
				.append("SELECT COUNT (DISTINCT data_id) total_count,scene_role,SUM (done_time) / COUNT (DISTINCT data_id) avg_time FROM OA_GW_DELAY_RECORD WHERE scene_role IN (1, 3)")
				.append(" and BEGIN_TIME >= '").append(bTime).append("' and BEGIN_TIME <= '").append(eTime)
				.append("' group by scene_role");
		List<Bean> groupList = Transaction.getExecutor().query(sqlBuilder.toString());
		Bean rtnBean = new Bean();
		int gwTotalCount = 1;
		for (Bean group : groupList) {
			if (group.getInt("SCENE_ROLE") == 1) {
				gwTotalCount = group.getInt("TOTAL_COUNT");
				rtnBean.set("zb", DataStatsUtil.division(group.getInt("AVG_TIME"), 60));
			} else if (group.getInt("SCENE_ROLE") == 3) {
				rtnBean.set("hq", DataStatsUtil.division(group.getInt("AVG_TIME"), 60));
			}
		}
		sqlBuilder = new StringBuilder();
		sqlBuilder
				.append("SELECT COUNT (DISTINCT data_id) TOTAL_COUNT,scene_role FROM OA_GW_DELAY_RECORD WHERE scene_role IN (1, 3)")
				.append(" and BEGIN_TIME >= '").append(bTime).append("' and BEGIN_TIME <= '").append(eTime)
				.append("' and IS_DELAY = 2 group by scene_role");
		List<Bean> cqList = Transaction.getExecutor().query(sqlBuilder.toString());
		for (Bean cqBean : cqList) {
			if (cqBean.getInt("SCENE_ROLE") == 1) {
				rtnBean.set("cq_zb", DataStatsUtil.division(cqBean.getInt("TOTAL_COUNT"), gwTotalCount));
			} else if (cqBean.getInt("SCENE_ROLE") == 3) {
				rtnBean.set("cq_hq", DataStatsUtil.division(cqBean.getInt("TOTAL_COUNT"), gwTotalCount));
			}
		}
		sqlBuilder = new StringBuilder();
		sqlBuilder
				.append("SELECT COUNT (DISTINCT data_id) TOTAL_COUNT,scene_role FROM OA_GW_DELAY_RECORD WHERE scene_role IN (1, 3)")
				.append(" and BEGIN_TIME >= '").append(bTime).append("' and BEGIN_TIME <= '").append(eTime)
				.append("' and IS_DELAY = 1 group by scene_role");
		List<Bean> yqList = Transaction.getExecutor().query(sqlBuilder.toString());
		for (Bean cqBean : yqList) {
			if (cqBean.getInt("SCENE_ROLE") == 1) {
				rtnBean.set("yq_zb", DataStatsUtil.division(cqBean.getInt("TOTAL_COUNT"), gwTotalCount));
			} else if (cqBean.getInt("SCENE_ROLE") == 3) {
				rtnBean.set("yq_hq", DataStatsUtil.division(cqBean.getInt("TOTAL_COUNT"), gwTotalCount));
			}
		}

		sqlBuilder = new StringBuilder();
		sqlBuilder
				.append("SELECT COUNT (gw_id), SUM (SCORE) / COUNT (gw_id) AVG_SCORE FROM OA_GW_GONGWEN WHERE tmpl_type_code IN ('OA_GW_GONGWEN_FW', 'OA_GW_GONGWEN_QB')")
				.append(" and GW_BEGIN_TIME >= '").append(bTime).append("' and GW_BEGIN_TIME <= '").append(eTime)
				.append("' and SCORE != 0");
		Bean scoreBean = Transaction.getExecutor().queryOne(sqlBuilder.toString());
		if (scoreBean != null) {
			rtnBean.set("score", DataStatsUtil.getNum(scoreBean.getDouble("AVG_SCORE")));
		}
		outBean.setData(rtnBean);
		return outBean;
	}

	@Override
	public ApiOutBean getOrgSWQsCount(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		return outBean;
	}

	@Override
	public ApiOutBean getOrgBLTime(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		return outBean;
	}

	private Bean genOrgYearData(List<Bean> deptList, List<Bean> groupList) {
		Bean rtnBean = new Bean();
		List<Bean> rtnList = new ArrayList<Bean>();
		Bean chartData = new Bean();
		List<Integer> data1 = new ArrayList<Integer>();
		List<Integer> data2 = new ArrayList<Integer>();
		List<Integer> data3 = new ArrayList<Integer>();
		Bean data;

		int fwTotal = 0;
		int swTotal = 0;
		int qbTotal = 0;

		for (int i = 1; i <= 12; i++) {
			data = new Bean();
			data.set("sort", i);
			int fwCount = 0;
			int swCount = 0;
			int qbCount = 0;
			double score = 0.0f;
			for (Bean b : groupList) {
				if (b.getInt("GW_TIME") != i) {
					continue;
				}
				if (_OA_GW_GONGWEN_FW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					fwCount += b.getInt("TOTAL_COUNT");
				} else if (_OA_GW_GONGWEN_SW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					swCount += b.getInt("TOTAL_COUNT");
				} else if (_OA_GW_GONGWEN_QB.equals(b.getStr("TMPL_TYPE_CODE"))) {
					qbCount += b.getInt("TOTAL_COUNT");
				}
			}
			data1.add(swCount);
			data2.add(fwCount);
			data3.add(qbCount);
			fwTotal += fwCount;
			swTotal += swCount;
			qbTotal += qbCount;
		}

		for (int i = 0, len = deptList.size(); i < len; i++) {
			Bean dept = deptList.get(i);
			data = new Bean();
			data.set("DEPT_NAME", dept.getStr("DEPT_NAME"));
			for (Bean b : groupList) {
				if (!dept.getId().equals(b.getStr("S_TDEPT"))) {
					continue;
				}
				if (_OA_GW_GONGWEN_FW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					data.set("fw", b.getInt("TOTAL_COUNT"));
				} else if (_OA_GW_GONGWEN_SW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					data.set("sw", b.getInt("TOTAL_COUNT"));
				} else if (_OA_GW_GONGWEN_QB.equals(b.getStr("TMPL_TYPE_CODE"))) {
					data.set("qb", b.getInt("TOTAL_COUNT"));
				}
			}
			rtnList.add(data);
		}

		chartData.set("fwTotal", fwTotal);
		chartData.set("swTotal", swTotal);
		chartData.set("qbTotal", qbTotal);
		chartData.set("data1", data1);
		chartData.set("data2", data2);
		chartData.set("data3", data3);
		rtnBean.set("list", rtnList);
		rtnBean.set("chartData", chartData);

		return rtnBean;
	}

	private Bean genOrgMonthData(List<Bean> deptList, List<Bean> groupList, String bTime, String eTime) {
		Bean rtnBean = new Bean();
		List<Bean> rtnList = new ArrayList<Bean>();
		Bean data;

		Bean chartData = new Bean();
		List<Integer> data1 = new ArrayList<Integer>();
		List<Integer> data2 = new ArrayList<Integer>();
		List<Integer> data3 = new ArrayList<Integer>();

		int days = DateUtils.getDiffDays(bTime + " 00:00:00", eTime + " 23:59:59");
		int fwTotal = 0;
		int swTotal = 0;
		int qbTotal = 0;
		for (int i = 0; i < days; i++) {
			String currDate = DateUtils.getDateAdded(i, bTime);
			if (DateUtils.getDiffTime(currDate + " 00:00:00", eTime + " 00:00:00") < 0) {
				break;
			}
			data = new Bean();
			int fwCount = 0;
			int swCount = 0;
			int qbCount = 0;
			for (Bean b : groupList) {
				String ymd = b.getStr("GW_TIME").substring(0, 10);
				if (!ymd.equals(currDate)) {
					continue;
				}
				if (_OA_GW_GONGWEN_FW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					fwCount += b.getInt("TOTAL_COUNT");
				} else if (_OA_GW_GONGWEN_SW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					swCount += b.getInt("TOTAL_COUNT");
				} else if (_OA_GW_GONGWEN_QB.equals(b.getStr("TMPL_TYPE_CODE"))) {
					qbCount += b.getInt("TOTAL_COUNT");
				}
			}

			data1.add(swCount);
			data2.add(fwCount);
			data3.add(qbCount);
			fwTotal += fwCount;
			swTotal += swCount;
			qbTotal += qbCount;
		}

		for (int i = 0, len = deptList.size(); i < len; i++) {
			Bean dept = deptList.get(i);
			data = new Bean();
			data.set("DEPT_NAME", dept.getStr("DEPT_NAME"));
			for (Bean b : groupList) {
				if (!dept.getId().equals(b.getStr("S_TDEPT"))) {
					continue;
				}
				if (_OA_GW_GONGWEN_FW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					data.set("fw", b.getInt("TOTAL_COUNT"));
				} else if (_OA_GW_GONGWEN_SW.equals(b.getStr("TMPL_TYPE_CODE"))) {
					data.set("sw", b.getInt("TOTAL_COUNT"));
				} else if (_OA_GW_GONGWEN_QB.equals(b.getStr("TMPL_TYPE_CODE"))) {
					data.set("qb", b.getInt("TOTAL_COUNT"));
				}
			}
			rtnList.add(data);
		}

		chartData.set("fwTotal", fwTotal);
		chartData.set("swTotal", swTotal);
		chartData.set("qbTotal", qbTotal);
		chartData.set("data1", data1);
		chartData.set("data2", data2);
		chartData.set("data3", data3);
		rtnBean.set("list", rtnList);
		rtnBean.set("chartData", chartData);

		return rtnBean;
	}

	private Bean genOrgDayData(List<Bean> deptList, List<Bean> groupList, String bTime, String eTime) {
		return genOrgMonthData(deptList, groupList, bTime, eTime);
	}
}
