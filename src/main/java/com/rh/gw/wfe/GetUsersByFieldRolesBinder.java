package com.rh.gw.wfe;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.db.SqlExecutor;
import com.rh.core.org.UserBean;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.WfAct;
import com.rh.core.wfe.def.WfNodeDef;
import com.rh.core.wfe.resource.ExtendBinder;
import com.rh.core.wfe.resource.ExtendBinderResult;

/**
 * 
 * @author peixiujuan
 *
 */
public class GetUsersByFieldRolesBinder implements ExtendBinder {
    /**
     *获取主办单位下的司局文书
     *
     **/
    
    @Override
    public ExtendBinderResult run(WfAct currentWfAct, WfNodeDef nextNodeDef, UserBean doUser) {
    	StringBuffer aUserIDs=new StringBuffer();//秘书 ID 
    	
    	Bean icbcSwBean = currentWfAct.getProcess().getServInstBean();//获取表单数据
    	//获取字段编码
    	String extCls = nextNodeDef.getStr("NODE_EXTEND_CLASS");
    	String configStr = "";
    	String[] classes = extCls.split(",,");
    	if(classes.length==2){
    		configStr = classes[1];
    	}
    	Bean configBean =JsonUtils.toBean(configStr);
    	String fieldStr = configBean.getStr("fieldStr");//字段编码
        String fielevalue = icbcSwBean.getStr(fieldStr);//获取字段编码的值。
        fielevalue = fielevalue.replaceAll(",", "','");
    	String roleCodes = configBean.getStr("roleCodes");//角色编码
    	String deptLevel = configBean.getStr("deptLevel");//机构层级
    	StringBuffer sql = new StringBuffer();
    	sql.append("SELECT USER_CODE FROM SY_ORG_ROLE_USER_V WHERE S_FLAG = 1 ");
    	sql.append("and ROLE_CODE in( '");
    	sql.append(roleCodes.replaceAll(",", "','"));
    	sql.append("') ");
    	if("3".equals(deptLevel)){//处室层级
    		sql.append("and RU_ID in (SELECT RU_ID FROM SY_ORG_ROLE_USER WHERE DEPT_CODE in('");
    		sql.append(fielevalue);
    		sql.append("') ");
    	}else if("2".equals(deptLevel)){//司局级别
    		sql.append("and RU_ID in (SELECT RU_ID FROM SY_ORG_ROLE_USER WHERE TDEPT_CODE in('");
    		sql.append(fielevalue);
    		sql.append("') ");    		
    	}else if("23".equals(deptLevel)){//司局、处室级别
    		sql.append("and RU_ID in (SELECT RU_ID FROM SY_ORG_ROLE_USER WHERE DEPT_CODE in('");
    		sql.append(fielevalue);
    		sql.append("') ");
    		sql.append("or TDEPT_CODE in ('");
    		sql.append(fielevalue);
    		sql.append("') ");    		
    	}else {//机构级别
    		sql.append("and RU_ID in (SELECT RU_ID FROM SY_ORG_ROLE_USER WHERE DEPT_CODE in('");
    		sql.append(fielevalue);
    		sql.append("') ");
    		sql.append("or TDEPT_CODE in ('");
    		sql.append(fielevalue);
    		sql.append("') ");    	
    		sql.append("or ODEPT_CODE in ('");
    		sql.append(fielevalue);
    		sql.append("') ");    	
    	}
    	sql.append(") ");    	
     	SqlExecutor se = Context.getExecutor();//获取SqlExecutor对象，用于执行sql语句
    	List<Bean> zbUserSqlList = se.query(sql.toString());
     	
     	ExtendBinderResult result = new ExtendBinderResult();
        result.setAutoSelect(false);
     	if(zbUserSqlList !=null && zbUserSqlList.size()>0){
     		for(Bean b: zbUserSqlList){
         		aUserIDs.append(b.getStr("USER_CODE")).append(",");
         	}
            result.setUserIDs(aUserIDs.toString().substring(0, aUserIDs.length()-1));
    	}else{
    		 result.setUserIDs(null);
    	}

        result.setReadOnly(false);
        return result;
    }
    
}