package com.rh.core.comm.entity;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;

/**
 * 
 * 分发签收情况查询
 * @author wanglong
 *
 */
public class EntitySendQueryServ extends CommonServ {

    @Override
    protected void beforeQuery(ParamBean paramBean) {
        String servId = paramBean.getServId();
        ServDefBean defBean = ServUtils.getServDef(servId);
        StringBuilder select = new StringBuilder(" ");
        select.append(getListSelectFields(defBean));
        select.append(", '2' as RECV_STAT");
//        select.append(", (")
//            .append("SELECT COUNT (SEND_ID) FROM SY_COMM_SEND_DETAIL ")
//            .append("WHERE DATA_ID = T.DATA_ID AND SEND_STATUS = 2 AND ")
//            .append("SEND_TIME = t.SEND_TIME) as RECV_STAT ");
        
        paramBean.setSelect(select.toString());
        
        paramBean.setGroupBy(" ENTITY_ID ");
    }
    
    /**
     * @param defBean 服务定义
     * @return 服务中定义的表字段和视图字段组成的用于列表查询的Select 语句字符串
     */
    private String getListSelectFields(ServDefBean defBean) {
        StringBuilder select = new StringBuilder();
        List<Bean> tableFields = defBean.getViewItems();
        int fieldCount = tableFields.size();
        for (int i = 0; i < fieldCount; i++) { // 获取全部字段数据
            Bean itemBean = tableFields.get(i);
            if (itemBean.getInt("ITEM_LIST_FLAG") != Constant.NO_INT) { //除不显示的字段之外都要放到本地
                if ("ENTITY_ID".equalsIgnoreCase(itemBean.getStr("ITEM_CODE"))) {
                    select.append(itemBean.get("ITEM_CODE")).append(",");
                } else {
                    select.append("max(");
                    select.append(itemBean.get("ITEM_CODE"));
                    select.append(")");
                    select.append(" as ").append(itemBean.get("ITEM_CODE"));
                    select.append(",");
                }
            }
        }

        if (select.length() > 0) {
            select.setLength(select.length() - 1);
            return select.toString();
        }

        return "";
    }

    @Override
    protected void afterQuery(ParamBean paramBean, OutBean outBean) {
        super.afterQuery(paramBean, outBean);
        StringBuilder select = new StringBuilder();

        select.append("SELECT COUNT (SEND_ID) AS \"_COUNT_\" FROM SY_COMM_SEND_DETAIL ")
                .append("WHERE DATA_ID = ?")
                .append(" AND SEND_STATUS = 2 ");
        List<Bean> list = outBean.getDataList();
        for (Bean bean : list) {
            ArrayList<Object> values = new ArrayList<Object>();
            values.add(bean.getStr("DATA_ID"));
            Bean result = Context.getExecutor().queryOne(select.toString(), values);
            bean.set("RECV_STAT", result.getInt("_COUNT_"));
        }
    }
    
    
}
