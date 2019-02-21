package com.rh.core.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.util.ServUtils;

/**
 * 服务关联定义明细类
 * 
 * @author wanglong
 */
public class ServDefLinkItem extends CommonServ {
    /** 服务主键 */
    public static final String SERV_ID_SERV_LINK_ITEM = "SY_SERV_LINK_ITEM";
    /**
     * 修改关联明细后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && !paramBean.getLinkMode()) { //非级联才更新缓存
            if (!outBean.isEmpty("LINK_ID")) {
                Bean data = ServDao.find(ServDefLink.SERV_ID_SERV_LINK, outBean.getStr("LINK_ID"));
                ServUtils.udpateMtime(data.getStr("SERV_ID"), outBean.getStr("S_MTIME")); 
            }
        }
    }

    /**
     * 删除关联明细后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && !paramBean.getLinkMode()) { //非级联才更新缓存
            List<Bean> dataList = outBean.getDataList();
            if (dataList.size() > 0) {
                Bean dataBean = dataList.get(0);
                if (dataBean != null && !dataBean.isEmpty("LINK_ID")) {
                    Bean data = ServDao.find(ServDefLink.SERV_ID_SERV_LINK, dataBean.getStr("LINK_ID"));
                    ServUtils.udpateMtime(data.getStr("SERV_ID"), null);
                }
            }
        }
    }
}
