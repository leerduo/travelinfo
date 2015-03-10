package com.ustc.dystu.dandelion.utils;

import java.util.Date;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Utils {

	public static void share(Context ctx) {
		try {
			Intent intent = new Intent(Intent.ACTION_SEND);

			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "蒲公英");
			intent.putExtra(Intent.EXTRA_TEXT,
					"5秒钟取回你的游记，旅游要低调，分享要奢华，制作游记就用蒲公英。 http://t.cn/8kAQZvD ");
			ctx.startActivity(Intent.createChooser(intent, "分享"));
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(ctx, "没有找到相关应用!", Toast.LENGTH_SHORT).show();
		}
	}

	public static String getTimeBefore(Date date) {
		long delta = System.currentTimeMillis() - date.getTime();

		long min = delta / (1000 * 60);
		if (min < 1) {
			return "刚刚";
		}

		long hour = min / 60;
		if (hour < 24) {
			return hour + "小时前";
		}

		long day = hour / 24;
		if (day < 7) {
			return day + "天前";
		}

		long week = day / 7;
		if (week < 5) {
			return week + "周前";
		}

		long month = day / 30;
		if (month < 12) {
			return month + "月前";
		}

		long year = month / 12;
		return year + "年前";
	}

	public static long getWeiboTextLength(String text) {

		double len = 0;
		for (int i = 0; i < text.length(); i++) {
			int temp = (int) text.charAt(i);
			if (temp > 0 && temp < 127) {
				len += 0.5;
			} else {
				len++;
			}
		}
		return Math.round(len);
	}

}
