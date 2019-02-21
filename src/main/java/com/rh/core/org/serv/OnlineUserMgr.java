package com.rh.core.org.serv;

import java.util.List;

import org.apache.log4j.Logger;

import com.rh.core.base.Context;
import com.rh.core.comm.CacheMgr;
import com.rh.core.org.UserStateBean;
import com.rh.core.org.mgr.UserMgr;

/**
 * 
 * @author wanglong
 * 
 */
public class OnlineUserMgr {
    /**
     * log4j 记录日志的类
     */
    private static Logger log = Logger.getLogger(Context.class);

    /** 缓存名称 **/
    public static final String ONLINE_USER = "ONLINE_USER";

    /**
     * 增加在线用户
     * @param userState userStateBean
     */
    public static void addOnlineUser(UserStateBean userState) {
        final String sessionId = userState.getId();
        UserStateBean usb = (UserStateBean) CacheMgr.getInstance().get(sessionId, ONLINE_USER);
        if (usb == null) { // 如果内存无在线用户则放置一份（适用session存储文件的情况）
            log.info("addOnlineUser:" + sessionId + "," + userState.getUserCode()
                    + "," + userState.getLastIp());
            CacheMgr.getInstance().set(sessionId, userState, ONLINE_USER);
        }
    }

    /**
     * 根据sessionId清除在线用户
     * @param sessionId session编码
     */
    public static void clearOnlineUser(String sessionId) {
        UserStateBean userState = getOnlineUserState(sessionId);

        if (userState != null) {
            UserMgr.clearCacheMenuList(userState.getStr("USER_CODE")); // 清除用户对应菜单缓存
            UserMgr.clearCacheVarMap(userState.getStr("USER_CODE")); // 清除用户对应变量缓存
            Context.clearSessionId(userState.getStr("USER_CODE"));
            userState.set("USER_LAST_IP", null);  //清除IP确保session验证IP处理
            CacheMgr.getInstance().remove(sessionId, ONLINE_USER);
            // 清除在线用户
            log.info("clearOnlineUser:" + sessionId + "," + userState.getStr("USER_CODE"));
        }
    }

    /**
     * 获取指定用户的在线用户的状态信息，未登录不存在返回null
     * @param sessionId session编码
     * @return 在线用户状态
     */
    public static UserStateBean getOnlineUserState(String sessionId) {
        UserStateBean usb = (UserStateBean) CacheMgr.getInstance().get(sessionId, ONLINE_USER);
        return usb;
    }

    /**
     * 
     * @return 取得在线用户数量
     */
    public static int getSize() {
        return CacheMgr.getInstance().getSize(ONLINE_USER);
    }
    
    /**
     * 
     * @param offset 从那个位置开始取，从0开始
     * @param size 取多少条记录
     * @return 从指定缓存中取得数据列表
     */
    @SuppressWarnings("unchecked")
    public static List<UserStateBean> getOnlineUserList(int offset, int size) {
        List<UserStateBean> list = CacheMgr.getInstance().getDataList(ONLINE_USER, offset, size);
        return list;
    }
}
