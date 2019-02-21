package com.rh.core.org.mgr;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.CmpyBean;
import com.rh.core.org.DeptBean;
import com.rh.core.org.util.OrgConstant;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.DateUtils;
import com.rh.core.util.lang.ListHandler;

/**
 * 组织机构管理器
 * 
 * @author wanglong
 * 
 */
public class OrgMgr {

    private static final String COL_S_FLAG = "S_FLAG";

    /**
     * 部门层级字段
     */
    public static final String COL_DEPT_LEVEL = "DEPT_LEVEL";

    /**
     * 部门编码字段
     */
    public static final String COL_DEPT_CODE = "DEPT_CODE";

    /**
     * 父部门编码字段
     */
    public static final String COL_DEPT_PCODE = "DEPT_PCODE";

    /**
     * 公司层级字段
     */
    public static final String COL_CMPY_LEVEL = "CMPY_LEVEL";

    /**
     * 编码路径字段
     */
    public static final String COL_CODE_PATH = "CODE_PATH";

    /**
     * 公司编码字段
     */
    public static final String COL_CMPY_CODE = "CMPY_CODE";

    /**
     * 父公司编码字段
     */
    public static final String COL_CMPY_PCODE = "CMPY_PCODE";

    /**
     * 取得公司Bean
     * 
     * @param cmpyCode 公司ID
     * @return 公司Bean对象
     */
    public static CmpyBean getCmpy(String cmpyCode) {
        Bean bean = ServDao.find(ServMgr.SY_ORG_CMPY, cmpyCode);
        if (bean == null) {
            throw new TipException(Context.getSyMsg("SY_CMPY_NOT_FOUND", cmpyCode));
        }
        return new CmpyBean(bean);
    }
    
    /**
     * 取得带层级的，完整的部门名称。格式为：处室^部门^机构
     * 
     * @param dept 处室或部门对象
     * @return 部门完整名称
     */
    public static String getDeptNames(DeptBean dept) {
        StringBuilder str = new StringBuilder();
        str.append(dept.getName());
        
        if (dept.getType() != OrgConstant.DEPT_TYPE_ORG) {
            // 取父部门名称
            DeptBean parentDept = getDept(dept.getPcode());
            
            //是否遍历到最顶层节点
            if (parentDept != null) {
                str.append("/" + getDeptNames(parentDept));
            }
        }
        
        return str.toString();
    }

    /**
     * 获取所有的公司
     * 
     * @return 公司Bean列表
     */
    public static List<Bean> getAllCmpys() {
        StringBuilder condition = new StringBuilder(" and " + COL_S_FLAG + "=1");
        return ServDao.finds(ServMgr.SY_ORG_CMPY, condition.toString());
    }
    
    /**
     * 
     * @param cmpyCode 公司CODE
     * @param parentOrgCode 父机构ID
     * @return 指定机构的所有下级机构，以及下级机构的所有部门
     */
    public static List<DeptBean> getSubOrgAndChildDepts(String cmpyCode, String parentOrgCode) {
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, getSubOrgAndChildDeptsSql(cmpyCode, parentOrgCode));
        
        List<DeptBean> deptBeanList = new ArrayList<DeptBean>();
        List<Bean> beanList = ServDao.finds(ServMgr.SY_ORG_DEPT, paramBean);
        if (beanList != null) {
            for (Bean bean : beanList) {
                deptBeanList.add(new DeptBean(bean));
            }
        }

        return deptBeanList;        
    }
    
    /**
     * 
     * @param cmpyCode 公司CODE
     * @param parentOrgCode 父机构ID
     * @return 指定机构的所有下级机构，以及下级机构的所有部门 的查询语句
     */
    public static String getSubOrgAndChildDeptsSql(String cmpyCode, String parentOrgCode) {
        StringBuilder sql = new StringBuilder();
        sql.append(" and ODEPT_CODE IN (SELECT DEPT_CODE");
        sql.append(" FROM SY_ORG_DEPT WHERE CMPY_CODE = '").append(cmpyCode).append("'");
        sql.append(" and DEPT_PCODE = '").append(parentOrgCode).append("'");
        sql.append(" and DEPT_TYPE = ").append(OrgConstant.DEPT_TYPE_ORG); //机构
        sql.append(") and DEPT_CODE = TDEPT_CODE");  //有效部门, 或者机构

        return sql.toString();        
    }
    
    /**
     * 
     * @param cmpyCode  公司CODE
     * @param parentOrgCode 父机构ID
     * @return 指定机构的所有下级机构，以及下级机构的所有部门 的查询语句
     */
    public static List<DeptBean> getSubOrgs(String cmpyCode, String parentOrgCode) {
        SqlBean sqlBean = new SqlBean();
        sqlBean.and("DEPT_PCODE", parentOrgCode);
        sqlBean.and("DEPT_TYPE", OrgConstant.DEPT_TYPE_ORG);
        sqlBean.and("CMPY_CODE", cmpyCode);

        List<DeptBean> deptBeanList = new ArrayList<DeptBean>();
        List<Bean> beanList = ServDao.finds(ServMgr.SY_ORG_DEPT, sqlBean);
        if (beanList != null) {
            for (Bean bean : beanList) {
                deptBeanList.add(new DeptBean(bean));
            }
        }

        return deptBeanList;
    }
    
    /**
     * 
     * @param cmpyCode 公司CODE
     * @param parentOrgCode 父机构ID
     * @return 子机构的列表
     */
    public static String getSubOrgDeptsSql(String cmpyCode, String parentOrgCode) {
        StringBuilder sql = new StringBuilder();
        sql.append(" and DEPT_PCODE = '").append(parentOrgCode).append("'"); //子 
        sql.append(" and DEPT_TYPE = ").append(OrgConstant.DEPT_TYPE_ORG); //机构
        
        return sql.toString();
    }
    

    /**
     * 根据部门取得所有子处室
     * @param cmpyId 公司ID
     * @param deptCode 部门编码
     * @return 部门下的所有子处室
     */
    public static List<DeptBean> getChildDepts(String cmpyId, String deptCode) {
    	DeptBean deptBean = getDept(deptCode);
    	
        List<DeptBean> deptBeanList = new ArrayList<DeptBean>();
        
        StringBuffer condition = new StringBuffer();
        condition.append(" and CMPY_CODE ='");
        condition.append(cmpyId);
        condition.append("' and CODE_PATH like '");
        condition.append(deptBean.getCodePath());
        condition.append("%'");
        condition.append("and DEPT_CODE != '" + deptCode + "'");
        condition.append("and ODEPT_CODE = '").append(deptBean.getODeptCode()).append("'");
        
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, condition.toString());
        
        List<Bean> beanList = ServDao.finds(ServMgr.SY_ORG_DEPT, paramBean);
        if (beanList != null) {
            for (Bean bean : beanList) {
            	deptBeanList.add(new DeptBean(bean));
            }
        }

        return deptBeanList;
    }
    
    /**
     * 
     * @param cmpyId 公司ID
     * @return 顶级部门， 即该部门没有父部门  的列表
     */
    public static List<DeptBean> getTopDepts(String cmpyId) {
        List<DeptBean> deptBeanList = new ArrayList<DeptBean>();
        
        StringBuffer condition = new StringBuffer();
        condition.append(" and CMPY_CODE ='");
        condition.append(cmpyId);
        condition.append("' and DEPT_LEVEL = 1");
        
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, condition.toString());
        
        List<Bean> beanList = ServDao.finds(ServMgr.SY_ORG_DEPT, paramBean);
        if (beanList != null) {
            for (Bean bean : beanList) {
            	deptBeanList.add(new DeptBean(bean));
            }
        }

        return deptBeanList;
    }
    
    /**
     * 
     * @param cmpyId 公司ID
     * @return 顶级部门， 即该部门没有父部门  的列表
     */
    public static List<DeptBean> getDepts(String cmpyId) {
        List<DeptBean> deptBeanList = new ArrayList<DeptBean>();
        
        StringBuffer condition = new StringBuffer();
        condition.append(" and CMPY_CODE ='");
        condition.append(cmpyId);
        condition.append("' and DEPT_LEVEL = 2");
        
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, condition.toString());
        paramBean.set(Constant.PARAM_ORDER, "dept_sort");
        
        List<Bean> beanList = ServDao.finds(ServMgr.SY_ORG_DEPT, paramBean);
        if (beanList != null) {
            for (Bean bean : beanList) {
            	deptBeanList.add(new DeptBean(bean));
            }
        }

        return deptBeanList;
    }
    
    /**
     * 
     * @param cmpyId 公司ID
     * @return 所有部门列表
     */
    public static List<DeptBean> getAllDepts(String cmpyId) {
        List<DeptBean> deptBeanList = new ArrayList<DeptBean>();
        SqlBean sql = new SqlBean().and("CMPY_CODE", cmpyId);
        List<Bean> beanList = ServDao.finds(ServMgr.SY_ORG_DEPT, sql);
        if (beanList != null) {
            for (Bean bean : beanList) {
            	deptBeanList.add(new DeptBean(bean));
            }
        }

        return deptBeanList;
    }
    
    /**
     * 
     * @param deptCode 机构Code
     * @return DeptBean 部门Bean
     */
    public static DeptBean getParentOrg(String deptCode) {
    	DeptBean deptBean = OrgMgr.getDept(deptCode);
    	if (deptBean.getType() == OrgConstant.DEPT_TYPE_ORG) {
    		return deptBean;
    	} else {
    		return getParentOrg(deptBean.getPcode());
    	} 
    }

    /**
     * 更新子部门信息
     * 
     * @param oldBean 原部门信息
     * @param newBean 新部门信息
     */
    public static void updateChildDepts(Bean oldBean, Bean newBean) {
        StringBuilder condition = new StringBuilder(" and DEPT_PCODE='");
        condition.append(oldBean.getStr("DEPT_CODE")).append("'");
        Bean paramBean = new Bean();
        paramBean.set(Constant.PARAM_WHERE, condition.toString());
        List<Bean> beanList = ServDao.finds(ServMgr.SY_ORG_DEPT, paramBean);
        String ds = DateUtils.getDatetimeTS();
        for (Bean deptBean : beanList) {
            ParamBean updateDept = new ParamBean(ServMgr.SY_ORG_DEPT, ServMgr.ACT_SAVE).setId(deptBean.getId());
            updateDept.set(COL_DEPT_PCODE, newBean.get(COL_DEPT_CODE)).set("S_MTIME", ds);
            updateDept.set(COL_DEPT_LEVEL, newBean.get(COL_DEPT_LEVEL, 0) + 1);
            updateDept.set(COL_CODE_PATH, newBean.getStr(COL_CODE_PATH) + updateDept.getId()
                    + Constant.CODE_PATH_SEPERATOR);
            if (deptBean.getInt("DEPT_TYPE") == OrgConstant.DEPT_TYPE_ORG) { //自身为机构
                updateDept.set("TDEPT_CODE", deptBean.getId());
                updateDept.set("ODEPT_CODE", deptBean.getId());
            } else { //自身为部门，查找父部门类型
                if (newBean.getInt("DEPT_TYPE") == OrgConstant.DEPT_TYPE_ORG) { //父部门为机构
                    updateDept.set("TDEPT_CODE", deptBean.getId()); //自身为有效部门
                } else { //父部门不为机构
                    updateDept.set("TDEPT_CODE", newBean.getStr("TDEPT_CODE")); //取父有效部门
                }
                updateDept.set("ODEPT_CODE", newBean.getStr("ODEPT_CODE")); //取父所在机构
            }
            ServMgr.act(updateDept);
        }
    }

    /**
     * 取得部门Bean
     * 
     * @param deptCode 部门ID
     * @return 部门Bean对象
     */
    public static DeptBean getDept(String deptCode) {
        Bean bean = ServDao.find(ServMgr.SY_ORG_DEPT, deptCode);
        if (bean != null) {
            return new DeptBean(bean);
        } else {
            return null;
        }
    }
    
    /**
     * 获取当前部门的机构部门
     * @param deptCode 当前部门编码
     * @return 机构部门
     */
    public static DeptBean getOdept(String deptCode) {
        return getOdept(getDept(deptCode));
    }
    
    /**
     * 获取当前部门的机构部门
     * @param deptBean 当前部门
     * @return 机构部门
     */
    public static DeptBean getOdept(DeptBean deptBean) {
        return deptBean.getODeptBean();
    }
    
    /**
     * 获取当前部门的有效部门
     * @param deptCode 当前部门编码
     * @return 有效部门
     */
    public static DeptBean getTdept(String deptCode) {
        return getTdept(getDept(deptCode));
    }
    
    /**
     * 获取当前部门的有效部门
     * @param deptBean 当前部门
     * @return 有效部门
     */
    public static DeptBean getTdept(DeptBean deptBean) {
        return deptBean.getTDeptBean();
    }

    /**
     * 检查是否拥有服务权限
     * @param servId 服务定义编码
     * @return 当前用户是否有使用此服务的权限
     */
    public static boolean checkServAuth(String servId) {
        return checkServAuth(ServUtils.getServDef(servId));
    }
    
    /**
     * 
     * @param servId 服务ID
     * @return 判断指定服务是否在菜单上配置了，且当前用户有访问权限
     */
    public static boolean checkServMenuAuth(String servId) {
        Bean userState = Context.getOnlineUserState();
        if (userState == null) {
            return false;
        } else {
            initAuthServ(userState);
            List<String> authList = userState.getList("_AUTH_SERV");
            if (authList.contains(servId)) { //存在此授权
                return true;
            } 
        }
            
        return false;
    }
    
    /**
     * 
     * @param userState 用户状态对象
     */
    private static void initAuthServ(Bean userState) {
        if (userState.contains("_AUTH_SERV")) { //数据已经存在？
            return;
        }
        
        List<Bean> menuTree = UserMgr.getCacheMenuList(userState.getStr("USER_CODE"));
        if (menuTree != null) { //初始化权限列表
            final List<String> newList = new ArrayList<String>();
            DictMgr.handleTree(menuTree, new ListHandler<Bean>() {
                public void handle(Bean data) {
                    if (data.isNotEmpty("INFO")) {
                        if (data.getInt("TYPE") == 1) { //服务类菜单
                            newList.add(data.getStr("INFO"));
                        } else if (data.getInt("TYPE") == 2) { //自定义链接
                            String info = data.getStr("INFO");
                            int pos = info.indexOf(".");
                            if (pos > 0) {
                                info = info.substring(0, pos);
                            }
                            newList.add(info);
                        }
                    } //end if
                } //end handle
            });
            userState.put("_AUTH_SERV", newList);
        }
    }
    
    /**
     * 检查服务权限
     * @param servDef 服务定义
     * @return 当前用户是否有使用此服务的权限
     */
    public static boolean checkServAuth(Bean servDef) {
        boolean hasAuth;
        //非开发调试模式，且需要判断服务权限时才进行权限判断处理（开发调试模式不需要判断权限，web.xml中配置）
        if (!Context.isDebugMode() && servDef.getInt("SERV_AUTH_FLAG") == ServConstant.AUTH_FLAG_SERV) { 
            Bean userState = Context.getOnlineUserState();
            if (userState == null) {
                hasAuth = false;
            } else {
                initAuthServ(userState);
                List<String> authList = userState.getList("_AUTH_SERV");
                if (authList.contains(servDef.getId())) { //存在此授权
                    hasAuth = true;
                } else {
                    String devUser = "," + Context.getSyConf("SY_DEV_USERS", "superadmin") + ",";  //超级管理员不判断权限
                    if (devUser.indexOf("," + Context.getUserBean().getLoginName() + ",") >= 0) {
                        hasAuth = true;
                    } else {
                        hasAuth = false;
                    }
                }
            }
        } else { //无需判断支持认证为有权限
            hasAuth = true;
        }
        return hasAuth;
    }
}
