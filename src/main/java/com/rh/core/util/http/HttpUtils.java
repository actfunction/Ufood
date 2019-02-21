package com.rh.core.util.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * http utils
 * @author wanglong
 */
public class HttpUtils {

	/**
	 * @param url target url
	 * @return http get response
	 */
	public static HttpGetResponse httpGet(String url) {
		try {
			// prepare get method
			HttpGet httpget = new HttpGet(url);
			// use firefox user-agent
			httpget.setHeader("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:10.0) Gecko/20100101 Firefox/10.0");

			// create the client and execute the post method
			HttpClient client = new DefaultHttpClient();
			// Execute get
			HttpContext context = new BasicHttpContext();
			HttpResponse getResponse = client.execute(httpget, context);
			if (getResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new IOException(getResponse.getStatusLine().toString() + "  url: " + url);
			}
			HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			String currentUrl = URLDecoder.decode(currentReq.getURI().toString(), "UTF-8");

			int i = currentUrl.lastIndexOf('/');
			String fileName = "";
			if (i < 0) {
				fileName = currentUrl;
			} else {
				fileName = currentUrl.substring(i + 1);
			}
			// set file name
			Header[] contentHeader = getResponse.getHeaders("content-disposition");
			String contentHeaderStr = "";
			for (Header temp : contentHeader) {
				contentHeaderStr += temp.getValue();
			}
			String key = "attachment;filename*=UTF-8''";
			int fileNameIndex = contentHeaderStr.indexOf(key);
			if (-1 < fileNameIndex) {
				fileName = contentHeaderStr.substring(fileNameIndex + key.length());
				fileName = URLDecoder.decode(fileName, "UTF-8");
			}
			InputStream is = getResponse.getEntity().getContent();
			// http get response
			HttpGetResponse response = new HttpGetResponse();
			response.setDownloadName(fileName);
			response.setInputStream(is);
			response.setResponseBody(getResponse.getStatusLine().toString());
			response.setHttpClient(client);
			response.setStatusCode(getResponse.getStatusLine().getStatusCode());

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @param url url
	 * @param is inputstream
	 * @throws IOException in case of a problem or the connection was aborted
	 * @return response string
	 */
	public static String httpPost(String url, InputStream is) throws IOException {
		HttpPost httpost = new HttpPost(url);
		httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

		InputStreamEntity reqEntity = new InputStreamEntity(is, -1);
		reqEntity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
		reqEntity.setChunked(true);
		httpost.setEntity(reqEntity);

		// create the client and execute the post method
		HttpClient client = new DefaultHttpClient();

		// Execute post
		HttpContext context = new BasicHttpContext();
		HttpResponse postResponse = client.execute(httpost, context);
		if (200 != postResponse.getStatusLine().getStatusCode()) {
			throw new IOException(postResponse.getStatusLine().toString());
		}

		String text = IOUtils.toString(postResponse.getEntity().getContent());
		// String msg = postResponse.getStatusLine().toString();
		client.getConnectionManager().shutdown();
		return text;

	}

	/**
	 * 测试方法
	 * @param args 参数
	 */
	public static void main(String[] args) {
		// http get
		/*
		 * String url = "http://localhost:8080/file/0INtAnYd98QHoo94WUhVEI.docx"; try { HttpGetResponse response =
		 * httpGet(url); System.out.println(response.getDownloadName()); } catch (Exception e) { e.printStackTrace(); }
		 */
		// http post
		String url = "http://172.16.0.4:8888/solr-3.6/seg";
		String xml = " <?xml version=\"1.0\" encoding=\"UTF-8\"?><add><word>中国人民</word><word>扩展配置</word></add>";
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		try {
			httpPost(url, is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
