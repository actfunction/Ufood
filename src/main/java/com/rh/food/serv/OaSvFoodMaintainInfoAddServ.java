package com.rh.food.serv;

import java.util.List;
import java.util.ArrayList;

import com.rh.food.util.GenUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.DateUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;

/**
 * 审计管理分系统行政办公管理子系统
 * 
 * @author: 梅琪
 * @date: 2018年11月15日 上午10:13:56
 * @version: V1.0
 * @description: 保存或发布副食品维护信息
 */
public class OaSvFoodMaintainInfoAddServ extends CommonServ {

	private static Log log = LogFactory.getLog(OaSvFoodMaintainInfoAddServ.class);

	private static final String SPACE = " ";// 空格
	
	/**
	 * 新增维护数据保存时，将领取时间年月日和时分秒进行合并
	 */
	@Override
	public void afterByid(ParamBean paramBean, OutBean outBean) {
		String orderStartDate = outBean.getStr("ORDER_START_DATE");
		String orderStartTime = outBean.getStr("ORDER_START_TIME");
		String orderEndDate = outBean.getStr("ORDER_END_DATE");
		String orderEndTime = outBean.getStr("ORDER_END_TIME");
		if (StringUtils.isNotEmpty(orderStartDate) && StringUtils.isNotEmpty(orderStartTime)) {
			outBean.put("ORDER_START_DATETIME", orderStartDate + " " + orderStartTime);
		}
		if (StringUtils.isNotEmpty(orderEndDate) && StringUtils.isNotEmpty(orderEndTime)) {
			outBean.put("ORDER_END_DATETIME", orderEndDate + " " + orderEndTime);
		}
	}

	public OutBean savead(ParamBean paramBean) {
			
		OutBean result = new OutBean();
		String newID=null;
		try {
			Transaction.begin();
			try {
				Bean main = JsonUtils.toBean(paramBean.getStr("main"));
				String ORDER_START_DATETIME = main.getStr("ORDER_START_DATETIME");
				String ORDER_END_DATETIME = main.getStr("ORDER_END_DATETIME");
				main.set("ORDER_START_DATE", ORDER_START_DATETIME.split(SPACE)[0]);
				main.set("ORDER_START_TIME", ORDER_START_DATETIME.split(SPACE)[1]);
				main.set("ORDER_END_DATE", ORDER_END_DATETIME.split(SPACE)[0]);
				main.set("ORDER_END_TIME", ORDER_END_DATETIME.split(SPACE)[1]);

				SqlExecutor sqlExecutor = Transaction.getExecutor();

				String maintainStatus = main.getStr("MAINTAIN_STATUS");

				String pkCode = main.getStr("MAINTAIN_ID");

				if (!pkCode.contains("OSFMI")) {
					// 新增
					String maintainId = GenUtil.getAutoIdNumner("OSFMI", "OA_SV_FOOD_MAINTAIN_INFO_SEQ", "NUM");
					newID=maintainId;
					main.set("MAINTAIN_ID", maintainId);
					ServDao.create("OA_SV_FOOD_MAINTAIN_INFO", main);
					// 副食品详细列表
					if (StringUtils.isNotEmpty(paramBean.getStr("FOOD_LIST"))) {
						List<Bean> foods = JsonUtils.toBeanList(paramBean.getStr("FOOD_LIST"));
						List<Bean> insertFoods = new ArrayList<Bean>();
						for (Bean bean : foods) {
							if ("".equals(bean.getStr("FOOD_NAME"))) {
								continue;
							}
							bean.set("FOOD_ID", GenUtil.getAutoIdNumner("OSFMD", "OA_SV_FOOD_MAINTAIN_DET_SEQ", "NUM"));
							bean.set("MAINTAIN_ID", maintainId);
							insertFoods.add(bean);
						}

						ServDao.creates("OA_SV_FOOD_MAINTAIN_DET", insertFoods);
					}
					// 领取时间详细列表

					if (StringUtils.isNotEmpty(paramBean.getStr("TIME_LIST"))) {
						List<Bean> timeList = JsonUtils.toBeanList(paramBean.getStr("TIME_LIST"));
						List<Bean> insertTimeList = new ArrayList<Bean>();
						for (Bean bean : timeList) {
							if ("".equals(bean.getStr("OBTAIN_START_DATETIME"))) {
								continue;
							}
							bean.set("MAINTAIN_ID", maintainId);
							bean.set("OBTAIN_TIME_ID",GenUtil.getAutoIdNumner("OSFRI", "OA_SV_FOOD_RECEIVE_INFO_SEQ", "NUM"));
							bean.set("OBTAIN_START_DATE", bean.getStr("OBTAIN_START_DATETIME").split(SPACE)[0]);
							bean.set("OBTAIN_START_TIME", bean.getStr("OBTAIN_START_DATETIME").split(SPACE)[1]);
							bean.set("OBTAIN_END_DATE", bean.getStr("OBTAIN_END_DATETIME").split(SPACE)[0]);
							bean.set("OBTAIN_END_TIME", bean.getStr("OBTAIN_END_DATETIME").split(SPACE)[1]);
							bean.set("SELECTED_NUMBER", 0);
							insertTimeList.add(bean);
						}

						ServDao.creates("OA_SV_FOOD_RECEIVE_INFO", insertTimeList);
					}

					/**
					 * 当状态为0时往公共待办列表添加数据
					 */
					if ("0".equals(maintainStatus)) {
						// MAINTAIN_STATUS为0时，往公共待办表中增加一条记录
						saveTodo("OA_SV_FOOD_MAINTAIN_INFO_ADD", main.getStr("MAINTAIN_TITLE"), newID);
					}
				} else {
					// 修改
					newID=main.getStr("MAINTAIN_ID");
					StringBuilder sql = new StringBuilder();
					sql.append("update OA_SV_FOOD_MAINTAIN_INFO set ");
					sql.append("MAINTAIN_TITLE='" + main.getStr("MAINTAIN_TITLE") + "',");
					sql.append("ORDER_START_DATE='" + main.getStr("ORDER_START_DATE") + "',");
					sql.append("ORDER_END_DATE='" + main.getStr("ORDER_END_DATE") + "',");
					sql.append("ORDER_START_TIME='" + main.getStr("ORDER_START_TIME") + "',");
					sql.append("ORDER_END_TIME='" + main.getStr("ORDER_END_TIME") + "',");
					sql.append("MAINTAIN_STATUS='" + main.getStr("MAINTAIN_STATUS") + "',");
					sql.append("MAINTAIN_REMARK='" + main.getStr("MAINTAIN_REMARK") + "' ");
					sql.append("where MAINTAIN_ID='" + main.getStr("MAINTAIN_ID") + "'");
					sqlExecutor.execute(sql.toString());
					sqlExecutor.execute("DELETE FROM OA_SV_FOOD_MAINTAIN_DET WHERE MAINTAIN_ID='"+ main.getStr("MAINTAIN_ID") + "'");
					// 副食品详细列表
					if (StringUtils.isNotEmpty(paramBean.getStr("FOOD_LIST"))) {
						List<Bean> foods = JsonUtils.toBeanList(paramBean.getStr("FOOD_LIST"));
						List<Bean> insertFoods = new ArrayList<Bean>();
						for (Bean bean : foods) {
							if ("".equals(bean.getStr("FOOD_NAME"))) {
								continue;
							}
							bean.set("FOOD_ID", GenUtil.getAutoIdNumner("OSFMD", "OA_SV_FOOD_MAINTAIN_DET_SEQ", "NUM"));
							bean.set("MAINTAIN_ID", main.getStr("MAINTAIN_ID"));
							insertFoods.add(bean);
						}

						ServDao.creates("OA_SV_FOOD_MAINTAIN_DET", insertFoods);
					}

					sqlExecutor.execute("DELETE FROM OA_SV_FOOD_RECEIVE_INFO WHERE MAINTAIN_ID='"+ main.getStr("MAINTAIN_ID") + "'");
					// 领取时间详细列表
					if (StringUtils.isNotEmpty(paramBean.getStr("TIME_LIST"))) {
						List<Bean> timeList = JsonUtils.toBeanList(paramBean.getStr("TIME_LIST"));
						List<Bean> insertTimeList = new ArrayList<Bean>();
						for (Bean bean : timeList) {
							if ("".equals(bean.getStr("OBTAIN_START_DATETIME"))) {
								continue;
							}
							bean.set("MAINTAIN_ID", main.getStr("MAINTAIN_ID"));
							bean.set("OBTAIN_TIME_ID",GenUtil.getAutoIdNumner("OSFRI", "OA_SV_FOOD_RECEIVE_INFO_SEQ", "NUM"));
							bean.set("OBTAIN_START_DATE", bean.getStr("OBTAIN_START_DATETIME").split(SPACE)[0]);
							bean.set("OBTAIN_START_TIME", bean.getStr("OBTAIN_START_DATETIME").split(SPACE)[1]);
							bean.set("OBTAIN_END_DATE", bean.getStr("OBTAIN_END_DATETIME").split(SPACE)[0]);
							bean.set("OBTAIN_END_TIME", bean.getStr("OBTAIN_END_DATETIME").split(SPACE)[1]);
							bean.set("SELECTED_NUMBER", 0);
							insertTimeList.add(bean);
						}

						ServDao.creates("OA_SV_FOOD_RECEIVE_INFO", insertTimeList);
					}

					if ("1".equals(maintainStatus)) {
						// MAINTAIN_STATUS为1时，从公共待办表中删除关联记录
						deleteTodo(sqlExecutor, newID);
					}else {
						deleteTodo(sqlExecutor, newID);
						saveTodo("OA_SV_FOOD_MAINTAIN_INFO_ADD", main.getStr("MAINTAIN_TITLE"), newID);
					}
				}
				Transaction.commit();
				Bean outmain = ServDao.find(paramBean.getServId(), newID);
				result.set("MAINTAIN_ID", outmain.get("MAINTAIN_ID"));
				result.set("MAINTAIN_TITLE", outmain.get("MAINTAIN_TITLE"));
				result.set("ORDER_START_DATE", outmain.get("ORDER_START_DATE"));
				result.set("ORDER_END_DATE", outmain.get("ORDER_END_DATE"));
				result.set("ORDER_START_TIME", outmain.get("ORDER_START_TIME"));
				result.set("ORDER_END_TIME", outmain.get("ORDER_END_TIME"));
				result.set("MAINTAIN_REMARK", outmain.get("MAINTAIN_REMARK"));
				result.set("MAINTAIN_STATUS", outmain.get("MAINTAIN_STATUS"));
				result.set("MAINTAIN_ADMIN", outmain.get("MAINTAIN_ADMIN"));
				result.set("MAINTAIN_BACKUP", outmain.get("MAINTAIN_BACKUP"));
				result.set("S_FLAG", outmain.get("S_FLAG"));
				result.set("S_USER", outmain.get("S_USER"));
				result.set("S_ATIME", outmain.get("S_ATIME"));
				result.set("S_MTIME", outmain.get("S_MTIME"));
				result.set("S_CMPY", outmain.get("S_CMPY"));
				result.set("S_DEPT", outmain.get("S_DEPT"));
				result.set("S_ODEPT", outmain.get("S_ODEPT"));
				result.set("S_TDEPT", outmain.get("S_TDEPT"));
				
				result.set("FOOD_LIST", queryFoodsByFK(main.getStr("MAINTAIN_ID")));
				result.set("TIME_LIST", queryReceiveTimeByFK(main.getStr("MAINTAIN_ID")));
				result.setOk("副食品添加成功！");
			} catch (Exception e) {
				Transaction.rollback();
				throw e;
			}
			Transaction.end();
		} catch (Exception e) {
			log.error("添加保存异常" + e.getMessage());
			result.setError("添加失败！");
		}
		return result;
	}

	

	/**
	 * @title: saveTodo @description: 保存公共待办 @param servId @param title @param
	 * pkCode @throws Exception @return void @throws
	 */
	public void saveTodo(String servId, String title, String pkCode) throws Exception {
		UserBean user = Context.getUserBean();
		String time = DateUtils.getDatetime();
		String nodeId = Lang.getUUID();

		Bean nodeData = new Bean();
		nodeData.set("NI_ID", nodeId); // 节点主键 此处填写待办表的TODO_OBJECT_ID2的值（必须相等）
		nodeData.set("NODE_NAME", "name"); // 节点名称
		nodeData.set("DONE_DESC", "正常结束");
		nodeData.set("S_MTIME", time); // 修改时间
		nodeData.set("NODE_BTIME", time); // 节点开始时间
		nodeData.set("DONE_USER_ID", user.getCode()); // 办理人员ID
		nodeData.set("DONE_USER_NAME", user.getName()); // 办理人员名称
		nodeData.set("DONE_DEPT_IDS", user.getDeptCode()); // 办理人员部门ID
		nodeData.set("DONE_DEPT_NAMES", user.getDeptName()); // 办理人员部门名称
		nodeData.set("DONE_TYPE", 1); // 1是自动正常停止
		nodeData.set("NODE_IF_RUNNING", 2); // 2是代表节点停止
		nodeData.set("TO_USER_ID", user.getCode()); // 处理人员ID
		nodeData.set("TO_USER_NAME", user.getName()); // 处理人员名称
		nodeData.set("OPEN_TIME", ""); // 打开时间
		ServDao.save("SY_WFE_NODE_INST", nodeData);

		TodoBean dataBean = new TodoBean();
		dataBean.setTitle(title);
		dataBean.setSender(user.getCode());
		dataBean.setOwner(user.getCode());
		dataBean.setCode(servId);
		dataBean.setCodeName("副食品维护");
		dataBean.setDraftDeptName(user.getDeptName());
		
		dataBean.setObjectId1(pkCode);
		dataBean.setCatalog(1);// 待办的类型，1,办件，2，阅件
		dataBean.setObjectId2(nodeId); // TODO_OBJECT_ID2 中存 分发的ID
		//dataBean.setUrl("OA_SV_FOOD_MAINTAIN_INFO_DB_UPDATE&uid="+user.getCode()+"&tTitle=待办编辑&dataId="+pkCode);
		TodoUtils.insert(dataBean);
	}

	/**
	 * @title: deleteTodo @description: 删除公共待办 @param sqlExecutor @param
	 * servId @param pkCode @throws Exception @return void @throws
	 */
	private void deleteTodo(SqlExecutor sqlExecutor, String pkCode) throws Exception {

		Bean todoBean = sqlExecutor.queryOne("SELECT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE " + "TODO_OBJECT_ID1='" + pkCode + "'");
		if (todoBean == null) {
			return;
		}
		sqlExecutor.execute("DELETE FROM SY_WFE_NODE_INST WHERE NI_ID='" + todoBean.getStr("TODO_OBJECT_ID2") + "'");
		sqlExecutor.execute("DELETE FROM SY_COMM_TODO WHERE TODO_OBJECT_ID1='" + pkCode + "'");

	}

	private OutBean queryFoodsByFK(String fk) {
		ParamBean listParam = new ParamBean();
		listParam.set("serv", "OA_SV_FOOD_MAINTAIN_DET_CHILD");
		listParam.set("act", "query");
		listParam.set("_linkWhere", " and MAINTAIN_ID='" + fk + "' and S_FLAG='1'");
		listParam.set("_linkServQuery", 2);
		listParam.set("_NOPAGE_", true);
		listParam.set("_TRANS_", false);
		return query(listParam);
	}

	private OutBean queryReceiveTimeByFK(String fk) {
		ParamBean listParam = new ParamBean();
		listParam.set("serv", "OA_SV_FOOD_RECEIVE_INFO_CHILD");
		listParam.set("act", "query");
		listParam.set("_linkWhere", " and MAINTAIN_ID='" + fk + "' and S_FLAG='1'");
		listParam.set("_linkServQuery", 2);
		listParam.set("_NOPAGE_", true);
		listParam.set("_TRANS_", false);
		return receiveTimeConvert(query(listParam));
	}

	private OutBean receiveTimeConvert(OutBean outBean) {
		List<Bean> dataList = outBean.getDataList();
		if (dataList != null && dataList.size() > 0) {
			for (Bean bean : dataList) {
				String OBTAIN_START_DATE = bean.getStr("OBTAIN_START_DATE");
				String OBTAIN_START_TIME = bean.getStr("OBTAIN_START_TIME");
				bean.put("OBTAIN_START_DATETIME", OBTAIN_START_DATE + SPACE + OBTAIN_START_TIME);
				String OBTAIN_END_DATE = bean.getStr("OBTAIN_END_DATE");
				String OBTAIN_END_TIME = bean.getStr("OBTAIN_END_TIME");
				bean.put("OBTAIN_END_DATETIME", OBTAIN_END_DATE + SPACE + OBTAIN_END_TIME);
			}
		}
		return outBean;
	}
}
