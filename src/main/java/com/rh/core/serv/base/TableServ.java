package com.rh.core.serv.base;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.DB_TYPE;
import com.rh.core.base.BaseContext.DS;
import com.rh.core.base.db.TableBean;
import com.rh.core.base.db.Transaction;
import com.rh.core.plug.IXdoc;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;
import com.rh.core.util.RequestUtils;

/**
 * 数据表及字段服务类，负责表的增删改查及字典的增加删除和修改。
 * 
 * @author wanglong
 * 
 */
public class TableServ extends CommonServ {
    
    /**
     * 删除表
     * @param paramBean 参数
     * @return 是否成功
     */
    public OutBean delete(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        String[] ids = paramBean.getId().split(Constant.SEPARATOR);
        int count = 0;
        for (String id : ids) {
            log.info("SYSTEM ACTION:  drop table " + id);
            if (id.endsWith("_V")) {
                Transaction.getExecutor().execute("drop table " + paramBean.getId() + " CASCADE");
            } else {
                Transaction.getExecutor().execute("drop view " + paramBean.getId());
            }
            count++;
        }
        if (count > 0) {
            outBean.setOk();
        }
        return outBean;
    }
    
    /**
     * 生成指定表或视图的DDL，不指定则生成全部的DDL
     * @param bean 参数信息
     * @return 结果信息
     */
    public OutBean expDDL(Bean bean) {
        ParamBean paramBean = new ParamBean(bean);
        OutBean outBean = new OutBean();
        String servId = paramBean.getServId();
        ServDefBean servDef = ServUtils.getServDef(servId);
        HttpServletResponse res = Context.getResponse();
        HttpServletRequest req = Context.getRequest();
        if (res != null) {
            res.setContentType("application/x-msdownload; charset=gbk");
            RequestUtils.setDownFileName(req, res, "TABLE_DDL.txt");
            try {
                PrintWriter out = res.getWriter();
                StringBuilder sb = new StringBuilder();
                if (paramBean.getId().length() > 0) {
                    String[] ids = paramBean.getId().split(Constant.SEPARATOR);
                    for (String id : ids) {
                        TableBean tableBean = Transaction.getExecutor().getDBTable(id);
                        String dbType = paramBean.getStr("DB_TYPE");
                        if (dbType.isEmpty()) {
                            sb.append(Transaction.getBuilder().getDBTableDDL(tableBean));
                        } else {
                            sb.append(Context.getBuilderByType(dbType).getDBTableDDL(tableBean));
                        }
                    }
                } else {
                    paramBean.set(Constant.PARAM_WHERE, servDef.getServDefWhere());
                    List<Bean> tblList = finds(paramBean).getList(Constant.RTN_DATA);
                    for (Bean tbl : tblList) {
                        TableBean tableBean = Transaction.getExecutor().getDBTable(tbl.getStr("TABLE_NAME"));
                        String dbType = paramBean.getStr("DB_TYPE");
                        if (dbType.isEmpty()) {
                            sb.append(Transaction.getBuilder().getDBTableDDL(tableBean));
                        } else {
                            sb.append(Context.getBuilderByType(dbType).getDBTableDDL(tableBean));
                        }
                    }
                }
                out.write(sb.toString());
                out.flush();
                out.close();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return outBean;
    }
    
    /**
     * 获取Xdoc格式化之后的文件流
     * @param paramBean 参数集合
     */
    public void getOutputXdocFile(ParamBean paramBean) {
        Bean query = new Bean();
        List<Bean> beanList = null;
        String ids = paramBean.getStr("ids");
        String sql = "";
        if (ids.isEmpty()) {
            sql = " and TABLE_NAME not like 'BIN$%' and TABLE_TYPE = 'TABLE'";
        } else {
            sql = " and TABLE_NAME in (" + ids + ")";
        }
        query.set(Constant.PARAM_WHERE, sql).set(Constant.PARAM_LINK_FLAG, true);
        beanList = ServDao.finds(paramBean.getServId(), query);
        paramBean.set("data", beanList);
        IXdoc xdoc = (IXdoc) Lang.createObject(IXdoc.class, 
                Context.getInitConfig("rh.xdoc", "com.rh.opt.plug.xdoc.XdocOutput"));
        xdoc.outputXdoc(paramBean);
    }
    
    /**
     * 生成非视图模式对应的字典信息
     * @param paramBean 参数信息
     * @return 生成结果是否成功
     */
    public OutBean noView(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        List<Bean> viewList = Transaction.getExecutor().getViewList();
        String dictId = "SY_SERV_NO_VIEW_" + Transaction.getDBType().toString();
        ParamBean param = new ParamBean(ServMgr.SY_SERV_DICT_ITEM, ServMgr.ACT_DELETE);
        param.setWhere("and DICT_ID='" + dictId + "'");
        ServMgr.act(param);
        int i = 1;
        List<Bean> itemList = new ArrayList<Bean>(viewList.size());
        for (Bean view : viewList) {
            Bean item = new Bean();
            String viewName = view.getStr("VIEW_NAME");
            item.set("DICT_ID", dictId).set("ITEM_CODE", viewName).set("ITEM_NAME", viewName)
                .set("ITEM_FLAG", Constant.YES_INT).set("ITEM_FIELD1", view.getStr("VIEW_TEXT")).set("ITEM_ORDER", i);
            itemList.add(item);
            i++;
        }
        param = new ParamBean(ServMgr.SY_SERV_DICT_ITEM, ServMgr.ACT_BATCHSAVE);
        param.setBatchSaveDatas(itemList);
        ServMgr.act(param);
        return outBean.setOk();
    }
    
    @Override
    protected void beforeFinds(ParamBean paramBean) {
        beforeDBType(paramBean);
    }
    
    @Override
    protected void beforeQuery(ParamBean paramBean) {
        beforeDBType(paramBean);
    }
    
    @Override
    protected void beforeByid(ParamBean paramBean) {
        if (Transaction.getDBType().equals(DB_TYPE.MYSQL) || Transaction.getDBType().equals(DB_TYPE.H2)) {
            paramBean.setTable("information_schema.tables")
                .setSelect("TABLE_NAME, TABLE_TYPE, TABLE_COMMENT COMMENTS")
                .setWhere("and TABLE_SCHEMA='" + Context.getDSBean().getStr(DS.SCHEMA) + "' and TABLE_NAME='" 
                        + paramBean.getId() + "'");
        }
    }
    
    /**
     * 根据数据库类型重载查询处理
     * @param paramBean 参数信息
     */
    private void beforeDBType(ParamBean paramBean) {
        if (Transaction.getDBType().equals(DB_TYPE.MYSQL) || Transaction.getDBType().equals(DB_TYPE.H2)) {
            paramBean.setTable("information_schema.tables")
                .setSelect("TABLE_NAME, TABLE_TYPE, TABLE_COMMENT COMMENTS")
                .setWhere("and table_schema='" + Context.getDSBean().getStr(DS.SCHEMA) + "'");
        }
    }
}
