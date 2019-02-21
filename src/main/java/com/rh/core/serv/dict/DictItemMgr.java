package com.rh.core.serv.dict;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 字典项管理器
 * 
 * @author wanglong
 * 
 */
public class DictItemMgr {

    /**
     * 字典项层级字段
     */
    protected static final String COL_ITEM_LEVEL = "ITEM_LEVEL";
    /**
     * 父字典项编码字段
     */
    protected static final String COL_ITEM_PCODE = "ITEM_PCODE";
    /**
     * 字典项状态字段
     */
    protected static final String COL_ITEM_FLAG = "ITEM_FLAG";
    /**
     * 字典项编码字段
     */
    protected static final String COL_ITEM_CODE = "ITEM_CODE";
    /**
     * 编码路径字段
     */
    protected static final String COL_CODE_PATH = "CODE_PATH";
    /**
     * 字典ID字段
     */
    protected static final String COL_DICT_ID = "DICT_ID";
    /** 服务主键 */
    protected static final String SERV_DICT_ITEM = "SY_SERV_DICT_ITEM";

    /**
     * 获得字典项
     * 
     * @param itemCode 字典项编码
     * @param dictId 字典ID
     * @return 字典项
     */
    private static Bean getDictItem(String itemCode, String dictId) {
        Bean paramBean = new Bean();
        paramBean.set(COL_ITEM_CODE, itemCode).set(COL_DICT_ID, dictId).set(COL_ITEM_FLAG, 1);
        return ServDao.find(SERV_DICT_ITEM, paramBean);
    }

    /**
     * 更新子字典项
     * 
     * @param oldBean 旧字典项对象
     * @param newBean 新字典项对象
     */
    protected static void updateChildItems(Bean oldBean, Bean newBean) {
        StringBuilder condition = new StringBuilder(" and " + COL_ITEM_FLAG + "=1");
        condition.append(" and " + COL_CODE_PATH + " like '").append(oldBean.getStr(COL_CODE_PATH))
                .append(Constant.CODE_PATH_SEPERATOR + "%'").append(" and " + COL_DICT_ID + "='")
                .append(oldBean.getStr(COL_DICT_ID)).append("'");
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, condition.toString());
        List<Bean> beanList = ServDao.finds(SERV_DICT_ITEM, paramBean);
        for (Bean cmpyBean : beanList) {
            cmpyBean.set(COL_ITEM_PCODE, newBean.get(COL_ITEM_CODE));
            cmpyBean.set(COL_ITEM_LEVEL, newBean.get(COL_ITEM_LEVEL, 0) + 1);
            cmpyBean.set(
                    COL_CODE_PATH,
                    cmpyBean.getStr(COL_CODE_PATH).replace(oldBean.getStr(COL_CODE_PATH),
                            newBean.getStr(COL_CODE_PATH)));
            ServDao.update(SERV_DICT_ITEM, cmpyBean);
        }
    }

    /**
     * 判断两个字典项是否具有继承关系
     * 
     * @param parentCode 上级字典项编码
     * @param itemCode 字典项编码
     * @param dictId 字典ID
     * @return true/false
     */
    protected static boolean isSuperiorItem(String parentCode, String itemCode, String dictId) {
        Bean dictItemBean = getDictItem(itemCode, dictId);
        if (dictItemBean == null) {
            return false;
        }
        String itemCodePath = Constant.CODE_PATH_SEPERATOR + dictItemBean.getStr(COL_CODE_PATH);
        String parentCodeWithSeperator = Constant.CODE_PATH_SEPERATOR + parentCode
                + Constant.CODE_PATH_SEPERATOR;
        return (itemCodePath.indexOf(parentCodeWithSeperator) >= 0 ? true : false);
    }

    /**
     * 修改CODE_PATH和ITEM_LEVEL信息
     * 
     * @param outBean 传出的参数
     */
    protected static void updateCodePathAndLevel(Bean outBean) {
        if (outBean.getStr(COL_ITEM_PCODE).length() > 0) {
            Bean parentItem = getDictItem(outBean.getStr(COL_ITEM_PCODE), outBean.getStr(COL_DICT_ID));
            if (parentItem != null) {
                outBean.set(COL_CODE_PATH,
                        parentItem.getStr(COL_CODE_PATH) + outBean.getStr(COL_ITEM_CODE)
                            + Constant.CODE_PATH_SEPERATOR);
                outBean.set(COL_ITEM_LEVEL, parentItem.get(COL_ITEM_LEVEL, 0) + 1);
            }
        } else {
            outBean.set(COL_CODE_PATH, outBean.getStr(COL_ITEM_CODE) + Constant.CODE_PATH_SEPERATOR);
            outBean.set(COL_ITEM_LEVEL, 1);
        }

        ServDao.update(SERV_DICT_ITEM, outBean);
    }
}
