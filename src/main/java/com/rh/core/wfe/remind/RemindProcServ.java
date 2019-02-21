package com.rh.core.wfe.remind;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;

/**
 * 处理催督办办理过程服务的监听类
 * 
 * @author wanglong
 * 
 */
public class RemindProcServ extends CommonServ {

    /**
     * 字段：流程意见
     */
    private static final String COLUMN_PROC_MIND = "PROC_MIND";

    /**
     * 完成工作，并反馈给催办人
     * @param paramBean 参数信息
     * @return 办结信息
     */
    public OutBean finish(ParamBean paramBean) {
        OutBean outBean;
        // 获取办理结果
        // 修改催办单的状态为：已反馈
        Bean cdBean = new Bean();
        cdBean.setId(paramBean.getStr(WfeRemindItem.REMD_ID));
        cdBean = ServDao.find(RemindServ.SERV_ID, cdBean);
        if (cdBean != null) {
            outBean = new OutBean(cdBean);
        } else {
            outBean = new OutBean();
        }
            
        // 如果办理结果为空，则取被催办人的反馈意见作为办理结果；如果不为空，则由用户自行修改
        if (cdBean.getStr(WfeRemindItem.DO_MIND).length() == 0) {
            List<Bean> cdProcList = this.finds(paramBean).getDataList();
            StringBuilder doMind = new StringBuilder("");
            for (Bean cdProcBean : cdProcList) {
                doMind.append(cdProcBean.get(COLUMN_PROC_MIND) + "\n");
            }
            if (doMind.length() > 0) {
                doMind.delete(doMind.length() - 1, doMind.length());
            }
            cdBean.set(WfeRemindItem.DO_MIND, doMind.toString());
        }
        cdBean.set(WfeRemindItem.REMD_STATUS, new Integer(RemindServ.STATE_FEEDBACK));
        ServDao.update(RemindServ.SERV_ID, cdBean);
        // 完成待办
        TodoBean todoBean = new TodoBean();
        todoBean.setObjectId1(paramBean.getStr(WfeRemindItem.REMD_ID));
        todoBean.setCode(RemindServ.SERV_ID);
        TodoUtils.ends(todoBean);
        // 给催办人送待办
        todoBean.setSender(cdBean.getStr(WfeRemindItem.ACPT_USER));
        todoBean.setTitle(cdBean.getStr(WfeRemindItem.REMD_TITLE));
        todoBean.setOwner(cdBean.getStr(WfeRemindItem.S_USER));
        todoBean.setCode(RemindServ.SERV_ID);
        todoBean.setObjectId1(paramBean.getStr(WfeRemindItem.REMD_ID));
        todoBean.setUrl(RemindServ.SERV_ID + ".byid.do?data={_PK_:" + paramBean.getStr(WfeRemindItem.REMD_ID) + "}");
        TodoUtils.insert(todoBean);
        outBean.setOk();
        return outBean;
    }

}
