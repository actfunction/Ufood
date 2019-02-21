package com.rh.core.org.auth.acl;

import com.rh.core.base.Bean;

/**
 * 权限控制Bean
 * 
 * @author wanglong
 * 
 */
public class AclBean {

    private Bean bean = null;

    /**
     * 私有化构造方法
     */
    @SuppressWarnings("unused")
    private AclBean() {

    }

    /**
     * 构造方法
     * 
     * @param aclBean 数据对象
     */
    public AclBean(Bean aclBean) {
        this.bean = aclBean;
    }

    /**
     * 获取权限控制ID
     * 
     * @return 权限控制ID
     */

    public String getId() {
        return this.bean.getStr("ACL_ID");
    }

    /**
     * 获取服务ID或菜单编码
     * 
     * @return 服务ID或菜单编码
     */
    public String getServId() {
        return this.bean.getStr("SERV_ID");
    }

    /**
     * 获取操作编码
     * 
     * @return 操作编码
     */
    public String getActCode() {
        return this.bean.getStr("ACT_CODE");
    }

    /**
     * 获取权限控制类型
     * 
     * @return 权限控制类型
     */
    public int getType() {
        return this.bean.get("ACL_TYPE", 0);
    }

    /**
     * 获取权限所有者编码
     * 
     * @return 权限所有者编码
     */
    public String getOcode() {
        return this.bean.getStr("ACL_OWNER");
    }

    /**
     * 获取权限所有者类型
     * 
     * @return 权限所有者类型
     */
    public int getOtype() {
        return this.bean.get("ACL_OTYPE", 0);
    }

    /**
     * 获取公司编码
     * 
     * @return 公司编码
     */
    public String getsCmpy() {
        return this.bean.getStr("S_CMPY");
    }
}
