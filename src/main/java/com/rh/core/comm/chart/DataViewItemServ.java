package com.rh.core.comm.chart;

import com.rh.core.comm.CacheMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 数据项展示处理
 * @author wanglong
 */
public class DataViewItemServ extends CommonServ {

    @Override
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && !paramBean.getLinkMode()) { //清除父缓存
            CacheMgr.getInstance().remove(outBean.getStr("DV_ID"), DataViewServ.CACHE_DATA_VIEW);
        }
    }
    
    @Override
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOkOrWarn() && !paramBean.getLinkMode()) { //清除父缓存
            CacheMgr.getInstance().remove(outBean.getDataList().get(0).getStr("DV_ID"), DataViewServ.CACHE_DATA_VIEW);
        }
    }
    
}
