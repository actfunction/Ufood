package com.rh.oss;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Test {
 public static void main(String[] args) {
	HttpPost httpPost  = new HttpPost("http://localhost:8888/file/oss.do?fileId=37WuZ0z49blGXuAnkTavdg.doc");
	CloseableHttpClient client = null;
    client = HttpClients.createDefault();
	try {
		HttpResponse httpResponse = client.execute(httpPost);
	} catch (ClientProtocolException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
