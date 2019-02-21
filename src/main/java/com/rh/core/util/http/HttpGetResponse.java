package com.rh.core.util.http;

import java.io.InputStream;

import org.apache.http.client.HttpClient;

/**
 * http get response
 * @author wanglong
 */
public class HttpGetResponse {

	/**
	 * can new instance
	 */
	public HttpGetResponse() {

	}

	/**
	 * get inputstream (if it's download file)
	 * @return InputStream
	 */
	public InputStream getInputStream() {
		return is;
	}

	/**
	 * set inputstream
	 * @param targetIs inputstream
	 */
	protected void setInputStream(InputStream targetIs) {
		is = targetIs;
	}

	/**
	 * get response message
	 * @return response message string
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/**
	 * set response body message
	 * @param responseMsg responseMessage
	 */
	protected void setResponseBody(String responseMsg) {
		responseBody = responseMsg;
	}

	/**
	 * get download file name
	 * @return download file name (base firefox )
	 */
	public String getDownloadName() {
		return downloadName;
	}

	/**
	 * set download file name
	 * @param downloadFileName download file name
	 */
	protected void setDownloadName(String downloadFileName) {
		downloadName = downloadFileName;
	}

	/**
	 * get http client
	 * @return http client
	 */
	public HttpClient getClient() {
		return client;
	}

	/**
	 * set http client
	 * @param httpClient http client
	 */
	protected void setHttpClient(HttpClient httpClient) {
		client = httpClient;
	}
	
	/**
	 * get http statusCode
     * @return http statusCode
	 */
	public int getStatusCode() {
        return statusCode;
    }

	/**
	 * * set http statusCode
     * @param statusCode http statusCode
	 */
	protected void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

	/**
	 * close http connection
	 */
	public void closeClient() {
		if (null != client) {
			client.getConnectionManager().shutdown();
		}
	}

	/** inputstream */
	private InputStream is = null;
	/** response message */
	private String responseBody = "";
	/** download file name */
	private String downloadName = "";
	/** httpclient */
	private HttpClient client = null;
	/** status code */
	private int statusCode;
}