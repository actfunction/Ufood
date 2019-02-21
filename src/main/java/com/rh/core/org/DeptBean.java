package com.rh.core.org;

import com.rh.core.base.Bean;
import com.rh.core.org.mgr.OrgMgr;

/**
 * 部门Bean
 * 
 * @author wanglong
 * 
 */
public class DeptBean extends Bean {

    /** serialVersionUID */
    private static final long serialVersionUID = 4007480160344445867L;

    /**
     * 私有化构造方法
     */
    @SuppressWarnings("unused")
    private DeptBean() {
    }

    /**
     * 对象构造方法
     * 
     * @param deptBean 数据对象
     */
    public DeptBean(Bean deptBean) {
        super(deptBean);
        this.setId(deptBean.getId());
    }

    /**
     * 部门编码
     * 
     * @return 部门编码
     */
    public String getCode() {
        return this.getStr("DEPT_CODE");
    }

    /**
     * 部门名称
     * 
     * @return 部门名称
     */
    public String getName() {
        return this.getStr("DEPT_NAME");
    }
    
    /**
     * 部门简称
     * 
     * @return 部门简称
     */
    public String getShortName() {
        return this.getStr("DEPT_SHORT_NAME");
    }
    
    /**
     * 部门全称
     * 
     * @return 部门全称
     */
    public String getFullName() {
        return this.getStr("DEPT_FULL_NAME");
    }
    
    
    /**
     * 机构部门编码
     * 
     * @return 机构部门编码
     */
    public String getODeptCode() {
        return this.getStr("ODEPT_CODE");
    }
    
    /**
     * 有效部门编码
     * 
     * @return 有效部门编码
     */
    public String getTDeptCode() {
        return this.getStr("TDEPT_CODE");
    }
    
    
    /**
     * @return 取得带层级的，完整的部门名称。格式为：处室^部门^机构
     */
    public String getFullDeptNames() {
        return OrgMgr.getDeptNames(this);
    }

    /**
     * 上级部门ID
     * 
     * @return 上级部门ID
     */
    public String getPcode() {
        return this.getStr("DEPT_PCODE");
    }

    /**
     * 部门排序号
     * 
     * @return 部门排序号
     */
    public int getSort() {
        return this.get("DEPT_SORT", 0);
    }
    
    /**
     * 部门类型：1：普通部门；2：机构
     * @return 部门类型：1：普通部门；2：机构
     */
    public int getType() {
    	return this.get("DEPT_TYPE", 1);
    }

    /**
     * 部门描述
     * 
     * @return 部门描述
     */
    public String getMemo() {
        return this.getStr("DEPT_MEMO");
    }

    /**
     * 部门邮箱
     * 
     * @return 部门邮箱
     */
    public String getEmail() {
        return this.getStr("DEPT_EMAIL");
    }

    /**
     * 启用标志
     * @return 启用标志
     */
    public int getsFlag() {
        return this.get("S_FLAG", 0);
    }

    /**
     * 公司ID
     * @return 公司ID
     */
    public String getCmpyCode() {
        return this.getStr("CMPY_CODE");
    }

    /**
     * 添加者
     * 
     * @return 添加者
     */
    public String getsUser() {
        return this.getStr("S_USER");
    }

    /**
     * 更新时间
     * 
     * @return 更新时间
     */
    public String getsMtime() {
        return this.getStr("S_MTIME");
    }

    /**
     * 取得编码路径
     * 
     * @return 编码路径
     */
    public String getCodePath() {
        return this.getStr("CODE_PATH");
    }

    /**
     * 取得公司层级
     * 
     * @return 公司层级
     */
    public int getLevel() {
        return this.get("DEPT_LEVEL", 0);
    }

    /**
     * 取得部门原始类型
     * 
     * @return 部门原始类型
     */
    public String getSrcType1() {
        return this.getStr("DEPT_SRC_TYPE1");
    }
    
    /**
     * 取得部门原始类型
     * 
     * @return 部门原始类型
     */
    public String getSrcType2() {
        return this.getStr("DEPT_SRC_TYPE2");
    }
    
    /**
     * 取得部门业务条线
     * 
     * @return 部门业务条线
     */
    public String getLine() {
        return this.getStr("DEPT_LINE");
    }
    
    /**
     * 获取机构部门
     * 
     * @return 机构部门
     */ 
    public DeptBean getODeptBean() {
        String odeptCode = getODeptCode();
        if (odeptCode.equals(getCode())) { //自身为机构部门
            return this;
        } else {
            return OrgMgr.getDept(odeptCode);
        }
    }
    
    /**
     * 获取有效部门
     * 
     * @return 有效部门
     */ 
    public DeptBean getTDeptBean() {
        String tdeptCode = getTDeptCode();
        if (tdeptCode.equals(getCode())) { //自身为有效部门
            return this;
        } else {
            return OrgMgr.getDept(tdeptCode);
        }
    }
    
    /**
     * 
     * @return 父部门对象。如果父部门不存在，则返回null。
     */
    public DeptBean getParentDeptBean() {
        if (this.isEmpty("DEPT_PCODE")) {
            return null;
        }

        return OrgMgr.getDept(this.getStr("DEPT_PCODE"));
    }
    
    /**
     * 生成排序号路径，用于比较不同父部门的多个部门的排列顺序。
     * @return 按照由小到大层级顺序，把每一级的排序号合并成一个排序号路径。每一级的排序号占2位数字，不够2位在前面补0。
     */
    public String getSortPath() {        
        DeptBean parent = this.getParentDeptBean(); 
        if (parent == null) {
            return formartSortVal();
        } else {
            return (parent.getSortPath() + "^" + formartSortVal());
        }
    }
    
    /**
     * 
     * @return 取得格式化成2个数字的排序值
     */
    private String formartSortVal() {
        String sortVal = this.getStr("DEPT_SORT");
        int len = sortVal.length();

        if (len > 2) {
            return "99";
        } else if (len == 2) {
            return sortVal;
        } else if (len == 1) {
            return "0" + sortVal;
        } else {
            return "00";
        }
    }
}
