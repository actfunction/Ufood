package com.rh.core.comm.sso;

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

import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.util.RequestUtils;

/**
 * 用作身份认证
 * 
 * @author zjl
 *
 */
public class RhssoFilter implements Filter {

	/** log */
	private static Log log = LogFactory.getLog(RhssoFilter.class);

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		HttpServletRequest request = null;
		HttpServletResponse response = null;

		UserBean userBean = null;

		try {
			request = (HttpServletRequest) req;
			response = (HttpServletResponse) res;

			request.setCharacterEncoding("UTF-8");

			userBean = Context.getUserBean(request);

			if (userBean == null) {
				RequestUtils.sendDir(response, "/sy/comm/login/jumpToIndex.jsp");
				res.flushBuffer();
			}

			chain.doFilter(req, res);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
