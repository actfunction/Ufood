package com.rh.core.serv.dict;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.Constant;

/**
 * 字典项服务类
 * 
 * @author wanglong
 * 
 */
public class DictItemServ extends CommonServ {
    

    /**
     * 查询之前的拦截方法，由子类重载
     * @param paramBean 参数信息
     */
    protected void beforeQuery(ParamBean paramBean) {
        if (paramBean.contains("DICT_ID")) {
            paramBean.set("_extWhere", " and DICT_ID='" + paramBean.getStr("DICT_ID") + "'");
        }
    }

    /**
     * 修改字典项后清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        if (outBean.isOk()) {
            update(paramBean, outBean);
            if (!outBean.isEmpty(DictItemMgr.COL_DICT_ID) 
                    && !paramBean.getBoolean(Constant.IS_LINK_ACT)) { //非级联添加才更新字典定义的更新时间
                String dictId = outBean.getStr(DictItemMgr.COL_DICT_ID);
                Bean dict = DictMgr.getDict(dictId);
                if (dict.getInt("DICT_IS_INNER") == Constant.YES_INT) { //内部字典更新主信息及文件
                    DictMgr.udpateMtime(dictId, outBean.getStr("S_MTIME"));
                } else { //外部字典，仅更新缓存
                    DictMgr.clearAllCache(dictId, false);
                }
            }
        }
    }
    
    /**
     * 删除字典项项定义之前清除cache
     * 
     * @param paramBean 参数Bean
     * @param outBean 输出信息
     */
    protected void afterDelete(ParamBean paramBean, OutBean outBean) {
        if (!paramBean.getBoolean(Constant.IS_LINK_ACT)) { //非级联删除模式才更新json文件
            List<Bean> dataList = outBean.getDataList();
            if (dataList.size() > 0) {
                Bean dataBean = dataList.get(0);
                if (dataBean != null) {
                    String dictId = dataBean.getStr(DictItemMgr.COL_DICT_ID);
                    Bean dict = DictMgr.getDict(dictId);
                    if (dict != null && dict.getInt("DICT_IS_INNER") == Constant.YES_INT) { //内部字典更新主信息及文件
                        DictMgr.udpateMtime(dictId, null);
                    } else { //外部字典，仅更新缓存
                        DictMgr.clearAllCache(dictId, false);
                    }
                }
            }
        }
    }

    /**
     * 更新字典项数据
     * 
     * @param paramBean 传入的参数
     * @param outBean 传出的参数
     */
    private void update(ParamBean paramBean, OutBean outBean) {
        //判断字典类型是否为：列表，如果是列表，不进行以下修改。
        Bean dict = DictMgr.getDictDef(outBean.getStr(DictItemMgr.COL_DICT_ID));
        if (dict == null) {
            return;
        }
        if (dict.get(DictMgr.COL_DICT_TYPE, DictMgr.DIC_TYPE_LIST) == DictMgr.DIC_TYPE_LIST) {
            return;
        }
        //判断是否为新建的字典项，如果是，则只修改自己的code_path和item_level
        if (paramBean.getId().length() <= 0) {
            DictItemMgr.updateCodePathAndLevel(outBean);
            return;
        }
        Bean oldBean = paramBean.getSaveOldData();
        if (!oldBean.getStr(DictItemMgr.COL_ITEM_PCODE).equals(outBean.getStr(DictItemMgr.COL_ITEM_PCODE))) {
            if (DictItemMgr.isSuperiorItem(outBean.getStr(DictItemMgr.COL_ITEM_CODE),
                    outBean.getStr(DictItemMgr.COL_ITEM_PCODE), outBean.getStr(DictItemMgr.COL_DICT_ID))) {
                throw new RuntimeException("不能将下级字典项设置为上级字典项.");
            }
        }
        if (!oldBean.getStr(DictItemMgr.COL_ITEM_CODE).equals(outBean.getStr(DictItemMgr.COL_ITEM_CODE))
                || !oldBean.getStr(DictItemMgr.COL_ITEM_PCODE).equals(outBean.getStr(DictItemMgr.COL_ITEM_PCODE))) {
            DictItemMgr.updateCodePathAndLevel(outBean);
            // 根据旧的CODE_PATH取所有的子孙
            DictItemMgr.updateChildItems(oldBean, outBean);
        }
    }
}
