package com.rh.core.comm.file.upload;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;

/**
 * ckedit response
 * @author wanglong
 * 
 */
public class DefaultUploadHandler implements FileUploadHandler {

	@Override
	public void handleRequest(HttpServletRequest request, ParamBean paramBean) {
	}

	@Override
	public void handleResponse(HttpServletRequest request, HttpServletResponse response, Bean result)
			throws IOException {
		// 返回信息
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		out.write(JsonUtils.toJson(result));
		out.flush();
		out.close();
	}

}
