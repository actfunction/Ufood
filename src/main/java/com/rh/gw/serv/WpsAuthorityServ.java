package com.rh.gw.serv;

import com.rh.core.base.Bean;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.base.db.Transaction;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

import java.util.List;

public class WpsAuthorityServ extends GwExtServ {

    public OutBean doGetAuthrity(ParamBean bean){
        String nid = bean.getStr("NI_ID");
        OutBean out = new OutBean();
        SqlExecutor executor = Transaction.getExecutor();
        String sql ="SELECT * FROM PLATFORM.SY_WFE_WPS_AUTHORITY WHERE PROC_CODE = (SELECT PROC_CODE FROM PLATFORM.SY_WFE_NODE_INST WHERE NI_ID = '" + nid + "') AND NODE_CODE = (SELECT NODE_CODE FROM PLATFORM.SY_WFE_NODE_INST WHERE NI_ID = '" + nid + "')";
        List<Bean> query = executor.query(sql);
        return  out.setData(query);
    }
}
