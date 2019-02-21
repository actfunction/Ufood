package com.rh.core.org;

import com.rh.core.base.Bean;

/**
 * 角色Bean
 * 
 * @author wanglong
 * 
 */
public class RoleBean extends Bean {
    /** serialVersionUID */
    private static final long serialVersionUID = -8496321546219287716L;

    /**
     * 私有化构造方法
     */
    @SuppressWarnings("unused")
    private RoleBean() {
    }

    /**
     * 对象构造方法
     * 
     * @param roleBean 数据对象
     */
    public RoleBean(Bean roleBean) {
        super(roleBean);
        this.setId(roleBean.getId());
    }

    /**
     * 获取角色编码
     * 
     * @return 角色编码
     */
    public String getCode() {
        return this.getStr("ROLE_CODE");
    }

    /**
     * 获取角色名称
     * 
     * @return 角色名称
     */
    public String getName() {
        return this.getStr("ROLE_NAME");
    }

    /**
     * 获取角色排序号
     * 
     * @return 角色排序号
     */
    public int getSort() {
        return this.get("ROLE_SORT", 0);
    }

    /**
     * 获取角色描述
     * 
     * @return 角色描述
     */
    public String getMemo() {
        return this.getStr("ROLE_MEMO");
    }

    /**
     * 获取角色分类
     * 
     * @return 角色分类
     */
    public String getCatalog() {
        return this.getStr("ROLE_CATALOG");
    }

    /**
     * 获取是否公共角色
     * 
     * @return 是否公共角色
     */
    public int getsPublic() {
        return this.get("S_PUBLIC", 0);
    }

    /**
     * 获取启用标志
     * 
     * @return 启用标志
     */
    public int getsFlag() {
        return this.get("S_FLAG", 0);
    }

    /**
     * 获取公司Code
     * 
     * @return 公司Code
     */
    public String getCmpyCode() {
        return this.getStr("CMPY_CODE");
    }

    /**
     * 获取添加者
     * 
     * @return 添加者
     */
    public String getsUser() {
        return this.getStr("S_USER");
    }

    /**
     * 获取更新时间
     * 
     * @return 更新时间
     */
    public String getsMtime() {
        return this.getStr("S_MTIME");
    }

}
