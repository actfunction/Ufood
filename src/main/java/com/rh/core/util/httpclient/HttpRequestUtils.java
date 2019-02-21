package com.rh.core.util.httpclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.rh.core.serv.OutBean;
import com.rh.core.util.JsonUtils;

/**
 * 
 * @author wanglong
 * 
 */
public class HttpRequestUtils {

    /**
     * @param url 访问web页面的url
     * @return HttpGet 对象
     */
    public static HttpGet createHttpGet(String url) {
        HttpGet post = new HttpGet(url);
        return post;
    }

    /**
     * @param url 请求web地址
     * @param postParams post参数
     * @param encoding post数据编码
     * @return HttpPost 对象
     * @throws IOException 异常
     */
    @SuppressWarnings("rawtypes")
    public static HttpPost createHttpPost(String url,
            Map<String, String> postParams,
            String encoding) throws IOException {
        HttpPost post = new HttpPost(url);

        if (postParams != null) {
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator it = postParams.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                NameValuePair pair = new BasicNameValuePair(key,
                        postParams.get(key));
                list.add(pair);
            }

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,
                    encoding);

            post.setEntity(entity);
        }

        return post;
    }

    /**
     * @param url 访问web页面的url
     * @return url指向页面的HTML内容
     * @throws IOException 异常
     */
    public static String getWebContent(String url) throws IOException {
        HttpClient client = HttpClientUtils.createHttpClient();
        HttpResponse response = client.execute(createHttpGet(url));
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException(response.getStatusLine().toString()
                    + "  url: " + url);
        }

        return HttpResponseUtils.getResponseContent(response);
    }

    /**
     * @param url 请求web地址
     * @param postParam post参数
     * @param endcoding post数据编码
     * @return url指向页面的HTML内容
     * @throws IOException 异常
     */
    public static String getWebContent(String url,
            Map<String, String> postParam,
            String endcoding) throws IOException {
        HttpClient client = HttpClientUtils.createHttpClient();
        HttpResponse response = client.execute(createHttpPost(url,
                postParam, endcoding));
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException(response.getStatusLine().toString() + "  url: "
                    + url + "\r\n" + HttpResponseUtils.getResponseContent(response));
        }

        return HttpResponseUtils.getResponseContent(response);
    }

    /**
     * 执行Post请求
     * 
     * @param post post请求对象
     * @return 返回请求结果
     * @throws IOException IO异常
     */
    public static OutBean execute(HttpPost post) throws IOException {
        HttpClient httpclient = HttpClientUtils.createHttpClient();
        HttpResponse response = httpclient.execute(post);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("操作失败。"
                    + HttpResponseUtils.getResponseContent(response));
        }

        String json = HttpResponseUtils.getResponseContent(response);

        OutBean outBean = new OutBean(JsonUtils.toBean(json));

        if (!outBean.isOk()) {
            throw new IOException("操作失败。" + outBean.getMsg());
        }

        return outBean;
    }
}
