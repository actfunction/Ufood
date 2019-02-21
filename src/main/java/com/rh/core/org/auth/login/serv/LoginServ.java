package com.rh.core.org.auth.login.serv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.org.auth.login.LoginModuleFactory;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.base.BaseServ;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.RequestUtils;
import com.rh.core.util.Strings;
import com.rh.core.util.var.ConfVar;
import com.rh.core.util.var.VarMgr;

/**
 * 登录服务类
 * 
 * @author wanglong
 * 
 */
public class LoginServ extends BaseServ {

    /**
     * 登录方法
     * 
     * @param bean 传入的参数
     * @return outBean
     */
    public OutBean login(ParamBean bean) {
        ParamBean paramBean = new ParamBean(bean);
        String servId = paramBean.getServId();
        ServDefBean servDef = ServUtils.getServDef(servId);
        checkActExpression(servDef, "login", paramBean);
        // 如果用户将SY_ORG_LOGIN_MODULE_NAME设置为“custom”，SY_ORG_LOGIN_MODULE_CLASS配置会生效
        String loginModuleName = Context.getSyConf("SY_ORG_LOGIN_MODULE_NAME", "");
        String loginModuleClass = Context.getSyConf("SY_ORG_LOGIN_MODULE_CLASS", "");

        OutBean outBean = LoginModuleFactory.getLoginModule(loginModuleName, loginModuleClass).login(paramBean);
        if (bean.getBoolean("ORG_VARS")) { //根据客户端请求返回组织参数
            outBean.set("ORG_VARS", VarMgr.getOrgMap());  //用户属性信息
            outBean.set("USER_ICON", Context.getUserBean().getImgB64()); //用户头像Base64格式
        }
        if (bean.contains("CONF_VARS")) { //根据客户端请求返回指定的配置参数
            String confParam = bean.getStr("CONF_VARS");
            if (confParam.equals("*")) { //全部配置参数给前端
                outBean.set("CONF_VARS", ConfVar.getInst().getMap(true));
            } else { //将指定的配置参数给前端
                String[] vars = confParam.split(Constant.SEPARATOR);
                Bean varBean = new Bean();
                Map<String, String> confMap = ConfVar.getInst().getMap(true);
                for (String var : vars) {
                    String value = confMap.get(var);
                    if (value != null) {
                        varBean.set(var, value);
                    }
                }
                outBean.set("CONF_VARS", varBean);
            }
        }
        outBean.setOk();
        return outBean;
    }
    
    /**
     * 判断用户是否已经登录
     * 
     * @param bean 传入的参数
     * @return outBean
     */
    public OutBean isLogin(ParamBean bean) {
        String sessionId = bean.getStr("SID");
        OutBean outBean = new OutBean();
        UserBean userBean = Context.getUserBean(sessionId);
        if (userBean != null) {
            outBean.setOk();
        } else {
            outBean.setError();
        }
        return outBean;
    }

    /**
     * 注销方法
     * 
     * @param paramBean 传入的参数
     * @return outBean
     */
    public OutBean logout(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        if(Context.getUserBean() != null) {
        	outBean.set("USER_CODE", Context.getUserBean().getCode());
            outBean.set("USER_TOKEN", Context.getUserBean().getToken());
            
            RequestUtils.removeSession(Context.getRequest()); // 清除session，自动处理在线信息
        }
        outBean.setOk();
        return outBean;
    }

    /**
     * webservice退出方法
     * 
     * @param paramBean 传入的参数
     * @return outBean
     */
    public OutBean wsLogout(ParamBean paramBean) {
        String sId = paramBean.getStr("SID");
        if (sId == null || sId.length() == 0) {
            throw new RuntimeException("SID不能为空！");
        }
        OutBean outBean = new OutBean();
        Context.clearOnlineUser(sId);
        RequestUtils.removeSession(Context.getRequest());
        return outBean;
    }
    
    /**
     * 在工作兼岗组内切换用户
     * @param paramBean 传入的参数，要求提供TO_USER_CODE参数
     * @return outBean
     */
    public OutBean changeUser(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        HttpServletRequest req = Context.getRequest();
        String toUserCode = paramBean.getStr("TO_USER_CODE");
        Bean userState = Context.getOnlineUserState();
        if (Strings.containsValue(userState.getStr("JIAN_CODES"), toUserCode)) { // 切换的用户在兼岗组内
            RequestUtils.removeSession(req); // 清除session，自动处理在线信息
            Context.setOnlineUser(req, UserMgr.getUser(toUserCode));
            outBean.setOk();
        } else {
            outBean.setError();
        }
        return outBean;
    }

    /**
     * 获取当前用户的兼岗用户列表，包含自身
     * @param paramBean 传入的参数
     * @return outBean
     */
    public OutBean getJianUsers(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        Bean userState = Context.getOnlineUserState();
        String[] jianCodes = userState.getStr("JIAN_CODES").split(Constant.SEPARATOR);
        UserBean currUser = Context.getUserBean();
        List<Bean> userList = new ArrayList<Bean>();
        for (String jianCode : jianCodes) {
            UserBean user = null;
            try {
                 user = UserMgr.getUser(jianCode);
            } catch (Exception e) {
                continue;
            }
            
            if (user.getCode().equals(currUser.getCode())) { // 如果为当前用户则忽略
                continue;
            }
            
            Bean jianBean = new Bean(user.getCode());
            jianBean.set("USER_CODE", user.getCode()).set("DEPT_NAME", user.getDeptName())
                    .set("TDEPT_NAME", user.getTDeptName())
                    .set("USER_NAME", user.getName()).set("TODO_COUNT", 
                            TodoUtils.getToDoCount(jianCode, paramBean.getStr("I_MENU_IDS")));
            jianBean.set("ODEPT_NAME", user.getODeptName());
            userList.add(jianBean);
        }
        outBean.setData(userList);
        return outBean;
    }
}
