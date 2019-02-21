package com.rh.api.util;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfProcDef;

public class GwTimeHelper {

	private static final String OA_GW_GONGWEN_WFE_TIME = "OA_GW_GONGWEN_WFE_TIME";

	public static void addNodeTime(String nid, Bean dataBean) {

		WfAct currWfAct = new WfAct(nid, false);

		List<Bean> trackList = currWfAct.getProcess().wfTracking();

		WfProcDef procDef = currWfAct.getProcess().getProcDef();

		List<Bean> timeList = ServDao.finds(OA_GW_GONGWEN_WFE_TIME,
				" and TMPL_CODE = '" + dataBean.getStr("TMPL_CODE") + "' and PROC_CODE = '" + procDef.getProcCode() + "'");

		Bean nodeMap = new Bean();
		for (Bean nodeDef : procDef.getAllNodeDef()) {
			nodeMap.put(nodeDef.getStr("NODE_CODE"), nodeDef);
		}

		Bean timeNodeMap = new Bean();
		if (timeList != null && timeList.size() > 0) {
			for (Bean timeNode : timeList) {
				timeNodeMap.put(timeNode.getStr("NODE_CODE"), timeNode);
			}
		}

		ParamBean timeBean = null;
		for (Bean nodeDef : procDef.getAllNodeDef()) {
			if (timeNodeMap.contains(nodeDef.getStr("NODE_CODE"))) {
				timeBean = new ParamBean(timeNodeMap.getBean(nodeDef.getStr("NODE_CODE")));
			} else {
				timeBean = new ParamBean();
				timeBean.set("NODE_CODE", nodeDef.getStr("NODE_CODE"));
				timeBean.set("NODE_NAME", nodeDef.getStr("NODE_NAME"));
				timeBean.set("TMPL_CODE", dataBean.getStr("TMPL_CODE"));
				timeBean.set("NODE_SORT", nodeDef.getStr("NODE_SORT"));
				timeBean.set("NODE_TIMES", 0);
				timeBean.set("AVG_TIME", 0);
				timeBean.set("TOTAL_TIME", 0);
				timeBean.set("PROC_CODE", nodeDef.getStr("PROC_CODE"));
			}

			for (Bean trackBean : trackList) {
				if (trackBean.getStr("NODE_CODE").equals(nodeDef.getStr("NODE_CODE"))) {
					int times = timeBean.getInt("NODE_TIMES") + 1;
					timeBean.set("NODE_TIMES", times);
					int totalTime = timeBean.getInt("TOTAL_TIME");
					timeBean.set("TOTAL_TIME", totalTime + trackBean.getInt("NODE_DAYS"));
				}
			}
			int times = timeBean.getInt("NODE_TIMES");
			int totalTime = timeBean.getInt("TOTAL_TIME");

			timeBean.set("AVG_TIME", Math.ceil(totalTime / (times > 0 ? times : 1)));

			ServMgr.act(OA_GW_GONGWEN_WFE_TIME, ServMgr.ACT_SAVE, timeBean);
		}
	}

	public static void subNodeTime(String nid, Bean dataBean) {
		WfAct currWfAct = new WfAct(nid, true);

		List<Bean> trackList = currWfAct.getProcess().wfTracking();

		WfProcDef procDef = currWfAct.getProcess().getProcDef();

		List<Bean> timeList = ServDao.finds(OA_GW_GONGWEN_WFE_TIME,
				" and TMPL_CODE = '" + dataBean.getStr("TMPL_CODE") + "' and PROC_CODE = '" + procDef.getProcCode() + "'");

		Bean nodeMap = new Bean();
		for (Bean nodeDef : procDef.getAllNodeDef()) {
			nodeMap.put(nodeDef.getStr("NODE_CODE"), nodeDef);
		}

		Bean timeNodeMap = new Bean();
		if (timeList != null && timeList.size() > 0) {
			for (Bean timeNode : timeList) {
				timeNodeMap.put(timeNode.getStr("NODE_CODE"), timeNode);
			}
		}

		ParamBean timeBean = null;
		for (Bean nodeDef : procDef.getAllNodeDef()) {
			if (timeNodeMap.contains(nodeDef.getStr("NODE_CODE"))) {
				timeBean = new ParamBean(timeNodeMap.getBean(nodeDef.getStr("NODE_CODE")));
			} else {
				timeBean = new ParamBean();
				timeBean.set("NODE_CODE", nodeDef.getStr("NODE_CODE"));
				timeBean.set("NODE_NAME", nodeDef.getStr("NODE_NAME"));
				timeBean.set("TMPL_CODE", dataBean.getStr("TMPL_CODE"));
				timeBean.set("NODE_SORT", nodeDef.getStr("NODE_SORT"));
				timeBean.set("NODE_TIMES", 0);
				timeBean.set("AVG_TIME", 0);
				timeBean.set("TOTAL_TIME", 0);
				timeBean.set("PROC_CODE", nodeDef.getStr("PROC_CODE"));
			}

			for (Bean trackBean : trackList) {
				if (trackBean.getStr("NODE_CODE").equals(nodeDef.getStr("NODE_CODE"))) {
					int times = timeBean.getInt("NODE_TIMES") - 1;
					timeBean.set("NODE_TIMES", times > 0 ? times : 0);
					int totalTime = timeBean.getInt("TOTAL_TIME");
					timeBean.set("TOTAL_TIME", totalTime - trackBean.getInt("NODE_DAYS"));
				}
			}
			int times = timeBean.getInt("NODE_TIMES");
			int totalTime = timeBean.getInt("TOTAL_TIME");

			timeBean.set("AVG_TIME", Math.ceil(totalTime / (times > 0 ? times : 1)));

			ServMgr.act(OA_GW_GONGWEN_WFE_TIME, ServMgr.ACT_SAVE, timeBean);
		}
	}

	public static Bean getNodeDoneInfo(List<Bean> nodeAll, List<Bean> instList, String tmplCode) {
		Bean rtnBean = new Bean();
		List<Bean> nodeDoneList = new ArrayList<Bean>();
		List<String> nodeNameList = new ArrayList<String>();
		List<String> currNodeTimeList = new ArrayList<String>();
		List<String> avgNodeTimeList = new ArrayList<String>();
		List<Bean> timeList = ServDao.finds(OA_GW_GONGWEN_WFE_TIME,
				" and TMPL_CODE = '" + tmplCode + "' and PROC_CODE = '" + nodeAll.get(0).getStr("PROC_CODE") + "'");
		int totalTime = 0;
		int currTime = 0;
		for (Bean node : nodeAll) {
			Bean doneIndo = new Bean();
			doneIndo.set("NODE_SORT", node.getInt("NODE_SORT"));
			doneIndo.set("NODE_NAME", node.getStr("NODE_NAME"));
			int currNodeTime = 0;
			nodeNameList.add(node.getInt("NODE_SORT") + "、" + node.getStr("NODE_NAME"));
			for (Bean inst : instList) {
				if (node.getStr("NODE_CODE").equals(inst.getStr("NODE_CODE"))) {
					if (doneIndo.isEmpty("DONE_TIME")) {
						doneIndo.set("DONE_TIME", inst.getStr("NODE_BTIME"));
					}
					currNodeTime += inst.getInt("NODE_DAYS");
				}
			}
			currTime += currNodeTime;
			currNodeTimeList.add(DataStatsUtil.division(currNodeTime, 60));

			int avgNodeTime = 0;
			for (Bean timeBean : timeList) {
				if (node.getStr("NODE_CODE").equals(timeBean.getStr("NODE_CODE"))) {
					avgNodeTime = timeBean.getInt("AVG_TIME");
					totalTime += timeBean.getInt("TOTAL_TIME");
					break;
				}
			}
			avgNodeTimeList.add(DataStatsUtil.division(avgNodeTime, 60));

			nodeDoneList.add(doneIndo);
		}

		rtnBean.set("totalTime", DataStatsUtil.division(totalTime, 60));
		rtnBean.set("currTime", DataStatsUtil.division(currTime, 60));
		rtnBean.set("nodeNameList", nodeNameList);
		rtnBean.set("currNodeTimeList", currNodeTimeList);
		rtnBean.set("avgNodeTimeList", avgNodeTimeList);
		rtnBean.set("NODE_LIST", nodeDoneList);
		return rtnBean;
	}
}
