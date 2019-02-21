package com.rh.core.util.httpclient;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

/**
 * 
 * @author wanglong
 *
 */
public class HttpResponseUtils {
    
    private static final String CHARSET_FLAG = "charset=";
    
    /**
     * 
     * @param response Http请求的响应对象
     * @return resonse中的内容
     * @throws IOException 异常
     */
    public static String getResponseContent(HttpResponse response) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //@TODO 读取response指定的字符编码，按照字符编码转换
        
        String charset = parseCharset(response);
        
        InputStream is = null;
        try {
            is = response.getEntity().getContent();
            IOUtils.copy(is, out);
            byte[] bytes = out.toByteArray();
            
            //返回指定字符集的文字
            return new String(bytes, charset);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(is);
        }

    }
    
    /**
     * 解析ContentType中指定的字符集。如果未指定，默认值为ISO-8859-1
     * @param response Http响应
     * @return  字符集名称
     */
    public static String parseCharset(HttpResponse response) {
        Header[] headers = response.getHeaders("Content-Type");
        String contentType = "";
        for (Header temp : headers) {
            contentType += temp.getValue();
        }
        
        return parseCharset(contentType);
    }
    
    /**
     * 解析ContentType中指定的字符集。如果未指定，默认值为ISO-8859-1
     * @param contentType Http响应指定的字符集
     * @return 字符集名称
     */
    private static String parseCharset(String contentType) {
        // text/html; charset=utf-8
        
        String[] values = contentType.split(";");

        for (String value : values) {
            value = value.trim();
            if (value.startsWith(CHARSET_FLAG)) {
                return value.substring(CHARSET_FLAG.length());
            }
        }

        return "ISO-8859-1";
    }
}
