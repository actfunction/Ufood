package com.rh.core.util.ws;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServUtils;

/**
 * 服务管理器
 * @author wanglong
 */
public class ServiceMgr {
    private static Map<String, Service> serviceMap = new HashMap<String, Service>();
    /**
     * 获取服务
     * @param name 名称
     * @return 服务
     */
    @SuppressWarnings("unchecked")
    public static Service getService(String name) {
        Service service = serviceMap.get(name);
        if (service == null) {
            //从服务定义中加载
            Bean servBean = ServUtils.getServDef(name);
            if (servBean != null) {
                service = new Service(name);
                service.setAuthFlag(servBean.getInt("SERV_AUTH_FLAG"));
                List<Bean> acts = (List<Bean>) servBean.get(ServMgr.SY_SERV_ACT);
                Method method;
                List<Bean> paramDefs;
                Parameter param;
                Element ele;
                StringBuffer sb = new StringBuffer();
                String resType;
                for (Bean act : acts) {
                    if (act.getBoolean("ACT_WS_FLAG")) {
                        method = new Method(act.getStr("ACT_CODE"));
                        resType = act.getStr("ACT_WS_RESULT");
                        if (resType.length() > 0) {
                            if (resType.startsWith("xs:")) {
                                method.setReturn(new Parameter("", resType));
                            } else {
                                sb.setLength(0);
                                sb.append("<parameter>");
                                sb.append(resType);
                                sb.append("</parameter>");
                                try {
                                    ele = DocumentHelper.parseText(sb.toString()).getRootElement();
                                    ele.addAttribute("name", "");
                                    ele.addAttribute("type", "tns:");
                                    method.setReturn(new Parameter(ele));
                                } catch (DocumentException e) {
                                    method.setReturn(null);
                                }
                            }
                        }
                        service.getMethods().add(method);
                        paramDefs = (List<Bean>) act.get(ServMgr.SY_SERV_ACT_PARAM);
                        if (paramDefs != null) {
                            for (Bean paramDef: paramDefs) {
                                sb.setLength(0);
                                sb.append("<parameter>");
                                sb.append(paramDef.getStr("PARAM_FORMAT"));
                                sb.append("</parameter>");
                                try {
                                    ele = DocumentHelper.parseText(sb.toString()).getRootElement();
                                    ele.addAttribute("name", paramDef.getStr("PARAM_CODE"));
                                    ele.addAttribute("type", paramDef.getStr("PARAM_TYPE"));
                                    param = new Parameter(ele);
                                } catch (DocumentException e) {
                                    param = new Parameter(paramDef.getStr("PARAM_CODE"), paramDef.getStr("PARAM_TYPE"));
                                }
                                method.getParams().add(param);
                            }
                        }
                    }
                }
                serviceMap.put(service.getName(), service);
            }
        }
        return service;
    }
    /**
     * 清除服务缓存
     * @param name 服务名称
     */
    public static void remove(String name) {
        serviceMap.remove(name);
    }
    /*
    static {
        loadFromXml(ServiceMgr.class.getResourceAsStream("service.xml"));
    }
    */
    /**
     * 从XML流中加载
     * @param in 流
     * @throws Exception 例外 
     */
    public static void loadFromXml(InputStream in) throws Exception {
        Document doc = new SAXReader().read(in);
        Iterator<?> it = doc.getRootElement().elementIterator();
        Service service;
        Method method;
        Element elServ, elMethod, elParam;
        Iterator<?> itMethod, itParam;
        while (it.hasNext()) {
            elServ = (Element) it.next();
            service = new Service(elServ.attributeValue("name"));
            serviceMap.put(service.getName(), service);
            itMethod = elServ.elementIterator();
            while (itMethod.hasNext()) {
                elMethod = (Element) itMethod.next();
                method = new Method(elMethod.attributeValue("name"));
                service.getMethods().add(method);
                itParam = elMethod.elementIterator();
                while (itParam.hasNext()) {
                    elParam = (Element) itParam.next();
                    if (elParam.getName().equals("parameter")) {
                        method.getParams().add(new Parameter(elParam));
                    } else if (elParam.getName().equals("return")) {
                        method.setReturn(new Parameter(elParam));
                    }
                }
            }
        }
    }
}
