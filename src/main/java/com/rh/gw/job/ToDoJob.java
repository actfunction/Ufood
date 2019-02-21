package com.rh.gw.job;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.rh.core.base.Bean;

public class ToDoJob implements Job {
	/** 记录日志 */
	private static Log log = LogFactory.getLog(ToDoJob.class);

	/**
	 * 定时查询
	 *
	 * @param context
	 * @throws JobExecutionException
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
//		// 首先获取token
//		String token = "";
//		token = GwToDoUtils.getToken(Context.getSyConf("toDoDomain", System.getProperty("java.io.tmpdir")),
//				Context.getSyConf("toDoFalg", System.getProperty("java.io.tmpdir")),
//				Context.getSyConf("toDosecrteKey", System.getProperty("java.io.tmpdir")));
//		if (null == token || !"".equals(token)) {
//			return;
//		} else {
//			StringBuilder strSql = new StringBuilder();
//			strSql.append("SELECT * FROM OA_GW_INTERFACE_GATEWAY_LOG WHERE SUCCESS_FLAG = 'F'");
//			List<Bean> doorList = Transaction.getExecutor().query(strSql.toString());
//			for (int i = 0; i < doorList.size(); i++) {
//				String type = doorList.get(i).getStr("SEND_TYPE");
//				if ("1".equals(type)) {
//					String message = doorList.get(i).getStr("SEND_MESSAGE");
//					Bean measageBean = JsonUtils.toBean(message);
//					//将bean中的数据转换成map
//					Map<String,Object> paramMap=new HashMap<String,Object>();
//					paramMap=bean2Map(measageBean);
//					// 创建待办
//					 GwToDoUtils.createToDao(Context.getSyConf("XASPAPPTOKEN",
//					 System.getProperty("java.io.tmpdir")), paramMap, token);
//					ServDao.update(Context.getSyConf("doorLogService", System.getProperty("java.io.tmpdir")),
//							doorList.get(i).set("SUCCESS_FLAG", "T"));
//				} else if ("2".equals(type)) {
//					// 删除待办
//					String oid = doorList.get(i).getStr("LOG_OID");
//					String status = GwToDoUtils.delToDo(oid);
//					if ("200".equals(status)) {
//						ServDao.update(Context.getSyConf("doorLogService", System.getProperty("java.io.tmpdir")),
//								doorList.get(i).set("SUCCESS_FLAG", "T"));
//					}
//				} else if ("3".equals(type)) {
//					String oid = doorList.get(i).getStr("LOG_OID");
//					Bean measageBean = JsonUtils.toBean(doorList.get(i).getStr("SEND_MESSAGE"));
//					Map<String,Object> paramMap=new HashMap<String,Object>();
//					paramMap=bean2Map(measageBean);
//					// 修改待办
//					 GwToDoUtils.updateToDao(oid, paramMap, token);
//					ServDao.update(Context.getSyConf("doorLogService", System.getProperty("java.io.tmpdir")),
//					doorList.get(i).set("SUCCESS_FLAG", "T"));
//				}
//			}
//		}
	}
	
	private Map<String,Object> bean2Map(Bean saveGetBean){
		Map<String,Object> paramMap=new HashMap<String,Object>();
		paramMap.put("DOOR_ID", saveGetBean.getStr("DOOR_ID")); // VARCHAR2(40),
		// --主键
		paramMap.put("TODO_ID", saveGetBean.getStr("TODO_ID")); // VARCHAR2(40),
		// --主键 待办ID
		paramMap.put("TODO_STATUS", saveGetBean.getStr("TODO_STATUS")); // VARCHAR2(40), --我们的 待办状态
		paramMap.put("DOOR_OID", saveGetBean.getStr("DOOR_OID")); // VARCHAR2(40), --门户 返回的 待办 门户主键
		// id
		paramMap.put("USER_NUMBER", saveGetBean.getStr("USER_NUMBER")); // VARCHAR2(40),
		// --个人账号
		// saveGetBean.set("SEND_COUNT", obj); //NUMBER(4), --发送次数
		paramMap.put("DOOR_CREATOR", saveGetBean.getStr("DOOR_CREATOR")); // VARCHAR2(40),
		// --待办创建者(传用户登录名) ,
		paramMap.put("DOOR_EXECUTOR", saveGetBean.getStr("DOOR_EXECUTOR")); // VARCHAR2(40),
		// --待办执行者(传用户登录名) ,
		paramMap.put("TODO_URL", saveGetBean.getStr("TODO_URL"));
		// saveGetBean.set("DOOR_NAME", obj); //VARCHAR2(40), ---待办名称 ,
		paramMap.put("DOOR_NOTE", saveGetBean.getStr("DOOR_NOTE")); // VARCHAR2(2000), ---待办备注 ,
		paramMap.put("DOOR_READA", saveGetBean.getStr("DOOR_READA")); // VARCHAR2(40), ---待办已读或未读 =
		// ['NO', 'YES'],
		// saveGetBean.set("DOOR_REMINDER", obj); //VARCHAR2(40), ---待办提醒设置
		// ,
		paramMap.put("DOOR_SOURCEAPP", saveGetBean.getStr("DOOR_SOURCEAPP")); // VARCHAR2(40),
		// ---待办所属应用系统 ,
		paramMap.put("DOOR_STATUS", saveGetBean.getStr("DOOR_STATUS")); // VARCHAR2(40),
		// ---待办状态(不填默认未开始) =
		// ['PREPARE', 'START',
		// 'FINISH', 'CANCEL',
		// 'TERMINATE'],
		paramMap.put("DOOR_TYPE", saveGetBean.getStr("DOOR_TYPE")); // VARCHAR2(40), ---待办类型 =
		return paramMap;
	}
}
