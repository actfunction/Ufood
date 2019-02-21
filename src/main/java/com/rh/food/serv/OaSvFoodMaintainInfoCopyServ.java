package com.rh.food.serv;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * 
 * 审计管理分系统行政办公管理子系统
 * @author: kfzx-fanqq
 * @date: 2018年11月15日 下午3:31:19
 * @version: V1.0
 * @description: 副食品维护复制
 */
public class OaSvFoodMaintainInfoCopyServ extends CommonServ{
	private static Log log = LogFactory.getLog(OaSvFoodMaintainInfoCopyServ.class);
	
	private static final String SPACE = " ";//空格
	
	/**
	 * @title: copy
	 * @description: 副食品维护列表的复制操作
	 * @param param
	 * @return 返回数据中包含副食品维护主表及食品列表、领取时间列表
	 * @throws
	 */
	public Bean copy(ParamBean param) {
		OutBean result = new OutBean();
		try {
			String oldId = param.getStr("MAINTAIN_ID");
			Bean main = ServDao.find(param.getServId(), oldId);
			main.set("MAINTAIN_ID", GenUtil.getAutoIdNumner("OSFMI", "OA_SV_FOOD_MAINTAIN_INFO_SEQ", "NUM"));
			main.set("ORDER_START_DATETIME", main.getStr("ORDER_START_DATE") + SPACE + main.getStr("ORDER_START_TIME"));
			main.set("ORDER_END_DATETIME", main.getStr("ORDER_END_DATE") + SPACE + main.getStr("ORDER_END_TIME"));
			
			main.set("FOOD_LIST", queryFoodsByFK(oldId));
			main.set("TIME_LIST", queryReceiveTimeByFK(oldId));
			result.set("data", main);
			result.setOk();
			return result;
		} catch (Exception e) {
			log.error("复制异常" + e.getMessage());
			result.setError(e.getMessage());
			return result;
		}
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
	
	/**
	 * @title: receiveTimeConvert
	 * @description: 将数据库中分开的领取日期和时间字段拼接为完整的界面自定义日期时间字段
	 * @param outBean
	 * @return OutBean
	 * @throws
	 */
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
	
	/**
	 * 发布前的判断查询，根据主键查询
	 * @param param
	 * @return
	 */
	public Bean getById(ParamBean param) {
		String id = param.getStr("MAINTAIN_ID");
		Bean main = ServDao.find(param.getServId(), id);
		
		OutBean result = new OutBean();
		if (main != null) {
			result.set("data",main);
		}
		result.setOk();
		return result;
	}
	
	
	/**
	 * @title: copySave
	 * @description: 复制保存
	 * @param param
	 * @return Bean
	 * @throws
	 */
	public Bean copySave(ParamBean param) {
		OutBean result = new OutBean();
		try {
			Bean validateResult = validateData(param);
			if (!validateResult.getBoolean("FLAG")) {
				result.setError(validateResult.getStr("RESULT"));
				return result;
			}
			
			//事务开始
			Transaction.begin();
			try {
				Bean main = JsonUtils.toBean(param.getStr("main"));
				String ORDER_START_DATETIME = main.getStr("ORDER_START_DATETIME");
				String ORDER_END_DATETIME = main.getStr("ORDER_END_DATETIME");
				main.set("ORDER_START_DATE", ORDER_START_DATETIME.split(SPACE)[0]);
				main.set("ORDER_START_TIME", ORDER_START_DATETIME.split(SPACE)[1]);
				main.set("ORDER_END_DATE", ORDER_END_DATETIME.split(SPACE)[0]);
				main.set("ORDER_END_TIME", ORDER_END_DATETIME.split(SPACE)[1]);
				
				SqlExecutor sqlExecutor = Transaction.getExecutor();
				
				String maintainStatus = main.getStr("MAINTAIN_STATUS");
				String servId = "OA_SV_FOOD_MAINTAIN_INFO_COPY";
				String pkCode = main.getStr("MAINTAIN_ID");
				
				String addFlag = param.getStr("addFlag");
				/*
				 * addFlag为前端传过来的标识符，用来区分是新增还是修改操作
				 * addFlag:1：执行新增，对应前端界面操作为点击‘保存’或直接点击‘发布’；0：执行修改，对应前端界面操作为点击‘保存’之后的点击‘发布’
				 */
				if (StringUtils.isNotEmpty(addFlag) && "0".equals(addFlag)) {
					//addFlag为0，执行修改
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
					
					sqlExecutor.execute("DELETE FROM OA_SV_FOOD_MAINTAIN_DET WHERE MAINTAIN_ID='" + main.getStr("MAINTAIN_ID") + "'");
					if (StringUtils.isNotEmpty(param.getStr("FOOD_LIST"))) {
						List<Bean> foods = JsonUtils.toBeanList(param.getStr("FOOD_LIST"));
						for (Bean bean : foods) {
							if (StringUtils.isEmpty(bean.getStr("FOOD_ID")) || !bean.getStr("FOOD_ID").startsWith("OSFMD")) {
								bean.set("FOOD_ID", GenUtil.getAutoIdNumner("OSFMD", "OA_SV_FOOD_MAINTAIN_DET_SEQ", "NUM"));
							}
						}
						
						ServDao.creates("OA_SV_FOOD_MAINTAIN_DET", foods);
					}
					
					sqlExecutor.execute("DELETE FROM OA_SV_FOOD_RECEIVE_INFO WHERE MAINTAIN_ID='" + main.getStr("MAINTAIN_ID") + "'");
					if (StringUtils.isNotEmpty(param.getStr("TIME_LIST"))) {
						List<Bean> timeList = JsonUtils.toBeanList(param.getStr("TIME_LIST"));
						for (Bean bean : timeList) {
							if (StringUtils.isEmpty(bean.getStr("OBTAIN_TIME_ID")) || !bean.getStr("OBTAIN_TIME_ID").startsWith("OSFRI")) {
								bean.set("OBTAIN_TIME_ID", GenUtil.getAutoIdNumner("OSFRI", "OA_SV_FOOD_RECEIVE_INFO_SEQ", "NUM"));
							}
							bean.set("OBTAIN_START_DATE", bean.getStr("OBTAIN_START_DATETIME").split(SPACE)[0]);
							bean.set("OBTAIN_START_TIME", bean.getStr("OBTAIN_START_DATETIME").split(SPACE)[1]);
							bean.set("OBTAIN_END_DATE", bean.getStr("OBTAIN_END_DATETIME").split(SPACE)[0]);
							bean.set("OBTAIN_END_TIME", bean.getStr("OBTAIN_END_DATETIME").split(SPACE)[1]);
							bean.set("SELECTED_NUMBER", 0);
						}
						
						ServDao.creates("OA_SV_FOOD_RECEIVE_INFO", timeList);
					}
					
					if ("1".equals(maintainStatus)) {
						//MAINTAIN_STATUS为1时，从公共待办表中删除关联记录
						deleteTodo(sqlExecutor, servId, pkCode);
					}
				} else {
					//addFlag为1，执行新增
					ServDao.create("OA_SV_FOOD_MAINTAIN_INFO", main);
					
					if (StringUtils.isNotEmpty(param.getStr("FOOD_LIST"))) {
						List<Bean> foods = JsonUtils.toBeanList(param.getStr("FOOD_LIST"));
						for (Bean bean : foods) {
							bean.set("FOOD_ID", GenUtil.getAutoIdNumner("OSFMD", "OA_SV_FOOD_MAINTAIN_DET_SEQ", "NUM"));
						}
						
						ServDao.creates("OA_SV_FOOD_MAINTAIN_DET", foods);
					}
					
					if (StringUtils.isNotEmpty(param.getStr("TIME_LIST"))) {
						List<Bean> timeList = JsonUtils.toBeanList(param.getStr("TIME_LIST"));
						for (Bean bean : timeList) {
							bean.set("OBTAIN_TIME_ID", GenUtil.getAutoIdNumner("OSFRI", "OA_SV_FOOD_RECEIVE_INFO_SEQ", "NUM"));
							bean.set("OBTAIN_START_DATE", bean.getStr("OBTAIN_START_DATETIME").split(SPACE)[0]);
							bean.set("OBTAIN_START_TIME", bean.getStr("OBTAIN_START_DATETIME").split(SPACE)[1]);
							bean.set("OBTAIN_END_DATE", bean.getStr("OBTAIN_END_DATETIME").split(SPACE)[0]);
							bean.set("OBTAIN_END_TIME", bean.getStr("OBTAIN_END_DATETIME").split(SPACE)[1]);
							bean.set("SELECTED_NUMBER", 0);
						}
						
						ServDao.creates("OA_SV_FOOD_RECEIVE_INFO", timeList);
					}
					
					if ("0".equals(maintainStatus)) {
						//MAINTAIN_STATUS为0时，往公共待办表中增加一条记录
						saveTodo(servId, main.getStr("MAINTAIN_TITLE"), pkCode);
					}
				}
				
				//事务正常提交
				Transaction.commit();
				
				result.set("FOOD_LIST", queryFoodsByFK(main.getStr("MAINTAIN_ID")));
				result.set("TIME_LIST", queryReceiveTimeByFK(main.getStr("MAINTAIN_ID")));
				result.setOk("复制成功！");
			} catch (Exception e) {
				//事务异常回滚
				Transaction.rollback();
				throw e;
			}
			//事务结束
			Transaction.end();
		} catch (Exception e) {
			log.error("复制保存异常" + e.getMessage());
			result.setError("复制失败！");
		}
		return result;
	}
	
	/**
	 * @title: validateData
	 * @description: 业务数据校验
	 * @param param
	 * @return Bean
	 * @throws
	 */
	private Bean validateData(ParamBean param) {
		Bean main = JsonUtils.toBean(param.getStr("main"));
		if (StringUtils.isEmpty(main.getStr("MAINTAIN_TITLE"))) {
			return generateResult(false, "请输入标题！");
		}
		if (StringUtils.isEmpty(main.getStr("ORDER_START_DATETIME"))) {
			return generateResult(false, "请输入预订开始时间！");
		}
		if (StringUtils.isEmpty(main.getStr("ORDER_END_DATETIME"))) {
			return generateResult(false, "请输入预订结束时间！");
		}
		
		String foodsStr = param.getStr("FOOD_LIST");
		if (StringUtils.isEmpty(foodsStr)) {
			return generateResult(false, "请输入食品列表中的信息！");
		}
		String timesStr = param.getStr("TIME_LIST");
		if (StringUtils.isEmpty(timesStr)) {
			return generateResult(false, "请输入领取时间列表中的信息！");
		}
		
		List<Bean> foods = JsonUtils.toBeanList(foodsStr);
		for (Bean bean : foods) {
			if (StringUtils.isEmpty(bean.getStr("FOOD_NAME"))) {
				return generateResult(false, "请输入食品名称！");
			}
			if (StringUtils.isEmpty(bean.getStr("FOOD_TYPE"))) {
				return generateResult(false, "请输入食品类型！");
			}
			if (StringUtils.isEmpty(bean.getStr("FOOD_STOCK"))) {
				return generateResult(false, "请输入库存数量！");
			}
//			if (!match("^[1-9][0-9]*$", bean.getStr("FOOD_STOCK"))) {
//				return generateResult(false, "请输入正整数格式的库存数量！");
//			}
			if (StringUtils.isEmpty(bean.getStr("FOOD_LIMIT_NUMBER"))) {
				return generateResult(false, "请输入限购数量！");
			}
//			if (!match("^[1-9][0-9]*$", bean.getStr("FOOD_LIMIT_NUMBER"))) {
//				return generateResult(false, "请输入正整数格式的限购数量！");
//			}
			if (StringUtils.isEmpty(bean.getStr("FOOD_PRICE"))) {
				return generateResult(false, "请输入价格！");
			}
//			if (!match("^[1-9][0-9]*(.\\d{0,1})?$", bean.getStr("FOOD_PRICE"))) {
//				return generateResult(false, "请输入正确格式的价格！");
//			}
		}
		
		List<Bean> timeList = JsonUtils.toBeanList(timesStr);
		for (Bean bean : timeList) {
			if (StringUtils.isEmpty(bean.getStr("OBTAIN_START_DATETIME"))) {
				return generateResult(false, "请输入领取开始时间！");
			}
			if (StringUtils.isEmpty(bean.getStr("OBTAIN_END_DATETIME"))) {
				return generateResult(false, "请输入领取结束时间！");
			}
			if (StringUtils.isEmpty(bean.getStr("OPTIONAL_NUMBER"))) {
				return generateResult(false, "请输入最大领取人数！");
			}
//			if (!match("^[1-9][0-9]*$", bean.getStr("OPTIONAL_NUMBER"))) {
//				return generateResult(false, "请输入正整数格式的最大领取人数！");
//			}
		}
		
		return generateResult(true, "ok");
	}

	/**
	 * @title: generateResult
	 * @description: 生成校验结果bean
	 * @param flag
	 * @param msg
	 * @return Bean
	 * @throws
	 */
	private Bean generateResult(boolean flag, String msg) {
		Bean bean = new Bean();
		bean.set("FLAG", flag);
		bean.set("RESULT", msg);
		return bean;
	}

	/**
	 * @title: match
	 * @description: 正则校验
	 * @param regex
	 * @param str
	 * @return boolean
	 * @throws
	 */
	private static boolean match(String regex, String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	/**
	 * @title: saveTodo
	 * @description: 保存公共待办
	 * @param servId
	 * @param title
	 * @param pkCode
	 * @throws Exception
	 * @return void
	 * @throws
	 */
	private void saveTodo(String servId, String title, String pkCode) throws Exception {
		UserBean user = Context.getUserBean();
		String time = DateUtils.getDatetime();
		String nodeId = Lang.getUUID(); 
		
		Bean nodeData = new Bean();
		nodeData.set("NI_ID", nodeId); //节点主键 此处填写待办表的TODO_OBJECT_ID2的值（必须相等）
		nodeData.set("NODE_NAME", "name"); //节点名称
		nodeData.set("DONE_DESC", "正常结束");
		nodeData.set("S_MTIME", time); //修改时间
		nodeData.set("NODE_BTIME", time); // 节点开始时间
		nodeData.set("DONE_USER_ID", user.getCode()); // 办理人员ID
		nodeData.set("DONE_USER_NAME", user.getName()); // 办理人员名称
		nodeData.set("DONE_DEPT_IDS", user.getDeptCode()); // 办理人员部门ID
		nodeData.set("DONE_DEPT_NAMES", user.getDeptName()); // 办理人员部门名称
		nodeData.set("DONE_TYPE", 1); // 1是自动正常停止
		nodeData.set("NODE_IF_RUNNING", 2); // 2是代表节点停止
		nodeData.set("TO_USER_ID", user.getCode()); // 处理人员ID
		nodeData.set("TO_USER_NAME", user.getName()); // 处理人员名称
		nodeData.set("OPEN_TIME", ""); //打开时间
		ServDao.save("SY_WFE_NODE_INST", nodeData);
		
		TodoBean dataBean = new TodoBean();
		dataBean.setTitle(title);
		dataBean.setSender(user.getCode());
		dataBean.setOwner(user.getCode());
		dataBean.setCode(servId);
		dataBean.setCodeName("副食品维护");
		dataBean.setObjectId1(pkCode);
		dataBean.setCatalog(1);// 待办的类型，1,办件，2，阅件
		dataBean.setObjectId2(nodeId); // TODO_OBJECT_ID2 中存 分发的ID
		dataBean.setUrl("OA_SV_FOOD_MAINTAIN_INFO_DB_UPDATE.byid.do?data={_PK_:"+pkCode+"}");
		TodoUtils.insert(dataBean);
	}
	
	/**
	 * @title: deleteTodo
	 * @description: 删除公共待办
	 * @param sqlExecutor
	 * @param servId
	 * @param pkCode
	 * @throws Exception
	 * @return void
	 * @throws
	 */
	private void deleteTodo(SqlExecutor sqlExecutor, String servId, String pkCode) throws Exception {
		Bean todoBean = sqlExecutor.queryOne("SELECT TODO_OBJECT_ID2 FROM SY_COMM_TODO WHERE SERV_ID='" + servId
				+ "' AND TODO_OBJECT_ID1='" + pkCode + "'");
		if (todoBean != null && StringUtils.isNotEmpty(todoBean.getStr("TODO_OBJECT_ID2"))) {
			sqlExecutor.execute("DELETE FROM SY_WFE_NODE_INST WHERE NI_ID='" + todoBean.getStr("TODO_OBJECT_ID2") + "'");
			sqlExecutor.execute("DELETE FROM SY_COMM_TODO WHERE SERV_ID='" + servId + "' AND TODO_OBJECT_ID1='" + pkCode + "'");
		}
	}
}
