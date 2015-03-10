package com.ustc.dystu.dandelion.net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.openapi.AbsOpenAPI;
import com.ustc.dystu.dandelion.atk.AccessTokenKeeper;
import com.ustc.dystu.dandelion.bean.FootInfo;
import com.ustc.dystu.dandelion.bean.GeoInfo;
import com.ustc.dystu.dandelion.bean.NearInfo;
import com.ustc.dystu.dandelion.bean.NoteInfo;
import com.ustc.dystu.dandelion.bean.UserInfo;
import com.ustc.dystu.dandelion.constant.Constants;
import com.ustc.dystu.dandelion.utils.CacheUtils;
import com.ustc.dystu.dandelion.utils.Logger;
import com.ustc.dystu.dandelion.utils.SharePrefUtils;



public class DandelionAPI extends AbsOpenAPI{
	
	public static final String DANDELION_SERVER = "http://1.infotravel.sinaapp.com";
	
	private static final String TAG = DandelionAPI.class.getSimpleName();
	
	private static DandelionAPI sInstance;
	private static Oauth2AccessToken sAccessToken;
	

	private static String sUid;
	private static Context sContext;
	
	public synchronized static DandelionAPI getInstance(Context ctx) {
		if (sInstance == null) {
			Oauth2AccessToken accessToken = AccessTokenKeeper
					.readAccessToken(ctx);
			sInstance = new DandelionAPI(accessToken);
			sAccessToken = accessToken;
			sUid = SharePrefUtils.getUid(ctx);
			sContext = ctx;
			Logger.d(TAG, "accessToken-->" + sAccessToken.getToken());
		}

		return sInstance;
	}

	public Oauth2AccessToken getAccessToken() {
		return sAccessToken;
	}

	public String getUid() {
		return sUid;
	}

	public void logout() {
		sInstance = null;
	}

	private DandelionAPI(Oauth2AccessToken oauth2AccessToken) {
		super(sContext, sUid, oauth2AccessToken);
	}
	
	public void getLocationStatus(RequestListener listener, String uid, int page) {
		String url = API_SERVER + "/place/user_timeline.json";
		

		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("uid", uid);
		params.put("count", String.valueOf(50));
		params.put("page", page);

		requestAsync(url, params, "GET", listener);
	}

	public void getUserInfo(DandRequestListener listener, String uid) {
		String url = API_SERVER + "/users/show.json";

		String res = CacheUtils.getCache(
				sContext,
				CacheUtils.getKey(CacheUtils.CACHE_USER_INFO, new String[] {
						"uid", uid }), 3 * 24 * 60 * 60 * 1000);

		if (res != null) {
			listener.onCache(res);
		} else {
			WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
			params.put("uid", uid);
			requestAsync(url, params, "GET", listener);
		}
	}

	public String[] getUserInfo(String uid) throws WeiboException {
		String url = API_SERVER + "/users/show.json";

		String res = CacheUtils.getCache(
				sContext,
				CacheUtils.getKey(CacheUtils.CACHE_USER_INFO, new String[] {
						"uid", uid }), 3 * 24 * 60 * 60 * 1000);

		if (res != null) {
			return new String[] { "true", res };
		}
		
			
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("uid", uid);
		params.put("access_token", sAccessToken.getToken());
		
		
		return new String[] { "false",
				new AsyncWeiboRunner(sContext).request(url, params, "GET") };
	}

	public void getFansList(RequestListener listener, int page, int count) {
		String url = API_SERVER + "/friendships/friends/bilateral.json";

		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("uid", sUid);
		params.put("count", String.valueOf(count));
		params.put("page", String.valueOf(page));

		requestAsync(url, params, "GET", listener);
	}

	public String searchFans(String q, String count) throws WeiboException {
		String url = API_SERVER + "/search/suggestions/at_users.json";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("access_token", sAccessToken.getToken());
		params.put("q", q);
		params.put("count", count);
		params.put("type", "3");
		
		
		return new AsyncWeiboRunner(sContext).request(url, params, "GET");
	}

	public String getAddress(GeoInfo geo) throws WeiboException {
		if (geo == null) {
			return null;
		}
		String url = API_SERVER + "/location/geo/geo_to_address.json";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("access_token", sAccessToken.getToken());
		params.put("coordinate", geo.longitude + "," + geo.latitude);

		return new AsyncWeiboRunner(sContext).request(url, params, "GET");
	}

	/*public String getGoogleAddress(GeoInfo geo) throws WeiboException {
		if (geo == null) {
			return null;  
		}

		//   http://ditu.google.com/maps/api/geocode/json
		String url = "http://ditu.google.com/maps/api/geocode/json";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("latlng", geo.latitude + "," + geo.longitude);
		params.put("language", "zh-CN");
		params.put("sensor", false);

		Logger.d(TAG, "lat:" + geo.latitude + "; long:" + geo.longitude);
		
		

		return new AsyncWeiboRunner(sContext).request(url, params, "GET");
	}*/
	
	
	public void getSelectTravelNotes(DandRequestListener listener,
			boolean useCache) {
		String url = DANDELION_SERVER + "/travel_select.php";

		String res = null;
		if (useCache) {
			res = CacheUtils
					.getCache(sContext, CacheUtils.getKey(
							CacheUtils.CACHE_SELECTED_NOTES, null), 0);
		}

		if (res != null) {
			listener.onCache(res);
		} else {
			WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
			requestAsync(url, params, "GET", listener);
		}
	}

	public void getLatestTravelNotes(RequestListener listener, int num) {
		String url = "http://1.infotravel.sinaapp.com/travel_latest.php";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("note_num", num);
		requestAsync(url, params, "GET", listener);
	}

	public void getTravelNotes(RequestListener listener, String uid) {
		String url = "http://1.infotravel.sinaapp.com/travel_me.php";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("uid", uid);
		requestAsync(url, params, "POST", listener);
	}

	public void getWeiboBatchInfo(RequestListener listener, String weiboIds) {
		// 需要高级别的权限
		String url = API_SERVER + "/statuses/show_batch.json";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("ids", weiboIds);
		params.put("trim_user", "1");
		requestAsync(url, params, "GET", listener);
	}

	public ArrayList<FootInfo> getWeiboInfo(String[] weiboIds) {
		ArrayList<FootInfo> list = new ArrayList<FootInfo>();

		FootInfo info;
		for (int i = 0; i < weiboIds.length; i++) {
			try {
				String[] rs = getWeiboInfo(weiboIds[i]);

				if (rs != null) {
					JSONObject jo = new JSONObject(rs[1]);
					info = FootInfo.create(jo, true);
					list.add(info);

					if ("false".equals(rs[0])) {
						CacheUtils.updateCache(sContext, CacheUtils.getKey(
								CacheUtils.CACHE_WEIBO_INFO, new String[] {
										"id", weiboIds[i] }), rs[1]);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public String[] getWeiboInfo(String weiboId) throws WeiboException {
		String url = API_SERVER + "/statuses/show.json";

		String res = CacheUtils.getCache(
				sContext,
				CacheUtils.getKey(CacheUtils.CACHE_WEIBO_INFO, new String[] {
						"id", weiboId }), 3 * 24 * 60 * 60 * 1000);

		if (res != null) {
			return new String[] { "true", res };
		}

		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("id", weiboId);
		params.put("access_token", sAccessToken.getToken());
		return new String[] { "false",
				new AsyncWeiboRunner(sContext).request(url, params, "GET") };
		
	}

	public void createTravelNote(RequestListener listener, NoteInfo info,
			String weiboInfo) {
		String url = "http://1.infotravel.sinaapp.com/travel_create.php";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("note_folder_url", info.note_folder_url);
		params.put("note_is_suggest", info.note_is_suggest);
		params.put("note_location", info.note_location);
		params.put("note_title", info.note_title);
		params.put("note_uid", info.note_uid);
		params.put("note_time_from", info.note_time_from);
		params.put("note_time_to", info.note_time_to);

		params.put("weiboInfo", weiboInfo);
		requestAsync(url, params, "POST", listener); 
	}

	public ArrayList<NearInfo> getNearTravelNote(double lat, double lon) {
		String url = "http://1.infotravel.sinaapp.com/travel_near.php";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("lat", String.valueOf(lat));
		params.put("lon", String.valueOf(lon));

		ArrayList<NearInfo> data;
		try {
			String response = new AsyncWeiboRunner(sContext).request(url, params, "GET");
			JSONArray array = new JSONArray(response);
			data = NearInfo.create(array);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (data != null) {
			for (NearInfo nearInfo : data) {
				try {
					String[] rs = getWeiboInfo(nearInfo.weiboId);

					if (!"[]".equals(rs) && rs != null) {
						JSONObject jo = new JSONObject(rs[1]);
						nearInfo.footInfo = FootInfo.create(jo, true);

						if ("false".equals(rs[0])) {
							CacheUtils.updateCache(sContext, CacheUtils.getKey(
									CacheUtils.CACHE_WEIBO_INFO, new String[] {
											"id", nearInfo.weiboId }), rs[1]);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}

		return data;
	}

	public NoteInfo getRandomTravelNote() {
		String url = "http://1.infotravel.sinaapp.com/random_note_info.php";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		try {
			String rs = new AsyncWeiboRunner(sContext).request(url, params, "GET");

			NoteInfo info = NoteInfo.create(new JSONObject(rs));

			String[] userInfo = getUserInfo(info.note_uid);

			if (userInfo != null) {
				info.userIno = UserInfo.create(new JSONObject(userInfo[1]));

				if ("false".equals(userInfo[0])) {
					CacheUtils.updateCache(sContext, CacheUtils.getKey(
							CacheUtils.CACHE_USER_INFO, new String[] { "uid",
									info.note_uid }), userInfo[1]);
				}
			}

			return info;
		} catch (WeiboException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void deleteNote(RequestListener listener, NoteInfo info) {
		String url = "http://1.infotravel.sinaapp.com/travel_delete.php";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("note_id", info.note_id);
		requestAsync(url, params, "POST", listener);
	}

	public void updateNote(RequestListener listener, NoteInfo info) {
		String url = "http://1.infotravel.sinaapp.com/travel_update.php";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);  

		Logger.d(TAG, "note titl-->" + info.note_title);

		params.put("note_id", info.note_id);
		params.put("note_title", info.note_title);
		params.put("note_folder_url", info.note_folder_url);
		requestAsync(url, params, "POST", listener);
	}

	public void getComments(RequestListener listener, String weiboId) {
		String url = API_SERVER + "/comments/show.json";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("id", weiboId);
		requestAsync(url, params, "GET", listener);
	}

	public void setComments(RequestListener listener, String weiboId,
			String comment) {
		String url = API_SERVER + "/comments/create.json";
		WeiboParameters params = new WeiboParameters(Constants.APP_KEY);
		params.put("id", weiboId);
		params.put("comment", comment);
		requestAsync(url, params, "POST", listener);
	}
}
