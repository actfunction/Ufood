package com.rh.core.serv.dict;

import com.rh.core.base.Bean;

/**
 * 字典数据监听
 * @author wanglong
 */
public interface DictListener {
    /**
     * 构建字典列表或者字典树时，对单条数据进行预处理
     * @param item 字典数据项
     */
    void each(Bean item);
}
