package com.rh.core.wfe.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDao;

/**
 * 节点用户实例 历史
 * @author wanglong
 *
 */
public class WfNodeUserHisDao {
    private static Log log = LogFactory.getLog(WfNodeUserHisDao.class);

    /**
     * 节点用户实例 历史 code
     */
    public static final String SY_WFE_NODE_USERS_HIS = "SY_WFE_NODE_USERS_HIS";

    /**
     * 复制历史信息到节点用户表
     * @param pid 流程实例ID
     */
    public static void copyHisToNodeUser(String pid) {
        log.debug("copy the node user inst history data to the inst table");

        String sqlStr = "insert into "
                + WfNodeUserDao.SY_WFE_NODE_USERS
                + " (select * from " + SY_WFE_NODE_USERS_HIS
                + " where PI_ID = '" + pid + "')";

        Context.getExecutor().execute(sqlStr);
    }

    /**
     * 真删除 历史节点用户信息
     * @param pid 流程实例ID
     */
    public static void destroyHisNodeUser(String pid) {
        Bean paramBean = new Bean();
        paramBean.set("PI_ID", pid);

        ServDao.destroys(SY_WFE_NODE_USERS_HIS, paramBean);
    }
}
