package com.rh.core.org.auth.acl.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.auth.acl.AclBean;
import com.rh.core.org.auth.acl.mgr.AclMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;

/**
 * 权限服务类
 * 
 * @author wanglong
 * 
 */
public class AclServ extends CommonServ {

    /**
     * 批量保存之后的拦截方法
     * @param paramBean 参数信息
     * @param outBean 输出信息
     */
    protected void afterBatchSave(ParamBean paramBean, OutBean outBean) {
        if (paramBean.contains("ACL_OWNER")) {
            int otype = paramBean.get("ACL_OTYPE", AclMgr.ACL_OTYPE_ROLE);
            String owner = paramBean.getStr("ACL_OWNER");
            if (otype == AclMgr.ACL_OTYPE_ROLE) {
                String publicRole = Context.getSyConf("SY_ORG_ROLE_PUBLIC", "RPUB");
                if (owner.startsWith(publicRole)) { // 系统设定的公共角色，清除全部用户的菜单时间
                    UserMgr.clearMenuByCmpy(Context.getCmpy());
                } else {
                    UserMgr.clearMenuByRole(owner, Context.getCmpy());
                }
            } else if (otype == AclMgr.ACL_OTYPE_DEPT) {
                UserMgr.clearMenuByDept(owner);
            } else if (otype == AclMgr.ACL_OTYPE_USER) {
                UserMgr.clearMenuByUsers(owner);
            }
        }
    }

    /**
     * 显示授权页面
     * @param paramBean 传入的参数
     * @return 传出的参数
     */
    public OutBean show(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        Bean[] beanArray = new Bean[3];
        ParamBean param;
        String curCmpy = Context.getThreadStr(Context.THREAD.CMPYCODE);
        if (paramBean.contains("CMPY_CODE")) { // 动态指定cmpy_code
            String aclCmpy = paramBean.getStr("CMPY_CODE");
            boolean canAcl = false;
            if (curCmpy.equals(Context.getSyConf("SY_ORG_SUPER_CMPY", "1"))) { // 判断是否有代为授权的权限
                canAcl = true;
            } else {
                param = new ParamBean(ServMgr.SY_ORG_CMPY, ServMgr.ACT_BYID).setId(aclCmpy);
                Bean cmpy = ServMgr.act(param);
                if ((cmpy.getStr("CODE_PATH")).indexOf(curCmpy + Constant.CODE_PATH_SEPERATOR) >= 0) {
                    canAcl = true;
                }
            }
            if (canAcl) {
                outBean.set("CMPY_CODE", aclCmpy);
                Context.setThread(Context.THREAD.CMPYCODE, aclCmpy);
            }
        }
        String act = "dict";
        param = new ParamBean(ServMgr.SY_COMM_INFO, act).setId("SY_ORG_ROLE");
        beanArray[0] = ServMgr.act(param);
        param = new ParamBean(ServMgr.SY_COMM_INFO, act).setId("SY_ORG_DEPT_USER_SUB");
        beanArray[1] = ServMgr.act(param);
        String menu = paramBean.getBoolean("WITH_ACL") ? "SY_COMM_MENU_USER" : "SY_COMM_MENU";
        param = new ParamBean(ServMgr.SY_COMM_INFO, act).setId(menu);
        beanArray[2] = ServMgr.act(param);

        if (paramBean.contains("CMPY_CODE")) { // 恢复原有的cmpyCode
            Context.setThread(Context.THREAD.CMPYCODE, curCmpy);
        }
        outBean.setToDispatcher("/sy/comm/acl/aclView.jsp");
        outBean.set("BEANS", beanArray);
        return outBean;
    }

    /**
     * 显示ACL
     * 
     * @param paramBean 传入的参数
     * @return 传出的参数
     */
    public OutBean showAcl(ParamBean paramBean) {
        String cmpyCode;
        if (!paramBean.isEmpty("CMPY_CODE")) { // 动态指定cmpy_code
            cmpyCode = paramBean.getStr("CMPY_CODE");
        } else {
            cmpyCode = Context.getCmpy();
        }
        List<Bean> beanList = new ArrayList<Bean>();
        String ocode = paramBean.getStr("ocode");
        int otype = paramBean.get("otype", 0);
        if (ocode.length() > 0 && otype > 0) {
            List<AclBean> aclBeanList = AclMgr.getAclList(cmpyCode, ocode, otype);
            for (AclBean aclBean : aclBeanList) {
                Bean bean = new Bean();
                if (aclBean.getType() == 1) {
                    bean.set("SERV_ID", aclBean.getServId() + "-_-" + aclBean.getActCode());
                } else {
                    bean.set("SERV_ID", aclBean.getServId());
                }
                bean.set("ACL_TYPE", aclBean.getType());
                bean.set("ACL_ID", aclBean.getId());
                beanList.add(bean);
            }
        } else {
            throw new TipException(Context.getSyMsg("SY_PARAM_INVALID"));
        }
        OutBean outBean = new OutBean().setOk();
        outBean.set("aclList", beanList);
        return outBean;
    }
}
