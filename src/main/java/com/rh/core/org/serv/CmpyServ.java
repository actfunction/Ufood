package com.rh.core.org.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.base.db.Transaction;
import com.rh.core.comm.MenuServ;
import com.rh.core.org.util.OrgConstant;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

/**
 * 公司服务类
 * 
 * @author wanglong
 * 
 */
public class CmpyServ extends CommonServ {
    
    @Override
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && paramBean.getAddFlag()) { //公司创建成功自动创建管理员
            addAdmin(outBean.getStr("CMPY_CODE"));
        }
    }
    
    /**
     * 与OA系统同步公司、部门、角色、用户等信息
     * @param paramBean 参数信息
     * @return 同步结果
     */
    public OutBean syncOAOrg(ParamBean paramBean) {
        int count = 0;
        OutBean data;
        //导入公司信息
        data = syncOACmpy(paramBean);
        count += data.getCount();
        //导入部门信息
        data = syncOADept(paramBean);
        count += data.getCount();
        //导入用户信息
        data = syncOAUser(paramBean);
        count += data.getCount();
        //导入角色信息
        data = syncOARole(paramBean);
        count += data.getCount();
        OutBean outBean = new OutBean();
        if (count > 0) {
            outBean.setOk(Context.getSyMsg("SY_SYNC_OK", String.valueOf(count)));
        } else {
            outBean.setError(Context.getSyMsg("SY_SYNC_ERROR"));
        }
        return outBean;
    }

    /**
     * 同步OA公司信息
     * @param paramBean 参数
     * @return 同步结果
     */
    public OutBean syncOACmpy(ParamBean paramBean) {
        int count = 0;
        ParamBean param = new ParamBean("TBL_ZOTN_COMPANY", "finds");
        StringBuilder sql = new StringBuilder("");
        sql.append("COMPANY_ID CMPY_CODE,COMPANY_NAME CMPY_NAME,COMPANY_FULLNAME CMPY_FULLNAME,")
        .append("COMPANY_COUNTRY CMPY_COUNTRY,COMPANY_PROVINCE CMPY_PROVINCE,COMPANY_CITY CMPY_CITY,")
        .append("COMPANY_POSTAL_CODE CMPY_POSTCODE,COMPANY_PHONE CMPY_PHONE,COMPANY_FAX CMPY_FAX,")
        .append("COMPANY_CONTACTOR CMPY_CONTACTOR,COMPANY_PARENT CMPY_PCODE,COMPANY_PRIORITY CMPY_SORT,")
        .append("DESCRIPTION CMPY_DESC,COMPANY_LEVEL CMPY_LEVEL,DEL_FLAG+1 S_FLAG");
        param.set(Constant.PARAM_SELECT, sql.toString());
        param.set(Constant.PARAM_WHERE, "and COMPANY_ID>1");
        List<Bean> dataList = ServMgr.act(param).getDataList();
        sql = new StringBuilder("insert into SY_ORG_CMPY (");
        sql.append("CMPY_CODE,CMPY_NAME,CMPY_FULLNAME,CMPY_COUNTRY,CMPY_PROVINCE,CMPY_CITY,CMPY_ADDRESS,")
        .append("CMPY_POSTCODE,CMPY_PHONE,CMPY_FAX,CMPY_CONTACTOR,CMPY_PCODE,CMPY_SORT,CMPY_DESC,")
        .append("CMPY_LEVEL,S_FLAG) values (");
        sql.append("#CMPY_CODE#,#CMPY_NAME#,#CMPY_FULLNAME#,#CMPY_COUNTRY#,#CMPY_PROVINCE#,#CMPY_CITY#,")
        .append("#CMPY_ADDRESS#,#CMPY_POSTCODE#,#CMPY_PHONE#,#CMPY_FAX#,#CMPY_CONTACTOR#,#CMPY_PCODE#,")
        .append("#CMPY_SORT#,#CMPY_DESC#,#CMPY_LEVEL#,#S_FLAG#)");
        count += Transaction.getExecutor().executeBatchBean(sql.toString(), dataList);
        OutBean outBean = new OutBean();
        if (count > 0) {
            outBean.setOk(Context.getSyMsg("SY_SYNC_OK", String.valueOf(count)));
        } else {
            outBean.setError(Context.getSyMsg("SY_SYNC_ERROR"));
        }
        outBean.setCount(count);
        return outBean;
    }
    
    /**
     * 同步OA部门信息
     * @param paramBean 参数
     * @return 同步结果
     */
    public OutBean syncOADept(ParamBean paramBean) {
        int count = 0;
        ParamBean param = new ParamBean("TBL_ZOTN_DEPARTMENT", "finds");
        StringBuilder sql = new StringBuilder("");
        sql.append("DEPART_ID DEPT_CODE,DEPART_FULLNAME DEPT_NAME,DEPART_PARENT DEPT_PCODE,")
            .append("DEPART_PRIORITY DEPT_SORT,DESCRIPTION DEPT_MEMO,DEPT_EMAIL DEPT_EMAIL,")
            .append("DEPT_LEVEL DEPT_LEVEL,COMPANY_ID CMPY_CODE,")
            .append("DEL_FLAG+1 S_FLAG");
        param.set(Constant.PARAM_SELECT, sql.toString());
        param.set(Constant.PARAM_WHERE, "and COMPANY_ID>1");
        List<Bean> dataList = ServMgr.act(param).getDataList();
        sql = new StringBuilder("insert into SY_ORG_DEPT (");
        sql.append("DEPT_CODE,DEPT_NAME,DEPT_PCODE,DEPT_SORT,DEPT_MEMO,DEPT_EMAIL,")
            .append("DEPT_LEVEL,CMPY_CODE,S_FLAG) values (");
        sql.append("#DEPT_CODE#,#DEPT_NAME#,#DEPT_PCODE#,#DEPT_SORT#,#DEPT_MEMO#,#DEPT_EMAIL#,")
            .append("#DEPT_LEVEL#,#CMPY_CODE#,#S_FLAG#)");
        count += Transaction.getExecutor().executeBatchBean(sql.toString(), dataList);
        OutBean outBean = new OutBean();
        if (count > 0) {
            outBean.setOk(Context.getSyMsg("SY_SYNC_OK", String.valueOf(count)));
        } else {
            outBean.setError(Context.getSyMsg("SY_SYNC_ERROR"));
        }
        outBean.setCount(count);
        return outBean;
    }
    
    /**
     * 同步OA用户信息
     * @param paramBean 参数
     * @return 同步结果
     */
    public OutBean syncOAUser(ParamBean paramBean) {
        int count = 0;
        ParamBean param;
        StringBuilder sql = new StringBuilder("");
        param = new ParamBean("TBL_ZOTN_USER", "finds");
        sql = new StringBuilder("");
        sql.append("USER_ID USER_CODE,USER_LOGIN_NAME USER_LOGIN_NAME,COMPANY_ID CMPY_CODE,")
            .append("USER_NAME USER_NAME,DEPT_CODE DEPT_CODE,USER_OFFICE_PHONE USER_OFFICE_PHONE,")
            .append("USER_MOBILE USER_MOBILE,USER_ICQ USER_QQ,USER_EMAIL USER_EMAIL,")
            .append("USER_PASSWORD USER_PASSWORD, USER_PRIORITY USER_SORT,")
            .append("STAFF_SEX USER_SEX,STAFF_BIRTHDAY USER_BIRTHDAY,STAFF_NATION USER_NATION,")
            .append("STAFF_IDCARD USER_IDCARD,USER_LEVEL USER_POST_LEVEL,DEL_FLAG+1 S_FLAG");
        param.set(Constant.PARAM_SELECT, sql.toString());
        param.set(Constant.PARAM_WHERE, "and COMPANY_ID>1");
        List<Bean> dataList = ServMgr.act(param).getDataList();
        sql = new StringBuilder("insert into SY_ORG_USER (");
        sql.append("USER_CODE,USER_LOGIN_NAME,CMPY_CODE,USER_NAME,DEPT_CODE,USER_OFFICE_PHONE,")
            .append("USER_MOBILE,USER_QQ,USER_EMAIL,USER_PASSWORD,USER_SORT,")
            .append("USER_SEX,USER_BIRTHDAY,USER_NATION,")
            .append("USER_IDCARD,USER_POST_LEVEL,S_FLAG) values (");
        sql.append("#USER_CODE#,#USER_LOGIN_NAME#,#CMPY_CODE#,#USER_NAME#,#DEPT_CODE#,#USER_OFFICE_PHONE#,")
            .append("#USER_MOBILE#,#USER_QQ#,#USER_EMAIL#,#USER_PASSWORD#,#USER_SORT#,")
            .append("#USER_SEX#,#USER_BIRTHDAY#,#USER_NATION#,")
            .append("#USER_IDCARD#,#USER_POST_LEVEL#,#S_FLAG#)");
        count += Transaction.getExecutor().executeBatchBean(sql.toString(), dataList);
        OutBean outBean = new OutBean();
        if (count > 0) {
            outBean.setOk(Context.getSyMsg("SY_SYNC_OK", String.valueOf(count)));
        } else {
            outBean.setError(Context.getSyMsg("SY_SYNC_ERROR"));
        }
        outBean.setCount(count);
        return outBean;
    }
    
    /**
     * 同步OA角色信息
     * @param paramBean 参数
     * @return 同步结果
     */
    public OutBean syncOARole(ParamBean paramBean) {
        int count = 0;
        ParamBean param;
        OutBean out;
        StringBuilder sql = new StringBuilder("");
        //同步角色
        param = new ParamBean("TBL_ZOTN_ROLE", "finds");
        sql = new StringBuilder("");
        sql.append("ROLE_ID ROLE_CODE,ROLE_NAME ROLE_NAME,COMPANY_ID CMPY_CODE,")
            .append("DESCRIPTION ROLE_MEMO,DEL_FLAG+1 S_FLAG");
        param.set(Constant.PARAM_SELECT, sql.toString());
        param.set(Constant.PARAM_WHERE, "and COMPANY_ID>1");
        out = ServMgr.act(param);
        List<Bean> dataList = out.getDataList();
        sql = new StringBuilder("insert into SY_ORG_ROLE (");
        sql.append("ROLE_CODE,ROLE_NAME,CMPY_CODE,ROLE_MEMO,S_FLAG) values (");
        sql.append("#ROLE_CODE#,#ROLE_NAME#,#CMPY_CODE#,#ROLE_MEMO#,#S_FLAG#)");
        count += Transaction.getExecutor().executeBatchBean(sql.toString(), dataList);
        //同步角色下用户
        param = new ParamBean("TBL_ZOTN_ROLE_USER", "finds");
        sql = new StringBuilder("");
        sql.append("RU_ID RU_ID,USER_ID USER_CODE,ROLE_ID ROLE_CODE,")
            .append("DEL_FLAG+1 S_FLAG,CMPY_ID CMPY_CODE");
        param.set(Constant.PARAM_SELECT, sql.toString());
        param.set(Constant.PARAM_WHERE, "and CMPY_ID>1");
        out = ServMgr.act(param);
        dataList = out.getDataList();
        sql = new StringBuilder("insert into SY_ORG_ROLE_USER (");
        sql.append("RU_ID,USER_CODE,ROLE_CODE,S_FLAG,CMPY_CODE) values (");
        sql.append("#RU_ID#,#USER_CODE#,#ROLE_CODE#,#S_FLAG#,#CMPY_CODE#)");
        count += Transaction.getExecutor().executeBatchBean(sql.toString(), dataList);
        
        OutBean outBean = new OutBean();
        if (count > 0) {
            outBean.setOk(Context.getSyMsg("SY_SYNC_OK", String.valueOf(count)));
        } else {
            outBean.setError(Context.getSyMsg("SY_SYNC_ERROR"));
        }
        outBean.setCount(count);
        return outBean;
    }
    
    /**
     * 创建为指定公司编码创建系统管理员
     * @param id 公司编码
     */
    private void addAdmin(String id) {
        String oldCmpy = Context.getCmpy();
        Context.changeCmpy(id); //设定公司
        Bean cmpyBean = ServDao.find(ServMgr.SY_ORG_CMPY, id);
        //先创建根部门
        ParamBean param = new ParamBean(ServMgr.SY_ORG_DEPT, ServMgr.ACT_SAVE);
        param.set("CMPY_CODE", id);
        param.set("DEPT_CODE", id + "0001").set("DEPT_NAME", cmpyBean.getStr("CMPY_NAME"));
        param.set("ODEPT_CODE", id + "0001").set("DEPT_TYPE", OrgConstant.DEPT_TYPE_ORG);
        
        OutBean outBean = ServMgr.act(param);
        //再创建系统管理员
        param = new ParamBean(ServMgr.SY_ORG_USER, ServMgr.ACT_SAVE);
        param.set("CMPY_CODE", id);
        param.set("USER_LOGIN_NAME", "admin").set("USER_NAME", "系统管理员")
            .set("USER_PASSWORD", Context.getSyConf("SY_USER_PASSWORD_INIT", "123456"))
            .set("DEPT_CODE", outBean.getId());
        outBean = ServMgr.act(param);
        //将系统管理员加入系统管理角色
        param = new ParamBean(ServMgr.SY_ORG_ROLE_USER, ServMgr.ACT_SAVE);
        param.set("ROLE_CODE", "RADMIN").set("USER_CODE", outBean.getId()).set("CMPY_CODE", id);
        outBean = ServMgr.act(param);
        Transaction.commit();
        DictMgr.rebuildCache(); //清除缓存
        Context.changeCmpy(oldCmpy); //恢复公司
    }

    /**
     * 复制公共菜单到选中的公司
     * @param paramBean 参数
     * @return 同步结果
     */
    public OutBean copyMenu(ParamBean paramBean) {
        int count = 0;
        String menuIds = paramBean.getStr("MENU_ID");
        if (menuIds.length() > 0) {
            String oldCmpy = Context.getCmpy();
            String cmpy = paramBean.getStr("CMPY_CODE");
            Context.changeCmpy(cmpy); //设定公司
            menuIds = menuIds.replaceAll(",", "','");
            ParamBean param = new ParamBean(ServMgr.SY_COMM_MENU_PUBLIC, ServMgr.ACT_FINDS);
            ServDefBean servDef = ServUtils.getServDef(ServMgr.SY_COMM_MENU_PUBLIC);
            String where = servDef.getServDefWhere() + " and MENU_ID in ('" + menuIds + "')";
            param.set(Constant.PARAM_WHERE, where);
            List<Bean> menuList = ServMgr.act(param).getDataList();
            count = MenuServ.importMenu(menuList, null, ServMgr.SY_COMM_MENU, "");  //批量导入菜单
            Context.changeCmpy(oldCmpy); //还原公司
        }
        OutBean outBean = new OutBean();
        outBean.setOk(Context.getSyMsg("SY_BATCHSAVE_OK", String.valueOf(count)));
        return outBean;
    }
        
    /**
     * 删除之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeDelete(ParamBean paramBean) {
        List<Bean> dataList = paramBean.getDeleteDatas();
        for (Bean dataBean : dataList) {
            String cmpy = dataBean.getId();
            String oldCmpy = Context.getCmpy();
            Context.changeCmpy(cmpy);
            List<Bean> datas;
            ParamBean param, delParam;
            //删除菜单信息
            param = new ParamBean(ServMgr.SY_COMM_MENU, ServMgr.ACT_FINDS).set("S_CMPY", cmpy)
                    .setOrder("MENU_LEVEL desc");
            datas = ServMgr.act(param).getDataList();
            delParam = new ParamBean(ServMgr.SY_COMM_MENU, ServMgr.ACT_DELETE).setDeleteDatas(datas)
                    .setLinkFlag(true).setDeleteDropFlag(true);
            ServMgr.act(delParam);
            //删除角色信息
            param = new ParamBean(ServMgr.SY_ORG_ROLE, ServMgr.ACT_FINDS).set("CMPY_CODE", dataBean.getId())
                    .set("S_PUBLIC", Constant.NO_INT);
            datas = ServMgr.act(param).getDataList();
            delParam = new ParamBean(ServMgr.SY_ORG_ROLE, ServMgr.ACT_DELETE).setDeleteDatas(datas)
                    .setLinkFlag(true).setDeleteDropFlag(true);
            ServMgr.act(delParam);
            //删除用户信息
            param = new ParamBean(ServMgr.SY_ORG_USER, ServMgr.ACT_FINDS).set("CMPY_CODE", dataBean.getId());
            datas = ServMgr.act(param).getDataList();
            delParam = new ParamBean(ServMgr.SY_ORG_USER, ServMgr.ACT_DELETE).setDeleteDatas(datas)
                    .setLinkFlag(true).setDeleteDropFlag(true);
            ServMgr.act(delParam);
            //删除部门信息
            param = new ParamBean(ServMgr.SY_ORG_DEPT, ServMgr.ACT_FINDS).set("CMPY_CODE", dataBean.getId());
            datas = ServMgr.act(param).getDataList();
            delParam = new ParamBean(ServMgr.SY_ORG_DEPT, ServMgr.ACT_DELETE).setDeleteDatas(datas)
                    .setLinkFlag(true).setDeleteDropFlag(true); //忽略假删除，强制进行级联删除
            ServMgr.act(delParam);
            Context.changeCmpy(oldCmpy);
        }
    }
    
    /**
     * 同步分散公司部门到集中公司
     * @param paramBean 参数
     * @return 同步结果
     */
    public OutBean syncCmpyDept(ParamBean paramBean) {
        int count = 0;
        //导入公司数据
        ParamBean param = new ParamBean("TBL_ZOTN_COMPANY", "finds");
        StringBuilder sql = new StringBuilder("");
        sql.append("COMPANY_ID CMPY_CODE,COMPANY_NAME CMPY_NAME,COMPANY_FULLNAME CMPY_FULLNAME,")
        .append("COMPANY_COUNTRY CMPY_COUNTRY,COMPANY_PROVINCE CMPY_PROVINCE,COMPANY_CITY CMPY_CITY,")
        .append("COMPANY_POSTAL_CODE CMPY_POSTCODE,COMPANY_PHONE CMPY_PHONE,COMPANY_FAX CMPY_FAX,")
        .append("COMPANY_CONTACTOR CMPY_CONTACTOR,COMPANY_PARENT CMPY_PCODE,COMPANY_PRIORITY CMPY_SORT,")
        .append("DESCRIPTION CMPY_DESC,COMPANY_LEVEL CMPY_LEVEL,DEL_FLAG+1 S_FLAG");
        param.set(Constant.PARAM_SELECT, sql.toString());
        param.set(Constant.PARAM_WHERE, "and COMPANY_PARENT=1 and DEL_FLAG=0");
        List<Bean> dataList = ServMgr.act(param).getDataList();
        sql = new StringBuilder("insert into SY_ORG_CMPY (");
        sql.append("CMPY_CODE,CMPY_NAME,CMPY_FULLNAME,CMPY_COUNTRY,CMPY_PROVINCE,CMPY_CITY,CMPY_ADDRESS,")
        .append("CMPY_POSTCODE,CMPY_PHONE,CMPY_FAX,CMPY_CONTACTOR,CMPY_PCODE,CMPY_SORT,CMPY_DESC,")
        .append("CMPY_LEVEL,S_FLAG) values (");
        sql.append("#CMPY_CODE#,#CMPY_NAME#,#CMPY_FULLNAME#,#CMPY_COUNTRY#,#CMPY_PROVINCE#,#CMPY_CITY#,")
        .append("#CMPY_ADDRESS#,#CMPY_POSTCODE#,#CMPY_PHONE#,#CMPY_FAX#,#CMPY_CONTACTOR#,#CMPY_PCODE#,")
        .append("#CMPY_SORT#,#CMPY_DESC#,#CMPY_LEVEL#,#S_FLAG#)");
        count += Transaction.getExecutor().executeBatchBean(sql.toString(), dataList);
        String cmpyCode = dataList.get(0).getStr("CMPY_CODE"); //取出父公司编码作为共同的公司编码
        Transaction.getExecutor().execute("delete from tbl_zotn_department where company_id not in" 
                + " (select company_id from tbl_zotn_company)");
        sql = new StringBuilder("");
        //初始化数据
        sql.append("update tbl_zotn_department set depart_parent=company_id where depart_parent in (select ")
           .append("depart_id from tbl_zotn_department where depart_iscmpy=1)");
        Transaction.getExecutor().execute(sql.toString());
        sql = new StringBuilder("");
        sql.append("update TBL_STAFF_INFO set depart_id=company_id ")
            .append(" where depart_id in (select depart_id from ")
            .append("tbl_zotn_department where depart_iscmpy=1)");
        Transaction.getExecutor().execute(sql.toString());
        sql = new StringBuilder("");
        sql.append("update tbl_zotn_department a set depart_id=company_id, (depart_name, depart_parent)")
            .append("=(select company_name,company_parent from tbl_zotn_company ")
            .append("where company_id=a.company_id) where depart_iscmpy=1");
        Transaction.getExecutor().execute(sql.toString());
        Transaction.getExecutor().execute("update tbl_zotn_department set company_id=" + cmpyCode 
                + " where company_id>1");
        Transaction.getExecutor().execute("update tbl_zotn_user set company_id=" + cmpyCode 
                + " where company_id>1");
        Transaction.getExecutor().execute("update TBL_STAFF_INFO set company_id=" + cmpyCode 
                + " where company_id>1");
        //导入部门数据
        sql = new StringBuilder("");
        sql.append("DEPART_ID DEPT_CODE,DEPART_FULLNAME DEPT_NAME,DEPART_PARENT DEPT_PCODE,")
            .append("DEPART_PRIORITY DEPT_SORT,DESCRIPTION DEPT_MEMO,DEPT_EMAIL DEPT_EMAIL,")
            .append("COMPANY_ID CMPY_CODE,decode(DEPART_ISCMPY, null, 1, 1, 2) DEPT_TYPE,")
            .append("DEL_FLAG+1 S_FLAG");
        param = new ParamBean("TBL_ZOTN_DEPARTMENT", "finds");
        param.set(Constant.PARAM_SELECT, sql.toString());
        param.set(Constant.PARAM_WHERE, "and COMPANY_ID>1");
        dataList = ServMgr.act(param).getDataList();
        sql = new StringBuilder("insert into SY_ORG_DEPT (");
        sql.append("DEPT_CODE,DEPT_NAME,DEPT_PCODE,DEPT_SORT,DEPT_MEMO,DEPT_EMAIL,")
            .append("DEPT_LEVEL,CMPY_CODE,S_FLAG, DEPT_TYPE) values (");
        sql.append("#DEPT_CODE#,#DEPT_NAME#,#DEPT_PCODE#,#DEPT_SORT#,#DEPT_MEMO#,#DEPT_EMAIL#,")
            .append("#DEPT_LEVEL#,#CMPY_CODE#,#S_FLAG#,#DEPT_TYPE#)");
        count += Transaction.getExecutor().executeBatchBean(sql.toString(), dataList);
        //导入用户数据
        param = new ParamBean("TBL_ZOTN_USER", "finds");
        sql = new StringBuilder("");
        sql.append("USER_ID USER_CODE,USER_LOGIN_NAME USER_LOGIN_NAME,COMPANY_ID CMPY_CODE,")
            .append("USER_NAME USER_NAME,DEPT_CODE DEPT_CODE,USER_OFFICE_PHONE USER_OFFICE_PHONE,")
            .append("USER_MOBILE USER_MOBILE,USER_ICQ USER_QQ,USER_EMAIL USER_EMAIL,")
            .append("USER_PASSWORD USER_PASSWORD, USER_PRIORITY USER_SORT,")
            .append("STAFF_SEX USER_SEX,STAFF_BIRTHDAY USER_BIRTHDAY,STAFF_NATION USER_NATION,")
            .append("STAFF_IDCARD USER_IDCARD,USER_LEVEL USER_POST_LEVEL,DEL_FLAG+1 S_FLAG");
        param.set(Constant.PARAM_SELECT, sql.toString());
        param.set(Constant.PARAM_WHERE, "and COMPANY_ID>1");
        dataList = ServMgr.act(param).getDataList();
        sql = new StringBuilder("insert into SY_ORG_USER (");
        sql.append("USER_CODE,USER_LOGIN_NAME,CMPY_CODE,USER_NAME,DEPT_CODE,USER_OFFICE_PHONE,")
            .append("USER_MOBILE,USER_QQ,USER_EMAIL,USER_PASSWORD,USER_SORT,")
            .append("USER_SEX,USER_BIRTHDAY,USER_NATION,")
            .append("USER_IDCARD,USER_POST_LEVEL,S_FLAG) values (");
        sql.append("#USER_CODE#,#USER_LOGIN_NAME#,#CMPY_CODE#,#USER_NAME#,#DEPT_CODE#,#USER_OFFICE_PHONE#,")
            .append("#USER_MOBILE#,#USER_QQ#,#USER_EMAIL#,#USER_PASSWORD#,#USER_SORT#,")
            .append("#USER_SEX#,#USER_BIRTHDAY#,#USER_NATION#,")
            .append("#USER_IDCARD#,#USER_POST_LEVEL#,#S_FLAG#)");
        count += Transaction.getExecutor().executeBatchBean(sql.toString(), dataList);
        //处理部门层级
        ParamBean dept = new ParamBean(ServMgr.SY_ORG_DEPT, ServMgr.ACT_SAVE)
                .setId(cmpyCode).set("DEPT_TYPE", 1).set("DEPT_PCODE", "")
                .set("S_MTIME", DateUtils.getDatetimeTS());
        ServMgr.act(dept);
        OutBean outBean = new OutBean();
        if (count > 0) {
            outBean.setOk(Context.getSyMsg("SY_SYNC_OK", String.valueOf(count)));
        } else {
            outBean.setError(Context.getSyMsg("SY_SYNC_ERROR"));
        }
        outBean.setCount(count);
        return outBean;
    }
    
    /**
     * 根据部门编码重建部门树
     * @param paramBean 部门编码
     * @return 执行结果
     */
    public OutBean rebuildDept(ParamBean paramBean) {
        if (paramBean.getId().length() == 0) {
            throw new TipException("no dept code error!");
        }
        ParamBean dept = new ParamBean(ServMgr.SY_ORG_DEPT, ServMgr.ACT_SAVE);
        dept.setId(paramBean.getId()).set("DEPT_TYPE", 1).set("DEPT_PCODE", "")
                .set("S_MTIME", DateUtils.getDatetimeTS());
        return ServMgr.act(dept);
    }
}
