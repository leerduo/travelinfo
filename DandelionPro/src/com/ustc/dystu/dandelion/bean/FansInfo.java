package com.ustc.dystu.dandelion.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FansInfo extends UserInfo {

	private static final long serialVersionUID = -6879748846047678980L;
	public boolean isChecked = false; // 用于表示粉丝是否被选中
	public String groupTag;

	public String id;
	public String nickname;
	public String screen_name;
	public String profile_image_url;

	public FansInfo() {

	}

	public FansInfo(String groupTag) {

		this.groupTag = groupTag;
	}

	public static ArrayList<FansInfo> create(JSONArray array)
			throws JSONException {

		ArrayList<FansInfo> list = new ArrayList<FansInfo>();
		FansInfo info;
		JSONObject jo;
		for (int i = 0; i < array.length(); i++) {

			jo = (JSONObject) array.get(i);
			info = new FansInfo();
			
			String uid = jo.optString("id", null);
			if(uid == null) {
				uid = jo.optString("uid", null);
			}
			
			info.id = uid;
			info.screen_name = jo.getString("screen_name");
			info.profile_image_url = jo.getString("profile_image_url");
			list.add(info);
			info = null;
		}

		return list;
	}

	public static ArrayList<FansInfo> sCreate(JSONArray array)
			throws JSONException {

		ArrayList<FansInfo> list = new ArrayList<FansInfo>();
		// {"uid":2171499515,"nickname":"php163","remark":""}
		FansInfo info;
		JSONObject jo;
		for (int i = 0; i < array.length(); i++) {

			jo = (JSONObject) array.get(i);
			info = new FansInfo();
			info.id = jo.getString("uid");
			info.screen_name = jo.getString("nickname");
			list.add(info);
			info = null;
		}

		return list;
	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof FansInfo) {
			FansInfo info = (FansInfo) o;

			if (info.id != null && info.id.equals(this.id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

}
