package com.rh.core.util.httpclient;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * @author wanglong
 */
public class HttpClientUtils {

    /**
     * @param connTimeout 连接服务器超时时间，单位为秒，一般为3秒以内。
     * @param soTimeout 返回数据超时时间。视具体情况而定，不宜太长，普通请求为5秒中以内。
     * @return 默认的HttpParams参数
     */
    public static HttpParams createHttpParams(int connTimeout, int soTimeout) {
        HttpParams params = new BasicHttpParams();
        // 连接服务器超时时间：3秒钟
        HttpConnectionParams.setConnectionTimeout(params, connTimeout * 1000);
        // 5分钟没返回则超时
        HttpConnectionParams.setSoTimeout(params, soTimeout * 1000);
        StringBuilder agent = new StringBuilder();
        agent.append("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:10.0) ");
        agent.append(" Gecko/20100101 Firefox/10.0");
        HttpProtocolParams.setUserAgent(params, agent.toString());

        return params;
    }

    /**
     * 创建默认HttpClient参数：3秒中连接服务器超时，5分钟响应超时
     * @return 默认的HttpParams参数
     */
    public static HttpParams createDefaultHttpParams() {
        return createHttpParams(3, 5 * 60);
    }

    /**
     * @return HttpClient 对象
     */
    public static HttpClient createHttpClient() {
        HttpClient client = new DefaultHttpClient(
                HttpClientUtils.createDefaultHttpParams());

        return client;
    }

    /**
     * 
     * @param httpParams HttpClient参数
     * @return HttpClient 对象
     */
    public static HttpClient createHttpClient(HttpParams httpParams) {
        HttpClient client = new DefaultHttpClient(httpParams);

        return client;
    }

}
