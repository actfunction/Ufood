package com.rh.core.org.auth.login;

import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

/**
 * 登陆模块接口
 * 
 * @author wanglong
 * 
 */
public interface LoginModule {

    /**
     * 用户身份认证接口
     * 
     * @param paramBean 传入的参数
     * @return 认证结果
     */
    OutBean login(ParamBean paramBean);
}
