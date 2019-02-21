package com.rh.core.comm.sso;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * 数据同步接口
 * 
 * @author wanglong
 * 
 */
@WebService(targetNamespace = "http://sso.common.rh.com", name = "SyncWS", serviceName = "SyncWS")
public interface SyncWS {
	/**
	 * 获取需要同步的日志信息
	 * 
	 * @param sysCode
	 *            系统编码
	 * @param servId
	 *            同步服务ID
	 * @return 同步信息
	 */
	@WebResult(name = "return", targetNamespace = "http://sso.common.rh.com")
	@WebMethod
	String getLog(
			@WebParam(name = "sysCode", targetNamespace = "http://sso.common.rh.com") String sysCode,
			@WebParam(name = "servId", targetNamespace = "http://sso.common.rh.com") String servId);

	/**
	 * 单个通知已经同步的日志信息
	 * 
	 * @param recordId
	 *            日志主键
	 * @return 同步成功与否
	 */
	@WebResult(name = "String", targetNamespace = "http://sso.common.rh.com")
	@WebMethod
	String setSgSync(
			@WebParam(name = "sysCode", targetNamespace = "http://sso.common.rh.com") String recordId);
}
