package com.ustc.dystu.dandelion.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.ustc.dystu.dandelion.utils.Logger;


public class GeoInfo implements Serializable{
	public String type;
	public String longitude;// 经度
	public String latitude;// 纬度

	public static GeoInfo create(JSONObject jo){
		GeoInfo info;
		try {
			info = new GeoInfo();
			info.type = jo.getString("type");
			String coordinates = jo.getString("coordinates"); // [26.66926,100.24685]

			coordinates = coordinates.substring(1, coordinates.length() - 1);

			Logger.d("GeoInfo", "coordinates-->" + coordinates);
			String[] split = coordinates.split(",");

			info.latitude = split[0];
			info.longitude = split[1];
			
			return info;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}
}
