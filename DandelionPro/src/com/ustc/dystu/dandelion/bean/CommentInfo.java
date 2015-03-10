package com.ustc.dystu.dandelion.bean;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ustc.dystu.dandelion.utils.Utils;


public class CommentInfo {
	public String created_at;
	public String id;
	public String text;
	public UserInfo userInfo;
	
	public static ArrayList<CommentInfo> create(JSONArray array) {
		ArrayList<CommentInfo> list = new ArrayList<CommentInfo>();
		
		try {
			CommentInfo info;
			for(int i=0;i<array.length();i++) {
				JSONObject jo = array.getJSONObject(i);
				info = create(jo);
				list.add(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	public static CommentInfo create(JSONObject jo) {
		try {
			CommentInfo info = new CommentInfo();
			info.created_at = jo.getString("created_at");
			info.id = jo.getString("id");
			info.text = jo.getString("text");
			info.userInfo = UserInfo.create(jo.getJSONObject("user"));
			
			return info;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	static final DateFormat dateFormat = new SimpleDateFormat(
			"EEE MMM dd kk:mm:ss ZZZ yyyy", Locale.US);
	
	public String getFormatTime() {
		try {
			Date date = dateFormat.parse(created_at);
			return Utils.getTimeBefore(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static Comparator<CommentInfo> comparator = new Comparator<CommentInfo>() {

		@Override
		public int compare(CommentInfo lhs, CommentInfo rhs) {
			BigInteger lid = new BigInteger(lhs.id);
			BigInteger rid = new BigInteger(rhs.id);

			return rid.compareTo(lid);
		}
	};
	
//	{
//	    "comments": [
//	        {
//	            "created_at": "Wed Jun 01 00:50:25 +0800 2011",
//	            "id": 12438492184,
//	            "text": "love your work.......",
//	            "source": "<a href="http://weibo.com" rel="nofollow">新浪微博</a>",
//	            "mid": "202110601896455629",
//	            "user": {
//	                "id": 1404376560,
//	                "screen_name": "zaku",
//	                "name": "zaku",
//	                "province": "11",
//	                "city": "5",
//	                "location": "北京 朝阳区",
//	                "description": "人生五十年，乃如梦如幻；有生斯有死，壮士复何憾。",
//	                "url": "http://blog.sina.com.cn/zaku",
//	                "profile_image_url": "http://tp1.sinaimg.cn/1404376560/50/0/1",
//	                "domain": "zaku",
//	                "gender": "m",
//	                "followers_count": 1204,
//	                "friends_count": 447,
//	                "statuses_count": 2908,
//	                "favourites_count": 0,
//	                "created_at": "Fri Aug 28 00:00:00 +0800 2009",
//	                "following": false,
//	                "allow_all_act_msg": false,
//	                "remark": "",
//	                "geo_enabled": true,
//	                "verified": false,
//	                "allow_all_comment": true,
//	                "avatar_large": "http://tp1.sinaimg.cn/1404376560/180/0/1",
//	                "verified_reason": "",
//	                "follow_me": false,
//	                "online_status": 0,
//	                "bi_followers_count": 215
//	            },
//	            "status": {
//	                "created_at": "Tue May 31 17:46:55 +0800 2011",
//	                "id": 11488058246,
//	                "text": "求关注。"，
//	                "source": "<a href="http://weibo.com" rel="nofollow">新浪微博</a>",
//	                "favorited": false,
//	                "truncated": false,
//	                "in_reply_to_status_id": "",
//	                "in_reply_to_user_id": "",
//	                "in_reply_to_screen_name": "",
//	                "geo": null,
//	                "mid": "5612814510546515491",
//	                "reposts_count": 8,
//	                "comments_count": 9,
//	                "annotations": [],
//	                "user": {
//	                    "id": 1404376560,
//	                    "screen_name": "zaku",
//	                    "name": "zaku",
//	                    "province": "11",
//	                    "city": "5",
//	                    "location": "北京 朝阳区",
//	                    "description": "人生五十年，乃如梦如幻；有生斯有死，壮士复何憾。",
//	                    "url": "http://blog.sina.com.cn/zaku",
//	                    "profile_image_url": "http://tp1.sinaimg.cn/1404376560/50/0/1",
//	                    "domain": "zaku",
//	                    "gender": "m",
//	                    "followers_count": 1204,
//	                    "friends_count": 447,
//	                    "statuses_count": 2908,
//	                    "favourites_count": 0,
//	                    "created_at": "Fri Aug 28 00:00:00 +0800 2009",
//	                    "following": false,
//	                    "allow_all_act_msg": false,
//	                    "remark": "",
//	                    "geo_enabled": true,
//	                    "verified": false,
//	                    "allow_all_comment": true,
//	                    "avatar_large": "http://tp1.sinaimg.cn/1404376560/180/0/1",
//	                    "verified_reason": "",
//	                    "follow_me": false,
//	                    "online_status": 0,
//	                    "bi_followers_count": 215
//	                }
//	            }
//	        },
//	        ...
//	    ],
//	    "previous_cursor": 0,
//	    "next_cursor": 0,
//	    "total_number": 7
//	}
}
