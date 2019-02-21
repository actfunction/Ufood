package com.rh.core.org;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.RowHandler;
import com.rh.core.comm.CacheMgr;
import com.rh.core.comm.FileMgr;
import com.rh.core.org.mgr.OrgMgr;
import com.rh.core.org.mgr.RoleMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;

/**
 * 用户Bean
 * 
 * @author wanglong
 * 
 */
public class UserBean extends Bean {

    /** serialVersionUID */
    private static final long serialVersionUID = -8344706614378598099L;

    /** 角色列表 */
    private static final String ROLE_CODES = "ROLE_CODES";
    /** 群组列表 */
    private static final String GROUP_CODES = "GROUP_CODES";
    /** 管理群组列表 */
    private static final String ADMIN_GROUP_CODES = "ADMIN_GROUP_CODES";
    /** 用户扩展信息缓存键值 */
    private static final String CACHE_USER_EXT = "_CACHE_SY_ORG_USER_EXT";

    private DeptBean oDeptBean = null;
    private DeptBean tDeptBean = null;
    private DeptBean deptBean = null;
    private CmpyBean cmpyBean = null;

    /**
     * 构造方法
     */
    @SuppressWarnings("unused")
    private UserBean() {
    }

    /**
     * 对象构造方法
     * 
     * @param userBean 数据对象
     */
    public UserBean(Bean userBean) {
        super(userBean);
        // 初始化用户头像信息
        String userImg = UserMgr.getUserImg(this.getImgSrc(), this.getSex(), this.getStr("S_MTIME"));
        this.set("USER_IMG", userImg);
    }

    /**
     * 获取用户编码
     * 
     * @return 用户编码
     */
    public String getCode() {
        return this.getStr("USER_CODE");
    }

    /**
     * 获取用户系统登录名
     * 
     * @return 用户系统登录名
     */
    public String getLoginName() {
        return this.getStr("USER_LOGIN_NAME");
    }

    /**
     * 获取用户名称
     * 
     * @return 用户名称
     */
    public String getName() {
        return this.getStr("USER_NAME");
    }

    /**
     * 获取部门编码（处室）
     * @return 部门编码（处室）
     */
    public String getDeptCode() {
        return this.getStr("DEPT_CODE");
    }

    /**
     * 获取有效部门编码
     * 
     * @return 有效部门编码
     */
    public String getTDeptCode() {
        return this.getStr("TDEPT_CODE");
    }

    /**
     * 获取机构部门编码
     * @return 机构部门编码
     */
    public String getODeptCode() {
        return this.getStr("ODEPT_CODE");
    }

    /**
     * 获取部门层级码
     * @return 机构部门层级码
     */
    public String getCodePath() {
        return this.getStr("CODE_PATH");
    }

    /**
     * 获取办公电话
     * 
     * @return 办公电话
     */
    public String getOfficePhone() {
        return this.getStr("USER_OFFICE_PHONE");
    }

    /**
     * 获取家庭电话
     * 
     * @return 家庭电话
     */
    public String getHomePhone() {
        return this.getStr("USER_HOME_PHONE");
    }

    /**
     * 获取手机号码
     * 
     * @return 手机号码
     */
    public String getMobile() {
        return this.getStr("USER_MOBILE");
    }

    /**
     * 获取用户QQ
     * 
     * @return 用户QQ
     */
    public String getQq() {
        return this.getStr("USER_QQ");
    }

    /**
     * 获取用户邮箱
     * 
     * @return 用户邮箱
     */
    public String getEmail() {
        return this.getStr("USER_EMAIL");
    }

    /**
     * 获取用户职务 / 身份
     * 
     * @return 用户邮箱
     */
    public String getPost() {
        return this.getStr("USER_POST");
    }

    /**
     * 获取用户 职务级别
     * 
     * @return 用户邮箱
     */
    public int getPostLevel() {
        return this.get("USER_POST_LEVEL", 0);
    }

    /**
     * 获取用户密码
     * 
     * @return 用户密码
     */
    public String getPassword() {
        return this.getStr("USER_PASSWORD");
    }

    /**
     * 获取用户性别
     * 
     * @return 用户性别
     */
    public int getSex() {
        return this.get("USER_SEX", 0);
    }

    /**
     * 获取出生日期
     * 
     * @return 出生日期
     */
    public String getBirthday() {
        return this.getStr("USER_BIRTHDAY");
    }

    /**
     * 获取民族
     * 
     * @return 民族
     */
    public String getNation() {
        return this.getStr("USER_NATION");
    }

    /**
     * 获取身份证号
     * 
     * @return 身份证号
     */
    public String getIdcard() {
        return this.getStr("USER_IDCARD");
    }

    /**
     * 获取委托代理标志（只有在线用户有此属性）
     * 
     * @return 委托代理标志
     */
    public int getCurrentAgtFlag() {
        Bean userState = Context.getOnlineUserState();
        if (userState != null) {
            return userState.getInt("USER_AGT_FLAG");
        } else {
            return 0;
        }
    }

    /**
     * 获取被委托用户编码，多个逗号分隔（只有在线用户有此属性）
     * 
     * @return 被委托用户编码
     */
    public String getCurrentSubCodes() {
        Bean userState = Context.getOnlineUserState();
        if (userState != null) {
            return userState.getStr("SUB_CODES");
        } else {
            return "";
        }
    }

    /**
     * 获取被委托用户编码，多个单引号以及逗号分隔，（只有在线用户有此属性）
     * 
     * @return 被委托用户编码
     */
    public String getCurrentSubCodesQuotaStr() {
        return "'" + getCurrentSubCodes().replaceAll(",", "','") + "'";
    }

    /**
     * 获取当前用户作为秘书对应的领导列表，多个逗号分隔（只有在线用户有此属性）
     * 
     * @return 领导列表
     */
    public String getCurrentLeadCodes() {
        Bean userState = Context.getOnlineUserState();
        if (userState != null) {
            return userState.getStr("LEAD_CODES");
        } else {
            return "";
        }

    }

    /**
     * 获取被委托用户编码，多个单引号以及逗号分隔，（只有在线用户有此属性）
     * 
     * @return 领导列表
     */
    public String getCurrentLeadCodesQuotaStr() {
        return "'" + getCurrentLeadCodes().replaceAll(",", "','") + "'";
    }

    /**
     * 获取IP地址，（只有在线用户有此属性）
     * 
     * @return IP地址
     */
    public String getCurrentIpAddress() {
        Bean userState = Context.getOnlineUserState();
        if (userState != null) {
            return userState.getStr("USER_LAST_IP");
        } else {
            return "";
        }
    }

    /**
     * 获取最后登录时间，（只有在线用户有此属性）
     * 
     * @return 登录时间
     */
    public String getCurrentLastLogin() {
        Bean userState = Context.getOnlineUserState();
        if (userState != null) {
            return userState.getStr("USER_LAST_LOGIN");
        } else {
            return "";
        }
    }

    /**
     * 获取工作兼岗用户列表
     * 
     * @return 工作兼岗用户列表
     */
    public String getCurrentJianCodes() {
        Bean userState = Context.getOnlineUserState();
        if (userState != null) {
            return userState.getStr("JIAN_CODES");
        } else {
            return "";
        }
    }

    /**
     * 获取此用户code对应的最后登录时间，如果多个用户用同一个帐号登录，则取最后一个登录用户的登录地址
     * 
     * @return 最后登录时间
     */
    public String getIpAddress() {
        Bean userState = Context.getOnlineUserStateByUser(this.getCode());
        if (userState != null) {
            return userState.getStr("USER_LAST_IP");
        } else {
            return "";
        }
    }

    /**
     * 获取用户排序号
     * 
     * @return 用户排序号
     */
    public int getSort() {
        return this.get("USER_SORT", 0);
    }

    /**
     * 获取用户照片，照片文件名
     * 
     * @return 用户照片
     */
    public String getImg() {
        return this.getStr("USER_IMG");
    }

    /**
     * 获取用户头像，剪裁后的Base64格式。
     * 
     * @return 用户头像
     */
    public String getImgB64() {
        return FileMgr.getBase64IconByImg(this.getImgSrc());
    }

    /**
     * 获取用户照片，照片文件名
     * 
     * @return 用户照片
     */
    public String getImgSrc() {
        return this.getStr("USER_IMG_SRC");
    }

    /**
     * 获取用户模版编码
     * 
     * @return 用户模版编码
     */
    public String getUserPt() {
        return this.getStr("PT_ID");
    }

    /**
     * 获取公司模版编码
     * 
     * @return 公司模版编码
     */
    public String getCmpyPt() {
        return this.getCmpyBean().getStr("PT_ID");
    }

    /**
     * 获取处室模版编码
     * 
     * @return 处室模版编码
     */
    public String getDeptPt() {
        return this.getDeptBean().getStr("PT_ID");
    }

    /**
     * 获取部门模版编码
     * 
     * @return 部门模版编码
     */
    public String getTDeptPt() {
        return this.getTDeptBean().getStr("PT_ID");
    }

    /**
     * 获取机构模版编码
     * 
     * @return 获取机构模版编码
     */
    public String getODeptPt() {
        return this.getODeptBean().getStr("PT_ID");
    }

    /**
     * 获取启用标志
     * 
     * @return 启用标志
     */
    public int getFlag() {
        return this.getInt("S_FLAG");
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
    public String getUser() {
        return this.getStr("S_USER");
    }

    /**
     * 获取更新时间
     * 
     * @return 更新时间
     */
    public String getMtime() {
        return this.getStr("S_MTIME");
    }

    /**
     * 获取部门信息
     * 
     * @return 部门信息对象
     */
    public synchronized DeptBean getDeptBean() {
        if (deptBean == null) {
            deptBean = OrgMgr.getDept(this.getDeptCode());
            if (deptBean == null) {
                return new DeptBean(new Bean());
            }
        }
        return deptBean;
    }

    /**
     * 获取有效部门信息
     * 
     * @return 有效部门
     */
    public synchronized DeptBean getTDeptBean() {
        if (tDeptBean == null) {
            String tDeptCode = getTDeptCode();
            if (tDeptCode.length() == 0) {
                tDeptCode = this.getDeptCode();
            }
            tDeptBean = OrgMgr.getDept(tDeptCode);
            if (tDeptBean == null) {
                tDeptBean = new DeptBean(new Bean());
            }
        }
        return tDeptBean;
    }

    /**
     * 获取机构部门信息
     * 
     * @return 机构部门
     */
    public synchronized DeptBean getODeptBean() {
        if (oDeptBean == null) {
            String oDeptCode = getODeptCode();
            if (oDeptCode.length() == 0) {
                oDeptCode = this.getDeptCode();
            }
            oDeptBean = OrgMgr.getDept(oDeptCode);
            if (oDeptBean == null) {
                oDeptBean = new DeptBean(new Bean());
            }
        }
        return oDeptBean;
    }

    /**
     * 获取公司信息
     * 
     * @return 公司信息对象
     */
    public synchronized CmpyBean getCmpyBean() {
        if (cmpyBean == null) {
            cmpyBean = OrgMgr.getCmpy(this.getCmpyCode());
        }
        return cmpyBean;
    }

    /**
     * 获取部门名称
     * 
     * @return 部门名称
     */
    public String getDeptName() {
        return this.getDeptBean().getName();
    }

    /**
     * 获取部门简称
     * 
     * @return 部门简称
     */
    public String getDeptShortName() {
        return this.getDeptBean().getShortName();
    }

    /**
     * 获取有效部门名称
     * 
     * @return 有效部门名称
     */
    public String getTDeptName() {
        return this.getTDeptBean().getName();
    }

    /**
     * 获取有效部门简称
     * 
     * @return 部门简称
     */
    public String getTDeptShortName() {
        return this.getTDeptBean().getShortName();
    }

    /**
     * 获取有效部门原始类型
     * 
     * @return 有效部门原始类型
     */
    public String getTDeptSrcType1() {
        return this.getTDeptBean().getSrcType1();
    }

    /**
     * 获取部门业务条线
     * 
     * @return 部门业务条线
     */
    public String getDeptLine() {
        return this.getDeptBean().getLine();
    }

    /**
     * 获取有效部门原始类型
     * 
     * @return 有效部门原始类型
     */
    public String getTDeptSrcType2() {
        return this.getTDeptBean().getSrcType2();
    }

    /**
     * @return 取得带层级的，完整的部门名称。格式为：处室^部门^机构
     */
    public String getFullDeptNames() {
        return OrgMgr.getDeptNames(this.getDeptBean());
    }

    /**
     * 获取所在机构名称（部门类型为机构）
     * 
     * @return 机构名称
     */
    public String getODeptName() {
        return this.getODeptBean().getName();
    }

    /**
     * 获取机构部门简称
     * 
     * @return 机构部门简称
     */
    public String getODeptShortName() {
        return this.getODeptBean().getShortName();
    }

    /**
     * 获取机构部门全称
     * 
     * @return 机构部门全称
     */
    public String getODeptFullName() {
        return this.getODeptBean().getFullName();
    }

    /**
     * 获取部门原始类型1
     * 
     * @return 部门原始类型1
     */
    public String getODeptSrcType1() {
        return this.getODeptBean().getSrcType1();
    }

    /**
     * 获取部门原始类型2
     * 
     * @return 部门原始类型2
     */
    public String getODeptSrcType2() {
        return this.getODeptBean().getSrcType2();
    }

    /**
     * 获取处室原始类型1
     * 
     * @return 处室原始类型1
     */
    public String getDeptSrcType1() {
        return this.getDeptBean().getSrcType1();
    }

    /**
     * 获取处室原始类型2
     * 
     * @return 处室原始类型2
     */
    public String getDeptSrcType2() {
        return this.getDeptBean().getSrcType2();
    }

    /**
     * 获取所在机构层级（部门类型为机构）
     * 
     * @return 所在机构层级，如果没有设置，则返回
     */
    public int getODeptLevel() {
        return this.getODeptBean().getLevel();
    }

    /**
     * 获取所在机构的编码层级
     * 
     * @return 所在机构编码层级
     */
    public String getODeptCodePath() {
        return this.getODeptBean().getCodePath();
    }

    /**
     * 获取上级机构编码
     * @return 上级机构编码,不存在返回“”字符串
     */
    public String getParentODeptCode() {
        String pCode = this.getODeptBean().getPcode();
        if (pCode.length() > 0) {
            return OrgMgr.getDept(pCode).getODeptCode();
        } else {
            return "";
        }
    }

    /**
     * 获取公司名称
     * 
     * @return 公司名称
     */
    public String getCmpyName() {
        return this.getCmpyBean().getName();
    }

    /**
     * 获取公司全名称
     * 
     * @return 公司全名称
     */
    public String getCmpyFullName() {
        return this.getCmpyBean().getFullname();
    }

    /**
     * 判断用户是否属于指定角色
     * @param roleCode 角色编码
     * @return 用户是否属于这个角色
     */
    public boolean existInRole(String roleCode) {
        if (StringUtils.isEmpty(roleCode)) {
            return false;
        }

        List<String> list = getRoleCodeList();
        roleCode = roleCode.trim();
        if (list.contains(roleCode)) {
            return true;
        }

        return false;
    }

    /**
     * 判断用户是否属于有系统管理角色
     * @return 用户是否有系统管理角色
     */
    public boolean isAdminRole() {
        return existInRole(Context.getSyConf("SY_ROLE_ADMIN", "sysadmin"));
    }

    /**
     * 获取用户对应的部门（以D_开头）、角色（以R_开头）、用户编码（以U_开头）列表字符串。
     * @return 用户对应角色、部门、用户编码字符串，逗号单引号分隔
     */
    public String getDeptRoleUserStr() {
        StringBuilder sbUser = new StringBuilder();
        sbUser.append("'U_").append(getCode()).append("','D_").append(getDeptCode())
                .append("'");
        String roleCodes = getRoleCodeQuotaStr();
        if (roleCodes.length() > 0) {
            roleCodes = "," + roleCodes;
            sbUser.append(roleCodes.replaceAll(",\'", ",\'R_"));
        }
        return sbUser.toString();
    }

    /**
     * @return 用户对应部门编码列表，包含本处室部门以及有效部门编码
     */
    public List<String> getDeptCodeList() {
        String oDeptCode = getODeptCode();
        String codePath = getCodePath();
        int pos = codePath.indexOf(oDeptCode);
        codePath = codePath.substring(pos);
        // String[] deptArray = codePath.split(Constant.CODE_PATH_SEPERATOR);
        // 使用 \\^ 进行split
        String[] deptArray = codePath.split("\\^");
        return Arrays.asList(deptArray);
    }

    /**
     * @return 角色编码列表
     */
    public List<String> getRoleCodeList() {
        return Arrays.asList(getRoleCodes());
    }

    /**
     * @return 角色编码列表
     */
    public List<String> getGroupCodeList() {
        return Arrays.asList(getGroupCodes());
    }

    /**
     * @return 返回用户所有的角色Code
     */
    public String getRoleCodeStr() {
        return Lang.arrayJoin(getRoleCodes(), Constant.SEPARATOR);
    }

    /**
     * @return 返回用户所有的群组Code
     */
    public String getGroupCodeStr() {
        return Lang.arrayJoin(getGroupCodes(), Constant.SEPARATOR);
    }

    /**
     * @return 返回用户所有的管理群组Code
     */
    public String getAdminGroupCodeStr() {
        return Lang.arrayJoin(getAdminGroupCodes(), Constant.SEPARATOR);
    }

    /**
     * 返回用户机构内所有层级的部门编码，每个CODE都使用单引号引起来，多个CODE之间使用逗号分隔。
     * @return 部门编码列表字符串。
     */
    public String getDeptCodeQuotaStr() {
        String oDeptCode = getODeptCode();
        String codePath = getCodePath();
        int pos = codePath.indexOf(oDeptCode);
        if (pos > 0) {
            codePath = codePath.substring(pos);
        }
        return "'" + codePath.replaceAll("\\" + Constant.CODE_PATH_SEPERATOR, "','") + "'";
    }

    /**
     * 
     * @return 返回用户所有的角色CODE，每个CODE都使用单引号引起来，多个CODE之间使用逗号分隔。
     */
    public String getRoleCodeQuotaStr() {
        return "'" + Lang.arrayJoin(getRoleCodes(), "','") + "'";
    }

    /**
     * 
     * @return 返回用户所有的群组CODE，每个CODE都使用单引号引起来，多个CODE之间使用逗号分隔。
     */
    public String getGroupCodeQuotaStr() {
        return "'" + Lang.arrayJoin(getGroupCodes(), "','") + "'";
    }

    /**
     * 
     * @return 返回用户所有的管理群组CODE，每个CODE都使用单引号引起来，多个CODE之间使用逗号分隔。
     */
    public String getAdminGroupCodeQuotaStr() {
        return "'" + Lang.arrayJoin(getAdminGroupCodes(), "','") + "'";
    }

    /**
     * @return 返回用户所有的角色Code
     */
    public String[] getRoleCodes() {
        Bean userExt = getUserExt();
        return (String[]) userExt.get(ROLE_CODES);
    }

    /**
     * @return 返回用户所有的群组Code
     */
    public String[] getGroupCodes() {
        Bean userExt = getUserExt();
        return (String[]) userExt.get(GROUP_CODES);
    }

    /**
     * @return 返回用户所有的群组Code
     */
    public String[] getAdminGroupCodes() {
        Bean userExt = getUserExt();
        return (String[]) userExt.get(ADMIN_GROUP_CODES);
    }

    /**
     * 清除当前用户的群组编码信息
     */
    public void clearUserExt() {
        CacheMgr.getInstance().remove(this.getCode(), CACHE_USER_EXT);
    }

    /**
     * 获取用户扩展信息（角色和群组信息），支持缓存
     * 
     * @return 用户扩展信息
     */
    private Bean getUserExt() {
        Bean userExt = (Bean) CacheMgr.getInstance().get(this.getCode(), CACHE_USER_EXT);
        if (userExt == null) {
            userExt = new Bean();
            // 设置角色信息
            userExt.set(ROLE_CODES,
                    RoleMgr.getRoleCodes(this.getCode(), this.getCmpyCode(), this.getODeptLevel()));
            // 设置群组信息
            SqlBean param = new SqlBean().selects("distinct GROUP_CODE, GU_ADMIN")
                    .and("USER_CODE", this.getCode()).and("S_FLAG", Constant.YES_INT)
                    .and("GROUP_FLAG", Constant.YES_INT)
                    .and("USER_FLAG", Constant.YES_INT);
            final List<String> groups = new ArrayList<String>();
            final List<String> adminGroups = new ArrayList<String>();
            ServDao.findsCall(ServMgr.SY_ORG_GROUP_USER, param, new RowHandler() {
                public void handle(List<Bean> columns, Bean data) {
                    groups.add(data.getStr("GROUP_CODE"));
                    if (data.getInt("GU_ADMIN") == Constant.YES_INT) { // 当前人负责管理的
                        adminGroups.add(data.getStr("GROUP_CODE"));
                    }
                }
            });
            userExt.set(GROUP_CODES, groups.toArray(new String[0]));
            userExt.set(ADMIN_GROUP_CODES, adminGroups.toArray(new String[0]));
            CacheMgr.getInstance().set(this.getCode(), userExt, CACHE_USER_EXT); // 放置到缓存中
        }
        return userExt;
    }
    
    /**
     * 生成排序号路径，用户的排序号路径=部门排序号路径 + 用户排序号。常用于比较不同部门几个用户之间的排列顺序。
     * @return 按照由小到大层级顺序，把每一级的排序号合并成一个排序号路径。每一级的排序号占2位数字，不够2位在前面补0。
     */
    public String getSortPath() {
        DeptBean db = this.getDeptBean();
        if (db != null) {
            return db.getSortPath() + "^" + this.formartSortVal();
        }

        return this.formartSortVal();
    }

    /**
     * 获取用户资料完整度（只有在线用户有此属性）
     * 
     * @return 用户资料完整度
     */
    public int getCurrentCmleDeg() {
        Bean userState = Context.getOnlineUserState();
        if (userState != null) {
            return userState.getInt("USER_CMLE_DEG");
        } else {
            return 0;
        }
    }

    /**
     * 获取用户token
     * @return - token str
     */
    public String getToken() {
        Bean userState = Context.getOnlineUserState();
        if (userState != null) {
            return userState.getStr("USER_TOKEN");
        } else {
            return "";
        }
    }
    
    /**
     * 
     * @return 取得格式化成2个数字的排序值
     */
    private String formartSortVal() {
        String sortVal = this.getStr("USER_SORT");
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
