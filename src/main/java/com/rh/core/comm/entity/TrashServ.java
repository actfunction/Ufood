package com.rh.core.comm.entity;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.wfe.WfProcess;

/**
 * 回收站服务类，处理已删除文件的恢复或者彻底删除
 * 
 * @author wanglong
 * 
 */
public class TrashServ extends CommonServ {
    /**
     * 恢复已删除的数据
     * @param paramBean 参数信息，通过_PK_设置，多条逗号分隔
     * @return 恢复的结果
     */
    public OutBean restore(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        String[] ids = paramBean.getId().split(Constant.SEPARATOR);
        int count = 0;
        for (String id : ids) {
            Bean entity = ServDao.find(ServMgr.SY_COMM_ENTITY, id);
            if (entity != null) {
                //先设置对应服务数据的有效标志
                String servId = entity.getStr("SERV_ID"); //原始服务ID
                String dataId = entity.getStr("DATA_ID"); //原始数据ID
                ParamBean param = new ParamBean().set("S_FLAG", Constant.YES_INT);
                param.setId(dataId);
                ServMgr.act(servId, ServMgr.ACT_SAVE, param);
                if (entity.isNotEmpty("S_WF_INST")) { //恢复工作流
                    restoreWf(servId, dataId);
                }
                //清除服务对应的数据和字典缓存
                ServDefBean servDef = ServUtils.getServDef(servId);
                servDef.clearDataCache(dataId);
                servDef.clearDictCache(entity.getStr("S_CMPY"));
                //再修改实体信息
                EntityMgr.restoreEntity(dataId);
                count++;
            }
        }
        outBean.setOk(Context.getSyMsg("SY_DATA_RESTOR_OK", String.valueOf(count)));
        return outBean;
    }
    
    
    /**
     * 恢复流程数据 
     * @param servId
     *            服务ID
     * @param dataId
     *            数据ID

     */
    private void restoreWf(String servId, String dataId) {
        Bean dataBean = ServDao.find(servId, dataId);
        if (dataBean != null && !dataBean.isEmpty("S_WF_INST")) {
            // 恢复流程
            WfProcess process = new WfProcess(dataBean.getStr("S_WF_INST"), false);
            process.restore();            
        }
    }
    
    /**
     * 彻底删除回收站数据
     * @param paramBean 参数集合
     * @return 删除结果集
     */
    public OutBean thoroughDelete(ParamBean paramBean) {
        //设置为强制删除
        String[] servIds = paramBean.getStr("servIds").split(Constant.SEPARATOR);
        String[] dataIds = paramBean.getStr("dataIds").split(Constant.SEPARATOR);
        int count = 0;
        for (int i = 0; i < servIds.length; i++) {
            if (!"".equals(servIds[i].trim())) {
                ParamBean delBean = new ParamBean(servIds[i], ServMgr.ACT_DELETE);
                delBean.setDeleteDropFlag(true);
                delBean.setId(dataIds[i]);
                ServMgr.act(delBean);
                count = count + 1;
            }
        }
        return new OutBean().set("MSG", "OK").set("COUNT", count);
    }
}
