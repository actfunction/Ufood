package com.rh.core.comm.todo;


import com.rh.core.base.Bean;

/**
 * 待办数量获取类，适用于外部待办获取
 * @author wanglong
 */
public interface ITodoCount {
    /**
     * 获取待办数
     * @param param 参数
     * @return 待办数量，要求至少有TODO_COUNT
     */
    Bean getTodo(Bean param);

}
