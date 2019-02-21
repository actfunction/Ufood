package com.rh.core.comm.file.upload;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;

/**
 * @author wanglong
 * 
 */
public interface FileUploadHandler {

	/**
	 * handler http request
	 * @param request <code>HttpServletRequest</code>
	 * @param paramBean 缺省参数信息
	 */
	void handleRequest(HttpServletRequest request, ParamBean paramBean);

	/**
	 * handle http response
	 * @param request <code>HttpServletRequest</code>
	 * @param response <code>HttpServletResponse</code>
	 * @param result result bean
	 * @throws IOException throws this exception ,if response write error
	 */
	void handleResponse(HttpServletRequest request, HttpServletResponse response, Bean result)
			throws IOException;

}
