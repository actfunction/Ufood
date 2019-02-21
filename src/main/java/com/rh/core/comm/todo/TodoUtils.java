/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.comm.todo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.db.RowHandler;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.CacheMgr;
import com.rh.core.comm.entity.EntityMgr;
import com.rh.core.comm.remind.RemindMgr;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.Strings;
import com.rh.core.util.lang.Assert;
import com.rh.tc.api.client.bean.Todo;
import com.rh.tc.api.client.service.todo.ITodoAsyncClientService;
import com.rh.tc.api.client.service.todo.ITodoClientService;
import com.rh.tc.api.client.service.todo.TodoAsyncServiceFactory;
import com.rh.tc.api.client.service.todo.TodoServiceFactory;
//import com.rh.tc.api.client.service.todo.impl.AsyncHandler;

import com.rh.gw.util.GwToDoUtils;

/***
 * 处理待办事务、已办事务的统一处理类
 * 
 * @author wanglong
 * @version $Id$
 */
public class TodoUtils extends Object {
	/** log */
	private static Log log = LogFactory.getLog(TodoUtils.class);
	/** 草稿前缀 **/
	public static final String CAO_GAO = "[草稿]";
	/** 待办字段 **/
	public static class ToDoItem {
		/** 待办标题 **/
		public static final String TODO_TITLE = "TODO_TITLE";
		/** 待办编码 **/
		public static final String TODO_CODE = "TODO_CODE";
		/** 待办名称 **/
		public static final String TODO_CODE_NAME = "TODO_CODE_NAME";
		/** 待办URL **/
		public static final String TODO_URL = "TODO_URL";
		/** 发送人ID **/
		public static final String SEND_USER_CODE = "SEND_USER_CODE";
		/** 办理人ID **/
		public static final String OWNER_CODE = "OWNER_CODE";
		/** 缓急 **/
		public static final String S_EMERGENCY = "S_EMERGENCY";
		/** 分类：待办 1/待阅 2/消息 3 **/
		public static final String TODO_CATALOG = "TODO_CATALOG";
		/** 与SERV_ID对应的主数据ID **/
		public static final String TODO_OBJECT_ID1 = "TODO_OBJECT_ID1";
		/** 辅数据ID，用于区分待办 **/
		public static final String TODO_OBJECT_ID2 = "TODO_OBJECT_ID2";
		/** 办理期限 **/
		public static final String TODO_DEADLINE1 = "TODO_DEADLINE1";
		/** 办理期限 **/
		public static final String TODO_DEADLINE2 = "TODO_DEADLINE2";
		/** 代理状态，1：可代理；2：不可代理 **/
		public static final String TODO_BENCH_FLAG = "TODO_BENCH_FLAG";
		/** 待办提示消息内容 **/
		public static final String TODO_CONTENT = "TODO_CONTENT";
		/** 待办的办理提示，如：请批示，请核稿等 **/
		public static final String TODO_OPERATION = "TODO_OPERATION";
		/** 服务ID，访问URL时卡片页面对应的服务ID **/
		public static final String SERV_ID = "SERV_ID";
		/** 文件编号 **/
		public static final String SERV_DATA_CODE = "SERV_DATA_CODE";
		/** 待办来自哪种业务：如wf工作流，remind消息提醒 **/
		public static final String TODO_FROM = "TODO_FROM";

		/** 起草人ID **/
		public static final String DRAFT_USER = "DRAFT_USER";
		/** 起草人姓名 **/
		public static final String DRAFT_UNAME = "DRAFT_UNAME";
		/** 起草人处室ID **/
		public static final String DRAFT_DEPT = "DRAFT_DEPT";
		/** 起草人处室姓名 **/
		public static final String DRAFT_DNAME = "DRAFT_DNAME";
		/** 起草时间 **/
		public static final String DRAFT_TIME = "DRAFT_TIME";
		/** 上一环节名称 **/
		public static final String PRE_OPT_NAME = "PRE_OPT_NAME";

	};

	/** 插入操作 */
	public static final String ACTINSET = "insert";

	/** 更新 */
	public static final String ACTUPDATE = "update";

	/** 删除 */
	public static final String ACTDELETE = "delete";

	/** 真删除 */
	public static final String ACTDELETETRUE = "deleteTrue";

	/** 送用户 */
	public static final int SEND_USER = 1;

	/** 送角色 */
	public static final int SEND_ROLE = 2;

	/** 待办 */
	public static final int TODO_CATLOG_BAN = 1;

	/** 待阅 */
	public static final int TODO_CATLOG_YUE = 2;

	/** 消息提醒 */
	public static final int TODO_CATLOG_MSG = 3;

	/** 缓存类型:用户待办数 */
	private static final String CACHE_TODO = "_CACHE_TODO";

	/**
	 * 待办提醒方式代码
	 */
	public static final String REMIND_TYPE_TODO = "TODO";

	/**
	 * 向角色发送待办
	 * 
	 * @param todoBean
	 *            待办Bean
	 * @param deptCode
	 *            部门编码
	 * @param roleCode
	 *            角色编码
	 */
	public static void insertToDeptRole(TodoBean todoBean, String deptCode, String roleCode) {
		List<UserBean> userBeanList = UserMgr.getUsersByDept(deptCode, roleCode);

		for (UserBean userBean : userBeanList) {
			todoBean.setOwner(userBean.getCode());
			insert(todoBean);
		}
	}

	/**
	 * 新增一条待办
	 * 
	 * @param dataBean
	 *            参数对象
	 * @return resBean 结果Bean
	 */
	public static Bean insert(TodoBean dataBean) {
		// 参数判断
		if (dataBean.isEmpty("SEND_USER_CODE")) { // 发待办用户
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", ACTINSET, "SEND_USER_CODE"));
		}
		if (dataBean.isEmpty("TODO_TITLE")) { // 待办标题
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", ACTINSET, "TODO_TITLE"));
		} else {
			dataBean.set("TODO_TITLE", Strings.escapeHtml(dataBean.getStr("TODO_TITLE")));
		}
		if (dataBean.isNotEmpty("TODO_CONTENT")) { // 待办内容
			dataBean.set("TODO_CONTENT", Strings.escapeHtml(dataBean.getStr("TODO_CONTENT")));
		}
		if (dataBean.isEmpty("OWNER_CODE")) { // 待办用户[角色]
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", "insert", "OWNER_CODE"));
		}
		if (dataBean.isEmpty("TODO_CODE")) { // 待办编码 服务ID
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", ACTINSET, "TODO_CODE"));
		} else if (dataBean.isEmpty("TODO_CODE_NAME")) {
			dataBean.set("TODO_CODE_NAME", ServUtils.getServDef(dataBean.getStr("TODO_CODE")).getStr("SERV_NAME"));
		}

		if (dataBean.isEmpty("TODO_CODE_NAME")) { // 待办编码名称 服务名称
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", ACTINSET, "TODO_CODE_NAME"));
		}
		// 如果没给待办类型，默认类别为待办
		if (dataBean.getInt("TODO_CATALOG") == 0) {
			dataBean.set("TODO_CATALOG", TODO_CATLOG_BAN);
		}

		String url = dataBean.getStr("TODO_URL");

		if (!url.startsWith("http://")) {
			if (dataBean.isEmpty("TODO_OBJECT_ID1")) { // 对象ID1 DATA Id
				throw new TipException(Context.getSyMsg("SY_TODO_ERROR", ACTINSET, "TODO_OBJECT_ID1"));
			}
		}

		// 兼容老代码：如果服务ID为NULL，则以TODO_CODE为服务ID
		if (dataBean.isEmpty("SERV_ID")) {
			dataBean.set("SERV_ID", dataBean.getStr("TODO_CODE"));
		}
		ServDefBean servDef = ServUtils.getServDef(dataBean.getStr("SERV_ID"));
		if (servDef != null) {
			dataBean.set("TODO_TYPE", servDef.getTodoType()); // 判断
		}
		// 添加发送时间
		dataBean.set("TODO_SEND_TIME", DateUtils.getDatetime());

		if (dataBean.isEmpty("TODO_BENCH_FLAG")) {
			dataBean.set("TODO_BENCH_FLAG", Constant.YES);
		}

		// 通过服务保存数据，支持消息监听和服务扩展
		ParamBean queryBean = new ParamBean(dataBean);
		queryBean.setServId(ServMgr.SY_COMM_TODO);
		queryBean.setAct(ServMgr.ACT_SAVE);
		Bean resBean = ServMgr.act(queryBean);

		CacheMgr.getInstance().remove(dataBean.getStr("OWNER_CODE"), CACHE_TODO); // 清除缓存

		// 判断如果是由提醒触发的，待办发完后不再发提醒，防止嵌套循环
		if (dataBean.isEmpty("remindFlag")) {
			sendTodoMsg(dataBean);
		}
		UserBean doUser = UserMgr.getUser(dataBean.getStr("OWNER_CODE"));

		// 复制实例数据
		EntityMgr.copyEntity(dataBean.getStr("TODO_OBJECT_ID1"), doUser.getODeptCode());
		// 创建待办
		remoteTodo(resBean);

		return resBean;
	}

	/**
	 * 发送待办提醒
	 * 
	 * @param todoBean
	 *            待办数据
	 */
	private static void sendTodoMsg(Bean todoBean) {
		// 自己给自己发送的待办没有提醒
		if (todoBean.getStr(ToDoItem.OWNER_CODE).equals(todoBean.getStr(ToDoItem.SEND_USER_CODE))) {
			return;
		}

		String remindType = TodoRemindTypeMgr.getUserRemindType(todoBean.getStr("OWNER_CODE"));

		if (remindType.indexOf("TODO,") >= 0) { // 取消待办对待办的提醒
			remindType = remindType.replaceAll("TODO,", "");
		} else if (remindType.equals("TODO")) {
			return;
		}

		if (StringUtils.isNotBlank(remindType)) {
			Bean remindBean = new Bean();
			StringBuilder title = new StringBuilder();
			title.append("您有一项新待办事务：").append(todoBean.getStr(ToDoItem.TODO_TITLE));
			title.append("，请及时办理！");
			String msg = title.toString();

			String remoteUrl = createTodoUrl(todoBean, Context.getHttpUrl(), "");

			// if (todoBean.isNotEmpty("TODO_URL")) {
			// msg += " [<a href='" + Context.getHttpUrl() + "'>详情" + "</a>]";
			// }

			remindBean.set("REM_TITLE", todoBean.getStr(ToDoItem.TODO_TITLE));

			remindBean.set("REM_CONTENT", msg);
			// remindBean.set("REM_URL", todoBean.getStr(ToDoItem.TODO_URL));
			remindBean.set("S_USER", todoBean.getStr(ToDoItem.SEND_USER_CODE));
			remindBean.set("EXECUTE_TIME", "");
			remindBean.set("TYPE", remindType);
			remindBean.set("S_EMGRENCY", todoBean.getInt(ToDoItem.S_EMERGENCY));
			remindBean.set("SERV_ID", todoBean.getStr(ToDoItem.SERV_ID));
			remindBean.set("SERV_SRC_ID", todoBean.getStr(ToDoItem.SERV_ID));
			remindBean.set("DATA_ID", todoBean.getStr(ToDoItem.TODO_OBJECT_ID2));
			/*---------------添加外部待办URL---------begin----hdy--2013-6-26 11:37--------*/
			// 添加外部URL
			remindBean.set("REMOTE_URL", remoteUrl);
			/*---------------添加外部待办URL---------end--------------*/
			RemindMgr.add(remindBean, todoBean.getStr(ToDoItem.OWNER_CODE));

			/**
			 * ImMgr.getIm().sendNotify(todoBean.getStr("OWNER_CODE"), "来自【" +
			 * Context.getUserBean().getName() + "】的消息", msg);
			 **/
		}
	}

	/**
	 * 更新一条待办
	 * 
	 * @param dataBean
	 *            参数对象
	 * @return resBean 结果Bean
	 */
	public static Bean updateById(Bean dataBean) {
		if (dataBean.getId().length() == 0) { // 主键信息
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", "updateById", "setId()"));
		}
		ServDao.update(ServMgr.SY_COMM_TODO, dataBean);
		return dataBean;
	}

	/**
	 * 根据条件，更新多条待办
	 * 
	 * @param setBean
	 * @param whereBean
	 *            参数对象
	 * @return resInt 成功数
	 */
	public static int updates(Bean setBean, Bean whereBean) {
		if (setBean.size() == 0) { // 更改字段信息
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", "updates", "setBean为空"));
		}
		if (whereBean.size() == 0) { // 条件信息
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", "updates", "whereBean为空"));
		}
		int res = ServDao.updates(ServMgr.SY_COMM_TODO, setBean, whereBean);

		// if (res > 0) {
		// remoteUpdates(setBean, whereBean);
		// }

		return res;
	}

	/**
	 * 删除一条待办，同时将此待办转成一条已办
	 * 
	 * @param id
	 *            待办主键
	 */
	public static void endById(String id) {
		if (id.isEmpty()) { // 主键信息
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", "delete", "setId()"));
		}
		// 生成一条已办
		Bean toDoBean = ServDao.find(ServMgr.SY_COMM_TODO, id);
		if (toDoBean != null) {
			// 删除待办数据
			ParamBean param = new ParamBean(ServMgr.SY_COMM_TODO, ServMgr.ACT_DELETE);
			List<Bean> delList = new ArrayList<Bean>(1);
			delList.add(toDoBean);
			param.setDeleteDatas(delList);
			ServMgr.act(param);
			// 添加入已经办理
			toDoBean.set("TODO_FINISH_TIME", DateUtils.getDatetime()); // 添加完成时间
			toDoBean.set("TODO_TITLE", toDoBean.getStr("TODO_TITLE").replace(TodoUtils.CAO_GAO, "")); // 删除草稿两字
			ServDao.create(ServMgr.SY_COMM_TODO_HIS, toDoBean);
			// 清除缓存
			CacheMgr.getInstance().remove(toDoBean.getStr("OWNER_CODE"), CACHE_TODO); // 清除缓存

			remoteTodo2Done(delList);
		}
	}

	/**
	 * 删除多条待办，同时将此待办转成多条已办
	 * 
	 * @param whereBean
	 *            参数对象
	 * @return resInt 成功数
	 */
	public static long ends(Bean whereBean) {
		if (whereBean.size() == 0) { // 为空值判断
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", "ends", "whereBean为空"));
		}
		long resInt = 0;
		List<Bean> toDoList = ServDao.finds(ServMgr.SY_COMM_TODO, whereBean);

		for (Bean toDoBean : toDoList) {
			// 添加完成时间
			toDoBean.set("TODO_FINISH_TIME", DateUtils.getDatetime());
			CacheMgr.getInstance().remove(toDoBean.getStr("OWNER_CODE"), CACHE_TODO); // 清除缓存
		}

		if (toDoList.size() > 0) {
			// 生成多条已办
			int res = ServDao.creates(ServMgr.SY_COMM_TODO_HIS, toDoList);
			if (res > 0) {
				 remoteTodo2Done(toDoList);
				// 删除多条待办
				ParamBean queryBean = new ParamBean(whereBean);
				queryBean.setServId(ServMgr.SY_COMM_TODO);
				queryBean.setAct(ServMgr.ACT_DELETE);
				queryBean.setDeleteDatas(toDoList);

				OutBean outBean = ServMgr.act(queryBean);
				resInt = outBean.getCount();
			}
		}
		return resInt;
	}

	/**
	 * 办结指定用户的待办
	 * 
	 * @param userCode
	 *            指定用户ID
	 * @param objectID1
	 *            服务ID
	 * @param objectID2
	 *            被处理对象的ID。常用于保存分发ID。
	 * @return 成功办结的数量
	 */
	public static long endUserTodo(String userCode, String objectID1, String objectID2) {
		Bean whereBean = new Bean();
		whereBean.set("TODO_OBJECT_ID1", objectID1);
		whereBean.set("OWNER_CODE", userCode);
		whereBean.set("TODO_OBJECT_ID2", objectID2);

		return ends(whereBean);
	}

	/**
	 * 清除指定数据的所有待办。
	 * 
	 * @param objectID1
	 *            对象ID1
	 * @param objectID2
	 *            对象ID2
	 * @return 成功办结的数量
	 */
	public static long endAllUserTodo(String objectID1, String objectID2) {
		Bean whereBean = new Bean();
		whereBean.set("TODO_OBJECT_ID1", objectID1);
		whereBean.set("TODO_OBJECT_ID2", objectID2);

		return ends(whereBean);
	}

	/**
	 * 仅彻底删除一条待办
	 * 
	 * @param dataBean
	 *            参数对象
	 * @return resInt 成功数
	 * 
	 */
	public static long destroyById(Bean dataBean) {
		if (dataBean.getId().length() == 0) { // 主键信息
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", "delete", "setId()"));
		}
		// int resInt = ServDao.destroys(ServMgr.SY_COMM_TODO, dataBean);

		ParamBean queryBean = new ParamBean(dataBean);
		queryBean.setServId(ServMgr.SY_COMM_TODO);
		queryBean.setAct(ServMgr.ACT_FINDS);

		List<Bean> datas = ServMgr.act(queryBean).getDataList();

		// 通过服务操作数据
		queryBean.setServId(ServMgr.SY_COMM_TODO);
		queryBean.setAct(ServMgr.ACT_DELETE);
		queryBean.setDeleteDatas(datas);
		OutBean outBean = ServMgr.act(queryBean);
		long resInt = outBean.getCount();

		return resInt;
	}

	/**
	 * 仅彻底删除多条待办
	 * 
	 * @param whereBean
	 *            参数对象
	 * @return resInt 成功数
	 */
	public static long destroys(Bean whereBean) {
		if (whereBean.size() == 0) { // 为空值判断
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", "destroys", "whereBean为空"));
		}
		// int resInt = ServDao.destroys(ServMgr.SY_COMM_TODO, whereBean);
		// 通过服务操作数据
		ParamBean queryBean = new ParamBean(whereBean);
		queryBean.setServId(ServMgr.SY_COMM_TODO);
		queryBean.setAct(ServMgr.ACT_FINDS);

		List<Bean> datas = ServMgr.act(queryBean).getDataList();



		queryBean.setAct(ServMgr.ACT_DELETE);
		queryBean.setDeleteDatas(datas);

		OutBean outBean = ServMgr.act(queryBean);
		long resInt = outBean.getCount();

		if (resInt > 0) {
			remoteDelete(datas);
		}
		return resInt;
	}

	/**
	 * 彻底删除已办
	 * 
	 * @param whereBean
	 * @return
	 */
	public static long destorysHis(Bean whereBean) {
		if (whereBean.size() == 0) { // 为空值判断
			throw new TipException(Context.getSyMsg("SY_TODO_ERROR", "destroys", "whereBean为空"));
		}

		ParamBean queryBean = new ParamBean(whereBean);
		queryBean.setServId(ServMgr.SY_COMM_TODO_HIS);
		queryBean.setAct(ServMgr.ACT_FINDS);

		List<Bean> datas = ServMgr.act(queryBean).getDataList();

		queryBean.setAct(ServMgr.ACT_DELETE);
		queryBean.setDeleteDatas(datas);

		OutBean outBean = ServMgr.act(queryBean);
		long resInt = outBean.getCount();

		return resInt;
	}

	/**
	 * 彻底删除指定服务数据的所有待办，用于删除服务时。
	 * 
	 * @param servId
	 *            服务ID
	 * @param dataId
	 *            数据ID
	 * @return 删除数据数量
	 */
	public static long destroys(String servId, String dataId) {
		Assert.hasLength(servId);
		Assert.hasLength(dataId);
		SqlBean sql = new SqlBean();
		sql.and("SERV_ID", servId);
		sql.and("TODO_OBJECT_ID1", dataId);

		return destroys(sql);
	}

	/**
	 * 仅彻底删除多条待办
	 * 
	 * @param sqlBean
	 *            参数对象
	 * @return resInt 成功数
	 */
	private static long destroys(SqlBean sqlBean) {
		// int resInt = ServDao.destroys(ServMgr.SY_COMM_TODO, sqlBean);
		// 通过服务操作数据
		ParamBean queryBean = new ParamBean(sqlBean);
		queryBean.setServId(ServMgr.SY_COMM_TODO);
		queryBean.setAct(ServMgr.ACT_FINDS);

		List<Bean> datas = ServMgr.act(queryBean).getDataList();

		// 通过服务操作数据
		queryBean.setDeleteDatas(datas);
		queryBean.setAct(ServMgr.ACT_DELETE);
		OutBean outBean = ServMgr.act(queryBean);
		long resInt = outBean.getCount();

		// 彻底删除多条代办通过接口调用
		if (resInt > 0) {
			remoteDelete(datas);
		}

		return resInt;
	}

	/**
	 * 获取指定用户的待办数量
	 * 
	 * @param userCode
	 *            用户编码
	 * @param interfaceMenuIds
	 *            接口对应的菜单ID列表，通过接口单独获取
	 * @return 待办数量
	 */
	public static int getToDoCount(String userCode, String interfaceMenuIds) {
		return getToDoCountBean(userCode, interfaceMenuIds).getInt("TODO_COUNT");
	}

	/**
	 * 获取指定用户的待办数量
	 * 
	 * @param userCode
	 *            用户编码
	 * @param interfaceMenuIds
	 *            接口对应的菜单ID列表，通过接口单独获取
	 * @return 待办数量信息：包含总待办数
	 */
	@SuppressWarnings("unchecked")
	public static Bean getToDoCountBean(String userCode, String interfaceMenuIds) {
		Bean todo = (Bean) CacheMgr.getInstance().get(userCode, CACHE_TODO);
		if (todo == null) {
			final Bean todoBean = new Bean(); // 分类数量
			SqlBean sql = new SqlBean()
					.selects("TODO_TYPE,TODO_CODE_NAME,TODO_TITLE,SERV_DATA_CODE,TODO_SEND_TIME"
							+ ",SEND_USER_CODE,TODO_URL,"
							+ "(select count(b.TODO_ID) from SY_COMM_TODO b where a.TODO_TYPE=b.TODO_TYPE "
							+ "and a.OWNER_CODE=b.OWNER_CODE group by b.TODO_TYPE,b.OWNER_CODE) TODO_COUNT")
					.tables("SY_COMM_TODO a").and("OWNER_CODE", userCode).and("TODO_LAST", 1)
					.orders("TODO_SEND_TIME desc");
			ServDao.findsCall(ServMgr.SY_COMM_TODO, sql, new RowHandler() {
				public void handle(List<Bean> columns, Bean data) {
					int count = data.getInt("TODO_COUNT");
					int oldCount = todoBean.getInt("TODO_COUNT");
					todoBean.set("TODO_COUNT", count + oldCount); // 累加计算分类数量
					List<Bean> dataList = (List<Bean>) todoBean.get(Constant.RTN_DATA);
					if (dataList == null) {
						dataList = new ArrayList<Bean>();
						todoBean.set(Constant.RTN_DATA, dataList);
					}
					UserMgr.appendUserItemInfo("SEND_USER_CODE", data);
					dataList.add(data);
				}
			});
			CacheMgr.getInstance().set(userCode, todoBean, CACHE_TODO);
			todo = todoBean;
		}
		if (!interfaceMenuIds.isEmpty()) {
			String[] menuIds = interfaceMenuIds.split(Constant.SEPARATOR);
			Bean moreTodo = todo.copyOf();
			List<Bean> todoList = todo.getList(Constant.RTN_DATA);
			List<Bean> moreTodoList = new ArrayList<Bean>(todoList.size() + menuIds.length);
			moreTodoList.addAll(todoList);
			for (String mId : menuIds) {
				Bean menu = DictMgr.getItem("SY_COMM_MENU_USER", mId);
				if (menu != null) {
					try {
						String[] todoInterface = menu.getStr("ALERT").split(Constant.SEPARATOR);
						ITodoCount todoInst = (ITodoCount) Lang.createObject(ITodoCount.class, todoInterface[0]);
						Bean todoBean = todoInst.getTodo(JsonUtils.toBean(ServUtils.replaceSysVars(todoInterface[1])));
						moreTodoList.add(todoBean);
						// 累加计算分类数量
						moreTodo.set(menu.getStr("ID"), todoBean.getInt("TODO_COUNT"));
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
			moreTodo.set(Constant.RTN_DATA, moreTodoList);
			return moreTodo;
		} else {
			return todo;
		}
	}

	/**
	 * 
	 * @param userCode
	 *            用户编码
	 * @param start
	 *            从多少条开始
	 * @param count
	 *            取多少条
	 * @return 待办列表
	 */
	public static List<Bean> getUserTodos(String userCode, int start, int count) {
		StringBuilder strSql = new StringBuilder();
		strSql.append("SELECT todo_id, todo_code, todo_title, "
				+ "todo_code_name,todo_send_time, send_user_code,todo_object_id1,todo_url");
		strSql.append(" FROM cm_todo where owner_code = '" + userCode + "'");
		strSql.append(" ORDER BY s_emergency DESC, todo_send_time DESC");

		List<Bean> todoList = Transaction.getExecutor().query(strSql.toString(), start, count);

		return todoList;
	}

	/**
	 * 
	 * @param todoBean
	 *            待办数据Bean
	 * @param hostAddr
	 *            主机地址
	 * @param mainUserCode
	 *            用户编号
	 * @return 访问待办的完整URL，可用于门户、RTX、邮件系统。
	 */
	public static String createTodoUrl(Bean todoBean, String hostAddr, String mainUserCode) {
		// 处理本人和委托的兼岗问题
		String todoUser = "";
		String ownerCode = todoBean.getStr("OWNER_CODE");
		boolean agtentFlag = todoBean.getBoolean("_agtFLag");
		// 1本人自己的、2本人代理的、3兼岗自己的
		if (!mainUserCode.isEmpty() && !mainUserCode.equals(ownerCode)) {
			if (!agtentFlag) { // 兼岗自己的
				todoUser = ownerCode;
			}
		}
		if (StringUtils.isEmpty(hostAddr)) {
			hostAddr = Context.getHttpUrl();
		}
		StringBuilder result = new StringBuilder();
		if (todoBean.getStr("TODO_URL").indexOf(".showDialog.do") >= 0) {
			result.append("{'_PK_':'").append(todoBean.getStr("TODO_ID"));
			result.append("','servId':'").append("SY_COMM_TODO");
			result.append("'}");
			String url = "";
			try {
				url = hostAddr + "/sy/comm/page/confirm.jsp?param="
						+ Hex.encodeHexString(result.toString().getBytes("UTF-8"));
				if (!todoUser.isEmpty()) {
					url += "&TODO_USER=" + Hex.encodeHexString(todoUser.getBytes("UTF-8"));
				}
				return url;
			} catch (UnsupportedEncodingException e) {
				throw new TipException(e.getMessage() + ": " + url);
			}
		} else {
			String todoUrl = todoBean.getStr("TODO_URL");
			todoUrl = todoUrl.replaceAll("\\.byid\\.do", "\\.card\\.do");
			if (agtentFlag) { // 针对代他人办理情况
				todoUrl = todoUrl.substring(0, todoUrl.length() - 1);
				todoUrl = todoUrl + ",_AGENT_USER_:" + todoBean.getStr("OWNER_CODE") + "}";
			}
			todoUrl = todoUrl + "&pkCode=" + todoBean.getStr("TODO_OBJECT_ID1");
			String title = todoBean.getStr("TODO_TITLE");
			if (title.length() > 6) {
				title = title.substring(0, 6) + "..";
			}
			result.append("{'tTitle':'待办--").append(title);
			result.append("','url':'").append(todoUrl);
			result.append("'}");
			String url = "";
			try {
				url = hostAddr + "/sy/comm/page/page.jsp?openTab="
						+ Hex.encodeHexString(result.toString().getBytes("UTF-8"));
				if (!todoUser.isEmpty()) {
					url += "&TODO_USER=" + Hex.encodeHexString(todoUser.getBytes("UTF-8"));
				}
				return url;
			} catch (UnsupportedEncodingException e) {
				throw new TipException(e.getMessage() + ": " + url);
			}
		}
	}

	public static ITodoClientService getService() {
		ITodoClientService service = TodoServiceFactory.getInstance();
		service.connectRemote(Context.getSyConf("TODO_SYNC_API_URL", "http://172.16.0.183:8189"));// 待办中心地址
		service.addHeader("appId", "ruaho");
		return service;
	}

	public static ITodoAsyncClientService getAsyncService() {
		ITodoAsyncClientService service = TodoAsyncServiceFactory.getInstance();
		service.connectRemote(Context.getSyConf("TODO_SYNC_API_URL", "http://172.16.0.183:8189"));// 待办中心地址
		service.addHeader("appId", "ruaho");
		return service;
	}

	/**
	 * 添加待办
	 * 
	 * @param dataBean
	 */
	private static void remoteTodo(Bean dataBean) {
		// ITodoAsyncClientService service = getAsyncService();
		// service.addTodo(beanTransTodo(dataBean), new AsyncHandler());
		try {
			GwToDoUtils.addToDo(dataBean);
		} catch (Exception e) {
			log.error("创建待办失败,错误信息是:"+e.getMessage());
		}
	}

	/**
	 * 待办转已办
	 * 
	 * @param delTodoId
	 */
	private static void remoteTodo2Done(List<Bean> list) {
		// 调用待办转已办接口
		try {
			GwToDoUtils.todo2Done(list);
		} catch (Exception e) {
			log.error("调用接口待办转已办 出现错误,错误信息是:"+e.getMessage());
		}

		/*ITodoAsyncClientService service = getAsyncService();

		boolean finish = false;

		StackTraceElement stack[] = Thread.currentThread().getStackTrace();

		for (StackTraceElement ste : stack) {

			if (ste.getMethodName().endsWith("finish") && ste.getClassName().endsWith("ProcServ")) {
				finish = true;
				break;
			}
		}

		List<Todo> todoList = new ArrayList<Todo>();

		for (Bean bean : list) {

			Todo todo = beanTransTodo(bean);

			if (finish) {
				todo.setFinish(true);
			}
			todoList.add(todo);
		}
		service.todo2Done(todoList, new AsyncHandler());*/

	}

	/**
	 * 删除待办
	 * 
	 * @param datas
	 */
	private static void remoteDelete(List<Bean> datas) {
/*		ITodoAsyncClientService service = getAsyncService();
		for (Bean bean : datas) {
			service.deleteTodo(bean.getId(), new AsyncHandler());
		}*/
		// 彻底删除多条待办通过接口调用
		try {
			GwToDoUtils.delToDos(datas);
		} catch (Exception e) {
			log.error("调用接口删除多条待办失败,错误信息是:"+e.getMessage());
		}
	}

	public static Todo beanTransTodo(Bean dataBean) {

		Todo todo = new Todo();
		if (dataBean.getStr("TODO_CODE").startsWith("FM_")) {
			todo.setAppCode("fm");
			todo.setAppId("fm");
		} else if (dataBean.getStr("TODO_CODE").startsWith("OA_")) {
			todo.setAppCode("oa");
			todo.setAppId("oa");
		} else {
			todo.setAppCode("ruaho");
			todo.setAppId("ruaho");
		}

		todo.setTodoNode(dataBean.getStr("TODO_OPERATION"));
		todo.setTodoId(dataBean.getStr("TODO_ID")); // 业务系统待办主键
		todo.setTodoCode(dataBean.getStr("TODO_CODE")); // 待办类型编码
		todo.setTodoAlias(dataBean.getStr("TODO_CODE_NAME"));
		todo.setTodoEmergy(dataBean.getStr("S_EMERGENCY"));
		todo.setTodoUrl(dataBean.getStr("TODO_URL"));
		todo.setTodoTitle(dataBean.getStr("TODO_TITLE")); // 待办标题
		todo.setTodoGroup(dataBean.getStr("TODO_OBJECT_ID1")); // 流程主键
		todo.setTodoCatalog(dataBean.getInt("TODO_CATALOG"));
		todo.setCmpyCode(dataBean.getStr("S_CMPY"));
		todo.setTodoSendTime(DateUtils.getDatetimeTS()); // 待办送交时间
		todo.setTodoMobile(1);
		String url = dataBean.getStr("TODO_URL");

		Bean servBean = ServDao.find(todo.getTodoCode(), todo.getTodoGroup());

		try {

			try {
				String dataStr = url.substring(url.indexOf("{"), url.lastIndexOf("}") + 1);

				Bean data = JsonUtils.toBean(dataStr);

				String pcUrl = "/sy/base/view/stdCardView.jsp?sId=" + todo.getTodoCode() + "&_PK_="
						+ data.getStr("_PK_");

				todo.setTodoUrl(URLEncoder.encode(pcUrl, "UTF-8")); // 待办URL地址

				if (dataBean.getStr("TODO_CODE").indexOf("OA_GW_GONGWEN") >= 0) {
					String selfUrl = "oa/view/page/gwForm.html?servId=" + dataBean.getStr("TODO_CODE") + "&dataId="
							+ dataBean.getStr("TODO_OBJECT_ID1");
					if (data.isNotEmpty("SEND_ID")) {
						selfUrl += "&sendId=" + data.getStr("SEND_ID");
					}

					if (servBean.getInt("ISDRAFT") == 3) {
						selfUrl += "&mode=registration&tmpl=OA_GW_GONGWEN_GSSW_DJ";
					}
					selfUrl += "&uid=" + dataBean.getStr("OWNER_CODE");
					todo.setTodoUrl(URLEncoder.encode(selfUrl, "UTF-8"));
				}
				if (dataBean.getStr("TODO_CODE").startsWith("FM_")) {// 流程中心待办url处理
					String selfUrl = "oa/view/page/rh/servCard.html?servId=" + dataBean.getStr("TODO_CODE") + "&dataId="
							+ dataBean.getStr("TODO_OBJECT_ID1");
					if (data.isNotEmpty("SEND_ID")) {
						selfUrl += "&sendId=" + data.getStr("SEND_ID");
					}

					if (servBean.getInt("ISDRAFT") == 3) {
						selfUrl += "&mode=registration&tmpl=OA_GW_GONGWEN_GSSW_DJ";
					}
					selfUrl += "&uid=" + dataBean.getStr("OWNER_CODE");
					selfUrl += "&globalFlag=1";
					todo.setTodoUrl(URLEncoder.encode(selfUrl, "UTF-8"));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			url = url.substring(url.indexOf("{") + 1, url.lastIndexOf("}"));
			url = url.replace("_PK_:", "dataId=").replace(",", "&").replace(":", "=");

			if (todo.getTodoCatalog() == 1) { // 待办url
				url = url.replace("NI_ID", "nid");
				todo.setTodoAppUrl(URLEncoder.encode("app?servId=" + todo.getTodoCode() + "&" + url, "UTF-8"));
			} else {// 待阅url
				url = url.replace("SEND_ID", "sendId").replace("MODE", "mode");
				todo.setTodoAppUrl(URLEncoder.encode("app?servId=" + todo.getTodoCode() + "&" + url, "UTF-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		UserBean doUser = UserMgr.getUser(dataBean.getStr("OWNER_CODE"));
		todo.setDoneDeptCode(doUser.getDeptCode()); // 办理人部门编码
		todo.setDoneDeptName(doUser.getDeptName());
		todo.setDoneUserCode(doUser.getCode());// 办理人统一编码
		todo.setDoneUserName(doUser.getName());
		todo.setDoneCmpyName(doUser.getCmpyName());

		UserBean sendUser = UserMgr.getUser(dataBean.getStr("SEND_USER_CODE"));
		todo.setSendUserCode(sendUser.getCode());// 送交人部门编码
		todo.setSendUserName(sendUser.getName());
		todo.setSendDeptCode(sendUser.getDeptCode());// 送交人统一编码
		todo.setSendDeptName(sendUser.getDeptName());
		todo.setSendCmpyName(sendUser.getCmpyName());
		todo.setDocNumber(servBean.getStr("GW_FULL_CODE"));
		if (StringUtils.isNotBlank(servBean.getStr("S_USER"))) {
			DeptBean draftDept = OrgMgr.getDept(servBean.getStr("S_DEPT"));
			todo.setMainDept(draftDept.getName()); // 主办部门名称
			todo.setMainDeptCode(servBean.getStr("S_DEPT"));// 主办部门编码
			todo.setDraftDeptCode(servBean.getStr("S_DEPT"));// 起草人部门编码
			todo.setDraftUserCode(servBean.getStr("S_USER"));// 起草人统一编码
			todo.setDraftUser(servBean.getStr("S_USER"));
			todo.setDraftDept(draftDept.getName());// 起草人部门编码
			todo.setDraftCmpy(servBean.getStr("S_CMPY"));
			todo.setDraftTime(DateUtils.getDatetime());// 起草时间
		}

		if (StringUtils.isNotBlank(dataBean.getStr("TODO_DEADLINE1"))) {
			todo.setTodoDeadline(dataBean.getStr("TODO_DEADLINE1"));// 办理期限
		}

		return todo;
	}

	/**
	 * 
	 * @param todoBean
	 *            待办数据Bean
	 * @param hostAddr
	 *            主机地址
	 * @param existJiangang
	 *            是否存在兼岗
	 * @return 访问待办的完整URL，可用于门户、RTX、邮件系统。
	 */
	public static String createTodoUrl(Bean todoBean, String hostAddr, boolean existJiangang) {
		// 处理本人和委托的兼岗问题
		String todoUser = "";
		String ownerCode = todoBean.getStr("OWNER_CODE");
		boolean agtentFlag = todoBean.getBoolean("_agtFLag");
		if (existJiangang) {
			if (!agtentFlag) {
				todoUser = ownerCode;
			}
		}
		StringBuilder result = new StringBuilder();
		if (todoBean.getStr("TODO_URL").indexOf(".showDialog.do") >= 0) {
			result.append("{'_PK_':'").append(todoBean.getStr("TODO_ID"));
			result.append("','servId':'").append("OA_SY_COMM_TODO");
			result.append("'}");
			String url = "";
			try {
				url = hostAddr + "/sy/comm/page/confirm.jsp?param="
						+ Hex.encodeHexString(result.toString().getBytes("UTF-8"));
				if (!todoUser.isEmpty()) {
					url += "&TODO_USER=" + Hex.encodeHexString(todoUser.getBytes("UTF-8"));
				}
				return url;
			} catch (UnsupportedEncodingException e) {
				throw new TipException(e.getMessage() + ": " + url);
			}
		} else {
			/**
			 * http://127.0.0.1/sy/base/view/stdCardView.jsp
			 * ?sId=GF_RM_APPLY&title=&pkCode=3DyBi2Fop6tX17hwQuXqHE
			 */
			String todoUrl = todoBean.getStr("TODO_URL");
			if (agtentFlag) { // 针对代他人办理情况
				todoUrl = todoUrl.substring(0, todoUrl.length() - 1);
				todoUrl = todoUrl + ",_AGENT_USER_:'" + todoBean.getStr("OWNER_CODE") + "'}";
			}
			String titleString = todoBean.getStr("TODO_TITLE");
			if (StringUtils.isNotEmpty(titleString) && titleString.length() > 5) {
				titleString = titleString.substring(0, 4) + "…";
			}

			try {
				result.append(hostAddr).append("/sy/base/view/stdCardView.jsp?");
				result.append("sId=").append(todoBean.getStr("SERV_ID"));
				result.append("&pkCode=").append(todoBean.getStr("TODO_OBJECT_ID1"));
				result.append("&title=").append(URLEncoder.encode(titleString, "UTF-8"));
				result.append("&replaceUrl=").append(todoUrl);
				if (!todoUser.isEmpty()) {
					result.append("&TODO_USER=").append(Hex.encodeHexString(todoUser.getBytes("UTF-8")));
				}
			} catch (Exception e) {
				throw new TipException(e.getMessage() + ":" + result.toString());
			}

			return result.toString();
		}
	}
}
