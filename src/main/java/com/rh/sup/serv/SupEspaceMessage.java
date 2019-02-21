package com.rh.sup.serv;

import com.rh.core.base.Bean;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;

import com.rh.gw.util.GwEspaceUtil;


/**
 * 督查系统实时消息发送服务类
 * @author kfzx-xuqin
 *
 */
public class SupEspaceMessage extends CommonServ {

    /**
     * 督查发送消息接口
     * @param paramBean
     * @return
     */
	public OutBean sendMsg(ParamBean paramBean){
		OutBean outbean = new OutBean();
		
		outbean = GwEspaceUtil.sendESpaceMsg(paramBean);
		String msgg = "";
		
		return outbean;
	}
	
	/**
	 * 正式发送
	 * 参数：bean
	 * bean中必须有的参数：
	 */
	public OutBean sendEspaceMsg(Bean bean){
		return GwEspaceUtil.sendESpaceMsg(bean);
	}
	
}
