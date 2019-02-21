package com.rh.core.comm.cms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rh.core.base.Bean;
import com.rh.core.util.Lang;

public class CmsHelper {

    private static final String PARAM_SERV_ID = "servId";
    
    private static final String PARAM_DATA_ID = "dataId";
    
    private static final String PARAM_PAGE = "page";
    
     /**
      * parse uri
      * @param relativePath - relative path
      * @return parse result bean
      */
    public static Bean parseUri(String relativePath) {
         Bean result = null;
         String patternStr = relativePath;
         if (patternStr.startsWith("/")) {
             patternStr = patternStr.substring(1);
         }
         String[] pattern = patternStr.split("/");
         if (0 == pattern.length || 3 < pattern.length) {
             return result;
         }
         result = new Bean();
         String servId = pattern[0];
         String dataId = pattern[1].replace(".html", "");
         int page = 1;
         if (pattern.length == 3) {
             String pageStr = Lang.subString(pattern[2], "index_", ".html");
             try {
             page = Integer.valueOf(pageStr);
             } catch (Exception e) {
                 page = 1;
             }
         }     
         result.set(PARAM_SERV_ID, servId);
         result.set(PARAM_DATA_ID, dataId);
         result.set(PARAM_PAGE, page);
         return result;
     }
    
    /**
     * get serv id param
     * @param bean - param bean
     * @return servid
     */
    public static String getServId(Bean bean) {
        return  bean.getStr(PARAM_SERV_ID);
    }
    /**
     * get data id param
     * @param bean - param bean
     * @return dataid
     */
    public static String getDataId(Bean bean) {
        return  bean.getStr(PARAM_DATA_ID);
    }
    
    /**
     * get page param
     * @param bean - param bean
     * @return page
     */
    public static int getPage(Bean bean) {
        return  bean.getInt(PARAM_PAGE);
    }
    
    

    
    /**
     * @param request 请求头
     * @param response 响应头
     * @param message - error msg
     * @throws IOException IOException
     * @return html string
     */
    public static String buildErrorPage(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(404);
        // response.sendError(404);
        String errorHtml = "<html>" + "<br><br><br>" + message + "</html>";
        response.getOutputStream().write(errorHtml.getBytes());
        response.getOutputStream().flush();
        return errorHtml;
    }

  
    /**
     * get resource from request uri
     * @param request - HttpRequest
     * @return resource string
     * @throws UnsupportedEncodingException - url decode exception
     */
    public static String getResource(HttpServletRequest request) throws UnsupportedEncodingException {
        String uri = request.getRequestURI();
        uri = URLDecoder.decode(uri, "UTF-8");
        String servlet = request.getServletPath();
        int index = uri.indexOf(servlet);
        String relativePath = uri.substring(index + servlet.length());
        if (!relativePath.startsWith("/")) {
            relativePath = "";
        }
        return relativePath;
    }

    
}
