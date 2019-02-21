package com.rh.core.wfe.serv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.WfProcess;
import com.rh.core.wfe.db.WfNodeInstDao;
import com.rh.core.wfe.db.WfNodeInstHisDao;
import com.rh.core.wfe.db.WfNodeUserDao;
import com.rh.core.wfe.db.WfNodeUserHisDao;
import com.rh.core.wfe.util.WfeConstant;
import com.rh.core.wfe.util.WfeFigure;

/**
 * 图形化流程跟踪
 *
 */
public class FigureServ extends CommonServ {

	/**
	 * 图形化显示流程跟踪
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @return 返回前台Bean
	 */
	public OutBean show(ParamBean paramBean) {
		String pid = paramBean.getStr("PI_ID");
		String procRunning = paramBean.getStr("INST_IF_RUNNING");

		String servId = WfNodeInstDao.SY_WFE_NODE_INST_SERV; // 流程未办结
		String userServId = WfNodeUserDao.SY_WFE_NODE_USERS;

		if (!procInstIsRunning(paramBean)) { // 流程已经办结
			servId = WfNodeInstHisDao.SY_WFE_NODE_INST_HIS_SERV;
			userServId = WfNodeUserHisDao.SY_WFE_NODE_USERS_HIS;
		}

		Bean queryBean = new Bean();
		queryBean.set("PI_ID", pid);
		queryBean.set("INST_IF_RUNNING", procRunning);
		queryBean.set(Constant.PARAM_ORDER, "NODE_BTIME ASC");

		boolean isProcRunning = procInstIsRunning(paramBean);

		WfProcess wfProcess = new WfProcess(pid, isProcRunning);

		queryBean.set("PROC_CODE", wfProcess.getProcDef().getStr("PROC_CODE"));

		// 图形化数据 for 测试
		String xmlContent = wfProcess.getProcDef().getStr("PROC_XML"); // 通过工作流定义，取到xml文件

		List<Bean> wfInstBeanList = (List<Bean>) ServDao.finds(servId, queryBean);

		SqlBean sqlUser = new SqlBean();
		sqlUser.and("PI_ID", pid);
		sqlUser.asc("NI_ID");
		List<Bean> wfNodeUsers = (List<Bean>) ServDao.finds(userServId, sqlUser);

		List<WfAct> wfInstList = new ArrayList<WfAct>();
		for (Bean wfInstBean : wfInstBeanList) {
			WfAct wfAct = new WfAct(wfProcess, wfInstBean.getId(), isProcRunning);

			wfInstList.add(wfAct);
		}

		WfeFigure wfeFigure = new WfeFigure(xmlContent, wfInstList, wfNodeUsers);

		OutBean outBean = new OutBean();
		outBean.setToDispatcher("/sy/wfe/track.jsp");
		outBean.set("WF_XML", wfeFigure.getXMLContent());
		outBean.set("ANI_XML", getWfeAniPath(paramBean));
		outBean.set("WF_JSON", xmlToJson(wfeFigure.getXMLContent()));
		return outBean;
	}

	/**
	 * 图形化显示流程跟踪
	 * 
	 * @param paramBean
	 *            参数Bean
	 * @return 返回前台Bean
	 */
	public OutBean showWfeAniForMob(ParamBean paramBean) {
		String pid = paramBean.getStr("PI_ID");
		String procRunning = paramBean.getStr("INST_IF_RUNNING");

		String servId = WfNodeInstDao.SY_WFE_NODE_INST_SERV; // 流程未办结
		String userServId = WfNodeUserDao.SY_WFE_NODE_USERS;

		if (!procInstIsRunning(paramBean)) { // 流程已经办结
			servId = WfNodeInstHisDao.SY_WFE_NODE_INST_HIS_SERV;
			userServId = WfNodeUserHisDao.SY_WFE_NODE_USERS_HIS;
		}

		Bean queryBean = new Bean();
		queryBean.set("PI_ID", pid);
		queryBean.set("INST_IF_RUNNING", procRunning);
		queryBean.set(Constant.PARAM_ORDER, "NODE_BTIME ASC");

		boolean isProcRunning = procInstIsRunning(paramBean);

		WfProcess wfProcess = new WfProcess(pid, isProcRunning);

		queryBean.set("PROC_CODE", wfProcess.getProcDef().getStr("PROC_CODE"));

		// 图形化数据 for 测试
		String xmlContent = wfProcess.getProcDef().getStr("PROC_XML"); // 通过工作流定义，取到xml文件

		List<Bean> wfInstBeanList = (List<Bean>) ServDao.finds(servId, queryBean);

		SqlBean sqlUser = new SqlBean();
		sqlUser.and("PI_ID", pid);
		sqlUser.asc("NI_ID");
		List<Bean> wfNodeUsers = (List<Bean>) ServDao.finds(userServId, sqlUser);

		List<WfAct> wfInstList = new ArrayList<WfAct>();
		for (Bean wfInstBean : wfInstBeanList) {
			WfAct wfAct = new WfAct(wfProcess, wfInstBean.getId(), isProcRunning);

			wfInstList.add(wfAct);
		}

		WfeFigure wfeFigure = new WfeFigure(xmlContent, wfInstList, wfNodeUsers);

		OutBean outBean = new OutBean();
		outBean.setToDispatcher("/sy/wfe/trackAni.jsp");
		outBean.set("WF_XML", wfeFigure.getXMLContent());
		outBean.set("ANI_XML", getWfeAniPath(paramBean));
		outBean.set("WF_JSON", xmlToJson(wfeFigure.getXMLContent()));
		return outBean;
	}

	private String getWfeAniPath(ParamBean bean) {
		try {
			ParamBean paramBean = new ParamBean();
			String queryTable = "";
			String procRunning = bean.getStr("INST_IF_RUNNING");
			paramBean.set("PI_ID", bean.getStr("PI_ID"));
			if (procRunning.equals("2")) { // 流程已办结
				queryTable = WfNodeInstHisDao.SY_WFE_NODE_INST_HIS_SERV;
			} else { // 流程未办结
				queryTable = WfNodeInstDao.SY_WFE_NODE_INST_SERV;
			}

			List<Bean> list = (List<Bean>) ServMgr.act(queryTable, ServMgr.ACT_FINDS, paramBean).getData();

			Map<String, Bean> nodeMap = new HashMap<String, Bean>();

			for (Bean node : list) {
				nodeMap.put(node.getId(), node);
			}
			List<Bean> newlist = new ArrayList<Bean>();
			for (int i = 0; i < list.size(); i++) {
				Bean action = list.get(i);
				Bean tempBean = new Bean();
				Bean preNode = nodeMap.get(action.getStr("PRE_NI_ID"));
				UserBean doneUserBean = UserMgr.getUser(action.getStr("TO_USER_ID"));
				UserBean sendUserBean = UserMgr.getUser(action.getStr("TO_USER_ID"));
				if (action.isNotEmpty("PRE_NI_ID")) {
					doneUserBean = UserMgr.getUser(action.getStr("TO_USER_ID"));
					sendUserBean = UserMgr.getUser(preNode.getStr("DONE_USER_ID"));
				}
				if (preNode != null) {
					tempBean.set("category", "token").set("at", preNode.getStr("NODE_CODE"))
							.set("next", action.getStr("NODE_CODE")).set("doneUser", doneUserBean)
							.set("sendUser", sendUserBean).set("NODE_NAME", action.getStr("NODE_NAME"))
							.set("beginTime", action.getStr("NODE_BTIME")).set("endTime", action.getStr("NODE_ETIME"));
				} else {
					tempBean.set("category", "token").set("at", action.getStr("NODE_CODE"))
							.set("next", action.getStr("NODE_CODE")).set("doneUser", doneUserBean)
							.set("sendUser", sendUserBean).set("NODE_NAME", action.getStr("NODE_NAME"))
							.set("beginTime", action.getStr("NODE_BTIME")).set("endTime", action.getStr("NODE_ETIME"));
				}

				newlist.add(tempBean);
			}

			Collections.reverse(newlist); // 倒序排列

			return JsonUtils.toJson(newlist);
		} catch (Exception e) {
			throw new TipException(e.getMessage());
		}
	}

	private String xmlToJson(String xmlContext) {
		try {
			Document document = DocumentHelper.parseText(xmlContext);
			Element root = document.getRootElement();
			Element jsonDesignElement = root.element("jsonDesign");
			Bean wfeXmlBean = JsonUtils.toBean(jsonDesignElement.getText());
			String currNodeCode = "";
			if (root.element("current") != null) {
				currNodeCode = root.element("current").attributeValue("currentNodeCode").trim();
			}

			List<Element> actionList = root.elements("Action");

			List<Bean> nodeList = wfeXmlBean.getList("nodeDataArray");

			Map<String, Bean> nodeMap = new HashMap<String, Bean>();

			for (Bean node : nodeList) {
				if (node.get("category").equals("LinkLabel")) {
					nodeMap.put(node.getStr("key"), node);
				} else {
					node.remove("color");
					node.set("category", "tomorrow");
					nodeMap.put(node.getStr("key"), node);
				}
			}

			Map<String, List<String>> actionMap = new HashMap<String, List<String>>();

			for (Element action : actionList) {
				Bean node = nodeMap.get(action.attributeValue("NodeCode").trim());
				node.set("category", "yesterday");
				nodeMap.put(node.getStr("key"), node);
				if (!actionMap.containsKey(action.attributeValue("NodeCode").trim())) {
					List<String> tmpList = new ArrayList<String>();
					tmpList.add("{'EndTime':'" + action.attributeValue("EndTime") + "','BeginTime':'"
							+ action.attributeValue("BeginTime") + "','DoneUser':'" + action.attributeValue("DoneUser")
							+ "','DoneDept':'" + action.attributeValue("DoneDept") + "','SendUser':'"
							+ action.attributeValue("SendUser") + "','SendDept':'" + action.attributeValue("SendDept")
							+ "','NodeName':'" + node.getStr("text") + "'}");
					actionMap.put(action.attributeValue("NodeCode").trim(), tmpList);
				} else {
					List<String> tmpList = actionMap.get(action.attributeValue("NodeCode").trim());
					tmpList.add("{'EndTime':'" + action.attributeValue("EndTime") + "','BeginTime':'"
							+ action.attributeValue("BeginTime") + "','DoneUser':'" + action.attributeValue("DoneUser")
							+ "','DoneDept':'" + action.attributeValue("DoneDept") + "','SendUser':'"
							+ action.attributeValue("SendUser") + "','SendDept':'" + action.attributeValue("SendDept")
							+ "','NodeName':'" + node.getStr("text") + "'}");
					actionMap.put(action.attributeValue("NodeCode").trim(), tmpList);
				}
			}
			if (currNodeCode != null && currNodeCode.length() > 0) {
				Bean currNode = nodeMap.get(currNodeCode);
				currNode.set("category", "today");
				nodeMap.put(currNodeCode, currNode);
			}

			List<Bean> tmpList = new ArrayList<Bean>();
			for (Bean node : nodeMap.values()) {
				if (actionMap.containsKey(node.getStr("key"))) {
					node.set("actionMap", actionMap.get(node.getStr("key")));
				}
				tmpList.add(node);
			}
			wfeXmlBean.set("nodeDataArray", tmpList);
			return JsonUtils.mapsToJson(wfeXmlBean);
		} catch (Exception e) {
			throw new TipException(e.getMessage());
		}
	}

	/**
	 * @param paramBean
	 *            从页面传来的 INST_IF_RUNNING 字符串类型的 流程是否运行
	 * @return 流程是否运行
	 */
	private boolean procInstIsRunning(ParamBean paramBean) {

		String procRunning = paramBean.getStr("INST_IF_RUNNING");
		int sFlag = paramBean.getInt("S_FLAG");
		boolean procIsRunning = true;
		if (sFlag > 0 && sFlag == Constant.NO_INT) {
			procIsRunning = false;
		} else if (procRunning.equals(String.valueOf(WfeConstant.WFE_PROC_INST_NOT_RUNNING))) {
			procIsRunning = false;
		}

		return procIsRunning;
	}
}
