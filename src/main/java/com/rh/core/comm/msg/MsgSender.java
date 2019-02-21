package com.rh.core.comm.msg;


import com.rh.core.base.Bean;

/**
 * 
 * 消息发送器接口
 * @author wanglong
 * 
 */
public interface MsgSender {
    /**
     * 
     * 消息载体对象中需要保护的数据项
     * 
     */
    public static class MsgItem {
        /** 发送用户 **/
        public static final String SEND_USER = ("SEND_USER");
        /** 标题 **/
        public static final String REM_TITLE = ("REM_TITLE");
        /** 内容 **/
        public static final String REM_CONTENT = ("REM_CONTENT");
        /** 紧急程度 **/
        public static final String S_EMERGENCY = ("S_EMERGENCY");
        /** url:常用于待办 **/
        public static final String REM_URL = ("REM_URL");
        /** 服务ID **/
        public static final String SERV_ID = ("SERV_ID");
        /** 数据ID **/
        public static final String DATA_ID = ("DATA_ID");
        /** 外系统访问URL **/
        public static final String REMOTE_URL = ("REMOTE_URL");
        /** 接收人UserBean列表 **/
        public static final String RECEIVER_LIST = ("RECEIVER_LIST");     
    }

    /**
     * 发送消息
     * @param msgBean 消息数据载体
     */
    void send(Bean msgBean);
    
    /**
     * 取得发送状态，是否给指定用户成功发送提醒。
     * @param userCode 用户Code
     * @return 是否成功发送
     */
//    boolean getStatus(String userCode);
    
    /**
     * 取得执行结果日志
     * @param userCode 用户Code
     * @return 执行结果日志
     */
    MsgResultBean getExecResult(String userCode);
}
