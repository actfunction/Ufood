package com.rh.core.util.ws;

import java.io.OutputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import com.rh.core.serv.util.ServConstant;

/**
 * 服务描述
 * @author wanglong
 */
public class Wsdl {
    /**
     * 命名空间
     */
    public static final String RH_NAMESPACE = "http://ws.ruaho.com/";

    /**
     * 根据服务定义生成WSDL
     * @param service 服务
     * @param out 输出流
     * @param serverURL 流
     */
    public void genWsdl(Service service, OutputStream out, String serverURL) {
        Document doc = DocumentHelper.createDocument();
        // definitions
        Element root = doc.addElement("definitions");
        root.addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
        root.addNamespace("tns", RH_NAMESPACE);
        root.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        root.addAttribute("xmlns", "http://schemas.xmlsoap.org/wsdl/");
        root.addAttribute("targetNamespace", RH_NAMESPACE);
        root.addAttribute("name", service.getName());
        // types
        Element imp = root.addElement("types").addElement("xsd:schema").addElement("xsd:import");
        imp.addAttribute("namespace", RH_NAMESPACE);
        imp.addAttribute("schemaLocation", serverURL + "/" + service.getName() + ".xsd.ws");
        // message
        List<Method> methods = service.getMethods();
        Element msg, part;
        for (Method method : methods) {
            msg = root.addElement("message");
            msg.addAttribute("name", method.getName());
            part = msg.addElement("part");
            part.addAttribute("name", "parameters");
            part.addAttribute("element", "tns:" + method.getName());

            msg = root.addElement("message");
            msg.addAttribute("name", method.getName() + "Response");
            part = msg.addElement("part");
            part.addAttribute("name", "parameters");
            part.addAttribute("element", "tns:" + method.getName() + "Response");
        }
        // message:例外
        msg = root.addElement("message");
        msg.addAttribute("name", "Exception");
        part = msg.addElement("part");
        part.addAttribute("name", "fault");
        part.addAttribute("element", "tns:Exception");
        // portType
        Element portType = root.addElement("portType");
        portType.addAttribute("name", service.getName());
        Element op, fault;
        for (Method method : methods) {
            op = portType.addElement("operation");
            op.addAttribute("name", method.getName());
            op.addElement("input").addAttribute("message", "tns:" + method.getName());
            op.addElement("output").addAttribute("message", "tns:" + method.getName() + "Response");
            fault = op.addElement("fault");
            fault.addAttribute("message", "tns:Exception");
            fault.addAttribute("name", "Exception");
        }
        // binding
        Element bind = root.addElement("binding");
        bind.addAttribute("name", service.getName() + "PortBinding");
        bind.addAttribute("type", "tns:" + service.getName());
        Element soap = bind.addElement("soap:binding");
        soap.addAttribute("transport", "http://schemas.xmlsoap.org/soap/http");
        soap.addAttribute("style", "document");
        for (Method method : methods) {
            op = bind.addElement("operation");
            op.addAttribute("name", method.getName());
            op.addElement("soap:operation").addAttribute("soapAction", "");
            op.addElement("input").addElement("soap:body").addAttribute("use", "literal");
            op.addElement("output").addElement("soap:body").addAttribute("use", "literal");
            fault = op.addElement("fault");
            fault.addAttribute("name", "Exception");
            soap = fault.addElement("soap:fault");
            soap.addAttribute("name", "Exception");
            soap.addAttribute("use", "literal");
        }
        // service
        Element es = root.addElement("service");
        es.addAttribute("name", service.getName() + "Service");
        Element port = es.addElement("port");
        port.addAttribute("name", service.getName() + "Port");
        port.addAttribute("binding", "tns:" + service.getName() + "PortBinding");
        port.addElement("soap:address").addAttribute("location", serverURL + "/" + service.getName() + ".ws");
        write(doc, out);
    }

    /**
     * 根据服务定义生成Schema定义
     * @param service 服务
     * @param out 输出流
     */
    public void genXsd(Service service, OutputStream out) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("xs:schema");
        root.addNamespace("tns", RH_NAMESPACE);
        root.addNamespace("xs", "http://www.w3.org/2001/XMLSchema");
        root.addAttribute("version", "1.0");
        root.addAttribute("targetNamespace", RH_NAMESPACE);
        // element
        List<Method> methods = service.getMethods();
        Element el;
        for (Method method : methods) {
            el = root.addElement("xs:element");
            el.addAttribute("name", method.getName());
            el.addAttribute("type", "tns:" + method.getName());
            el = root.addElement("xs:element");
            el.addAttribute("name", method.getName() + "Response");
            el.addAttribute("type", "tns:" + method.getName() + "Response");
        }
        el = root.addElement("xs:element");
        el.addAttribute("name", "Exception");
        el.addAttribute("type", "tns:Exception");
        Element seq, pel;
        List<Parameter> params;
        for (Method method : methods) {
            el = root.addElement("xs:complexType");
            el.addAttribute("name", method.getName());
            seq = el.addElement("xs:sequence");
            // 非Login并且启用了权限控制自动添加SID参数
            if (!WsUtils.isLogin(service.getName(), method.getName()) 
                    &&  service.getAuthFlag() != ServConstant.AUTH_FLAG_NONE) {
                pel = seq.addElement("xs:element");
                pel.addAttribute("name", "SID");
                pel.addAttribute("type", "xs:string");
            }
            params = method.getParams();
            for (Parameter param : params) {
                pel = seq.addElement("xs:element");
                pel.addAttribute("name", param.getName());
                pel.addAttribute("type", param.getType());
                if (param.isObjectType()) {
                    param.typeToXml(root);
                }
            }
            el = root.addElement("xs:complexType");
            el.addAttribute("name", method.getName() + "Response");
            seq = el.addElement("xs:sequence");
            if (method.getReturn() != null) {

                if (method.getReturn().isObjectType()) {
                    method.getReturn().returnToXml(root, seq);
                } else {
                    pel = seq.addElement("xs:element");
                    pel.addAttribute("name", "return");
                    pel.addAttribute("type", method.getReturn().getType());
                }

            }
        }
        el = root.addElement("xs:complexType");
        el.addAttribute("name", "Exception");
        seq = el.addElement("xs:sequence");
        pel = seq.addElement("xs:element");
        pel.addAttribute("name", "message");
        pel.addAttribute("type", "xs:string");
        pel.addAttribute("minOccurs", "0");
        write(doc, out);
    }

    /**
     * 输出
     * @param doc 文档
     * @param out 流
     */
    private void write(Document doc, OutputStream out) {
        try {
            XMLWriter writer = new XMLWriter(out);
            writer.write(doc);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
