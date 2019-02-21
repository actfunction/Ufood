package com.rh.core.comm.remind;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.lang.Assert;

/**
 * 提醒管理类
 * @author wanglong
 * 
 */
public class RemindMgr {

    /** 提醒服务ID **/
    public static final String REMIND_SERV_ID = "SY_COMM_REMIND";

    /** 提醒人服务ID **/
    public static final String REMIND_USER_SERV_ID = "SY_COMM_REMIND_USERS";
    /** 提醒服务ID **/
    public static final String REMIND_HIS_SERV_ID = "SY_COMM_REMIND_HIS";

    /**
     * 结束提醒
     * @param paramBean 提醒消息bean
     * 
     */
    public static void finish(Bean paramBean) {
        Bean newBean = paramBean.copyOf();
        newBean.set("STATUS", "FINISHED");
        newBean.set("FINISH_TIME", DateUtils.getDatetime());
        newBean.set("REM_ID", newBean.getId());
        newBean.setId("");
        ServDao.save(REMIND_HIS_SERV_ID, newBean);
        ServDao.delete(REMIND_SERV_ID, paramBean);
    }
    
    /**
     * 取消提醒
     * @param paramBean 提醒消息bean
     * 
     */
    public static void cancle(Bean paramBean) {
        Bean newBean = paramBean.copyOf();
        newBean.set("STATUS", "CANCLE");
        newBean.set("REM_ID", newBean.getId());
        newBean.setId("");
        ServDao.save(REMIND_HIS_SERV_ID, newBean);
        ServDao.delete(REMIND_SERV_ID, paramBean);
    }

    /**
     * 添加提醒
     * @param msgBean 提醒消息bean
     * @param receivers 被提醒人
     */
    public static void add(Bean msgBean, String receivers) {
        // 提醒数据保存后保存被提醒人的数据,先删除再添加
        String userCode = null;
        String cmpyCode = null;
        UserBean userBean = Context.getUserBean();
        if (userBean != null) {
            userCode = userBean.getCode();
            cmpyCode = userBean.getCmpyCode();
        }
        String servId = msgBean.getStr("SERV_ID");
        Bean servBean = ServUtils.getServDef(servId);
        msgBean.set("STATUS", msgBean.get("STATUS", "WATING"));
        msgBean.set("TYPE", msgBean.get("TYPE", "TODO"));
        msgBean.set("S_EMERGENCY", msgBean.get("S_EMERGENCY", 10));
        msgBean.set("S_ATIME", DateUtils.getDatetime());
        msgBean.set("S_FLAG", Constant.NO_INT);
        msgBean.set("SERV_ID", servId);
        msgBean.set("SERV_SRC_ID", servBean.getStr("SERV_SRC_ID"));
        msgBean.set("DATA_ID", msgBean.getStr("DATA_ID"));
        msgBean.set("S_USER", userCode);
        msgBean.set("S_CMPY", cmpyCode);
        msgBean.set("USER_ID", receivers);

        msgBean = ServDao.save(REMIND_SERV_ID, msgBean);

        Assert.hasText(receivers, "接收人不能为空");
        
        if (receivers.length() > 0) {
            String[] remindUsers = receivers.split(",");
            Bean remindUserBean = new Bean();
            remindUserBean.set("REMIND_ID", msgBean.getId());
            ServDao.delete(REMIND_USER_SERV_ID, remindUserBean);
            for (String remindUserID : remindUsers) {
                remindUserBean = new Bean();
                remindUserBean.set("USER_ID", remindUserID);
                remindUserBean.set("REMIND_ID", msgBean.getId());
                ServDao.save(REMIND_USER_SERV_ID, remindUserBean);
            }
        }
    }

    /**
     * 
     * @param userCode 用户代码
     * @param remindId 提醒消息ID
     * @param successType 成功的提醒类型
     * @param failureType 失败的提醒类型
     */
    public static void modifyUserRemindStatus(String userCode, String remindId,
            String successType, String failureType) {
        Bean whereBean = new Bean();
        whereBean.set("REMIND_ID", remindId);
        whereBean.set("USER_ID", userCode);

        Bean setBean = new Bean();
        setBean.set("SUCCESS", successType);
        setBean.set("FAILURE", failureType);

        ServDao.updates(REMIND_USER_SERV_ID, setBean, whereBean);
    }
}
