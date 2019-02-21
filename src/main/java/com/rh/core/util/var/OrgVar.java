package com.rh.core.util.var;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;


/**
 * 组织用户变量类，变量格式为"@系统配置键值@"，内部无前缀。
 * 
 * @author wanglong
 * @version $Id$
 */
public class OrgVar implements Var {
    /** 单例 */
    private static OrgVar inst = null;
    /**
     * 私有构建体，单例模式
     */
    private OrgVar() {
    }
    
    /**
     * 单例方法
     * @return 获取系统配置变量类
     */
    public static OrgVar getInst() {
        if (inst == null) {
            inst = new OrgVar();
        }
        return inst;
    }
    
    /**
     * 获取变量值
     * @param key 键值
     * @return 变量值
     */
    public String get(String key) {
        String value = getMap().get(key);
        if (value == null) {
            value = key;
        }
        return value;
    }
    
    
    /**
     * 获取系统变量信息
     * @return 系统变量信息
     */
    public Map<String, String> getMap() {
        Map<String, String> vars;
        UserBean userBean = Context.getUserBean();
        if (userBean != null) {
            String userCode = userBean.getCode();
            vars = UserMgr.getCacheVarMap(userCode);
            if (vars != null) {
                return vars;
            } else {
                vars = new HashMap<String, String>();
                vars.put("@USER_CODE@", userBean.getCode()); //用户编码
                vars.put("@USER_NAME@", userBean.getName()); //用户名称
                vars.put("@LOGIN_NAME@", userBean.getLoginName()); //用户登录名
                vars.put("@USER_POST@", userBean.getPost()); //用户岗位
                vars.put("@USER_IDCARD@", userBean.getIdcard()); //身份证号
                vars.put("@USER_SEX@", String.valueOf(userBean.getSex())); //用户性别
                vars.put("@USER_BIRTHDAY@", userBean.getBirthday()); //用户生日
                vars.put("@USER_IMG@", userBean.getImg());  //截取后的头像文件
                vars.put("@USER_IMG_SRC@", userBean.getImgSrc()); //原始头像文件
                vars.put("@CMPY_CODE@", userBean.getCmpyCode()); //公司编码
                vars.put("@CMPY_NAME@", userBean.getCmpyName()); //公司名称（简称）
                vars.put("@CMPY_FULLNAME@", userBean.getCmpyFullName()); //公司全名
                vars.put("@DEPT_CODE@", userBean.getDeptCode()); //处室编码
                vars.put("@DEPT_NAME@", userBean.getDeptName()); //处室名称
                vars.put("@DEPT_SRC_TYPE1@", userBean.getDeptSrcType1());   //处室扩展类型1
                vars.put("@DEPT_SRC_TYPE2@", userBean.getDeptSrcType2());   //处室扩展类型2
                vars.put("@DEPT_LINE@", userBean.getDeptLine());   //部门业务条线
                vars.put("@TDEPT_CODE@", userBean.getTDeptCode()); //部门编码（有效部门）
                vars.put("@TDEPT_NAME@", userBean.getTDeptName()); //部门名称
                vars.put("@TDEPT_SRC_TYPE1@", userBean.getTDeptSrcType1()); //部门扩展类型1
                vars.put("@TDEPT_SRC_TYPE2@", userBean.getTDeptSrcType2()); //部门扩展类型2
                vars.put("@OFFICE_PHONE@", userBean.getOfficePhone()); //办公电话
                vars.put("@USER_MOBILE@", userBean.getMobile()); //手机
                vars.put("@USER_EMAIL@", userBean.getEmail()); //邮箱
                vars.put("@ROLE_CODES@", userBean.getRoleCodeQuotaStr());   //用户所有角色，单引号包含，逗号分隔
                vars.put("@GROUP_CODES@", userBean.getGroupCodeQuotaStr());   //用户所有群组，单引号包含，逗号分隔
                vars.put("@ADMIN_GROUP_CODES@", userBean.getAdminGroupCodeQuotaStr());   //用户所有管理群组
                vars.put("@DEPT_CODES@", userBean.getDeptCodeQuotaStr());   //用户所有部门，（当前、父、父的父...）
                vars.put("@ODEPT_CODE@", userBean.getODeptCode()); // 机构代码(有效部门的父)
                vars.put("@ODEPT_NAME@", userBean.getODeptName()); //机构名称
                vars.put("@ODEPT_FULL_NAME@", userBean.getODeptFullName()); //机构全称
                vars.put("@ODEPT_SRC_TYPE1@", userBean.getODeptSrcType1()); //机构部门扩展类型1
                vars.put("@ODEPT_SRC_TYPE2@", userBean.getODeptSrcType2()); //机构部门扩展类型1
                vars.put("@ODEPT_CODE_PATH@", userBean.getODeptCodePath()); //机构代码路径 
                vars.put("@ODEPT_PCODE@", userBean.getParentODeptCode()); //父机构代码
                vars.put("@ODEPT_LEVEL@", String.valueOf(userBean.getODeptLevel())); //机构所在层级
                vars.put("@SUB_CODES@", userBean.getCurrentSubCodesQuotaStr()); //所有工作委托给当期人的用户列表
                vars.put("@LEAD_CODES@", userBean.getCurrentLeadCodesQuotaStr()); //秘书对应领导编码列表
                vars.put("@JIAN_CODES@", userBean.getCurrentJianCodes()); //所有设定跟当前人工作兼岗的用户列表
                vars.put("@AGT_FLAG@", String.valueOf(userBean.getCurrentAgtFlag())); //当前用户是否出于委托别人办理状态
                vars.put("@USER_CMLE_DEG@", String.valueOf(userBean.getCurrentCmleDeg())); //当前用户的资料完整度
                vars.put("@urlPath@", Context.appStr(Context.APP.CONTEXTPATH)); //虚路径变量
                initUserPortalTempl(vars, userBean); //初始化用户对应模版
                UserMgr.setCacheVarMap(userCode, vars);
            }
        } else {
            vars = new HashMap<String, String>();
        }
        return vars;
    }
    
    /**
     * 初始化用户对应的各类门户模版
     * @param vars  用户变量
     * @param userBean  用户信息
     */
    private void initUserPortalTempl(Map<String, String> vars, UserBean userBean) {
        List<Bean> itemList = DictMgr.getItemList("SY_COMM_TEMPL");
        String pt = userBean.getCmpyPt(); //公司模版
        if (pt.isEmpty()) {
            pt = execPortalExp(vars, itemList, "1");
        }
        vars.put("@CMPY_PT@", pt);
        pt = userBean.getODeptPt(); //机构模版
        if (pt.isEmpty()) {
            pt = execPortalExp(vars, itemList, "2");
        }
        vars.put("@ODEPT_PT@", pt);
        pt = userBean.getTDeptPt(); //部门模版
        if (pt.isEmpty()) {
            pt = execPortalExp(vars, itemList, "3");
        }
        vars.put("@TDEPT_PT@", pt);
        pt = userBean.getUserPt(); //用户模版
        if (pt.isEmpty()) {
            pt = execPortalExp(vars, itemList, "4");
        }
        vars.put("@USER_PT@", pt);
    }
    
    /**
     * 初始化用户对应的各类门户模版
     * @param vars  用户变量
     * @param itemList  模版列表
     * @param type 模版类型
     * @return 符合条件的模版ID
     */
    private String execPortalExp(Map<String, String> vars, List<Bean> itemList, String type) {
        String id = "";
        for (Bean item : itemList) {
            if (item.getInt("FLAG") == Constant.NO_INT 
                    || !item.getStr("PT_TYPE_ATTRIBUTE").equals(type)) { //忽略无效数据
                continue;
            }
            String exp = item.getStr("PT_EXP");
            if (exp.isEmpty()) {
                id = item.getStr("ID");
                break;
            } else {
                String pn = "@(\\w|_|[\u4e00-\u9fa5])*@";
                Pattern pattern = Pattern.compile(pn, Pattern.CASE_INSENSITIVE); //不区分大小写
                Matcher mt = pattern.matcher(exp);
                StringBuffer sb = new StringBuffer();
                while (mt.find()) {
                    String key = mt.group(0);
                    if (key.startsWith("@DATE")) {
                        mt.appendReplacement(sb, DateVar.getInst().get(key));
                    } else if (key.startsWith("@C_")) { //系统配置
                        mt.appendReplacement(sb, ConfVar.getInst().get(key));
                    } else {
                        mt.appendReplacement(sb, vars.get(key));
                    }
                }
                mt.appendTail(sb);
                if (Lang.isTrueScript(sb.toString())) {
                    id = item.getStr("ID");
                    break;
                }
            }
        }
        return id;
    }
}
