package com.rh.sup.util;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 立项单下载，导出Word
 */
public class OfficeWord extends CommonServ {

    private static Configuration configuration = null;
    //导出字段
    private static String[] strings = {"ITEM_NUM","SUPERV_ITEM","APPR_DATE","LIMIT_DATE","ITEM_SOURCE","ISSUE_CODE","ISSUE_DEPT"
            ,"CENTER_DENOTE","LEAD_DENOTE","DEPT_DENOTE","DIRECT","DIRECT_NAME_ONE","DIRECT_NAME_TWO","DIRECT_PHONE"
            ,"OTHER_DIRECT","OTHER_DIRECT_NAME_ONE","OTHER_DIRECT_NAME_TWO","OTHER_DIRECT_PHONE","JOINTLY"
            ,"JOINTLY_NAME_ONE","JOINTLY_NAME_TWO","JOINTLY_PHONE","REMARK","OFFICE_OVERSEER",
            "OFFICE_OVERSEER_TEL"};

    /**
     * 导出方法
     * @param list
     * @param ITEM_NUM
     */
    public static void createWord(List<Bean> list,String ITEM_NUM) {
        configuration = new Configuration();
        configuration.setDefaultEncoding("UTF-8");
        Map<String, Object> dataMap = new HashMap<String, Object>();
        //获取导出字段
        getData(dataMap,list);
        //获取导出流
        HttpServletResponse response = Context.getResponse();
        HttpServletRequest request = Context.getRequest();
        response.resetBuffer();
        response.setContentType("application/x-msdownload");
        //在流中创建Word
        com.rh.core.util.RequestUtils.setDownFileName(request, response, ITEM_NUM+"立项单.doc");
        Template t ;
        Writer out = null;
        try {
            //引入模板
            configuration.setDirectoryForTemplateLoading(new File(request.getRealPath("/oa/imp_template")));
            t = configuration.getTemplate("督查立项单下载.ftl");
            out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
            //将数据传递给低层导出并生成Word
            t.process(dataMap, out);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out!=null){
                    out.close();
                }
                if (response != null) {
                    response.flushBuffer();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理导出字段
     * @param dataMap
     * @param list
     */
    private static void getData(Map<String, Object> dataMap,List<Bean> list) {
        for (int i = 0 ; i < strings.length ; i++ ){
            dataMap.put(strings[i],list.get(0).getStr(strings[i]));
        }
    }
}
