package com.rh.gw.wfe;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * 业务公文流程线扩展类
 *
 * @author kfzx-linll
 * @date 2018/11/22
 */
public class YeWuGongWenBinder implements ExtendBinder {

	/**记录历史*/
	private static Log log = LogFactory.getLog(YeWuGongWenBinder.class);

	/**OA_GW_TIME_PRESCRIPTION表和服务一致*/
	private static final String OA_GW_TIME_PRESCRIPTION = "OA_GW_TIME_PRESCRIPTION";


    /**
     *
     */
    @Override
    public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
    	StringBuffer aUserIDs = new StringBuffer();
		String userCode = getMishu();//获取当前登录人的用户编码
		ParamBean bean = new ParamBean("OA_GW_LEADER_MS_SHIP", ServMgr.ACT_FINDS);
    	//设置参数
    	bean.set("S_FLAG","1");
    	bean.set("MS_ID",userCode);
    	//执行方法并获取数据集合
    	List<Bean> list = ServMgr.act(bean).getDataList();
    	
    	ExtendBinderResult result = new ExtendBinderResult();
        result.setAutoSelect(true);
        
        if (list != null && list.size() > 0) {
        	for(Bean userBean:list){
        		aUserIDs.append(userBean.getStr("LEADER_USER_CODE")).append(",");;//署领导的用户编码
        	}
            result.setUserIDs(aUserIDs.toString().substring(0, aUserIDs.length()-1));
    	} else {
        	result.setUserIDs(null);
    	}
        result.setReadOnly(false);
        return result;
    }
    
    
    /**
     * 获取当前用户的 UserCode
	 *
     * @param currentWfAct currentWfAct
     * @return draftDept 起草部门
     */
    private String getMishu() {
    	UserBean currentUser = Context.getUserBean();
    	String userCode = currentUser.getCode();
        return userCode;
    }
}