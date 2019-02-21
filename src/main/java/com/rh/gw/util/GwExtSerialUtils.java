package com.rh.gw.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 用于审批单上的公共方法
 * 此类专门放置处理公文编码和流水号的方法
 * @author WeiTl
 * @version 1.0
 */
public class GwExtSerialUtils {
	/*** 记录历史 */
	private static Log log = LogFactory.getLog(GwExtSerialUtils.class);
    
    /**
     * 按照年份和机构查出最大的流水号
     * @param paramBean 查询参数
     * @param servId 查询对应的服务ID
     * @param maxItem 查询最大流水号对应的字段
     * @return 返回最大的流水号
     */
    public static int getSerial(ParamBean paramBean, String servId, String maxItem) {
        int maxValue = 1;
        String max = "MAX(" + maxItem + ")";
        paramBean.set(Constant.PARAM_SELECT, " " + max);
        Bean result = ServDao.find(servId, paramBean);
        maxValue = result.getInt(max);
        if (maxValue <= 0) {
            maxValue = 1;
        } else {
            ++maxValue;
        }
        return maxValue;
    }
    
    /**
     * 按照年份和机构查出最大的流水号
     * 并拼接成GW_CODE编号保存
     * @param paramBean 查询参数
     * @param servId 查询对应的服务ID
     * @param maxItem 查询最大流水号对应的字段
     * @return 返回最大的流水号
     */
    public static int getAndSaveSerial(ParamBean paramBean, OutBean outBean, String servId, String maxItem) {
        int maxValue = 1;
        String max = "MAX(" + maxItem + ")";
        paramBean.set(Constant.PARAM_SELECT, " " + max);
        Bean result = ServDao.find(servId, paramBean);
        maxValue = result.getInt(max);
        if (maxValue <= 0) {
            maxValue = 1;
        } else {
            ++maxValue;
        }
        outBean.set(maxItem, maxValue);
        outBean.set("GW_CODE", outBean.getStr("GW_YEAR_CODE") + "[" + outBean.getStr("GW_YEAR") + "]" + maxValue + "号");
        ServDao.save(servId, outBean);
        return maxValue;
    }
}
