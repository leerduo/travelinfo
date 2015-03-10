package com.ustc.dystu.dandelion.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo extends BaseInfo implements Serializable {
	
	/*各个字段的含义，参见  http://open.weibo.com/wiki/2/users/show  */

	private static final long serialVersionUID = 1L;
	public String id;
	public String screen_name;
	public String name;
	public String province;
	public String city;
	public String location;
	public String description;
	public String url;
	public String profile_image_url;
	public String domain;
	public String gender;
	public String followers_count;
	public String friends_count;
	public String statuses_count;
	public String favourites_count;
	public String created_at;

	public UserInfo(){
	}
	
	public static UserInfo create(JSONObject jsonObject) throws JSONException {

		UserInfo info = new UserInfo();

		String id = jsonObject.optString("id", null);
		if (id != null) {
			info.id = id;
		}

		String screen_name = jsonObject.optString("screen_name");
		if (screen_name != null) {
			info.screen_name = screen_name;
		}
		String name = jsonObject.optString("name");
		if (name != null) {
			info.name = name;
		}
		String province = jsonObject.optString("province");
		if (province != null) {
			info.province = province;
		}
		String city = jsonObject.optString("city");
		if (city != null) {
			info.city = city;
		}
		String location = jsonObject.optString("location");
		if (location != null) {
			info.location = location;
		}
		String description = jsonObject.optString("description");
		if (description != null) {
			info.description = description;
		}
		String profile_image_url = jsonObject.optString("profile_image_url");
		if (profile_image_url != null) {
			info.profile_image_url = profile_image_url;
		}

		return info;
	}

}
