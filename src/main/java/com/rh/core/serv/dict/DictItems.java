package com.rh.core.serv.dict;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;

/**
 * 自定义的字典数据输出接口类
 * @author wanglong
 * 
 */
public interface DictItems {
    /**
     * 
     * @param paramBean 参数Bean
     * @return 字典数据
     */
    Bean getItems(ParamBean paramBean);
    
    /**
     * 获取字典ID
     * @param paramBean 参数Bean
     * @return 字典ID
     */
    String getDictId(ParamBean paramBean);
}
