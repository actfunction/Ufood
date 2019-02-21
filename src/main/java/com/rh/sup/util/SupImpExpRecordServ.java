package com.rh.sup.util;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

public class SupImpExpRecordServ extends CommonServ {
    public OutBean getIngState(ParamBean paramBean) {
        String dataId = paramBean.getStr("DATA_ID");

        long aTimeL = Long.parseLong(dataId);

        Bean bean = ImpExpRecordUtils.find(aTimeL);
        OutBean outBean = new OutBean();

        if (bean != null) {
            outBean.putAll(bean);
//            if (!bean.getStr("STATE").contains(ING)) {
//                TsObjectUtils.delete(servId, dataId);
//            }
        }
        return outBean;
    }
}
