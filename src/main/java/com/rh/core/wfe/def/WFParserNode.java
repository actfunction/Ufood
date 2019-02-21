package com.rh.core.wfe.def;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.rh.core.base.Bean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.JsonUtils;
import com.rh.core.wfe.db.WfNodeActDao;
import com.rh.core.wfe.db.WfNodeDefDao;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 解析工作流中的节点定义
 */
public class WFParserNode extends WFParserBase {

	private static Log log = LogFactory.getLog(WFParserNode.class);

	private Element nodedef;

	private Bean nodeDefBean = new Bean();
	
	private Bean nodeActBean = new Bean();
	
	private Bean nodeOrgBean = new Bean();

	/**
	 * 
	 * @return 原始定义的 xml 转换的Bean
	 */
	public Bean getNodeOrgBean() {
		return nodeOrgBean;
	}

	/**
	 * 
	 * @return 节点操作关联类
	 */
	public Bean getNodeActBean() {
		return nodeActBean;
	}

	/**
	 * 
	 * @return 节点定义bean
	 */
	public Bean getNodeDefBean() {
		return nodeDefBean;
	}

	/**
	 * 
	 * @return xml节点定义
	 */
	public Element getNodedef() {
		return nodedef;
	}

	/**
	 * 
	 * @param cmpyId
	 *            公司ID
	 * @param wfDefCode
	 *            工作流定义名
	 * @param node
	 *            节点定义
	 */
	public WFParserNode(Element node, String cmpyId, String wfDefCode) {
		super(cmpyId, wfDefCode);

		this.nodedef = node;
	}

	/**
	 * 解析工作流节点
	 */
	public void parse() {
		String nodeDefStr = nodedef.getText();
		this.nodeOrgBean = JsonUtils.toBean(nodeDefStr);
		nodeOrgBean.set("PROC_CODE", this.getWfProcCode());
		nodeOrgBean.set("S_CMPY", this.getCmpyID());
		nodeOrgBean.set("NODE_NAME", nodeOrgBean.getStr("NODE_NAME").trim());
		//子流程节点 没有这些配置
		if (nodeOrgBean.getInt("NODE_TYPE") != WfeConstant.NODE_TYPE_SUB_PROCESS) {
    		nodeOrgBean.set("MIND_SCRIPT", decodeHex(nodeOrgBean.getStr("MIND_SCRIPT")));
    		nodeOrgBean.set("MIND_REGULAR_SCRIPT", decodeHex(nodeOrgBean.getStr("MIND_REGULAR_SCRIPT")));
    		nodeOrgBean.set("MIND_TERMINAL_SCRIPT", decodeHex(nodeOrgBean.getStr("MIND_TERMINAL_SCRIPT")));
    		nodeOrgBean.set("NODE_TIMEOUT", decodeHex(nodeOrgBean.getStr("NODE_TIMEOUT")));
		}
		saveNodeDef();
	}
	
	/**
	 * 
	 * @param nodeBean 从客户端传递过来的节点Bean
	 * @param fieldName 字段名称
	 */
    private void decodeParam(Bean nodeBean, String fieldName) {
        if (nodeBean.isEmpty(fieldName)) {
            return;
        }
        
        String str = decodeHex(nodeBean.getStr(fieldName));
        nodeBean.set(fieldName, str);
    }
	
	/**
	 * 保存节点定义到数据库
	 */
	@SuppressWarnings("unchecked")
    private void saveNodeDef() {
		this.nodeDefBean = WfNodeDefDao.insertNodeDef(this.nodeOrgBean);
		
		//子流程节点 没有这些配置
		if (nodeOrgBean.getInt("NODE_TYPE") == WfeConstant.NODE_TYPE_SUB_PROCESS) {
		    return;   
		}
		
		log.debug("parse new node id = " + nodeDefBean.getId());
        decodeParam(this.nodeOrgBean, "BUTTON_PARAMS");
        decodeParam(this.nodeOrgBean, "FIELD_UPDATE");
        decodeParam(this.nodeOrgBean, "BUTTON_ALIAS");
        
		this.nodeActBean = WfNodeActDao.saveNodeAct(this.nodeOrgBean);
		
		String strCustVars = this.nodeOrgBean.getStr(WfeConstant.CUSTOM_VARS);
		
        List<Bean> list = null;
        Object obj = JsonUtils.toBeanList(decodeHex(strCustVars));
        if (obj instanceof List) {
            list = (List<Bean>) obj;
        } else {
            list = new ArrayList<Bean>();
        }
         
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Bean bean = list.get(i);
                bean.set("PROC_CODE", this.getWfProcCode());
                bean.set("NODE_CODE", this.nodeOrgBean.get("NODE_CODE"));
                try {
                    bean.set("VAR_CONTENT", bean.getStr("VAR_CONTENT"));
                    bean.set("VAR_MEMO", bean.getStr("VAR_MEMO"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ServDao.create(WfeConstant.SY_WFE_CUSTOM_VAR, bean);
            }
        }
        
        // 保存扩展办理人
        String strCustBl = this.nodeOrgBean.getStr(WfeConstant.CUSTOM_BL);
		
        List<Bean> custlist = null;
        Object custobj = JsonUtils.toBeanList(decodeHex(strCustBl));
        if (custobj instanceof List) {
        	custlist = (List<Bean>) custobj;
        } else {
        	custlist = new ArrayList<Bean>();
        }
        if (custlist != null) {
            for (int i = 0; i < custlist.size(); i++) {
                Bean bean = custlist.get(i);
                bean.set("PROC_CODE", this.getWfProcCode());
                bean.set("NODE_CODE", this.nodeOrgBean.get("NODE_CODE"));
                ServDao.create(WfeConstant.SY_WFE_CUSTOM_BL, bean);
            }
        }

		log.debug("parse new node act id = " + nodeActBean.getId());
	}
}
