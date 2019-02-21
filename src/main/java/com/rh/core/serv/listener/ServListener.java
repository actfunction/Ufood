package com.rh.core.serv.listener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 服务监听
 * @author wanglong
 */
public class ServListener {
    private static final String INIT = "init";
    private static final String AFTER = "after";
    private static final String BEFORE = "before";
    /**
     * 监听类
     */
    private Class<?> cls;
    /**
     * 方法map
     */
    private Map<String, Method> methodMap = new HashMap<String, Method>();
    private String conf;
    private String serv;
    /**
     * 服务监听
     * @param serv 服务
     * @param lisCls 类名
     * @param conf 配置
     * @throws Exception 例外
     */
    public ServListener(String serv, String lisCls, String conf) throws Exception {
        this.cls = Class.forName(lisCls);
        this.serv = serv;
        this.conf = conf;
    }
    /**
     * 调用方法
     * @param at 时机
     * @param methodName 方法名称
     * @param classes 类
     * @param objects 对象
     * @throws Exception 例外
     */
    private void invoke(String at, String methodName, Class<?>[] classes, Object[] objects) throws Exception {
        Method method = null;
        if (!methodMap.containsKey(at)) { //监听before和after
            Class<?>[] classes2 = new Class<?>[classes.length + 1];
            classes2[0] = String.class;
            System.arraycopy(classes, 0, classes2, 1, classes.length);
            try {
                method = this.cls.getMethod(at, classes2);
            } catch (Exception e) {
                //do nothing
            }
            this.methodMap.put(at, method);
        } else {
            method = this.methodMap.get(at);
        }
        if (method != null) {
            Object[] objects2 = new Object[objects.length + 1];
            objects2[0] = methodName;
            System.arraycopy(objects, 0, objects2, 1, objects.length);
            method.invoke(newInstance(), objects2);
        } else {
            methodName = at + Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
            if (!methodMap.containsKey(methodName)) {
                try {
                    method = this.cls.getMethod(methodName, classes);
                } catch (Exception e) {
                    //do nothing
                }
                this.methodMap.put(methodName, method);
            } else {
                method = methodMap.get(methodName);
            }
            if (method != null) {
                method.invoke(newInstance(), objects);
            }
        }
    }
    
    /**
     * 实例化
     * @return 实例
     * @throws Exception 错误
     */
    private Object newInstance() throws Exception {
        Object instance = this.cls.newInstance();
        Method method = null;
        try {
            if (!methodMap.containsKey(INIT)) { //监听before和after
                method = this.cls.getMethod(INIT, String.class, String.class);
                this.methodMap.put(INIT, method);
            } else {
                method = this.methodMap.get(INIT);
            }
        } catch (Exception e) {
            //do nothing
        }
        if (method != null) {
            method.invoke(instance, new Object[] {this.serv, this.conf});
        }
        return instance;
    }
    /**
     * before监听
     * @param act 操作
     * @param param 参数bean
     * @throws Exception 例外
     */
    public void before(String act, ParamBean param) throws Exception {
        invoke(BEFORE, act, new Class[] {ParamBean.class}, 
                new Object[] {param});
    }
    /**
     * after监听
     * @param act 操作
     * @param param 参数
     * @param result 结果
     * @throws Exception 例外
     */
    public void after(String act, ParamBean param, OutBean result) throws Exception {
        invoke(AFTER, act, new Class[] {ParamBean.class, OutBean.class}, 
                new Object[] {param, result});
    }
}
