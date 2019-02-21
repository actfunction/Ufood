package com.rh.core.serv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.serv.util.ServUtils;

/**
 * 从服务定义上查询服务字段
 * @author wanglong
 *
 */
public class ServDefItemQuery extends CommonServ {
    /**
     * 
     * @param paramBean 参数Bean
     * @return 从服务定义上查询服务字段
     */
    public OutBean query(ParamBean paramBean) {
        String servId = paramBean.getStr("SRC_SERV_ID");
        if (StringUtils.isEmpty(servId)) {
            servId = paramBean.getServId();
        }
        
        ServDefBean servDef = ServUtils.getServDef(servId);
        LinkedHashMap<String, Bean> list = servDef.getAllItems();
        
        //ITEM_TYPE
        int itemType = paramBean.getInt("ITEM_TYPE");
        int itemInputType  = paramBean.getInt("ITEM_INPUT_TYPE");
        
        List<Bean> rtnList = new ArrayList<Bean>();

        for (String key : list.keySet()) {
            Bean itemBean = list.get(key);
            if (itemType > 0) {
                if (itemBean.getInt("ITEM_TYPE") == itemType) {
                    rtnList.add(itemBean);
                }

            } else if (itemInputType > 0) {
                if (itemBean.getInt("ITEM_INPUT_TYPE") == itemInputType) {
                    rtnList.add(itemBean);
                }
            } else {
                rtnList.add(itemBean);
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
