package com.rh.core.util.freemarker;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

/**
 * 执行指定的freemarker模板，并返回html结果。
 * 
 * @author wanglong
 */
public class FreeMarkerTransfer {
    
    private static final Map<String, Configuration> CONFIG_MAP = new ConcurrentHashMap<String, Configuration>();
    
    /**
     * 需要传递给FreeMarker的参数
     */
    private Map<Object, Object> params = new HashMap<Object, Object>();
    
    /**
     * freemarker模板所在的目录
     */
    private String templateDir = "";
    
    /**
     * @param tmplDir 模板文件的路径
     */
    public FreeMarkerTransfer(String tmplDir) {
        templateDir = tmplDir;
    }
    
    /**
     * 增加需要传递给模板的变量，这些变量可以直接在模板中调用
     * 
     * @param name 变量的名称。
     * @param var 变量对应的对象。
     */
    public void addVariable(String name , Object var) {
        params.put(name, var);
    }
    
    /**
     * 将freemarker文件的执行结果输出到writer对象中
     * 
     * @param writer 输出
     * @param template freemarker文件
     */
    public void write(Writer writer , String template) {
        try {
            getConfig(templateDir).getTemplate(template).process(params, writer);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * 将freemarker文件的执行结果输出成
     * 
     * @param template 模板名称
     * @return freemarker文件的执行结果
     */
    public String write2Str(String template) {
        StringWriter writer = new StringWriter();
        this.write(writer, template);
        return writer.toString();
    }
    
    /**
     * 设置传递给FreeMarker的参数。
     * 
     * @param bean 传递给FreeMarker的参数
     */
    public void setParams(Map<Object, Object> bean) {
        this.params = bean;
    }
    
    /**
     * 根据指定目录从缓存中取得Configuration对象，如果缓存中不存在，则重新创建。
     * 
     * @param tmplDir ftl模板所在的路径（先对于操作系统的绝对路径）。
     * @return 取得FreeMarker的配置文件
     * @throws Exception 构造config对象时产生的错误
     */
    private static Configuration getConfig(String tmplDir) throws Exception {
        Configuration config = CONFIG_MAP.get(tmplDir);
        if (config != null) {
            return config;
        }
        
        config = new Configuration();
        config.setObjectWrapper(new DefaultObjectWrapper());
        config.setDirectoryForTemplateLoading(new File(tmplDir));
//        config.setEncoding(Locale.CHINA, "UTF-8");
        config.setDefaultEncoding("UTF-8");
        CONFIG_MAP.put(tmplDir, config);
        
        return config;
    }
}
