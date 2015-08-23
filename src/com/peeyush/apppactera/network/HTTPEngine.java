package com.peeyush.apppactera.network;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HTTPEngine {

	private static String TAG = "HTTPEngine"; 
	
	public static String GetData(String url){
		
		String response = null;
		try{
			HttpGet httpGet = new HttpGet(url);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("SSLSocketFactory", SSLSocketFactory.getSocketFactory(), 443));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			
			HttpEntity httpEntity = httpResponse.getEntity();
			response = EntityUtils.toString(httpEntity);
			
		}catch(Exception e){
			
			Log.e(TAG, "GetData: " + e.getMessage());
		}
		
		return response;
	}
	
}
