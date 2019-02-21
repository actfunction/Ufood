package com.rh.core.serv.listener;

import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 服务监听配置
 * @author wanglong
 */
public class ServListenerServ extends CommonServ {
    /**
     * 重新初始化缓存
     * @param param 参数信息
     * @return 结构
     */
    public OutBean reload(ParamBean param) {
        OutBean outBean = new OutBean();
        ServLisMgr.getInstance().init();
        return outBean.setOk();
    }

}
