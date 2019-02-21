package com.rh.core.comm.print;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.mind.UserMind;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;
/**
 * 
 * @author wanglong
 * 
 */
public class PrintDataHelper {
    @SuppressWarnings("unused")
    private String ftlName = "printMind.ftl";

    /**
     * 
     * @param ftl 模版
     */
    public void setMindPrintFtl(String ftl) {
        this.ftlName = ftl;
    }
   /**
    * 获取公共数据
    * @param paramBean 入参
    * @return 需要打印的数据
    */
    public OutBean getCommData(Bean paramBean) {
        /**
         * 这四个参数必须确保都传递了，代码不做检查，默认认为都传了
         */
        String servId = paramBean.getStr("servId"); // 服务ID
        String servSrcId = paramBean.getStr("servSrcId");
        String dataId = paramBean.getStr("dataId"); // 数据ID

        // 审签单基础数据数据
        ParamBean queryBean = new ParamBean().set(Constant.PARAM_SERV_ID,
                servId).set(Constant.KEY_ID, dataId);
        OutBean outBean = ServMgr.act(servId, "getPrintData", queryBean);

        FileDataHelper fdh = new FileDataHelper(servSrcId, dataId);
        outBean.put("_ZHENGWEN_", fdh.getFileNames("ZHENGWEN"));
        outBean.put("_FUJIAN_", fdh.getFileNames("FUJIAN"));
        outBean.put("_ZHUANFA_", fdh.getFileNames("ZHUANFA"));

        /*
         * 取得意见列表中的数据 按意见编码分类，并拼接显示样式
         */
        UserMind userMind = new UserMind(Context.getUserBean());
        userMind.query(servId, dataId);

        outBean.put("_userMind_", userMind);

        return outBean;
    }
}
