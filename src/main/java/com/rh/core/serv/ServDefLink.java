package com.rh.core.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;

/**
 * 服务关联定义类
 * 
 * @author wanglong
 */
public class ServDefLink extends CommonServ {
    /** 服务主键 */
    public static final String SERV_ID_SERV_LINK = "SY_SERV_LINK";
    /**
     * 修改服务定义后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && !paramBean.getLinkMode()) { //非级联添加才更新缓存
            if (!outBean.isEmpty("SERV_ID")) {
                ServUtils.udpateMtime(outBean.getStr("SERV_ID"), outBean.getStr("S_MTIME"));
            }
        }
    }

    /**
     * 删除服务项定义后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        if (!paramBean.getBoolean(Constant.IS_LINK_ACT)) { //非级联删除模式才更新json文件
            List<Bean> dataList = outBean.getDataList();
            if (dataList.size() > 0) {
                Bean dataBean = dataList.get(0);
                if (dataBean != null) {
                    ServUtils.udpateMtime(dataBean.getStr("SERV_ID"), null);
                }
            }
        }
    }
}
