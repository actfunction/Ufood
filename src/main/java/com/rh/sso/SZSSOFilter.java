package com.rh.sso;

import java.io.IOException;
import java.util.Map;

import com.rh.sso.util.IdentityToken;
import com.rh.sso.util.SSOUser;
import com.rh.sso.util.SSOserver;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;

/*
 * @auther kfzx-xuyj01
 */
public class SZSSOFilter implements Filter{
	/**
	 * 统一认证地址
	 */
	private String SSO_URL = Context.getSyConf("SSO_URL", "http://122.21.189.89:9006/aa/");
	private String OA_SY_URL = Context.getSyConf("OA_SY_URL", "http://localhost:8083");
	private String SSO_REFRESH_TOKEN_URL = Context.getSyConf("SSO_REFRESH_TOKEN_URL","auth/refreshToken");
	// todo 配置项
	private final static String SSO_TOKEN = Context.getSyConf("SSO_TOKEN", "sso_token");
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(SZSSOFilter.class);
	private RestTemplate restTemplate = new RestTemplate();
	/**
	 * 刷新区间
	 */
	private final int exp_distance = Context.getSyConf("SSO_EXP_DISTANCE", 1800000);
	private ObjectMapper mapper;
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws ServletException, IOException {
		HttpServletRequest request = null;
		HttpServletResponse response = null;
		UserBean userBean = null;
		
		try {
			request = (HttpServletRequest) req;
			response = (HttpServletResponse) res;
			response.setHeader("Cache-Control", "no-cache");
			request.setCharacterEncoding("UTF-8");
			StringBuffer urlParam = new StringBuffer();
			Map<String,String[]> map = request.getParameterMap();
			for ( Map.Entry<String,String[]> entry:map.entrySet()) {
				String[] values =(String[])entry.getValue();
				String value = values[0];
				urlParam.append(entry.getKey()).append("=").append(value).append("&");
			}
			//拼接跳转url
			String redirectUrl = OA_SY_URL + request.getRequestURI();

			if (urlParam.length() !=0) {//如果有参数
				String url  =urlParam.toString();
				url = url.substring(0, url.length()-1);//删除最后一个&符号
				redirectUrl = redirectUrl+"?" + url;
			}
			Boolean isSession = false;
			//判断session中是否存在用户信息
			if (request.getParameter("X-XSRF-TOKEN") != null) {
				userBean = Context.getUserBean(request, request.getParameter("X-XSRF-TOKEN"),
						request.getParameter("X-DEVICE-NAME"));
			} else {
				userBean = Context.getUserBean(request);
			}
			if (userBean != null) {
				autoRefreshToken(request);
				isSession = true;
			}else{
				//判断wps登录，直接返回错误信息
				if (request.getHeader("X-DEVICE-NAME") != null || request.getParameter("X-DEVICE-NAME") != null) { // 远程设备方式访问返回401错误码
					if(!response.isCommitted()) {
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
						response.flushBuffer();
					}
					//return;
				}
			}
			//没有用户信息，通过sso_token获取
			if (!isSession){
				// 从url参数获取sso_token

				String sso_token = getSSOTokenFromQueryString(request);
				if (StringUtils.isEmpty(sso_token)) {
					sso_token = request.getHeader(SSO_TOKEN);
				}
				if (StringUtils.isEmpty(sso_token)) {
					if(!response.isCommitted()) {
					
						response.sendRedirect(SSO_URL+"auth/login" + "?redirect_url="+redirectUrl);
						response.flushBuffer();
					}
				//	response.sendError(HttpStatus.BAD_REQUEST.value(), "sso_token is required.");
					//return;
				}
				try {
					SSOserver ssoService = new SSOserver();
					// 解析和验签
					SSOUser ssoToken = ssoService.verify(sso_token);
					// 建立用户
					request.getSession().setAttribute("user", ssoToken);
					String userLoginName= ssoToken.getUserLoginName();
					userBean = UserMgr.getUserByLoginName(userLoginName);
					request.getSession().setAttribute("sso_token", sso_token);
					isSession = true;
					if (null != userBean) {
						// 设用系统内在线用户信息
						Context.setOnlineUser(request, userBean);
						//todo多机构
						log.info("SZSSOFilter:SSOTOKEN VERIFY SUCCESS:" +userLoginName );
					
					}
				} catch (Exception e) {
					//todo 记录日志
					log.error("SZSSOFilter:SSOTOKEN VERIFY FAILED:"+e.getMessage());
					//response.sendRedirect(SSO_URL+"auth/login" + "?redirect_url="+redirectUrl);
					if(!response.isCommitted()) {
						response.sendError(HttpStatus.FORBIDDEN.value(), "Token is invaild");
						response.flushBuffer();
					}
					
				}
			}
	

			if (!isSession){
				if (isAjaxRequest(request)) {// ajax请求直接返回403
					log.error("SZSSOFilter:Session is invalid");
					if(!response.isCommitted()) {
						response.sendError(HttpStatus.FORBIDDEN.value(), "Session is invalid.");
						response.flushBuffer();
					}
				} else {// 引导用户去统一认证,并指定认证成功重定向回应用登录入口
					if(!response.isCommitted()) {
						response.sendRedirect(SSO_URL+"auth/login" + "?redirect_url="+redirectUrl);
						response.flushBuffer();
					}
				}
			}

		}catch(Exception e){
			e.printStackTrace();
			log.error("SZSSOFilter:LOGIN ERROR:" + e.getMessage());
			if(!response.isCommitted()) {
				response.sendError(HttpStatus.FORBIDDEN.value(), "Login Error");
				response.flushBuffer();
			}
		}
		chain.doFilter(req, res);
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}
	public static boolean isAjaxRequest(HttpServletRequest request) {
		String requestedWith = request.getHeader("x-requested-with");
		if (requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest")) {
			return true;
		} else {
			return false;
		}
	}
	
	private String getSSOTokenFromQueryString(HttpServletRequest request) {
		String queryStr = request.getQueryString();
		if (queryStr != null) {
			String[] paras = queryStr.split("&");
			for (int i = 0; i < paras.length; i++) {
				String[] para = paras[i].split("=");
				if (SSO_TOKEN.equals(para[0])) {
					return para[1];
				}
			}
		}
		return null;
	}
	//刷新token
	private void autoRefreshToken(HttpServletRequest request) throws Exception {
		HttpSession session = request.getSession();
		String token = (String) session.getAttribute("sso_token");
		//如果token不存在，则通过本系统首页登录，无需刷新
		if ("".equals(token)||null == token||token.isEmpty()) {
			return;
		}
		Jwt dt = JwtHelper.decode(token);
		ObjectMapper mapper = new ObjectMapper();
		String a = dt.getClaims();
		IdentityToken idToken = mapper.readValue(a.replaceAll("null", "true"), IdentityToken.class);
		long range = idToken.getExp() - System.currentTimeMillis();

		if (range < exp_distance) {
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("sso_token", token);
			HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
			ResponseEntity<Map> response = restTemplate.exchange(SSO_URL+SSO_REFRESH_TOKEN_URL, HttpMethod.GET, requestEntity,
					Map.class);
			Map result = response.getBody();
			String newToken = (String) ((Map) result.get("data")).get("sso_token");
			session.setAttribute("sso_token", newToken);
		}

	}
}
