package com.rh.api.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.rh.api.BaseApiServ;
import com.rh.api.anno.URLAnno;
import com.rh.api.bean.ApiOutBean;
import com.rh.api.bean.ApiParamBean;
import com.rh.api.impl.FileApiServImpl;
import com.rh.api.impl.FormInfoApiServImpl;
import com.rh.api.impl.GwApiServImpl;
import com.rh.api.impl.GwControlApiServImpl;
import com.rh.api.impl.MenuServImpl;
import com.rh.api.impl.MindApiServImpl;
import com.rh.api.impl.RemindApiServImpl;
import com.rh.api.impl.RhServImpl;
import com.rh.api.impl.SendApiServImpl;
import com.rh.api.impl.WfeApiServImpl;
import com.rh.api.impl.stats.DeptStatsServImpl;
import com.rh.api.impl.stats.OrgStatsServImpl;
import com.rh.api.impl.stats.StatsServImpl;
import com.rh.api.impl.stats.UserStatsServImpl;
import com.rh.api.serv.IDataStatsServ;
import com.rh.api.serv.IFileApiServ;
import com.rh.api.serv.IFormInfoApiServ;
import com.rh.api.serv.IGwApiServ;
import com.rh.api.serv.IGwControlApiServ;
import com.rh.api.serv.IMenuApiServ;
import com.rh.api.serv.IMindApiServ;
import com.rh.api.serv.IRemindApiServ;
import com.rh.api.serv.IRhServ;
import com.rh.api.serv.ISendApiServ;
import com.rh.api.serv.IWfeApiServ;
import com.rh.api.util.ApiConstant;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.base.TipException;
import com.rh.core.org.UserBean;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.RequestUtils;
import com.rh.core.util.var.VarMgr;

import com.rh.gw.util.GwUtils;

public class ApiServlet extends HttpServlet {

	/** UID */
	private static final long serialVersionUID = 4075153275125051860L;

	/** log */
	private static Log log = LogFactory.getLog(ApiServlet.class);

	/**
	 * 请求处理
	 * 
	 * @param request  请求头
	 * @param response 响应头
	 * @throws ServletException ServletException
	 * @throws IOException      IOException
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ApiOutBean outBean = null;
		String uri = request.getRequestURI();
		String[] sa = uri.substring(uri.lastIndexOf("/") + 1).split("\\.");

		if (uri.indexOf("/app") >= 0 || uri.indexOf("/showForm") >= 0 || uri.indexOf("/urgeForm") >= 0) {
			request.getRequestDispatcher("/app.html").forward(request, response);
			return;
		}

		try {
			if (!RequestUtils.getStr(request, "noAuth").equals("yes")) {
				anthUser(request, response);
			}

			if (uri.indexOf("/getWfeImg") >= 0) {
				ApiParamBean paramBean = new ApiParamBean(request);
				download(paramBean, response);
				return;
			}

			for (Method method : this.getClass().getMethods()) {
				if (method.isAnnotationPresent(URLAnno.class)) {
					URLAnno urlAnno = method.getAnnotation(URLAnno.class);
					if (urlAnno.value().equals(sa[0])) {
						ApiParamBean paramBean = new ApiParamBean(request);
						outBean = (ApiOutBean) method.invoke(this, paramBean);
						break;
					}
				}
			}
		} catch (InvocationTargetException e) {
			log.error(e.getMessage(), e);
			outBean = new ApiOutBean();
			outBean.setCode(ApiConstant.RTN_CODE_ENUM.CODE_002.getCode());
			outBean.setMessage(ApiConstant.RTN_CODE_ENUM.CODE_002.getValue());
			outBean.setData(new Bean().set(ApiOutBean.ERR_INFO, e.getTargetException().getMessage()));
		} catch (TipException e) {
			log.error(e.getMessage(), e);
			outBean = new ApiOutBean();
			outBean.setCode(ApiConstant.RTN_CODE_ENUM.CODE_002.getCode());
			outBean.setMessage(ApiConstant.RTN_CODE_ENUM.CODE_002.getValue());
			outBean.setData(new Bean().set(ApiOutBean.ERR_INFO, e.getMessage()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			outBean = new ApiOutBean();
			outBean.setCode(ApiConstant.RTN_CODE_ENUM.CODE_002.getCode());
			outBean.setMessage(ApiConstant.RTN_CODE_ENUM.CODE_002.getValue());
			outBean.setData(new Bean().set(ApiOutBean.ERR_INFO, e.getMessage()));
		}

		if (!response.isCommitted()) {
			String result = outBean.output();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gout = new GZIPOutputStream(bos);
			gout.write(result.getBytes("UTF-8"));
			gout.close();
			byte dest[] = bos.toByteArray();
			response.setHeader("Content-Encoding", "gzip");// 告诉浏览器，当前发送的是gzip格式的内容
			response.setContentType("text/json; charset=utf-8");
            String myOrigin = request.getHeader("origin");
    		String allowOrigin = GwUtils.ifWhiteList(myOrigin);
    		response.setHeader("Access-Control-Allow-Origin", allowOrigin);
			response.setHeader("Access-Control-Allow-Methods", "POST,GET");
			response.setHeader("Access-Control-Allow-Headers", "X-Requested-With,content-type");
			OutputStream out = response.getOutputStream();
			out.write(dest);
			out.flush();
			out.close();
		}
	}

    /**
     * 判断用户
     * @param request request
     * @param response response
     * @return userBean
     * @throws TipException TipException
     * @throws IOException IOException
     */
    private UserBean anthUser(HttpServletRequest request, HttpServletResponse response)
			throws TipException, IOException {
		UserBean userBean = Context.getUserBean(request);
		if (userBean == null) {
			RequestUtils.sendDisp(request, response, "/sy/comm/login/jumpToIndex.jsp");
			response.flushBuffer();
		}

		return userBean;
	}

	@URLAnno(value = "getFormLayout")
	public ApiOutBean getFormLayout(ApiParamBean paramBean) {
		IFormInfoApiServ apiServ = new FormInfoApiServImpl();
		return apiServ.getServDef(paramBean.getStr("servId"));
	}

	@URLAnno(value = "toHref")
	public ApiOutBean toHref(ApiParamBean paramBean) {
		ApiOutBean outBean = new ApiOutBean();
		return outBean;
	}

	@URLAnno(value = "getFormLayoutAndData")
	public ApiOutBean getFormLayoutAndData(ApiParamBean paramBean) {
		IFormInfoApiServ apiServ = new FormInfoApiServImpl();
		return apiServ.getServDefAndData2(paramBean);
	}

	@URLAnno(value = "getFormLayoutAndData2")
	public ApiOutBean getFormLayoutAndData2(ApiParamBean paramBean) {
		IFormInfoApiServ apiServ = new FormInfoApiServImpl();
		return apiServ.getServDefAndData2(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getWfeTrack")
	public ApiOutBean getWfeTrack(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.getWfeTrack(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getWfeTrackForPC")
	public ApiOutBean getWfeTrackForPC(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.getWfeTrackForPC(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getFileListByDataId")
	public ApiOutBean getFileListByDataId(ApiParamBean paramBean) {
		IFileApiServ apiServ = new FileApiServImpl();
		return apiServ.getFileListByDataId(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getDict")
	public ApiOutBean getDict(ApiParamBean paramBean) {
		IFormInfoApiServ apiServ = new FormInfoApiServImpl();
		return apiServ.getDict(paramBean.getStr("dictId"));
	}

	@URLAnno(value = "getDictTreeData")
	public ApiOutBean getDictTreeData(ApiParamBean paramBean) {
		IFormInfoApiServ apiServ = new FormInfoApiServImpl();
		return apiServ.getDictTreeData(paramBean.getStr("dictId"), paramBean.getStr("pid"));
	}

	@URLAnno(value = "getMindListByDataId")
	public ApiOutBean getMindListByDataId(ApiParamBean paramBean) {
		IMindApiServ apiServ = new MindApiServImpl();
		return apiServ.getMindListByDataId(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getMindListByDataIdForRule")
	public ApiOutBean getMindListByDataIdForRule(ApiParamBean paramBean) {
		IMindApiServ apiServ = new MindApiServImpl();
		return apiServ.getMindListByDataIdForRule(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "inputMind")
	public ApiOutBean inputMind(ApiParamBean paramBean) {
		IMindApiServ apiServ = new MindApiServImpl();
		return apiServ.inputMind(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getWfeBtn")
	public ApiOutBean getWfeBtn(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.getWfeBtn(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "duZhan")
	public ApiOutBean duZhan(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.duZhan(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "qianShou")
	public ApiOutBean qianShou(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.qianShou(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "qianShou2Shouwen")
	public ApiOutBean qianShou2Shouwen(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.qianShou2Shouwen(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "wfeSend")
	public ApiOutBean wfeSend(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.wfeSend(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getOftenUseMindList")
	public ApiOutBean getOftenUseMindList(ApiParamBean paramBean) {
		IMindApiServ apiServ = new MindApiServImpl();
		return apiServ.getOftenUseMindList(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "addOftenMind")
	public ApiOutBean addOftenMind(ApiParamBean paramBean) {
		IMindApiServ apiServ = new MindApiServImpl();
		return apiServ.addOftenMind(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "finish")
	public ApiOutBean finish(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.finish(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "undoFinish")
	public ApiOutBean undoFinish(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.undoFinish(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "withdraw")
	public ApiOutBean withdraw(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.withdraw(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "stopParallelWf")
	public ApiOutBean stopParallelWf(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.stopParallelWf(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "deleteDoc")
	public ApiOutBean deleteDoc(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.deleteDoc(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "saveWfeImg")
	public ApiOutBean saveWfeImg(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		Bean bean = new Bean();
		String reqDataString = paramBean.getStr("reqData");
		JSONObject obj = JSONObject.parseObject(reqDataString);
		bean.set("servId", obj.getString("servId"));
		bean.set("imageStr", obj.getString("image"));
		return apiServ.saveWfeImg(bean);
	}

	@URLAnno(value = "save")
	public ApiOutBean save(ApiParamBean paramBean) {
		IFormInfoApiServ apiServ = new FormInfoApiServImpl();
		return apiServ.save(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "saveResultData")
	public ApiOutBean saveResultData(ApiParamBean paramBean) {
		IFormInfoApiServ apiServ = new FormInfoApiServImpl();
		return apiServ.saveResultData(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "delete")
	public ApiOutBean delete(ApiParamBean paramBean) {
		IFormInfoApiServ apiServ = new FormInfoApiServImpl();
		return apiServ.delete(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getUserBeanByUserCode")
	public ApiOutBean getUserBeanByUserCode(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.getUserBeanByUserCode(paramBean.getStr("userCode"));
	}

	@URLAnno(value = "getWfePercent")
	public ApiOutBean getWfePercent(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.getWfePercent(paramBean);
	}

	private void download(ApiParamBean paramBean, HttpServletResponse response) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		try {
			ApiOutBean outBean = apiServ.getWfeImg(paramBean.getStr("servId"));
			OutputStream out = response.getOutputStream();
			FileInputStream is = new FileInputStream(new File(outBean.getStr("imgPath")));
			IOUtils.copyLarge(is, out);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(out);
			out.flush();
		} catch (Exception e) {
			throw new TipException(e.getMessage());
		}
	}

	@URLAnno(value = "getUserTabDataByStime")
	public ApiOutBean getUserTabDataByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new UserStatsServImpl();
		return apiServ.getUserTabDataByStime(paramBean);
	}

	@URLAnno(value = "getUserStatsDataByStime")
	public ApiOutBean getUserStatsDataByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new UserStatsServImpl();
		return apiServ.getUserStatsDataByStime(paramBean);
	}

	@URLAnno(value = "getUser")
	public ApiOutBean getUser(ApiParamBean paramBean) {
		BaseApiServ apiServ = new BaseApiServ();
		return apiServ.getUser(paramBean.getStr("userCode"));
	}

	@URLAnno(value = "getDeptTabDataByStime")
	public ApiOutBean getDeptTabDataByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getDeptTabDataByStime(paramBean);
	}

	@URLAnno(value = "getDeptStatsDataZBByStime")
	public ApiOutBean getDeptStatsDataZBByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getDeptStatsDataZBByStime(paramBean);
	}

	@URLAnno(value = "getOrgTabDataByStime")
	public ApiOutBean getOrgTabDataByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgTabDataByStime(paramBean);
	}

	@URLAnno(value = "getDeptStatsDataHQByStime")
	public ApiOutBean getDeptStatsDataHQByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getDeptStatsDataHQByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsDataSLSXByStime")
	public ApiOutBean getOrgStatsDataSLSXByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsDataSLSXByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsDataQSJByStime")
	public ApiOutBean getOrgStatsDataQSJByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsDataQSJByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsDataBLSJByStime")
	public ApiOutBean getOrgStatsDataBLSJByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsDataBLSJByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsDataPHBByStime")
	public ApiOutBean getOrgStatsDataPHBByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsDataPHBByStime(paramBean);
	}

	@URLAnno(value = "getDeptStatsDataPHBByStime")
	public ApiOutBean getDeptStatsDataPHBByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getDeptStatsDataPHBByStime(paramBean);
	}

	@URLAnno(value = "getUserStatsCharBJLByStime")
	public ApiOutBean getUserStatsCharBJLByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new UserStatsServImpl();
		return apiServ.getUserStatsCharBJLByStime(paramBean);
	}

	@URLAnno(value = "getUserStatsCharWORKByStime")
	public ApiOutBean getUserStatsCharWORKByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new UserStatsServImpl();
		return apiServ.getUserStatsCharWORKByStime(paramBean);
	}

	@URLAnno(value = "getDeptStatsCharBJLByStime")
	public ApiOutBean getDeptStatsCharBJLByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getDeptStatsCharBJLByStime(paramBean);
	}

	@URLAnno(value = "getDeptStatsCharRunningByStime")
	public ApiOutBean getDeptStatsCharRunningByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getDeptStatsCharRunningByStime(paramBean);
	}

	@URLAnno(value = "getDeptStatsCharZBGWCountByStime")
	public ApiOutBean getDeptStatsCharZBGWCountByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getDeptStatsCharZBGWCountByStime(paramBean);
	}

	@URLAnno(value = "getDeptStatsCharHQGWCountByStime")
	public ApiOutBean getDeptStatsCharHQGWCountByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getDeptStatsCharHQGWCountByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsCharBJLByStime")
	public ApiOutBean getOrgStatsCharBJLByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsCharBJLByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsCharRunningByStime")
	public ApiOutBean getOrgStatsCharRunningByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsCharRunningByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsTabGWCountByStime")
	public ApiOutBean getOrgStatsTabGWCountByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsTabGWCountByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsCharSLSXGWCountByStime")
	public ApiOutBean getOrgStatsCharSLSXGWCountByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsCharSLSXGWCountByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsQSJGWCountByStime")
	public ApiOutBean getOrgStatsQSJGWCountByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsQSJGWCountByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsCharQSJGWCountByStime")
	public ApiOutBean getOrgStatsCharQSJGWCountByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsCharQSJGWCountByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsCharBLSJByStime")
	public ApiOutBean getOrgStatsCharBLSJByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsCharBLSJByStime(paramBean);
	}

	@URLAnno(value = "getOrgStatsGWSXByStime")
	public ApiOutBean getOrgStatsGWSXByStime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new OrgStatsServImpl();
		return apiServ.getOrgStatsGWSXByStime(paramBean);
	}

	@URLAnno(value = "send")
	public ApiOutBean send(ApiParamBean paramBean) {
		ISendApiServ apiServ = new SendApiServImpl();
		Bean param = paramBean.getBean("param");
		List<Bean> list = paramBean.getList("reqData");
		return apiServ.send(list, param);
	}

	@URLAnno(value = "sendRead")
	public ApiOutBean sendRead(ApiParamBean paramBean) {
		ISendApiServ apiServ = new SendApiServImpl();
		return apiServ.sendRead(paramBean);
	}

	@URLAnno(value = "undo")
	public ApiOutBean undo(ApiParamBean paramBean) {
		ISendApiServ apiServ = new SendApiServImpl();
		return apiServ.undo(paramBean);
	}

	@URLAnno(value = "getReadList")
	public ApiOutBean getReadList(ApiParamBean paramBean) {
		ISendApiServ apiServ = new SendApiServImpl();
		return apiServ.getReadList(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getDistrList")
	public ApiOutBean getDistrList(ApiParamBean paramBean) {
		ISendApiServ apiServ = new SendApiServImpl();
		return apiServ.getDistrList(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getRemind")
	public ApiOutBean getRemind(ApiParamBean paramBean) {
		IRemindApiServ apiServ = new RemindApiServImpl();
		return apiServ.getRemind(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "saveRemind")
	public ApiOutBean saveRemind(ApiParamBean paramBean) {
		IRemindApiServ apiServ = new RemindApiServImpl();
		return apiServ.saveRemind(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "copyZhengwen")
	public ApiOutBean copyZhengwen(ApiParamBean paramBean) {
		IFormInfoApiServ apiServ = new FormInfoApiServImpl();
		return apiServ.copyZhengwen(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getWfeDelayInfo")
	public ApiOutBean getWfeDelayInfo(ApiParamBean paramBean) {
		IGwApiServ apiServ = new GwApiServImpl();
		return apiServ.getWfeDelayInfo(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getMaxNum")
	public ApiOutBean getMaxNum(ApiParamBean paramBean) {
		IGwApiServ apiServ = new GwApiServImpl();
		return apiServ.getMaxNum(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "cmRedHead")
	public ApiOutBean cmRedHead(ApiParamBean paramBean) {
		IGwApiServ apiServ = new GwApiServImpl();
		return apiServ.cmRedHead(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "saveDelayInfo")
	public ApiOutBean saveDelayInfo(ApiParamBean paramBean) {
		IGwApiServ apiServ = new GwApiServImpl();
		return apiServ.saveDelayInfo(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getWfeBinderByNode")
	public ApiOutBean getWfeBinderByNode(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.getWfeBinderByNode(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getZhengwenList")
	public ApiOutBean getZhengwenList(ApiParamBean paramBean) {
		IGwApiServ apiServ = new GwApiServImpl();
		return apiServ.getZhengwenList(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getGwControlInfo")
	public ApiOutBean getGwControlInfo(ApiParamBean paramBean) {
		IGwControlApiServ apiServ = new GwControlApiServImpl();
		return apiServ.getGwControlInfo(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getRingCount")
	public ApiOutBean getRingCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getRingCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getGwPhbCount")
	public ApiOutBean getGwPhbCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getGwPhbCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getDealCount")
	public ApiOutBean getDealCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getDealCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getGwCount")
	public ApiOutBean getGwCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getGwCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getGwAgingCount")
	public ApiOutBean getGwAgingCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getGwAgingCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getGwBlCount")
	public ApiOutBean getGwBlCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getGwBlCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getZbRingCount")
	public ApiOutBean getZbRingCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getZbRingCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getZbBarCount")
	public ApiOutBean getZbBarCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getZbBarCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getZbCount")
	public ApiOutBean getZbCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getZbCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "rtnQcr")
	public ApiOutBean rtnQcr(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.rtnQcr(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getHqBarCount")
	public ApiOutBean getHqBarCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getHqBarCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getHqCount")
	public ApiOutBean getHqCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getHqCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getGwDealSitu")
	public ApiOutBean getGwDealSitu(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getGwDealSitu(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getGwSxzlCount")
	public ApiOutBean getGwSxzlCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new DeptStatsServImpl();
		return apiServ.getGwSxzlCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getUserGwCount")
	public ApiOutBean getUserGwCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new UserStatsServImpl();
		return apiServ.getUserGwCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getUserRingCount")
	public ApiOutBean getUserRingCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new UserStatsServImpl();
		return apiServ.getUserRingCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getUserGwDealSitu")
	public ApiOutBean getUserGwDealSitu(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new UserStatsServImpl();
		return apiServ.getUserGwDealSitu(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getUserGwRepList")
	public ApiOutBean getUserGwRepList(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new UserStatsServImpl();
		return apiServ.getUserGwRepList(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getUserGwList")
	public ApiOutBean getUserGwList(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new UserStatsServImpl();
		return apiServ.getUserGwList(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getUserMenu")
	public ApiOutBean getUserMenu(ApiParamBean paramBean) {
		IMenuApiServ apiServ = new MenuServImpl();
		return apiServ.getUserMenu(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getUserStatsData")
	public ApiOutBean getUserStatsData(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new StatsServImpl();
		return apiServ.getUserStatsData(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getDeptStatsDataTodo")
	public ApiOutBean getDeptStatsDataTodo(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new StatsServImpl();
		return apiServ.getDeptStatsDataTodo(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getDeptStatsDataTodoHis")
	public ApiOutBean getDeptStatsDataTodoHis(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new StatsServImpl();
		return apiServ.getDeptStatsDataTodoHis(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getOrgGwCount")
	public ApiOutBean getOrgGwCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new StatsServImpl();
		return apiServ.getOrgGwCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getOrgGwDelay")
	public ApiOutBean getOrgGwDelay(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new StatsServImpl();
		return apiServ.getOrgGwDelay(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getOrgSWQsCount")
	public ApiOutBean getOrgSWQsCount(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new StatsServImpl();
		return apiServ.getOrgSWQsCount(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getOrgBLTime")
	public ApiOutBean getOrgBLTime(ApiParamBean paramBean) {
		IDataStatsServ apiServ = new StatsServImpl();
		return apiServ.getOrgBLTime(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getServListParam")
	public ApiOutBean getServListParam(ApiParamBean paramBean) {
		IRhServ apiServ = new RhServImpl();
		return apiServ.getServListParam(paramBean);
	}

	@URLAnno(value = "getServTmpl")
	public ApiOutBean getServTmpl(ApiParamBean paramBean) {
		IRhServ apiServ = new RhServImpl();
		return apiServ.getServTmpl(paramBean);
	}

	/***
	 * 保存表单并返回表单数据-移动端使用
	 * 
	 * @param paramBean
	 * @return
	 */
	@URLAnno(value = "getConfig")
	public ApiOutBean getConfig(ApiParamBean paramBean) {
		ApiOutBean rtnBean = new ApiOutBean();
		String confVal = Context.getSyConf(paramBean.getStr("key"), "");
		rtnBean.setData(new Bean().set(paramBean.getStr("key"), confVal));
		return rtnBean;
	}
	
	/***
	 * 获取所有的系统配置
	 * 
	 * @param paramBean
	 * @return
	 */
	@URLAnno(value = "getAllConfig")
	public ApiOutBean getAllConfig(ApiParamBean paramBean) {
		ApiOutBean rtnBean = new ApiOutBean();
		String sysParams = JsonUtils.mapsToJson(VarMgr.getOrgMap(), VarMgr.getConfMap(), VarMgr.getDateMap());
		rtnBean.setData(new Bean().set("RTN_DATA", sysParams));
		return rtnBean;
	}

	/***
	 * 保存表单并返回表单数据-移动端使用
	 * 
	 * @param paramBean
	 * @return
	 */
	@URLAnno(value = "getLineDefByNode")
	public ApiOutBean getLineDefByNode(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.getLineDefByNode(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getDiscuss")
	public ApiOutBean getDiscuss(ApiParamBean paramBean) {
		IMindApiServ apiServ = new MindApiServImpl();
		return apiServ.getDiscuss(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "disToMind")
	public ApiOutBean disToMind(ApiParamBean paramBean) {
		IMindApiServ apiServ = new MindApiServImpl();
		return apiServ.disToMind(paramBean.getBean("reqData"));
	}

	/** 保存表单并返回表单数据-移动端使用 */
	@URLAnno(value = "checkNodeRunning")
	public ApiOutBean checkNodeRunning(ApiParamBean paramBean) {
		IWfeApiServ apiServ = new WfeApiServImpl();
		return apiServ.checkNodeRunning(paramBean.getBean("reqData"));
	}

	@URLAnno(value = "getCurrUser")
	public ApiOutBean getCurrUser(ApiParamBean paramBean) {
		ApiOutBean rtnBean = new ApiOutBean();
		UserBean currUser = Context.getUserBean();
		currUser.set("ROLE_CODES", currUser.getRoleCodeStr());
		rtnBean.setData(currUser);
		return rtnBean;
	}
}
