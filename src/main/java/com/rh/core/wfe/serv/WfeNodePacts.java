package com.rh.core.wfe.serv;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.wfe.def.WfProcDefManager;

/**
 * 
 * @author 操作公共按钮类
 * 
 */
public class WfeNodePacts extends CommonServ {

    /** 公共按钮服务 **/
    public static final String SY_WFE_NODE_PACTS = "SY_WFE_NODE_PACTS";

    /**
     * 
     * @param paramBean 参数Bean
     * @return Bean
     */
    public Bean addPublicButtons(Bean paramBean) {
        int actType = paramBean.getInt("ACT_TYPE");
        String procCode = paramBean.getStr("PROC_CODE");
        String[] actCode = paramBean.getStr("ACT_CODE").split(",");
        String[] actName = paramBean.getStr("ACT_NAME").split(",");
        int actSort = 0;
        for (int i = 0; i < actCode.length; i++) {
            SqlBean queryBean = new SqlBean();
            queryBean.and("PROC_CODE", procCode).and("ACT_TYPE", actType);
            queryBean.and("ACT_CODE", actCode[i]);
            int count = ServDao.count(SY_WFE_NODE_PACTS, queryBean);
            if (count == 0) {
                Bean dataBean = new Bean();
                dataBean.set("PROC_CODE", procCode).set("ACT_CODE", actCode[i]).set("ACT_NAME", actName[i])
                        .set("ACT_TYPE", actType).set("ACT_SORT", actSort);
                ServDao.create(SY_WFE_NODE_PACTS, dataBean);
            }
        }
        
        //清除流程缓存
        WfProcDefManager.removeFromCache(procCode);
        return new Bean().set(Constant.RTN_MSG, Constant.RTN_MSG_OK);
    }

    /**将 SY_WFE_PROC_DEF 表中的 PROC_CODE, BIND_BUTTONS,BIND_BUTTONS_NAME 迁移到表 SY_WFE_NODE_PACTS 中
     * 迁移数据
     */
    public void impOldData() {
        List<Bean> resultBeans = ServDao.finds("SY_WFE_PROC_DEF",
                new Bean().set(Constant.PARAM_SELECT, " PROC_CODE,BIND_BUTTONS,BIND_BUTTONS_NAME,S_MTIME"));
        for (int i = 0; i < resultBeans.size(); i++) {
            Bean oneBean = resultBeans.get(i);
            String[] bindButtons = oneBean.getStr("BIND_BUTTONS").split(",");
            String[] bingButtonsName = oneBean.getStr("BIND_BUTTONS_NAME").split(",");
            for (int j = 0; j < bindButtons.length; j++) {
                if (!StringUtils.isEmpty(bindButtons[j])) {
                    Bean dataBean = new Bean();
                    dataBean.set("PROC_CODE", oneBean.getStr("PROC_CODE")).set("ACT_TYPE", 1)
                            .set("S_ATIME", oneBean.getStr("S_MTIME"))
                            .set("ACT_CODE", bindButtons[j]).set("ACT_NAME", bingButtonsName[j]);

                    ServDao.create(SY_WFE_NODE_PACTS, dataBean);
                }
            }

        }

    }
}
