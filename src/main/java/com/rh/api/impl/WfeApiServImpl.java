package com.rh.api.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.util.Base64;
import com.rh.api.BaseApiServ;
import com.rh.api.bean.ApiOutBean;
import com.rh.api.serv.IWfeApiServ;
import com.rh.api.util.ApiConstant;
import com.rh.api.util.DataStatsUtil;
import com.rh.api.util.GwTimeHelper;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.FileMgr;
import com.rh.core.comm.workday.WorkTime;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.org.util.OrgConstant;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
//import com.rh.core.serv.ServDao;
import com.rh.core.serv.send.SendUserServ;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
//import com.rh.core.util.DateUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfContext;
import com.rh.core.wfe.WfParam;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.core.wfe.db.WfNodeInstHisDao;
import com.rh.core.wfe.db.WfNodeUserDao;
import com.rh.core.wfe.db.WfNodeUserHisDao;
import com.rh.core.wfe.def.WfLineDef;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.resource.GroupBean;
import com.rh.core.wfe.resource.WfBinderManager;
import com.rh.core.wfe.resource.WfeBinder;
import com.rh.core.wfe.serv.ProcServ;
import com.rh.core.wfe.util.WfeConstant;

public class WfeApiServImpl extends BaseApiServ implements IWfeApiServ {

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public ApiOutBean getWfeTrack(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");

		if (reqData.isEmpty("servId") || reqData.isEmpty("dataId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		// 取得SERV_ID服务相关表中S_WF_INST字段值
		// Bean wfBean = ServDao.find(servId, dataId);
		Bean wfBean = ServMgr.act(servId, ServMgr.ACT_BYID, new Bean().setId(dataId));
		String wfInst = wfBean.getStr("S_WF_INST");
		String procRunning = wfBean.getStr("S_WF_STATE");
		String queryTable = "";
		ParamBean paramBean = new ParamBean();
		paramBean.set("PI_ID", wfInst);
		if (procRunning.equals("2")) { // 流程已办结
			queryTable = WfNodeInstHisDao.SY_WFE_NODE_INST_HIS_SERV;
		} else { // 流程未办结
			queryTable = WfNodeInstDao.SY_WFE_NODE_INST_SERV;
		}

		// List<Bean> list = ServDao.finds(queryTable, paramBean);
		List<Bean> list = (List<Bean>) ServMgr.act(queryTable, ServMgr.ACT_FINDS, paramBean).getData();

		HashMap<String, Bean> map = new HashMap<String, Bean>();

		for (Bean nodeInstBean : list) {
			map.put(nodeInstBean.getId(), nodeInstBean);
		}

		List<Bean> mindList = (List<Bean>) ServMgr.act("SY_COMM_MIND", ServMgr.ACT_FINDS,
				new ParamBean().set("_WHERE_", " and DATA_ID = '" + dataId + "'")).getData();

		HashMap<String, Bean> mindMap = new HashMap<String, Bean>();

		for (Bean mindBean : mindList) {
			mindMap.put(mindBean.getStr("WF_NI_ID"), mindBean);
		}

		List<Bean> newlist = new ArrayList<Bean>();
		for (Bean bean : list) {
			Bean preNode = map.get(bean.getStr("PRE_NI_ID"));
			Bean mindNode = mindMap.get(bean.getId());
			String mindContent = "";
			if (mindNode != null) {
				mindContent = mindNode.getStr("MIND_CONTENT");
			}
			UserBean doneUserBean = UserMgr.getUser(bean.getStr("TO_USER_ID"));
			UserBean sendUserBean = UserMgr.getUser(bean.getStr("TO_USER_ID"));
			if (bean.isNotEmpty("PRE_NI_ID")) {
				doneUserBean = UserMgr.getUser(bean.getStr("TO_USER_ID"));
				sendUserBean = UserMgr.getUser(preNode.getStr("DONE_USER_ID"));
			}
			Bean resBean = new Bean();
			/**
			 * wfNId 流经实例ID nodeName 流经节点名称 doneUserCode 办理人编码 doneUserName 办理人姓名
			 * doneUserPost 办理人职位 doneDeptCode 办理人部门编码 doneDeptName 办理人部门名称 doneTime 办理时间
			 * sendUserCode 送交人编码 sendUserName 送交人名称 sendUserPost 送交人职位 sendDeptCode 送交人部门编码
			 * sendDeptName 送交人部门名称 sendTime 送交时间 duration 办理时长 mindContent 办理意见
			 */
			resBean.set("wfNId", bean.getStr("NI_ID"));
			resBean.set("nodeName", bean.getStr("NODE_NAME"));
			resBean.set("doneUserCode", doneUserBean.getId());
			resBean.set("doneUserName", doneUserBean.getName());
			resBean.set("doneUserPost", doneUserBean.getPost());
			resBean.set("doneDeptCode", doneUserBean.getDeptCode());
			resBean.set("doneDeptName", doneUserBean.getDeptName());
			resBean.set("doneTime", bean.getStr("NODE_ETIME"));
			resBean.set("sendUserCode", sendUserBean.getId());
			resBean.set("sendUserName", sendUserBean.getName());
			resBean.set("sendUserPost", sendUserBean.getPost());
			resBean.set("sendDeptCode", sendUserBean.getDeptCode());
			resBean.set("sendDeptName", sendUserBean.getDeptName());
			resBean.set("sendTime", bean.getStr("NODE_BTIME"));
			resBean.set("duration", bean.getDouble("NODE_DAYS") > 0 ? bean.getStr("NODE_DAYS") : 1);
			resBean.set("mindContent", mindContent);
			resBean.set("doneType", bean.getStr("DONE_TYPE"));
			newlist.add(resBean);
		}

		Bean dataBean = new Bean();
		dataBean.set("list", newlist);
		dataBean.set("imgURL", Context.getHttpUrl() + "/api/getWfeImg?noAuth=yes&servId=" + servId);
		dataBean.set("aniURL", Context.getHttpUrl() + "/sy/comm/page/SY_WFE_TRACK_FIGURE.showWfeAniForMob.do?PI_ID="
				+ wfInst + "&INST_IF_RUNNING=" + procRunning + "&S_FLAG=" + wfBean.getStr("S_FLAG"));
		Bean resWfe = wfePercent(servId, dataId);

		Bean three = wfeThreePercent(servId, dataId);
		dataBean.set("wfe", resWfe);
		dataBean.set("wfeThree", three);
		outBean.setData(dataBean);
		return outBean;
	}

	@SuppressWarnings({"unchecked" })
	@Override
	public ApiOutBean getWfeTrackForPC(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");

		if (reqData.isEmpty("servId") || reqData.isEmpty("dataId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		Bean wfBean = ServDao.find(servId, dataId);
		String wfInst = wfBean.getStr("S_WF_INST");
		String procRunning = wfBean.getStr("S_WF_STATE");
		String queryTable = "";
		ParamBean paramBean = new ParamBean();
		paramBean.set("PI_ID", wfInst);
		paramBean.set("S_FLAG", wfBean.getInt("S_FLAG"));
		paramBean.set("INST_IF_RUNNING", procRunning);
		if (procRunning.equals("2")) { // 流程已办结
			queryTable = WfNodeInstHisDao.SY_WFE_NODE_INST_HIS_SERV;
		} else { // 流程未办结
			queryTable = WfNodeInstDao.SY_WFE_NODE_INST_SERV;
		}
		paramBean.setSelect("*");
		List<Bean> list = (List<Bean>) ServMgr.act(queryTable, ServMgr.ACT_FINDS, paramBean).getData();

		List<Bean> newlist = this.parseTrackList(list, paramBean);

		Bean dataBean = new Bean();
		dataBean.set("list", newlist);
		dataBean.set("imgURL", Context.getHttpUrl() + "/api/getWfeImg?noAuth=yes&servId=" + servId);
		dataBean.set("aniURL", Context.getHttpUrl() + "/sy/comm/page/SY_WFE_TRACK_FIGURE.showWfeAniForMob.do?PI_ID="
				+ wfInst + "&INST_IF_RUNNING=" + procRunning + "&S_FLAG=" + wfBean.getStr("S_FLAG"));

		Bean chartData = new Bean();

		chartData.set("CompleteData", wfeCompleteChartData(wfBean, list));

		dataBean.set("chartData", chartData);

		outBean.setData(dataBean);
		return outBean;
	}
	
	public Bean wfeTimeChartData(Bean dataBean, Bean instBean) {
		Bean rtnBean = new Bean();

		String sql = "select min(sum(node_days)) time,'minTime' name from SY_WFE_NODE_INST_GW_V where PROC_CODE = '"
				+ instBean.getStr("PROC_CODE")
				+ "' group by pi_id union select sum(node_days)/count(distinct pi_id) time,'avgTime' name  from SY_WFE_NODE_INST_GW_V where PROC_CODE = '"
				+ instBean.getStr("PROC_CODE")
				+ "' union select sum(node_days) time,'totalTime' name from SY_WFE_NODE_INST_GW_V where PI_ID = '"
				+ instBean.getStr("PI_ID") + "'";

		List<Bean> timeList = Transaction.getExecutor().query(sql);
		for (Bean timeBean : timeList) {
			rtnBean.set(timeBean.getStr("NAME"), DataStatsUtil.divFunc((float) timeBean.getDouble("TIME"), 60));
		}

		return rtnBean;
	}

	public Bean wfeCompleteChartData(Bean dataBean, List<Bean> instList) {
		Bean rtnBean = new Bean();
		List<Bean> nodeAll = ServDao.finds("SY_WFE_NODE_DEF",
				"and PROC_CODE = '" + instList.get(0).getStr("PROC_CODE") + "' order by NODE_SORT");

		List<Bean> completeList = new ArrayList<Bean>();
		List<Bean> noCompleteList = new ArrayList<Bean>();
		Bean existTmp = new Bean();

		for (Bean inst : instList) {
			if (!existTmp.containsKey(inst.getStr("NODE_CODE"))) {
				existTmp.put(inst.getStr("NODE_CODE"), inst);
				completeList.add(new Bean().set("nodeName", inst.getStr("NODE_NAME")).set("beginTime",
						inst.getStr("NODE_BTIME")));
			}
		}

		for (Bean node : nodeAll) {
			if (!existTmp.containsKey(node.getStr("NODE_CODE"))) {
				noCompleteList.add(new Bean().set("nodeName", node.getStr("NODE_NAME")));
			}
		}

		float percentData = DataStatsUtil.divFunc(completeList.size() * 100, nodeAll.size());

		Bean doneInfo = GwTimeHelper.getNodeDoneInfo(nodeAll, instList, dataBean.getStr("TMPL_CODE"));

		rtnBean.set("nodeList", doneInfo.getList("NODE_LIST")).set("nodeNameList", doneInfo.getList("nodeNameList"))
				.set("percentData", percentData);
		rtnBean.set("totalTime", doneInfo.getDouble("totalTime"));
		rtnBean.set("currTime", doneInfo.getDouble("currTime"));
		rtnBean.set("currNodeTimeList", doneInfo.getList("currNodeTimeList"));
		rtnBean.set("avgNodeTimeList", doneInfo.getList("avgNodeTimeList"));

		return rtnBean;
	}

	public List<Bean> wfeRankChartData(Bean dataBean, List<Bean> instList) {

		int size = instList.size();

		Bean instUserBean = new Bean();

		for (Bean inst : instList) {
			if (!instUserBean.containsKey(inst.getStr("TO_USER_ID"))) {
				UserBean doneUserBean = UserMgr.getUser(inst.getStr("TO_USER_ID"));
				Bean bean = new Bean();
				bean.set("userName", doneUserBean.getName());
				bean.set("userPost", doneUserBean.getPost());
				bean.set("userImg", doneUserBean.getImgSrc());
				bean.set("deptName", doneUserBean.getDeptName());
				bean.set("useTime", 0L);
				instUserBean.put(inst.getStr("TO_USER_ID"), bean);
			}
		}

		List<Bean> rankList = new ArrayList<Bean>();
		for (int i = 0; i < size; i++) {
			Bean inst = instList.get(i);
			long time = 0;
			if (inst.isEmpty("NODE_ETIME")) {
				WorkTime workTime = new WorkTime();
				time = workTime.calWorktime("", inst.getStr("NODE_BTIME"), DateUtils.getDatetime());
				Bean tmp = instUserBean.getBean(inst.getStr("TO_USER_ID"));
				tmp.set("useTime", tmp.getLong("useTime") + time);
			} else {
				time = inst.getLong("NODE_DAYS");
				Bean tmp = instUserBean.getBean(inst.getStr("TO_USER_ID"));
				tmp.set("useTime", tmp.getLong("useTime") + time);
			}
		}

		Iterator<?> iter = instUserBean.entrySet().iterator();

		while (iter.hasNext()) {
			@SuppressWarnings({ "rawtypes" })
			Map.Entry entry = (Map.Entry) iter.next();
			rankList.add((Bean) entry.getValue());
		}

		Collections.sort(rankList, new Comparator<Bean>() {
			@Override
			public int compare(Bean o1, Bean o2) {
				if (o1.getLong("useTime") > o2.getLong("useTime"))
					return 1;
				else if (o1.getLong("useTime") < o2.getLong("useTime"))
					return -1;
				return 0;
			}
		});

		return rankList;
	}

	/**
	 * 根据servId,dataId 统计流程节点总数 完成度 人员效率
	 * 
	 * @param servId
	 * @param dataId
	 * @return
	 */
	public Bean wfePercent(String servId, String dataId) {
		DecimalFormat dFormat = new DecimalFormat("#.##");
		// 流程节点计数部分
		// 流程节点总数
		List<Bean> nodeAll = ServDao.finds("SY_WFE_NODE_DEF", "and PROC_CODE like '" + servId + "@%'");
		int nodeTotal = nodeAll.size();
		HashMap<String, String> nodeAllMap = new HashMap<String, String>();
		HashMap<String, String> completeMap = new HashMap<String, String>();
		for (Bean bean : nodeAll) {
			String node = bean.getStr("NODE_CODE");
			String nodeName = bean.getStr("NODE_NAME");
			nodeAllMap.put(node, nodeName);
		}

		String piId = ServDao.find(servId, dataId).getStr("S_WF_INST");
		List<Bean> list = ServDao.finds("SY_WFE_TRACK", " and PI_ID = '" + piId + "'");
		if (list.size() == 0) {
			ParamBean param = new ParamBean();
			param.set("PI_ID", piId);
			param.setTable("SY_WFE_NODE_INST_HIS");
			list = ServDao.finds("SY_WFE_TRACK", param);
		}

		List<String> tempList = new ArrayList<String>();
		int finishFlag = 0;
		for (Bean bean : list) {
			String node = bean.getStr("NODE_CODE");
			String nodeName = bean.getStr("NODE_NAME");
			if (!tempList.contains(node)) {
				tempList.add(node);
			}

			int doneType = bean.getInt("DONE_TYPE");
			if (doneType == 16) {
				finishFlag++;
			}

			if (doneType == 1 || doneType == 16) {
				completeMap.put(node, nodeName);
			}
		}
		double res = 0.0;
		if (finishFlag > 0) {
			res = 100.0;
		} else {
			res = (double) tempList.size() * 100 / nodeTotal;
		}
		String wfePercent = dFormat.format(res) + "%";
		Bean resBean = new Bean();
		resBean.set("wfePercent", wfePercent);
		resBean.set("nodeTotal", nodeTotal);
		resBean.set("nodeComplete", tempList.size());
		ArrayList<String> completeList = new ArrayList<String>();
		for (String val : completeMap.values()) {
			completeList.add(val);
		}
		resBean.set("completeList", completeList);

		for (String key : completeMap.keySet()) {
			nodeAllMap.remove(key);
		}
		ArrayList<String> noCompleteList = new ArrayList<String>();
		for (String val : nodeAllMap.values()) {
			noCompleteList.add(val);
		}
		resBean.set("noCompleteList", noCompleteList);

		// 流程人员效率统计部分

		List<Bean> list_inst = ServDao.finds("SY_WFE_NODE_INST", new ParamBean().set("PI_ID", piId));
		List<Bean> list_inst_his = ServDao.finds("SY_WFE_NODE_INST_HIS", new ParamBean().set("PI_ID", piId));
		ArrayList<Bean> arrayList = new ArrayList<Bean>();
		for (Bean bean : list_inst) {
			Bean tmpBean = new Bean();
			String userName = bean.getStr("DONE_USER_NAME");
			String userId = bean.getStr("DONE_USER_ID");
			if (userName.equals("")) {
				userName = bean.getStr("TO_USER_NAME");
				userId = bean.getStr("TO_USER_ID");
				String endTime = DateUtils.getDatetime();
				long nodeMinute = 0;
				WorkTime workTime = new WorkTime();
				nodeMinute = workTime.calWorktime("", bean.getStr("NODE_BTIME"), endTime);
				tmpBean.set("spendMin", nodeMinute > 0 ? nodeMinute : 1);
			} else {
				// String hour = dFormat.format(bean.getDouble("NODE_DAYS") /
				// 60);
				tmpBean.set("spendMin", bean.getDouble("NODE_DAYS") > 0 ? bean.getDouble("NODE_DAYS") : 1);
			}
			tmpBean.set("userName", userName);
			tmpBean.set("userId", userId);
			arrayList.add(tmpBean);
		}
		for (Bean bean : list_inst_his) {
			String userName = bean.getStr("DONE_USER_NAME");
			String userId = bean.getStr("DONE_USER_ID");
			if (userId.equals(""))
				continue;
			// String hour = dFormat.format(bean.getDouble("NODE_DAYS") / 60);
			Bean tmpBean = new Bean();
			tmpBean.set("userName", userName);
			tmpBean.set("spendMin", bean.getDouble("NODE_DAYS") > 0 ? bean.getDouble("NODE_DAYS") : 1);
			tmpBean.set("userId", userId);
			arrayList.add(tmpBean);
		}
		Bean dueBean = sortUserSpendTime(arrayList);
		resBean.set("pie", dueBean);
		return resBean;
	}

	private Bean sortUserSpendTime(ArrayList<Bean> list) {
		Bean resbean = new Bean();
		HashMap<String, Double> detail = new HashMap<String, Double>();
		for (Bean bean2 : list) {
			if (detail.containsKey(bean2.getStr("userId"))) {
				double value = detail.get(bean2.getStr("userId")) + bean2.getDouble("spendMin");
				detail.put(bean2.getStr("userId"), value);
			} else {
				detail.put(bean2.getStr("userId"), bean2.getDouble("spendMin"));
			}
		}
		Set<Entry<String, Double>> ks_detail = detail.entrySet();
		List<Entry<String, Double>> sortlist_detail = new ArrayList<Map.Entry<String, Double>>(ks_detail);
		Collections.sort(sortlist_detail, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if (o1.getValue() < o2.getValue())
					return 1;
				else if (o1.getValue() > o2.getValue())
					return -1;
				return 0;
			}
		});

		ArrayList<Bean> detaiList = new ArrayList<Bean>();
		for (Entry<String, Double> entry : sortlist_detail) {
			Bean tmpBean = new Bean();
			String tmpId = entry.getKey();
			Double tmpTime = entry.getValue();
			UserBean userBean = UserMgr.getUser(tmpId);
			tmpBean.set("userId", tmpId);
			tmpBean.set("userName", userBean.getName());
			tmpBean.set("deptName", userBean.getDeptName());
			tmpBean.set("userPost", userBean.getPost());
			tmpBean.set("spendMin", (new Double(tmpTime)).intValue());
			detaiList.add(tmpBean);
		}
		resbean.set("detail", detaiList);

		HashMap<String, Double> map = new HashMap<String, Double>();
		for (Bean bean2 : list) {
			if (map.containsKey(bean2.getStr("userName"))) {
				double value = map.get(bean2.getStr("userName")) + bean2.getDouble("spendMin");
				map.put(bean2.getStr("userName"), value);
			} else {
				map.put(bean2.getStr("userName"), bean2.getDouble("spendMin"));
			}
		}

		Set<Entry<String, Double>> ks = map.entrySet();
		List<Entry<String, Double>> sortlist = new ArrayList<Map.Entry<String, Double>>(ks);
		// 降序
		Collections.sort(sortlist, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if (o1.getValue() < o2.getValue())
					return 1;
				else if (o1.getValue() > o2.getValue())
					return -1;
				return 0;
			}
		});

		if (sortlist.size() > 5) {
			Double otherSpend = 0.0;
			for (int i = 5; i < sortlist.size(); i++) {
				otherSpend += sortlist.get(i).getValue();
			}
			// 前5
			Double totalFive = 0.0;
			for (int i = 0; i < 5; i++) {
				Bean bean = new Bean();
				bean.set("userName", sortlist.get(i).getKey());
				bean.set("spendMin", (new Double(sortlist.get(i).getValue())).intValue());
				resbean.set(i + 1, bean);
				totalFive += sortlist.get(i).getValue();
			}
			// 其他
			Bean bean = new Bean();
			bean.set("userName", "其他");
			bean.set("spendMin", (new Double(otherSpend)).intValue());
			resbean.set(6, bean);
			resbean.set("totalFive", (new Double(totalFive)).intValue());
			resbean.set("total", (new Double(totalFive + otherSpend)).intValue());
			resbean.set("peopleCount", 6);
		} else {
			Double totalFive = 0.0;
			for (int i = 0; i < sortlist.size(); i++) {
				Bean bean = new Bean();
				bean.set("userName", sortlist.get(i).getKey());
				bean.set("spendMin", (new Double(sortlist.get(i).getValue())).intValue());
				resbean.set(i + 1, bean);
				totalFive += sortlist.get(i).getValue();
			}
			resbean.set("totalFive", (new Double(totalFive)).intValue());
			resbean.set("total", (new Double(totalFive)).intValue());
			resbean.set("peopleCount", sortlist.size());
		}

		ArrayList<Bean> allList = new ArrayList<Bean>();
		for (Entry<String, Double> entry : sortlist) {
			Bean tmpBean = new Bean();
			String tmpName = entry.getKey();
			Double tmpTime = entry.getValue();
			tmpBean.set("userName", tmpName);
			tmpBean.set("spendMin", (new Double(tmpTime)).intValue());
			allList.add(tmpBean);
		}
		resbean.set("allList", allList);
		return resbean;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean finish(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");
		String niId = reqData.getStr("niId");
		// if (reqData.isNotEmpty("niId")) {
		// return outBean;
		// }
		// Bean servBean = ServDao.find(servId, dataId);
		Bean servBean = ServMgr.act(servId, ServMgr.ACT_BYID, new Bean().setId(dataId));
		String pid = servBean.getStr("S_WF_INST");
		String procRunning = servBean.getStr("S_WF_STATE");
		int procInstIsRunning = this.procInstIsRunning(procRunning);
		ProcServ procServ = new ProcServ();
		ParamBean paramBean = new ParamBean();
		paramBean.set("NI_ID", niId);
		paramBean.set("PI_ID", pid);
		paramBean.set("INST_IF_RUNNING", procInstIsRunning);
		paramBean.set("serv", "SY_WFE_PROC");
		paramBean.set("act", "finish");
		paramBean.set("_TRANS_", false);
		procServ.finish(paramBean);

		GwTimeHelper.addNodeTime(niId, servBean);

		WfContext.cleanThreadData();
		return outBean;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean undoFinish(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");
		String niId = reqData.getStr("niId");
		if (reqData.isNotEmpty("niId")) {

		}
		// Bean servBean = ServDao.find(servId, dataId);
		Bean servBean = ServMgr.act(servId, ServMgr.ACT_BYID, new Bean().setId(dataId));
		String pid = servBean.getStr("S_WF_INST");
		String procRunning = servBean.getStr("S_WF_STATE");
		int procInstIsRunning = this.procInstIsRunning(procRunning);

		ProcServ procServ = new ProcServ();
		ParamBean paramBean = new ParamBean();
		paramBean.set("NI_ID", niId);
		paramBean.set("PI_ID", pid);
		paramBean.set("INST_IF_RUNNING", procInstIsRunning);
		paramBean.set("serv", "SY_WFE_PROC");
		paramBean.set("act", "undoFinish");
		paramBean.set("_TRANS_", false);
		procServ.undoFinish(paramBean);

		GwTimeHelper.subNodeTime(niId, servBean);

		WfContext.cleanThreadData();
		return outBean;
	}

	/**
	 * @param procRunning 从页面传来的 WF_INST_ID 字符串类型的 流程是否运行
	 * @return 流程是否运行
	 */
	private int procInstIsRunning(String procRunning) {
		int procIsRunning = 1;

		if (procRunning.equals(String.valueOf(WfeConstant.WFE_PROC_INST_NOT_RUNNING))) {
			procIsRunning = 2;
		}
		return procIsRunning;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean withdraw(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");
		String niId = reqData.getStr("niId");
		// Bean servBean = ServDao.find(servId, dataId);
		Bean servBean = ServMgr.act(servId, ServMgr.ACT_BYID, new Bean().setId(dataId));
		String pid = servBean.getStr("S_WF_INST");
		String procRunning = servBean.getStr("S_WF_STATE");
		int procInstIsRunning = this.procInstIsRunning(procRunning);

		WfAct wfAct = new WfAct(niId, true);
		// String userCode = wfAct.getNodeInstBean().getStr("TO_USER_ID");
		// String userName = wfAct.getNodeInstBean().getStr("TO_USER_NAME");
		// Map<String, String> actMap = new HashMap<String, String>();
		// actMap.put("userCode", userCode);
		// actMap.put("userName", userName);
		//
		List<WfAct> nextList = wfAct.getNextWfAct();

		List<Map<String, String>> actList = new ArrayList<Map<String, String>>();
		for (WfAct act : nextList) {
			Map<String, String> actMap = new HashMap<String, String>();
			String userCode = act.getNodeInstBean().getStr("TO_USER_ID");
			String userName = act.getNodeInstBean().getStr("TO_USER_NAME");
			UserBean userBean = UserMgr.getUser(userCode);
			actMap.put("USER_CODE", userCode);
			actMap.put("USER_NAME", userName);
			actMap.put("USER_POST", userBean.getPost());
			actMap.put("DEPT_NAME", userBean.getDeptName());
			actMap.put("DEPT_CODE", userBean.getDeptCode());
			actList.add(actMap);
		}

		ProcServ procServ = new ProcServ();
		ParamBean paramBean = new ParamBean();
		paramBean.set("NI_ID", niId);
		paramBean.set("PI_ID", pid);
		paramBean.set("INST_IF_RUNNING", procInstIsRunning);
		paramBean.set("serv", "SY_WFE_PROC");
		paramBean.set("act", "withdraw");
		paramBean.set("_TRANS_", false);
		procServ.withdraw(paramBean);

		Bean dataBean = new Bean();
		// dataBean.set("rtnstr", rtnBean.getStr("rtnstr"));
		dataBean.set("backUser", actList);
		outBean.setData(dataBean);

		WfContext.cleanThreadData();
		return outBean;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean stopParallelWf(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");
		String niId = reqData.getStr("niId");
		// Bean servBean = ServDao.find(servId, dataId);
		Bean servBean = ServMgr.act(servId, ServMgr.ACT_BYID, new Bean().setId(dataId));
		String pid = servBean.getStr("S_WF_INST");
		String procRunning = servBean.getStr("S_WF_STATE");
		int procInstIsRunning = this.procInstIsRunning(procRunning);
		ProcServ procServ = new ProcServ();
		ParamBean paramBean = new ParamBean();
		paramBean.set("NI_ID", niId);
		paramBean.set("PI_ID", pid);
		paramBean.set("SERV_ID", servId);
		paramBean.set("INST_IF_RUNNING", procInstIsRunning);
		paramBean.set("serv", "SY_WFE_PROC");
		paramBean.set("act", "withdraw");
		paramBean.set("_TRANS_", false);
		procServ.stopParallelWf(paramBean);

		WfContext.cleanThreadData();
		return outBean;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ApiOutBean deleteDoc(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");
		String niId = reqData.getStr("niId");
		// Bean servBean = ServDao.find(servId, dataId);
		Bean servBean = ServMgr.act(servId, ServMgr.ACT_BYID, new Bean().setId(dataId));
		String pid = servBean.getStr("S_WF_INST");
		String procRunning = servBean.getStr("S_WF_STATE");
		int procInstIsRunning = this.procInstIsRunning(procRunning);
		ProcServ procServ = new ProcServ();
		ParamBean paramBean = new ParamBean();
		paramBean.set("NI_ID", niId);
		paramBean.set("PI_ID", pid);
		paramBean.set("SERV_ID", servId);
		paramBean.set("INST_IF_RUNNING", procInstIsRunning);
		paramBean.set("serv", "SY_WFE_PROC");
		paramBean.set("act", "withdraw");
		paramBean.set("_TRANS_", false);
		procServ.deleteDoc(paramBean);
		return outBean;
	}

	/**
	 * 下一步按钮及绑定人员
	 */

	public ApiOutBean getWfeBtn(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		Bean wfeBean = this.getNextStepBtn(reqData);

		List<Bean> nextList = wfeBean.getList("wfeBtn");

		Bean node = wfeBean.getBean("nodeInstBean");

		List<Bean> list = new ArrayList<Bean>();

		for (int i = 0; i < nextList.size(); i++) {
			Bean btn = nextList.get(i);
			Bean resBean = new Bean();
			resBean.set("btnName", btn.getStr("NODE_NAME"));
			resBean.set("btnCode", btn.getStr("NODE_CODE"));
			resBean.set("toType", node.getStr("TO_TYPE"));
			resBean.set("btnData", btn);

			try {
				ParamBean paramBean = new ParamBean();
				paramBean.put("NODE_CODE", btn.getStr("NODE_CODE"));
				paramBean.put("PI_ID", node.getStr("PI_ID"));
				paramBean.put("NI_ID", node.getStr("NI_ID"));
				paramBean.put("INST_IF_RUNNING", node.getStr("NODE_IF_RUNNING"));

				if (StringUtils.isBlank(btn.getStr("NODE_USER"))) {
					OutBean rtnBean = getNextStepUsers(paramBean);
					resBean.set("toUsers", rtnBean.getList("USERS"));
				} else {
					String userCode = btn.getStr("NODE_USER");
					UserBean userBean = UserMgr.getUser(userCode);
					List<Map<String, String>> userList = new ArrayList<Map<String, String>>();
					Map<String, String> map = new HashMap<String, String>();
					map.put("userCode", userBean.getCode());
					map.put("userName", userBean.getName());
					map.put("deptCode", userBean.getDeptCode());
					map.put("deptName", userBean.getDeptName());
					map.put("userPost", userBean.getPost());
					userList.add(map);
					resBean.set("toUsers", userList);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			list.add(resBean);
		}

		Bean dataBean = new Bean();
		dataBean.set("list", list);
		dataBean.set("pid", node.getStr("PI_ID"));
		dataBean.set("nid", node.getStr("NI_ID"));
		outBean.setData(dataBean);

		WfContext.cleanThreadData();
		return outBean;
	}

	public ApiOutBean getWfeBinderByNode(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		String nodeCode = reqData.getStr("NODE_CODE");
		String pid = reqData.getStr("PI_ID");
		String nid = reqData.getStr("NI_ID");
		String ifRunning = reqData.getStr("NODE_IF_RUNNING");
		Bean btn = reqData.getBean("btnData");
		Bean resBean = new Bean();
		try {
			ParamBean paramBean = new ParamBean();
			paramBean.put("NODE_CODE", nodeCode);
			paramBean.put("PI_ID", pid);
			paramBean.put("NI_ID", nid);
			paramBean.put("INST_IF_RUNNING", ifRunning);

			if (StringUtils.isBlank(btn.getStr("NODE_USER"))) {
				OutBean rtnBean = getNextStepUsers(paramBean);
				resBean.set("toUserTree", rtnBean.getList("USERTREE"));
				resBean.set("multiSelect", rtnBean.getBoolean("multiSelect"));
			} else {
				String userCode = btn.getStr("NODE_USER");
				UserBean userBean = UserMgr.getUser(userCode);
				List<Bean> userTreeList = new ArrayList<Bean>();
				StringBuffer sb = new StringBuffer();
				sb.append("{'CHILD':[").append("{'ID':'usr:").append(userCode).append("','LEAF':'1','NAME':'")
						.append(userBean.getName()).append("','NODETYPE':'usr','PID':'dept:")
						.append(userBean.getDeptCode()).append("','SORT':'").append(userBean.getSort())
						.append("','USER_IMG':'").append(userBean.getStr("USER_IMG_SRC")).append("'}").append("]}");
				userTreeList.add(JsonUtils.toBean(sb.toString()));
				resBean.set("toUserTree", userTreeList);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			WfContext.cleanThreadData();
		}

		outBean.setData(resBean);

		return outBean;
	}

	/**
	 * 流程送交
	 * 
	 * @param reqData 参数:pid;nid;agentUserId;nodeCode;toType;toUser
	 * @return
	 */
	public ApiOutBean wfeSend(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		if (reqData.isEmpty("pid") || reqData.isEmpty("nid") || reqData.isEmpty("nodeCode")
				|| reqData.isEmpty("toType")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		ParamBean paramBean = new ParamBean();
		paramBean.put("PI_ID", reqData.getStr("pid"));
		paramBean.put("NI_ID", reqData.getStr("nid"));
		paramBean.put("INST_IF_RUNNING", 1);
		paramBean.put("NODE_CODE", reqData.getStr("nodeCode"));
		paramBean.put("TO_TYPE", reqData.getInt("toType"));

		paramBean.put("TO_USERS", reqData.getStr("toUser"));
		paramBean.put("TO_DEPT", reqData.getStr("toDept"));
		paramBean.put("TO_ROLE", reqData.getStr("toRole"));
		paramBean.put("NODE_LIMIT_TIME", reqData.getStr("limitTime"));
		paramBean.put("LINE_CODE", reqData.getStr("lineCode"));

		if (!reqData.getStr("agentUserId").isEmpty()) {
			paramBean.put(OrgConstant.AGENT_USER, reqData.getStr("agentUser"));
		}

		List<Map<String, String>> sendUserlist = getWfSendUser(paramBean);

		ProcServ procServ = new ProcServ();
		OutBean rtnBean = procServ.toNext(paramBean);

		if (!rtnBean.getMsg().equals(Constant.RTN_MSG_OK)) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_002.getValue());
		}

		Bean data = new Bean();
		data.put("sendUser", sendUserlist);
		outBean.setData(data);

		WfContext.cleanThreadData();

		return outBean;
	}

	/**
	 * 获取办理用户
	 * 
	 * @param paramBean
	 * @return
	 */
	private List<Map<String, String>> getWfSendUser(ParamBean paramBean) {

		WfProcess process = new WfProcess(paramBean.getStr("PI_ID"), paramBean.getBoolean("INST_IF_RUNNING"));
		WfParam param = new WfParam();
		param.copyFrom(paramBean);
		param.setTypeTo(paramBean.getInt("TO_TYPE"));
		param.setDoneUser(getDoUserBean(paramBean));
		param.setToUser(paramBean.getStr("TO_USERS"));
		param.setToDept(paramBean.getStr("TO_DEPT"));
		param.setToRole(paramBean.getStr("TO_ROLE"));

		List<GroupBean> list = process.getNextActors(param);

		List<Map<String, String>> userList = new ArrayList<Map<String, String>>();
		for (GroupBean group : list) {
			Iterator<String> iter = group.getUserIds().iterator();
			while (iter.hasNext()) {
				Map<String, String> map = new HashMap<String, String>();
				UserBean user = UserMgr.getUser(iter.next());
				map.put("USER_CODE", user.getId());
				map.put("USER_NAME", user.getName());
				map.put("DEPT_NAME", user.getDeptName());
				map.put("DEPT_CODE", user.getDeptCode());
				map.put("USER_POST", user.getPost());
				userList.add(map);
			}
		}
		return userList;
	}

	/**
	 * 独占
	 * 
	 * @param reqData 参数:pid;nid;agentUserId
	 * @return
	 */
	public ApiOutBean duZhan(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		if (reqData.isEmpty("pid") || reqData.isEmpty("nid")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		ParamBean paramBean = new ParamBean();
		paramBean.put("PI_ID", reqData.getStr("pid"));
		paramBean.put("NI_ID", reqData.getStr("nid"));

		if (!reqData.getStr("agentUserId").isEmpty()) {
			paramBean.put(OrgConstant.AGENT_USER, reqData.getStr("agentUser"));
		}

		// ServMgr.act(ServMgr.SY_WFE_PROC, WfBtnConstant.BUTTON_DUZHAN,
		// paramBean);

		ProcServ procServ = new ProcServ();
		procServ.duZhan(paramBean);

		return outBean;
	}

	/**
	 * 阅件签收，更新分发的记录(分发的ID, ) , 取消待办
	 * 
	 * @param reqData 参数:sendId;agentUserId
	 * @return 返回前台Bean
	 */
	public ApiOutBean qianShou(Bean reqData) {

		ApiOutBean outBean = new ApiOutBean();
		if (reqData.isEmpty("sendId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		ParamBean paramBean = new ParamBean();
		paramBean.put("SEND_ID", reqData.getStr("sendId"));

		if (!reqData.getStr("agentUserId").isEmpty()) {
			paramBean.put(OrgConstant.AGENT_USER, reqData.getStr("agentUser"));
		}

		// ServMgr.act(ServMgr.SY_COMM_SEND_SHOW_CARD,
		// WfBtnConstant.BUTTON_QIANSHOU, paramBean);

		SendUserServ sendServ = new SendUserServ();
		sendServ.cmQianShou(paramBean);

		return outBean;
	}

	public ApiOutBean qianShou2Shouwen(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();

		if (reqData.isEmpty("sendId") || reqData.isEmpty("dataId") || reqData.isEmpty("servId")
				|| reqData.isEmpty("tarServId")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		Bean dataBean = ServMgr.act(reqData.getStr("servId"), ServMgr.ACT_BYID,
				new ParamBean().setId(reqData.getStr("dataId")));

		Bean newBean = new Bean();

		newBean.set("GW_TITLE", dataBean.getStr("GW_TITLE"));
		newBean.set("GW_YEAR_CODE", dataBean.getStr("GW_YEAR_CODE"));
		newBean.set("GW_YEAR", dataBean.getStr("GW_YEAR"));
		newBean.set("GW_YEAR_NUMBER", dataBean.getStr("GW_YEAR_NUMBER"));
		newBean.set("GW_SRCRET", dataBean.getStr("GW_SRCRET"));
		newBean.set("S_EMERGENCY", dataBean.getStr("S_EMERGENCY"));
		newBean.set("GW_FILE_TYPE", dataBean.getStr("GW_FILE_TYPE"));
		newBean.set("GW_SECRET_PERIOD", dataBean.getStr("GW_SECRET_PERIOD"));
		newBean.set("GW_COPIES", 1);
		newBean.set("ISDRAFT", 3);
		newBean.set("GW_CODE", dataBean.getStr("GW_YEAR_CODE") + "[" + dataBean.getStr("GW_YEAR") + "]"
				+ dataBean.getStr("GW_YEAR_NUMBER"));
		DeptBean deptBean = OrgMgr.getDept(dataBean.getStr("S_ODEPT"));
		newBean.set("GW_SW_CNAME", deptBean.getFullName());
		newBean.set("GW_BEGIN_TIME", DateUtils.getDate());
		ServDefBean serv = ServUtils.getServDef(reqData.getStr("tarServId"));
		newBean.set("TMPL_CODE", reqData.getStr("tarServId"));
		newBean.set("TMPL_TYPE_CODE", serv.getPId());
		OutBean saveBean = ServMgr.act(reqData.getStr("tarServId"), ServMgr.ACT_SAVE, new ParamBean(newBean));

		List<Bean> fileList = ServDao.finds("SY_COMM_FILE", " and DATA_ID = '" + reqData.getStr("dataId") + "'");
		for (Bean file : fileList) {
			if (file.getStr("ITEM_CODE").equals("WENGAO")) {
				continue;
			}
			Bean tarBean = new Bean();
			tarBean.set("DATA_ID", saveBean.getId());
			tarBean.set("SERV_ID", reqData.getStr("tarServId"));
			FileMgr.copyFile(file, tarBean);
		}

		this.qianShou(reqData);
		outBean.setData(saveBean);
		return outBean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ApiOutBean getWfeImg(String servId) {
		ApiOutBean outBean = new ApiOutBean();
		if (servId == null || servId.equals("")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}
		// 分局服务ID查找到绑定的对应流程
		ParamBean paramBean = new ParamBean();
		paramBean.set("SERV_ID", servId);
		paramBean.set("S_FLAG", 1);
		paramBean.set("_SELECT_", "EN_NAME");
		// List<Bean> list = ServDao.finds("SY_WFE_PROC_DEF", paramBean);
		List<Bean> list = (List<Bean>) ServMgr.act("SY_WFE_PROC_DEF", ServMgr.ACT_FINDS, paramBean).getData();
		if (list.get(0).isEmpty("EN_NAME")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}
		String enName = list.get(0).getStr("EN_NAME");
		// 根据流程名称 到WEB-INF/DOC／SY_WF_IMG 找对应的图片
		String path = FileMgr.getAbsolutePath("@" + Context.APP.WEBINF_DOC + "@") + "/SY_WF_IMG/" + enName + ".png";
		File file = new File(path);
		if (!file.exists()) {
			throw new TipException("文件不存在");
		}
		outBean.set("imgPath", path);
		return outBean;
	}

	@Override
	public ApiOutBean saveWfeImg(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		if (reqData.isEmpty("servId") || reqData.isEmpty("imageStr")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}
		String servId = reqData.getStr("servId");
		String imageStr = reqData.getStr("imageStr");
		imageStr = imageStr.replaceAll(" ", "+");
		imageStr = imageStr.replace("data:image/png;base64,", "");
		String path = FileMgr.getAbsolutePath("@" + Context.APP.WEBINF_DOC + "@") + "/SY_WF_IMG/";
		File file = new File(path);
		if (!file.exists() && !file.isDirectory()) {
			file.mkdir();
		}
		Boolean res = GenerateImage(imageStr, path + servId + ".png");
		if (!res) {
			throw new TipException("创建文件失败");
		}
		// 改变图片大小
		// try {
		// // 未缩小图片尺寸 1914*1200
		// changeImgSize(path + servId + ".png", path + servId + ".png", 957,
		// 600);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		return outBean;
	}

	/**
	 * 对字节数组字符串进行Base64解码并生成图片
	 * 
	 * @param imgStr
	 * @param imgFilePath
	 * @return
	 */
	public static boolean GenerateImage(String imgStr, String imgFilePath) {
		if (imgStr == null) // 图像数据为空
			return false;
		try {
			// Base64解码
			byte[] bytes = Base64.decodeFast(imgStr);
			for (int i = 0; i < bytes.length; ++i) {
				if (bytes[i] < 0) {// 调整异常数据
					bytes[i] += 256;
				}
			}
			// 生成图片
			File file = new File(imgFilePath);
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream out = new FileOutputStream(file);
			out.write(bytes);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 图片转byte数组
	 * 
	 * @param path 图片路径
	 * @return
	 */
	public byte[] image2byte(String path) {
		byte[] data = null;
		FileImageInputStream input = null;
		try {
			input = new FileImageInputStream(new File(path));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int numBytesRead = 0;
			while ((numBytesRead = input.read(buf)) != -1) {
				output.write(buf, 0, numBytesRead);
			}
			data = output.toByteArray();
			output.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * 压缩图片
	 * 
	 * @param srcImgPath  图片路径
	 * @param distImgPath 输出图片路径
	 * @param width
	 * @param height
	 * @throws IOException
	 */
	public void changeImgSize(String srcImgPath, String distImgPath, int width, int height) throws IOException {
		String subfix = "png";
		subfix = srcImgPath.substring(srcImgPath.lastIndexOf(".") + 1, srcImgPath.length());
		File srcFile = new File(srcImgPath);
		Image srcImg = ImageIO.read(srcFile);
		BufferedImage buffImg = null;
		if (subfix.equals("png")) {
			buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		} else {
			buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}

		Graphics2D graphics = buffImg.createGraphics();
		graphics.setBackground(Color.WHITE);
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		graphics.drawImage(srcImg.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
		ImageIO.write(buffImg, subfix, new File(distImgPath));
	}

	/**
	 * 获取实际的办理用户
	 * 
	 * @param paramBean 参数信息
	 * @return 取得流程的办理用户 ， 如果参数中包含_AGENT_USER_，
	 *         则返回这个用户的UserBean表示当前用户代替该用户办理,否则取当前用户UserBean
	 */
	private UserBean getDoUserBean(ParamBean paramBean) {
		// 获取 委托人 的 UserBean
		if (!paramBean.isEmpty(OrgConstant.AGENT_USER)) {
			String realUserId = paramBean.getStr(OrgConstant.AGENT_USER);
			UserBean doUserBean = UserMgr.getUser(realUserId);
			return doUserBean;
		}

		return Context.getUserBean();
	}

	/**
	 * 节点绑定资源
	 * 
	 * @param paramBean 参数Bean
	 * @return 返回页面的树结构 串
	 */
	private OutBean getNextStepUsers(ParamBean paramBean) {
		try {
			WfAct currWfAct = new WfAct(paramBean.getStr("NI_ID"), true);

			WfProcDef procDef = currWfAct.getProcess().getProcDef();
			String nextNodeCode = paramBean.getStr("NODE_CODE");
			WfNodeDef nextNodeDef = procDef.findNode(nextNodeCode);

			UserBean doUser = getDoUserBean(paramBean);
			WfBinderManager wfBinderManager = new WfBinderManager(nextNodeDef, currWfAct, doUser);

			WfLineDef lineBean = procDef.findLineDef(currWfAct.getCode(), nextNodeDef.getStr("NODE_CODE"));

			if (lineBean.isEnableOrgDef()) { // 如果启动了线组织资源定义，则使用线组织资源定义
				wfBinderManager.initBinderResource(lineBean.getOrgDefBean());
			} else { // 使用节点组织资源定义
				wfBinderManager.initBinderResource(nextNodeDef);
			}

			WfeBinder wfBinder = wfBinderManager.getWfeBinder();

			List<Map<Object, Object>> userList = new ArrayList<Map<Object, Object>>();

			// 优先取按组过滤任务处理人
			if (wfBinder.getGroupBeanList().size() > 0) {
				OutBean outBean = new OutBean();

				for (GroupBean groupBean : wfBinder.getGroupBeanList()) {

					List<Map<Object, Object>> userGroup = new ArrayList<Map<Object, Object>>();

					Iterator<String> iter = groupBean.getUserIds().iterator();

					while (iter.hasNext()) {
						UserBean user = UserMgr.getUser(iter.next());
						Map<Object, Object> map = new HashMap<Object, Object>();
						map.put("userCode", user.getCode());
						map.put("userName", user.getName());
						map.put("userPost", user.getPost());
						map.put("deptCode", user.getDeptCode());
						map.put("deptName", user.getDeptName());
						map.put("multiSelect", wfBinder.isMutilSelect());
						map.put("roleCode", wfBinder.getRoleCode());
						map.put("autoSelect", wfBinder.isAutoSelect());
						userGroup.add(map);
					}

					userList.addAll(userGroup);
				}
				outBean.put("USERS", userList);
				return outBean.setOk();

			} else {
				String rtnTreeStr = wfBinder.getBinders();

				List<Bean> list1 = JsonUtils.toBeanList(rtnTreeStr);

				iterationChild(list1, null, wfBinder, userList);

				OutBean rtnBean = new OutBean();

				rtnBean.put("USERS", userList);

				rtnBean.put("USERTREE", list1);
				rtnBean.put("multiSelect", wfBinder.isMutilSelect());
				return rtnBean;
			}
		} finally {
			WfContext.cleanThreadData();
		}
	}

	private void iterationChild(List<Bean> list, DeptBean dept, WfeBinder wfBinder,
			List<Map<Object, Object>> userList) {
		for (Bean bean : list) {

			int leaf = bean.getInt("LEAF");
			List<Bean> list1 = bean.getList("CHILD");
			if (leaf == 1) {
				try {
					Map<Object, Object> map = new HashMap<Object, Object>();
					String userCode = bean.getStr("ID");
					if (userCode.indexOf(":") >= 0) {
						userCode = userCode.split(":")[1];
					}
					UserBean user = UserMgr.getUser(userCode);
					map.put("userCode", user.getCode());
					map.put("userName", user.getName());
					map.put("deptCode", dept.getCode());
					map.put("deptName", dept.getName());
					map.put("userPost", user.getPost());
					map.put("multiSelect", wfBinder.isMutilSelect());
					map.put("roleCode", wfBinder.getRoleCode());
					map.put("autoSelect", wfBinder.isAutoSelect());
					userList.add(map);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				DeptBean dept1 = null;
				String deptCode = bean.getStr("ID");

				if (deptCode.indexOf(":") >= 0) {
					deptCode = deptCode.split(":")[1];
					dept1 = OrgMgr.getDept(deptCode);
				}
				iterationChild(list1, dept1, wfBinder, userList);
			}
		}

	}

	/**
	 * 获取下一步按钮和当前节点信息
	 * 
	 * @param reqData
	 * @return
	 */
	private Bean getNextStepBtn(Bean reqData) {
		Bean outBean = new Bean();

		try {
			String servId = reqData.getStr("servId");
			String dataId = reqData.getStr("dataId");

			if (reqData.isEmpty("servId") || reqData.isEmpty("dataId")) {
				throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
			}

			ParamBean paramBean = new ParamBean();
			paramBean.setId(dataId);
			Bean servBean = ServMgr.act(servId, ServMgr.ACT_BYID, paramBean);
			if (servBean.isNotEmpty("S_WF_INST")) { // 此数据在工作流中

				UserBean doUserBean = getDoUserBean(new ParamBean(reqData));

				String procRunning = servBean.getStr("S_WF_STATE");

				int procInstIsRunning = this.procInstIsRunning(procRunning);

				WfProcess process = new WfProcess(servBean.getStr("S_WF_INST"), procInstIsRunning);

				WfAct wfAct = process.getUserLastToDoWfAct(doUserBean);

				if (wfAct != null) {

					outBean.set("nodeInstBean", wfAct.getNodeInstBean());

					outBean.set("wfeBtn", wfAct.getNextAvailableSteps(doUserBean));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			WfContext.cleanThreadData();
		}

		return outBean;
	}

	@Override
	public ApiOutBean getUserBeanByUserCode(String userCode) {
		ApiOutBean outBean = new ApiOutBean();
		if (userCode.equals(""))
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		UserBean userBean = UserMgr.getUser(userCode);
		if (!userBean.isEmpty()) {
			outBean.setData(userBean);
		}
		return outBean;
	}

	@Override
	public ApiOutBean getWfePercent(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = reqData.getStr("servId");
		String dataId = reqData.getStr("dataId");
		// 确定服务绑定的流程
		String procCode = ServDao.finds("SY_WFE_PROC_DEF", new ParamBean().set("S_FLAG", 1).set("SERV_ID", servId))
				.get(0).getId();
		// 流程节点总数

		List<Bean> nodeAll = ServDao.finds("SY_WFE_NODE_DEF", "and PROC_CODE = '" + procCode + "'");
		int nodeTotal = nodeAll.size();
		HashMap<String, String> nodeAllMap = new HashMap<String, String>();
		for (Bean bean : nodeAll) {
			String node = bean.getStr("NODE_CODE");
			String nodeName = bean.getStr("NODE_NAME");
			nodeAllMap.put(node, nodeName);
		}

		String piId = ServDao.find(servId, dataId).getStr("S_WF_INST");
		List<Bean> list = ServDao.finds("SY_WFE_TRACK", " and PI_ID = '" + piId + "'");
		List<String> tempList = new ArrayList<String>();

		if (list.size() == 0) {
			ParamBean param = new ParamBean();
			param.set("PI_ID", piId);
			param.setTable("SY_WFE_NODE_INST_HIS");
			list = ServDao.finds("SY_WFE_TRACK", param);
		}

		HashMap<String, String> completeMap = new HashMap<String, String>();

		int finishFlag = 0;
		for (Bean bean : list) {
			String node = bean.getStr("NODE_CODE");
			String nodeName = bean.getStr("NODE_NAME");
			if (!tempList.contains(node)) {
				tempList.add(node);
			}

			int doneType = bean.getInt("DONE_TYPE");
			if (doneType == 16) {
				finishFlag++;
			}

			if (doneType == 1 || doneType == 16) {
				completeMap.put(node, nodeName);
			}

		}

		DecimalFormat df = new DecimalFormat(".##");
		double res = 0.0;
		if (finishFlag > 0) {
			res = 100.0;
		} else {
			res = (double) tempList.size() * 100 / nodeTotal;
		}
		String wfePercent = df.format(res) + "%";
		Bean resBean = new Bean();
		resBean.set("wfePercent", wfePercent);
		resBean.set("nodeTotal", nodeTotal);
		resBean.set("nodeComplete", tempList.size());
		resBean.set("completeList", completeMap);

		for (String key : completeMap.keySet()) {
			nodeAllMap.remove(key);
		}
		HashMap<String, String> noCompleteMap = nodeAllMap;

		resBean.set("noCompleteList", noCompleteMap);
		outBean.setData(resBean);
		return outBean;
	}

	public Bean wfeThreePercent(String servId, String dataId) {
		Bean resBean = new Bean();
		Bean bean = ServDao.find(servId, dataId);
		String pid = bean.getStr("S_WF_INST");
		// 确定服务绑定的流程
		String procCode = ServDao.finds("SY_WFE_PROC_DEF", new ParamBean().set("S_FLAG", 1).set("SERV_ID", servId))
				.get(0).getId();
		String sqlWhere = "and PROC_CODE LIKE '" + servId + "%'";
		String sqlWhere_now = "and PROC_CODE LIKE '" + servId + "%' and PI_ID = '" + pid + "'";
		List<Bean> list = ServDao.finds("SY_WFE_NODE_INST_HIS", sqlWhere);
		List<Bean> list_now = ServDao.finds("SY_WFE_NODE_INST", sqlWhere_now);
		if (list_now.size() == 0) {
			list_now = ServDao.finds("SY_WFE_NODE_INST_HIS", sqlWhere_now);
		}
		ArrayList<Bean> arrayList = wfeDataDue(list);
		ArrayList<Bean> arrayList_now = wfeDataDue(list_now);

		HashMap<String, Double> hashMap = new HashMap<String, Double>();
		DecimalFormat df = new DecimalFormat("######0.00");
		for (Bean bean2 : arrayList) {
			String piId = bean2.getStr("PI_ID");
			Double hour = bean2.getDouble("HOURS");
			if (hashMap.containsKey(piId)) {
				hour = hashMap.get(piId) + hour;
			}
			hashMap.put(piId, Double.parseDouble(df.format(hour)));
		}
		int dataCount = hashMap.size();
		// 最优数据的PI_ID
		String maxPiId = "";
		Double maxHour = 0.0;
		Double sumHour = 0.0;
		Iterator<Entry<String, Double>> iter = hashMap.entrySet().iterator();
		while (iter.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			Double val = (Double) entry.getValue();
			sumHour += val;
			if (maxHour == 0) {
				maxPiId = key;
				maxHour = val;
			} else if (val < maxHour) {
				maxPiId = key;
				maxHour = val;
			}
		}

		double now_total = 0.0;
		double avg_total = 0.0;
		for (Bean bean2 : arrayList_now) {
			Double hour = bean2.getDouble("HOURS");
			now_total += hour;
		}

		if (dataCount > 0) {
			avg_total += sumHour / dataCount;
		}

		resBean.set("hashMap_NOW_TOTAL", df.format(now_total));
		resBean.set("hashMap_QUICK_TOTAL", df.format(maxHour));
		resBean.set("hashMap_AVG_TOTAL", df.format(avg_total));

		// 流程节点总数
		List<Bean> nodeList1 = ServDao.finds("SY_WFE_NODE_DEF",
				"and PROC_CODE = '" + procCode + "' and s_flag = 1 order by NODE_CODE");
		int nodeTotal = nodeList1.size();
		// 1-10 一个一组 11-20 两个一组 21-20 三个一组
		int groupNodeCount = (int) (nodeTotal / 10.1) + 1;
		ArrayList<String> nodeList2 = new ArrayList<String>();
		for (int i = 1; i <= groupNodeCount; i++) {
			String nodeStr = "";
			for (int j = ((i - 1) * 10), size = ((i - 1) * 10 + 10); j < size; j++) {
				if (nodeTotal > j) {
					nodeStr += "," + nodeList1.get(j).getStr("NODE_CODE");
				} else {
					break;
				}
			}
			nodeList2.add(nodeStr.substring(1));
		}
		// for (int i = 0; i < nodeList1.size(); i++) {
		// String nodeStr = nodeList1.get(i).getStr("NODE_CODE");
		// int nodeTmpCount = groupNodeCount;
		// while (nodeTmpCount - 1 > 0) {
		// if (i < nodeList1.size()) {
		// nodeStr += "," + nodeList1.get(i).getStr("NODE_CODE");
		// } else {
		// break;
		// }
		// }
		// nodeList2.add(nodeStr);
		// }

		// 当前数据
		HashMap<Integer, Double> hashMap_NOW = new HashMap<Integer, Double>();
		// 最优数据
		HashMap<Integer, Double> hashMap_QUICK = new HashMap<Integer, Double>();
		// 平均数据
		HashMap<Integer, Double> hashMap_AVG = new HashMap<Integer, Double>();
		for (int i = 0; i < nodeList2.size(); i++) {
			String[] nodeCodes = nodeList2.get(i).split(",");
			double value_NOW = 0.0;
			double value_QUICK = 0.0;
			double value_total = 0.0;

			for (String code : nodeCodes) {
				// 最优 平均
				for (Bean bean_his : arrayList) {
					String pi_his = bean_his.getStr("PI_ID");
					String node_his = bean_his.getStr("NODE_CODE");
					Double hours_his = bean_his.getDouble("HOURS");
					if (pi_his.equals(maxPiId) && node_his.equals(code)) {
						value_QUICK += hours_his;
					}
					if (node_his.equals(code)) {
						value_total += hours_his;
					}
				}
				// 当前
				for (Bean bean_now : arrayList_now) {
					// String pi_his = bean_now.getStr("PI_ID");
					String node_his = bean_now.getStr("NODE_CODE");
					Double hours_his = bean_now.getDouble("HOURS");
					if (node_his.equals(code)) {
						value_NOW += hours_his;
					}
				}

			}
			double value_AVG = 0.0;
			if (dataCount > 0) {
				value_AVG = value_total / dataCount;
			}
			hashMap_NOW.put(i, Double.parseDouble(df.format(value_NOW)));
			hashMap_QUICK.put(i, Double.parseDouble(df.format(value_QUICK)));
			hashMap_AVG.put(i, Double.parseDouble(df.format(value_AVG)));
		}

		resBean.set("count", nodeList2.size());
		resBean.set("hashMap_NOW", hashMap_NOW);
		resBean.set("hashMap_QUICK", hashMap_QUICK);
		resBean.set("hashMap_AVG", hashMap_AVG);

		return resBean;
	}

	private ArrayList<Bean> wfeDataDue(List<Bean> list) {
		ArrayList<Bean> resList = new ArrayList<Bean>();
		DecimalFormat dFormat = new DecimalFormat(".##");
		for (Bean bean2 : list) {
			Bean tmpBean = new Bean();
			double nodeDays = bean2.getDouble("NODE_DAYS") > 0 ? bean2.getDouble("NODE_DAYS") : 1;
			String hour = dFormat.format(nodeDays / 60);
			tmpBean.set("PI_ID", bean2.getStr("PI_ID"));
			tmpBean.set("NODE_CODE", bean2.getStr("NODE_CODE"));
			tmpBean.set("HOURS", hour);
			resList.add(tmpBean);
		}
		return resList;
	}

	public ApiOutBean rtnQcr(Bean reqData) {
		ApiOutBean outBean = new ApiOutBean();
		if (reqData.isEmpty("nid")) {
			throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_004.getValue());
		}

		try {
			String niId = reqData.getStr("nid");

			WfAct wfAct = new WfAct(niId, true);
			if (!wfAct.isRunning()) {
				throw new TipException(ApiConstant.RTN_CODE_ENUM.CODE_002.getValue());
			}

			WfAct firstAct = wfAct.getProcess().getFirstWfAct();
			String definedUsers = firstAct.getNodeInstBean().getStr("TO_USER_ID");
			String definedNodeCode = firstAct.getNodeInstBean().getStr("NODE_CODE");

			UserBean currUserBean = Context.getUserBean();
			WfParam wfParam = new WfParam();

			wfParam.setTypeTo(WfParam.TYPE_TO_USER);
			wfParam.setDoneUser(currUserBean);
			wfParam.setToUser(definedUsers);
			wfParam.setDoneType(WfeConstant.NODE_DONE_TYPE_END);
			wfParam.setDoneDesc(WfeConstant.NODE_DONE_TYPE_END_DESC);

			wfAct.toNextAndEndMe(definedNodeCode, wfParam);

			OutBean rtnBean = new OutBean();
			rtnBean.set("rtnstr", "success");

			return outBean;
		} finally {
			WfContext.cleanThreadData();
		}
	}

	public ApiOutBean getLineDefByNode(Bean paramBean) {
		ApiOutBean outBean = new ApiOutBean();
		String nid = paramBean.getStr("nid");
		WfAct wfAct = new WfAct(nid, true);

		String startNode = paramBean.getStr("startNode");
		String endNode = paramBean.getStr("endNode");

		String lineCode = paramBean.getStr("lineCode");

		WfLineDef lineDef = null;
		if (!StringUtils.isEmpty(lineCode)) {// 优先使用lineCode查询
			lineDef = wfAct.getProcess().getProcDef().findLineDefByLineCode(lineCode);
		}
		if (lineDef == null) {
			lineDef = wfAct.getProcess().getProcDef().findLineDef(startNode, endNode);
		}

		if (lineDef == null) {
			lineDef = new WfLineDef(new Bean());
		}

		if (lineDef.isNotEmpty("MIND_CODE")) {
			Bean itemBean = DictMgr.getItem("SY_COMM_MIND_CODE", lineDef.getStr("MIND_CODE"));
			lineDef.set("MIND_NAME", itemBean.getStr("NAME"));
		}

		outBean.setData(lineDef);

		return outBean;
	}
	
	private List<Bean> getNodeUsers(ParamBean paramBean) {
		String userServId = WfNodeUserDao.SY_WFE_NODE_USERS;
		String pid = paramBean.getStr("PI_ID");

		if (procInstIsRunning(paramBean.getStr("INST_IF_RUNNING")) == 2) { // 流程已经办结
			userServId = WfNodeUserHisDao.SY_WFE_NODE_USERS_HIS;
		}

		SqlBean sqlUser = new SqlBean();
		sqlUser.and("PI_ID", pid);
		sqlUser.asc("NI_ID");

		return ServDao.finds(userServId, sqlUser);
	}

	/**
	 * 
	 * @param wfNodeUsers
	 *            节点用户
	 * @return map<节点实例id, 办理人list>
	 */
	private Map<String, List<Bean>> getNodeUserListMap(List<Bean> wfNodeUsers) {
		Map<String, List<Bean>> nodeUsers = new HashMap<String, List<Bean>>();

		for (Bean nodeUser : wfNodeUsers) {

			String nid = nodeUser.getStr("NI_ID");
			if (nodeUsers.containsKey(nid)) {
				List<Bean> users = nodeUsers.get(nid);

				users.add(nodeUser);
			} else {
				List<Bean> users = new ArrayList<Bean>();
				users.add(nodeUser);
				nodeUsers.put(nid, users);
			}
		}

		return nodeUsers;
	}

	private List<Bean> parseTrackList(List<Bean> dataList, ParamBean paramBean) {
		List<Bean> newList = new ArrayList<Bean>();

		if (dataList.size() == 0) {
			return newList;
		}

		HashMap<String, Bean> map = new HashMap<String, Bean>();
		for (Bean nodeInstBean : dataList) {
			map.put(nodeInstBean.getId(), nodeInstBean);
		}

		List<Bean> wfNodeUsers = getNodeUsers(paramBean);

		Map<String, List<Bean>> nodeUsersMap = getNodeUserListMap(wfNodeUsers);

		for (Bean niBean : dataList) {
			Bean resBean = new Bean();

			String nid = niBean.getStr("NI_ID");
			resBean.set("wfNId", niBean.getStr("NI_ID"));
			resBean.set("nodeName", niBean.getStr("NODE_NAME"));
			resBean.set("doneTime", niBean.getStr("NODE_ETIME"));
			resBean.set("sendTime", niBean.getStr("NODE_BTIME"));
			resBean.set("nodeIfRunning", niBean.getStr("NODE_IF_RUNNING"));
			resBean.set("doneType", niBean.getStr("DONE_TYPE"));
			resBean.set("duration",
					niBean.getInt("NODE_DAYS") > 0 ? DataStatsUtil.division(niBean.getInt("NODE_DAYS"), 60) : 0);
			resBean.set("doneType", niBean.getStr("DONE_TYPE"));
			resBean.set("xdtime", niBean.getStr("NODE_LIMIT_TIME"));

			List<Bean> resList = ServDao.finds("SY_WFE_TRACK_V", "AND NI_ID = '" + nid + "'");

			if (resList.size() > 0) {
				resBean.set("mindContent", resList.get(0).getStr("MIND_CONTENT"));
				resBean.set("mindFile", resList.get(0).getStr("MIND_FILE"));
			} else {
				resBean.set("mindContent", "");
				resBean.set("mindFile", "");
			}

			Bean preNode = map.get(niBean.getStr("PRE_NI_ID"));

			if (niBean.getStr("PRE_LINE_CODE").startsWith("R")) {
				niBean.set("PRE_LINE_CODE", niBean.getStr("PRE_LINE_CODE").replace("R", ""));
			}

			List<Bean> nodeUsers = nodeUsersMap.get(nid);
			if (niBean.isNotEmpty("TO_USER_ID")) { // 如果接收人不为NULL，则将接收人的部门作为办理部门。
				UserBean doneUser = UserMgr.getUser(niBean.getStr("TO_USER_ID"));
				resBean.set("doneUserCode", doneUser.getId());
				resBean.set("doneUserName", doneUser.getName());
				resBean.set("doneUserPost", doneUser.getPost());
				resBean.set("doneDeptCode", doneUser.getDeptCode());
				resBean.set("doneDeptName", doneUser.getDeptName());
				resBean.set("doneUserImg", doneUser.getImgSrc());
			}

			if (niBean.isEmpty("TO_USER_ID")) { // 还没设置上，这个是肯定多人的
				StringBuilder strUser = new StringBuilder();
				StringBuilder strId = new StringBuilder();
				StringBuilder strDept = new StringBuilder();
				StringBuilder strDeptId = new StringBuilder();
				for (Bean nodeUser : nodeUsers) {
					UserBean doneUser = UserMgr.getUser(nodeUser.getStr("TO_USER_ID"));
					strUser.append(doneUser.getName()).append(",");
					strId.append(doneUser.getId()).append(",");
					strDept.append(doneUser.getDeptName()).append(",");
					strDeptId.append(doneUser.getDeptCode()).append(",");
				}

				if (strUser.length() > 0) {
					strUser.setLength(strUser.length() - 1);
				}
				if (strId.length() > 0) {
					strId.setLength(strId.length() - 1);
				}
				if (strDept.length() > 0) {
					strDept.setLength(strDept.length() - 1);
				}
				if (strDeptId.length() > 0) {
					strDeptId.setLength(strDeptId.length() - 1);
				}
				resBean.set("doneUserCode", strId);
				resBean.set("doneUserName", strUser);
				resBean.set("doneUserPost", "");
				resBean.set("doneDeptCode", strDeptId);
				resBean.set("doneDeptName", strDept);
				resBean.set("doneUserImg", "");
			} else if (niBean.isEmpty("DONE_USER_NAME")) {
				// 如果实际办理人为空则把当前接收人的值作为实际办理人。解决正在办理节点没有实际办理人的问题。
				UserBean doneUser = UserMgr.getUser(niBean.getStr("TO_USER_ID"));
				resBean.set("doneUserCode", doneUser.getId());
				resBean.set("doneUserName", doneUser.getName());
				resBean.set("doneUserPost", doneUser.getPost());
				resBean.set("doneDeptCode", doneUser.getDeptCode());
				resBean.set("doneDeptName", doneUser.getDeptName());
				resBean.set("doneUserImg", doneUser.getImgSrc());
			} else {
				UserBean toUser = UserMgr.getUser(niBean.getStr("TO_USER_ID"));
				UserBean doneUser = UserMgr.getUser(niBean.getStr("DONE_USER_ID"));
				String doneUserName = doneUser.getName();
				if (!toUser.getId().equals(doneUser.getId())) { // 如果接收人和办理人不是同一个人则显示2人的姓名。
					doneUserName = toUser.getName() + " / " + doneUser.getName();
				}

				if (niBean.isNotEmpty("SUB_USER_NAME")) {
					doneUserName += "[" + niBean.getStr("SUB_USER_NAME") + " 代办]";
				}

				final int doneType = niBean.getInt("DONE_TYPE");
				if (doneType == WfeConstant.NODE_DONE_TYPE_WITHDRAW) {
					// 收回
					doneUserName += "(被" + WfeConstant.NODE_DONE_TYPE_WITHDRAW_DESC + ")";
				} else if (doneType == WfeConstant.NODE_DONE_TYPE_STOP) {
					// 中止
					doneUserName += "(" + WfeConstant.NODE_DONE_TYPE_STOP_DESC + ")";
				} else if (doneType == WfeConstant.NODE_DONE_TYPE_FINISH) {
					// 办结
					doneUserName += "(" + WfeConstant.NODE_DONE_TYPE_FINISH_DESC + ")";
				} else if (doneType == WfeConstant.NODE_DONE_TYPE_CONVERGE) {
					// 合并
					doneUserName += "(" + WfeConstant.NODE_DONE_TYPE_CONVERGE_DESC + ")";
				} else if (doneType == WfeConstant.NODE_DONE_TYPE_AGREE) {
                    // 同意
					doneUserName += "(" + WfeConstant.NODE_DONE_TYPE_AGREE_DESC + ")";
                } else if (doneType == WfeConstant.NODE_DONE_TYPE_DISAGREE) {
                    // 不同意
                	doneUserName += "(" + WfeConstant.NODE_DONE_TYPE_DISAGREE_DESC + ")";
                }

				resBean.set("doneUserCode", doneUser.getId());
				resBean.set("doneUserName", doneUserName);
				resBean.set("doneUserPost", doneUser.getPost());
				resBean.set("doneDeptCode", doneUser.getDeptCode());
				resBean.set("doneDeptName", doneUser.getDeptName());
				resBean.set("doneUserImg", doneUser.getImgSrc());
			}

			if (preNode != null) {
				UserBean toUser;
				// 如果前一个节点不为空，则把上一个节点办理人作为本节点的送交人。
				if (preNode.isEmpty("DONE_USER_NAME")) { // 如果前一个节点未结束
					toUser = UserMgr.getUser(preNode.getStr("TO_USER_ID"));
				} else {
					toUser = UserMgr.getUser(preNode.getStr("DONE_USER_ID"));
				}
				resBean.set("sendUserCode", toUser.getId());
				resBean.set("sendUserName", toUser.getName());
				resBean.set("sendUserPost", toUser.getPost());
				resBean.set("sendDeptCode", toUser.getDeptCode());
				resBean.set("sendDeptName", toUser.getDeptName());
				resBean.set("sendUserImg", toUser.getImgSrc());
			} else {
				// 如果前一个节点不空，则把上一个节点办理人设置为NULL
				resBean.set("sendUserCode", "");
				resBean.set("sendUserName", "");
				resBean.set("sendUserPost", "");
				resBean.set("sendDeptCode", "");
				resBean.set("sendDeptName", "");
				resBean.set("sendUserImg", "");
			}

			newList.add(resBean);
		}
		return newList;
	}
	
	public ApiOutBean checkNodeRunning(Bean paramBean) {
		ApiOutBean outBean = new ApiOutBean();

		String nid = paramBean.getStr("nid");

		Bean instBean = ServDao.find(WfNodeInstDao.SY_WFE_NODE_INST_SERV, nid);

		if (instBean == null || instBean.getInt("DONE_TYPE") != 0) {
			outBean.setData(new Bean().set("flag", false));
		}

		return outBean;
	}

}
