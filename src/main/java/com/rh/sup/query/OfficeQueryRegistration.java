package com.rh.sup.query;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.bean.PageBean;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.sup.util.FromDbToExcel;
import com.rh.sup.util.ProcessingUnitData;
import java.util.*;

/**
 * 署内事项登记中
 */
public class OfficeQueryRegistration extends CommonServ {

    /**
     * 重写query方法
     * @param paramBean
     * @return
     */
    @Override
    public OutBean query(ParamBean paramBean) {
        //调用查询之前的所执行的方法
        this.beforeQuery(paramBean);
        final ServDefBean serv = ServUtils.getServDef(paramBean.getServId());
        PageBean page = paramBean.getQueryPage();
        int rowCount = paramBean.getShowNum();
        //获取系统参数对分页条件进行设定
        if (rowCount > 0) {
            page.setShowNum(rowCount);
            page.setNowPage(paramBean.getNowPage());
        } else if (!page.contains("SHOWNUM")) {
            if (paramBean.getQueryNoPageFlag()) {
                page.setShowNum(0);
            } else {
                page.setShowNum(serv.getPageCount(50));
            }
        }
        OutBean outBean = new OutBean();
        final LinkedHashMap<String, Bean> cols = new LinkedHashMap();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
        LinkedHashMap<String, Bean> items = serv.getAllItems();
        StringBuilder select = new StringBuilder(serv.getPKey());
        boolean bKey = true;
        //更改sql拼接，将自定义字段进行拼接
        for (String key : items.keySet()) {
            Bean item = items.get(key);
            int listFlag = item.getInt("ITEM_LIST_FLAG");
            if (bKey && item.getStr("ITEM_CODE").equals(serv.getPKey())) { //主键无论是否列表显示都输出
                if (listFlag == ServConstant.ITEM_LIST_FLAG_HIDDEN) { //如果定义为隐藏有数据，则提供给前端时设为不显示
                    listFlag = ServConstant.ITEM_LIST_FLAG_NO;
                }
                addCols(cols, item, listFlag);
                bKey = false;
            } else if (listFlag != ServConstant.ITEM_LIST_FLAG_NO) {
                if (item.getInt("ITEM_TYPE") == ServConstant.ITEM_TYPE_TABLE
                        || item.getInt("ITEM_TYPE") == ServConstant.ITEM_TYPE_VIEW) {
                    if (key.equals("DIRECT") || key.equals("JOINTLY")){
                        select.append(",'' AS ").append(item.get("ITEM_CODE"));
                    }else{
                        select.append(",").append(item.get("ITEM_CODE"));
                    }
                }
                if (listFlag == ServConstant.ITEM_LIST_FLAG_HIDDEN) { //如果定义为隐藏有数据，则提供给前端时设为不显示
                    listFlag = ServConstant.ITEM_LIST_FLAG_NO;
                }
                addCols(cols, item, listFlag);
            }
        } //end for
        //增加程序所定义的其他字段
        sql.append(select+" ,NOT_LIMIT_TIME_REASON,'1' AS FLAG,'' AS INITIATE_LEADER_NAME");
        //设定查询表名，并添加查询条件
        sql.append(" FROM SUP_APPRO_OFFICE  WHERE (APPLY_STATE = '1' OR APPLY_STATE = '2' OR APPLY_STATE = '3') ");
        //拼接用户所选查询条件
        if (paramBean.getId().length() > 0) {
            sql.append(" and " + serv.getPKey() + " in ('" + paramBean.getId().replaceAll(",", "','") + "')");
        }
        //拼接查询权限的限制
        sql.append(ProcessingUnitData.getQueryShiro("Office"));
        //拼接导出数据所需的特定查询条件
        if (paramBean.get("_searchWhere")!=null){
            sql.append(paramBean.get("_searchWhere"));
        }
        //将两个查询数据进行合并
        sql.append(" UNION  SELECT DISTINCT ID,'' AS S_CODE,'' AS SUPERV_ITEM,GW_TITLE AS ITEM_SOURCE,'' ISSUE_CODE,\n" +
                "S_ATIME AS APPR_DATE,'' AS LIMIT_DATE,HOST_TDEPT_CODE AS DIRECT,OTHER_TDEPT_CODE AS JOINTLY,'' AS S_UNAME," +
                "'' AS NOT_LIMIT_TIME_REASON,'0' AS FLAG,INITIATE_LEADER_NAME\n" +
                "FROM SUP_APPRO_INITIATE_GW  WHERE 1 = 1 AND S_WF_STATE = '0'");
        //拼接用户所选查询条件
        if (paramBean.getId().length() > 0) {
            sql.append(" and " + serv.getPKey() + " in ('" + paramBean.getId().replaceAll(",", "','") + "')");
        }
        sql.append(" AND S_TDEPT = '" + Context.getUserBean().getDeptCode() + "' ");
        //拼接查询权限的限制
        sql.append(ProcessingUnitData.getQueryNlx());
        //拼接导出数据所需的特定查询条件
        if (paramBean.get("_searchWhere")!=null){
            sql.append(paramBean.get("_searchWhere"));
        }
        //根据特定条件对数据进行分类
        sql.append(" ORDER BY APPR_DATE DESC");
        List<Bean> list = Transaction.getExecutor().query("SELECT COUNT(0) AS COUNT FROM (" + sql.toString() +")");
        //数据库分页查询
        int rowBegin = (page.getNowPage()-1)*page.getShowNum();
        int rowEnd = page.getNowPage()*page.getShowNum();
        if (rowBegin!=0){
            rowBegin += 1;
        }
        //根据条件查询数据
        List<Bean> list1 = Transaction.getExecutor().query("SELECT * FROM (SELECT ROWNUM AS num,P.* FROM (" + sql.toString() + ") AS P) WHERE num BETWEEN "+ rowBegin +" AND " + rowEnd);
        List<Bean> dataList = new ArrayList<>();
        for( int i = list1.size()-1 ; i >=0  ; i-- ){
            dataList.add(list1.get(i));
        }
        int count = Integer.parseInt(list.get(0).get("COUNT").toString());
        int showCount = page.getShowNum();
        boolean bCount;
        //根据参数对分页类条件进行设定
        if (showCount != 0 && !serv.noCount() && !paramBean.getQueryNoPageFlag()) {
            bCount = true;
        } else {
            bCount = false;
        }
        if (bCount) {
            if (!page.contains("ALLNUM")) {
                int allNum;
                if (page.getNowPage() == 1 && count < showCount) {
                    allNum = count;
                } else {
                    allNum = Integer.parseInt(list.get(0).get("COUNT").toString());
                }
                page.setAllNum((long)allNum);
            }
            outBean.setCount(page.getAllNum());
        } else {
            outBean.setCount((long)dataList.size());
        }
        //将渲染所需数据存放到对象当中
        outBean.set("code", 0);
        outBean.setData(dataList);
        outBean.setPage(page);
        outBean.setCols(cols);
        this.afterQuery(paramBean, outBean);
        return outBean;
    }

    /**
     * 重写方法对个别返回数据进行处理
     * @param paramBean
     * @param outBean
     */
    @Override
    protected void afterQuery(ParamBean paramBean, OutBean outBean) {
        List<Bean> beanList = (List<Bean>) outBean.getData();
        String Appro = "SUP_APPRO_OFFICE_DEPT";
        for (int i = 0 ; i < beanList.size() ; i++ ){
            Bean bean = beanList.get(i);
            //调用方法前台渲染数据进行处理
            if (bean.getStr("ID")!=""){
                bean = ProcessingUnitData.Processing(bean,Appro);
                if(bean.getStr("SUPERV_ITEM")==""){
                    bean.set("GAIN_TEXT",bean.getStr("GAIN_TEXT").substring(0,bean.getStr("GAIN_TEXT").length()-2));
                }
            }
            //设定当字段为空时显示指定字段
            if (bean.getStr("LIMIT_DATE")==""){
                bean.set("LIMIT_DATE",bean.getStr("NOT_LIMIT_TIME_REASON"));
            }
        }
        Collections.reverse(beanList);
        outBean.set(Constant.RTN_DATA,beanList);
    }

    /**
     * 重写导出Excel方法
     * @param paramBean
     * @return
     */
    @Override
    public OutBean exp(ParamBean paramBean) {
        return new FromDbToExcel().QueryStatementExcel(paramBean,this.query(paramBean));
    }
}
