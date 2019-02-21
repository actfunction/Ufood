/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.comm;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.serv.ServMgr;
import com.rh.core.util.Constant;

/**
 * file service extends <CODE>CommonServ</CODE>
 * 
 * 使用前需要配置各个服务的文件保存路径，如果没有配置将使用默认目录。 配置格式 ${servId}_FILE_PATH=${path}
 * (例：SY_TEST_FILE_PATH=/home/admin/doc/SY_TEST/)
 * @author wanglong
 * 
 */
public class FileServ extends CommonServ {

	/** service Id */
	private static final String CURRENT_SERVICE = FileMgr.CURRENT_SERVICE;

	/**
	 * 查询文件
	 * @param param 参数bean
	 * @return 结果bean
	 */
	public OutBean finds(ParamBean param) {
        if (param.isEmpty("DATA_ID")) {
            param.set("DATA_ID", "undefined");
        }
	    param.set("serv", CURRENT_SERVICE);
	    return super.finds(param);
	}

	/**
	 * 更新File
	 * @param param 参数Bean 必须包含FILE_IDS(多个文件ID以","分隔)
	 * @return 结果Bean
	 */
	public OutBean update(ParamBean param) {
		String condition = getCondition(param);
		ServDao.updates(CURRENT_SERVICE, param, new Bean().set(Constant.PARAM_WHERE, condition));
		return new OutBean().setOk();
	}

	/**
	 * 删除 File
	 * @param param 参数Bean 必须包含FILE_IDS(多个文件ID以","分隔)
	 * @return 结果Bean
	 * TODO 将删除部分代码迁移至FileMgr
	 * TODO 删除历史文件
	 */
	public OutBean delete(ParamBean param) {
		// param validate
		if (0 == param.getId().length()) {
			if (0 == param.getStr("SERV_ID").length()) {
				throw new TipException(Context.getSyMsg("SY_PARAM_FUNC_ERROR", "SERV_ID"));
			}
			if (0 == param.getStr("DATA_ID").length()) {
				throw new TipException(Context.getSyMsg("SY_PARAM_FUNC_ERROR", "DATA_ID"));
			}
		} else {
			String condition = getCondition(param);
			param.set(Constant.PARAM_WHERE, condition);
            if (param.contains(Constant.PARAM_PRE_VALUES)) {
                param.remove(Constant.PARAM_PRE_VALUES);
            }
		}
		List<Bean> files = ServDao.finds(CURRENT_SERVICE, param);
	//	ServDao.deletes(CURRENT_SERVICE, param);
		int count = FileMgr.deleteFile(files);
		
		OutBean out = new OutBean();
		out.setOk(Context.getSyMsg("SY_DELETE_OK", String.valueOf(count)));
		return out;
	}

	/**
	 * get sql condition
	 * @param param param bean
	 * @return sql condition
	 */
	private String getCondition(ParamBean param) {
		String fileIds = param.getId();
		String[] ids = fileIds.split(",");
		if (ids == null || 0 == ids.length) {
			throw new TipException(Context.getSyMsg("SY_PARAM_FUNC_ERROR", "FILE_ID"));
		}
		String condition = " AND FILE_ID in (";
		for (String id : ids) {
			condition += "'";
			condition += id;
			condition += "',";
		}
		condition = condition.substring(0, condition.length() - 1);
		condition += ")";
		return condition;
	}
	
	/**
	 * 复制指定FileID的文件到另一个指定服务（指定DataID）中
	 * @param param param bean
	 * @return 新文件Bean
	 */
    public OutBean copyFile(ParamBean param) {
        String fileID = param.getStr("OLD_FILE_ID");
        Bean fileBean = ServDao.find("SY_COMM_FILE", fileID);
        
        if (param.isNotEmpty("DEL_FILE_ID")) {
            ParamBean delBean = new ParamBean(param.getServId(), ServMgr.ACT_DELETE, param.getStr("DEL_FILE_ID"));
            ServMgr.act(delBean);
        }
        
        InputStream is = null;
        OutBean resultBean = null;
        try {
            is = FileMgr.download(fileBean);
            resultBean = new OutBean(FileMgr.upload(param.getStr("NEW_SERV_ID"), param.getStr("NEW_DATA_ID"),
                    param.getStr("NEW_FILE_CAT"), is, fileBean.getStr("FILE_NAME")));
            IOUtils.closeQuietly(is);
            is = null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
            is = null;
        }
        resultBean.setOk();
        return resultBean;
    }
    
    /**
     * 显示文档的自定义页面，直接显示正文
     * @param paramBean 参数Bean
     * @return 成文模板列表
     */
    public OutBean show(Bean paramBean) {
        boolean isMobile = false;
        String dataId = paramBean.getId();
        String servId = paramBean.getStr("SERV_ID");       
        OutBean out = new OutBean();
        out.set("dataId", dataId);
        out.set("servId", servId);
        // SHOW_TYPE为配置显示的文件分类，逗号分隔
//        if (paramBean.contains("SHOW_TYPE")) {
//            String[] showType = paramBean.getStr("SHOW_TYPE").split(",");
//        }
        if (isMobile) {
            out.setToDispatcher("/sy/comm/file/jsp/show_mb.jsp");
        } else {
            out.setToDispatcher("/sy/comm/file/jsp/show.jsp");
        }
        return out;
    }

}
