package com.rh.core.comm.entity;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;

/**
 * Entity常用方法
 * @author wanglong
 * 
 */
public class EntityMgr {
    private static final String QUERY_ODEPT = "QUERY_ODEPT";

    /**
     * 更新数据对应的实体信息表
     * @param servDef 服务定义
     * @param dataBean 数据信息
     * @param paramBean 参数Bean
     */
    public static void updateEntity(ServDefBean servDef, Bean dataBean, ParamBean paramBean) {
        List<Bean> entities = findEntities(dataBean.getId());
        if (entities.size() > 0) {
            ParamBean commEntity = new ParamBean();
            String newKey = dataBean.getStr(servDef.getPKey());
            if (!dataBean.getId().equals(newKey)) { // 主键发生变化
                commEntity.set("DATA_ID", newKey);
            }
            
            appendEntityData(servDef, dataBean, commEntity, paramBean);
            
            Bean updateBean = null;
            if (entities.size() > 1) {
                updateBean = commEntity.copyOf();
            }
            
            // 单独保存一次，触发一次更新待办的程序。
            commEntity.setId(entities.get(0).getId());
            ServMgr.act(ServMgr.SY_COMM_ENTITY, ServMgr.ACT_SAVE, commEntity);

            if (updateBean != null) { // 存在多条Entity数据，则批量更新第一条以后的数据。
                updateOtherEntities(updateBean, entities);
            }

        } else {
            addEntity(servDef, dataBean, paramBean);
        }
    }
    
    /**
     * 批量更新同一个DataID对应的Entity第一条以后的数据。
     * @param commEntity 需要变更的值
     * @param list 数据ID
     */
    private static void updateOtherEntities(Bean commEntity, List<Bean> list) {
        if (list.size() <= 1) {
            return;
        }
        commEntity.remove("ENTITY_ID");
        commEntity.setId("");

        String[] entityIds = new String[list.size() - 1];
        for (int i = 1; i < list.size(); i++) { // 第一条以后的数据
            Bean entity = list.get(i);
            entityIds[i - 1] = entity.getId();
        }

        SqlBean whereBean = new SqlBean();
        whereBean.andIn("ENTITY_ID", entityIds);

        ServDao.updates(ServMgr.SY_COMM_ENTITY, commEntity, whereBean);
    }
    
    
    /**
     * 
     * @param servDef 服务定义Ben
     * @param dataBean 应用数据Bean
     * @param entityBean entity数据Bean
     * @param paramBean 参数Bean，可能有些参数DataBean中没有
     */
    private static void appendEntityData(ServDefBean servDef, Bean dataBean, Bean entityBean, ParamBean paramBean) {
        entityBean.set("TITLE", ServUtils.replaceValues(servDef.getDataTitle(), servDef.getId(), dataBean));
        String dataCode = servDef.getDataCode();
        if (dataCode.length() > 0) {
            entityBean.set("ENTITY_CODE", ServUtils.replaceValues(servDef.getDataCode(), servDef.getId(), dataBean));
        }
        if (null != dataBean.get("S_WF_NODE")) {
            entityBean.set("S_WF_NODE", dataBean.getStr("S_WF_NODE"));
            entityBean.set("S_WF_USER", dataBean.getStr("S_WF_USER"));
            if (paramBean != null && paramBean.get("S_WF_USER_STATE") != null) {
                entityBean.set("S_WF_USER_STATE", paramBean.getStr("S_WF_USER_STATE"));
            }
            entityBean.set("S_WF_STATE", dataBean.getStr("S_WF_STATE"));
        }

        if (dataBean.isNotEmpty("S_EMERGENCY")) {
            entityBean.set("S_EMERGENCY", dataBean.getStr("S_EMERGENCY"));
        }
        // 删除标记同步
        if (dataBean.contains("S_FLAG")) {
            entityBean.set("S_FLAG", dataBean.getStr("S_FLAG"));
        }
        // 设置有批示意见的机构CODE
        if (dataBean.contains("S_HAS_PS_MIND")) {
            entityBean.set("S_HAS_PS_MIND", dataBean.getStr("S_HAS_PS_MIND"));
        }
    }

    /**
     * 添加数据对应的实体信息表
     * @param servDef 服务定义
     * @param dataBean 数据信息
     * @param paramBean 参数Bean，可能有些参数dataBean
     * @return 新添加的实体信息
     */
    public static Bean addEntity(ServDefBean servDef, Bean dataBean, ParamBean paramBean) {
        Bean commEntity = new Bean();
        commEntity.set("SERV_ID", servDef.getId());
        commEntity.set("SERV_SRC_ID", servDef.getSrcId());
        commEntity.set("SERV_NAME", servDef.getName());
        commEntity.set("DATA_ID", dataBean.getId());
        commEntity.set("S_WF_INST", dataBean.getStr("S_WF_INST"));

        appendEntityData(servDef, dataBean, commEntity, paramBean);
        
        if (dataBean.isNotEmpty("S_ODEPT")) {  //如果有这个参数就不用取当前用户
            String odept = dataBean.getStr("S_ODEPT");
            commEntity.set("S_ODEPT", odept);
            commEntity.set("QUERY_ODEPT", odept);
        } else {
            UserBean bean = Context.getUserBean();
            if (bean != null) {
                commEntity.set("S_ODEPT", bean.getODeptCode());
                commEntity.set("QUERY_ODEPT", bean.getODeptCode());
            }
        }
        
        return ServDao.create("SY_COMM_ENTITY", commEntity);
    }    
    
    /**
     * 删除数据对应的实体信息表
     * @param servDef 服务定义
     * @param dataBean 数据信息
     * @param falseDel 假删除标志：true,false
     */
    public static void deleteEntity(ServDefBean servDef, Bean dataBean, boolean falseDel) {
        if (falseDel) { // 假删除：更新有效标志及删除人和机构
            Bean entityBean = getEntity(dataBean.getId());
            if (entityBean == null) { // 不存在则新建一个实体信息
                entityBean = addEntity(servDef, dataBean, null);
            } 
            setDelFlag(dataBean.getId());
        } else { // 真删除：删除实体信息
            ServDao.deletes(ServMgr.SY_COMM_ENTITY,
                    new Bean().set("SERV_SRC_ID", servDef.getSrcId()).set("DATA_ID", dataBean.getId()));
        }
    }    
    
    /**
     * 
     * @param dataId 设置删除标记
     */
    private static void setDelFlag(String dataId) {
        if (StringUtils.isEmpty(dataId)) {
            return;
        }
        
        Bean dataBean = new Bean();
        dataBean.set("S_FLAG", Constant.NO_INT);
        UserBean userBean = Context.getUserBean();
        if (userBean != null) {
            dataBean.set("DEL_USER", userBean.getCode())
                    .set("DEL_ODEPT", userBean.getODeptCode());
        }

        SqlBean where = new SqlBean();
        where.and("DATA_ID", dataId);
        ServDao.updates(ServMgr.SY_COMM_ENTITY, dataBean, where);
    }
    

    /**
     * 
     * @param dataId 数据ID
     * @return 与数据ID对应的Entity记录
     */
    public static Bean getEntity(String dataId) {
        List<Bean> list = findEntities(dataId);
        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }
    
    /**
     * 
     * @param dataId 数据ID
     * @return 返回数据对应的Entity列表对象
     */
    public static List<Bean> findEntities(String dataId) {
        Bean bean = new Bean();
        bean.set("DATA_ID", dataId);
        return ServDao.finds(ServMgr.SY_COMM_ENTITY, bean);
    }
    
    

    /**
     * 复制指定Entity 数据
     * @param dataId Entity 数据 Bean
     * @param odeptCode 所属机构Code
     */
    public static void copyEntity(String dataId, String odeptCode) {
        List<Bean> list = findEntities(dataId);
        
        if (list.size() == 0) { //没有找到对应Entity
            return;
        }

        if (existsEntity(list, odeptCode)) {  //是否指定机构存在Entity
            return;
        }

        Bean newEntity = list.get(0).copyOf();
        newEntity.setId("");
        newEntity.remove("ENTITY_ID");
        newEntity.remove(QUERY_ODEPT);
        newEntity.set(QUERY_ODEPT, odeptCode);
        ServDao.create(ServMgr.SY_COMM_ENTITY, newEntity);
    }
        
    /**
     * 
     * @param list 列表
     * @param odept 指定机构
     * @return 是否指定机构存在Entity对象
     */
    private static boolean existsEntity(List<Bean> list, String odept) {
        for (Bean bean : list) {
            if (bean.getStr(QUERY_ODEPT).equals(odept)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * 恢复Entity数据
     * @param dataId 指定表单数据ID
     */
    public static void restoreEntity(String dataId) {
        Bean dataBean = new Bean().set("S_FLAG", Constant.YES_INT).set("DEL_USER", "").set("DEL_ODEPT", "");

        update(dataId, dataBean);
    }

    /**
     * 更新指定数据ID的字段值
     * @param dataId 数据ID
     * @param updateBean 需要更新的字段及其Value
     */
    public static void update(String dataId, Bean updateBean) {
        SqlBean where = new SqlBean();
        where.and("DATA_ID", dataId);
        ServDao.updates(ServMgr.SY_COMM_ENTITY, updateBean, where);
    }
}
