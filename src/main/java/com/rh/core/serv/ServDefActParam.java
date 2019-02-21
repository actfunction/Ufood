package com.rh.core.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.util.ServUtils;

/**
 * 服务方法参数明细类
 * 
 * @author wanglong
 */
public class ServDefActParam extends CommonServ {

    /**
     * 修改参数清除服务定义
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && !paramBean.getLinkMode()) { //非级联才更新缓存
            if (outBean.isNotEmpty("ACT_ID")) {
                Bean data = ServDao.find(ServMgr.SY_SERV_ACT, outBean.getStr("ACT_ID"));
                ServUtils.udpateMtime(data.getStr("SERV_ID"), outBean.getStr("S_MTIME"));
            }
        }
    }

    /**
     * 删除服务操作后清除cache
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && !paramBean.getLinkMode()) { //非级联才更新缓存
            List<Bean> dataList = outBean.getDataList();
            if (dataList.size() > 0) {
                Bean dataBean = dataList.get(0);
                if (dataBean != null && !dataBean.isEmpty("ACT_ID")) {
                    Bean data = ServDao.find(ServMgr.SY_SERV_ACT, dataBean.getStr("ACT_ID"));
                    ServUtils.udpateMtime(data.getStr("SERV_ID"), null);
                }
            }
        }
    }
}
