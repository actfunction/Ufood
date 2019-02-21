package com.rh.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.serv.ParamBean;
import com.rh.core.serv.util.SQLTransUtil;
import com.rh.core.util.Constant;

public class ApiServletUtils {
	
	/** log */
	private static Log log = LogFactory.getLog(DoServlet.class);
	private static final String _ext = "_extWhere";
	private static final String _link = "_linkWhere";
	private static final String _search = "_searchWhere";


	/**
	 * 转化SQL
	 * 
	 * @param paramBean
	 * 
	 * @return ParamBean
	 */
	public static void sysTransSql(ParamBean paramBean, String serv) {
		String [] transItemArr = {Constant.PARAM_WHERE, _ext, _link, _search};
		
		for (String transItem : transItemArr) {
			try {
				if (paramBean.isNotEmpty(transItem)) {
					String transWhere = paramBean.getStr(Constant.PARAM_WHERE);
					paramBean.setWhere(SQLTransUtil.trans(transWhere, serv));
				}
			} catch (Exception e) {
				log.info(transItem + e.getMessage(), e);
			}
		}

	}
}
