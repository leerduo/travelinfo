package com.ustc.dystu.dandelion.bean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ustc.dystu.dandelion.utils.Logger;

import android.text.TextUtils;


public class FootInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	public String id;
	public String text;
	public GeoInfo geo;
	public int comments_count;

	public String created_at;
	public String[] picIds;
	public String thumbnail_pic;
	public String bmiddle_pic;
	public String original_pic;
	public String defaultPicId;

	public String formate_loaction;
	public String city;
	public String province;
	public String country;
	public String city_area;// 区县

	public int headerId;
	public boolean isChecked = false;
	public Date date;

	public UserInfo userInfo;

	// "annotations": [
	// {
	// "place": {
	// "poiid": "B209465CD66EA3F9429E",
	// "title": "丽江三义机场",
	// "lon": 100.24685,
	// "lat": 26.66926,
	// "type": "checkin"
	// }
	// }
	// ],

	// "thumbnail_pic":
	// "http://ww3.sinaimg.cn/thumbnail/590473f6jw1dwmgp7b8tjj.jpg",
	// "bmiddle_pic":
	// "http://ww3.sinaimg.cn/bmiddle/590473f6jw1dwmgp7b8tjj.jpg",
	// "original_pic": "http://ww3.sinaimg.cn/large/590473f6jw1dwmgp7b8tjj.jpg",

	// "pic_ids": [
	// "43653df6jw1eabqyexqw7j20hs0dcdh8",
	// "43653df6jw1eabqyhdbc8j20hs0dcgmq",
	// "43653df6jw1eabqyio9o6j20hs0dc0tn",
	// "43653df6jw1eabqywiuodj20hs0dcq44",
	// "43653df6jw1eabqz6tszuj20hs0dct9p"
	// ],

	public static ArrayList<FootInfo> create(JSONArray array, int size)
			throws JSONException {

		ArrayList<FootInfo> list = new ArrayList<FootInfo>();
		FootInfo info;
		FootInfo preInfo = null;

		int currentHeaderId = size;

		JSONObject jo;
		for (int i = 0; i < array.length(); i++) {
			jo = (JSONObject) array.get(i);
			info = new FootInfo();

			String date = jo.getString("created_at");
			info.date = getDate(date);

			if (info.date != null) {
				info.created_at = getFormateTime(info.date);
			}

			info.id = jo.getString("id");
			info.text = weiboTextFilter(jo.optString("text"));
			info.comments_count = jo.optInt("comments_count", 0);

			try {
				info.geo = GeoInfo.create(jo.getJSONObject("geo"));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			info.thumbnail_pic = jo.optString("thumbnail_pic");
			info.bmiddle_pic = jo.optString("bmiddle_pic");
			info.original_pic = jo.optString("original_pic");
			try {
				if (!TextUtils.isEmpty(info.original_pic)) {
					info.defaultPicId = info.original_pic.substring(
							info.original_pic.lastIndexOf("/") + 1,
							info.original_pic.lastIndexOf("."));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			String picIds = jo.optString("pic_ids");
			info.picIds = getPicIds(picIds);

			if (preInfo != null) {
				if (info.created_at != null && preInfo.created_at != null) {
					if (!info.created_at.equals(preInfo.created_at)) {
						currentHeaderId = i + size;
					}
				}
			}

			preInfo = info;

			info.headerId = currentHeaderId;

			list.add(info);
		}

		return list;
	}

	// "pic_urls": [
	// {
	// "thumbnail_pic":
	// "http://ww3.sinaimg.cn/thumbnail/66f61948jw1e61z5gfubxj218g0xcq8w.jpg"
	// },
	// {
	// "thumbnail_pic":
	// "http://ww4.sinaimg.cn/thumbnail/66f61948jw1e61z5kx45sj218g0xcwkv.jpg"
	// }
	// ],
	public static FootInfo create(JSONObject jo, boolean needUserInfo)
			throws JSONException {
		FootInfo info = new FootInfo();
		String date = jo.getString("created_at");
		info.date = getDate(date);

		if (info.date != null) {
			info.created_at = getFormateTime(info.date);
		}

		info.id = jo.getString("id");
		info.text = weiboTextFilter(jo.optString("text"));
		info.comments_count = jo.optInt("comments_count", 0);

		JSONArray array = jo.getJSONArray("pic_urls");
		info.picIds = makePicIds(array);

		try {
			info.geo = GeoInfo.create(jo.getJSONObject("geo"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		info.thumbnail_pic = jo.optString("thumbnail_pic");
		info.bmiddle_pic = jo.optString("bmiddle_pic");
		info.original_pic = jo.optString("original_pic");

		try {
			if (!TextUtils.isEmpty(info.original_pic)) {
				info.defaultPicId = info.original_pic.substring(
						info.original_pic.lastIndexOf("/") + 1,
						info.original_pic.lastIndexOf("."));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (needUserInfo) {
			JSONObject userJo = jo.getJSONObject("user");
			UserInfo user = UserInfo.create(userJo);
			info.userInfo = user;
		}

		return info;
	}

	private static String[] makePicIds(JSONArray array) {
		try {
			String[] str = new String[array.length()];
			for (int i = 0; i < array.length(); i++) {
				JSONObject j = array.getJSONObject(i);
				String url = j.getString("thumbnail_pic");
				if (!TextUtils.isEmpty(url)) {
					String id = url.substring(url.lastIndexOf("/") + 1,
							url.lastIndexOf("."));
					str[i] = id;

					Logger.d("FootInfo", "make pic ids-->" + id);
				}
			}

			return str;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static String[] getPicIds(String ids) {
		Logger.d("FootInfo", "pic ids-->" + ids);
		try {
			if (!TextUtils.isEmpty(ids) && !ids.equals("[]")) {
				ids = ids.substring(1, ids.length() - 1);

				String[] str = ids.split(",");

				if (str != null) {
					for (int i = 0; i < str.length; i++) {
						str[i] = str[i].substring(1, str[i].length() - 1);
					}
				}

				return str;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void copy(FootInfo src, FootInfo target) {
		target.bmiddle_pic = src.bmiddle_pic;
		target.city = src.city;
		target.city_area = src.city_area;
		target.country = src.country;
		target.created_at = src.created_at;
		target.formate_loaction = src.formate_loaction;
		target.geo = src.geo;
		target.headerId = src.headerId;
		target.id = src.id;
		target.original_pic = src.original_pic;
		target.picIds = src.picIds;
		target.province = src.province;
		target.text = src.text;
		target.thumbnail_pic = src.thumbnail_pic;
		target.date = src.date;
		target.comments_count = src.comments_count;
	}

	public static FootInfo copy(FootInfo src) {
		FootInfo target = new FootInfo();

		target.bmiddle_pic = src.bmiddle_pic;
		target.city = src.city;
		target.city_area = src.city_area;
		target.country = src.country;
		target.created_at = src.created_at;
		target.formate_loaction = src.formate_loaction;
		target.geo = src.geo;
		target.headerId = src.headerId;
		target.id = src.id;
		target.original_pic = src.original_pic;
		target.picIds = src.picIds;
		target.province = src.province;
		target.text = src.text;
		target.thumbnail_pic = src.thumbnail_pic;
		target.date = src.date;
		target.defaultPicId = src.defaultPicId;
		target.comments_count = src.comments_count;

		return target;
	}

	public static final SimpleDateFormat sDateFormat = new SimpleDateFormat(
			"yyyy年MM月dd日 EEE", Locale.CHINA);

	public static final SimpleDateFormat sDateFormat2 = new SimpleDateFormat(
			"yyyy年MM月dd日", Locale.CHINA);

	public static final SimpleDateFormat sDateFormat3 = new SimpleDateFormat(
			"MM月dd日", Locale.CHINA);

	// 2012.04.22 22:09
	public static final SimpleDateFormat sDateFormat4 = new SimpleDateFormat(
			"kk:mm", Locale.CHINA);

	public static final SimpleDateFormat sDateFormat5 = new SimpleDateFormat(
			"yyyy.MM.dd kk:mm", Locale.CHINA);

	static final DateFormat dateFormat = new SimpleDateFormat(
			"EEE MMM dd kk:mm:ss ZZZ yyyy", Locale.US);

	private static Date getDate(String dateStr) {
		// "Thu Nov 07 00:11:26 +0800 2013"
		try {
			Date date = dateFormat.parse(dateStr);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static String getFormateTime(Date date) {
		return sDateFormat.format(date);
	}

	private static String weiboTextFilter(String text) {
		if (text != null) {
			int index = text.indexOf("我在:http://");

			if (index > 0) {
				return text.substring(0, index);
			} else {
				index = text.indexOf("我在这里:http://");
				if (index > 0) {
					return text.substring(0, index);
				}
			}
		}

		return text;
	}

	@Override
	public int hashCode() {
		return (id + original_pic).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (o instanceof FootInfo) {
			FootInfo info = (FootInfo) o;

			if (info.id != null && info.defaultPicId != null) {
				return info.id.equals(id)
						&& info.defaultPicId.equals(defaultPicId);
			} else if (info.id != null) {
				return info.id.equals(id);
			}

			return false;
		} else {
			return false;
		}
	}

	public int getPicNum() {
		if (picIds != null) {
			return picIds.length;
		}

		return 0;
	}

	public String getFormatTime() {
		String time = "";

		if (date.getYear() == new Date().getYear()) {
			time = FootInfo.sDateFormat3.format(date);
		} else {
			time = FootInfo.sDateFormat2.format(date);
		}
		return time;
	}

	public String getDetailFormatTime() {
		return FootInfo.sDateFormat4.format(date);
	}

	public String getDetailFormatTime2() {
		return FootInfo.sDateFormat5.format(date);
	}

	public static String getTimeSort(Date firstDay, Date currentDay) {
		if (currentDay != null && firstDay != null) {
			String f = sDateFormat2.format(firstDay);
			String c = sDateFormat2.format(currentDay);
			try {
				Date first = sDateFormat2.parse(f);
				Date current = sDateFormat2.parse(c);

				double day = current.getTime() - first.getTime();
				day = day / 1000 / 60 / 60 / 24;

				if (day > 0 && day < 1
						&& (current.getDate() != first.getDate())) {
					day++;
				}

				day++;

				String num = "第" + (int) day + "天";
				return num;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	public String getFormatLoaciton() {
		if (formate_loaction != null) {
			if (!formate_loaction.contains("中国")) {
				return formate_loaction;
			} else {
				String location = formate_loaction.replace("中国", "");

				if (location.length() > 12) {

					if (province != null && city_area != null) {
						location = province + city_area;
					}
				}

				return location;
			}
		}

		return formate_loaction;
	}
}
