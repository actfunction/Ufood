package com.rh.core.wfe.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;

/**
 * 工作流中 业务数据 表 数据库操作
 * @author wanglong
 * 
 */
public class ServDataDao {
    private static Log log = LogFactory.getLog(ServDataDao.class);

    /** 业务数据中流程ID的字段名 */
    public static final String SERV_DATA_PROC_ID = "S_WF_INST";

    /**
     * 数据实体更新操作 , 如果没有docID , 则会添加一条记录
     * 
     * @param docId 文档ID
     * @param servId 功能
     * @param recordBean 数据
     * @return 新的数据
     */
    public static Bean updateServInstBean(String servId, String docId,
            Bean recordBean) {
        recordBean.setId(docId);
        ParamBean param = new ParamBean(recordBean);
        param.setServId(servId).setAct(ServMgr.ACT_SAVE);
        recordBean = ServMgr.act(param);
        return recordBean;
    }

    /**
     * 数据实体的删除操作，如果数据实体启用了假删除，则只处理删除标志，不真删除数据
     * @param docId 文档ID
     * @param servId 功能
     */
    public static void deleteServInstBean(String servId, String docId) {
        ParamBean param = new ParamBean(servId, ServMgr.ACT_DELETE, docId);
        ServMgr.act(param);
    }
    
    /**
     * 取得 业务数据
     * @param servId 表单ID
     * @param docId 表单实例ID
     * @return 业务数据bean
     */
    public static Bean findServInst(String servId, String docId) {
        log.debug("the serv bean information is servID " + servId + " and the docId is " + docId);

        return ServDao.find(servId, docId);
    }
}
