package com.rh.gw.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.ServDao;
import com.rh.core.util.JsonUtils;

/**
 * 
 * @author lizhiyu
 *
 */
public class GwToDoUtils {

	/*** 记录历史 */
	private static Log log = LogFactory.getLog(GwToDoUtils.class);

	/**
	 * 
	 * 新建待办
	 * 
	 * @param xaspappToken
	 *            标识应用身份
	 * @param taskStart
	 *            详细信息
	 * @param xaspsession
	 *            当前用户token
	 * @return Map 中有状态码 和 响应信息
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String createToDao(String xaspappToken, Map<String, Object> paramMap, String xaspsession) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String result = "";
		try {
			// 创建httpClient实例
			httpClient = HttpClients.createDefault();
			// 创建httpPost远程连接实例
			HttpPost httpPost = new HttpPost(Context.getSyConf("addDaiBanUrl", ""));
			// 配置请求参数实例
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 设置连接主机服务超时时间
					.setConnectionRequestTimeout(35000)// 设置连接请求超时时间
					.setSocketTimeout(60000)// 设置读取数据连接超时时间
					.build();
			// 为httpPost实例设置配置
			httpPost.setConfig(requestConfig);
			httpPost.setHeader("XASPAPPTOKEN", xaspappToken);
			httpPost.setHeader("XASPSESSION", xaspsession);
			// 封装post请求参数
			if (null != paramMap && paramMap.size() > 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				// 通过map集成entrySet方法获取entity
				Set<Entry<String, Object>> entrySet = paramMap.entrySet();
				// 循环遍历，获取迭待器
				Iterator<Entry<String, Object>> iterator = entrySet.iterator();
				while (iterator.hasNext()) {
					Entry<String, Object> mapEntry = iterator.next();
					nvps.add(new BasicNameValuePair(mapEntry.getKey(), mapEntry.getValue().toString()));
				}

				// 为httpPost设置封装好的请求参数
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}
			// 执行post请求得到返回对象
			response = httpClient.execute(httpPost);
			// 通过返回对象获取数据
			HttpEntity entity = response.getEntity();
			// 将返回的数据转换为字符串
			result = EntityUtils.toString(entity);
		} catch (Exception e) {
			log.error("调用创建待办接口出现错误,错误信息是:" + e.getMessage());
			e.printStackTrace();
		} finally {
			// 关闭资源
			if (null != response) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * 修改待办
	 * 
	 * @param xaspappToken
	 *            标识应用身份
	 * @param taskStart
	 *            详细信息
	 * @param xaspsession
	 *            当前用户token
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String updateToDao(String oid, Map<String, Object> params, String xaspsession) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPut httpPut = new HttpPut(Context.getSyConf("addDaiBanUrl", "") + "?taskStartId=" + oid);
		// header存放 token
		httpPut.addHeader("XASPSESSION", xaspsession);
		httpPut.addHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8");
		if (params != null && params.size() > 0) {
			StringEntity se = new StringEntity(new Gson().toJson(params), "UTF-8");
			httpPut.setEntity(se);
		}
		HttpResponse httpResponse = null;
		String res = null;
		try {
			httpResponse = httpClient.execute(httpPut);
			if (httpResponse != null && 200 == httpResponse.getStatusLine().getStatusCode()) {
				res = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			}
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			// 关闭资源
			if (null != httpResponse) {
				try {
					((Closeable) httpResponse).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	/**
	 * 删除待办
	 * 
	 * @param taskStartIds
	 *            待办id 支持多删除,多个待办id使用逗号分隔
	 * @return 状态码 200则待表成功
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String delToDo(String taskStartIds) {
		String stateCode = "";
		String url = Context.getSyConf("deltedDaiBanUrl", "");
		url += "?taskStartIds=" + taskStartIds;
		CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
		HttpDelete httpdelete = new HttpDelete(url);
		CloseableHttpResponse httpResponse = null;
		try {
			httpResponse = closeableHttpClient.execute(httpdelete);
			stateCode = httpResponse.getStatusLine().getStatusCode() + "";
		} catch (Exception e) {
			log.error("在 ToDoInterfaceUtils.java方法delToDo中删除待办出错,错误信息：" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				httpResponse.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try { // 关闭连接、释放资源
			closeableHttpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 返回状态码
		return stateCode;
	}

	/**
	 * //彻底删除多条待办通过接口调用
	 * 
	 * @param whereBean
	 * @param queryBean
	 * @param datas
	 */
	public static void delToDos(List<Bean> datas) {
		String statusCode = "";
		// 调用接口删除待办的数据
		for (int i = 0; i < datas.size(); i++) {
			String oid = "";
			try {
				StringBuilder strSql = new StringBuilder();
				strSql.append(
						"SELECT * FROM OA_GW_INTERFACE_GATEWAYS WHERE DOOR_ID  in (select DOOR_ID from OA_GW_INTERFACE_GATEWAYS where TODO_ID ='"
								+ datas.get(i).getStr("TODO_ID") + "')");
				List<Bean> todoList = Transaction.getExecutor().query(strSql.toString());
				if(0==todoList.size()){
					ServDao.save("OA_GW_INTERFACE_GATEWAY_LOGS",
							new Bean().set("LOG_OID", oid).set("SEND_TYPE", "2").set("SUCCESS_FLAG", "F").set("DOOR_ID", datas.get(i).getStr("TODO_ID")));
					log.error("调用门户接口删除失败");
				}
				for (int j = 0; j < todoList.size(); j++) {
					// 调用接口进行删除操作
					oid = todoList.get(j).getStr("TODO_ID");
					statusCode = GwToDoUtils.delToDo(oid);
					// statusCode="200";
					// 修改本地的待办状态
					if ("200".equals(statusCode)) {
						Transaction.getExecutor()
								.execute("update OA_GW_INTERFACE_GATEWAYS set TODO_STATUS ='待办成功删除'where DOOR_ID ='"
										+ todoList.get(j).getStr("DOOR_ID") + "'");
						ServDao.save("OA_GW_INTERFACE_GATEWAY_LOGS",
								new Bean().set("LOG_OID", oid).set("SEND_TYPE", "2").set("SUCCESS_FLAG", "T").set("DOOR_ID", "123"));
					} else {
						// 将错误信息记录到log表中用来下次进行调用
						ServDao.save("OA_GW_INTERFACE_GATEWAY_LOGS",
								new Bean().set("LOG_OID", oid).set("SEND_TYPE", "2").set("SUCCESS_FLAG", "F").set("DOOR_ID", "123"));
					}
				}
			} catch (Exception e) {
				if (!"200".equals(statusCode)) {
					// 将错误信息记录到log表中用来下次进行调用
					ServDao.save("OA_GW_INTERFACE_GATEWAY_LOGS",
							new Bean().set("LOG_OID", oid).set("SEND_TYPE", "2").set("SUCCESS_FLAG", "F").set("DOOR_ID", "123"));
				}
				log.error(e.getMessage());
			}
		}
	}


	/**
	 * 添加待办
	 * 
	 * @param dataBean
	 */
	public static void addToDo(Bean dataBean) {
		String token = Context.getSyConf("token", "");
		// token = "121212";// 测试待码
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// 获得要传递参数
		paramMap = getToDoParam(dataBean);
		paramMap.put("TODO_ID", dataBean.getStr("TODO_ID"));
		try {
			// 创建待办
			if (token != null && !token.equals("")) {
				// 调用接口创建token
				String toDoParam = GwToDoUtils.createToDao(Context.getSyConf("XASPAPPTOKEN", ""), paramMap, token);
				//将参数保存到代办表中
				saveNewToDo(toDoParam, paramMap);
				/**
				 * sendType 1 是添加待办 2是删除待办 3 修改待办
				 */
				ServDao.save("OA_GW_INTERFACE_GATEWAY_LOGS",
						new Bean().set("SEND_MESSAGE", new Gson().toJson(toDoParam)).set("SEND_TYPE", "1")
								.set("SUCCESS_FLAG", "T").set("DOOR_ID", "123"));
			} else {
				log.error("调用添加门户接口的token是空的");
				// 将要传递的数据和参数全部传递到历史表 等待下次调用
				String requestParamJson = new Gson().toJson(paramMap);
				toDoSaveError(requestParamJson);
			}
		} catch (Exception e) {
			// 将信息进行记录log表中
			String requestParamJson = new Gson().toJson(paramMap);
			toDoSaveError(requestParamJson);
			log.error("创建待办出现错误,错误信息是 :" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 当调用接口失败后将请求参数保存到log表中用于下一次进行调用
	 * 
	 * @param requestParamJson
	 */
	private static void toDoSaveError(String requestParamJson) {
		// 将json转换成bean
		// Bean requestParamBean = JsonUtils.toBean(requestParamJson);
		Bean toDoLogBean = new Bean();
		toDoLogBean.set("SEND_MESSAGE", requestParamJson);
		toDoLogBean.set("SEND_TYPE", "1").set("SUCCESS_FLAG", "F").set("DOOR_ID", "123");
		ServDao.save("OA_GW_INTERFACE_GATEWAY_LOGS", toDoLogBean);
	}

	/**
	 * 待办转已办
	 * 
	 * @param list
	 */
	public static void todo2Done(List<Bean> DoneList) {
		String token = Context.getSyConf("token", "");

		// token = "121212";// 测试待码
		String oid = "";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Map<String, Object> doDoneParam=null;
		// 获得要传递参数
		for (int i = 0; i < DoneList.size(); i++) {
			try {
					// 获得参数
					doDoneParam = getDoDoneParam(DoneList.get(i));
					// 根据DoneList.get(i) 中的数据查询出oid
					List<Bean> todoList = Transaction.getExecutor()
							.query("select * from OA_GW_INTERFACE_GATEWAYS where TODO_ID='"
									+ DoneList.get(i).getStr("TODO_ID") + "'");
					paramMap.put("TODO_ID", DoneList.get(i).getStr("TODO_ID"));
					if (token != null && !token.equals("")) {
						oid = todoList.get(0).getStr("DOOR_OID");
					// 将待办装换成已办
					String updateToResult = GwToDoUtils.updateToDao(oid, doDoneParam, token);
					// 接收参数以及信息保存到数据库中
					saveToDoToDone(updateToResult, new Bean().set("TODO_ID", DoneList.get(i).getStr("TODO_ID")));
					updateToDoSaveError(new Gson().toJson(doDoneParam), oid, "T");
				} else {
					log.error("调用修改门户接口的token是空的");
					doDoneParam.put("TODO_ID", DoneList.get(i).getStr("TODO_ID"));
					// 将要传递的数据和参数全部传递到历史表 等待下次调用
					String requestParamJson = new Gson().toJson(doDoneParam);
					updateToDoSaveError(requestParamJson, oid, "F");
				}
			} catch (Exception e) {
				doDoneParam.put("TODO_ID", DoneList.get(i).getStr("TODO_ID"));
				// 将信息进行记录log表中
				String requestParamJson = new Gson().toJson(doDoneParam);
				updateToDoSaveError(requestParamJson, oid, "F");
				log.error("修改待办出现错误,错误信息是 :" + e.getMessage());
			}
		}
	}

	// ----------------------------------------------------------------
	private static void updateToDoSaveError(String requestParamJson, String oid, String flag) {
		// 将json转换成bean
		 Bean requestParamBean = JsonUtils.toBean(requestParamJson);
		Bean toDoLogBean = new Bean();
		toDoLogBean.set("SEND_MESSAGE", requestParamJson);
		toDoLogBean.set("SEND_TYPE", "3");
		toDoLogBean.set("LOG_OID", oid).set("SUCCESS_FLAG", flag).set("DOOR_ID", "123").set("TODO_ID",requestParamBean.getStr("TODO_ID"));
		ServDao.save("OA_GW_INTERFACE_GATEWAY_LOGS", toDoLogBean);
	}

	private static Map<String, Object> getToDoParam(Bean dataBean) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// creator (string, optional): 待办创建者(传用户登录名) ,
		Bean sendUser = ServDao.find("SY_ORG_USER", dataBean.getStr("SEND_USER_CODE"));
		paramMap.put("creator", sendUser.getStr("USER_LOGIN_NAME"));
		// executor (string): 待办执行者(传用户登录名) ,
		Bean owenUser = ServDao.find("SY_ORG_USER", dataBean.getStr("OWNER_CODE"));
		paramMap.put("executor", owenUser.getStr("USER_LOGIN_NAME"));
		// name (string): 待办名称 ,
		paramMap.put("name", dataBean.getStr("TODO_CODE_NAME"));
		// note (string): 待办备注 ,
		paramMap.put("note", "插入了一条待办");
		// reada (string): 待办已读或未读 = ['NO', 'YES'],
		paramMap.put("reada", "NO");
		// reminder (string): 待办提醒设置 ,
		paramMap.put("reminder", "待办提醒");
		// sourceApp (string): 待办所属应用系统 ,
		paramMap.put("sourceApp", "行政公文办公");
		// status (string, optional): 待办状态(不填默认未开始) = ['PREPARE', 'START',
		// 'FINISH', 'CANCEL', 'TERMINATE'],
		paramMap.put("status", "PREPARE");
		// type (string): 待办类型 = ['APPROVAL', 'OTHER']
		paramMap.put("type", "APPROVAL");
		return paramMap;
	}

	private static Map<String, Object> getDoDoneParam(Bean dataBean) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// creator (string, optional): 待办创建者(传用户登录名) ,
		Bean sendUser = ServDao.find("SY_ORG_USER", dataBean.getStr("SEND_USER_CODE"));
		paramMap.put("creator", sendUser.getStr("USER_LOGIN_NAME"));
		// executor (string): 待办执行者(传用户登录名) ,
		Bean owenUser = ServDao.find("SY_ORG_USER", dataBean.getStr("OWNER_CODE"));
		paramMap.put("executor", owenUser.getStr("USER_LOGIN_NAME"));
		// name (string): 待办名称 ,
		paramMap.put("name", dataBean.getStr("TODO_CODE_NAME"));
		// note (string): 待办备注 ,
		paramMap.put("note", "对待办进行了修改");
		// reada (string): 待办已读或未读 = ['NO', 'YES'],
		paramMap.put("reada", "YES");
		paramMap.put("sourceUrl", dataBean.getStr("TODO_URL"));
		// sourceApp (string): 待办所属应用系统 ,
		paramMap.put("sourceApp", "行政公文办公");
		// status (string, optional): 待办状态(不填默认未开始) = ['PREPARE', 'START',
		// 'FINISH', 'CANCEL', 'TERMINATE'],
		paramMap.put("status", "START");
		// type (string): 待办类型 = ['APPROVAL', 'OTHER']
		paramMap.put("type", "APPROVAL");
		return paramMap;
	}

	private static void saveNewToDo(String toDoParam, Map<String, Object> paramMap) {
		try {
			Bean paramBean = JsonUtils.toBean(toDoParam);
			String oid = paramBean.getStr("oid");
			String note = paramBean.getStr("note");
			String executor = paramBean.getStr("executor");
			String sourceApp = paramBean.getStr("sourceApp");
			String creator = paramBean.getStr("creator");
			// String createDate = paramBean.getStr("createDate");
			String status = paramBean.getStr("status");
			// String planStartDate = paramBean.getStr("planStartDate");
			// String realStartDate = paramBean.getStr("realStartDate");
			// String planFinishDate = paramBean.getStr("planFinishDate");
			// String realFinishDate = paramBean.getStr("realFinishDate");
			// String expireTime = paramBean.getStr("expireTime");
			String sourceUrl = paramBean.getStr("sourceUrl");
			// String sourceAppType = paramBean.getStr("sourceAppType");
			String reada = paramBean.getStr("reada");
			String type = paramBean.getStr("type");

			Bean saveGetBean = new Bean();
			Gson requestParamer = new Gson();
			String reqPar = requestParamer.toJson(toDoParam);

			saveGetBean.set("DOOR_ID", UUID.randomUUID().toString()); // VARCHAR2(40),
																		// --主键
			saveGetBean.set("TODO_ID", paramMap.get("TODO_ID")); // VARCHAR2(40),
																	// --主键 待办ID
			saveGetBean.set("TODO_STATUS", "daiban"); // VARCHAR2(40), --我们的 待办状态
			saveGetBean.set("DOOR_OID", oid); // VARCHAR2(40), --门户 返回的 待办 门户主键
												// id
			saveGetBean.set("USER_NUMBER", paramMap.get("executor")); // VARCHAR2(40),
																		// --个人账号
			// saveGetBean.set("SEND_COUNT", obj); //NUMBER(4), --发送次数
			saveGetBean.set("DOOR_CREATOR", creator); // VARCHAR2(40),
														// --待办创建者(传用户登录名) ,
			saveGetBean.set("DOOR_EXECUTOR", executor); // VARCHAR2(40),
														// --待办执行者(传用户登录名) ,
			saveGetBean.set("TODO_URL", sourceUrl);
			// saveGetBean.set("DOOR_NAME", obj); //VARCHAR2(40), ---待办名称 ,
			saveGetBean.set("DOOR_NOTE", note); // VARCHAR2(2000), ---待办备注 ,
			saveGetBean.set("DOOR_READA", reada); // VARCHAR2(40), ---待办已读或未读 =
													// ['NO', 'YES'],
			// saveGetBean.set("DOOR_REMINDER", obj); //VARCHAR2(40), ---待办提醒设置
			// ,
			saveGetBean.set("DOOR_SOURCEAPP", sourceApp); // VARCHAR2(40),
															// ---待办所属应用系统 ,
			saveGetBean.set("DOOR_STATUS", status); // VARCHAR2(40),
													// ---待办状态(不填默认未开始) =
													// ['PREPARE', 'START',
													// 'FINISH', 'CANCEL',
													// 'TERMINATE'],
			saveGetBean.set("DOOR_TYPE", type); // VARCHAR2(40), ---待办类型 =
												// ['APPROVAL', 'OTHER']
			// 发送的消息内容
			saveGetBean.set("SEND_MESSAGE", reqPar); // VARCHAR2(2000), --消息内容
			// 将paramBean 保存到数据库中
			ServDao.save("OA_GW_INTERFACE_GATEWAYS", saveGetBean);
		} catch (Exception e) {
			log.error("创建待办返回的数据保存到数据库出现错误,错误信息是：" + e.getMessage());
		}
	}

	/**
	 * 保存到数据库中
	 * 
	 * @param updateToResult
	 */
	private static void saveToDoToDone(String updateToResult, Bean dataBean) {
		Bean paramBean = JsonUtils.toBean(updateToResult);
		String oid = paramBean.getStr("oid");
		String note = paramBean.getStr("note");
		String executor = paramBean.getStr("executor");
		String sourceApp = paramBean.getStr("sourceApp");
		String creator = paramBean.getStr("creator");
		// String createDate = paramBean.getStr("createDate");
		String status = paramBean.getStr("status");
		// String planStartDate = paramBean.getStr("planStartDate");
		// String realStartDate = paramBean.getStr("realStartDate");
		// String planFinishDate = paramBean.getStr("planFinishDate");
		// String realFinishDate = paramBean.getStr("realFinishDate");
		// String expireTime = paramBean.getStr("expireTime");
		String sourceUrl = paramBean.getStr("sourceUrl");
		// String sourceAppType = paramBean.getStr("sourceAppType");
		String reada = paramBean.getStr("reada");
		String type = paramBean.getStr("type");

		Bean saveGetBean = new Bean();
		saveGetBean.set("DOOR_ID", UUID.randomUUID().toString()); // VARCHAR2(40),
																	// --主键
		saveGetBean.set("TODO_ID", dataBean.getStr("TODO_ID")); // VARCHAR2(40),
																// --主键 待办ID
		saveGetBean.set("TODO_STATUS", "修改了待办状态"); // VARCHAR2(40), --我们的 待办状态
		saveGetBean.set("DOOR_OID", oid); // VARCHAR2(40), --门户 返回的 待办 门户主键 id
		saveGetBean.set("USER_NUMBER", executor); // VARCHAR2(40), --个人账号
		// saveGetBean.set("SEND_COUNT", obj); //NUMBER(4), --发送次数
		saveGetBean.set("DOOR_CREATOR", creator); // VARCHAR2(40),
													// --待办创建者(传用户登录名) ,
		saveGetBean.set("DOOR_EXECUTOR", executor); // VARCHAR2(40),
													// --待办执行者(传用户登录名) ,
		saveGetBean.set("TODO_URL", sourceUrl);
		// saveGetBean.set("DOOR_NAME", obj); //VARCHAR2(40), ---待办名称 ,
		saveGetBean.set("DOOR_NOTE", note); // VARCHAR2(2000), ---待办备注 ,
		saveGetBean.set("DOOR_READA", reada); // VARCHAR2(40), ---待办已读或未读 =
												// ['NO', 'YES'],
		// saveGetBean.set("DOOR_REMINDER", obj); //VARCHAR2(40), ---待办提醒设置 ,
		saveGetBean.set("DOOR_SOURCEAPP", sourceApp); // VARCHAR2(40),
														// ---待办所属应用系统 ,
		saveGetBean.set("DOOR_STATUS", status); // VARCHAR2(40),
												// ---待办状态(不填默认未开始) =
												// ['PREPARE', 'START',
												// 'FINISH', 'CANCEL',
												// 'TERMINATE'],
		saveGetBean.set("DOOR_TYPE", type); // VARCHAR2(40), ---待办类型 =
											// ['APPROVAL', 'OTHER']
		// 发送的消息内容
		saveGetBean.set("SEND_MESSAGE", updateToResult); // VARCHAR2(2000),
															// --消息内容
		ServDao.update("OA_GW_INTERFACE_GATEWAYS", saveGetBean);
	}
}