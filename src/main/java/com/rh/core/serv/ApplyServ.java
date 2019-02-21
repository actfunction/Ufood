package com.rh.core.serv;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.rh.core.base.BaseContext.APP;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;
import com.rh.core.wfe.db.WfProcDefDao;
import com.rh.core.wfe.def.WFParser;
import com.rh.core.wfe.def.WfServCorrespond;
import com.rh.core.wfe.util.WfeConstant;

public class ApplyServ extends CommonServ {
	
	private static final String SERV_SY_SERV = "SY_SERV";
	
	private static final String SERV_SY_WF = "SY_WFE_PROC_DEF";
	
	public OutBean copyApply(ParamBean paramBean) {
		try {
//			Transaction.begin();
			copyForm(paramBean);
			copyWfe(paramBean);
			OutBean rtnBean = new OutBean();
			rtnBean.setOk(Context.getSyMsg("SY_SAVE_OK"));
			return rtnBean;
		} catch (Exception e) {
			throw new TipException("创建流程错误:" + e.getMessage());
		} finally {
//			Transaction.end();
		}
	}
	
	public OutBean copyWfe(ParamBean paramBean) {
		try {
			String newServId = paramBean.getStr("SWF_CODE");
			String newServName = paramBean.getStr("SWF_NAME");
			String fromServId = paramBean.getStr("SWF_TYPE");

			ParamBean param = new ParamBean();
			param.put("SERV_ID", newServId);
			Bean wfBean = ServDao.find(SERV_SY_WF, param);

			if (wfBean != null) {
				throw new TipException("流程编码" + newServId + "已存在!");
			}

			param = new ParamBean();
			param.put("EN_NAME", fromServId);
			Bean newBean = ServDao.find(SERV_SY_WF, param);
			String oldProcCode = newBean.getStr("PROC_CODE");

			// paramBean.copyFrom(fromWfBean);
			// Bean newBean = new Bean();
			newBean.put("PROC_CODE", newServId + "@" + Context.getCmpy() + "@1"); // 流程主键
			newBean.put("EN_NAME", newServId); // 流程编码
			newBean.put("PROC_NAME", newServName);// 流程名称
			newBean.put("SERV_ID", newServId);// 表单服务编码
			newBean.put("PROC_VERSION", 1);
			newBean.put("PROC_IS_LATEST", WfeConstant.PROC_IS_LATEST);
			// 将 paramBean 中的值 转成ProcDefBean
			String wfXmlStr = newBean.getStr("PROC_XML");
			wfXmlStr = wfXmlStr.replaceAll("gb2312", "UTF-8").replaceAll(fromServId, newServId);
			newBean.put("PROC_XML", wfXmlStr);

			UserBean userBean = Context.getUserBean();

			// procCode为流程定义主键
			String procCode = newBean.getStr("PROC_CODE");
			Bean oldProcBean = WfProcDefDao.getWfProcBeanByProcCode(procCode);
			if (null != oldProcBean) {
				String oldServId = oldProcBean.getStr("SERV_ID");
				clearCache(oldServId);
			}

			WFParser myParser = new WFParser(userBean.getCmpyCode(), newBean);

			myParser.setOldProcCode(procCode);
			// 保存定义文件
			wfXmlStr = wfXmlStr.replaceAll("\r\n", "");
			myParser.setDefContent(wfXmlStr);

			procCode = newBean.getStr("EN_NAME") + WfeConstant.PROC_CMPY_PREFIX + Context.getUserBean().getCmpyCode()
					+ WfeConstant.PROC_VERSION_PREFIX + newBean.getStr("PROC_VERSION");
			myParser.setProcCode(procCode);
			myParser.save();

			// 流程公共按钮
			param = new ParamBean();
			param.put("PROC_CODE", oldProcCode);
			Bean shareBtn = ServDao.find("SY_WFE_NODE_PACTS", param);
			shareBtn.set("PROC_CODE", newBean.getStr("PROC_CODE"));
			shareBtn.setId("");
			shareBtn.set("PACT_ID", "");
			ServDao.create("SY_WFE_NODE_PACTS", shareBtn);

			// 刷新流程 服务对应关系的缓存
			WfServCorrespond.removeFromCache(newServId);
			if (null != oldProcBean) {
				WfServCorrespond.removeFromCache(oldProcBean.getStr("SERV_ID"));
			}

			OutBean rtnBean = new OutBean();
			rtnBean.setOk(Context.getSyMsg("SY_SAVE_OK"));
			rtnBean.set(Constant.RTN_DATA, new Bean().set("PROC_CODE", procCode));
			return rtnBean;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new TipException(e.getMessage());
		}
	}
	
	
	
	/**
	 * 复制流程审批
	 * @param paramBean
	 * @return
	 */
	public OutBean copyForm(ParamBean paramBean) {
        String newServId = paramBean.getStr("SWF_CODE");
		String newServName = paramBean.getStr("SWF_NAME");
        String fromServId = paramBean.getStr("SWF_TYPE");
        
        Bean bean = ServDao.find(SERV_SY_SERV, newServId);
        
        if(bean != null){
        	throw new TipException("服务编码"+newServId+"已存在!");
        }
        
        Bean fromServ = ServUtils.getServData(fromServId);
        int count = 0;
        //导入item
        List<Bean> dataList = (List<Bean>) fromServ.get(ServUtils.TABLE_SERV_ITEM);
        for (Bean data : dataList) {
            data.setId("");
            data.set("ITEM_ID", "");
            data.set("SERV_ID", newServId);
            try {
                if (ServDao.create(ServUtils.TABLE_SERV_ITEM, data) != null) {
                    count++;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        //导入act
        dataList = (List<Bean>) fromServ.get(ServUtils.TABLE_SERV_ACT);
        for (Bean data : dataList) {
            data.setId("");
            data.set("ACT_ID", "");
            data.set("SERV_ID", newServId);
            try {
                if (ServDao.create(ServUtils.TABLE_SERV_ACT, data) != null) {
                    count++;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        //导入where规则
        dataList = (List<Bean>) fromServ.get(ServUtils.TABLE_SERV_WHERE);
        for (Bean data : dataList) {
            data.setId("");
            data.set("WHERE_ID", "");
            data.set("SERV_ID", newServId);
            try {
                if (ServDao.create(ServUtils.TABLE_SERV_WHERE, data) != null) {
                    count++;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        //导入link
        dataList = fromServ.getList(ServUtils.TABLE_SERV_LINK);
        for (Bean data : dataList) {
            data.setId("");
            data.set("LINK_ID", "");
            data.set("SERV_ID", newServId);
            Bean newData = null;
            try {
                newData = ServDao.create(ServUtils.TABLE_SERV_LINK, data); 
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            if (newData != null) {
                count++;
                if (data.contains(ServUtils.TABLE_SERV_LINK_ITEM)) {
                    List<Bean> items = (List<Bean>) data.get(ServUtils.TABLE_SERV_LINK_ITEM);
                    for (Bean item : items) {
                        item.setId("");
                        item.set("LITEM_ID", "");
                        item.set("LINK_ID", newData.getStr("LINK_ID"));
                        if (ServDao.create(ServUtils.TABLE_SERV_LINK_ITEM, item) != null) {
                            count++;
                        }
                    }
                }
            } //end if
        } //end for
        fromServ.setId(newServId);
        fromServ.set("SERV_ID", newServId);
        fromServ.set("SERV_NAME", newServName);
		fromServ.set("SERV_SQL_WHERE", "and SERV_ID = '" + newServId + "'");
        ServDao.create(SERV_SY_SERV, fromServ);
        
        //复制卡片js
		if (fromServ.getInt("SERV_CARD_LOAD") == 1) {
			copyServJs(fromServId,newServId,"card");
	      }
	      //复制列表js
		if (fromServ.getInt("SERV_LIST_LOAD") == 1) {
			copyServJs(fromServId,newServId,"list");
	      }
      		
        OutBean outBean = new OutBean();
        outBean.setOk(Context.getSyMsg("SY_IMPORT_OK", String.valueOf(count)));
        return outBean;
	}
	
	/**
	 * 复制卡片、列表js
	 * @param srcServID
	 * @param destServID
	 * @param type
	 */
	private void copyServJs(String srcServID, String destServID, String type) {
		String srcModule = srcServID.substring(0, srcServID.indexOf("_")).toLowerCase();
		String destModule = destServID.substring(0, destServID.indexOf("_")).toLowerCase();
		String srcPath = Context.appStr(APP.SYSPATH) + srcModule + "/servjs/" + srcServID + "_" + type + ".js";
		String destPath = Context.appStr(APP.SYSPATH) + destModule + "/servjs/" + destServID + "_" + type + ".js";
		File srcFile = new File(srcPath);
		if (srcFile.exists()) {
			File destFile = new File(destPath);
			try {
				FileUtils.copyFile(srcFile, destFile);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	 /**
     * 清除指定服务的流程定义缓存， 在流程定义有变动时调用
     * @param servId 服务Id
     */
    private void clearCache(String servId) {
        String procKey = "_WF_MAP";
        ServDefBean servDef = ServUtils.getServDef(servId);
        servDef.remove(procKey);
    }

}
