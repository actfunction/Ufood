package com.rh.core.util.freemarker;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.util.ServUtils;
import com.rh.core.util.Constant;
import com.rh.core.util.file.FileHelper;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;

/**
 * freeMaker模版处理工具类
 * 
 * @author wanglong
 * @version $Id$
 */
public class FreeMarkerUtils {

    /** 自定义模版名称的键值 */
    private static final String ITEM_FTL = "$ITEM_FTL";
    /**
     * 用于解析字符串模板
     */
    private static final Configuration STRING_CONFIG = new Configuration();
    static {
        STRING_CONFIG.setDefaultEncoding("UTF-8");
    }

    /**
     * 根据模版名称和实际数据解析生成html文本
     * @param fileName 模版名称
     * @param data 数据
     * @return html文本
     */
    public static String parseText(String fileName, Bean data) {

        if (StringUtils.isEmpty(fileName)) {
            return "<font color='#F00'>Template not found.</font>";
        }

        File file = new File(fileName);

        FreeMarkerTransfer trans = new FreeMarkerTransfer(file.getParent());
        trans.setParams(data);
        return trans.write2Str(file.getName());
    }

    /**
     * 根据模板内容和实际数据解析生成html文本
     * @param ftlContent 模板内容
     * @param data 数据
     * @return html文本
     */
    public static String parseString(String ftlContent, Map<String, Object> data) {
        // 设置一个字符串模板加载器
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        STRING_CONFIG.setTemplateLoader(stringLoader);

        stringLoader.putTemplate("", ftlContent);
        StringWriter writer = new StringWriter();
        try {
            // 获取匿名模板
            STRING_CONFIG.getTemplate("").process(data, writer);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return writer.toString();
    }

    /**
     * 获取服务项自定义模版解析生成的html文本
     * @param servDef 服务定义
     * @param itemBean 服务项定义
     * @param data 数据信息
     * @return html文本
     */
    public static String parseItemText(Bean servDef, Bean itemBean, Bean data) {
        if (itemBean.isEmpty(ITEM_FTL)) {
            String ftlName = getItemFtlName(servDef, itemBean.getStr("ITEM_CODE"));
            itemBean.set(ITEM_FTL, ftlName);
        }
        if (!itemBean.isEmpty(ITEM_FTL)) {
            return parseText(itemBean.getStr(ITEM_FTL), data);
        } else {
            return "";
        }
    }

    /**
     * 获取服务项对应的模版文件名称
     * @param servDef 服务定义
     * @param itemCode 服务项编码
     * @return 模版文件名称
     */
    private static String getItemFtlName(Bean servDef, String itemCode) {
        String servId = servDef.getId();
        int pos = servId.indexOf("_");
        String module = pos > 0 ? servId.substring(0, pos) : servId;
        StringBuilder path = new StringBuilder(Context.appStr(Context.APP.SYSPATH));
        path.append(module.toLowerCase()).append(File.separator);
        path.append("ftl").append(File.separator);
        StringBuilder ftlName = new StringBuilder(servId).append(Constant.CODE_PATH_SEPERATOR)
                .append(itemCode).append(".ftl");
        path.append(ftlName);
        if (FileHelper.exists(path.toString())) {
            return path.toString();
        } else if (!servDef.isEmpty("SERV_PID")) {
            return getItemFtlName(ServUtils.getServDef(servDef.getStr("SERV_PID")), itemCode);
        } else {
            return "";
        }
    }
}
