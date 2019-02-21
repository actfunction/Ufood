package com.rh.gw.util;


import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.Context;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.DateUtils;
import com.rh.core.util.Lang;
import com.rh.core.util.encoder.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.serv.OutBean;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.List;

/**
 * 客户端消息Espace接口工具类
 * 
 * @author kfzx-linll
 * @date 2018/11/15
 */
public class GwEspaceUtil {
	/** 记录历史 */
	private static final Log log = LogFactory.getLog(GwEspaceUtil.class);
	
	/**Espace代办表和服务一致*/
	private static final String OA_GW_INTERFACE_ESPACE = "OA_GW_INTERFACE_ESPACE";
	
	/**日志表和服务一致*/
	private static final String OA_GW_INTERFACE_ESPACE_LOG = "OA_GW_INTERFACE_ESPACE_LOG";

	/** 获取鉴权的地址 */
	private static final String EC_GET_AUTH_URL_DEF = "http://10.174.5.208:443/login";
	private static final String EC_GET_AUTH_URL = Context.getSyConf("EC_GET_AUTH_URL", EC_GET_AUTH_URL_DEF);

	/** 用户名(鉴权时用到) */
	private static final String EC_USERNAME_DEF = "test";
	private static final String EC_USERNAME = Context.getSyConf("EC_USERNAME", EC_USERNAME_DEF);

	/** 发送消息的地址 */
    private static final String EC_SEND_URL_DEF = "http://10.174.5.208:443/im/ecAccount";
	private static final String EC_SEND_URL = Context.getSyConf("EC_SEND_URL", EC_SEND_URL_DEF);

	/** 发送者的EC账号(发送时用到) */
	private static final String EC_ACCOUNT_DEF = "espace03";
	private static final String EC_ACCOUNT = Context.getSyConf("EC_ACCOUNT", EC_ACCOUNT_DEF);



	/**
	 * 鉴权(获取授权信息)
	 * 
	 * @return HttpResponse
	 */
	private static HttpResponse getUserService() {
		HttpResponse response = null;
		try {
			HttpPost post = new HttpPost(EC_GET_AUTH_URL);
			// 设置请求头
			post.setHeader("Content-Type", "application/json;charset=UTF-8");

			// 使用base64进行加密,然后将加密的信息转换为string
			String tokenStr = new String(Base64.encode((EC_USERNAME + ", algorithm=MD5").getBytes()));
			String token = "Basic " + tokenStr;	// 拼装token的格式：Basic YFUDIBGDJHFK78HFJDHF==
			// 设置认证信息
			post.setHeader("Authorization:", token);

			DefaultHttpClient client = new DefaultHttpClient();
			response = client.execute(post);
			log.debug("已经发送鉴权信息");
		} catch(Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
	/**
	 * 定时调度轮询发送消息
	 * 
	 * @return
	 */
	public static OutBean sendMessageAuto() {
		OutBean outBean = new OutBean();
		// 从OA_GW_INTERFACE_ESPACE表中,查询
		OutBean ecOutBean = ServMgr.act(OA_GW_INTERFACE_ESPACE, ServMgr.ACT_QUERY, new ParamBean());
		List<Bean> esList = ecOutBean.getDataList();

		for (Bean esBean : esList) {
			outBean = getESapceAuth(esBean);
		}
		return outBean;
	}


	/**
	 * 鉴权接口(处理成功或失败信息)
	 * 其他部门实时发送消息的时候,也得先鉴权
	 *
	 * @param esBean
	 * @return
	 */
	public static OutBean getESapceAuth(Bean esBean) {
		OutBean outBean = new OutBean();
		// 备份一个新的数据bean到logBean
		Bean logBean = new Bean();
		BeanUtils.trans(esBean, logBean);

		logBean.setId(""); // 去除logBean的主键

		HttpResponse userAuth = null;
		try {
			// 获取鉴权信息
			userAuth = getUserService();
			int statusCode = userAuth.getStatusLine().getStatusCode();

			// 此处判断是否鉴权成功(后续需要根据具体返回值修改)
			if (statusCode == 200) { // 鉴权成功,再调用实时发消息的接口
				outBean = sendESpaceMsg(esBean);
			} else {
				/**鉴权失败不发消息,失败信息记录到LOG表中*/
				// 所有信息都从result获取
				saveESpaceAuthLog(userAuth, logBean);
			}
		} catch (Exception e) {
			/**鉴权失败不发消息,记录到LOG表中*/
			saveESpaceAuthLog(userAuth, logBean);
		}
		return outBean.set("userAuth", userAuth).set("esBean", esBean);
	}


	/**
	 * 鉴权失败消息保存到日志表
	 *
	 * @param userAuth
	 * @param logBean
	 */
	public static void saveESpaceAuthLog(HttpResponse userAuth, Bean logBean) {
		logBean.set("LOG_ID", Lang.getUUID());
		logBean.set("STATUS_CODE", userAuth.getStatusLine().getStatusCode());
		logBean.set("SUCCESS_FLAG", "F");
		ServDao.save(OA_GW_INTERFACE_ESPACE_LOG, logBean);
	}


	/**
	 * 发送失败消息保存到日志表
	 *
	 * @param result
	 * @param logBean
	 */
	public static void saveESpaceMsgLog(HttpResponse result, Bean logBean) {
		// 所有信息都从result获取
		logBean.set("LOG_ID", Lang.getUUID());
		logBean.set("STATUS_CODE", result.getStatusLine().getStatusCode());

		Header[] headers = result.getAllHeaders();
		for (Header header : headers) {
			if ("RETURN_CODE".equalsIgnoreCase(header.getName())) {
				// 返回码
				logBean.set("RETURN_CODE", header.getValue());
			}

			if ("RETURN_CODE".equalsIgnoreCase(header.getName())) {
				String f_value = header.getValue();
				switch (f_value) {
					case "1" :
						// 失败
						logBean.set("RETURN_CODE", "失败");
						break;
					case "2" :
						// 密码错误
						logBean.set("RETURN_CODE", "密码错误");
						break;
					case "3" :
						// 用户不存在
						logBean.set("RETURN_CODE", "用户不存在");
						break;
					case "4" :
						// 用户已登录 (此返回值表示会将其他地方登录的用户踢下线)
						logBean.set("RETURN_CODE", "用户已登录");
						break;
					case "5" :
						// 账号被锁定
						logBean.set("RETURN_CODE", "账号被锁定");
						break;
					case "20" :
						// 服务器超时没有响应
						logBean.set("RETURN_CODE", "服务器超时没有响应");
						break;
					case "21" :
						// 连接出现异常
						logBean.set("RETURN_CODE", "连接出现异常");
					case "23" :
						// nonce无效
						logBean.set("RETURN_CODE", "nonce无效");
						break;
				}
			}
		}
		logBean.set("SUCCESS_FLAG", "F"); // 是否成功
		ServDao.save(OA_GW_INTERFACE_ESPACE_LOG, logBean);
	}
	
	
	/**
	 * 其他部门保存信息
	 * 
	 * @param dataBean
	 * @return OutBean 返回数据
	 */
	public OutBean saveESpaceMessage(Bean dataBean) {
		OutBean reBean = null;
		try {
			Bean saveBean = ServDao.save(OA_GW_INTERFACE_ESPACE, dataBean);
			reBean.setData(saveBean);
			reBean.setOk( "保存成功！");
		} catch (Exception e) {
			log.error(e.getMessage());
			reBean.setError("保存失败！");
			e.printStackTrace();
		}
		return reBean;
	}


	/**
	 * 其他部门实时发送消息
	 *
	 * @param esBean 参数对象
	 * @return
	 */
	public static OutBean sendESpaceMsg(Bean esBean) {
		OutBean outBean = new OutBean();
		Bean logBean = new Bean();
		BeanUtils.trans(esBean, logBean);
		HttpPost post = new HttpPost(EC_SEND_URL);
		post.setHeader("Content-Type", esBean.getStr("CONTENT_TYPE"));

		//byte[] tokenByte = Base64.encode((USERNAME + ", algorithm=MD5").getBytes()); // 使用base64进行加密
		//String tokenStr = new String(tokenByte);	// 将加密的信息转换为string
		//String token = "Basic " + tokenStr;	// 拼装token的格式：Basic YFUDIBGDJHFK78HFJDHF==

		// 把表字段信息放到post中
		byte[] tokenByte = Base64.encode(esBean.getStr("AUTH").getBytes());
		String tokenStr = new String(tokenByte);	// 将加密的信息转换为string
		post.setHeader("Authorization:", "Basic " + tokenStr);	// 鉴权信息, 拼装token的格式：Basic YFUDIBGDJHFK78HFJDHF==

		post.setHeader("sendNumber", esBean.getStr("SEND_NUMBER")); // 发送者EC账号
		post.setHeader("ucAccount", esBean.getStr("UC_ACCOUNT")); // 接收者EC账号
		post.setHeader("message", esBean.getStr("SEND_MESSAGE")); // 消息内容
		post.setHeader("dateTime", DateUtils.getDatetime());	// 消息提交时间
		//post.setHeader("priorityLevel", priorityLevel);	// 消息优先级,此字段预留,暂不使用

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse result = null;
		try {
			// 发送消息成功
			result = client.execute(post);
			log.debug("发送消息完毕！");

			// 响应的状态码
			if (result.getStatusLine().getStatusCode() == 200) { //发送成功,记录到LOG表,并从OA_GW_INTERFACE_ESPACE表中删除该条数据。
				ServDao.delete(OA_GW_INTERFACE_ESPACE, esBean);

				// 所有信息都从result获取
				logBean.set("LOG_ID", Lang.getUUID());
				logBean.set("STATUS_CODE", 200); // 响应码

				Header[] headers = result.getAllHeaders();
				for (Header header : headers) {
					if ("RETURN_CONTEXT".equalsIgnoreCase(header.getName())) {
						// 返回描述
						logBean.set("RETURN_CONTEXT", header.getValue());
					}

					if ("RETURN_CODE".equalsIgnoreCase(header.getName())) {
						String value = header.getValue();
						if ("0".equals(value)) { // 成功
							logBean.set("RETURN_CODE", value); // 返回码
						}
					}
				}
				logBean.set("SUCCESS_FLAG", "T"); // 是否成功
				ServDao.save(OA_GW_INTERFACE_ESPACE_LOG, logBean);
			} else { //发送失败,则记录到LOG表,并修改发送次数+1
				esBean.set("SEND_COUNT", esBean.getInt("SEND_COUNT") + 1); // 发送次数加1
				ServDao.update(OA_GW_INTERFACE_ESPACE, esBean);

				saveESpaceMsgLog(result, logBean); // 保存到日志表
			}
		} catch (Exception e) {
			/** 发送消息失败,把失败记录插入到LOG中*/
			saveESpaceMsgLog(result, logBean);
		}
		return outBean.set("result", result);
	}
}
