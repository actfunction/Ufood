package com.rh.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;
import com.rh.api.serv.IFileApiServ;
import com.rh.api.serv.IRhServ;
import com.rh.api.serv.ISendApiServ;
import com.rh.core.base.Bean;
import com.rh.core.comm.CacheMgr;
import com.rh.core.comm.mind.MindServ;
import com.rh.core.comm.mind.MindUtils;
import com.rh.core.org.UserBean;
import com.rh.core.org.mgr.UserMgr;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServMgr;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.Strings;
import com.rh.core.util.var.VarMgr;

public class RhServImpl implements IRhServ {

	private static Log log = LogFactory.getLog(RhServImpl.class);

	/** 得到列表所需数据 */
	@Override
	public ApiOutBean getServListParam(ApiParamBean paramBean) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = paramBean.getStr("servId");
		String dataId = paramBean.getStr("dataId");
		String nid = paramBean.getStr("nid");
		String sendId = paramBean.getStr("sendId");
		String _AGENT_USER_ = paramBean.getStr("_AGENT_USER_");
		// 封装所有参数
		Bean result = new Bean();

		// 服务定义
		Bean servBean = ServMgr.servDef(servId);
		result.set("servDef", servBean);

		// 表单数据
		ParamBean pBean = new ParamBean();
		pBean.setId(dataId);
		pBean.set("NI_ID", nid);
		pBean.set("SEND_ID", sendId);
		pBean.set("_AGENT_USER_", _AGENT_USER_);
		OutBean dataBean = ServMgr.act(servId, "byid", pBean);
		result.set("dataVal", dataBean);

		if (!Strings.isEmpty(dataId)) {
			// 意见列表
			List<Bean> mindList = MindUtils.getMindList(null, dataId, MindServ.MIND_SORT_TIME);
			List<Bean> newlist = new ArrayList<Bean>();
			for (Bean bean : mindList) {
				Bean mindBean = new Bean();
				UserBean user = UserMgr.getUser(bean.getStr("S_USER"));
				mindBean.set("mindId", bean.getStr("MIND_ID"));
				mindBean.set("mindContent", bean.getStr("MIND_CONTENT"));
				mindBean.set("mindType", bean.getStr("MIND_TYPE"));
				mindBean.set("mindDisRule", bean.getStr("MIND_DIS_RULE"));
				mindBean.set("mindCode", bean.getStr("MIND_CODE"));
				mindBean.set("mindTypeName", DictMgr.getItem("SY_COMM_MIND_TYPE", bean.getStr("MIND_TYPE")));
				mindBean.set("mindTime", bean.getStr("S_MTIME"));
				mindBean.set("userCode", bean.getStr("S_USER"));
				mindBean.set("userName", bean.getStr("S_UNAME"));
				mindBean.set("userPost", user.getPost());
				mindBean.set("userImg", user.getImgSrc());
				mindBean.set("deptCode", bean.getStr("S_DEPT"));
				mindBean.set("deptName", bean.getStr("S_DNAME"));
				mindBean.set("wfNId", bean.getStr("WF_NI_ID"));
				mindBean.set("mindCodeName", bean.getStr("MIND_CODE_NAME"));
				mindBean.set("wfNName", bean.getStr("WF_NI_NAME"));
				mindBean.set("mindFile", bean.getStr("MIND_FILE"));
				mindBean.set("userAutoGraph", user.getStr("USER_AUTOGRAPH"));
				mindBean.set("isBD", bean.getStr("IS_BD"));
				mindBean.set("bdUser", bean.getStr("BD_USER"));
				mindBean.set("bdUserName", bean.getStr("BD_UNAME"));
				newlist.add(mindBean);
			}
			result.set("mindList", newlist);

			// 文件列表
			IFileApiServ fileApiServ = new FileApiServImpl();
			ApiOutBean fileOutBean = fileApiServ.getFileListByDataId(new Bean().set("dataId", dataId));
			List<Bean> fileList = fileOutBean.getData().getList("list");
			result.set("fileList", fileList);

			ISendApiServ sendApiServ = new SendApiServImpl();
			// 送阅列表
			result.set("readList",
					sendApiServ.getReadList(new Bean().set("dataId", dataId)).getData().getList("readList"));

			// 分发列表
			result.set("distrList",
					sendApiServ.getDistrList(new Bean().set("dataId", dataId)).getData().getList("distrList"));

			// 相关文件列表
			List<Bean> relateList = ServMgr.act("SY_SERV_RELATE", ServMgr.ACT_QUERY,
					new ParamBean().setWhere(" and DATA_ID = '" + dataId + "'")).getDataList();
			result.set("relateList", relateList);
		} else {
			result.set("mindList", new Bean());
			result.set("fileList", new Bean());
			result.set("distrList", new Bean());
			result.set("relateList", new Bean());
		}

		// 系统级参数
		result.set("sysParams", JsonUtils.mapsToJson(VarMgr.getOrgMap(), VarMgr.getConfMap(), VarMgr.getDateMap()));

		outBean.setData(result);
		return outBean;
	}

	/** 模板预览时用到,得到服务定义信息 */
	@Override
	public ApiOutBean getServTmpl(ApiParamBean paramBean) {
		ApiOutBean outBean = new ApiOutBean();
		String servId = paramBean.getStr("servId");
		// 封装所有参数
		Bean result = new Bean();

		// 服务定义
		Bean servBean = ServMgr.servDef(servId);
		result.set("servDef", servBean);

		// 表单数据
		OutBean dataBean = ServMgr.act(servId, "byid", new ParamBean());
		result.set("dataVal", dataBean);
		// 意见列表等
		result.set("mindList", new Bean());
		result.set("fileList", new Bean());
		result.set("distrList", new Bean());
		result.set("relateList", new Bean());
		// 系统级参数
		result.set("sysParams", JsonUtils.mapsToJson(VarMgr.getOrgMap(), VarMgr.getConfMap(), VarMgr.getDateMap()));
		// 保存返回数据
		outBean.setData(result);
		// 避免内存浪费，这里清空下缓存
		CacheMgr.getInstance().remove(servId, ServUtils.CACHE_TYPE_SERV);
		return outBean;
	}
}
