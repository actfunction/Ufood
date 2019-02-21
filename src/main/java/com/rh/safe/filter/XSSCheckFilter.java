package com.rh.safe.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Context;
import com.rh.core.comm.ConfMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;

/**
 * XSS 检查过滤器
 */
public class XSSCheckFilter implements Filter {
	private Log log = LogFactory.getLog(XSSCheckFilter.class);
	// 需要拦截的JS字符关键字
	private String errorPath = "";

	// 非法XSS 字符
	private static final String[] SAFE_LESS = { "set-cookie", "<", "%3c", "%3e", ">", "%27", "+", "%22",
			"alert(", "||", "eval(" };

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.setErrorPath(filterConfig.getInitParameter("errorPath"));
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		boolean isSafe = true;

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// (为解决漏洞扫描的问题)添加Referer 校验 add by lyf 2017-11-15
		if (ConfMgr.getConf("SY_HOST_REFERER_CHECK", false) && !checkReferer(request)) {
			response.sendError(403, "非法访问！");
			return;
		}

		// iFrame跨域Session丢失问题
		log.debug("---------------------Header设置P3P，防止iFrame跨域Session丢失-------------------------");
		response.setHeader("P3P", "CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");

		String requestUrl = request.getRequestURI();
		String queryStr = request.getQueryString();
		try {
			if (StringUtils.isNotBlank(queryStr)) {
				queryStr = URLDecoder.decode(queryStr, "UTF-8").replaceAll("'", "").replaceAll("\"", "");
			}
		} catch (Exception ignored) {
		}

		if (!isSafeStr(requestUrl) || !isSafeStr(queryStr)) {
			isSafe = false;
			log.error("非法字符：requestUrl=" + requestUrl + ", queryStr=" + queryStr);
		} else { // 检查post参数
			Enumeration<?> names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				if (!"md5_bean".equals(key)) {
					String value = request.getParameter(key);
					if ("data".equals(key)) {
						value = URLEncoder.encode(value, "UTF-8");
						value = URLDecoder.decode(value, "UTF-8").replaceAll("'", "").replaceAll("\"", "");
					}
					if (!isSafeStr(value)) {
						isSafe = false;
						log.error("非法字符：" + key + "=" + value);
						break;
					}
				}
			}
		}

		if (isSafe) {
			chain.doFilter(req, resp);
		} else {
			if (requestUrl.endsWith(".html") || requestUrl.endsWith(".jsp")) {
				request.setAttribute("error", "Url or params contains of illegal XSS character.");
				request.getRequestDispatcher(this.getErrorPath()).forward(request, response);
			} else {
				String errMsg = Context.getSyConf("SY_XSS_ERROR_MSG", "请求被阻止，链接或者参数中存在特殊字符，请联系管理员。");
				OutBean outBean = new OutBean();
				outBean.setError(errMsg);
				String header = "text/html; charset=utf-8";
				String content = JsonUtils.toJson(outBean, false, true); // 支持压缩空值输出
				response.setContentType(header);
				PrintWriter out = response.getWriter();
				out.write(content);
				out.flush();
				out.close();
			}

		}
	}

	/**
	 * 判断URL是否存在非法字符
	 */
	private boolean isSafeStr(String str) {
 		boolean result = true;
		if (StringUtils.isNotBlank(str)) {
			for (String s : SAFE_LESS) {
				if (str.toLowerCase().contains(s)) {
					log.error("检测出非法字符：" + s);
					result = false;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public void destroy() {

	}

	private String getErrorPath() {
		return errorPath;
	}

	private void setErrorPath(String errorPath) {
		this.errorPath = errorPath;
	}

	/**
	 * 解决跨站点伪造的漏洞问题
	 * 
	 * @param req
	 * @return
	 */
	private boolean checkReferer(HttpServletRequest req) {
		boolean isCheck = false;
		String referer = req.getHeader("Referer");
		if (StringUtils.isNotEmpty(referer)) {
			if (referer.startsWith("http://")) {
				referer = referer.substring("http://".length());
				String localHost = " ", localName = " ";
				try {
					localHost = InetAddress.getLocalHost().getHostAddress();
					localName = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				if (referer.startsWith(localHost) || referer.startsWith(localName)) {
					isCheck = true;
				}
				String[] hostNameArr = ConfMgr.getConf("SY_HOST_REFERER", "localhost,127.0.0.1,47.92.92.68")
						.split(Constant.SEPARATOR);
				for (String hostName : hostNameArr) {
					if (referer.startsWith(hostName)) {
						isCheck = true;
					}
				}
			}
		} else {// 保证Referer为空的时候能够通过
			isCheck = true;
		}
		return isCheck;
	}
}
