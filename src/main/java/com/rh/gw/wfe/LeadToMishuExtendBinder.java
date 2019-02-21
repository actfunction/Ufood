package com.rh.gw.wfe;

import com.rh.core.base.Bean;
import com.rh.core.org.UserBean;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfLineDef;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.def.WfProcDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;

//com.rh.ks.wfe.ExcludeExtendBinder,,{'allDepts':'','excRoles':'RDRAFT','excUsers':'','bindRole':'false'}
/**
 * 
 * @author WeiTl
 *
 */
public class LeadToMishuExtendBinder implements ExtendBinder {
    
    @Override
    public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
        WfProcDef procDef = currentWfAct.getProcess().getProcDef();
//		Bean dataBean = currentWfAct.getProcess().getServInstBean();
        WfLineDef lineDef = procDef.findLineDef(currentWfAct.getCode(), nextNodeDef.getStr("NODE_CODE"));
        //先取线上的定义
        String extCls = lineDef.getBean("ORG_DEF").getStr("NODE_EXTEND_CLASS");
        if (extCls.length() == 0) { //线上没定义， 则用点上的扩展条件
            extCls = nextNodeDef.getStr("NODE_EXTEND_CLASS");
        }

        String configStr = "";
        
        String[] classes = extCls.split(",,");
        if (classes.length == 2) {
            configStr = classes[1];
        }
        
        Bean configBean = JsonUtils.toBean(configStr);
		String addDeptStr = nextNodeDef.getStr("NODE_DEPT_CODES");

//        String excRoles = configBean.getStr("excRoles"); //得到定义里的排除角色信息
//        String excUsers = configBean.getStr("excUsers"); //得到定义里的排除用户信息
        boolean isBindRole = configBean.getStr("bindRole").equals("true") ? true : false; 

        ExtendBinderResult result = new ExtendBinderResult();
        result.setDeptIDs(addDeptStr); //需要送交的部门
        if (isBindRole) {
            result.setBindRole(true);
        } else {
            result.setBindRole(false);
        }
        result.setReadOnly(false);
        result.setAutoSelect(false);
        return result;
    }
}