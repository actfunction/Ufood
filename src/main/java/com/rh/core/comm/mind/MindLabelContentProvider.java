package com.rh.core.comm.mind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.org.UserBean;
import com.rh.core.serv.ServDefBean;
import com.rh.core.serv.util.ServConstant;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.JsonUtils;
import com.rh.core.util.freemarker.FreeMarkerUtils;

/**
 * 根据服务定义，获取服务商定义的意见Label，然后向outBean中填充这些意见的内容。
 * @author wanglong
 * 
 */
public class MindLabelContentProvider {
    private UserMind userMind = null;

    /** 服务ID **/
    private String servId = null;

    /** 数据ID **/
    private String dataId = null;
    
    private static final String APP_PATH = Context.appStr(Context.APP.SYSPATH);
    
    private static final String MIND_CONFIG_ERROR = "MIND_CONFIG_ERROR";
    
    private static final String MIND_CONFIG_NOT = "MIND_CONFIG_NOT";
    
    private static final String DEFAULT_VAL = "&nbsp;";

    /**
     * 
     * @param servId 服务ID
     * @param dataId 审批单ID
     */
    public MindLabelContentProvider(String servId, String dataId) {
        this.servId = servId;
        this.dataId = dataId;
    }

    /**
     * @param userBean 用户Bean
     * @param outBean 审批单数据bean
     */
    public void fillData(UserBean userBean, Bean outBean) {
        HashMap<String, Bean> itemMap = filterMindLabelItems(this.servId);
        // 如果没有显示
        if (itemMap.keySet().size() == 0) {
            return;
        }
        
        //初始化用户意见对象
        this.userMind = new UserMind(userBean);
        this.userMind.query(servId, dataId);
        //如果没有意见，则不处理
        if (this.userMind.getMindCount() > 0) {
            //向outBean中，加入意见内容参数
            Iterator<String> itr = itemMap.keySet().iterator();
            while (itr.hasNext()) {
                final String itemCode = itr.next();
                final Bean mindConfig = itemMap.get(itemCode);
                outBean.set(itemCode, getHtmlContent(mindConfig));
            }
        }
        
        //如果没有值，则设置默认值
        Iterator<String> itr = itemMap.keySet().iterator();
        while (itr.hasNext()) {
            final String itemCode = itr.next();
            if (outBean.isEmpty(itemCode)) {
                outBean.set(itemCode, DEFAULT_VAL);
            }
        }
    }

    /**
     * 
     * @param servId 服务ID
     * @return 返回意见标签项的名称和意见CODE
     */
    private HashMap<String, Bean> filterMindLabelItems(String servId) {
        ServDefBean servBean = ServUtils.getServDef(servId);
        HashMap<String, Bean> mindLabelItems = new HashMap<String, Bean>();

        LinkedHashMap<String, Bean> allItems = servBean.getAllItems();
        for (String key : allItems.keySet()) {
            Bean item = allItems.get(key);
            // 如果是label字段，且字段名以"_MIND_"开头, ITEM_TYPE自定义字段，则认为是意见标签字段   
            if (item.getInt("ITEM_TYPE") == ServConstant.ITEM_TYPE_DEFINE
                    && item.getInt("ITEM_INPUT_TYPE") == ServConstant.ITEM_INPUT_TYPE_LABEL
                    && item.getStr("ITEM_CODE").startsWith("_MIND_")) {
                if (item.isNotEmpty("ITEM_INPUT_CONFIG") && !StringUtils.isBlank("ITEM_INPUT_CONFIG")) {
                    String configStr = item.getStr("ITEM_INPUT_CONFIG");
                    
                    if (configStr.indexOf("MIND_CODE") == -1) { //没找到 意见编码
                        mindLabelItems.put(item.getStr("ITEM_CODE"), new Bean().set("MIND_CODE", MIND_CONFIG_ERROR));
                        continue;
                    }
                    
                    Bean configBean = JsonUtils.toBean(configStr);
                    
                    mindLabelItems.put(item.getStr("ITEM_CODE"), configBean);
                } else {
                    mindLabelItems.put(item.getStr("ITEM_CODE"), new Bean().set("MIND_CODE", MIND_CONFIG_NOT));
                }
            }
        }

        return mindLabelItems;
    }

    /**
     * 
     * @param mindConfig 意见配置
     * @return 输出意见编码对应的意见
     */
    private String getHtmlContent(Bean mindConfig) {
        String mindCode = mindConfig.getStr("MIND_CODE");
        
        if (mindCode.equals(MIND_CONFIG_ERROR)) {
            return "<span style='color:red'>服务中配置的意见字段有误，请检查。 </font>";
        } else if (mindCode.equals(MIND_CONFIG_NOT)) {
            return "<span style='color:red'>服务中意见字段没有配置，请检查。</font>";
        }
        
        List<Bean> list = this.userMind.getMindListByMindCode(mindCode);
        
        if (list.size() == 0) {
            return "";
        }
        
        List<Bean> showList = new ArrayList<Bean>();
        if (mindConfig.getStr("SHOW_TYPE").equals("FIRST")) { //最早的那条意见
            showList.add(list.get(list.size() - 1)); 
        } else if (mindConfig.getStr("SHOW_TYPE").equals("LAST")) { //最晚的那条意见
            showList.add(list.get(0));
        } else { //所有意见
            showList = list;
        }
        
        String ftlName = "/sy/comm/mind/labelMindDefault.ftl";
        if (mindConfig.isNotEmpty("TMPL_FTL")) {
            ftlName = mindConfig.getStr("TMPL_FTL");
        }
        
        Bean bean = new Bean();
        bean.set("mindList", showList);
        
        String strHtml = FreeMarkerUtils.parseText(APP_PATH + ftlName, bean);
        return strHtml;
    }
}
