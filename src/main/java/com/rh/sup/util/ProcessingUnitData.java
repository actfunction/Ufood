package com.rh.sup.util;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.org.UserBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProcessingUnitData {

    /**
     * 主办单位数据处理
     * @param bean
     */
    public static Bean Processing(Bean bean , String Appro){
        String DIRECT = "" ;
        String JOINTLY = "" ;
        String DETAIL_TEXT = "" ;
        Date date = new Date();
        List<Bean> list =null;
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //根据所传对象的ID查询其立项单办理单位的数据
        stringBuilder.append("SELECT  t1.DEPT_TYPE,t2.DEPT_NAME FROM "+Appro+" t1,SY_ORG_DEPT t2 WHERE ");
        if(Appro.equals("SUP_APPRO_OFFICE_DEPT")){
            stringBuilder.append(" t1.OFFICE_ID = '");
        }else{
            stringBuilder.append(" t1.BUREAU_ID = '");
        }
        stringBuilder.append(bean.getStr("ID"));
        stringBuilder.append("' AND t1.DEPT_CODE=t2.DEPT_CODE");
        //调用接口查询数据
        List<Bean> beanList1 = Transaction.getExecutor().query(stringBuilder.toString());
        long time = 0 ;
        long beign = 0 ;
        long end = 0 ;
        if (bean.getStr("Handling").equals("执行")){
            time = date.getTime();
            list = PlanSelect(bean);
        }
        //遍历查询结果处理其办理单位的顺序以及显示格式
        for (int j = 0 ; j < beanList1.size() ; j++ ){
            Bean bean1 = beanList1.get(j);
            //判断是否为主办单位
            if (bean1.getStr("DEPT_TYPE").equals("1")){
                DIRECT = bean1.getStr("DEPT_NAME") + "," + DIRECT;
                //根据需求判断查询计划处理数据并将数据绑定到对应的对象中
                if (bean.getStr("Handling").equals("执行")){
                    for (int i = 0 ; i < list.size() ; i++ ){
                        try {
                            beign = simpleDateFormat.parse(list.get(i).getStr("BEIGN_DATE")).getTime();
                            end = simpleDateFormat.parse(list.get(i).getStr("END_DATE")).getTime();
                        }catch (Exception e){}
                        if (bean1.getStr("DEPT_NAME").equals(list.get(i).getStr("DEPT_NAME")) && beign < time && time < end){
                            bean.set("BEIGN_END",list.get(i).getStr("BEIGN_DATE")+"至"+list.get(i).getStr("END_DATE"));
                            bean.set("SPEC_FILL",list.get(i).getStr("SPEC_FILL"));
                            bean.set("USER_NAME",list.get(i).getStr("USER_NAME"));
                            DETAIL_TEXT = list.get(i).getStr("DETAIL_TEXT") + " \n" + DETAIL_TEXT ;
                        }
                    }
                }
                //判断是否为其他协办单位
            }else if(bean1.getStr("DEPT_TYPE").equals("2")){
                if(DIRECT.length()==0){
                    DIRECT = bean1.getStr("DEPT_NAME") + "," ;
                }else{
                    DIRECT = DIRECT + bean1.getStr("DEPT_NAME") + ",";
                }
                if (bean.getStr("Handling").equals("执行")){
                    for (int i = 0 ; i < list.size() ; i++ ){
                        try {
                            beign = simpleDateFormat.parse(list.get(i).getStr("BEIGN_DATE")).getTime();
                            end = simpleDateFormat.parse(list.get(i).getStr("END_DATE")).getTime();
                        }catch (Exception e){}
                        if (bean1.getStr("DEPT_NAME").equals(list.get(i).getStr("DEPT_NAME")) && beign < time && time < end){
                            bean.set("BEIGN_END",list.get(i).getStr("BEIGN_DATE")+"至"+list.get(i).getStr("END_DATE"));
                            bean.set("SPEC_FILL",list.get(i).getStr("SPEC_FILL"));
                            bean.set("USER_NAME",list.get(i).getStr("USER_NAME"));
                            DETAIL_TEXT = DETAIL_TEXT + " \n" + list.get(i).getStr("DETAIL_TEXT") ;
                        }
                    }
                }
                //协办单位
            }else{
                JOINTLY = JOINTLY + bean1.getStr("DEPT_NAME")+",";
            }
        }
        //对处理后的数据进行调整绑定到指定的对象当中
        if (bean.getStr("Handling").equals("执行")){
            bean.set("DETAIL_TEXT",DETAIL_TEXT);
        }
        if(DIRECT.length()!=0){
            bean.set("DIRECT",DIRECT.substring(0,DIRECT.length()-1));
        }else{
            bean.set("DIRECT",DIRECT);
        }
        if(JOINTLY.length()!=0){
            bean.set("JOINTLY",JOINTLY.substring(0,JOINTLY.length()-1));
        }else{
            bean.set("JOINTLY",JOINTLY);
        }
        return bean;
    }

    /**
     * 查询办理阶段信息
     * @return
     */
    private static List<Bean> PlanSelect(Bean bean){
        //根据所传对象查询其办理阶段信息
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append("SELECT t1.SPEC_FILL,t1.DEPT_NAME,t2.DETAIL_TEXT,t2.BEIGN_DATE,t2.END_DATE,t1.USER_NAME \n" +
                "FROM SUP_APPRO_PLAN t1,SUP_APPRO_PLAN_CONTENT t2 WHERE t1.APPRO_ID = '");
        stringBuilder1.append(bean.getStr("ID"));
        stringBuilder1.append("' AND t2.PLAN_ID = t1.PLAN_ID " +
                "GROUP BY t1.SPEC_FILL,t1.DEPT_NAME,t2.DETAIL_TEXT,t2.BEIGN_DATE,t2.END_DATE,t1.USER_NAME");
        return Transaction.getExecutor().query(stringBuilder1.toString());
    }

    /**
     * 根据角色限制用户数据权限
     * @param Process
     * @return
     */
    public static String getQueryShiro(String Process){
        UserBean userBean = Context.getUserBean();
        String str = "";
        //获取系统所存储的用户信息，并对信息进行处理
        for (int i = 0 ; i < userBean.getRoleCodes().length ; i++){
            if (i==userBean.getRoleCodes().length-1){
                str += userBean.getRoleCodes()[i];
            }else{
                str += userBean.getRoleCodes()[i] + ",";
            }
        }
        String quanXian = "";
        //判断参数是否为署发流程
        if (Process.equals("Office")){
            //根据不同的角色权限对相应的查询数据范围进行限制
            if(str.indexOf("SUP003") > -1 || str.indexOf("SUP025") > -1){
                quanXian = " AND ID IN (SELECT OFFICE_ID FROM SUP_APPRO_OFFICE_DEPT WHERE DEPT_TYPE IN ('1','2') AND DEPT_CODE = '"+userBean.getTDeptCode()+"')";
            }
            if(str.indexOf("SUP_DC_001") > -1 || str.indexOf("SUP_DC_002") > -1){
                quanXian = " AND 1=1";
            }
            if(quanXian==""){
                quanXian = " AND ID = ''";
            }
            //判断参数是否为司内流程
        }else if(Process.equals("Bureau")){
            //根据不同的角色权限对相应的查询数据范围进行限制
            if(str.indexOf("SUP003") > -1 || str.indexOf("SUP025") > -1){
                quanXian = " AND (ID IN (SELECT BUREAU_ID FROM SUP_APPRO_BUREAU_STAFF WHERE DEPT_TYPE IN ('1','2') AND DEPT_CODE = '"+userBean.getTDeptCode()+"') OR S_TDEPT = '"+userBean.getTDeptCode()+"') ";
            }
            if(str.indexOf("SUP017") > -1 || str.indexOf("SUP026") >-1){
                if(quanXian!=""){
                    quanXian = " AND ((ID IN (SELECT BUREAU_ID FROM SUP_APPRO_BUREAU_STAFF WHERE DEPT_TYPE IN ('1','2') AND DEPT_CODE = '"+userBean.getTDeptCode()+"') OR S_TDEPT = '"+userBean.getTDeptCode()+"') " +
                            " OR (ID IN (SELECT BUREAU_ID FROM SUP_APPRO_BUREAU_STAFF WHERE DEPT_TYPE IN ('1','2') AND DEPT_CODE = '"+userBean.getDeptCode()+"') OR S_USER = '"+userBean.getCode()+"')) ";
                }else{
                    quanXian = " AND (ID IN (SELECT BUREAU_ID FROM SUP_APPRO_BUREAU_STAFF WHERE DEPT_TYPE IN ('1','2') AND DEPT_CODE = '"+userBean.getDeptCode()+"') OR S_USER = '"+userBean.getCode()+"') ";
                }
            }
            if(str.indexOf("SUP_DC_001") > -1 || str.indexOf("SUP_DC_002") > -1){
                quanXian = " AND 1=1";
            }
            if(quanXian==""){
                quanXian = " AND ID = ''";
            }
            //判断参数是否为要点类流程
        }else if(Process.equals("Point")){
            //根据不同的角色权限对相应的查询数据范围进行限制
            if(str.indexOf("SUP003") > -1 || str.indexOf("SUP025") > -1){
                quanXian = " AND t1.DEPT_CODE = '"+userBean.getTDeptCode()+"'";
            }
            if(str.indexOf("SUP_DC_001") > -1 || str.indexOf("SUP_DC_002") > -1){
                quanXian = " AND 1=1";
            }
            if(quanXian==""){
                quanXian = " AND ID = ''";
            }
        }
        return quanXian;
    }

    /**
     * 立项登记拟立项根据角色限制用户数据权限
     * @return
     */
    public static String getQueryNlx(){
        UserBean userBean = Context.getUserBean();
        String str = "";
        //获取系统所存储的用户信息，并对信息进行处理
        for (int i = 0 ; i < userBean.getRoleCodes().length ; i++){
            if (i==userBean.getRoleCodes().length-1){
                str += userBean.getRoleCodes()[i];
            }else{
                str += userBean.getRoleCodes()[i] + ",";
            }
        }
        String quanXian = "";
        //根据不同的角色权限对相应的查询数据范围进行限制
        if(str.indexOf("SUP003") > -1 || str.indexOf("SUP025") > -1){
            quanXian = " AND HOST_TDEPT_CODE = '"+userBean.getTDeptCode()+"'";
        }
        if(str.indexOf("SUP_DC_001") > -1 || str.indexOf("SUP_DC_002") > -1){
            quanXian = " AND 1=1";
        }
        if(quanXian==""){
            quanXian = " AND ID = ''";
        }
        return quanXian;
    }

    /**
     * 根据用户角色限制显示字段
     * @param select
     * @return
     */
    public static StringBuilder getShowShiro(StringBuilder select,String appro){
        UserBean userBean = Context.getUserBean();
        String str = "";
        //获取系统所存储的用户信息，并对信息进行处理
        for (int i = 0 ; i < userBean.getRoleCodes().length ; i++){
            if (i==userBean.getRoleCodes().length-1){
                str += "'" + userBean.getRoleCodes()[i] + "'";
            }else{
                str += "'" + userBean.getRoleCodes()[i] + "',";
            }
        }
        //根据不同的角色权限对相应的查询字段进行数据限制
        if(appro!=""){
            if(str.indexOf("SUP_DC_001") > 0 || str.indexOf("SUP_DC_002") > 0 ){
                select.append(",").append("HOST_LEAD_DENOTE AS STATE");
            }else{
                select.append(",").append("'无权查看' AS STATE");
            }
        }else{
            if(str.indexOf("SUP_DC_001") > 0 || str.indexOf("SUP_DC_002") > 0 ){
                select.append(",").append("HOST_LEAD_DENOTE AS STATE");
            }else{
                select.append(",").append("'无权查看' AS STATE");
            }
        }
        return select;
    }

}
