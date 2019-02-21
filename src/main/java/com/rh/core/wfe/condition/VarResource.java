package com.rh.core.wfe.condition;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.JsonUtils;

/**
 * 条件流 变量表达式
 * 
 */
public class VarResource {
	private static Log log = LogFactory.getLog(VarResource.class);
    
    /**
     * 变量表达式中，变量的类型，string表示字符串,number表示数字。
     */
    private static final String PARAM_TYPE = "PARAM_TYPE";

	private List<Bean> paramList = new ArrayList<Bean>();
	
	private List<Bean> paramOperList = new ArrayList<Bean>();
	
	private HashMap<String, List<Bean>> setMap = new HashMap<String, List<Bean>>();
	
    /**
     * @return 流程参数列表
     */
    public List<Bean> getParamList() {
        

        
        return paramList;
    }

    /**
     * @param servId 服务ID
     * @return 审批单对应的流程变量
     */
    public Bean getServParams(String servId) {
        // 流程条件
        Bean formParam = new Bean();
        formParam.set("ID", "formCond");
        formParam.set("NAME", "审批单变量");
        formParam.set("NODETYPE", "DIR");
        
        List<Bean> paraList = new ArrayList<Bean>();
        ServDefBean servBean = ServUtils.getServDef(servId);
        List<Bean> itemList = (List<Bean>) servBean.getViewItems();
        
        for (int i = 0; i < itemList.size(); i++) {
            Bean item = itemList.get(i);
            Bean bean = new Bean();
            bean.set("ID", item.getStr("ITEM_CODE"));
            bean.set("NAME", item.getStr("ITEM_NAME"));
            paraList.add(bean);
            addParamOpt(item);
        }
        
        formParam.set("CHILD", paraList);
        return formParam;
    }
    
    /**
     * 为指定参数增加操作符定义
     * @param itemBean 从服务定义中取得的数据字段定义Bean
     */
    private void addParamOpt(Bean itemBean) {
        Bean optBean = new Bean();
        optBean.set("ID", itemBean.getStr("ITEM_CODE"));
        if (itemBean.get("ITEM_FIELD_TYPE").equals(ServConstant.ITEM_FIELD_TYPE_STR)) {
            addOperSet(optBean, "string", itemBean.getStr("ITEM_CODE"));
        } else if (itemBean.get("ITEM_FIELD_TYPE").equals(ServConstant.ITEM_FIELD_TYPE_NUM)) {
            addOperSet(optBean, "number", itemBean.getStr("ITEM_CODE"));
        }
        paramOperList.add(optBean);
    }

	private Document doc;

	/**
	 * 
	 */
	public VarResource() {
		openDefFile();
		parseOperatorSet();
		parseParamList();
	}

	/**
	 * 
	 * @return 集合值
	 */
	public String getOperatorList() {
		return JsonUtils.toJson(paramOperList);
	}

	/**
	 * 读取工作流定义的系统参数
	 */
	public void openDefFile() {
		try {
			String sysParamFilePath = Context.appStr(Context.APP.SYSPATH) + File.separator 
					+ "sy" + File.separator + "wfe" + File.separator
					+ "wf_sys_parameter.xml";

			File wfXmlFile = new File(sysParamFilePath);

			SAXReader reader = new SAXReader();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    FileUtils.readFileToByteArray(wfXmlFile));
			doc = reader.read(inputStream);

		} catch (IOException e) {
			log.error("IOException " + e.getMessage(), e);
		} catch (DocumentException e) {
			log.error("DocumentException " + e.getMessage(), e);
		}
	}
	
	/**
	 * 解析XML文件中的参数列表
	 */
    private void parseParamList() {
        @SuppressWarnings("unchecked")
        List<Element> nodes = doc.selectNodes("//data/paramList");
        
        for (Element node : nodes) {
            // 流程条件
            Bean wfParams = new Bean();
            wfParams.set("ID", "wfCond");
            wfParams.set("NAME", node.attributeValue("name"));
            wfParams.set("CHILD", parseParams(node));
            wfParams.set("NODETYPE", "DIR");
            
            paramList.add(wfParams);
        }
        
    }

    /**
     * @param paraList paramList节点对象
     * @return 解析后的Bean List
     */
    private List<Bean> parseParams(Element paraList) {
	    @SuppressWarnings("unchecked")
        List<Element> nodes = paraList.elements();
		
	    List<Bean> params = new ArrayList<Bean>();
		

		for (int i = 0; i < nodes.size(); i++) {
			Element node = (Element) nodes.get(i);
			Bean treeNodeBean = new Bean();
			treeNodeBean.set("ID", node.attribute("name").getData());
			treeNodeBean.set("NAME", node.attribute("name").getData());

			params.add(treeNodeBean);
			
			Bean paramOperBean = new Bean();
			paramOperBean.set("ID", node.attribute("name").getText());
			//取到所有的operation  operatorSet转化或者 operator循环
			if (node.elementIterator("operatorSet").hasNext()) {  //operatorSet
				//读取 operatorSet 
				Element operSetElement = (Element) node.element("operatorSet");
				String setName = (String) operSetElement.attribute("name").getData();
				String setSyntax = (String) operSetElement.attribute("syntax").getData();
				
				addOperSet(paramOperBean, setName, setSyntax);
			} else { //operator
				List<Bean> operaList = new ArrayList<Bean>();
				@SuppressWarnings("unchecked")
				Iterator<Element> iter = node.elementIterator("operator");
				while (iter.hasNext()) {
					Bean subBean = new Bean();
					Element subElement = (Element) iter.next();

					subBean.set("NAME", subElement.attribute("name").getData());
					subBean.set("SYNTAX", subElement.attribute("syntax").getData());
					operaList.add(subBean);
				}
				
				//参数类型不为null，则设置参数类型
				String paramType = node.attributeValue("paramType");
                if (!StringUtils.isEmpty(paramType)) {
                    paramOperBean.set(PARAM_TYPE, paramType);
                }

				paramOperBean.set("operList", operaList);
			}
			paramOperList.add(paramOperBean);
		}
		
		return params;
	}

	/**
	 * ${var}.equals(${value}) 替换成  context.initUser().getLoginName().equals(${value})
	 * @param paramOperBean 参数操作Bean
	 * @param setName 操作组名称
	 * @param setSyntax 操作组语法
	 */
	private void addOperSet(Bean paramOperBean, String setName, String setSyntax) {
		//读取到name 为 setName的所有operation
		List<Bean> operaList = new ArrayList<Bean>();
		for (Bean operBean: setMap.get(setName)) {
			Bean newOperBean = new Bean();
			
			String oldSyntax = operBean.getStr("SYNTAX");
			
			String newSyntax = oldSyntax.replaceAll("\\$\\{var\\}", setSyntax);
			
			newOperBean.set("SYNTAX", newSyntax);
			newOperBean.set("NAME", operBean.getStr("NAME"));
			operaList.add(newOperBean);
		}
		paramOperBean.set(PARAM_TYPE, setName);
		paramOperBean.set("operList", operaList);
	}

	/**
	 * 
	 */
	private void parseOperatorSet() {
		@SuppressWarnings("unchecked")
		List<Element> nodes = doc.selectNodes("//data/operatorSet");
		for (int i = 0; i < nodes.size(); i++) {
			Element node = (Element) nodes.get(i);

			List<Bean> operaList = new ArrayList<Bean>();
			@SuppressWarnings("unchecked")
			Iterator<Element> iter = node.elementIterator();
			while (iter.hasNext()) {
				Bean subBean = new Bean();
				Element subElement = (Element) iter.next();

				subBean.set("NAME", subElement.attribute("name").getData());
				subBean.set("SYNTAX", subElement.attribute("syntax").getData());
				operaList.add(subBean);
			}
			
			String setName = (String) node.attribute("name").getData();
			setMap.put(setName, operaList);
		}
	}

	/**
	 * 
	 * @param args
	 *            测试
	 */
	public static void main(String[] args) {
		VarResource varRes = new VarResource();
		// varRes.getTreeData();

		System.out.println("" + varRes.getOperatorList());
	}
}
