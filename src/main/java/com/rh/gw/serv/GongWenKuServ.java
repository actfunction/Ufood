package com.rh.gw.serv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.JsonUtils;
import com.tongtech.backport.java.util.Arrays;

/***
 * 公文CommonServ类，所有公文扩展类都继承此类 公共方法写在这里，单独处理的写在扩展类里，用户可根据自身权限查看相关公文
 * 
 * @author zhoumeng
 * @version 1.0
 *
 */

// 署领导秘书,办公厅主任,机要室秘书,秘书处,角色编码。按照顺序排部
enum RoleCode {

	署领导("R_GW_SLD",0),
	署领导秘书("R_GW_SLDMS", 1), 
	办公厅主任("R_GW_BGT_ZR", 2), 
	机要室秘书("R_GW_JYSMS", 3),
	秘书处("R_GW_MSCMSFW",4),
	司局文书("R_GW_SJWS",5),
	司局主要负责人("R_GW_SJZYFZR",6),
	办公厅督察处经办("SUP_DC_001", 7),
	办公厅督察处复合("SUP_DC_002", 8), 
	机构督察员("SUP003", 9);
	/*无("无",7),
	工作秘密("工作秘密",8),
	机密("机密",9),
	秘密("秘密",10);*/

	private String name;
	private int beancount;
	private RoleCode(String name, int beancount) {
		this.name = name;
		this.beancount = beancount;
	}
	
	public static String getName(int beancount){
		for(RoleCode r:RoleCode.values()){
			if(r.getbeancount()==beancount){
				return r.getName();
			}
		}
		return "不存在";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getbeancount() {
		return beancount;
	}

	public void setbeancount(int beancount) {
		this.beancount = beancount;
	}

}

public class GongWenKuServ extends GwExtServ {
	
		private static boolean requestflag  = false;
		private static boolean requestcount = false;
	
		// 公文库列表
		public OutBean getGongWenKu(ParamBean paramBean) {
			// 查询条件
			// (公文标题:gw_title,文号:gw_wenhao,正文:gw_zhengwen,发文日期:gw_fawenriqi,密级:gw_miji,附件:gw_fujian,
			// 发文机关:gw_fawenjiguan,缓急:gw_huanji
			// 主办单位:gw_zhubandanwei
			int limit =0;
			boolean flag = false;//定义flag,判断是否有参数
			StringBuffer buffer = new StringBuffer();
			OutBean out = new OutBean();
			buffer.append(" 1=1 ");
			String data = paramBean.getStr("where_items");
			String limitstr = paramBean.getStr("limit");
			String pagestr = paramBean.getStr("page");
			System.out.println(data);
			if(Integer.parseInt(paramBean.getStr("flag"))==1){
				limit = 50;
			}else{
				limit = Integer.parseInt(limitstr.equals("") ? "10" : limitstr);
			}
			
			int page = Integer.parseInt(pagestr.equals("") ? "1" : pagestr);
			int pagetrue = (page-1)*limit+1;
			
			if (data != "") {
				Bean dataBean = JsonUtils.toBean(data);
				Set<Entry<Object, Object>> entry = dataBean.entrySet();
				for (Entry<Object, Object> entrySet : entry) {
					String key = entrySet.getKey().toString();
					String value = entrySet.getValue().toString();
					if(!value.equals("")){
						buffer.append(" and " + key + " like " + "'%"+value+"%'");
						flag=true;
					}
				}
			}
			
 			 if(flag){
				  page=1;
				  pagetrue = (page-1)*limit+1;
			 }
			 else{
				 pagetrue = (page-1)*limit+1;
			 }
			
			 
			SqlExecutor executor = Transaction.getExecutor();
			String conditions = buffer.toString();
			System.out.println(conditions);
			return getCurrentUser(conditions,executor,pagetrue,limit,out,flag);
		}


		// 获取当前登录人
		public OutBean getCurrentUser(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out,boolean flag) {
			UserBean userbean = Context.getUserBean();
			String[] roles = userbean.getRoleCodes();// 获取当前登录人角色
			List<String> list = Arrays.asList(roles);
			List<Bean> listBean = new ArrayList<Bean>();
			int beancount=0;
			String dept=userbean.getDeptCode();
			String	user_code=userbean.getStr("USER_CODE");//获取USER_CODE
			String tdeptcode=userbean.getTDeptCode();
			System.out.println("察看角色");
			System.out.println(roles+" "+userbean.getRoleCodeList());
			//署领导
			if(list.contains(RoleCode.getName(0))){
				listBean.addAll(getAllGongWenLeader(conditions,executor,pagetrue,limit,out));
				beancount +=getAllGongWenLeadercount(conditions,executor,pagetrue,limit,out);
			}
		
			for(int i=0;i<5;i++){
				if(list.contains(RoleCode.getName(i))){
					List<Bean> listBeanAll = new ArrayList<Bean>();
						listBeanAll=getAllGongWen(conditions,executor,pagetrue,limit,out); 
						beancount +=getAllGongWencount(conditions,executor,pagetrue,limit,out);
						listBean.addAll(listBeanAll);
					break;
				} 
			}
		
		    //司局文书,司局主要负责人
			for(int j=5;j<7;j++){
				if(list.contains(RoleCode.getName(j))){
					System.out.println("司局主要负责人");
					List<Bean> listDept = new ArrayList<Bean>();
						listDept=getOwnDept(conditions,executor,pagetrue,limit,out,dept);
						beancount +=getOwnDeptcount(conditions,executor,pagetrue,limit,out,tdeptcode);
						listBean.addAll(listDept);
					break;
				} 
			}
			
			//获取督察的接口数据
			for(int count=7;count<10;count++){
				if(list.contains(RoleCode.getName(count))){
					System.out.println("获取督察");
					List<Bean> listSupervise = new ArrayList<Bean>();
						listSupervise =  getSuperviseList(conditions,executor,pagetrue,limit,out);
						beancount +=getSuperviseListcount(conditions,executor,pagetrue,limit,out);
						listBean.addAll(listSupervise);
					break;
				}
			}		
			
			  listBean.addAll(getAllGongWenRange(conditions,executor,pagetrue,limit,out,dept,tdeptcode));
			  beancount+=getAllGongWenRangecount(conditions,executor,pagetrue,limit,out,dept,tdeptcode);
			  listBean.addAll(getOwner(conditions,executor,pagetrue,limit,out,dept,user_code));
			  beancount+=getOwnercount(conditions,executor,pagetrue,limit,out,dept,user_code);
			  System.out.println("22222");
			  
			  /*System.out.println(listBean.size());
			  HashSet set = new HashSet(listBean);
			  listBean.clear();
			  listBean.addAll(set);*/
			  System.out.println(listBean.size());
			  List<Bean> listTemp = new ArrayList<>();
			  for(int i=0;i<listBean.size();i++){
				  boolean flagAdd = true;
				  for (Bean bean : listTemp) {
					  if(listBean.get(i).getStr("GW_ID").equals(bean.getStr("GW_ID"))){
						  flagAdd=false;
						  break;
					  }
				  }
				  if(flagAdd){
					  listTemp.add(listBean.get(i));
				  }
			  }
			  System.out.println(listTemp.size());

			  int pagelimit = pagetrue+limit-1;
			  if(pagetrue+limit>listTemp.size()){
				  pagelimit = listTemp.size();
			  }
			
			  Collections.sort(listTemp, new Comparator<Bean>(){
				@Override
				public int compare(Bean o1, Bean o2) {
						SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
						String time1str = o1.getStr("GW_END_TIME");
						String time2str=o2.getStr("GW_END_TIME");
						String time1 = null;
						String time2=null;
						try {
							if(!time1str.equals("")){
 								time1=time1str.substring(0, 10);
							}
							if(!time2str.equals("")){
								time2=time2str.substring(0, 10);
							}
							if(time1==null &&time2!=null){
								return 1;
							}else if(time2==null&&time1!=null){ 
								return -1;
							}else if(time1==null && time2==null){
								return 0;
							}else{
								Date date1 = format.parse(time1);
								Date date2 = format.parse(time2); 
								if(date1.getTime()>date2.getTime()){
									return -1;
								}else if(date1.getTime()<date2.getTime()){
									return 1;
								}else{
									return 0;
								}
							}
							
						} catch (ParseException e) {
							return 0;
						}
				}
			  });
			 
			  List<Bean> sublistBean=listTemp.subList(pagetrue-1, pagelimit);
			  for(Bean bean:sublistBean){
				  System.out.println(bean.getStr("GW_END_TIME"));
			  }
			  System.out.println(sublistBean);
			  out.set("authority", sublistBean);
			  out.set("beancount", listTemp.size());
			  out.set("pagetrue", pagetrue);
			  return out;
		}


		//督察关联签报的数据
		public OutBean getAllGain(){
			SqlExecutor executor = Transaction.getExecutor();
			String sql = "SELECT * FROM OA_GW_GONGWEN gw WHERE gw.GW_ID IN (SELECT gain.GAIN_LINK FROM SUP_APPRO_GAIN gain) AND gw.IS_FW_TO_SW ='2'";
			List<Bean> efstr = executor.query(sql);
			OutBean out = new OutBean();
			out.set("efstr", efstr);
			return null;
		}

		
		//督察处人员察看公文库权限
		public List<Bean> getSuperviseList(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out){
			String sql ="SELECT gw.*,zcdz.FINAL_JGJC, dctype.DCTYPE_NAME FROM OA_GW_GONGWEN gw  LEFT JOIN OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gw.GW_YEAR_CODE LEFT JOIN  OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gw.GW_FILE_TYPE  WHERE "+conditions+" AND  (GW_GONGWEN_DCFQ ='1' OR gw.GW_ID IN (SELECT gain.GAIN_LINK FROM SUP_APPRO_GAIN gain)) AND (gw.IS_FW_TO_SW='2' OR gw.IS_FW_TO_SW IS NULL) AND  gw.S_FLAG = '1' "+
					    " UNION SELECT gw.*,zcdz.FINAL_JGJC,dctype.DCTYPE_NAME  FROM OA_GW_GONGWEN gw  LEFT JOIN OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gw.GW_YEAR_CODE LEFT JOIN  OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gw.GW_FILE_TYPE   WHERE "+conditions+" AND  gw.GW_ID IN (SELECT sup.GONGWEN_ID  FROM SUP_APPRO_OFFICE sup)   AND  (gw.IS_FW_TO_SW='2' OR gw.IS_FW_TO_SW IS NULL) AND  gw.S_FLAG = '1'";
			//List<Bean> superviseData = executor.query(sql, pagetrue, limit);
			List<Bean> superviseData = executor.query(sql);
			System.out.println("督察的数据长度数据"+superviseData.size());
			return superviseData;
		}
		
		//督察处人员察看公文库权限
		public int getSuperviseListcount(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out){
			String sql ="SELECT gw.*,zcdz.FINAL_JGJC,dctype.DCTYPE_NAME FROM OA_GW_GONGWEN gw  LEFT JOIN OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gw.GW_YEAR_CODE  LEFT JOIN  OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gw.GW_FILE_TYPE  WHERE  "+conditions+" AND (GW_GONGWEN_DCFQ ='1' OR gw.GW_ID IN (SELECT gain.GAIN_LINK FROM SUP_APPRO_GAIN gain)) AND (gw.IS_FW_TO_SW='2' OR gw.IS_FW_TO_SW IS NULL) AND  gw.S_FLAG = '1' "+
					    " UNION SELECT gw.*,zcdz.FINAL_JGJC,dctype.DCTYPE_NAME FROM OA_GW_GONGWEN gw  LEFT JOIN OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gw.GW_YEAR_CODE  LEFT JOIN  OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gw.GW_FILE_TYPE WHERE  "+conditions+"  AND  gw.GW_ID IN (SELECT sup.GONGWEN_ID  FROM SUP_APPRO_OFFICE sup)  AND   (gw.IS_FW_TO_SW='2' OR gw.IS_FW_TO_SW IS NULL) AND gw.S_FLAG = '1'";
				String sqlcount ="SELECT COUNT(*) " + "FROM  ("+ sql +")";
			int beancount=executor.queryOne(sqlcount).getInt("COUNT");
			System.out.println("督察的数据长度"+beancount);
			return beancount;
		}		
		
		//署领导察看
		private List<Bean> getAllGongWenLeader(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out){
			String sql = "SELECT gwk.*,dctype.DCTYPE_NAME" + "  FROM OA_GONGWENKU_V  gwk LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE " + " WHERE "+conditions+ " AND (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL)   UNION SELECT gwk.*,zcdz.FINAL_JGJC,dctype.DCTYPE_NAME FROM OA_GW_GONGWEN gwk   LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE LEFT JOIN OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gwk.GW_YEAR_CODE WHERE gwk.GW_FILE_TYPE IN (SELECT dctype.DCTYPE_NAME FROM OA_GW_GONGWEN_DCTYPE dctype WHERE dctype.IS_FLAG=2) AND  (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL) AND gwk.S_FLAG='1'";
			System.out.println(sql);
			//List<Bean> leaderData = executor.query(sql, pagetrue, limit);
			List<Bean> leaderData = executor.query(sql);
			System.out.println("署领导察看公文库"+leaderData.size());
			return leaderData;
		}
		
		//署领导察看数量
		private int getAllGongWenLeadercount(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out){
			String sql = "SELECT gwk.*,dctype.DCTYPE_NAME" + "  FROM OA_GONGWENKU_V  gwk LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE " + " WHERE "+conditions+ " AND (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL)   UNION SELECT gwk.*,zcdz.FINAL_JGJC,dctype.DCTYPE_NAME FROM OA_GW_GONGWEN gwk   LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE LEFT JOIN OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gwk.GW_YEAR_CODE WHERE gwk.GW_FILE_TYPE IN (SELECT dctype.DCTYPE_NAME FROM OA_GW_GONGWEN_DCTYPE dctype WHERE dctype.IS_FLAG=2) AND  (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL) AND gwk.S_FLAG='1'";
			String sqlcount ="SELECT COUNT(*) " + "FROM  ("+ sql +")";
			int beancount =  executor.queryOne(sqlcount).getInt("COUNT");
			return beancount;
		}
		
	
		//可以查看所有文
		private List<Bean> getAllGongWen(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out){
			String sql = "SELECT gwk.*,dctype.DCTYPE_NAME " + "FROM OA_GONGWENKU_V gwk LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE  " + " WHERE " + conditions +" AND (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL) AND gwk.S_FLAG='1'";
			//List<Bean> leaderData = executor.query(sql, pagetrue, limit);
			List<Bean> leaderData = executor.query(sql);
			System.out.println("察看所有文"+leaderData.size());
			return  leaderData;
		}
		
		//可以查看所有文数量
		private int getAllGongWencount(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out){
			String sql = "SELECT gwk.*,dctype.DCTYPE_NAME " + "FROM OA_GONGWENKU_V gwk LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE  " + " WHERE " + conditions +" AND (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL) AND gwk.S_FLAG='1'";
			String sqlcount = "SELECT COUNT(*) " +"FROM ("+ sql + ")";
			int beancount =  executor.queryOne(sqlcount).getInt("COUNT");
			return beancount;
		}		
		
	
		//司局文书,司局主要负责人流经本司局
		private List<Bean> getOwnDept(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out,String tdeptcode){
			String sql = "SELECT gwk.*,zcdz.FINAL_JGJC,dctype.DCTYPE_NAME FROM OA_GW_GONGWEN gwk LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE LEFT JOIN  OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gwk.GW_YEAR_CODE WHERE  "+conditions+" AND  (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL)  AND GW_ID IN (SELECT TODO_OBJECT_ID1 FROM SY_COMM_TODO_HIS his WHERE his.OWNER_CODE IN (SELECT deptuser.USER_CODE FROM SY_ORG_DEPT_USER deptuser WHERE INSTR(deptuser.CODE_PATH,'"+tdeptcode+"')>0) AND gwk.S_FLAG='1')";
			String sqlcount = "SELECT COUNT(*) " +"FROM ("+ sql + ")";
			//List<Bean> departData = executor.query(sql, pagetrue, limit);
			List<Bean> departData = executor.query(sql);
			return departData;
		}
	
		//司局文书,司局主要负责人流经本司局数量
		private int getOwnDeptcount(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out,String tdeptcode){
			String sql = "SELECT gwk.*,zcdz.FINAL_JGJC,dctype.DCTYPE_NAME FROM OA_GW_GONGWEN gwk LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE LEFT JOIN  OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gwk.GW_YEAR_CODE WHERE  "+conditions+" AND  (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL) AND GW_ID IN (SELECT TODO_OBJECT_ID1 FROM SY_COMM_TODO_HIS his WHERE his.OWNER_CODE IN (SELECT deptuser.USER_CODE FROM SY_ORG_DEPT_USER deptuser WHERE INSTR(deptuser.CODE_PATH,'"+tdeptcode+"')>0) AND gwk.S_FLAG='1')";
			String sqlcount = "SELECT COUNT(*) " +"FROM ("+ sql + ")";
			int beancount =  executor.queryOne(sqlcount).getInt("COUNT");
			return beancount;
		}		
		
		//发送范围下的所有文
		private List<Bean> getAllGongWenRange(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out,String dept_codeIn,String tdeptcode){
			String sql = " SELECT gwk.*,zcdz.FINAL_JGJC,dctype.DCTYPE_NAME FROM OA_GW_GONGWEN gwk  LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype  ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE  LEFT JOIN  OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gwk.GW_YEAR_CODE  WHERE  "+conditions+" AND  (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL) AND  gwk.GW_ID IN ("+" SELECT GW_ID FROM OA_GW_GONGWEN gw WHERE INSTR(gw.GW_MAIN_TO,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_MAIN_TO,'"+tdeptcode+"')>0 OR "+
						 " INSTR(gw.GW_COPY_TO,'"+dept_codeIn+"')>0 OR INSTR(gw.GW_COPY_TO,'"+tdeptcode+"')>0" +" OR INSTR(gw.GW_SEND_TO,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_SEND_TO,'"+tdeptcode+"'"+")>0 OR"+
						 " INSTR(gw.GW_COSIGN_TO,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_COSIGN_TO,'"+tdeptcode+"'"+")>0 OR" +	
						 " INSTR(gw.GW_MAIN_HANDLE,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_MAIN_HANDLE,'"+tdeptcode+"'"+")>0 OR" +
						 " INSTR(gw.GW_COPY_HANDLE,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_COPY_HANDLE,'"+tdeptcode+"'"+")>0 ) AND "+conditions+" AND gwk.S_FLAG='1'";
			//List<Bean> userData =executor.query(sql,pagetrue,limit);
			List<Bean> userData =executor.query(sql);
			System.out.println("发送范围下文"+userData.size());
			return userData;
		}
		//发送范围下的所有文
		private int getAllGongWenRangecount(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out,String dept_codeIn,String tdeptcode){
			String sql = " SELECT gwk.*,zcdz.FINAL_JGJC,dctype.DCTYPE_NAME  FROM OA_GW_GONGWEN gwk  LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype  ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE  LEFT JOIN  OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gwk.GW_YEAR_CODE    WHERE  "+conditions+" AND  (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL) AND gwk.GW_ID IN ("+" SELECT GW_ID FROM OA_GW_GONGWEN gw WHERE INSTR(gw.GW_MAIN_TO,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_MAIN_TO,'"+tdeptcode+"')>0 OR "+
					 " INSTR(gw.GW_COPY_TO,'"+dept_codeIn+"')>0 OR INSTR(gw.GW_COPY_TO,'"+tdeptcode+"')>0" +" OR INSTR(gw.GW_SEND_TO,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_SEND_TO,'"+tdeptcode+"'"+")>0 OR"+
					 " INSTR(gw.GW_COSIGN_TO,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_COSIGN_TO,'"+tdeptcode+"'"+")>0 OR" +	
					 " INSTR(gw.GW_MAIN_HANDLE,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_MAIN_HANDLE,'"+tdeptcode+"'"+")>0 OR" +
					 " INSTR(gw.GW_COPY_HANDLE,'"+dept_codeIn+"'"+")>0 OR INSTR(gw.GW_COPY_HANDLE,'"+tdeptcode+"'"+")>0 ) AND "+conditions+" AND gwk.S_FLAG='1'";
			String sqlcount = "SELECT COUNT(*)  FROM ("+ sql + ")";
			int beancount =  executor.queryOne(sqlcount).getInt("COUNT");
			return beancount;
		}	
		
		
		//只能查看流经过自己的文
		private List<Bean> getOwner(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out,String dept,String user_code){
			System.out.println("察看流经自己的文");
			String sql = "SELECT gwk.*,dctype.DCTYPE_NAME FROM  OA_GONGWENKU_V gwk  LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE WHERE "+conditions+" AND (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL) AND GW_ID IN( SELECT  his.TODO_OBJECT_ID1   FROM SY_COMM_TODO_HIS his WHERE  '"+ user_code+ "'= OWNER_CODE OR '"+user_code+"' = SEND_USER_CODE  UNION  SELECT todo.TODO_OBJECT_ID1 FROM SY_COMM_TODO todo WHERE '"+user_code+"' = OWNER_CODE OR '"+user_code+"' = SEND_USER_CODE) AND  gwk.S_FLAG='1' ";
			//List<Bean> userData = executor.query(sql, pagetrue, limit);
			List<Bean> userData =executor.query(sql);
			return userData;
		}		
		
		//只能查看流经过自己的文
		private int getOwnercount(String conditions,SqlExecutor executor,int pagetrue,int limit,OutBean out,String dept,String user_code){
			String sql = "SELECT gwk.*,dctype.DCTYPE_NAME FROM  OA_GONGWENKU_V gwk  LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gwk.GW_FILE_TYPE WHERE "+conditions+" AND (gwk.IS_FW_TO_SW='2' OR gwk.IS_FW_TO_SW IS NULL) AND GW_ID IN( SELECT  his.TODO_OBJECT_ID1   FROM SY_COMM_TODO_HIS his WHERE  '"+ user_code+ "'= OWNER_CODE OR '"+user_code+"' = SEND_USER_CODE  UNION  SELECT todo.TODO_OBJECT_ID1 FROM SY_COMM_TODO todo WHERE '"+user_code+"' = OWNER_CODE OR '"+user_code+"' = SEND_USER_CODE) AND  gwk.S_FLAG='1' ";
			String sqlcount = "SELECT COUNT(*)  FROM ("+ sql + ")";
			int beancount =  executor.queryOne(sqlcount).getInt("COUNT");
			return beancount;
		}		
	
	
		// 通知公告栏
		public OutBean getGongWenKuFujian(ParamBean paramBean) {
			OutBean out = new OutBean();
			return out;
		}
	
		//根据dataId获得公文库详情
		public OutBean getGongWenKuXiangQing(ParamBean paramBean) {
			OutBean out = new OutBean();
			UserBean userbean = Context.getUserBean();
			String dept=userbean.getDeptCode();
			String dataId = paramBean.getStr("dataId");
			if(dataId != ""){
				SqlExecutor executor = Transaction.getExecutor();
				try {
					System.out.println("访问公文库详情"+dataId);
					//获得公文数据	
					//String sqlData = "SELECT GW_YEAR_CODE,GW_YEAR,GW_YEAR_NUMBER,GW_CW_TNAME,GW_GONGWEN_WJDJH,GW_TITLE,GW_END_TIME,GW_SRCRET,GW_MAIN_HANDLE,gw.S_EMERGENCY,dctype.DCTYPE_NAME FROM OA_GW_GONGWEN gw INNER JOIN OA_GW_GONGWEN_DCTYPE dctype ON  dctype.DCTYPE_ID = gw.GW_FILE_TYPE  WHERE GW_ID='" + dataId + "'"+" AND gw.S_FLAG='1'";
					String sqlData = "SELECT gw.GW_ID,zcdz.FINAL_JGJC,gw.GW_END_TIME,GW_YEAR,GW_YEAR_NUMBER,GW_CW_TNAME,GW_GONGWEN_WJDJH,GW_TITLE,GW_END_TIME,GW_SRCRET,GW_MAIN_HANDLE,gw.S_EMERGENCY,dctype.DCTYPE_NAME "+
	" FROM OA_GW_GONGWEN gw LEFT JOIN OA_GW_GONGWEN_DCTYPE dctype ON dctype.DCTYPE_ID = gw.GW_FILE_TYPE LEFT JOIN  OA_GW_FWZH_JCDZ zcdz ON zcdz.FJ_ID=gw.GW_YEAR_CODE  WHERE  gw.GW_ID='"+dataId+"' AND gw.S_FLAG='1'";		
					Bean data = executor.queryOne(sqlData);
					out.set("gwData", data);
					String sqlzhixi = "SELECT GW_MAIN_TO,GW_COPY_TO,GW_SEND_TO,GW_COSIGN_TO FROM OA_GW_GONGWEN  WHERE GW_ID='"+dataId+"' AND S_FLAG='1'";
					String sqlcopy_handcode = "SELECT gw.GW_COPY_HANDLE FROM  OA_GW_GONGWEN gw WHERE  GW_ID='"+dataId+"'";
					
					String sqlzhuban = "SELECT dep.DEPT_NAME FROM OA_GW_GONGWEN gw LEFT JOIN SY_ORG_DEPT dep ON gw.GW_MAIN_HANDLE = dep.DEPT_CODE  WHERE GW_ID='"+dataId+"'";
					StringBuffer buffer = new StringBuffer();
					Bean handcode = executor.queryOne(sqlcopy_handcode);
					String [] arraystr= handcode.getStr("GW_COPY_HANDLE").split(",");
					for(String str:arraystr){
							buffer.append("'"+str+"',");
					}
					System.out.println(buffer.substring(0,buffer.length()-1));
					String sqlfanwei = "SELECT dep.DEPT_NAME FROM SY_ORG_DEPT dep  WHERE   dep.DEPT_CODE  IN ("+buffer.substring(0,buffer.length()-1)+")";
					Bean datazhixi = executor.queryOne(sqlzhixi);
					List<Bean> datafanwei = executor.query(sqlfanwei);
					Bean datazhuban = executor.queryOne(sqlzhuban);
					out.set("datazhixi",datazhixi);
					out.set("datafanwei", datafanwei);
					out.set("datazhuban", datazhuban);
					//获得所有附件
					if(dept.equals("R_GW_SLD")||dept.equals("R_GW_SLDMS")||dept.equals("R_GW_SJWS")||dept.equals("R_GW_SJZYFZR")){
						String sqlFlie = "select FILE_NAME,FILE_ID from OA_GONGWEN_FILE_V WHERE DATA_ID='" + dataId + "'";			
						List<Bean> list = executor.query(sqlFlie);
						out.set("gongWenKuFile", list);
					}else{
						//System.out.println(dataId);
						String sql = "select FILE_NAME,FILE_ID from OA_GONGWEN_FILE_V WHERE DATA_ID='" + dataId + "'"+" AND ITEM_CODE !='OFD'";
						List<Bean> list = executor.query(sql);
						out.set("gongWenKuFile", list);
					}
				} catch (Exception e) {
					out.setError("查询失败");
				}
			}
			return out;	
		}

		// 通知公告栏 
		public OutBean setAdvice(ParamBean paramBean) {
			GongWenAdviceServ gongWenAdvice = new GongWenAdviceServ();
			return gongWenAdvice.setAdvice(paramBean);
		}
}
