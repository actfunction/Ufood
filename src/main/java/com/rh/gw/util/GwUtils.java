package com.rh.gw.util;


import com.rh.core.base.Context;

/**
 * 公文公共Utils类
 *
 * @author WeiTl
 * @version 1.0
 */
public class GwUtils {

    /**
     * 判断是否是白名单，如果系统配置为null，则直接返回true
     * @param myOrigin ipAddr
     * @return true/false
     */
    public static String ifWhiteList(String myOrigin) {
        String whiteListStr = Context.getSyConf("SY_ALLOW_WITHE_LIST", "*");
        String allowOrigin = "null";
        if (whiteListStr.equals("*")) {
        	allowOrigin = "*";
        } else {
            String[] whiteList = whiteListStr.split(",");
            for( String ipAddr : whiteList ) {
                if( myOrigin != null && myOrigin.equals(ipAddr) ){
                	allowOrigin = "myOrigin";
                    break;
                }
            }
        }
        return allowOrigin;
    }

}
