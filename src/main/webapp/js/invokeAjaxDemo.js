/*平台级方法定义(FireFly Platform javascript methods defined)*/
var _self = this;
_self.rtnOk = "OK";
_self.rtnMsg = "_MSG_";
_self.rtnErr = "ERROR";
_self.rtnWarn = "WARN";
_self.rtnTime = "_TIME_";
_self.pkKey = "_PK_";


/**
 * firefly对象,平台级缓存和与后台交互方法
 */
var FireFly = {
	contextPath:FireFlyContextPath,  //inHearder.jsp里定义的系统变量
    getContextPath: function() {
        return FireFly.contextPath;
    },
    doAct: function(serviceName, action, serviceParameter, func, async) {
        var ajaxUrl = serviceName + "/" + action + ".do";
        var serviceParameter = serviceParameter || {};
        return rh_processData(ajaxUrl, serviceParameter, func, async);
    }
};


/**
 * 通过ajax方式取得后台数据
 * @param ajaxUrl
 * 参数：请求url地址
 * @param params
 * 参数：请求参数数据
 * @param func 异步执行的方法
 * @param async 是否异步
 * @return
 * 返回值：JSON数据对象
 */
function rh_processData(ajaxUrl, queryParams, func, async) {
    var resultData = new Object();

    var params = jQuery.extend({}, queryParams, {expando:jQuery.expando});
    var tempasync = false;
    if (async) {
    	tempasync = async;
    }
    ajaxUrl = FireFly.getContextPath() + "" + ajaxUrl;
    jQuery.ajax({
        type:"post",
        url:encodeURI(ajaxUrl),
        dataType:"json",
        data:params,
        cache:false,
        async:tempasync,
        timeout:60000,
        success:function(data) {
            resultData = {};
            resultData = data;
            if (typeof data === "string") {//判断返回数据类型
				try {
					resultData = jQuery.parseJSON(data);
					alert(resultData);
				} catch (e) {

				}                
            }
	        
            //根据结果展示消息
//            fe_processMsg(resultData);
            
            if(func) {
            	func.call(this, resultData);
            }
        },
        error:function(err) {
        	debugger;
            resultData = {};
            resultData.exception = err;
            resultData.msg = err.responseText || "error";
            alert(resultData.msg);
//            if(loginJuge(resultData) == true) {
//            	return false;
//            } else {
//            	Debug.add(resultData.msg);
//            	throw new Error(resultData.msg);
//            }
        }
    });
    return resultData;
}