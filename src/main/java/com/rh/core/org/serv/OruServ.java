package com.rh.core.org.serv;

import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 处理组织机构、用户和角色公共业务的类
 * 
 * @author wanglong
 * 
 */
public abstract class OruServ extends CommonServ {
    
    /**
     * 更新数据
     * 
     * @param paramBean 传入的参数
     * @param outBean 传出的参数
     */
    protected abstract void update(ParamBean paramBean, OutBean outBean);

    /**
     * 保存组织机构后清楚缓存的操作
     * 
     * @param paramBean 传入的参数Bean
     * @param outBean 返回的Bean
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk()) {
            update(paramBean, outBean);
        }
    }
}
