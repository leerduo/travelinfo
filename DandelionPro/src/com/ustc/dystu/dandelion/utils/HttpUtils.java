package com.ustc.dystu.dandelion.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ustc.dystu.dandelion.bean.ChatMessage;
import com.ustc.dystu.dandelion.bean.ChatMessage.Type;
import com.ustc.dystu.dandelion.bean.Result;


public class HttpUtils {

	private static final String URLSTR = "http://www.tuling123.com/openapi/api";
	private static final String API_KEY = "28404da63d36c39b90880a484c37aaff";
	private static InputStream is = null;
	private static ByteArrayOutputStream baos = null;

	
	public static ChatMessage sendMessage(String msg){
		
		ChatMessage chatMessage = new ChatMessage();
		
		String jsonRes = doGet(msg);
	
		Gson gson = new Gson();

		Result result = null;
		
		try {
			result = gson.fromJson(jsonRes, Result.class);
			chatMessage.setMsg(result.getText());
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			chatMessage.setMsg("服务器繁忙，请稍后重试");
		}
		
		chatMessage.setDate(new Date());
		
		chatMessage.setType(Type.INCOMING);
		
		return chatMessage;
	}
	
	
	
	
	
	public static String doGet(String msg) {

		String result = "";

		String url = setParams(msg);

		try {
			URL urlStr = new URL(url);

			HttpURLConnection conn = (HttpURLConnection) urlStr
					.openConnection();

			conn.setReadTimeout(5000);

			conn.setConnectTimeout(5000);

			conn.setRequestMethod("GET");

			is = conn.getInputStream();

			int len = -1;

			byte[] buf = new byte[128];

			baos = new ByteArrayOutputStream();

			while ((len = is.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			baos.flush();
			result = new String(baos.toByteArray());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return result;

	}

	private static String setParams(String msg) {

		String url = null;
		try {
			url = URLSTR + "?key=" + API_KEY + "&info=" + URLEncoder.encode(msg, "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return url;
	}

}
