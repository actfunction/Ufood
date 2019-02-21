package com.rh.core.util.ws;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.serv.util.ServConstant;

/**
 * 服务
 * @author wanglong
 */
public class Service {
    /**
     * 服务
     * @param name 名称
     */
    public Service(String name) {
        this.name = name;
    }
    /**
     * 名称
     */
    private String name;
    /**
     * 方法列表
     */
    private List<Method> methods = new ArrayList<Method>();
    /**
     * 获取方法列表
     * @return 方法列表
     */
    public List<Method> getMethods() {
        return methods;
    }
    /**
     * 名称
     * @return 名称
     */
    public String getName() {
        return name;
    }
    /**
     * 设置名称
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * 获取方法
     * @param name 名称
     * @return 方法
     */
    public Method getMethod(String name) {
        for (Method method : this.methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
    /**
     * Service要求权限，默认服务权限
     */
    private int authFlag = ServConstant.AUTH_FLAG_SERV;
    /**
     * 设置权限
     * @param authFlag 权限值
     */
    public void setAuthFlag(int authFlag) {
        this.authFlag = authFlag;
    }
    /**
     * 获取权限
     * @return 返回权限值
     */
    public int getAuthFlag() {
        return authFlag;
    }
}
