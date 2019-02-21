package com.rh.core.wfe.def;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.rh.core.base.Bean;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Lang;
import com.rh.core.wfe.db.WfLineDao;


/**
 * 解析工作流线定义文件，并将解析结果保存到数据库中
 *
 */
public class WFParserLine extends WFParserBase {
	private static Log log = LogFactory.getLog(WFParserLine.class);
    
	private Element lineDef;
	
	private Bean lineDefBean = new Bean();
	
	/**
	 * 
	 * @return 连线定义bean
	 */
	public Bean getLineDefBean() {
		return lineDefBean;
	}

	/**
	 * 
	 * @return xml中连线定义
	 */
    public Element getLineDef() {
		return lineDef;
	}

	/**
     * 解析工作流连线定义
     * @param line xml中连线定义
     * @param cmpyId 公司ID
     * @param wfProcCode 流程编码
     */
    public WFParserLine(Element line, String cmpyId , String wfProcCode) {
        super(cmpyId, wfProcCode);
		
        this.lineDef = line;
    }

    /**
     * 解析工作流 连线定义
     */
	public void parse() {
		String lineDefStr = lineDef.getText();
		lineDefBean = JsonUtils.toBean(lineDefStr);
		
		if (!lineDefBean.isEmpty("LINE_CONDS_SCRIPT")) {
			
            if (lineDefBean.getBoolean("BASE64_ENCODE")) {
                lineDefBean.set("LINE_CONDS_SCRIPT", Lang.decodeBase64(lineDefBean.getStr("LINE_CONDS_SCRIPT")));
            } else if (lineDefBean.getBoolean("HEX_ENCODE")) {
                lineDefBean.set("LINE_CONDS_SCRIPT", decodeHex(lineDefBean.getStr("LINE_CONDS_SCRIPT")));
            } else {
                lineDefBean.set("LINE_CONDS_SCRIPT", unescape(lineDefBean.getStr("LINE_CONDS_SCRIPT")));
            }
		}
		
        lineDefBean.set("PROC_CODE", this.getWfProcCode());
        lineDefBean.set("S_CMPY", this.getCmpyID());
        lineDefBean.set("UPDATE_EXPRESS", decodeHex(lineDefBean.getStr("UPDATE_EXPRESS")));
        lineDefBean.set("CONFIRM_MSG", decodeHex(lineDefBean.getStr("CONFIRM_MSG")));
        lineDefBean.set("COND_MSG", decodeHex(lineDefBean.getStr("COND_MSG")));
        lineDefBean.set("ORG_DEF", decodeHex(lineDefBean.getStr("ORG_DEF")));
        
		saveLineDef();
	}

	/**
	 * 将连线定义保存到数据库中
	 */
	private void saveLineDef() {
		lineDefBean = WfLineDao.insertLineDef(lineDefBean);
		log.debug("parse line complete, the id = " + lineDefBean.getId());
	}
    

    /**
     * 
     * @param src 源字符串
     * @return 解码之后的串
     */
	public static String unescape(String src) {
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0;
		char ch;
		while (lastPos < src.length()) {
			pos = src.indexOf("%", lastPos);
			if (pos == lastPos) {
				if (src.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(
							src.substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(
							src.substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					tmp.append(src.substring(lastPos));
					lastPos = src.length();
				} else {
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}


}
