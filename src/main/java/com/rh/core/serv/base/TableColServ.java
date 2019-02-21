package com.rh.core.serv.base;

import com.rh.core.base.Context;
import com.rh.core.base.BaseContext.DB_TYPE;
import com.rh.core.base.BaseContext.DS;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ParamBean;

/**
 * 数据表字段服务类。
 * 
 * @author wanglong
 * 
 */
public class TableColServ extends CommonServ {
        
    @Override
    protected void beforeFinds(ParamBean paramBean) {
        beforeDBType(paramBean);
    }
    
    @Override
    protected void beforeQuery(ParamBean paramBean) {
        beforeDBType(paramBean);
    }
    
    /**
     * 根据数据库类型重载查询处理
     * @param paramBean 参数信息
     */
    private void beforeDBType(ParamBean paramBean) {
        if (Transaction.getDBType().equals(DB_TYPE.MYSQL) || Transaction.getDBType().equals(DB_TYPE.H2)) {
            paramBean.setTable("information_schema.columns")
                .setSelect("TABLE_NAME, COLUMN_NAME, CHARACTER_MAXIMUM_LENGTH DATA_LENGTH, "
                        + "NUMERIC_PRECISION DATA_PRECISION, NUMERIC_SCALE DATA_SCALE, "
                        + "IS_NULLABLE NULLABLE, COLUMN_COMMENT COMMENTS, ORDINAL_POSITION COLUMN_ID")
                .setWhere("and table_schema='" + Context.getDSBean().getStr(DS.SCHEMA) + "'");
        }
    }
}
