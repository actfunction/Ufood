package com.rh.sup.serv;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.serv.*;
import com.rh.core.serv.dict.DictMgr;

/**
 * 司内立项扩展类
 * 
 * @author hukesen
 *
 */
public class SupApproBureauStaff extends CommonServ {
	
    protected void afterQuery(ParamBean paramBean, OutBean outBean) {
        List<Bean> userList = (List<Bean>) outBean.getData();
        for (int i = 0 ; i < userList.size() ; i++ ){
        	Bean bean = userList.get(i);
        	String userCode = bean.getStr("USER_CODE");
        	bean.set("USER_CODE__NAME", DictMgr.getName("SY_ORG_USER", userCode));
        }
    }
}
