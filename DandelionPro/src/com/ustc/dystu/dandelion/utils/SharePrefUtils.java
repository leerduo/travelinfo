package com.ustc.dystu.dandelion.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePrefUtils {

	public static String getUid(Context ctx) {
		SharedPreferences sp = ctx.getSharedPreferences("prefs",
				Context.MODE_PRIVATE);
		return sp.getString("uid", null);
	}

	public static void setUid(Context ctx, String uid) {
		SharedPreferences sp = ctx.getSharedPreferences("prefs",
				Context.MODE_PRIVATE);
		sp.edit().putString("uid", uid).commit();
	}

}
