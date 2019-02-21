package com.rh.core.serv.send;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.flow.FlowMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;

/**
 * 分发明细的公用类
 * 
 */
public class SendUtils {
    
    /**
     * 
     * @param sendBean 分发Bean
     */
    public static void updateSendRecord(Bean sendBean) {
        ServDao.update(ServMgr.SY_COMM_SEND_DETAIL, sendBean);
    }

    /**
     * @param sendBean 分发明细记录
     * 
     * @return 是否已经签收
     */
    public static boolean isRecved(Bean sendBean) {
        if (null != sendBean 
                && sendBean.getInt("SEND_STATUS") == SendConstant.SEND_STATUS_RECEIVED) {
            return true;
        }

        return false;
    }

    /**
     * @param sendId 分发ID
     * 
     * @return 是否已经签收
     */
    public static boolean isReturned(String sendId) {
        Bean sendBean = ServDao.find(ServMgr.SY_COMM_SEND_DETAIL, sendId);

        if (null != sendBean && sendBean.getInt("RETN_FLAG") == SendConstant.SEND_RTN_FLAG) {
            return true;
        }

        return false;
    }

    /**
     * 通过分发ID查询分发Bean
     * @param sendId 分发ID
     * @return 分发Bean
     */
    public static Bean getSendDetailById(String sendId) {
        Bean sendBean = new Bean();
        sendBean.setId(sendId);

        return ServDao.find(ServMgr.SY_COMM_SEND_DETAIL, sendBean);
    }
    
    /**
     * 
     * @param dataId 数据ID
     * @return 未签收的分发列表
     */
    public static List<Bean> getUnrecvedSendDetailList(String dataId) {
        if (StringUtils.isEmpty(dataId)) {
            return new ArrayList<Bean>();
        }

        Bean sendBean = new Bean();
        sendBean.set("DATA_ID", dataId);
        sendBean.set("SEND_STATUS", SendConstant.SEND_STATUS_SNEDING);
        return ServDao.finds(ServMgr.SY_COMM_SEND_DETAIL, sendBean);
    }
    
    /**
     * 
     * @param dataId 审批单ID
     * @return 接收人UserCode 集合
     */
    public static Set<String> getReceivedUserSet(String dataId) {
        Set<String> users = new HashSet<String>();
        SqlBean sql = new SqlBean();
        sql.and("DATA_ID", dataId);
        sql.andGTE("SEND_STATUS", SendConstant.SEND_STATUS_SNEDING);
        sql.selects("RECV_USER");
        List<Bean> list = ServDao.finds(ServMgr.SY_COMM_SEND_DETAIL, sql);
        for (Bean bean : list) {
            users.add(bean.getStr("RECV_USER"));
        }
        return users;
    }
    
    /**
     * 是否是分发记录的接收人
     * @param sendBean 分发明细对象
     * @param userId 指定用户ID
     * @return 如果分发记录的接收人是指定用户则返回true，否则返回false
     */
    public static boolean isReciver(Bean sendBean, String userId) {
        if (sendBean == null) {
            return false;
        }

        if (sendBean.getStr("RECV_USER").equals(userId)) {
            return true;
        }
        
        return false;
    }

    /**
     * 
     * @param sendBean 分发明细对象
     * @return 如果分发记录的接收人是指定用户则返回true，否则返回false
     */
    public static boolean isNotFinished(Bean sendBean) {
        if (sendBean == null) {
            return false;
        }

        if (sendBean.getInt("SEND_STATUS") <= SendConstant.SEND_STATUS_SNEDING) {
            return true;
        }

        return false;
    }

    /**
     * 
     * @param sendDetail 分发记录
     * @param sendId 分发ID
     */
    private static void finishFenfaTodo(Bean sendDetail, String sendId) {
        Bean queryBean = new Bean();
        queryBean.set("TODO_CODE", sendDetail.getStr("SERV_ID"));
        queryBean.set("TODO_OBJECT_ID1", sendDetail.getStr("DATA_ID"));
        queryBean.set("OWNER_CODE", sendDetail.getStr("RECV_USER"));
        queryBean.set("TODO_OBJECT_ID2", sendId);
        
        if (sendDetail.getStr("RECV_TYPE").equals(Constant.OUTSIDE)) { //分发给外单位，则结束所有的待办
            TodoUtils.endAllUserTodo(sendDetail.getStr("DATA_ID"), sendId);
        } else {
            TodoUtils.ends(queryBean);
        }
    }

    /**
     * 退回的分发，改其标志位
     * @param sendDtlBean 分发Bean
     * @param userBean 用户Bean
     */
    public static void tuihui(Bean sendDtlBean, UserBean userBean) {
        tuihuiAll(sendDtlBean.getStr("DATA_ID"), userBean);
    }
    
    /**
     * 退回所有的数据
     * @param dataId 审批单ID
     * @param userBean 办理用户对象
     */
    private static void tuihuiAll(String dataId, UserBean userBean) {
        List<Bean> sendList = findNotQianshouList(dataId, userBean);
        for (Bean sendBean : sendList) {
            sendBean.setId(sendBean.getId());
            sendBean.set("RETN_FLAG", SendConstant.SEND_RTN_FLAG).set("SEND_STATUS", SendConstant.SEND_STATUS_RETURNED);
            sendBean.set("RECV_TIME", DateUtils.getDatetime());
            ServDao.update(ServMgr.SY_COMM_SEND_DETAIL, sendBean);
            
            SendUtils.finishFenfaTodo(sendBean, sendBean.getId());
        }
    }

    /**
     * @param cmpyId 所属公司ID
     * @param servId Service ID，如公文
     * @param dataId 所属数据ID，如公文ID。
     * @return 是否存在分发记录
     */
    public static boolean isShowFenfaList(String cmpyId, String servId,
            String dataId) {
        UserBean userBean = Context.getUserBean();
        if (!userBean.getCmpyCode().equals(cmpyId)) {
            return false;
        }

        // 查分发表的count
        Bean queryBean = new Bean();
        queryBean.set("SERV_ID", servId);
        queryBean.set("DATA_ID", dataId);

        int sendNum = ServDao.count(ServMgr.SY_COMM_SEND_DETAIL, queryBean);

        if (sendNum == 0) {
            return false;
        }

        return true;
    }
    
    /**
     * 签收与指定分发记录关联的所有数据
     * @param sendId 分发记录数据ID
     */
    public static void qianShou(String sendId) {
        UserBean userBean = Context.getUserBean();
        Bean sendDtlBean = getSendDetailById(sendId);
        qianShou(sendDtlBean, userBean);
    }
    
    /**
     * 
     * @param sendDtlId 分发明细ID
     * @param recvUser 接收用户UserBean
     */
    public static void qianShou(String sendDtlId, UserBean recvUser) {
        Bean sendDtlBean = getSendDetailById(sendDtlId);
        qianShou(sendDtlBean, recvUser);
    }
    
    /**
     * 
     * @param sendDtlBean 分发明细对象
     * @param recvUser 接收用户UserBean
     */
    public static void qianShou(Bean sendDtlBean, UserBean recvUser) {
        qianShouAll(sendDtlBean.getStr("DATA_ID"), recvUser);
    }
    
    /**
     * 签收指定用户 + 指定审批单的所有分发记录
     * @param dataId 被分发的数据ID
     * @param recvUser 接收用户UserBean
     */
    public static void qianShouAll(String dataId, UserBean recvUser) {
        List<Bean> sendList = findNotQianshouList(dataId, recvUser);
        for (Bean sendBean : sendList) {
            qianShouSingle(sendBean);
        }
    }
    
    /**
     * 查询"审批单ID + 用户ID"对应的所有未签收的分发明细
     * @param dataId 审批单ID
     * @param userBean 用户Bean
     * @return 未签收的数据明细列表
     */
    private static List<Bean> findNotQianshouList(String dataId, UserBean userBean) {
        StringBuilder strWhere = new StringBuilder();
        strWhere.append(" and DATA_ID = '").append(dataId).append("'");
        strWhere.append(" and RECV_USER = '").append(userBean.getCode()).append("'");
        strWhere.append(" and SEND_STATUS = ").append(SendConstant.SEND_STATUS_SNEDING);
        Bean queryBean = new Bean().set(Constant.PARAM_WHERE, strWhere.toString());

        List<Bean> sendList = ServDao.finds(ServMgr.SY_COMM_SEND_DETAIL, queryBean);
        return sendList;
    }
    
    /**
     * 签收指定分发记录。不判断当前用户是否有权限签收。
     * @param sendDtlBean 分发明细记录数据
     */
    public static void qianShouSingle(Bean sendDtlBean) {
        qianShouSingle(sendDtlBean, true);
    }
    
    /**
     * 签收指定分发记录。不判断当前用户是否有权限签收。
     * @param sendDtlBean 分发明细记录数据
     * @param addFlow 是否增加流经信息
     */
    public static void qianShouSingle(Bean sendDtlBean, boolean addFlow) {
        if (sendDtlBean.getInt("SEND_STATUS") == SendConstant.SEND_STATUS_SNEDING) {
        	String sendId = sendDtlBean.getId();
            Bean updateBean = new Bean();
            updateBean.setId(sendId);
            updateBean.set("RECV_TIME", DateUtils.getDatetime()).set("SEND_STATUS", SendConstant.SEND_STATUS_RECEIVED);
            
            //设置实际签收人
            UserBean currUser = Context.getUserBean();
            updateBean.set("RECV_REAL_USER", currUser.getCode());
            updateBean.set("RECV_REAL_USER_NAME", currUser.getName());
            SendDetailServ.updateSend(updateBean);
            if (addFlow) {
                // 增加流经信息
                addFlowRecord(currUser, sendDtlBean);
            }
            // 存在节点编码
            if (sendDtlBean.isNotEmpty("NI_ID")) {
                SendUtils.finishReadNode(sendDtlBean.getStr("NI_ID"));
            }
            // 取消分发的待办
            SendUtils.finishFenfaTodo(sendDtlBean, sendId);
        }
    }
    
    /**
     * 将送阅的节点信息改为停止
     * @param dataId 分发（送阅）存储的节点主键
     */
    private static void finishReadNode(String dataId) {
		Bean nodeBean = ServDao.find(ServMgr.SY_WFE_NODE_INST, dataId);

		if (null != nodeBean) { //如果节点信息不为NULL
			String nodeETime = DateUtils.getDatetime();
			String nodeBTime = nodeBean.getStr("NODE_BTIME");
			long nodeDay = DateUtils.getDiffTime(nodeBTime, nodeETime);
			Bean updBean = new Bean();
			updBean.setId(dataId);
			updBean.set("DONE_TYPE", 20);
			updBean.set("DONE_DESC", "签收");
			updBean.set("NODE_DAYS", nodeDay/1000/60);
			updBean.set("OPEN_TIME", nodeETime);
			updBean.set("NODE_ETIME", nodeETime);
			ServDao.save(ServMgr.SY_WFE_NODE_INST, updBean);
		}
	}

	/**
     * 添加分发到流经记录
     * @param userBean 办理用户
     * @param sendDtlBean 分发实例Bean
     */
    private static void addFlowRecord(UserBean userBean, Bean sendDtlBean) {
        String docId = sendDtlBean.getStr("DATA_ID");
        
        FlowMgr.addUserFlow(docId, userBean); 
    }
}
