package com.ustc.dystu.dandelion.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.ustc.dystu.dandelion.db.DandDB;
import com.ustc.dystu.dandelion.net.DandelionAPI;

import android.content.Context;
import android.text.TextUtils;


public class CacheUtils {

	public static final String API_SERVER = "https://api.weibo.com/2";

	private static final long DEFAULT_EXPIRE_IN = 1 * 24 * 60 * 60 * 1000;// 一天

	public static final String CACHE_SELECTED_NOTES = DandelionAPI.DANDELION_SERVER
			+ "/travel_select.php";
	public static final String CACHE_WEIBO_INFO = API_SERVER
			+ "/statuses/show.json";
	public static final String CACHE_USER_INFO = API_SERVER
			+ "/users/show.json";

	public static void updateCache(Context ctx, String key, String value) {
		if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
			DandDB.getInstance(ctx).updateCache(key, value);
		}
	}

	public static String getCache(Context ctx, String key, long expireIn) {
		if (!TextUtils.isEmpty(key)) {
			Object[] obj = DandDB.getInstance(ctx).getCache(key);

			if (obj != null && obj[0] != null && obj[1] != null) {
				long cacheTime = Long.parseLong((String) obj[1]);

				if (expireIn <= 0) {
					expireIn = DEFAULT_EXPIRE_IN;
				}

				if ((cacheTime + expireIn) > System.currentTimeMillis()) {
					return (String) obj[0];
				}
			}

		}
		return null;
	}

	public static String getKey(String url, String[] params) {
		String target = url;
		if (params != null && params.length > 0) {
			target += "?" + urlencode(params);
		}

		return target;
	}

	public static String urlencode(String[] params) {
		if (params.length % 2 != 0) {
			throw new IllegalArgumentException(
					"Params must have an even number of elements.");
		}

		String result = "";
		try {
			boolean firstTime = true;
			for (int i = 0; i < params.length; i += 2) {
				if (params[i + 1] != null) {
					if (firstTime) {
						firstTime = false;
					} else {
						result += "&";
					}
					result += URLEncoder.encode(params[i], "UTF-8") + "="
							+ URLEncoder.encode(params[i + 1], "UTF-8");
				}
			}
			result.replace("*", "%2A");
		} catch (UnsupportedEncodingException e) {
			return null;
		}

		return result;
	}

}
