package com.rh.core.org.auth.login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 登录方式工厂
 * 
 * @author wanglong
 * 
 */
public class LoginModuleFactory {

    private static final String LOGIN_MODULE_LDAP = "com.rh.core.org.auth.login.LdapLoginModule";
    private static final String LOGIN_MODULE_DEFAULT = "com.rh.core.org.auth.login.PasswordLoginModule";
    /** log. */
    private static Log log = LogFactory.getLog(LoginModuleFactory.class);

    /**
     * 获取认证模块对象
     * 
     * @param loginStyle 登录方式
     * @param loginModuleClass 认证模块类名
     * @return 认证对象
     */
    public static LoginModule getLoginModule(String loginStyle, String loginModuleClass) {
        try {
            if ("ldap".equals(loginStyle)) {
                return (LoginModule) Class.forName(LOGIN_MODULE_LDAP).newInstance();
            }
            if ("custom".equals(loginStyle)) {
                return (LoginModule) Class.forName(loginModuleClass).newInstance();
            }

            return (LoginModule) Class.forName(LOGIN_MODULE_DEFAULT).newInstance();
        } catch (Exception e) {
            log.error("无法找到登陆认证类。认证方式：" + loginStyle, e);
            throw new RuntimeException("无法找到登陆认证类。", e);
        }
    }

}
