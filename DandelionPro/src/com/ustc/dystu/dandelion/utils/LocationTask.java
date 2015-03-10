package com.ustc.dystu.dandelion.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.ustc.dystu.dandelion.bean.FootInfo;
import com.ustc.dystu.dandelion.bean.GoogleGeoInfo;
import com.ustc.dystu.dandelion.net.DandelionAPI;


public class LocationTask {

	private static final int MAX_TASK_NUM = 20;
	private ArrayList<WeakReference<LoactionAsycTask>> mTaskList = new ArrayList<WeakReference<LoactionAsycTask>>();
	private Context mContext;

	public LocationTask(Context ctx) {
		mContext = ctx;
	}

	public void loadLocation(FootInfo geo, TextView tvLocation) {
		final LoactionAsycTask task = new LoactionAsycTask(tvLocation);
		tvLocation.setTag(task);

		executeTask(task, geo);
	}

	private void executeTask(LoactionAsycTask task, Object data) {
		if (mTaskList.size() >= MAX_TASK_NUM) {
			LoactionAsycTask bitmapWorkerTask = mTaskList.get(0).get();
			if (bitmapWorkerTask != null) {
				bitmapWorkerTask.cancel(true);
			}

			mTaskList.remove(0);
		}
		mTaskList.add(new WeakReference<LoactionAsycTask>(task));

		try {
			task.execute(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class LoactionAsycTask extends DandAsyncTask<Object, Void, FootInfo> {
		private FootInfo data;
		private final WeakReference<TextView> textViewReference;

		public LoactionAsycTask(TextView textView) {
			textViewReference = new WeakReference<TextView>(textView);
		}

		@Override
		protected FootInfo doInBackground(Object... params) {
			data = (FootInfo) params[0];

			// 先从微博接口访问地址信息
			try {
				String response = DandelionAPI.getInstance(mContext)
						.getAddress(data.geo);
				if (TextUtils.isEmpty(response)
						|| response.contains("error_code")) {
					JSONObject obj = new JSONObject(response);
					String error = obj.getString("error");

					Logger.d("LocationUtils", "error-->" + error);
				} else {
					JSONObject obj = new JSONObject(response);
					JSONArray array = obj.getJSONArray("geos");
					JSONObject jo = (JSONObject) array.get(0);
					
					data.city = jo.getString("city_name");
					data.province = jo.getString("province_name");
					//data.formate_loaction = jo.getString("address");
					data.formate_loaction = data.province + data.city;
					return data;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			/*// 如果微博接口返回为空, 再从google访问
			try {
				String response = DandelionAPI.getInstance(mContext)
						.getGoogleAddress(data.geo);
				if (response != null) {
					JSONObject jo = new JSONObject(response);
					JSONArray array = jo.getJSONArray("results");

					GoogleGeoInfo info = GoogleGeoInfo.getBestLocation(array);
					if (info != null) {
						data.formate_loaction = info.formatted_address;
						data.city = info.getComponent(GoogleGeoInfo.TYPE_CITY);
						data.city_area = info
								.getComponent(GoogleGeoInfo.TYPE_CITY_AREA);
						data.province = info
								.getComponent(GoogleGeoInfo.TYPE_PROVINCE);
						data.country = info
								.getComponent(GoogleGeoInfo.TYPE_COUNTRY);

						Logger.d("Test", "city:" + data.city + "; province:"
								+ data.province + ";country:" + data.country
								+ "; city_area:" + data.city_area);
						return data;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}*/

			return null;

			// {
			// "geos": [
			// {
			// "longitude": "116.39794",
			// "latitude": "39.90817",
			// "city": "11",
			// "province": "32",
			// "city_name": "北京",
			// "province_name": "朝阳区",
			// "address": "中国北京市海淀区中关村"
			// }
			// ]
			// }
		}

		@Override
		protected void onPostExecute(FootInfo result) {
			// if cancel was called on this task or the "exit early" flag is set
			// then we're done
			if (isCancelled()) {
				result = null;
			}

			final TextView textView = getAttachedTextView();
			if (result != null && textView != null) {
				textView.setText(result.getFormatLoaciton());
			}
		}

		private TextView getAttachedTextView() {
			final TextView textView = textViewReference.get();
			final LoactionAsycTask bitmapWorkerTask = getWorkerTask(textView);

			if (this == bitmapWorkerTask) {
				return textView;
			}

			return null;
		}
	}

	private static LoactionAsycTask getWorkerTask(TextView textView) {
		if (textView != null) {
			final Object task = textView.getTag();
			if (task instanceof LoactionAsycTask) {
				return (LoactionAsycTask) task;
			}
		}
		return null;
	}

}
