package com.rh.gw.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/***
 * 公文CommonServ类，所有公文扩展类都继承此类 公共方法写在这里，单独处理的写在扩展类里，用户可根据自身权限查看相关公文
 * 
 * @author zupke
 * @version 1.0
 *
 */

public class GongWenAdviceServ {
	
	/**
	 * 
	 * 将公文添加通知公告栏
	 * 
	 * */
	public OutBean setAdvice(ParamBean paramBean) {
		OutBean out = new OutBean();
		SqlExecutor executor = Transaction.getExecutor();	
		List<Bean> dataList = paramBean.getList("dataList");
		String[] sqls = new  String[dataList.size()];
		StringBuffer buffer  = new StringBuffer();
		for (Bean data : dataList) {
			if(data.getStr("GW_ID") != ""){
				buffer.append("'"+data.getStr("GW_ID")+"'"+",");
			}
		}	
		
		String sql  = "SELECT advice.DATA_ID FROM OA_GW_ADVICE advice WHERE advice.DATA_ID  IN ("+buffer.substring(0, buffer.length()-1).toString()+")";
		System.out.println(sql);
		List<Bean> advices=executor.query(sql);
		int index = -1;
		System.out.println(advices.size());
		if(advices.size()==0){
			for(Bean data : dataList){
				index=index+1;
				sqls[index] = "insert into OA_GW_ADVICE values(SYS_GUID(2),'" + data.getStr("GW_ID") + "',now())";
			}
			
		}else{
		    List<String> datas = new ArrayList<>();//前段gw_id数据
		    List<String> lists = new ArrayList<>();
			for(Bean data:dataList){
				datas.add(data.getStr("GW_ID"));
			}
			for(Bean advice:advices){
				lists.add(advice.getStr("DATA_ID"));
			}
			for(int j=0;j<datas.size();j++){
				if(lists.contains(datas.get(j))){
					continue;
				}else{
					index=index+1;
					sqls[index] = "insert into OA_GW_ADVICE values(SYS_GUID(2),'" + datas.get(j) + "',now())";
				}
				
			}
		}
		
		try {
			if(index==-1){
				System.out.println("数据全部重复");
				out.setError("已添加到通知公告栏，请勿重复添加");
			}else{
				System.out.println("全部添加成功");
				int j = executor.executeBatch(sqls);
				out.set("result", j);
			}
			
		} catch (Exception e) {
			out.setError("查询失败");
		}		
		return out;
	}
	
	
	/**
	 * 
	 * 获得通知公告栏
	 * 
	 * */
	public OutBean getAdvice(ParamBean paramBean) {
		OutBean out = new OutBean();
		SqlExecutor executor = Transaction.getExecutor();
		StringBuffer sql = new StringBuffer();
//		sql.append("select gw.*,ad.ADVICE_ID,zh.FINAL_JGJC,dc.DCTYPE_NAME ");
//		sql.append("from OA_GW_GONGWEN gw ");
//		//sql.append("INNER JOIN SY_ORG_DEPT_USER de ON  gw.S_USER=de.USER_CODE ");
//		sql.append("INNER JOIN OA_GW_ADVICE ad ON  gw.GW_ID=ad.DATA_ID  ");
//		sql.append("LEFT JOIN OA_GW_FWZH_JCDZ zh ON gw.GW_YEAR_CODE=zh.FJ_ID ");
//		sql.append("LEFT JOIN OA_GW_GONGWEN_DCTYPE dc ON gw.GW_FILE_TYPE=dc.DCTYPE_ID ");

		sql.append("SELECT A.ADVICE_ID, B.* , (SELECT FINAL_JGJC FROM OA_GW_FWZH_JCDZ E WHERE E.FJ_ID = B.GW_YEAR_CODE) FINAL_JGJC, (SELECT DCTYPE_NAME FROM OA_GW_GONGWEN_DCTYPE D  WHERE D.DCTYPE_ID = B.GW_FILE_TYPE) DCTYPE_NAME FROM OA_GW_ADVICE A, OA_GW_GONGWEN B WHERE A.DATA_ID = B.GW_ID ");
	    String title = paramBean.getStr("GW_TITLE");
	    String limitStr = paramBean.getStr("limit");
	    String pageStr = paramBean.getStr("page");
	    
	    int limit = limitStr.equals("") ? 10 :Integer.parseInt(limitStr);
	    int page = pageStr.equals("") ? 1 :Integer.parseInt(pageStr);
	    int offset = (page - 1)*limit + 1;
	    
	    if(title != ""){
	    	sql.append("AND B.GW_TITLE LIKE '%");
	    	sql.append(title);
	    	sql.append("%' ");
	    }
	    
	    sql.append("order by A.S_ATIME desc");
		
		try {
			List<Bean> list = executor.query(sql.toString(),offset,limit);
			String sqlCount = "SELECT count(ADVICE_ID) FROM (" + sql.toString() + ")";
			Bean queryOne = executor.queryOne(sqlCount);
			out.set("offset", offset);
			out.set("count", queryOne.getStr("COUNT"));
			out.set("adviceList", list);
		} catch (Exception e) {
			out.setError("查询失败");
		}
		return out;
	}
}
