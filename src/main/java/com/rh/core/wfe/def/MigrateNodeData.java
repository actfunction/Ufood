package com.rh.core.wfe.def;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.rh.core.base.Bean;
import com.rh.core.util.Dom4JHelper;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 由于解决工作流定义功能不支持IE8的BUG，需要改变数据库定义文件，因此写了这个类整理工作流节点定义数据。相对于原有的数据格式，新的格式把 BUTTONS_DEF（操作权限） 和 WF_CUSTOM_VARS（自定义变量）
 * 属性的值做了一个整体HEX编码。BUTTONS_DEF属性的值不影响流程流转， 只用于显示，WF_CUSTOM_VARS属性的值会影响流转。
 * @author wanglong
 */
public class MigrateNodeData extends WFParserBase {

    private static Log log = LogFactory.getLog(MigrateNodeData.class);

    /** 设置节点定义数据的版本 **/
    private static final String NODE_DEF_VERSION = "1.1";

    /**
     * 迁移节点数据
     * @param cmpyId    公司编码
     * @param procCode  流程编码
     */
    public MigrateNodeData(String cmpyId, String procCode) {
        super(cmpyId, procCode);
    }

    /**
     * 解析从页面传来的工作流定义的xml文件，
     * 
     * @param xml xml文件内容
     * @throws Exception exception对象
     * @return 整理完成的XML数据
     */
    public String migrate(String xml) throws Exception {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));

            SAXReader reader = new SAXReader();
            log.debug("parsexml reader " + reader);

            Document doc = reader.read(inputStream);

            return parseNodes(doc);
        } catch (DocumentException e) {
            log.error("解析工作流xml 文件出错   DocumentException  " + e.getMessage(), e);
            throw new RuntimeException("解析工作流xml 文件出错 ", e);
        } catch (UnsupportedEncodingException e) {
            log.error("解析工作流xml 文件出错   UnsupportedEncodingException  " + e.getMessage(), e);
            throw new RuntimeException("解析工作流xml 文件出错  UnsupportedEncodingException ", e);
        }
    }

    /**
     * 解析XML文件的内容
     * 
     * @param doc xml Document 对象
     * @throws Exception exception对象
     * @return 整理完成之后的XML字符串
     */
    private String parseNodes(Document doc) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> nodes = doc.selectNodes("//ADDFLOW/NODEDEF");

        for (int i = 0; i < nodes.size(); i++) {
            Element nodeDef = (Element) nodes.get(i);
            String nodeDefStr = nodeDef.getText();

            Bean nodeBean = JsonUtils.toBean(nodeDefStr);
            if (!nodeBean.getStr("NODE_DEF_VERSION").equals(NODE_DEF_VERSION)) {
                parseBtnDef(nodeBean);
                parseCustomfVar(nodeBean);

                nodeBean.set("NODE_DEF_VERSION", NODE_DEF_VERSION);

                nodeDef.setText(JsonUtils.toJson(nodeBean));
            }
        }

        return Dom4JHelper.doc2String(doc);
    }

    /**
     * @param nodeBean 节点Bean
     */
    private void parseBtnDef(Bean nodeBean) {
        List<Bean> btnBean = nodeBean.getList("BUTTONS_DEF");
        if (btnBean == null) {
            return;
        }
        String str = JsonUtils.toJson(btnBean);
        try {
            String hexStr = Hex.encodeHexString(str.getBytes("UTF-8"));
            nodeBean.set("BUTTONS_DEF", hexStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param nodeBean 节点Bean
     */
    @SuppressWarnings("unchecked")
    private void parseCustomfVar(Bean nodeBean) {
        List<Bean> list = null;
        Object obj = nodeBean.get(WfeConstant.CUSTOM_VARS);
        if (obj instanceof List) {
            list = (List<Bean>) obj;
        } else {
            list = new ArrayList<Bean>();
        }

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Bean bean = list.get(i);
                try {
                    bean.set("VAR_CONTENT", decodeHex(bean.getStr("VAR_CONTENT")));
                    bean.set("VAR_MEMO", decodeHex(bean.getStr("VAR_MEMO")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        String str = JsonUtils.toJson(list);
        try {
            String hexStr = Hex.encodeHexString(str.getBytes("UTF-8"));
            nodeBean.set(WfeConstant.CUSTOM_VARS, hexStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
