/*
 * Copyright (c) 2012 Ruaho All rights reserved.
 */
package com.rh.core.plug.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rh.core.base.Context;
import com.rh.core.util.http.HttpUtils;

/**
 * Ruaho file service client
 * 
 * @author wanglong
 * 
 */
public class RhFileClient {

    private static RhFileClient instance = new RhFileClient();

    /** Ruaho file server expresstion */
    public static final String FILE_SERVER_EXPR = "${rh.file.server}";

    private static final String RHFILE_SERVER = "SY_PLUG_SEARCH_FILE_SERVER";

    private static final String RHFILE_REDIRECT_SERVER = "SY_PLUG_SEARCH_FILE_REDIRECT_SERVER";

    /**
     * can not new instance
     */
    private RhFileClient() {

    }

    /**
     * singleton
     * @return instance
     */
    public static RhFileClient getInstance() {
        return instance;
    }

    /** log */
    private static Log log = LogFactory.getLog(RhFileClient.class);

    /**
     * @param is inputstream
     * @param suffix file suffix
     * @return file url
     * @throws IOException upload error
     */
    public String upload(InputStream is, String suffix) throws IOException {
        String url = getServerUri() + "/upload" + "?suffix=" + suffix;
        String resultUrl = "";
        try {
            resultUrl = HttpUtils.httpPost(url, is);
        } catch (IOException e) {
            log.error("upload file error.", e);
            throw e;
        }
        return resultUrl;
    }

    /**
     * @param src - inputstream
     * @return content text
     * @throws IOException - io exception
     */
    public String extract(InputStream src) throws IOException {
        String url = getServerUri() + "/extract";
        String text = HttpUtils.httpPost(url, src);
        text = URLDecoder.decode(text, "UTF-8");
        return text;
    }

    /**
     * make a snappshot image and return it's url
     * @param src - inputstream
     * @param name - file name (operation)
     * @return snapshot - image's url
     * @throws IOException - io Exception
     */
    public String snapshot(InputStream src, String name) throws IOException {
        if (null == name) {
            name = "";
        } else {
            name = name.replace(" ", "+");
        }

        String url = getServerUri();
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += "snapshot" + "?name=" + name;
        String text = HttpUtils.httpPost(url, src);
        return text;
    }

    /**
     * get preview file cached url?
     * @param fileChecksum - checksum
     * @return - str preview url
     */
    public String getPreviewUrl(String fileChecksum) {
        String server = getServerUri();
        String url = server + "getPreview?checksum=" + fileChecksum;
        return HttpUtils.httpGet(url).getResponseBody();
    }
    
    /**
     * get preview status code
     * @param fileChecksum - checksum
     * @param isMobile 是否移动版
     * @return - preview status code
     */
    public int getPreviewStatusCode(String fileChecksum, boolean isMobile) {
        String server = getServerUri();
        String url = server + "getPreview?checksum=" + fileChecksum;
        if (isMobile) {
            url += "&previewType=mobile";
        }
        return HttpUtils.httpGet(url).getStatusCode();
    }

    /**
     * to html preview and return it's url
     * @param src - inputstream
     * @param name - file name (operation)
     * @param isMobile - 是否为手机版
     * @return preview - html's url
     * @throws IOException - io Exception
     */
    public String preview(InputStream src, String name, boolean isMobile) throws IOException {
        if (null == name) {
            name = "";
        } else {
            name = name.replace(" ", "+");
        }

        String url = getServerUri();
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += "preview" + "?name=" + name;
        if (isMobile) {
            url += "&previewType=mobile";
        }
        String text = HttpUtils.httpPost(url, src);
        return text;
    }

    /**
     * get client redirect server uri
     * @return uri
     */
    public String getRedirectServerUri() {
        String result = Context.getSyConf(RHFILE_REDIRECT_SERVER, "");
        if (0 == result.length()) {
            result = getServerUri();
        }
        return result;
    }

    /**
     * get file server uri
     * 
     * @return server uri
     */
    public String getServerUri() {
        // if(true) return "http://localhost:1897/file/";
        String result = "";
        result = Context.getSyConf(RHFILE_SERVER, "http://staff.zotn.com:8888/searchserver/file/");
        if (!result.endsWith("/")) {
            result += "/";
        }
        return result;
    }

}
