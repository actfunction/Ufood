package com.rh.core.comm.remind;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.dict.DictMgr;

/**
 * 提醒服务类
 * @author wanglong
 * 
 */
public class RemindServ extends CommonServ {
    /**
     * 通过字典code获取字典名称
     * @param paramBean 参数
     * @return outBean
     */
    public Bean getUserDict(Bean paramBean) {
        Bean outBean = new Bean();
        //判断字典项是否为空
        if (!paramBean.getStr("DICT_CODE").equals("")) {
            String[] dictCodes = paramBean.getStr("DICT_CODE").split(",");
            StringBuffer userName = new StringBuffer();
            for (int i = 0; i < dictCodes.length; i++) {
                //根据字典CODE获取字典项名称
                outBean = DictMgr.getItem(paramBean.getStr("DICT_ID"), dictCodes[i]);
                if (i != (dictCodes.length - 1)) {
                    userName.append(outBean.getStr("NAME") + ",");
                } else {
                    userName.append(outBean.getStr("NAME"));
                }
            }
            //返回一个字符串
            outBean.set("USER_NAMES", userName);
        }
        return outBean;
    }   
}
