package com.rh.core.comm;

import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.msg.MsgCenter;

/**
 * 消息监听服务
 * @author wanglong
 */
public class MsgLisServ extends CommonServ {
    /**
     * 重新初始化缓存
     * @param param 参数信息
     * @return 结构
     */
    public OutBean reload(ParamBean param) {
        OutBean outBean = new OutBean();
        MsgCenter.getInstance().init();
        return outBean.setOk();
    }
}
