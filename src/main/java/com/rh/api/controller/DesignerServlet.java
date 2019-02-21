package com.rh.api.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.gw.util.GwUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.api.anno.URLAnno;
import com.rh.api.serv.IFormServ;
import com.rh.api.impl.FormServImpl;
import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.util.RequestUtils;
import com.rh.api.util.ApiConstant;

public class DesignerServlet extends HttpServlet {

	/** uid */
	private static final long serialVersionUID = 8508098085189610760L;
	/** log */
	private static Log log = LogFactory.getLog(DesignerServlet.class);

	/**
	 * 请求处理
	 * 
	 * @param request
	 *            请求头
	 * @param response
	 *            响应头
	 * @throws ServletException
	 *             ServletException
	 * @throws IOException
	 *             IOException
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ApiOutBean outBean = null;
		String uri = request.getRequestURI();
		String[] sa = uri.substring(uri.lastIndexOf("/") + 1).split("\\.");

		try {
			UserBean userBean = Context.getUserBean(request);
			if (userBean == null) {
				String token = RequestUtils.getStr(request, "token");
				if (token.equals("")) {
					throw new TipException("缺少用户唯一标识");
				}
				userBean = UserMgr.getUser(token);
				Context.setOnlineUser(request, userBean); // 登录成功
			}
			
			for (Method method : this.getClass().getMethods()) {
				if (method.isAnnotationPresent(URLAnno.class)) {
					URLAnno urlAnno = method.getAnnotation(URLAnno.class);
					if (urlAnno.value().equals(sa[0])) {
						ApiParamBean paramBean = new ApiParamBean(request);
						outBean = (ApiOutBean) method.invoke(this, paramBean);
						break;
					}
				}
			}
		} catch (InvocationTargetException e) {
			log.error(e.getMessage(), e);
			outBean = new ApiOutBean();
			outBean.setCode(ApiConstant.RTN_CODE_ENUM.CODE_002.getCode());
			outBean.setMessage(ApiConstant.RTN_CODE_ENUM.CODE_002.getValue());
			outBean.setData(new Bean().set(ApiOutBean.ERR_INFO, e.getTargetException().getMessage()));
		} catch (TipException e) {
			log.error(e.getMessage(), e);
			outBean = new ApiOutBean();
			outBean.setCode(ApiConstant.RTN_CODE_ENUM.CODE_002.getCode());
			outBean.setMessage(ApiConstant.RTN_CODE_ENUM.CODE_002.getValue());
			outBean.setData(new Bean().set(ApiOutBean.ERR_INFO, e.getMessage()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			outBean = new ApiOutBean();
			outBean.setCode(ApiConstant.RTN_CODE_ENUM.CODE_002.getCode());
			outBean.setMessage(ApiConstant.RTN_CODE_ENUM.CODE_002.getValue());
			outBean.setData(new Bean().set(ApiOutBean.ERR_INFO, e.getMessage()));
		}

		if (!response.isCommitted()) {
			String result = outBean.output();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gout = new GZIPOutputStream(bos);
			gout.write(result.getBytes("UTF-8"));
			gout.close();
			byte dest[] = bos.toByteArray();
			response.setHeader("Content-Encoding", "gzip");// 告诉浏览器，当前发送的是gzip格式的内容
			response.setContentType("text/json; charset=utf-8");
			String myOrigin = request.getHeader("origin");
			String allowOrigin = GwUtils.ifWhiteList(myOrigin);
			response.setHeader("Access-Control-Allow-Origin", allowOrigin);
			response.addHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Methods", "POST,GET");
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With,content-type");
			OutputStream out = response.getOutputStream();
			out.write(dest);
			out.flush();
			out.close();
		}
	}

	@URLAnno(value = "getServDef")
	public ApiOutBean getServDef(ApiParamBean paramBean) {
		IFormServ formServ = new FormServImpl();
		return formServ.getServDef(paramBean.getStr("servId"));
	}

	@URLAnno(value = "getDictDef")
	public ApiOutBean getDictDef(ApiParamBean paramBean) {
		IFormServ formServ = new FormServImpl();
		return formServ.getDictDef(paramBean.getStr("dictId"));
	}

	@URLAnno(value = "saveServDef")
	public ApiOutBean saveServDef(ApiParamBean paramBean) {
		IFormServ formServ = new FormServImpl();
		return formServ.saveServDef(paramBean);
	}

	@URLAnno(value = "getServList")
	public ApiOutBean getServList(ApiParamBean paramBean) {
		IFormServ formServ = new FormServImpl();
		return formServ.getServList();
	}

	@URLAnno(value = "createServ")
	public ApiOutBean createServ(ApiParamBean paramBean) {
		IFormServ formServ = new FormServImpl();
		return formServ.createServ(paramBean);
	}

	@URLAnno(value = "getServerUrl")
	public ApiOutBean getServerUrl(ApiParamBean paramBean) {
		IFormServ formServ = new FormServImpl();
		return formServ.getServerUrl(paramBean);
	}
}
