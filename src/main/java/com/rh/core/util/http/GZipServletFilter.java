package com.rh.core.util.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 压缩服务器端反馈给客户端数据的过滤器。常用于压缩js、css等
 * 
 * @author wanglong
 * 
 */
public class GZipServletFilter implements Filter {
	private Log log = LogFactory.getLog(GZipServletFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		// 获取用户当前访问的uri
		String requestURI = ((HttpServletRequest) request).getRequestURI();
		// 获取父url--如果不是直接输入的话就是先前的访问过来的页面，要是用户输入了，这个父url是不存在的
		String referer = ((HttpServletRequest) request).getHeader("REFERER");

		if (isLegalReq(referer, requestURI)) {
			if (acceptsGZipEncoding(httpRequest)) {
				httpResponse.addHeader("Content-Encoding", "gzip");
				GZipServletResponseWrapper gzipResponse = null;
				try {
					gzipResponse = new GZipServletResponseWrapper(httpResponse);
					chain.doFilter(request, gzipResponse);
				} finally {
					if (gzipResponse != null) {
						gzipResponse.close();
					}
				}
			} else {
				chain.doFilter(request, response);
			}
		} else {
			httpResponse.sendError(502, "不支持的非法请求！");
			return;
		}
	}

	/**
	 * 判断请求的URI是否合法
	 * 
	 * @param referer    父url
	 * @param requestURI 用户访问的URI
	 * @return true/false
	 */
	private boolean isLegalReq(String referer, String requestURI) {
		boolean result = true;
		// 如果父URL为null并且当前请求是.js就拦截。
		if (null == referer && requestURI.endsWith(".js")) {
			log.error("已拦截非法请求：" + requestURI + ";referer：" + referer);
			result = false;
		}
		return result;
	}

	/**
	 * 判断客户端浏览器是否支持Gzip压缩
	 * 
	 * @param httpRequest 客户端请求
	 * @return 是否支持Gzip压缩
	 */
	private boolean acceptsGZipEncoding(HttpServletRequest httpRequest) {
		String acceptEncoding = httpRequest.getHeader("Accept-Encoding");
		return acceptEncoding != null && acceptEncoding.indexOf("gzip") != -1;
	}

}
