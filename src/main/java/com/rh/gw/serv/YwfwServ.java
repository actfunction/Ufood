package com.rh.gw.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;

import com.rh.gw.util.GwExtTabUtils;

/**
 * 业务发文扩展类
 * 
 * @author kfzx-linll
 */
public class YwfwServ extends GwExtServ {

	/**
	 * 根据前后页签的不同来删除数据库
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean deleteRalateTab(ParamBean paramBean) {
		GwExtTabUtils gwUtil = new GwExtTabUtils();
		return gwUtil.deleteRalateTab(paramBean);
	}

	
	/**
	 * 根据流程实例ID和节点ID获得自定义变量
	 * 
	 * @param paramBean
	 * @return
	 */
	public OutBean getTabs(ParamBean paramBean) {
		GwExtTabUtils gwUtil = new GwExtTabUtils();
		return gwUtil.getTabs(paramBean);
	}
	
	/**
	 * 根据流程实例ID，得到流程跟踪信息
	 * @param paramBean 传入参数
	 * @return 流程跟踪信息
	 */
	public OutBean getWefFollowByPid(ParamBean paramBean){
 		 OutBean out = new OutBean(); //定义输出对象
 		 String pid = paramBean.getStr("pId"); //定义得到传入参数对象
 		 List<Bean> nodeList = ServDao.finds("SY_WFE_NODE_INST", "AND NODE_IF_RUNNING = 1 AND PI_ID = '" + pid + "'");
 		 
 		 String where = paramBean.getStr("where");
 		 SqlExecutor executor = Transaction.getExecutor(); //得到执行sql对象
 		 if ( !pid.equals("") ) { //如果参数不为空，则执行以下操作
 			String sql = "SELECT NODE_CODE,NODE_NAME,DONE_USER_NAME,DONE_DEPT_NAMES,TO_USER_NAME,PROC_CODE,PI_ID,NODE_BTIME,NODE_ETIME,OPEN_TIME FROM \"PLATFORM\".\"SY_WFE_NODE_INST\" WHERE PI_ID = '"+pid+"'"+where;
 		try {
 				List<Bean> wefFollowInfo = executor.query(sql); //定义 执行sql，用于保存结果信息对象
 				out.set("wefInfo", wefFollowInfo); //输出对象
 				out.set("nodeList", nodeList); //输出对象
 			} catch (Exception exception) {
 				out.setError("更新失败!");
 			} 
 		}
 		return out;
	}
}
