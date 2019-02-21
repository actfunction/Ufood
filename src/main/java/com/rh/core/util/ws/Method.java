package com.rh.core.util.ws;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法
 * @author wanglong
 */
public class Method {
    private List<Parameter> params = new ArrayList<Parameter>();
    /**
     * 构造器
     * @param name 名称
     */
    public Method(String name) {
        this.name = name;
    }
    /**
     * 获取参数列表
     * @return 参数列表
     */
    public List<Parameter> getParams() {
        return params;
    }
    private String name;
    private Parameter paramReturn;
    /**
     * 获取名称
     * @return 名称
     */
    public String getName() {
        return name;
    }
    /**
     * 设置返回值参数
     * @param paramReturn 返回值参数
     */
    public void setReturn(Parameter paramReturn) {
        this.paramReturn = paramReturn;
    }
    /**
     * 获取返回值参数
     * @return 返回值参数
     */
    public Parameter getReturn() {
        return this.paramReturn;
    }
    
}
