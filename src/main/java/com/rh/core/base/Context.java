package com.rh.core.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.rh.core.comm.ConfMgr;
import com.rh.core.comm.MenuServ;
import com.rh.core.org.UserBean;
import com.rh.core.org.UserStateBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.org.serv.OnlineUserMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.EncryptUtils;
import com.rh.core.util.RequestUtils;

/**
 * 系统的总体控制类，管理各种系统级变量。
 * 
 * @author wanglong
 * @version $Id$
 */
public class Context extends BaseContext {

    /**
     * 私有构建体方法
     */
    private Context() {
    }
    
    /** 用户Session对照 */
    private static Map<String, String> userSessionMap = new ConcurrentHashMap<String, String>();
    
    /** http URL 地址 */
    private static String httpUrl = null;

    /**
     * 获取系统提示信息 将指定参数替换表达式中的变量，并返回结果
     * 信息编码要求在DICT_ID为SY_COMM_MESSAGE的字段中添加，信息的内容支持变量替换{1} {2} {3}，
     * 调用时带上对应的参数。
     * 例如：SY_SERV_ID_ERROR:  无效的服务主键：{1}
     * Context.get("SY_SERV_ID_ERROR", servId);
     * 获取到的信息为：“无效的服务主键：SY_DDDD ”
     * 
     * @param msgCode 错误码
     * @param params 需要在表达式中替换的参数，使用时请确认参数次序是否符合表达式format
     * @return 表达式替换后生成的错误信息
     */
    public static String getSyMsg(String msgCode, Object... params) {
        String msg = DictMgr.getName("SY_COMM_MESSAGE", msgCode);
        if (msg.length() > 0) {
            msg = msg.replaceAll("\\{(\\d)\\}", "\\%$1\\$s");
            return String.format(msg, params);
        } else {
            return params.toString();
        }
    }
    
    /**
     * 获取系统配置信息
     * @param key 键值
     * @param def 缺省配置
     * @return 配置信息
     */
    public static String getSyConf(String key, String def) {
        return ConfMgr.getConf(key, def);
    }
    
    /**
     * 获取系统配置信息
     * @param key 键值
     * @param def 缺省配置
     * @return 配置信息
     */
    public static boolean getSyConf(String key, boolean def) {
        return ConfMgr.getConf(key, def);
    }
    
    /**
     * 获取系统配置信息
     * @param key 键值
     * @param def 缺省配置
     * @return 配置信息
     */
    public static int getSyConf(String key, int def) {
        return ConfMgr.getConf(key, def);
    }
    
    /**
     * 从线程或者session中获取用户信息
     * @param request 对象
     * @param token 用户令牌
     * @param device 用户设备
     * @return 用户信息
     */
    public static UserBean getUserBean(HttpServletRequest request, String token, String device) {
        UserBean userBean = null;
        if (request != null 
                && request.getAttribute(Constant.AUTH_NO_SESSION) != null) {
            return (UserBean) getThread(THREAD.USERBEAN);
        }
        
        if (request != null) { //从session中获取
            UserStateBean userState = (UserStateBean) RequestUtils.getSession(request, "USER_BEAN");
            if (userState != null && userState.isNotEmpty("USER_LAST_IP")) {
                userBean = setThreadUser(userState);
            } else { //判断是否启用token验证模式
                if (StringUtils.isNotEmpty(token) 
                        && StringUtils.isNotEmpty(device)) { // 远程设备方式：根据request头中的token和设备信息进行自动登录处理
                    userBean = Context.getUserBeanByToken(token, device);
                    if (userBean != null) { // 自动登录
                        Context.setOnlineUser(request, userBean);
                    }
                }
            }
        }
        return userBean;
    }
    
    /**
     * 从线程或者session中获取用户信息，如果都没有从header中获取token信息进行比对
     * @param request 对象
     * @return 用户信息
     */
    public static UserBean getUserBean(HttpServletRequest request) {
        return getUserBean(request, request.getHeader("X-XSRF-TOKEN"), request.getHeader("X-DEVICE-NAME"));
    }
    
    /**
     * 获取用户信息，先从线程获取，不存从session获取，都不存在就返回null。
     * @return 用户信息
     */
    public static UserBean getUserBean() {
        UserBean userBean = (UserBean) getThread(THREAD.USERBEAN);
        HttpServletRequest req = getRequest();
        if ((userBean == null) && (req != null)) {
            return getUserBean(req);
        }
        return userBean;
    }
    
    /**
     * 
     * @param userBean 指定用户对象
     * @return 指定用户是否是当前用户
     */
    public static boolean isCurrentUser(UserBean userBean) {
        if (userBean == null) {
            return false;
        }
        UserBean currUser = Context.getUserBean();

        if (userBean.getCode().equals(currUser.getCode())) {
            return true;
        }

        return false;
    }
    
    /**
     * 根据session编码获取在线用户信息
     * @param sessionId session编码
     * @return 用户信息
     */
    public static UserBean getUserBean(String sessionId) {
        UserBean userBean = null;
        UserStateBean userState = OnlineUserMgr.getOnlineUserState(sessionId);
        if (userState != null && userState.isNotEmpty("USER_LAST_IP")) {
                userBean = setThreadUser(userState);
        }
        return userBean;
    }
    
    /**
     * 远程设备模式根据用户令牌获取在线用户信息
     * @param token 用户令牌
     * @return 用户信息
     */
    public static UserBean getUserBeanByToken(String token, String uuid) {
        UserBean userBean = null;
        UserStateBean userState = UserMgr.getUserStateByToken(token);
        if (userState != null) {
            String userCode = userState.getStr("USER_CODE");
            if (uuid == null || uuid.length() == 0 || uuid.startsWith("@")) { //网页版不比对uuid，可以直接使用token
                userBean = UserMgr.getUser(userCode);
            } else { //手机版需要比对机器码，只有本机的token才可以自动登录
                String checkToken = EncryptUtils.encrypt(userCode + uuid, EncryptUtils.MD5);
                if (checkToken.equals(token)) {
                    userBean = UserMgr.getUser(userCode);
                }
            }
        }
        return userBean;
    }
    
    /**
     * 增加在线用户
     * @param userState userStateBean
     */
    public static void addOnlineUser(UserStateBean userState) {
        OnlineUserMgr.addOnlineUser(userState);
    }
    
    /**
     * 将当前用户相关信息设置到线程变量中使用，放置sessionID，userBean，cmpyCode
     * @param userState 用户状态信息
     * @return userBean信息
     */
    public static UserBean setThreadUser(UserStateBean userState) {
        String sessionId = userState.getId();
        addOnlineUser(userState);
        UserBean userBean = UserMgr.getUser(userState.getStr("USER_CODE"));
        setThread(THREAD.SESSIONID, sessionId);
        setThread(THREAD.USERBEAN, userBean); //放入线程供后续调用
        setThread(THREAD.CMPYCODE, userBean.getCmpyCode());
        return userBean;
    }
    
    /**
     * 清除线程变量中用户信息
     */
    public static void removeThreadUser() {
        removeThread(THREAD.SESSIONID);
        removeThread(THREAD.USERBEAN);
        removeThread(THREAD.CMPYCODE);
    }
    
    /**
     * 设置用户信息
     * @param userBean 用户信息
     */
    public static void setOnlineUser(UserBean userBean) {
        setOnlineUser(getRequest(), userBean);
    }

    /**
     * 设置用户信息，一般给JSP，用来设置自动登录
     * @param req request对象
     * @param userBean 用户信息
     */
    public static void setOnlineUser(HttpServletRequest req, UserBean userBean) {
        //设置线程变量
        setThread(THREAD.USERBEAN, userBean);
        setThread(THREAD.CMPYCODE, userBean.getCmpyCode());
        //设置session变量
        if (req != null) {
            String userCode = userBean.getCode();
            UserMgr.clearCacheVarMap(userCode); //清除以重新获取用户变量
            String ip = req.getRemoteAddr();
            String time = DateUtils.getDatetime();
            //获取用户状态信息
            Bean updataState = new Bean();
            UserStateBean userState = UserMgr.getUserState(userCode); //获取用户状态信息
            if (userState == null) {
                userState = new UserStateBean(userCode);
                userState.set("USER_CODE", userCode);
                updataState.set("USER_CODE", userCode);  //为新建作准备
                userState.set("USER_FIRST_LOGIN", true);  //首次登录标志
                userState.set("MODIFY_PASSWORD", 1);
            } else {
                updataState.setId(userCode); //为修改作准备
            }
            if (req.getHeader("X-XSRF-TOKEN") == null) { //非令牌模式为正式登录，进行登录数据处理
                userState.set("USER_LAST_IP", ip);
                updataState.set("USER_LAST_IP", ip);
                userState.set("USER_LAST_LOGIN", time);
                updataState.set("USER_LAST_LOGIN", time);
                String client = RequestUtils.get(req, "USER_LAST_CLIENT", "");
                String os = RequestUtils.get(req, "USER_LAST_OS", "");
                String pcName = RequestUtils.get(req, "USER_LAST_PCNAME", "");
                String browser = RequestUtils.get(req, "USER_LAST_BROWSER", "");
                userState.set("USER_LAST_CLIENT", client).set("USER_LAST_OS", os).set("USER_LAST_PCNAME", pcName)
                    .set("USER_LAST_BROWSER", browser);
                updataState.set("USER_LAST_CLIENT", client).set("USER_LAST_OS", os).set("USER_LAST_PCNAME", pcName)
                    .set("USER_LAST_BROWSER", browser);
                String device = req.getHeader("X-DEVICE-NAME");
                if ((device != null && !device.startsWith("@")) || userState.isEmpty("USER_TOKEN")) {
                    String token = EncryptUtils.encrypt(userCode + device, EncryptUtils.MD5);
                    userState.set("USER_TOKEN", token);
                    updataState.set("USER_TOKEN", token);
                    String sId = getUserSessionId(userCode);
                    if (sId != null) { //清除本用户其他登录的session
                        clearOnlineUser(sId);
                    }
                }
            }
            boolean bMenu;
            //如果没有启用缓存或者菜单时间为空，则生成菜单
            if (userState.isEmpty("MENU_TIME") || !Context.getSyConf("SY_COMM_MENU_CACHE", true)) { 
                userState.set("MENU_TIME", time); //设置菜单时间
                updataState.set("MENU_TIME", time);
                bMenu = true;
            } else {
                bMenu = false;
            }
            //更新用户状态信息
            UserMgr.saveUserState(updataState, false); //更新数据库用户状态
            RequestUtils.setSession(req, "USER_BEAN", userState); //设置session自动在线用户
            setThread(THREAD.SESSIONID, userState.getId());
            setUserSessionId(userCode, userState.getId()); //记录用户编码与session的对应关系
            if (bMenu) { //则生成菜单
                MenuServ.menuToFile(userCode);
            } else {
                //在集群环境下，菜单改变之后，不是所有节点都会去更新缓存，所以在登录的时候，去清除菜单缓存
                UserMgr.clearCacheMenuList(userCode);
            }
        }
    }
    
    /**
     * 获取指定用户的在线用户的状态信息，未登录不存在返回null
     * @return 在线用户状态
     */
    public static UserStateBean getOnlineUserState() {
        return getOnlineUserState(Context.getThreadStr(THREAD.SESSIONID));
    }
    /**
     * 获取指定用户的在线用户的状态信息，未登录不存在返回null
     * @param sessionId session编码
     * @return 在线用户状态
     */
    public static UserStateBean getOnlineUserState(String sessionId) {
        return OnlineUserMgr.getOnlineUserState(sessionId);
    }
    /**
     * 获取指定用户的在线用户的状态信息，未登录不存在返回null
     * @param userCode 用户编码
     * @return 在线用户状态
     */
    public static Bean getOnlineUserStateByUser(String userCode) {
        String sessionId = getUserSessionId(userCode);
        if (sessionId != null) {
            return getOnlineUserState(sessionId);
        } else {
            return null;
        }
    }
    /**
     * 根据sessionId清除在线用户
     * @param sessionId session编码
     */
    public static void clearOnlineUser(String sessionId) {
        OnlineUserMgr.clearOnlineUser(sessionId);
    }
    
    /**
     * 根据用户编码获取最后登录的sessionId信息
     * @param userCode 用户编码
     * @return 最后登录sessionId
     */
    public static String getUserSessionId(String userCode) {
        return userSessionMap.get(userCode);
    }
    
    /**
     * 用户是否在线
     * @param userCode - usercode
     * @return 1: 在线, 2:离线
     */
    public static int userOnline(String userCode) {
        return Context.getUserSessionId(userCode) == null ? Constant.NO_INT : Constant.YES_INT;
    }
    
    /**
     * 设定用户编码对应最后登录的sessionId信息
     * @param userCode 用户编码
     * @param sessionId 最后登录sessionId
     */
    public static void setUserSessionId(String userCode, String sessionId) {
        userSessionMap.put(userCode, sessionId);
    }
    
    
    /**
     * 设定用户编码对应最后登录的sessionId信息
     * @param userCode 用户编码
     */
    public static void clearSessionId(String userCode) {
        userSessionMap.remove(userCode);
    }
    
    /**
     * 变更公司编码，主要用户根据数据的公司字段的值去获取字典信息，而不是根据当前用户。
     * @param newCmpy 新指定的公司编码
     * @return 原先的公司编码
     */
    public static String changeCmpy(String newCmpy) {
        String oldCmpy = getThreadStr(THREAD.CMPYCODE);
        setThread(THREAD.CMPYCODE, newCmpy);
        return oldCmpy;
    }
    
    /**
     * 获取当前用户对应的公司编码。
     * @return 公司编码
     */
    public static String getCmpy() {
        return getThreadStr(THREAD.CMPYCODE);
    }
    
    /**
     * 是否运行在开发调试模式下，如果是开发调试模式：
     *  1. 卡片模版不再进行缓存，修改后自动生效，便于调试；
     *  2. JS等文件不进行压缩，便于调试；
     *  3. 服务权限不再判断，便于Junit和开发调试；
     * @return 是否开发模式
     */
    public static boolean isDebugMode() {
        return Context.appBoolean("DEBUG_MODE");
    }
    
    /**
     * 获取服务器的httpd地址，如果系统集群则返回系统配置中的配置地址，优先取系统配置地址，如果取不到则通过request获取
     * 系统配置键值为:SY_HTTP_URL
     * 得到对应的URL的全名称信息及虚路径，比如：http://www.sina.com.cn:81/rh
     * @return 本机地址
     */
    public static String getHttpUrl() {
        String url = getSyConf("SY_HTTP_URL", "");
        if (url.isEmpty()) {
            if (httpUrl == null) {
                HttpServletRequest request = getRequest();
                // 服务器地址
                httpUrl = request.getScheme() + "://" + request.getServerName() + ":" 
                        + request.getServerPort();
            }
            url = httpUrl;
        }
        String contextPath = Context.appStr(APP.CONTEXTPATH);
        if (contextPath.length() > 0) {
            url = url + contextPath;
        }
        return url;
    }
}
