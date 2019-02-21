package com.rh.core.comm.sso;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Constant;
import com.rh.core.util.Dom4JHelper;
import com.rh.core.util.JsonUtils;

/**
 * 根据同步日志生成同步数据的xml
 * 
 * @author wanglong
 */
public class LogXmlDeal {
    
    /**
     * 需要同步更新的信息
     * 
     * @param list 日志记录
     * @return 返回信息为XML，详细格式见相关文档。
     */
    public String createLogXml(List<Bean> list) {
        // 创建一个doc
        Document doc = Dom4JHelper.createDocument();
        Element el = doc.addElement("records");
        
        for (Bean logger : list) {
            createRecord(el, logger);
        }
        
        try {
            return Dom4JHelper.doc2String(doc);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * 生成record节点
     * 
     * @param parentEle 父节点
     * @param logger 日志Bean
     */
    private void createRecord(Element parentEle , Bean logger) {
        
        // 创建一个节点<record>,并加载到doc中作为根节点
        Element el = parentEle.addElement("record");
        // 创建元素属性createTime,设置为节点record的一个属性
        el.addAttribute("createTime", logger.get("SYNC_TIME").toString());
        el.addAttribute("id", logger.getId().toString());
        el.addAttribute("operation", logger.get("SYNC_OPERATION").toString());
        
        Bean paramBean = new Bean("");
        paramBean.set("SYNC_ID", logger.getStr("SYNC_ID"));
        Bean syncBean = ServDao.find("PT_APP_SYNC", paramBean);
        String servId = syncBean.getStr("SERV_ID");
        
        createDoc(el, servId, logger.getStr("SYNC_DATAS"),
                logger.getStr("SYNC_OPERATION"));
        
    }
    
    /**
     * 创建数据节点
     * 
     * @param parentEle 父节点
     * @param servId 服务编码
     * @param code 部门编码
     * @param opt 操作类型，2为删除
     */
    private void createDoc(Element parentEle ,
        String servId ,
        String code ,
        String opt) {
        // 创建data节点并加载到相应的record中
        Element dataEle = parentEle.addElement("data");
        dataEle.addAttribute("pk", code);
        
        if (!opt.equals("delete")) {
            // 获取dpt对象
            Bean paramBean = new Bean();
            paramBean.setId(code);
            Bean data = ServDao.find(servId, paramBean);
            if (data != null) {
                this.getDataXml(null, data, dataEle);
            }
            
        }
    }
    
    /**
     * @param sysCode 系统编码
     * @param data 数据Bean
     * @param dataEle 数据节点
     */
    public void getDataXml(String sysCode , Bean data , Element dataEle) {
        
        if (data != null) {
            // 基本元素
            // 设置子节点即data对象中所有属性参数
            
            /** 数据主键 **/
            if (data.getId() != null) {
                dataEle.addElement(Constant.KEY_ID, data.getId());
            }
            for (Object key : data.keySet()) {
                Element code = dataEle.addElement(key.toString());
                Object value = data.get(key);
                String str = "";
                if (value != null) {
                    if ((value instanceof String)
                            || value.getClass().isPrimitive()) {
                        str = JsonUtils.encode(value.toString());
                    } else if (value instanceof Bean) {
                        str = JsonUtils.toJson((Bean) value);
                    } else if (value instanceof List) {
                        str = JsonUtils.toJson((List<?>) value);
                    } else if (value instanceof Map) {
                        str = JsonUtils.toJson((Map<?, ?>) value);
                    } else {
                        str = JsonUtils.encode(value.toString());
                    }
                }
                code.setText(str);
                
            } // end for
            
        }
    }
}
