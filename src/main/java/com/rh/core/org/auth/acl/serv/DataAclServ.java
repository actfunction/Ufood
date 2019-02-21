package com.rh.core.org.auth.acl.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.auth.acl.mgr.DataAclMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 数据权限服务类
 * 
 * @author wanglong
 * 
 */
public class DataAclServ extends CommonServ {
    
    private static final String COL_SERV_ID = "SERV_ID";
    private static final String COL_DATA_ID = "DATA_ID";
    private static final String COL_ACL_TYPE = "ACL_TYPE";
    private static final String SERV_ACL = "SY_SERV_DACL_ITEM";
    private static final String COL_ACL_OWNER = "ACL_OWNER";
    /**
     * 根据DATA_ID、SERV_ID、ACL_TYPE，查出哪些实体具有该条数据的操作权限
     * @param paramBean 参数
     * @return 返回所有数据权限
     */
    public Bean show(Bean paramBean) {
        Bean outBean = new Bean();
        String servId = (String) paramBean.get(COL_SERV_ID);
        String dataId = (String) paramBean.get(COL_DATA_ID);
        String aclType = (String) paramBean.get(COL_ACL_TYPE);
        
        String[] aclTypeArr = new String[]{};
        if (aclType != null) {
            aclTypeArr = aclType.split(",");
        }
        
        int len = aclTypeArr.length;
        for (int i = 0; i < len; i++) {
            outBean.set(aclTypeArr[i], DataAclMgr.getDataAclList(servId, dataId, aclTypeArr[i]));
        }
        return outBean;
    }
    /**
     * 获取ACL列表
     * @param ocode 所有者编码
     * @param aclType 所有者类型
     * @return COMS列表
     */
    public static List<Bean> getAclList(String ocode, String aclType) {
        StringBuilder con = getCondition(ocode, aclType);
        return getAcls(con);
    }
    /**
     * 得到SQL条件
     * 
     * @param ocode 所有者编码
     * @param aclType 所有者类型
     * @return COMS列表
     */
    private static StringBuilder getCondition(String ocode, String aclType) {
        StringBuilder condition = new StringBuilder(" and " + COL_ACL_OWNER + "='" + ocode + "'");
        condition.append(" and " + COL_ACL_TYPE + "='" + aclType + "'");
        return condition;
    }
    /**
     * 根据查询条件获取COMS列表
     * @param condition 查询条件
     * @return COMS列表
     */
    private static ArrayList<Bean> getAcls(StringBuilder condition) {
        ArrayList<Bean> aclBeanList = new ArrayList<Bean>();
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, condition.toString());
        List<Bean> beanList = ServDao.finds(SERV_ACL, paramBean);
        if (beanList != null) {
            for (Bean bean : beanList) {
                aclBeanList.add(bean);
            }
        }
        return aclBeanList;
    }
    
}
