package com.rh.food.serv;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.rh.food.util.thread.Callback;
import com.rh.food.util.thread.Message;
import com.rh.food.util.thread.SimpleAsyncExecutor;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.util.StringUtil;


import com.alibaba.fastjson.JSONObject;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.CommonServ;

import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.JsonUtils;

import com.rh.food.util.GenUtil;

/**
  * 审计管理分系统行政办公管理子系统
 * @author: kfzx-lilong
 * @date: 2018年11月15日 上午10:40:53
 * @version: V1.0
 * @description: TODO
 */
public class OaSvFoodOrderInfoServ extends CommonServ {
	 String[] foarr={"FOOD_ID","FOOD_TYPE","FOOD_NAME","FOOD_STOCK","FOOD_LIMIT_NUMBER","FOOD_PRICE","BUY_NUMBER","FOOD_T_STOCK"};
	 String[] rtarr={"OBTAIN_TIME_ID","LQKSSJ","LQJSSJ","SELECTED_NUMBER","RESIDUAL"};
	 String[] divids= {"OA_SV_FOOD_ORDER_DET_ORDER-viListViewBatch","OA_SV_FOOD_RECEIVE_INFO_ORDER-viListViewBatch"};
	 String[] tabnames= {"OA_SV_FOOD_ORDER_DET_ORDER", "OA_SV_FOOD_RECEIVE_INFO_ORDER"};
	 String[] cdArr= {"S_USER","S_FLAG","S_ATIME","S_MTIME","S_CMPY","S_DEPT","S_ODEPT","S_TDEPT"};
	 private static Log log = LogFactory.getLog(OaSvFoodOrderInfoServ.class);

	 SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 /**
	 * @throws SQLException 
	  * 
	  * @title: saveAll
	  * @descriptin: 解析前台传到后台的预定数据，进行更新操作
	  * @param @param paramBean
	  * @param @return
	  * @return OutBean
	  * mess:
	  * 1:购买食物（食物名）数量超过上限，最多只能购买（库存与限购数量取小）
	  * 2:选择的领取时间段人数已满，请重新选择
	  * 3:您已经在当前维护中预定过了，每位用户只能预定一次
	  * 4:发布成功
	  * @throws
	  */
 public OutBean saveAll(ParamBean paramBean) throws SQLException {
	 log.debug("将预定数据信息进行更新");
	 UserBean userBean = Context.getUserBean();
	 String usercode=userBean.getCode();
	 OutBean ob=new OutBean();
	 Map<String,Object> sdmap=new HashMap<>();
	 String code="1";
	 Connection conn = Transaction.getConn();
	 Transaction.getConn().setAutoCommit(false);
	 Transaction.begin();
	 try {
	  Bean param = JsonUtils.toBean(paramBean.getStr("param"));
	  String orderid=param.getStr("ORDER_ID");
	  String maintain_id=param.getStr("MAINTAIN_ID");
	  String fpsql="SELECT * FROM OA_SV_FOOD_MAINTAIN_INFO WHERE MAINTAIN_ID='"+maintain_id+"' FOR update;";
	  Transaction.getExecutor().query(fpsql);
	 //1：新增，2：修改
	  String flag=param.getStr("flag");
	  String cutime=sdf.format(new Date());
	 //循环每张表必有得字段
	 //通用个人信息，当前用户等
	 Map<String,Object> cmmap=getCMsql(userBean,maintain_id,cutime);
	 String cmcolsql=cmmap.get("colsql").toString();
	 String cmvalsql=cmmap.get("valsql").toString();
	 String  upgxsql=cmmap.get("upsql").toString();
	 //需要向数据库进行更新的数据
	  List<Map<String,Object>> sdList = param.getList("saveData");
	  //校验当前数据是否符合标准
	sdmap=validate(sdList,orderid,maintain_id,userBean.getCode(), flag);
	if("1".equals(sdmap.get("code"))) {  
	  //循环list向数据库更新数据
//	  for (Map<String, Object> map : sdList) {
//		String tabname=map.get("tabname").toString();
//		List<Map<String,Object>> collist=(List<Map<String, Object>>) map.get("cols");
//		code=new SimpleAsyncExecutor().call(new Task("sss") {
//			@Override
//			public String call() {
//				String code="1";
//			try {
//				 code=updateData(collist,tabname,cmcolsql,cmvalsql,flag,orderid,maintain_id,upgxsql,usercode,conn);
//				} catch (Exception e) {
//					e.printStackTrace();
//			}
//			return code;
//			}
//			});
//	  }
		
		 SimpleAsyncExecutor<Map<String,Object>, Boolean> simpleAsyncExecutor=new SimpleAsyncExecutor<Map<String,Object>, Boolean>();
	 	 Boolean tflag=simpleAsyncExecutor.execute(sdList,  new Callback<Map<String,Object>, Boolean>("orderInsert") {
	         @Override
	         public Boolean call(Map<String,Object> map, Message message) {
	        	 
	         	 try {
	         		String tabname=map.get("tabname").toString();
	        		List<Map<String,Object>> collist=(List<Map<String, Object>>) map.get("cols");
	         		String code=updateData(collist,tabname,cmcolsql,cmvalsql,flag,orderid,maintain_id,upgxsql,usercode,conn);
	         		if("0".equals(code)) {
	         			return false;
	         		}
	         		return true;
	         	 }catch(Exception e){
	        		e.printStackTrace();
	        		return false;
	        	 }
	             
	         }
	     });
	 	if(tflag) {
			Transaction.commit();
		}else {
			Transaction.rollback();
			 sdmap.put("code", code);
			 sdmap.put("mess", "更新数据失败，请联系管理员");
		}
	}
	sdmap.put("S_MTIME",cutime);
	
	 }catch (Exception e) {
		 Transaction.rollback();
		 sdmap.put("code", code);
		 sdmap.put("mess", "更新数据失败，请联系管理员");
		 e.printStackTrace();
	}finally {
		ob.put("result", sdmap);
	Transaction.end();
	}
	 return ob;
 }
	
/**
 * 
 * @title: getCMsql
 * @descriptin: 处理基础语句，
 * @param @param userBean
 * @param @param maintain_id
 * @param @return
 * @return Map<String,Object>
 * @throws
 */
private Map<String, Object> getCMsql(UserBean userBean, String maintain_id,String cutime) {
	Map<String,Object> map=new HashMap<>();
			String cmcolsql="S_USER,S_DEPT,S_ODEPT,S_TDEPT,S_CMPY,S_ATIME,S_MTIME,MAINTAIN_ID,S_FLAG,";
				 String user=userBean.getCode();
				 String dept=userBean.getDeptCode();
				 String tdept=userBean.getTDeptCode();
				 String odept=userBean.getODeptCode();
				 String cmpy=userBean.getCmpyCode();
			String cmvalsql="'"+user+"','"+dept+"','"+odept+"','"+tdept+"','"+cmpy+"','"+cutime+"','"+cutime+"','"+maintain_id+"','1',";
			String upsql="S_MTIME='"+cutime+"',";
			map.put("colsql", cmcolsql);
			map.put("valsql", cmvalsql);
			map.put("upsql", upsql);
		return map;
	}


/**
 * @param conn 
 * @param t 
 * 
 * @title: updateData
 * @descriptin: 将前台传来的预定数据更新到数据库中
 * @param @param collist
 * @param @param tabname
 * @param @param cmcolsql
 * @param @param cmvalsql
 * @param @param flag
 * @param @param orderid
 * @param @param maintain_id
 * @param @param upgxsql
 * @return void
 * @throws
 */
private String updateData(List<Map<String, Object>> collist, String tabname, String cmcolsql, String cmvalsql,
		String flag,String orderid,String maintain_id,String upgxsql,String usercode, Connection conn){
	String code="1";
	try {
	log.debug("将校验通过的预定数据信息插入数据库");
	//最后执行的语句载体
	String excusql="";
	String pkval=orderid;
	String buy_number="";
	String obtain_time_id="";
	for (Map<String, Object> map : collist) {
		cmcolsql+=map.get("colname").toString()+",";
		cmvalsql+="'"+map.get("colval").toString()+"',";
		upgxsql+=map.get("colname").toString()+"='"+map.get("colval").toString()+"',";
		if("FOOD_ID".equals(map.get("colname").toString())) {
			pkval=map.get("colval").toString();
		}
		
		if("BUY_NUMBER".equals(map.get("colname").toString())) {
			buy_number=map.get("colval").toString();
		}
		if("OBTAIN_TIME_ID".equals(map.get("colname").toString())) {
			obtain_time_id=map.get("colval").toString();
		}
	}
	cmcolsql=cmcolsql.substring(0,cmcolsql.length()-1);
	cmvalsql=cmvalsql.substring(0,cmvalsql.length()-1);
	upgxsql=upgxsql.substring(0,upgxsql.length()-1);
	//新增或更新OA_SV_FOOD_ORDER_INFO表的数据
	if("OA_SV_FOOD_ORDER_INFO".equals(tabname)) {
	//flag为1则进行新增预定信息
	if("1".equals(flag)) { 
		excusql="insert into "+tabname+"("+cmcolsql+",ORDER_ID) values("+cmvalsql+",'"+orderid+"')";
		String addsql="update OA_SV_FOOD_RECEIVE_INFO set SELECTED_NUMBER=SELECTED_NUMBER+1  where OBTAIN_TIME_ID = '"+obtain_time_id +"'";
		Transaction.getExecutor().execute(conn,addsql);
		//删除存储的校验码信息
		String sql="delete from  OA_SV_FOOD_CHECK where maintain_id='"+maintain_id+"' and user_code='"+usercode+"'";
		Transaction.getExecutor().execute(conn,sql);
	}
	else {
		//获取原本数据的obtain_tiem_id值，判断是否更新该表数据
		 SqlBean sqlbean = new SqlBean();
		 sqlbean.selects("OBTAIN_TIME_ID");
		 sqlbean.and("ORDER_ID", pkval);
		 List<Bean> orIds= ServDao.finds("OA_SV_FOOD_ORDER_INFO", sqlbean);
		 Bean bean=orIds.get(0);
			 String oldOtId = bean.getStr("OBTAIN_TIME_ID");
			 //如果之前的obtain_time_id值与本次选择的不同，则将OA_SV_FOOD_RECEIVE_INFO表中两条数据的SELECTED_NUMBER分别更新
			 if(!oldOtId.equals(obtain_time_id)) {
			 String addsql="update OA_SV_FOOD_RECEIVE_INFO set SELECTED_NUMBER=SELECTED_NUMBER+1  where OBTAIN_TIME_ID = '"+obtain_time_id +"'";
			 Transaction.getExecutor().execute(conn,addsql);
			 String resisql="update OA_SV_FOOD_RECEIVE_INFO set SELECTED_NUMBER=SELECTED_NUMBER-1  where OBTAIN_TIME_ID = '"+oldOtId +"'";			 
			 Transaction.getExecutor().execute(conn,resisql);
			 }
		excusql="update "+tabname+" set "+upgxsql+" where order_id='"+pkval+"'";
	}
	}
	//如果表名为OA_SV_FOOD_ORDER_DET且购买数量为0，则删除这条数据（物理删除）
			if("OA_SV_FOOD_ORDER_DET".equals(tabname)) {
				//先向数据库查询该数据是否存在于数据库中
				String countsql="select order_detail_id,buy_number from OA_SV_FOOD_ORDER_DET WHERE food_id='"+pkval+"' and order_id='"+orderid+"'";
				List<Bean> querylist = Transaction.getExecutor().query(countsql);
				String odid="";
				int oldbn=0;
				//如果当前数据在表中存在则更新，否则新增，
				if(querylist.size()==0) {
					//将order_detail_id赋值
					if(!"0".equals(buy_number)) {
					odid=GenUtil.getAutoIdNumner("OSFOD","OA_SV_FOOD_ORDER_DET_SEQ", "NUM");;
					excusql="insert into "+tabname+" ("+cmcolsql+",order_detail_id,order_id) values("+cmvalsql+",'"+odid+"','"+orderid+"')";
					String upfoodsql="update OA_SV_FOOD_MAINTAIN_DET set food_stock=food_stock-"+buy_number+" where food_id='"+pkval+"'";
					Transaction.getExecutor().execute(conn,upfoodsql);
					}
				}
				else {
					//获取之前的购买数量
					oldbn=querylist.get(0).getInt("BUY_NUMBER");
					odid=querylist.get(0).getStr("ORDER_DETAIL_ID");
					if("0".equals(buy_number)) {
					excusql="delete from "+tabname+" where order_detail_id='"+odid+"'";	
				} else {
					excusql="update "+tabname+" set buy_number='"+buy_number+"' where order_detail_id='"+odid+"'";
						}
					//更新库存数据
					int num=oldbn-Integer.parseInt(buy_number);
					upgxsql="update OA_SV_FOOD_MAINTAIN_DET SET FOOD_STOCK=FOOD_STOCK+("+num+") where food_id='"+pkval+"'";
					Transaction.getExecutor().execute(conn,upgxsql);
				}
		
			}
			Transaction.getExecutor().execute(conn,excusql);
	}catch (Exception e) {
		e.printStackTrace();
		code="0";
	}
			return code;
}




     /**
      * 校验当前数据是否合格
      * @param sdList
      * @param orderid
     * @param maintain_id 
     * @param user 
      * @return
      */
     public synchronized Map<String,Object> validate(List<Map<String, Object>> sdList, String orderid, String maintain_id, String user,String flag){
    	 Map<String,Object> retmap=new HashMap<>();
    	 log.debug("将预定数据信息进行校验");
    	 String code="1";
    	 String mess="提交成功";
    	 if("1".equals(flag)) {
    	//每个用户只能对每个维护表单进行一次预定
    	 SqlBean sb=new SqlBean();
    	 sb.selects("ORDER_ID");
    	 sb.and("MAINTAIN_ID", maintain_id);
    	 sb.and("S_FLAG", "1");
    	 sb.and("S_USER", user);
    	 List<Bean> finds2 = ServDao.finds("OA_SV_FOOD_ORDER_INFO", sb);
    	 if(finds2.size()>0) {
    		 code="0";
    		 mess="您已经在当前维护中预定过了，每位用户只能预定一次";
    		 retmap.put("code", code);
        	 retmap.put("mess", mess);
        	 return retmap;
    	 }
    	 }
    	mainf: for (Map<String, Object> map : sdList) {
			String tabname=map.get("tabname").toString();
			List<Map<String,Object>> list=(List<Map<String, Object>>) map.get("cols");
			//判断选择的时间
			if("OA_SV_FOOD_ORDER_INFO".equals(tabname)) {
				//查询当前orderid是否是进行更新操作，获取存在的领取时间id
				String obtid="";
				SqlBean selbean=new SqlBean();
				selbean.selects("OBTAIN_TIME_ID");
				selbean.and("ORDER_ID", orderid);
				 List<Bean> finds = ServDao.finds("OA_SV_FOOD_ORDER_INFO", selbean);
				 if(finds.size()>0) {
					 obtid=finds.get(0).getStr("OBTAIN_TIME_ID");
				 }
				for (Map<String, Object> oimap : list) {
					if("OBTAIN_TIME_ID".equals(oimap.get("colname").toString())) {
					String otid=oimap.get("colval").toString();
					if(!obtid.equals(otid)) {
					SqlBean sqlbean=new SqlBean();
					sqlbean.selects("OPTIONAL_NUMBER-SELECTED_NUMBER NUM");
					sqlbean.and("OBTAIN_TIME_ID", otid);
					List<Bean> numBean = ServDao.finds("OA_SV_FOOD_RECEIVE_INFO", sqlbean);
					String num=numBean.get(0).getStr("NUM");
					if("0".equals(num)) {
						code="0";
						mess="您选择的领取时间，选择人数已满，请换一个领取时间！";
						break mainf;
					}
					}
					}
				}
				
			}
			//判断购买数量
			if("OA_SV_FOOD_ORDER_DET".equals(tabname)) {
				String foodid="";
            	int buy_number=0;
                for (Map<String, Object> odmap : list) {
                	if("FOOD_ID".equals(odmap.get("colname").toString())){
					 foodid=odmap.get("colval").toString();
                	}
                	if("BUY_NUMBER".equals(odmap.get("colname").toString())) {
					 buy_number=Integer.parseInt(odmap.get("colval").toString());
                	}
            		
    			}
		
                String fnsql="SELECT sfmd.FOOD_STOCK,FOOD_NAME,sfmd.FOOD_LIMIT_NUMBER,decode(sfod.BUY_NUMBER,NULL,0,sfod.buy_number) buy_number FROM (SELECT * FROM OA_SV_FOOD_maintain_DET where food_id='"+foodid+"') sfmd  left JOIN (SELECT buy_number,FOOD_ID FROM OA_SV_FOOD_ORDER_DET  WHERE ORDER_ID='"+orderid+"'  ) sfod on sfmd.FOOD_ID=sfod.FOOD_ID";
				List<Bean> qlist = Transaction.getExecutor().query(fnsql);
				int fs=Integer.parseInt(qlist.get(0).getStr("FOOD_STOCK"));
				int fln=Integer.parseInt(qlist.get(0).getStr("FOOD_LIMIT_NUMBER"));
				int obn=Integer.parseInt(qlist.get(0).getStr("BUY_NUMBER"));
				String fn=qlist.get(0).getStr("FOOD_NAME");
				int num=0;
				num=fln>(fs+obn)?(fs+obn):fln;
				String fname=fln<(fs+obn)?"限购数量":"剩余数量";
				if(buy_number>num) {
					mess="您购买的数量超过了"+fname+"，您只能购买"+fn+num+"件";
					code="0";
					break mainf;
				}
			}
		
		}
    	 retmap.put("code", code);
    	 retmap.put("mess", mess);
    	 return retmap;
     }
 
     /**
      * 
      * @title: queryInit
      * @descriptin: 查询新增，查看，修改预定单时的初始化数据
      * @param @param paramBean
      * @param @return
      * @return OutBean
      * @throws
      */
     public OutBean queryInit(ParamBean paramBean) {
    	 log.debug("根据当前用户点击的维护单，查询初始化数据");
    	 UserBean userBean = Context.getUserBean();
    	 String usercode=userBean.getCode();
    	 
    	 Map<String,Object> map=new HashMap<String,Object>();
        String maintain_id=paramBean.getStr("MAINTAIN_ID");
        String code="1";
        String mess="初始化页面成功";
   	 String orderid=paramBean.getStr("ORDER_ID");
   	 //序列号
   	 String sn="";
   	 //校验码
   	 String cc="";
		 //1：新增，2：修改或查看
		 String flag=paramBean.getStr("flag");
		 //新增流程，如果重复点击一条维护，则第一次是新增，第二次是修改
		 //当flag为1时进行校验，是否数据库中已经存在一条同维护，同用户的数据，如存在，则将flag重置
		 
		 if("1".equals(flag)) {
			SqlBean sb=new SqlBean();
			sb.selects("ORDER_ID");
			sb.and("MAINTAIN_ID", maintain_id);
			sb.and("S_USER", usercode);
			sb.and("S_FLAG", "1");
			List<Bean> finds = ServDao.finds("OA_SV_FOOD_ORDER_INFO", sb);
			if(finds.size()>0) {
				flag="2";
				code="2";
				mess="请注意：您已经提交过此预订单";
				orderid=finds.get(0).getStr("ORDER_ID");
			}
		 }
		//maintain_id为空，则是进行修改
	        if("".equals(maintain_id)||"2".equals(flag) ){
	        	String mtsql="select maintain_id,CHECK_CODE,SERIAL_NUMBER from oa_sv_food_order_info where order_id='"+orderid+"'";
	        	List<Bean> queryl = Transaction.getExecutor().query(mtsql);
	        	if(queryl.size()>0) {
	        		maintain_id=queryl.get(0).get("MAINTAIN_ID").toString();
	        	sn=queryl.get(0).get("SERIAL_NUMBER").toString();
	        	 cc=queryl.get(0).get("CHECK_CODE").toString();
	        	}
	        }else {
	        	//否则进行新增
	        	Map<String, Object> check = getCheck(maintain_id);
	        	sn=check.get("SERIAL_NUMBER").toString();
	        	cc=check.get("CHECK_CODE").toString();
	       	}
		 //使用order_id与m
	        String fosql="SELECT *,FOOD_STOCK+BUY_NUMBER AS FOOD_T_STOCK FROM ( SELECT sfmd.S_FLAG,SFMD.FOOD_NAME,SFMD.FOOD_STOCK,SFMD.FOOD_ID,SFMD.FOOD_TYPE,SFMD.FOOD_PRICE,SFMD.FOOD_LIMIT_NUMBER,decode(FO.BUY_NUMBER,NULL,0,FO.BUY_NUMBER) buy_number FROM OA_SV_FOOD_MAINTAIN_DET SFMD " + 
			 		" LEFT JOIN " + 
			 		"(SELECT SFOD.FOOD_ID,SFOD.BUY_NUMBER buy_number FROM OA_SV_FOOD_ORDER_INFO SFOI,OA_SV_FOOD_ORDER_DET SFOD WHERE SFOD.ORDER_ID=SFOI.ORDER_ID AND SFOI.ORDER_ID='"+orderid+"') FO  " + 
			 		" ON SFMD.FOOD_ID=FO.FOOD_ID  WHERE SFMD.MAINTAIN_ID='"+maintain_id+"' AND sfmd.s_flag='1'  order by SFMD.s_mtime desc)";
	        String rtsql="select sfri.obtain_time_id,sfri.obtain_start_date||' '||sfri.obtain_start_time lqkssj,sfri.obtain_end_date||' '||sfri.obtain_end_time lqjssj,sfri.selected_number,sfri.optional_number-sfri.selected_number RESIDUAL,decode(OPTIONAL_NUMBER-SELECTED_NUMBER,0,0,null,0,1) full from oa_sv_food_receive_info sfri where maintain_id='"+maintain_id+"' order by s_mtime desc";
		 
		 //判断当前时间是否已经超过了预定结束时间，如果已超过，则将当前数据只能查看，否则可以进行编辑
		 String flagsql="SELECT 1 FROM OA_SV_FOOD_MAINTAIN_INFO sfmi,OA_SV_FOOD_ORDER_INFO sfoi WHERE sfoi.MAINTAIN_ID=sfmi.MAINTAIN_ID AND sfmi.ORDER_END_DATE||' '||sfmi.ORDER_END_TIME >to_char(sysdate(),'yyyy-MM-dd HH24:MI') AND sfoi.ORDER_ID='"+orderid+"'";
		 List<Bean> countlist = Transaction.getExecutor().query(flagsql);
		 
		 
		 String selsql="select sfri.obtain_time_id,sfri.obtain_start_date||' '||sfri.obtain_start_time lqkssj,sfri.obtain_end_date||' '||sfri.obtain_end_time lqjssj,sfri.selected_number,sfri.optional_number-sfri.selected_number RESIDUAL from oa_sv_food_receive_info sfri,OA_SV_FOOD_ORDER_INFO sfoi WHERE sfoi.OBTAIN_TIME_ID=sfri.OBTAIN_TIME_ID AND sfoi.ORDER_ID='"+orderid+"'";
		 if(countlist.size()==0&&!"1".equals(flag)) {
			 flag="3";
			 rtsql=selsql;
		 }
		 List<Bean> selBeans= Transaction.getExecutor().query(selsql);
		 List<Bean> foBeans = Transaction.getExecutor().query(fosql);
		 
		 List<Bean> defBeans = Transaction.getExecutor().query(rtsql);
		 //组装返回前端的数据
		 OutBean ob=new OutBean();
		 map=getRetJson(foBeans,defBeans,selBeans,flag);
		 
		 if("1".equals(flag)&&"".equals(orderid)) {
			 //获取符合规则的id值
			 orderid=GenUtil.getAutoIdNumner("OSFOI","OA_SV_FOOD_ORDER_INFO_SEQ", "NUM");
		 }
		 
		 //edit by pye 20181212
		 //在页面上增加:维护标题，预定开始时间，预定结束时间，备注信息
		 String sql="SELECT MAINTAIN_TITLE AS title,ORDER_START_DATE||' '||ORDER_START_TIME AS ydkssj,ORDER_END_DATE||' '||ORDER_END_TIME AS ydjssj,MAINTAIN_REMARK AS remark FROM OA_SV_FOOD_MAINTAIN_INFO where maintain_id='"+maintain_id+"'";
		 List<Bean> beans = Transaction.getExecutor().query(sql);
		 String ydkssj="";
		 String ydjssj="";
		 String title="";
		 String remark="";
		 if(beans.size()>0) {
			 Bean bean = beans.get(0);
			 ydkssj=bean.getStr("YDKSSJ");
			 ydjssj=bean.getStr("YDJSSJ");
			 title=bean.getStr("TITLE");
			 remark=bean.getStr("REMARK");
		 }
		 //edit end
		 
		 map.put("ORDER_ID", orderid);
		 map.put("MAINTAIN_ID", maintain_id);
 		map.put("SERIAL_NUMBER", sn);
 		map.put("CHECK_CODE", cc);
 		map.put("YDKSSJ", ydkssj);
 		map.put("YDJSSJ", ydjssj);
 		map.put("TITLE", title);
 		map.put("REMARK", remark);
 		ob.put("mess", mess);
 		ob.put("code", code);
		 ob.put("result", map);
		 return ob;
	}

	public Map<String,Object> getRetJson(List<Bean> foBeans, List<Bean> defBeans, List<Bean> selBeans, String flag){
		String selTime="";
		if(selBeans.size()>0) {
			selTime=selBeans.get(0).getStr("OBTAIN_TIME_ID");
		}
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("code", "1");
		List<Map<String,Object>> ja=new ArrayList<>();
		 //预定食物列表数据
		 JSONObject fojson=new JSONObject();
		 //将数据存入json
		 fojson.put("data", foBeans);
		 ja.add(fojson);
		 //预定领取时间列表数据
		 JSONObject rtjson=new JSONObject();
		//将数据存入json
		rtjson.put("data", defBeans);
		ja.add(rtjson);
		map.put("data", ja);
		map.put("divids", arrayToList(divids));
		map.put("tabnames", arrayToList(tabnames));
		 List<String> focolList=arrayToList(foarr);
		 List<String> rtcolList=arrayToList(rtarr);
		 List<List<String>> collist=new ArrayList<>();
		 collist.add(focolList);
		 collist.add(rtcolList);
		 map.put("cols",collist);
		 map.put("selTime", selTime);
		 map.put("flag", flag);
		 return map;
	}
	@Override
	protected void afterQuery(ParamBean paramBean, OutBean outBean) {
		System.out.println(outBean);
		List<Bean> dataList = outBean.getDataList();
		if(dataList.size()>0) {
			for (Bean data : dataList) {
				if(data.getStr("S_ATIME")!=null && data.getStr("S_ATIME").length()>0) {
					data.set("S_ATIME", data.getStr("S_ATIME").substring(0, 19));
				}
				if(data.getStr("S_MTIME")!=null && data.getStr("S_MTIME").length()>0) {
					data.set("S_MTIME", data.getStr("S_MTIME").substring(0, 19));
				}
			}
		}
	}


public String getSerialNumber(String maintain_id) {
	log.debug("根据当前维护单生成序号");
	String sn="0000";
	SqlBean sqlBean = new SqlBean();
			sqlBean.selects(" SERIAL_NUMBER  ");
			sqlBean.and("MAINTAIN_ID", maintain_id);
//		sqlBean.and("rownum",1);
		sqlBean.desc("S_ATIME ");
		
		//fileid 3DvDn2uacRdhqpN4Y3Un0OJ
		//servid SY_WFE_PROC_DEF
//		Bean result    = (Bean) ServDao.find("SY_COMM_FILE", "3DvDn2uacRdhqpN4Y3Un0OJ");
		Bean result    = (Bean) ServDao.find("OA_SV_FOOD_ORDER_INFO", sqlBean);
			if(null!=result&& !StringUtil.isEmpty((String)result.get("SERIAL_NUMBER"))) {
				sn=GenUtil.getQualityNum(Integer.valueOf((String)result.get("SERIAL_NUMBER")));
				
			}else {
				sn=GenUtil.getQualityNum(0);
			}
			return    sn;
}

public List<String> arrayToList(String[] strArr){
	List<String> strList=new ArrayList<>();
	for (String string : strArr) {
		strList.add(string);
	}
	return strList;
}


/**
 * 生成随机校验码
 * @param args
 */
public String getCheckCode(String maintain_id) {
	//edit by pye 2019-12-17
	String cc="";
	UserBean userBean = Context.getUserBean();
	 String usercode=userBean.getCode();
	 String sql="select CHECK_CODE from OA_SV_FOOD_CHECK where user_code='"+usercode+"' and maintain_id='"+maintain_id+"'";
	 List<Bean> ccs = Transaction.getExecutor().query(sql);
	 if(ccs.size()>0) {
		 cc=ccs.get(0).get("CHECK_CODE").toString();
	 }else {
	//edit by pye end
	log.debug("生成随机校验码");
	String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	StringBuilder sb = new StringBuilder(5);
	for (int i = 0; i < 5; i++) {
		char ch = str.charAt(new Random().nextInt(str.length()));
		sb.append(ch);
	}
	cc= sb.toString();
	 String s_atime=sdf.format(new Date());
	  String insql="insert into OA_SV_FOOD_CHECK (maintain_id,user_code,check_code,s_Atime) values('"+maintain_id+"','"+usercode+"','"+cc+"','"+s_atime+"')";
	  Transaction.getExecutor().execute(insql);
	 }
	return cc;

}


/**
    * 订单删除
  * @param paramBean
  * @return
  */
 public OutBean deleteAll(ParamBean paramBean) {
	 log.debug("删除选定的所有预订单数据");
	 	int execute = 0 ;
		String pkCodes=paramBean.getStr("ORDER_ID");
		String[] pkCodeArr = pkCodes.split(",");
			for(int i=0;i<pkCodeArr.length;i++) {
				if(deleteOrder(pkCodeArr[i])) {
					execute++;
				}
		}
		OutBean outBean=new OutBean();
		outBean.setOk("成功删除"+execute+"条数据");
		return outBean;
	}

 public boolean deleteOrder(String orderId) {
	 log.debug("删除一条选定的预订单数据");
	 int execute = 0 ;
	 if(StringUtil.isEmpty(orderId)) {
		 return false;
	 }
	 try {
		 
		 String sql="update OA_SV_FOOD_ORDER_INFO set S_FLAG='2' where ORDER_ID = '"+orderId +"' and s_flag='1'";
		 execute = Transaction.getExecutor().execute(sql);
		 if(execute>0) {
			 SqlBean sqlbean1 = new SqlBean();
			 SqlBean sqlbean2 = new SqlBean();
			 sqlbean1.selects("FOOD_ID,BUY_NUMBER");
			 sqlbean1.and("ORDER_ID", orderId);
			 
			 sqlbean2.selects("OBTAIN_TIME_ID");
			 sqlbean2.and("ORDER_ID", orderId);
			 
			 List<Bean> foodIds= ServDao.finds("OA_SV_FOOD_ORDER_DET", sqlbean1);
			 List<Bean> obtainIds= ServDao.finds("OA_SV_FOOD_ORDER_INFO", sqlbean2);
			 for (Bean bean : foodIds) {
				 String foodId = (String) bean.get("FOOD_ID");
				 int foodNum = Integer.valueOf(bean.get("BUY_NUMBER").toString());
				 sql="update OA_SV_FOOD_MAINTAIN_DET set FOOD_STOCK=FOOD_STOCK+ "+foodNum+" where FOOD_ID = '"+foodId +"'";
				 execute = Transaction.getExecutor().execute(sql);
				 
			}
			 for (Bean bean : obtainIds) {
				 String obtainId = (String) bean.get("OBTAIN_TIME_ID");
				 //int perNum = Integer.valueOf(bean.get("SELECTED_NUMBER").toString());
				 sql="update OA_SV_FOOD_RECEIVE_INFO set SELECTED_NUMBER=SELECTED_NUMBER- "+1+" where OBTAIN_TIME_ID = '"+obtainId +"' and selected_number>0";
				 execute = Transaction.getExecutor().execute(sql);
				 
			}
			 sql="update OA_SV_FOOD_ORDER_DET set S_FLAG='2' where ORDER_ID = '"+orderId +"'";;
			 execute = Transaction.getExecutor().execute(sql);
			 return true;
		 }
	} catch (Exception e) {
		e.printStackTrace();
	}
	return false;
 } 
 
 /**
  * 
  * @title: getCheck
  * @descriptin: 判断当前maintain_id下，不能同时存在相同的序号和校验码，如果存在，则重新生成一遍
  * @param @param maintain_id
  * @param @return
  * @return Map<String,Object>
  * @throws
  */
 public Map<String,Object> getCheck(String maintain_id) {
    	   log.debug("获取新增预定单的序号与校验码，并校验是否重复");
    	   Map<String,Object> map=new HashMap<>();
    	   List<Bean> find =new ArrayList<>();
    	   int size=1;
    	   String sn="";
    	   String cc="";
    	   do {
    	//生成随机的校验码，与自增的序号
     	 sn= getSerialNumber(maintain_id);
      	 cc=getCheckCode(maintain_id);
      	 //判断是否存在一条数据，其maintain_id，序号，校验码都与当前数据相同，如存在，则重新生成校验码与序号
      	 SqlBean sb=new SqlBean();
      	 sb.selects("order_id");
      	 sb.and("SERIAL_NUMBER", sn);
      	 sb.and("CHECK_CODE", cc);
      	 sb.and("MAINTAIN_ID",maintain_id);
     	find= ServDao.finds("OA_SV_FOOD_ORDER_INFO", sb);
     	 size = find.size();
     	 map.put("SERIAL_NUMBER", sn);
  	   map.put("CHECK_CODE", cc);
    	   }while(size!=0);
      	 return map;
       }
}