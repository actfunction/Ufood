package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SupQuery extends CommonServ {
	private static final String OFFICE="署发事项";
	private static final String BUREAU="司内事项";
	private static final String POINT="要点类事项";
    /**
     * 条件查询
     * @param paramBean
     * @return
     * @author kfzx-zhangsy
     */
    public OutBean findByAuthority(ParamBean paramBean){
    	String dw = paramBean.getStr("dw1");
    	//    	分页参数
    	String limitStr = paramBean.getStr("limit");
	    String pageStr = paramBean.getStr("page");
	    int limit = limitStr.equals("") ? 10 :Integer.parseInt(limitStr);
	    int currentPageNum = pageStr.equals("") ? 1 :Integer.parseInt(pageStr);
	    int offset = (currentPageNum - 1)*limit + 1;
	    //查询总数
	    SqlBean sqlBean = limitQuery(paramBean);
    	sqlBean.selects("COUNT(1) AS COUNT");
    	//分页查询
    	SqlBean sqlBean1 = limitQuery(paramBean);
		sqlBean1.limit(limit);
		sqlBean1.page(currentPageNum);
		List<Bean> countall = new ArrayList<>();
		List<Bean> finds = new ArrayList<>();
		//署发事项查询
	    if(StringUtils.equals(OFFICE,dw)){
	    	countall = ServDao.finds("OA_SUP_QUERY_OFFICE",sqlBean);
    		finds = ServDao.finds("OA_SUP_QUERY_OFFICE",sqlBean1);
    	}
	    //司内事项查询
	    if(StringUtils.equals(BUREAU,dw)){
	    	countall = ServDao.finds("OA_SUP_QUERY_BUREAU",sqlBean);
    		finds = ServDao.finds("OA_SUP_QUERY_BUREAU",sqlBean1);
    	}
	    //要点类事项查询
	    if(StringUtils.equals(POINT,dw)){
	    	countall = ServDao.finds("OA_SUP_QUERY_POINT",sqlBean);
    		finds = ServDao.finds("OA_SUP_QUERY_POINT",sqlBean1);
    	}
	    //处理数据
	    List<Bean> list = dealData(finds, paramBean);
	    int COUNT = 0;
	    if(countall.size()>0){
	    	COUNT = Integer.parseInt(countall.get(0).getStr("COUNT"));
	    }
	    OutBean outBean = new OutBean();
        outBean.put("list",list);
        outBean.put("count",COUNT);
        outBean.put("offset",offset);
    	return outBean;
    }
    /**
     * 对查询数据进行处理
     * @param query
     * @param paramBean
     * @return
     * @author kfzx-zhangsy
     */
    public List<Bean> dealData(List<Bean> list,ParamBean paramBean) {
         //查询办理情况
         List<Bean> gainList = Transaction.getExecutor().query("select t1.GAIN_TEXT,t1.APPRO_ID," +
                 "t2.DEPT_NAME,t1.GAIN_MONTH FROM SUP_APPRO_GAIN t1,SY_ORG_DEPT t2 " +
                 "WHERE t1.DEPT_CODE = t2.DEPT_CODE ORDER BY t1.GAIN_MONTH DESC");
         for (int i = 0;i<list.size();i++){
             String s_wf_state = list.get(i).get("S_WF_STATE").toString();
             String apply_state = list.get(i).get("APPLY_STATE").toString();
           //处理显示的状态
//             1.立项草稿2.立项审批中,3.立项审批通过,4.分发提交填写计划,5.分发提交办理情况,6.办结审批中,7.办结审批通过,8.已归档,20.待下次发送
             if (StringUtils.equals("11",s_wf_state)||StringUtils.equals("1",apply_state)||
            		 StringUtils.equals("2",apply_state)||StringUtils.equals("3",apply_state)){
                 list.get(i).set("S_WF_STATE","立项登记中");
             }
//             if (StringUtils.equals("1",plan_state)||StringUtils.equals("2",plan_state)||StringUtils.equals("4",apply_state)){
             else if (StringUtils.equals("4",apply_state)){
                 list.get(i).set("S_WF_STATE","事项计划中");
             }
//        	 if (StringUtils.equals("1",gain_state)||StringUtils.equals("2",gain_state)||StringUtils.equals("5",apply_state)){
             else if (StringUtils.equals("5",apply_state)){
                 list.get(i).set("S_WF_STATE","事项办理中");
             }
             else if (StringUtils.equals("6",apply_state)){
                 list.get(i).set("S_WF_STATE","事项待审核");
             }
             else if (StringUtils.equals("7",apply_state)&&StringUtils.equals("1",s_wf_state)){
                 list.get(i).set("S_WF_STATE","近期已办结");
             }
             else if (StringUtils.equals("7",apply_state)&&StringUtils.equals("2",s_wf_state)){
                 list.get(i).set("S_WF_STATE","已归档");
             }
             //如果无完成时限 则显示原因
             String limit_date = list.get(i).get("LIMIT_DATE").toString();
             String not_limit_date_reason = list.get(i).get("NOT_LIMIT_TIME_REASON").toString();
             if(StringUtils.isEmpty(limit_date)){
             	list.get(i).set("LIMIT_DATE",not_limit_date_reason);
             }
           //对查询出来的数据进行处理  办理情况
             String id = list.get(i).get("ID").toString();
             //牵头主办放在前面
             //时间降序
             String GAIN_TEXT_MAIN ="";
             String GAIN_TEXT_OTHER ="";
             String main_dept = list.get(i).get("MAIN_DEPT").toString();
             for (int g = 0;g<gainList.size();g++){
                 if (StringUtils.equals(gainList.get(g).get("APPRO_ID").toString(),id)){
                     String gain_text = gainList.get(g).get("GAIN_TEXT").toString();
                     String dept_name1 = gainList.get(g).get("DEPT_NAME").toString();
                     String gain_month = gainList.get(g).get("GAIN_MONTH").toString();
                     if(StringUtils.equals(main_dept, dept_name1)){
                     	GAIN_TEXT_MAIN+=dept_name1+"在"+gain_month+"月的办理情况为："+gain_text+"\n";
                     }else{
                     	GAIN_TEXT_OTHER+=dept_name1+"在"+gain_month+"月的办理情况为："+gain_text+"\n";
                     }
                 }
             }
             list.get(i).set("GAIN_TEXT",GAIN_TEXT_MAIN+GAIN_TEXT_OTHER);
         }
         return list;
	}
    /**
     * 查询主办单位和协办单位，绑定查询条件
     * @param paramBean
     * @return
     * @author kfzx-zhangsy
     */
    public OutBean findDept(ParamBean paramBean){
        OutBean outBean = new OutBean();
        String dw = paramBean.get("dw1").toString();
        if (StringUtils.equals(OFFICE,dw)){
        	//署内牵头主办
            String sql = "select distinct t2.DEPT_NAME from SUP_APPRO_OFFICE_DEPT t1,SY_ORG_DEPT t2 where t1.DEPT_CODE = t2.DEPT_CODE AND t1.DEPT_TYPE = '1'";
            List<Bean> list = Transaction.getExecutor().query(sql);
            outBean.put("main",list);
            //署内其他主办
            String sql1 = "select distinct t2.DEPT_NAME from SUP_APPRO_OFFICE_DEPT t1,SY_ORG_DEPT t2 where t1.DEPT_CODE = t2.DEPT_CODE AND t1.DEPT_TYPE = '3'";
            List<Bean> list1 = Transaction.getExecutor().query(sql1);
            outBean.put("sub",list1);
            //署内协办单位
            String sql2 = "select distinct t2.DEPT_NAME from SUP_APPRO_OFFICE_DEPT t1,SY_ORG_DEPT t2 where t1.DEPT_CODE = t2.DEPT_CODE AND t1.DEPT_TYPE = '2'";
            List<Bean> list2 = Transaction.getExecutor().query(sql2);
            outBean.put("other",list2);
            return outBean;
        }else if (StringUtils.equals(BUREAU,dw)){
        	//司内牵头主办
            String sql = "select distinct t2.DEPT_NAME from SUP_APPRO_BUREAU_STAFF t1,SY_ORG_DEPT t2 where t1.DEPT_CODE = t2.DEPT_CODE AND t1.DEPT_TYPE = '1'";
            List<Bean> list = Transaction.getExecutor().query(sql);
            outBean.put("main",list);
            //司内其他主办
            String sql1 = "select distinct t2.DEPT_NAME from SUP_APPRO_BUREAU_STAFF t1,SY_ORG_DEPT t2 where t1.DEPT_CODE = t2.DEPT_CODE AND t1.DEPT_TYPE = '3'";
            List<Bean> list1 = Transaction.getExecutor().query(sql1);
            outBean.put("sub",list1);
            //司内协办单位
            String sql2 = "select distinct t2.DEPT_NAME from SUP_APPRO_BUREAU_STAFF t1,SY_ORG_DEPT t2 where t1.DEPT_CODE = t2.DEPT_CODE AND t1.DEPT_TYPE = '2'";
            List<Bean> list2 = Transaction.getExecutor().query(sql2);
            outBean.put("other",list2);
            return outBean;
        }else{
        	//要点牵头主办
            String sql = "select distinct PROVIN_NAME as DEPT_NAME from SUP_APPRO_POINT";
            List<Bean> list = Transaction.getExecutor().query(sql);
            outBean.put("main",list);
            //要点其他主办
            String sql1 = "select distinct LIABLE_OFFICE as DEPT_NAME from SUP_APPRO_POINT";
            List<Bean> list1 = Transaction.getExecutor().query(sql1);
            HashMap<String, Object> map = new HashMap<>();
            outBean.put("other",list1);
            List<Object> list2 = new ArrayList<>();
            map.put("DEPT_NAME","");
            list2.add(map);
            outBean.put("sub",list2);
            return outBean;
        }
    }

    /**
     * 判断当前用户权限，限制查询范围
     * @param paramBean
     * @return
     * @author kfzx-zhangsy
     */
    public SqlBean limitQuery(ParamBean paramBean){
    	//获取当前登录用户信息
    	Bean userBean = Context.getUserBean();
    	//user_code
    	String user_code = userBean.getStr("USER_CODE");
    	//权限集合
    	String role_codes = userBean.getStr("ROLE_CODES");
    	//部门编码和名字
    	String dept_code = userBean.getStr("DEPT_CODE");
    	String dept_name = userBean.getStr("DEPT_NAME");
        String[] split = role_codes.replace("'", "").split(",");
      //署发事项，司内事项，要点类事项类
        String dw = paramBean.get("dw1").toString();
        //系统编号
        String s_code = paramBean.get("S_CODE").toString();
        //督查事项
        String superv_item = paramBean.get("SUPERV_ITEM").toString();
        //事项来源
        String item_source = paramBean.get("ITEM_SOURCE").toString();
        //发文字号
        String issue_code = paramBean.get("ISSUE_CODE").toString();
        //立项时间 查询期间开始
        String appr_date_begin = paramBean.get("APPR_DATE_BEGIN").toString();
      //办结时间 查询期间开始
        String dealt_time_begin = paramBean.get("DEALT_TIME_BEGIN").toString();
      //办结时间 查询期间结束
        String dealt_time_end = paramBean.get("DEALT_TIME_END").toString();
        //拟稿人姓名
        String s_uname = paramBean.get("S_UNAME").toString();
      //立项时间 查询期间结束
        String appr_date_end = paramBean.get("APPR_DATE_END").toString();
      //完成时限 查询期间开始
        String limit_date_begin = paramBean.get("LIMIT_DATE_BEGIN").toString();
      //完成时限 查询期间结束
        String limit_date_end = paramBean.get("LIMIT_DATE_END").toString();
        //牵头主办单位
        String main_dept = paramBean.get("MAIN_DEPT").toString();
        //状态
        String s_wf_state = paramBean.get("S_WF_STATE").toString();
      //更新时间 查询期间开始
        String s_mtime_begin = paramBean.get("S_MTIME_BEGIN").toString();
      //更新时间 查询期间结束
        String s_mtime_end = paramBean.get("S_MTIME_END").toString();
        //当前处理人
        String done_user_name = paramBean.get("DONE_USER_NAME").toString();
        //协办单位
        String assist_dept = paramBean.get("ASSIST_DEPT").toString();
        //其他主办单位
        String other_main_dept = paramBean.get("OTHER_MAIN_DEPT").toString();
        //立项编号
        String item_num = paramBean.get("ITEM_NUM").toString();
        SqlBean sqlBean = new SqlBean();
        sqlBean.selects("*");
        if (s_wf_state.length()>2){
        	//按照状态查询sql拼接
            StringBuffer s_wf_state_buffer = new StringBuffer();
            s_wf_state_buffer.append(" AND (");
            if (s_wf_state.contains("事项计划中")){
            	s_wf_state_buffer.append(" OR APPLY_STATE = '4'");
            }
            if (s_wf_state.contains("事项办理中")){
            	s_wf_state_buffer.append(" OR APPLY_STATE = '5'");
            }
            if (s_wf_state.contains("近期已办结")){
            	s_wf_state_buffer.append(" OR (APPLY_STATE = '7' AND S_WF_STATE = 1)");            
            }
            if (s_wf_state.contains("已归档")){
            	s_wf_state_buffer.append(" OR (APPLY_STATE = '7' AND S_WF_STATE = 2)");  
            }
            if (s_wf_state.contains("事项待审核")){
            	s_wf_state_buffer.append(" OR APPLY_STATE='6'");
            }
            if (s_wf_state.contains("立项登记中")){
            	s_wf_state_buffer.append(" OR (APPLY_STATE='1' OR APPLY_STATE = '2' OR S_WF_STATE = 11)");
            }
            if(s_wf_state_buffer.toString().contains("OR")){
            	int indexOf = s_wf_state_buffer.indexOf("OR");
                s_wf_state_buffer.replace(indexOf, indexOf+2, "");
            }else{
            	s_wf_state_buffer.append("1=1");
            }
            s_wf_state_buffer.append(")");
            sqlBean.appendWhere(s_wf_state_buffer.toString());
        }
        //系统编号模糊查询
        if(StringUtils.isNotEmpty(s_code)){
            sqlBean.andLike("S_CODE", s_code);
        }
        if(StringUtils.isNotEmpty(superv_item)){
            sqlBean.andLike("SUPERV_ITEM", superv_item);
        }
        if(StringUtils.isNotEmpty(item_source)){
            sqlBean.andLike("ITEM_SOURCE", item_source);
        }
        if(StringUtils.isNotEmpty(issue_code)){
            sqlBean.andLike("ISSUE_CODE", issue_code);
        }
        if(StringUtils.isNotEmpty(item_num)){
            sqlBean.andLike("ITEM_NUM", item_num);
            
        }
        //日期区间包左也包右
        if(StringUtils.isNotEmpty(appr_date_begin)&&StringUtils.isNotEmpty(appr_date_end)){
            sqlBean.andLike("APPR_DATE", "20");
            sqlBean.andGTE("TO_CHAR(TO_DATE(APPR_DATE, 'YYYY-MM-DD'),'YYYY-MM-DD')", appr_date_begin);
            sqlBean.andLTE("TO_CHAR(TO_DATE(APPR_DATE, 'YYYY-MM-DD'),'YYYY-MM-DD')", appr_date_end);
        }
      //日期区间包左也包右
        if(StringUtils.isNotEmpty(dealt_time_begin)&&StringUtils.isNotEmpty(dealt_time_end)){
            sqlBean.andLike("DEALT_TIME", "20");
            sqlBean.andGTE("TO_CHAR(TO_DATE(DEALT_TIME, 'YYYY-MM-DD'),'YYYY-MM-DD')", dealt_time_begin);
            sqlBean.andLTE("TO_CHAR(TO_DATE(DEALT_TIME, 'YYYY-MM-DD'),'YYYY-MM-DD')", dealt_time_end);
        }
        if(StringUtils.isNotEmpty(s_uname)){
            sqlBean.andLike("S_UNAME", s_uname);
        }
        if(StringUtils.isNotEmpty(done_user_name)){
            sqlBean.andLike("DONE_USER_NAME", done_user_name);
        }
        if(StringUtils.isNotEmpty(assist_dept)){
            sqlBean.andLike("ASSIST_DEPT", assist_dept);
        }
        if(StringUtils.isNotEmpty(other_main_dept)){
            sqlBean.andLike("OTHER_MAIN_DEPT", other_main_dept);
        }
      //日期区间包左也包右
        if(StringUtils.isNotEmpty(limit_date_begin)&&StringUtils.isNotEmpty(limit_date_end)){
            sqlBean.andLike("LIMIT_DATE", "20");
            sqlBean.andGTE("TO_CHAR(TO_DATE(LIMIT_DATE, 'YYYY-MM-DD'),'YYYY-MM-DD')", limit_date_begin);
            sqlBean.andLTE("TO_CHAR(TO_DATE(LIMIT_DATE, 'YYYY-MM-DD'),'YYYY-MM-DD')", limit_date_end);
        }
        if(StringUtils.isNotEmpty(main_dept)){
            sqlBean.and("MAIN_DEPT", main_dept);
        }
      //日期区间包左也包右
        if(StringUtils.isNotEmpty(s_mtime_begin)&&StringUtils.isNotEmpty(s_mtime_end)){
            sqlBean.andLike("S_MTIME", "20");
            sqlBean.andGTE("TO_CHAR(TO_DATE(SUBSTR(S_MTIME,1,10), 'YYYY-MM-DD'),'YYYY-MM-DD')", s_mtime_begin);
            sqlBean.andLTE("TO_CHAR(TO_DATE(SUBSTR(S_MTIME,1,10), 'YYYY-MM-DD'),'YYYY-MM-DD')", s_mtime_end);
        }
        // 查询当前用户的所有  根据角色判断查询范围
        List<String> roleList = new ArrayList<>();
        for (int i = 0;i<split.length;i++){
            roleList.add(split[i]);
        }
        //归口管理司局开关是否打开
        List<Bean> stlist = Transaction.getExecutor().query("SELECT ST_VALUE FROM SUP_SERV_SETTING_VALUE WHERE ST_KEY='MgrBureauRead'");
        String st_value = "";
        if(stlist.size()>0){
        	st_value = stlist.get(0).get("ST_VALUE").toString();
        }
        //办公厅主任，办公厅督察处经办，办公厅督察处复核
        if (roleList.contains("SUP015")||roleList.contains("SUP_DC_001")||roleList.contains("SUP_DC_002")){
            sqlBean.appendWhere("");
        }
        //机构督察员  roleList.contains("SUP003")||roleList.contains("SUP025")||roleList.contains("SUP026")||roleList.contains("SUP017")
        //机构督察员可以看本机构人员立项和下级人员立项 流经本机构及下级机构  本机构和下级机构的待办 立项       处室督察员只能看自己立项和流经本处室的立项
        //机构督察员  省厅机构督察员
        else if (roleList.contains("SUP003")||roleList.contains("SUP025")){
            if(StringUtils.equals(OFFICE,dw)){
                sqlBean.appendWhere(" AND (ID IN (SELECT DISTINCT T1.ID FROM SUP_APPRO_OFFICE T1,SUP_APPRO_OFFICE_DEPT T2 WHERE T1.ID = T2.OFFICE_ID AND T2.DEPT_CODE IN "+
							"(SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?))"
							+ " OR SY_ORG_DEPT.DEPT_CODE= ? )) OR S_USER IN "+
							"(SELECT DISTINCT T2.USER_CODE FROM SY_ORG_DEPT T1,SY_ORG_USER T2 WHERE T2.DEPT_CODE = T1.DEPT_CODE AND T1.DEPT_CODE IN (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?))"
								+ " OR SY_ORG_DEPT.DEPT_CODE=?)) OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE IN "
										+ "(SELECT DISTINCT T2.USER_CODE FROM SY_ORG_DEPT T1,SY_ORG_USER T2 WHERE T2.DEPT_CODE = T1.DEPT_CODE AND T1.DEPT_CODE IN (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?)) OR SY_ORG_DEPT.DEPT_CODE= ?))))",
										dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code);
            }
            if(StringUtils.equals(BUREAU,dw)){
            	sqlBean.appendWhere(" AND (ID IN (SELECT DISTINCT T1.ID FROM SUP_APPRO_BUREAU T1,SUP_APPRO_BUREAU_STAFF T2 WHERE T1.ID = T2.BUREAU_ID AND T2.DEPT_CODE IN "+
						"(SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?))"
								+ " OR SY_ORG_DEPT.DEPT_CODE= ?)) OR S_USER IN "+
						"(SELECT DISTINCT T2.USER_CODE FROM SY_ORG_DEPT T1,SY_ORG_USER T2 WHERE T2.DEPT_CODE = T1.DEPT_CODE AND T1.DEPT_CODE IN (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?))"
								+ " OR SY_ORG_DEPT.DEPT_CODE= ?)) OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE IN "
										+ "(SELECT DISTINCT T2.USER_CODE FROM SY_ORG_DEPT T1,SY_ORG_USER T2 WHERE T2.DEPT_CODE = T1.DEPT_CODE AND T1.DEPT_CODE IN (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?)) OR SY_ORG_DEPT.DEPT_CODE=?))))",
										dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code);
            }
            
            if(StringUtils.equals(POINT,dw)){
            	//还需要判断开关是否开启
                if(StringUtils.equals("1", st_value)){
                	 //机构督察员只能查询归口管理司局为自己部门的要点类事项
                	sqlBean.appendWhere(" AND ((ID IN (SELECT DISTINCT T1.ID FROM SUP_APPRO_POINT T1 WHERE T1.DEPT_CODE IN "+
						"(SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?))"
								+ " OR SY_ORG_DEPT.DEPT_CODE= ?))) OR S_USER IN "+
						"(SELECT DISTINCT T2.USER_CODE FROM SY_ORG_DEPT T1,SY_ORG_USER T2 WHERE T2.DEPT_CODE = T1.DEPT_CODE AND T1.DEPT_CODE IN (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?))"
								+ " OR SY_ORG_DEPT.DEPT_CODE= ?)) OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE IN "
										+ "(SELECT DISTINCT T2.USER_CODE FROM SY_ORG_DEPT T1,SY_ORG_USER T2 WHERE T2.DEPT_CODE = T1.DEPT_CODE AND T1.DEPT_CODE IN (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?)) OR SY_ORG_DEPT.DEPT_CODE= ?))) AND CENTRALIED_MGR_BUREAU LIKE ?)",
										dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,"%"+dept_name+"%");
                }else{
                	sqlBean.appendWhere(" AND ((ID IN (SELECT DISTINCT T1.ID FROM SUP_APPRO_POINT T1 WHERE T1.DEPT_CODE IN "+
						"(SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?))"
								+ " OR SY_ORG_DEPT.DEPT_CODE=?))) OR S_USER IN "+
						"(SELECT DISTINCT T2.USER_CODE FROM SY_ORG_DEPT T1,SY_ORG_USER T2 WHERE T2.DEPT_CODE = T1.DEPT_CODE AND T1.DEPT_CODE IN (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?))"
								+ " OR SY_ORG_DEPT.DEPT_CODE=?)) OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE IN "
										+ "(SELECT DISTINCT T2.USER_CODE FROM SY_ORG_DEPT T1,SY_ORG_USER T2 WHERE T2.DEPT_CODE = T1.DEPT_CODE AND T1.DEPT_CODE IN (SELECT DEPT_CODE FROM SY_ORG_DEPT WHERE (ODEPT_CODE = ? AND DEPT_LEVEL = (SELECT DEPT_LEVEL+1 FROM SY_ORG_DEPT WHERE DEPT_CODE = ?)) OR SY_ORG_DEPT.DEPT_CODE=?))))",
										dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code,dept_code);
                }
            }
        }
        //处室督察员   省厅处室督察员
        else if (roleList.contains("SUP017")||roleList.contains("SUP026")){
            if(StringUtils.equals(OFFICE,dw)){
            	sqlBean.appendWhere(" AND (ID IN (SELECT DISTINCT T1.ID FROM SUP_APPRO_OFFICE T1,SUP_APPRO_OFFICE_DEPT T2 WHERE T1.ID = T2.OFFICE_ID AND T2.DEPT_CODE =?) OR S_USER = ? OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE = ?))",dept_code,user_code,user_code);
            }
            if(StringUtils.equals(BUREAU,dw)){
            	sqlBean.appendWhere(" AND (ID IN (SELECT DISTINCT T1.ID FROM SUP_APPRO_BUREAU T1,SUP_APPRO_BUREAU_STAFF T2 WHERE T1.ID = T2.BUREAU_ID AND T2.DEPT_CODE =?) OR S_USER = ? OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE = ?))",dept_code,user_code,user_code);
            }
            if(StringUtils.equals(POINT,dw)){
            	//还需要判断开关是否开启
                if(StringUtils.equals("1", st_value)){
                	 //机构督察员只能查询归口管理司局为自己部门的要点类事项
                	sqlBean.appendWhere(" AND (((MAIN_DEPT=? OR OTHER_MAIN_DEPT LIKE ?)) OR S_USER =? OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE = ?)) AND CENTRALIED_MGR_BUREAU LIKE ?",dept_name,"%"+dept_name+"%",user_code,user_code,"%"+dept_name+"%");
                }else{
                	sqlBean.appendWhere("  AND (((MAIN_DEPT=? OR OTHER_MAIN_DEPT LIKE ?)) OR S_USER =? OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE = ?))",dept_name,"%"+dept_name+"%",user_code,user_code);
                }
            }
        }
        //其他角色只能查询流经该角色的数据
        //流程表中处理过+立项人+待办
        else{
            if(StringUtils.equals(OFFICE,dw)){
            	sqlBean.appendWhere(" AND (ID IN (SELECT DISTINCT t1.ID FROM SUP_APPRO_OFFICE t1,SY_WFE_NODE_INST t2 " +
                        "where t1.S_WF_INST=t2.PI_ID AND t2.DONE_USER_ID = ? \n" +
                        ") OR S_USER = ?  OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE = ?))",user_code,user_code,user_code);
            }
            if(StringUtils.equals(BUREAU,dw)){
            	sqlBean.appendWhere(" AND (ID IN (SELECT DISTINCT t1.ID FROM SUP_APPRO_BUREAU t1,SY_WFE_NODE_INST t2 " +
                        "where t1.S_WF_INST=t2.PI_ID AND t2.DONE_USER_ID = ? \n" +
                        ") OR S_USER = ?  OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE = ?))",user_code,user_code,user_code);
            }
            if(StringUtils.equals(POINT,dw)){
            	sqlBean.appendWhere(" AND (ID IN (SELECT DISTINCT t1.ID FROM SUP_APPRO_POINT t1,SY_WFE_NODE_INST t2 " +
                        "where t1.S_WF_INST=t2.PI_ID AND t2.DONE_USER_ID = ? \n" +
                        ") OR S_USER = ?  OR ID IN (SELECT DISTINCT TODO_OBJECT_ID1 FROM SY_COMM_TODO WHERE OWNER_CODE = ?))",user_code,user_code,user_code);
            }
        }
        //查询结果按更新时间降序排列
        sqlBean.desc("S_MTIME");
        return sqlBean;
    }
    /**
     * @author kfzx-zhangsy
     * @param paramBean
     */
    public void exportExcel(ParamBean paramBean) {
    	String dw = paramBean.getStr("dw1");
    	//调方法拼接sql
    	SqlBean limitQuery = limitQuery(paramBean);
    	//获取导出的id集合
        String ids =  paramBean.get("ids").toString();
        String[] split = ids.split(",");
        HashMap<String,String> map = new HashMap<>();
        List<Bean> beans = new ArrayList<>();
        List<Bean> query2 = new ArrayList<>();
        //如果勾选 则导出勾选项  
        if (!("".equals(ids))){
        	//查询
        	OutBean findByAuthority = findByAuthority(paramBean);
        	List<Bean> beanList = (List<Bean>) findByAuthority.get("list");
            for (int i = 0;i<split.length;i++){
                map.put(split[i],"");
            }
            List<String> list = new ArrayList<>();
            Set<String> keys1 = map.keySet();
            for (String key1:keys1){
                list.add(key1);
            }
            for (int i = 0;i<beanList.size();i++){
                if (list.contains(beanList.get(i).get("ID").toString())){
                    beans.add(beanList.get(i));
                }
            }
        }else{
        	if(StringUtils.equals(OFFICE,dw)){
        		query2 = ServDao.finds("OA_SUP_QUERY_OFFICE",limitQuery);
        	}
    	    if(StringUtils.equals(BUREAU,dw)){
    	    	query2 = ServDao.finds("OA_SUP_QUERY_BUREAU",limitQuery);
        	}
    	    if(StringUtils.equals(POINT,dw)){
    	    	query2 = ServDao.finds("OA_SUP_QUERY_POINT",limitQuery);
        	}
        	//如果不勾选 则导出符合条件查询所有数据
        	List<Bean> beanList = dealData(query2, paramBean);
        	for(int i = 0;i<beanList.size();i++){
        		beans.add(beanList.get(i));
        	}
        }
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        //获取文件名称，默认为 ‘报表统计.xls’
        String fileName = paramBean.get("dw1").toString()+"督查事项详情表";
        WritableWorkbook wwb  = null;
        try {
            response.resetBuffer();
            response.setContentType("application/x-msdownload");
            com.rh.core.util.RequestUtils.setDownFileName(request, response, fileName + ".xls");
            OutputStream out = response.getOutputStream();
            wwb  = Workbook.createWorkbook(out);

            //设置标题的字体大小和样式
            WritableFont wfc = new WritableFont(WritableFont.createFont("宋体"),15);
            //设置单元格样式
            WritableCellFormat headerFormats = new WritableCellFormat(wfc);
            //水平居中对齐
            headerFormats.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormats.setVerticalAlignment(VerticalAlignment.CENTRE);

            //设置文字的字体大小和样式
            WritableFont wfcs = new WritableFont(WritableFont.createFont("宋体"),10);
            //设置单元格样式
            WritableCellFormat headerFormat = new WritableCellFormat(wfcs);
            //水平居中对齐
            headerFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            //自动换行
            headerFormat.setWrap(true);

            //生成第一页工作表
            WritableSheet sheetOne=wwb.createSheet("第一页",0);

            //第一列第一行——第二列第一行
            Label label1=new Label(0,0,"系统编号",headerFormat);
            Label label2=new Label(1,0,"督查事项",headerFormat);
            Label label3=new Label(2,0,"事项来源（文件标题等）",headerFormat);
            Label label4=new Label(3,0,"发文字号",headerFormat);
            Label label5=new Label(4,0,"立项时间",headerFormat);
            Label label6=new Label(5,0,"完成时限",headerFormat);
            Label label7=new Label(6,0,"办结时间",headerFormat);
            Label label8=new Label(7,0,"主办单位",headerFormat);
            Label label9=new Label(8,0,"其他主办单位",headerFormat);
            Label label10=new Label(9,0,"协办单位",headerFormat);
            Label label11=new Label(10,0,"办理情况",headerFormat);
            Label label12=new Label(11,0,"更新时间",headerFormat);
            Label label13=new Label(12,0,"状态",headerFormat);
            Label label14=new Label(13,0,"立项人",headerFormat);
            Label label15=new Label(14,0,"当前处理人",headerFormat);
            Label label16=new Label(15,0,"立项编号",headerFormat);
            //添加进第一页
            sheetOne.addCell(label1);
            sheetOne.addCell(label2);
            sheetOne.addCell(label3);
            sheetOne.addCell(label4);
            sheetOne.addCell(label5);
            sheetOne.addCell(label6);
            sheetOne.addCell(label7);
            sheetOne.addCell(label8);
            sheetOne.addCell(label9);
            sheetOne.addCell(label10);
            sheetOne.addCell(label11);
            sheetOne.addCell(label12);
            sheetOne.addCell(label13);
            sheetOne.addCell(label14);
            sheetOne.addCell(label15);
            sheetOne.addCell(label16);
            
            //动态绑定数据
            for( int i = 0 ; i < beans.size() ; i++ ){
                Bean bean = beans.get(i);
                label1 = new Label(0,1+i,bean.getStr("S_CODE"),headerFormat);
                label2 = new Label(1,1+i,bean.getStr("SUPERV_ITEM"),headerFormat);
                label3 = new Label(2,1+i,bean.getStr("ITEM_SOURCE"),headerFormat);
                label4 = new Label(3,1+i,bean.getStr("ISSUE_CODE"),headerFormat);
                label5 = new Label(4,1+i,bean.getStr("APPR_DATE"),headerFormat);
                label6 = new Label(5,1+i,bean.getStr("LIMIT_DATE"),headerFormat);
                label7 = new Label(6,1+i,bean.getStr("DEALT_TIME"),headerFormat);
                label8 = new Label(7,1+i,bean.getStr("MAIN_DEPT"),headerFormat);
                label9 = new Label(8,1+i,bean.getStr("OTHER_MAIN_DEPT"),headerFormat);
                label10 = new Label(9,1+i,bean.getStr("ASSIST_DEPT"),headerFormat);
                label11 = new Label(10,1+i,bean.getStr("GAIN_TEXT"),headerFormat);
                label12 = new Label(11,1+i,bean.getStr("S_MTIME"),headerFormat);
                label13 = new Label(12,1+i,bean.getStr("S_WF_STATE"),headerFormat);
                label14 = new Label(13,1+i,bean.getStr("S_UNAME"),headerFormat);
                label15 = new Label(14,1+i,bean.getStr("DONE_USER_NAME"),headerFormat);
                label16 = new Label(15,1+i,bean.getStr("ITEM_NUM"),headerFormat);
                
                sheetOne.addCell(label1);
                sheetOne.addCell(label2);
                sheetOne.addCell(label3);
                sheetOne.addCell(label4);
                sheetOne.addCell(label5);
                sheetOne.addCell(label6);
                sheetOne.addCell(label7);
                sheetOne.addCell(label8);
                sheetOne.addCell(label9);
                sheetOne.addCell(label10);
                sheetOne.addCell(label11);
                sheetOne.addCell(label12);
                sheetOne.addCell(label13);
                sheetOne.addCell(label14);
                sheetOne.addCell(label15);
                sheetOne.addCell(label16);
            }
            wwb.write();
            //关闭流
            closeStream(wwb, response);
        } catch (Exception e) {
        }finally{
        }
    }
    /**
     * @author kfzx-zhangsy
     * @param wookBook
     * @param response
     */
    private static void closeStream(WritableWorkbook wookBook, HttpServletResponse response){
        if (wookBook != null) {
            try {
                wookBook.close();
            } catch (Exception e) {
                /*log.error(e.getMessage(), e);*/
            }
        }
        if (response != null && !response.isCommitted()) {
            try {
                response.flushBuffer();
            } catch (Exception e) {
                /*log.error(e.getMessage(), e);*/
            }
        }
    }
}
