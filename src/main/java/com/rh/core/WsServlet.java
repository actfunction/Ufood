package com.rh.core;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.gw.util.GwUtils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.RequestUtils;
import com.rh.core.util.XmlUtils;
import com.rh.core.util.ws.Service;
import com.rh.core.util.ws.ServiceMgr;
import com.rh.core.util.ws.WsUtils;
import com.rh.core.util.ws.Wsdl;

/**
 * WebService Servlet 处理WebService请求
 * @author wanglong
 */
public class WsServlet extends HttpServlet {

    /**
     * sid
     */
    private static final long serialVersionUID = 1462301086730238241L;

    /**
     * Service.wsdl.ws 生成wsdl Service.xsd.ws Schema定义
     * @param request request对象
     * @param response response对象
     * @exception IOException IO错误
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String strUri = request.getRequestURI();
        if (strUri.indexOf("/") >= 0) {
            strUri = strUri.substring(strUri.lastIndexOf("/") + 1);
        }
        String[] uri = strUri.split("\\.");
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("text/xml;charset=UTF-8");
        if (uri.length > 1) {
            Service service = ServiceMgr.getService(uri[0]);
            if (service != null) {
                if (uri[1].equals("wsdl")) {
                    StringBuffer sb = new StringBuffer();
                    String protocol = request.getProtocol();
                    if (protocol.indexOf("/") > 0) {
                        protocol = protocol.substring(0, protocol.indexOf("/")).toLowerCase();
                    }
                    sb.append(protocol).append("://").append(request.getServerName());
                    if (request.getServerPort() != 80) {
                        sb.append(":").append(request.getServerPort());
                    }
                    sb.append(request.getContextPath());
                    new Wsdl().genWsdl(service, out, sb.toString());
                } else if (uri[1].equals("xsd")) {
                    new Wsdl().genXsd(service, out);
                } else if (uri[1].equals("remove")) {
                    ServiceMgr.remove(service.getName());
                    writeErr("ok", out);
                } else {
                    writeErr("无效请求:" + uri[1], out);
                }
            } else {
                writeErr("无效服务:" + uri[0], out);
            }
        } else {
            writeErr("无效服务", out);
        }
        out.flush();
        out.close();
    }

    /**
     * Service.ws 处理WebService请求
     * @param request request对象
     * @param response response对象
     * @exception IOException IO错误
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String myOrigin = request.getHeader("origin");
		String allowOrigin = GwUtils.ifWhiteList(myOrigin);
		response.setHeader("Access-Control-Allow-Origin", allowOrigin);
        response.addHeader("Access-Control-Allow-Credentials", "true");
        String strUri = request.getRequestURI();
        if (strUri.indexOf("/") >= 0) {
            strUri = strUri.substring(strUri.lastIndexOf("/") + 1);
        }
        boolean rhClient = RequestUtils.get(request, "rhClient", false);
        Context.cleanThreadData();
        response.setContentType("text/xml;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String[] uri = strUri.split("\\.");
        if (uri.length > 0) {
            String service = uri[0];
            try {
                Document doc = new SAXReader().read(request.getInputStream());
                Element root = doc.getRootElement();
                if (root.getName().equals("Envelope")) {
                    Element body = root.element("Body");
                    if (body.elements().size() > 0) {
                        Element eleMethod = (Element) body.elements().get(0);
                        String method = eleMethod.getName();

                        // 处理结果，打成soap包返回
                        try {
                            doc = DocumentHelper.createDocument();
                            root = doc.addElement("S:Envelope");
                            root.addNamespace("S", "http://schemas.xmlsoap.org/soap/envelope/");
                            body = root.addElement("S:Body");
                            ParamBean paramBean = new ParamBean(XmlUtils.toBean(eleMethod.asXML()));
                            paramBean.setServId(service).setAct(method);
                            String sid = paramBean.getStr("SID");
                            if (sid.length() > 0 && !WsUtils.isLogout(service, method)) {
                                UserBean user = Context.getUserBean(sid);
                                if (user == null) {
                                    throw new Exception("SESSION失效");
                                }
                            } else if (ServUtils.getServDef(service).getInt("SERV_AUTH_FLAG")
                            != ServConstant.AUTH_FLAG_NONE) {
                                throw new Exception("请先登录系统");
                            }
                            Context.setRequest(request);
                            Context.setResponse(response);
                            //增加客户端请求标记
                            paramBean.set("_CLIENT_REQ_", "TRUE");
                            OutBean resBean = ServMgr.act(paramBean);
                            body.addNamespace("ns2", Wsdl.RH_NAMESPACE);
                            Element elRes = body.addElement("ns2:" + method + "Response");
                            if (WsUtils.isLogin(service, method)) {
                                // 登录方法强制返回sessionID
                                elRes.addElement("return").setText(resBean.getId());
                            } else {
                                // 增加参数rhClient,判断是否是rhClient的客户端访问
                                if (rhClient) {
                                    Element rtn = elRes.addElement("return");
                                    Document resDoc = DocumentHelper.parseText(XmlUtils.toXml("result", resBean));
                                    resDoc.getRootElement().setDocument(doc);
                                    rtn.add(resDoc.getRootElement());
                                } else {
                                    if (resBean.contains(Constant.XML_ROOT)) { // 指定返回格式
                                        String myRoot = resBean.getStr(Constant.XML_ROOT);
                                        Bean myRootBean = resBean.getBean(Constant.XML_ROOT_BEAN);
                                        String xml = XmlUtils.toFullXml(myRoot, myRootBean);
                                        elRes.addElement("return").setText(xml);
                                    } else {
                                        // 其他的访问方式返回值为 将结果转为xml格式的字符串
                                        elRes.addElement("return").setText(XmlUtils.toXml("result", resBean));
                                    }
                                }

                            }
                            write(doc, out);
                        } catch (Exception e) {
                            e.printStackTrace();
                            writeErr(e.getMessage(), out);
                        }
                    } else {
                        writeErr("无效SOAP包", out);
                    }
                } else {
                    writeErr("无效SOAP包", out);
                }
            } catch (Exception e) {
                writeErr(e.getMessage(), out);
            }
        } else {
            writeErr("无效服务", out);
        }
        out.flush();
        out.close();
    }

    /**
     * 输出错误信息
     * @param msg 信息
     * @param out 输出流
     */
    private void writeErr(String msg, OutputStream out) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("S:Envelope");
        root.addNamespace("S", "http://schemas.xmlsoap.org/soap/envelope/");
        Element body = root.addElement("S:Body");
        body.addNamespace("ns2", "http://schemas.xmlsoap.org/soap/envelope/");
        body.addNamespace("ns3", "http://www.w3.org/2003/05/soap-envelope");
        Element fault = body.addElement("ns2:Fault");
        msg = String.valueOf(msg);
        fault.addElement("faultcode").setText("ns2:Server");
        fault.addElement("faultstring").setText(msg);
        Element detail = fault.addElement("detail");
        Element excp = detail.addElement("ns2:Exception");
        excp.addNamespace("ns2", Wsdl.RH_NAMESPACE);
        excp.addElement("message").setText(msg);
        try {
            write(doc, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出信息流
     * @param doc 信息
     * @param out 输出流
     * @exception IOException IO错误
     */

    private void write(Document doc, OutputStream out) throws IOException {
        XMLWriter writer = new XMLWriter(out, OutputFormat.createPrettyPrint());
        writer.write(doc);
        writer.flush();
        writer.close();
    }
}
