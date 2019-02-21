package com.rh.core.org.mgr;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.RoleBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;

/**
 * 角色管理器
 * 
 * @author wanglong
 * 
 */
public class RoleMgr {

    private static final String COL_S_FLAG = "S_FLAG";
    private static final String COL_USER_CODE = "USER_CODE";
    private static final String COL_CMPY_CODE = "CMPY_CODE";
    private static final String TBL_SY_ORG_ROLE_USER = "SY_ORG_ROLE_USER_V";
    private static final String COL_ROLE_CODE = "ROLE_CODE";

    /**
     * 取得角色Bean
     * 
     * @param roleCode 角色ID
     * @return 角色Bean对象
     */
    public static RoleBean getRole(String roleCode) {
        ParamBean paramBean = new ParamBean(ServMgr.SY_ORG_ROLE, ServMgr.ACT_BYID, roleCode);
        OutBean bean = ServMgr.act(paramBean);
//        if (!bean.isOk()) {
//            throw new TipException(Context.getSyMsg("SY_ROLE_NOT_FOUND", roleCode));
//        }
        return new RoleBean(bean);
    }

    /**
     * 获取查询角色的SQL
     * 
     * @param userCode 用户编码
     * @param cmpyCode 公司编码
     * @return 查询角色的SQL
     */
    public static String getRoleListSql(String userCode, String cmpyCode) {
        StringBuilder roleListSql = new StringBuilder("select distinct " + COL_ROLE_CODE + " from "
                + TBL_SY_ORG_ROLE_USER);
        roleListSql.append(" where " + COL_USER_CODE + "='" + userCode + "'");
        roleListSql.append(" and " + COL_CMPY_CODE + "='" + cmpyCode + "'");
        roleListSql.append(" and " + COL_S_FLAG + "=1");

        return roleListSql.toString();
    }

    /**
     * 
     * 取得指定用户的角色编码
     * @param userCode 用户编码
     * @param cmpyCode 公司编码
     * @param oDeptLevel 机构层级
     * @return 角色编码串
     */
    public static String getRoleCodesStr(String userCode, String cmpyCode, int oDeptLevel) {
        List<Bean> list = Context.getExecutor().query(getRoleListSql(userCode, cmpyCode));
        String roleStr = "";
        for (Bean bean : list) {
        	roleStr += bean.getStr(COL_ROLE_CODE) + ",";
        }

        if (roleStr.lastIndexOf(",") >= 0) {
        	roleStr = roleStr.substring(0, roleStr.length() - 1);
        }
        return roleStr;
    }

    /**
     * 
     * 取得指定用户的角色编码
     * @param userCode 用户编码
     * @param cmpyCode 公司编码
     * @param oDeptLevel 机构层级
     * @return 角色编码串
     */
    public static String[] getRoleCodes(String userCode, String cmpyCode, int oDeptLevel) {
        String publicRole = Context.getSyConf("SY_ORG_ROLE_PUBLIC", "");
        List<Bean> list = Context.getExecutor().query(getRoleListSql(userCode, cmpyCode));
        int size = list.size();
        if (publicRole.length() > 0) { //设置了公共角色
            size += 2;
        }
        String[] rtn = new String[size];
        int i = 0;
        for (Bean bean : list) {
            rtn[i] = bean.getStr(COL_ROLE_CODE);
            i++;
        }
        if (i == (size - 2)) {
            rtn[i] = publicRole;
            rtn[i + 1] = publicRole + oDeptLevel;
        }
        return rtn;
    }
    
    /**
     * 从指定角色中移除指定用户
     * @param userCode
     * @param roleCode
     */
    public static void removeUserOutRole(String userCode, String roleCode) {
        ParamBean paramBean = new ParamBean();
        paramBean.set("ROLE_CODE", roleCode);
        paramBean.set("USER_CODE", userCode);
        ServDao.delete(ServMgr.SY_ORG_ROLE_USER, paramBean);
    }
    
    /**
     * 向指定角色添加指定用户
     * @param userCode
     * @param roleCode
     */
    public static void addUserToRole(String userCode, String roleCode) {
        ParamBean paramBean = new ParamBean();
        paramBean.set("ROLE_CODE", roleCode);
        paramBean.set("USER_CODE", userCode);
        if (ServDao.count(ServMgr.SY_ORG_ROLE_USER, paramBean) == 0) {
            ServDao.create(ServMgr.SY_ORG_ROLE_USER, paramBean);
        }
    }
}
