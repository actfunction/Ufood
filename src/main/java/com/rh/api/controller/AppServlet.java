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
import com.rh.api.app.IFlowServ;
import com.rh.api.app.impl.FlowServImpl;
import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.util.RequestUtils;
import com.rh.api.util.ApiConstant;

public class AppServlet extends HttpServlet {
	private static final long serialVersionUID = 4075153275125051860L;
	private static Log log = LogFactory.getLog(AppServlet.class);

	/**
	 * 请求处理
	 * 
	 * @param request 请求头
	 * @param response 响应头
	 * @throws ServletException ServletException
	 * @throws IOException IOException
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ApiOutBean outBean = null;
		String uri = request.getRequestURI();
		String[] sa = uri.substring(uri.lastIndexOf("/") + 1).split("\\.");

		try {
			if (!RequestUtils.getStr(request, "noAuth").equals("yes")) {
				anthUser(request, response);
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

	/** 用户验证 */
	private UserBean anthUser(HttpServletRequest request, HttpServletResponse response) throws TipException {
		UserBean userBean = Context.getUserBean(request);
		// if (userBean == null) {
		if (RequestUtils.getStr(request, "uid").equals("")) {
			throw new TipException("缺少用户唯一标识");
		}
		/*String userCode = RequestUtils.getStr(request, "uid");
		if ("1P5Wo7ydQld19CYusmDT1BB".equals(userCode)) {
			userCode = "0ld3kCzIjN0FX2HWiSoRcSa";
		}
		try {
			userBean = UserMgr.getUser(userCode);
		} catch (Exception e) {
			userBean = UserMgr.getUserByMobileOrMail(userCode);
		}
		
		Context.setOnlineUser(request, userBean); // 登录成功
		*/ // }

		return userBean;
	}

	/** 获得常用流程列表 */
	@URLAnno(value = "getCommList")
	public ApiOutBean getCommList(ApiParamBean paramBean) {
		IFlowServ apiServ = new FlowServImpl();
		return apiServ.getCommList(paramBean);
	}

	/** 新增常用流程 */
	@URLAnno(value = "addComm")
	public ApiOutBean addComm(ApiParamBean paramBean) {
		IFlowServ apiServ = new FlowServImpl();
		return apiServ.addComm(paramBean);
	}

	/** 删除常用流程 */
	@URLAnno(value = "delCommList")
	public ApiOutBean delCommList(ApiParamBean paramBean) {
		IFlowServ apiServ = new FlowServImpl();
		return apiServ.delCommList(paramBean);
	}

	/** 获得流程类型列表 */
	@URLAnno(value = "getFlowTypeList")
	public ApiOutBean getFlowTypeList(ApiParamBean paramBean) {
		IFlowServ apiServ = new FlowServImpl();
		return apiServ.getFlowTypeList(paramBean);
	}

	/** 获得流程列表 */
	@URLAnno(value = "getFlowList")
	public ApiOutBean getFlowList(ApiParamBean paramBean) {
		IFlowServ apiServ = new FlowServImpl();
		return apiServ.getFlowList(paramBean);
	}

	/** 获得流程跟踪数据 */
	@URLAnno(value = "getWfeTrack")
	public ApiOutBean getWfeTrack(ApiParamBean paramBean) {
		IFlowServ apiServ = new FlowServImpl();
		return apiServ.getWfeTrack(paramBean);
	}

}
