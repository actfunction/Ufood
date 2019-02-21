package com.rh.food.serv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rh.core.base.Bean;
import com.rh.core.base.TipException;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;

import com.rh.food.util.GenUtil;

/***
 * 
  * 审计管理分系统行政办公管理子系统
 * @author: kfzx-zhangheng1
 * @date: 2018年11月15日 下午2:01:06
 * @version: V1.0
 * @description: OA_SV_FOOD_MAINTAIN_INFO_YB_UPDATE对应的服务类，处理增删改查业务
 */
public class OaSvFoodMaintainInfoYbUpdateServ extends CommonServ{

	/**
	 * @title: queryById
	 * @description: 副食品维护卡片列表数据查询
	 * @param param
	 * @return 返回数据中包含副食品维护主表及食品列表、领取时间列表
	 * @throws
	 */
	public Bean YbById(ParamBean param) {
		OutBean result = new OutBean();
		try {
			String oldId = param.getStr("MAINTAIN_ID");
			Bean main = ServDao.find(param.getServId(), oldId);
			//main.set("MAINTAIN_ID", GenUtil.getAutoIdNumner("OSFMI", "OA_SV_FOOD_MAINTAIN_INFO_SEQ", "NUM"));
			main.set("YB_UPDATE_ORDER_START_DATETIME", main.getStr("ORDER_START_DATE") + " " + main.getStr("ORDER_START_TIME"));
			main.set("YB_UPDATE_ORDER_END_DATETIME", main.getStr("ORDER_END_DATE") + " " + main.getStr("ORDER_END_TIME"));
			
			main.set("FOOD_DET", queryFoodsByFK(oldId));
			main.set("ORDER_RECEIVE", queryReceiveTimeByFK(oldId));
			
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
     * @title: 重写afterQuery
     * @descriptin:处理创建时间和更新时间的格式 YYYY-MM-dd HH:mm:ss 
     * @param  paramBean,outBean 
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
	  /**
     * 
     * @title: 重写afterByid
     * @descriptin:预定时间展示处理
     * @param  paramBean,outBean 
     */	
	@Override
	public void afterByid(ParamBean paramBean, OutBean outBean) {
		//预定时间展示处理
		String ORDER_START_DATE=outBean.getStr("ORDER_START_DATE");
		String ORDER_START_TIME=outBean.getStr("ORDER_START_TIME");
		outBean.set("YB_UPDATE_ORDER_START_DATETIME", ORDER_START_DATE+" "+ORDER_START_TIME);
		String ORDER_END_DATE=outBean.getStr("ORDER_END_DATE");
		String ORDER_END_TIME=outBean.getStr("ORDER_END_TIME");
		outBean.set("YB_UPDATE_ORDER_END_DATETIME", ORDER_END_DATE+" "+ORDER_END_TIME);
	}
   
	/**
	 * 
	 * @title: deleteMaintains
	 * @descriptin: 此方法用于多条维护数据的级联假删除（关联表的状态的修改），同时将假删除预订单的预定数量返还到维护单的食品列表的库存中
	 * @param @param paramBean接受前端传来的OA_SV_FOOD_MAINTAIN_INFO表的主键
	 * @param @return
	 * @return outBean封装了成功删除了几条数据
	 * @throws 删除失败的信息
	 */
	public OutBean deleteMaintains(ParamBean paramBean) {
		int count = 0;//标记成功删除记录数
		try {
			String pkCodes = paramBean.getStr("pkCodes");
			String[] codes = pkCodes.split(",");
			if(codes.length<1) {//没有记录
				return new OutBean().set("count", "0");
			}
			for (String code : codes) {//分别对每条maintain记录进行删除
				 String sql="update OA_SV_FOOD_MAINTAIN_INFO set MAINTAIN_STATUS='2',S_FLAG='2' where MAINTAIN_ID = '"+code +"'";
				 log.debug("已维护列表，执行维护单假删除");
				 int execute = Transaction.getExecutor().execute(sql);
				 if(execute>0 && isDeleteFood(code) && isDeleteOrder(code)) {//级联删除一条数据成功
					count++;
				 }
			}
		} catch (Exception e) {
			log.error("已办维护列表，删除异常信息----"+e.getMessage()+"\r\n----"+e.getCause().getMessage());
			throw new TipException("删除失败");
		}
		OutBean outBean = new OutBean();
	    outBean.set("count", count);
        return outBean;		
			
	}

	/**
	 * 
	 * @title: isDeleteFood
	 * @descriptin: 此方法用于维护单关联的食品列表和领取时间列表的假删除
	 * @param @param maintainId 需要删除表数据的主键
	 * @param @return
	 * @return boolean是否删除成功
	 * @throws 删除是否成功
	 */
	private boolean isDeleteFood(String maintainId) {
		
		try {
			//删除关联的领取时间列表数据
			 String reSql="update OA_SV_FOOD_RECEIVE_INFO set S_FLAG='2' where MAINTAIN_ID = '"+ maintainId +"'";
			 log.debug("已维护列表，执行领取时间表数据假删除");
			 Transaction.getExecutor().execute(reSql);
			 //删除关联的副食品明细单数据
			 String detSql="update OA_SV_FOOD_MAINTAIN_DET set S_FLAG='2' where MAINTAIN_ID = '"+ maintainId +"'";
			 log.debug("已维护列表，执行食品列表数据假删除");
			 Transaction.getExecutor().execute(detSql);
			return true;
		} catch (Exception e) {
			log.error("已办维护列表，食品列表和领取时间列表删除异常信息----"+e.getMessage()+"\r\n----"+e.getCause().getMessage());
			throw new TipException("删除失败");
		}	
	}
			
	/**
	 * 
	 * @title: isDeleteOrder
	 * @descriptin: 此方法执行预订单和预订明细单删除，并将预订单购买数量，添加到维护单库存上
	 * @param @param maintainId 需要删除表数据的主键
	 * @param @return
	 * @return 是否删除成功
	 * @throws 删除失败信息
	 */
	private boolean isDeleteOrder(String maintainId) {
		try {
			//查出关联的预订单数据
			SqlBean sqlBean = new SqlBean();
			sqlBean.selects("ORDER_ID");
			sqlBean.and("MAINTAIN_ID", maintainId);
			List<Bean> orderList = ServDao.finds("OA_SV_FOOD_ORDER_INFO", sqlBean);
			 if(orderList.size()>0) {
				//对每条预订单进行删除操作
				 for (Bean order : orderList) {
					 //副食品预订单删除操作
					 String sql="update OA_SV_FOOD_ORDER_INFO set S_FLAG='2' where ORDER_ID = '"+ order.getStr("ORDER_ID") +"'";
					 log.debug("已维护列表，执行食品预订单数据假删除");
					 int execute = Transaction.getExecutor().execute(sql);
					 if(execute>0) {//副食品预订单删除成功
						 //查出预订单对应的预定食品明细表
						 SqlBean sqlFoodbean = new SqlBean();
						 sqlFoodbean.selects("FOOD_ID,BUY_NUMBER");
						 sqlFoodbean.and("ORDER_ID", order.getStr("ORDER_ID"));
						 List<Bean> foodDets= ServDao.finds("OA_SV_FOOD_ORDER_DET", sqlFoodbean);
						 //返还购买数量到维护单库存中
						 if(foodDets.size()>0) {
							 for (Bean foodDet : foodDets) {
								 String foodId =  foodDet.getStr("FOOD_ID");
								 int foodNum = Integer.valueOf(foodDet.getStr("BUY_NUMBER"));
								 //对预订单明细表进行删除
								 log.debug("已维护列表，执行食品预订明细单数据假删除");
								 sql="update OA_SV_FOOD_MAINTAIN_DET set FOOD_STOCK=FOOD_STOCK+ "+foodNum+" where FOOD_ID = '"+ foodId +"'";
								 Transaction.getExecutor().execute(sql);
							}
						 }
					 }
				 }
				
			 }
			//副食品预订领取时间删除操作
			 String recSql ="update OA_SV_FOOD_ORDER_DET set S_FLAG='2' where MAINTAIN_ID = '"+ maintainId +"'";
			 Transaction.getExecutor().execute(recSql);
			 return true;
		} catch (Exception e) {
			log.error("已办维护列表，预订单和预订单明细单删除异常信息----"+e.getMessage()+"\r\n----"+e.getCause().getMessage());
			throw new TipException("删除失败");
		}	
			
	}
						
	/**
	 *  重写save方法
	 *  此方法执行维护单编辑页面的保存操作，分别对前段传来的数据进行分类，分别对更新和新增的数据做不同的保存处理
	 * @author kfzx-zhangheng1
	 * @param paramBean 接受前段传来被修改或新增的数据
	 * @return outBean 是否保存成功
	 */
	@Override
	public OutBean save(ParamBean paramBean) {
		
		//获取主键
		String _PK_ = paramBean.getStr("_PK_");//_pk_：系统主键，MAINTAIN_ID：是表的主键
		//获取当前系统时间
		Long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String mTime = sdf.format(time);
		OutBean outBean = new OutBean();
		
		try {
			//开启事物
			Transaction.begin();	
			try {
				//对维护表数据进行更新
				log.debug("已办维护编辑，获取食品维护数据");
				Bean maintainBean = ServDao.find(paramBean.getStr("serv"), _PK_);
				if(paramBean.getStr("MAINTAIN_TITLE")!=null && !paramBean.getStr("MAINTAIN_TITLE").isEmpty()) {
					maintainBean.set("MAINTAIN_TITLE", paramBean.getStr("MAINTAIN_TITLE"));
				}
				//对维护表时间处理和更新
				String startDateTime = paramBean.getStr("YB_UPDATE_ORDER_START_DATETIME");
				String endDateTime = paramBean.getStr("YB_UPDATE_ORDER_END_DATETIME");
				if(startDateTime!=null && !startDateTime.isEmpty()) {
					maintainBean.set("ORDER_START_DATE", startDateTime.substring(0, 10));
					maintainBean.set("ORDER_START_TIME", startDateTime.substring(11,16));
				}
				if(endDateTime!=null && !endDateTime.isEmpty()) {
					maintainBean.set("ORDER_END_DATE", endDateTime.substring(0, 10));
					maintainBean.set("ORDER_END_TIME", endDateTime.substring(11,16));
				}
				
				maintainBean.set("MAINTAIN_REMARK", paramBean.getStr("MAINTAIN_REMARK"));
			
				//更新修改时间
				if(mTime!=null && !mTime.isEmpty()) {
					maintainBean.set("S_MTIME", mTime);
				}
				Bean whereBean = new Bean();
				whereBean.set("MAINTAIN_ID", _PK_);
				log.debug("已办维护编辑，更新食品维护表数据");
				ServDao.updates(paramBean.getStr("serv"), maintainBean, whereBean);
				
				//对维护列表数据分成更新数据和新增数据
				List<Bean> foodList = paramBean.getList("OA_SV_FOOD_MAINTAIN_DET_CHILD");
				List<Bean> updateFoodList = new ArrayList<Bean>();//存放需要更新的数据
				List<Bean> newFoodList = new ArrayList<Bean>();//存放需要新增的数据
				for (int i=0;i<foodList.size();i++) {
					if(foodList.get(i).contains("_PK_")) {
						updateFoodList.add(foodList.get(i));
					}else {
						newFoodList.add(foodList.get(i));
					}
				}
				//对食品列表进行更新
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
					//设置修改时间
					if(mTime!=null && !mTime.isEmpty()) {
						foodBean.set("S_MTIME", mTime);
					}
					Bean upWhereBean = new Bean();
					upWhereBean.set("FOOD_ID", updateFoodList.get(i).getStr("_PK_"));
					log.debug("已办维护编辑，更新一条食品列表表数据");
					ServDao.updates("OA_SV_FOOD_MAINTAIN_DET_CHILD", foodBean, upWhereBean);
				}
				//对食品列表新增操作
				for (int i=0;i<newFoodList.size();i++) {
					Bean newFood = newFoodList.get(i);
					//设置新增食品主键
					String newFoodId = GenUtil.getAutoIdNumner("OSFMD", "OA_SV_FOOD_MAINTAIN_DET_SEQ", "NUM");
					newFood.set("FOOD_ID", newFoodId);
					log.debug("已办维护编辑，新增一条食品列表表数据");
					ServDao.save("OA_SV_FOOD_MAINTAIN_DET_CHILD", newFoodList.get(i));
				}
				
				//对领取时间表更新和新增
				List<Bean> receiveList = paramBean.getList("OA_SV_FOOD_RECEIVE_INFO_CHILD");
				List<Bean> updateReceiveList = new ArrayList<Bean>();//存放需要更新的数据
				List<Bean> newReceiveList = new ArrayList<Bean>();//处理需要新增的数据
				for (int i=0;i<receiveList.size();i++) {
					if(receiveList.get(i).contains("_PK_")) {
						updateReceiveList.add(receiveList.get(i));
					}else {
						newReceiveList.add(receiveList.get(i));
					}
				}
				//对领取时间进行更新
				for (int i=0;i<updateReceiveList.size();i++) {
					//截取时间
					String obtainStartDateTime = updateReceiveList.get(i).getStr("OBTAIN_START_DATETIME");
					String obtainEndDateTime = updateReceiveList.get(i).getStr("OBTAIN_END_DATETIME");
					String receiveId = updateReceiveList.get(i).getStr("_PK_");
					//查出旧数据
					Bean receiveBean = ServDao.find("OA_SV_FOOD_RECEIVE_INFO_CHILD", receiveId);
					if(receiveBean.getStr("OBTAIN_START_DATE")!=null && !receiveBean.getStr("OBTAIN_START_DATE").isEmpty()) {
						receiveBean.set("OBTAIN_START_DATE",  obtainStartDateTime.substring(0, 10));
					}
					if(receiveBean.getStr("OBTAIN_START_TIME")!=null && !receiveBean.getStr("OBTAIN_START_TIME").isEmpty()) {
						receiveBean.set("OBTAIN_START_TIME",  obtainStartDateTime.substring(11,16));
					}
					if(receiveBean.getStr("OBTAIN_END_DATE")!=null && !receiveBean.getStr("OBTAIN_END_DATE").isEmpty()) {
						receiveBean.set("OBTAIN_END_DATE",  obtainEndDateTime.substring(0, 10));
					}
					if(receiveBean.getStr("OBTAIN_END_TIME")!=null && !receiveBean.getStr("OBTAIN_END_TIME").isEmpty()) {
						receiveBean.set("OBTAIN_END_TIME",  obtainEndDateTime.substring(11,16));
					}
					if(receiveBean.getStr("OPTIONAL_NUMBER")!=null && !receiveBean.getStr("OPTIONAL_NUMBER").isEmpty()) {
						receiveBean.set("OPTIONAL_NUMBER",  updateReceiveList.get(i).getStr("OPTIONAL_NUMBER"));
					}
					//设置修改时间
					if(mTime!=null && !mTime.isEmpty()) {
						receiveBean.set("S_MTIME", mTime);
					}
					Bean reWhereBean = new Bean();
					reWhereBean.set("OBTAIN_TIME_ID", receiveId);
					log.debug("已办维护编辑，更新一条领取时间表数据");
					ServDao.updates("OA_SV_FOOD_RECEIVE_INFO_CHILD", receiveBean, reWhereBean);
				}
				//对领取时间表新增
				for (int i=0;i<newReceiveList.size();i++) {
					Bean newReceive = newReceiveList.get(i);
					//处理时间格式
					newReceive.set("OBTAIN_START_DATE", newReceive.getStr("OBTAIN_START_DATETIME").substring(0, 10));
					newReceive.set("OBTAIN_START_TIME", newReceive.getStr("OBTAIN_START_DATETIME").substring(11,16));
					newReceive.set("OBTAIN_END_DATE", newReceive.getStr("OBTAIN_END_DATETIME").substring(0, 10));
					newReceive.set("OBTAIN_END_TIME", newReceive.getStr("OBTAIN_END_DATETIME").substring(11,16));
					//设置默认已选人数"0"
					newReceive.set("SELECTED_NUMBER", "0");
					String newReceiveId = GenUtil.getAutoIdNumner("OSFRI", "OA_SV_FOOD_RECEIVE_INFO_SEQ", "NUM"); 
					newReceive.set("OBTAIN_TIME_ID", newReceiveId);
					log.debug("已办维护编辑，新增一条领取时间表数据");
					ServDao.save("OA_SV_FOOD_RECEIVE_INFO", newReceive);
				}
				//提交事物
				Transaction.commit();
				//显示数据
				Bean main = ServDao.find(paramBean.getServId(), _PK_);
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
				listParam.set("_linkWhere", " and MAINTAIN_ID='" + _PK_ + "' and S_FLAG='1'");
				listParam.set("_linkServQuery", 2);
				listParam.set("_NOPAGE_", true);
				listParam.set("_TRANS_", false);
				outBean.set("OA_SV_FOOD_MAINTAIN_DET_CHILD", query(listParam));

				listParam.set("serv", "OA_SV_FOOD_RECEIVE_INFO_CHILD");
				outBean.set("OA_SV_FOOD_RECEIVE_INFO_CHILD", query(listParam));
				outBean.setOk();
			}catch(Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
				//回滚事物
				Transaction.rollback();
				throw ex;
			}
			//关闭事物
			Transaction.end();
		}catch(Exception e) {
			log.error("已办维护编辑保存，异常信息----"+e.getMessage()+"\r\n----"+e.getCause().getMessage());
			//设置返回错误
			outBean.setError();
			throw new TipException("数据库执行错误----"+e.getMessage()+"\r\n----"+e.getCause().getMessage()); 
		}
		return outBean;	
	}

	/**
	* 
	* @title: getBuyNumOfOrder
	* @descriptin: 用于查找维护单一条副食品被订购的数量
	* @param @return
	* @return OutBean
	* @throws
	*/
	public OutBean getBuyNumOfOrder(ParamBean paramBean) {

		OutBean outBean = new OutBean();
	
		String MAINTAIN_ID = paramBean.getStr("MAINTAIN_ID");
		String FOOD_ID = paramBean.getStr("FOOD_ID");
	
		if(MAINTAIN_ID == null || "".equals(MAINTAIN_ID)) {
		return outBean.set("resultMsg", "MAINTAIN_ID为空");
		}
	
		if(FOOD_ID == null || "".equals(FOOD_ID)) {
		return outBean.set("resultMsg", "FOOD_ID为空");
		}
	
		try {
		String sql = "select sum(BUY_NUMBER) BUY_NUMBER from OA_SV_FOOD_ORDER_DET where MAINTAIN_ID='"+MAINTAIN_ID+"' and FOOD_ID='"+FOOD_ID+"' and s_flag='1'";
		Bean bean = Transaction.getExecutor().queryOne(sql);
		if(bean != null ) {
		String number = bean.getStr("BUY_NUMBER");
		return outBean.set("number", number);
		}
		} catch (Exception e) {
		log.error("获取副食品被订购的数量异常："+e.getMessage()+","+e.getCause().getMessage());
		return outBean.set("resultMsg", "获取副食品被订购的数量异常");
		}
	
		return outBean.set("resultMsg", "获取副食品被订购的数量异常");

	}
}
