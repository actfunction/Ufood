package com.rh.core.wfe.util;

import com.rh.core.base.Bean;
import com.rh.core.wfe.WfAct;

/**
 * 
 * 线事件抽象类
 * @author wanglong
 * 
 */
public abstract class AbstractLineEvent {
    /**
     * 顺序送下一个节点时触发此事件
     * @param preWfAct 前一个节点的实例
     * @param nextWfAct 下一个节点的实例
     * @param lineDef 线定义Bean
     */
    public abstract void forward(WfAct preWfAct, WfAct nextWfAct, Bean lineDef);

    /**
     * 返回操作送下一个节点时触发此事件
     * @param preWfAct 前一个节点的实例
     * @param nextWfAct 下一个节点的实例
     * @param lineDef 线定义Bean
     */
    public abstract void backward(WfAct preWfAct, WfAct nextWfAct, Bean lineDef);
}
