package com.rh.core.wfe.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.rh.core.base.Bean;
import com.rh.core.wfe.WfAct;


/**
 * 图形化的流程跟踪
 *
 */
public class WfeFigure {
	
	private Log log = LogFactory.getLog(this.getClass());
	
    /** 已经经过的点的颜色 */
    private static final String HAS_PASS_NODE_COLOR = "14540253";

    /** 当前点的颜色值 */
    private static final String CURRENT_ACTIVITY_NODE_COLOR = "8453888";
    
    
    private static final String STR_USER = "STR_USER";
    
    private static final String STR_DEPT = "STR_DEPT";
    

    /** DOCUMENT ME! */
    private List<String> linkName = new ArrayList<String>();

    /** 工作流定义xml */
    private String xmlContent = "";

    /** 流程定义xml doc对象 */
    private Document doc = null;

    /** 当前节点 */
    private HashMap<String, String> currentNode = new HashMap<String, String>();

    /** DOCUMENT ME! */
    private HashMap<String, String> linkTable = new HashMap<String, String>();

    /** 经过的节点的集合 */
    private HashMap<String, String> nodeTable = new HashMap<String, String>();

    /** 已经经过的连线的集合 */
    private List<String> hasLinkName = new ArrayList<String>();

    /** 节点实例列表 */
    private List<WfAct> actInstList = null;
    
    private List<Bean> wfNodeUsers = null;

    /**
     * 
     * @param content xml数据
     * @param wfInstList 节点实例列表
     * @param wfNodeUsers 节点人员
     */
    public WfeFigure(String content, List<WfAct> wfInstList, List<Bean> wfNodeUsers) {
    	this.xmlContent = content;
        this.actInstList = wfInstList;
        this.wfNodeUsers = wfNodeUsers;
        init();
        doParseXML();
    }

    /**
     * 初始化信息
     */
    private void init() {
    	WfAct wfAct = (WfAct) actInstList.get(0);
        int i = 0;

        for (; i < actInstList.size(); i++) {
        	wfAct = (WfAct) actInstList.get(i);

        	if (wfAct.isRunning()) {
        		if (!currentNode.containsKey(wfAct.getCode())) {
        			currentNode.put(wfAct.getCode(), wfAct.getCode());
        		}
        	} else {
        		nodeTable.put(wfAct.getId(), wfAct.getCode());
        	}
        	
            String preLineCode = wfAct.getNodeInstBean().getStr("PRE_LINE_CODE");
            if (!StringUtils.isEmpty(preLineCode)) {
                linkTable.put(wfAct.getId(), preLineCode);
                linkName.add(wfAct.getId());
            }
        }

        for (i = 0; i < linkName.size(); i++) {
            //取得节点CODE
            String linkStart = (String) nodeTable.get(linkName.get(i));
            String linkEnd = (String) nodeTable.get(linkTable.get(linkName.get(i)));
            hasLinkName.add(linkStart + linkEnd);
        }
    }

    /**
     * 生成可以用于显示流程跟踪的XML文件
     */
    public void doParseXML() {
		ByteArrayInputStream inputStream;
		try {
			inputStream = new ByteArrayInputStream(
						this.xmlContent.getBytes("UTF-8"));
	
	
			SAXReader reader = new SAXReader();
	
			doc = reader.read(inputStream);
	
			Element oRoot = doc.getRootElement();
	
			// 遍历节点，处理节点的颜色
			@SuppressWarnings("unchecked")
			List<Element> nodeList = doc.selectNodes("//ADDFLOW/NODE");
	
			for (int i = 0; i < nodeList.size(); i++) {
				Element nodeElement = (Element) nodeList.get(i);
				/**
				 * <NODE Index="3" Left="4905" Top="3450" Width="900" Height="495"
				 * ZOrderIndex="4"> <FillColor>65535</FillColor>
				 * <Text>第二个活动节点</Text> <Key>N3</Key> </NODE>
				 */
				Element nodeKeyElement = nodeElement.element("Key");
				String nodeID = nodeKeyElement.getText();
				
				if (null != currentNode.get(nodeID) && currentNode.get(nodeID).equals(nodeID)) {
					Element fillColorElement = nodeElement.element("FillColor");
					fillColorElement.setText(CURRENT_ACTIVITY_NODE_COLOR);
				} else if (nodeTable.containsValue(nodeID)) {
					Element fillColorElement = nodeElement.element("FillColor");
					fillColorElement.setText(HAS_PASS_NODE_COLOR);
				}
			}
	
			// 遍历连线，处理连线的颜色
			@SuppressWarnings("unchecked")
			List<Element> linkList = doc.selectNodes("//ADDFLOW/LINK");
	
			for (int i = 0; i < linkList.size(); i++) {
				Element link = (Element) linkList.get(i);
				Element linkKeyElement = link.element("Key");
				String linkID = linkKeyElement.getText();
	
				if (hasLinkName.contains(linkID)) {
					Element color = link.addElement("DrawColor");
					color.addText(HAS_PASS_NODE_COLOR);
				}
			}
			
			addCurrentNode(oRoot);
	
			// 增加流程跟踪信息
			addHistory(oRoot);
		} catch (UnsupportedEncodingException e) {
			log.error("生成可以用于流程跟踪的xml UnsupportedEncodingException " + e.getMessage(), e);
		} catch (DocumentException e) {
			log.error("生成可以用于流程跟踪的xml DocumentException " + e.getMessage(), e);
		}
    }

    /**
     * 添加当前点的信息
     * @param root 流程定义根节点
     */
    private void addCurrentNode(Element root) {
		//循环当前点 currentNode
		Iterator<Entry<String, String>> it = currentNode.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>) it.next();

			Element current = root.addElement("current");
			current.addAttribute("currentNodeCode", (String) entry.getKey());
		}
	}

	/**
     * 从流程跟踪的历史记录中取出数据放入打XML文件的Root节点下
     * @param root 流程定义根节点
     * 
     */
    private void addHistory(Element root) {
    	Map<String, WfAct> wfActMap = new HashMap<String, WfAct>();
    	Map<String, Map<String, String>> nodeUserMap = getNodeUserMap();
    	for (WfAct wfAct: actInstList) {
    		wfActMap.put(wfAct.getId(), wfAct);
    	}
    	
        for (WfAct wfAct: actInstList) {
            Element action = root.addElement("Action");

            //节点实例上能取到的值
            String beginTime = wfAct.getNodeInstBean().getStr("NODE_BTIME"); 
            String endTime = wfAct.getNodeInstBean().getStr("NODE_ETIME");
            String preLineCode = wfAct.getNodeInstBean().getStr("PRE_LINE_CODE");
            
            //需要到用户表中去取值
            Map<String, String> niUsers = nodeUserMap.get(wfAct.getId());
            String doneDept = niUsers.get(STR_DEPT); 
            String doneUser = niUsers.get(STR_USER);
            
            //默认给值，
            String sendDept = niUsers.get(STR_DEPT); 
            String sendUser = niUsers.get(STR_USER);
            
            if (!wfAct.getNodeInstBean().isEmpty("PRE_NI_ID")) { //如果有上个节点， 取上个节点的人
                String preNid = wfAct.getNodeInstBean().getStr("PRE_NI_ID");
                
                Map<String, String> preNiUsers = nodeUserMap.get(preNid);
                
                sendUser = preNiUsers.get(STR_USER);
                sendDept = preNiUsers.get(STR_DEPT); 
            }
            
            action.addAttribute("SendDept", sendDept);
            action.addAttribute("SendUser", sendUser);
            action.addAttribute("DoneDept", doneDept);
            action.addAttribute("DoneUser", doneUser);
            action.addAttribute("BeginTime", beginTime);
            action.addAttribute("EndTime", endTime);
            action.addAttribute("NodeCode", wfAct.getCode());
            action.addAttribute("PreLineCode", preLineCode);
        }
    }

    /**
     * @return 节点人员对应的map
     */
    private Map<String, Map<String, String>> getNodeUserMap() {
        Map<String, Map<String, String>> nodeUserMap = new HashMap<String, Map<String, String>>();

        
        StringBuilder user = new StringBuilder();
        StringBuilder dept = new StringBuilder();
        Map<String, String> userInfoMap = new HashMap<String, String>();
        
        String niId = "";
        for (Bean nodeUser: wfNodeUsers) {
            if (!niId.equals(nodeUser.getStr("NI_ID"))) { //NI_ID 变化，重新设置
                userInfoMap = new HashMap<String, String>();
                user.setLength(0);
                dept.setLength(0);
                
                niId = nodeUser.getStr("NI_ID");
            }
            
            if (user.length() > 0) {
                user.append(",");
            }
            if (dept.length() > 0) {
                dept.append(",");
            }
            
            user.append(nodeUser.getStr("TO_USER_NAME"));
            dept.append(nodeUser.getStr("TO_DEPT_NAME"));
            
            niId = nodeUser.getStr("NI_ID");
            
            userInfoMap.put(STR_USER, user.toString());
            userInfoMap.put(STR_DEPT, dept.toString());
            
            nodeUserMap.put(niId, userInfoMap);
    	}
        
        return nodeUserMap;
    }
    
    
    /**
     * 取得显示流程跟踪的XML文件。
     *
     * @return 包含XML文件内容的字符串
     */
    public String getXMLContent() {
    	StringWriter out = new StringWriter();
    	XMLWriter xw = new XMLWriter(out, new OutputFormat(" ", true, "UTF-8"));
    	try {
			xw.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return out.toString(); 
    }
}
