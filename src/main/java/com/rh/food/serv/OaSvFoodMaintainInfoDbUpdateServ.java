package com.rh.food.serv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rh.food.util.GenUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.DateUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;

/**
  * 审计管理分系统行政办公管理子系统
 * @author: kfzx-hongyi
 * @date: 2018年11月15日 上午10:38:56
 * @version: V1.0
 * @description: TODO
 */
public class OaSvFoodMaintainInfoDbUpdateServ extends CommonServ {
	
	private static Log log=LogFactory.getLog(OaSvFoodMaintainInfoDbUpdateServ.class);
    /**
     * 卡片页查询对预定时间进行处理
     * (将数据库的日期和时间进行拼接 格式 yyyy-MM-dd HH:mm:ss)
     */
	@Override
	public void afterByid(ParamBean paramBean, OutBean outBean) {
		String ORDER_START_DATE=outBean.getStr("ORDER_START_DATE");
		String ORDER_START_TIME=outBean.getStr("ORDER_START_TIME");
		outBean.set("NEED_START_DATE_TIME", ORDER_START_DATE+" "+ORDER_START_TIME);
		String ORDER_END_DATE=outBean.getStr("ORDER_END_DATE");
		String ORDER_END_TIME=outBean.getStr("ORDER_END_TIME");
		outBean.set("NEED_END_DATE_TIME", ORDER_END_DATE+" "+ORDER_END_TIME);
	}

    /**
     * 列表对时间进行处理
     * (去掉时间秒的部分)
     */
	@Override
	protected void afterQuery(ParamBean paramBean, OutBean outBean) {
		System.out.println(outBean);
		List<Bean> dataList = outBean.getDataList();
		
		//查出部门和用户
//		String sql = "SELECT U.USER_NAME,D.DEPT_NAME,M.S_USER,M.S_DEPT "
//				+ "FROM SY_ORG_USER U,SY_ORG_DEPT D,(SELECT S_USER,S_DEPT FROM OA_SV_FOOD_MAINTAIN_INFO GROUP BY (S_USER,S_DEPT)) M " 
//				+ "WHERE U.USER_CODE=M.S_USER AND D.DEPT_CODE=M.S_DEPT";
//		List<Bean> userDapts = Transaction.getExecutor().query(sql);
        if(dataList.size()<1) {
        	return;
        }
		
		StringBuilder sql = new StringBuilder("SELECT a.MAINTAIN_ID,b.DEPT_NAME,USER_NAME FROM OA_SV_FOOD_MAINTAIN_INFO a,SY_ORG_DEPT b, SY_ORG_USER c WHERE a.S_DEPT=b.dept_code AND a.S_USER=c.user_code and a.MAINTAIN_ID IN ('");
		
		for(int i=0;i<dataList.size();i++) {
			Bean data = dataList.get(i);
			if(data.getStr("S_ATIME")!=null && data.getStr("S_ATIME").length()>0) {
				data.set("S_ATIME", data.getStr("S_ATIME").substring(0, 16));
			}
			if(data.getStr("S_MTIME")!=null && data.getStr("S_MTIME").length()>0) {
				data.set("S_MTIME", data.getStr("S_MTIME").substring(0, 16));
			}
			data.set("MAINTAIN_FLAG", "副食品管理");
			
			
			if(i==dataList.size()-1) {
				sql.append(data.get("MAINTAIN_ID")+"')");
			}else {
				sql.append(data.get("MAINTAIN_ID")+"','");
			}
		}
		Map<String,String> userMap = new HashMap<String,String>();
		Map<String,String> deptMap = new HashMap<String,String>();
		String out = null;
		List<Bean> beanList = Transaction.getExecutor().query(sql.toString());
		if(beanList!=null && !beanList.isEmpty()) {
			for(Bean bean:beanList) {
				userMap.put(bean.getStr("MAINTAIN_ID"), bean.getStr("USER_NAME"));
				deptMap.put(bean.getStr("MAINTAIN_ID"), bean.getStr("DEPT_NAME"));
			}
		}
		for(int i=0;i<dataList.size();i++) {
			Bean data = dataList.get(i);
			out = data.getStr("MAINTAIN_ID");
			data.set("USER_NAME", userMap.get(out));
			data.set("DEPT_NAME", deptMap.get(out));
		}
	}


	
	//重写save方法
	//@Override
	public OutBean savedb(ParamBean paramBean) {
		
		//String maintainId = paramBean.getStr("_PK_");
		//获取当前系统时间
		Long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String mTime = sdf.format(time);
		OutBean outBean = new OutBean();
		
		try {
			Transaction.begin();
			try {
				Bean mainParam = JsonUtils.toBean(paramBean.getStr("main"));
				String maintainId = mainParam.getStr("MAINTAIN_ID");
				//对维护表数据进行更新
				Bean maintainBean = ServDao.find(paramBean.getStr("serv"), maintainId);
				
				if(mainParam.getStr("MAINTAIN_TITLE")!=null && !mainParam.getStr("MAINTAIN_TITLE").isEmpty()) {
					maintainBean.set("MAINTAIN_TITLE", mainParam.getStr("MAINTAIN_TITLE"));
				}
				
				//对维护表时间处理和更新
				String startDateTime = mainParam.getStr("NEED_START_DATE_TIME");
				String endDateTime = mainParam.getStr("NEED_END_DATE_TIME");
				if(startDateTime!=null && !startDateTime.isEmpty()) {
					maintainBean.set("ORDER_START_DATE", startDateTime.substring(0, 10));
					maintainBean.set("ORDER_START_TIME", startDateTime.substring(11,16));
				}
				if(endDateTime!=null && !endDateTime.isEmpty()) {
					maintainBean.set("ORDER_END_DATE", endDateTime.substring(0, 10));
					maintainBean.set("ORDER_END_TIME", endDateTime.substring(11,16));
				}

				if(mainParam.getStr("MAINTAIN_REMARK")!=null && !mainParam.getStr("MAINTAIN_REMARK").isEmpty()) {
					maintainBean.set("MAINTAIN_REMARK", mainParam.getStr("MAINTAIN_REMARK"));
				}
				
				if(mainParam.getStr("MAINTAIN_STATUS")!=null && !mainParam.getStr("MAINTAIN_STATUS").isEmpty()) {
					maintainBean.set("MAINTAIN_STATUS", mainParam.getStr("MAINTAIN_STATUS"));
					if("1".equals(mainParam.getStr("MAINTAIN_STATUS"))) {
						//删除公共待办
						deleteTodo(Transaction.getExecutor(), maintainId);
					}else {
						deleteTodo(Transaction.getExecutor(), maintainId);
						saveTodo("OA_SV_FOOD_MAINTAIN_INFO_ADD", maintainBean.getStr("MAINTAIN_TITLE"), maintainId);
					}
					
				}
				
				if(mTime!=null && !mTime.isEmpty()) {
					maintainBean.set("S_MTIME", mTime);
				}
				

				
				Bean whereBean = new Bean();
				whereBean.set("MAINTAIN_ID", maintainId);
				log.debug("待办修改副食品维护信息表信息！！！");
				ServDao.updates(paramBean.getStr("serv"), maintainBean, whereBean);
				
				//对维护列表数据分成更新数据和新增数据
				List<Bean> foodList = JsonUtils.toBeanList(paramBean.getStr("FOOD_LIST"));
				List<Bean> updateFoodList = new ArrayList<Bean>();
				List<Bean> newFoodList = new ArrayList<Bean>();
				for (int i=0;i<foodList.size();i++) {
					if(!"".equals(foodList.get(i).get("FOOD_ID"))) {
						updateFoodList.add(foodList.get(i));
					}else {
						newFoodList.add(foodList.get(i));
					}
				}
				//对维护列表进行更新
				for (int i=0;i<updateFoodList.size();i++) {
					Bean foodBean = ServDao.find("OA_SV_FOOD_MAINTAIN_DET_CHILD", updateFoodList.get(i).getStr("_PK_"));
					if(foodBean.getStr("FOOD_NAME")!=null && !foodBean.getStr("FOOD_NAME").isEmpty()) {
						foodBean.set("FOOD_NAME", updateFoodList.get(i).getStr("FOOD_NAME"));
					}
					if(foodBean.getStr("FOOD_TYPE")!=null && !foodBean.getStr("FOOD_TYPE").isEmpty()) {
						foodBean.set("FOOD_TYPE", updateFoodList.get(i).getStr("FOOD_TYPE"));
					}
					if(foodBean.getStr("FOOD_STOCK")!=null && !foodBean.getStr("FOOD_STOCK").isEmpty()) {
						foodBean.set("FOOD_STOCK", updateFoodList.get(i).getStr("FOOD_STOCK"));
					}
					if(foodBean.getStr("FOOD_LIMIT_NUMBER")!=null && !foodBean.getStr("FOOD_LIMIT_NUMBER").isEmpty()) {
						foodBean.set("FOOD_LIMIT_NUMBER", updateFoodList.get(i).getStr("FOOD_LIMIT_NUMBER"));
					}
					if(foodBean.getStr("FOOD_PRICE")!=null && !foodBean.getStr("FOOD_PRICE").isEmpty()) {
						foodBean.set("FOOD_PRICE", updateFoodList.get(i).getStr("FOOD_PRICE"));
					}
					if(mTime!=null && !mTime.isEmpty()) {
						foodBean.set("S_MTIME", mTime);
					}
					Bean upWhereBean = new Bean();
					upWhereBean.set("FOOD_ID", updateFoodList.get(i).getStr("_PK_"));
					ServDao.updates("OA_SV_FOOD_MAINTAIN_DET_CHILD", foodBean, upWhereBean);
				}
				for (int i=0;i<newFoodList.size();i++) {
					Bean newFood = newFoodList.get(i);
					String newFoodId = GenUtil.getAutoIdNumner("OSFMD", "OA_SV_FOOD_MAINTAIN_DET_SEQ", "NUM");
					newFood.set("FOOD_ID", newFoodId);
					log.debug("待办修改副食品维护食品明细表信息！！！");
					ServDao.save("OA_SV_FOOD_MAINTAIN_DET_CHILD", newFoodList.get(i));
				}
				
				//对领取时间表更新和新增
				List<Bean> receiveList = JsonUtils.toBeanList(paramBean.getStr("TIME_LIST"));
				List<Bean> updateReceiveList = new ArrayList<Bean>();
				List<Bean> newReceiveList = new ArrayList<Bean>();
				for (int i=0;i<receiveList.size();i++) {
					if(!"".equals(receiveList.get(i).get("_PK_"))) {
						updateReceiveList.add(receiveList.get(i));
					}else {
						newReceiveList.add(receiveList.get(i));
					}
				}
				for (int i=0;i<updateReceiveList.size();i++) {
					//截取时间
					String obtainStartDateTime = updateReceiveList.get(i).getStr("OBTAIN_START_DATETIME");
					String obtainEndDateTime = updateReceiveList.get(i).getStr("OBTAIN_END_DATETIME");
					String receiveId = updateReceiveList.get(i).getStr("_PK_");
					
					Bean receiveBean = ServDao.find("OA_SV_FOOD_RECEIVE_INFO_CHILD", receiveId);
					if(receiveBean.getStr("OBTAIN_START_DATE")!=null && !receiveBean.getStr("OBTAIN_START_DATE").isEmpty()) {
						receiveBean.set("OBTAIN_START_DATE",  obtainStartDateTime.substring(0, 10));
					}
					if(receiveBean.getStr("OBTAIN_START_TIME")!=null && !receiveBean.getStr("OBTAIN_START_TIME").isEmpty()) {
						receiveBean.set("OBTAIN_START_TIME",  obtainStartDateTime.substring(11,16));
					}
					//OBTAIN_END_DATE
					if(receiveBean.getStr("OBTAIN_END_DATE")!=null && !receiveBean.getStr("OBTAIN_END_DATE").isEmpty()) {
						receiveBean.set("OBTAIN_END_DATE",  obtainEndDateTime.substring(0, 10));
					}
					if(receiveBean.getStr("OBTAIN_END_TIME")!=null && !receiveBean.getStr("OBTAIN_END_TIME").isEmpty()) {
						receiveBean.set("OBTAIN_END_TIME",  obtainEndDateTime.substring(11,16));
					}
					if(receiveBean.getStr("OPTIONAL_NUMBER")!=null && !receiveBean.getStr("OPTIONAL_NUMBER").isEmpty()) {
						receiveBean.set("OPTIONAL_NUMBER",  updateReceiveList.get(i).getStr("OPTIONAL_NUMBER"));
					}
					if(mTime!=null && !mTime.isEmpty()) {
						receiveBean.set("S_MTIME", mTime);
					}
					Bean reWhereBean = new Bean();
					reWhereBean.set("OBTAIN_TIME_ID", receiveId);
					ServDao.updates("OA_SV_FOOD_RECEIVE_INFO_CHILD", receiveBean, reWhereBean);
				}
				//对领取时间表新增
				for (int i=0;i<newReceiveList.size();i++) {
					Bean newReceive = newReceiveList.get(i);
					newReceive.set("OBTAIN_START_DATE", newReceive.getStr("OBTAIN_START_DATETIME").substring(0, 10));
					newReceive.set("OBTAIN_START_TIME", newReceive.getStr("OBTAIN_START_DATETIME").substring(11,16));
					newReceive.set("OBTAIN_END_DATE", newReceive.getStr("OBTAIN_END_DATETIME").substring(0, 10));
					newReceive.set("OBTAIN_END_TIME", newReceive.getStr("OBTAIN_END_DATETIME").substring(11,16));
					//设置默认已选人数"0"
					newReceive.set("SELECTED_NUMBER", "0");
					String newReceiveId = GenUtil.getAutoIdNumner("OSFRI", "OA_SV_FOOD_RECEIVE_INFO_SEQ", "NUM"); 
					newReceive.set("OBTAIN_TIME_ID", newReceiveId);
					log.debug("待办修改副食品维护领取时间表！！！");
					ServDao.save("OA_SV_FOOD_RECEIVE_INFO", newReceive);
				}
				//提交事物
				Transaction.commit();
				//显示数据
				Bean main = ServDao.find(paramBean.getServId(), maintainId);
				outBean.set("MAINTAIN_ID", main.get("MAINTAIN_ID"));
				outBean.set("MAINTAIN_TITLE", main.get("MAINTAIN_TITLE"));
				outBean.set("ORDER_START_DATE", main.get("ORDER_START_DATE"));
				outBean.set("ORDER_END_DATE", main.get("ORDER_END_DATE"));
				outBean.set("ORDER_START_TIME", main.get("ORDER_START_TIME"));
				outBean.set("ORDER_END_TIME", main.get("ORDER_END_TIME"));
				outBean.set("MAINTAIN_REMARK", main.get("MAINTAIN_REMARK"));
				outBean.set("MAINTAIN_STATUS", main.get("MAINTAIN_STATUS"));
				outBean.set("MAINTAIN_ADMIN", main.get("MAINTAIN_ADMIN"));
				outBean.set("MAINTAIN_BACKUP", main.get("MAINTAIN_BACKUP"));
				outBean.set("S_FLAG", main.get("S_FLAG"));
				outBean.set("S_USER", main.get("S_USER"));
				outBean.set("S_ATIME", main.get("S_ATIME"));
				outBean.set("S_MTIME", main.get("S_MTIME"));
				outBean.set("S_CMPY", main.get("S_CMPY"));
				outBean.set("S_DEPT", main.get("S_DEPT"));
				outBean.set("S_ODEPT", main.get("S_ODEPT"));
				outBean.set("S_TDEPT", main.get("S_TDEPT"));
				ParamBean listParam = new ParamBean();
				listParam.set("serv", "OA_SV_FOOD_MAINTAIN_DET_CHILD");
				listParam.set("act", "query");
				listParam.set("_linkWhere", " and MAINTAIN_ID='" + maintainId + "' and S_FLAG='1'");
				listParam.set("_linkServQuery", 2);
				listParam.set("_NOPAGE_", true);
				listParam.set("_TRANS_", false);
				outBean.set("FOOD_LIST", query(listParam));

				listParam.set("serv", "OA_SV_FOOD_RECEIVE_INFO_CHILD");
				outBean.set("TIME_LIST", receiveTimeConvert(query(listParam)));
				outBean.setOk();
			}catch(Exception ex) {
				Transaction.rollback();
				throw ex;
			}
			Transaction.end();
		}catch(Exception e) {
			log.error("修改副食品信息数据库错误："+e.getMessage()+","+e.getCause().getMessage(), e);
			outBean.setError();
			throw new TipException("数据库执行错误----"+e.getMessage()+"\r\n----"+e.getCause().getMessage()); 
		}
		return outBean;	
	}
	
	public void deletefood(ParamBean paramBean) {
		String pks=paramBean.getStr("pks");
		StringBuilder sb=new StringBuilder();
		sb.append("delete from OA_SV_FOOD_MAINTAIN_DET where FOOD_ID in (");
		
		String[] pkarr = pks.split(",");
		for(int i=0;i<pkarr.length;i++) {
			if(i==pkarr.length-1) {
				sb.append("'"+pkarr[i]+"'").append(")");
				break;
			}
			sb.append("'"+pkarr[i]+"'").append(",");
		}
		String DEL_SQL=sb.toString();
		try {
			int i=Transaction.getExecutor().execute(DEL_SQL);
		} catch (Exception e) {
			log.error("删除副食品信息数据库错误："+e.getMessage()+","+e.getCause().getMessage(), e);
			throw new TipException("数据库执行错误----"+e.getMessage()+"\r\n----"+e.getCause().getMessage()); 
		}
		
	}
	
	public void deleteTime(ParamBean paramBean) {
		String pks=paramBean.getStr("pks");
		StringBuilder sb=new StringBuilder();
		sb.append("delete from OA_SV_FOOD_RECEIVE_INFO where OBTAIN_TIME_ID in (");
		String[] pkarr = pks.split(",");
		for(int i=0;i<pkarr.length;i++) {
			if(i==pkarr.length-1) {
				sb.append("'"+pkarr[i]+"'").append(")");
				break;
			}
			sb.append("'"+pkarr[i]+"'").append(",");
		}
		String DEL_SQL=sb.toString();
		try {
			int i=Transaction.getExecutor().execute(DEL_SQL);
			
		} catch (Exception e) {
			log.error("删除副食品信息数据库错误："+e.getMessage()+","+e.getCause().getMessage(), e);
			throw new TipException("数据库执行错误----"+e.getMessage()+"\r\n----"+e.getCause().getMessage()); 
		}
		
	}
	/**
	 * 待办列表的删除
	 * 并删除子表的数据,删除平台代办列表数据
	 * @param paramBean
	 * @return
	 */
	public OutBean deletes(ParamBean paramBean) {
		OutBean outBean=new OutBean();
		if(paramBean.getStr("PKS")!=null && !paramBean.getStr("PKS").isEmpty()) {
			StringBuilder sb1=new StringBuilder();
			StringBuilder sb2=new StringBuilder();
			StringBuilder sb3=new StringBuilder();
			StringBuilder sb4=new StringBuilder();
			sb1.append("delete from OA_SV_FOOD_MAINTAIN_INFO where MAINTAIN_ID in (");
			sb2.append("delete from OA_SV_FOOD_MAINTAIN_DET where MAINTAIN_ID in (");
			sb3.append("delete from OA_SV_FOOD_RECEIVE_INFO where MAINTAIN_ID in (");
			sb4.append("delete from SY_COMM_TODO where TODO_OBJECT_ID1 in (");
			String[] pks=paramBean.getStr("PKS").split(",");
			for(int i=0;i<pks.length;i++) {
				if(i==pks.length-1) {
					sb1.append("'"+pks[i]+"'").append(")");
					sb2.append("'"+pks[i]+"'").append(")");
					sb3.append("'"+pks[i]+"'").append(")");
					sb4.append("'"+pks[i]+"'").append(")");
					break;
				}
				sb1.append("'"+pks[i]+"'").append(",");
				sb2.append("'"+pks[i]+"'").append(",");
				sb3.append("'"+pks[i]+"'").append(",");
				sb4.append("'"+pks[i]+"'").append(",");
			}
			
			String MAINTAIN_DET_SQL=sb1.toString();
			String MAINTAIN_INFO_SQL=sb2.toString();
			String RECEIVE_INFO_SQL=sb3.toString();
			String TODO_SQL=sb4.toString();
			try {
				try {
					
					Transaction.begin();
					Transaction.getExecutor().execute(MAINTAIN_INFO_SQL);
					Transaction.getExecutor().execute(RECEIVE_INFO_SQL);
					Transaction.getExecutor().execute(MAINTAIN_DET_SQL);
					Transaction.getExecutor().execute(TODO_SQL);
					Transaction.commit();
					outBean.setOk();
					log.debug("成功删除待办列表数据------"+"MAINTAIN_ID=["+paramBean.getStr("PKS")+"]");
					outBean.set("count", pks.length);
				} catch (Exception e) {
					Transaction.rollback();
					throw e;
				}
				Transaction.end();
			} catch (Exception e) {
				log.error("删除副食品信息数据库错误："+e.getMessage()+","+e.getCause().getMessage(), e);
				outBean.setError();
				throw new TipException("数据库执行错误----"+e.getMessage()+"\r\n----"+e.getCause().getMessage()); 
			}
		}
		return outBean;
	}
	
	/**
	 * @title: queryById
	 * @description: 副食品维护卡片列表数据查询
	 * @param param
	 * @return 返回数据中包含副食品维护主表及食品列表、领取时间列表
	 * @throws
	 */
	public Bean queryById(ParamBean param) {
		OutBean result = new OutBean();
		try {
			String oldId = param.getStr("MAINTAIN_ID");
			Bean main = ServDao.find(param.getServId(), oldId);
			//main.set("MAINTAIN_ID", GenUtil.getAutoIdNumner("OSFMI", "OA_SV_FOOD_MAINTAIN_INFO_SEQ", "NUM"));
			main.set("NEED_START_DATE_TIME", main.getStr("ORDER_START_DATE") + " " + main.getStr("ORDER_START_TIME"));
			main.set("NEED_END_DATE_TIME", main.getStr("ORDER_END_DATE") + " " + main.getStr("ORDER_END_TIME"));
			
			main.set("FOOD_LIST", queryFoodsByFK(oldId));
			main.set("TIME_LIST", queryReceiveTimeByFK(oldId));
			
			result.set("data", main);
			result.setOk();
			return result;
		} catch (Exception e) {
			log.error("数据库异常，查询失败" + e.getMessage());
			result.setError(e.getMessage());
			return result;
		}
	}
	
	
	public OutBean queryFoodsByFK(String fk) {
		
		ParamBean listParam = new ParamBean();
		listParam.set("serv", "OA_SV_FOOD_MAINTAIN_DET_CHILD");
		listParam.set("act", "query");
		listParam.set("_linkWhere", " and MAINTAIN_ID='" + fk + "' and S_FLAG='1'");
		listParam.set("_linkServQuery", 2);
		listParam.set("_NOPAGE_", true);
		listParam.set("_TRANS_", false);
		return query(listParam);
	}
	
	public OutBean queryReceiveTimeByFK(String fk) {
		ParamBean listParam = new ParamBean();
		listParam.set("serv", "OA_SV_FOOD_RECEIVE_INFO_CHILD");
		listParam.set("act", "query");
		listParam.set("_linkWhere", " and MAINTAIN_ID='" + fk + "' and S_FLAG='1'");
		listParam.set("_linkServQuery", 2);
		listParam.set("_NOPAGE_", true);
		listParam.set("_TRANS_", false);
		OutBean outBean = query(listParam);
		System.out.println(outBean);
		return receiveTimeConvert(query(listParam));
	}
	
	
	/**
	 * @title: receiveTimeConvert
	 * @description: 将数据库中分开的领取日期和时间字段拼接为完整的界面自定义日期时间字段
	 * @param outBean
	 * @return OutBean
	 * @throws
	 */
	private OutBean receiveTimeConvert(OutBean outBean) {
		if(outBean.size()<1||outBean==null) {
			return outBean;
		}
		List<Bean> dataList = outBean.getDataList();
		if (dataList != null && dataList.size() > 0) {
			for (Bean bean : dataList) {
				String OBTAIN_START_DATE = bean.getStr("OBTAIN_START_DATE");
				String OBTAIN_START_TIME = bean.getStr("OBTAIN_START_TIME");
				bean.put("OBTAIN_START_DATETIME", OBTAIN_START_DATE + " " + OBTAIN_START_TIME);
				String OBTAIN_END_DATE = bean.getStr("OBTAIN_END_DATE");
				String OBTAIN_END_TIME = bean.getStr("OBTAIN_END_TIME");
				bean.put("OBTAIN_END_DATETIME", OBTAIN_END_DATE + " " + OBTAIN_END_TIME);
			}
		}
		return outBean;
	}
	
	/**
	 * @title: deleteTodo
	 * @description: 删除公共待办
	 * @param sqlExecutor
	 * @param servId
	 * @param pkCode
	 * @throws Exception
	 * @return void
	 * @throws
	 */
	private void deleteTodo(SqlExecutor sqlExecutor, String pkCode) throws Exception {
		Bean todoBean = sqlExecutor.queryOne("SELECT TODO_OBJECT_ID2 FROM SY_COMM_TODO WHERE "+"TODO_OBJECT_ID1='" + pkCode + "'");
		if(todoBean==null) {
			return;
		}
		
		sqlExecutor.execute("DELETE FROM SY_WFE_NODE_INST WHERE NI_ID='" + todoBean.getStr("TODO_OBJECT_ID2") + "'");
		sqlExecutor.execute("DELETE FROM SY_COMM_TODO WHERE TODO_OBJECT_ID1='" + pkCode + "'");
		
	}
	
	/**
	 * @title: saveTodo @description: 保存公共待办 @param servId @param title @param
	 * pkCode @throws Exception @return void @throws
	 */
	public void saveTodo(String servId, String title, String pkCode) throws Exception {
		UserBean user = Context.getUserBean();
		String time = DateUtils.getDatetime();
		String nodeId = Lang.getUUID();

		Bean nodeData = new Bean();
		nodeData.set("NI_ID", nodeId); // 节点主键 此处填写待办表的TODO_OBJECT_ID2的值（必须相等）
		nodeData.set("NODE_NAME", "name"); // 节点名称
		nodeData.set("DONE_DESC", "正常结束");
		nodeData.set("S_MTIME", time); // 修改时间
		nodeData.set("NODE_BTIME", time); // 节点开始时间
		nodeData.set("DONE_USER_ID", user.getCode()); // 办理人员ID
		nodeData.set("DONE_USER_NAME", user.getName()); // 办理人员名称
		nodeData.set("DONE_DEPT_IDS", user.getDeptCode()); // 办理人员部门ID
		nodeData.set("DONE_DEPT_NAMES", user.getDeptName()); // 办理人员部门名称
		nodeData.set("DONE_TYPE", 1); // 1是自动正常停止
		nodeData.set("NODE_IF_RUNNING", 2); // 2是代表节点停止
		nodeData.set("TO_USER_ID", user.getCode()); // 处理人员ID
		nodeData.set("TO_USER_NAME", user.getName()); // 处理人员名称
		nodeData.set("OPEN_TIME", ""); // 打开时间
		ServDao.save("SY_WFE_NODE_INST", nodeData);

		TodoBean dataBean = new TodoBean();
		dataBean.setTitle(title);
		dataBean.setSender(user.getCode());
		dataBean.setOwner(user.getCode());
		dataBean.setCode(servId);
		dataBean.setCodeName("副食品维护");
		dataBean.setDraftDeptName(user.getDeptName());
		
		dataBean.setObjectId1(pkCode);
		dataBean.setCatalog(1);// 待办的类型，1,办件，2，阅件
		dataBean.setObjectId2(nodeId); // TODO_OBJECT_ID2 中存 分发的ID
		//dataBean.setUrl("OA_SV_FOOD_MAINTAIN_INFO_DB_UPDATE&uid="+user.getCode()+"&tTitle=待办编辑&dataId="+pkCode);
		TodoUtils.insert(dataBean);
	}

}
