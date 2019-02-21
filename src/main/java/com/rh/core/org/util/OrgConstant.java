package com.rh.core.org.util;

/**
 * 组织机构常量
 * 
 * @author wanglong
 */
public final class OrgConstant {
   
    /** ----------------------部门模块--------------------------------------- **/
    /** 部门类型：部门 */
    public static final int DEPT_TYPE_DEPT = 1;
    /** 部门类型：机构 */
    public static final int DEPT_TYPE_ORG = 2;
    /**
     * 公共角色code值
     */
    public static final String PUBLIC_ROLE_CODE = "RPUB";
    /**
     * 委托人用户CODE；原始的参数名为ORIGINAL_USER，现在改为_AGENT_USER_
     */
    public static final String AGENT_USER = "_AGENT_USER_";
    /**
     * 委托人用户Bean；
     */
    public static final String AGENT_USER_BEAN = "_AGENT_USER_BEAN_";

    /**
     * 私有构建体方法
     */
    private OrgConstant() {
    }


}
