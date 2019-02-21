/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */
package com.rh.core.serv.base;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Bean;
import com.rh.core.base.BeanUtils;
import com.rh.core.base.Context;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.dict.DictMgr;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.Lang;

/**
 * 服务基础类，不需要表单服务的自定义类可以继承自这个类。
 * 
 * @author wanglong
 */
public abstract class BaseServ {

    /**
     * 记录日志信息的函数
     */
    protected Log log = LogFactory.getLog(this.getClass());

    /**
     * 提供服务相关设定信息
     * @param paramBean 参数Bean
     * @return 服务相关设定信息
     */
    public OutBean serv(ParamBean paramBean) {
        String servId = paramBean.getServId();
        ServDefBean servDef = ServUtils.getServDef(servId);
        if (paramBean.getBoolean("full")) {
            return new OutBean(servDef);
        }
        OutBean outBean = new OutBean();
        BeanUtils.trans(servDef, outBean, new String[] { "SERV_ID", "SERV_NAME", "SERV_PID", "SERV_TYPE",
                "SERV_LIST_STYLE", "SERV_PAGE_COUNT", "SERV_JS", "SERV_LIST_LOAD", "SERV_CARD_LOAD",
                "SERV_CARD_STYLE", "SERV_CARD_JSP", "SERV_FILE_FLAG", "SERV_DELETE_FLAG",
                "SERV_NAV_ITEMS", "SERV_KEYS", "SERV_MIND_LABLE_FLAG", "SERV_MOBILE_JS",
                "SERV_LIST_LOAD_NAMES", "SERV_CARD_LOAD_NAMES", "SERV_SRC_ID",
                "SERV_COMMENT_FLAG", "SERV_RELATE_IDS", "SERV_CARD_TMPL", "SERV_QUERY_MODE", "SERV_RED_HEAD"});
        Map<String, Bean> items = servDef.getAllItems(); // 所有有效项
        Bean dicts = new Bean();
        for (String key : items.keySet()) {
            Bean itemBean = items.get(key);
            String dictId = itemBean.getStr("DICT_ID");
            if (dictId.length() > 0 && !dicts.contains(dictId)) { // 预处理字典项
                Bean dict = DictMgr.getDict(dictId);
                if (dict != null && dict.getInt("DICT_TYPE") == DictMgr.DIC_TYPE_LIST) {
                    // 传递所有字典数据供列表及卡片页面共同使用
                    dicts.set(dictId, DictMgr.getTreeList(dictId));
                }
            }
        }
        //设定了卡片模版，但是没有模版内容，说明是调试模式，动态获取模版
        if (servDef.getCardTmplName().length() > 0) {
            outBean.set("SERV_CARD_TMPL_CONTENT", servDef.getCardTmplContent());
        }
        outBean.set("DICTS", dicts);
        outBean.set("ITEMS", items);
        outBean.set("LINKS", servDef.getAllLinks());
        outBean.set("BTNS", servDef.getAllActs());
        outBean.set("QUERIES", servDef.getAllQueries());
        return outBean;
    }

    /**
     * 执行按钮处理前的规则表达式验证
     * @param servDef 服务定义
     * @param act 操作编码
     * @param dataBean 数据信息
     */
    protected void checkActExpression(ServDefBean servDef, String act, Bean dataBean) {
        Bean actDef = servDef.getAct(act);
        if (actDef != null && actDef.isNotEmpty("ACT_EXPRESSION")) { // 设了表达式处理，且没有设定忽略验证
            String script = actDef.getStr("ACT_EXPRESSION");
            int pos = script.indexOf(";"); // 如果存在前后端表达式个性化规则设定，只启用后端规则
            if (pos >= 0) {
                script = script.substring(pos + 1);
            }
            if (script.length() > 0) {
                script = ServUtils.replaceSysAndData(script, dataBean);
                if (!Lang.isTrueScript(script)) {
                    throw new RuntimeException(Context.getSyMsg("SY_SERV_ACT_INVALID")
                            + dataBean.getId() + ": " + script);
                }
            }
        }
    }

    /**
     * 设置信息
     * @deprecated 推荐使用outBean.setMgr(String msg);
     * @param bean 输出Bean
     * @param msg 信息内容
     */
    protected void setMsg(Bean bean, String msg) {
        bean.set(Constant.RTN_MSG, msg);
    }
    
    /**
     * 设置缺省的成功信息
     * @deprecated 推荐使用outBean.setOk();
     * @param bean 输出Bean
     */
    protected void setOk(Bean bean) {
        setOk(bean, "");
    }

    /**
     * 设置成功信息
     * @deprecated 推荐使用outBean.setOk(String msg);
     * @param bean 输出Bean
     * @param msg 信息内容，可以为空
     */
    protected void setOk(Bean bean, String msg) {
        bean.set(Constant.RTN_MSG, Constant.RTN_MSG_OK + msg);
    }

    /**
     * 设置缺省的警告信息
     * @deprecated 推荐使用outBean.setWarn();
     * @param bean 输出Bean
     */
    protected void setWarn(Bean bean) {
        setWarn(bean, "");
    }

    /**
     * 设置警告信息
     * @deprecated 推荐使用outBean.setWarn(String msg);
     * @param bean 输出Bean
     * @param msg 信息内容，可以为空
     */
    protected void setWarn(Bean bean, String msg) {
        bean.set(Constant.RTN_MSG, Constant.RTN_MSG_WARN + msg);
    }

    /**
     * 设置缺省的失败信息
     * @deprecated 推荐使用outBean.setError();
     * @param bean 输出Bean
     */
    protected void setError(Bean bean) {
        setError(bean, "");
    }

    /**
     * 设置失败信息
     * @deprecated 推荐使用outBean.setError(String msg);
     * @param bean 输出Bean
     * @param msg 信息内容，可以为空
     */
    protected void setError(Bean bean, String msg) {
        bean.set(Constant.RTN_MSG, Constant.RTN_MSG_ERROR + msg);
    }

    /**
     * 获取执行是否成功
     * @deprecated 推荐使用outBean.isOk();
     * @param bean 输出信息
     * @return 是否执行成功
     */
    protected boolean isOk(Bean bean) {
        return bean.getStr(Constant.RTN_MSG).startsWith(Constant.RTN_MSG_OK) ? true : false;
    }
    
    /**
     * 获取执行是否有警告或者成功信息
     * @deprecated 推荐使用outBean.isOkOrWarn();
     * @param bean 输出信息
     * @return 是否有警告或者成功信息
     */
    protected boolean isOkOrWarn(Bean bean) {
        return bean.getStr(Constant.RTN_MSG).startsWith(Constant.RTN_MSG_ERROR) ? false : true;
    }

}
