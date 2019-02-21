package com.rh.gw.wfe;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;

/**
 * 
 * @author peixiujuan
 *
 */
public class MishuLeaderBinder implements ExtendBinder {
    /**
     *署领导秘书对应的署领导：来自于 署领导秘书关系维护表
     *
     **/
    
    @Override
    public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
    	StringBuffer aUserIDs=new StringBuffer();//秘书 ID 
    	ParamBean bean = new ParamBean("OA_GW_LEADER_MS_SHIP", ServMgr.ACT_FINDS);
    	String userCode = getMishu();//获取当前登录人的用户编码 - 署领导秘书的用户编码
    	//设置参数
    	bean.set("S_FLAG","1");
    	bean.set("MS_ID",userCode);
    	//执行方法并获取数据集合
    	List<Bean> list = ServMgr.act(bean).getDataList();
    	
    	ExtendBinderResult result = new ExtendBinderResult();
        result.setAutoSelect(true);
        
        if(list !=null && list.size()>0){
        	for(Bean userBean:list){
        		aUserIDs.append(userBean.getStr("LEADER_USER_CODE")).append(",");;//署领导的用户编码
        	}
    		
            result.setUserIDs(aUserIDs.toString().substring(0, aUserIDs.length()-1));
    	}else{
    		 result.setUserIDs(null);
    	}
        result.setAutoSelect(false);
        result.setReadOnly(false);
        return result;
    }
    
    
    /***
     * 获取署领导秘书
     * @param currentWfAct currentWfAct
     * @return draftDept 起草部门
     */
    private String getMishu() {
    	UserBean currentUser = Context.getUserBean();
    	String userCode = currentUser.getCode();
        return userCode;
    }
}