package com.rh.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;
import com.rh.api.client.DisThreadPool;
import com.rh.api.client.DistrClientServ;
import com.rh.api.entity.SendParamEntity;
import com.rh.api.serv.ISendApiServ;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;

public class SendApiServImpl implements ISendApiServ {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(SendApiServImpl.class);
	/**
	 * 收文的服务编码
	 */
	private static String SEND_SERV = "SY_COMM_SEND_DETAIL";
	/**
	 * 节点实例的服务编码
	 */
	private static String NODE_INST = "SY_WFE_NODE_INST";
	/**
	 * 节点实例历史的服务编码
	 */
	private static String NODE_INST_HIS = "SY_WFE_NODE_INST_HIS";

	@Override
	public ApiOutBean send(List<Bean> list, Bean param) {
		ApiOutBean result = new ApiOutBean();
		// 返回的message
		String msg = "";
		// 线程池数量
		int threadNum = 2;
		List<Bean> rtnList = new ArrayList<Bean>(list.size());
		List<Bean> copyNodeList = new ArrayList<Bean>();
		UserBean sendUser = Context.getUserBean();
		String servId = param.getStr("servId");
		String dataId = param.getStr("dataId");
		// 获取发文的数据信息
		Bean fwDataBean = ServDao.find(servId, dataId);

		if (null == fwDataBean) {
			msg = "没有查询到当前发文数据！";
			log.error(msg);
			throw new TipException(msg);
		}

		// 署领导最后签发时间为成文时间
		String cwDate = "";
		StringBuffer mindSql = new StringBuffer();
		mindSql.append("AND MIND_CODE = 'PS-0002' AND SERV_ID = '").append(servId).append("' AND DATA_ID = '")
				.append(dataId).append("'");
		ParamBean mindParam = new ParamBean();
		mindParam.setWhere(mindSql.toString()).setOrder("MIND_TIME desc");
		OutBean outMind = ServMgr.act("SY_COMM_MIND", ServMgr.ACT_QUERY, mindParam);
		List<Bean> mindList = outMind.getDataList();
		if (mindList.size() > 0) {
			Bean mindBean = mindList.get(0);
			if (null != mindBean && mindBean.isNotEmpty("MIND_TIME")) {
				cwDate = mindBean.getStr("MIND_TIME").substring(0, 10);
			}
		}
		fwDataBean.set("GW_CW_DATA", cwDate);
//        List<Bean> mind = ServDao.finds("SY_COMM_MIND", new ParamBean().setWhere(""));

		// 获取发文的正文/附件等信息
		StringBuffer fileSb = new StringBuffer();
		fileSb.append("AND DATA_ID = '").append(dataId).append("' AND (ITEM_CODE IN ('OFD','WENGAO') ")
				.append("OR FILE_CAT = 'FUJIAN')").append(" AND SERV_ID = '").append(servId).append("'");
		List<Bean> fileList = ServDao.finds("SY_COMM_FILE", fileSb.toString());

		// 分发的参数实体类
		SendParamEntity sendEntity = new SendParamEntity(cwDate, fwDataBean, sendUser, fileList);
		// 实例化类对象
		DistrClientServ disClient = new DistrClientServ(sendEntity, rtnList, copyNodeList);
		// 实例化线程池
		final DisThreadPool rhPool = new DisThreadPool(threadNum, list, disClient);
		// 执行
		rhPool.execute();
		msg = disClient.getMessage();

		// 批量增加分发记录日志
		ServDao.creates(SEND_SERV, disClient.rtnList);
		// 批量增加节点记录日志
		ServDao.creates(NODE_INST, disClient.copyNodeList);
		result.setMessage(msg); // 记录返回消息
		result.setData(new Bean().set("distrList", disClient.rtnList));

		return result;
	}

	@Override
	public ApiOutBean sendRead(ApiParamBean paramBean) {
		List<Bean> list = paramBean.getList("reqData");

		Bean nodeData = new Bean();
		if (paramBean.isNotEmpty("sendData")) {
			Bean sendData = paramBean.getBean("sendData");
			String niId = sendData.getStr("NI_ID");
			nodeData = ServDao.find(NODE_INST, niId);
			if (null == nodeData) {
				nodeData = ServDao.find(NODE_INST_HIS, niId);
			}
		} else {
			nodeData = paramBean.getBean("nodeData");
		}
		ApiOutBean result = new ApiOutBean();
		String sWfNid = nodeData.getId();
		List<Bean> rtnList = new ArrayList<Bean>(list.size());

		for (Bean detail : list) {
			ParamBean sendDetail = new ParamBean();
			String dataId = detail.getStr("dataId");
			String servId = detail.getStr("servId");
			nodeData.setId("");
			String nodePk = Lang.getUUID();
			nodeData.set("NI_ID", nodePk);
			sendDetail.set("SEND_FORR", 2);
			sendDetail.set("NI_ID", nodePk);
			sendDetail.set("DATA_ID", dataId);
			sendDetail.set("SERV_ID", servId);
			sendDetail.set("SEND_STATUS", "2");
			sendDetail.set("SEND_STATUS", "2");
			sendDetail.set("S_WF_NI_ID", sWfNid);
			sendDetail.set("SEND_TIME", DateUtils.getDatetime());
			sendDetail.set("SEND_NUM", detail.getInt("sendNum"));
			sendDetail.set("SEND_TYPE", detail.isNotEmpty("sendType") ? detail.getInt("sendType") : 1);
			sendDetail.set("RECV_TYPE", "inside");
			if (detail.getStr("type").equals("user")) {
				String recUserCode = detail.getStr("code");
				UserBean recUser = UserMgr.getUser(recUserCode);
				sendDetail.set("RECV_USER", recUser.getId());
				sendDetail.set("RECV_UNAME", recUser.getName());
				sendDetail.set("RECV_DEPT", recUser.getDeptCode());
				sendDetail.set("RECV_DNAME", recUser.getDeptName());
				sendDetail.set("RECV_ODEPT", recUser.getODeptCode());
				sendDetail.set("RECV_TDEPT", recUser.getTDeptCode());
				sendDetail.set("RECV_TNAME", recUser.getTDeptName());

				OutBean saveBean = ServMgr.act(SEND_SERV, ServMgr.ACT_SAVE, sendDetail);
				Bean nodeBean = new Bean(nodeData);
				Bean gwBean = ServDao.find(servId, dataId);
				result = this.saveToWfe(recUser, nodeBean, saveBean, gwBean);
				rtnList.add(saveBean);
			} else if (detail.getStr("type").equals("role")) {
				List<Bean> userList = UserMgr.getUserListByRole(detail.getStr("code"), Context.getCmpy());
				for (Bean user : userList) {
					UserBean recUser = UserMgr.getUser(user.getId());
					sendDetail.set("RECV_USER", recUser.getId());
					sendDetail.set("RECV_UNAME", recUser.getName());
					sendDetail.set("RECV_DEPT", recUser.getDeptCode());
					sendDetail.set("RECV_DNAME", recUser.getDeptName());
					sendDetail.set("RECV_ODEPT", recUser.getODeptCode());
					sendDetail.set("RECV_TDEPT", recUser.getTDeptCode());
					sendDetail.set("RECV_TNAME", recUser.getTDeptName());
					OutBean saveBean = ServMgr.act(SEND_SERV, ServMgr.ACT_SAVE, sendDetail);
					// this.saveToWfe(recUser, nodeData, detail);
					rtnList.add(saveBean);
				}
			} else if (detail.getStr("type").equals("extUnit")) {
				sendDetail.set("RECV_USER", detail.getStr("code"));
				sendDetail.set("RECV_UNAME", detail.getStr("name"));
				sendDetail.set("RECV_DEPT", detail.getStr("code"));
				sendDetail.set("RECV_DNAME", detail.getStr("name"));
				sendDetail.set("RECV_TYPE", "outside");
				OutBean saveBean = ServMgr.act(SEND_SERV, ServMgr.ACT_SAVE, sendDetail);
				rtnList.add(saveBean);
			} else if (detail.getStr("type").equals("dept")) {
				String ORG_RECV_ROLE = Context.getSyConf("ORG_RECV_ROLE", "RGSWY");
				List<Bean> userList = UserMgr.getUserListByRole(ORG_RECV_ROLE, Context.getCmpy());
				for (Bean user : userList) {
					UserBean recUser = UserMgr.getUser(user.getId());
					if (!recUser.getODeptCode().equals(detail.getStr("code"))) {
						continue;
					}
					sendDetail.set("RECV_USER", recUser.getId());
					sendDetail.set("RECV_UNAME", recUser.getName());
					sendDetail.set("RECV_DEPT", recUser.getDeptCode());
					sendDetail.set("RECV_DNAME", recUser.getDeptName());
					sendDetail.set("RECV_ODEPT", recUser.getODeptCode());
					sendDetail.set("RECV_TDEPT", recUser.getTDeptCode());
					sendDetail.set("RECV_TNAME", recUser.getTDeptName());
					OutBean saveBean = ServMgr.act(SEND_SERV, ServMgr.ACT_SAVE, sendDetail);
					// this.saveToWfe(recUser, nodeData, detail);
					rtnList.add(saveBean);
				}
			}
		}

		result.setData(new Bean().set("readList", rtnList));
		result.setMessage("送阅成功。");

		return result;
	}

	/***
	 * 判断是否能送阅给被送阅人 （true代表已经送过或者是自己；不能送）
	 * 
	 * @param recUserCode 被送阅人CODE
	 * @param dataId      数据主键
	 * @return true/false
	 */
	public boolean getRecvAuth(String recUserCode, String dataId) {
		UserBean userInfo = Context.getUserBean();
		if (recUserCode.equals(userInfo.getCode())) {
			// sendMsg += Context.getSyMsg("SY_SEND_SENDED_MSG", "已跳过本人的送阅；");
			return true;
		} else {
			List<Bean> sendList = ServDao.finds(SEND_SERV, "AND DATA_ID = '" + dataId + "'");
			for (Bean sendBean : sendList) {
				if (recUserCode.equals(sendBean.getStr("RECV_USER"))) {
					// sendUser += sendBean.getStr("RECV_UNAME") + "、";
					return true;
				}
			}
		}
		return false;
	}

	/***
	 * 把传阅保存到流程跟踪
	 * 
	 * @param recUser  收件人信息
	 * @param nodeData 节点Data
	 * @param saveBean 参数
	 * @param gwBean   公文数据
	 */
	private ApiOutBean saveToWfe(UserBean recUser, Bean nodeData, Bean saveBean, Bean gwBean) {
		ApiOutBean apiOut = new ApiOutBean();
		try {
			UserBean user = Context.getUserBean();
			final String codeName = getCodeName();
			final String nodeName = nodeData.getStr("NODE_NAME");
			nodeData.set("NODE_NAME", nodeName + "（" + codeName + "）"); // 节点名称
			String time = DateUtils.getDatetime();
			nodeData.set("DONE_DESC", codeName);
			nodeData.set("S_MTIME", time); // 修改时间
			nodeData.set("OPEN_TIME", ""); // 打开时间
			nodeData.set("NODE_ETIME", ""); // 办理时间
			nodeData.set("NODE_BTIME", time); // 节点开始时间
			nodeData.set("DONE_USER_ID", user.getCode()); // 办理人员ID
			nodeData.set("DONE_USER_NAME", user.getName()); // 办理人员名称
			nodeData.set("DONE_DEPT_IDS", user.getDeptCode()); // 办理人员部门ID
			nodeData.set("DONE_DEPT_NAMES", user.getDeptName()); // 办理人员部门名称
			nodeData.set("DONE_TYPE", 1); // 1是自动正常停止
			nodeData.set("NODE_IF_RUNNING", 2); // 2是代表节点停止
			nodeData.set("TO_USER_ID", recUser.getCode()); // 处理人员ID
			nodeData.set("TO_USER_NAME", recUser.getName()); // 处理人员名称

			ServDao.save("SY_WFE_NODE_INST", nodeData);
			apiOut.setMessage("流程跟踪添加成功！");

			this.sendTodo(recUser, saveBean, gwBean);
		} catch (Exception e) {
			log.error(e.getMessage());
			apiOut.setMessage(e.getMessage());
			e.printStackTrace();
		}
		return apiOut;
	}

	/***
	 * 发送待办
	 * 
	 * @param recUser    收件人信息
	 * @param sendDetail 分发明细
	 * @param dataBean   参数
	 */
	public ApiOutBean sendTodo(UserBean recUser, Bean sendDetail, Bean dataBean) {
		ApiOutBean apiOut = new ApiOutBean();
		try {
			String codeName = getCodeName();
			String servId = sendDetail.getStr("SERV_ID");
			ServDefBean servDef = ServUtils.getServDef(servId);
			String result = ServUtils.replaceValues(servDef.getDataTitle(), servId, dataBean);
			StringBuffer titleSb = new StringBuffer();
			titleSb.append("来自").append(Context.getUserBean().getName()).append("的").append(codeName).append(result);

			TodoBean todo = new TodoBean();
			todo.setTitle(titleSb.toString());
			todo.setSender(sendDetail.getStr("S_USER"));
			todo.setOwner(sendDetail.getStr("RECV_USER"));
			todo.setCode(sendDetail.getStr("SERV_ID"));
			todo.setCodeName(codeName);
			todo.setObjectId1(sendDetail.getStr("DATA_ID"));
			todo.setCatalog(2); // 待办的类型，1,办件，2，阅件
			todo.setObjectId2(sendDetail.getId()); // TODO_OBJECT_ID2 中存 分发的ID

			todo.setUrl(sendDetail.getStr("SERV_ID") + ".byid.do?data={_PK_:" + sendDetail.getStr("DATA_ID")
					+ ",MODE:READ,SEND_ID:" + sendDetail.getId() + "}");

			TodoUtils.insert(todo);
			apiOut.setMessage("待办发送成功！");
		} catch (Exception e) {
			log.error(e.getMessage());
			apiOut.setMessage(e.getMessage());
			e.printStackTrace();
		}

		return apiOut;
	}

	public String getCodeName() {
		return Context.getSyConf("SY_SEND_TODO_CODE_NAME", "送阅");
	}

	@Override
	public ApiOutBean undo(Bean reqData) {
		ApiOutBean result = new ApiOutBean();
		String details = reqData.getStr("ids");
		OutBean outBean = ServMgr.act(SEND_SERV, "undo", new ParamBean().setId(details));
		result.setData(outBean);
		return result;
	}

	@Override
	public ApiOutBean getReadList(Bean reqData) {
		ApiOutBean result = new ApiOutBean();
		List<Bean> readList = getDetailList(2, reqData.getStr("dataId"));
		result.setData(new Bean().set("readList", readList));
		return result;
	}

	@Override
	public ApiOutBean getDistrList(Bean reqData) {
		ApiOutBean result = new ApiOutBean();
		List<Bean> detailList = getDetailList(1, reqData.getStr("dataId"));
		result.setData(new Bean().set("detailList", detailList));
		return result;
	}

	private List<Bean> getDetailList(int sendForr, String dataId) {
		String where = "AND SEND_FORR = " + sendForr + " AND DATA_ID = '" + dataId + "'";
		List<Bean> detailList = ServMgr
				.act(SEND_SERV, ServMgr.ACT_QUERY, new ParamBean().setSelect("*").set("_WHERE_", where)).getDataList();
		return detailList;
	}
}
