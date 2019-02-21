package com.rh.core.wfe.remind;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.comm.todo.TodoBean;
import com.rh.core.comm.todo.TodoUtils;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;

/**
 * 处理催督办服务的监听类
 * 
 * @author wanglong
 * 
 */
public class RemindServ extends CommonServ {

    /**
     * 服务ID
     */
    public static final String SERV_ID = "SY_WFE_REMIND";

    /**
     * 状态：未送出
     */
    public static final int STATE_NOSEND = 0;

    /**
     * 状态：未反馈
     */
    public static final int STATE_SENT = 1;

    /**
     * 状态：已反馈
     */
    public static final int STATE_FEEDBACK = 2;

    /**
     * 状态：已办结
     */
    public static final int STATE_FINISH = 3;

    /**
     * 根据代字、年度取得最大流水号
     * @param paramBean 参数信息
     * @return 流水号
     */
    public OutBean getMaxCode(ParamBean paramBean) {
        String servId = paramBean.getServId();
        Bean cdBean = new Bean();
        cdBean.set(WfeRemindItem.REMD_CODE, paramBean.getStr(WfeRemindItem.REMD_CODE));
        cdBean.set(WfeRemindItem.REMD_YEAR, paramBean.getStr(WfeRemindItem.REMD_YEAR));
        Bean codeBean = RemindUtils.getMaxCode(servId, cdBean, WfeRemindItem.REMD_NUM);
        return new OutBean(codeBean);
    }

    /**
     * 发送催办单
     * @param paramBean 参数信息
     * @return 送交信息
     */
    public OutBean sendTodo(ParamBean paramBean) {
        String servId = paramBean.getServId();
        OutBean cdBean = this.byid(paramBean);
        TodoBean todoBean = new TodoBean();
        todoBean.setSender(cdBean.getStr(WfeRemindItem.S_USER));
        todoBean.setTitle(cdBean.getStr(WfeRemindItem.REMD_TITLE));
        todoBean.setOwner(cdBean.getStr(WfeRemindItem.ACPT_USER));
        todoBean.setCode(servId);
        
        String dataCode = cdBean.getStr(WfeRemindItem.REMD_CODE) + "(" + cdBean.getStr(WfeRemindItem.REMD_YEAR) 
                          + ")" + cdBean.getStr(WfeRemindItem.REMD_NUM) + "号";
        todoBean.setDataCode(dataCode);
        
        todoBean.setObjectId1(paramBean.getStr(WfeRemindItem.REMD_ID));
        todoBean.setUrl(servId + ".byid.do?data={_PK_:" + paramBean.getStr(WfeRemindItem.REMD_ID) + "}");
        TodoUtils.insert(todoBean);
        cdBean.set(WfeRemindItem.REMD_STATUS, new Integer(STATE_SENT));
        ServDao.update(servId, cdBean);
        // this.modify(cdBean);
        cdBean.setOk();
        return cdBean;
    }

    /**
     * 办结催办单
     * @param paramBean 参数信息
     * @return 办结信息
     */
    public OutBean finish(ParamBean paramBean) {
        String servId = paramBean.getServId();
        OutBean cdBean = this.byid(paramBean);
        if (cdBean.getId().length() <= 0) {
            throw new TipException(Context.getSyMsg("SY_DATA_NOT_EXIST", paramBean.getId()));
        }
        cdBean.set(WfeRemindItem.REMD_STATUS, new Integer(STATE_FINISH));
        ServDao.update(servId, cdBean);
        Bean todoBean = new Bean();
        todoBean.set("TODO_OBJECT_ID1", paramBean.getStr(WfeRemindItem.REMD_ID));
        todoBean.set("TODO_CODE", servId);
        TodoUtils.ends(todoBean);
        cdBean.setOk();
        return cdBean;
    }

    /**
     * 取消办结催办单
     * @param paramBean 参数信息
     * @return 取消办结信息
     */
    public OutBean unfinish(ParamBean paramBean) {
        OutBean cdBean = this.byid(paramBean);
        if (cdBean.getId().length() <= 0) {
            throw new TipException(Context.getSyMsg("SY_DATA_NOT_EXIST", paramBean.getId()));
        }
        cdBean.set(WfeRemindItem.REMD_STATUS, new Integer(STATE_NOSEND));
        ServDao.update(paramBean.getServId(), cdBean);
        cdBean.setOk();
        return cdBean;
    }

    @Override
    public OutBean byid(ParamBean paramBean) {
        OutBean outBean = super.byid(paramBean);
        if (outBean.isEmpty(WfeRemindItem.REMD_TITLE)) {
            appendServDataInfo(paramBean, outBean);
            appendOtherData(paramBean, outBean);
        }
        return outBean;
    }

    /**
     * 设置办理期限和被催办用户。ACPT_USER ,DEADLINE
     * @param paramBean 参数Bean
     * @param outBean 输出Bean
     */
    private void appendOtherData(ParamBean paramBean, OutBean outBean) {
        if (paramBean.isNotEmpty("DEADLINE")) {
            outBean.set(WfeRemindItem.DEADLINE, paramBean.getStr("DEADLINE"));
        }
        
        if (paramBean.isNotEmpty("ACPT_USER")) {
            UserBean user = UserMgr.getUser(paramBean.getStr("ACPT_USER"));
            outBean.set(WfeRemindItem.ACPT_USER, user.getCode());
            outBean.set(WfeRemindItem.ACPT_USER + "__NAME", user.getName());
            outBean.set("USER_NAME", user.getName());
        }
    }

    /**
     * DATA_ID ,SERV_ID ,
     * @param paramBean 参数Bean
     * @param outBean 输出Bean
     */
    private void appendServDataInfo(ParamBean paramBean, OutBean outBean) {
        if (paramBean.isEmpty("DATA_ID") || paramBean.isEmpty("SERV_ID")) {
            return;
        }

        String dataId = paramBean.getStr("DATA_ID");
        String servId = paramBean.getStr("SERV_ID");
        ServDefBean servDef = ServUtils.getServDef(servId);
        Bean dataBean = ServDao.find(servId, dataId);
        if (dataBean == null) {
            return;
        }
        
        outBean.set("DATA_ID", dataId);
        outBean.set("SERV_ID", servId);

        String title = "";
        if (servDef.getDataTitle().length() > 0) {
            title = ServUtils.replaceValues(servDef.getDataTitle(), servId, dataBean);
            outBean.set(WfeRemindItem.REMD_TITLE, title);
        }

        if (servDef.getDataCode().length() > 0) {
            final String code = ServUtils.replaceValues(servDef.getDataCode(), servDef.getId(), dataBean);
            outBean.set(WfeRemindItem.REMD_REASON, title + " " + code);
        }

    }

    @Override
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        super.afterDelete(paramBean, outBean);

        //删除催办的同时，删除催办单待办
        String servId = paramBean.getServId();
        List<Bean> list = outBean.getDataList();

        for (Bean bean : list) {
            TodoUtils.destroys(servId, bean.getId());
        }
    }
}
