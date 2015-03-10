package com.ustc.dystu.dandelion.bean;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class NoteInfo implements Serializable {

	public String note_id;
	public String note_uid;
	public String note_title;
	public String note_time_from;
	public String note_time_to;
	public String note_location;
	public String note_folder_url;
	public int note_is_suggest;
	public String[] weiboIds;
	public String note_create_at;// 以秒为单位

	public UserInfo userIno;

	public static ArrayList<NoteInfo> create(JSONArray array)
			throws JSONException {
		ArrayList<NoteInfo> list = new ArrayList<NoteInfo>();

		NoteInfo info;
		for (int i = 0; i < array.length(); i++) {
			JSONObject jo = (JSONObject) array.get(i);
			info = create(jo);
			list.add(info);
		}

		return list;
	}

	public static NoteInfo create(JSONObject jo) throws JSONException {
		NoteInfo info = new NoteInfo();
		info.note_id = jo.getString("note_id");
		info.note_uid = jo.getString("note_uid");
		info.note_title = jo.getString("note_title");
		// info.note_time = jo.getString("note_time");
		info.note_location = jo.getString("note_location");
		info.note_folder_url = jo.getString("note_folder_url");
		info.note_is_suggest = jo.getInt("note_is_suggest");
		String weibo_ids = jo.optString("weibo_ids");
		info.weiboIds = getIds(weibo_ids);
		info.note_time_from = jo.getString("note_time_from");
		info.note_time_to = jo.getString("note_time_to");
		info.note_create_at = jo.getString("note_create_at");
		return info;
	}

	private static String[] getIds(String ids) {
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

	public static final SimpleDateFormat sDateFormat = new SimpleDateFormat(
			"yyyy.MM.dd", Locale.CHINA);

	public String getFormatNoteFromTime() {
		if (!TextUtils.isEmpty(note_time_from)) {
			long from = Long.parseLong(note_time_from);

			Date before = new Date(from);

			return sDateFormat.format(before);
		}
		return "";
	}

	public String getTotalDays() {
		if (!TextUtils.isEmpty(note_time_from)
				&& !TextUtils.isEmpty(note_time_to)) {
			long from = Long.parseLong(note_time_from);
			long to = Long.parseLong(note_time_to);

			Date before = new Date(from);
			Date after = new Date(to);

			return getTotalDays(before, after) + "天";
		}

		return "";
	}

	public int getTotalDays(Date before, Date after) {
		if (before.equals(after)) {
			return 1;
		}

		String f = sDateFormat.format(before);
		String c = sDateFormat.format(after);
		try {
			Date first = sDateFormat.parse(f);
			Date current = sDateFormat.parse(c);

			double day = current.getTime() - first.getTime();
			day = day / 1000 / 60 / 60 / 24;

			if (day > 0 && day < 1 && (current.getDate() != first.getDate())) {
				day++;
			}

			day++;
			return (int) day;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (o instanceof NoteInfo) {
			NoteInfo info = (NoteInfo) o;
			return info.note_id.equals(this.note_id);

		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return note_id.hashCode();
	}

	public static Comparator<NoteInfo> comparator = new Comparator<NoteInfo>() {

		@Override
		public int compare(NoteInfo lhs, NoteInfo rhs) {
			long ltime = Long.parseLong(lhs.note_create_at);
			long rtime = Long.parseLong(rhs.note_create_at);

			int delta = (int) (rtime - ltime);
			return delta;
		}
	};

	public String getFirstLocation() {
		if (!TextUtils.isEmpty(note_location)) {
			int i = note_location.indexOf(",");

			if (i > 0) {
				String str = note_location.substring(0, i);
				return subLocation(str);
			}
		}

		return subLocation(note_location);
	}

	public static String subLocation(String location) {
		if (location != null) {
			try {
				location = location.replace("中国", "");
				int startIndex = location.indexOf("省");
				if (startIndex < 0) {
					startIndex = location.indexOf("自治区");

					if (startIndex > 0) {
						startIndex += 3;
					} else {
						startIndex = 0;
					}
				} else {
					startIndex++;
				}

				int index = location.indexOf("市");

				if (index < 0) {
					index = location.indexOf("自治州");
					if (index > 0) {
						index += 2;
					}
				}

				if (index > 0) {
					location = location.substring(startIndex, index + 1);
				}

				return location;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "";
	}
}
