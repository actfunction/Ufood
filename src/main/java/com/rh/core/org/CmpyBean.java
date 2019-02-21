package com.rh.core.org;

import com.rh.core.base.Bean;

/**
 * 公司Bean
 * 
 * @author wanglong
 * 
 */
public class CmpyBean extends Bean {

    /** serialVersionUID */
    private static final long serialVersionUID = -7773373747106255609L;

    /**
     * 私有化构造方法
     */
    @SuppressWarnings("unused")
    private CmpyBean() {

    }

    /**
     * 对象构造方法
     * 
     * @param cmpybean 数据对象
     */
    public CmpyBean(Bean cmpybean) {
        super(cmpybean);
    }

    /**
     * 公司编码
     * 
     * @return 公司编码
     */
    public String getCode() {
        return this.getStr("CMPY_CODE");
    }

    /**
     * 公司名称
     * 
     * @return 公司名称
     */
    public String getName() {
        return this.getStr("CMPY_NAME");
    }

    /**
     * 公司全称
     * 
     * @return 公司全称
     */
    public String getFullname() {
        return this.getStr("CMPY_FULLNAME");
    }

    /**
     * 所在国家
     * 
     * @return 所在国家
     */
    public String getCountry() {
        return this.getStr("CMPY_COUNTRY");
    }

    /**
     * 所在省份
     * 
     * @return 所在省份
     */
    public String getProvince() {
        return this.getStr("CMPY_PROVINCE");
    }

    /**
     * 所在城市
     * 
     * @return 所在城市
     */
    public String getCity() {
        return this.getStr("CMPY_CITY");
    }

    /**
     * 公司地址
     * 
     * @return 公司地址
     */
    public String getAddress() {
        return this.getStr("CMPY_ADDRESS");
    }

    /**
     * 公司邮编
     * 
     * @return 公司邮编
     */
    public String getPostcode() {
        return this.getStr("CMPY_POSTCODE");
    }

    /**
     * 公司电话
     * 
     * @return 公司电话
     */
    public String getPhone() {
        return this.getStr("CMPY_PHONE");
    }

    /**
     * 公司传真
     * 
     * @return 公司传真
     */
    public String getFax() {
        return this.getStr("CMPY_FAX");
    }

    /**
     * 联系人
     * 
     * @return 联系人
     */
    public String getContactor() {
        return this.getStr("CMPY_CONTACTOR");
    }

    /**
     * 上级公司Code
     * 
     * @return 上级公司Code
     */
    public String getPcode() {
        return this.getStr("CMPY_PCODE");
    }

    /**
     * 公司排序
     * 
     * @return 公司排序
     */
    public int getSort() {
        return this.get("CMPY_SORT", 0);
    }

    /**
     * 公司描述
     * 
     * @return 公司描述
     */
    public String getDesc() {
        return this.getStr("CMPY_DESC");
    }

    /**
     * 公司级别
     * 
     * @return 公司级别
     */
    public int getLevel() {
        return this.get("CMPY_LEVEL", 0);
    }

    /**
     * 启用标志
     * 
     * @return 启用标志
     */
    public int getsFlag() {
        return this.get("S_FLAG", 0);
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
}
