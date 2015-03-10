package com.ustc.dystu.dandelion.bean;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NearInfo implements Serializable {

	public NoteInfo noteInfo;
	public String weiboUid;
	public String weiboId;
	public FootInfo footInfo;

	// {
	// "id": "15",
	// "weibo_uid": "1727404360",
	// "note_id": "24",
	// "weibo_id": "3510081868332557",
	// "lon": "116.310935",
	// "lat": "39.983856",
	// "geohash": "wx4eqwz4c4zp",
	// "note_info": {
	// "note_id": "24",
	// "note_uid": "1727404360",
	// "note_title": "吞吞吐吐他",
	// "note_time": "2012年08月12日 - 2012年11月08日",
	// "note_location": "中国北京市海淀区",
	// "note_folder_url":
	// "http://ww1.sinaimg.cn/large/66f61948jw1dynkg17dqpj.jpg",
	// "note_is_suggest": "1",
	// "weibo_ids": [
	// "3510081868332557",
	// "3478350158710349"
	// ]
	// }
	// }

	public static ArrayList<NearInfo> create(JSONArray array)
			throws JSONException {
		ArrayList<NearInfo> list = new ArrayList<NearInfo>();

		NearInfo info;
		for (int i = 0; i < array.length(); i++) {
			info = new NearInfo();
			JSONObject jo = array.getJSONObject(i);
			info.weiboUid = jo.getString("weibo_uid");
			info.weiboId = jo.getString("weibo_id");

			JSONObject noteJo = jo.getJSONObject("note_info");
			info.noteInfo = NoteInfo.create(noteJo);

			list.add(info);
		}

		return list;
	}

}
