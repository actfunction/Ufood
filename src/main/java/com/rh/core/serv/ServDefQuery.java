package com.rh.core.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;

/**
 * 服务常用查询定义类
 * 
 * @author wanglong
 */
public class ServDefQuery extends CommonServ {
    /**
     * 修改服务定义后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && !paramBean.getLinkMode()) { //非级联添加才更新缓存
            if (outBean.isNotEmpty("SERV_ID")) {
                ServUtils.udpateMtime(outBean.getStr("SERV_ID"), outBean.getStr("S_MTIME"));
            }
        }
    }

    /**
     * 删除规则定义后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        if (!paramBean.getLinkMode()) { //非级联删除模式才更新json文件
            List<Bean> dataList = outBean.getDataList();
            if (dataList.size() > 0) {
                Bean dataBean = dataList.get(0);
                if (dataBean != null) {
                    ServUtils.udpateMtime(dataBean.getStr("SERV_ID"), null);
                }
            }
        }
    }
    
    /**
     * 导入常用查询
     * @param paramBean 参数信息
     * @return 输出信息
     */
    public OutBean impQueries(ParamBean paramBean) {
        String servId = paramBean.getServId();
        String[] name = {"本部门", "本处室", "本人", "本月", "本年"};
        String[] sql = {"and S_TDEPT='@TDEPT_CODE@'", 
                "and S_DEPT='@DEPT_CODE@'", "and S_USER='@USER_CODE@'", 
                "and S_MTIME like '@DATE_YEARMONTH@%'", "and S_MTIME like '@DATE_YEAR@%'"};
        String[] flag = {Constant.YES, Constant.YES, Constant.YES, Constant.YES, Constant.YES};
        ParamBean param = new ParamBean(servId, ServMgr.ACT_BATCHSAVE);
        List<Bean> wheres = new ArrayList<Bean>();
        for (int i = 0; i < name.length; i++) {
            Bean where = new Bean();
            where.set("SERV_ID", paramBean.getStr("SERV_ID"));
            where.set("QUERY_NAME", name[i]);
            where.set("QUERY_SQL", sql[i]);
            where.set("QUERY_DEFAULT", "2");
            where.set("QUERY_TYPE", "1"); //缺省为服务常用查询
            where.set("QUERY_ORDER", i * 10);
            where.set("S_FLAG", flag[i]);
            wheres.add(where);
        }
        param.setBatchSaveDatas(wheres);
        OutBean outBean = ServMgr.act(param);
        if (outBean.isOk()) {
            ServUtils.clearServCache(paramBean.getStr("SERV_ID"));
        }
        return outBean;
    }
}
