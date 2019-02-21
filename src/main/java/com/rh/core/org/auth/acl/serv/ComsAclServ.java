package com.rh.core.org.auth.acl.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
/**
 * 组件权限服务类
 * @author wanglong
 *
 */
public class ComsAclServ extends CommonServ {
    /**
     * 显示授权组件页面
     * @param paramBean 传入的参数
     * @return 传出的参数
     */
    public OutBean show(ParamBean paramBean) {
        OutBean outBean = new OutBean();
        Bean[] beanArray = new Bean[3];
        ParamBean param;
        String act = "dict";
        param = new ParamBean(ServMgr.SY_COMM_INFO, act).setId("SY_ORG_ROLE");
        beanArray[0] = ServMgr.act(param);
        param = new ParamBean(ServMgr.SY_COMM_INFO, act).setId("SY_ORG_DEPT_USER_SUB");
        beanArray[1] = ServMgr.act(param);
        param = new ParamBean(ServMgr.SY_COMM_INFO, act).setId("SY_COMM_TEMPL_COMS_TREE");
        beanArray[2] = ServMgr.act(param);
        outBean.setToDispatcher("/sy/comm/acl/comsAclView.jsp");
        outBean.set("BEANS", beanArray);
        return outBean;
    }
    /**
     * 显示组件列表
     * @param paramBean 传入的参数
     * @return 传出的参数
     */
    public OutBean showAcl(ParamBean paramBean) {
        List<Bean> beanList = new ArrayList<Bean>();
        String ocode = paramBean.getStr("ocode"); //所有者编码
        String aclType = paramBean.getStr("ACL_TYPE"); //组件类别
        if (ocode.length() > 0) {
            List<Bean> aclBeanList = DataAclServ.getAclList(ocode, aclType);
            for (Bean aclBean : aclBeanList) {
                Bean bean = new Bean();
                bean.set("SERV_ID", aclBean.get("SERV_ID"));
                bean.set("ACL_TYPE", aclBean.get("ACL_TYPE"));
                bean.set("DATA_ID", aclBean.get("DATA_ID"));
                bean.set("ACL_ID", aclBean.get("ACL_ID"));
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
