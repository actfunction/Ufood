package com.rh.core.org.auth.login;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.EncryptUtils;

/**
 * 基于用户名密码的认证模块
 * 
 * @author wanglong
 *
 */
public class PasswordLoginModule extends AbstractLoginModule {
    private static final String PARAM_ID = "id";
    private static final String PARAM_LOGINNAME = "loginName";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_CMPYCODE = "cmpyCode";

    @Override
    public UserBean authenticate(Bean paramBean) {
        UserBean userBean;
        String cmpyCode;
        // 接受的参数：id,password 或者 loginName,password,cmpyCode
        String id = paramBean.getStr(PARAM_ID);
        String password = paramBean.getStr(PARAM_PASSWORD);
        if (id.length() > 0) { //id认证模式
            userBean = UserMgr.getUserByMobileOrMail(id);
            cmpyCode = userBean.getStr("CMPY_CODE");
        } else { //login和cmpy认证模式
            String loginName = paramBean.getStr(PARAM_LOGINNAME);
            cmpyCode = paramBean.getStr(PARAM_CMPYCODE);
            if (loginName.length() <= 0 || password.length() <= 0 || cmpyCode.length() <= 0) {
                throw new RuntimeException("认证参数错误，用户名：" + loginName + ";公司编码：" + cmpyCode 
                        + ";密码长度：" + password.length());
            }

            if (Context.getSyConf("SY_ORG_LOGIN_NAME_LOWERCASE", true)) {
                loginName = loginName.toLowerCase();
            }
            userBean = UserMgr.getUser(loginName, cmpyCode);
        }
        //验证公司信息
        Bean cmpy = DictMgr.getItem("SY_ORG_CMPY", cmpyCode);
        if (cmpy == null) {
            throw new TipException(Context.getSyMsg("SY_CMPY_NOT_FOUND", cmpyCode));
        } else if (cmpy.getInt("FLAG") != Constant.YES_INT) { //公司被禁用
            throw new TipException(Context.getSyMsg("SY_CMPY_FORBIDDEN", cmpyCode));
        }
        //加密后进行判断
        password = EncryptUtils.encrypt(password, 
                Context.getSyConf("SY_USER_PASSWORD_ENCRYPT", EncryptUtils.DES));
        if (!password.equals(userBean.getPassword())) {
            throw new RuntimeException("输入的密码错误。");
        }
        return userBean;
    }

}
