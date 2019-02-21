package com.rh.core.comm.chart;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.CacheMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;

/**
 * 数据展示
 * @author wanglong
 */
public class DataViewServ extends CommonServ {
    /** 缓存键值 */
    public static final String CACHE_DATA_VIEW = "_CACHE_SY_COMM_DATA_VIEW";

    /**
     * 保存之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk()) { //清除缓存
            CacheMgr.getInstance().remove(outBean.getId(), CACHE_DATA_VIEW);
        }
    }
    
    /**
     * 根据图表编码获取图表定义信息，如果存在缓存直接从缓存中获取，不存在从数据库中获取并放入缓存
     * @param dvId 图表编码
     * @return 图表定义
     */
    public static Bean getDataViewDef(String dvId) {
        Bean def = (Bean) CacheMgr.getInstance().get(dvId, CACHE_DATA_VIEW);
        if (def == null) {
            def = ServDao.find(ServMgr.SY_COMM_DATA_VIEW, dvId, true);
            if (def == null) {
                throw new RuntimeException(Context.getSyMsg("SY_DATA_NOT_EXIST", dvId));
            } else {
                CacheMgr.getInstance().set(dvId, def, CACHE_DATA_VIEW);
            }
        }
        return def;
    }
}
