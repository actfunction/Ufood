package com.rh.core.comm.portal;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.freemarker.FreeMarkerTransfer;
import com.rh.core.util.var.VarMgr;

/**
 * 首页模板的解析，包括：办公首页,集体首页,个人首页等,
 * @author wanglong
 */
public class ParsePortal {
    
    /** log */
    private static Log log = LogFactory.getLog(ParsePortal.class);
    
    private static String defaultFilePath = Context.appStr(Context.APP.SYSPATH) + "/sy/comm/home/ftl/";
    
    /**
     * 根据配置的区块内容，获取对应的数据对象，构造成html返回
     * @param comBean 组件对象
     * @param comParamBean 组件的附近参数
     * @return sw.toString() 字符串
     */
    public static String transComms(Bean comBean, Bean comParamBean) {
    	String dataUrl = comBean.getStr("PC_DATA");
    	String ftlFile = comBean.getStr("PC_CON");
    	String[] dataUrlArray = dataUrl.split(";;");
    	String temlDir = defaultFilePath;
    	if (comBean.isNotEmpty("customFilePath")) {
    	    temlDir = comBean.getStr("customFilePath");
    	}
    	Bean ftlBean = new Bean(); //ftl文件里要用到的参数bean
        try {
            /* 创建和调整配置 */
            FreeMarkerTransfer trans = new FreeMarkerTransfer(temlDir);
            Bean dataBean = new Bean();
            for (int i = 0; i < dataUrlArray.length; i++) {
            	Bean actBean = getActBean(dataUrlArray[i]);
            	String serv = actBean.getStr("serv");
            	String act = actBean.getStr("act");
            	ParamBean paramBean = new ParamBean(actBean.getBean("data"));
            	paramBean.setServId(serv).setAct(act);
            	ftlBean.putAll(paramBean); //把data里的Bean放到ftlBean里使用
            	if (serv.length() > 0 && act.length() > 0) {
            	    if (comParamBean.getStr("showNum").length() > 0) { //数据对象中的显示条数
            	        paramBean.setQueryPageShowNum(comParamBean.getInt("showNum"));
            	    }
            	    paramBean.putAll(comParamBean); //日期相关信息
            	    paramBean.set("serv", serv);
            	    paramBean.set("act", act);
            		if (dataUrlArray.length == 1) {
            			dataBean = ServMgr.act(paramBean);
            		} else {
            			dataBean.set("_DATA_" + i, ServMgr.act(paramBean));
            		}
            	}
            }
            //wangchen add-begin
            if (comParamBean.contains("json")) { //将个性参数中的json字符串参数转换为map放入ftl
                Bean jsonBean = comParamBean.getBean("json");
                comParamBean.remove("json");
                dataBean.putAll(jsonBean);
            }
            //wangchen add-end
                
            //添加系统变量、配置到ftl中
            dataBean.put("confVar", VarMgr.getConfMap());
            dataBean.put("orgVar", VarMgr.getOrgMap());
            dataBean.put("dateVar", VarMgr.getDateMap());
            
            trans.setParams(dataBean);
            
            //组件的个性参数定义
            comParamBean.putAll(ftlBean); // 增加ftlBean到个性参数里传递到ftl文件
            for (Object key : comParamBean.keySet()) {
                String keyStr = (String) key;
                String item = comParamBean.getStr(key);
                trans.addVariable(keyStr, item); 
            }
            //组件的系统参数定义区域
            trans.addVariable("titleBar", comParamBean.getStr("titleBar")); //${titleBar}，是否显示标题条
            trans.addVariable("boxTheme", comParamBean.getStr("boxTheme")); //${boxTheme}，组件样式
            trans.addVariable("icon", comParamBean.getStr("icon")); //${icon}，图标
            trans.addVariable("urlPath", Context.appStr(Context.APP.CONTEXTPATH)); //虚路径变量
            trans.addVariable("title", comParamBean.get("title", comBean.get("PC_NAME"))); //${title}，标题
            trans.addVariable("id", comBean.get("PC_ID")); //${id}，组件ID
            trans.addVariable("height", //${height}，高度
                    comParamBean.getStr("height").indexOf("px") > 0  
                    ? comParamBean.getStr("height") : comParamBean.getStr("height") + "px"); 
            return trans.write2Str(ftlFile);
        } catch (Exception e) {
            log.error("transComms 错误，模板ID：" + comBean.getId(), e);
            return getErrorDiv(comBean, comParamBean, e.getMessage());
            //throw new RuntimeException(e);
        }
    }
    
    /**
     * 对于执行错误的组件，返回错误信息。
     * @param comBean 组件对象
     * @param comParamBean 组件参数
     * @param errorMsg 错误信息
     * @return 错误信息组件框
     */
    private static String getErrorDiv(Bean comBean, Bean comParamBean, String errorMsg) {
        FreeMarkerTransfer trans = new FreeMarkerTransfer(defaultFilePath);
        //组件的系统参数定义区域
        trans.addVariable("titleBar", comParamBean.getStr("titleBar")); //${titleBar}，是否显示标题条
        trans.addVariable("boxTheme", comParamBean.getStr("boxTheme")); //${boxTheme}，组件样式
        trans.addVariable("icon", comParamBean.getStr("icon")); //${icon}，图标
        trans.addVariable("urlPath", Context.appStr(Context.APP.CONTEXTPATH)); //虚路径变量
        trans.addVariable("title", comParamBean.get("title", comBean.get("PC_NAME"))); //${title}，标题
        trans.addVariable("id", comBean.get("PC_ID")); //${id}，组件ID
        trans.addVariable("height", //${height}，高度
                comParamBean.getStr("height").indexOf("px") > 0  
                ? comParamBean.getStr("height") : comParamBean.getStr("height") + "px"); 
        trans.addVariable("errorMsg", errorMsg);
        return trans.write2Str("errorCom.ftl");
    }
    
    /**
     * 根据数据对象返回ben形式的数据对象结果
     * @param dataUrl 参数
     * @return res 字符串
     */
    private static Bean getActBean(String dataUrl) {
        String serv = "";
        String act = "";
        String data = "";
        Bean resBean = new Bean();
        if (dataUrl.length() > 0) { //有数据对象
            String[] preUrl = dataUrl.substring(dataUrl.lastIndexOf("/") + 1).split("\\.");
            serv = preUrl[0];
            act = preUrl[1];
        }
        if (dataUrl.indexOf("data=") > 0) {
            int index = dataUrl.indexOf("data=") + 5;
            data = dataUrl.substring(index);
        }
        // 获取数据
        Bean paramBean = null;
        if (data.length() > 0) {
            paramBean = JsonUtils.toBean(data);
        } else {
            paramBean = new Bean();
        }
        resBean.set("serv", serv);
        resBean.set("act", act);
        resBean.set("data", paramBean);
        return resBean;
    }   
    /**
     * 获取模板
     * @param paramBean 参数对象
     * @return res 字符串
     */
    public static String getTempl(Bean paramBean) {
        String tempCon = paramBean.getStr("PT_CONTENT"); // 对应布局
        String res = "";
        if (tempCon.trim().length() > 0) {
        	res = formatTempl(paramBean);
        }
        return res;
    }
//    /**
//     * 对原始字符串中以[]包含的字段名称进行替换，替换的值来自于数据bean中的数据，替换规则为键值对照。
//     * 例如src为：“你好，[TEST_NAME]”，bean中TEST_NAME键值为"world"，替换后为：“你好，world”
//     * @param ptBean 模版bean对象
//     * @return 替换后的字符串
//     */
//    private static String replaceComs(Bean ptBean) {
//        String tempStr = ptBean.getStr("PT_CONTENT");
//        String tempParam = ptBean.getStr("PT_PARAM"); //模版数据对象
//        Bean paramBean = new Bean();
//        if (tempParam.length() > 0) {
//            paramBean = JsonUtils.toBean(tempParam);
//        }
//        String pn = "\\[((\\w|_|-|[\u4e00-\u9fa5])*)\\]";
//        Pattern pattern = Pattern.compile(pn, Pattern.CASE_INSENSITIVE); //不区分大小写
//        Matcher mt = pattern.matcher(tempStr);
//
//        StringBuffer sb = new StringBuffer();
//        
//        Bean comBean = new Bean();
//        //获取权限过滤的组件
//        while (mt.find()) {
//            StringBuffer beforeSb = new StringBuffer();
//            String fixStr = mt.group(1);
//        	comBean = ServDao.find("SY_COMM_TEMPL_COMS", fixStr);
//        	if (comBean == null) {
//        	    log.error("[" + fixStr + "]组件不存在！");
//        	    continue;
//        	}
//        	if (paramBean.get(fixStr, "").length() > 0) {
//        	    if (JsonUtils.toBean(paramBean.get(fixStr, "")).getStr("data").length() > 0) { //模版中定义了数据对象
//                    String dataUrl = JsonUtils.toBean(paramBean.get(fixStr, "")).getStr("data"); 
//                    String pre = "SY_COMM_NEWS.query.do?data={\"_extWhere\":\"";
//                    pre += " and CHNL_ID =";
//                    pre += "'";
//                    pre += dataUrl;
//                    pre += "'";
//                    pre += " and NEWS_TYPE='1'\"}";
//                    comBean.set("PC_DATA", pre); //模版中定义了改组件的参数对象
//        	    } else {
//        	        
//        	    }
//        	}
//        	String ftl = comBean.getStr("PC_CON");
//            if (ftl.indexOf("/") >= 0) { // 增加自定义目录文件的读取
//                String customFilePath = "";
//                customFilePath = Context.appStr(Context.APP.SYSPATH)
//                        + ftl.substring(0, ftl.lastIndexOf("/") + 1);
//                ftl = ftl.substring(ftl.lastIndexOf("/") + 1);
//                comBean.set("customFilePath", customFilePath);
//            }
//            comBean.set("PC_CON", ftl);
//            beforeSb.append("<div class='portal-temp' comid='");
//            beforeSb.append(comBean.getStr("PC_ID")).append("'>");
//            if (comBean.getStr("PC_DATA").indexOf("<") >= 0) {
//              beforeSb.append(transSelfContent(comBean));
//            } else {
//              beforeSb.append(transComms(comBean));
//            }
//            beforeSb.append("</div>");
//            mt.appendReplacement(sb, beforeSb.toString());
//        }
//        mt.appendTail(sb);
//        return sb.toString();
//    }
    /**
     * 对原始字符串中以[]包含的字段名称进行替换，替换的值来自于数据bean中的数据，替换规则为键值对照。
     * 例如src为：“你好，[TEST_NAME]”，bean中TEST_NAME键值为"world"，替换后为：“你好，world”
     * @param ptParamBean 外面传递给模版的参数对象含$SITE_ID$=2
     * @return 替换后的字符串
     */
    private static String formatTempl(Bean ptParamBean) {
        String tempCon = ptParamBean.getStr("PT_CONTENT"); //模版布局
        String tempParam = ptParamBean.getStr("PT_PARAM"); //模版参数对象
        Bean paramBean = new Bean(); //模版参数Bean
        if (tempParam.length() > 0) {
            paramBean = JsonUtils.toBean(tempParam);
        }
        String pn = "\\[((\\w|_|-|[\u4e00-\u9fa5])*)\\]";
        Pattern pattern = Pattern.compile(pn, Pattern.CASE_INSENSITIVE); //不区分大小写
        Matcher mt = pattern.matcher(tempCon); //匹配出组件

        StringBuffer sb = new StringBuffer();
        //获取权限过滤的组件
        while (mt.find()) {
            Bean comBean = new Bean();  //组件对象
            StringBuffer beforeSb = new StringBuffer();
            String comId = mt.group(1); //组件主键
            comBean = ServDao.find("SY_COMM_TEMPL_COMS", comId);
            if (comBean == null) {
                log.error("[" + comId + "]组件不存在！");
                continue;
            }
            //区块外层系统包装
            beforeSb.append("<div class='portal-temp' comid='"); //组件的外层html容器
            beforeSb.append(comBean.getStr("PC_ID")).append("' id='"); //组件id放到容器上
            beforeSb.append(comBean.getStr("PC_ID")).append("__temp' "); //组件id放外层容器
            int c = comBean.getStr("PC_PARAM").indexOf("refresh"); //获取自刷新标识
            if (c >= 0) {
                beforeSb.append(" comrefresh='true' "); //组件id放到容器上
            }
            beforeSb.append(">"); //关闭标签
            //组件本身处理
            if (c == -1) { //非自刷新组件
                String selfParam = comBean.getStr("PC_SELF_PARAM"); //对应个性参数定义
                Bean selfParamBean = new Bean();
                if (selfParam.length() > 0) { //个性参数的组件里定义，值都为空串
                    if (selfParam.length() > 0) {
                        List<Bean> paramList = JsonUtils.toBeanList(selfParam);
                        for (Bean item : paramList) { //处理组合值字段
                            String itemCode = item.getStr("id");
                            //wangchen modify-begin
                            if (item.get("value") instanceof Bean) {
                                  selfParamBean.set(itemCode, item.getBean("value"));
                            } else {
//                              if (item.getStr("type").equals("1")) { //$默认值$
//                                  selfParamBean.set(itemCode, item.getStr("value"));        
//                              } else {
                                  selfParamBean.set(itemCode, item.getStr("value"));                          
//                              }   
                            }
                            //wangchen modify-end
                        }
                    }
                }
                Bean comParamBean = new Bean();
                //模版参数中有此组件的数据对象定义，则用参数的数据对象
                if (paramBean.get(comId, "").length() > 0) {
                    if (paramBean.getBean(comId).isNotEmpty("data")) { //模版中定义了数据对象
                        String dataUrl = paramBean.getBean(comId).getStr("data"); 
                        comBean.set("PC_DATA", dataUrl); //模版中定义了改组件的参数对象
                    }
                    comParamBean = paramBean.getBean(comId).remove("data"); //去掉data,保留样式定义等参数
                }
                selfParamBean.putAll(comParamBean);
                fixCustomFilePath(comBean);
                if (comBean.getStr("PC_DATA").indexOf("<") == 0) { //自定义html区块
                    beforeSb.append(transSelfContent(comBean));
                } else {  //构造html区块
                    //替换comBean中的数据对象，将含有$字段$的和ptParamBean进行替换
                    comBean.set("PC_DATA", replaceDataUrl(comBean.getStr("PC_DATA"), ptParamBean, selfParamBean));
                    beforeSb.append(transComms(comBean, selfParamBean));
               }
            }
            beforeSb.append("</div>");
            //对$转义
            String filterDollarStr = beforeSb.toString().replaceAll("\\$", "\\\\\\$");
            mt.appendReplacement(sb, filterDollarStr);
        }
        mt.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 自定义目录文件的处理
     * @param comBean 组件对象
     * 
     */
    protected static void fixCustomFilePath(Bean comBean) {
        String ftl = comBean.getStr("PC_CON");
        if (ftl.indexOf("/") >= 0) { // 增加自定义目录文件的读取
            String customFilePath = "";
            customFilePath = Context.appStr(Context.APP.SYSPATH)
                    + ftl.substring(0, ftl.lastIndexOf("/") + 1);
            comBean.set("customFilePath", customFilePath);
            comBean.set("PC_CON", ftl.substring(ftl.lastIndexOf("/") + 1));
        }
    }
    
    /**
     * 根据配置的html区块，获取对应的自定义html格式的数据对象，构造成html返回
     * @param comBean 组件对象
     * @return 替换后的字符串
     */
    public static String transSelfContent(Bean comBean) {
        //获取默认模版的值
        String tempStr = "";
        String ftlFile = "SY_DEFAULT.ftl";
        if (comBean.getStr("ftl").length() > 0) {
            ftlFile = comBean.getStr("PC_CON");
        }
        String temlDir = defaultFilePath;
        try {
            /* 创建和调整配置。 */
            FreeMarkerTransfer trans = new FreeMarkerTransfer(temlDir);
            Bean dataBean = new Bean();
            trans.setParams(dataBean);
            tempStr = trans.write2Str(ftlFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //替换模版的[]中的值
        String pn = "\\[((\\w|_|[\u4e00-\u9fa5])*)\\]";
        Pattern pattern = Pattern.compile(pn, Pattern.CASE_INSENSITIVE); //不区分大小写
        Matcher mt = pattern.matcher(tempStr); // 匹配

        StringBuffer sb = new StringBuffer();
        //获取权限过滤的组件
        while (mt.find()) {
            StringBuffer beforeSb = new StringBuffer();
            String resStr = mt.group(1);
            if (resStr.equals("id")) {
                beforeSb.append(comBean.getStr("PC_ID"));
            } else if (resStr.equals("name")) {
                beforeSb.append(comBean.getStr("PC_NAME"));
            } else if (resStr.equals("content")) {
                beforeSb.append(comBean.getStr("PC_DATA"));
            }
            mt.appendReplacement(sb, beforeSb.toString());
        }
        mt.appendTail(sb);
        return sb.toString();
    }
    /**
     * 替换数据对象中含有$参数$的值
     * @param data 对象串
     * @param paramBean 组件参数对象
     * @param selfParamBean 默认值参数对象
     * @return 替换后的字符串
     */
    public static String replaceDataUrl(String data, Bean paramBean, Bean selfParamBean) {
        //替换模版的[]中的值
        String tempStr = data;
        if (!paramBean.isEmpty() || !selfParamBean.isEmpty()) {
            String pn = "\\$((\\w|_|[\u4e00-\u9fa5])*)\\$";
            Pattern pattern = Pattern.compile(pn, Pattern.CASE_INSENSITIVE); //不区分大小写
            Matcher mt = pattern.matcher(tempStr); // 匹配
            //获取权限过滤的组件
            while (mt.find()) {
                String resStr = "$" + mt.group(1) + "$";
                if (paramBean.getStr(resStr).length() > 0) {
                    tempStr = tempStr.replace(resStr, paramBean.getStr(resStr));
                } else if (selfParamBean.getStr(resStr).length() > 0) {
                    tempStr = tempStr.replace(resStr, selfParamBean.getStr(resStr));
                }
            }
        } 
        
        return tempStr;
    }
}
