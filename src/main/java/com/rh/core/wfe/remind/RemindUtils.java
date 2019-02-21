package com.rh.core.wfe.remind;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 催督办帮助类
 * 
 * @author wanglong
 * 
 */
public class RemindUtils {

    /**
     * 自动催督办的状态：未完成
     */
    public static final int STATUS_AUTO_UNFINISH = 0;

    /**
     * 自动催督办的状态：已完成
     */
    public static final int STATUS_AUTO_FINISHED = 1;

    /**
     * 自动催督办的类型：催办
     */
    public static final int TYPE_AUTO_REMD = 0;

    /**
     * 自动催督办的类型：督办
     */
    public static final int TYPE_AUTO_SUPE = 1;

    /**
     * 获取代字流水号
     * @param servId 服务ID
     * @param cdBean 催办或督办Bean
     * @param codeNum 代字流水号
     * @return 代字流水号
     */
    public static Bean getMaxCode(String servId, Bean cdBean, String codeNum) {
        cdBean.set(Constant.PARAM_SELECT, "max(" + codeNum + ") " + codeNum);
        Bean codeBean = ServDao.find(servId, cdBean);
        if (!codeBean.isEmpty(codeNum)) {
            codeBean.set(codeNum, codeBean.getInt(codeNum) + 1);
        } else {
            codeBean.set(codeNum, 1);
        }
        return codeBean;
    }
}
