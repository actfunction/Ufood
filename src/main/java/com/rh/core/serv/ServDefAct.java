package com.rh.core.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.util.ServUtils;

/**
 * 服务操作定义类
 * 
 * @author wanglong
 */
public class ServDefAct extends CommonServ {
    
    /**
     * 导入标准按钮
     * @param paramBean 参数信息
     * @return 输出信息
     */
    public Bean impActs(ParamBean paramBean) {       
        String[] code = {"add", "byid", "batchSave", "delete", "exp", "imp", "save", "copyNew", "logItem", 
                "receive", "impZip", "expZip", "modify"};
        String[] name = {" 添 加 ", " 查 看 ", " 保 存 ", " 删 除 ", " 导 出 ", " 导 入 ", " 保 存 ",
                "复制并新建", "变更历史", " 接 收 ", "批量导入", "批量导出", "修改"};
        int[] type = {1, 3, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 3};
        int[] group = {1, 2, 1, 1, 2, 1, 1, 1, 2, 2, 1, 2, 1};
        int[] logFlag = {2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] flag = {1, 1, 2, 1, 2, 2, 1, 2, 2, 2, 2, 2, 1};
        String[] css = {"add", "byid", "batchSave", "delete", "exp", "imp", "save", "copy", "search", "exp", "imp",
                "imp", "exp", "modify"};
        String[] express = {"", "", "", "", "", "", "", "'#_ADD_#'==''", "'#_ADD_#'==''", "", "", "", ""};
        String[] actMemo = {"", "", "", "", "@{\"_SELECT_\":\"*\"}", "", "", "", "",  "this.receive();", "", "", ""};
        String[] actTip = {"", "", "", "", "", "", "", "", "", "", "zip文件导入", "zip文件导出", 
                "确保列表无编辑组按钮卡片也可以保存"};
        int[] order = {20, 10, 30, 100, 120, 110, 50, 60, 70, 130, 140, 150, 15};
        ParamBean param = new ParamBean(ServMgr.SY_SERV_ACT, ServMgr.ACT_BATCHSAVE);
        List<Bean> acts = new ArrayList<Bean>();
        for (int i = 0; i < code.length; i++) {
            Bean act = new Bean();
            act.set("SERV_ID", paramBean.getStr("SERV_ID"));
            act.set("ACT_CODE", code[i]);
            act.set("ACT_NAME", name[i]);
            act.set("ACT_TYPE", type[i]);
            act.set("ACT_GROUP", group[i]);
            act.set("ACT_ORDER", order[i]);
            act.set("ACT_LOG_FLAG", logFlag[i]);
            act.set("ACT_CSS", css[i]);
            act.set("ACT_EXPRESSION", express[i]);
            act.set("ACT_MEMO", actMemo[i]);
            act.set("ACT_TIP", actTip[i]);
            act.set("S_FLAG", flag[i]);
            acts.add(act);
        }
        param.setBatchSaveDatas(acts);
        OutBean outBean = ServMgr.act(param);
        if (outBean.isOkOrWarn()) {
            ServUtils.clearServCache(paramBean.getStr("SERV_ID"));
        }
        return outBean;
    }
    /**
     * 修改服务定义后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && !paramBean.getLinkMode()) { //非级联才更新缓存
            if (!outBean.isEmpty("SERV_ID")) {
                ServUtils.udpateMtime(outBean.getStr("SERV_ID"), outBean.getStr("S_MTIME"));
            }
        }
    }

    /**
     * 删除服务操作后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk() && !paramBean.getLinkMode()) { //非级联才更新缓存
            List<Bean> dataList = outBean.getDataList();
            if (dataList.size() > 0) {
                Bean dataBean = dataList.get(0);
                if (dataBean != null) {
                    ServUtils.udpateMtime(dataBean.getStr("SERV_ID"), null);
                }
            }
        }
    }
}
