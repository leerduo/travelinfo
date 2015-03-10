package com.ustc.dystu.dandelion.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class GoogleGeoInfo {

	public static final String TYPE_CITY = "locality";
	public static final String TYPE_PROVINCE = "administrative_area_level_1";
	public static final String TYPE_COUNTRY = "country";
	public static final String TYPE_CITY_AREA = "sublocality";

	public String formatted_address;
	public ArrayList<String> types;
	public ArrayList<Components> comps;

	public static GoogleGeoInfo create(JSONObject jo) {
		GoogleGeoInfo info = new GoogleGeoInfo();
		try {
			info.formatted_address = jo.getString("formatted_address");
			String types = jo.optString("types");
			info.types = getTypes(types);

			JSONArray detail = jo.getJSONArray("address_components");
			info.comps = Components.create(detail);

			return info;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<GoogleGeoInfo> create(JSONArray array) {
		ArrayList<GoogleGeoInfo> list = new ArrayList<GoogleGeoInfo>();

		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject jo = (JSONObject) array.get(i);
				list.add(create(jo));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return list;
	}

	public static GoogleGeoInfo getCustomLocation(JSONArray array, String type) {
		ArrayList<GoogleGeoInfo> list = create(array);
		return getCustomLocation(list, type);
	}

	public static GoogleGeoInfo getCustomLocation(
			ArrayList<GoogleGeoInfo> list, String type) {
		if (type != null) {
			for (GoogleGeoInfo googleGeoInfo : list) {
				if (googleGeoInfo.types != null) {
					if (googleGeoInfo.types.contains(type)) {
						return googleGeoInfo;
					}
				}
			}
		}

		if (type != null && type.equals("sublocality")) {
			for (GoogleGeoInfo googleGeoInfo : list) {
				if (googleGeoInfo.types != null) {
					if (googleGeoInfo.types.contains("locality")) {
						return googleGeoInfo;
					}
				}
			}
		}

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	public static GoogleGeoInfo getBestLocation(JSONArray array) {
		ArrayList<GoogleGeoInfo> list = create(array);

		for (GoogleGeoInfo googleGeoInfo : list) {
			if (googleGeoInfo.types != null) {
				if (googleGeoInfo.types.contains("country")
						&& !"中国".equals(googleGeoInfo.formatted_address)) {
					return getCustomLocation(list, "locality");
				}
			}
		}

		return getCustomLocation(list, "sublocality");
	}

	public static ArrayList<String> getTypes(String types) {
		if (types == null) {
			return null;
		}

		try {
			if (!TextUtils.isEmpty(types) && !types.equals("[]")) {
				ArrayList<String> typeList = new ArrayList<String>();

				types = types.substring(1, types.length() - 1);

				String[] str = types.split(",");

				if (str != null) {
					for (int i = 0; i < str.length; i++) {
						str[i] = str[i].substring(1, str[i].length() - 1);
						typeList.add(str[i]);
					}
				}

				return typeList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static class Components {
		public String long_name;
		public String short_name;
		public ArrayList<String> types;

		public static ArrayList<Components> create(JSONArray array)
				throws JSONException {
			ArrayList<Components> list = new ArrayList<GoogleGeoInfo.Components>();

			Components comp;
			for (int i = 0; i < array.length(); i++) {
				comp = new Components();
				JSONObject jo = (JSONObject) array.get(i);

				comp.long_name = jo.optString("long_name");
				comp.short_name = jo.optString("short_name");
				String types = jo.optString("types");
				comp.types = getTypes(types);

				list.add(comp);
			}

			return list;
		}
	}

	public String getComponent(String type) {
		if (comps != null && !comps.isEmpty()) {
			for (Components comp : comps) {
				if (comp.types != null) {
					if (comp.types.contains(type)) {
						return comp.long_name;
					}
				}
			}
		}

		return null;
	}
}
