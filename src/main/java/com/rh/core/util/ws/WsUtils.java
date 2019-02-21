package com.rh.core.util.ws;

/**
 * WebService工具类
 * @author wanglong
 */
public class WsUtils {
    /**
     * 是否登录系统服务
     * @param service 服务
     * @param method 方法
     * @return 是否登录系统服务
     */
    public static boolean isLogin(String service, String method) {
        return service.equals("SY_ORG_LOGIN") && method.equals("login");
    }
    /**
     * 是否登录系统服务的登出方法
     * @param service 服务
     * @param method 方法
     * @return 是否登录系统服务
     */
    public static boolean isLogout(String service, String method) {
        return service.equals("SY_ORG_LOGIN") 
                && (method.equals("logout") || method.equals("wsLogout"));
    }
    /**
     * 是否获取登录配置信息服务
     * @param service 服务
     * @param method 方法
     * @return 是否获取登录配置信息服务
     */
    public static boolean isLoginConf(String service, String method) {
        return service.equals("SY_ORG_LOGIN") && method.equals("conf");
    }
}
