package com.rh.core.plug.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.msg.Msg;
import com.rh.core.util.msg.MsgListener;

/**
 * index save handler
 * @author wanglong
 * 
 */
public class IndexReceiver implements MsgListener {

    /** log */
    private static Log log = LogFactory.getLog(IndexReceiver.class);

    @Override
    public void onMsg(Msg msg) {
        log.debug("index listener got one message. " + msg);
        if (!(msg instanceof IndexMsg)) {
            log.error(" the message is not index message." + msg);
            return;
        }
        IndexMsg indexMsg = (IndexMsg) msg;

        try {
            IIndexServ servIndex = ServUtils.getIndexServ();
            ARhIndex index =  indexMsg.getIndex();
            if (index.getAct().equals(ServMgr.ACT_ADD)) {
                servIndex.saveIndex(index);
            } else if (index.getAct().equals(ServMgr.ACT_DELETE)) {
                // delete index
                servIndex.deleteIndex(index);
            } else {
                servIndex.saveIndex(index);
            }
        } catch (Exception ie) {
            log.error("handling index msg error.", ie);
        } 
    }

    @Override
    public void init(String conf) {
    }
}
