package com.rh.gw.serv;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.todo.TodoServ;
import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;


/**
 * 公文系统办文依据
 * 
 * @author kfzx-zhanghao01
 * @date 2018/11/28
 */
public class ToDoExtendServ extends TodoServ {
	
	// 公文系统前缀
	private static final String OA_GW = "OA_GW";
	
	// 督查系统前缀
	private static final String OA_SUP = "OA_SUP";
	
	// 副食品系统前缀
	private static final String OA_FOOD = "OA_FOOD";
	
	// 公文待办表(视图)
	//private static final String SY_COMM_TODO_GW_V = "SY_COMM_TODO_GW_V";
	
	// 已办
	//private static final String SY_COMM_ENTITY = "SY_COMM_ENTITY";
	
	// 用户表
	private static final String SY_ORG_USER = "SY_ORG_USER";
	
	
	/**
	 * 待办工作(列表和搜索)
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean getMenuWaitData(ParamBean paramBean) {
		OutBean out = new OutBean();
		UserBean userBean = Context.getUserBean();
		String userCode = userBean.getCode();
		
		String limitStr = paramBean.getStr("limit");
		String pageStr = paramBean.getStr("page");
		String name = paramBean.getStr("reg1"); // 所属系统
		String column = paramBean.getStr("reg2"); // 分类
		
		SqlExecutor executor = Transaction.getExecutor();
		StringBuffer sql = new StringBuffer();
		
		sql.append("SELECT ST.*,SU.USER_NAME SEND_USER_NAME,SV.NODE_NAME PRO_NODE FROM ");
		sql.append("SY_COMM_TODO_ALL_V ST  ");
		sql.append("LEFT JOIN " + SY_ORG_USER + " SU ON SU.USER_CODE=ST.SEND_USER_CODE ");
		sql.append("LEFT JOIN SY_WFE_PRE_NODE_V SV ON SV.NI_ID=ST.TODO_OBJECT_ID2 ");
		sql.append("WHERE ST.OWNER_CODE = '" + userCode + "' ");
	    sql.append("AND (ST.S_FLAG ISNULL OR ST.S_FLAG NOT IN (3)) ");
	    sql.append("AND ST.TODO_CATALOG = 1 ");
		// 所属系统
		if ("公文".equals(name)) {
			// 公文系统
			sql.append("AND SERV_ID LIKE '" + OA_GW + "%' ");
		} else if ("督查".equals(name)) {
			// 督查系统
			sql.append("AND SERV_ID LIKE '" + OA_SUP + "%' ");
		} else if ("副食品".equals(name)) {
			// 副食品系统
			sql.append("AND SERV_ID LIKE '" + OA_FOOD + "%' ");
		}
		
	    int limit = limitStr.equals("") ? 10 : Integer.parseInt(limitStr);
	    int page = pageStr.equals("") ? 1 : Integer.parseInt(pageStr);
	    int offset = (page - 1)*limit + 1;
	    
	    // 搜索不为空
	    if (!"".equals(column) && !"全部".equals(column)) {
	    	sql.append("AND TODO_CODE_NAME LIKE '%" + column + "%' ");
	    } 	    	
	    
	    sql.append("ORDER BY TODO_SEND_TIME DESC ");
		log.debug("SQL语句：" + sql);
		
		try {
			List<Bean> list = executor.query(sql.toString(), offset, limit);
			String sqlCount = "SELECT COUNT(TODO_ID) FROM (" + sql.toString() + ")";
			Bean queryOne = executor.queryOne(sqlCount);
			
			for (Bean bean : list) {
				ParamBean sendData = new ParamBean();
				sendData.set("_PK_", bean.getStr("TODO_OBJECT_ID1"));
				String servId = bean.getStr("SERV_ID");
				if(servId.length() > 0){
					Bean servBean = ServDao.find(servId, sendData);
					if(servBean != null){
						bean.set("S_UNAME",servBean.getStr("S_UNAME"));
						bean.set("S_TNAME",servBean.getStr("S_TNAME"));
					}
				}
			}
			out.set("offset", offset);
			out.set("count", queryOne.getStr("COUNT"));
			out.set("adviceList", list);
		} catch (Exception e) {
			out.setError("查询失败");
		}
		return out;
	}
	
	
	/**
	 * 公文管理中的待办(列表和搜索)
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean getWaitData(ParamBean paramBean) {
		OutBean out = new OutBean();
		UserBean userBean = Context.getUserBean();
		String userCode = userBean.getCode();
		
		SqlExecutor executor = Transaction.getExecutor();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ST.*,SU.USER_NAME SEND_USER_NAME,SV.NODE_NAME PRO_NODE FROM ");
		sql.append("SY_COMM_TODO_V ST  ");
		sql.append("LEFT JOIN " + SY_ORG_USER + " SU ON SU.USER_CODE=ST.SEND_USER_CODE ");
		sql.append("LEFT JOIN SY_WFE_PRE_NODE_V SV ON SV.NI_ID=ST.TODO_OBJECT_ID2 ");
		sql.append("WHERE ST.OWNER_CODE = '" + userCode + "' ");
	    sql.append("AND TODO_OBJECT_ID1 IN (SELECT GW_ID FROM OA_GW_GONGWEN WHERE S_FLAG=1)  ");
	    sql.append("AND TODO_CATALOG = 1 ");
	    String limitStr = paramBean.getStr("limit");
	    String pageStr = paramBean.getStr("page");
	    String name = paramBean.getStr("reg1");
	    String column = paramBean.getStr("reg2");
	    
	    int limit = limitStr.equals("") ? 10 :Integer.parseInt(limitStr);
	    int page = pageStr.equals("") ? 1 :Integer.parseInt(pageStr);
	    int offset = (page - 1)*limit + 1;
	    
	    // 搜索不为空
	    if (StringUtils.isNotEmpty(column)) {
	    	sql.append("AND " + name + " LIKE '%" + column + "%' ");
	    }
	    sql.append("ORDER BY TODO_SEND_TIME DESC ");
		log.debug("SQL语句：" + sql);
		
		try {
			List<Bean> list = executor.query(sql.toString(), offset, limit);
			String sqlCount = "SELECT COUNT(TODO_ID) FROM (" + sql.toString() + ")";
			Bean queryOne = executor.queryOne(sqlCount);
			
			for (Bean bean : list) {
				ParamBean sendData = new ParamBean();
				sendData.set("_PK_", bean.getStr("TODO_OBJECT_ID1"));
				Bean servBean = ServDao.find(bean.getStr("SERV_ID"), sendData);
				if(servBean != null){
					bean.set("S_UNAME",servBean.getStr("S_UNAME"));
					bean.set("S_TNAME",servBean.getStr("S_TNAME"));
				}
			}
			
			out.set("offset", offset);
			out.set("count", queryOne.getStr("COUNT"));
			out.set("adviceList", list);
		} catch (Exception e) {
			out.setError("查询失败");
		}
		return out;
	}
	
	
	/**
	 * 已办(列表和搜索)
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean getAlreadyData(ParamBean paramBean) {
		OutBean out = new OutBean();
		UserBean userBean = Context.getUserBean();
		String userCode = userBean.getCode();
		
		SqlExecutor executor = Transaction.getExecutor();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * ");
		sql.append("FROM (SELECT RANK () ");
		sql.append("OVER (PARTITION BY owner_code, todo_object_id1 ");
		sql.append("ORDER BY todo_send_time DESC) ");
		sql.append("rn,");
		sql.append("ST.*,SU.USER_NAME SEND_USER_NAME,SV.NODE_NAME PRO_NODE ");
		sql.append("FROM SY_COMM_TODO_GW_HIS_V ST,SY_ORG_USER SU,SY_WFE_PRE_NODE_V SV ");
		sql.append("WHERE ST.OWNER_CODE = '" + userCode + "' ");
		sql.append("AND SU.USER_CODE=ST.SEND_USER_CODE ");
		sql.append("AND SV.NI_ID=ST.TODO_OBJECT_ID2  ");
		sql.append("AND TODO_CATALOG = 1 ");
		sql.append("AND TODO_OBJECT_ID1 IN (SELECT GW_ID FROM OA_GW_GONGWEN WHERE S_FLAG=1 OR S_FLAG=4) ");
		
	    String limitStr = paramBean.getStr("limit");
	    String pageStr = paramBean.getStr("page");
	    String name = paramBean.getStr("reg1");
	    String column = paramBean.getStr("reg2");
	    
	    int limit = limitStr.equals("") ? 10 :Integer.parseInt(limitStr);
	    int page = pageStr.equals("") ? 1 :Integer.parseInt(pageStr);
	    int offset = (page - 1)*limit + 1;
	    
	    // 搜索不为空
	    if (StringUtils.isNotEmpty(column)) {
	    	sql.append("AND " + name + " LIKE '%" + column + "%' ");
	    }
	    sql.append("ORDER BY ST.TODO_FINISH_TIME DESC ");

		sql.append(")WHERE rn = 1 ");
		
		try {
			List<Bean> list = executor.query(sql.toString(),offset,limit);
			String sqlCount = "SELECT count(TODO_ID) FROM (" + sql.toString() + ")";
			Bean queryOne = executor.queryOne(sqlCount);
			out.set("offset", offset);
			out.set("count", queryOne.getStr("COUNT"));
			out.set("adviceList", list);
		} catch (Exception e) {
			out.setError("查询失败");
		}
		return out;
	}
	
	
	/**
	 * 阅文列表(列表和搜索)
	 * 
	 * @param paramBean
	 * @return OutBean
	 */
	public OutBean getReadData(ParamBean paramBean) {
		OutBean out = new OutBean();
		UserBean userBean = Context.getUserBean();
		String userCode = userBean.getCode();
		
		SqlExecutor executor = Transaction.getExecutor();
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("SELECT * FROM ");
		sql.append("(SELECT RANK() ");
		sql.append("OVER (PARTITION BY OWNER_CODE,TODO_ID ");
		sql.append("ORDER BY GW.S_MTIME DESC ");
		sql.append(")RN,GW.*,SU.USER_NAME SEND_USER_NAME ");
		sql.append("FROM OA_GW_SEND_V GW,SY_ORG_USER SU ");		
		sql.append("WHERE OWNER_CODE = '" + userCode + "' ");
		sql.append("AND SU.USER_CODE=GW.SEND_USER_CODE ");
		
	    String limitStr = paramBean.getStr("limit");
	    String pageStr = paramBean.getStr("page");
	    String columnName = paramBean.getStr("reg1"); // 查询字段名
	    String columnValue = paramBean.getStr("reg2"); // 具体字段值
	    
	    int limit = limitStr.equals("") ? 10 :Integer.parseInt(limitStr);
	    int page = pageStr.equals("") ? 1 :Integer.parseInt(pageStr);
	    int offset = (page - 1)*limit + 1;
	    
	    
	    // 搜索不为空
	    if ("SEND_STATUS".equalsIgnoreCase(columnName)) { // 是否已阅
		    if (StringUtils.isNotEmpty(columnValue) && "已阅".equals(columnValue)) {
		    	// 已阅
		    	sql.append(" AND SEND_STATUS = '2' ");
		    } else if (StringUtils.isNotEmpty(columnValue) && "未阅".equals(columnValue)) {
		    	// 未阅
		    	sql.append(" AND SEND_STATUS = '1' ");
		    }
		    
		    // 全部
		    
	    } else if ("TMPL_CODE".equalsIgnoreCase(columnName)) { // 文件类型
	    	if (StringUtils.isNotEmpty(columnValue) && "行政公文".equals(columnValue)) {
	    		// 收文
	    		sql.append(" AND SERV_ID = 'OA_GW_GONGWEN_ICBC_XZFW' ");
	    	} else if (StringUtils.isNotEmpty(columnValue) && "业务公文".equals(columnValue)) {
	    		// 行政公文
	    		sql.append(" AND SERV_ID = 'OA_GW_GONGWEN_ICBC_YWFW' ");
	    	} else if (StringUtils.isNotEmpty(columnValue) && "收文".equals(columnValue)) {
	    		// 业务公文
	    		sql.append(" AND SERV_ID = 'OA_GW_GONGWEN_ICBCSW' ");
	    	} else if (StringUtils.isNotEmpty(columnValue) && "签报".equals(columnValue)) {
	    		// 签报
	    		sql.append(" AND SERV_ID = 'OA_GW_GONGWEN_ICBCQB' ");
	    	}
	    } else if ("SEND_TIME".equalsIgnoreCase(columnName)) { // 送阅时间 2018-12-18
	    	if (StringUtils.isNotEmpty(columnValue) && !"全部".equals(columnValue)) {
	    		// 给了时间,就查询当天时间
	    		sql.append(" AND TODO_SEND_TIME LIKE '" + columnValue + "%' ");
	    	}
	    	
	    	// 全部
	    }
	    
	    
	    sql.append("ORDER BY TODO_SEND_TIME DESC ");
	    sql.append(") WHERE RN = 1 ");
	    log.debug("SQL语句：" + sql);
		
		
		try {
			List<Bean> list = executor.query(sql.toString(), offset, limit);
			String sqlCount = "SELECT COUNT(TODO_ID) FROM (" + sql.toString() + ")";
			Bean queryOne = executor.queryOne(sqlCount);
			for (Bean bean : list) {
				ParamBean sendData = new ParamBean();
				sendData.set("_PK_", bean.getStr("TODO_OBJECT_ID1"));
				Bean servBean = ServDao.find(bean.getStr("SERV_ID"), sendData);
				if(servBean != null){
					bean.set("S_UNAME",servBean.getStr("S_UNAME"));
					bean.set("S_TNAME",servBean.getStr("S_TNAME"));
				}
			}
			
			out.set("offset", offset);
			out.set("count", queryOne.getStr("COUNT"));
			out.set("adviceList", list);
		} catch (Exception e) {
			out.setError("查询失败");
		}
		return out;
	}

}
