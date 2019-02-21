package com.rh.core.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;

/**
 * 服务项定义类
 * 
 * @author wanglong
 */
public class ServDefItem extends CommonServ {
    /** 服务主键 */
    public static final String SERV_ID_SERV_ITEM = "SY_SERV_ITEM";
    
    /**
     * 修改服务项之前处理
     * 
     * @param paramBean 参数Bean
     */
    protected void beforeSave(ParamBean paramBean) {
        //处理字典的获取
        boolean dictFlag = false;
        Bean oldBean = paramBean.getSaveOldData();
        Bean dataBean = paramBean.getSaveFullData();
        if (dataBean.getInt("ITEM_INPUT_TYPE") == ServConstant.ITEM_INPUT_TYPE_CHECKBOX
            || dataBean.getInt("ITEM_INPUT_TYPE") == ServConstant.ITEM_INPUT_TYPE_RADIO
            || dataBean.getInt("ITEM_INPUT_TYPE") == ServConstant.ITEM_INPUT_TYPE_SELECT) {
            dictFlag = true;
        } else if (dataBean.getInt("ITEM_INPUT_MODE") == ServConstant.ITEM_INPUT_MODE_DICT) {
            dictFlag = true;
        }
        if (dictFlag) { //需要修改字典项
            String dictCode = dataBean.getStr("ITEM_INPUT_CONFIG");
            if (dictCode.length() > 0) {
                int pos = dictCode.indexOf(Constant.SEPARATOR);
                if (pos > 0) {
                    dictCode = dictCode.substring(0, pos);
                }
            }
            paramBean.set("DICT_ID", dictCode);
        } else { //非字典模式，清除dictCode
            paramBean.set("DICT_ID", "");
        }
        
        if (paramBean.contains("ITEM_LIST_FLAG")) { //调整列表显示自动调整对应的高级查询
            if (paramBean.getInt("ITEM_LIST_FLAG") == ServConstant.ITEM_LIST_FLAG_NO) {
                if (oldBean.getInt("ITEM_SEARCH_FLAG") == Constant.YES_INT) {
                    paramBean.set("ITEM_SEARCH_FLAG", Constant.NO_INT);
                }
            } else {
                if (oldBean.getInt("ITEM_SEARCH_FLAG") != Constant.YES_INT) {
                    paramBean.set("ITEM_SEARCH_FLAG", Constant.YES_INT);
                }
            }
        }
    }
    
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
     * 删除服务项定义后清除cache
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
}
