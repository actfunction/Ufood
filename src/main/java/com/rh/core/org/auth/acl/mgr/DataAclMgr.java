package com.rh.core.org.auth.acl.mgr;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDao;

/**
 * 
 * @author wanglong
 *
 */
public class DataAclMgr {
    /** 数据权限授权服务 */
    private static final String DATA_ACL = "SY_SERV_DACL_ITEM";
    /** 数据权限定义服务 */
    private static final String DATA_DEF = "SY_SERV_DACL";
    /** log */
    private static Log log = LogFactory.getLog(DataAclMgr.class);
    
    /**
     * 通过SERV_ID、DATA_ID、ACL_TYPE获取权限实体，角色、部门、用户等
     * @param servId 服务ID
     * @param dataId 数据ID
     * @param aclType 权限类型
     * @return 返回所有数据权限
     */
    public static List<Bean> getDataAclList(String servId, String dataId, String aclType) {
        log.info("根据SERV_ID、DATA_ID、ACL_TYPE查询当前数据授权实体");
        Bean paramBean = new Bean();
        paramBean.set("SERV_ID", servId).set("DATA_ID", dataId).set("ACL_TYPE", aclType);
        List<Bean> beans = ServDao.finds(DATA_ACL, paramBean);
        return beans;
    }
    
    /**
     * 将where语句中的数据权限定义替换为实际的过滤SQL
     * @param whereSql where语句
     * @return 替换后的where语句
     */
    public static String replaceDataAcl(String whereSql) {
        String pn = "@@(.+)@(.+)@@";
        Pattern pattern = Pattern.compile(pn);
        Matcher mt = pattern.matcher(whereSql);
        while (mt.find()) {
            whereSql = whereSql.replaceAll(mt.group(0), getDataAclSql(mt.group(1), mt.group(2)));
        }
        return whereSql;
    }
    
    /**
     * 根据数据权限编码生成数据权限过滤SQL
     * @param itemCode 需要被过滤数据权限的表实际数据库字段名
     * @param defCode 数据权限定义编码
     * @return 数据权限过滤SQL
     */
    public static String getDataAclSql(String itemCode, String defCode) {
        StringBuilder where = new StringBuilder();
        UserBean userBean = Context.getUserBean();
        if (userBean != null && !userBean.isAdminRole()) { //系统管理角色缺省不判断数据权限
            Bean dataDef = ServDao.find(DATA_DEF, defCode); //查询数据权限定义
            if (dataDef != null) {
                where.append(" and ").append(itemCode).append(" in (")
                    .append("select DATA_ID from  SY_SERV_DACL_ITEM where SERV_ID='")
                    .append(dataDef.getStr("SERV_ID")).append("' and ACL_TYPE='")
                    .append(defCode).append("' and ACL_OWNER in (").append(userBean.getDeptRoleUserStr())
                    .append("))");
            } else {
              //  throw new RuntimeException(Context.getSyMsg("", defCode));
                throw new RuntimeException("error acl def code:" + defCode);
            }
        }
        return where.toString();
    }
}