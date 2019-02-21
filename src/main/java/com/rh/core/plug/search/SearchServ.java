package com.rh.core.plug.search;

import java.util.List;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.OutBean;
import com.rh.core.serv.ParamBean;
import com.rh.core.util.Lang;

/**
 * search service extends <CODE>CommonServ</CODE>
 * 
 */
public class SearchServ extends CommonServ {

    /**
     * 搜索入口页面
     * @param paramBean 传入的参数
     * @return 传出的参数
     */
    public OutBean show(ParamBean paramBean) {
        return getSearchServ().show(paramBean);
    }

    /**
     * 提供基于列表的查询服务
     * 
     * @param paramBean 参数Bean
     * @return 查询结果
     */
    public OutBean query(ParamBean paramBean) {
        return getSearchServ().query(paramBean);
    }

    /**
     * 提供基于facet的分组功能
     * 
     * @param paramBean 参数Bean
     * @return 查询结果
     */
    public OutBean groupBy(ParamBean paramBean) {
        return getSearchServ().groupBy(paramBean);
    }

    /**
     * get data by id
     * @param paramBean param
     * @return Bean out Bean
     */
    public OutBean preview(ParamBean paramBean) {
        return getSearchServ().preview(paramBean);
    }

    /**
     * delete data by id
     * @param paramBean param
     * @return Bean out Bean
     */
    public OutBean delete(ParamBean paramBean) {
        return getSearchServ().delete(paramBean);
    }

    /**
     * get relevant Search
     * @param paramBean param
     * @return Bean out Bean
     */
    public OutBean relevantSearch(ParamBean paramBean) {
        return getSearchServ().relevantSearch(paramBean);
    }
    
    /**
     * 获取搜索服务实例
     * @return 搜索服务
     */
    private static ISearchServ getSearchServ() {
        return (ISearchServ) Lang.createObject(ISearchServ.class, 
                Context.getInitConfig("rh.search", "com.rh.opt.plug.search.RhSearchServ"));
    }

    /**
     * is contains object?
     * @param list list
     * @param filterName target bean's id
     * @return is contains?
     */
    public static boolean contains(List<Bean> list, String filterName) {
        boolean result = false;
        for (Bean bean : list) {
            if (bean.getStr("id").equals(filterName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * is contains date object?
     * @param list list
     * @param datefilter date filter
     * @return is contains date?
     */
    public static boolean containsDateFilter(List<Bean> list, String datefilter) {
        boolean result = false;
        for (Bean bean : list) {
            if (bean.getStr("id").equals("date")) {
                result = bean.getStr("data").equals(datefilter);
                break;
            }
        }
        return result;
    }
    
    /**
     * 返回搜索建议服务的Uri地址
     * @return 搜索建议服务的Uri地址
     */
    public static String getSuggestionUri() {
        String result = "";
        result = Context.getSyConf("SY_PLUG_SEARCH_SUGGEST_SERVER", 
                "http://staff.zotn.com:8888/searchserver/suggestion");
        if (!result.endsWith("/")) {
            result += "/";
        }
        return result;
    }
    
    /**
     * 返回拼写检查服务的Uri地址
     * @return 拼写检查服务的Uri地址
     */
    public static String getSpellcheckUri() {
        String result = "";
        result = Context.getSyConf("SY_PLUG_SEARCH_SPELLCHECK_SERVER", 
                "http://staff.zotn.com:8888/searchserver/spellcheck");
        if (!result.endsWith("/")) {
            result += "/";
        }
        return result;
    }
    

    /** attachment id key */
    public static final String ATTACHMENT_ID = "att_id";

    /** attachment index in the list */
    public static final String ATTACHMENT_INDEX = "att_index";

    /** attachment title key */
    public static final String ATTACHMENT_TITLE = "att_title";

    /** attachment title key */
    public static final String ATTACHMENT_CONTENT = "att_content";

    /** attachment path key */
    public static final String ATTACHMENT_PATH = "att_path";

    /** attachment mime type */
    public static final String ATTACHMENT_MTYPE = ARhIndex.ATTACHMENT_MTYPE;
}
