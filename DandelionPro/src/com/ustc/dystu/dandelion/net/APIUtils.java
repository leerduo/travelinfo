package com.ustc.dystu.dandelion.net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.ustc.dystu.dandelion.bean.FootInfo;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.SharePrefUtils;

public class APIUtils {

	// 构建JSON字符串
	public static String buildJson(ArrayList<FootInfo> footList, int noteId,
			Context ctx){
		try {
			JSONArray json = new JSONArray();
			JSONObject jsonObj;

			String weiboUid = SharePrefUtils.getUid(ctx);

			for (FootInfo info : footList) {
				jsonObj = new JSONObject();
				jsonObj.put("note_id", noteId);
				jsonObj.put("weibo_id", info.id);
				jsonObj.put("weibo_uid", weiboUid);
				jsonObj.put("lon", info.geo.longitude);
				jsonObj.put("lat", info.geo.latitude);
				json.put(jsonObj);
			}

			String jsondata = json.toString();
			Logger.d("Test", "json-->" + jsondata);
			return jsondata;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
