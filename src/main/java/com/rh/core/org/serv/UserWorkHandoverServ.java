package com.rh.core.org.serv;

import java.util.ArrayList;
import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.org.auth.acl.AclBean;
import com.rh.core.org.auth.acl.mgr.AclMgr;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.bean.SqlBean;
import com.rh.core.serv.flow.FlowMgr;

/**
 * 用户工作交接服务类
 * 
 * @author wanglong
 * 
 */
public class UserWorkHandoverServ extends CommonServ {
    
    @Override
    protected void beforeSave(ParamBean paramBean) {
        String origUserCode = paramBean.getStr("ORIG_USER_CODE");
        String targetUserCode = paramBean.getStr("TARGET_USER_CODE");
        if (origUserCode.isEmpty() || targetUserCode.isEmpty()) {
            throw new TipException("必填项为空，请重新检查");
        }
        
        String origUserName = paramBean.get("ORIG_USER_NAME", UserMgr.getUser(origUserCode).getName());
        String origOdeptCode = paramBean.get("ORIG_ODEPT_CODE", UserMgr.getUser(origUserCode).getODeptCode());
        String targetUserName = paramBean.get("TARGET_USER_NAME", UserMgr.getUser(targetUserCode).getName());
        String targetOdeptCode = paramBean.get("TARGET_ODEPT_CODE", UserMgr.getUser(targetUserCode).getODeptCode());
        
        paramBean.set("ORIG_USER_NAME", origUserName);
        paramBean.set("ORIG_ODEPT_CODE", origOdeptCode);
        paramBean.set("TARGET_USER_NAME", targetUserName);
        paramBean.set("TARGET_ODEPT_CODE", targetOdeptCode);
    }
    
    @Override
    protected void afterSave(ParamBean paramBean, OutBean outBean) {
        ParamBean param = new ParamBean();
        param.set("USER_CODE", outBean.getStr("ORIG_USER_CODE"));
        param.set("TO_USER_CODE", outBean.getStr("TARGET_USER_CODE"));
        ServMgr.act(paramBean.getServId(), "transTo", param);
    }
    
    /**
     * 工作交接：将当前用户的角色、权限交接给指定用户：
     * 1.交接所有角色
     * 2.交接用户上的权限
     * 3.交接工作流的流经
     * 4.处理全文检索的权限
     * @param paramBean 参数信息
     * @return 交接结果信息
     */
    public OutBean transTo(ParamBean paramBean) {
        int count = 0;
        String userCode = paramBean.getStr("USER_CODE");
        final String toUserCode = paramBean.getStr("TO_USER_CODE");
        if ((userCode.length() == 0) || (toUserCode.length() == 0)) {
            throw new TipException(Context.getSyMsg(""));
        }
        UserBean userBean = UserMgr.getUser(userCode);
        String[] roles = userBean.getRoleCodes();
        final String cmpyCode = userBean.getCmpyCode();
        //批量添加角色信息
        count = UserMgr.addRoles(cmpyCode, toUserCode, roles);
        //批量添加用户的权限
        List<AclBean> aclList = AclMgr.getUserAclList(userCode);
        List<AclBean> toAclList = AclMgr.getUserAclList(toUserCode);
        List<Bean> dataList = new ArrayList<Bean>();
        for (AclBean acl : aclList) {
            boolean addFlag = true;
            for (AclBean toAcl : toAclList) { //只有被送交用户不存在的权限再进行复制
                if (acl.getServId().equals(toAcl.getServId()) 
                        && acl.getActCode().equals(toAcl.getActCode())) {
                    addFlag = false;
                    break;
                }
            }
            if (addFlag) {
                Bean data = new Bean();
                data.set("SERV_ID", acl.getServId());
                data.set("ACT_CODE", acl.getActCode());
                data.set("S_CMPY", acl.getsCmpy());
                data.set("ACL_TYPE", acl.getType());
                data.set("ACL_OWNER", toUserCode);
                data.set("ACL_OTYPE", AclMgr.ACL_OTYPE_USER);
                dataList.add(data);
            }
        }
        count += ServDao.creates(ServMgr.SY_ORG_ACL, dataList);
        //批量添加流经信息
        SqlBean countQuery = new SqlBean();
        countQuery.set("OWNER_ID", userCode); //当前人的流经
        
        //每页数据量
        final int perPageItemSize = 100;
        //总数据量
        final int itemCount = ServDao.count(ServMgr.SY_SERV_FLOW, countQuery);
        //总页数
        if (itemCount > 0) {
            final int pageNum = itemCount / perPageItemSize + 1;
            UserBean toUserBean = UserMgr.getUser(toUserCode);
            for (int i = 0; i < pageNum; i++) {
                SqlBean queryBean = new SqlBean();
                queryBean.and("OWNER_ID", userCode);
                queryBean.asc("FLOW_ID");
                queryBean.limit(perPageItemSize);  //页数
                queryBean.page(i + 1); // 每页数据量
                
//                final String mtime = DateUtils.getDatetimeTS();
                List<Bean> flowList =  ServDao.finds(ServMgr.SY_SERV_FLOW, queryBean);
//                        , new QueryCallback() {
//                    public void call(List<Bean> columns, Bean data) {
//                        //为批量增加设置数据初始格式
//                        data.set("FLOW_ID", Lang.getUUID());
//                        data.set("OWNER_ID", toUserCode);
//                        data.set("S_MTIME", mtime);
                        //同时判断是否为全文检索，自动在索引中增加权限
                        //ServUtils.updateIndexGrantUser(ServUtils.getServDef(data.getStr("SERV_ID")), 
                        //        data.getStr("DATA_ID"), toUserCode);
//                    }
//                });
                //count += ServDao.creates(ServMgr.SY_SERV_FLOW, flowList);
                for (Bean b : flowList) {
                    FlowMgr.addUserFlow(b.getStr("DATA_ID"), toUserBean);
                }
                count += flowList.size();
            }
        }
        
        OutBean outBean = new OutBean();
        if (count > 0) {
            UserMgr.clearMenuByUsers(toUserCode); //清除交接给用户的菜单
            outBean.setOk(Context.getSyMsg("SY_BATCHSAVE_OK", String.valueOf(count)));
        } else {
            outBean.setError(Context.getSyMsg("SY_BATCHSAVE_NONE"));
        }
        return outBean;
    }
}
