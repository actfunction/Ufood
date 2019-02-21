package com.rh.core.serv.relate;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.comm.entity.EntityMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServUtils;

/**
 * 相关数据的服务类
 *
 */
public class RelateServ  extends CommonServ {

    /**
     * 删除之后的拦截方法，由子类重载
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        List<Bean> dataList = outBean.getDataList();
        String servId = paramBean.getServId();
        for (Bean dataBean : dataList) {
            String relateServId = dataBean.getStr("RELATE_SERV_ID");
            ServDefBean relateServ = ServUtils.getServDef(relateServId);
            if (relateServ.hasRelate() && relateServ.getRelateIds().contains(dataBean.getStr("SERV_ID"))) {
                Bean param = new Bean().set("SERV_ID", dataBean.getStr("RELATE_SERV_ID"))
                        .set("DATA_ID", dataBean.getStr("RELATE_DATA_ID"))
                        .set("RELATE_SERV_ID", dataBean.getStr("SERV_ID"))
                        .set("RELATE_DATA_ID", dataBean.getStr("DATA_ID"));
                ServDao.delete(servId, param);
            }
        }
    }
   
    /**
     * 添加之后判断是否存在逆向关联，如果存在则复制一条逆向关联数据。
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        String servId = paramBean.getServId();
        if (outBean.isOk()) { //添加时增加逆向连接
            String relateServId = paramBean.getStr("RELATE_SERV_ID");
            ServDefBean relateServ = ServUtils.getServDef(relateServId);
            if (paramBean.getId().length() == 0 && relateServ.hasRelate()) {
                if (relateServ.getRelateIds().contains(paramBean.getStr("SERV_ID"))) {
                    Bean param = new Bean().set("SERV_ID", paramBean.getStr("RELATE_SERV_ID"))
                            .set("DATA_ID", paramBean.getStr("RELATE_DATA_ID"))
                            .set("RELATE_SERV_ID", paramBean.getStr("SERV_ID"))
                            .set("RELATE_DATA_ID", paramBean.getStr("DATA_ID"))
                            .set("RELATE_TYPE", paramBean.getStr("RELATE_TYPE"));
                    ServDao.create(servId, param);
                }
            }
        }
    }

    @Override
    protected void beforeBatchSave(ParamBean paramBean) {
        super.beforeBatchSave(paramBean);
        List<Bean> batchSaves = paramBean.getBatchSaveDatas();
        List<Bean> resultBean = new  ArrayList<Bean>();
        for (Bean bean : batchSaves) {
            String servId = bean.getStr("RELATE_SERV_ID");
            String dataId = bean.getStr("DATA_ID");
            //如果从综合查询界面选择相关文件，那么把服务ID改成正确的服务。
            if (servId.indexOf("SY_COMM_ENTITY") > 0) {  
                Bean entity = EntityMgr.getEntity(dataId);
                if (entity == null) {
                    continue;
                }
                bean.set("RELATE_SERV_ID", entity.getStr("SERV_ID"));
            }
            String relateDataId = bean.getStr("RELATE_DATA_ID");
            if (!dataId.equals(relateDataId)) {
                resultBean.add(bean);
            }
        }
        paramBean.setBatchSaveDatas(resultBean);
    }
}
