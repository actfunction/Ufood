package com.rh.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * dom4j的处理类。
 * 
 */
public class Dom4JHelper {
    private static Log log = LogFactory.getLog(Dom4JHelper.class);
    /**
     * @return 创建Dom4j的Document对象
     */
    public static Document createDocument() {
        Document doc = DocumentHelper.createDocument();
        return doc;
    }

    /**
     * 输出Document到指定输出流，XML文件的编码为UTF-8。
     * @param output 输出流
     * @param doc 文档内容
     * @throws IOException 例外
     */
    public static void output(OutputStream output, Document doc) throws IOException {

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        XMLWriter writer = new XMLWriter(output, format);
        writer.write(doc);
    }

    /**
     * 输出Document成字符串，XML文件的编码为UTF-8。
     * @param doc 文档内容
     * @return 字符串
     * @throws IOException 例外
     */
    public static String doc2String(Document doc) throws IOException {
        StringWriter sw = new StringWriter();
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        XMLWriter writer = new XMLWriter(sw, format);
        writer.write(doc);
        return sw.toString();
    }

    /**
     * 输出到文件
     * @param file 文件
     * @param doc 文件内容
     */
    public static void output(File file, Document doc) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output(output, doc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

}
