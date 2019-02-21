package com.rh.core.comm.cms;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.gw.util.GwUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;
import com.rh.core.util.freemarker.FreeMarkerUtils;

public class CmsServlet extends HttpServlet {
	/** log */
	private static Log log = LogFactory.getLog(CmsServlet.class);
	/** UID */
	private static final long serialVersionUID = 1L;


	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String myOrigin = request.getHeader("origin");
		String allowOrigin = GwUtils.ifWhiteList(myOrigin);
		response.setHeader("Access-Control-Allow-Origin", allowOrigin);
		response.addHeader("Access-Control-Allow-Credentials", "true");
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		Context.setRequest(request);
		
//		String uri = request.getRequestURI();
		String relativePath = CmsHelper.getResource(request);
		Bean bean = CmsHelper.parseUri(relativePath);
		String dataId = bean.getStr("dataId");
		String ftltmp = Context.appStr(APP.SYSPATH) + "sy/comm/cms/ftl/SY_MSG_CONTENT/notice.ftl";
        Bean data1 = ServDao.find("SY_MSG_CONTENT", dataId);
		String html = FreeMarkerUtils.parseText(ftltmp, data1);
		response.getOutputStream().write(html.getBytes("UTF-8"));
		IOUtils.closeQuietly(response.getOutputStream());
	}
	
	 public void doPost(HttpServletRequest request, HttpServletResponse response)

             throws ServletException, IOException {
		 doGet(request, response);
	 }
}
