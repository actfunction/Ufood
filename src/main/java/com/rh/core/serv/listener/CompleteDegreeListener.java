package com.rh.core.serv.listener;

import java.util.Map;

import com.rh.core.base.Bean;
import com.rh.core.comm.CacheMgr;
import com.rh.core.comm.CompleteDegreeMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.Constant;

/**
 * 监听完成度，触发计算其主服务的完整度并置回表中（内存变量会有集群同步的问题暂时不支持）
 * @author wanglong
 */
public class CompleteDegreeListener {
    /** 需要忽略的方法  */
    private static final String NONE_WRITE_ACTIONS = "show,byid,query,serv,batchSave";
    /** 完整度缓存类型名 */
    private static final String COMPLETESETTING = "SY_COMM_COMPLETE_SETTINGS";
    
    /**
     * 执行后
     * @param act 操作
     * @param paramBean 参数bean
     * @param result 结果
     */
    @SuppressWarnings("unchecked")
    public void after(String act, ParamBean paramBean, OutBean result) {
        //？？针对多条记录的result，可以判断是否为空后只传送一条，然后计分的字段只选子表的外键即可
        // 截获配置了完成度的服务，触发计算其主服务的完整度并置回表中（内存变量会有集群同步的问题暂时不支持）
        if (result.getStr(Constant.RTN_MSG).indexOf(Constant.RTN_MSG_OK) >= 0) {
            if (NONE_WRITE_ACTIONS.indexOf(act) < 0) {
                Bean dataBean = new Bean();
                if (act.equals("delete")) {
                    dataBean = (Bean) result.getList("_DATA_").get(0);
                } else {
                    dataBean = result;
                }
                // 获取等价服务配置缓存
                Map<String, String[]> equalServHash = (Map<String, String[]>) CacheMgr.getInstance().get(
                        "equalServHash", COMPLETESETTING);
                if (equalServHash == null) {
                    CompleteDegreeMgr.initCompleteDegSettings();
                    equalServHash = (Map<String, String[]>) CacheMgr.getInstance().get(
                            "equalServHash", COMPLETESETTING);
                }
                if (equalServHash.containsKey(paramBean.getServId())) {
                    String[] equalServs = (String[]) equalServHash.get(paramBean.getServId());
                    for (String equalServ : equalServs) {
                        CompleteDegreeMgr.computeCompleteDegree(equalServ, paramBean.getId(), dataBean, act);
                    }
                }
                CompleteDegreeMgr.computeCompleteDegree(paramBean.getServId(), paramBean.getId(), dataBean, act);
            }
        }
    }
}
