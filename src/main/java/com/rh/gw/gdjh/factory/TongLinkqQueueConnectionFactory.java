package com.rh.gw.gdjh.factory;

import javax.naming.InitialContext;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rh.gw.gdjh.util.ConfigInfo;

/***
 *   平台mq连接工厂的配置
 * 
 * @author  
 *  
 */
public class TongLinkqQueueConnectionFactory     {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(TongLinkqQueueConnectionFactory.class);

	private static TongLinkqQueueConnectionFactory instance;   
//	private static TongLinkqQueueConnectionFactory instanceJH;   

    private InitialContext context;   
  
    private TongLinkqQueueConnectionFactory() throws Exception {   
        context = ConfigInfo.getInitialContext(null, null);   
    }   
    
    private TongLinkqQueueConnectionFactory(String type) throws Exception {   
        if(type.equals(ConfigInfo.GW_GD_CONNECTION)) {
        	context = ConfigInfo.getGdInitialContext();   
        }
        else if(type.equals(ConfigInfo.GW_JH_CONNECTION)){
        	context = ConfigInfo.getJhInitialContext();
        }
        else {
        	context = ConfigInfo.getInitialContext(null,null); 
        }
    }   
     
    private TongLinkqQueueConnectionFactory(String ip,String port) throws Exception {   
        context = ConfigInfo.getInitialContext(ip,port);   
    }   
    public static TongLinkqQueueConnectionFactory getInstance() throws Exception {   
        if (instance == null) {   
            instance = new TongLinkqQueueConnectionFactory();   
        }   
        return instance;   
    }   
    
    public static TongLinkqQueueConnectionFactory getInstance(String type) throws Exception {   
        return new TongLinkqQueueConnectionFactory(type);     
    }   
    
    public static TongLinkqQueueConnectionFactory getInstance(String ip,String port) throws Exception {   
    	 return   new TongLinkqQueueConnectionFactory(ip,port);
    }  
    
    public Object lookup(String jndiName) throws Exception {   
        Object obj = new Object();   
        obj = context.lookup(jndiName);   
        return obj;   
    }   
     
 
}
