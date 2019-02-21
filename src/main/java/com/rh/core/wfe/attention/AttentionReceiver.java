package com.rh.core.wfe.attention;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.comm.remind.RemindMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.msg.Msg;
import com.rh.core.util.msg.MsgListener;

/**
 * 关注的监听类
 * @author wanglong
 *
 */
public class AttentionReceiver implements MsgListener {

    /** log */
    private static Log log = LogFactory.getLog(AttentionReceiver.class);
    
    /**
     * 构建体方法
     */
    public AttentionReceiver() {
        log.info("AttentionReceiver start...");
    }
    
    @Override
    public void init(String conf) {
    }
    
    @Override
    public void onMsg(Msg msg) {
        log.debug("attention listener got one message. " + msg);
        if (!(msg instanceof AttentionMsg)) {
            log.error(" the message is not attention message." + msg);
            return;
        }
        AttentionMsg attentionMsg = (AttentionMsg) msg;
        try {
            addRemind(attentionMsg);
        } catch (Exception ie) {
            ie.printStackTrace();
            log.error("handling attention msg error.", ie);
        } 
    }
    
    /**
     * 添加提醒
     * @param attentionMsg 关注的消息
     */
    private void addRemind(AttentionMsg attentionMsg) {
        Bean msgBean = attentionMsg.getBody();
        String toUserIds = msgBean.getStr("TO_USERS");
        
        StringBuilder strWhere = new StringBuilder();

        //查询流程中满足 到哪个人了， 或者到了哪个节点了
        Bean queryBean = new Bean();
        
        strWhere.append(" and PI_ID = '").append(msgBean.getStr("PI_ID")).append("'");
        strWhere.append(" and (NODE_CODE = '").append(msgBean.getStr("NEXT_NODE")).append("'");
        strWhere.append(" or ");
        strWhere.append(" USER_CODE in ('" + toUserIds.replace(",", "','") + "'))");
        strWhere.append(" and S_FLAG = ").append(Constant.YES_INT);
        queryBean.set(Constant.PARAM_WHERE, strWhere);
        
        List<Bean> attentions = ServDao.finds(ServMgr.SY_COMM_ATTENTION, queryBean);
        
        for (Bean attention: attentions) {
            Bean attBean = new Bean();
            attBean.set("TYPE", attention.getStr("REMIND_TYPE")); //关注设置的提醒方式
            attBean.set("REM_CONTENT", attention.getStr("REMIND_CONTENT"));
            attBean.set("REM_TITLE", "关注提醒：" + msgBean.getStr("TITLE"));
            attBean.set("SERV_ID", attention.getStr("SERV_ID"));
            attBean.set("DATA_ID", attention.getStr("DATA_ID"));
            
            //attBean.set("EXECUTE_TIME", DateUtils.getDatetime()); 
            
            String urlStr = attention.getStr("SERV_ID") + ".byid.do?data={_PK_:" + attention.getStr("DATA_ID") + "}";
            attBean.set("REM_URL", urlStr);
            
            RemindMgr.add(attBean, attention.getStr("S_USER"));
            
            //发了提醒之后，将attention 的S_FLAG置成2
            Bean updateAtt = new Bean(attention.getId()).set("S_FLAG", Constant.NO_INT);
            ServDao.update(ServMgr.SY_COMM_ATTENTION, updateAtt);
        }
    }

}
