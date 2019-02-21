package com.rh.core;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.DeptBean;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.serv.ServDao;

public class ChangeSessionFilter implements Filter {

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws ServletException, IOException {
		HttpServletRequest request = (HttpServletRequest) req;
		String nid = request.getParameter("nid");
		String agentUser = request.getParameter("_AGENT_USER_");

		if (agentUser == null || agentUser.length() <= 0) {
			if (nid != null && nid.length() > 0) {
				Bean nodeInstBean = ServDao.find("SY_WFE_NODE_INST", nid);
				UserBean userBean = Context.getUserBean();
				if (nodeInstBean != null && nodeInstBean.isNotEmpty("TO_DEPT_ID")) {
					if (!userBean.getDeptCode().equals(nodeInstBean.getStr("TO_DEPT_ID"))) {
						List<UserBean> jgList = userBean.getList("JG_INFO");
						if (jgList != null && jgList.size() > 0) {
							boolean flag = false;
							for (UserBean jgBean : jgList) {
								if (jgBean.getDeptCode().equals(nodeInstBean.getStr("TO_DEPT_ID"))) {
									userBean.set("DEPT_CODE", jgBean.getDeptCode());
									userBean.set("TDEPT_CODE", jgBean.getTDeptCode());
									userBean.set("ODEPT_CODE", jgBean.getODeptCode());
									userBean.set("CODE_PATH", jgBean.getCodePath());
									userBean.set("DEPT_LEVEL", jgBean.getDeptBean().getLevel());
									userBean.set("DEPT_NAME", jgBean.getDeptName());
									userBean.set("DEPT_SORT", jgBean.getDeptBean().getSort());
									userBean.set("ROLE_CODES", jgBean.getStr("ROLE_CODES"));
									userBean.set("isMain", "2");
									flag = true;
									break;
								}
							}

							if (!flag) {
								DeptBean deptBean = OrgMgr.getDept(nodeInstBean.getStr("TO_DEPT_ID"));
								userBean.set("DEPT_CODE", deptBean.getCode());
								userBean.set("TDEPT_CODE", deptBean.getTDeptCode());
								userBean.set("ODEPT_CODE", deptBean.getODeptCode());
								userBean.set("CODE_PATH", deptBean.getCodePath());
								userBean.set("DEPT_LEVEL", deptBean.getLevel());
								userBean.set("DEPT_NAME", deptBean.getName());
								userBean.set("DEPT_SORT", deptBean.getSort());
								userBean.set("ROLE_CODES", userBean.getRoleCodeStr());
								userBean.set("isMain", "1");
							}
						}
					}
				}
			}
		}

		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

}
