package com.rh.core.org.auth.acl.mgr;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.org.auth.acl.AclBean;
import com.rh.core.org.mgr.RoleMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;

/**
 * 权限验证管理器
 * 
 * @author wanglong
 * 
 *         TODO 接缓存
 * 
 */
public class AclMgr {

    private static final String COL_SERV_ID = "SERV_ID";
    private static final String COL_ACT_CODE = "ACT_CODE";
    private static final String COL_ACL_TYPE = "ACL_TYPE";
    private static final String COL_ACL_OTYPE = "ACL_OTYPE";
    private static final String COL_ACL_OWNER = "ACL_OWNER";
    private static final String SERV_ACL = "SY_ORG_ACL";
    private static final int ACL_TYPE_SERV = 1;
    private static final int ACL_TYPE_MENU = 2;
    /** owner类型：1角色 */
    public static final int ACL_OTYPE_ROLE = 1;
    /** owner类型：2部门 */
    public static final int ACL_OTYPE_DEPT = 2;
    /** owner类型：3用户 */
    public static final int ACL_OTYPE_USER = 3;

    /**
     * 获取某服务的用户的操作列表
     * 
     * @param servId 服务编码
     * @param userCode 用户编码
     * @return true/false
     */
    private static List<AclBean> getActList(String servId, String userCode) {
        // 获取部门列表SQL
        StringBuilder condition = getAclCondition(userCode, ACL_TYPE_SERV);
        condition.append(" and " + COL_SERV_ID + "='" + servId + "'");

        return getAcls(condition);
    }

    /**
     * 获取某服务的用户有权限的操作列表
     * 
     * @param servId 服务ID
     * @param userCode 用户编码
     * @return 操作列表，字符串数据形式
     */
    public static String[] getActCodes(String servId, String userCode) {
        String[] actCodes;
        List<AclBean> aclBeanList = getActList(servId, userCode);
        if (aclBeanList != null) {
            actCodes = new String[aclBeanList.size()];
            for (int i = 0; i < aclBeanList.size(); i++) {
                actCodes[i] = aclBeanList.get(i).getActCode();
            }
        } else {
            actCodes = new String[0];
        }

        return actCodes;
    }

    /**
     * 获取权限查询条件
     * 
     * @param userCode 用户编码
     * @param aclType 权限控制类型
     * @return 权限查询条件
     */
    private static StringBuilder getAclCondition(String userCode, int aclType) {
        UserBean userBean = UserMgr.getUser(userCode);
        String deptCode = userBean.getDeptCode();
        String roleSelectStr = RoleMgr.getRoleListSql(userCode, userBean.getCmpyCode());

        StringBuilder condition = new StringBuilder(" and ((" + COL_ACL_OWNER + "='" + userCode + "'");
        condition.append(" and " + COL_ACL_OTYPE + "=" + ACL_OTYPE_USER + ")");
        if (deptCode.length() > 0) {
            condition.append(" or (" + COL_ACL_OWNER + "='" + deptCode + "'");
            condition.append(" and " + COL_ACL_OTYPE + "=" + ACL_OTYPE_DEPT + ")");
        }
        if (roleSelectStr.length() > 0) {
            condition.append(" or (" + COL_ACL_OWNER + " in (" + roleSelectStr + ")");
            condition.append(" and " + COL_ACL_OTYPE + "=" + ACL_OTYPE_ROLE + ")");
        }
        condition.append(")");
        condition.append(" and " + COL_ACL_TYPE + "=" + aclType);

        return condition;
    }

    /**
     * 判断用户是否具有某服务的指定操作的权限
     * 
     * @param servId 服务编码
     * @param actCode 操作编码
     * @param userCode 用户编码
     * @return true/false
     */
    @SuppressWarnings("unused")
    private static boolean ifActAuth(String servId, String actCode, String userCode) {
        StringBuilder condition;
        if (actCode != null) {
            condition = getAclCondition(userCode, ACL_TYPE_SERV);
            condition.append(" and " + COL_ACT_CODE + "='" + actCode + "'");
        } else {
            condition = getAclCondition(userCode, ACL_TYPE_MENU);
        }
        condition.append(" and " + COL_SERV_ID + "='" + servId + "'");
        ArrayList<AclBean> aclBeanList = getAcls(condition);
        if (aclBeanList != null && aclBeanList.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 获得部门的acl列表
     * 
     * @param deptCode 部门编码
     * @return ACL列表
     */
    public static List<AclBean> getDeptAclList(String deptCode) {
        return getAclList(null, deptCode, ACL_OTYPE_DEPT);
    }

    /**
     * 判断角色的ACL列表
     * @param cmpyCode 公司编码
     * @param roleCode 角色编码
     * @return ACL列表
     */
    public static List<AclBean> getRoleAclList(String cmpyCode, String roleCode) {
        return getAclList(cmpyCode, roleCode, ACL_OTYPE_ROLE);
    }

    /**
     * 获取用户的ACL列表
     * 
     * @param userCode 用户编码
     * @return ACL列表
     */
    public static List<AclBean> getUserAclList(String userCode) {
        return getAclList(null, userCode, ACL_OTYPE_USER);
    }

    /**
     * 获取ACL列表
     * @param cmpyCode 公司编码，为空则不过滤公司，一般只有角色（公共角色）授权需要用到公司
     * @param ocode 所有者编码
     * @param otype 所有者类型
     * @return ACL列表
     */
    public static List<AclBean> getAclList(String cmpyCode, String ocode, int otype) {
        StringBuilder con = getCondition(ocode, otype);
        if (cmpyCode != null && cmpyCode.length() > 0) {
            con.append(" and S_CMPY='").append(cmpyCode).append("'");
        }
        return getAcls(con);
    }

    /**
     * 得到SQL条件
     * 
     * @param ocode 所有者编码
     * @param otype 所有者类型
     * @return ACL列表
     */
    private static StringBuilder getCondition(String ocode, int otype) {
        StringBuilder condition = new StringBuilder(" and " + COL_ACL_OWNER + "='" + ocode + "'");
        condition.append(" and " + COL_ACL_OTYPE + "=" + otype);
        return condition;
    }

    /**
     * 根据查询条件获取ACL列表
     * 
     * @param condition 查询条件
     * @return ACL列表
     */
    private static ArrayList<AclBean> getAcls(StringBuilder condition) {
        ArrayList<AclBean> aclBeanList = new ArrayList<AclBean>();
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, condition.toString());
        List<Bean> beanList = ServDao.finds(SERV_ACL, paramBean);
        if (beanList != null) {
            for (Bean bean : beanList) {
                aclBeanList.add(new AclBean(bean));
            }
        }

        return aclBeanList;
    }

    /**
     * 删除部门的ACL列表，删除部门时调用
     * 
     * @param deptCode 部门编码
     */
    public static void delDeptAclList(String deptCode) {
        delAcls(getCondition(deptCode, ACL_OTYPE_DEPT));
    }

    /**
     * 删除角色的ACL列表，删除角色时调用
     * 
     * @param roleCode 角色编码
     */
    public static void delRoleAclList(String roleCode) {
        delAcls(getCondition(roleCode, ACL_OTYPE_ROLE));
    }

    /**
     * 删除用户的ACL列表，删除用户时调用
     * 
     * @param userCode 用户编码
     */
    public static void delUserAclList(String userCode) {
        delAcls(getCondition(userCode, ACL_OTYPE_USER));
    }

    /**
     * 删除ACL
     * 
     * @param condition 条件
     * @return 删除的数量
     */
    private static int delAcls(StringBuilder condition) {
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, condition.toString());
        int count = ServDao.destroys(SERV_ACL, paramBean);

        return count;
    }
}
