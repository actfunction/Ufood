package com.rh.core.serv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;

/**
 * 从服务定义上查询服务操作
 * @author wanglong
 *
 */
public class ServActQuery extends CommonServ {
    /**
     * 
     * @param paramBean 参数Bean
     * @return 从服务定义上查询服务操作
     */
    public OutBean query(ParamBean paramBean) {
        String servId = paramBean.getStr("SRC_SERV_ID");
        ServDefBean servDef = ServUtils.getServDef(servId);
        LinkedHashMap<String, Bean> list = servDef.getAllActs();

        List<Bean> rtnList = new ArrayList<Bean>();

        for (String key : list.keySet()) {
            Bean actBean = list.get(key);
            if (actBean.getStr("ACT_CODE").equals(ServMgr.ACT_BYID)) {
                continue;
            }
            if (actBean.getInt("S_FLAG") == Constant.NO_INT) {
                continue;
            }
            if (actBean.getStr("ACT_CODE").equals(ServMgr.ACT_SAVE)
                    || actBean.getStr("ACT_CODE").equals(ServMgr.ACT_DELETE)) {
                rtnList.add(actBean);
            }
            if (actBean.getInt("ACT_TYPE") == 3) {
                rtnList.add(actBean);
            }
        }

        OutBean outBean = new OutBean();
        outBean.setPage(rtnList.size());
        outBean.setData(rtnList);
        ServDefBean servDefAct = ServUtils.getServDef(paramBean.getServId());
        outBean.setCols(servDefAct.getAllItems());
        return outBean;
    }
}
