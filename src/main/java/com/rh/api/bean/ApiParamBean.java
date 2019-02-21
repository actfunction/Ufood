package com.rh.api.bean;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.util.Constant;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.RequestUtils;

public class ApiParamBean extends Bean {

    /** UID */
    private static final long serialVersionUID = -1317751968474123018L;

    /** log */
    private static Log log = LogFactory.getLog(ApiParamBean.class);

    /** APPID:应用ID */
    public static final String APPID = "appId";
    /** IFID:接口编码 */
    public static final String IFID = "ifId";
    /** ACT:操作方法 */
    public static final String ACT = "act";
    /** TIME_STAMP:接口调用时间 */
    public static final String TIME_STAMP = "timeStamp";
    /** ACC_USER:接口调用约定用户名 */
    public static final String ACC_USER = "accUser";
    /** ACC_PWD:接口调用约定密码 */
    public static final String ACC_PWD = "accPwd";

    /** REQ_DATA:请求参数 */
    public static final String REQ_DATA = "reqData";

    /**
     * 将客户端request对象中的参数直接转换为paramBean对象，缺省从reader中读取
     * 
     * @param request
     *            客户端请求
     */
    public ApiParamBean(HttpServletRequest request) {
	this(request, true);
    }

    /**
     * 将客户端request对象中的参数直接转换为paramBean对象，先取request中键值data的json格式数据，再取其他参数数据
     * 
     * @param request
     *            客户端请求
     * @param readerFlag
     *            是否从reader中读取参数
     */
    public ApiParamBean(HttpServletRequest request, boolean readerFlag) {
	String data = null;
	String device = request.getHeader("X-DEVICE-NAME");
	if (device != null) { // 客户端请求
	    if (readerFlag) {
		try { // 预处理request payload信息
		    data = IOUtils.toString(request.getReader());
		    if (data != null && !data.isEmpty()) {
			this.putAll(JsonUtils.toBean(data));
		    }
		} catch (Exception e) {
		    log.debug(e.getMessage(), e);
		    data = null;
		}
	    } else {
		data = request.getParameter("data");
	    }
	} else { // 本身前端请求
	    data = request.getParameter("data"); // 预处理data参数
	}
	if (data != null) {
	    this.putAll(JsonUtils.toBean(data));
	}

	// 获取其他参数信息
	Enumeration<String> paramNames = request.getParameterNames();
	while (paramNames.hasMoreElements()) {
	    String pName = paramNames.nextElement();
	    if (!pName.equals("data")) { // 不再处理data中的数据
		String value = RequestUtils.getStr(request, pName);
		if (pName.equals(Constant.KEY_ID)) { // 预处理直接传主键的机制
		    this.setId(value);
		} else {
		    this.set(pName, value);
		}
	    }
	}
	this.remove(Constant.PARAM_JSON_RANDOM); // 去掉客户端的 随机参数
    }

    public String getAppid() {
	return this.getStr(APPID);
    }

    public String getIfid() {
	return this.getStr(IFID);
    }

    public String getAct() {
	return this.getStr(ACT);
    }

    public String getTimeStamp() {
	return this.getStr(TIME_STAMP);
    }

    public String getAccUser() {
	return this.getStr(ACC_USER);
    }

    public String getAccPwd() {
	return this.getStr(ACC_PWD);
    }

    public Bean getReqData() {
	return this.getBean(REQ_DATA);
    }

}
