package com.rh.core.wfe.def;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.comm.FileMgr;
import com.rh.core.serv.ServDao;
import com.rh.core.util.Lang;
import com.rh.core.wfe.db.WfLineDao;
import com.rh.core.wfe.db.WfNodeActDao;
import com.rh.core.wfe.db.WfNodeDefDao;
import com.rh.core.wfe.db.WfProcDefDao;
import com.rh.core.wfe.util.WfeConstant;

/**
 * 解析工作流定义字符串，并将解析结果保存到数据中。
 * 
 * @author wanglong
 * 
 */
public class WFParser {

	private static Log log = LogFactory.getLog(WFParser.class);

	private String procDefContent = "";

	private Bean wfProcDefBean = new Bean();

	/**
	 * @param cmpyID
	 *            公司ID
	 * @param wfProcDef
	 *            工作流对象
	 */
	public WFParser(String cmpyID, Bean wfProcDef) {
		wfProcDefBean.set("S_CMPY", cmpyID);
		
		this.wfProcDefBean = wfProcDef;
	}

	/**
	 * 保存解析结果到数据中。
	 * 
	 */
	public void save() {
		boolean existProc = WfProcDefDao.procCodeIsUsed(wfProcDefBean.getStr("PROC_CODE"));

		if (existProc) {
			throw new RuntimeException("工作流定义已经存在 " + wfProcDefBean.getStr("EN_NAME") 
			        + " 公司ID为 " + wfProcDefBean.getStr("S_CMPY")
			        + " 版本号为" + wfProcDefBean.getStr("PROC_VERSION"));
		}

		saveWfProcDef();
	}

	/**
	 * 修改指定ID的工作流。先删除指定id的工作流定义，然后再将新的定义结果保存到数据库中。
	 */
	public void modify() {
		//删除节点操作关联数据
		Bean nodeActBean = new Bean();
		nodeActBean.set("PROC_CODE", this.getOldProcCode());
		nodeActBean.set("S_CMPY", wfProcDefBean.getStr("S_CMPY"));
		ServDao.deletes(WfNodeActDao.SY_WFE_NODE_ACT_SERV, nodeActBean);
		
		// 删除节点定义
		WfNodeDefDao.deleteNodeDefByProcCode(getOldProcCode());

		// 删除线定义
		WfLineDao.deleteLineDefByProcCode(getOldProcCode());

		// 删除流程定义
		WfProcDefDao.delWfProcDefBeanByProcCode(getOldProcCode());
		
		//删除变量定义
		Bean varBean = new Bean();
		varBean.set("PROC_CODE", this.getOldProcCode());
		ServDao.deletes(WfeConstant.SY_WFE_CUSTOM_VAR, varBean);
		
		//删除扩展办理定义
		Bean custBean = new Bean();
		custBean.set("PROC_CODE", this.getOldProcCode());
		ServDao.deletes(WfeConstant.SY_WFE_CUSTOM_BL, varBean);

		saveWfProcDef();
	}

	/**
	 * 保存工作流定义
	 */
	private void saveWfProcDef() {
		wfProcDefBean.set("PROC_XML", this.procDefContent);
		
		String servProcDef = Context.getThread(Context.THREAD.SERVID, WfProcDefDao.SY_WFE_PROC_DEF_SERV);
		ServDao.create(servProcDef, this.wfProcDefBean);

		// 解析工作流定义文件
		parseXml();

		try {
			this.saveDefFile(this.procDefContent);
		} catch (IOException e) {
			throw new RuntimeException("保存工作流定义文件出错 " + this.procDefContent, e);
		}
		
		//将之前缓存中的流程进行删除
		WfProcDefManager.removeFromCache(this.getOldProcCode());
	}

	/**
	 * 解析从页面传来的工作流定义的xml文件，
	 */
	private void parseXml() {
        if (StringUtils.isEmpty(procDefContent)) {
            return;
        }
		try {
			log.debug("procDefContent " + procDefContent);

			ByteArrayInputStream inputStream = new ByteArrayInputStream(
						this.procDefContent.getBytes("UTF-8"));

			// File wfXmlFile = new File("D:\\dev\\firefly\\docs\\newWf.xml");
			// ByteArrayInputStream inputStream;
			// inputStream = new
			// ByteArrayInputStream(FileUtils.readFileToByteArray(wfXmlFile));

			SAXReader reader = new SAXReader();
			log.debug("parsexml reader " + reader);

			Document doc = reader.read(inputStream);

			parseNodes(doc);

			parseLines(doc);
		} catch (DocumentException e) {
			log.error("解析工作流xml 文件出错   DocumentException  " + e.getMessage(), e);
			throw new RuntimeException("解析工作流xml 文件出错 ", e);
		} 	catch (UnsupportedEncodingException e) {
			log.error("解析工作流xml 文件出错   UnsupportedEncodingException  " + e.getMessage(), e);
			throw new RuntimeException("解析工作流xml 文件出错  UnsupportedEncodingException ", e);
		}
	}

	/**
	 * 解析xml 节点 , 并保存节点定义
	 * 
	 * @param doc
	 *            xml doc对象
	 * @throws ApplicationException
	 */
	private void parseNodes(Document doc) {
		@SuppressWarnings("unchecked")
		List<Element> nodes = doc.selectNodes("//ADDFLOW/NODEDEF");

		for (int i = 0; i < nodes.size(); i++) {
			Element node = (Element) nodes.get(i);
			WFParserNode parseNode = new WFParserNode(node,
					wfProcDefBean.getStr("S_CMPY"),
					wfProcDefBean.getStr("PROC_CODE"));
			parseNode.parse();
		}

		log.debug("parse xml node define complte");
	}

	/**
	 * 解析xml 中的 连线信息 ， 保存连线定义信息
	 * 
	 * @param doc
	 *            xml doc对象
	 */
	private void parseLines(Document doc) {
		@SuppressWarnings("unchecked")
		List<Element> lines = doc.selectNodes("//ADDFLOW/LINEDEF");

		for (int i = 0; i < lines.size(); i++) {
			Element line = (Element) lines.get(i);
			WFParserLine parseLine = new WFParserLine(line,
					wfProcDefBean.getStr("S_CMPY"),
					wfProcDefBean.getStr("PROC_CODE"));
			parseLine.parse();
		}

		log.debug("parse xml line define complete");

	}


	/**
	 * 
	 * @param strXml
	 *            工作流xml文件
	 */
	public void setDefContent(String strXml) {
		this.procDefContent = strXml;
	}


	/**
	 * 
	 * @param procCode
	 *            主键序号
	 */
	public void setProcCode(String procCode) {
		this.wfProcDefBean.setId(procCode);
		this.wfProcDefBean.set("PROC_CODE", procCode);
	}

	/**
	 * 
	 * @param oldProcCode
	 *            旧的流程编码
	 */
	public void setOldProcCode(String oldProcCode) {
		this.wfProcDefBean.set("OLD_PROC_CODE", oldProcCode);
	}

	/**
	 * 
	 * @return 旧的流程编码
	 */
	public String getOldProcCode() {
		return this.wfProcDefBean.getStr("OLD_PROC_CODE");
	}

	/**
	 * 保存工作流定义文件
	 * 
	 * @param strXml
	 *            定义文件的内容
	 * @throws IOException
	 *             IO异常
	 */
	private void saveDefFile(String strXml) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				strXml.getBytes("UTF-8"));
		
		String servId = WfProcDefDao.SY_WFE_PROC_DEF_SERV;
		String mtype = "text/xml";
		String name = this.wfProcDefBean.getId();
		String fileId = Lang.getUUID();
		String category = "WFECONFIG";
		
		FileMgr.upload(servId, name, fileId, category, name, inputStream, name, mtype);
	}
}
