package com.ustc.dystu.dandelion.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * bean类的基类
 * 
 * @author 
 *
 */
public class BaseInfo {

	public int errCode = Integer.MIN_VALUE;
	public String errMsg;

	public static BaseInfo create(JSONObject jobj) throws JSONException {
		BaseInfo info = new BaseInfo();
		info.errCode = jobj.optInt("err_code");
		info.errMsg = jobj.optString("err_msg");
		return info;
	}
}
